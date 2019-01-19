package com.module.playways.grab.room.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import com.common.view.ex.ExTextView;
import com.module.rank.R;

/**
 * xxx获得演唱机会
 * 轮到你唱了
 * 演唱提示cardview
 */
public class SingBeginTipsCardView extends RelativeLayout {

    public ExTextView mDescTv;

    public SingBeginTipsCardView(Context context) {
        super(context);
        init();
    }

    public SingBeginTipsCardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SingBeginTipsCardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        inflate(getContext(),R.layout.grab_sing_begin_tips_card_layout,this);
        mDescTv = (ExTextView) this.findViewById(R.id.desc_tv);
    }
}
