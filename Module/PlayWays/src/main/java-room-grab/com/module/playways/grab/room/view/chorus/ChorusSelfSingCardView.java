package com.module.playways.grab.room.view.chorus;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import com.module.playways.grab.room.model.GrabRoundInfoModel;

/**
 * 合唱 的歌唱者看到的板子
 */
public class ChorusSelfSingCardView extends RelativeLayout {
    public ChorusSelfSingCardView(Context context) {
        super(context);
    }

    public ChorusSelfSingCardView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ChorusSelfSingCardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void playLyric(GrabRoundInfoModel infoModel) {

    }
}
