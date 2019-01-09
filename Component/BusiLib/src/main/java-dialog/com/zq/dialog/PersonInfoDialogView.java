package com.zq.dialog;

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;

import com.alibaba.fastjson.JSON;
import com.common.base.BaseActivity;
import com.common.base.BaseFragment;
import com.common.core.avatar.AvatarUtils;
import com.common.core.userinfo.UserInfoManager;
import com.common.core.userinfo.UserInfoServerApi;
import com.common.core.userinfo.model.UserInfoModel;
import com.common.core.userinfo.model.UserRankModel;
import com.common.flowlayout.FlowLayout;
import com.common.flowlayout.TagAdapter;
import com.common.flowlayout.TagFlowLayout;
import com.common.log.MyLog;
import com.common.rxretrofit.ApiManager;
import com.common.rxretrofit.ApiMethods;
import com.common.rxretrofit.ApiObserver;
import com.common.rxretrofit.ApiResult;
import com.common.utils.U;
import com.common.view.ex.ExTextView;
import com.component.busilib.R;
import com.facebook.drawee.view.SimpleDraweeView;
import com.zq.level.view.HorizonLevelView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

// 个人信息卡片view
public class PersonInfoDialogView extends RelativeLayout {

    public final static String TAG = "PersonInfoDialogView";

    private static final int RANK_TAG = 0;     //排名标签
    private static final int LOCATION_TAG = 1; //地区标签
    private static final int AGE_TAG = 2;      //年龄标签

    private List<String> mTags = new ArrayList<>();  //标签
    private HashMap<Integer, String> mHashMap = new HashMap();

    UserInfoServerApi mUserInfoServerApi;
    UserInfoModel mUserInfoModel = new UserInfoModel();

    TagAdapter<String> mTagAdapter;

    SimpleDraweeView mAvatarIv;
    ExTextView mNameTv;
    HorizonLevelView mHorizLevelView;
    ExTextView mSignTv;
    ExTextView mReport;
    TagFlowLayout mFlowlayout;
    ExTextView mFollowTv;

    public PersonInfoDialogView(Context context, int userID) {
        super(context);
        initView();
        initData(context, userID);
    }

    public UserInfoModel getUserInfoModel() {
        return mUserInfoModel;
    }


    private void initView() {
        inflate(getContext(), R.layout.person_info_dialog_view, this);

        mAvatarIv = (SimpleDraweeView) this.findViewById(R.id.avatar_iv);
        mNameTv = (ExTextView) this.findViewById(R.id.name_tv);
        mHorizLevelView = (HorizonLevelView) this.findViewById(R.id.horiz_level_view);
        mSignTv = (ExTextView) this.findViewById(R.id.sign_tv);
        mReport = (ExTextView) this.findViewById(R.id.report);
        mFlowlayout = (TagFlowLayout) this.findViewById(R.id.flowlayout);
        mFollowTv = (ExTextView) this.findViewById(R.id.follow_tv);

        // TODO: 2018/12/26 暂时砍掉举报 
        mReport.setVisibility(GONE);

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

    }

    private void initData(Context context, int userID) {
        mUserInfoServerApi = ApiManager.getInstance().createService(UserInfoServerApi.class);
        // 个人基础资料
        UserInfoManager.getInstance().getUserInfoByUuid(userID, new UserInfoManager.ResultCallback<UserInfoModel>() {
            @Override
            public boolean onGetLocalDB(UserInfoModel userInfoModel) {
                showBaseInfo(userInfoModel);
                return false;
            }

            @Override
            public boolean onGetServer(UserInfoModel userInfoModel) {
                showBaseInfo(userInfoModel);
                return false;
            }
        });

        // 关系
        ApiMethods.subscribe(mUserInfoServerApi.getRelation(userID), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult result) {
                if (result.getErrno() == 0) {
                    boolean isFriend = result.getData().getBoolean("isFriend");
                    boolean isFollow = result.getData().getBoolean("isFollow");
                    showRelation(isFriend, isFollow);
                }
            }
        }, (BaseActivity) context);

        // 排名
        ApiMethods.subscribe(mUserInfoServerApi.getReginRank(userID), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult result) {
                if (result.getErrno() == 0) {
                    List<UserRankModel> userRankModels = JSON.parseArray(result.getData().getString("seqInfo"), UserRankModel.class);
                    showReginRank(userRankModels);
                }
            }
        }, (BaseActivity) context);


    }

    private void showBaseInfo(UserInfoModel userInfo) {
        if (userInfo != null) {
            this.mUserInfoModel.setUserId(userInfo.getUserId());
            this.mUserInfoModel.setNickname(userInfo.getNickname());
            this.mUserInfoModel.setSex(userInfo.getSex());
            this.mUserInfoModel.setBirthday(userInfo.getBirthday());
            this.mUserInfoModel.setAvatar(userInfo.getAvatar());
            this.mUserInfoModel.setSignature(userInfo.getSignature());
            this.mUserInfoModel.setLocation(userInfo.getLocation());
        }

        AvatarUtils.loadAvatarByUrl(mAvatarIv,
                AvatarUtils.newParamsBuilder(userInfo.getAvatar())
                        .setCircle(true)
                        .setBorderWidth(U.getDisplayUtils().dip2px(3))
                        .setBorderColor(Color.parseColor("#33A4E1"))
                        .build());
        mNameTv.setText(userInfo.getNickname());
        mSignTv.setText(userInfo.getSignature());

        if (userInfo.getLocation() != null) {
            mHashMap.put(LOCATION_TAG, userInfo.getLocation().getCity());
        }

        if (!TextUtils.isEmpty(userInfo.getBirthday())) {
            mHashMap.put(AGE_TAG, String.format(U.app().getString(R.string.age_tag), userInfo.getAge()));
        }

        refreshTag();
    }

    private void showRelation(boolean isFriend, boolean isFollow) {
        this.mUserInfoModel.setFriend(isFriend);
        this.mUserInfoModel.setFollow(isFollow);
        if (isFriend) {
            mFollowTv.setText("互关");
        } else if (isFollow) {
            mFollowTv.setText("已关注");
        } else {
            mFollowTv.setText("未关注");
        }
    }

    private void showReginRank(List<UserRankModel> userRankModels) {
        UserRankModel reginRankModel = new UserRankModel();
        if (userRankModels != null && userRankModels.size() > 0) {
            for (UserRankModel model : userRankModels) {
                if (model.getCategoy() == UserRankModel.REGION) {
                    reginRankModel = model;
                }
            }
        }

        // TODO: 2019/1/6 必须加上策略，比如没有位置信息
        if (reginRankModel != null) {
            mHashMap.put(RANK_TAG, reginRankModel.getRegionDesc() + "荣耀榜" + String.valueOf(reginRankModel.getSeq()) + "位");
            refreshTag();
        }
    }

    private void refreshTag() {
        mTags.clear();
        if (mHashMap != null) {
            if (!TextUtils.isEmpty(mHashMap.get(RANK_TAG))) {
                mTags.add(mHashMap.get(RANK_TAG));
            }

            if (!TextUtils.isEmpty(mHashMap.get(LOCATION_TAG))) {
                mTags.add(mHashMap.get(LOCATION_TAG));
            }

            if (!TextUtils.isEmpty(mHashMap.get(AGE_TAG))) {
                mTags.add(mHashMap.get(AGE_TAG));
            }
        }
        mTagAdapter.setTagDatas(mTags);
        mTagAdapter.notifyDataChanged();
    }
}
