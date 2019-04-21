package com.module.playways.grab.room.view.pk;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import com.module.playways.grab.room.model.GrabRoundInfoModel;


/**
 * PK的歌唱者看到的板子，带歌词
 */
public class PKSelfSingCardView extends RelativeLayout {

    public PKSelfSingCardView(Context context) {
        super(context);
    }

    public PKSelfSingCardView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PKSelfSingCardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void playLyric(GrabRoundInfoModel infoModel) {

    }
}
