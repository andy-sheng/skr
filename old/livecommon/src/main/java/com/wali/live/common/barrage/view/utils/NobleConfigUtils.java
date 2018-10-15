package com.wali.live.common.barrage.view.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.DrawableRes;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.base.utils.CommonUtils;
import com.base.utils.display.DisplayUtils;
import com.live.module.common.R;
import com.mi.live.data.user.User;

/**
 * Created by liuyanyan on 2018/3/24.
 *
 * @module 贵族特权配置
 * @description 获取不同贵族级别对应的需要配置的色值 icon等
 */
public class NobleConfigUtils {

    public static int getImageResoucesByNobelLevelInBarrage(int nobelLevel) {
        switch (nobelLevel) {
            case User.NOBLE_LEVEL_TOP:
                return R.drawable.nobleicon2_wangzhe;
            case User.NOBLE_LEVEL_SECOND:
                return R.drawable.nobleicon2_gongjue;
            case User.NOBLE_LEVEL_THIRD:
                return R.drawable.nobleicon2_houjue;
            case User.NOBLE_LEVEL_FOURTH:
                return R.drawable.nobleicon2_bojue;
            case User.NOBLE_LEVEL_FIFTH:
                return R.drawable.nobleicon2_zijue;
            default:
                return 0;
        }
    }

    public static int getImageResoucesByNobelLevelInAvatar(int nobelLevel) {
        switch (nobelLevel) {
            case User.NOBLE_LEVEL_TOP:
                return R.drawable.nobleicon_wang;
            case User.NOBLE_LEVEL_SECOND:
                return R.drawable.nobleicon_gong;
            case User.NOBLE_LEVEL_THIRD:
                return R.drawable.nobleicon_hou;
            case User.NOBLE_LEVEL_FOURTH:
                return R.drawable.nobleicon_bo;
            case User.NOBLE_LEVEL_FIFTH:
                return R.drawable.nobleicon_zi;
            default:
                return 0;
        }
    }

    /*
     * 用户名片，头像外围的徽章
     */
    public static int getImageResourceByNobleLevelInUserCard(int nobelLevel) {
        switch (nobelLevel) {
            case User.NOBLE_LEVEL_TOP:
                return R.drawable.noble_wangzhe;
            case User.NOBLE_LEVEL_SECOND:
                return R.drawable.noble_gongjue;
            case User.NOBLE_LEVEL_THIRD:
                return R.drawable.noble_houjue;
            case User.NOBLE_LEVEL_FOURTH:
                return R.drawable.noble_bojue;
            case User.NOBLE_LEVEL_FIFTH:
            default:
                return R.drawable.noble_zijue;
        }
    }

    public static Bitmap getBitmapByDrawableResId(@DrawableRes int drawableResId, Context context) {
        TextView view = new TextView(context);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT
                , LinearLayout.LayoutParams.WRAP_CONTENT);
        view.setLayoutParams(lp);
        lp.setMargins(DisplayUtils.dip2px(3f), 0, 0, 0);
        view.setGravity(Gravity.CENTER);
        view.setTextSize(TypedValue.COMPLEX_UNIT_PX, context.getResources().getDimension(R.dimen.margin_24));
        view.setTextColor(context.getResources().getColor(R.color.color_white));
        view.setBackgroundResource(drawableResId);
        //TODO 文案待定
//        view.setText(medalValue);
        return CommonUtils.convertViewToBitmap(view);
    }

    public static int getNobleGoldenBackgroundByNobelLevel(int nobleLevel) {
        switch (nobleLevel) {
            case User.NOBLE_LEVEL_TOP:
                return R.drawable.avatar_item_noble_golden_king;
            case User.NOBLE_LEVEL_SECOND:
                return R.drawable.avatar_item_noble_golden_duke;
            case User.NOBLE_LEVEL_THIRD:
                return R.drawable.avatar_item_noble_golden_marquis;
            default:
                return 0;
        }
    }

    public static int getNobleGoldenFlybarrageBackgroundByNobelLevel(int nobleLevel) {
        switch (nobleLevel) {
            case User.NOBLE_LEVEL_TOP:
                return R.drawable.flybarrage_noble_golden_king;
            case User.NOBLE_LEVEL_SECOND:
                return R.drawable.flybarrage_noble_golden_duke;
            case User.NOBLE_LEVEL_THIRD:
                return R.drawable.flybarrage_noble_golden_marquis;
            default:
                return 0;
        }

    }

}
