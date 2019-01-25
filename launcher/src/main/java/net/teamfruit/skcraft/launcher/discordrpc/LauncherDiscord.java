package net.teamfruit.skcraft.launcher.discordrpc;

import java.io.FileNotFoundException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.logging.Level;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableMap;
import com.jagrosh.discordipc.IPCClient;
import com.jagrosh.discordipc.IPCListener;
import com.jagrosh.discordipc.entities.Callback;
import com.jagrosh.discordipc.entities.pipe.PipeStatus;
import com.jagrosh.discordipc.exceptions.NoDiscordClientException;
import com.skcraft.launcher.Configuration;

import lombok.extern.java.Log;
import net.teamfruit.skcraft.launcher.windowdetector.ActiveWindowDetector;

@Log
public class LauncherDiscord {
	@Nullable
	private static LauncherDiscord instance;

	public static void init(Configuration config) {
		try {
			instance = new LauncherDiscord(config);
		} catch (Exception e) {
			log.log(Level.WARNING, "[DiscordRPC] exception: ", e);
		} catch (Throwable t) {
			log.log(Level.WARNING, "[DiscordRPC] error: ", t);
		}
	}

	private final Configuration config;
	private final IPCClient client;

	private DiscordRichPresence lastPresence = DiscordStatus.DEFAULT.createRPC(new DiscordRichPresence(), ImmutableMap.<String, String> of());

	private LauncherDiscord(final Configuration config) throws Exception {
		this.config = config;

		log.info("[DiscordRPC] initializing.");
//		client = new IPCClient(425297966069317632L);
		client = new IPCClient(479257307885207562L);

		client.setListener(new IPCListener() {
			@Override
			public void onReady(IPCClient client) {
				log.info("[DiscordRPC] online.");
				updateStatusWithNoChange();
			}

			@Override
			public void onDisconnect(IPCClient client, Throwable t) {
				log.info("[DiscordRPC] disconnected. reconnecting...");
				disconnectDiscord();
			}
		});

		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
			@Override
			public void run() {
				log.info("[DiscordRPC] shutting down.");
				disconnectDiscord();
			}
		}));

		log.info("[DiscordRPC] starting.");
		Executors.newSingleThreadScheduledExecutor().scheduleWithFixedDelay(new Runnable() {
			@Override
			public void run() {
				if (client.getStatus()!=PipeStatus.CONNECTED) {
					try {
						client.connect();
						log.info("[DiscordRPC] connected.");
					} catch (IllegalStateException e) {
						log.log(Level.FINE, "[DiscordRPC] already connected. continuing...", e);
					} catch (NoDiscordClientException e) {
						log.log(Level.FINE, "[DiscordRPC] no client detected. continuing...", e);
					} catch (Exception e) {
						if (e.getCause() instanceof FileNotFoundException)
							log.log(Level.FINE, "[DiscordRPC] connection error. continuing...: ", e);
						else
							log.log(Level.FINE, "[DiscordRPC] exception. continuing...: ", e);
					}
				} else {
					if (LauncherDiscord.this.config.isDiscordPing()) {
						if (ActiveWindowDetector.detectWindow())
							updateStatusWithNoChange();
					}
				}
			}
		}, 0, 15, TimeUnit.SECONDS);
	}

	private void disconnectDiscord() {
		if (client.getStatus()!=PipeStatus.CONNECTED)
			try {
				client.close();
			} catch (Exception e) {
				log.log(Level.FINE, "Discord disconnecting error: ", e);
			}
	}

	public void updateStatusImpl(@Nullable DiscordRichPresence presence) {
		lastPresence = presence;
		try {
			if (client.getStatus()==PipeStatus.CONNECTED) {
				//log.info("[DiscordRPC] : "+presence.details+", "+presence.state);
				//log.info("[DiscordRPC] : check: "+(client.getStatus()!=PipeStatus.DISCONNECTED&&client.getStatus()!=PipeStatus.CLOSED));
				client.sendRichPresence(presence==null ? null : presence.toRichPresence(), new Callback(new Consumer<String>() {
					@Override
					public void accept(String t) {
						log.info("[DiscordRPC] status update failed: "+t);
					}
				}));
			}
		} catch (Throwable t) {
			log.log(Level.WARNING, "[DiscordRPC] status update error: ", t);
		}
	}

	public static void updateStatusWithNoChange() {
		final LauncherDiscord inst = instance;
		if (inst!=null)
			inst.updateStatusImpl(inst.lastPresence);
	}

	public static void updateStatus(DiscordRichPresence presence) {
		final LauncherDiscord inst = instance;
		if (inst!=null)
			inst.updateStatusImpl(presence);
	}
}
