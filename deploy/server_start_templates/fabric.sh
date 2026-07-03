#!/usr/bin/env bash
# /opt/pokewing-mc/fabric/server_start.sh olarak kopyalanip calistirilir.
# fabric-server-launch.jar, Fabric'in resmi installer'i ile onceden kurulmus olmalidir:
#   java -jar fabric-installer.jar server -mcversion 1.20.1 -downloadMinecraft
cd "$(dirname "$0")" || exit 1
exec java -Xms1G -Xmx3G -XX:+UseG1GC -XX:MaxGCPauseMillis=50 \
  -jar fabric-server-launch.jar nogui
