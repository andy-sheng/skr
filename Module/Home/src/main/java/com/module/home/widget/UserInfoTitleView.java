package com.module.home.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import com.common.core.avatar.AvatarUtils;
import com.common.core.myinfo.MyUserInfoManager;
import com.common.core.userinfo.model.UserRankModel;
import com.common.view.ex.ExTextView;
import com.facebook.drawee.view.SimpleDraweeView;
import com.module.home.R;
import com.zq.level.utils.LevelConfigUtils;

public class UserInfoTitleView extends RelativeLayout {

    public final static String TAG = "UserInfoTitleView";

    ImageView mTopUserBg;
    ImageView mLevelBg;
    SimpleDraweeView mIvUserIcon;
    ExTextView mUserLevelTv;

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

    private void init() {
        inflate(getContext(), R.layout.user_info_title_layout, this);
        mTopUserBg = (ImageView) findViewById(R.id.top_user_bg);
        mLevelBg = (ImageView) findViewById(R.id.level_bg);
        mIvUserIcon = (SimpleDraweeView) findViewById(R.id.iv_user_icon);
        mUserLevelTv = (ExTextView) findViewById(R.id.user_level_tv);
    }

    public void showRankView(UserRankModel userRankModel) {
        mTopUserBg.setBackground(getResources().getDrawable(LevelConfigUtils.getHomePageLevelTopBg(userRankModel.getMainRanking())));
        mLevelBg.setBackground(getResources().getDrawable(LevelConfigUtils.getAvatarLevelBg(userRankModel.getMainRanking())));
        mUserLevelTv.setText(userRankModel.getLevelDesc());
    }

    public void showBaseInfo() {
        AvatarUtils.loadAvatarByUrl(mIvUserIcon,
                AvatarUtils.newParamsBuilder(MyUserInfoManager.getInstance().getAvatar())
                        .setCircle(true)
                        .build());
    }
}
