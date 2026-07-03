package com.pokewing.launcher.ui.activities;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.pokewing.launcher.databinding.ActivityModStudioBinding;

/**
 * Mod Studio modulunun giris noktasi: Blockbench (.json) model yukleme,
 * blok tabanli mod editoru ve "mods klasorune enjekte et" akisi buradan
 * baslar. Iceriginin detayli implementasyonu sonraki adimda ele alinacaktir;
 * su an icin sadece durumu belirten bir yer tutucu ekran gosterilir.
 */
public class ModStudioActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityModStudioBinding binding = ActivityModStudioBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        // TODO: Blockbench parser (modstudio/blockbench) ve blok editoru
        // (modstudio/blocks) burada baglanacak.
    }
}
