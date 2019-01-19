package com.module.playways.grab.room.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import com.common.view.ex.ExTextView;
import com.module.rank.R;

/**
 * 其他人主场景收音机
 */
public class OthersSingCardView extends RelativeLayout {

    public ExTextView mSongName;

    public OthersSingCardView(Context context) {
        super(context);
        init();
    }

    public OthersSingCardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public OthersSingCardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        inflate(getContext(),R.layout.grab_others_sing_card_layout,this);
        mSongName = (ExTextView) this.findViewById(R.id.desc_tv);
    }
}
