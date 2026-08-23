package com.universal.performance;

import android.app.*;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

public class PerformanceService extends Service {
    private static final String TAG = "PerformanceService";
    private static final String CHANNEL_ID = "universal_performance";
    private static final int ID = 12026;
    private static final String ENABLE = "ENABLE";
    private static final String DISABLE = "DISABLE";
    private boolean enabled = true;

    @Override public void onCreate() {
        super.onCreate();
        try {
            createChannel();
            startForeground(ID, buildNotification());
        } catch (SecurityException e) {
            Log.e(TAG, "Foreground-service permission/type rejected", e);
            stopSelf();
        } catch (RuntimeException e) {
            Log.e(TAG, "Foreground-service startup failed", e);
            stopSelf();
        }
    }

    @Override public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            if (ENABLE.equals(intent.getAction())) enabled = true;
            if (DISABLE.equals(intent.getAction())) enabled = false;
        }
        updateNotification();
        return START_STICKY;
    }

    private Notification buildNotification() {
        Intent on = new Intent(this, PerformanceService.class).setAction(ENABLE);
        Intent off = new Intent(this, PerformanceService.class).setAction(DISABLE);

        PendingIntent a = PendingIntent.getService(
            this, 1, on,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        PendingIntent b = PendingIntent.getService(
            this, 2, off,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Notification.Builder n = Build.VERSION.SDK_INT >= 26
            ? new Notification.Builder(this, CHANNEL_ID)
            : new Notification.Builder(this);

        return n.setSmallIcon(R.drawable.ic_service)
            .setContentTitle("Universal Performance")
            .setContentText(enabled ? "Performance service active"
                                    : "Performance service disabled")
            .setOngoing(true)
            .addAction(new Notification.Action.Builder(
                android.graphics.drawable.Icon.createWithResource(
                    this, android.R.drawable.ic_media_play),
                "Enable", a).build())
            .addAction(new Notification.Action.Builder(
                android.graphics.drawable.Icon.createWithResource(
                    this, android.R.drawable.ic_media_pause),
                "Disable", b).build())
            .build();
    }

    private void updateNotification() {
        try {
            NotificationManager m =
                getSystemService(NotificationManager.class);
            if (m != null) m.notify(ID, buildNotification());
        } catch (RuntimeException e) {
            Log.e(TAG, "Notification update failed", e);
        }
    }

    private void createChannel() {
        if (Build.VERSION.SDK_INT < 26) return;
        NotificationChannel c = new NotificationChannel(
            CHANNEL_ID, getString(R.string.service_channel),
            NotificationManager.IMPORTANCE_LOW);
        c.setDescription(getString(R.string.service_description));
        NotificationManager m =
            getSystemService(NotificationManager.class);
        if (m != null) m.createNotificationChannel(c);
    }

    @Override public void onDestroy() {
        if (Build.VERSION.SDK_INT >= 24) stopForeground(STOP_FOREGROUND_REMOVE);
        super.onDestroy();
    }

    @Override public IBinder onBind(Intent intent) { return null; }
}
