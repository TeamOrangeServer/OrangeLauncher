package net.teamfruit.skcraft.launcher.dirs;

import com.skcraft.launcher.Configuration;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public abstract class ConfigLauncherDirectories extends OptionLauncherDirectories {

	@Getter
	private final Configuration config;

	@Override
	public String getPathCommonDataDir() {
		return getConfig().getPathCommonDataDir();
	}

	@Override
	public String getPathInstancesDir() {
		return getConfig().getPathInstancesDir();
	}

}
