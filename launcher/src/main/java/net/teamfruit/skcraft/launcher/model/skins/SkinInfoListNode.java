package net.teamfruit.skcraft.launcher.model.skins;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SkinInfoListNode {
	private String title;
	private String url;
}
