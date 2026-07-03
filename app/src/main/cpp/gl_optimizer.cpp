#include <jni.h>
#include <android/log.h>
#include <GLES3/gl3.h>

#define LOG_TAG "PokeWing/GLOptimizer"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)

// Dusuk donanimli cihazlar icin planlanan optimizasyonlar (sonraki adimlarda doldurulacak):
//  - Texture atlas / mipmap on-bellekleme (chunk yeniden yuklemede GC baskisini azaltir)
//  - Frame-pacing: cihazin gercek refresh-rate'ine gore adaptif frame-time hedefi
//  - LWJGL GLFW cagrilarinin Android EGL/Surface katmanina koprulenmesi
//
// Bu dosya su an icin sadece native kutuphanenin yuklenebilir oldugunu dogrulayan
// bir "canary" fonksiyon barindirir; gercek GL koprusu Mod Studio/Runtime
// entegrasyonu asamasinda genisletilecek.

extern "C"
JNIEXPORT jstring JNICALL
Java_com_pokewing_launcher_jvm_JvmBridge_nativeGlOptimizerVersion(JNIEnv *env, jobject thiz) {
    LOGI("gl_optimizer native katmani aktif.");
    return env->NewStringUTF("gl_optimizer/0.1.0-skeleton");
}
