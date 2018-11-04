package net.teamfruit.skcraft.launcher.windowdetector;

import java.lang.reflect.Field;

import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.WinNT;

public class ProcessUtils {
	public static long getProcessID(Process p) {
		long result = -1;
		try {
			//for windows
			if (
				p.getClass().getName().equals("java.lang.Win32Process")||
						p.getClass().getName().equals("java.lang.ProcessImpl")
			) {
				Field f = p.getClass().getDeclaredField("handle");
				f.setAccessible(true);
				long handl = f.getLong(p);
				Kernel32 kernel = Kernel32.INSTANCE;
				WinNT.HANDLE hand = new WinNT.HANDLE();
				hand.setPointer(Pointer.createConstant(handl));
				result = kernel.GetProcessId(hand);
				f.setAccessible(false);
			}
			//for unix based operating systems
			else if (p.getClass().getName().equals("java.lang.UNIXProcess")) {
				Field f = p.getClass().getDeclaredField("pid");
				f.setAccessible(true);
				result = f.getLong(p);
				f.setAccessible(false);
			}
		} catch (Exception ex) {
			result = -1;
		}
		return result;
	}
}
