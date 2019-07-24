package com.component.mediaengine.framework;

/**
 * @hide
 */

public class CredtpModel {
    //beauty type
    public static final int BEAUTY_DENOISE_FILTER = 1;
    public static final int BEAUTY_SOFT_FILTER = 2;
    public static final int BEAUTY_ILLUSION_FILTER = 3;
    public static final int BEAUTY_SKIN_WHITEN_FILTER = 4;
    public static final int BEAUTY_SMOOTH_FILTER = 5;
    public static final int BEAUTY_SHARPEN_FILTER = 6;
    public static final int BEAUTY_SOFT_EXT_FILTER = 7;
    public static final int BEAUTY_SKIN_DETECT_FILTER = 8;
    public static final int BEAUTY_GRIND_FACE_FILTER = 9;
    public static final int BEAUTY_LOOK_UP_FILTER = 10;
    public static final int BEAUTY_GRIND_SIMPLE_FILTER = 11;
    public static final int BEAUTY_ADJ_SKIN_COLOR_FILTER = 12;
    public static final int BEAUTY_GRIND_ADVANCE_FILTER = 13;
    public static final int BEAUTY_1977_FILTER = 14;
    public static final int BEAUTY_AMARO_FILTER = 15;
    public static final int BEAUTY_BRANNAN_FILTER = 16;
    public static final int BEAUTY_EARLY_BIRD_FILTER = 17;
    public static final int BEAUTY_HEFE_FILTER = 18;
    public static final int BEAUTY_HUDSON_FILTER = 19;
    public static final int BEAUTY_INK_FILTER = 20;
    public static final int BEAUTY_LOMO_FILTER = 21;
    public static final int BEAUTY_LORD_KELVIN_FILTER = 22;
    public static final int BEAUTY_NASHVILLE_FILTER = 23;
    public static final int BEAUTY_RISE_FILTER = 24;
    public static final int BEAUTY_SIERRA_FILTER = 25;
    public static final int BEAUTY_SUTRO_FILTER = 26;
    public static final int BEAUTY_TOASTER_FILTER = 27;
    public static final int BEAUTY_VALENCIA_FILTER = 28;
    public static final int BEAUTY_WALDEN_FILTER = 29;
    public static final int BEAUTY_XPROLL_FILTER = 30;
    public static final int BEAUTY_SHAKE_COLOR = 31;
    public static final int BEAUTY_SHAKE_ZOOM = 32;
    public static final int BEAUTY_SHAKE_WAVE = 33;
    public static final int BEAUTY_SHAKE_70S = 34;
    public static final int BEAUTY_SHAKE_XSINGLE = 35;
    public static final int BEAUTY_SHAKE_ILLUSION = 36;

    private int type;
    private String key;
    private String body;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
