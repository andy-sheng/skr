package com.module.home.widget;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import com.common.core.avatar.AvatarUtils;
import com.common.core.myinfo.MyUserInfoManager;
import com.common.utils.U;
import com.common.view.ex.ExTextView;
import com.facebook.drawee.view.SimpleDraweeView;
import com.module.home.R;
public class UserInfoTitleView extends RelativeLayout {
    SimpleDraweeView mIvUserIcon;
    ExTextView mTvUserName;
    ExTextView mTvUserLevel;
    public UserInfoTitleView(Context context) {
        this(context, null);
    }

    public UserInfoTitleView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public UserInfoTitleView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init(){
        inflate(getContext(), R.layout.user_info_title_layout, this);

        mIvUserIcon = findViewById(R.id.iv_user_icon);
        mTvUserName = findViewById(R.id.tv_user_name);
        mTvUserLevel = findViewById(R.id.tv_user_level);

        AvatarUtils.loadAvatarByUrl(mIvUserIcon,
                AvatarUtils.newParamsBuilder(MyUserInfoManager.getInstance().getAvatar())
                        .setWebpFormat(true)
                        .setCircle(true)
                        .setCornerRadius(200)
                        .setBorderWidth(U.getDisplayUtils().dip2px(3))
                        .setBorderColor(Color.WHITE)
                        .build());

        mTvUserName.setText(MyUserInfoManager.getInstance().getNickName());
        mTvUserLevel.setText("铂金唱将");
    }


}
