package com.component.level.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.common.utils.U;
import com.component.busilib.R;
import com.component.level.utils.LevelConfigUtils;

/**
 * 新的段位视图
 */
public class NormalLevelView2 extends RelativeLayout {

    public final String TAG = "NormalLeveView2";

    ImageView mLevelIv;
    ImageView mSubLeveIv;

    int mainWidth = U.getDisplayUtils().dip2px(99);  // 主段位宽度
    int mainHeight = U.getDisplayUtils().dip2px(86); // 主段位高度
    int subWidth = U.getDisplayUtils().dip2px(44);   // 子段位宽度
    int subHeight = U.getDisplayUtils().dip2px(24);  // 子段位高度
    int sudBottom = U.getDisplayUtils().dip2px(1);   // 子段位距离主段位底部距离


    public NormalLevelView2(Context context) {
        super(context);
        init(context, null);
    }

    public NormalLevelView2(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public NormalLevelView2(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.levelView2);
        mainWidth = typedArray.getDimensionPixelSize(R.styleable.levelView2_mainWidth2, U.getDisplayUtils().dip2px(99));
        mainHeight = typedArray.getDimensionPixelSize(R.styleable.levelView2_mainHeight2, U.getDisplayUtils().dip2px(86));
        subWidth = typedArray.getDimensionPixelSize(R.styleable.levelView2_subWidth2, U.getDisplayUtils().dip2px(15));
        subHeight = typedArray.getDimensionPixelSize(R.styleable.levelView2_subHeight2, U.getDisplayUtils().dip2px(15));
        sudBottom = typedArray.getDimensionPixelSize(R.styleable.levelView2_subBottom2, U.getDisplayUtils().dip2px(1));

        typedArray.recycle();

        inflate(getContext(), R.layout.normal_level_view2_layout, this);
        mLevelIv = (ImageView) this.findViewById(R.id.level_iv);
        mSubLeveIv = (ImageView) this.findViewById(R.id.sub_leve_iv);
    }

    public void bindData(int level, int subLevel) {
        // 主段位
        if (LevelConfigUtils.getImageResoucesLevel(level) != 0) {
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(mainWidth, mainHeight);
            params.addRule(RelativeLayout.CENTER_HORIZONTAL);
            mLevelIv.setLayoutParams(params);
            mLevelIv.setBackground(getResources().getDrawable(LevelConfigUtils.getImageResoucesLevel(level)));
        }

        // 子段位
        if (LevelConfigUtils.getImageResoucesSubLevel(level, subLevel) != 0) {
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(subWidth, subHeight);
            params.addRule(RelativeLayout.CENTER_HORIZONTAL);
            params.addRule(RelativeLayout.ALIGN_BOTTOM, mLevelIv.getId());
            params.setMargins(0, 0, 0, sudBottom);
            mSubLeveIv.setLayoutParams(params);
            mSubLeveIv.setBackground(getResources().getDrawable(LevelConfigUtils.getImageResoucesSubLevel(level, subLevel)));
        }
    }
}
