package net.teamfruit.skcraft.launcher.dirs;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.logging.Level;

import org.apache.commons.io.FileUtils;

import com.skcraft.launcher.model.minecraft.VersionManifest;

import lombok.extern.java.Log;

@Log
public abstract class AbstractLauncherDirectories implements LauncherDirectories {

	@Override
	public File getDataDir() {
		return getBaseDir();
	}

	@Override
	public File getCommonDataDir() {
		return getDataDir();
	}

	@Override
	public File getInstancesDir() {
		return new File(getDataDir(), "instances");
	}

	@Override
	public File getTemporaryDir() {
		return new File(getBaseDir(), "temp");
	}

	@Override
	public File getInstallerDir() {
		return new File(getTemporaryDir(), "install");
	}

	@Override
	public File getNativeDir() {
		return new File(getTemporaryDir(), "natives");
	}

	@Override
	public File getIconDir() {
		return new File(getBaseDir(), "icons");
	}

	@Override
	public File getSkinDir() {
		return new File(getBaseDir(), "skins");
	}

	@Override
	public File getExtractDir() {
		return new File(getTemporaryDir(), "extract");
	}

	@Override
	public void cleanupExtractDir() {
		log.info("Cleaning up temporary extracted files directory...");

		final long now = System.currentTimeMillis();

		File[] dirs = getExtractDir().listFiles(new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				try {
					long time = Long.parseLong(pathname.getName());
					return (now-time)>(1000*60*60);
				} catch (NumberFormatException e) {
					return false;
				}
			}
		});

		if (dirs!=null) {
			for (File dir : dirs) {
				log.info("Removing "+dir.getAbsolutePath()+"...");
				try {
					FileUtils.deleteDirectory(dir);
				} catch (IOException e) {
					log.log(Level.WARNING, "Failed to delete "+dir.getAbsolutePath(), e);
				}
			}
		}
	}

	@Override
	public File createExtractDir() {
		File dir = new File(getExtractDir(), String.valueOf(System.currentTimeMillis()));
		dir.mkdirs();
		log.info("Created temporary directory "+dir.getAbsolutePath());
		return dir;
	}

	@Override
	public File getLauncherBinariesDir() {
		return new File(getBaseDir(), "launcher");
	}

	@Override
	public File getAssetsDir() {
		return new File(getCommonDataDir(), "assets");
	}

	@Override
	public File getLibrariesDir() {
		return new File(getCommonDataDir(), "libraries");
	}

	@Override
	public File getVersionsDir() {
		return new File(getCommonDataDir(), "versions");
	}

	@Override
	public File getVersionDir(String version) {
		return new File(getVersionsDir(), version);
	}

	@Override
	public File getJarPath(VersionManifest versionManifest) {
		return new File(getVersionDir(versionManifest.getId()), versionManifest.getId()+".jar");
	}

}
