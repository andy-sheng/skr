package com.wali.live.common.barrage.view.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.annotation.DrawableRes;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.base.log.MyLog;
import com.base.utils.CommonUtils;
import com.base.utils.display.DisplayUtils;
import com.live.module.common.R;

import java.lang.reflect.Field;

/**
 * Created by zyh on 2017/12/27.
 */

public class CommentVipUtils {
    private static final String TAG = "CommentVipUtils";
    public static final int MAX_LEVEL_IMAGE_NO = 7;//最大等级图片，超过之后使用live_vip_7图片
    private static final String LEVEL_NAME_PREFIX = "live_vip_";
    private static final String LEVEL_DISABLE_SUFFIX = "_disable";

    /**
     * 获取VIP等级对应资源ID
     */
    public static int getLevelBadgeResId(int level, boolean isFrozen, boolean showFrozen) {
        if (level <= 0 || (isFrozen && !showFrozen)) {
            return -1;
        }
        int index = level > MAX_LEVEL_IMAGE_NO ? MAX_LEVEL_IMAGE_NO : level;
        String rName = LEVEL_NAME_PREFIX + index;
        if (isFrozen && showFrozen) {
            rName = rName + LEVEL_DISABLE_SUFFIX;
        }
        return getResId(rName, R.drawable.class);
    }

    /**
     * @param resName 资源名称
     * @param c       资源类型
     *                eg:getResId("icon", Drawable.class);
     */
    public static int getResId(String resName, Class<?> c) {
        try {
            Field idField = c.getDeclaredField(resName);
            return idField.getInt(idField);
        } catch (Exception e) {
            e.printStackTrace();
            MyLog.w(TAG, e);
            return -1;
        }
    }

    public static Bitmap getBitmapByResId(Context context, @DrawableRes int resId) {
        TextView view = new TextView(context);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT
                , LinearLayout.LayoutParams.WRAP_CONTENT);
        view.setLayoutParams(lp);
        lp.setMargins(DisplayUtils.dip2px(3f), 0, 0, 0);
        view.setLayoutParams(lp);
        view.setCompoundDrawablePadding(DisplayUtils.dip2px(2));
        view.setGravity(Gravity.CENTER);
        view.setPadding(context.getResources().getDimensionPixelSize(R.dimen.lvl_left_padding), 0
                , context.getResources().getDimensionPixelSize(R.dimen.lvl_right_padding), 0);
        view.setTextSize(TypedValue.COMPLEX_UNIT_PX, context.getResources().getDimension(R.dimen.lvl_text_size));
        view.setTextColor(context.getResources().getColor(R.color.lvl_txt_color));
        view.setBackgroundResource(resId);
        return CommonUtils.convertViewToBitmap(view);
    }
}
