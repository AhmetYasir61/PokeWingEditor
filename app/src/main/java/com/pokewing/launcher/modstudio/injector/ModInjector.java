package com.pokewing.launcher.modstudio.injector;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipException;

/**
 * Mod Studio'nun ürettiği .jar dosyasını, Runtime & Launcher modülünün taklit
 * ettiği ".minecraft/mods" dizinine güvenli şekilde yerleştirir.
 *
 * Sorumluluk sınırı: bu sınıf sadece dosya bütünlüğünü (gecerli bir JAR mi?)
 * doğrular ve kopyalar. Sınıf/paket çakışması analizi veya mod yükleyici
 * (Forge/Fabric benzeri) uyumluluğu ayrı bir katmanda ele alınacaktır.
 */
public final class ModInjector {

    public static final class InjectionException extends Exception {
        public InjectionException(String message, Throwable cause) {
            super(message, cause);
        }

        public InjectionException(String message) {
            super(message);
        }
    }

    private final File modsDir;

    public ModInjector(File modsDir) {
        if (!modsDir.isDirectory()) {
            throw new IllegalArgumentException("modsDir bir dizin degil: " + modsDir);
        }
        this.modsDir = modsDir;
    }

    /**
     * Verilen jar'i dogrular ve mods dizinine kopyalar.
     *
     * @param sourceJar Mod Studio'nun code-gen adiminda urettigi .jar dosyasi
     * @return mods dizinindeki nihai dosya
     */
    public File inject(File sourceJar) throws InjectionException {
        validateJar(sourceJar);

        File destination = new File(modsDir, sourceJar.getName());
        try (InputStream in = Files.newInputStream(sourceJar.toPath());
             FileOutputStream out = new FileOutputStream(destination)) {

            byte[] buffer = new byte[64 * 1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
        } catch (IOException e) {
            throw new InjectionException("Mod jar mods dizinine kopyalanamadi: " + sourceJar.getName(), e);
        }
        return destination;
    }

    public boolean remove(String jarFileName) {
        File target = new File(modsDir, jarFileName);
        return target.exists() && target.delete();
    }

    /**
     * Dosyanin gecerli bir JAR/ZIP arsivi oldugunu ve bos olmadigini dogrular.
     * Bozuk veya yaridan kalmis (transfer sirasinda kesilmis) dosyalarin sessizce
     * mods dizinine dusup oyunu crash ettirmesini engeller.
     */
    private void validateJar(File sourceJar) throws InjectionException {
        if (!sourceJar.exists() || sourceJar.length() == 0) {
            throw new InjectionException("Kaynak jar bulunamadi veya bos: " + sourceJar);
        }
        try (JarFile jarFile = new JarFile(sourceJar)) {
            JarEntry firstEntry = jarFile.entries().hasMoreElements()
                    ? jarFile.entries().nextElement() : null;
            if (firstEntry == null) {
                throw new InjectionException("Jar arsivi bos: " + sourceJar.getName());
            }
        } catch (ZipException e) {
            throw new InjectionException("Gecersiz/bozuk JAR arsivi: " + sourceJar.getName(), e);
        } catch (IOException e) {
            throw new InjectionException("Jar okunurken hata: " + sourceJar.getName(), e);
        }
    }
}
