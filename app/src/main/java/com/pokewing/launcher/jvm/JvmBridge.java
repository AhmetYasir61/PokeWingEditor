package com.pokewing.launcher.jvm;

/**
 * NDK/C++ katmanina (jvm_bridge.cpp) JNI Invocation API uzerinden erisim noktasi.
 *
 * Sorumlulugu sadece "sinir gecisi" (Java <-> native) yapmaktir; launch
 * parametrelerinin hazirlanmasi ve is akisi {@link com.pokewing.launcher.core.LauncherController}
 * tarafindan yonetilir. Bu ayrim, JVM baslatma detaylarinin (gomulu JRE, LWJGL/GL
 * baglanti noktalari) UI ve mod-enjeksiyon kodundan izole kalmasini saglar.
 */
public final class JvmBridge {

    static {
        System.loadLibrary("gl_optimizer");
        System.loadLibrary("jvm_bridge");
    }

    private static volatile JvmBridge instance;

    private JvmCallback callback;

    private JvmBridge() {
    }

    public static JvmBridge getInstance() {
        if (instance == null) {
            synchronized (JvmBridge.class) {
                if (instance == null) {
                    instance = new JvmBridge();
                }
            }
        }
        return instance;
    }

    /**
     * Gomulu JRE'yi (ornek: /data/data/<pkg>/files/jre17/lib/server/libjvm.so)
     * verilen classpath ve JVM argumanlariyla ayaga kaldirir.
     *
     * @param jvmLibPath   Cihaza kurulmus/cikartilmis libjvm.so tam yolu
     * @param classpath    Minecraft client jar + kutuphaneler (LWJGL portlari, mod jarlari dahil)
     * @param jvmArgs      "-Xmx1024m", "-Djava.library.path=..." gibi standart JVM bayraklari
     * @param mainClass    Genellikle "net.minecraft.client.main.Main"
     * @param programArgs  --username, --version, --gameDir, --assetsDir vb.
     * @param callback     Yasam dongusu bildirimleri
     * @return baslatma islemi basariyla tetiklendiyse true
     */
    public boolean startJvm(String jvmLibPath, String[] classpath, String[] jvmArgs,
                             String mainClass, String[] programArgs, JvmCallback callback) {
        this.callback = callback;
        return nativeStartJvm(jvmLibPath, classpath, jvmArgs, mainClass, programArgs);
    }

    public void shutdown() {
        nativeShutdownJvm();
    }

    public boolean isRunning() {
        return nativeIsRunning();
    }

    // --- Native (JNI) katmandan geri cagrilir; dokunmayin, imzayi degistirmeden once cpp tarafini guncelleyin. ---

    private void dispatchLog(String line) {
        if (callback != null) callback.onLog(line);
    }

    private void dispatchReady() {
        if (callback != null) callback.onJvmReady();
    }

    private void dispatchExit(int code) {
        if (callback != null) callback.onGameExit(code);
    }

    private void dispatchError(String message) {
        if (callback != null) callback.onFatalError(message);
    }

    // --- Native metod bildirimleri (jvm_bridge.cpp icinde implemente edilir) ---

    private native boolean nativeStartJvm(String jvmLibPath, String[] classpath,
                                           String[] jvmArgs, String mainClass, String[] programArgs);

    private native void nativeShutdownJvm();

    private native boolean nativeIsRunning();
}
