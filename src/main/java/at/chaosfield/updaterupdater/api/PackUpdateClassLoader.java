package at.chaosfield.updaterupdater.api;

import java.net.URL;

public interface PackUpdateClassLoader {
    void addClassLoader(ClassLoader loader);

    void addURL(URL url);

    ClassLoader asClassLoader();
}
