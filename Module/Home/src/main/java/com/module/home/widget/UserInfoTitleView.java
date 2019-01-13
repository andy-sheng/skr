package com.module.home.widget;

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.alibaba.fastjson.JSON;
import com.common.core.avatar.AvatarUtils;
import com.common.core.myinfo.MyUserInfoManager;
import com.common.core.myinfo.event.MyUserInfoEvent;
import com.common.core.userinfo.UserInfoServerApi;
import com.common.core.userinfo.model.UserRankModel;
import com.common.rxretrofit.ApiManager;
import com.common.rxretrofit.ApiMethods;
import com.common.rxretrofit.ApiObserver;
import com.common.rxretrofit.ApiResult;
import com.common.utils.U;
import com.common.view.ex.ExTextView;
import com.facebook.drawee.view.SimpleDraweeView;
import com.module.home.R;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import static com.common.core.userinfo.model.UserRankModel.COUNTRY;
import static com.common.core.userinfo.model.UserRankModel.REGION;

public class UserInfoTitleView extends RelativeLayout {
    SimpleDraweeView mIvUserIcon;
    ExTextView mTvUserName;
    ExTextView mTvUserLevel;
    ExTextView mArea;
    UserInfoServerApi mUserInfoServerApi;

    FrameLayout mFlRankRoot;

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
        this.mUserInfoServerApi = ApiManager.getInstance().createService(UserInfoServerApi.class);
        mIvUserIcon = findViewById(R.id.iv_user_icon);
        mTvUserName = findViewById(R.id.tv_user_name);
        mTvUserLevel = findViewById(R.id.tv_user_level);
        mArea = (ExTextView) findViewById(R.id.area);
        EventBus.getDefault().register(this);

        mFlRankRoot = (FrameLayout) findViewById(R.id.fl_rank_root);
        mFlRankRoot.setVisibility(INVISIBLE);

        setData();
        getOwnInfo();
    }

    public void setData() {
        AvatarUtils.loadAvatarByUrl(mIvUserIcon,
                AvatarUtils.newParamsBuilder(MyUserInfoManager.getInstance().getAvatar())
                        .setCircle(true)
                        .setBorderWidth(U.getDisplayUtils().dip2px(3))
                        .setBorderColor(Color.WHITE)
                        .build());

        mTvUserName.setText(MyUserInfoManager.getInstance().getNickName());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvnet(MyUserInfoEvent.UserInfoChangeEvent userInfoChangeEvent) {
        getOwnInfo();
    }

    public void getOwnInfo() {
        ApiMethods.subscribe(mUserInfoServerApi.getReginRank((int) MyUserInfoManager.getInstance().getUid()), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult result) {
                if (result.getErrno() == 0) {
                    List<UserRankModel> userRankModels = JSON.parseArray(result.getData().getString("seqInfo"), UserRankModel.class);
                    for (UserRankModel userRankModel :
                            userRankModels) {
                        if (!TextUtils.isEmpty(MyUserInfoManager.getInstance().getLocationDesc())) {
                            if (userRankModel.getCategory() == REGION) {
                                mArea.setText(getAreaFromLocation(MyUserInfoManager.getInstance().getLocationDesc()) + "排名");
                                mTvUserLevel.setText(userRankModel.getSeq() + "");
                                mFlRankRoot.setVisibility(VISIBLE);
                                break;
                            }
                        } else {
                            if (userRankModel.getCategory() == COUNTRY) {
                                mArea.setText("全国" + "排名");
                                mTvUserLevel.setText(userRankModel.getSeq() + "");
                                mFlRankRoot.setVisibility(VISIBLE);
                                break;
                            }
                        }
                    }
                }
            }
        });
    }

    public void destroy() {
        EventBus.getDefault().unregister(this);
    }

    private String getAreaFromLocation(String location) {
        String[] strs = location.split("-");
        String area = strs[strs.length - 1];

        return area;
    }
}
