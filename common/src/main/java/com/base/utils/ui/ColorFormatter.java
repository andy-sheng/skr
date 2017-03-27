package com.base.utils.ui;

import android.graphics.Color;

/**
 * Created by lan on 17/3/27.
 */
public class ColorFormatter {
    private static final char[] HEX = "0123456789abcdef".toCharArray();

    public static String toFullHexString(int color) {
        char[] chs = new char[6];
        for (int i = 0; i < chs.length; i++) {
            chs[chs.length - 1 - i] = HEX[(color >> (i << 2)) & 0xf];
        }
        return new String(chs);
    }

    public static int toHexColor(int color) {
        return Color.parseColor("#" + toFullHexString(color));
    }
}
