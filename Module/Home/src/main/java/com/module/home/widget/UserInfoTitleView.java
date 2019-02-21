package com.module.home.widget;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.alibaba.android.arouter.launcher.ARouter;
import com.alibaba.fastjson.JSON;
import com.common.base.BaseActivity;
import com.common.base.BaseFragment;
import com.common.base.FragmentDataListener;
import com.common.core.avatar.AvatarUtils;
import com.common.core.myinfo.MyUserInfoManager;
import com.common.core.myinfo.event.MyUserInfoEvent;
import com.common.core.userinfo.UserInfoServerApi;
import com.common.core.userinfo.model.UserRankModel;
import com.common.rxretrofit.ApiManager;
import com.common.rxretrofit.ApiMethods;
import com.common.rxretrofit.ApiObserver;
import com.common.rxretrofit.ApiResult;
import com.common.utils.FragmentUtils;
import com.common.utils.U;
import com.common.view.ex.ExTextView;
import com.facebook.drawee.view.SimpleDraweeView;
import com.jakewharton.rxbinding2.view.RxView;
import com.module.RouterConstants;
import com.module.home.R;
import com.module.home.fragment.GameFragment;
import com.module.rank.IRankingModeService;
import com.zq.level.utils.LevelConfigUtils;
import com.zq.live.proto.Common.ESex;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import io.reactivex.functions.Consumer;

import static com.common.core.userinfo.model.UserRankModel.COUNTRY;
import static com.common.core.userinfo.model.UserRankModel.REGION;

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
