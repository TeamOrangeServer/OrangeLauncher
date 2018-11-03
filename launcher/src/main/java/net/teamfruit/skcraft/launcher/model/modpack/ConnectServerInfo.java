package net.teamfruit.skcraft.launcher.model.modpack;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.common.base.Strings;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(doNotUseGetters = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor
@AllArgsConstructor
public class ConnectServerInfo {
	private String serverHost;
	private int serverPort;

	@JsonIgnore
	public boolean isValid() {
		return !Strings.isNullOrEmpty(serverHost)&&serverPort>0&&serverPort<65535;
	}

	@Override
	public String toString() {
		return serverHost+(serverPort!=25565 ? ":"+serverPort : "");
	}
}
