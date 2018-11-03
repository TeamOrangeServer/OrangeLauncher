package net.teamfruit.skcraft.launcher.skins;

import java.awt.Image;
import java.io.IOException;
import java.io.InputStream;
import java.util.ResourceBundle;

import javax.imageio.ImageIO;

import com.skcraft.launcher.FancyBackgroundPanel;
import com.skcraft.launcher.Launcher;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DefaultSkin implements Skin {
	private final Launcher launcher;

	@Override
	public String getNewsURL() {
		return launcher.getProperties().getProperty("newsUrl");
	}

	@Override
	public String getTipsURL() {
		return launcher.getProperties().getProperty("tipsUrl");
	}

	@Override
	public String getSupportURL() {
		return launcher.getProperties().getProperty("supportUrl");
	}

	@Override
	public ResourceBundle getLang() {
		return null;
	}

	@Getter(lazy = true)
	private final Image backgroundImage = createBackgroundImage();

	public Image createBackgroundImage() {
		try {
			InputStream input = FancyBackgroundPanel.class.getResourceAsStream("launcher_bg.jpg");
			if (input!=null)
				return ImageIO.read(input);
		} catch (IOException e) {
		}
		return null;
	}

	@Override
	public boolean isShowList() {
		return true;
	}

	@Override
	public String getSelectModPack() {
		return null;
	}

	@Override
	public String getLoginModPack() {
		return null;
	}

	@Override
	public void downloadResources() throws Exception {
	}

}
