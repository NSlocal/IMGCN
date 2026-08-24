package com.universal.performance;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

public final class MainActivity extends Activity {

    private TextView statusText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        statusText = findViewById(R.id.statusText);

        Button startButton = findViewById(R.id.startButton);
        Button stopButton = findViewById(R.id.stopButton);

        startButton.setOnClickListener(v -> {
            requestNotificationPermissionIfNeeded();
            startPerformanceService();
        });

        stopButton.setOnClickListener(v -> {
            stopPerformanceService();
        });
    }

    private void requestNotificationPermissionIfNeeded() {

        if (Build.VERSION.SDK_INT >= 33) {

            if (checkSelfPermission(
                    Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {

                requestPermissions(
                    new String[] {
                        Manifest.permission.POST_NOTIFICATIONS
                    },
                    100
                );
            }
        }
    }

    private void startPerformanceService() {

        Intent intent =
            new Intent(this, PerformanceService.class);

        if (Build.VERSION.SDK_INT >= 26) {
            startForegroundService(intent);
        } else {
            startService(intent);
        }

        statusText.setText(
            R.string.service_enabled
        );
    }

    private void stopPerformanceService() {

        Intent intent =
            new Intent(this, PerformanceService.class);

        stopService(intent);

        statusText.setText(
            R.string.service_disabled
        );
    }
}
