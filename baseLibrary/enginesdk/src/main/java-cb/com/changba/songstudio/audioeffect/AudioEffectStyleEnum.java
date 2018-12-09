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

	public static int getAudioEffectType(AudioEffectStyleEnum audioEffectStyleEnum){
		return AudioEffectType.BASE_AUDIO_EFFECT_TYPE.getId();
	}
	
	public static ArrayList<Integer> getVocalFilterChain(AudioEffectStyleEnum audioEffectStyleEnum) {
		ArrayList<Integer> targetFilterChain = new ArrayList<Integer>();
		switch (audioEffectStyleEnum) {
		case ORIGINAL:
		case LIVE_ORIGINAL:
		case GRAMOPHONE:
			targetFilterChain.add(AudioEffectFilterType.VocalAGCEffectFilter.getId());
			targetFilterChain.add(AudioEffectFilterType.CompressorFilter.getId());
			targetFilterChain.add(AudioEffectFilterType.EqualizerFilter.getId());
			targetFilterChain.add(AudioEffectFilterType.Mono2StereoFilter.getId());
			targetFilterChain.add(AudioEffectFilterType.VocalVolumeAdjustFilter.getId());
			break;
		case RNB:
		case ROCK:
		case POPULAR:
		case DANCE:
		case NEW_CENT:
			targetFilterChain.add(AudioEffectFilterType.VocalAGCEffectFilter.getId());
			targetFilterChain.add(AudioEffectFilterType.CompressorFilter.getId());
			targetFilterChain.add(AudioEffectFilterType.EqualizerFilter.getId());
			targetFilterChain.add(AudioEffectFilterType.ReverbEchoFilter.getId());
			targetFilterChain.add(AudioEffectFilterType.VocalVolumeAdjustFilter.getId());
			break;
		case LIVE_MAGIC:
		case LIVE_SIGNER:
		case LIVE_PROFFESSION:
		case LIVE_GOD:
			targetFilterChain.add(AudioEffectFilterType.CompressorFilter.getId());
			targetFilterChain.add(AudioEffectFilterType.EqualizerFilter.getId());
			targetFilterChain.add(AudioEffectFilterType.ReverbEchoFilter.getId());
			targetFilterChain.add(AudioEffectFilterType.VocalVolumeAdjustFilter.getId());
			break;
		default:
			break;
		}
		return targetFilterChain;
	}
	public static ArrayList<Integer> getAccompanyFilterChain(AudioEffectStyleEnum audioEffectStyleEnum) {
		ArrayList<Integer> targetFilterChain = new ArrayList<Integer>();
		switch (audioEffectStyleEnum) {
		case ORIGINAL:
		case LIVE_ORIGINAL:
		case GRAMOPHONE:
		case RNB:
		case ROCK:
		case POPULAR:
		case DANCE:
		case NEW_CENT:
			targetFilterChain.add(AudioEffectFilterType.AccompanyAGCEffectFilter.getId());
			targetFilterChain.add(AudioEffectFilterType.AccompanyVolumeAdjustFilter.getId());
			break;
		case LIVE_MAGIC:
		case LIVE_SIGNER:
		case LIVE_PROFFESSION:
		case LIVE_GOD:
			targetFilterChain.add(AudioEffectFilterType.AccompanyVolumeAdjustFilter.getId());
			break;
		default:
			break;
		}
		return targetFilterChain;
	}
	public static ArrayList<Integer> getMixPostFilterChain(AudioEffectStyleEnum audioEffectStyleEnum) {
		ArrayList<Integer> targetFilterChain = new ArrayList<Integer>();
		switch (audioEffectStyleEnum) {
		case ORIGINAL:
		case LIVE_ORIGINAL:
		case GRAMOPHONE:
		case RNB:
		case ROCK:
		case POPULAR:
		case DANCE:
		case NEW_CENT:
			targetFilterChain.add(AudioEffectFilterType.FadeOutEffectFilter.getId());
			break;
		case LIVE_MAGIC:
		case LIVE_SIGNER:
		case LIVE_PROFFESSION:
		case LIVE_GOD:
			break;
		default:
			break;
		}
		return targetFilterChain;
	}
	
	public static AudioEffectStyleEnum getEnum(int eqaulizerId) {
		switch (eqaulizerId) {
		case 0:
			return ORIGINAL;
		case 1:
			return GRAMOPHONE;
		case 5:
			return RNB;
		case 6:
			return ROCK;
		case 7:
			return POPULAR;
		case 8:
			return DANCE;
		case 9:
			return NEW_CENT;
		case 10:
			return LIVE_ORIGINAL;
		case 11:
			return LIVE_MAGIC;
		case 12:
			return LIVE_SIGNER;
		case 13:
			return LIVE_PROFFESSION;
		case 14:
			return LIVE_GOD;
		default:
			return ORIGINAL;
		}
	}
}
