# JNI ile cagrilan native metodlarin adi/imzasi degismemeli
-keepclasseswithmembernames class * {
    native <methods>;
}

# Gson reflection ile calisiyor; model siniflarini koru
-keep class com.pokewing.launcher.modstudio.blockbench.** { *; }
-keep class com.pokewing.launcher.core.model.** { *; }
