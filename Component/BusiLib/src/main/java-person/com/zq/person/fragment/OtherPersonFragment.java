package com.zq.person.fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;

import com.common.base.BaseFragment;
import com.common.core.avatar.AvatarUtils;
import com.common.core.userinfo.UserInfoManager;
import com.common.core.userinfo.model.UserInfoModel;
import com.common.core.userinfo.event.RelationChangeEvent;
import com.common.core.userinfo.model.UserRankModel;
import com.common.flowlayout.FlowLayout;
import com.common.flowlayout.TagAdapter;
import com.common.flowlayout.TagFlowLayout;
import com.common.image.fresco.BaseImageView;
import com.common.utils.U;
import com.common.view.ex.ExImageView;
import com.common.view.ex.ExTextView;
import com.component.busilib.R;
import com.jakewharton.rxbinding2.view.RxView;
import com.module.ModuleServiceManager;
import com.zq.live.proto.Common.ESex;
import com.zq.person.presenter.OtherPersonPresenter;
import com.zq.person.view.IOtherPersonView;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.functions.Consumer;
import model.RelationNumModel;
import model.UserLevelModel;


public class OtherPersonFragment extends BaseFragment implements IOtherPersonView {

    public static final String BUNDLE_USER_MODEL = "budle_user_model";

    public static final int RELATION_FOLLOWED = 1; // 已关注关系
    public static final int RELATION_UN_FOLLOW = 2; // 未关注关系

    private static final int LOCATION_TAG = 0;     //地区标签  省/市
    private static final int AGE_TAG = 1;          //年龄标签
    private static final int FANS_NUM_TAG = 2;     // 粉丝数标签

    private List<String> mTags = new ArrayList<>();  //标签
    private HashMap<Integer, String> mHashMap = new HashMap();

    TagAdapter<String> mTagAdapter;

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

    int rank = 0;           //当前父段位
    int subRank = 0;        //当前子段位
    int starNum = 0;        //当前星星
    int starLimit = 0;      //当前星星上限

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
            mOtherPersonPresenter.getHomePage(mUserInfoModel.getUserId());
        }


        mTagAdapter = new TagAdapter<String>(mTags) {
            @Override
            public View getView(FlowLayout parent, int position, String o) {
                ExTextView tv = (ExTextView) LayoutInflater.from(getContext()).inflate(R.layout.tag_textview,
                        mFlowlayout, false);
                tv.setText(o);
                return tv;
            }
        };
        mFlowlayout.setAdapter(mTagAdapter);

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
                        ModuleServiceManager.getInstance().getMsgService().startPrivateChat(getContext(),
                                String.valueOf(mUserInfoModel.getUserId()), mUserInfoModel.getNickname());
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

        if (model.getSex() == ESex.SX_MALE.getValue()) {
            AvatarUtils.loadAvatarByUrl(mAvatarIv,
                    AvatarUtils.newParamsBuilder(model.getAvatar())
                            .setCircle(true)
                            .setBorderWidth(U.getDisplayUtils().dip2px(3))
                            .setBorderColor(Color.parseColor("#33A4E1"))
                            .build());
        } else if (model.getSex() == ESex.SX_FEMALE.getValue()) {
            AvatarUtils.loadAvatarByUrl(mAvatarIv,
                    AvatarUtils.newParamsBuilder(model.getAvatar())
                            .setCircle(true)
                            .setBorderWidth(U.getDisplayUtils().dip2px(3))
                            .setBorderColor(Color.parseColor("#FF75A2"))
                            .build());
        }

        mNameTv.setText(model.getNickname());
        mSignTv.setText(model.getSignature());

        if (model.getLocation() != null) {
            mHashMap.put(LOCATION_TAG, model.getLocation().getCity() + "/" + model.getLocation().getDistrict());
        }

        if (!TextUtils.isEmpty(model.getBirthday())) {
            mHashMap.put(AGE_TAG, String.format(getString(R.string.age_tag), model.getAge()));
        }

        refreshTag();
    }

    @Override
    public void showRelationNum(List<RelationNumModel> list) {
        int fansNum = 0;
        for (RelationNumModel mode : list) {
            if (mode.getRelation() == UserInfoManager.RELATION_FANS) {
                fansNum = mode.getCnt();
            }
        }
        mHashMap.put(FANS_NUM_TAG, String.format(getString(R.string.fans_num_tag), fansNum));
        refreshTag();
    }

    @Override
    public void showReginRank(List<UserRankModel> list) {

    }

    @Override
    public void showUserLevel(List<UserLevelModel> list) {



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
                if (event.isFriend) {
                    mFollowTv.setText("互关");
                } else if (event.isFollow) {
                    mFollowTv.setText("已关注");
                }
                mFollowTv.setTag(RELATION_FOLLOWED);
            } else if (event.type == RelationChangeEvent.UNFOLLOW_TYPE) {
                mFollowTv.setText("关注TA");
                mFollowTv.setTag(RELATION_UN_FOLLOW);
            }
        }
    }

    private void refreshTag() {
        mTags.clear();
        if (mHashMap != null) {
            if (!TextUtils.isEmpty(mHashMap.get(LOCATION_TAG))) {
                mTags.add(mHashMap.get(LOCATION_TAG));
            }

            if (!TextUtils.isEmpty(mHashMap.get(AGE_TAG))) {
                mTags.add(mHashMap.get(AGE_TAG));
            }

            if (!TextUtils.isEmpty(mHashMap.get(FANS_NUM_TAG))) {
                mTags.add(mHashMap.get(FANS_NUM_TAG));
            }
        }
        mTagAdapter.setTagDatas(mTags);
        mTagAdapter.notifyDataChanged();
    }

}
