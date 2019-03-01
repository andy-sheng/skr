package com.module.home.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.SpannableStringBuilder;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.alibaba.android.arouter.launcher.ARouter;
import com.common.base.BaseFragment;
import com.common.core.avatar.AvatarUtils;
import com.common.core.myinfo.MyUserInfoManager;
import com.common.core.myinfo.event.MyUserInfoEvent;
import com.common.core.myinfo.event.ScoreDetailChangeEvent;
import com.common.core.share.SharePanel;
import com.common.core.share.ShareType;
import com.common.core.upgrade.UpgradeData;
import com.common.core.upgrade.UpgradeManager;
import com.common.core.userinfo.UserInfoManager;
import com.common.core.userinfo.event.RelationChangeEvent;
import com.common.core.userinfo.model.GameStatisModel;
import com.common.core.userinfo.model.UserInfoModel;
import com.common.core.userinfo.model.UserLevelModel;
import com.common.core.userinfo.model.UserRankModel;
import com.common.image.fresco.BaseImageView;
import com.common.log.MyLog;

import com.common.utils.FragmentUtils;
import com.common.utils.SpanUtils;
import com.common.utils.U;
import com.common.view.DebounceViewClickListener;
import com.common.view.ex.ExRelativeLayout;
import com.common.view.ex.ExTextView;
import com.component.busilib.constans.GameModeType;
import com.module.home.musictest.fragment.MusicTestFragment;
import com.module.home.setting.fragment.SettingFragment;

import com.jakewharton.rxbinding2.view.RxView;
import com.module.RouterConstants;
import com.module.home.R;
import com.module.home.persenter.PersonCorePresenter;
import com.module.home.view.IPersonView;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.zq.level.view.NormalLevelView;
import com.zq.relation.fragment.RelationFragment;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.functions.Consumer;
import model.RelationNumModel;

public class PersonFragment extends BaseFragment implements IPersonView {

    public final static String TAG = "PersonFragment";

    SmartRefreshLayout mRefreshLayout;
    BaseImageView mAvatarIv;
    ExTextView mShareTv;
    ExTextView mNameTv;
    ExTextView mUseridTv;
    ExTextView mSignTv;
    RelativeLayout mFriends;
    ExTextView mFriendsNumTv;
    RelativeLayout mFans;
    ExTextView mFansNumTv;
    RelativeLayout mFollows;
    ExTextView mFollowsNumTv;

    ExRelativeLayout mMedalLayout;
    ExTextView mRankNumTv;
    ExTextView mSingendNumTv;
    NormalLevelView mLevelView;
    ExTextView mRankTv;

    RelativeLayout mWalletArea;
    RelativeLayout mAuditionArea;
    RelativeLayout mMusicTestArea;
    RelativeLayout mSettingArea;
    ImageView mSettingRedDot;

    PersonCorePresenter mPersonCorePresenter;

    int rank = 0;           //当前父段位
    int subRank = 0;        //当前子段位
    int starNum = 0;        //当前星星
    int starLimit = 0;      //当前星星上限

    int mFriendNum = 0;  // 好友数
    int mFansNum = 0;    // 粉丝数
    int mFocusNum = 0;   // 关注数

    @Override
    public int initView() {
        return R.layout.person_fragment_layout;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        MyLog.d(TAG, "initData" + " savedInstanceState=" + savedInstanceState);
        U.getSoundUtils().preLoad(TAG, R.raw.allclick);
        initTopView();
        initMedalView();
        initAudioView();
        initMusicTest();
        initWallet();
        initSetting();

        initViewData();

        mPersonCorePresenter = new PersonCorePresenter(this);
        addPresent(mPersonCorePresenter);
        mPersonCorePresenter.getHomePage((int) MyUserInfoManager.getInstance().getUid());
    }

    private void initTopView() {
        mRefreshLayout = (SmartRefreshLayout) mRootView.findViewById(R.id.refreshLayout);

        mRefreshLayout.setEnableRefresh(false);
        mRefreshLayout.setEnableLoadMore(false);
        mRefreshLayout.setEnableOverScrollDrag(true);
        mRefreshLayout.setEnablePureScrollMode(true);

        mAvatarIv = (BaseImageView) mRootView.findViewById(R.id.avatar_iv);
        mShareTv = (ExTextView) mRootView.findViewById(R.id.share_tv);
        mNameTv = (ExTextView) mRootView.findViewById(R.id.name_tv);
        mUseridTv = (ExTextView) mRootView.findViewById(R.id.userid_tv);
        mSignTv = (ExTextView) mRootView.findViewById(R.id.sign_tv);
        mFriends = (RelativeLayout) mRootView.findViewById(R.id.friends);
        mFriendsNumTv = (ExTextView) mRootView.findViewById(R.id.friends_num_tv);
        mFans = (RelativeLayout) mRootView.findViewById(R.id.fans);
        mFansNumTv = (ExTextView) mRootView.findViewById(R.id.fans_num_tv);
        mFollows = (RelativeLayout) mRootView.findViewById(R.id.follows);
        mFollowsNumTv = (ExTextView) mRootView.findViewById(R.id.follows_num_tv);

        RxView.clicks(mShareTv)
                .throttleFirst(500, TimeUnit.MILLISECONDS)
                .subscribe(new Consumer<Object>() {
                    @Override
                    public void accept(Object o) {
                        SharePanel sharePanel = new SharePanel(getActivity());
                        sharePanel.setShareContent("http://res-static.inframe.mobi/common/skr-share.png");
                        sharePanel.show(ShareType.IMAGE_RUL);
                    }
                });
//
//        RxView.clicks(mAvatarIv)
//                .throttleFirst(500, TimeUnit.MILLISECONDS)
//                .subscribe(new Consumer<Object>() {
//                    @Override
//                    public void accept(Object o) {
//                        // TODO: 2018/12/28 可能会加上一个大图预览的功能
//                        ResPicker.getInstance().setParams(ResPicker.newParamsBuilder()
//                                .setSelectLimit(1)
//                                .setCropStyle(CropImageView.Style.CIRCLE)
//                                .build()
//                        );
//
//                        U.getFragmentUtils().addFragment(FragmentUtils.newAddParamsBuilder(getActivity(), ResPickerFragment.class)
//                                .setAddToBackStack(true)
//                                .setHasAnimation(true)
//                                .setFragmentDataListener(new FragmentDataListener() {
//                                    @Override
//                                    public void onFragmentResult(int requestCode, int resultCode, Bundle bundle, Object object) {
//                                        ImageItem imageItem = ResPicker.getInstance().getSingleSelectedImage();
//                                        UploadTask uploadTask = UploadParams.newBuilder(imageItem.getPath())
//                                                .setNeedCompress(true)
//                                                .startUploadAsync(new UploadCallback() {
//                                                    @Override
//                                                    public void onProgress(long currentSize, long totalSize) {
//
//                                                    }
//
//                                                    @Override
//                                                    public void onSuccess(String url) {
//                                                        MyLog.d(TAG, "onSuccess" + " url=" + url);
//                                                        MyUserInfoManager.getInstance().updateInfo(MyUserInfoManager.newMyInfoUpdateParamsBuilder()
//                                                                .setAvatar(url)
//                                                                .build(), false);
//                                                    }
//
//                                                    @Override
//                                                    public void onFailure(String msg) {
//
//                                                    }
//
//                                                });
//                                    }
//                                })
//                                .build());
//                    }
//                });

        mAvatarIv.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                ARouter.getInstance().build(RouterConstants.ACTIVITY_EDIT_INFO)
                        .navigation();
            }
        });

        RxView.clicks(mFriends)
                .throttleFirst(500, TimeUnit.MILLISECONDS)
                .subscribe(new Consumer<Object>() {
                    @Override
                    public void accept(Object o) {
                        U.getSoundUtils().play(TAG, R.raw.allclick);
                        // 好友，双向关注
                        Bundle bundle = new Bundle();
                        bundle.putInt(RelationFragment.FROM_PAGE_KEY, RelationFragment.FROM_FRIENDS);
                        U.getFragmentUtils().addFragment(
                                FragmentUtils.newAddParamsBuilder(getActivity(), RelationFragment.class)
                                        .setBundle(bundle)
                                        .setAddToBackStack(true)
                                        .setHasAnimation(true)
                                        .build());

                    }
                });

        RxView.clicks(mFans)
                .throttleFirst(500, TimeUnit.MILLISECONDS)
                .subscribe(new Consumer<Object>() {
                    @Override
                    public void accept(Object o) {
                        U.getSoundUtils().play(TAG, R.raw.allclick);
                        // 粉丝，我关注的
                        Bundle bundle = new Bundle();
                        bundle.putInt(RelationFragment.FROM_PAGE_KEY, RelationFragment.FROM_FANS);
                        U.getFragmentUtils().addFragment(
                                FragmentUtils.newAddParamsBuilder(getActivity(), RelationFragment.class)
                                        .setBundle(bundle)
                                        .setAddToBackStack(true)
                                        .setHasAnimation(true)
                                        .build());
                    }
                });

        RxView.clicks(mFollows)
                .throttleFirst(500, TimeUnit.MILLISECONDS)
                .subscribe(new Consumer<Object>() {
                    @Override
                    public void accept(Object o) {
                        U.getSoundUtils().play(TAG, R.raw.allclick);
                        // 关注, 关注我的
                        Bundle bundle = new Bundle();
                        bundle.putInt(RelationFragment.FROM_PAGE_KEY, RelationFragment.FROM_FOLLOW);
                        U.getFragmentUtils().addFragment(
                                FragmentUtils.newAddParamsBuilder(getActivity(), RelationFragment.class)
                                        .setBundle(bundle)
                                        .setAddToBackStack(true)
                                        .setHasAnimation(true)
                                        .build());
                    }
                });
    }

    private void initMedalView() {
        mMedalLayout = (ExRelativeLayout) mRootView.findViewById(R.id.medal_layout);
        mRankNumTv = (ExTextView) mRootView.findViewById(R.id.rank_num_tv);
        mSingendNumTv = (ExTextView) mRootView.findViewById(R.id.singend_num_tv);
        mLevelView = (NormalLevelView) mRootView.findViewById(R.id.level_view);
        mRankTv = (ExTextView) mRootView.findViewById(R.id.rank_tv);
    }

    private void initAudioView() {
        mAuditionArea = (RelativeLayout) mRootView.findViewById(R.id.audition_area);
        RxView.clicks(mAuditionArea)
                .throttleFirst(500, TimeUnit.MILLISECONDS)
                .subscribe(new Consumer<Object>() {
                    @Override
                    public void accept(Object o) {
                        U.getSoundUtils().play(PersonFragment.TAG, R.raw.allclick, 500);
                        ARouter.getInstance().build(RouterConstants.ACTIVITY_AUDIOROOM)
                                .withBoolean("selectSong", true)
                                .navigation();
                    }
                });
    }

    private void initMusicTest() {
        mMusicTestArea = (RelativeLayout) mRootView.findViewById(R.id.music_test_area);
        RxView.clicks(mMusicTestArea)
                .throttleFirst(500, TimeUnit.MILLISECONDS)
                .subscribe(new Consumer<Object>() {
                    @Override
                    public void accept(Object o) {
                        U.getSoundUtils().play(PersonFragment.TAG, R.raw.allclick, 500);
                        U.getFragmentUtils().addFragment(
                                FragmentUtils.newAddParamsBuilder(getActivity(), MusicTestFragment.class)
                                        .setAddToBackStack(true)
                                        .setHasAnimation(true)
                                        .build());
                    }
                });
    }

    private void initWallet() {
        mWalletArea = (RelativeLayout) mRootView.findViewById(R.id.wallet_area);
        RxView.clicks(mWalletArea)
                .throttleFirst(500, TimeUnit.MILLISECONDS)
                .subscribe(new Consumer<Object>() {
                    @Override
                    public void accept(Object o) {
                        U.getSoundUtils().play(PersonFragment.TAG, R.raw.allclick, 500);
                        U.getFragmentUtils().addFragment(
                                FragmentUtils.newAddParamsBuilder(getActivity(), WalletFragment.class)
                                        .setAddToBackStack(true)
                                        .setHasAnimation(true)
                                        .build());
                    }
                });
    }

    private void initSetting() {
        mSettingArea = (RelativeLayout) mRootView.findViewById(R.id.setting_area);
        mSettingArea.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                U.getSoundUtils().play(PersonFragment.TAG, R.raw.allclick, 500);
                U.getFragmentUtils().addFragment(
                        FragmentUtils.newAddParamsBuilder(getActivity(), SettingFragment.class)
                                .setAddToBackStack(true)
                                .setHasAnimation(true)
                                .build());
            }
        });
        mSettingRedDot = mSettingArea.findViewById(R.id.setting_red_dot);
        updateSettingRedDot();
    }

    private void updateSettingRedDot() {
        if (UpgradeManager.getInstance().needShowRedDotTips()) {
            mSettingRedDot.setVisibility(View.VISIBLE);
        } else {
            mSettingRedDot.setVisibility(View.GONE);
        }
    }

    private void initViewData() {
        AvatarUtils.loadAvatarByUrl(mAvatarIv, AvatarUtils.newParamsBuilder(MyUserInfoManager.getInstance()
                .getAvatar())
                .setCircle(true)
                .setBorderColorBySex(MyUserInfoManager.getInstance().getSex() == 1)
                .setBorderWidth(U.getDisplayUtils().dip2px(3))
                .build());
        mNameTv.setText(MyUserInfoManager.getInstance().getNickName());
        mUseridTv.setText("撕歌号：" + MyUserInfoManager.getInstance().getUid());
        mSignTv.setText(MyUserInfoManager.getInstance().getSignature());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvnet(MyUserInfoEvent.UserInfoChangeEvent userInfoChangeEvent) {
        mPersonCorePresenter.getHomePage((int) MyUserInfoManager.getInstance().getUid());
        initViewData();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvnet(ScoreDetailChangeEvent scoreDetailChangeEvent) {
        mLevelView.bindData(scoreDetailChangeEvent.level, scoreDetailChangeEvent.subLevel,
                scoreDetailChangeEvent.totalStats, scoreDetailChangeEvent.selecStats);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(RelationChangeEvent event) {
        if (event.type == RelationChangeEvent.FOLLOW_TYPE) {
            if (event.isFriend) {
                // 新增好友,好友数加1
                mFriendNum = mFriendNum + 1;
                mFocusNum = mFocusNum + 1;
            } else if (event.isFollow) {
                // 新增关注,关注数加1
                mFocusNum = mFocusNum + 1;
            }
        } else if (event.type == RelationChangeEvent.UNFOLLOW_TYPE) {
            // 关注数减1
            mFocusNum = mFocusNum - 1;
            // TODO: 2019/1/17 怎么判断之前也是好友
            if (event.isOldFriend) {
                mFriendNum = mFriendNum - 1;
            }
        }

        refreshRelationNum();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(UpgradeData.RedDotStatusEvent event) {
        updateSettingRedDot();
    }

    @Override
    public boolean useEventBus() {
        return true;
    }


    @Override
    public void showUserInfo(UserInfoModel userInfoModel) {

    }

    @Override
    public void showRelationNum(List<RelationNumModel> list) {
        for (RelationNumModel mode : list) {
            if (mode.getRelation() == UserInfoManager.RELATION_FRIENDS) {
                mFriendNum = mode.getCnt();
            } else if (mode.getRelation() == UserInfoManager.RELATION_FANS) {
                mFansNum = mode.getCnt();
            } else if (mode.getRelation() == UserInfoManager.RELATION_FOLLOW) {
                mFocusNum = mode.getCnt();
            }
        }

        refreshRelationNum();

    }

    private void refreshRelationNum() {
        mFriendsNumTv.setText(String.valueOf(mFriendNum));
        mFansNumTv.setText(String.valueOf(mFansNum));
        mFollowsNumTv.setText(String.valueOf(mFocusNum));
    }

    @Override
    public void showReginRank(List<UserRankModel> list) {
        UserRankModel reginRankModel = new UserRankModel();
        UserRankModel countryRankModel = new UserRankModel();
        if (list != null && list.size() > 0) {
            for (UserRankModel model : list) {
                if (model.getCategory() == UserRankModel.REGION) {
                    reginRankModel = model;
                }
                if (model.getCategory() == UserRankModel.COUNTRY) {
                    countryRankModel = model;
                }
            }
        }

        if (reginRankModel != null && reginRankModel.getRankSeq() != 0) {
            mRankTv.setText(reginRankModel.getRegionDesc() + "第" + String.valueOf(reginRankModel.getRankSeq()) + "位");
        } else if (countryRankModel != null && countryRankModel.getRankSeq() != 0) {
            mRankTv.setText(countryRankModel.getRegionDesc() + "第" + String.valueOf(countryRankModel.getRankSeq()) + "位");
        } else {
            mRankTv.setText("暂无排名");
        }
    }

    @Override
    public void showUserLevel(List<UserLevelModel> list) {
        // 展示段位信息
        for (UserLevelModel userLevelModel : list) {
            if (userLevelModel.getType() == UserLevelModel.RANKING_TYPE) {
                rank = userLevelModel.getScore();
            } else if (userLevelModel.getType() == UserLevelModel.SUB_RANKING_TYPE) {
                subRank = userLevelModel.getScore();
            } else if (userLevelModel.getType() == UserLevelModel.TOTAL_RANKING_STAR_TYPE) {
                starNum = userLevelModel.getScore();
            } else if (userLevelModel.getType() == UserLevelModel.REAL_RANKING_STAR_TYPE) {
                starLimit = userLevelModel.getScore();
            }
        }
        mLevelView.bindData(rank, subRank, starLimit, starNum);
    }

    @Override
    public void showGameStatic(List<GameStatisModel> list) {
        for (GameStatisModel gameStatisModel : list) {
            if (gameStatisModel.getMode() == GameModeType.GAME_MODE_CLASSIC_RANK) {
                SpannableStringBuilder stringBuilder = new SpanUtils()
                        .append(String.valueOf(gameStatisModel.getTotalTimes())).setFontSize(14, true)
                        .append("场").setFontSize(10, true)
                        .create();
                mRankNumTv.setText(stringBuilder);
            } else if (gameStatisModel.getMode() == GameModeType.GAME_MODE_GRAB) {
                SpannableStringBuilder stringBuilder = new SpanUtils()
                        .append(String.valueOf(gameStatisModel.getTotalTimes())).setFontSize(14, true)
                        .append("首").setFontSize(10, true)
                        .create();
                mSingendNumTv.setText(stringBuilder);
            }
        }
    }
}
