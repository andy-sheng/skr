package com.module.home.fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;

import com.alibaba.android.arouter.launcher.ARouter;
import com.alibaba.fastjson.JSON;
import com.common.base.BaseFragment;
import com.common.core.avatar.AvatarUtils;
import com.common.core.myinfo.MyUserInfoManager;
import com.common.core.myinfo.event.MyUserInfoEvent;

import com.common.core.share.SharePanel;
import com.common.core.share.ShareType;
import com.common.core.upgrade.UpgradeData;
import com.common.core.upgrade.UpgradeManager;
import com.common.core.userinfo.UserInfoManager;
import com.common.core.userinfo.UserInfoServerApi;
import com.common.core.userinfo.event.RelationChangeEvent;
import com.common.core.userinfo.model.GameStatisModel;
import com.common.core.userinfo.model.UserInfoModel;
import com.common.core.userinfo.model.UserLevelModel;
import com.common.core.userinfo.model.UserRankModel;
import com.common.image.fresco.BaseImageView;
import com.common.log.MyLog;

import com.common.notification.event.FollowNotifyEvent;
import com.common.rxretrofit.ApiManager;
import com.common.rxretrofit.ApiMethods;
import com.common.rxretrofit.ApiObserver;
import com.common.rxretrofit.ApiResult;
import com.common.utils.FragmentUtils;
import com.common.utils.SpanUtils;
import com.common.utils.U;
import com.common.view.DebounceViewClickListener;
import com.common.view.ex.ExImageView;
import com.common.view.ex.ExRelativeLayout;
import com.common.view.ex.ExTextView;
import com.component.busilib.constans.GameModeType;
import com.component.busilib.view.MarqueeTextView;
import com.module.home.musictest.fragment.MusicTestFragment;
import com.module.home.setting.fragment.SettingFragment;

import com.jakewharton.rxbinding2.view.RxView;
import com.module.RouterConstants;
import com.module.home.R;
import com.module.home.persenter.PersonCorePresenter;
import com.module.home.view.IPersonView;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.header.ClassicsHeader;
import com.scwang.smartrefresh.layout.listener.OnRefreshLoadMoreListener;
import com.zq.level.view.NormalLevelView2;
import com.zq.relation.fragment.RelationFragment;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.reactivex.functions.Consumer;
import model.RelationNumModel;

import static com.module.home.fragment.GameFragment.SHANDIAN_BADGE;
import static com.module.home.fragment.GameFragment.STAR_BADGE;
import static com.module.home.fragment.GameFragment.TOP_BADGE;

public class PersonFragment extends BaseFragment implements IPersonView {

    public final static String TAG = "PersonFragment";

    SmartRefreshLayout mRefreshLayout;
    BaseImageView mAvatarIv;
    ExTextView mShareTv;
    ExTextView mNameTv;
    ExTextView mUseridTv;
    MarqueeTextView mSignTv;
    RelativeLayout mFriends;
    ExTextView mFriendsNumTv;
    RelativeLayout mFans;
    ExTextView mFansNumTv;
    RelativeLayout mFollows;
    ExTextView mFollowsNumTv;

    ExRelativeLayout mMedalLayout;
    ExTextView mRankNumTv;
    ExTextView mSingendNumTv;
    NormalLevelView2 mLevelView;
    ExTextView mLevelTv;
    RelativeLayout mRankArea;
    ExTextView mRankText;
    ExImageView mRankDiffIv;
    ExImageView mMedalIv;


    RelativeLayout mWalletArea;
    RelativeLayout mAuditionArea;
    RelativeLayout mMusicTestArea;
    RelativeLayout mSettingArea;
    ImageView mSettingRedDot;

    PopupWindow mPopupWindow;  // 显示上升或者下降的标识
    LinearLayout mPopArea;
    ExTextView mRankDiffTv;
    ImageView mRankDiffIcon;

    PersonCorePresenter mPersonCorePresenter;

    int rank = 0;           //当前父段位
    int subRank = 0;        //当前子段位
    int starNum = 0;        //当前星星
    int starLimit = 0;      //当前星星上限
    String levelDesc;

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
        initTopView();
        initMedalView();
        initAudioView();
        initMusicTest();
        initWallet();
        initSetting();

        initViewData();

        mPersonCorePresenter = new PersonCorePresenter(this);
        addPresent(mPersonCorePresenter);
        mPersonCorePresenter.getHomePage((int) MyUserInfoManager.getInstance().getUid(), true);
        mPersonCorePresenter.getRankLevel(true);
    }

    private void initTopView() {
        mRefreshLayout = (SmartRefreshLayout) mRootView.findViewById(R.id.refreshLayout);

        mAvatarIv = (BaseImageView) mRootView.findViewById(R.id.avatar_iv);
        mShareTv = (ExTextView) mRootView.findViewById(R.id.share_tv);
        mNameTv = (ExTextView) mRootView.findViewById(R.id.name_tv);
        mUseridTv = (ExTextView) mRootView.findViewById(R.id.userid_tv);
        mSignTv = (MarqueeTextView) mRootView.findViewById(R.id.sign_tv);
        mFriends = (RelativeLayout) mRootView.findViewById(R.id.friends);
        mFriendsNumTv = (ExTextView) mRootView.findViewById(R.id.friends_num_tv);
        mFans = (RelativeLayout) mRootView.findViewById(R.id.fans);
        mFansNumTv = (ExTextView) mRootView.findViewById(R.id.fans_num_tv);
        mFollows = (RelativeLayout) mRootView.findViewById(R.id.follows);
        mFollowsNumTv = (ExTextView) mRootView.findViewById(R.id.follows_num_tv);

        mRefreshLayout.setEnableRefresh(true);
        mRefreshLayout.setEnableLoadMore(false);
        mRefreshLayout.setEnableLoadMoreWhenContentNotFull(true);
        mRefreshLayout.setEnableOverScrollDrag(true);
        mRefreshLayout.setRefreshHeader(new ClassicsHeader(getContext()));
        mRefreshLayout.setOnRefreshLoadMoreListener(new OnRefreshLoadMoreListener() {
            @Override
            public void onLoadMore(@NonNull RefreshLayout refreshLayout) {

            }

            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {
                mPersonCorePresenter.getHomePage((int) MyUserInfoManager.getInstance().getUid(), true);
            }
        });

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
//                Bundle bundle = new Bundle();
//                bundle.putString(ImageBigPreviewFragment.BIG_IMAGE_PATH, MyUserInfoManager.getInstance().getAvatar());
//                U.getFragmentUtils().addFragment(
//                        FragmentUtils.newAddParamsBuilder(getActivity(), ImageBigPreviewFragment.class)
//                                .setAddToBackStack(true)
//                                .setHasAnimation(true)
//                                .setBundle(bundle)
//                                .build());
            }
        });

        RxView.clicks(mFriends)
                .throttleFirst(500, TimeUnit.MILLISECONDS)
                .subscribe(new Consumer<Object>() {
                    @Override
                    public void accept(Object o) {
                        // 好友，双向关注
                        openRelationFragment(RelationFragment.FROM_FRIENDS);
                    }
                });

        RxView.clicks(mFans)
                .throttleFirst(500, TimeUnit.MILLISECONDS)
                .subscribe(new Consumer<Object>() {
                    @Override
                    public void accept(Object o) {
                        // 粉丝，我关注的
                        openRelationFragment(RelationFragment.FROM_FANS);
                    }
                });

        RxView.clicks(mFollows)
                .throttleFirst(500, TimeUnit.MILLISECONDS)
                .subscribe(new Consumer<Object>() {
                    @Override
                    public void accept(Object o) {
                        // 关注, 关注我的
                        openRelationFragment(RelationFragment.FROM_FOLLOW);
                    }
                });
    }

    private void openRelationFragment(int mode) {
        Bundle bundle = new Bundle();
        bundle.putInt(RelationFragment.FROM_PAGE_KEY, mode);
        bundle.putInt(RelationFragment.FRIEND_NUM_KEY, mFriendNum);
        bundle.putInt(RelationFragment.FOLLOW_NUM_KEY, mFocusNum);
        bundle.putInt(RelationFragment.FANS_NUM_KEY, mFansNum);
        U.getFragmentUtils().addFragment(
                FragmentUtils.newAddParamsBuilder(getActivity(), RelationFragment.class)
                        .setBundle(bundle)
                        .setAddToBackStack(true)
                        .setHasAnimation(true)
                        .build());
    }

    private void initMedalView() {
        mMedalLayout = (ExRelativeLayout) mRootView.findViewById(R.id.medal_layout);
        mRankNumTv = (ExTextView) mRootView.findViewById(R.id.rank_num_tv);
        mSingendNumTv = (ExTextView) mRootView.findViewById(R.id.singend_num_tv);
        mLevelView = (NormalLevelView2) mRootView.findViewById(R.id.level_view);
        mLevelTv = (ExTextView) mRootView.findViewById(R.id.level_tv);

        mRankArea = (RelativeLayout) mRootView.findViewById(R.id.rank_area);
        mRankText = (ExTextView) mRootView.findViewById(R.id.rank_text);
        mRankDiffIv = (ExImageView) mRootView.findViewById(R.id.rank_diff_iv);
        mMedalIv = (ExImageView) mRootView.findViewById(R.id.medal_iv);

        LinearLayout linearLayout = (LinearLayout) getActivity().getLayoutInflater().inflate(R.layout.area_diff_popup_window_layout, null);
        mPopArea = (LinearLayout) linearLayout.findViewById(R.id.pop_area);
        mRankDiffIcon = (ImageView) linearLayout.findViewById(R.id.rank_diff_icon);
        mRankDiffTv = (ExTextView) linearLayout.findViewById(R.id.rank_diff_tv);
        mPopupWindow = new PopupWindow(linearLayout);
        mPopupWindow.setOutsideTouchable(true);
    }

    private void initAudioView() {
        mAuditionArea = (RelativeLayout) mRootView.findViewById(R.id.audition_area);
        RxView.clicks(mAuditionArea)
                .throttleFirst(500, TimeUnit.MILLISECONDS)
                .subscribe(new Consumer<Object>() {
                    @Override
                    public void accept(Object o) {
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

    @Override
    protected void onFragmentVisible() {
        super.onFragmentVisible();
        mPersonCorePresenter.getHomePage((int) MyUserInfoManager.getInstance().getUid(), false);
        mPersonCorePresenter.getRankLevel(false);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvnet(MyUserInfoEvent.UserInfoChangeEvent userInfoChangeEvent) {
        initViewData();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(RelationChangeEvent event) {
        getRelationNums();
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(FollowNotifyEvent event) {
        getRelationNums();
    }

    private void getRelationNums() {
        UserInfoServerApi userInfoServerApi = ApiManager.getInstance().createService(UserInfoServerApi.class);
        ApiMethods.subscribe(userInfoServerApi.getRelationNum((int) MyUserInfoManager.getInstance().getUid()), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult result) {
                if (result.getErrno() == 0) {
                    List<RelationNumModel> relationNumModels = JSON.parseArray(result.getData().getString("cnt"), RelationNumModel.class);
                    if (relationNumModels != null && relationNumModels.size() > 0) {
                        for (RelationNumModel mode : relationNumModels) {
                            if (mode.getRelation() == UserInfoManager.RELATION_FRIENDS) {
                                mFriendNum = mode.getCnt();
                            } else if (mode.getRelation() == UserInfoManager.RELATION_FANS) {
                                mFansNum = mode.getCnt();
                            } else if (mode.getRelation() == UserInfoManager.RELATION_FOLLOW) {
                                mFocusNum = mode.getCnt();
                            }
                        }
                    }
                    refreshRelationNum();
                }
            }
        }, this);

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
    public void showRankView(UserRankModel userRankModel) {
        MyLog.d(TAG, "showRankView" + " userRankModel=" + userRankModel);

        if (userRankModel.getDiff() == 0) {
            // 默认按照上升显示
            mRankDiffIv.setVisibility(View.GONE);
            mRankText.setText(highlight(userRankModel.getText(), userRankModel.getHighlight(), true));
        } else if (userRankModel.getDiff() > 0) {
            mRankDiffIv.setVisibility(View.VISIBLE);
            mRankDiffIv.setImageResource(R.drawable.shangsheng_ic);
            mRankText.setText(highlight(userRankModel.getText(), userRankModel.getHighlight(), true));
        } else if (userRankModel.getDiff() < 0) {
            mRankDiffIv.setVisibility(View.VISIBLE);
            mRankDiffIv.setImageResource(R.drawable.xiajiang_ic);
            mRankText.setText(highlight(userRankModel.getText(), userRankModel.getHighlight(), false));
        }

        showPopWindow(userRankModel.getDiff());

        if (userRankModel.getBadge() == STAR_BADGE) {
            mMedalIv.setBackground(getResources().getDrawable(R.drawable.paiming));
        } else if (userRankModel.getBadge() == TOP_BADGE) {
            mMedalIv.setBackground(getResources().getDrawable(R.drawable.paihang));
        } else if (userRankModel.getBadge() == SHANDIAN_BADGE) {
            mMedalIv.setBackground(getResources().getDrawable(R.drawable.dabai));
        }
    }

    private void showPopWindow(int diff) {
        if (mPopupWindow.isShowing()) {
            mPopupWindow.dismiss();
        }

        if (diff == 0) {
            return;
        }

        String content = "";
        if (diff > 0) {
            content = "上升" + diff + "名";
            mRankDiffTv.setText(content);
            mPopArea.setBackground(getResources().getDrawable(R.drawable.shangsheng_bj));
            mRankDiffIcon.setImageResource(R.drawable.shangsheng_smail);

            mPopupWindow.setWidth(U.getDisplayUtils().dip2px(36) + content.length() * U.getDisplayUtils().dip2px(10));
            mPopupWindow.setHeight(U.getDisplayUtils().dip2px(31));
            mRankText.post(new Runnable() {
                @Override
                public void run() {
                    if (PersonFragment.this.fragmentVisible && U.getFragmentUtils().getTopFragment(getActivity()) == PersonFragment.this) {
                        mPopupWindow.showAsDropDown(mRankText);
                    }
                }
            });
        } else {
            content = "下降" + Math.abs(diff) + "名";
            mRankDiffTv.setText(content);
            mPopArea.setBackground(getResources().getDrawable(R.drawable.xiajiang_bj));
            mRankDiffIcon.setImageResource(R.drawable.xiajiang_cry);

            mPopupWindow.setWidth(U.getDisplayUtils().dip2px(36) + content.length() * U.getDisplayUtils().dip2px(10));
            mPopupWindow.setHeight(U.getDisplayUtils().dip2px(31));
            mRankText.post(new Runnable() {
                @Override
                public void run() {
                    if (PersonFragment.this.fragmentVisible && U.getFragmentUtils().getTopFragment(getActivity()) == PersonFragment.this) {
                        mPopupWindow.showAsDropDown(mRankText);
                    }
                }
            });
        }

    }

    private SpannableString highlight(String text, String target, boolean isUp) {
        SpannableString spannableString = new SpannableString(text);
        Pattern pattern = Pattern.compile(target);
        Matcher matcher = pattern.matcher(text);
        while (matcher.find()) {
            ForegroundColorSpan span = new ForegroundColorSpan(Color.parseColor("#FF3B3C"));
            spannableString.setSpan(span, matcher.start(), matcher.end(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        return spannableString;
    }


    @Override
    public void showUserInfo(UserInfoModel userInfoModel) {
        mRefreshLayout.finishRefresh();
    }

    @Override
    public void showRelationNum(List<RelationNumModel> list) {
        mRefreshLayout.finishRefresh();
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
        mRefreshLayout.finishRefresh();
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

//        if (reginRankModel != null && reginRankModel.getRankSeq() != 0) {
//            mRankTv.setText(reginRankModel.getRegionDesc() + "第" + String.valueOf(reginRankModel.getRankSeq()) + "位");
//        } else if (countryRankModel != null && countryRankModel.getRankSeq() != 0) {
//            mRankTv.setText(countryRankModel.getRegionDesc() + "第" + String.valueOf(countryRankModel.getRankSeq()) + "位");
//        } else {
//            mRankTv.setText(getResources().getString(R.string.default_rank_text));
//        }
    }

    @Override
    public void showUserLevel(List<UserLevelModel> list) {
        mRefreshLayout.finishRefresh();
        // 展示段位信息
        for (UserLevelModel userLevelModel : list) {
            if (userLevelModel.getType() == UserLevelModel.RANKING_TYPE) {
                rank = userLevelModel.getScore();
            } else if (userLevelModel.getType() == UserLevelModel.SUB_RANKING_TYPE) {
                subRank = userLevelModel.getScore();
                levelDesc = userLevelModel.getDesc();
            } else if (userLevelModel.getType() == UserLevelModel.TOTAL_RANKING_STAR_TYPE) {
                starNum = userLevelModel.getScore();
            } else if (userLevelModel.getType() == UserLevelModel.REAL_RANKING_STAR_TYPE) {
                starLimit = userLevelModel.getScore();
            }
        }
        mLevelView.bindData(rank, subRank);
        mLevelTv.setText(levelDesc);
    }

    @Override
    public void showGameStatic(List<GameStatisModel> list) {
        mRefreshLayout.finishRefresh();
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

    @Override
    public boolean isInViewPager() {
        return true;
    }
}
