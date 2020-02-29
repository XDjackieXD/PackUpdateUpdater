package at.chaosfield.updaterupdater;

import at.chaosfield.updaterupdater.api.UpdaterAPI;
import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.awt.*;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Properties;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

/**
 * Created by Jakob (XDjackieXD) Riepler
 */
public class UpdaterUpdater {

    public static final String PROJECT_NAME = "PackUpdateUpdater";

    public static void main(String[] args) {
        try {
            new UpdaterUpdater().run(args);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getOwnVersion() throws IOException {
        Enumeration<URL> resources = getClass().getClassLoader().getResources("META-INF/MANIFEST.MF");
        while (resources.hasMoreElements()) {
            URL url = resources.nextElement();
            Manifest manifest = new Manifest(url.openStream());

            String implTitle = manifest.getMainAttributes().getValue("Implementation-Title");

            if (implTitle != null && implTitle.equals(PROJECT_NAME)) {
                return manifest.getMainAttributes().getValue("Implementation-Version");
            }
        }
        return "Unknown";
    }

    public void run(String[] args) throws IOException {

        String selfVersion = getOwnVersion();

        System.out.println("PackUpdateUpdater Version: " + selfVersion);

        String version = null;

        String mmcDir = args[0].equals("client") ? System.getenv("INST_MC_DIR") : ".";

        if (mmcDir == null) {
            System.err.println("This program is intended to run as MultiMC Pre-Launch Hook");
            System.exit(1);
        }

        File baseFile = new File(mmcDir);
        File packupdateDir = new File(baseFile, "packupdate");
        if (!packupdateDir.exists()) {
            if (!packupdateDir.mkdirs()) {
                System.err.println("Could not create packupdate data directory");
            }
        }

        File packupdateFile = new File(packupdateDir, "PackUpdate.jar");
        File propertyFile = new File(packupdateDir, "updater.properties");

        Properties prop = new Properties();
        prop.setProperty("beta", "false");
        prop.setProperty("forceVersion", "");
        prop.setProperty("apiUrl", "https://api.github.com/repos/XDjackieXD/PackUpdate/releases");

        try {
            if (propertyFile.exists()) {
                prop.load(new FileReader(propertyFile));
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Could not read settings, exiting");
        }

        boolean beta = prop.getProperty("beta").equalsIgnoreCase("true");
        String force_version = prop.getProperty("forceVersion");
        URL apiUrl = new URL(prop.getProperty("apiUrl"));

        if (force_version.equals("")) {
            force_version = null;
        }

        try {

            if (packupdateFile.exists()) {
                Manifest manifest = new JarFile(packupdateFile).getManifest();
                version = manifest.getMainAttributes().getValue("Implementation-Version");
            }


        } catch (IOException e) {
            System.out.println("[PackUpdate Updater] Warning: could not find original PackUpdate. Downloading it now.");
        }

        try {
            if (version == null || hasUpdate(version, beta, force_version, apiUrl)) {
                System.out.println("PackUpdate Update available, downloading...");
                if (!downloadPackUpdate(packupdateFile.getPath(), beta, force_version, apiUrl)) {
                    System.err.println("[PackUpdate Updater] Update Failed.");
                }
            }
        } catch (IOException e) {
            System.err.println("[PackUpdate Updater] Update Failed.");
            e.printStackTrace();
        }

        try {
            prop.store(new FileWriter(propertyFile), "PackUpdate Updater settings");
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Could not store properties");
        }

        String mainClass = null;

        try {
            if (packupdateFile.exists()) {
                Manifest manifest = new JarFile(packupdateFile).getManifest();
                mainClass = manifest.getMainAttributes().getValue("Main-Class");
                if (mainClass == null) {
                    System.err.println("[PackUpdate Updater] Downloaded packupdate jar lacks main class. Can't continue.");
                    System.exit(1);
                }
            } else {
                System.err.println("[PackUpdate Updater] Could not find the file i just downloaded. Refusing to perform.");
                System.exit(1);
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("[PackUpdate Updater] Everything is broken, call for help!");
            System.exit(1);
        }

        try {
            UpdaterClassLoader loader = new UpdaterClassLoader();
            UpdaterAPI.setClassLoader(loader);
            loader.addURL(packupdateFile.toURI().toURL());
            Method mainMethod = loader.loadClass(mainClass).getDeclaredMethod("main", String[].class);
            mainMethod.invoke(null, (Object) args);
        } catch (IOException | InvocationTargetException | NoSuchMethodException | IllegalAccessException | ClassNotFoundException e) {
            System.out.println("[PackUpdate Updater] Execution of PackUpdater failed");
            e.printStackTrace();
        }

    }

    public static JSONObject getReleaseForBranch(boolean beta, String force_version, URL apiUrl) throws IOException {
         JSONArray data = getJSON(apiUrl);
         for (Object foo: data) {
             JSONObject release = (JSONObject) foo;
             if (force_version == null) {
                 if (release.getBoolean("draft")) {
                     continue;
                 }

                 if (beta || !release.getBoolean("prerelease")) {
                     return release;
                 }
             } else {
                 if (release.getString("tag_name").equals(force_version)) {
                     return release;
                 }
             }
         }
         throw new RuntimeException(String.format("[PackUpdate Updater] No release found - channel: %s, force_version: %s \n", beta ? "beta" : "stable", force_version == null ? "none" : force_version));
    }

    public static boolean hasUpdate(String version, boolean beta, String force_version, URL apiUrl) throws IOException {
        if (force_version != null) {
            return !force_version.equals(version);
        }
        JSONObject jsonObject = getReleaseForBranch(beta, null, apiUrl);
        return !(jsonObject.get("tag_name")).equals(version);
    }

    public static boolean downloadPackUpdate(String path, boolean beta, String force_version, URL apiUrl) throws IOException {
        JSONObject jsonRelease = getReleaseForBranch(beta, force_version, apiUrl);

        for (Object asset: jsonRelease.getJSONArray("assets")) {
            if (((JSONObject) asset).getString("name").startsWith("PackUpdate")) {

                FileUtils.copyURLToFile(new URL(((JSONObject) asset).getString("browser_download_url")), new File(path));

                if (getLength(path) == ((JSONObject) asset).getLong("size")) {
                    return true;
                }

            }
        }
        return false;
    }

    private static JSONArray getJSON(URL url) throws IOException {
        BufferedReader release = new BufferedReader(new BufferedReader(new InputStreamReader(url.openStream())));

        StringBuilder responseStrBuilder = new StringBuilder();
        String inputStr;
        while ((inputStr = release.readLine()) != null) {
            responseStrBuilder.append(inputStr);
        }

        return new JSONArray(responseStrBuilder.toString());
    }

    public static long getLength(String filePath) throws IOException {
        File file = new File(filePath);
        if (file.exists()) {
            return file.length();
        }
        return 0;
    }
}
