package com.module.playways.grab.room.view.pk;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import com.common.core.userinfo.model.UserInfoModel;
import com.module.playways.grab.room.GrabRoomData;


/**
 * 别人唱歌PK时，自己看到的板子
 */
public class PKOthersSingCardView extends RelativeLayout {

    public PKOthersSingCardView(Context context) {
        super(context);
    }

    public PKOthersSingCardView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PKOthersSingCardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    public void bindData(GrabRoomData roomData, UserInfoModel left, UserInfoModel right) {

    }
}
