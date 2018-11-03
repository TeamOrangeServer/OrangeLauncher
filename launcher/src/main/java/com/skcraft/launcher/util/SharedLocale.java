/*
 * SK's Minecraft Launcher
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com> and contributors
 * Please see LICENSE.txt for license information.
 */

package com.skcraft.launcher.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.logging.Level;

import javax.annotation.Nullable;

import org.apache.commons.lang.ArrayUtils;

import com.google.common.base.Supplier;

import lombok.NonNull;
import lombok.Setter;
import lombok.extern.java.Log;

/**
 * Handles loading a shared message {@link java.util.ResourceBundle}.
 */
@Log
public class SharedLocale {

    private static Locale locale = Locale.getDefault();
    private static ResourceBundle bundle;
    @Setter @Nullable private static Supplier<ResourceBundle> bundleSupplier;

    /**
     * Get the current locale.
     *
     * @return the current locale
     */
    public static Locale getLocale() {
        return locale;
    }

    /**
     * Translate a string.
     *
     * <p>If the string is not available, then ${key} will be returned.</p>
     *
     * @param key the key
     * @return the translated string
     */
    public static String tr(String key) {
    	if (bundleSupplier!=null) {
	    	ResourceBundle skinbundle = bundleSupplier.get();
	        if (skinbundle != null) {
	            try {
	                return skinbundle.getString(key);
	            } catch (MissingResourceException e) {
	                log.log(Level.FINE, "Failed to find message from skin", e);
	            }
	        }
    	}

        if (bundle != null) {
            try {
                return bundle.getString(key);
            } catch (MissingResourceException e) {
                log.log(Level.FINE, "Failed to find message", e);
            }
        }

        return "${" + key + "}";
    }

    /**
     * Format a translated string.
     *
     * <p>If the string is not available, then ${key}:args will be returned.</p>
     *
     * @param key the key
     * @param args arguments
     * @return a translated string
     */
    public static String tr(String key, Object... args) {
    	if (bundleSupplier!=null) {
	    	ResourceBundle skinbundle = bundleSupplier.get();
	        if (skinbundle != null) {
	            try {
	                MessageFormat formatter = new MessageFormat(skinbundle.getString(key));
	                formatter.setLocale(getLocale());
	                return formatter.format(args);
	            } catch (MissingResourceException e) {
	                log.log(Level.FINE, "Failed to find message from skin", e);
	            }
	        }
    	}

        if (bundle != null) {
            try {
                MessageFormat formatter = new MessageFormat(bundle.getString(key));
                formatter.setLocale(getLocale());
                return formatter.format(args);
            } catch (MissingResourceException e) {
                log.log(Level.FINE, "Failed to find message", e);
            }
        }

        return "${" + key + ":" + ArrayUtils.toString(args) + "}";
    }

    /**
     * Load a shared resource bundle.
     *
     * @param baseName the bundle name
     * @param locale the locale
     * @return true if loaded successfully
     */
    public static boolean loadBundle(@NonNull String baseName, @NonNull Locale locale) {
        try {
            SharedLocale.locale = locale;
            bundle = ResourceBundle.getBundle(baseName, locale,
                    SharedLocale.class.getClassLoader(), new ResourceBundleUtf8Control());
            return true;
        } catch (MissingResourceException e) {
            log.log(Level.SEVERE, "Failed to load resource bundle", e);
            return false;
        }
    }

    public static class ResourceBundleUtf8Control extends ResourceBundle.Control {
        @Override
        public ResourceBundle newBundle(final String baseName, final Locale locale, final String format,
            final ClassLoader loader, final boolean reload)
            throws IllegalAccessException, InstantiationException, IOException {
            final String bundleName = toBundleName(baseName, locale);
            final String resourceName = toResourceName(bundleName, "properties");

            InputStream is = loader.getResourceAsStream(resourceName);
            InputStreamReader isr = new InputStreamReader(is, "UTF-8");
            BufferedReader reader = new BufferedReader(isr);
            try {
                return new PropertyResourceBundle(reader);
            } finally {
				is.close();
				isr.close();
				reader.close();
			}
        }
    }
}
