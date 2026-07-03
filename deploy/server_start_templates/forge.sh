#!/usr/bin/env bash
# /opt/pokewing-mc/forge/server_start.sh olarak kopyalanip calistirilir.
# Forge'un resmi installer'i ile "--installServer" kurulduktan sonra
# olusan run.sh/args dosyalarina gore ayarlanmalidir (Forge surumune gore degisir).
cd "$(dirname "$0")" || exit 1
exec java -Xms1G -Xmx3G -XX:+UseG1GC -XX:MaxGCPauseMillis=50 \
  @libraries/net/minecraftforge/forge/*/unix_args.txt nogui
