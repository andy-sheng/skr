package com.module.playways.grab.room.view.chorus;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import com.common.core.userinfo.model.UserInfoModel;
import com.module.playways.grab.room.listener.SVGAListener;
import com.module.rank.R;

/**
 * 合唱开始的板子
 */
public class ChorusSingBeginTipsCardView extends RelativeLayout {

    public ChorusSingBeginTipsCardView(Context context) {
        super(context);
        init();
    }

    public ChorusSingBeginTipsCardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ChorusSingBeginTipsCardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.grab_chorus_sing_begin_tips_card_layout, this);
    }

    public void bindData(UserInfoModel left, UserInfoModel right, SVGAListener listener) {

    }
}
