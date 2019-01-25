package net.teamfruit.skcraft.launcher.mcpinger;

import java.io.IOException;

public class PingerException extends IOException {

	private final String localizedMessage;

	public PingerException(String message, String localizedMessage) {
		super(message);
		this.localizedMessage = localizedMessage;
	}

	public PingerException(Throwable cause, String localizedMessage) {
		super(cause.getMessage(), cause);
		this.localizedMessage = localizedMessage;
	}

	@Override
	public String getLocalizedMessage() {
		return localizedMessage;
	}

}
