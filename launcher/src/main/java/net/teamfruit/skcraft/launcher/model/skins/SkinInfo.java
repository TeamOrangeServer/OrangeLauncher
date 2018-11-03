package net.teamfruit.skcraft.launcher.model.skins;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SkinInfo {
	private String newsURL;
	private String tipsURL;
	private String supportURL;
	private String langURL;
	private String backgroundURL;
	private boolean showList;
	private String selectModPack;
	private String loginModPack;
}