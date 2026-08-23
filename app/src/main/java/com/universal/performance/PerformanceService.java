package com.universal.performance;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

public final class PerformanceService extends Service {

    private static final String TAG = "UniversalPerformance";
    private static final String CHANNEL_ID = "universal_performance";
    private static final int NOTIFICATION_ID = 12026;

    private static final String ACTION_ENABLE =
            "com.universal.performance.ENABLE";
    private static final String ACTION_DISABLE =
            "com.universal.performance.DISABLE";

    private boolean enabled = true;

    @Override
    public void onCreate() {
        super.onCreate();

        try {
            createNotificationChannel();
            startForeground(NOTIFICATION_ID, buildNotification());

        } catch (SecurityException e) {
            Log.e(TAG, "Foreground service permission/type rejected", e);
            stopSelf();

        } catch (RuntimeException e) {
            Log.e(TAG, "Foreground service startup failed", e);
            stopSelf();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (intent != null) {
            String action = intent.getAction();

            if (ACTION_ENABLE.equals(action)) {
                enabled = true;
            } else if (ACTION_DISABLE.equals(action)) {
                enabled = false;
            }
        }

        updateNotification();

        // No busy loop, permanent WakeLock, overclocking,
        // process injection, or modification of another app.
        return START_STICKY;
    }

    private Notification buildNotification() {

        Intent on = new Intent(this, PerformanceService.class);
        on.setAction(ACTION_ENABLE);

        Intent off = new Intent(this, PerformanceService.class);
        off.setAction(ACTION_DISABLE);

        PendingIntent enable = PendingIntent.getService(
                this, 1, on,
                PendingIntent.FLAG_UPDATE_CURRENT |
                        PendingIntent.FLAG_IMMUTABLE);

        PendingIntent disable = PendingIntent.getService(
                this, 2, off,
                PendingIntent.FLAG_UPDATE_CURRENT |
                        PendingIntent.FLAG_IMMUTABLE);

        Notification.Builder builder =
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                        ? new Notification.Builder(this, CHANNEL_ID)
                        : new Notification.Builder(this);

        builder.setSmallIcon(R.drawable.ic_service)
                .setContentTitle("Universal Performance")
                .setContentText(enabled
                        ? "Performance active"
                        : "Performance disabled")
                .setOngoing(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            builder.addAction(new Notification.Action.Builder(
                    android.graphics.drawable.Icon.createWithResource(
                            this, android.R.drawable.ic_media_play),
                    "Enable", enable).build());

            builder.addAction(new Notification.Action.Builder(
                    android.graphics.drawable.Icon.createWithResource(
                            this, android.R.drawable.ic_media_pause),
                    "Disable", disable).build());
        }

        return builder.build();
    }

    private void updateNotification() {
        try {
            NotificationManager manager =
                    getSystemService(NotificationManager.class);

            if (manager != null) {
                manager.notify(NOTIFICATION_ID, buildNotification());
            }

        } catch (RuntimeException e) {
            Log.e(TAG, "Notification update failed", e);
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return;

        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "Performance Service",
                NotificationManager.IMPORTANCE_LOW);

        channel.setDescription("Low-overhead performance service");

        NotificationManager manager =
                getSystemService(NotificationManager.class);

        if (manager != null) {
            manager.createNotificationChannel(channel);
        }
    }

    @Override
    public void onDestroy() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                stopForeground(STOP_FOREGROUND_REMOVE);
            }
        } catch (RuntimeException e) {
            Log.e(TAG, "stopForeground failed", e);
        }

        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
