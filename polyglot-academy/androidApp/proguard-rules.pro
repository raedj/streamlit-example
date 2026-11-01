# kotlinx.serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.**
-keepclassmembers class com.polyglot.academy.** { *** Companion; }
-keepclasseswithmembers class com.polyglot.academy.** { kotlinx.serialization.KSerializer serializer(...); }
# Ktor / OkHttp
-dontwarn org.slf4j.**
-dontwarn io.ktor.**
# Tesseract4Android: native code calls back into these classes by name.
-keep class com.googlecode.tesseract.android.** { *; }
-keep class com.googlecode.leptonica.android.** { *; }
# whisper.cpp JNI: the native symbols name this class and its methods.
-keep class com.polyglot.academy.android.WhisperNative { native <methods>; }
-keep class com.polyglot.academy.android.LlamaNative { native <methods>; }
