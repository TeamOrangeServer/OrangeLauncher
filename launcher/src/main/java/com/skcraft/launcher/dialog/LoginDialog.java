/*
 * SK's Minecraft Launcher
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com> and contributors
 * Please see LICENSE.txt for license information.
 */

package com.skcraft.launcher.dialog;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPasswordField;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import org.apache.commons.lang.StringUtils;

import com.beust.jcommander.internal.Maps;
import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.skcraft.concurrency.ObservableFuture;
import com.skcraft.concurrency.ProgressObservable;
import com.skcraft.launcher.Configuration;
import com.skcraft.launcher.Instance;
import com.skcraft.launcher.Launcher;
import com.skcraft.launcher.auth.Account;
import com.skcraft.launcher.auth.AccountList;
import com.skcraft.launcher.auth.AuthenticationException;
import com.skcraft.launcher.auth.LoginService;
import com.skcraft.launcher.auth.OfflineSession;
import com.skcraft.launcher.auth.Session;
import com.skcraft.launcher.launch.LaunchOptions;
import com.skcraft.launcher.persistence.Persistence;
import com.skcraft.launcher.swing.ActionListeners;
import com.skcraft.launcher.swing.FormPanel;
import com.skcraft.launcher.swing.LinedBoxPanel;
import com.skcraft.launcher.swing.LinkButton;
import com.skcraft.launcher.swing.PopupMouseAdapter;
import com.skcraft.launcher.swing.SwingHelper;
import com.skcraft.launcher.swing.TextFieldPopupMenu;
import com.skcraft.launcher.util.SharedLocale;
import com.skcraft.launcher.util.SwingExecutor;

import lombok.Getter;
import lombok.NonNull;
import net.teamfruit.skcraft.launcher.discordrpc.DiscordStatus;
import net.teamfruit.skcraft.launcher.discordrpc.LauncherStatus;
import net.teamfruit.skcraft.launcher.discordrpc.LauncherStatus.WindowDisablable;
import net.teamfruit.skcraft.launcher.model.modpack.ConnectServerInfo;
import net.teamfruit.skcraft.launcher.swing.InstanceCellFactory;
import net.teamfruit.skcraft.launcher.swing.InstanceCellPanel;
import net.teamfruit.skcraft.launcher.swing.ServerInfoStyle;

/**
 * The login dialog.
 */
public class LoginDialog extends JDialog {

	private final Launcher launcher;
	@Getter
	private final AccountList accounts;
	private LaunchOptions options;
	@Getter
	private Session session;
	@Getter
	private boolean connectServer;

	private final JComboBox<Account> idCombo = new JComboBox<Account>();
	private final JPasswordField passwordText = new JPasswordField();
	private final JCheckBox rememberIdCheck = new JCheckBox(SharedLocale.tr("login.rememberId"));
	private final JCheckBox rememberPassCheck = new JCheckBox(SharedLocale.tr("login.rememberPassword"));
	private final JButton loginServerButton = new JButton(SharedLocale.tr("login.loginServer"));
	private final JButton loginButton = new JButton(SharedLocale.tr("login.login"));
	private final LinkButton recoverButton = new LinkButton(SharedLocale.tr("login.recoverAccount"));
	private final JButton offlineButton = new JButton(SharedLocale.tr("login.playOffline"));
	private final JButton cancelButton = new JButton(SharedLocale.tr("button.cancel"));
	private final FormPanel formPanel = new FormPanel();
	private final LinedBoxPanel buttonsPanel = new LinedBoxPanel(true);

	/**
	 * Create a new login dialog.
	 *
	 * @param owner the owner
	 * @param launcher2
	 * @param launcher the launcher
	 * @param instance
	 */
	public LoginDialog(final Window owner, @NonNull final Launcher launcher, final LaunchOptions options) {
		super(owner, ModalityType.DOCUMENT_MODAL);

		this.launcher = launcher;
		this.accounts = launcher.getAccounts();
		this.options = options;

		setTitle(SharedLocale.tr("login.title"));
		initComponents();
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setMinimumSize(new Dimension(450, 0));
		setResizable(false);
		pack();
		setLocationRelativeTo(owner);

		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(final WindowEvent event) {
				removeListeners();
				dispose();
			}
		});

		Map<String, String> status = Maps.newHashMap();
		if (options!=null) {
			Instance instance = options.getInstance();
			if (instance!=null) {
				status.put("instance", instance.getTitle());
				if (StringUtils.isEmpty(instance.getKey())) {
					ConnectServerInfo server = instance.getServer();
					if (server!=null)
						status.put("server", server.toString());
				}
			}
		}
		LauncherStatus.instance.open(DiscordStatus.LOGIN, new WindowDisablable(this), status);

        addWindowListener(new WindowAdapter() {
        	@Override
        	public void windowActivated(WindowEvent e) {
        		LauncherStatus.instance.update();
        	}
		});
	}

	@Override
	public void dispose() {
		super.dispose();
		LauncherStatus.instance.close(DiscordStatus.WAITING);
		LauncherStatus.instance.close(DiscordStatus.LOGIN);
	}

	private void removeListeners() {
		this.idCombo.setModel(new DefaultComboBoxModel<Account>());
	}

	private void initComponents() {
		this.idCombo.setModel(getAccounts());
		updateSelection();

		this.rememberIdCheck.setBorder(BorderFactory.createEmptyBorder());
		this.rememberPassCheck.setBorder(BorderFactory.createEmptyBorder());
		this.idCombo.setEditable(true);
		this.idCombo.getEditor().selectAll();

		this.loginButton.setFont(this.loginButton.getFont().deriveFont(Font.BOLD));
		this.loginServerButton.setFont(this.loginServerButton.getFont().deriveFont(Font.BOLD));

		this.formPanel.addRow(new JLabel(SharedLocale.tr("login.idEmail")), this.idCombo);
		this.formPanel.addRow(new JLabel(SharedLocale.tr("login.password")), this.passwordText);
		this.formPanel.addRow(new JLabel(), this.rememberIdCheck);
		this.formPanel.addRow(new JLabel(), this.rememberPassCheck);
		this.buttonsPanel.setBorder(BorderFactory.createEmptyBorder(26, 13, 13, 13));

		final Instance instance;
		if (options!=null)
			instance = options.getInstance();
		else
			instance = null;

		if (this.launcher.getConfig().isOfflineModeEnabled()) {
			this.buttonsPanel.addElement(this.offlineButton);
			this.buttonsPanel.addElement(Box.createHorizontalStrut(2));
		}

		//this.buttonsPanel.addElement(this.recoverButton);
		this.buttonsPanel.addGlue();
		if (instance!=null) {
			ConnectServerInfo server = instance.getServer();
			if (server!=null&&server.isValid())
				this.buttonsPanel.addElement(this.loginServerButton);
		}
		this.buttonsPanel.addElement(this.loginButton);
		this.buttonsPanel.addElement(this.cancelButton);

		final InstanceCellPanel title;
		if (instance!=null) {
			title = InstanceCellFactory.instance.getCellComponent(this.getRootPane(), instance, false, ServerInfoStyle.DETAILS);
			add(title, BorderLayout.NORTH);
		} else
			title = null;
		add(this.formPanel, BorderLayout.CENTER);
		add(this.buttonsPanel, BorderLayout.SOUTH);

		getRootPane().setDefaultButton(loginServerButton.getParent()!=null ? this.loginServerButton : this.loginButton);

		this.passwordText.setComponentPopupMenu(TextFieldPopupMenu.INSTANCE);

		this.idCombo.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				updateSelection();
			}
		});

		this.idCombo.getEditor().getEditorComponent().addMouseListener(new PopupMouseAdapter() {
			@Override
			protected void showPopup(final MouseEvent e) {
				popupManageMenu(e.getComponent(), e.getX(), e.getY());
			}
		});

		this.recoverButton.addActionListener(
				ActionListeners.openURL(this.recoverButton, this.launcher.getProperties().getProperty("resetPasswordUrl")));

		final Predicate<Boolean> updateMessagePredicate = new Predicate<Boolean>() {
			@Override
			public boolean apply(Boolean input) {
				if (input!=null&&input)
					loginServerButton.setText(SharedLocale.tr("login.loginServer"));
				else
					loginServerButton.setText(SharedLocale.tr("login.loginServerOffline"));
				return true;
			}
		};
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				if (title!=null)
					updateMessagePredicate.apply(title.getServerInfoPanel().isOnline());
			}
		});
		this.loginServerButton.addActionListener(new ActionListener() {
			private boolean enable;
			private Predicate<Boolean> joinPredicate = new Predicate<Boolean>() {
				@Override
				public boolean apply(Boolean input) {
					updateMessagePredicate.apply(input);
					if (enable&&input!=null&&input) {
						enable = false;
						connectServer = true;
						prepareLogin();
					}
					return true;
				}
			};

			{
				if (title!=null)
					title.getServerInfoPanel().setStatusCallback(joinPredicate);
			}

			@Override
			public void actionPerformed(final ActionEvent e) {
				enable = true;
				if (title==null)
					joinPredicate.apply(true);
				else {
					boolean online = title.getServerInfoPanel().isOnline();
					updateMessagePredicate.apply(online);
					if (online)
						joinPredicate.apply(true);
					else {
						loginServerButton.setText(SharedLocale.tr("login.loginServerWaiting"));
						Map<String, String> status = Maps.newHashMap();
						if (instance!=null) {
							status.put("instance", instance.getTitle());
							if (StringUtils.isEmpty(instance.getKey())) {
								ConnectServerInfo server = instance.getServer();
								if (server!=null)
									status.put("server", server.toString());
							}
						}
						LauncherStatus.instance.open(DiscordStatus.WAITING, new WindowDisablable(LoginDialog.this), status);
					}
				}
			}
		});

		this.loginButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				connectServer = false;
				prepareLogin();
			}
		});

		this.offlineButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				String playerName = LoginDialog.this.launcher.getConfig().getOfflineModePlayerName();
				if (StringUtils.isEmpty(playerName))
					playerName = LoginDialog.this.launcher.getProperties().getProperty("offlinePlayerName");
				setResult(new OfflineSession(playerName));
				removeListeners();
				dispose();
			}
		});

		this.cancelButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				removeListeners();
				dispose();
			}
		});

		this.rememberPassCheck.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				if (LoginDialog.this.rememberPassCheck.isSelected())
					LoginDialog.this.rememberIdCheck.setSelected(true);
			}
		});

		this.rememberIdCheck.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				if (!LoginDialog.this.rememberIdCheck.isSelected())
					LoginDialog.this.rememberPassCheck.setSelected(false);
			}
		});
	}

	private void popupManageMenu(final Component component, final int x, final int y) {
		final Object selected = this.idCombo.getSelectedItem();
		final JPopupMenu popup = new JPopupMenu();
		JMenuItem menuItem;

		if (selected!=null&&selected instanceof Account) {
			final Account account = (Account) selected;

			menuItem = new JMenuItem(SharedLocale.tr("login.forgetUser"));
			menuItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					LoginDialog.this.accounts.remove(account);
					Persistence.commitAndForget(LoginDialog.this.accounts);
				}
			});
			popup.add(menuItem);

			if (!Strings.isNullOrEmpty(account.getPassword())) {
				menuItem = new JMenuItem(SharedLocale.tr("login.forgetPassword"));
				menuItem.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(final ActionEvent e) {
						account.setPassword(null);
						Persistence.commitAndForget(LoginDialog.this.accounts);
					}
				});
				popup.add(menuItem);
			}
		}

		menuItem = new JMenuItem(SharedLocale.tr("login.forgetAllPasswords"));
		menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				if (
					SwingHelper.confirmDialog(LoginDialog.this,
							SharedLocale.tr("login.confirmForgetAllPasswords"),
							SharedLocale.tr("login.forgetAllPasswordsTitle"))
				) {
					LoginDialog.this.accounts.forgetPasswords();
					Persistence.commitAndForget(LoginDialog.this.accounts);
				}
			}
		});
		popup.add(menuItem);

		popup.show(component, x, y);
	}

	private void updateSelection() {
		final Object selected = this.idCombo.getSelectedItem();

		if (selected!=null&&selected instanceof Account) {
			final Account account = (Account) selected;
			final String password = account.getPassword();

			this.rememberIdCheck.setSelected(true);
			if (!Strings.isNullOrEmpty(password)) {
				this.rememberPassCheck.setSelected(true);
				this.passwordText.setText(password);
			} else
				this.rememberPassCheck.setSelected(false);
		} else {
			this.passwordText.setText("");
			this.rememberIdCheck.setSelected(true);
			this.rememberPassCheck.setSelected(false);
		}
	}

	@SuppressWarnings("deprecation")
	private void prepareLogin() {
		final Object selected = this.idCombo.getSelectedItem();

		if (selected!=null&&selected instanceof Account) {
			final Account account = (Account) selected;
			final String password = this.passwordText.getText();

			if (password==null||password.isEmpty())
				SwingHelper.showErrorDialog(this, SharedLocale.tr("login.noPasswordError"), SharedLocale.tr("login.noPasswordTitle"));
			else {
				if (this.rememberPassCheck.isSelected())
					account.setPassword(password);
				else
					account.setPassword(null);

				if (this.rememberIdCheck.isSelected())
					this.accounts.add(account);
				else
					this.accounts.remove(account);

				account.setLastUsed(new Date());

				Persistence.commitAndForget(this.accounts);

				attemptLogin(account, password);
			}
		} else
			SwingHelper.showErrorDialog(this, SharedLocale.tr("login.noLoginError"), SharedLocale.tr("login.noLoginTitle"));
	}

	private void attemptLogin(final Account account, final String password) {
		final LoginCallable callable = new LoginCallable(account, password);
		final ObservableFuture<Session> future = new ObservableFuture<Session>(
				this.launcher.getExecutor().submit(callable), callable);

		Futures.addCallback(future, new FutureCallback<Session>() {
			@Override
			public void onSuccess(final Session result) {
				setResult(result);
			}

			@Override
			public void onFailure(final Throwable t) {
			}
		}, SwingExecutor.INSTANCE);

		ProgressDialog.showProgress(this, future, SharedLocale.tr("login.loggingInTitle"), SharedLocale.tr("login.loggingInStatus"));
		SwingHelper.addErrorDialogCallback(this, future);
	}

	private void setResult(final Session session) {
		this.session = session;
		removeListeners();
		dispose();
	}

	public static Session showLoginRequest(final Window window, final Launcher launcher) {
		final LoginDialog dialog = new LoginDialog(window, launcher, null);
		dialog.setVisible(true);
		return dialog.getSession();
	}

	public static LoginDialog showLoginRequest(final LaunchOptions options, final Launcher launcher) {
		final LoginDialog dialog = new LoginDialog(options.getWindow(), launcher, options);
		dialog.setVisible(true);
		return dialog;
	}

	private class LoginCallable implements Callable<Session>, ProgressObservable {
		private final Account account;
		private final String password;

		private LoginCallable(final Account account, final String password) {
			this.account = account;
			this.password = password;
		}

		@Override
		public Session call() throws AuthenticationException, IOException, InterruptedException {
			final LoginService service = LoginDialog.this.launcher.getLoginService();
			final List<? extends Session> identities = service.login(LoginDialog.this.launcher.getProperties().getProperty("agentName"), this.account.getId(), this.password);

			// The list of identities (profiles in Mojang terms) corresponds to whether the account
			// owns the game, so we need to check that
			if (identities.size()>0) {
				// Set offline enabled flag to true
				final Configuration config = LoginDialog.this.launcher.getConfig();
				if (!config.isOfflineEnabled()) {
					config.setOfflineEnabled(true);
					Persistence.commitAndForget(config);
				}

				Persistence.commitAndForget(getAccounts());
				return identities.get(0);
			} else
				throw new AuthenticationException("Minecraft not owned", SharedLocale.tr("login.minecraftNotOwnedError"));
		}

		@Override
		public double getProgress() {
			return -1;
		}

		@Override
		public String getStatus() {
			return SharedLocale.tr("login.loggingInStatus");
		}
	}

}
