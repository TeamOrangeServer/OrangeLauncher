/*
 * SK's Minecraft Launcher
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com> and contributors
 * Please see LICENSE.txt for license information.
 */

package com.skcraft.launcher.dialog;

import static com.skcraft.launcher.util.SharedLocale.*;

import java.awt.AlphaComposite;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.lang.ref.WeakReference;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.plaf.basic.BasicPanelUI;

import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableMap;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.skcraft.concurrency.ObservableFuture;
import com.skcraft.launcher.FancyBackgroundPanel;
import com.skcraft.launcher.Instance;
import com.skcraft.launcher.InstanceList;
import com.skcraft.launcher.Launcher;
import com.skcraft.launcher.launch.LaunchListener;
import com.skcraft.launcher.launch.LaunchOptions;
import com.skcraft.launcher.launch.LaunchOptions.UpdatePolicy;
import com.skcraft.launcher.swing.ActionListeners;
import com.skcraft.launcher.swing.DoubleClickToButtonAdapter;
import com.skcraft.launcher.swing.InstanceTable;
import com.skcraft.launcher.swing.InstanceTableModel;
import com.skcraft.launcher.swing.PopupMouseAdapter;
import com.skcraft.launcher.swing.SwingHelper;
import com.skcraft.launcher.swing.WebpagePanel;
import com.skcraft.launcher.util.SharedLocale;
import com.skcraft.launcher.util.SwingExecutor;

import lombok.Getter;
import lombok.NonNull;
import lombok.extern.java.Log;
import net.teamfruit.skcraft.launcher.TipList;
import net.teamfruit.skcraft.launcher.appicon.AppIcon;
import net.teamfruit.skcraft.launcher.discordrpc.DiscordStatus;
import net.teamfruit.skcraft.launcher.discordrpc.LauncherStatus;
import net.teamfruit.skcraft.launcher.discordrpc.LauncherStatus.WindowDisablable;
import net.teamfruit.skcraft.launcher.integration.AppleHandler;
import net.teamfruit.skcraft.launcher.skins.LocalSkin;
import net.teamfruit.skcraft.launcher.skins.RemoteSkin;
import net.teamfruit.skcraft.launcher.skins.RemoteSkinList;
import net.teamfruit.skcraft.launcher.skins.Skin;
import net.teamfruit.skcraft.launcher.skins.SkinUtils;
import net.teamfruit.skcraft.launcher.swing.BoardPanel;
import net.teamfruit.skcraft.launcher.swing.InstanceCellFactory;
import net.teamfruit.skcraft.launcher.swing.InstanceCellPanel;
import net.teamfruit.skcraft.launcher.swing.ServerInfoStyle;
import net.teamfruit.skcraft.launcher.swing.TipsPanel;
import net.teamfruit.skcraft.launcher.swing.WebpageScrollBarUI;
import net.teamfruit.skcraft.launcher.util.SharedLocaleUpdater;

/**
 * The main launcher frame.
 */
@Log
public class LauncherFrame extends JFrame {

    private final Launcher launcher;

    private final SharedLocaleUpdater localeUpdater = SharedLocaleUpdater.create();

    private JPanel container;

	@Getter
    private final InstanceTable instancesTable = new InstanceTable();
    private final InstanceTableModel instancesModel;
    @Getter
    private final JScrollPane instanceScroll = new JScrollPane(this.instancesTable);
    private WebpagePanel webView;
    private BoardPanel<InstanceCellPanel> selectedPane;
    private JPanel splitPane;
    private final JButton launchButton = localeUpdater.tr(new JButton(), "launcher.launch");
    private final JButton refreshButton = localeUpdater.tr(new JButton(), "launcher.checkForUpdates");
    private final JButton optionsButton = localeUpdater.tr(new JButton(), "launcher.options");
    //private final JButton selfUpdateButton = localeUpdater.tr(new JButton(), "launcher.updateLauncher");
    private final JCheckBox updateCheck = localeUpdater.tr(new JCheckBox(), "launcher.downloadUpdates");

    /**
     * Create a new frame.
     *
     * @param launcher the launcher
     */
    public LauncherFrame(@NonNull final Launcher launcher) {
    	localeUpdater.tr(this, "launcher.title", launcher.getVersion());

        this.launcher = launcher;
        this.instancesModel = new InstanceTableModel(launcher.getInstances());

        AppleHandler.register(launcher, this);

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(750, 500));
        initComponents();
        pack();
        setLocationRelativeTo(null);

        AppIcon.setFrameIconSet(this, AppIcon.getSwingIconSet(AppIcon.getAppIconSet()));

        addWindowListener(new WindowAdapter() {
        	@Override
        	public void windowActivated(WindowEvent e) {
        		LauncherStatus.instance.update();
        	}
		});
    }

    private void setExpand(boolean visible) {
		rightPane.setVisible(visible);
		final int rightWidth = rightPane.getWidth();
		setMinimumSize(new Dimension(visible?750:500, 500));
		setSize(new Dimension(visible?750:750-rightWidth, getHeight()));
    }

    private void loadTips() {
    	final ObservableFuture<TipList> future = launcher.getInstanceTasks().reloadTips(LauncherFrame.this);
    	Futures.addCallback(future, new FutureCallback<TipList>() {
			@Override
			public void onSuccess(TipList result) {
				if (result!=null)
					TipsPanel.instance.updateTipList(result.getTipList());
			}

			@Override
			public void onFailure(Throwable t) {
			}
		}, SwingExecutor.INSTANCE);
    }

    private void loadSkinList() {
    	SkinUtils.loadSkinList(this, launcher, false, new Predicate<RemoteSkinList>() {
			@Override
			public boolean apply(RemoteSkinList remoteSkinList) {
				if (remoteSkinList!=null) {
					String skinname = launcher.getConfig().getSkin();
					RemoteSkin remoteSkin = remoteSkinList.getRemoteSkin(skinname);
					SkinUtils.loadSkin(LauncherFrame.this, launcher, false, remoteSkin, new Predicate<RemoteSkin>() {
						@Override
						public boolean apply(RemoteSkin remoteSkin) {
							if (remoteSkin!=null) {
								LocalSkin localSkin = remoteSkin.getLocalSkin();
								if (localSkin!=null) {
									Skin skin = localSkin.getSkin();
									updateSkin(skin);
								}
							}
							return true;
						}
					});
				}
				return true;
			}
		});
    }

    public void updateSkin(Skin skin) {
		if (skin!=null&&!skin.equals(launcher.getSkin())) {
			launcher.setSkin(skin);
			//loadTips();
			localeUpdater.update();
			webView.browse(launcher.getNewsURL(), false);
			initSkin(skin);
			repaint();
		}
    }

    public void initSkin(Skin skin) {
		setExpand(skin.isShowList());
    	loadTips();
		loadInstances();
    }

    private void initComponents() {
    	setResizable(false);

        container = createContainerPanel();
        container.setBackground(Color.WHITE);
        container.setLayout(new BorderLayout());

        this.webView = createNewsPanel();
        this.webView.setBrowserBorder(BorderFactory.createEmptyBorder());
        this.webView.setPreferredSize(new Dimension(250, 250));
        final JScrollPane webViewScroll = this.webView.getDocumentScroll();
        webViewScroll.getVerticalScrollBar().setUI(new WebpageScrollBarUI(webViewScroll));
        webViewScroll.getHorizontalScrollBar().setUI(new WebpageScrollBarUI(webViewScroll));
        // this.webView.getDocumentScroll().getVerticalScrollBar().setPreferredSize(new Dimension(10, 0));

        this.splitPane = new JPanel(new BorderLayout());

        this.selectedPane = new BoardPanel<InstanceCellPanel>();
        this.selectedPane.setOpaque(false);
        this.selectedPane.setPreferredSize(new Dimension(250, 60));
        localeUpdater.trTooltip(selectedPane, "launcher.launchButton");

        //this.selfUpdateButton.setVisible(this.launcher.getUpdateManager().getPendingUpdate());

        this.launcher.getUpdateManager().addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(final PropertyChangeEvent evt) {
                if (evt.getPropertyName().equals("readyUpdate"))
                	if (!BooleanUtils.toBoolean(System.getProperty("com.skcraft.launcher.noupdate")))
                		launcher.getUpdateManager().performUpdate(LauncherFrame.this);
            }
        });
        /*
        this.launcher.getUpdateManager().addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(final PropertyChangeEvent evt) {
                if (evt.getPropertyName().equals("pendingUpdate")) {
                	final boolean enabled = (Boolean) evt.getNewValue();
					LauncherFrame.this.selfUpdateButton.setVisible(enabled);
					if (enabled)
						LauncherFrame.this.selfUpdateButton.doClick();
                }
            }
        });
        */

        this.updateCheck.setSelected(true);
        this.instancesTable.setModel(this.instancesModel);

        this.instanceScroll.setPreferredSize(new Dimension(250, this.instanceScroll.getPreferredSize().height));
        this.instanceScroll.getVerticalScrollBar().setUI(new WebpageScrollBarUI(this.instanceScroll));
        this.instanceScroll.getHorizontalScrollBar().setUI(new WebpageScrollBarUI(this.instanceScroll));
        this.instanceScroll.setBorder(BorderFactory.createEmptyBorder());

        this.launchButton.setFont(this.launchButton.getFont().deriveFont(Font.BOLD));
        final JButton expandButton = new JButton(">");
        final JPanel buttons = new JPanel(new GridLayout(3, 1));
        this.refreshButton.setIcon(SwingHelper.createIcon(Launcher.class, "refresh_icon.png", 20, 20));
        localeUpdater.trTooltip(refreshButton, "launcher.refreshButton");
        this.refreshButton.setText(null);
        buttons.add(this.refreshButton);
        // buttons.add(this.updateCheck);
        // buttons.add(this.selfUpdateButton);
        this.optionsButton.setIcon(SwingHelper.createIcon(Launcher.class, "settings_icon.png", 20, 20));
        localeUpdater.trTooltip(optionsButton, "launcher.optionButton");
        this.optionsButton.setText(null);
        buttons.add(this.optionsButton);
        // buttons.add(this.launchButton);
        expandButton.setIcon(SwingHelper.createIcon(Launcher.class, "expand_icon.png", 20, 20));
        localeUpdater.trTooltip(expandButton, "launcher.expandButton");
        expandButton.setText(null);
        buttons.add(expandButton);
        SwingHelper.removeOpaqueness(buttons);
        final JPanel leftBottomTopPanel = new JPanel(new GridBagLayout());
        final JLabel leftBottomTopText = localeUpdater.tr(new JLabel(), "launcher.launchTitle");
        leftBottomTopText.setFont(new Font(leftBottomTopText.getFont().getName(), Font.PLAIN, 16));
        leftBottomTopText.setForeground(Color.WHITE);
        leftBottomTopPanel.add(leftBottomTopText);
        leftBottomTopPanel.setUI(new BasicPanelUI());
        leftBottomTopPanel.setBackground(new Color(32, 30, 98));
        final JPanel leftBottomPane = new JPanel(new BorderLayout());
        leftBottomPane.add(leftBottomTopPanel, BorderLayout.NORTH);
        leftBottomPane.add(this.selectedPane, BorderLayout.CENTER);
        leftBottomPane.add(buttons, BorderLayout.EAST);
        leftBottomPane.setOpaque(false);
        final JPanel leftPane = new JPanel(new BorderLayout());
        leftPane.add(this.webView, BorderLayout.CENTER);
        leftPane.add(leftBottomPane, BorderLayout.SOUTH);
        leftPane.setOpaque(false);
        final JLabel instanceLabel = localeUpdater.tr(new JLabel(SwingHelper.createIcon(Launcher.class, "package_icon.png", 20, 20),SwingConstants.LEFT), "launcher.instance");
        instanceLabel.setFont(new Font(instanceLabel.getFont().getName(), Font.PLAIN, 16));
        instanceLabel.setBorder(new EmptyBorder(3, 3, 3, 3));
        instanceLabel.setForeground(Color.WHITE);
        rightPane = new JPanel(new BorderLayout()) {
        	@Override
        	protected void paintComponent(Graphics g) {
        		if(!isOpaque()) {
        			Graphics2D g2d = (Graphics2D) g.create();
        			g2d.setRenderingHint(
        		            RenderingHints.KEY_ANTIALIASING,
        		            RenderingHints.VALUE_ANTIALIAS_ON);
    		        g2d.setComposite(AlphaComposite.getInstance(
    		        		AlphaComposite.SRC_OVER, 0.3f));
	        		g2d.setColor(Color.BLACK);
	        		g2d.fillRect(0, 0, getWidth(), getHeight());
	        		g2d.dispose();
        		}
        		super.paintComponent(g);
        	}
        };
        rightPane.add(instanceLabel, BorderLayout.NORTH);
        rightPane.add(this.instanceScroll, BorderLayout.CENTER);
        //rightPane.setVisible(true);
        SwingHelper.removeOpaqueness(instanceScroll);
        rightPane.setOpaque(false);
        this.splitPane.add(leftPane, BorderLayout.CENTER);
        this.splitPane.add(rightPane, BorderLayout.EAST);
        this.splitPane.setOpaque(false);

        expandButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				final boolean visible = !rightPane.isVisible();
				setExpand(visible);
			}
		});
        // SwingHelper.flattenJSplitPane(this.splitPane);

        container.add(this.splitPane, BorderLayout.CENTER);
        add(container, BorderLayout.CENTER);

        this.instancesModel.addTableModelListener(new TableModelListener() {
            @Override
            public void tableChanged(final TableModelEvent e) {
                if (LauncherFrame.this.instancesTable.getRowCount() > 0)
					LauncherFrame.this.instancesTable.setRowSelectionInterval(0, 0);
            }
        });

		final Cursor cursorhand = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
		final Cursor cursornormal = Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR);
		this.selectedPane.setCursor(cursorhand);
		this.selectedPane.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(final MouseEvent e) {
				LauncherFrame.this.launchButton.doClick();
			}

			@Override
			public void mouseEntered(MouseEvent e) {
				InstanceCellPanel panel = selectedPane.get();
				if (panel!=null) {
					panel.setShowSelected(true);
					selectedPane.repaint();
				}
			}

			@Override
			public void mouseExited(MouseEvent e) {
				InstanceCellPanel panel = selectedPane.get();
				if (panel!=null) {
					panel.setShowSelected(false);
					selectedPane.repaint();
				}
			}
		});

        instancesTable.addMouseListener(new DoubleClickToButtonAdapter(launchButton));

		this.instancesTable.addMouseMotionListener(new MouseAdapter() {
			@Override
			public void mouseMoved(final MouseEvent e) {
				final int i = LauncherFrame.this.instancesTable.rowAtPoint(e.getPoint());
                if (i>=0)
					LauncherFrame.this.instancesTable.setCursor(cursorhand);
				else
					LauncherFrame.this.instancesTable.setCursor(cursornormal);
			}
		});
		this.instancesTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(final ListSelectionEvent e) {
		        if (!e.getValueIsAdjusting()) {
		        	int index = LauncherFrame.this.instancesTable.getSelectionModel().getLeadSelectionIndex();
		        	if (index<0)
		        		index = 0;
		        	final Instance instance = LauncherFrame.this.instancesModel.getValueAt(index, 0);

		        	final InstanceCellPanel tablecell = InstanceCellFactory.instance.getCellComponent(LauncherFrame.this.selectedPane, instance, false, ServerInfoStyle.NORMAL);
		        	tablecell.setShowPlayIcon(true);
		    		LauncherFrame.this.selectedPane.set(tablecell);
		        }
			}
		});

        this.refreshButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                loadInstances();
                LauncherFrame.this.launcher.getUpdateManager().checkForUpdate();
                LauncherFrame.this.webView.browse(LauncherFrame.this.launcher.getNewsURL(), false);
            }
        });

        /*
        this.selfUpdateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                LauncherFrame.this.launcher.getUpdateManager().performUpdate(LauncherFrame.this);
            }
        });
        */

        this.optionsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                showOptions();
            }
        });

        this.launchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                launch();
            }
        });

        this.instancesTable.addMouseListener(new PopupMouseAdapter() {
            @Override
            protected void showPopup(final MouseEvent e) {
                final int index = LauncherFrame.this.instancesTable.rowAtPoint(e.getPoint());
                Instance selected = null;
                if (index >= 0) {
                    LauncherFrame.this.instancesTable.setRowSelectionInterval(index, index);
                    selected = LauncherFrame.this.launcher.getInstances().get(index);
                }
                popupInstanceMenu(e.getComponent(), e.getX(), e.getY(), selected);
            }
        });

    	loadSkinList();
		initSkin(launcher.getSkin());
    }

    protected JPanel createContainerPanel() {
        return new FancyBackgroundPanel(launcher);
    }

    /**
     * Return the news panel.
     *
     * @return the news panel
     */
    protected WebpagePanel createNewsPanel() {
        return WebpagePanel.forURL(this.launcher.getNewsURL(), false);
    }

    /**
     * Popup the menu for the instances.
     *
     * @param component the component
     * @param x mouse X
     * @param y mouse Y
     * @param selected the selected instance, possibly null
     */
    private void popupInstanceMenu(final Component component, final int x, final int y, final Instance selected) {
        final JPopupMenu popup = new JPopupMenu();
        JMenuItem menuItem;

        if (selected != null) {
            menuItem = new JMenuItem(!selected.isLocal() ? tr("instance.install") : tr("instance.launch"));
            menuItem.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(final ActionEvent e) {
                    launch();
                }
            });
            popup.add(menuItem);

            if (selected.isLocal()) {
                popup.addSeparator();

                menuItem = new JMenuItem(SharedLocale.tr("instance.openFolder"));
                menuItem.addActionListener(ActionListeners.browseDir(
                        LauncherFrame.this, selected.getContentDir(), true));
                popup.add(menuItem);

                menuItem = new JMenuItem(SharedLocale.tr("instance.openMods"));
                menuItem.addActionListener(ActionListeners.browseDir(
                        LauncherFrame.this, new File(selected.getContentDir(), "mods"), true));
                popup.add(menuItem);

                menuItem = new JMenuItem(SharedLocale.tr("instance.openConfig"));
                menuItem.addActionListener(ActionListeners.browseDir(
                        LauncherFrame.this, new File(selected.getContentDir(), "config"), true));
                popup.add(menuItem);

                menuItem = new JMenuItem(SharedLocale.tr("instance.openSaves"));
                menuItem.addActionListener(ActionListeners.browseDir(
                        LauncherFrame.this, new File(selected.getContentDir(), "saves"), true));
                popup.add(menuItem);

                menuItem = new JMenuItem(SharedLocale.tr("instance.openResourcePacks"));
                menuItem.addActionListener(ActionListeners.browseDir(
                        LauncherFrame.this, new File(selected.getContentDir(), "resourcepacks"), true));
                popup.add(menuItem);

                menuItem = new JMenuItem(SharedLocale.tr("instance.openScreenshots"));
                menuItem.addActionListener(ActionListeners.browseDir(
                        LauncherFrame.this, new File(selected.getContentDir(), "screenshots"), true));
                popup.add(menuItem);

                menuItem = new JMenuItem(SharedLocale.tr("instance.copyAsPath"));
                menuItem.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(final ActionEvent e) {
                        final File dir = selected.getContentDir();
                        dir.mkdirs();
                        SwingHelper.setClipboard(dir.getAbsolutePath());
                    }
                });
                popup.add(menuItem);

                popup.addSeparator();

                if (!selected.isUpdatePending()) {
                    menuItem = new JMenuItem(SharedLocale.tr("instance.forceUpdate"));
                    menuItem.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(final ActionEvent e) {
                            selected.setUpdatePending(true);
                            launch();
                            LauncherFrame.this.instancesModel.update();
                        }
                    });
                    popup.add(menuItem);
                }

                menuItem = new JMenuItem(SharedLocale.tr("instance.hardForceUpdate"));
                menuItem.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(final ActionEvent e) {
                        confirmHardUpdate(selected);
                    }
                });
                popup.add(menuItem);

                menuItem = new JMenuItem(SharedLocale.tr("instance.deleteFiles"));
                menuItem.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(final ActionEvent e) {
                        confirmDelete(selected);
                    }
                });
                popup.add(menuItem);
            }

            popup.addSeparator();
        }

        menuItem = new JMenuItem(SharedLocale.tr("launcher.refreshList"));
        menuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                loadInstances();
            }
        });
        popup.add(menuItem);

        popup.show(component, x, y);

    }

    @Getter private boolean firstLoaded = true;

	private JPanel rightPane;

	private void onInstanceReady() {
        if (firstLoaded) {
        	firstLoaded = false;
            LauncherStatus.instance.open(DiscordStatus.MENU, new WindowDisablable(this), ImmutableMap.<String, String>of());
        	processRun();
        }
    }

	@Override
	public void dispose() {
		super.dispose();
		LauncherStatus.instance.close(DiscordStatus.MENU);
	}

	public boolean processRun() {
		launcher.getOptions().processURI();
		String runid = launcher.getOptions().getRun();
		String runkey = launcher.getOptions().getKey();
		if (launchFromID(runid, runkey, true))
			return true;
		String skinrunid = launcher.getSkin().getLoginModPack();
		if (launchFromID(skinrunid, null, false))
			return true;
    	return false;
	}

	private boolean launchFromID(String runid, String runkey, boolean keyRequired) {
		if (!StringUtils.isEmpty(runid)) {
			log.info("Trying to launch "+runid);
			Instance runinstance = null;
			for (Instance instance: launcher.getInstances().getInstances())
				if (StringUtils.equalsIgnoreCase(runid, instance.getName())||StringUtils.equalsIgnoreCase(runid, instance.getKey())) {
					runinstance = instance;
					break;
				}
			if (runinstance==null)
				for (Instance instance: launcher.getInstances().getInstancesSecret())
					if (StringUtils.equalsIgnoreCase(runid, instance.getKey())||
							(StringUtils.equalsIgnoreCase(runid, instance.getName())&&(!keyRequired||StringUtils.equalsIgnoreCase(runkey, instance.getKey())))) {
						runinstance = instance;
						break;
					}
			if (runinstance!=null) {
				log.info("Launching "+runinstance.getName());
				launch(runinstance);
				//loadInstances();
				return true;
			} else {
				log.warning("Unable to find "+runid);
				SwingHelper.showErrorDialog(LauncherFrame.this,
						SharedLocale.tr("errors.missingInstance", runid),
						SharedLocale.tr("errors.missingInstanceTitle", runid));
			}
		}
		return false;
	}

	private void selectInstance(String instanceName) {
		InstanceList list = instancesModel.getInstances();
		if (list.size()>0) {
			int findindex = 0;
			if (!StringUtils.isEmpty(instanceName)) {
				for (int index = 0; index<list.size(); index++) {
					Instance instance = list.get(index);
					if (StringUtils.equals(instance.getName(), instanceName)) {
						findindex = index;
						break;
					}
				}
			}
			if (findindex>=0)
				instancesTable.setRowSelectionInterval(findindex, findindex);
		}
	}

    private void confirmDelete(final Instance instance) {
        if (!SwingHelper.confirmDialog(this,
                tr("instance.confirmDelete", instance.getTitle()), SharedLocale.tr("confirmTitle")))
			return;

        final ObservableFuture<Instance> future = this.launcher.getInstanceTasks().delete(this, instance);

        // Update the list of instances after updating
        future.addListener(new Runnable() {
            @Override
            public void run() {
                loadInstances();
            }
        }, SwingExecutor.INSTANCE);
    }

    private void confirmHardUpdate(final Instance instance) {
        if (!SwingHelper.confirmDialog(this, SharedLocale.tr("instance.confirmHardUpdate"), SharedLocale.tr("confirmTitle")))
			return;

        final ObservableFuture<Instance> future = this.launcher.getInstanceTasks().hardUpdate(this, instance);

        // Update the list of instances after updating
        future.addListener(new Runnable() {
            @Override
            public void run() {
                launch();
                LauncherFrame.this.instancesModel.update();
            }
        }, SwingExecutor.INSTANCE);
    }

    public void loadInstances() {
        final ObservableFuture<InstanceList> future = this.launcher.getInstanceTasks().reloadInstances(this);

        future.addListener(new Runnable() {
            @Override
            public void run() {
        		String defaultModPack = launcher.getSkin().getSelectModPack();
        		instancesModel.getInstances().unlock(defaultModPack);

                instancesModel.update();

        		selectInstance(defaultModPack);

            	onInstanceReady();
                requestFocus();
            }
        }, SwingExecutor.INSTANCE);

        ProgressDialog.showProgress(this, future, SharedLocale.tr("launcher.checkingTitle"), SharedLocale.tr("launcher.checkingStatus"));
        SwingHelper.addErrorDialogCallback(this, future);
    }

    private void showOptions() {
        final ConfigurationDialog configDialog = new ConfigurationDialog(this, this.launcher);
        configDialog.setVisible(true);
    }

    public void launch() {
    	launch(this.launcher.getInstances().get(this.instancesTable.getSelectedRow()));
    }

    public void launch(Instance instance) {
        final boolean permitUpdate = this.updateCheck.isSelected();

        final LaunchOptions options = new LaunchOptions.Builder()
                .setInstance(instance)
                .setListener(new LaunchListenerImpl(this))
                .setUpdatePolicy(permitUpdate ? UpdatePolicy.UPDATE_IF_SESSION_ONLINE : UpdatePolicy.NO_UPDATE)
                .setWindow(this)
                .build();
        this.launcher.getLaunchSupervisor().launch(options);
    }

    private static class LaunchListenerImpl implements LaunchListener {
        private final WeakReference<LauncherFrame> frameRef;
        private final Launcher launcher;

        private LaunchListenerImpl(final LauncherFrame frame) {
            this.frameRef = new WeakReference<LauncherFrame>(frame);
            this.launcher = frame.launcher;
        }

        @Override
        public void instancesUpdated() {
            final LauncherFrame frame = this.frameRef.get();
            if (frame != null)
				frame.loadInstances();
        }

        @Override
        public void gameStarted() {
            final LauncherFrame frame = this.frameRef.get();
            if (frame != null)
				frame.dispose();
        }

        @Override
        public void gameClosed() {
            this.launcher.showLauncherWindow();
        }
    }

}
