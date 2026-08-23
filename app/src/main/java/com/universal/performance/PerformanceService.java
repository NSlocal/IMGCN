package com.universal.performance;

import android.app.*;
import android.content.Intent;
import android.os.IBinder;
import android.graphics.drawable.Icon;

public class PerformanceService extends Service {
    private static final String CHANNEL = "universal_performance";
    private static final int ID = 12026;
    private static final String ENABLE = "ENABLE";
    private static final String DISABLE = "DISABLE";
    private boolean enabled = true;

    @Override public void onCreate() {
        super.onCreate();
        createChannel();
        startForeground(ID, notification());
    }

    @Override public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            if (ENABLE.equals(intent.getAction())) enabled = true;
            if (DISABLE.equals(intent.getAction())) enabled = false;
        }
        NotificationManager m = getSystemService(NotificationManager.class);
        if (m != null) m.notify(ID, notification());
        return START_STICKY;
    }

    private Notification notification() {
        Intent on = new Intent(this, PerformanceService.class).setAction(ENABLE);
        Intent off = new Intent(this, PerformanceService.class).setAction(DISABLE);
        PendingIntent a = PendingIntent.getService(this, 1, on,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        PendingIntent b = PendingIntent.getService(this, 2, off,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        return new Notification.Builder(this, CHANNEL)
            .setSmallIcon(R.drawable.ic_service)
            .setContentTitle("Universal Performance")
            .setContentText(enabled ? "Performance profile active" : "Profile disabled")
            .setOngoing(true)
            .addAction(new Notification.Action.Builder(
                Icon.createWithResource(this, android.R.drawable.ic_media_play),
                "Enable", a).build())
            .addAction(new Notification.Action.Builder(
                Icon.createWithResource(this, android.R.drawable.ic_media_pause),
                "Disable", b).build())
            .build();
    }

    private void createChannel() {
        NotificationChannel c = new NotificationChannel(CHANNEL,
            getString(R.string.service_channel), NotificationManager.IMPORTANCE_LOW);
        c.setDescription(getString(R.string.service_description));
        NotificationManager m = getSystemService(NotificationManager.class);
        if (m != null) m.createNotificationChannel(c);
    }

    @Override public IBinder onBind(Intent intent) { return null; }
}
