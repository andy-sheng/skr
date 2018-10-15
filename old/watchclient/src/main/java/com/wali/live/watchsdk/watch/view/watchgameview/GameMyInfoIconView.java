package com.wali.live.watchsdk.watch.view.watchgameview;

import android.content.Context;
import android.util.AttributeSet;

import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.component.view.MyInfoIconView;

/**
 * Created by zhujianning on 18-8-17.
 */

public class GameMyInfoIconView extends MyInfoIconView {
    public GameMyInfoIconView(Context context) {
        super(context);
    }

    public GameMyInfoIconView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public GameMyInfoIconView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void init(Context context) {
        inflate(context, R.layout.game_my_info_view, this);
        bindView();
    }
}
