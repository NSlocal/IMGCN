package com.universal.performance;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Settings;
import android.view.Choreographer;
import android.view.Display;
import android.view.Gravity;
import android.view.WindowManager;
import android.widget.TextView;

import java.util.Locale;

public class OverlayService extends Service implements Choreographer.FrameCallback {
    private static final String CHANNEL_ID = "performance_overlay";
    private static final int NOTIFICATION_ID = 1202;

    private WindowManager wm;
    private TextView overlay;
    private Handler handler;
    private long frameCount;
    private long windowStartNs;
    private float measuredFps;
    private boolean added;

    private final Runnable refresh = new Runnable() {
        @Override public void run() {
            updateOverlay();
            if (handler != null) handler.postDelayed(this, 500);
        }
    };

    public static void start(Context context) {
        if (!Settings.canDrawOverlays(context)) return;
        Intent i = new Intent(context, OverlayService.class);
        if (Build.VERSION.SDK_INT >= 26) context.startForegroundService(i);
        else context.startService(i);
    }

    public static void stop(Context context) {
        context.stopService(new Intent(context, OverlayService.class));
    }

    @Override
    public void onCreate() {
        super.onCreate();
        handler = new Handler(getMainLooper());
        createChannel();
        startForeground(NOTIFICATION_ID, buildNotification());

        if (!Settings.canDrawOverlays(this)) {
            stopSelf();
            return;
        }

        wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        overlay = new TextView(this);
        overlay.setTextColor(Color.WHITE);
        overlay.setTextSize(getSharedPreferences("overlay", MODE_PRIVATE).getInt("textSize", 10) + 12);
        overlay.setGravity(Gravity.CENTER);
        overlay.setPadding(18, 8, 18, 8);
        overlay.setBackgroundColor(0x99000000);
        overlay.setElevation(12);

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                Build.VERSION.SDK_INT >= 26
                        ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                        : WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                        | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSLUCENT
        );
        lp.gravity = Gravity.TOP | Gravity.END;
        lp.x = 16;
        lp.y = 80;

        try {
            wm.addView(overlay, lp);
            added = true;
        } catch (Exception e) {
            stopSelf();
            return;
        }

        windowStartNs = System.nanoTime();
        Choreographer.getInstance().postFrameCallback(this);
        handler.post(refresh);
    }

    private Notification buildNotification() {
        Notification.Builder b = Build.VERSION.SDK_INT >= 26
                ? new Notification.Builder(this, CHANNEL_ID)
                : new Notification.Builder(this);

        return b.setSmallIcon(android.R.drawable.ic_menu_info_details)
                .setContentTitle("Universal Performance")
                .setContentText("Performance overlay is active")
                .setOngoing(true)
                .build();
    }

    private void createChannel() {
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel c = new NotificationChannel(
                    CHANNEL_ID, "Performance overlay",
                    NotificationManager.IMPORTANCE_LOW
            );
            NotificationManager nm = getSystemService(NotificationManager.class);
            if (nm != null) nm.createNotificationChannel(c);
        }
    }

    @Override
    public void doFrame(long frameTimeNanos) {
        frameCount++;
        long elapsed = frameTimeNanos - windowStartNs;
        if (elapsed >= 1_000_000_000L) {
            measuredFps = frameCount * 1_000_000_000f / elapsed;
            frameCount = 0;
            windowStartNs = frameTimeNanos;
        }
        Choreographer.getInstance().postFrameCallback(this);
    }

    private void updateOverlay() {
        if (!added || overlay == null) return;

        Display display = wm.getDefaultDisplay();
        float rr = display != null ? display.getRefreshRate() : 0f;
        boolean showRefresh = getSharedPreferences("overlay", MODE_PRIVATE)
                .getBoolean("showRefresh", true);

        if (showRefresh) {
            overlay.setText(String.format(Locale.US, "FPS %.0f\nRR %.0f Hz", measuredFps, rr));
        } else {
            overlay.setText(String.format(Locale.US, "FPS %.0f", measuredFps));
        }
    }

    @Override
    public void onDestroy() {
        if (handler != null) handler.removeCallbacksAndMessages(null);
        try {
            Choreographer.getInstance().removeFrameCallback(this);
        } catch (Exception ignored) {}

        if (added && wm != null && overlay != null) {
            try { wm.removeView(overlay); } catch (Exception ignored) {}
        }
        added = false;
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
