/*
 * SK's Minecraft Launcher
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com> and contributors
 * Please see LICENSE.txt for license information.
 */

package com.skcraft.launcher;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.commons.lang.StringUtils;

import com.beust.jcommander.Parameter;

import lombok.Data;

/**
 * The command line arguments that the launcher accepts.
 */
@Data
public class LauncherArguments {

	@Parameter(names = "--dir")
	private File dir;

	@Parameter(names = "--bootstrap-version")
	private Integer bootstrapVersion;

	@Parameter(names = "--portable")
	private boolean portable;

	@Parameter(names = "--uripath")
	private String uriPath;

	@Parameter(names = "--edition")
	private String edition;

	@Parameter(names = "--run")
	private String run;

	@Parameter(names = "--key")
	private String key;

	public void processURI() {
		String uripath = getUriPath();
		if (!StringUtils.isEmpty(uripath)) {
			try {
				if (StringUtils.startsWith(uripath, "'")&&StringUtils.endsWith(uripath, "'"))
					uripath = StringUtils.substringBetween(uripath, "'");
				URI uri = new URI(uripath);
				String scheme = uri.getScheme();
				if (StringUtils.isEmpty(scheme)||StringUtils.equalsIgnoreCase(scheme, "fruitlauncher")) {
					String host = uri.getHost();
					String path = uri.getPath();
					if (!StringUtils.isEmpty(path))
						if (StringUtils.equalsIgnoreCase(host, "run")) {
							String[] uripaths = StringUtils.split(path, "/");
							if (uripaths.length>0) {
								String runid = uripaths[0];
								setRun(runid);
							}
						}
					String query = uri.getQuery();
					if (!StringUtils.isEmpty(query)) {
						String[] qStrings = StringUtils.split(query, "&");
						for (String qString : qStrings) {
							String key = StringUtils.substringBefore(qString, "=");
							String value = StringUtils.substringAfter(qString, "=");
							if (StringUtils.equalsIgnoreCase(key, "key")&&!StringUtils.isEmpty(value))
								setKey(value);
						}
					}
				}
			} catch (URISyntaxException e) {
			}
		}
	}
}
