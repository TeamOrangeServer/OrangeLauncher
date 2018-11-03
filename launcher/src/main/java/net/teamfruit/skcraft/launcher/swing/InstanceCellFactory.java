package net.teamfruit.skcraft.launcher.swing;

import java.awt.Component;
import java.awt.Image;
import java.util.Map;
import java.util.WeakHashMap;

import javax.annotation.Nullable;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JTable;
import javax.swing.ListCellRenderer;
import javax.swing.table.TableCellRenderer;

import com.google.common.base.Supplier;
import com.google.common.collect.Table;
import com.google.common.collect.Tables;
import com.skcraft.launcher.Instance;

public class InstanceCellFactory implements TableCellRenderer, ListCellRenderer<Instance> {
	private InstanceCellFactory() {
	}

	public static final InstanceCellFactory instance = new InstanceCellFactory();

	@Override
	public Component getTableCellRendererComponent(final JTable table, final Object value, final boolean isSelected, final boolean hasFocus, final int row, final int column) {
		return getCellComponent(table, value, isSelected, ServerInfoStyle.SIMPLE);
	}

	@Override
	public Component getListCellRendererComponent(final JList<? extends Instance> list, final Instance value, final int index, final boolean isSelected, final boolean cellHasFocus) {
		return getCellComponent(list, value, isSelected, ServerInfoStyle.SIMPLE);
	}

	private final Table<Instance, JComponent, InstanceCellPanel> table = Tables.newCustomTable(new WeakHashMap<Instance, Map<JComponent, InstanceCellPanel>>(), new Supplier<Map<JComponent, InstanceCellPanel>>() {
		@Override
		public Map<JComponent, InstanceCellPanel> get() {
			return new WeakHashMap<JComponent, InstanceCellPanel>();
		}
	});
	private final Map<Instance, Image> iconCaches = new WeakHashMap<Instance, Image>();
	private final InstanceCellPanel defaultPanel = new InstanceCellPanel(null);
	private final ServerInfoFactory serverInfoFactory = new ServerInfoFactory();

	public InstanceCellPanel getCellComponent(final JComponent component, @Nullable final Object value, final boolean isSelected, ServerInfoStyle style) {
		if (value instanceof Instance) {
			final Instance instance = (Instance) value;

			InstanceCellPanel tablecell = table.get(instance, component);
			if (tablecell==null) {
				tablecell = new InstanceCellPanel(component);

				tablecell.setTitle(instance.getTitle());
				tablecell.setInstance(instance, iconCaches);
				tablecell.setServerInfoPanel(serverInfoFactory.getServerInfo(style, instance.getServer()));

				table.put(instance, component, tablecell);
			}

			tablecell.setShowPlayIcon(isSelected);
			tablecell.setShowSelected(isSelected);

			return tablecell;
		}

		return defaultPanel;
	}
}
