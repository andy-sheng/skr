package com.module.playways.grab.room.view.pk;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import com.common.core.userinfo.model.UserInfoModel;
import com.module.playways.grab.room.listener.SVGAListener;

/**
 * PK开始的板子
 */
public class PKSingBeginTipsCardView extends RelativeLayout {

    public PKSingBeginTipsCardView(Context context) {
        super(context);
    }

    public PKSingBeginTipsCardView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PKSingBeginTipsCardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void bindData(UserInfoModel left, UserInfoModel right, SVGAListener listener) {

    }
}
