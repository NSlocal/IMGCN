# Universal Performance R8 rules
-keepattributes *Annotation*
-keepattributes Signature
-keepattributes InnerClasses
-keepattributes EnclosingMethod

# Keep main known entry points
-keep public class com.universal.performance.MainActivity { *; }
-keep public class com.universal.performance.PerformanceService { *; }

-keepclassmembers class com.universal.performance.PerformanceService {
    public <init>(...);
    public android.os.IBinder onBind(android.content.Intent);
    public int onStartCommand(android.content.Intent, int, int);
}

-keepclassmembers class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}
-keep @interface * { *; }

# ------------------------------------------------------------------
# TEMPORARY DEBUG KEEPS: keep the app package classes to avoid crashes
# caused by minification/obfuscation while we investigate.
# Remove or tighten these after debugging.
# ------------------------------------------------------------------
# Keep everything in the app package (temporary)
-keep class com.universal.performance.** { *; }

# Keep Android components referenced from manifest (activities, services, receivers)
-keep public class * extends android.app.Activity { *; }
-keep public class * extends android.app.Service { *; }
-keep public class * extends android.content.BroadcastReceiver { *; }
-keep public class * extends android.content.ContentProvider { *; }

# Keep custom views (constructors used by inflation)
-keepclassmembers class * extends android.view.View {
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
}

# Keep reflection-used methods/fields (Gson/JSON/DI usage)
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# Keep annotations
-keepclassmembers class * {
    @androidx.annotation.Keep *;
}

# Don't warn about common third-party libs used only in debug investigation
-dontwarn kotlin.**
-dontwarn com.google.gson.**
-dontwarn com.squareup.**

# End of file
