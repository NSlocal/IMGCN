package com.universal.performance;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {
    private static final int PERMISSION_REQUEST = 2001;

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        Window window = getWindow();
        window.setFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
                WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            WindowManager.LayoutParams p = window.getAttributes();
            p.preferredRefreshRate = 120.0f;
            window.setAttributes(p);
        }

        // Request POST_NOTIFICATIONS on Android 13+ if needed. If we must request,
        // postpone starting the PerformanceService until we get the permission result.
        if (Build.VERSION.SDK_INT >= 33 &&
            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS},
                    PERMISSION_REQUEST);
            return; // wait for permission result before starting the service
        }

        // If we already have permission (or on older platforms), start the service now.
        startPerformanceService();
    }

    private void startPerformanceService() {
        try {
            Intent service = new Intent(this, PerformanceService.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                startForegroundService(service);
            else startService(service);
        } catch (Throwable t) {
            // Avoid letting service startup exceptions crash the app process.
            // Log to console (adb logcat) for debugging.
            t.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST) {
            // Regardless of grant/deny, continue and start the service in a safe way.
            startPerformanceService();
        }
        // Other permission requests (optional permissions) are handled elsewhere.
        // If you want the optional-permissions flow to also start the service, add handling here.
    }

    public void requestOptionalPermissions() {
        if (Build.VERSION.SDK_INT < 23) return;
        List<String> p = new ArrayList<>();
        String[] optional = {
            Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO,
            Manifest.permission.READ_PHONE_STATE, Manifest.permission.READ_CALENDAR,
            Manifest.permission.WRITE_CALENDAR,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
        };
        for (String x : optional)
            if (checkSelfPermission(x) != PackageManager.PERMISSION_GRANTED) p.add(x);

        if (Build.VERSION.SDK_INT >= 31) {
            if (checkSelfPermission(Manifest.permission.BLUETOOTH_SCAN)
                    != PackageManager.PERMISSION_GRANTED)
                p.add(Manifest.permission.BLUETOOTH_SCAN);
            if (checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT)
                    != PackageManager.PERMISSION_GRANTED)
                p.add(Manifest.permission.BLUETOOTH_CONNECT);
        }
        if (!p.isEmpty())
            requestPermissions(p.toArray(new String[0]), PERMISSION_REQUEST + 1);
    }
}
