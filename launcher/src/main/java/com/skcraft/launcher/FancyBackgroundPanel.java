/*
 * SKCraft Launcher
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com> and contributors
 * Please see LICENSE.txt for license information.
 */

package com.skcraft.launcher;

import java.awt.Graphics;
import java.awt.Image;

import javax.swing.JPanel;

import lombok.RequiredArgsConstructor;
import net.teamfruit.skcraft.launcher.util.ImageSizes;
import net.teamfruit.skcraft.launcher.util.SizeData;

@RequiredArgsConstructor
public class FancyBackgroundPanel extends JPanel {

	private final Launcher launcher;

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		Image background = launcher.getSkin().getBackgroundImage();
		if (background!=null) {
			final int panel_width = getWidth();
			final int panel_height = getHeight();

			final int img_width = background.getWidth(this);
			final int img_height = background.getHeight(this);
			final SizeData img_size = ImageSizes.OUTER.size(img_width, img_height, panel_width, panel_height);
			g.drawImage(background, (int)((panel_width-img_size.getWidth())/2), (int)((panel_height-img_size.getHeight())/2), (int)img_size.getWidth(), (int)img_size.getHeight(), this);
		}
	}

}
