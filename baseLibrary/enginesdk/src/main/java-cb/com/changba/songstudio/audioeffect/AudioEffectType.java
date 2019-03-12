package com.changba.songstudio.audioeffect;

public enum AudioEffectType {
	BASE_AUDIO_EFFECT_TYPE(0, "原声|留声机|迷幻|流行|摇滚|舞曲|新世纪|R&B|LIVE_XXX");
	
	private int id;
	private String name;
	
	AudioEffectType(int id, String name){
		this.id = id;
		this.name = name;
	}

	public Integer getId() {
		return id;
	}	
	
	public String getName() {
		return name;
	}

}
