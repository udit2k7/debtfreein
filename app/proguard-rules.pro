# Proguard rules for DebtFreeIn Application

# Keep all database entity and data models intact
-keep class com.debtfreein.app.data.model.** { *; }

# Keep all Gemini AI advisory mapping models intact
-keep class com.debtfreein.app.data.ai.** { *; }

# Gson-specific rules to preserve serialized fields
-keepattributes *Annotation*
-keepattributes Signature
-keepattributes InnerClasses
-keepattributes EnclosingMethod
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}
-keep class com.google.gson.** { *; }

# Retrofit-specific rules
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keepclasseswithmembers class * {
    @retrofit2.http.** <methods>;
}

# OkHttp-specific rules
-dontwarn okhttp3.**
-keep class okhttp3.** { *; }
-dontwarn okio.**

# Gemini Generative AI SDK rules
-keep class com.google.ai.client.generativeai.** { *; }
-dontwarn com.google.ai.client.generativeai.**
