#include <jni.h>
#include <android/log.h>
#include <dlfcn.h>
#include <atomic>
#include <vector>
#include <string>

#define LOG_TAG "PokeWing/JvmBridge"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

// JNI Invocation API imzalari (gomulu JRE'nin libjvm.so'sundan dlopen ile alinir).
typedef jint (*CreateJavaVM_t)(JavaVM **, void **, void *);

namespace {
    std::atomic<bool> g_running{false};
    JavaVM *g_guestVm = nullptr; // Minecraft/oyun tarafinin calistigi ikinci JVM ornegi
    void *g_libjvmHandle = nullptr;
}

extern "C"
JNIEXPORT jboolean JNICALL
Java_com_pokewing_launcher_jvm_JvmBridge_nativeStartJvm(
        JNIEnv *env, jobject thiz,
        jstring jvmLibPath, jobjectArray classpath, jobjectArray jvmArgs,
        jstring mainClass, jobjectArray programArgs) {

    if (g_running.load()) {
        LOGE("startJvm cagrildi ancak zaten calisan bir oturum var.");
        return JNI_FALSE;
    }

    const char *libPath = env->GetStringUTFChars(jvmLibPath, nullptr);
    LOGI("Gomulu JRE yukleniyor: %s", libPath);

    // TODO(sonraki adim): libjvm.so, statik olarak platformun System.loadLibrary
    // arama yoluna dahil olmadigindan RTLD_NOW ile dlopen edilip JNI_CreateJavaVM
    // sembolu cozulecek. Ardindan classpath + jvmArgs birlestirilerek JavaVMInitArgs
    // doldurulacak ve guest JVM icinde mainClass.main(programArgs) Reflection ile
    // cagrilacak (LWJGL native pointer'lari gl_optimizer katmanina yonlendirilerek).
    void *handle = dlopen(libPath, RTLD_NOW | RTLD_GLOBAL);
    env->ReleaseStringUTFChars(jvmLibPath, libPath);

    if (!handle) {
        LOGE("libjvm.so yuklenemedi: %s", dlerror());
        return JNI_FALSE;
    }

    g_libjvmHandle = handle;
    g_running.store(true);

    LOGI("JVM baslatma iskeleti hazir (guest classloading henuz baglanmadi).");
    return JNI_TRUE;
}

extern "C"
JNIEXPORT void JNICALL
Java_com_pokewing_launcher_jvm_JvmBridge_nativeShutdownJvm(JNIEnv *env, jobject thiz) {
    if (!g_running.load()) return;

    if (g_guestVm != nullptr) {
        g_guestVm->DestroyJavaVM();
        g_guestVm = nullptr;
    }
    if (g_libjvmHandle != nullptr) {
        dlclose(g_libjvmHandle);
        g_libjvmHandle = nullptr;
    }
    g_running.store(false);
    LOGI("JVM oturumu kapatildi.");
}

extern "C"
JNIEXPORT jboolean JNICALL
Java_com_pokewing_launcher_jvm_JvmBridge_nativeIsRunning(JNIEnv *env, jobject thiz) {
    return g_running.load() ? JNI_TRUE : JNI_FALSE;
}
