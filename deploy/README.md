# Sunucu Barındırma ve Otomatik Publish/Restart Kurulumu

Bu klasör, "Mod Studio'da Publish'e bas → sunucu otomatik güncellensin ve
restart olsun" akışının **sunucu tarafını** içerir. Telefondan yapman gereken
tek şey Claude Code üzerinden değişiklik yaptırıp push etmek; derleme ve
deploy `.github/workflows/publish-mod.yml` içinde bulutta çalışır.

## 1) Ücretsiz/ucuz bir VPS edinin

Önerilen: **Oracle Cloud Free Tier** (Always Free ARM Ampere instance, 4 vCPU/24GB
RAM'e kadar ücretsiz) — kredi kartı istese de ücretlendirmez, öğrenci projeleri
için yeterince güçlü. Alternatif: herhangi bir 5-10$/ay KVM VPS (Hetzner,
Contabo vb.). Kurulum adımları:

1. VPS'i Ubuntu 22.04/24.04 ile oluşturun.
2. SSH ile bağlanıp Java 21 kurun: `sudo apt update && sudo apt install -y openjdk-21-jre-headless`
3. Deploy için ayrı, sınırlı yetkili bir kullanıcı oluşturun:
   ```
   sudo useradd -m -s /bin/bash mcserver
   sudo useradd -m -s /bin/bash deploy
   sudo usermod -aG mcserver deploy
   ```
4. Her loader için bir dizin açın ve resmi kurulum araçlarıyla sunucuyu kurun
   (bu adım loader'a göre değişir, resmi Fabric/Forge/NeoForge/Vanilla
   installer'larını kullanın):
   ```
   sudo mkdir -p /opt/pokewing-mc/{fabric,forge,neoforge,vanilla}
   sudo chown -R mcserver:mcserver /opt/pokewing-mc
   ```
   Her klasöre `deploy/server_start_templates/<loader>.sh` dosyasını
   `server_start.sh` adıyla kopyalayıp `chmod +x` yapın; `eula.txt` içine
   `eula=true` yazmayı unutmayın (Mojang EULA onayı).
5. Systemd servisini kurun:
   ```
   sudo cp deploy/pokewing-mc@.service /etc/systemd/system/
   sudo systemctl daemon-reload
   sudo systemctl enable --now pokewing-mc@fabric   # kullandığınız loader(lar) için tekrarlayın
   ```
6. `deploy` kullanıcısının `systemctl start/stop` ve dosya kopyalama yapabilmesi
   için sudoers'a sınırlı bir kural ekleyin:
   ```
   echo 'deploy ALL=(mcserver) NOPASSWD: /usr/bin/systemctl start pokewing-mc@*, /usr/bin/systemctl stop pokewing-mc@*' \
     | sudo tee /etc/sudoers.d/pokewing-deploy
   ```

## 2) SSH anahtarı ve GitHub Secrets

VPS üzerinde `deploy` kullanıcısı için bir SSH anahtar çifti oluşturup genel
anahtarı `~deploy/.ssh/authorized_keys` içine ekleyin. Özel anahtarı GitHub
reponuzun **Settings → Secrets and variables → Actions** kısmına şu isimlerle
ekleyin (hepsi telefondan GitHub mobil sitesi/uygulaması üzerinden yapılabilir):

| Secret adı | Değer |
|---|---|
| `VPS_HOST` | VPS'in IP adresi veya alan adı |
| `VPS_USER` | `deploy` |
| `VPS_SSH_KEY` | `deploy` kullanıcısının özel SSH anahtarı (tam içerik) |
| `VPS_PORT` | SSH portu (genelde `22`) |
| `VPS_LOADER` | `fabric`, `forge`, `neoforge` veya `vanilla` (şimdilik tek loader) |
| `VPS_SERVER_DIR` | örn. `/opt/pokewing-mc/fabric` |

## 3) Akışın tamamı

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
