package com.module.playways.grab.room.view.chorus;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import com.module.playways.grab.room.model.GrabRoundInfoModel;
import com.module.rank.R;

/**
 * 合唱的歌唱者看到的板子
 */
public class ChorusSelfSingCardView extends RelativeLayout {
    public ChorusSelfSingCardView(Context context) {
        super(context);
        init();
    }

    public ChorusSelfSingCardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ChorusSelfSingCardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.grab_chorus_self_sing_card_layout, this);
    }

    public void playLyric(GrabRoundInfoModel infoModel) {

    }
}
