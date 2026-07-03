#!/usr/bin/env bash
# /opt/pokewing-mc/neoforge/server_start.sh olarak kopyalanip calistirilir.
# NeoForge resmi installer'i ile "--installServer" kurulduktan sonra
# olusan run.sh dosyasina gore ayarlanmalidir (NeoForge surumune gore degisir).
cd "$(dirname "$0")" || exit 1
exec java -Xms1G -Xmx3G -XX:+UseG1GC -XX:MaxGCPauseMillis=50 \
  @libraries/net/neoforged/neoforge/*/unix_args.txt nogui
