/*
 * SK's Minecraft Launcher
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com> and contributors
 * Please see LICENSE.txt for license information.
 */

package com.skcraft.launcher.dialog;

import static com.skcraft.launcher.util.SharedLocale.*;

import java.awt.AWTException;
import java.awt.Color;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.PrintWriter;
import java.util.List;

import javax.swing.JButton;
import javax.swing.SwingUtilities;

import com.skcraft.launcher.swing.LinedBoxPanel;
import com.skcraft.launcher.swing.MessageLog;
import com.skcraft.launcher.swing.SwingHelper;
import com.skcraft.launcher.util.SharedLocale;

import lombok.Getter;
import net.teamfruit.skcraft.launcher.appicon.AppIcon;
import net.teamfruit.skcraft.launcher.swing.ChatMessagePanel;

/**
 * A version of the console window that can manage a process.
 */
public class ProcessConsoleFrame extends ConsoleFrame {

    private JButton killButton;
    private JButton minimizeButton;
    private TrayIcon trayIcon;

    @Getter private Process process;
    //@Getter @Setter private boolean killOnClose;

    private PrintWriter processOut;

    /**
     * Create a new instance of the frame.
     *
     * @param numLines the number of log lines
     * @param colorEnabled whether color is enabled in the log
     */
    public ProcessConsoleFrame(int numLines, boolean colorEnabled) {
        super(SharedLocale.tr("console.title"), numLines, colorEnabled);
        processOut = new PrintWriter(
                getMessageLog().getOutputStream(new Color(0, 0, 255)), true);
        initComponents();
        updateComponents();
    }

    /**
     * Track the given process.
     *
     * @param process the process
     */
    public synchronized void setProcess(Process process) {
        try {
            Process lastProcess = this.process;
            if (lastProcess != null) {
                processOut.println(tr("console.processEndCode", lastProcess.exitValue()));
            }
        } catch (IllegalThreadStateException e) {
        }

        if (process != null) {
            processOut.println(SharedLocale.tr("console.attachedToProcess"));
        }

        this.process = process;

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                updateComponents();
            }
        });
    }

    private synchronized boolean hasProcess() {
        return process != null;
    }

    @Override
    protected void performClose() {
        if (hasProcess()) {
            if (!performKill()) {
                return;
            }
        }

        if (trayIcon != null) {
            SystemTray.getSystemTray().remove(trayIcon);
        }

        super.performClose();
    }

    private boolean performKill() {
        if (!confirmKill()) {
            return false;
        }

        synchronized (this) {
            if (hasProcess()) {
                process.destroy();
                setProcess(null);
            }
        }

        updateComponents();

        return true;
    }

    protected void initComponents() {
    	MessageLog message = getMessageLog();

    	message.setTitleAt(0, SharedLocale.tr("console.gameTab"));

    	message.addTab(SharedLocale.tr("console.chatTab"), new ChatMessagePanel(message));

        killButton = new JButton(SharedLocale.tr("console.forceClose"));
        minimizeButton = new JButton(); // Text set later

        LinedBoxPanel buttonsPanel = getButtonsPanel();
        buttonsPanel.addGlue();
        buttonsPanel.addElement(killButton);
        buttonsPanel.addElement(minimizeButton);

        killButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                performKill();
            }
        });

        minimizeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                contextualClose();
            }
        });

        if (!setupTrayIcon()) {
            minimizeButton.setEnabled(true);
        }
    }

    private boolean setupTrayIcon() {
        if (!SystemTray.isSupported()) {
            return false;
        }

        List<BufferedImage> icon = getTrayRunningIcon();
        if (icon.isEmpty())
        	return false;

        trayIcon = new TrayIcon(icon.get(0));
        trayIcon.setImageAutoSize(true);
        trayIcon.setToolTip(SharedLocale.tr("console.trayTooltip"));

        trayIcon.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                reshow();
            }
        });

        PopupMenu popup = new PopupMenu();
        MenuItem item;

        popup.add(item = new MenuItem(SharedLocale.tr("console.trayTitle")));
        item.setEnabled(false);

        popup.add(item = new MenuItem(SharedLocale.tr("console.tray.showWindow")));
        item.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                reshow();
            }
        });

        popup.add(item = new MenuItem(SharedLocale.tr("console.tray.forceClose")));
        item.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                performKill();
            }
        });

        trayIcon.setPopupMenu(popup);

        try {
            SystemTray tray = SystemTray.getSystemTray();
            tray.add(trayIcon);
            return true;
        } catch (AWTException e) {
        }

        return false;
    }

    private synchronized void updateComponents() {
        List<BufferedImage> iconSet = hasProcess() ? getTrayRunningIcon() : getTrayClosedIcon();

        killButton.setEnabled(hasProcess());

        if (!hasProcess() || trayIcon == null) {
            minimizeButton.setText(SharedLocale.tr("console.closeWindow"));
        } else {
            minimizeButton.setText(SharedLocale.tr("console.hideWindow"));
        }

        if (trayIcon != null) {
        	if (iconSet.isEmpty())
        		trayIcon.setImage(iconSet.get(0));
        }

        AppIcon.setFrameIconSet(this, iconSet);
    }

    private synchronized void contextualClose() {
        if (!hasProcess() || trayIcon == null) {
            performClose();
        } else {
            minimize();
        }

        updateComponents();
    }

    private boolean confirmKill() {
        if (System.getProperty("skcraftLauncher.killWithoutConfirm", "false").equalsIgnoreCase("true")) {
            return true;
        } else {
            return SwingHelper.confirmDialog(this,  SharedLocale.tr("console.confirmKill"), SharedLocale.tr("console.confirmKillTitle"));
        }
    }

    private void minimize() {
        setVisible(false);
    }

    private void reshow() {
        setVisible(true);
        requestFocus();
    }

}
