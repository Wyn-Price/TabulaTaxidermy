package com.wynprice.taxidermy;

import net.minecraftforge.fml.ModList;
import org.lwjgl.system.Platform;
import org.lwjgl.util.nfd.NativeFileDialog;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * If you're looking a this class for anything, good luck.
 *
 * Essentially this class allows the hacky loading of the lwjgl-nfd library.
 * Bundled with this jar will be the native files and the nfd jar.
 *
 * Essentially, we extract the native we require, and place it into a folder, then
 * Extract the jar and place it into that same folder.
 *
 * We then add that folder as the system libraries path (and invalidate cache), and load
 * the jar with the url classloader.
 *
 * @author Wyn Price
 */
public class TaxidermyNativeFileDialog {
    public static void tryLoad() {
        Path source = ModList.get().getModFileById(Taxidermy.MODID).getFile().getFilePath();
        //In dev, it works as expected and we don't need to mess around
        if(Files.isRegularFile(source)) {
            //Make sure teh source is actually a file. This shouldn't throw.
            try {
                source.toFile();
            } catch (UnsupportedOperationException e) {
                throw new IllegalStateException("Tried to load file dialogs, but file doesn't exist on default system? " + source, e);
            }
            try {
                Path destFolder = source.getParent().resolve("taxidermy_native_file_dialog");
                Path jar = extractNativeAndJar(source, destFolder);
                editSysPropertyAndLoadJar(destFolder, jar);

                Taxidermy.getLogger().debug("Loading successful Attempting link...");
                NativeFileDialog.NFD_GetError();
            } catch (IOException e) {
                throw new IllegalStateException("Unable to create native file dialog bindings. For now this is needed", e);
            }
        }
    }

    /**
     * Extract the natives from the jar and into the `mods/taxidermy_native_file_dialog` file
     */
    private static Path extractNativeAndJar(Path source, Path destFolder) throws IOException {
        Taxidermy.getLogger().debug("Began Extracting NFD for: " + destFolder.toAbsolutePath().toString());

        if(Files.exists(destFolder)) {
            deleteOldDirectory(destFolder);
        }
        Files.createDirectories(destFolder);

        String fileName = System.mapLibraryName(Platform.mapLibraryNameBundled("lwjgl_nfd"));
        Path path = destFolder.resolve(fileName);
        Path jar = destFolder.resolve("lwjgl-nfd-3.2.2.jar");
        Taxidermy.getLogger().debug("Resolved native {} at path {} with jar {}", fileName, path, jar);

        try(FileSystem system = FileSystems.newFileSystem(source, null)) {
            Files.copy(system.getPath("lwjgl_natives", fileName), path);
            Files.copy(system.getPath("lwjgl_natives", "lwjgl-nfd-3.2.2.jar"), jar);
        }
        return jar;
    }

    private static void deleteOldDirectory(Path destFolder) throws IOException {
        Taxidermy.getLogger().debug("Deleting...");
        try (Stream<Path> walk = Files.walk(destFolder)) {
            for (Path path : walk.sorted(Comparator.reverseOrder()).collect(Collectors.toList())) {
                Files.delete(path);
            }
        }
    }

    /**
     * Edits the system property and loads the jar.
     */
    private static void editSysPropertyAndLoadJar(Path destFolder, Path jar) {
        String str = "java.library.path";
        String old = System.getProperty(str);
        System.setProperty(str, System.getProperty(str) + File.pathSeparator + destFolder.toAbsolutePath().toString());
        Taxidermy.getLogger().debug("Changed system property '{}' from '{}' to '{}'", str, old, System.getProperty(str));

        try {
            URL url = jar.toUri().toURL();
            URLClassLoader classLoader = (URLClassLoader)ClassLoader.getSystemClassLoader();
            Method method = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
            method.setAccessible(true);
            method.invoke(classLoader, url);

            Field field = ClassLoader.class.getDeclaredField("sys_paths");
            field.setAccessible(true);
            field.set(null, null);
        } catch (Exception e) {
            throw new IllegalStateException("Unable to add jar to classloader", e);
        }
    }
}
