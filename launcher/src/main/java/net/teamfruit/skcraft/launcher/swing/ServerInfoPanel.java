package net.teamfruit.skcraft.launcher.swing;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.List;
import java.util.concurrent.Callable;

import javax.swing.JComponent;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;

import com.google.common.base.Predicate;
import com.google.common.util.concurrent.ListenableFuture;
import com.skcraft.launcher.util.SharedLocale;
import com.skcraft.launcher.util.SwingExecutor;

import lombok.Data;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Builder;
import lombok.extern.java.Log;
import net.teamfruit.skcraft.launcher.mcpinger.PingResult;
import net.teamfruit.skcraft.launcher.mcpinger.PingResult.Description;
import net.teamfruit.skcraft.launcher.mcpinger.PingResult.Player;
import net.teamfruit.skcraft.launcher.mcpinger.PingResult.Players;
import net.teamfruit.skcraft.launcher.mcpinger.PingResult.Version;
import net.teamfruit.skcraft.launcher.model.modpack.ConnectServerInfo;
import net.teamfruit.skcraft.launcher.sounds.Sounds;
import net.teamfruit.skcraft.launcher.swing.ChatMessagePanel.HTMLLog;

@Log
@RequiredArgsConstructor
public class ServerInfoPanel {

	private final ServerInfoStyle style;
	private final ConnectServerInfo server;
	private final Callable<ListenableFuture<PingResult>> futureSupplier;
	private InstanceCellPanel instancePanel;
	private InfoMessageStatus lastStatus = InfoMessageStatus.PINGING;

	public void paint(final Graphics g, Rectangle r, @NonNull InstanceCellPanel instancePanel) {
		this.instancePanel = instancePanel;
		if (futureSupplier!=null) {
			InfoMessage infoMessage = getMessage();
			InfoMessageStatus status = infoMessage.getStatus();
			String message = infoMessage.getMessage();
			String details = infoMessage.getDetails();
			if (!StringUtils.isEmpty(message)) {
				Rectangle rect = new Rectangle(r.x+50, r.y+0, r.width-50-25, 20);
				final Graphics2D g2d = (Graphics2D) g.create();
				g2d.translate(rect.x, rect.y);

				FontMetrics fontMetrics = g2d.getFontMetrics();
				int padding = 3;
				int width = fontMetrics.stringWidth(message);
				g2d.setColor(new Color(0, 0, 0, 50));
				//g2d.drawRect(0, 0, rect.width, rect.height);
				g2d.fillRect(rect.width-width-padding*2, 0, width+padding*2, rect.height);
				g2d.setColor(Color.WHITE);
				g2d.drawString(message, rect.width-width-padding, rect.height-fontMetrics.getDescent());
				g2d.dispose();
			}
			if (style!=ServerInfoStyle.SIMPLE)
				if (status!=InfoMessageStatus.PINGING) {
					if (lastStatus==InfoMessageStatus.OFFLINE&&status==InfoMessageStatus.ONLINE) {
						Sounds.play("notice.wav");
					}
					if (lastStatus!=status)
						if (statusCallback!=null)
							statusCallback.apply(status==InfoMessageStatus.ONLINE);
					lastStatus = status;
				}
			if (style==ServerInfoStyle.DETAILS)
				instancePanel.setToolTipText(StringUtils.isEmpty(details) ? null : "<html>"+details.replace("\n", "<br>")+"</html>");
		}
	}

	public boolean isOnline() {
		return lastStatus==InfoMessageStatus.ONLINE;
	}

	@Getter
	@Setter
	private Predicate<Boolean> statusCallback;

	private ListenableFuture<PingResult> resultFuture;

	private InfoMessage getMessage() {
		InfoMessage.InfoMessageBuilder builder = InfoMessage.builder();
		try {
			{
				ListenableFuture<PingResult> future = futureSupplier.call();
				if (future!=resultFuture) {
					if (!future.isDone())
						future.addListener(new Runnable() {
							@Override
							public void run() {
								update();
							}
						}, SwingExecutor.INSTANCE);
					resultFuture = future;
				}
			}
			if (resultFuture!=null) {
				if (!resultFuture.isDone()) {
					builder.status(InfoMessageStatus.PINGING);
					builder.message(SharedLocale.tr("mcpinger.message.pinging"));
				} else {
					PingResult result = null;
					String error = null;
					try {
						result = resultFuture.get();
					} catch (Exception e) {
						error = e.getMessage();
					}
					if (result!=null) {
						builder.status(InfoMessageStatus.ONLINE);
						Players players = result.getPlayers();
						Version version = result.getVersion();
						Description description = result.getDescription();

						String online = "?";
						String max = "?";
						if (players!=null) {
							online = String.valueOf(players.getOnline());
							max = String.valueOf(players.getMax());
						}

						builder.message(SharedLocale.tr(
								(style==ServerInfoStyle.SIMPLE ? "mcpinger.message.statusOnlineSimple" : "mcpinger.message.statusOnline"), server, online, max));

						StringBuilder stb = new StringBuilder();
						stb.append(SharedLocale.tr("mcpinger.details.ip", server)).append("\n");
						stb.append(SharedLocale.tr("mcpinger.details.statusOnline")).append("\n");
						if (version!=null)
							stb.append(SharedLocale.tr("mcpinger.details.version", version.getName())).append("\n");
						if (description!=null) {
							HTMLLog htmllog = new HTMLLog();
							ChatMessagePanel.log(description.getText(), htmllog);
							stb.append(SharedLocale.tr("mcpinger.details.description", htmllog)).append("\n");
						}
						stb.append(SharedLocale.tr("mcpinger.details.players", online, max)).append("\n");
						if (players!=null) {
							List<Player> sampleplayers = players.getSample();
							if (sampleplayers!=null) {
								for (Player player : sampleplayers)
									if (player!=null)
										stb.append(SharedLocale.tr("mcpinger.details.playerLine", player.getName())).append("\n");
								if (NumberUtils.isNumber(online)) {
									int intonline = NumberUtils.toInt(online);
									int samplesize = sampleplayers.size();
									if (intonline>samplesize)
										stb.append(SharedLocale.tr("mcpinger.details.playerRemaining", intonline-samplesize));
								}
							}
						}
						builder.details(stb.toString());
					} else {
						builder.status(InfoMessageStatus.OFFLINE);
						builder.message(SharedLocale.tr(
								(style==ServerInfoStyle.SIMPLE ? "mcpinger.message.statusOfflineSimple" : "mcpinger.message.statusOffline"), server));

						StringBuilder stb = new StringBuilder();
						stb.append(SharedLocale.tr("mcpinger.details.ip", server)).append("\n");
						stb.append(SharedLocale.tr("mcpinger.details.statusOffline")).append("\n");
						if (error!=null)
							stb.append(SharedLocale.tr("mcpinger.details.error", error)).append("\n");
						builder.details(stb.toString());
					}
				}
			}
		} catch (Exception e1) {
		}
		return builder.build();
	}

	@Builder(fluent = true)
	@Data
	private static class InfoMessage {
		private InfoMessageStatus status;
		private String message;
		private String details;
	}

	private static enum InfoMessageStatus {
		ONLINE,
		OFFLINE,
		PINGING,
	}

	public void update() {
		if (instancePanel!=null) {
			JComponent updateComponent = instancePanel.getUpdateComponent();
			if (updateComponent!=null)
				updateComponent.repaint();
		}
	}

}
