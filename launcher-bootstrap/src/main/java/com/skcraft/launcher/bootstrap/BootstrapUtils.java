/*
 * SK's Minecraft Launcher
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com> and contributors
 * Please see LICENSE.txt for license information.
 */

package com.skcraft.launcher.bootstrap;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public final class BootstrapUtils {

    //private static final Pattern absoluteUrlPattern = Pattern.compile("^[A-Za-z0-9\\-]+://.*$");

    private BootstrapUtils() {
    }

    public static void checkInterrupted() throws InterruptedException {
        if (Thread.interrupted()) {
            throw new InterruptedException();
        }
    }

    public static void closeQuietly(Closeable closeable) {
        try {
            if (closeable != null) {
                closeable.close();
            }
        } catch (IOException e) {
        }
    }

    public static Properties loadProperties(Class<?> clazz, String name) throws IOException {
        Properties prop = new Properties();
        InputStream in = null;
        try {
            in = clazz.getResourceAsStream(name);
            prop.load(in);
        } finally {
            closeQuietly(in);
        }
        return prop;
    }

    public static boolean endsWith(String str, String suffix, boolean ignoreCase) {
        if (str == null || suffix == null) {
            return (str == null && suffix == null);
        }
        if (suffix.length() > str.length()) {
            return false;
        }
        int strOffset = str.length() - suffix.length();
        return str.regionMatches(ignoreCase, strOffset, suffix, 0, suffix.length());
    }

}
