# PokeWing Launcher

PojavLauncher benzeri, Android native (Java + NDK) bir "Runtime & Launcher" ve
"Mod Studio" mimarisinin temel iskeleti.

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

## Bilinen Eksikler / Sonraki Adimlar

- **Gomulu JRE saglanmasi**: `jvm_bridge.cpp` su an sadece `libjvm.so`'yu
  `dlopen` ediyor; gercek classloading ve `main()` cagrisi (Reflection ile)
  bir sonraki adimda eklenecek. Uygun bir Android-uyumlu OpenJDK/JRE portu
  (ör. Zulu Embedded, ya da topluluk derlemeleri) saglanmalidir.
- **Gradle wrapper jar'i**: Bu ortamda `services.gradle.org`'a giden HEAD
  istegi 403 dondugu icin `gradle-wrapper.jar` binary'si otomatik uretilemedi.
  Projeyi Android Studio'da actiginizda "Gradle wrapper olustur" istemini
  kabul edin ya da terminalde `gradle wrapper --gradle-version 8.7` calistirin.
- **Android SDK/NDK bu ortamda kurulu degil**, dolayisiyla iskelet
  derleme (gradle build) ile dogrulanamadi; Android Studio'da acildiginda
  SDK/NDK bilesenlerinin indirilmesi gerekecek.
- Mod Studio: Blockbench parser, blok editoru ve code-gen paketleri
  (`modstudio/blockbench`, `modstudio/blocks`, `modstudio/codegen`) henuz bos;
  bir sonraki adimda doldurulacak.
