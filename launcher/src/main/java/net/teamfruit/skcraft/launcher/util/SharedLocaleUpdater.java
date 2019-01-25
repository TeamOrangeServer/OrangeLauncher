package net.teamfruit.skcraft.launcher.util;

import java.awt.Frame;
import java.util.Map;
import java.util.WeakHashMap;

import javax.annotation.Nonnull;
import javax.swing.AbstractButton;
import javax.swing.JComponent;
import javax.swing.JLabel;

import com.skcraft.launcher.util.SharedLocale;

public class SharedLocaleUpdater {
	private final Map<Object, Update> updates = new WeakHashMap<Object, Update>();

	public static SharedLocaleUpdater create() {
		return new SharedLocaleUpdater();
	}

	private SharedLocaleUpdater() {
	}

	public <T> T tr(final T component, @Nonnull final Update upd) {
		if (component!=null) {
			upd.update();
			updates.put(component, upd);
		}
		return component;
	}

	public <T extends AbstractButton> T tr(final T component, final String key, final Object... args) {
		return tr(component, new AbstractUpdate(key, args) {
			@Override
			public void update() {
				component.setText(tr());
			}
		});
	}

	public <T extends JLabel> T tr(final T component, final String key, final Object... args) {
		return tr(component, new AbstractUpdate(key, args) {
			@Override
			public void update() {
				component.setText(tr());
			}
		});
	}

	public <T extends Frame> T tr(final T component, final String key, final Object... args) {
		return tr(component, new AbstractUpdate(key, args) {
			@Override
			public void update() {
				component.setTitle(tr());
			}
		});
	}

	public <T extends JComponent> T trTooltip(final T component, final String key, final Object... args) {
		return tr(component, new AbstractUpdate(key, args) {
			@Override
			public void update() {
				component.setToolTipText(tr());
			}
		});
	}

	public void update() {
		for (Update update : updates.values())
			update.update();
	}

	public static interface Update {
		void update();
	}

	public static abstract class AbstractUpdate implements Update {
		protected final String key;
		protected final Object[] args;

		public AbstractUpdate(String key, Object... args) {
			this.key = key;
			this.args = args;
		}

		protected String tr() {
			if (args!=null&&args.length>0)
				return SharedLocale.tr(key, args);
			return SharedLocale.tr(key);
		}
	}
}