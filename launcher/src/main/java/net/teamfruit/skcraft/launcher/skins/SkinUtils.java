package net.teamfruit.skcraft.launcher.skins;

import java.awt.Window;
import java.util.logging.Level;

import com.google.common.base.Predicate;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.skcraft.concurrency.ObservableFuture;
import com.skcraft.launcher.Launcher;
import com.skcraft.launcher.dialog.ProgressDialog;
import com.skcraft.launcher.swing.SwingHelper;
import com.skcraft.launcher.util.SharedLocale;
import com.skcraft.launcher.util.SwingExecutor;

import lombok.extern.java.Log;

@Log
public class SkinUtils {
	public static void loadSkinList(final Window window, final Launcher launcher, boolean showProgress, final Predicate<RemoteSkinList> callback) {
		RemoteSkinList.Enumerator remoteSkinListEnumerator = launcher.getRemoteSkins().createEnumerator();
		ObservableFuture<RemoteSkinList> skinListFuture = new ObservableFuture<RemoteSkinList>(launcher.getExecutor().submit(remoteSkinListEnumerator), remoteSkinListEnumerator);
		Futures.addCallback(skinListFuture, new FutureCallback<RemoteSkinList>() {
			@Override
			public void onSuccess(RemoteSkinList remoteSkinList) {
				callback.apply(remoteSkinList);
			}

			@Override
			public void onFailure(Throwable t) {
				log.log(Level.WARNING, "Failed to load skin list: ", t);
			}
		}, SwingExecutor.INSTANCE);
		if (showProgress)
			ProgressDialog.showProgress(window, skinListFuture, SharedLocale.tr("skins.loadingListTitle"), SharedLocale.tr("skins.loadingList"));
        SwingHelper.addErrorDialogCallback(window, skinListFuture);
	}

	public static void loadSkin(final Window window, final Launcher launcher, boolean showProgress, final RemoteSkin remoteSkin, final Predicate<RemoteSkin> callback) {
		if (remoteSkin!=null) {
			RemoteSkin.Enumerator remoteSkinEnumerator = remoteSkin.createEnumerator();
			ObservableFuture<RemoteSkin> skinFuture = new ObservableFuture<RemoteSkin>(launcher.getExecutor().submit(remoteSkinEnumerator), remoteSkinEnumerator);
			Futures.addCallback(skinFuture, new FutureCallback<RemoteSkin>() {
				@Override
				public void onSuccess(RemoteSkin remoteSkin) {
					callback.apply(remoteSkin);
				}

				@Override
				public void onFailure(Throwable t) {
					log.log(Level.WARNING, "Failed to load skin "+remoteSkin.getName()+": ", t);
				}
			}, SwingExecutor.INSTANCE);
			if (showProgress)
				ProgressDialog.showProgress(window, skinFuture, SharedLocale.tr("skins.loadingTitle"), SharedLocale.tr("skins.loading"));
	        SwingHelper.addErrorDialogCallback(window, skinFuture);
		}
	}
}
