package com.changba.songstudio.audioeffect;

import java.util.ArrayList;

public enum AudioEffectStyleEnum {

	ORIGINAL(0, "原声"),
	GRAMOPHONE(1, "留声机"),
	RNB(5, "R&B"),
	ROCK(6, "摇滚"),
	POPULAR(7, "流行"),
	DANCE(8, "舞曲"),
	NEW_CENT(9, "新世纪"),
	LIVE_ORIGINAL(10, "直播原声"),
	LIVE_MAGIC(11, "直播魔音"),
	LIVE_SIGNER(12, "直播唱将"),
	LIVE_PROFFESSION(13, "直播专业"),
	LIVE_GOD(14, "直播顶尖");

	private int id;
	private String name;

	private AudioEffectStyleEnum(int id, String name) {
		this.id = id;
		this.name = name;
	}

	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

}
