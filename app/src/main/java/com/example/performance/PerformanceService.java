package com.example.performance;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class PerformanceService extends Service {
    private static final String CHANNEL = "performance";
    private static final int ID = 1201;

    private static final String ENABLE = "performance.ENABLE";
    private static final String DISABLE = "performance.DISABLE";

    private boolean enabled = true;

    @Override
    public void onCreate() {
        super.onCreate();
        createChannel();
        startForeground(ID, buildNotification());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            if (ENABLE.equals(intent.getAction())) enabled = true;
            if (DISABLE.equals(intent.getAction())) enabled = false;
        }
        updateNotification();

        // No polling loop, wake lock, overclocking, process injection,
        // or GPU/CPU bypass. Keep the service stable and low overhead.
        return START_STICKY;
    }

    private Notification buildNotification() {
        Intent on = new Intent(this, PerformanceService.class);
        on.setAction(ENABLE);
        Intent off = new Intent(this, PerformanceService.class);
        off.setAction(DISABLE);

        PendingIntent onPending = PendingIntent.getService(
                this, 1, on,
                PendingIntent.FLAG_UPDATE_CURRENT |
                        PendingIntent.FLAG_IMMUTABLE);
        PendingIntent offPending = PendingIntent.getService(
                this, 2, off,
                PendingIntent.FLAG_UPDATE_CURRENT |
                        PendingIntent.FLAG_IMMUTABLE);

        return new Notification.Builder(this, CHANNEL)
                .setSmallIcon(android.R.drawable.ic_menu_manage)
                .setContentTitle("Performance service")
                .setContentText(enabled
                        ? "UI/GPU optimization profile active"
                        : "Optimization profile disabled")
                .setOngoing(true)
                .addAction(new Notification.Action.Builder(
                        android.graphics.drawable.Icon.createWithResource(
                                this, android.R.drawable.ic_media_play),
                        "Enable", onPending).build())
                .addAction(new Notification.Action.Builder(
                        android.graphics.drawable.Icon.createWithResource(
                                this, android.R.drawable.ic_media_pause),
                        "Disable", offPending).build())
                .build();
    }

    private void updateNotification() {
        NotificationManager manager =
                getSystemService(NotificationManager.class);
        if (manager != null) manager.notify(ID, buildNotification());
    }

    private void createChannel() {
        NotificationChannel channel = new NotificationChannel(
                CHANNEL, "Performance service",
                NotificationManager.IMPORTANCE_LOW);
        channel.setDescription("Low-overhead performance profile");
        NotificationManager manager =
                getSystemService(NotificationManager.class);
        if (manager != null) manager.createNotificationChannel(channel);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
