package net.teamfruit.skcraft.launcher.dialog;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileFilter;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.AbstractListModel;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import org.apache.commons.lang.StringUtils;

import com.beust.jcommander.internal.Lists;
import com.google.common.base.Predicate;
import com.skcraft.launcher.Launcher;
import com.skcraft.launcher.swing.ActionListeners;
import com.skcraft.launcher.swing.LinedBoxPanel;
import com.skcraft.launcher.swing.SwingHelper;
import com.skcraft.launcher.util.SharedLocale;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import net.teamfruit.skcraft.launcher.skins.LocalSkin;
import net.teamfruit.skcraft.launcher.skins.RemoteSkin;
import net.teamfruit.skcraft.launcher.skins.SkinUtils;

public class SkinSelectionDialog extends JDialog {
	private final Launcher launcher;

	private final JTextField targetText;

	private final JList<SkinItem> skins = new JList<SkinItem>();
	private final JScrollPane scrollpanel = new JScrollPane(skins);

	private final LinedBoxPanel buttonsPanel = new LinedBoxPanel(true);
	private final JButton okButton = new JButton(SharedLocale.tr("button.ok"));
	private final JButton cancelButton = new JButton(SharedLocale.tr("button.cancel"));

	public SkinSelectionDialog(Window owner, @NonNull Launcher launcher, JTextField pathDirText) {
		super(owner, ModalityType.DOCUMENT_MODAL);

		this.launcher = launcher;
		this.targetText = pathDirText;

		setTitle(SharedLocale.tr("options.skinSelectTitle"));
		initComponents();
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setSize(new Dimension(250, 350));
		setLocationRelativeTo(owner);
	}

	private void initComponents() {
		Map<String, RemoteSkin> prop = launcher.getRemoteSkins().getSkinMap();
		if (prop!=null) {
			final List<SkinItem> skinNames = Lists.newArrayList(autoSkinItem, defaultSkinItem);
			for (Entry<String, RemoteSkin> entry : prop.entrySet())
				skinNames.add(new RemoteSkinItem(entry.getKey(), entry.getValue()));
			File[] files = launcher.getSkinDir().listFiles(new FileFilter() {
				@Override
				public boolean accept(File pathname) {
					return pathname.isDirectory()&&new File(pathname, "skin.json").isFile();
				}
			});
			if (files!=null)
				for (File local : files) {
					String localname = local.getName();
					if (!prop.containsKey(localname))
						skinNames.add(new LocalSkinItem(localname, new LocalSkin(launcher, localname)));
				}

			skins.setModel(new AbstractListModel<SkinItem>() {
				public int getSize() {
					return skinNames.size();
				}

				public SkinItem getElementAt(int i) {
					return skinNames.get(i);
				}
			});
			String skinName = targetText.getText();
			skins.setSelectedValue(skinOf(skinName), true);
		}

		SwingHelper.removeOpaqueness(scrollpanel);
		scrollpanel.setBorder(BorderFactory.createEmptyBorder());
		add(scrollpanel, BorderLayout.CENTER);

		buttonsPanel.addGlue();
		buttonsPanel.addElement(okButton);
		buttonsPanel.addElement(cancelButton);

		add(buttonsPanel, BorderLayout.SOUTH);

		cancelButton.addActionListener(ActionListeners.dispose(this));

		okButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				save();
			}
		});
	}

	/**
	 * Save the configuration and close the dialog.
	 */
	public void save() {
		SkinItem skinName = skins.getSelectedValue();
		if (skinName==null)
			skinName = defaultSkinItem;
		skinName.apply();
	}

	private static interface SkinItem {
		String toString();

		void apply();
	}

	@Data
	private static abstract class AbstractSkinItem implements SkinItem {
		private final String name;

		@Override
		public String toString() {
			return name;
		}
	}

	private final SkinItem autoSkinItem = new DefaultSkinItem("", SharedLocale.tr("options.skinSelectAuto"));
	private final SkinItem defaultSkinItem = new DefaultSkinItem("-", SharedLocale.tr("options.skinSelectDefault"));

	private SkinItem skinOf(String name) {
		if (StringUtils.isEmpty(name))
			return autoSkinItem;
		if (StringUtils.equals(name, "-"))
			return defaultSkinItem;
		RemoteSkin remoteSkin = launcher.getRemoteSkins().getRemoteSkin(name);
		return new RemoteSkinItem(name, remoteSkin);
	}

	@Data
	@EqualsAndHashCode(callSuper = true, of = {})
	private class DefaultSkinItem extends AbstractSkinItem {
		private final String localization;

		private DefaultSkinItem(String name, String localization) {
			super(name);
			this.localization = localization;
		}

		@Override
		public String toString() {
			return localization;
		}

		@Override
		public void apply() {
			targetText.setText(getName());
			dispose();
		}
	}

	@Data
	@EqualsAndHashCode(callSuper = true, of = {})
	private class RemoteSkinItem extends AbstractSkinItem {
		private final RemoteSkin skin;

		public RemoteSkinItem(String name, RemoteSkin skin) {
			super(name);
			this.skin = skin;
		}

		@Override
		public String toString() {
			RemoteSkin remoteSkin = getSkin();
			if (remoteSkin!=null)
				return remoteSkin.getTitle();
			return getName();
		}

		public void apply() {
			final String name = getName();
			RemoteSkin remoteSkin = getSkin();
			if (remoteSkin!=null)
				SkinUtils.loadSkin(SkinSelectionDialog.this, launcher, true, remoteSkin, new Predicate<RemoteSkin>() {
					@Override
					public boolean apply(RemoteSkin input) {
						targetText.setText(name);
						dispose();
						return true;
					}
				});
		}
	}


	@Data
	@EqualsAndHashCode(callSuper = true, of = {})
	private class LocalSkinItem extends AbstractSkinItem {
		private final LocalSkin skin;

		public LocalSkinItem(String name, LocalSkin skin) {
			super(name);
			this.skin = skin;
		}

		@Override
		public String toString() {
			return getName();
		}

		public void apply() {
			targetText.setText(getName());
			dispose();
		}
	}
}
