package net.teamfruit.skcraft.launcher.discordrpc;

import java.awt.Component;
import java.lang.ref.WeakReference;
import java.util.Map;

import javax.annotation.Nullable;

import com.google.common.collect.Maps;

import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

public class LauncherStatus {
	public static final LauncherStatus instance = new LauncherStatus();

	private final Map<DiscordStatus, StatusNode> store = Maps.newHashMap();
	private StatusNode last;

	public void open(@NonNull DiscordStatus status, @NonNull Disablable window, @NonNull Map<String, String> map) {
		StatusNode parent = getAvailableNode(last);
		DiscordRichPresence presence;
		if (parent!=null)
			presence = parent.getPresence();
		else
			presence = new DiscordRichPresence();
		StatusNode node = new StatusNode(parent, window, status, presence, map);
		last = node;
		store.put(status, node);
		status.update(presence, map);
	}

	public void close(@NonNull DiscordStatus status) {
		StatusNode node = store.get(status);
		if (node!=null) {
			store.remove(status);
			node.setClosed(true);
			StatusNode parent = getAvailableNode(node);
			if (parent!=null)
				parent.getStatus().update(node.getPresence(), parent.getMap());
		}
	}

	public void update(@NonNull DiscordStatus status, @Nullable DiscordStatus newStatus, @Nullable Map<String, String> map) {
		StatusNode node = store.get(status);
		if (node!=null) {
			if (newStatus!=null) {
				store.remove(status);
				node.setStatus(newStatus);
				store.put(newStatus, node);
			}
			if (map!=null)
				node.setMap(map);
			node.getStatus().update(node.getPresence(), node.getMap());
		}
	}

	public void update() {
		StatusNode node = last;
		if (node!=null)
			node.getStatus().update(node.getPresence(), node.getMap());
	}

	private StatusNode getAvailableNode(StatusNode node) {
		if (node!=null)
			do {
				if (!node.isClosed()) {
					if (node.getWindow().isDisabled())
						node.setClosed(true);
					else
						break;
				}
			} while ((node = node.getParent())!=null);
		return node;
	}

	public static interface Disablable {
		boolean isDisabled();
	}

	public static class WindowDisablable implements Disablable {
		private final WeakReference<Component> window;

		public WindowDisablable(Component window) {
			this.window = new WeakReference<Component>(window);
		}

		@Override
		public boolean isDisabled() {
			Component window = this.window.get();
			if (window!=null)
				if (window.isVisible())
					return false;
			return true;
		}
	}

	public static class NullDisablable implements Disablable {
		@Override
		public boolean isDisabled() {
			return false;
		}
	}

	@Data
	@RequiredArgsConstructor
	public static class StatusNode {
		private final StatusNode parent;
		private final Disablable window;
		@NonNull private DiscordStatus status;
		@NonNull private DiscordRichPresence presence;
		@NonNull private Map<String, String> map;
		private boolean closed;
	}
}
