package com.zq.mediaengine.filter.audio;

// TODO: CB的Sox库是改过的，目前直接使用他们的参数效果不对
public class ZqAudioEffectFilter extends KSYAudioEffectFilter {
    private static final String TAG = "ZqAudioEffectFilter";
    private static final boolean VERBOSE = false;

    public static final int EFFECT_TYPE_NONE = 0;
    public static final int EFFECT_TYPE_ORIGIN = 1;
    public static final int EFFECT_TYPE_RNB = 2;
    public static final int EFFECT_TYPE_ROCK = 3;
    public static final int EFFECT_TYPE_POPULAR = 4;
    public static final int EFFECT_TYPE_DANCE = 5;
    public static final int EFFECT_TYPE_NEW_CENT = 6;

    private int mEffectType = EFFECT_TYPE_NONE;

    public ZqAudioEffectFilter() {
        super(AUDIO_EFFECT_TYPE_NONE);
    }

    private String[] getReverbArgv(int type) {
        String[] argv = null;
        switch (type) {
            case EFFECT_TYPE_RNB:
                argv = new String[]{"62", "15", "64", "83", "77", "4"};
                break;
            case EFFECT_TYPE_ROCK:
                argv = new String[]{"89", "26", "85", "98", "76", "7"};
                break;
            case EFFECT_TYPE_POPULAR:
                argv = new String[]{"81", "50", "95", "100", "84", "3"};
                break;
            case EFFECT_TYPE_DANCE:
                argv = new String[]{"35", "20", "84", "100", "66", "3"};
                break;
            case EFFECT_TYPE_NEW_CENT:
                argv = new String[]{"78", "42", "92", "89", "95", "5"};
                break;
            case EFFECT_TYPE_ORIGIN:
            default:
                break;
        }
        return argv;
    }

    @Override
    public void setAudioEffectType(int type) {
        if (mEffectType == type) {
            return;
        }

        mEffectType = type;
        if (type == EFFECT_TYPE_NONE) {
            super.setAudioEffectType(AUDIO_EFFECT_TYPE_NONE);
            return;
        }

        super.setAudioEffectType(Audio_EFFECT_TYPE_USER_DEFINE);
        // compressor
        String[] compandArgv = {"0.02,0.04", "3:-100,-75,-100,-20,-20,-15,-15,-10,-11,-6,-9,0,-6", "0", "-100", "0"};
        addEffect("compand", compandArgv.length, compandArgv);
        // equalizer
        String[] equalizerArgv = {"1600", "2.5q", "2"};
        addEffect("equalizer", equalizerArgv.length, equalizerArgv);
        // reverb
        String[] reverbArgv = getReverbArgv(type);
        if (reverbArgv != null) {
            addEffect("reverb", reverbArgv.length, reverbArgv);
        }
        applyEffects();
    }

    @Override
    public int getAudioEffectType() {
        return mEffectType;
    }
}
