package com.pokewing.launcher.utils;

import android.content.Context;

import java.io.File;

/**
 * Launcher'in taklit ettigi ".minecraft" dizin agacini tek bir yerden yonetir.
 * Tum modullerin (Runtime, Mod Studio, Injector) ayni dizin yapisina referans
 * vermesini garanti eder; yollarin dagitik sekilde string olarak kurulmasini onler.
 */
public final class GameDirectoryManager {

    private static final String ROOT_DIR_NAME = "pokewing_minecraft";

    private final File rootDir;

    public GameDirectoryManager(Context context) {
        // Uygulamaya ozel harici depolama: kullanici izni gerektirmez, kaldirmada otomatik temizlenir.
        this.rootDir = new File(context.getExternalFilesDir(null), ROOT_DIR_NAME);
        ensureDirectoryTree();
    }

    private void ensureDirectoryTree() {
        for (File dir : new File[]{
                rootDir, getModsDir(), getLibrariesDir(), getAssetsDir(),
                getVersionsDir(), getNativesDir(), getSavesDir()
        }) {
            if (!dir.exists() && !dir.mkdirs()) {
                throw new IllegalStateException("Dizin olusturulamadi: " + dir.getAbsolutePath());
            }
        }
    }

    public File getRootDir() {
        return rootDir;
    }

    public File getModsDir() {
        return new File(rootDir, "mods");
    }

    public File getLibrariesDir() {
        return new File(rootDir, "libraries");
    }

    public File getAssetsDir() {
        return new File(rootDir, "assets");
    }

    public File getVersionsDir() {
        return new File(rootDir, "versions");
    }

    public File getNativesDir() {
        return new File(rootDir, "natives");
    }

    public File getSavesDir() {
        return new File(rootDir, "saves");
    }

    public File getVersionJar(String versionId) {
        return new File(new File(getVersionsDir(), versionId), versionId + ".jar");
    }
}
