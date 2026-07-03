package com.pokewing.aggregator;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * pokewing_aggregator'in "bos mod, biz ne gelistirirsek onunla dolacak" iskeleti.
 *
 * Mod Studio'nun code-gen ciktisi (bkz. Android tarafindaki
 * modstudio/codegen), her publish'te bu listeye yeni bir {@link StudentModule}
 * ekleyen bir kaynak dosyasi uretir/gunceller (bkz. GeneratedModules.java —
 * ilk publish'e kadar bos kalir). Boylece tek bir jar, sinifin urettigi tum
 * modlari icinde barindirir ve sunucu restart'inda hepsi birden yuklenir.
 */
public final class ModuleRegistry {

    private static final Logger LOGGER = Logger.getLogger("PokeWingAggregator");
    private static final List<StudentModule> MODULES = new ArrayList<>();

    private ModuleRegistry() {
    }

    public static void register(StudentModule module) {
        MODULES.add(module);
    }

    /** Common entrypoint (bkz. AggregatorMod#init) tarafindan sunucu her ayaga kalktiginda cagrilir. */
    public static void initAll() {
        LOGGER.info("PokeWing Aggregator: " + MODULES.size() + " ogrenci modulu yukleniyor...");
        for (StudentModule module : MODULES) {
            try {
                module.register();
                LOGGER.info(" -> yuklendi: " + module.displayName() + " [" + module.id() + "]");
            } catch (Exception e) {
                // Bir ogrencinin hatali kodu tum sunucuyu dusurmesin; sadece o modul atlanir.
                LOGGER.severe(" -> HATA (" + module.id() + "): " + e.getMessage());
            }
        }
    }

    public static List<StudentModule> registeredModules() {
        return List.copyOf(MODULES);
    }
}
