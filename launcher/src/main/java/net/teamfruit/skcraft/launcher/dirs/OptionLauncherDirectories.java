package net.teamfruit.skcraft.launcher.dirs;

import java.io.File;

import org.apache.commons.lang.StringUtils;

public abstract class OptionLauncherDirectories extends AbstractLauncherDirectories {

	public static final String DefaultPathCommonDataDir = "";

	public static final String DefaultPathInstancesDir = "instances";

	public abstract String getPathCommonDataDir();

	public abstract String getPathInstancesDir();

	public File getDefaultCommonDataDir() {
		return getDataDir();
	}

	public File getDefaultInstancesDir() {
		return getDataDir();
	}

	@Override
	public File getCommonDataDir() {
		return DirectoryUtils.getDirFromOption(getDefaultCommonDataDir(), StringUtils.defaultString(getPathCommonDataDir(), DefaultPathCommonDataDir));
	}

	@Override
	public File getInstancesDir() {
		return DirectoryUtils.getDirFromOption(getDefaultInstancesDir(), StringUtils.defaultString(getPathInstancesDir(), DefaultPathInstancesDir));
	}

}
