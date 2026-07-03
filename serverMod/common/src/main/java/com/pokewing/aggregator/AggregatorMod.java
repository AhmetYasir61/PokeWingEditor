package com.pokewing.aggregator;

/**
 * Loader'dan bagimsiz ortak baslatma noktasi. Fabric/Forge/NeoForge modulleri
 * kendi entrypoint siniflarindan (bkz. fabric/forge/neoforge alt projeleri)
 * bu metodu cagirir; loader-spesifik kod bu sinifin icine asla sizmaz.
 */
public final class AggregatorMod {

    public static final String MOD_ID = "pokewing_aggregator";

    private AggregatorMod() {
    }

    public static void init() {
        GeneratedModules.registerAll(); // Mod Studio blok editorunden (codegen) gelen moduller
        ManualModules.registerAll();    // duz kod ile elle yazilan moduller
        ModuleRegistry.initAll();
    }
}
