package com.zq.person.fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;

import com.common.base.BaseFragment;
import com.common.core.avatar.AvatarUtils;
import com.common.core.myinfo.MyUserInfoManager;
import com.common.core.userinfo.UserInfoManager;
import com.common.core.userinfo.UserInfoModel;
import com.common.core.userinfo.event.RelationChangeEvent;
import com.common.flowlayout.FlowLayout;
import com.common.flowlayout.TagAdapter;
import com.common.flowlayout.TagFlowLayout;
import com.common.image.fresco.BaseImageView;
import com.common.utils.U;
import com.common.view.ex.ExImageView;
import com.common.view.ex.ExTextView;
import com.component.busilib.R;
import com.jakewharton.rxbinding2.view.RxView;
import com.zq.person.presenter.OtherPersonPresenter;
import com.zq.person.view.IOtherPersonView;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.concurrent.TimeUnit;

import io.reactivex.functions.Consumer;


public class OtherPersonFragment extends BaseFragment implements IOtherPersonView {

    public static final String BUNDLE_USER_MODEL = "budle_user_model";

    public static final int RELATION_FOLLOWED = 1; // 已关注关系
    public static final int RELATION_UN_FOLLOW = 2; // 未关注关系

    private String[] mVals = new String[]{"北京市/昌平", "22岁", "粉丝/345"};

    RelativeLayout mPersonMainContainner;
    BaseImageView mAvatarIv;
    ExImageView mBackIv;
    ExTextView mShareTv;
    ExTextView mNameTv;
    ExTextView mSignTv;
    TagFlowLayout mFlowlayout;
    ExTextView mFollowTv;
    ExTextView mMessageTv;

    OtherPersonPresenter mOtherPersonPresenter;

    UserInfoModel mUserInfoModel;

    @Override
    public int initView() {
        return R.layout.other_person_fragment_layout;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {

        mPersonMainContainner = (RelativeLayout) mRootView.findViewById(R.id.person_main_containner);
        mAvatarIv = (BaseImageView) mRootView.findViewById(R.id.avatar_iv);
        mBackIv = (ExImageView) mRootView.findViewById(R.id.back_iv);
        mShareTv = (ExTextView) mRootView.findViewById(R.id.share_tv);
        mNameTv = (ExTextView) mRootView.findViewById(R.id.name_tv);
        mSignTv = (ExTextView) mRootView.findViewById(R.id.sign_tv);
        mFlowlayout = (TagFlowLayout) mRootView.findViewById(R.id.flowlayout);
        mFollowTv = (ExTextView) mRootView.findViewById(R.id.follow_tv);
        mMessageTv = (ExTextView) mRootView.findViewById(R.id.message_tv);

        mOtherPersonPresenter = new OtherPersonPresenter(this);
        addPresent(mOtherPersonPresenter);

        Bundle bundle = getArguments();
        if (bundle != null) {
            mUserInfoModel = (UserInfoModel) bundle.getSerializable(BUNDLE_USER_MODEL);
            mOtherPersonPresenter.getUserInfo(mUserInfoModel.getUserId());
            mOtherPersonPresenter.getRelation(mUserInfoModel.getUserId());
        }

        // TODO: 2018/12/26 可能会变，先写死
        mFlowlayout.setAdapter(new TagAdapter<String>(mVals) {
            @Override
            public View getView(FlowLayout parent, int position, String o) {
                ExTextView tv = (ExTextView) LayoutInflater.from(getContext()).inflate(R.layout.tag_textview,
                        mFlowlayout, false);
                tv.setText(o);
                return tv;
            }
        });

        RxView.clicks(mBackIv)
                .throttleFirst(500, TimeUnit.MILLISECONDS)
                .subscribe(new Consumer<Object>() {
                    @Override
                    public void accept(Object o) {
                        U.getFragmentUtils().popFragment(OtherPersonFragment.this);
                    }
                });

        RxView.clicks(mFollowTv)
                .throttleFirst(500, TimeUnit.MILLISECONDS)
                .subscribe(new Consumer<Object>() {
                    @Override
                    public void accept(Object o) {
                        if ((int) mFollowTv.getTag() == RELATION_FOLLOWED) {
                            UserInfoManager.getInstance().mateRelation(mUserInfoModel, UserInfoManager.RA_UNBUILD);
                        } else if ((int) mFollowTv.getTag() == RELATION_UN_FOLLOW) {
                            UserInfoManager.getInstance().mateRelation(mUserInfoModel, UserInfoManager.RA_BUILD);
                        }
                    }
                });

        RxView.clicks(mMessageTv)
                .throttleFirst(500, TimeUnit.MILLISECONDS)
                .subscribe(new Consumer<Object>() {
                    @Override
                    public void accept(Object o) {

                    }
                });
    }

    @Override
    public boolean useEventBus() {
        return true;
    }

    @Override
    public void showUserInfo(UserInfoModel model) {
        this.mUserInfoModel = model;

        AvatarUtils.loadAvatarByUrl(mAvatarIv,
                AvatarUtils.newParamsBuilder(MyUserInfoManager.getInstance().getAvatar())
                        .setCircle(true)
                        .setBorderWidth(U.getDisplayUtils().dip2px(3))
                        .setBorderColor(Color.parseColor("#33A4E1"))
                        .build());
        mNameTv.setText(model.getNickname());
        mSignTv.setText(model.getSignature());
    }

    @Override
    public void showUserRelation(boolean isFriend, boolean isFollow) {
        if (isFriend) {
            mFollowTv.setText("互关");
            mFollowTv.setTag(RELATION_FOLLOWED);
        } else if (isFollow) {
            mFollowTv.setText("已关注");
            mFollowTv.setTag(RELATION_FOLLOWED);
        } else {
            mFollowTv.setText("关注TA");
            mFollowTv.setTag(RELATION_UN_FOLLOW);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(RelationChangeEvent event) {
        if (event.userInfoModel.getUserId() == mUserInfoModel.getUserId()) {
            if (event.type == RelationChangeEvent.FOLLOW_TYPE) {
                // TODO: 2019/1/3 需要服务器完善接口
                mFollowTv.setText("已关注");
                mFollowTv.setTag(RELATION_FOLLOWED);
            } else if (event.type == RelationChangeEvent.UNFOLLOW_TYPE) {
                mFollowTv.setText("关注TA");
                mFollowTv.setTag(RELATION_UN_FOLLOW);
            }
        }
    }

}
