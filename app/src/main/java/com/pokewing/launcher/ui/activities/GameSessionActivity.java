package com.pokewing.launcher.ui.activities;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.pokewing.launcher.core.GameProfile;
import com.pokewing.launcher.core.LauncherApplication;
import com.pokewing.launcher.core.LauncherController;
import com.pokewing.launcher.databinding.ActivityGameSessionBinding;
import com.pokewing.launcher.jvm.JvmCallback;

/**
 * Runtime & Launcher modulunun test oturumu ekrani. Ayri process'te
 * (:gameRuntime, bkz. AndroidManifest.xml) calisir; JVM/Minecraft tarafinda
 * bir cokme olsa dahi Mod Studio ve ana launcher etkilenmez.
 *
 * Gomulu JVM/Minecraft baslatma katmani henuz iskelet halinde oldugundan
 * (bkz. jvm_bridge.cpp) bu ekran gercek bir oyun penceresi degil, durum
 * gunlugu gosteren basit bir arayuzdur — boylece basarisiz/eksik bir baslatma
 * "cevapsiz siyah ekran" yerine acikca goruntulenir.
 */
public class GameSessionActivity extends AppCompatActivity implements JvmCallback {

    private static final String TAG = "GameSessionActivity";

    private LauncherController controller;
    private ActivityGameSessionBinding binding;
    private final StringBuilder log = new StringBuilder();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityGameSessionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.buttonClose.setOnClickListener(v -> finish());

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

    private void appendLog(String line) {
        log.append(line).append('\n');
        runOnUiThread(() -> binding.textStatusLog.setText(log.toString()));
    }

    @Override
    public void onLog(String line) {
        Log.d(TAG, line);
        appendLog(line);
    }

    @Override
    public void onJvmReady() {
        Log.i(TAG, "JVM hazir, oyun baslatildi.");
        appendLog("JVM hazir, oyun baslatildi.");
    }

    @Override
    public void onGameExit(int exitCode) {
        Log.i(TAG, "Oyun sonlandi, kod: " + exitCode);
        appendLog("Oyun sonlandi, kod: " + exitCode);
        runOnUiThread(this::finish);
    }

    @Override
    public void onFatalError(String message) {
        Log.e(TAG, "Kritik hata: " + message);
        appendLog("HATA: " + message);
    }
}
