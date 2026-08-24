package com.universal.performance;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.SystemClock;

import java.util.Locale;

public final class PerformanceService extends Service {

    private static final String CHANNEL_ID =
        "performance_monitor";

    private static final int NOTIFICATION_ID =
        1001;

    /*
     * Normal polling:
     * 5 seconds.
     *
     * When the device is hot:
     * 15 seconds.
     *
     * This prevents the monitor itself from wasting CPU while
     * the device is under thermal pressure.
     */
    private static final long NORMAL_INTERVAL_MS =
        5000L;

    private static final long HOT_INTERVAL_MS =
        15000L;

    private final Handler handler =
        new Handler();

    private PowerManager powerManager;
    private BatteryManager batteryManager;

    private boolean running;

    private final Runnable monitorRunnable =
        new Runnable() {

            @Override
            public void run() {

                if (!running) {
                    return;
                }

                updatePerformanceNotification();

                long interval =
                    getRecommendedInterval();

                handler.postDelayed(
                    this,
                    interval
                );
            }
        };

    @Override
    public void onCreate() {
        super.onCreate();

        powerManager =
            (PowerManager) getSystemService(
                Context.POWER_SERVICE
            );

        batteryManager =
            (BatteryManager) getSystemService(
                Context.BATTERY_SERVICE
            );

        createNotificationChannel();

        running = true;

        startForeground(
            NOTIFICATION_ID,
            createNotification(
                "Performance monitor starting"
            )
        );

        handler.post(monitorRunnable);
    }

    @Override
    public int onStartCommand(
            Intent intent,
            int flags,
            int startId) {

        /*
         * START_NOT_STICKY prevents Android from aggressively
         * recreating the service after the user stops it.
         *
         * This reduces unnecessary background work.
         */
        return START_NOT_STICKY;
    }

    private long getRecommendedInterval() {

        if (Build.VERSION.SDK_INT >= 29 &&
            powerManager != null) {

            int thermalStatus =
                powerManager.getCurrentThermalStatus();

            if (thermalStatus >=
                PowerManager.THERMAL_STATUS_SEVERE) {

                return HOT_INTERVAL_MS;
            }
        }

        return NORMAL_INTERVAL_MS;
    }

    private void updatePerformanceNotification() {

        String temperature =
            readBatteryTemperature();

        String battery =
            readBatteryLevel();

        String thermal =
            readThermalStatus();

        String text =
            String.format(
                Locale.US,
                "Battery %s • Temp %s • %s",
                battery,
                temperature,
                thermal
            );

        NotificationManager manager =
            (NotificationManager)
                getSystemService(
                    Context.NOTIFICATION_SERVICE
                );

        if (manager != null) {

            manager.notify(
                NOTIFICATION_ID,
                createNotification(text)
            );
        }
    }

    private String readBatteryTemperature() {

        if (batteryManager == null) {
            return "--";
        }

        /*
         * BatteryManager does not expose battery temperature
         * on every Android device through a stable public API.
         *
         * Therefore this implementation deliberately reports
         * unavailable instead of reading private vendor files.
         */

        return "--";
    }

    private String readBatteryLevel() {

        if (batteryManager == null) {
            return "--";
        }

        int level =
            batteryManager.getIntProperty(
                BatteryManager.BATTERY_PROPERTY_CAPACITY
            );

        if (level < 0 || level > 100) {
            return "--";
        }

        return level + "%";
    }

    private String readThermalStatus() {

        if (Build.VERSION.SDK_INT < 29 ||
            powerManager == null) {

            return "thermal API unavailable";
        }

        int status =
            powerManager.getCurrentThermalStatus();

        switch (status) {

            case PowerManager.THERMAL_STATUS_NONE:
                return "thermal normal";

            case PowerManager.THERMAL_STATUS_LIGHT:
                return "thermal light";

            case PowerManager.THERMAL_STATUS_MODERATE:
                return "thermal moderate";

            case PowerManager.THERMAL_STATUS_SEVERE:
                return "thermal severe";

            case PowerManager.THERMAL_STATUS_CRITICAL:
                return "thermal critical";

            case PowerManager.THERMAL_STATUS_EMERGENCY:
                return "thermal emergency";

            case PowerManager.THERMAL_STATUS_SHUTDOWN:
                return "thermal shutdown";

            default:
                return "thermal unknown";
        }
    }

    private Notification createNotification(
            String text) {

        Notification.Builder builder;

        if (Build.VERSION.SDK_INT >= 26) {

            builder =
                new Notification.Builder(
                    this,
                    CHANNEL_ID
                );

        } else {

            builder =
                new Notification.Builder(this);
        }

        return builder
            .setSmallIcon(
                android.R.drawable.ic_menu_info_details
            )
            .setContentTitle(
                getString(R.string.app_name)
            )
            .setContentText(text)
            .setOngoing(true)
            .setCategory(
                Notification.CATEGORY_SERVICE
            )
            .setOnlyAlertOnce(true)
            .build();
    }

    private void createNotificationChannel() {

        if (Build.VERSION.SDK_INT < 26) {
            return;
        }

        NotificationChannel channel =
            new NotificationChannel(
                CHANNEL_ID,
                "Performance monitor",
                NotificationManager.IMPORTANCE_LOW
            );

        channel.setDescription(
            "Lightweight device performance monitoring"
        );

        channel.setShowBadge(false);

        NotificationManager manager =
            getSystemService(
                NotificationManager.class
            );

        if (manager != null) {
            manager.createNotificationChannel(
                channel
            );
        }
    }

    @Override
    public void onDestroy() {

        running = false;

        handler.removeCallbacks(
            monitorRunnable
        );

        stopForeground(
            STOP_FOREGROUND_REMOVE
        );

        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
