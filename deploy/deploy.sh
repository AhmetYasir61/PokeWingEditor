#!/usr/bin/env bash
# VPS uzerinde CI (GitHub Actions) tarafindan SSH ile calistirilir.
# Amac: yeni yayinlanan pokewing_aggregator jar'ini (ve varsa datapack'i) devreye
# alip Minecraft sunucusunu yeniden baslatmak — "essentials mod gibi otomatik
# guncelleme" akisinin sunucu tarafi.
#
# Kullanim:
#   deploy.sh <loader> <server_dir> <incoming_dir>
#     loader        fabric | forge | neoforge | vanilla
#     server_dir    orn. /opt/pokewing-mc/fabric
#     incoming_dir  CI'nin scp ile jar/datapack dosyalarini birakdigi gecici klasor
set -euo pipefail

LOADER="${1:?loader belirtilmedi (fabric|forge|neoforge|vanilla)}"
SERVER_DIR="${2:?server_dir belirtilmedi}"
INCOMING_DIR="${3:?incoming_dir belirtilmedi}"
SERVICE_NAME="pokewing-mc-${LOADER}"

echo "[deploy] ${SERVICE_NAME} durduruluyor..."
sudo systemctl stop "${SERVICE_NAME}" || true

if [ "${LOADER}" != "vanilla" ]; then
  MODS_DIR="${SERVER_DIR}/mods"
  mkdir -p "${MODS_DIR}"
  echo "[deploy] eski pokewing_aggregator jar'lari temizleniyor..."
  find "${MODS_DIR}" -maxdepth 1 -name "pokewing_aggregator-*.jar" -delete

  NEW_JAR=$(find "${INCOMING_DIR}" -maxdepth 1 -name "pokewing_aggregator-*.jar" | head -n1)
  if [ -z "${NEW_JAR}" ]; then
    echo "[deploy] HATA: ${INCOMING_DIR} icinde pokewing_aggregator jar'i bulunamadi." >&2
    exit 1
  fi
  cp "${NEW_JAR}" "${MODS_DIR}/"
  echo "[deploy] $(basename "${NEW_JAR}") -> ${MODS_DIR} kopyalandi."
fi

DATAPACK_SRC="${INCOMING_DIR}/pokewing_classroom"
if [ -d "${DATAPACK_SRC}" ]; then
  DATAPACKS_DIR="${SERVER_DIR}/world/datapacks"
  mkdir -p "${DATAPACKS_DIR}"
  rm -rf "${DATAPACKS_DIR:?}/pokewing_classroom"
  cp -r "${DATAPACK_SRC}" "${DATAPACKS_DIR}/"
  echo "[deploy] datapack guncellendi: ${DATAPACKS_DIR}/pokewing_classroom"
fi

echo "[deploy] ${SERVICE_NAME} yeniden baslatiliyor..."
sudo systemctl start "${SERVICE_NAME}"

sleep 5
sudo systemctl is-active --quiet "${SERVICE_NAME}" \
  && echo "[deploy] basarili: sunucu calisiyor." \
  || { echo "[deploy] HATA: sunucu baslatilamadi, loglara bakin: journalctl -u ${SERVICE_NAME} -n 100" >&2; exit 1; }
