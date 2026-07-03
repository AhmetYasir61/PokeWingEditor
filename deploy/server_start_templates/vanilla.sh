#!/usr/bin/env bash
# /opt/pokewing-mc/vanilla/server_start.sh olarak kopyalanip calistirilir.
# server.jar, mojang.com/eula uyarinca kabul edilmis bir eula.txt ile
# ayni klasorde bulunmalidir. Bu profilde mod yuklenmez; sadece
# datapacks/ altina kopyalanan ogrenci datapack'leri aktiftir.
cd "$(dirname "$0")" || exit 1
exec java -Xms1G -Xmx3G -XX:+UseG1GC -XX:MaxGCPauseMillis=50 \
  -jar server.jar nogui
