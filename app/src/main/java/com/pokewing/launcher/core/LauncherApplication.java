package com.pokewing.launcher.core;

import android.app.Application;

/**
 * Uygulama genelinde tek instance olarak tutulan LauncherController'i barindirir.
 * Activity'ler arasi (MainActivity, GameSessionActivity, ModStudioActivity)
 * ayni dizin/JVM durumunun paylasilmasini saglar.
 */
public class LauncherApplication extends Application {

    private LauncherController launcherController;

    @Override
    public void onCreate() {
        super.onCreate();
        launcherController = new LauncherController(this);
    }

    public LauncherController getLauncherController() {
        return launcherController;
    }
}
