package com.skcraft.launcher.swing;

import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.swing.UIManager;

import org.apache.commons.lang.ArrayUtils;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.java.Log;

@Log
public class DefaultFont {
	@Getter
	private final GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
	@Getter
	private final String[] fontFamilies = ge.getAvailableFontFamilyNames(Locale.JAPANESE);

	@Getter
	@Setter
	private Font bestFont;
	@Getter
	@Setter
	private String[] betterFontFamilies = {
			"Meiryo UI",
			"メイリオ",
			"Yu Gothic UI",
			"游ゴシック",
			"游ゴシック体",
			"VL Gothic",
			"ヒラギノ丸ゴ ProN",
			"Osaka",
			"Klee",
			"M+ 1p",
			"Takaoゴシック",
			"Noto Sans CJK JP Regular",
			"Noto Sans Mono CJK JP Regular",
			"MS UI Gothic",
			"ＭＳ ゴシック",
	};
	@Getter
	@Setter
	private List<Font> fontCandidates = Lists.newArrayList();

	public void searchBestFont() {
		if (fontCandidates.isEmpty()) {
			Set<String> fonts = Sets.newHashSet(fontFamilies);
			for (String fontfamily : betterFontFamilies) {
				if (fonts.contains(fontfamily)) {
					Font font = new Font(fontfamily, Font.PLAIN, 12);
					if (font.canDisplay('\u4e80'))
						fontCandidates.add(font);
				}
			}
		}
	}

	public void decideBestFont() {
		searchBestFont();
		if (!fontCandidates.isEmpty())
			setBestFont(fontCandidates.get(0));
	}

	public void applyDefaultFont() {
		if (bestFont!=null)
			setUIFont(new javax.swing.plaf.FontUIResource(bestFont));
	}

	public static void setUIFont(javax.swing.plaf.FontUIResource f) {
		java.util.Enumeration<Object> keys = UIManager.getDefaults().keys();
		while (keys.hasMoreElements()) {
			Object key = keys.nextElement();
			Object value = UIManager.get(key);
			if (value instanceof javax.swing.plaf.FontUIResource)
				UIManager.put(key, f);
		}
	}

	public static void configUIFont() {
		log.info("Setting UI font");
		DefaultFont defaultFont = new DefaultFont();
		log.fine("Better fonts are "+ArrayUtils.toString(defaultFont.getBetterFontFamilies()));
		log.fine("Available fonts are "+ArrayUtils.toString(defaultFont.getFontFamilies()));
		defaultFont.decideBestFont();
		List<String> candidateFamilies = Lists.newArrayList();
		for (Font candidate : defaultFont.getFontCandidates())
			candidateFamilies.add(candidate.getFamily());
		log.fine("Available better fonts are "+candidateFamilies);
		Font bestFont = defaultFont.getBestFont();
		log.info("UI font is "+(bestFont==null ? "not set" : ("set to "+bestFont.getFontName())));
		defaultFont.applyDefaultFont();
	}
}
