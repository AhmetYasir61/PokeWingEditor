package com.pokewing.aggregator;

/**
 * OTOMATIK URETILIR — elle duzenlemeyin.
 *
 * Mod Studio'nun "Publish" adimi, ogrencilerin urettigi her mod icin bu
 * dosyayi yeniden yazar ve her birini {@link ModuleRegistry#register}
 * ile kaydeder. Publish -> CI (bkz. .github/workflows/publish-mod.yml)
 * -> bu dosya + ilgili StudentModule siniflari derlenip fabric/forge/neoforge
 * jar'lari uretilir -> sunucuya deploy edilip restart tetiklenir.
 *
 * Henuz hic mod publish edilmedigi icin liste bostur.
 */
public final class GeneratedModules {

    private GeneratedModules() {
    }

    public static void registerAll() {
        // <<< POKEWING_CODEGEN_INSERT_POINT >>>
        // Mod Studio codegen bu satirin hemen ustune
        // "ModuleRegistry.register(new XyzModule());" cagrilari ekler.
    }
}
