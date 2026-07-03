package com.pokewing.launcher.ui.activities;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.pokewing.launcher.core.GameProfile;
import com.pokewing.launcher.core.LauncherApplication;
import com.pokewing.launcher.core.LauncherController;
import com.pokewing.launcher.jvm.JvmCallback;

/**
 * Runtime & Launcher modulunun test oturumu ekrani. Ayri process'te
 * (:gameRuntime, bkz. AndroidManifest.xml) calisir; JVM/Minecraft tarafinda
 * bir cokme olsa dahi Mod Studio ve ana launcher etkilenmez.
 */
public class GameSessionActivity extends AppCompatActivity implements JvmCallback {

    private static final String TAG = "GameSessionActivity";

    private LauncherController controller;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        controller = ((LauncherApplication) getApplication()).getLauncherController();

        GameProfile profile = new GameProfile.Builder()
                .versionId("1.20.1")
                .username("TestPlayer")
                .allocatedRamMb(1536)
                .windowSize(1280, 720)
                .build();

        controller.launch(profile, this);
    }

    @Override
    protected void onDestroy() {
        controller.terminate();
        super.onDestroy();
    }

    @Override
    public void onLog(String line) {
        Log.d(TAG, line);
    }

    @Override
    public void onJvmReady() {
        Log.i(TAG, "JVM hazir, oyun baslatildi.");
    }

    @Override
    public void onGameExit(int exitCode) {
        Log.i(TAG, "Oyun sonlandi, kod: " + exitCode);
        runOnUiThread(this::finish);
    }

    @Override
    public void onFatalError(String message) {
        Log.e(TAG, "Kritik hata: " + message);
    }
}
