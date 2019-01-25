
package net.teamfruit.skcraft.launcher.dirs;

import java.io.File;

import com.skcraft.launcher.model.minecraft.VersionManifest;

public interface LauncherDirectories {

	/**
	 * Get the base directory.
	 *
	 * @return the icons directory
	 */
	File getBaseDir();

	/**
	 * Get the directory containing the configs.
	 *
	 * @return the instances dir
	 */
	File getConfigDir();

	/**
	 * Get the directory to store icons.
	 *
	 * @return the icons directory
	 */
	File getDataDir();

	/**
	 * Get the directory to store common data files.
	 *
	 * @return the common data directory
	 */
	File getCommonDataDir();

	/**
	 * Get the directory containing the instances.
	 *
	 * @return the instances dir
	 */
	File getInstancesDir();

	/**
	 * Get the directory to store temporary files.
	 *
	 * @return the temporary directory
	 */
	File getTemporaryDir();

	/**
	 * Get the directory to store temporary install files.
	 *
	 * @return the temporary install directory
	 */
	File getInstallerDir();

	/**
	 * Get the directory to store temporary native files.
	 *
	 * @return the temporary native directory
	 */
	File getNativeDir();

	/**
	 * Get the directory to store icons.
	 *
	 * @return the icons directory
	 */
	File getIconDir();

	/**
	 * Get the directory to store skins.
	 *
	 * @return the skins directory
	 */
	File getSkinDir();

	/**
	 * Get the directory to store temporarily extracted files.
	 *
	 * @return the directory
	 */
	File getExtractDir();

	/**
	 * Delete old extracted files.
	 */
	void cleanupExtractDir();

	/**
	 * Create a new temporary directory to extract files to.
	 *
	 * @return the directory path
	 */
	File createExtractDir();

	/**
	 * Get the directory to store the launcher binaries.
	 *
	 * @return the libraries directory
	 */
	File getLauncherBinariesDir();

	/**
	 * Get the directory to store assets.
	 *
	 * @return the assets directory
	 */
	File getAssetsDir();

	/**
	 * Get the directory to store libraries.
	 *
	 * @return the libraries directory
	 */
	File getLibrariesDir();

	/**
	 * Get the directory to store versions.
	 *
	 * @return the versions directory
	 */
	File getVersionsDir();

	/**
	 * Get the directory to store a version.
	 *
	 * @param version the version
	 * @return the directory
	 */
	File getVersionDir(String version);

	/**
	 * Get the path to the JAR for the given version manifest.
	 *
	 * @param versionManifest the version manifest
	 * @return the path
	 */
	File getJarPath(VersionManifest versionManifest);

}
