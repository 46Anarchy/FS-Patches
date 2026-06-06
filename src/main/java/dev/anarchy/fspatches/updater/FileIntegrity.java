package dev.anarchy.fspatches.updater;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import lombok.Cleanup;
import lombok.Getter;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

import javax.annotation.Nonnull;
import java.io.*;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

@UtilityClass
public class FileIntegrity {

    private static final String CDN_URL = "https://cdn.paladium-pvp.fr/games/paladiumv2/paladium.json";
    private static final int MAX_RETRIES = 5;
    private static final int THREADS = Math.max(4, Runtime.getRuntime().availableProcessors());

    private static final ConcurrentHashMap<String, DownloadProgress> ACTIVE_DOWNLOADS = new ConcurrentHashMap<>();
    private static final Set<String> COMPLETED_DOWNLOADS = ConcurrentHashMap.newKeySet();
    private static volatile int TOTAL_FILES = 0;

    private static final Set<String> BLACKLISTED_MODELS = new HashSet<>(Arrays.asList(
            "natives-windows", "natives-macos", "natives-linux",
            "versions", "java-windows", "java-macos", "java-linux"
    ));

    private static final Set<String> BLACKLISTED_FILES = new HashSet<>(Arrays.asList(
            "palaforge-1.7.10-10.13.4.2512.jar"
    ));

    @SneakyThrows
    public static String getSHA1(@NonNull File file) {
        if (!file.exists() || !file.canRead() || file.isDirectory())
            return "";

        byte[] data = Files.readAllBytes(Paths.get(file.getAbsolutePath()));
        byte[] hash = MessageDigest.getInstance("SHA1").digest(data);
        String sha1 = new BigInteger(1, hash).toString(16);

        while (sha1.length() < 40)
            sha1 = "0" + sha1;

        return sha1;
    }

    @SneakyThrows
    private static void downloadLastCDNJson() {
        HttpURLConnection connection = (HttpURLConnection) new URL(CDN_URL).openConnection();
        connection.setConnectTimeout(10000);
        connection.setReadTimeout(10000);
        connection.setRequestMethod("GET");
        connection.connect();

        if (connection.getResponseCode() != 200)
            throw new IOException("HTTP " + connection.getResponseCode());

        File output = new File(".cdn_new.json");
        if (output.exists())
            output.delete();

        @Cleanup InputStream inputStream = connection.getInputStream();
        @Cleanup FileOutputStream fos = new FileOutputStream(output);

        byte[] buffer = new byte[8192];
        int read;
        while ((read = inputStream.read(buffer)) != -1)
            fos.write(buffer, 0, read);
    }

    @SneakyThrows
    public static CDNSTATUS areFilesPresent() {
        File oldJson = new File(".cdn_last.json");
        File newJson = new File(".cdn_new.json");

        if (!oldJson.exists()) {
            try {
                downloadLastCDNJson();
            } catch (Exception ignored) {
                return CDNSTATUS.OK;
            }
            return CDNSTATUS.INIT;
        }

        try {
            downloadLastCDNJson();
        } catch (Exception e) {
            e.printStackTrace();
            return CDNSTATUS.CDN_FAILURE;
        }

        if (!newJson.exists() || !isValidCDNFile(newJson))
            return CDNSTATUS.CDN_FAILURE;

        if (getSHA1(oldJson).equalsIgnoreCase(getSHA1(newJson)) && performSha1()) {
            newJson.delete();
            return CDNSTATUS.OK;
        }

        return CDNSTATUS.OUT_OF_DATE;
    }

    @SneakyThrows
    private static boolean isValidCDNFile(File file) {
        if (!file.exists() || file.length() == 0)
            return false;

        try {
            @Cleanup FileReader reader = new FileReader(file);
            Map<?, ?> json = new Gson().fromJson(reader, Map.class);
            return json != null && json.containsKey("models") && json.containsKey("files");
        } catch (JsonSyntaxException e) {
            return false;
        }
    }

    @SneakyThrows
    public static List<DownloadFile> buildDownloadList(File manifestFile, File gameDirectory) {
        Manifest manifest = new Gson().fromJson(new FileReader(manifestFile), Manifest.class);

        Map<String, Model> modelMap = new HashMap<>();
        if (manifest.models != null)
            for (Model model : manifest.models)
                modelMap.put(model.name, model);

        List<DownloadFile> downloads = new ArrayList<>();
        if (manifest.files == null)
            return downloads;

        for (ManifestFile file : manifest.files) {
            if (BLACKLISTED_MODELS.contains(file.model))
                continue;

            Model model = modelMap.get(file.model);
            if (model == null)
                continue;

            downloads.add(new DownloadFile(file.name, file.url, file.sha1, getFile(gameDirectory, file, model)));
        }

        return downloads;
    }

    @Nonnull
    private static File getFile(File gameDirectory, ManifestFile file, Model model) {
        String baseFolder = (file.model.equals("libraries-windows")
                || file.model.equals("libraries-macos")
                || file.model.equals("libraries-linux"))
                ? "mods"
                : model.dest;

        String filePath = file.path != null && file.path.endsWith(".pala")
                ? file.path.substring(0, file.path.length() - 5) + ".jar"
                : file.path;

        return new File(gameDirectory, baseFolder + File.separator + filePath);
    }

    @SneakyThrows
    public static boolean performSha1() {
        File manifestFile = new File(".cdn_new.json");
        if (!manifestFile.exists())
            manifestFile = new File(".cdn_last.json");
        if (!manifestFile.exists())
            return false;

        Manifest manifest = new Gson().fromJson(new FileReader(manifestFile), Manifest.class);
        if (manifest == null || manifest.models == null || manifest.files == null)
            return false;

        Map<String, Model> modelMap = new HashMap<>();
        for (Model model : manifest.models)
            modelMap.put(model.name, model);

        for (ManifestFile file : manifest.files) {
            String resolvedModel = file.model.contains("libraries") ? "mods" : file.model;

            if (BLACKLISTED_MODELS.contains(resolvedModel))
                continue;

            Model model = modelMap.get(resolvedModel);
            if (model == null)
                continue;

            String filePath = file.path != null && file.path.endsWith(".pala")
                    ? file.path.substring(0, file.path.length() - 5) + ".jar"
                    : file.path;

            String fileName = new File(filePath).getName();
            if (BLACKLISTED_FILES.contains(fileName))
                continue;

            File target = new File("./", model.dest + File.separator + filePath);
            if (!target.exists())
                return false;

            if (!getSHA1(target).equalsIgnoreCase(file.sha1))
                return false;
        }

        return true;
    }

    public static void downloadFiles(List<DownloadFile> files) throws DownloadCouldntCompleteException {
        TOTAL_FILES = files.size();
        ACTIVE_DOWNLOADS.clear();
        COMPLETED_DOWNLOADS.clear();

        AtomicBoolean cancelled = new AtomicBoolean(false);
        ExecutorService executor = Executors.newFixedThreadPool(THREADS);
        List<Future<?>> futures = new ArrayList<>();

        for (DownloadFile file : files) {
            File parent = file.getDestination().getParentFile();
            if (parent != null && !parent.exists())
                parent.mkdirs();
        }

        for (final DownloadFile file : files) {
            futures.add(executor.submit((Callable<Void>) () -> {
                if (!cancelled.get())
                    downloadWithRetry(file, cancelled);
                return null;
            }));
        }

        executor.shutdown();

        DownloadCouldntCompleteException firstError = null;
        for (Future<?> future : futures) {
            try {
                future.get();
            } catch (ExecutionException e) {
                Throwable cause = e.getCause();
                if (cause instanceof DownloadCouldntCompleteException && firstError == null) {
                    firstError = (DownloadCouldntCompleteException) cause;
                    cancelled.set(true);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        if (firstError != null) {
            for (DownloadFile file : files) {
                File tmp = new File(file.getDestination().getAbsolutePath() + ".tmp");
                if (tmp.exists())
                    tmp.delete();
            }
            throw firstError;
        }

        for (DownloadFile file : files) {
            File tmp = new File(file.getDestination().getAbsolutePath() + ".tmp");
            if (tmp.exists()) {
                if (file.getDestination().exists())
                    file.getDestination().delete();
                tmp.renameTo(file.getDestination());
            }
        }
    }

    private static void downloadWithRetry(DownloadFile file, AtomicBoolean cancelled)
            throws DownloadCouldntCompleteException {

        File tmp = new File(file.getDestination().getAbsolutePath() + ".tmp");

        if (file.getDestination().exists()
                && getSHA1(file.getDestination()).equalsIgnoreCase(file.sha1)) {
            COMPLETED_DOWNLOADS.add(file.name);
            return;
        }

        List<String> errors = new ArrayList<>();

        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            if (cancelled.get()) {
                if (tmp.exists())
                    tmp.delete();
                return;
            }

            try {
                downloadFile(file);

                String sha = getSHA1(tmp);
                if (!sha.equalsIgnoreCase(file.sha1)) {
                    tmp.delete();
                    throw new IOException("SHA1 mismatch");
                }

                COMPLETED_DOWNLOADS.add(file.name);
                return;
            } catch (Exception e) {
                errors.add("Attempt " + attempt + ": " + e.getMessage());
                if (tmp.exists())
                    tmp.delete();
                try {
                    Thread.sleep(1000L * attempt);
                } catch (InterruptedException ignored) {}
            }
        }

        ACTIVE_DOWNLOADS.remove(file.name);
        throw new DownloadCouldntCompleteException(file.name, errors);
    }

    @SneakyThrows
    private static void downloadFile(DownloadFile file) {
        HttpURLConnection connection = (HttpURLConnection) new URL(file.url).openConnection();
        connection.setConnectTimeout(10000);
        connection.setReadTimeout(30000);
        connection.setRequestMethod("GET");
        connection.connect();

        if (connection.getResponseCode() != 200)
            throw new IOException("HTTP " + connection.getResponseCode());

        long totalBytes = connection.getContentLengthLong();
        File temp = new File(file.destination.getAbsolutePath() + ".tmp");
        if (temp.exists())
            temp.delete();

        @Cleanup InputStream inputStream = connection.getInputStream();
        @Cleanup FileOutputStream fos = new FileOutputStream(temp);

        byte[] buffer = new byte[8192];
        int read;
        long downloadedBytes = 0;

        ACTIVE_DOWNLOADS.put(file.name, new DownloadProgress(file.name, 0, totalBytes, 0D));

        while ((read = inputStream.read(buffer)) != -1) {
            fos.write(buffer, 0, read);
            downloadedBytes += read;
            double percentage = totalBytes <= 0 ? 0D : ((double) downloadedBytes / totalBytes) * 100D;
            ACTIVE_DOWNLOADS.put(file.name, new DownloadProgress(file.name, downloadedBytes, totalBytes, percentage));
        }

        ACTIVE_DOWNLOADS.remove(file.name);
    }

    public static List<DownloadProgress> getCurrentDownloads() {
        return new ArrayList<>(ACTIVE_DOWNLOADS.values());
    }

    public static int getDownloadedFileCount() { return COMPLETED_DOWNLOADS.size(); }
    public static int getDownloadingFileCount() { return ACTIVE_DOWNLOADS.size(); }
    public static int getRemainingFileCount()   { return TOTAL_FILES - getDownloadedFileCount() - getDownloadingFileCount(); }

    @Getter
    static class DownloadProgress {
        private final String fileName;
        private final long downloadedBytes;
        private final long totalBytes;
        private final double percentage;

        public DownloadProgress(String fileName, long downloadedBytes, long totalBytes, double percentage) {
            this.fileName = fileName;
            this.downloadedBytes = downloadedBytes;
            this.totalBytes = totalBytes;
            this.percentage = percentage;
        }
    }

    @Getter
    static class DownloadFile {
        private final String name;
        private final String url;
        private final String sha1;
        private final File destination;

        public DownloadFile(String name, String url, String sha1, File destination) {
            this.name = name;
            this.url = url;
            this.sha1 = sha1;
            this.destination = destination;
        }
    }

    @Getter
    private static class Manifest {
        private List<Model> models;
        private List<ManifestFile> files;
    }

    @Getter
    private static class Model {
        private String name;
        private String dest;
    }

    @Getter
    private static class ManifestFile {
        private String name;
        private String model;
        private String url;
        private String sha1;
        private String path;
    }
}