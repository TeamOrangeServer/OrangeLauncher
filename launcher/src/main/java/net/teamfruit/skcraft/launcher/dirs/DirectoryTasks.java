package net.teamfruit.skcraft.launcher.dirs;

import java.awt.Window;
import java.io.File;

import com.skcraft.concurrency.ObservableFuture;
import com.skcraft.launcher.Launcher;
import com.skcraft.launcher.dialog.ProgressDialog;
import com.skcraft.launcher.swing.SwingHelper;
import com.skcraft.launcher.util.SharedLocale;

public class DirectoryTasks {

    private final Launcher launcher;

    public DirectoryTasks(Launcher launcher) {
        this.launcher = launcher;
    }

    public ObservableFuture<File> move(Window window, File src, File dest) {
        // Execute the deleter
        Mover mover = new Mover(src, dest);
        ObservableFuture<File> future = new ObservableFuture<File>(
                launcher.getExecutor().submit(mover), mover);

        // Show progress
        ProgressDialog.showProgress(
                window, future, SharedLocale.tr("directoryMover.movingTitle"), SharedLocale.tr("directoryMover.movingStatus", src, dest));
        SwingHelper.addErrorDialogCallback(window, future);

        return future;
    }

}