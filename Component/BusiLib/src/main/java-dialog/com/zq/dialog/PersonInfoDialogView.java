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
import com.common.core.account.UserAccountManager;
import com.common.core.avatar.AvatarUtils;
import com.common.core.myinfo.MyUserInfoManager;
import com.common.core.userinfo.UserInfoManager;
import com.common.core.userinfo.UserInfoServerApi;
import com.common.core.userinfo.event.RelationChangeEvent;
import com.common.core.userinfo.model.UserInfoModel;
import com.common.core.userinfo.model.UserLevelModel;
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
import com.component.busilib.view.MarqueeTextView;
import com.facebook.drawee.view.SimpleDraweeView;
import com.zq.level.view.HorizonLevelView;

import junit.framework.Test;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

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
    MarqueeTextView mSignTv;
    ExTextView mReport;
    TagFlowLayout mFlowlayout;
    ExTextView mFollowTv;

    public PersonInfoDialogView(Context context, int userID) {
        super(context);
        initView();
        initData(context, userID);
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }

    public UserInfoModel getUserInfoModel() {
        return mUserInfoModel;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
    }

    private void initView() {
        inflate(getContext(), R.layout.person_info_dialog_view, this);

        mAvatarIv = (SimpleDraweeView) this.findViewById(R.id.avatar_iv);
        mNameTv = (ExTextView) this.findViewById(R.id.name_tv);
        mHorizLevelView = (HorizonLevelView) this.findViewById(R.id.horiz_level_view);
        mSignTv = (MarqueeTextView) this.findViewById(R.id.sign_tv);
        mReport = (ExTextView) this.findViewById(R.id.report);
        mFlowlayout = (TagFlowLayout) this.findViewById(R.id.flowlayout);
        mFollowTv = (ExTextView) this.findViewById(R.id.follow_tv);

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
        if (userID == UserAccountManager.SYSTEM_GRAB_ID || userID == UserAccountManager.SYSTEM_RANK_AI) {
            mReport.setVisibility(GONE);
        }

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

        ApiMethods.subscribe(mUserInfoServerApi.getScoreDetail(userID), new ApiObserver<ApiResult>() {

            @Override
            public void process(ApiResult result) {
                List<UserLevelModel> userLevelModels = JSON.parseArray(result.getData().getString("userScore"), UserLevelModel.class);
                showUserLevel(userLevelModels);
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
                        .setBorderColorBySex(userInfo.getIsMale())
                        .build());
        mNameTv.setText(userInfo.getNickname());
        mSignTv.setText(userInfo.getSignature());

        if (userInfo.getUserId() == MyUserInfoManager.getInstance().getUid()) {
            mFollowTv.setVisibility(GONE);
            mReport.setVisibility(GONE);
        }

        if (userInfo.getLocation() != null && !TextUtils.isEmpty(userInfo.getLocation().getCity())) {
            mHashMap.put(LOCATION_TAG, userInfo.getLocation().getCity());
        } else {
            mHashMap.put(LOCATION_TAG, "未知星球");
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
            mFollowTv.setTextColor(Color.WHITE);
            mFollowTv.setBackgroundResource(R.drawable.img_btn_bg_dark_gray);
        } else if (isFollow) {
            mFollowTv.setBackgroundResource(R.drawable.img_btn_bg_yellow);
            mFollowTv.setTextColor(Color.parseColor("#0C2275"));
            mFollowTv.setText("已关注");
        } else {
            mFollowTv.setBackgroundResource(R.drawable.img_btn_bg_red);
            mFollowTv.setTextColor(Color.WHITE);
            mFollowTv.setText("关注");
        }
    }

    private void showReginRank(List<UserRankModel> userRankModels) {
        UserRankModel reginRankModel = new UserRankModel();
        if (userRankModels != null && userRankModels.size() > 0) {
            for (UserRankModel model : userRankModels) {
                if (model.getCategory() == UserRankModel.REGION) {
                    reginRankModel = model;
                }
            }
        }

        // TODO: 2019/1/6 必须加上策略，比如没有位置信息
        if (reginRankModel != null) {
            if (reginRankModel.getRankSeq() != 0) {
                mHashMap.put(RANK_TAG, reginRankModel.getRegionDesc() + "荣耀榜" + String.valueOf(reginRankModel.getRankSeq()) + "位");
            } else {
                mHashMap.put(RANK_TAG, getResources().getString(R.string.default_rank_text));
            }
        } else {
            mHashMap.put(RANK_TAG, getResources().getString(R.string.default_rank_text));
        }
        refreshTag();
    }

    public void showUserLevel(List<UserLevelModel> list) {
        // 展示段位信息
        int rank = 0;           //当前父段位
        String rankDesc = "";   //父段位描述
        int subRank = 0;        //当前子段位
        int starNum = 0;        //当前星星
        int starLimit = 0;      //当前星星上限
        for (UserLevelModel userLevelModel : list) {
            if (userLevelModel.getType() == UserLevelModel.RANKING_TYPE) {
                rank = userLevelModel.getScore();
                rankDesc = userLevelModel.getDesc();
            } else if (userLevelModel.getType() == UserLevelModel.SUB_RANKING_TYPE) {
                subRank = userLevelModel.getScore();
            } else if (userLevelModel.getType() == UserLevelModel.TOTAL_RANKING_STAR_TYPE) {
                starNum = userLevelModel.getScore();
            } else if (userLevelModel.getType() == UserLevelModel.REAL_RANKING_STAR_TYPE) {
                starLimit = userLevelModel.getScore();
            }
        }
        mHorizLevelView.bindData(rank, subRank, rankDesc, starLimit, starNum);
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


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(RelationChangeEvent event) {
        if (event.useId == mUserInfoModel.getUserId()) {
            mUserInfoModel.setFollow(event.isFollow);
            mUserInfoModel.setFriend(event.isFriend);
            if (event.type == RelationChangeEvent.FOLLOW_TYPE) {
                if (event.isFriend) {
                    mFollowTv.setText("互关");
                    mFollowTv.setTextColor(Color.WHITE);
                    mFollowTv.setBackgroundResource(R.drawable.img_btn_bg_dark_gray);
                } else if (event.isFollow) {
                    mFollowTv.setText("已关注");
                    mFollowTv.setTextColor(Color.parseColor("#0C2275"));
                    mFollowTv.setBackgroundResource(R.drawable.img_btn_bg_yellow);
                }
            } else if (event.type == RelationChangeEvent.UNFOLLOW_TYPE) {
                mFollowTv.setBackgroundResource(R.drawable.img_btn_bg_red);
                mFollowTv.setTextColor(Color.WHITE);
                mFollowTv.setText("关注TA");
            }
        }
    }
}
