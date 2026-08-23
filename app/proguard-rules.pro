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
# NARROWED DEBUG KEEPS: keep only app-package Android components and
# custom view constructors. This is smaller than keeping the whole app
# package and should still prevent startup ClassNotFound/NoClassDef issues.
# ------------------------------------------------------------------
# Keep Activities/Services/Receivers/Providers that live in our app package
-keep public class com.universal.performance.** extends android.app.Activity { *; }
-keep public class com.universal.performance.** extends android.app.Service { *; }
-keep public class com.universal.performance.** extends android.content.BroadcastReceiver { *; }
-keep public class com.universal.performance.** extends android.content.ContentProvider { *; }

# Keep constructors for custom views in our package (used by XML inflation)
-keepclassmembers class com.universal.performance.** {
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
}

# Keep reflection-used methods/fields where we use Gson annotations
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# Keep androidx.annotation.Keep annotated members
-keepclassmembers class * {
    @androidx.annotation.Keep *;
}

# Don't warn about common third-party libs used in the project
-dontwarn kotlin.**
-dontwarn com.google.gson.**
-dontwarn com.squareup.**

# End of file
