package net.teamfruit.skcraft.launcher.discordrpc;

import java.time.Instant;
import java.time.ZoneId;
import java.util.Objects;

import org.apache.commons.lang.StringUtils;

import com.jagrosh.discordipc.entities.RichPresence;

public class DiscordRichPresence {
	/**
	 * The user's current party status.
	 * <br>Example: "Looking to Play", "Playing Solo", "In a Group"
	 *
	 * <p><b>Maximum: 128 characters</b>
	 */
	public String state;

	/**
	 * What the player is currently doing.
	 * <br>Example: "Competitive - Captain's Mode", "In Queue", "Unranked PvP"
	 *
	 * <p><b>Maximum: 128 characters</b>
	 */
	public String details;

	/**
	 * Unix timestamp (seconds) for the start of the game.
	 * <br>Example: 1507665886
	 */
	public long startTimestamp;

	/**
	 * Unix timestamp (seconds) for the start of the game.
	 * <br>Example: 1507665886
	 */
	public long endTimestamp;

	/**
	 * Name of the uploaded image for the large profile artwork.
	 * <br>Example: "default"
	 *
	 * <p><b>Maximum: 32 characters</b>
	 */
	public String largeImageKey;

	/**
	 * Tooltip for the largeImageKey.
	 * <br>Example: "Blade's Edge Arena", "Numbani", "Danger Zone"
	 *
	 * <p><b>Maximum: 128 characters</b>
	 */
	public String largeImageText;

	/**
	 * Name of the uploaded image for the small profile artwork.
	 * <br>Example: "rogue"
	 *
	 * <p><b>Maximum: 32 characters</b>
	 */
	public String smallImageKey;

	/**
	 * Tooltip for the smallImageKey.
	 * <br>Example: "Rogue - Level 100"
	 *
	 * <p><b>Maximum: 128 characters</b>
	 */
	public String smallImageText;

	/**
	 * ID of the player's party, lobby, or group.
	 * <br>Example: "ae488379-351d-4a4f-ad32-2b9b01c91657"
	 *
	 * <p><b>Maximum: 128 characters</b>
	 */
	public String partyId;

	/**
	 * Current size of the player's party, lobby, or group.
	 * <br>Example: 1
	 */
	public int partySize;

	/**
	 * Maximum size of the player's party, lobby, or group.
	 * <br>Example: 5
	 */
	public int partyMax;

	/**
	 * Unique hashed string for Spectate and Join.
	 * Required to enable match interactive buttons in the user's presence.
	 * <br>Example: "MmhuZToxMjMxMjM6cWl3amR3MWlqZA=="
	 *
	 * <p><b>Maximum: 128 characters</b>
	 */
	public String matchSecret;

	/**
	 * Unique hashed string for Spectate button.
	 * This will enable the "Spectate" button on the user's presence if whitelisted.
	 * <br>Example: "MTIzNDV8MTIzNDV8MTMyNDU0"
	 *
	 * <p><b>Maximum: 128 characters</b>
	 */
	public String joinSecret;

	/**
	 * Unique hashed string for chat invitations and Ask to Join.
	 * This will enable the "Ask to Join" button on the user's presence if whitelisted.
	 * <br>Example: "MTI4NzM0OjFpMmhuZToxMjMxMjM="
	 *
	 * <p><b>Maximum: 128 characters</b>
	 */
	public String spectateSecret;

	/**
	 * Marks the matchSecret as a game session with a specific beginning and end.
	 * Boolean value of 0 or 1.
	 * <br>Example: 1
	 */
	public byte instance;

	public RichPresence toRichPresence() {
		return new RichPresence.Builder()
				.setState(state)
				.setDetails(details)
				.setStartTimestamp(startTimestamp<=0?null:Instant.ofEpochSecond(startTimestamp).atZone(ZoneId.systemDefault()).toOffsetDateTime())
				.setEndTimestamp(endTimestamp<=0?null:Instant.ofEpochSecond(endTimestamp).atZone(ZoneId.systemDefault()).toOffsetDateTime())
				.setLargeImage(largeImageKey, StringUtils.length(largeImageText)<2?null:largeImageText)
				.setSmallImage(smallImageKey, StringUtils.length(smallImageText)<2?null:smallImageText)
				.setParty(partyId, partySize, partyMax)
				.setMatchSecret(matchSecret)
				.setJoinSecret(joinSecret)
				.setSpectateSecret(spectateSecret)
				.setInstance(instance!=0)
				.build();
	}

	@Override
	public boolean equals(Object o) {
		if (this==o)
			return true;
		if (!(o instanceof DiscordRichPresence))
			return false;
		DiscordRichPresence presence = (DiscordRichPresence) o;
		return startTimestamp==presence.startTimestamp
				&&endTimestamp==presence.endTimestamp
				&&partySize==presence.partySize
				&&partyMax==presence.partyMax
				&&instance==presence.instance
				&&Objects.equals(state, presence.state)
				&&Objects.equals(details, presence.details)
				&&Objects.equals(largeImageKey, presence.largeImageKey)
				&&Objects.equals(largeImageText, presence.largeImageText)
				&&Objects.equals(smallImageKey, presence.smallImageKey)
				&&Objects.equals(smallImageText, presence.smallImageText)
				&&Objects.equals(partyId, presence.partyId)
				&&Objects.equals(matchSecret, presence.matchSecret)
				&&Objects.equals(joinSecret, presence.joinSecret)
				&&Objects.equals(spectateSecret, presence.spectateSecret);
	}

	@Override
	public int hashCode() {
		return Objects.hash(state, details, startTimestamp, endTimestamp, largeImageKey, largeImageText, smallImageKey,
				smallImageText, partyId, partySize, partyMax, matchSecret, joinSecret, spectateSecret, instance);
	}
}