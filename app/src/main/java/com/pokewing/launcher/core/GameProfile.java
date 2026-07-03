package com.pokewing.launcher.core;

import java.util.ArrayList;
import java.util.List;

/**
 * Bir baslatma oturumu icin gereken tum bilgileri tasiyan degismez (immutable) profil.
 * PojavLauncher'daki "profile" kavraminin sadelestirilmis karsiligidir.
 */
public final class GameProfile {

    public final String versionId;      // orn. "1.20.1"
    public final String username;
    public final int allocatedRamMb;
    public final int windowWidth;
    public final int windowHeight;
    public final List<String> extraJvmArgs;

    private GameProfile(Builder b) {
        this.versionId = b.versionId;
        this.username = b.username;
        this.allocatedRamMb = b.allocatedRamMb;
        this.windowWidth = b.windowWidth;
        this.windowHeight = b.windowHeight;
        this.extraJvmArgs = b.extraJvmArgs;
    }

    public static final class Builder {
        private String versionId = "1.20.1";
        private String username = "Player";
        private int allocatedRamMb = 1024;
        private int windowWidth = 1280;
        private int windowHeight = 720;
        private final List<String> extraJvmArgs = new ArrayList<>();

        public Builder versionId(String versionId) {
            this.versionId = versionId;
            return this;
        }

        public Builder username(String username) {
            this.username = username;
            return this;
        }

        public Builder allocatedRamMb(int mb) {
            this.allocatedRamMb = mb;
            return this;
        }

        public Builder windowSize(int width, int height) {
            this.windowWidth = width;
            this.windowHeight = height;
            return this;
        }

        public Builder addJvmArg(String arg) {
            this.extraJvmArgs.add(arg);
            return this;
        }

        public GameProfile build() {
            return new GameProfile(this);
        }
    }
}
