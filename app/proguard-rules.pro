# Universal Performance R8 rules
-keepattributes *Annotation*
-keepattributes Signature
-keepattributes InnerClasses
-keepattributes EnclosingMethod

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

# Do not use global -keep rules: R8 should remove unused code.
