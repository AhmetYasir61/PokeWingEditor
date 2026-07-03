package com.pokewing.launcher.jvm;

/**
 * Native (C++) katmanindan Java tarafina JVM yasam dongusu bildirimleri.
 * Tum metodlar native thread uzerinden cagrilir; UI guncellemesi yapan
 * implementasyonlar cagriyi ana thread'e (Handler/runOnUiThread) tasimalidir.
 */
public interface JvmCallback {

    void onLog(String line);

    void onJvmReady();

    void onGameExit(int exitCode);

    void onFatalError(String message);
}
