package net.teamfruit.skcraft.launcher.mcpinger;

import lombok.Data;
import lombok.experimental.Accessors;
import net.teamfruit.skcraft.launcher.model.modpack.ConnectServerInfo;

/**
 * Storage class for {@link Pinger} options.
 */
@Accessors(fluent = true)
@Data
public class PingOptions {

	private String hostname;
	private int port = 25565;
	private int timeout = 8000;
	private String charset = "UTF-8";

	public PingOptions withServer(ConnectServerInfo serverInfo) {
		if (serverInfo.isValid())
			hostname(serverInfo.getServerHost()).port(serverInfo.getServerPort());
		return this;
	}

}
