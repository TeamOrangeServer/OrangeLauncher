package net.teamfruit.skcraft.launcher.swing;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.swing.Timer;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Sets;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

import lombok.extern.java.Log;
import net.teamfruit.skcraft.launcher.mcpinger.PingOptions;
import net.teamfruit.skcraft.launcher.mcpinger.PingResult;
import net.teamfruit.skcraft.launcher.mcpinger.Pinger;
import net.teamfruit.skcraft.launcher.model.modpack.ConnectServerInfo;

@Log
public class ServerInfoFactory {
	private final ListeningExecutorService executor = MoreExecutors.listeningDecorator(Executors.newCachedThreadPool(new ThreadFactoryBuilder().setNameFormat("MCPinger-%d").setDaemon(true).build()));

	private final LoadingCache<ConnectServerInfo, ListenableFuture<PingResult>> status = CacheBuilder.newBuilder()
			.build(new CacheLoader<ConnectServerInfo, ListenableFuture<PingResult>>() {
				@Override
				public ListenableFuture<PingResult> load(final ConnectServerInfo key) throws Exception {
					return executor.submit(new Callable<PingResult>() {
						@Override
						public PingResult call() throws Exception {
							return Pinger.ping(new PingOptions().withServer(key));
						}
					});
				}
			});

	private final Set<ServerInfoPanel> updateComponents = Sets.newSetFromMap(new WeakHashMap<ServerInfoPanel, Boolean>());

	private final Timer timer = new Timer((int) TimeUnit.SECONDS.toMillis(60), new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			status.invalidateAll();
			for (ServerInfoPanel updateComponent : updateComponents)
				updateComponent.update();
		}
	});

	{
		timer.start();
	}

	public ServerInfoPanel getServerInfo(ServerInfoStyle style, final ConnectServerInfo server) {
		ServerInfoPanel panel = new ServerInfoPanel(style, server, new Callable<ListenableFuture<PingResult>>() {
			@Override
			public ListenableFuture<PingResult> call() throws Exception {
				if (server!=null)
					return status.get(server);
				return null;
			}
		});
		updateComponents.add(panel);
		return panel;
	}
}
