package at.chaosfield.updaterupdater.api;

public class UpdaterAPI {
    private static PackUpdateClassLoader classLoader;

    public static PackUpdateClassLoader classLoader() {
        if (classLoader == null) {
            throw new NullPointerException("UpdaterAPI.classLoader was not set prior to calling");
        }

        return classLoader;
    }

    public static boolean classLoaderIsSet() {
        return classLoader != null;
    }

    public static void setClassLoader(PackUpdateClassLoader classLoader) {
        if (UpdaterAPI.classLoader != null) {
            throw new RuntimeException("Cannot set UpdaterAPI.classLoader twice!");
        }
        UpdaterAPI.classLoader = classLoader;
    }
}
