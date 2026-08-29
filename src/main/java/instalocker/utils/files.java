package instalocker.utils;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class files {

    public static void downloadFile(String url, File path) {
        try (BufferedInputStream bufferedInputStream = new BufferedInputStream(new URL(url).openStream())) {
            try (BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(Files.newOutputStream(path.toPath()))) {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = bufferedInputStream.read(buffer)) != -1) {
                    bufferedOutputStream.write(buffer, 0, bytesRead);
                }
                bufferedOutputStream.flush();
                bufferedInputStream.close();
            }
        } catch (Exception ignored) {}
    }

    public static void unzip(String path, String destinationPath) {
        File file = new File(destinationPath);
        if (!file.exists()) {
            file.mkdirs();
        }
        byte[] buffer = new byte[1024];
        try {
            FileInputStream fileInputStream = new FileInputStream(path);
            ZipInputStream zipInputStream = new ZipInputStream(fileInputStream);
            ZipEntry zipEntry = zipInputStream.getNextEntry();
            while (zipEntry != null) {
                String fileName = zipEntry.getName();
                File newFile = new File(destinationPath + File.separator + fileName);
                if (zipEntry.isDirectory()) {
                    newFile.mkdirs();
                    zipEntry = zipInputStream.getNextEntry();
                    continue;
                }
                new File(newFile.getParent()).mkdirs();
                FileOutputStream fileOutputStream = new FileOutputStream(newFile);
                int len;
                while ((len = zipInputStream.read(buffer)) > 0) {
                    fileOutputStream.write(buffer, 0, len);
                }
                fileOutputStream.close();
                zipInputStream.closeEntry();
                zipEntry = zipInputStream.getNextEntry();
            }
            zipInputStream.closeEntry();
            zipInputStream.close();
            fileInputStream.close();
        } catch (Exception ignored) {}
    }
}