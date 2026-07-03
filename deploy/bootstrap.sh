#!/usr/bin/env bash
# VPS uzerinde ROOT olarak, SADECE BIR KEZ (ilk kurulumda) calistirilir.
# .github/workflows/bootstrap-vps.yml tarafindan tetiklenir.
#
# Idempotent'tir (guvenle tekrar calistirilabilir) ama normalde bir kez
# calisip su isleri yapar:
#   1) Java 21 kurar
#   2) mcserver (sunucu prosesi) ve deploy (CI'nin baglandigi kisitli kullanici) hesaplarini olusturur
#   3) /opt/pokewing-mc/{fabric,forge,neoforge,vanilla} dizinlerini hazirlar
#   4) deploy kullanicisina SADECE anahtar ile giris izni verir (bkz. authorized_keys/deploy_key.pub)
#   5) systemd servisini ve loader baslatma script'lerini kurar
#
# GUVENLIK NOTU: bu script kasitli olarak root/parola SSH girisini KAPATMAZ
# ve root parolasini KILITLEMEZ — bu, deploy anahtari calismazsa sizi kendi
# sunucunuzdan tamamen disarida birakabilecek geri donusu zor bir islem
# oldugu icin otomatik yapilmiyor. Bootstrap basariyla tamamlandiktan ve
# deploy anahtarinin calistigini dogruladiktan SONRA, VPS saglayicinizin
# panelinden/konsolundan root parolasini kendiniz degistirin (bu script bu
# adimi sizin yerinize yapmiyor, cunku bu paylasilan gecici sifre artik
# konusma gecmisinde durdugu icin degistirilmesi onerilir).
#
# NOT (Mojang EULA): asagidaki script her loader dizinine "eula=true" yazar.
# Bu, https://www.minecraft.net/en-us/eula adresindeki Minecraft EULA'sini
# kabul ettiginiz anlamina gelir — sunucuyu kendi sinifiniz icin
# calistirdiginizi varsayarak burada otomatik yapiyoruz.
set -euo pipefail

INCOMING_DIR="${1:?incoming_dir belirtilmedi (systemd/servis dosyalarinin bulundugu klasor)}"

echo "[bootstrap] paket listesi guncelleniyor ve Java 21 kuruluyor..."
apt-get update -y
apt-get install -y openjdk-21-jre-headless

echo "[bootstrap] kullanicilar olusturuluyor..."
id -u mcserver &>/dev/null || useradd -m -s /bin/bash mcserver
id -u deploy &>/dev/null   || useradd -m -s /bin/bash deploy
usermod -aG mcserver deploy

echo "[bootstrap] sunucu dizinleri hazirlaniyor..."
mkdir -p /opt/pokewing-mc/{fabric,forge,neoforge,vanilla}
chown -R mcserver:mcserver /opt/pokewing-mc

echo "[bootstrap] deploy kullanicisi icin anahtar-tabanli SSH girisi kuruluyor..."
install -d -m 700 -o deploy -g deploy /home/deploy/.ssh
install -m 600 -o deploy -g deploy "${INCOMING_DIR}/authorized_keys/deploy_key.pub" /home/deploy/.ssh/authorized_keys

echo "[bootstrap] sudoers kurali ekleniyor..."
cat > /etc/sudoers.d/pokewing-deploy <<'EOF'
deploy ALL=(mcserver) NOPASSWD: /usr/bin/systemctl start pokewing-mc@*, /usr/bin/systemctl stop pokewing-mc@*, /usr/bin/systemctl is-active pokewing-mc@*
EOF
chmod 440 /etc/sudoers.d/pokewing-deploy

echo "[bootstrap] systemd servisi ve loader baslatma script'leri kuruluyor..."
cp "${INCOMING_DIR}/pokewing-mc@.service" /etc/systemd/system/
systemctl daemon-reload

for loader in fabric forge neoforge vanilla; do
  cp "${INCOMING_DIR}/server_start_templates/${loader}.sh" "/opt/pokewing-mc/${loader}/server_start.sh"
  chmod +x "/opt/pokewing-mc/${loader}/server_start.sh"
  echo "eula=true" > "/opt/pokewing-mc/${loader}/eula.txt"
  chown mcserver:mcserver "/opt/pokewing-mc/${loader}/server_start.sh" "/opt/pokewing-mc/${loader}/eula.txt"
done

echo "[bootstrap] TAMAMLANDI."
echo "[bootstrap] Sonraki adimlar:"
echo "[bootstrap]  1) deploy anahtarinin calistigini dogrulayin: ssh -i deploy_key deploy@<VPS_HOST>"
echo "[bootstrap]  2) Dogruladiktan sonra VPS saglayicinizin panelinden root parolasini degistirin"
echo "[bootstrap]     (bu script guvenlik geregi parola girisini kapatmiyor/kilitlemiyor, bkz. yorumlar)."
echo "[bootstrap]  3) Her loader klasorune (orn. /opt/pokewing-mc/fabric) resmi Fabric/Forge/"
echo "[bootstrap]     NeoForge/Vanilla installer'i ile gercek sunucu jar'ini kurun (deploy/README.md 4. adim)."
