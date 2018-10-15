package com.wali.live.common.barrage.view.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.base.utils.CommonUtils;
import com.base.utils.display.DisplayUtils;
import com.live.module.common.R;

/**
 * Created by anping on 17/6/9.
 */
public class VfansInfoUtils {

    public static int getImageResoucesByCharmLevelValue(int charmLevel) {
        int id = R.drawable.live_pet_group_level;
        if (charmLevel > 0 && charmLevel < 2) {
            id = R.drawable.live_pet_group_level;
        } else if (charmLevel < 3) {
            id = R.drawable.live_pet_grade_tomorrow;
        } else if (charmLevel < 4) {
            id = R.drawable.live_pet_grade_trend;
        } else if (charmLevel < 5) {
            id = R.drawable.live_pet_grade_big;
        } else if (charmLevel < 6) {
            id = R.drawable.live_pet_grade_super;
        } else if (charmLevel < 7) {
            id = R.drawable.live_pet_grade_big_cast;
        } else if (charmLevel < 8) {
            id = R.drawable.live_pet_grade_superstar;
        } else if (charmLevel < 9) {
            id = R.drawable.live_pet_grade_king;
        } else {
            id = R.drawable.live_pet_grade_goddess;
        }
        return id;
    }


    public static int getGroupMemberLevelDrawable(int level) {
        int id = R.drawable.live_pet_group_member_level1;
        switch (level) {
            case 2:
                id = R.drawable.live_pet_group_member_level2;
                break;
            case 3:
                id = R.drawable.live_pet_group_member_level3;
                break;
            case 4:
                id = R.drawable.live_pet_group_member_level4;
                break;
            case 5:
                id = R.drawable.live_pet_group_member_level5;
                break;
            case 6:
                id = R.drawable.live_pet_group_member_level6;
                break;
            case 7:
                id = R.drawable.live_pet_group_member_level7;
                break;
            case 8:
                id = R.drawable.live_pet_group_member_level8;
                break;
            case 9:
                id = R.drawable.live_pet_group_member_level9;
                break;
            case 10:
                id = R.drawable.live_pet_group_member_level10;
                break;
        }
        return id;
    }

    public static Bitmap getBitmapByVfansLevel(int petLevel, String medalValue, Context context) {
        TextView view = new TextView(context);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT
                , LinearLayout.LayoutParams.WRAP_CONTENT);
        view.setLayoutParams(lp);
        lp.setMargins(DisplayUtils.dip2px(3f), 0, 0, 0);
        view.setGravity(Gravity.CENTER);
        view.setTextSize(TypedValue.COMPLEX_UNIT_PX, context.getResources().getDimension(R.dimen.margin_24));
        view.setTextColor(context.getResources().getColor(R.color.color_white));
        view.setBackgroundResource(getGroupMemberLevelDrawable(petLevel));
        view.setText(medalValue);
        Bitmap result = CommonUtils.convertViewToBitmap(view);
        view = null;
        return result;
    }
}