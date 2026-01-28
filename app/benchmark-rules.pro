# ProGuard rules for benchmark builds.
#
# These rules ensure that benchmark builds have the same optimizations as release
# builds while maintaining stack trace readability for profiling.

# Disable obfuscation for better profiling results
-dontobfuscate

# Keep line number information for stack traces
-keepattributes LineNumberTable,SourceFile

# Keep annotations for reflection-based tools
-keepattributes *Annotation*

# Keep all classes for profiling (prevents aggressive removal)
-keep class com.restock.** { *; }
