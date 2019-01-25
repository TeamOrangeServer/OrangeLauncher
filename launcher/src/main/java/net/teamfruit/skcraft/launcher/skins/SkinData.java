package net.teamfruit.skcraft.launcher.skins;

import java.awt.Image;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.logging.Level;

import javax.imageio.ImageIO;

import org.apache.commons.lang.StringUtils;

import com.beust.jcommander.internal.Nullable;
import com.google.common.io.Closer;
import com.skcraft.launcher.Launcher;
import com.skcraft.launcher.LauncherUtils;
import com.skcraft.launcher.util.HttpRequest;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import net.teamfruit.skcraft.launcher.model.skins.SkinInfo;

@Log
@RequiredArgsConstructor
@EqualsAndHashCode(doNotUseGetters = true, of = { "skinInfo", "resourceDir" })
public class SkinData implements Skin {
	private final Launcher launcher;
	private final Skin defaultSkin;
	@Nullable
	private final SkinInfo skinInfo;
	private final File resourceDir;

	@Override
	public String getNewsURL() {
		String data = null;
		if (skinInfo!=null)
			data = skinInfo.getNewsURL();
		if (data==null)
			data = defaultSkin.getNewsURL();
		return data;
	}

	@Override
	public String getTipsURL() {
		String data = null;
		if (skinInfo!=null)
			data = skinInfo.getTipsURL();
		if (data==null)
			data = defaultSkin.getTipsURL();
		return data;
	}

	@Override
	public String getSupportURL() {
		String data = null;
		if (skinInfo!=null)
			data = skinInfo.getSupportURL();
		if (data==null)
			data = defaultSkin.getSupportURL();
		return data;
	}

	private ResourceBundle lang;
	@Getter(lazy = true, value = AccessLevel.PRIVATE)
	private final ResourceBundle backingLang = loadLang();

	@Override
	public ResourceBundle getLang() {
		if (skinInfo!=null)
			if (lang==null)
				lang = getBackingLang();
		if (lang==null)
			return defaultSkin.getLang();
		return lang;
	}

	private ResourceBundle loadLang() {
		File langFile = getLangFile();
		if (langFile.isFile())
			try {
				return new PropertyResourceBundle(new InputStreamReader(new FileInputStream(langFile), "UTF-8"));
			} catch (Exception e) {
				log.log(Level.WARNING, "Could not load skin lang file: ", e);
			}
		return null;
	}

	private File getLangFile() {
		return new File(resourceDir, "lang");
	}

	private Image backgroundImage;
	@Getter(lazy = true, value = AccessLevel.PRIVATE)
	private final Image backingBackgroundImage = loadBackgroundImage();

	@Override
	public Image getBackgroundImage() {
		if (skinInfo!=null)
			if (backgroundImage==null)
				backgroundImage = getBackingBackgroundImage();
		if (backgroundImage==null)
			backgroundImage = defaultSkin.getBackgroundImage();
		return backgroundImage;
	}

	private Image loadBackgroundImage() {
		File backgroundImageFile = getBackgroundImageFile();
		if (backgroundImageFile.isFile())
			try {
				return ImageIO.read(backgroundImageFile);
			} catch (Exception e) {
				log.log(Level.WARNING, "Could not load skin background image file: ", e);
			}
		return null;
	}

	private File getBackgroundImageFile() {
		return new File(resourceDir, "background");
	}

	@Override
	public boolean isShowList() {
		boolean data = defaultSkin.isShowList();
		if (skinInfo!=null)
			data = skinInfo.isShowList();
		return data;
	}

	@Override
	public String getSelectModPack() {
		String data = null;
		if (skinInfo!=null)
			data = skinInfo.getSelectModPack();
		if (data==null)
			data = defaultSkin.getSelectModPack();
		return data;
	}

	@Override
	public String getLoginModPack() {
		String data = null;
		if (skinInfo!=null)
			data = skinInfo.getLoginModPack();
		if (data==null)
			data = defaultSkin.getLoginModPack();
		return data;
	}

	@Override
	public void downloadResources() throws Exception {
		if (skinInfo==null) {
			defaultSkin.downloadResources();
			return;
		}

		resourceDir.mkdirs();

		try {
			String langURL = skinInfo.getLangURL();
			if (!StringUtils.isEmpty(langURL)) {
				byte[] bytes = HttpRequest
						.get(HttpRequest.url(langURL))
						.execute()
						.expectResponseCode(200)
						.returnContent()
						.saveContent(getLangFile())
						.asBytes();
				Closer closer = Closer.create();
				try {
					lang = new PropertyResourceBundle(closer.register(new InputStreamReader(new ByteArrayInputStream(bytes), "UTF-8")));
				} finally {
					LauncherUtils.closeQuietly(closer);
				}
			}
		} catch (IOException e) {
			log.log(Level.WARNING, "The skin resource (lang file) could not be downloaded.", e);
		}

		try {
			String backgroundImageURL = skinInfo.getBackgroundURL();
			if (!StringUtils.isEmpty(backgroundImageURL)) {
				byte[] bytes = HttpRequest
						.get(HttpRequest.url(backgroundImageURL))
						.execute()
						.expectResponseCode(200)
						.returnContent()
						.saveContent(getBackgroundImageFile())
						.asBytes();
				Closer closer = Closer.create();
				try {
					backgroundImage = ImageIO.read(new ByteArrayInputStream(bytes));
				} finally {
					LauncherUtils.closeQuietly(closer);
				}
			}
		} catch (IOException e) {
			log.log(Level.WARNING, "The skin resource (background image file) could not be downloaded.", e);
		}
	}

}
