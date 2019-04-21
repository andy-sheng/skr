package com.module.playways.grab.room.view.chorus;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import com.common.core.userinfo.model.UserInfoModel;
import com.module.playways.grab.room.GrabRoomData;

/**
 * 别人唱歌是，自己看到的板子
 */
public class ChorusOthersSingCardView extends RelativeLayout {
    public ChorusOthersSingCardView(Context context) {
        super(context);
    }

    public ChorusOthersSingCardView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ChorusOthersSingCardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void bindData(GrabRoomData roomData, UserInfoModel left, UserInfoModel right) {

    }
}
