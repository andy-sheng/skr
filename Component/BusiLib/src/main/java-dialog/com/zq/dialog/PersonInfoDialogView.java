package com.zq.dialog;

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;

import com.alibaba.fastjson.JSON;
import com.common.base.BaseActivity;
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
import com.common.view.DebounceViewClickListener;
import com.common.view.ex.ExRelativeLayout;
import com.common.view.ex.ExTextView;
import com.component.busilib.R;
import com.component.busilib.constans.GameModeType;
import com.component.busilib.view.MarqueeTextView;
import com.facebook.drawee.view.SimpleDraweeView;
import com.module.ModuleServiceManager;
import com.module.common.ICallback;
import com.module.msg.IMsgService;
import com.zq.level.view.HorizonLevelView;

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


    int mTargetUserId;

    ExRelativeLayout mContentArea;
    ExTextView mNameTv;
    HorizonLevelView mHorizLevelView;
    MarqueeTextView mSignTv;
    TagFlowLayout mFlowlayout;
    RelativeLayout mFollowArea;
    ExTextView mFollowTv;
    SimpleDraweeView mAvatarIv;
    ExTextView mReport;
    ExRelativeLayout mKick;

    PersonInfoDialogView(Context context, int userID, boolean showReport, boolean showKick) {
        super(context);
        initView();
        initData(context, userID, showReport, showKick);
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

        mContentArea = (ExRelativeLayout) this.findViewById(R.id.content_area);
        mNameTv = (ExTextView) this.findViewById(R.id.name_tv);
        mHorizLevelView = (HorizonLevelView) this.findViewById(R.id.horiz_level_view);
        mSignTv = (MarqueeTextView) this.findViewById(R.id.sign_tv);
        mFlowlayout = (TagFlowLayout) this.findViewById(R.id.flowlayout);
        mFollowArea = (RelativeLayout) this.findViewById(R.id.follow_area);
        mFollowTv = (ExTextView) this.findViewById(R.id.follow_tv);
        mAvatarIv = (SimpleDraweeView) this.findViewById(R.id.avatar_iv);
        mReport = (ExTextView) this.findViewById(R.id.report);
        mKick = (ExRelativeLayout) this.findViewById(R.id.kick);

        mNameTv.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (MyLog.isDebugLogOpen()) {
                    MyLog.d(TAG, "clickValid" + " v=" + v);
                    IMsgService msgService = ModuleServiceManager.getInstance().getMsgService();
                    if (msgService != null) {
                        msgService.sendSpecialDebugMessage(String.valueOf(mTargetUserId), 1, "请求上传日志", new ICallback() {
                            @Override
                            public void onSucess(Object obj) {
                                U.getToastUtil().showLong("请求成功,稍等看该用户是否有返回");
                            }

                            @Override
                            public void onFailed(Object obj, int errcode, String message) {

                            }
                        });
                    }
                } else {
                }
                return false;
            }
        });

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

    private void initData(Context context, int userID, boolean showReport, boolean showKick) {
        mTargetUserId = userID;

        mReport.setVisibility(showReport ? VISIBLE : GONE);
        mKick.setVisibility(showKick ? VISIBLE : GONE);

        // 多音和ai裁判
        if (userID == UserAccountManager.SYSTEM_GRAB_ID || userID == UserAccountManager.SYSTEM_RANK_AI) {
            mReport.setVisibility(GONE);
            mKick.setVisibility(GONE);
        }

        // 自己卡片的处理
        if (userID == MyUserInfoManager.getInstance().getUid()) {
            mFollowArea.setVisibility(GONE);
            mFollowTv.setVisibility(GONE);
            mReport.setVisibility(GONE);
            mKick.setVisibility(GONE);
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
            mFollowArea.setBackgroundResource(R.drawable.img_follow_dark_bg);
        } else if (isFollow) {
            mFollowArea.setBackgroundResource(R.drawable.img_follow_dark_bg);
            mFollowTv.setText("已关注");
        } else {
            mFollowArea.setBackgroundResource(R.drawable.img_follow_orange_bg);
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
                mHashMap.put(RANK_TAG, reginRankModel.getRegionDesc() + "第" + String.valueOf(reginRankModel.getRankSeq()) + "位");
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
        for (UserLevelModel userLevelModel : list) {
            if (userLevelModel.getType() == UserLevelModel.RANKING_TYPE) {
                rank = userLevelModel.getScore();
            } else if (userLevelModel.getType() == UserLevelModel.SUB_RANKING_TYPE) {
                subRank = userLevelModel.getScore();
                rankDesc = userLevelModel.getDesc();
            }
        }
        mHorizLevelView.bindData(rank, subRank, rankDesc);
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
                    mFollowArea.setBackgroundResource(R.drawable.img_follow_dark_bg);
                } else if (event.isFollow) {
                    mFollowTv.setText("已关注");
                    mFollowArea.setBackgroundResource(R.drawable.img_follow_dark_bg);
                }
            } else if (event.type == RelationChangeEvent.UNFOLLOW_TYPE) {
                mFollowArea.setBackgroundResource(R.drawable.img_follow_orange_bg);
                mFollowTv.setText("关注ta");
            }
        }
    }
}
