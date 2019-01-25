package net.teamfruit.skcraft.launcher.mcpinger;

import java.io.IOException;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * References:
 * http://wiki.vg/Server_List_Ping
 * https://gist.github.com/thinkofdeath/6927216
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class PingResult {

	@JsonDeserialize(using = Description.DescriptionDeserializer.class)
	private Description description;
	private Players players;
	private Version version;
	private String favicon;

	@JsonIgnoreProperties(ignoreUnknown = true)
	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public static class Description {
		private String text;

		public static class DescriptionDeserializer extends JsonDeserializer<Description> {
			@Override
			public Description deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
				final JsonToken curr = p.getCurrentToken();
				if (curr==JsonToken.VALUE_STRING)
					return new Description(p.getText());
				return p.readValueAs(Description.class);
			}
		}
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	@Data
	public static class Players {
		private int max;
		private int online;
		private List<Player> sample;
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	@Data
	public static class Player {
		private String name;
		private String id;
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	@Data
	public static class Version {
		private String name;
		private int protocol;
	}

}
