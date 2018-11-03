package net.teamfruit.skcraft.launcher.integration;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import com.beust.jcommander.JCommander;
import com.google.common.collect.Lists;
import com.google.common.io.Files;
import com.skcraft.launcher.Launcher;
import com.skcraft.launcher.LauncherArguments;
import com.skcraft.launcher.launch.JavaProcessBuilder;
import com.skcraft.launcher.util.Environment;
import com.skcraft.launcher.util.Platform;
import com.skcraft.launcher.util.WinRegistry;

import lombok.extern.java.Log;
import net.teamfruit.skcraft.launcher.appicon.AppIcon;
import net.teamfruit.skcraft.launcher.relaunch.LauncherBinary;

@Log
public class UriScheme {
	private final Launcher launcher;

	public UriScheme(Launcher launcher) {
		this.launcher = launcher;
	}

	public File getBinary() {
		File[] files = launcher.getLauncherBinariesDir().listFiles(new LauncherBinary.Filter());
		List<LauncherBinary> binaries = new ArrayList<LauncherBinary>();

		if (files!=null)
			for (File file : files) {
				log.info("Found "+file.getAbsolutePath()+"...");
				binaries.add(new LauncherBinary(file));
			}

		if (!binaries.isEmpty()) {
			Collections.sort(binaries);

			for (LauncherBinary binary : binaries) {
				try {
					File testFile = binary.getExecutableJar();
					log.info("Loaded entry point "+testFile.getAbsolutePath()+"...");
					return testFile;
				} catch (Throwable t) {
				}
			}
		}

		return null;
	}

	public void install() {
		Platform platform = Environment.getInstance().getPlatform();
		if (platform==Platform.WINDOWS)
			try {
				File binaryPath = getBinary();
				if (binaryPath!=null) {
					Class<?> clazz = load(binaryPath);

					JavaProcessBuilder builder = new JavaProcessBuilder();
					builder.setJvmApp("javaw");
					builder.classPath(binaryPath.getCanonicalFile());
					builder.setMainClass(clazz.getName());
					builder.getArgs().addAll(getArguments("\"%1\""));

					File iconPath = new File(launcher.getIconDir(), "launcher.ico");
					if (!iconPath.exists())
						try {
							Files.createParentDirs(iconPath);
							Files.copy(AppIcon.getWindowsAppIcon(), iconPath);
						} catch (Throwable ignored) {
							ignored.printStackTrace();
						}

					log.info("Installing URL Protocol...");
					WinRegistry.createKey(WinRegistry.HKEY_CURRENT_USER, "Software\\Classes\\fruitlauncher");
					WinRegistry.createKey(WinRegistry.HKEY_CURRENT_USER, "Software\\Classes\\fruitlauncher\\DefaultIcon");
					WinRegistry.createKey(WinRegistry.HKEY_CURRENT_USER, "Software\\Classes\\fruitlauncher\\shell\\open\\command");
					WinRegistry.writeStringValue(WinRegistry.HKEY_CURRENT_USER, "Software\\Classes\\fruitlauncher", "", "URL:FruitLauncher Protocol");
					WinRegistry.writeStringValue(WinRegistry.HKEY_CURRENT_USER, "Software\\Classes\\fruitlauncher", "URL Protocol", "");
					WinRegistry.writeStringValue(WinRegistry.HKEY_CURRENT_USER, "Software\\Classes\\fruitlauncher\\DefaultIcon", "", iconPath.getCanonicalPath());
					WinRegistry.writeStringValue(WinRegistry.HKEY_CURRENT_USER, "Software\\Classes\\fruitlauncher\\shell\\open\\command", "", joinCommand(builder.buildCommand()));
				}
			} catch (Throwable ignored) {
				ignored.printStackTrace();
			}
		else if (platform==Platform.LINUX)
			try {
				File binaryPath = getBinary();
				if (binaryPath!=null) {
					Class<?> clazz = load(binaryPath);

					JavaProcessBuilder builder = new JavaProcessBuilder();
					builder.classPath(binaryPath.getCanonicalFile());
					builder.setMainClass(clazz.getName());
					builder.getArgs().addAll(getArguments("\"%u\""));

					File userIconDir = new File(System.getProperty("user.home", "."), ".local/share/icons/hicolor");
					for (AppIcon appicon : AppIcon.getAppIconSet()) {
						File userIconFile = new File(new File(userIconDir, appicon.getName()), "apps/fruitlauncher.png");
						if (!userIconFile.exists())
							try {
								Files.createParentDirs(userIconFile);
								Files.copy(appicon.getInput(), userIconFile);
							} catch (Throwable ignored) {
								ignored.printStackTrace();
							}
					}

					log.info("Installing URL Protocol...");

					File file = new File(launcher.getTemporaryDir(), "fruitlauncher.desktop");
					String content = "[Desktop Entry]\n"+
							"Name=FruitLauncher\n"+
							"Exec="+joinCommand(builder.buildCommand())/*.replace("\\", "\\\\").replace("\"", "\\\"")*/ +"\n"+
							"Icon=fruitlauncher\n"+
							"Type=Application\n"+
							"Terminal=false\n"+
							"NoDisplay=true\n"+
							"MimeType=x-scheme-handler/fruitlauncher;";

					Files.write(content, file, Charset.forName("UTF-8"));

					String[] command = { "xdg-desktop-menu", "install", "--novendor", file.getCanonicalPath() };
					Process process = new ProcessBuilder().command(command).start();
					try {
						if (process.waitFor()!=0) {
							StringWriter writer = new StringWriter();
							IOUtils.copy(process.getInputStream(), writer);
							IOUtils.copy(process.getErrorStream(), writer);
							throw new IOException(writer.toString());
						}
					} catch (InterruptedException e) {
						Thread.currentThread().interrupt();
					}

					file.delete();
				}
			} catch (Throwable ignored) {
				ignored.printStackTrace();
			}
	}

	private List<String> getArguments(String uripathreplace) throws IOException {
		LauncherArguments options = new LauncherArguments();
		JCommander cmd = new JCommander(options);
		cmd.setAcceptUnknownOptions(true);
		cmd.parse(launcher.getArgs());

		List<String> arguments = Lists.newArrayList();
		arguments.add("--dir");
		arguments.add(launcher.getBaseDir().getCanonicalPath());
		Integer bsVersion = options.getBootstrapVersion();
		if (bsVersion!=null) {
			arguments.add("--bootstrap-version");
			arguments.add(String.valueOf(bsVersion));
		}
		if (options.isPortable())
			arguments.add("--portable");
		arguments.add("--uripath");
		arguments.add(uripathreplace);
		arguments.addAll(cmd.getUnknownOptions());
		return arguments;
	}

	public String joinCommand(List<String> arguments) {
		List<String> args = Lists.newArrayList(arguments);
		for (ListIterator<String> itr = args.listIterator(); itr.hasNext();) {
			String arg = itr.next();
			if (!(arg.startsWith("\"")&&arg.endsWith("\""))&&arg.contains(" "))
				itr.set("\""+arg+"\"");
		}
		return StringUtils.join(args, " ");
	}

	public Class<?> load(File jarFile) throws MalformedURLException, ClassNotFoundException {
		URL[] urls = new URL[] { jarFile.toURI().toURL() };
		URLClassLoader child = new URLClassLoader(urls, this.getClass().getClassLoader());
		Class<?> clazz = Class.forName(launcher.getMainClass().getName(), true, child);
		return clazz;
	}
}
