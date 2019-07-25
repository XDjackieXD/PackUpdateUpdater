package at.chaosfield.updaterupdater;

import at.chaosfield.updaterupdater.api.PackUpdateClassLoader;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

public class UpdaterClassLoader extends ClassLoader implements PackUpdateClassLoader {

    private List<ClassLoader> classLoaders = new ArrayList<>();
    private URLClassLoader urlClassLoader;
    private Method addURLMethod;

    public UpdaterClassLoader() {
        ClassLoader parentLoader = this.getClass().getClassLoader();
        classLoaders.add(parentLoader);
        if (parentLoader instanceof URLClassLoader) {
            urlClassLoader = (URLClassLoader) parentLoader;
        } else {
            urlClassLoader = new URLClassLoader(new URL[0]);
            classLoaders.add(urlClassLoader);
        }
        try {
            addURLMethod = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
            addURLMethod.setAccessible(true);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void addClassLoader(ClassLoader loader) {
        classLoaders.add(loader);
    }

    @Override
    public void addURL(URL url) {
        try {
            addURLMethod.invoke(urlClassLoader, url);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    @Override
    public ClassLoader asClassLoader() {
        return this;
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        for (ClassLoader loader : classLoaders) {
            try {
                Class klass = loader.loadClass(name);
                if (resolve) {
                    resolveClass(klass);
                }
                return klass;
            } catch (ClassNotFoundException ignored) {
            }
        }

        throw new ClassNotFoundException(name);
    }
}
