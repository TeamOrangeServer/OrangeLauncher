package net.teamfruit.skcraft.launcher.swing;

import javax.swing.text.AttributeSet;

public interface AttributeLog {
	/**
	 * Log a message given the {@link javax.swing.text.AttributeSet}.
	 *
	 * @param line line
	 * @param attributes attribute set, or null for none
	 */
	public void log(final String line, AttributeSet attributes);
}
