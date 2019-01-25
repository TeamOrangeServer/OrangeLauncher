package net.teamfruit.skcraft.launcher.windowdetector;

import java.util.logging.Level;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;

import com.sun.jna.Platform;
import com.sun.jna.platform.unix.X11;
import com.sun.jna.platform.unix.X11.Window;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.ptr.IntByReference;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import net.teamfruit.skcraft.launcher.Log;

public class ActiveWindowDetector {
	@Getter(lazy = true, value = AccessLevel.PRIVATE)
	private static final ScriptEngine OSXAppleScript = new ScriptEngineManager().getEngineByName("AppleScriptEngine");

	@Getter(lazy = true, value = AccessLevel.PRIVATE)
	private static final X11.Display X11Display = WMCtrl.x11.XOpenDisplay(null);

	@Setter
	private static long currentPID = -1;

	public static boolean detectWindow() {
		if (Platform.isWindows()) {
			final User32 user32 = User32.INSTANCE;
			WinDef.HWND windowHandle = user32.GetForegroundWindow();

			IntByReference pid = new IntByReference();
			user32.GetWindowThreadProcessId(windowHandle, pid);
			return (currentPID>=0&&currentPID==pid.getValue());
		} else if (Platform.isLinux()) {
			X11.Display display = getX11Display();
			Window window = WMCtrl.get_active_window(display);
			return StringUtils.startsWith(WMCtrl.get_window_title(display, window), "Minecraft ");
		} else if (Platform.isMac()) {
			final String scriptfilename = "tell application \"System Events\"\n"+
					"\tunix id of application processes whose frontmost is true\n"+
					"end";
			ScriptEngine engine = getOSXAppleScript();
			if (engine==null)
				Log.log.log(Level.WARNING, "could not detect active window: no engine");
			else {
				try {
					Object pidobj = engine.eval(scriptfilename);
					if (pidobj!=null) {
						int pid = (pidobj instanceof Number) ? ((Number) pidobj).intValue() : NumberUtils.toInt(pidobj.toString());
						return (currentPID>=0&&currentPID==pid);
					}
				} catch (ScriptException e) {
					Log.log.log(Level.WARNING, "could not detect active window: ", e);
				}
			}
		}
		return false;
	}

	/*
	public static void main(String[] args) {
		final JFrame jf = new JFrame("Test");
		final JPanel jp = new JPanel(new BorderLayout());
		final JTextArea jl = new JTextArea("App");
		jl.setEditable(false);

		Executors.newSingleThreadScheduledExecutor().scheduleWithFixedDelay(new Runnable() {
			@Override
			public void run() {
				try {
					boolean detect = detectWindow();
					Log.log.info("Detect: "+detect);
					jl.setText("Detect: "+detect);
				} catch (Throwable e) {
					e.printStackTrace();
					jl.setText(ExceptionUtils.getFullStackTrace(e));
				}
			}
		}, 0, 500, TimeUnit.MILLISECONDS);

		jp.add(jl, BorderLayout.CENTER);
		jf.add(jp);

		jf.pack();
		jf.setSize(500, 200);
		jf.setLocationRelativeTo(null);
		jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		jf.setVisible(true);
	}
	*/
}
