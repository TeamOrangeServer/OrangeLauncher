/*
 * SK's Minecraft Launcher
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com> and contributors
 * Please see LICENSE.txt for license information.
 */

package com.skcraft.launcher;

import com.skcraft.launcher.bootstrap.*;
import lombok.Getter;
import lombok.extern.java.Log;

import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.logging.Level;

import static com.skcraft.launcher.bootstrap.SharedLocale.tr;

@Log
public class Bootstrap {

    private static final int BOOTSTRAP_VERSION = 1;

    @Getter private final File baseDir;
    @Getter private final boolean portable;
    @Getter private final File binariesDir;
    @Getter private final Properties properties;
    private final String[] originalArgs;

    public static void main(String[] args) throws Throwable {
        SimpleLogFormatter.configureGlobalLogger();
        SharedLocale.loadBundle("com.skcraft.launcher.lang.Bootstrap", Locale.getDefault());

        Bootstrap bootstrap = new Bootstrap(args);
        try {
            bootstrap.cleanup();
            bootstrap.launch();
        } catch (Throwable t) {
            Bootstrap.log.log(Level.WARNING, "Error", t);
            Bootstrap.setSwingLookAndFeel();
            SwingHelper.showErrorDialog(null, tr("errors.bootstrapError"), tr("errorTitle"), t);
        }
    }

    public Bootstrap(String[] args) throws IOException {
        this.properties = BootstrapUtils.loadProperties(Bootstrap.class, "bootstrap.properties");

        File portableDir = getPortableDir();
        File baseDir = portableDir!=null ? portableDir : getUserLauncherDir();

        this.baseDir = baseDir;
        this.portable = portableDir!=null;
        this.binariesDir = new File(baseDir, "launcher");
        this.originalArgs = args;

        binariesDir.mkdirs();
    }

    public void cleanup() {
        File[] files = binariesDir.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.getName().endsWith(".tmp");
            }
        });

        if (files != null) {
            for (File file : files) {
                file.delete();
            }
        }
    }

    public void launch() throws Throwable {
        File[] files = binariesDir.listFiles(new LauncherBinary.Filter());
        List<LauncherBinary> binaries = new ArrayList<LauncherBinary>();

        if (files != null) {
            for (File file : files) {
                Bootstrap.log.info("Found " + file.getAbsolutePath() + "...");
                binaries.add(new LauncherBinary(file));
            }
        }

        if (!binaries.isEmpty()) {
            launchExisting(binaries, true);
        } else {
            launchInitial();
        }
    }

    public void launchInitial() throws Exception {
        Bootstrap.log.info("Downloading the launcher...");
        Thread thread = new Thread(new Downloader(this));
        thread.start();
    }

    public void launchExisting(List<LauncherBinary> binaries, boolean redownload) throws Exception {
        Collections.sort(binaries);
        LauncherBinary working = null;
        Class<?> clazz = null;

        for (LauncherBinary binary : binaries) {
            File testFile = binary.getPath();
            try {
                testFile = binary.getExecutableJar();
                Bootstrap.log.info("Trying " + testFile.getAbsolutePath() + "...");
                clazz = load(testFile);
                Bootstrap.log.info("Launcher loaded successfully.");
                working = binary;
                break;
            } catch (Throwable t) {
                Bootstrap.log.log(Level.WARNING, "Failed to load " + testFile.getAbsoluteFile(), t);
            }
        }

        if (working != null) {
            for (LauncherBinary binary : binaries) {
                if (working != binary) {
                    log.info("Removing " + binary.getPath() + "...");
                    binary.remove();
                }
            }

            execute(clazz);
        } else {
            if (redownload) {
                launchInitial();
            } else {
                throw new IOException("Failed to find launchable .jar");
            }
        }
    }

    public void execute(Class<?> clazz) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        Method method = clazz.getDeclaredMethod("main", String[].class);
        List<String> launcherArgs = new ArrayList<String>();

        String editon = getProperties().getProperty("edition");

        if (portable)
            launcherArgs.add("--portable");
        launcherArgs.add("--dir");
        launcherArgs.add(baseDir.getAbsolutePath());
        launcherArgs.add("--bootstrap-version");
        launcherArgs.add(String.valueOf(BOOTSTRAP_VERSION));
        if (editon!= null&&editon.length()>0) {
            launcherArgs.add("--edition");
            launcherArgs.add(editon);
        }
        launcherArgs.addAll(Arrays.asList(originalArgs));

        String[] args = launcherArgs.toArray(new String[launcherArgs.size()]);

        log.info("Launching with arguments " + Arrays.toString(args));

        method.invoke(null, new Object[] { args });
    }

    public Class<?> load(File jarFile) throws MalformedURLException, ClassNotFoundException {
        URL[] urls = new URL[] { jarFile.toURI().toURL() };
        URLClassLoader child = new URLClassLoader(urls, this.getClass().getClassLoader());
        Class<?> clazz = Class.forName(getProperties().getProperty("launcherClass"), true, child);
        return clazz;
    }

    public static void setSwingLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Throwable e) {
        }
    }

    private File getUserLauncherDir() {
        return OperatingSystem.getCurrentPlatform().getWorkingDirectory(getProperties().getProperty("homeFolder"));
    }

	private File getPortableDir() {
		final String portableName = "portable.txt";
		File portable = new File(portableName);
		try {
			portable = portable.getCanonicalFile();
		} catch (final IOException e) {
		}
		final File parentDir = portable.getParentFile();
		if (!(parentDir!=null&&parentDir.exists()))
			return null;
		if (portable.exists())
			return parentDir;
		final File contentsDir = parentDir.getParentFile();
		if (contentsDir!=null&&contentsDir.exists()&&contentsDir.getName().equals("Contents")) {
			final File appDir = contentsDir.getParentFile();
			if (appDir!=null&&appDir.exists()&&BootstrapUtils.endsWith(appDir.getName(), ".app", true)) {
				final File outBaseDir = appDir.getParentFile();
				if (outBaseDir!=null&&outBaseDir.exists()) {
					final File outBasePortable = new File(outBaseDir, portableName);
					if (outBasePortable.exists())
						return outBaseDir;
				}
			}
		}

		return null;
	}

}
