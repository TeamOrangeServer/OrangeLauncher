package net.teamfruit.skcraft.launcher.swing;

import java.awt.Color;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.text.AttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import com.google.common.collect.Maps;
import com.skcraft.launcher.swing.MessageLog;

import net.teamfruit.skcraft.launcher.discordrpc.DiscordStatus;
import net.teamfruit.skcraft.launcher.discordrpc.LauncherStatus;

public class ChatMessagePanel extends MessageLog.MessagePanel {
	public ChatMessagePanel(MessageLog messageLog) {
		messageLog.super();
		textComponent.setBackground(new Color(0.4f, 0.4f, 0.4f));
	}

	public static final Pattern chatPattern = Pattern.compile("\\[(.+?)\\] \\[.+?\\]: \\[CHAT\\] (.*)");
	public static final Pattern colorPattern = Pattern.compile("\u00A7(.)");

	private static final Map<Character, Color> colors = Maps.newHashMap();
	private static SimpleAttributeSet blankAttr = new SimpleAttributeSet();

	static {
		colors.put('0', new Color(0x000000));
		colors.put('1', new Color(0x0000AA));
		colors.put('2', new Color(0x00AA00));
		colors.put('3', new Color(0x00AAAA));
		colors.put('4', new Color(0xAA0000));
		colors.put('5', new Color(0xAA00AA));
		colors.put('6', new Color(0xFFAA00));
		colors.put('7', new Color(0xAAAAAA));
		colors.put('8', new Color(0x555555));
		colors.put('9', new Color(0x5555FF));
		colors.put('a', new Color(0x55FF55));
		colors.put('b', new Color(0x55FFFF));
		colors.put('c', new Color(0xFF5555));
		colors.put('d', new Color(0xFF55FF));
		colors.put('e', new Color(0xFFFF55));
		colors.put('f', new Color(0xFFFFFF));

		StyleConstants.setForeground(blankAttr, new Color(0xAAAAAA));
	}

	private static final Color defaultColor = new Color(0xFFFFFF);

	private boolean forge;

	@Override
	public void log(String line, AttributeSet attributes) {
		if (line.contains("[CHAT] ")) {
			Matcher m = chatPattern.matcher(line);
			AttributeLog out = new AttributeLog() {
				@Override
				public void log(String line, AttributeSet attributes) {
					ChatMessagePanel.super.log(line, attributes);
				}
			};
			while (m.find()) {
				String timeText = m.group(1);

				out.log("["+timeText+"] ", blankAttr);

				String chatText = m.group(2);

				log(chatText, out);

				out.log("\n", attributes);
			}
		} else if ((forge&&line.contains("SoundSystem shutting down..."))||(!forge&&line.contains("Starting up SoundSystem...")))
			LauncherStatus.instance.update(DiscordStatus.LAUNCHING, DiscordStatus.PLAYING, null);
		else if (line.contains("[MinecraftForge]: Completed early MinecraftForge initialization"))
			forge = true;
	}

	public static void log(String chatText, AttributeLog out) {
		Matcher cm = colorPattern.matcher(chatText);
		StringBuffer csb = new StringBuffer();

		SimpleAttributeSet nowAttr = new SimpleAttributeSet();
		SimpleAttributeSet lastAttr = nowAttr;
		StyleConstants.setForeground(nowAttr, defaultColor);

		while (cm.find()) {
			char key = cm.group(1).charAt(0);
			Color color = colors.get(key);
			if (color!=null) {
				nowAttr = new SimpleAttributeSet();
				StyleConstants.setForeground(nowAttr, color);
			} else {
				if (key=='r') {
					nowAttr = new SimpleAttributeSet();
					StyleConstants.setForeground(nowAttr, defaultColor);
				} else {
					nowAttr = new SimpleAttributeSet(nowAttr);
					if (key=='k')
						StyleConstants.setBackground(nowAttr, StyleConstants.getForeground(nowAttr));
					else if (key=='l')
						StyleConstants.setBold(nowAttr, true);
					else if (key=='m')
						StyleConstants.setStrikeThrough(nowAttr, true);
					else if (key=='n')
						StyleConstants.setUnderline(nowAttr, true);
					else if (key=='o')
						StyleConstants.setItalic(nowAttr, true);
				}
			}

			csb.setLength(0);
			cm.appendReplacement(csb, "");
			out.log(csb.toString(), lastAttr);
			lastAttr = new SimpleAttributeSet(nowAttr);
		}

		csb.setLength(0);
		cm.appendTail(csb);
		out.log(csb.toString(), nowAttr);
	}

	public static class HTMLLog implements AttributeLog {
		private final StringBuilder stb = new StringBuilder();

		@Override
		public void log(String line, AttributeSet attributes) {
			stb.append("<span style=\"");
			Color foreground = (Color) attributes.getAttribute(StyleConstants.Foreground);
			if (foreground!=null)
				stb.append("color:#").append(Integer.toHexString(foreground.getRGB())).append(";");
			Color background = (Color) attributes.getAttribute(StyleConstants.Background);
			if (background!=null)
				stb.append("color:#").append(Integer.toHexString(background.getRGB())).append(";");
			if (StyleConstants.isBold(attributes))
				stb.append("font-weight:bold;");
			boolean strike = StyleConstants.isStrikeThrough(attributes);
			boolean under = StyleConstants.isUnderline(attributes);
			if (strike||under) {
				stb.append("text-decoration:");
				if (strike)
					stb.append("line-through");
				if (strike&&under)
					stb.append(" ");
				if (under)
					stb.append("underline");
				stb.append(";");
			}
			if (StyleConstants.isItalic(attributes))
				stb.append("font-style:italic;");
			stb.append("\">").append(line).append("</span>");
		}

		public String toString() {
			return stb.toString();
		}
	}
}
