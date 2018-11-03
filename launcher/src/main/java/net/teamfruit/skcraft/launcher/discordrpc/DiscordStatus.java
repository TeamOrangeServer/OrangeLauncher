package net.teamfruit.skcraft.launcher.discordrpc;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;

import com.skcraft.launcher.util.SharedLocale;

public enum DiscordStatus {
	STARTING {
		@Override
		public DiscordRichPresence createRPC(final DiscordRichPresence lastPresence, final Map<String, String> args) {
			DiscordRichPresence presence = new DiscordRichPresence();
			presence.largeImageKey = "fruit_general";
			presence.state = SharedLocale.tr("discordrpc.status.starting.state");
			return presence;
		}
	},
	DEFAULT {
		@Override
		public DiscordRichPresence createRPC(final DiscordRichPresence lastPresence, final Map<String, String> args) {
			return STARTING.createRPC(lastPresence, args);
		}
	},
	CREATING {
		@Override
		public DiscordRichPresence createRPC(final DiscordRichPresence lastPresence, final Map<String, String> args) {
			DiscordRichPresence presence = new DiscordRichPresence();
			presence.largeImageKey = "fruit_general";
			presence.state = SharedLocale.tr("discordrpc.status.creating.state");
			return presence;
		}
	},
	MENU {
		@Override
		public DiscordRichPresence createRPC(final DiscordRichPresence lastPresence, final Map<String, String> args) {
			DiscordRichPresence presence = new DiscordRichPresence();
			presence.largeImageKey = "fruit_general";
			presence.state = SharedLocale.tr("discordrpc.status.menu.state");
			return presence;
		}
	},
	CONFIG {
		@Override
		public DiscordRichPresence createRPC(final DiscordRichPresence lastPresence, final Map<String, String> args) {
			DiscordRichPresence presence = new DiscordRichPresence();
			presence.smallImageKey = "icon_config";
			presence.largeImageKey = "fruit_general";
			presence.state = SharedLocale.tr("discordrpc.status.config.state");
			return presence;
		}
	},
	LOGIN {
		@Override
		public DiscordRichPresence createRPC(final DiscordRichPresence lastPresence, final Map<String, String> args) {
			DiscordRichPresence presence = new DiscordRichPresence();
			presence.smallImageKey = "icon_login";
			presence.largeImageKey = "fruit_general";
			String server = args.get("server");
			presence.smallImageText = !StringUtils.isEmpty(server) ? SharedLocale.tr("discordrpc.server", server) : SharedLocale.tr("discordrpc.server.nothing");
			String instance = args.get("instance");
			presence.details = !StringUtils.isEmpty(instance) ? SharedLocale.tr("discordrpc.instance", instance) : SharedLocale.tr("discordrpc.instance.nothing");
			presence.state = SharedLocale.tr("discordrpc.status.login.state");
			return presence;
		}
	},
	WAITING {
		@Override
		public DiscordRichPresence createRPC(final DiscordRichPresence lastPresence, final Map<String, String> args) {
			DiscordRichPresence presence = new DiscordRichPresence();
			presence.smallImageKey = "icon_waiting";
			presence.largeImageKey = "fruit_general";
			String server = args.get("server");
			presence.smallImageText = !StringUtils.isEmpty(server) ? SharedLocale.tr("discordrpc.server", server) : SharedLocale.tr("discordrpc.server.nothing");
			String instance = args.get("instance");
			presence.details = !StringUtils.isEmpty(instance) ? SharedLocale.tr("discordrpc.instance", instance) : SharedLocale.tr("discordrpc.instance.nothing");
			presence.state = SharedLocale.tr("discordrpc.status.waiting.state");
			return presence;
		}
	},
	FEATURE_SELECT {
		@Override
		public DiscordRichPresence createRPC(final DiscordRichPresence lastPresence, final Map<String, String> args) {
			DiscordRichPresence presence = new DiscordRichPresence();
			presence.smallImageKey = "icon_feature_select";
			presence.largeImageKey = "fruit_general";
			String server = args.get("server");
			presence.smallImageText = !StringUtils.isEmpty(server) ? SharedLocale.tr("discordrpc.server", server) : SharedLocale.tr("discordrpc.server.nothing");
			String instance = args.get("instance");
			presence.details = !StringUtils.isEmpty(instance) ? SharedLocale.tr("discordrpc.instance", instance) : SharedLocale.tr("discordrpc.instance.nothing");
			presence.state = SharedLocale.tr("discordrpc.status.feature_select.state");
			return presence;
		}
	},
	DOWNLOADING {
		@Override
		public DiscordRichPresence createRPC(final DiscordRichPresence lastPresence, final Map<String, String> args) {
			DiscordRichPresence presence = new DiscordRichPresence();
			presence.smallImageKey = "icon_download";
			presence.largeImageKey = "fruit_general";
			String server = args.get("server");
			presence.smallImageText = !StringUtils.isEmpty(server) ? SharedLocale.tr("discordrpc.server", server) : SharedLocale.tr("discordrpc.server.nothing");
			String instance = args.get("instance");
			presence.details = !StringUtils.isEmpty(instance) ? SharedLocale.tr("discordrpc.instance", instance) : SharedLocale.tr("discordrpc.instance.nothing");
			presence.state = SharedLocale.tr("discordrpc.status.downloading.state");
			return presence;
		}
	},
	LAUNCHING {
		@Override
		public DiscordRichPresence createRPC(final DiscordRichPresence lastPresence, final Map<String, String> args) {
			DiscordRichPresence presence = new DiscordRichPresence();
			presence.smallImageKey = "icon_launching";
			presence.largeImageKey = "fruit_general";
			String server = args.get("server");
			presence.smallImageText = !StringUtils.isEmpty(server) ? SharedLocale.tr("discordrpc.server", server) : SharedLocale.tr("discordrpc.server.nothing");
			String instance = args.get("instance");
			presence.details = !StringUtils.isEmpty(instance) ? SharedLocale.tr("discordrpc.instance", instance) : SharedLocale.tr("discordrpc.instance.nothing");
			String player = args.get("player");
			presence.largeImageText = !StringUtils.isEmpty(player) ? SharedLocale.tr("discordrpc.player", player) : SharedLocale.tr("discordrpc.player.nothing");
			presence.state = SharedLocale.tr("discordrpc.status.launching.state");
			presence.startTimestamp = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis());
			return presence;
		}
	},
	PLAYING {
		@Override
		public DiscordRichPresence createRPC(final DiscordRichPresence lastPresence, final Map<String, String> args) {
			DiscordRichPresence presence = new DiscordRichPresence();
			presence.smallImageKey = "icon_playing";
			presence.largeImageKey = "fruit_general";
			String server = args.get("server");
			presence.smallImageText = !StringUtils.isEmpty(server) ? SharedLocale.tr("discordrpc.server", server) : SharedLocale.tr("discordrpc.server.nothing");
			String instance = args.get("instance");
			presence.details = !StringUtils.isEmpty(instance) ? SharedLocale.tr("discordrpc.instance", instance) : SharedLocale.tr("discordrpc.instance.nothing");
			String player = args.get("player");
			presence.largeImageText = !StringUtils.isEmpty(player) ? SharedLocale.tr("discordrpc.player", player) : SharedLocale.tr("discordrpc.player.nothing");
			presence.state = SharedLocale.tr("discordrpc.status.playing.state");
			presence.startTimestamp = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis());
			return presence;
		}
	},
	CRASHED {
		@Override
		public DiscordRichPresence createRPC(final DiscordRichPresence lastPresence, final Map<String, String> args) {
			DiscordRichPresence presence = new DiscordRichPresence();
			presence.smallImageKey = "icon_crashed";
			presence.largeImageKey = "fruit_general";
			String server = args.get("server");
			presence.smallImageText = !StringUtils.isEmpty(server) ? SharedLocale.tr("discordrpc.server", server) : SharedLocale.tr("discordrpc.server.nothing");
			String instance = args.get("instance");
			presence.details = !StringUtils.isEmpty(instance) ? SharedLocale.tr("discordrpc.instance", instance) : SharedLocale.tr("discordrpc.instance.nothing");
			String player = args.get("player");
			presence.largeImageText = !StringUtils.isEmpty(player) ? SharedLocale.tr("discordrpc.player", player) : SharedLocale.tr("discordrpc.player.nothing");
			presence.state = SharedLocale.tr("discordrpc.status.crashed.state");
			return presence;
		}
	},

	;

	public abstract DiscordRichPresence createRPC(final DiscordRichPresence lastPresence, final Map<String, String> args);

	public void update(final DiscordRichPresence lastPresence, final Map<String, String> args) {
		LauncherDiscord.updateStatus(createRPC(lastPresence, args));
	}
}
