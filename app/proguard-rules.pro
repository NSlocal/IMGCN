# ============================================================
# Android Release / R8 baseline
# ============================================================

# Keep annotations and generic signatures used by Android APIs.
-keepattributes *Annotation*
-keepattributes Signature
-keepattributes InnerClasses
-keepattributes EnclosingMethod

# Keep the application's Android components.
-keep public class com.example.performance.MainActivity { *; }
-keep public class com.example.performance.PerformanceService { *; }

# Keep notification/service entry points.
-keepclassmembers class com.example.performance.PerformanceService {
    public <init>(...);
    public android.os.IBinder onBind(android.content.Intent);
    public int onStartCommand(android.content.Intent, int, int);
}

# Keep Parcelable/Serializable metadata if added by the application.
-keepclassmembers class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}

-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Preserve runtime annotations.
-keep @interface * { *; }

# Android resource/R classes can be safely optimized by R8.
# Do not globally keep every class; that would unnecessarily
# increase APK size and reduce optimization.

# ============================================================
# Optional library rules
# ============================================================
# Add rules here only when a dependency reports a missing-class
# or reflection warning that actually requires preservation.

# Example:
# -keep class com.example.yourlibrary.** { *; }

# ============================================================
# Optimization
# ============================================================
# R8 performs shrinking, optimization and obfuscation for the
# release build. No global -dontoptimize or -dontobfuscate is used.

# Do NOT use:
# -dontshrink
# -dontoptimize
# -dontobfuscate
# -keep class ** { *; }

# Those broad rules can make the APK larger and reduce R8's
# ability to optimize the application.
