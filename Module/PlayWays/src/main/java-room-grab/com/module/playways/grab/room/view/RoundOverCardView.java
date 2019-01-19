package com.module.playways.grab.room.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import com.common.view.ex.ExTextView;
import com.module.rank.R;

/**
 * 轮次结束
 */
public class RoundOverCardView extends RelativeLayout {

    public ExTextView mDescTv;

    public RoundOverCardView(Context context) {
        super(context);
        init();
    }

    public RoundOverCardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public RoundOverCardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        inflate(getContext(),R.layout.grab_round_over_card_layout,this);
        mDescTv = (ExTextView) this.findViewById(R.id.desc_tv);
    }
}
