package com.wali.live.common.view;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.base.utils.display.DisplayUtils;
import com.mi.live.data.R;

import java.util.ArrayList;
import java.util.List;

/**
 * 小图标容器，横向
 * Created by guoxiao on 17-4-28.
 */

public class LevelIconsLayout extends LinearLayout {
    private List<TextView> mViews = new ArrayList<>();

    public LevelIconsLayout(Context context) {
        super(context);
        setOrientation(HORIZONTAL);
    }

    public LevelIconsLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setOrientation(HORIZONTAL);
    }

    public LevelIconsLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setOrientation(HORIZONTAL);
    }

    public void addIcon(TextView view) {
        mViews.add(view);
        addView(view);
    }

    public void addIconsWithClear(List<TextView> views) {
        mViews.clear();
        mViews.addAll(views);
        removeAllViews();
        for (TextView view : views) {
            addView(view);
        }
    }

    public static TextView getDefaultTextView(Context context) {
        TextView view = new TextView(context);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT
                , LayoutParams.WRAP_CONTENT);
        lp.setMargins(DisplayUtils.dip2px(3f), 0, 0, 0);
        view.setLayoutParams(lp);
        view.setCompoundDrawablePadding(DisplayUtils.dip2px(2));
        view.setGravity(Gravity.CENTER);
        view.setPadding(context.getResources().getDimensionPixelSize(R.dimen.lvl_left_padding), 0
                , context.getResources().getDimensionPixelSize(R.dimen.lvl_right_padding), 0);
        view.setTextSize(TypedValue.COMPLEX_UNIT_PX, context.getResources().getDimension(R.dimen.lvl_text_size));
        view.setTextColor(context.getResources().getColor(R.color.lvl_txt_color));
        return view;
    }


}
