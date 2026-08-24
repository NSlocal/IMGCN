# Android metadata.
-keepattributes *Annotation*
-keepattributes Signature
-keepattributes InnerClasses
-keepattributes EnclosingMethod

# Main Activity.
-keep public class com.universal.performance.MainActivity {
    *;
}

# Foreground performance service.
-keep public class com.universal.performance.PerformanceService {
    *;
}
