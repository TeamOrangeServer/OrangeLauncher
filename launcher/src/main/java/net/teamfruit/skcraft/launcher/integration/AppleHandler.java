package net.teamfruit.skcraft.launcher.integration;

import javax.swing.JOptionPane;

import com.apple.eawt.AboutHandler;
import com.apple.eawt.AppEvent;
import com.apple.eawt.Application;
import com.apple.eawt.OpenFilesHandler;
import com.apple.eawt.OpenURIHandler;
import com.apple.eawt.PreferencesHandler;
import com.skcraft.launcher.Launcher;
import com.skcraft.launcher.dialog.AboutDialog;
import com.skcraft.launcher.dialog.ConfigurationDialog;
import com.skcraft.launcher.dialog.LauncherFrame;
import com.skcraft.launcher.swing.SwingHelper;
import com.skcraft.launcher.util.Environment;
import com.skcraft.launcher.util.Platform;

import lombok.RequiredArgsConstructor;

public class AppleHandler {
	@RequiredArgsConstructor
	private static class AppleHandlerImpl implements OpenFilesHandler, AboutHandler, PreferencesHandler, OpenURIHandler {
		private final Launcher launcher;
		private final LauncherFrame launcherFrame;

		@Override
		public void openFiles(AppEvent.OpenFilesEvent ofe) {
			SwingHelper.showMessageDialog(null, "openFiles: {searchTerm: "+ofe.getSearchTerm()+", files: "+ofe.getFiles()+"}", "openFiles", null, JOptionPane.INFORMATION_MESSAGE);
		}

		@Override
		public void handleAbout(AppEvent.AboutEvent ae) {
			new AboutDialog(launcherFrame).setVisible(true);
		}

		@Override
		public void handlePreferences(AppEvent.PreferencesEvent pe) {
			new ConfigurationDialog(launcherFrame, launcher).setVisible(true);
		}

		@Override
		public void openURI(AppEvent.OpenURIEvent oue) {
			launcher.getOptions().setUriPath(oue.getURI().toString());
			if (!launcherFrame.isFirstLoaded()&&launcherFrame.isVisible())
				launcherFrame.processRun();
		}

		public void register() {
			Application app = Application.getApplication();
			app.setAboutHandler(this);
			app.setPreferencesHandler(this);
			app.setOpenFileHandler(this);
			app.setOpenURIHandler(this);
		}

	}

	public static void register(Launcher launcher, LauncherFrame launcherFrame) {
		if (Environment.getInstance().getPlatform()==Platform.MAC_OS_X)
			try {
				new AppleHandlerImpl(launcher, launcherFrame).register();
			} catch (Throwable ignored) {
				ignored.printStackTrace();
			}
	}
}