package com.component.notification;

import android.content.Context;
import android.support.constraint.ConstraintLayout;
import android.util.AttributeSet;
import android.widget.TextView;

import com.common.core.userinfo.model.UserInfoModel;
import com.component.busilib.R;
import com.component.busilib.view.AvatarView;

public class GiftMallNotifyView extends ConstraintLayout {

    AvatarView mAvatarIv;
    TextView mDescTv;

    public GiftMallNotifyView(Context context) {
        super(context);
        init();
    }

    public GiftMallNotifyView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public GiftMallNotifyView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.gift_mall_notification_view_layout, this);
        mDescTv = findViewById(R.id.desc_tv);
        mAvatarIv = findViewById(R.id.avatar_iv);
    }

    public void bindData(String desc, UserInfoModel userInfoModel) {
        mDescTv.setText(desc);
        mAvatarIv.bindData(userInfoModel);
    }
}
