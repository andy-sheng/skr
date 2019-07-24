package com.component.level.view;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.common.view.ex.ExTextView;
import com.component.busilib.R;
import com.component.level.utils.LevelConfigUtils;


// 横排的等级和星星
public class HorizonLevelView extends LinearLayout {

    RelativeLayout mLevelImgArea;
    ImageView mLevelIv;
    ImageView mSubLeveIv;
    ExTextView mLevelTv;

    public HorizonLevelView(Context context) {
        super(context);
        init();
    }

    public HorizonLevelView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public HorizonLevelView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.horizon_level_view_layout, this);
        setOrientation(HORIZONTAL);
        setGravity(Gravity.CENTER_VERTICAL);
        mLevelImgArea = (RelativeLayout) this.findViewById(R.id.level_img_area);
        mLevelIv = (ImageView) this.findViewById(R.id.level_iv);
        mSubLeveIv = (ImageView) this.findViewById(R.id.sub_leve_iv);
        mLevelTv = (ExTextView) this.findViewById(R.id.level_tv);
    }

    public void bindData(int level, int subLevel, String leveDesc) {
        // 主段位
        if (LevelConfigUtils.getImageResoucesLevel(level) != 0) {
            mLevelIv.setImageResource(LevelConfigUtils.getImageResoucesLevel(level));
        }

        // 子段位
        if (LevelConfigUtils.getImageResoucesSubLevel(level, subLevel) != 0) {
            mSubLeveIv.setImageResource(LevelConfigUtils.getImageResoucesSubLevel(level, subLevel));
        }

        mLevelTv.setText(leveDesc + "");
    }

}
