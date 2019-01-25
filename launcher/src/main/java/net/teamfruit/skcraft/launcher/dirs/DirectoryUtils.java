package net.teamfruit.skcraft.launcher.dirs;

import java.awt.Component;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.Nullable;
import javax.swing.JOptionPane;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;

import com.skcraft.launcher.LauncherUtils;
import com.skcraft.launcher.swing.SwingHelper;
import com.skcraft.launcher.util.SharedLocale;

public class DirectoryUtils {
	public static boolean isAbsolute(String path) {
		return new File(path).isAbsolute();
	}

	public static boolean checkMovable(Component parent, File srcDir, File destDir) {
		/*if (!srcDir.isDirectory()) {
			SwingHelper.showMessageDialog(parent,
					SharedLocale.tr("directoryMover.unmovableSrc", srcDir.getAbsolutePath(), destDir.getAbsolutePath()),
					SharedLocale.tr("directoryMover.unmovableTitle"),
					SharedLocale.tr("directoryMover.unmovableSrcDetails", srcDir.getAbsolutePath(), destDir.getAbsolutePath()),
					JOptionPane.ERROR_MESSAGE);
			return false;
		}*/
		if (destDir.exists()) {
			if (!destDir.isDirectory()||destDir.length()>0) {
				SwingHelper.showMessageDialog(parent,
						SharedLocale.tr("directoryMover.unmovableDest", srcDir.getAbsolutePath(), destDir.getAbsolutePath()),
						SharedLocale.tr("directoryMover.unmovableTitle"),
						SharedLocale.tr("directoryMover.unmovableDestDetails", srcDir.getAbsolutePath(), destDir.getAbsolutePath()),
						JOptionPane.ERROR_MESSAGE);
				return false;
			}
		}
		try {
			if (destDir.getCanonicalPath().startsWith(srcDir.getCanonicalPath()+File.separator)) {
				SwingHelper.showMessageDialog(parent,
						SharedLocale.tr("directoryMover.unmovableInner", srcDir.getAbsolutePath(), destDir.getAbsolutePath()),
						SharedLocale.tr("directoryMover.unmovableTitle"),
						SharedLocale.tr("directoryMover.unmovableInnerDetails", srcDir.getAbsolutePath(), destDir.getAbsolutePath()),
						JOptionPane.ERROR_MESSAGE);
				return false;
			}
		} catch (IOException e) {
		}
		return true;
	}

	public static @Nullable File findExistsDirFromAncestors(File file) {
		if (file.isDirectory())
			return file;
		File parent = file.getParentFile();
		if (parent==null)
			return null;
		return findExistsDirFromAncestors(parent);
	}

	public static File getDirFromOption(File baseDir, String path) {
		File destDir = baseDir;
		if (!StringUtils.isEmpty(path)) {
			File absDir = new File(path);
			if (absDir.isAbsolute())
				destDir = absDir;
			else
				destDir = new File(baseDir, path);
		}
		return destDir;
	}

	public static File tryCanonical(File file) {
		if (file==null)
			return null;
		try {
			file = file.getCanonicalFile();
		} catch (IOException e) {
		}
		return file;
	}

	public static boolean isInSubDirectory(File dir, File file) {
		if (file==null)
			return false;

		if (file.equals(dir))
			return true;

		return isInSubDirectory(dir, file.getParentFile());
	}

	public static String getRelativePath(File baseDir, File targetDir) {
		return StringUtils.replace(StringUtils.stripEnd(baseDir.toURI().relativize(targetDir.toURI()).getPath(), "/"), "/", File.separator);
	}

	public static int getFilesCount(File file) {
		File[] files = file.listFiles();
		int count = 0;
		if (files!=null)
			for (File f : files)
				if (f.isDirectory())
					count += getFilesCount(f);
				else
					count++;
		return count;
	}

	public static void interruptibleMoveDirectory(File srcDir, File destDir, AtomicInteger done) throws IOException, InterruptedException {
		if (srcDir==null) {
			throw new NullPointerException("Source must not be null");
		}
		if (destDir==null) {
			throw new NullPointerException("Destination must not be null");
		}
		if (!srcDir.exists()) {
			throw new FileNotFoundException("Source '"+srcDir+"' does not exist");
		}
		if (!srcDir.isDirectory()) {
			throw new IOException("Source '"+srcDir+"' is not a directory");
		}
		if (destDir.exists()) {
			if (!destDir.isDirectory()||destDir.length()>0)
				throw new IOException("Destination '"+destDir+"' already exists");
			else
				destDir.delete();
		}
		boolean rename = srcDir.renameTo(destDir);
		if (!rename) {
			if (destDir.getCanonicalPath().startsWith(srcDir.getCanonicalPath()+File.separator)) {
				throw new IOException("Cannot move directory: "+srcDir+" to a subdirectory of itself: "+destDir);
			}
			interruptibleCopyDirectory(srcDir, destDir, null, true, done);
			done.set(-1);
			FileUtils.deleteDirectory(srcDir);
			if (srcDir.exists()) {
				throw new IOException("Failed to delete original directory '"+srcDir+
						"' after copy to '"+destDir+"'");
			}
		}
	}

	public static void interruptibleCopyDirectory(
			File srcDir, File destDir,
			FileFilter filter, boolean preserveFileDate, AtomicInteger done
	) throws IOException, InterruptedException {
		if (srcDir==null) {
			throw new NullPointerException("Source must not be null");
		}
		if (destDir==null) {
			throw new NullPointerException("Destination must not be null");
		}
		if (srcDir.exists()==false) {
			throw new FileNotFoundException("Source '"+srcDir+"' does not exist");
		}
		if (srcDir.isDirectory()==false) {
			throw new IOException("Source '"+srcDir+"' exists but is not a directory");
		}
		if (srcDir.getCanonicalPath().equals(destDir.getCanonicalPath())) {
			throw new IOException("Source '"+srcDir+"' and destination '"+destDir+"' are the same");
		}

		// Cater for destination being directory within the source directory (see IO-141)
		List<String> exclusionList = null;
		if (destDir.getCanonicalPath().startsWith(srcDir.getCanonicalPath())) {
			File[] srcFiles = filter==null ? srcDir.listFiles() : srcDir.listFiles(filter);
			if (srcFiles!=null&&srcFiles.length>0) {
				exclusionList = new ArrayList<String>(srcFiles.length);
				for (File srcFile : srcFiles) {
					File copiedFile = new File(destDir, srcFile.getName());
					exclusionList.add(copiedFile.getCanonicalPath());
				}
			}
		}
		interruptibleDoCopyDirectory(srcDir, destDir, filter, preserveFileDate, exclusionList, done);
	}

	private static void interruptibleDoCopyDirectory(
			File srcDir, File destDir, FileFilter filter,
			boolean preserveFileDate, List<String> exclusionList, AtomicInteger done
	) throws IOException, InterruptedException {
		// recurse
		File[] srcFiles = filter==null ? srcDir.listFiles() : srcDir.listFiles(filter);
		if (srcFiles==null) { // null if abstract pathname does not denote a directory, or if an I/O error occurs
			throw new IOException("Failed to list contents of "+srcDir);
		}
		if (destDir.exists()) {
			if (destDir.isDirectory()==false) {
				throw new IOException("Destination '"+destDir+"' exists but is not a directory");
			}
		} else {
			if (!destDir.mkdirs()&&!destDir.isDirectory()) {
				throw new IOException("Destination '"+destDir+"' directory cannot be created");
			}
		}
		if (destDir.canWrite()==false) {
			throw new IOException("Destination '"+destDir+"' cannot be written to");
		}
		for (File srcFile : srcFiles) {
			File dstFile = new File(destDir, srcFile.getName());
			if (exclusionList==null||!exclusionList.contains(srcFile.getCanonicalPath())) {
				if (srcFile.isDirectory()) {
					interruptibleDoCopyDirectory(srcFile, dstFile, filter, preserveFileDate, exclusionList, done);
				} else {
					LauncherUtils.checkInterrupted();
					FileUtils.copyFile(srcFile, dstFile, preserveFileDate);
					done.incrementAndGet();
				}
			}
		}

		// Do this last, as the above has probably affected directory metadata
		if (preserveFileDate) {
			destDir.setLastModified(srcDir.lastModified());
		}
	}
}
