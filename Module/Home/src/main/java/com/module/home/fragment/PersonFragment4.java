package com.module.home.fragment;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.AppBarLayout;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.text.SpannableStringBuilder;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.alibaba.android.arouter.launcher.ARouter;
import com.common.base.BaseFragment;
import com.common.core.avatar.AvatarUtils;
import com.common.core.myinfo.MyUserInfo;
import com.common.core.myinfo.MyUserInfoManager;
import com.common.core.myinfo.event.MyUserInfoEvent;
import com.common.core.upgrade.UpgradeData;
import com.common.core.upgrade.UpgradeManager;
import com.common.core.userinfo.UserInfoManager;
import com.common.core.userinfo.model.GameStatisModel;
import com.common.core.userinfo.model.UserInfoModel;
import com.common.core.userinfo.model.UserLevelModel;
import com.common.core.userinfo.model.UserRankModel;
import com.common.image.fresco.FrescoWorker;
import com.common.image.model.ImageFactory;
import com.common.log.MyLog;
import com.common.statistics.StatisticsAdapter;
import com.common.utils.FragmentUtils;
import com.common.utils.SpanUtils;
import com.common.utils.U;
import com.common.view.AnimateClickListener;
import com.common.view.DebounceViewClickListener;
import com.common.view.ex.ExConstraintLayout;
import com.common.view.ex.ExImageView;
import com.common.view.ex.ExTextView;
import com.common.view.viewpager.NestViewPager;
import com.common.view.viewpager.SlidingTabLayout;
import com.facebook.drawee.view.SimpleDraweeView;
import com.module.ModuleServiceManager;
import com.module.RouterConstants;
import com.module.feeds.IFeedsModuleService;
import com.module.feeds.IPersonFeedsWall;
import com.module.home.R;
import com.module.home.persenter.PersonCorePresenter;
import com.module.home.view.IPersonView;
import com.orhanobut.dialogplus.DialogPlus;
import com.orhanobut.dialogplus.ViewHolder;
import com.respicker.ResPicker;
import com.respicker.activity.ResPickerActivity;
import com.respicker.model.ImageItem;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshHeader;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.SimpleMultiPurposeListener;
import com.component.dialog.BusinessCardDialogView;
import com.component.level.view.NormalLevelView2;
import com.zq.live.proto.Common.ESex;
import com.component.person.utils.StringFromatUtils;
import com.component.person.fragment.OtherPersonFragment4;
import com.component.person.photo.view.PhotoWallView;
import com.component.person.producation.view.ProducationWallView;
import com.component.person.view.RequestCallBack;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import com.component.person.model.RelationNumModel;

/**
 * 自己
 * 带作品的照片墙
 */
public class PersonFragment4 extends BaseFragment implements IPersonView, RequestCallBack {

    SmartRefreshLayout mSmartRefresh;
    ExConstraintLayout mUserInfoArea;

    ImageView mSettingImgIv;
    ExImageView mSettingRedDot;

    SimpleDraweeView mImageBg;
    ImageView mSexIv;
    SimpleDraweeView mAvatarIv;
    NormalLevelView2 mLevelView;
    ExTextView mCharmTv;

    ExTextView mNameTv;
    ExTextView mSignTv;

    ImageView mBusinessCard;

    ExTextView mFriendsNumTv;
    ExTextView mFansNumTv;
    ExTextView mFollowsNumTv;

    ExImageView mIncomeIv;
    ExImageView mWalletIv;
    ExImageView mRechargeIv;

    AppBarLayout mAppbar;
    Toolbar mToolbar;
    ConstraintLayout mToolbarLayout;

    SimpleDraweeView mSrlAvatarIv;
    TextView mSrlNameTv;
    ImageView mSrlSexIv;
    ExTextView mSrlCharmTv;

    PersonCorePresenter mPresenter;

    SlidingTabLayout mPersonTab;
    NestViewPager mPersonVp;
    PagerAdapter mPersonTabAdapter;

    PhotoWallView mPhotoWallView;
    IPersonFeedsWall mFeedsWallView;
    ProducationWallView mProducationWallView;

    DialogPlus mDialogPlus;
    boolean isInitToolbar = false;

    int mFriendNum = 0;
    int mFansNum = 0;
    int mFocusNum = 0;
    int mCharmNum = 0;

    @Override
    public int initView() {
        return R.layout.person_fragment3_layout;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        mPresenter = new PersonCorePresenter(this);
        addPresent(mPresenter);

        initBaseContainArea();
        initUserInfoArea();
        initSettingArea();
        initFunctionArea();
        initPersonArea();

        refreshUserInfoView();
    }

    @Override
    protected void onFragmentVisible() {
        super.onFragmentVisible();
        StatisticsAdapter.recordCountEvent("Metab", "expose", null);
        mPresenter.getHomePage(false);
        if (mPhotoWallView != null && mPersonVp.getCurrentItem() == 0) {
            mPhotoWallView.getPhotos(false);
        }
        if (mFeedsWallView != null && mPersonVp.getCurrentItem() == 1) {
            mFeedsWallView.getFeeds(false);
        }
        if (mProducationWallView != null && mPersonVp.getCurrentItem() == 2) {
            mProducationWallView.getProducations(false);
        }
    }

    @Override
    protected void onFragmentInvisible(int from) {
        super.onFragmentInvisible(from);
        if (mProducationWallView != null) {
            mProducationWallView.stopPlay();
        }
        if (mFeedsWallView != null) {
            mFeedsWallView.unselected();
        }
        if (mDialogPlus != null) {
            mDialogPlus.dismiss(false);
        }
    }

    private void initBaseContainArea() {
        mSmartRefresh = getRootView().findViewById(R.id.smart_refresh);
        mUserInfoArea = getRootView().findViewById(R.id.user_info_area);

        mImageBg = getRootView().findViewById(R.id.image_bg);
        mAppbar = getRootView().findViewById(R.id.appbar);
        mToolbar = getRootView().findViewById(R.id.toolbar);
        mToolbarLayout = getRootView().findViewById(R.id.toolbar_layout);
        mSrlAvatarIv = getRootView().findViewById(R.id.srl_avatar_iv);
        mSrlNameTv = getRootView().findViewById(R.id.srl_name_tv);
        mSrlSexIv = getRootView().findViewById(R.id.srl_sex_iv);
        mSrlCharmTv = (ExTextView) getRootView().findViewById(R.id.srl_charm_tv);

        FrescoWorker.loadImage(mImageBg, ImageFactory.newPathImage(OtherPersonFragment4.PERSON_CENTER_TOP_ICON)
                .build());

        mSmartRefresh.setEnableRefresh(true);
        mSmartRefresh.setEnableLoadMore(true);
        mSmartRefresh.setEnableLoadMoreWhenContentNotFull(false);
        mSmartRefresh.setEnableOverScrollDrag(true);
        mSmartRefresh.setHeaderMaxDragRate(1.5f);
        mSmartRefresh.setOnMultiPurposeListener(new SimpleMultiPurposeListener() {

            float lastScale = 0;

            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {
                super.onRefresh(refreshLayout);
                mPresenter.getHomePage(true);
                MyLog.d("PersonFragment4", "mPersonVp.getCurrentItem() = " + mPersonVp.getCurrentItem());
                if (mPhotoWallView != null && mPersonVp.getCurrentItem() == 0) {
                    mPhotoWallView.getPhotos(true);
                }
                if (mFeedsWallView != null && mPersonVp.getCurrentItem() == 1) {
                    mFeedsWallView.getFeeds(true);
                }
                if (mProducationWallView != null && mPersonVp.getCurrentItem() == 2) {
                    mProducationWallView.getProducations(true);
                }
            }

            @Override
            public void onLoadMore(@NonNull RefreshLayout refreshLayout) {
                super.onLoadMore(refreshLayout);
                if (mPhotoWallView != null && mPersonVp.getCurrentItem() == 0) {
                    mPhotoWallView.getMorePhotos();
                }
                if (mFeedsWallView != null && mPersonVp.getCurrentItem() == 1) {
                    mFeedsWallView.getMoreFeeds();
                }
                if (mProducationWallView != null && mPersonVp.getCurrentItem() == 2) {
                    mProducationWallView.getMoreProducations();
                }
            }

            @Override
            public void onHeaderMoving(RefreshHeader header, boolean isDragging, float percent, int offset, int headerHeight, int maxDragHeight) {
                super.onHeaderMoving(header, isDragging, percent, offset, headerHeight, maxDragHeight);
                float scale = (float) offset / (float) U.getDisplayUtils().dip2px(300) + 1;
                if (Math.abs(scale - lastScale) >= 0.01) {
                    // TODO: 2019-06-23 不要加平移，会闪动，让设计把图给大一点可以避免
                    lastScale = scale;
                    mImageBg.setScaleX(scale);
                    mImageBg.setScaleY(scale);
                }
            }
        });

        mAppbar.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                // TODO: 2019-06-23  可以加效果，看产品需求
                mImageBg.setTranslationY(verticalOffset);
                int srollLimit = appBarLayout.getTotalScrollRange() - U.getDisplayUtils().dip2px(55);
                if (U.getDeviceUtils().hasNotch(U.app())) {
                    srollLimit = srollLimit - U.getStatusBarUtil().getStatusBarHeight(U.app());
                }
                if (verticalOffset == 0) {
                    // 展开状态
                    if (mToolbar.getVisibility() != View.GONE) {
                        mToolbar.setVisibility(View.GONE);
                    }
                } else if (Math.abs(verticalOffset) >= srollLimit) {
                    // 完全收缩状态
                    if (mToolbar.getVisibility() != View.VISIBLE) {
                        if (U.getDeviceUtils().hasNotch(U.app()) && !isInitToolbar) {
                            ViewGroup.LayoutParams params = mToolbarLayout.getLayoutParams();
                            params.height = params.height + U.getStatusBarUtil().getStatusBarHeight(U.app());
                            mToolbarLayout.setLayoutParams(params);
                            isInitToolbar = true;
                        }
                        mToolbar.setVisibility(View.VISIBLE);
                    }
                } else {
                    // TODO: 2019/4/8 过程中，可以加动画，先直接显示
                    if (mToolbar.getVisibility() != View.GONE) {
                        mToolbar.setVisibility(View.GONE);
                    }
                }

            }
        });
    }

    private void initUserInfoArea() {
        mAvatarIv = (SimpleDraweeView) getRootView().findViewById(R.id.avatar_iv);
        mLevelView = (NormalLevelView2) getRootView().findViewById(R.id.level_view);
        mNameTv = (ExTextView) getRootView().findViewById(R.id.name_tv);
        mSexIv = (ImageView) getRootView().findViewById(R.id.sex_iv);
        mSignTv = (ExTextView) getRootView().findViewById(R.id.sign_tv);
        mCharmTv = (ExTextView) getRootView().findViewById(R.id.charm_tv);

        mBusinessCard = (ImageView) getRootView().findViewById(R.id.business_card);

        mAvatarIv.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                ARouter.getInstance().build(RouterConstants.ACTIVITY_EDIT_INFO)
                        .navigation();
            }
        });

        mBusinessCard.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                // TODO: 2019-06-19 打开名片页面
                showBusinessCard();
            }
        });
    }

    private void showBusinessCard() {
        UserInfoModel userInfoModel = MyUserInfo.toUserInfoModel(MyUserInfoManager.getInstance().getMyUserInfo());
        BusinessCardDialogView businessCardDialogView = new BusinessCardDialogView(getContext(), userInfoModel, mFansNum, mCharmNum);
        mDialogPlus = DialogPlus.newDialog(getActivity())
                .setContentHolder(new ViewHolder(businessCardDialogView))
                .setGravity(Gravity.CENTER)
                .setMargin(U.getDisplayUtils().dip2px(40), -1, U.getDisplayUtils().dip2px(40), -1)
                .setContentBackgroundResource(R.color.transparent)
                .setOverlayBackgroundResource(R.color.black_trans_80)
                .setExpanded(false)
                .create();
        mDialogPlus.show();
    }

    private void initSettingArea() {
        mSettingImgIv = (ImageView) getRootView().findViewById(R.id.setting_img_iv);
        mSettingRedDot = (ExImageView) getRootView().findViewById(R.id.setting_red_dot);

        mSettingImgIv.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                ARouter.getInstance()
                        .build(RouterConstants.ACTIVITY_SETTING)
                        .navigation();
            }
        });
        updateSettingRedDot();
    }


    private void updateSettingRedDot() {
        if (UpgradeManager.getInstance().needShowRedDotTips()) {
            mSettingRedDot.setVisibility(View.VISIBLE);
        } else {
            mSettingRedDot.setVisibility(View.GONE);
        }
    }

    private void initFunctionArea() {
        mFriendsNumTv = (ExTextView) getRootView().findViewById(R.id.friends_num_tv);
        mFollowsNumTv = (ExTextView) getRootView().findViewById(R.id.follows_num_tv);
        mFansNumTv = (ExTextView) getRootView().findViewById(R.id.fans_num_tv);

        mWalletIv = (ExImageView) getRootView().findViewById(R.id.wallet_iv);
        mIncomeIv = (ExImageView) getRootView().findViewById(R.id.income_iv);
        mRechargeIv = (ExImageView) getRootView().findViewById(R.id.recharge_iv);

        mWalletIv.setOnClickListener(new AnimateClickListener() {
            @Override
            public void click(View view) {
                ARouter.getInstance()
                        .build(RouterConstants.ACTIVITY_DIAMOND_BALANCE)
                        .navigation();
            }
        });

        mIncomeIv.setOnClickListener(new AnimateClickListener() {
            @Override
            public void click(View view) {
                ARouter.getInstance()
                        .build(RouterConstants.ACTIVITY_INCOME)
                        .navigation();
            }
        });

        mRechargeIv.setOnClickListener(new AnimateClickListener() {
            @Override
            public void click(View view) {
                U.getFragmentUtils().addFragment(
                        FragmentUtils.newAddParamsBuilder(getActivity(), BallanceFragment.class)
                                .setAddToBackStack(true)
                                .setHasAnimation(true)
                                .build());
            }
        });

        mFriendsNumTv.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                // 好友，双向关注
                openRelationFragment(UserInfoManager.RELATION.FRIENDS.getValue());
            }
        });

        mFansNumTv.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                // 粉丝，我关注的
                openRelationFragment(UserInfoManager.RELATION.FANS.getValue());
            }
        });

        mFollowsNumTv.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                // 关注, 关注我的
                openRelationFragment(UserInfoManager.RELATION.FOLLOW.getValue());
            }
        });
    }

    private void openRelationFragment(int mode) {
        Bundle bundle = new Bundle();
        bundle.putInt("from_page_key", mode);
        bundle.putInt("friend_num_key", mFriendNum);
        bundle.putInt("follow_num_key", mFocusNum);
        bundle.putInt("fans_num_key", mFansNum);
        ARouter.getInstance()
                .build(RouterConstants.ACTIVITY_RELATION)
                .with(bundle)
                .navigation();

    }

    private void initPersonArea() {
        mPersonTab = (SlidingTabLayout) getRootView().findViewById(R.id.person_tab);
        mPersonVp = (NestViewPager) getRootView().findViewById(R.id.person_vp);

        mPersonTab.setCustomTabView(R.layout.person_tab_view, R.id.tab_tv);
        mPersonTab.setSelectedIndicatorColors(U.getColor(R.color.black_trans_20));
        mPersonTab.setDistributeMode(SlidingTabLayout.DISTRIBUTE_MODE_NONE);
        mPersonTab.setIndicatorAnimationMode(SlidingTabLayout.ANI_MODE_NONE);
        mPersonTab.setIndicatorWidth(U.getDisplayUtils().dip2px(67));
        mPersonTab.setIndicatorBottomMargin(U.getDisplayUtils().dip2px(12));
        mPersonTab.setSelectedIndicatorThickness(U.getDisplayUtils().dip2px(28));
        mPersonTab.setIndicatorCornorRadius(U.getDisplayUtils().dip2px(14));
        mPersonTabAdapter = new PagerAdapter() {
            @Override
            public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
                container.removeView((View) object);
            }

            @NonNull
            @Override
            public Object instantiateItem(@NonNull ViewGroup container, int position) {
                if (position == 0) {
                    // 照片墙
                    if (mPhotoWallView == null) {
                        mPhotoWallView = new PhotoWallView(PersonFragment4.this, PersonFragment4.this);
                        // 第一次拉歌曲
                        mPhotoWallView.getPhotos(false);
                    }
                    if (container.indexOfChild(mPhotoWallView) == -1) {
                        container.addView(mPhotoWallView);
                    }
                    return mPhotoWallView;
                } else if (position == 1) {
                    // 神曲
                    UserInfoModel userInfoModel = MyUserInfo.toUserInfoModel(MyUserInfoManager.getInstance().getMyUserInfo());
                    if (mFeedsWallView == null) {
                        IFeedsModuleService feedsModuleService = ModuleServiceManager.getInstance().getFeedsService();
                        mFeedsWallView = feedsModuleService.getPersonFeedsWall(PersonFragment4.this, userInfoModel, PersonFragment4.this);
                    }
                    if (container.indexOfChild((View) mFeedsWallView) == -1) {
                        container.addView((View) mFeedsWallView);
                    }
                    return mFeedsWallView;
                } else if (position == 2) {
                    // 作品
                    UserInfoModel userInfoModel = MyUserInfo.toUserInfoModel(MyUserInfoManager.getInstance().getMyUserInfo());
                    if (mProducationWallView == null) {
                        mProducationWallView = new ProducationWallView(PersonFragment4.this, userInfoModel, PersonFragment4.this);
                    }
                    if (container.indexOfChild(mProducationWallView) == -1) {
                        container.addView(mProducationWallView);
                    }
                    return mProducationWallView;
                }
                return super.instantiateItem(container, position);
            }

            @Override
            public int getCount() {
                return 3;
            }

            @Override
            public int getItemPosition(@NonNull Object object) {
                return POSITION_NONE;
            }

            @Nullable
            @Override
            public CharSequence getPageTitle(int position) {
                if (position == 0) {
                    return "相册";
                } else if (position == 1) {
                    return "小音乐";
                } else if (position == 2) {
                    return "录音";
                }
                return "";
            }

            @Override
            public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
                return view == (object);
            }
        };
        mPersonVp.setAdapter(mPersonTabAdapter);
        mPersonTab.setViewPager(mPersonVp);
        mPersonTabAdapter.notifyDataSetChanged();

        mPersonVp.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (position == 0) {
                    if (mPhotoWallView != null) {
                        mSmartRefresh.setEnableLoadMore(mPhotoWallView.getMHasMore());
                        mPhotoWallView.getPhotos(false);
                    }
                    if (mFeedsWallView != null) {
                        mFeedsWallView.unselected();
                    }
                    if (mProducationWallView != null) {
                        mProducationWallView.stopPlay();
                    }
                } else if (position == 1) {
                    if (mFeedsWallView != null) {
                        mSmartRefresh.setEnableLoadMore(mFeedsWallView.isHasMore());
                        mFeedsWallView.getFeeds(false);
                    }
                    if (mProducationWallView != null) {
                        mProducationWallView.stopPlay();
                    }
                } else if (position == 2) {
                    if (mFeedsWallView != null) {
                        mFeedsWallView.unselected();
                    }
                    if (mProducationWallView != null) {
                        mSmartRefresh.setEnableLoadMore(mProducationWallView.getHasMore());
                        mProducationWallView.getProducations(false);
                    }
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    @Override
    public boolean onActivityResultReal(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK && requestCode == ResPickerActivity.REQ_CODE_RES_PICK) {
            List<ImageItem> imageItems = ResPicker.getInstance().getSelectedImageList();
            if (mPhotoWallView != null) {
                mPhotoWallView.uploadPhotoList(imageItems);
            }
            return true;
        }
        return super.onActivityResultReal(requestCode, resultCode, data);
    }

    @Override
    public boolean isInViewPager() {
        return true;
    }

    @Override
    public boolean useEventBus() {
        return true;
    }

    @Override
    public void showHomePageInfo(List<RelationNumModel> relationNumModels, List<UserRankModel> userRankModels
            , List<UserLevelModel> userLevelModels, List<GameStatisModel> gameStatisModels, int meiLiCntTotal) {
        mSmartRefresh.finishRefresh();
        showCharmsTotal(meiLiCntTotal);
        showUserLevel(userLevelModels);
        showRelationNum(relationNumModels);
    }

    private void showCharmsTotal(int meiLiCntTotal) {
        mCharmNum = meiLiCntTotal;
        mCharmTv.setText("魅力：" + StringFromatUtils.formatMillion(meiLiCntTotal));
        mSrlCharmTv.setText("魅力：" + StringFromatUtils.formatMillion(meiLiCntTotal));
    }

    public void showUserLevel(List<UserLevelModel> list) {
        int mainRank = 0;
        int subRank = 0;
        if (list != null && list.size() > 0) {
            for (UserLevelModel userLevelModel : list) {
                if (userLevelModel.getType() == UserLevelModel.RANKING_TYPE) {
                    mainRank = userLevelModel.getScore();
                } else if (userLevelModel.getType() == UserLevelModel.SUB_RANKING_TYPE) {
                    subRank = userLevelModel.getScore();
                }
            }
        }
        mLevelView.bindData(mainRank, subRank);
    }

    public void showRelationNum(List<RelationNumModel> list) {
        for (RelationNumModel mode : list) {
            if (mode.getRelation() == UserInfoManager.RELATION.FRIENDS.getValue()) {
                mFriendNum = mode.getCnt();
            } else if (mode.getRelation() == UserInfoManager.RELATION.FANS.getValue()) {
                mFansNum = mode.getCnt();
            } else if (mode.getRelation() == UserInfoManager.RELATION.FOLLOW.getValue()) {
                mFocusNum = mode.getCnt();
            }
        }

        refreshRelationNum();
    }

    private void refreshRelationNum() {
        SpannableStringBuilder friendBuilder = new SpanUtils()
                .append(String.valueOf(mFriendNum)).setFontSize(24, true)
                .append("好友").setFontSize(14, true).setForegroundColor(U.getColor(R.color.white_trans_50))
                .create();
        mFriendsNumTv.setText(friendBuilder);

        SpannableStringBuilder fansBuilder = new SpanUtils()
                .append(String.valueOf(mFansNum)).setFontSize(24, true)
                .append("粉丝").setFontSize(14, true).setForegroundColor(U.getColor(R.color.white_trans_50))
                .create();
        mFansNumTv.setText(fansBuilder);

        SpannableStringBuilder focusBuilder = new SpanUtils()
                .append(String.valueOf(mFocusNum)).setFontSize(24, true)
                .append("关注").setFontSize(14, true).setForegroundColor(U.getColor(R.color.white_trans_50))
                .create();
        mFollowsNumTv.setText(focusBuilder);
    }

    @Override
    public void loadHomePageFailed() {
        mSmartRefresh.finishRefresh();
    }

    private void refreshUserInfoView() {
        if (MyUserInfoManager.getInstance().hasMyUserInfo()) {
            AvatarUtils.loadAvatarByUrl(mAvatarIv, AvatarUtils.newParamsBuilder(MyUserInfoManager.getInstance().getAvatar())
                    .setBorderColor(Color.WHITE)
                    .setBorderWidth(U.getDisplayUtils().dip2px(2f))
                    .setCircle(true)
                    .build());
            AvatarUtils.loadAvatarByUrl(mSrlAvatarIv, AvatarUtils.newParamsBuilder(MyUserInfoManager.getInstance().getAvatar())
                    .setBorderColor(Color.WHITE)
                    .setBorderWidth(U.getDisplayUtils().dip2px(2f))
                    .setCircle(true)
                    .build());
            mNameTv.setText(MyUserInfoManager.getInstance().getNickName());
            mSrlNameTv.setText(MyUserInfoManager.getInstance().getNickName());
            if (MyUserInfoManager.getInstance().getSex() == ESex.SX_MALE.getValue()) {
                mSexIv.setVisibility(View.VISIBLE);
                mSexIv.setBackgroundResource(R.drawable.sex_man_icon);
                mSrlSexIv.setVisibility(View.VISIBLE);
                mSrlSexIv.setBackgroundResource(R.drawable.sex_man_icon);
            } else if (MyUserInfoManager.getInstance().getSex() == ESex.SX_FEMALE.getValue()) {
                mSexIv.setVisibility(View.VISIBLE);
                mSexIv.setBackgroundResource(R.drawable.sex_woman_icon);
                mSrlSexIv.setVisibility(View.VISIBLE);
                mSrlSexIv.setBackgroundResource(R.drawable.sex_woman_icon);
            } else {
                mSexIv.setVisibility(View.GONE);
                mSrlSexIv.setVisibility(View.GONE);
            }
            mSignTv.setText(MyUserInfoManager.getInstance().getSignature());
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvnet(MyUserInfoEvent.UserInfoChangeEvent userInfoChangeEvent) {
        refreshUserInfoView();
        if (mProducationWallView != null) {
            UserInfoModel userInfoModel = MyUserInfo.toUserInfoModel(MyUserInfoManager.getInstance().getMyUserInfo());
            mProducationWallView.setUserInfoModel(userInfoModel);
        }
    }

//    @Subscribe(threadMode = ThreadMode.MAIN)
//    public void onEvent(RelationChangeEvent event) {
//        mPresenter.getRelationNums();
//    }
//
//
//    @Subscribe(threadMode = ThreadMode.MAIN)
//    public void onEvent(FollowNotifyEvent event) {
//        mPresenter.getRelationNums();
//    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(UpgradeData.RedDotStatusEvent event) {
        updateSettingRedDot();
    }

    @Override
    public void destroy() {
        super.destroy();
        if (mDialogPlus != null) {
            mDialogPlus.dismiss(false);
        }
    }

    @Override
    public void onRequestSucess(boolean hasMore) {
        mSmartRefresh.finishRefresh();
        mSmartRefresh.setEnableLoadMore(hasMore);
        mSmartRefresh.finishLoadMore();
    }
}


