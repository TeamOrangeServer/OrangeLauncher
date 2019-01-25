package net.teamfruit.skcraft.launcher.appicon;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;

import org.apache.commons.lang.StringUtils;

import com.google.common.collect.Lists;
import com.google.common.io.InputSupplier;
import com.skcraft.launcher.swing.SwingHelper;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class AppIcon {
	@Getter private final Class<?> clazz;
	@Getter private final String name;
	@Getter private final String path;

	@Getter(lazy=true) private final BufferedImage image = SwingHelper.readBufferedImage(clazz, path);

	@Getter private InputSupplier<InputStream> input = new InputSupplier<InputStream>() {
		@Override
		public InputStream getInput() throws IOException {
			return clazz.getResourceAsStream(path);
		}
	};

	public static void setFrameIconSet(JFrame frame, List<BufferedImage> iconSet) {
		frame.setIconImages(iconSet);
	}

	public static List<BufferedImage> getSwingTaskIcon(List<BufferedImage> iconSet, Color color) {
		final List<BufferedImage> icons = new ArrayList<BufferedImage>();
		for (BufferedImage base: iconSet) {
			int image_width = base.getWidth();
			int image_height = base.getHeight();
			int icon_size = Math.min(image_width, image_height)/2;
			BufferedImage image = new BufferedImage(image_width, image_height, BufferedImage.TYPE_INT_ARGB);
			Graphics2D g = image.createGraphics();
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		    g.drawImage(base, 0, 0, null);
		    g.setColor(color);
		    g.fillOval(image_width-icon_size, image_height-icon_size, icon_size, icon_size);
		    g.dispose();
		    icons.add(image);
		}
		return icons;
	}

	public static List<AppIcon> getIconSet(Class<?> clazz, String... paths) {
		final List<AppIcon> icons = Lists.newArrayList();
		for (String path : paths)
			icons.add(new AppIcon(clazz, StringUtils.substringBeforeLast(path, "/"), path));
		return icons;
	}

	public static List<BufferedImage> getSwingIconSet(List<AppIcon> appicons) {
		final List<BufferedImage> icons = Lists.newArrayList();
		for (AppIcon appicon : appicons) {
			BufferedImage icon = appicon.getImage();
			if (icon!=null)
				icons.add(icon);
		}
		return icons;
	}

	private static final @Getter(lazy = true) List<AppIcon> appIconSet = createAppIconSet();

	private static List<AppIcon> createAppIconSet() {
		final Class<?> clazz = AppIcon.class;
		final String path_format = "icon_%s.png";
		final String[] icon_names = {
				"16x16",
				"32x32",
				"48x48",
				"64x64",
				"128x128",
				"256x256",
		};
		final List<AppIcon> icons = Lists.newArrayList();
		for (String name : icon_names)
			icons.add(new AppIcon(clazz, name, String.format(path_format, name)));
		return icons;
	}

	@Getter private static final InputSupplier<InputStream> windowsAppIcon = new InputSupplier<InputStream>() {
		@Override
		public InputStream getInput() throws IOException {
			return AppIcon.class.getResourceAsStream("icon.ico");
		}
	};
}
