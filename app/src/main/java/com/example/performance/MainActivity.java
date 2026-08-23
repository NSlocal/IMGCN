package com.example.performance;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

public class MainActivity extends Activity {
    private static final int PERMISSION_REQUEST = 700;

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);

        Window window = getWindow();
        window.setFlags(
                WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
                WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);

        // Prefer 120 Hz; Android/device policy may choose a lower supported rate.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            WindowManager.LayoutParams lp = window.getAttributes();
            lp.preferredRefreshRate = 120.0f;
            window.setAttributes(lp);
        }

        requestRelevantPermissions();

        Intent service = new Intent(this, PerformanceService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(service);
        } else {
            startService(service);
        }
    }

    private void requestRelevantPermissions() {
        if (Build.VERSION.SDK_INT < 23) return;

        java.util.ArrayList<String> permissions =
                new java.util.ArrayList<>();

        if (Build.VERSION.SDK_INT >= 33 &&
                checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
                        != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS);
        }

        // Keep feature permissions optional. Request them only if the
        // corresponding feature is enabled by the real application.
        if (!permissions.isEmpty()) {
            requestPermissions(
                    permissions.toArray(new String[0]),
                    PERMISSION_REQUEST);
        }
    }
}
