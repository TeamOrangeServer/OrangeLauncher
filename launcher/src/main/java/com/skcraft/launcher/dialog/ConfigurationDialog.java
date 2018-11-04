/*
 * SK's Minecraft Launcher
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com> and contributors
 * Please see LICENSE.txt for license information.
 */

package com.skcraft.launcher.dialog;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;

import org.apache.commons.lang.StringUtils;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.skcraft.concurrency.ObservableFuture;
import com.skcraft.launcher.Configuration;
import com.skcraft.launcher.Instance;
import com.skcraft.launcher.Launcher;
import com.skcraft.launcher.persistence.Persistence;
import com.skcraft.launcher.swing.ActionListeners;
import com.skcraft.launcher.swing.FormPanel;
import com.skcraft.launcher.swing.LinedBoxPanel;
import com.skcraft.launcher.swing.ObjectSwingMapper;
import com.skcraft.launcher.swing.SwingHelper;
import com.skcraft.launcher.util.SharedLocale;
import com.skcraft.launcher.util.SwingExecutor;

import lombok.NonNull;
import lombok.extern.java.Log;
import net.teamfruit.skcraft.launcher.dialog.DirectorySelectionDialog;
import net.teamfruit.skcraft.launcher.dialog.SkinSelectionDialog;
import net.teamfruit.skcraft.launcher.dirs.DirectoryTasks;
import net.teamfruit.skcraft.launcher.dirs.DirectoryUtils;
import net.teamfruit.skcraft.launcher.dirs.OptionLauncherDirectories;
import net.teamfruit.skcraft.launcher.discordrpc.DiscordStatus;
import net.teamfruit.skcraft.launcher.discordrpc.LauncherStatus;
import net.teamfruit.skcraft.launcher.discordrpc.LauncherStatus.WindowDisablable;
import net.teamfruit.skcraft.launcher.skins.LocalSkin;
import net.teamfruit.skcraft.launcher.skins.Skin;

/**
 * A dialog to modify configuration options.
 */
@Log
public class ConfigurationDialog extends JDialog {

	private final Launcher launcher;

	private final Configuration config;
	private final ObjectSwingMapper mapper;

	private final JPanel tabContainer = new JPanel(new BorderLayout());
	private final JTabbedPane tabbedPane = new JTabbedPane();
	private final FormPanel javaSettingsPanel = new FormPanel();
	private final JTextField jvmPathText = new JTextField();
	private final JTextField jvmArgsText = new JTextField();
	private final JSpinner minMemorySpinner = new JSpinner();
	private final JSpinner maxMemorySpinner = new JSpinner();
	private final JSpinner permGenSpinner = new JSpinner();
	private final FormPanel gameSettingsPanel = new FormPanel();
	private final JCheckBox showConsoleCheck = new JCheckBox(SharedLocale.tr("options.showConsole"));
	private final JSpinner widthSpinner = new JSpinner();
	private final JSpinner heightSpinner = new JSpinner();
	private final JCheckBox serverEnabledCheck = new JCheckBox(SharedLocale.tr("options.serverEnabled"));
	private final JTextField serverHostText = new JTextField();
	private final JSpinner serverPortSpinner = new JSpinner();
	private final JCheckBox discordPingCheck = new JCheckBox(SharedLocale.tr("options.discordPing"));
	private final FormPanel proxySettingsPanel = new FormPanel();
	private final JCheckBox useProxyCheck = new JCheckBox(SharedLocale.tr("options.useProxyCheck"));
	private final JTextField proxyHostText = new JTextField();
	private final JSpinner proxyPortText = new JSpinner();
	private final JTextField proxyUsernameText = new JTextField();
	private final JPasswordField proxyPasswordText = new JPasswordField();
	private final FormPanel advancedPanel = new FormPanel();
	private final JTextField skinText = new JTextField();
	private final JButton skinButton = new JButton(SharedLocale.tr("options.skinButton"));
	private final JTextField gameKeyText = new JTextField();
	private final JTextField secretKeyText = new JTextField();
	private final JButton secretUnlockButton = new JButton(SharedLocale.tr("options.secretUnlockButton"));
	private final JCheckBox offlineModeEnabledCheck = new JCheckBox(SharedLocale.tr("options.offlineModeEnabled"));
	private final JTextField offlineModePlayerNameText = new JTextField();
	private final LinedBoxPanel buttonsPanel = new LinedBoxPanel(true);
	private final FormPanel pathDirPanel = new FormPanel();
	private final JTextField pathCommonDataDirText = new JTextField();
	private final JTextField pathInstancesDirText = new JTextField();
	private final JButton okButton = new JButton(SharedLocale.tr("button.ok"));
	private final JButton cancelButton = new JButton(SharedLocale.tr("button.cancel"));
	private final JButton aboutButton = new JButton(SharedLocale.tr("options.about"));
	private final JButton logButton = new JButton(SharedLocale.tr("options.launcherConsole"));

	private final JCheckBox moveFilesCheck = new JCheckBox(SharedLocale.tr("options.moveFiles"), true);

	private OptionLauncherDirectories launcherDirs = new OptionLauncherDirectories() {
		@Override
		public File getConfigDir() {
			return launcher.getConfigDir();
		}

		@Override
		public File getBaseDir() {
			return launcher.getBaseDir();
		}

		@Override
		public String getPathCommonDataDir() {
			return pathCommonDataDirText.getText();
		}

		@Override
		public String getPathInstancesDir() {
			return pathInstancesDirText.getText();
		}
	};

	/**
	 * Create a new configuration dialog.
	 *
	 * @param owner the window owner
	 * @param launcher the launcher
	 */
	public ConfigurationDialog(Window owner, @NonNull Launcher launcher) {
		super(owner, ModalityType.DOCUMENT_MODAL);

		this.launcher = launcher;

		this.config = launcher.getConfig();
		mapper = new ObjectSwingMapper(config);

		setTitle(SharedLocale.tr("options.title"));
		initComponents();
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setSize(new Dimension(400, 500));
		setResizable(false);
		setLocationRelativeTo(owner);

		mapper.map(jvmPathText, "jvmPath");
		mapper.map(jvmArgsText, "jvmArgs");
		mapper.map(minMemorySpinner, "minMemory");
		mapper.map(maxMemorySpinner, "maxMemory");
		mapper.map(permGenSpinner, "permGen");
		mapper.map(showConsoleCheck, "showConsole");
		mapper.map(widthSpinner, "windowWidth");
		mapper.map(heightSpinner, "widowHeight");
		mapper.map(serverEnabledCheck, "serverEnabled");
		mapper.map(serverHostText, "serverHost");
		mapper.map(serverPortSpinner, "serverPort");
		mapper.map(discordPingCheck, "discordPing");
		mapper.map(useProxyCheck, "proxyEnabled");
		mapper.map(proxyHostText, "proxyHost");
		mapper.map(proxyPortText, "proxyPort");
		mapper.map(proxyUsernameText, "proxyUsername");
		mapper.map(proxyPasswordText, "proxyPassword");
		mapper.map(skinText, "skin");
		mapper.map(gameKeyText, "gameKey");
		mapper.map(offlineModeEnabledCheck, "offlineModeEnabled");
		mapper.map(offlineModePlayerNameText, "offlineModePlayerName");
		mapper.map(pathCommonDataDirText, "pathCommonDataDir");
		mapper.map(pathInstancesDirText, "pathInstancesDir");

		mapper.copyFromObject();
	}

	private void initComponents() {
		javaSettingsPanel.addRow(new JLabel(SharedLocale.tr("options.jvmPath")), jvmPathText);
		javaSettingsPanel.addRow(new JLabel(SharedLocale.tr("options.jvmArguments")), jvmArgsText);
		javaSettingsPanel.addRow(Box.createVerticalStrut(15));
		javaSettingsPanel.addRow(new JLabel(SharedLocale.tr("options.64BitJavaWarning")));
		javaSettingsPanel.addRow(new JLabel(SharedLocale.tr("options.minMemory")), minMemorySpinner);
		javaSettingsPanel.addRow(new JLabel(SharedLocale.tr("options.maxMemory")), maxMemorySpinner);
		javaSettingsPanel.addRow(new JLabel(SharedLocale.tr("options.permGen")), permGenSpinner);
		SwingHelper.removeOpaqueness(javaSettingsPanel);
		tabbedPane.addTab(SharedLocale.tr("options.javaTab"), SwingHelper.alignTabbedPane(javaSettingsPanel));

		gameSettingsPanel.addRow(showConsoleCheck);
		gameSettingsPanel.addRow(new JLabel(SharedLocale.tr("options.windowWidth")), widthSpinner);
		gameSettingsPanel.addRow(new JLabel(SharedLocale.tr("options.windowHeight")), heightSpinner);
		gameSettingsPanel.addRow(serverEnabledCheck);
		gameSettingsPanel.addRow(new JLabel(SharedLocale.tr("options.serverHost")), serverHostText);
		gameSettingsPanel.addRow(new JLabel(SharedLocale.tr("options.serverPort")), serverPortSpinner);
		gameSettingsPanel.addRow(discordPingCheck);
		SwingHelper.removeOpaqueness(gameSettingsPanel);
		tabbedPane.addTab(SharedLocale.tr("options.minecraftTab"), SwingHelper.alignTabbedPane(gameSettingsPanel));

		proxySettingsPanel.addRow(useProxyCheck);
		proxySettingsPanel.addRow(new JLabel(SharedLocale.tr("options.proxyHost")), proxyHostText);
		proxySettingsPanel.addRow(new JLabel(SharedLocale.tr("options.proxyPort")), proxyPortText);
		proxySettingsPanel.addRow(new JLabel(SharedLocale.tr("options.proxyUsername")), proxyUsernameText);
		proxySettingsPanel.addRow(new JLabel(SharedLocale.tr("options.proxyPassword")), proxyPasswordText);
		SwingHelper.removeOpaqueness(proxySettingsPanel);
		tabbedPane.addTab(SharedLocale.tr("options.proxyTab"), SwingHelper.alignTabbedPane(proxySettingsPanel));

		advancedPanel.addRow(new JLabel(SharedLocale.tr("options.skin")), skinButton);
		skinText.setEditable(false);
		advancedPanel.addRow(skinText);
		advancedPanel.addRow(new JLabel(SharedLocale.tr("options.secretUnlock")), secretUnlockButton);
		advancedPanel.addRow(secretKeyText);
		advancedPanel.addRow(offlineModeEnabledCheck);
		advancedPanel.addRow(new JLabel(SharedLocale.tr("options.offlineModePlayerName")), offlineModePlayerNameText);
		SwingHelper.removeOpaqueness(advancedPanel);
		tabbedPane.addTab(SharedLocale.tr("options.advancedTab"), SwingHelper.alignTabbedPane(advancedPanel));

		File pathCurrentDir = DirectoryUtils.tryCanonical(new File("."));
		JTextField pathCurrentDirText = new JTextField(pathCurrentDir.getAbsolutePath());
		JButton openCurrentDirButton = new JButton(SharedLocale.tr("features.openFolder"));
		pathDirPanel.addRow(new JLabel(SharedLocale.tr("options.pathCurrentDir")), openCurrentDirButton);
		pathDirPanel.addRow(pathCurrentDirText);
		pathCurrentDirText.setEditable(false);
		File pathBaseDir = DirectoryUtils.tryCanonical(launcher.getBaseDir());
		JTextField pathBaseDirText = new JTextField(pathBaseDir.getAbsolutePath());
		JButton openBaseDirButton = new JButton(SharedLocale.tr("features.openFolder"));
		pathDirPanel.addRow(new JLabel(SharedLocale.tr("options.pathBaseDir")), openBaseDirButton);
		pathDirPanel.addRow(pathBaseDirText);
		pathBaseDirText.setEditable(false);
		JButton pathCommonDataDirButton = new JButton(SharedLocale.tr("options.pathDirButton"));
		pathDirPanel.addRow(new JLabel(SharedLocale.tr("options.pathCommonDataDir")), pathCommonDataDirButton);
		pathDirPanel.addRow(pathCommonDataDirText);
		pathCommonDataDirText.setEditable(false);
		JButton pathInstancesDirButton = new JButton(SharedLocale.tr("options.pathDirButton"));
		pathDirPanel.addRow(new JLabel(SharedLocale.tr("options.pathInstancesDir")), pathInstancesDirButton);
		pathDirPanel.addRow(pathInstancesDirText);
		pathInstancesDirText.setEditable(false);
		pathDirPanel.addRow(moveFilesCheck);
		SwingHelper.removeOpaqueness(pathDirPanel);
		tabbedPane.addTab(SharedLocale.tr("options.pathDirTab"), SwingHelper.alignTabbedPane(pathDirPanel));

		buttonsPanel.addElement(logButton);
		buttonsPanel.addElement(aboutButton);
		buttonsPanel.addGlue();
		buttonsPanel.addElement(okButton);
		buttonsPanel.addElement(cancelButton);

		tabContainer.add(tabbedPane, BorderLayout.CENTER);
		tabContainer.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		add(tabContainer, BorderLayout.CENTER);
		add(buttonsPanel, BorderLayout.SOUTH);

		SwingHelper.equalWidth(okButton, cancelButton);

		openCurrentDirButton.addActionListener(ActionListeners.browseDir(this, pathCurrentDir, true));
		openBaseDirButton.addActionListener(ActionListeners.browseDir(this, pathBaseDir, true));

		cancelButton.addActionListener(ActionListeners.dispose(this));

		aboutButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				AboutDialog.showAboutDialog(ConfigurationDialog.this);
			}
		});

		okButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				save();
			}
		});

		logButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				ConsoleFrame.showMessages();
			}
		});

		skinButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				new SkinSelectionDialog(ConfigurationDialog.this, launcher, skinText).setVisible(true);
			}
		});

		pathCommonDataDirButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				new DirectorySelectionDialog(ConfigurationDialog.this, launcher.getDirectories(), pathCommonDataDirText, SharedLocale.tr("options.pathCommonDataDirTitle"), launcherDirs.getDefaultCommonDataDir(), null).setVisible(true);
			}
		});

		pathInstancesDirButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				new DirectorySelectionDialog(ConfigurationDialog.this, launcher.getDirectories(), pathInstancesDirText, SharedLocale.tr("options.pathInstancesDirTitle"), launcherDirs.getDefaultInstancesDir(), "instances").setVisible(true);
			}
		});

		Window owner = getOwner();
		if (owner instanceof LauncherFrame) {
			final LauncherFrame frame = (LauncherFrame) owner;
			secretUnlockButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					String runid = secretKeyText.getText();
					Instance runinstance = null;
					for (Instance instance: launcher.getInstances().getInstancesSecret())
						if (StringUtils.equalsIgnoreCase(runid, instance.getKey())) {
							runinstance = instance;
							break;
						}
					if (runinstance!=null) {
						dispose();
						log.info("Launching "+runinstance.getName());
						frame.loadInstances();
						frame.launch(runinstance);
					} else {
						log.warning("Unable to find "+runid);
						SwingHelper.showErrorDialog(frame,
								SharedLocale.tr("errors.missingInstance", runid),
								SharedLocale.tr("errors.missingInstanceTitle", runid));
					}
				}
			});
		} else
			secretUnlockButton.setEnabled(false);

		LauncherStatus.instance.open(DiscordStatus.CONFIG, new WindowDisablable(this), ImmutableMap.<String, String>of());

        addWindowListener(new WindowAdapter() {
        	@Override
        	public void windowActivated(WindowEvent e) {
        		LauncherStatus.instance.update();
        	}
		});
	}

	/**
	 * Save the configuration and close the dialog.
	 */
	public void save() {
		Runnable saveAndClose = new Runnable() {
			@Override
			public void run() {
				String oldskin = config.getSkin();
				mapper.copyFromSwing();
				String newskin = config.getSkin();

				if (!StringUtils.equals(oldskin, config.getSkin())) {
					LocalSkin localSkin = new LocalSkin(launcher, newskin);
					Skin skin = localSkin.getSkin();

					Window owner = getOwner();
					if (owner instanceof LauncherFrame) {
						final LauncherFrame frame = (LauncherFrame) owner;
						frame.updateSkin(skin);
					} else
						launcher.setSkin(skin);
				}

				Persistence.commitAndForget(config);
				dispose();
			}
		};
		if (moveFilesCheck.isSelected())
			moveFiles(saveAndClose);
		else
			saveAndClose.run();
	}

	@Override
	public void dispose() {
		super.dispose();
		LauncherStatus.instance.close(DiscordStatus.CONFIG);
	}

	public void moveFiles(final Runnable callback) {
		File commonDataDirSrc = DirectoryUtils.tryCanonical(launcher.getCommonDataDir());
		File commonDataDirDest = DirectoryUtils.tryCanonical(launcherDirs.getCommonDataDir());
		File commonAssetsDirSrc = DirectoryUtils.tryCanonical(launcher.getAssetsDir());
		File commonAssetsDirDest = DirectoryUtils.tryCanonical(launcherDirs.getAssetsDir());
		File commonLibrariesDirSrc = DirectoryUtils.tryCanonical(launcher.getLibrariesDir());
		File commonLibrariesDirDest = DirectoryUtils.tryCanonical(launcherDirs.getLibrariesDir());
		File commonVersionsDirSrc = DirectoryUtils.tryCanonical(launcher.getVersionsDir());
		File commonVersionsDirDest = DirectoryUtils.tryCanonical(launcherDirs.getVersionsDir());
		File instancesDirSrc = DirectoryUtils.tryCanonical(launcher.getInstancesDir());
		File instancesDirDest = DirectoryUtils.tryCanonical(launcherDirs.getInstancesDir());

		if (!commonDataDirSrc.equals(commonDataDirDest))
			if (
				!(DirectoryUtils.checkMovable(this, commonAssetsDirSrc, commonAssetsDirDest)
						&&DirectoryUtils.checkMovable(this, commonLibrariesDirSrc, commonLibrariesDirDest)
						&&DirectoryUtils.checkMovable(this, commonVersionsDirSrc, commonVersionsDirDest))
			)
				return;
		if (!instancesDirSrc.equals(instancesDirDest))
			if (!(DirectoryUtils.checkMovable(this, instancesDirSrc, instancesDirDest)))
				return;

		DirectoryTasks tasks = new DirectoryTasks(launcher);
		List<ObservableFuture<File>> futures = Lists.newArrayList();
		if (!commonDataDirSrc.equals(commonDataDirDest)) {
			if (!commonAssetsDirSrc.equals(commonAssetsDirDest)&&commonAssetsDirSrc.isDirectory())
				futures.add(tasks.move(this, commonAssetsDirSrc, commonAssetsDirDest));
			if (!commonLibrariesDirSrc.equals(commonLibrariesDirDest)&&commonLibrariesDirSrc.isDirectory())
				futures.add(tasks.move(this, commonLibrariesDirSrc, commonLibrariesDirDest));
			if (!commonVersionsDirSrc.equals(commonVersionsDirDest)&&commonVersionsDirSrc.isDirectory())
				futures.add(tasks.move(this, commonVersionsDirSrc, commonVersionsDirDest));
		}
		if (!instancesDirSrc.equals(instancesDirDest)&&instancesDirSrc.isDirectory())
			futures.add(tasks.move(this, instancesDirSrc, instancesDirDest));

		Futures.addCallback(Futures.allAsList(futures), new FutureCallback<List<File>>() {
			@Override
			public void onSuccess(List<File> result) {
				callback.run();
			}

			@Override
			public void onFailure(Throwable t) {
			}
		}, SwingExecutor.INSTANCE);
	}
}
