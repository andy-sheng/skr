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
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.android.arouter.launcher.ARouter;
import com.common.base.BaseFragment;
import com.common.core.avatar.AvatarUtils;
import com.common.core.myinfo.MyUserInfoManager;
import com.common.core.myinfo.event.MyUserInfoEvent;
import com.common.core.permission.SkrCameraPermission;
import com.common.core.upgrade.UpgradeData;
import com.common.core.upgrade.UpgradeManager;
import com.common.core.userinfo.UserInfoManager;
import com.common.core.userinfo.event.RelationChangeEvent;
import com.common.core.userinfo.model.GameStatisModel;
import com.common.core.userinfo.model.UserInfoModel;
import com.common.core.userinfo.model.UserLevelModel;
import com.common.core.userinfo.model.UserRankModel;
import com.common.log.MyLog;
import com.common.notification.event.FollowNotifyEvent;
import com.common.utils.SpanUtils;
import com.common.utils.U;
import com.common.view.AnimateClickListener;
import com.common.view.DebounceViewClickListener;
import com.common.view.ex.ExImageView;
import com.common.view.ex.ExRelativeLayout;
import com.common.view.ex.ExTextView;
import com.common.view.titlebar.CommonTitleBar;
import com.common.view.viewpager.NestViewPager;
import com.common.view.viewpager.SlidingTabLayout;
import com.component.busilib.constans.GameModeType;
import com.component.busilib.view.BitmapTextView;
import com.facebook.drawee.view.SimpleDraweeView;
import com.module.RouterConstants;
import com.module.home.R;
import com.module.home.persenter.PersonCorePresenter;
import com.module.home.view.IPersonView;
import com.respicker.ResPicker;
import com.respicker.activity.ResPickerActivity;
import com.respicker.model.ImageItem;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.header.ClassicsHeader;
import com.scwang.smartrefresh.layout.listener.OnRefreshLoadMoreListener;
import com.zq.level.view.NormalLevelView2;
import com.zq.live.proto.Common.ESex;
import com.zq.live.proto.Common.UserInfo;
import com.zq.person.view.PhotoWallView;
import com.zq.person.view.ProducationWallView;
import com.zq.person.view.RequestCallBack;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import model.RelationNumModel;

/**
 * 自己
 * 带作品的照片墙
 */
public class PersonFragment3 extends BaseFragment implements IPersonView, RequestCallBack {

    SmartRefreshLayout mSmartRefresh;
    ClassicsHeader mClassicsHeader;
    ConstraintLayout mUserInfoArea;

    SimpleDraweeView mAvatarIv;

    ImageView mSettingImgIv;
    ExImageView mSettingRedDot;
    ExTextView mNameTv;
    ImageView mSexIv;
    ExTextView mUseridTv;
    ExTextView mSignTv;

    ExImageView mIncomeIv;
    ExImageView mWalletIv;
    ExImageView mRechargeIv;

    AppBarLayout mAppbar;
    Toolbar mToolbar;
    TextView mSrlNameTv;

    PersonCorePresenter mPresenter;

    SlidingTabLayout mPersonTab;
    NestViewPager mPersonVp;
    PagerAdapter mPersonTabAdapter;

    PhotoWallView mPhotoWallView;
    ProducationWallView mProducationWallView;

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
        mPresenter.getHomePage(false);
        if (mPhotoWallView != null && mPersonVp.getCurrentItem() == 0) {
            mPhotoWallView.getPhotos(false);
        }
        if (mProducationWallView != null && mPersonVp.getCurrentItem() == 1) {
            mProducationWallView.getProducations(false);
        }
    }

    @Override
    protected void onFragmentInvisible() {
        super.onFragmentInvisible();
        if (mProducationWallView != null) {
            mProducationWallView.stopPlay();
        }
    }

    private void initBaseContainArea() {
        mSmartRefresh = (SmartRefreshLayout) mRootView.findViewById(R.id.smart_refresh);
        mClassicsHeader = (ClassicsHeader) mRootView.findViewById(R.id.classics_header);
        mUserInfoArea = (ConstraintLayout) mRootView.findViewById(R.id.user_info_area);

        mAppbar = (AppBarLayout) mRootView.findViewById(R.id.appbar);
        mToolbar = (Toolbar) mRootView.findViewById(R.id.toolbar);
        mSrlNameTv = (TextView) mRootView.findViewById(R.id.srl_name_tv);

        mSmartRefresh.setEnableRefresh(true);
        mSmartRefresh.setEnableLoadMore(true);
        mSmartRefresh.setEnableLoadMoreWhenContentNotFull(false);
        mSmartRefresh.setEnableOverScrollDrag(true);
        mClassicsHeader.setBackgroundColor(Color.parseColor("#1f0e26"));
        mSmartRefresh.setRefreshHeader(mClassicsHeader);
        mSmartRefresh.setOnRefreshLoadMoreListener(new OnRefreshLoadMoreListener() {
            @Override
            public void onLoadMore(@NonNull RefreshLayout refreshLayout) {
                if (mPhotoWallView != null && mPersonVp.getCurrentItem() == 0) {
                    mPhotoWallView.getMorePhotos();
                }
                if (mProducationWallView != null && mPersonVp.getCurrentItem() == 1) {
                    mProducationWallView.getMoreProducations();
                }
            }

            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {
                mPresenter.getHomePage(true);
                if (mPhotoWallView != null && mPersonVp.getCurrentItem() == 0) {
                    mPhotoWallView.getPhotos(true);
                }
                if (mProducationWallView != null && mPersonVp.getCurrentItem() == 1) {
                    mProducationWallView.getProducations(true);
                }
            }
        });

        mAppbar.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                if (verticalOffset == 0) {
                    // 展开状态
                    if (mToolbar.getVisibility() != View.GONE) {
                        mToolbar.setVisibility(View.GONE);
                    }
                } else if (Math.abs(verticalOffset) >= (appBarLayout.getTotalScrollRange() - U.getDisplayUtils().dip2px(70))) {
                    // 完全收缩状态
                    if (mToolbar.getVisibility() != View.VISIBLE) {
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
        mAvatarIv = (SimpleDraweeView) mRootView.findViewById(R.id.avatar_iv);
        mNameTv = (ExTextView) mRootView.findViewById(R.id.name_tv);
        mSexIv = (ImageView) mRootView.findViewById(R.id.sex_iv);
        mUseridTv = (ExTextView) mRootView.findViewById(R.id.userid_tv);
        mSignTv = (ExTextView) mRootView.findViewById(R.id.sign_tv);

        mAvatarIv.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                ARouter.getInstance().build(RouterConstants.ACTIVITY_EDIT_INFO)
                        .navigation();
            }
        });
    }

    private void initSettingArea() {
        mSettingImgIv = (ImageView) mRootView.findViewById(R.id.setting_img_iv);
        mSettingRedDot = (ExImageView) mRootView.findViewById(R.id.setting_red_dot);

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
        mWalletIv = (ExImageView) mRootView.findViewById(R.id.wallet_iv);
        mIncomeIv = (ExImageView) mRootView.findViewById(R.id.income_iv);
        mRechargeIv = (ExImageView)mRootView.findViewById(R.id.recharge_iv);


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
                // TODO: 2019-06-19 充值入口
            }
        });
    }

    private void initPersonArea() {
        mPersonTab = (SlidingTabLayout) mRootView.findViewById(R.id.person_tab);
        mPersonVp = (NestViewPager) mRootView.findViewById(R.id.person_vp);

        mPersonTab.setCustomTabView(R.layout.person_tab_view, R.id.tab_tv);
        mPersonTab.setSelectedIndicatorColors(U.getColor(com.component.busilib.R.color.black_trans_20));
        mPersonTab.setDistributeMode(SlidingTabLayout.DISTRIBUTE_MODE_TAB_IN_SECTION_CENTER);
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
                        mPhotoWallView = new PhotoWallView(PersonFragment3.this, PersonFragment3.this);
                    }
                    if (container.indexOfChild(mPhotoWallView) == -1) {
                        container.addView(mPhotoWallView);
                    }
                    return mPhotoWallView;
                } else if (position == 1) {
                    // 作品
                    UserInfoModel userInfoModel = new UserInfoModel();
                    userInfoModel.setUserId((int) MyUserInfoManager.getInstance().getUid());
                    userInfoModel.setNickname(MyUserInfoManager.getInstance().getNickName());
                    userInfoModel.setAvatar(MyUserInfoManager.getInstance().getAvatar());
                    if (mProducationWallView == null) {
                        mProducationWallView = new ProducationWallView(PersonFragment3.this, userInfoModel, PersonFragment3.this);
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
                return 2;
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
                    return "作品";
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
                        mPhotoWallView.getPhotos(false);
                    }
                } else if (position == 1) {
                    if (mProducationWallView != null) {
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
    }

    private void showCharmsTotal(int meiLiCntTotal) {
        // TODO: 2019-06-19 魅力值
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
            mNameTv.setText(MyUserInfoManager.getInstance().getNickName());
            if (MyUserInfoManager.getInstance().getSex() == ESex.SX_MALE.getValue()) {
                mSexIv.setVisibility(View.VISIBLE);
                mSexIv.setBackgroundResource(R.drawable.sex_man_icon);
            } else if (MyUserInfoManager.getInstance().getSex() == ESex.SX_FEMALE.getValue()) {
                mSexIv.setVisibility(View.VISIBLE);
                mSexIv.setBackgroundResource(R.drawable.sex_woman_icon);
            } else {
                mSexIv.setVisibility(View.GONE);
            }
            mUseridTv.setText("撕歌号：" + MyUserInfoManager.getInstance().getUid());
            mSrlNameTv.setText(MyUserInfoManager.getInstance().getNickName());
            mSignTv.setText(MyUserInfoManager.getInstance().getSignature());
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvnet(MyUserInfoEvent.UserInfoChangeEvent userInfoChangeEvent) {
        refreshUserInfoView();
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
    }

    @Override
    public void onRequestSucess() {
        mSmartRefresh.finishLoadMore();
    }
}


