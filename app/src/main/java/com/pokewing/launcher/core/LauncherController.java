package com.pokewing.launcher.core;

import android.content.Context;

import com.pokewing.launcher.jvm.JvmBridge;
import com.pokewing.launcher.jvm.JvmCallback;
import com.pokewing.launcher.modstudio.injector.ModInjector;
import com.pokewing.launcher.utils.GameDirectoryManager;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Launcher'in orkestrasyon merkezi.
 *
 * Iki gorevi birlestirir:
 *  1) Mod Studio'da uretilen .jar dosyasini taklit edilen .minecraft/mods
 *     dizinine enjekte etmek ({@link #injectMod(File)}).
 *  2) Orijinal Minecraft client jar'ini + kutuphaneleri + enjekte edilen
 *     modlari classpath'e dizip native JVM katmanini (JNI Invocation API)
 *     dogru parametrelerle baslatmak ({@link #launch(GameProfile, JvmCallback)}).
 *
 * Bu sinif "ne baslatilacagini" bilir; "nasil baslatilacagini" (JNI_CreateJavaVM
 * detaylari) bilmez — o sorumluluk {@link JvmBridge}/native katmanda kalir.
 */
public final class LauncherController {

    private final Context appContext;
    private final GameDirectoryManager directories;
    private final ModInjector modInjector;

    public LauncherController(Context context) {
        this.appContext = context.getApplicationContext();
        this.directories = new GameDirectoryManager(appContext);
        this.modInjector = new ModInjector(directories.getModsDir());
        // JvmBridge (ve onun System.loadLibrary cagirdigi native .so'lar) BURADA
        // yuklenmez: Mod Studio'yu acip modlari duzenlemek icin native JVM
        // kutuphanelerine ihtiyac yok. Sadece launch()/terminate() cagrildiginda,
        // yani kullanici gercekten oyunu baslattiginda yuklenir — aksi halde
        // native kutuphaneler henuz hazir/uyumlu degilse (bkz. README) tum
        // uygulama daha ana ekran acilmadan cokerdi.
    }

    /**
     * Mod Studio'nun code-gen ciktisini (bkz. modstudio/codegen) mods dizinine yerlestirir.
     *
     * @param generatedJar Mod Studio tarafindan uretilmis, henuz launcher'a dahil olmamis jar
     * @return mods dizinindeki nihai dosya konumu
     */
    public File injectMod(File generatedJar) throws ModInjector.InjectionException {
        return modInjector.inject(generatedJar);
    }

    public boolean removeMod(String jarFileName) {
        return modInjector.remove(jarFileName);
    }

    public File[] listInstalledMods() {
        File[] mods = directories.getModsDir().listFiles((dir, name) -> name.endsWith(".jar"));
        return mods != null ? mods : new File[0];
    }

    /**
     * Verilen profile gore JVM'i baslatir. Cagrilmadan once ilgili surumun
     * jar'inin ve gerekli kutuphanelerin versions/ ve libraries/ dizinlerinde
     * hazir oldugu varsayilir (indirme/kurulum akisi ayri bir modulun isidir).
     */
    public boolean launch(GameProfile profile, JvmCallback callback) {
        File versionJar = directories.getVersionJar(profile.versionId);
        if (!versionJar.exists()) {
            callback.onFatalError("Surum jar dosyasi bulunamadi: " + versionJar.getAbsolutePath());
            return false;
        }

        JvmBridge jvmBridge;
        try {
            jvmBridge = JvmBridge.getInstance();
        } catch (UnsatisfiedLinkError e) {
            callback.onFatalError("Native JVM kutuphaneleri yuklenemedi: " + e.getMessage());
            return false;
        }

        if (jvmBridge.isRunning()) {
            callback.onFatalError("Zaten calisan bir oyun oturumu var.");
            return false;
        }

        String jvmLibPath = resolveEmbeddedJvmLibPath();
        String[] classpath = buildClasspath(versionJar);
        String[] jvmArgs = buildJvmArgs(profile);
        String[] programArgs = buildProgramArgs(profile);

        return jvmBridge.startJvm(
                jvmLibPath,
                classpath,
                jvmArgs,
                "net.minecraft.client.main.Main",
                programArgs,
                callback
        );
    }

    public void terminate() {
        try {
            JvmBridge.getInstance().shutdown();
        } catch (UnsatisfiedLinkError e) {
            // launch() hic basarili olmadiysa (native kutuphane yuklenemedi),
            // durduracak bir seyin zaten olmadigi anlamina gelir.
        }
    }

    // ------------------------------------------------------------------
    // Yardimci olusturucular
    // ------------------------------------------------------------------

    /**
     * Client jar + libraries/ dizinindeki tum jar'lar + mods/ dizinindeki
     * enjekte edilmis mod jar'lari tek bir classpath dizisinde birlestirilir.
     * LWJGL portu da libraries/ altinda diger kutuphanelerle ayni sekilde ele alinir.
     */
    private String[] buildClasspath(File versionJar) {
        List<String> classpath = new ArrayList<>();
        classpath.add(versionJar.getAbsolutePath());

        File[] libraries = directories.getLibrariesDir().listFiles((dir, name) -> name.endsWith(".jar"));
        if (libraries != null) {
            for (File lib : libraries) {
                classpath.add(lib.getAbsolutePath());
            }
        }

        for (File mod : listInstalledMods()) {
            classpath.add(mod.getAbsolutePath());
        }

        return classpath.toArray(new String[0]);
    }

    private String[] buildJvmArgs(GameProfile profile) {
        List<String> args = new ArrayList<>();
        args.add("-Xms" + Math.min(256, profile.allocatedRamMb) + "m");
        args.add("-Xmx" + profile.allocatedRamMb + "m");
        // Dusuk donanimli cihazlarda GC duraklamalarini azaltmak icin
        args.add("-XX:+UseG1GC");
        args.add("-XX:MaxGCPauseMillis=40");
        args.add("-Djava.library.path=" + directories.getNativesDir().getAbsolutePath());
        args.add("-Dorg.lwjgl.librarypath=" + directories.getNativesDir().getAbsolutePath());
        args.addAll(profile.extraJvmArgs);
        return args.toArray(new String[0]);
    }

    private String[] buildProgramArgs(GameProfile profile) {
        List<String> args = new ArrayList<>();
        args.add("--username"); args.add(profile.username);
        args.add("--version"); args.add(profile.versionId);
        args.add("--gameDir"); args.add(directories.getRootDir().getAbsolutePath());
        args.add("--assetsDir"); args.add(directories.getAssetsDir().getAbsolutePath());
        args.add("--width"); args.add(String.valueOf(profile.windowWidth));
        args.add("--height"); args.add(String.valueOf(profile.windowHeight));
        args.add("--accessToken"); args.add("0"); // test/offline mod oturumu
        return args.toArray(new String[0]);
    }

    /**
     * Uygulama ile birlikte dagitilan/ilk calistirmada acilan gomulu JRE'nin
     * libjvm.so yolunu dondurur. Gercek indirme/kurulum akisi (orn. Zulu/Temurin
     * Android portu) ayri bir "runtime provisioning" bilesenine ait olacak;
     * burada sadece beklenen konum sozlesmesi tanimlanir.
     */
    private String resolveEmbeddedJvmLibPath() {
        return new File(appContext.getFilesDir(), "jre17/lib/server/libjvm.so").getAbsolutePath();
    }
}
