package net.teamfruit.skcraft.launcher.skins;

import java.awt.Image;
import java.util.ResourceBundle;

public interface Skin {
	String getNewsURL();

	String getTipsURL();

	String getSupportURL();

	ResourceBundle getLang();

	Image getBackgroundImage();

	boolean isShowList();

	String getSelectModPack();

	String getLoginModPack();

	void downloadResources() throws Exception;
}
