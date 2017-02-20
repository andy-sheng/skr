package com.base.utils.layout;

import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by chengsimin on 2016/12/5.
 */

public class LayoutUtils {
    public static void setMargins(@NonNull View v, int l, int t, int r, int b) {
        if (v.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
            p.setMargins(l, t, r, b);
            v.setLayoutParams(p);
        }
    }
}
