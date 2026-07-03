# Sunucu Barındırma ve Otomatik Publish/Restart Kurulumu

Bu klasör, "Mod Studio'da Publish'e bas → sunucu otomatik güncellensin ve
restart olsun" akışının **sunucu tarafını** içerir. Telefondan yapman gereken
tek şey Claude Code üzerinden değişiklik yaptırıp push etmek; derleme ve
deploy `.github/workflows/publish-mod.yml` içinde bulutta çalışır.

## 1) Ücretsiz/ucuz bir VPS edinin

Önerilen: **Oracle Cloud Free Tier** (Always Free ARM Ampere instance, 4 vCPU/24GB
RAM'e kadar ücretsiz) — kredi kartı istese de ücretlendirmez, öğrenci projeleri
için yeterince güçlü. Alternatif: herhangi bir 5-10$/ay KVM VPS (Hetzner,
Contabo vb.). VPS'i Ubuntu 22.04/24.04 ile oluşturun; başka bir şey yapmanıza
gerek yok, geri kalanı aşağıdaki otomatik kurulum ile yapılır.

## 2) Otomatik kurulum: `bootstrap-vps.yml` (telefondan, tek tık)

Bilgisayarınız yoksa bile bu adımı GitHub'ın mobil sitesinden/uygulamasından
yapabilirsiniz — sunucuya SSH ile bağlanmanıza gerek yok, GitHub Actions bunu
sizin için yapar.

1. Repo **Settings → Secrets and variables → Actions** kısmına şu **geçici**
   secret'ları ekleyin:

   | Secret adı | Değer |
   |---|---|
   | `VPS_HOST` | VPS'in IP adresi veya alan adı |
   | `VPS_PORT` | SSH portu (genelde `22`) |
   | `VPS_ROOT_PASSWORD` | VPS'in **root** parolası (sadece bu tek seferlik kurulum için) |

2. **Actions** sekmesinden **"Bootstrap VPS (tek seferlik kurulum)"**
   workflow'unu bulup **Run workflow** ile elle çalıştırın.
3. Workflow otomatik olarak: Java 21'i kurar, `mcserver`/`deploy` kullanıcılarını
   oluşturur, `/opt/pokewing-mc/{fabric,forge,neoforge,vanilla}` dizinlerini
   hazırlar, systemd servisini + loader başlatma script'lerini kurar ve
   `deploy` kullanıcısına **sadece anahtar ile** giriş izni tanımlar
   (`deploy/authorized_keys/deploy_key.pub` — bu genel anahtar repoda
   saklanır, gizli değildir).
4. Bu genel anahtarın **özel eşi** ayrıca (bu depoya commitlenmez, size sohbet
   üzerinden verilir) — onu aşağıdaki secret'lara ekleyin:

   | Secret adı | Değer |
   |---|---|
   | `VPS_USER` | `deploy` |
   | `VPS_SSH_KEY` | size verilen özel SSH anahtarının tam içeriği |
   | `VPS_LOADER` | `fabric`, `forge`, `neoforge` veya `vanilla` (şimdilik tek loader) |
   | `VPS_SERVER_DIR` | örn. `/opt/pokewing-mc/fabric` |

5. **Önemli güvenlik adımı:** `VPS_SSH_KEY` ile bağlantının çalıştığını
   doğruladıktan sonra, VPS sağlayıcınızın panelinden/konsolundan **root
   parolasını değiştirin** ve `VPS_ROOT_PASSWORD` secret'ını GitHub'dan silin.
   Bootstrap script'i kasıtlı olarak parola girişini kapatmıyor/root'u
   kilitlemiyor (deploy anahtarı beklenmedik şekilde çalışmazsa sizi kendi
   sunucunuzdan tamamen dışarıda bırakabilirdi) — bu yüzden parolayı elle
   değiştirmeniz gerekiyor.

### Alternatif: elle kurulum

Otomasyonu kullanmak istemiyorsanız, `deploy/bootstrap.sh` dosyasındaki
adımları (Java kurulumu, kullanıcı oluşturma, systemd servisi, sudoers kuralı)
telefondan bir SSH uygulamasıyla (Termux, Termius, JuiceSSH) veya
bilgisayarınızdan elle de çalıştırabilirsiniz; script'in kendisi okunabilir
ve yorum satırlarıyla açıklanmıştır.

## 3) Fabric sunucu jar'ını kur (otomatik)

Bootstrap sadece işletim sistemi/kullanıcı/systemd katmanını hazırlar; asıl
Minecraft sunucu dosyaları (fabric-server-launch.jar vb.) ayrı bir adımda
kurulur — bu da telefondan, tek tık ile yapılabilir:

1. Yukarıdaki `VPS_SSH_KEY`, `VPS_USER`, `VPS_SERVER_DIR`, `VPS_LOADER`
   secret'larının eklendiğinden emin olun (adım 2'nin sonundaki tablo).
2. **Actions** → **"Sunucuya Fabric Server Jar Kur"** → **Run workflow**
   (istersen `mc_version` girdisini değiştirebilirsin, varsayılan `1.20.1`).
3. Workflow resmi Fabric installer'ını indirir, `fabric-server-launch.jar` +
   `libraries/` + vanilla `server.jar`'ı üretir, VPS'teki `VPS_SERVER_DIR`
   içine gönderir ve sunucuyu başlatır.

Forge/NeoForge/Vanilla seçtiyseniz benzer bir workflow gerekir (resmi
installer'ları farklı çalışır) — hangisini seçtiğinizi söylerseniz onu da
ekleyebilirim; şimdilik yalnızca Fabric için hazır.

## 4) Akışın tamamı

1. Sen (veya öğrencin) Mod Studio'da bir mod üretip "Publish" der / ya da
   doğrudan `serverMod/` altında koda dokunup push edersin.
2. `.github/workflows/publish-mod.yml` tetiklenir: `serverMod` projesini
   Architectury/Loom ile derler (fabric + forge + neoforge jar'ları), datapack
   klasörünü paketler.
3. Üretilen dosyalar `scp` ile VPS'teki geçici bir klasöre (`~/incoming`) kopyalanır.
4. `deploy/deploy.sh` SSH üzerinden çalıştırılır: eski aggregator jar'ı silinir,
   yenisi `mods/` içine konur, datapack güncellenir, sunucu `systemctl` ile
   yeniden başlatılır.
5. Öğrenciler (bilgisayarı olan/olmayan hepsi) sunucunun IP'sine bağlanıp
   güncel modları test eder — sen sadece telefondan push atarsın.

## Notlar

- Bu iskelet `fabric`/`forge`/`neoforge` için **aynı anda tek bir sunucu
  profilini** çalıştırmayı varsayar (`VPS_LOADER` secret'ı ile seçilir); aynı
  anda birden fazla loader'ı ayrı portlarda çalıştırmak istersen workflow'u
  matrix'e çevirip her loader için ayrı `VPS_SERVER_DIR`/port tanımlarsın.
- Vanilla + datapack modunda mod jar'ı deploy edilmez, sadece
  `serverMod/datapacks/pokewing_classroom` klasörü senkronize edilir.
