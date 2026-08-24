# Keep Android component constructors and metadata.
-keepattributes *Annotation*
-keepattributes Signature
-keepattributes InnerClasses
-keepattributes EnclosingMethod

# Keep the application Activity.
-keep public class com.universal.performance.MainActivity {
    *;
}

# Keep the foreground performance service.
-keep public class com.universal.performance.PerformanceService {
    *;
}

# Android Parcelable implementations.
-keepclassmembers class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}

# Enum compatibility.
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}
