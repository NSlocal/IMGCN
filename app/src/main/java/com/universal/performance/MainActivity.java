package com.universal.performance;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
    private static final int NOTIFICATION_REQUEST = 44;
    private SharedPreferences prefs;

    private int dp(float value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }

    private TextView title(String text, int size) {
        TextView v = new TextView(this);
        v.setText(text);
        v.setTextColor(Color.WHITE);
        v.setTextSize(size);
        v.setPadding(dp(16), dp(12), dp(16), dp(8));
        return v;
    }

    private TextView value(String text) {
        TextView v = new TextView(this);
        v.setText(text);
        v.setTextColor(Color.LTGRAY);
        v.setTextSize(14);
        v.setPadding(dp(16), dp(4), dp(16), dp(12));
        return v;
    }

    private LinearLayout card() {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(dp(4), dp(4), dp(4), dp(8));
        box.setBackgroundColor(Color.rgb(25, 31, 38));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(dp(10), dp(7), dp(10), dp(7));
        box.setLayoutParams(lp);
        return box;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = getSharedPreferences("overlay", MODE_PRIVATE);
        buildUi();
        requestNotificationPermission();
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= 33 &&
                checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, NOTIFICATION_REQUEST);
        }
    }

    private void buildUi() {
        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(Color.rgb(10, 14, 18));

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(0, dp(14), 0, dp(20));

        TextView header = title("Universal Performance", 25);
        header.setGravity(Gravity.CENTER);
        root.addView(header);

        TextView sub = value("FPS Meter • Refresh Rate • CPU & GPU • Device Info");
        sub.setGravity(Gravity.CENTER);
        root.addView(sub);

        LinearLayout overlayCard = card();
        overlayCard.addView(title("FPS & Refresh Rate Overlay", 19));

        final Switch enable = new Switch(this);
        enable.setText("Overlay enabled");
        enable.setTextColor(Color.WHITE);
        enable.setChecked(prefs.getBoolean("enabled", false));
        enable.setPadding(dp(16), 0, dp(16), 0);
        overlayCard.addView(enable);

        Button permission = new Button(this);
        permission.setText("Grant overlay permission");
        overlayCard.addView(permission);

        Button startStop = new Button(this);
        startStop.setText(enable.isChecked() ? "Stop overlay" : "Start overlay");
        overlayCard.addView(startStop);

        root.addView(overlayCard);

        permission.setOnClickListener(v -> {
            if (!Settings.canDrawOverlays(this)) {
                Intent i = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName()));
                startActivity(i);
            } else {
                Toast.makeText(this, "Overlay permission is already granted.", Toast.LENGTH_SHORT).show();
            }
        });

        startStop.setOnClickListener(v -> {
            boolean requested = !enable.isChecked();
            if (requested && !Settings.canDrawOverlays(this)) {
                Toast.makeText(this, "Grant overlay permission first.", Toast.LENGTH_LONG).show();
                return;
            }
            enable.setChecked(requested);
            prefs.edit().putBoolean("enabled", requested).apply();
            if (requested) {
                OverlayService.start(this);
                startStop.setText("Stop overlay");
            } else {
                OverlayService.stop(this);
                startStop.setText("Start overlay");
            }
        });

        enable.setOnCheckedChangeListener((buttonView, checked) -> {
            prefs.edit().putBoolean("enabled", checked).apply();
            if (checked) {
                if (Settings.canDrawOverlays(this)) {
                    OverlayService.start(this);
                    startStop.setText("Stop overlay");
                } else {
                    enable.setChecked(false);
                    Toast.makeText(this, "Grant overlay permission first.", Toast.LENGTH_LONG).show();
                }
            } else {
                OverlayService.stop(this);
                startStop.setText("Start overlay");
            }
        });

        LinearLayout refreshCard = card();
        refreshCard.addView(title("Screen Refresh Rate", 19));
        Switch refreshSwitch = new Switch(this);
        refreshSwitch.setText("Show refresh rate");
        refreshSwitch.setTextColor(Color.WHITE);
        refreshSwitch.setChecked(prefs.getBoolean("showRefresh", true));
        refreshSwitch.setPadding(dp(16), 0, dp(16), 0);
        refreshCard.addView(refreshSwitch);
        refreshSwitch.setOnCheckedChangeListener((b, c) -> prefs.edit().putBoolean("showRefresh", c).apply());

        refreshCard.addView(value("The overlay reads the active display refresh rate from Android's Display API."));
        root.addView(refreshCard);

        LinearLayout sizeCard = card();
        sizeCard.addView(title("FPS Meter", 19));
        sizeCard.addView(value("Adjust the overlay text size. The value is saved locally."));

        SeekBar size = new SeekBar(this);
        size.setMax(30);
        size.setProgress(prefs.getInt("textSize", 10));
        sizeCard.addView(size);
        size.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar bar, int progress, boolean fromUser) {
                prefs.edit().putInt("textSize", progress).apply();
            }
            public void onStartTrackingTouch(SeekBar bar) {}
            public void onStopTrackingTouch(SeekBar bar) {}
        });

        root.addView(sizeCard);

        LinearLayout deviceCard = card();
        deviceCard.addView(title("CPU & GPU Details", 19));
        deviceCard.addView(value(DeviceInfo.cpuSummary()));
        deviceCard.addView(value(DeviceInfo.gpuSummary()));
        deviceCard.addView(value("CPU temperature: " + DeviceInfo.cpuTemperature()));
        root.addView(deviceCard);

        LinearLayout displayCard = card();
        displayCard.addView(title("Display & Device", 19));
        displayCard.addView(value(DeviceInfo.displaySummary(this)));
        displayCard.addView(value("Android " + Build.VERSION.RELEASE + " • API " + Build.VERSION.SDK_INT));
        displayCard.addView(value(Build.MANUFACTURER + " " + Build.MODEL));
        root.addView(displayCard);

        Button about = new Button(this);
        about.setText("Open repository");
        root.addView(about);
        about.setOnClickListener(v -> {
            Intent i = new Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://github.com/NSlocal/IMGCN"));
            startActivity(i);
        });

        scroll.addView(root);
        setContentView(scroll);
    }
}
