package net.teamfruit.skcraft.launcher.discordrpc;

import java.util.function.Consumer;
import java.util.logging.Level;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableMap;
import com.jagrosh.discordipc.IPCClient;
import com.jagrosh.discordipc.IPCListener;
import com.jagrosh.discordipc.entities.Callback;
import com.jagrosh.discordipc.exceptions.NoDiscordClientException;

import lombok.extern.java.Log;
import net.teamfruit.skcraft.launcher.dirs.LauncherDirectories;

@Log
public class LauncherDiscord {
	@Nullable
	private static LauncherDiscord instance;

	public static void init(LauncherDirectories dirs) {
		try {
			instance = new LauncherDiscord();
		} catch (NoDiscordClientException e) {
			log.log(Level.INFO, "[DiscordRPC] No client detected");
			log.log(Level.FINE, "[DiscordRPC] No client detected: ", e);
		} catch (Exception e) {
			log.log(Level.WARNING, "[DiscordRPC] exception: ", e);
		} catch (Throwable t) {
			log.log(Level.WARNING, "[DiscordRPC] error: ", t);
		}
	}

	private final IPCClient client;

	private LauncherDiscord() throws Exception {
		log.info("[DiscordRPC] initializing.");
//		client = new IPCClient(425297966069317632L);
		client = new IPCClient(479257307885207562L);

		client.setListener(new IPCListener() {
			@Override
			public void onReady(IPCClient client) {
				log.info("[DiscordRPC] online.");
				updateStatusImpl(DiscordStatus.DEFAULT.createRPC(new DiscordRichPresence(), ImmutableMap.<String, String> of()));
			}
		});

		log.info("[DiscordRPC] starting.");
		client.connect();

		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
			@Override
			public void run() {
				log.info("[DiscordRPC] shutting down.");
				client.close();
			}
		}));
	}

	public void updateStatusImpl(DiscordRichPresence presence) {
		//log.info("[DiscordRPC] : "+presence.details+", "+presence.state);
		//log.info("[DiscordRPC] : check: "+(client.getStatus()!=PipeStatus.DISCONNECTED&&client.getStatus()!=PipeStatus.CLOSED));
		client.sendRichPresence(presence.toRichPresence(), new Callback(new Consumer<String>() {
			@Override
			public void accept(String t) {
				log.info("[DiscordRPC] status update failed: "+t);
			}
		}));
	}

	public void clearStatusImpl() {
		client.sendRichPresence(null);
	}

	public static void updateStatus(DiscordRichPresence presence) {
		final LauncherDiscord inst = instance;
		if (inst!=null)
			try {
				if (presence!=null)
					inst.updateStatusImpl(presence);
				else
					inst.clearStatusImpl();
			} catch (Throwable t) {
				log.log(Level.WARNING, "[DiscordRPC] status update error: ", t);
			}
	}
}
