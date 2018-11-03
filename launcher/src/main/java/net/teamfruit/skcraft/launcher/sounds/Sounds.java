package net.teamfruit.skcraft.launcher.sounds;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;

import com.google.common.io.Closer;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.skcraft.launcher.LauncherUtils;

import lombok.extern.java.Log;

@Log
public class Sounds {
	private static final ExecutorService executor = Executors.newCachedThreadPool(new ThreadFactoryBuilder().setNameFormat("SoundPlayer-%d").setDaemon(true).build());

	public static void play(final String resource) {
		executor.submit(new Runnable() {
			@Override
			public void run() {
				playImmediately(resource);
			}
		});
	}

	public static void playImmediately(final String resource) {
		final Closer closer = Closer.create();
		try {
			final Clip clip = AudioSystem.getClip();
			closer.register(new Closeable() {
				@Override
				public void close() throws IOException {
					clip.close();
				}
			});
			final AudioInputStream inputStream = closer.register(AudioSystem.getAudioInputStream(
					Sounds.class.getResourceAsStream(resource)));
			clip.addLineListener(new LineListener() {
				@Override
				public void update(final LineEvent event) {
					if (event.getType()==LineEvent.Type.STOP)
						LauncherUtils.closeQuietly(closer);
				}
			});
			clip.open(inputStream);
			FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
			gainControl.setValue(-20.0f);
			clip.start();
		} catch (Exception e) {
			log.log(Level.WARNING, "Could not play sound "+resource+" :", e);
		}
	}
}
