package net.teamfruit.skcraft.launcher.skins;

import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.logging.Level;

import javax.annotation.Nullable;

import org.apache.commons.lang.StringUtils;

import com.beust.jcommander.internal.Maps;
import com.skcraft.concurrency.DefaultProgress;
import com.skcraft.concurrency.ProgressObservable;
import com.skcraft.launcher.Launcher;
import com.skcraft.launcher.LauncherException;
import com.skcraft.launcher.util.HttpRequest;
import com.skcraft.launcher.util.SharedLocale;

import lombok.Getter;
import lombok.NonNull;
import lombok.extern.java.Log;
import net.teamfruit.skcraft.launcher.model.skins.SkinInfoList;
import net.teamfruit.skcraft.launcher.model.skins.SkinInfoListNode;

@Log
public class RemoteSkinList {

	private final Launcher launcher;
	@Getter
	private Map<String, RemoteSkin> skins = Maps.newHashMap();

	/**
	 * Create a new skin list.
	 *
	 * @param launcher the launcher
	 */
	public RemoteSkinList(@NonNull final Launcher launcher) {
		this.launcher = launcher;
	}

	/**
	 * Get the skin
	 *
	 * @return the skin map
	 */
	public Map<String, RemoteSkin> getSkinMap() {
		return Collections.unmodifiableMap(this.skins);
	}

	public @Nullable RemoteSkin getRemoteSkin(String name) {
		if (StringUtils.isEmpty(name)||skins==null)
			return null;
		RemoteSkin url = skins.get(name);
		if (url==null)
			return null;
		return url;
	}

	/**
	 * Create a worker that loads the list of skins from disk and from
	 * the remote list of packages.
	 *
	 * @return the worker
	 */
	public Enumerator createEnumerator() {
		return new Enumerator();
	}

	public final class Enumerator implements Callable<RemoteSkinList>, ProgressObservable {
		private ProgressObservable progress = new DefaultProgress(-1, null);

		private Enumerator() {
		}

		@Override
		public RemoteSkinList call() throws Exception {
			log.info("Enumerating skins list...");

			Map<String, RemoteSkin> remote = Maps.newHashMap();

			this.progress = new DefaultProgress(0.3, SharedLocale.tr("skins.checkingListRemote"));

			try {
				final URL skinsURL = RemoteSkinList.this.launcher.getSkinsURL();

				final SkinInfoList skinInfoList = HttpRequest
						.get(skinsURL)
						.execute()
						.expectResponseCode(200)
						.returnContent()
						.asJson(SkinInfoList.class);

				if (skinInfoList.getMinimumVersion()>SkinInfoList.MIN_VERSION)
					throw new LauncherException("Update required", SharedLocale.tr("errors.updateRequiredError"));

				Map<String, SkinInfoListNode> skinsInfo = skinInfoList.getSkins();
				if (skinsInfo!=null)
					for (final Entry<String, SkinInfoListNode> entry : skinsInfo.entrySet()) {
						String name = entry.getKey();
						SkinInfoListNode node = entry.getValue();
						remote.put(name, new RemoteSkin(launcher, name, node.getTitle(), node.getUrl()));
					}

			} catch (final IOException e) {
				log.log(Level.WARNING, "The list of skins could not be downloaded.", e);
			} finally {
				synchronized (RemoteSkinList.this) {
					RemoteSkinList.this.skins.clear();
					RemoteSkinList.this.skins.putAll(remote);

					log.info(RemoteSkinList.this.skins.size()+" skin(s) enumerated.");
				}
			}

			return RemoteSkinList.this;
		}

		@Override
		public double getProgress() {
			return -1;
		}

		@Override
		public String getStatus() {
			return this.progress.getStatus();
		}
	}
}