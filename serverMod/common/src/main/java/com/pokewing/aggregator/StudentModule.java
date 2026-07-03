package com.pokewing.aggregator;

/**
 * Ogrenci/ogretmen tarafindan Mod Studio'da (blok mantigi ya da duz kod ile)
 * uretilen her ozellik, bu arayuzu implemente eden bir sinif olarak
 * pokewing_aggregator moduna eklenir.
 *
 * Yeni bir modul eklemek = yeni bir StudentModule implementasyonu yazip
 * {@link ModuleRegistry}'ye kaydetmek demektir; loader-spesifik (Fabric/Forge/
 * NeoForge) hicbir sey bilmesi gerekmez, tum kayit islemleri common katmaninda
 * Architectury API uzerinden yapilir.
 */
public interface StudentModule {

    /** Benzersiz modul kimligi, ornek: "efsane_muhendis_dinamit_tnt" */
    String id();

    /** Ogrencinin/ogretmenin gordugu isim, log ve hata mesajlarinda kullanilir. */
    String displayName();

    /** Blok/item/event kayitlarini burada yap. Sunucu her baslatildiginda bir kez cagrilir. */
    void register();
}
