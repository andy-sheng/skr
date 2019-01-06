package com.zq.level.view;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.common.utils.U;
import com.common.view.ex.ExTextView;
import com.component.busilib.R;


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

        bindData(4, 3);
    }

    public void bindData(int totalStar, int selectStar) {
        for (int i = 0; i < totalStar ; i++) {
            // 左边的星星
            ImageView imageView = new ImageView(getContext());
            RelativeLayout.LayoutParams rl = new RelativeLayout.LayoutParams(U.getDisplayUtils().dip2px(14), U.getDisplayUtils().dip2px(14));
            rl.setMargins(U.getDisplayUtils().dip2px(6), 0, 0, 0);
            if (i < selectStar) {
                imageView.setBackground(ContextCompat.getDrawable(U.app(), R.drawable.zhanji_daxingxing_dianliang));
            } else {
                imageView.setBackground(ContextCompat.getDrawable(U.app(), R.drawable.zhanji_xiaoxingxing_zhihui));
            }
            imageView.setLayoutParams(rl);
            addView(imageView);
        }
    }

}
