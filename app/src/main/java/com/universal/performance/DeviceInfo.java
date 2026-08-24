package com.universal.performance;

import android.app.Activity;
import android.content.Context;
import android.hardware.display.DisplayManager;
import android.os.Build;
import android.view.Display;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Locale;

public final class DeviceInfo {
    private DeviceInfo() {}

    public static String cpuSummary() {
        int cores = Runtime.getRuntime().availableProcessors();
        String abi = Build.SUPPORTED_ABIS.length > 0 ? Build.SUPPORTED_ABIS[0] : "unknown";
        return "CPU cores: " + cores + "\n"
                + "Hardware: " + safe(Build.HARDWARE) + "\n"
                + "ABI: " + abi + "\n"
                + "Board: " + safe(Build.BOARD);
    }

    public static String gpuSummary() {
        String renderer = "Unavailable until a GL context is created";
        String vendor = "Unavailable";
        String version = "Unavailable";
        return "GPU renderer: " + renderer + "\n"
                + "Vendor: " + vendor + "\n"
                + "OpenGL/Vulkan: " + version;
    }

    public static String cpuTemperature() {
        String[] thermalFiles = {
                "/sys/class/thermal/thermal_zone0/temp",
                "/sys/class/thermal/thermal_zone1/temp",
                "/sys/devices/virtual/thermal/thermal_zone0/temp"
        };

        for (String path : thermalFiles) {
            try {
                File f = new File(path);
                if (!f.isFile()) continue;
                BufferedReader br = new BufferedReader(new FileReader(f));
                String s = br.readLine();
                br.close();
                if (s == null) continue;
                double raw = Double.parseDouble(s.trim());
                double c = raw > 1000 ? raw / 1000.0 : raw;
                if (c > 0 && c < 150) {
                    return String.format(Locale.US, "%.1f °C", c);
                }
            } catch (Exception ignored) {
            }
        }
        return "Not exposed by this device";
    }

    public static String displaySummary(Context context) {
        DisplayManager dm = (DisplayManager) context.getSystemService(Context.DISPLAY_SERVICE);
        Display d = dm != null ? dm.getDisplay(Display.DEFAULT_DISPLAY) : null;
        if (d == null) return "Display unavailable";

        android.util.DisplayMetrics m = new android.util.DisplayMetrics();
        d.getRealMetrics(m);

        return String.format(
                Locale.US,
                "%dx%d • density %.0f dpi • %.1f Hz",
                m.widthPixels,
                m.heightPixels,
                m.densityDpi,
                d.getRefreshRate()
        );
    }

    private static String safe(String s) {
        return s == null || s.trim().isEmpty() ? "unknown" : s;
    }
}
