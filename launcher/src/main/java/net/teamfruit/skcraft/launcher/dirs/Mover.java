package net.teamfruit.skcraft.launcher.dirs;

import static com.skcraft.launcher.LauncherUtils.*;
import static com.skcraft.launcher.util.SharedLocale.*;

import java.io.File;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

import com.skcraft.concurrency.ProgressObservable;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class Mover implements Callable<File>, ProgressObservable {

	private final File src;
	private final File dest;

	private int overall;
	private AtomicInteger done = new AtomicInteger(0);

	@Override
	public double getProgress() {
		if (overall<=0)
			return -1;
		int doneint = done.get();
		if (doneint<=0)
			return -1;
		return ((double) doneint)/overall;
	}

	@Override
	public String getStatus() {
		return tr("directoryMover.moving", src, dest);
	}

	@Override
	public File call() throws Exception {
		Thread.sleep(2000);

		checkInterrupted();

		overall = DirectoryUtils.getFilesCount(src);

		checkInterrupted();

		DirectoryUtils.interruptibleMoveDirectory(src, dest, done);

		return dest;
	}

}
