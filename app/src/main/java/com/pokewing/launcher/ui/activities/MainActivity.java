package com.pokewing.launcher.ui.activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.pokewing.launcher.databinding.ActivityMainBinding;

/**
 * Ana ekran: Runtime & Launcher (test oturumu baslatma) ile Mod Studio
 * arasindaki gecisi saglayan giris noktasi.
 */
public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.buttonLaunchGame.setOnClickListener(v ->
                startActivity(new Intent(this, GameSessionActivity.class)));

        binding.buttonOpenModStudio.setOnClickListener(v ->
                startActivity(new Intent(this, ModStudioActivity.class)));
    }
}
