package com.module.playways.grab.room.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import com.common.view.ex.ExTextView;
import com.module.rank.R;

/**
 * 你的主场景歌词
 */
public class SelfSingCardView extends RelativeLayout {

    public ExTextView mSongName;

    public SelfSingCardView(Context context) {
        super(context);
        init();
    }

    public SelfSingCardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SelfSingCardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        inflate(getContext(),R.layout.grab_self_sing_card_layout,this);
        mSongName = (ExTextView) this.findViewById(R.id.desc_tv);
    }
}
