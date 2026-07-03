# PokeWing Launcher

PojavLauncher benzeri, Android native (Java + NDK) bir "Runtime & Launcher" ve
"Mod Studio" mimarisinin temel iskeleti — buna ek olarak sinifin urettigi
modlari tek bir sunucuda toplayip her yayinda (publish) otomatik guncelleyip
yeniden baslatan bir sinif-ici coklu oyuncu sistemi.

## Bu Repo Uc Parcadan Olusuyor

1. **`app/`** — Android Mod Studio + Runtime/Launcher (bu bolumun detaylari asagida).
2. **`serverMod/`** — Fabric + Forge + NeoForge icin **Architectury tabanli**,
   ortak koddan uretilen "toplayici" (aggregator) sunucu modu. Bkz.
   [`serverMod/README ozeti`](#sinif-sunucusu-servermod--deploy). Vanilla +
   datapack kullanimi icin `serverMod/datapacks/` klasoru ayrica senkronize edilir.
3. **`deploy/`** + **`.github/workflows/`** — telefon-only workflow: push atinca
   bulutta derleme yapip hem Android APK'yi Release'e ekleyen hem de sunucu
   modunu VPS'e gonderip sunucuyu restart eden CI/CD otomasyonu. Kurulum icin
   [`deploy/README.md`](deploy/README.md) dosyasina bakin.

## Modul Haritasi

```
app/src/main/java/com/pokewing/launcher/
├── core/                    # Uygulama yasam dongusu + orkestrasyon
│   ├── LauncherApplication.java
│   ├── LauncherController.java   # Mod enjeksiyonu + JVM baslatma orkestratoru
│   └── GameProfile.java          # Baslatma parametreleri (RAM, kullanici, surum)
│
├── jvm/                     # Runtime & Launcher: Java <-> NDK sinir katmani
│   ├── JvmBridge.java             # JNI Invocation API'ye Java tarafi arayuzu
│   └── JvmCallback.java           # native -> Java yasam dongusu bildirimleri
│
├── modstudio/               # Mod Studio modulu
│   ├── blockbench/          # Blockbench .json model parser (bir sonraki adim)
│   ├── blocks/              # Surukle-birak blok tabanli mod mantigi (bir sonraki adim)
│   ├── codegen/             # Blok grafiginden Java kaynagi/jar uretimi (bir sonraki adim)
│   └── injector/
│       └── ModInjector.java       # Uretilen jar'i mods/ dizinine enjekte eder
│
├── ui/
│   ├── activities/          # MainActivity, GameSessionActivity, ModStudioActivity
│   ├── fragments/
│   └── adapters/
│
└── utils/
    └── GameDirectoryManager.java  # Taklit edilen .minecraft dizin agaci

app/src/main/cpp/            # NDK (C/C++) katmani
├── CMakeLists.txt
├── jvm_bridge.cpp           # JNI_CreateJavaVM ile gomulu JRE baslatma (iskelet)
└── gl_optimizer.cpp         # LWJGL/OpenGL ES optimizasyon koprusu (iskelet)
```

## Java <-> NDK Baglantisi Nasil Calisir?

1. `LauncherController` (Java) bir `GameProfile`'dan classpath, JVM argumanlari
   ve program argumanlarini hazirlar.
2. `JvmBridge.startJvm(...)` cagrilir; bu, `System.loadLibrary("jvm_bridge")`
   ile yuklenen native kutuphanedeki `Java_com_pokewing_launcher_jvm_JvmBridge_nativeStartJvm`
   JNI fonksiyonuna dogru sinyaturle (isim/paket eslesmesi zorunlu) delege eder.
3. `jvm_bridge.cpp`, gomulu JRE'nin `libjvm.so`'sunu `dlopen` ile yukler ve
   JNI Invocation API (`JNI_CreateJavaVM`) uzerinden **ikinci bir JVM ornegi**
   (guest VM) baslatir — bu, Minecraft'in kendi Java kodunu calistiracagi VM'dir.
4. `gl_optimizer.cpp`, guest VM icindeki LWJGL/GLFW cagrilarini Android'in
   EGL/Surface katmanina baglayacak GL koprusunu barindirir (iskelet halinde).
5. Oyun cikisinda/hata durumunda native katman `JvmCallback` uzerinden Java
   tarafina bildirim yollar (`onGameExit`, `onFatalError`).

Bu katmanlama sayesinde: UI ve mod-enjeksiyon mantigi native JVM detaylarindan
tamamen izole kalir; NDK katmani sadece "performans kritik" JVM baslatma ve
grafik koprusu isini ustlenir.

## Gradle Notlari

- `minSdk 26`: modern dosya API'leri ve adaptive-icon destegi icin taban seviye.
- `splits.abi`: her cihaz sadece kendi mimarisine (`arm64-v8a`/`armeabi-v7a`)
  ait native kutuphaneyi indirir; dusuk donanimli cihazlarda APK/disk baskisini azaltir.
- `packagingOptions.jniLibs.useLegacyPackaging = true`: buyuk `.so` dosyalarinin
  (gomulu JRE, LWJGL native) sikistirilmadan paketlenmesini saglar, boylece
  ilk acilista decompress suresi olusmaz.
- Bagimliliklar: `gson` (Blockbench/.json ayristirma), `commons-compress`
  (jar/zip islemleri), `org.lwjgl:lwjgl` (Java tarafi sabit/arayuz seti;
  gercek GL baglama NDK katmanindan yapilir).

## Sinif Sunucusu (`serverMod/` + `deploy/`)

Amac: "Essentials mod gibi" tek bir sunucu modu, sinifin urettigi TUM ogrenci
modlarini icinde barindirsin; her publish'te otomatik guncellensin ve sunucu
yeniden baslasin — boylece bilgisayari olmayan ogrenciler bile (telefon +
Minecraft istemcisi ile) ayni sunucuya baglanip test edebilsin.

- **Multi-loader mimari**: `serverMod/common` paylasilan Java kodunu tutar;
  `fabric/`, `forge/`, `neoforge/` alt projeleri bu ortak koddan **Architectury**
  araciligiyla loader'a ozgu jar uretir (gercek kod paylasimi — ayri ayri
  kopyalanmis "sablon" degil). Vanilla sunucularda mod yuklenemeyecegi icin
  o profil `serverMod/datapacks/pokewing_classroom/` altindaki datapack ile
  desteklenir.
- **Blok + duz kod bir arada**: `common/GeneratedModules.java` Mod Studio'nun
  blok editorunden (codegen) her publish'te yeniden yazilir;
  `common/ManualModules.java` ise codegen'in asla dokunmadigi, duz Java
  yazmak isteyenlerin elle duzenleyebilecegi ayri bir dosyadir. Her ikisi de
  ayni `ModuleRegistry`'ye kayit yapar ve ayni jar icinde birlikte calisir.
- **Otomatik publish + restart**: `.github/workflows/publish-mod.yml`,
  `serverMod/**` altinda degisiklik push edildiginde fabric/forge/neoforge
  jar'larini derler, `deploy/deploy.sh` araciligiyla VPS'e gonderir ve
  `systemctl` ile sunucuyu yeniden baslatir. VPS kurulumu (ucretsiz Oracle
  Cloud Free Tier onerilir) icin adim adim rehber: [`deploy/README.md`](deploy/README.md).
- **Telefondan APK yayinlama**: `.github/workflows/android-build.yml`, `app/`
  altinda degisiklik push edildiginde APK'yi bulutta derleyip GitHub
  Releases'e ekler — Android Studio veya bilgisayar gerekmez.

## Sonraki Faz: Mod Studio'nun Windows/Mac'e Tasinmasi

Bugun icin oncelik sunucu/oyun tarafiydi (bkz. yukarisi); Mod Studio
aray uzunun (blok editoru + kod editoru) Windows/Mac'te de native calismasi
ayri, kapsamli bir faz olarak planlaniyor (Kotlin Multiplatform veya benzeri
bir cati gerektirir — mevcut Android Java/NDK kodunun dogrudan tasinmasi
mumkun degildir). Sunucu/oyun tarafi oturduktan sonra ele alinacak.

## Bilinen Eksikler / Sonraki Adimlar

- **Gomulu JRE saglanmasi**: `jvm_bridge.cpp` su an sadece `libjvm.so`'yu
  `dlopen` ediyor; gercek classloading ve `main()` cagrisi (Reflection ile)
  bir sonraki adimda eklenecek. Uygun bir Android-uyumlu OpenJDK/JRE portu
  (ör. Zulu Embedded, ya da topluluk derlemeleri) saglanmalidir.
- **Gradle wrapper**: `gradle/wrapper/gradle-wrapper.jar` (hem `app/` hem
  `serverMod/` icin) bu ortamda gercek Gradle dagitimindan cikarilarak
  eklendi ve calisir durumda. Bu sandbox'ta `services.gradle.org`'a giden
  indirme istegi proxy tarafindan 403 ile engellendigi icin ilk
  `./gradlew` calistirmasi burada test edilemedi; GitHub Actions veya kendi
  bilgisayarinizda/Android Studio'da bu kisitlama olmayacaktir.
- **Android SDK/NDK bu ortamda kurulu degil**, dolayisiyla `app/` iskeleti
  gradle build ile dogrulanamadi; Android Studio'da acildiginda SDK/NDK
  bilesenlerinin indirilmesi gerekecek. Ayni sekilde `serverMod/` da bu
  ortamda Architectury/Loom ile derlenip dogrulanamadi (Fabric/Forge/NeoForge
  Maven depolarina erisim + gercek surum uyumlulugu CI'da/gercek ortamda
  test edilmelidir).
- **VPS henuz kurulmadi**: `deploy/` klasoru genel gecer bir sablon; gercek
  bir sunucuya baglanmasi icin `deploy/README.md`'deki adimlarin (VPS acma,
  SSH anahtari, GitHub Secrets) tamamlanmasi gerekiyor.
- Mod Studio: Blockbench parser, blok editoru ve code-gen paketleri
  (`modstudio/blockbench`, `modstudio/blocks`, `modstudio/codegen`) henuz bos;
  bunlarin `serverMod/common/GeneratedModules.java`'i uretmesi bir sonraki
  adimda ele alinacak.
