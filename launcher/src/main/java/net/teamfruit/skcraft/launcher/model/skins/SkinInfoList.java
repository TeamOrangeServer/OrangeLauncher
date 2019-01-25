package net.teamfruit.skcraft.launcher.model.skins;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SkinInfoList {

	public static final int MIN_VERSION = 1;

	private int minimumVersion;
	private Map<String, SkinInfoListNode> skins;

}
