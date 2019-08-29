package com.module.home.widget;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.common.core.avatar.AvatarUtils;
import com.common.core.myinfo.MyUserInfoManager;
import com.component.person.model.UserRankModel;
import com.common.view.DebounceViewClickListener;
import com.common.view.ex.ExTextView;
import com.facebook.drawee.view.SimpleDraweeView;
import com.module.home.R;
import com.component.level.utils.LevelConfigUtils;

public class UserInfoTitleView extends RelativeLayout {

    public final String TAG = "UserInfoTitleView";

    ImageView mTopUserBg;
    ImageView mLevelBg;
    SimpleDraweeView mIvUserIcon;
    ExTextView mUserLevelTv;

    UserTitleClickListener mListener;

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

        // TODO: 2019/4/2 因为 mLevelBg盖在头像上
        mLevelBg.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                if (mListener != null) {
                    mListener.onClickAvatar();
                }
            }
        });
    }

    public void setListener(UserTitleClickListener userTitleClickListener) {
        this.mListener = userTitleClickListener;
    }

    public void showRankView(UserRankModel userRankModel) {
        mTopUserBg.setBackground(getResources().getDrawable(LevelConfigUtils.getHomePageLevelTopBg(userRankModel.getMainRanking())));
        mLevelBg.setBackground(getResources().getDrawable(LevelConfigUtils.getAvatarLevelBg(userRankModel.getMainRanking())));
        mUserLevelTv.setTextColor(Color.parseColor(LevelConfigUtils.getHomePageLevelTextColor(userRankModel.getMainRanking())));
        mUserLevelTv.setText(userRankModel.getLevelDesc());
    }

    public ImageView getTopUserBg() {
        return mTopUserBg;
    }

    public void showBaseInfo() {
        AvatarUtils.loadAvatarByUrl(mIvUserIcon,
                AvatarUtils.newParamsBuilder(MyUserInfoManager.getInstance().getAvatar())
                        .setCircle(true)
                        .build());
    }

    public interface UserTitleClickListener {
        void onClickAvatar();
    }
}
