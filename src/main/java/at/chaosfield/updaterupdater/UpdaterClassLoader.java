package at.chaosfield.updaterupdater;

import at.chaosfield.updaterupdater.api.PackUpdateClassLoader;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

public class UpdaterClassLoader extends ClassLoader implements PackUpdateClassLoader {

    private final List<ClassLoader> classLoaders = new ArrayList<>();

    public UpdaterClassLoader() {
        ClassLoader parentLoader = this.getClass().getClassLoader();
        classLoaders.add(parentLoader);
    }

    @Override
    public void addClassLoader(ClassLoader loader) {
        classLoaders.add(loader);
    }

    @Override
    public void addURL(URL url) {
        ClassLoader cl = new URLClassLoader(new URL[] { url });
        classLoaders.add(cl);
    }

    @Override
    public ClassLoader asClassLoader() {
        return this;
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        for (ClassLoader loader : classLoaders) {
            try {
                Class<?> klass = loader.loadClass(name);
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
