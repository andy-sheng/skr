package com.module.home.fragment;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.Group;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
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
import com.common.core.userinfo.model.ScoreStateModel;
import com.common.core.userinfo.model.UserInfoModel;
import com.common.log.MyLog;
import com.common.player.SinglePlayer;
import com.common.player.SinglePlayerCallbackAdapter;
import com.common.rxretrofit.ApiManager;
import com.common.statistics.StatisticsAdapter;
import com.common.utils.SpanUtils;
import com.common.utils.U;
import com.common.view.AnimateClickListener;
import com.common.view.DebounceViewClickListener;
import com.common.view.ex.ExImageView;
import com.common.view.ex.ExTextView;
import com.common.view.viewpager.NestViewPager;
import com.common.view.viewpager.SlidingTabLayout;
import com.component.busilib.event.PostsPublishSucessEvent;
import com.component.busilib.friends.VoiceInfoModel;
import com.component.dialog.BusinessCardDialogView;
import com.component.level.utils.LevelConfigUtils;
import com.component.person.event.ChildViewPlayAudioEvent;
import com.component.person.event.UploadMyVoiceInfo;
import com.component.person.model.RelationNumModel;
import com.component.person.model.ScoreDetailModel;
import com.component.person.photo.view.PhotoWallView;
import com.component.person.producation.view.ProducationWallView;
import com.component.person.view.CommonAudioView;
import com.component.person.view.GuardView;
import com.component.person.view.PersonTagView;
import com.component.person.view.RequestCallBack;
import com.facebook.drawee.view.SimpleDraweeView;
import com.module.ModuleServiceManager;
import com.module.RouterConstants;
import com.module.feeds.IFeedsModuleService;
import com.module.feeds.IPersonFeedsWall;
import com.module.home.R;
import com.module.home.persenter.PersonCorePresenter;
import com.module.home.view.IPersonView;
import com.module.post.IPersonPostsWall;
import com.module.post.IPostModuleService;
import com.orhanobut.dialogplus.DialogPlus;
import com.respicker.ResPicker;
import com.respicker.activity.ResPickerActivity;
import com.respicker.model.ImageItem;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshHeader;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.SimpleMultiPurposeListener;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;

/**
 * 自己
 * 带作品的照片墙
 */
public class PersonFragment5 extends BaseFragment implements IPersonView, RequestCallBack {

    SmartRefreshLayout mSmartRefresh;
    ConstraintLayout mUserInfoArea;

    ImageView mWalletIv;
    ImageView mSettingIv;
    ExImageView mSettingRedDot;

    ImageView mImageBg;
    ImageView mBottomBg;

    SimpleDraweeView mAvatarIv;
    ImageView mLevelBg;
    TextView mLevelDesc;
    ExTextView mNameTv;
    ImageView mHonorIv;
    Group mOpenHonorArea;

    ImageView mBusinessIv;
    GuardView mGuardView;

    CommonAudioView mAudioView;
    ExTextView mEditAudio;

    ExTextView mFriendsNumTv;
    ExTextView mFansNumTv;
    ExTextView mFollowsNumTv;

    AppBarLayout mAppbar;
    Toolbar mToolbar;
    ConstraintLayout mToolbarLayout;

    AppBarLayout.OnOffsetChangedListener mAppbarListener;

    TextView mSrlNameTv;

    PersonCorePresenter mPresenter;

    LinearLayout mContainer;
    SlidingTabLayout mPersonTab;
    NestViewPager mPersonVp;
    PagerAdapter mPersonTabAdapter;

    PhotoWallView mPhotoWallView;
    IPersonPostsWall mPostWallView;
    IPersonFeedsWall mFeedsWallView;
    ProducationWallView mProducationWallView;

    int srollDivider = U.getDisplayUtils().dip2px(122);  // 滑到分界线的时候

    int mFriendNum = 0;
    int mFansNum = 0;
    int mFocusNum = 0;

    int meiLiCntTotal = 0;

    private BusinessCardDialogView mBusinessCardDialogView = null;

    VoiceInfoModel mVoiceInfoModel; // 声音信息

    int lastVerticalOffset = Integer.MAX_VALUE;

    boolean isPlay = false;
    String playTag = "PersonFragment4" + hashCode();
    private SinglePlayerCallbackAdapter playCallback;

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
        adjustView();

        refreshUserInfoView();

        playCallback = new SinglePlayerCallbackAdapter() {
            @Override
            public void onCompletion() {
                super.onCompletion();
                stopPlay();
            }

            @Override
            public void onPlaytagChange(@org.jetbrains.annotations.Nullable String oldPlayerTag, @org.jetbrains.annotations.Nullable String newPlayerTag) {
                if (newPlayerTag != playTag) {
                    stopPlay();
                }
            }
        };
        SinglePlayer.INSTANCE.addCallback(playTag, playCallback);
    }

    private void stopPlay() {
        isPlay = false;
        SinglePlayer.INSTANCE.stop(playTag);
        mAudioView.setPlay(false);
    }

    private void adjustView() {
        ViewGroup.LayoutParams params = mToolbarLayout.getLayoutParams();
        params.height = params.height + U.getStatusBarUtil().getStatusBarHeight(U.app());
        mToolbarLayout.setLayoutParams(params);

        if (U.getDeviceUtils().hasNotch(U.app())) {
            CollapsingToolbarLayout.LayoutParams layoutParams = (CollapsingToolbarLayout.LayoutParams) mUserInfoArea.getLayoutParams();
            layoutParams.topMargin = layoutParams.topMargin + U.getStatusBarUtil().getStatusBarHeight(U.app());
            mUserInfoArea.setLayoutParams(layoutParams);

            RelativeLayout.LayoutParams bottomParams = (RelativeLayout.LayoutParams) mBottomBg.getLayoutParams();
            bottomParams.topMargin = bottomParams.topMargin + U.getStatusBarUtil().getStatusBarHeight(U.app());
            mBottomBg.setLayoutParams(bottomParams);

            ViewGroup.LayoutParams containerParams = mContainer.getLayoutParams();
            containerParams.height = U.getDisplayUtils().getScreenHeight() - U.getDisplayUtils().dip2px(56 + 54);
            mContainer.setLayoutParams(containerParams);
        } else {
            ViewGroup.LayoutParams containerParams = mContainer.getLayoutParams();
            containerParams.height = U.getDisplayUtils().getScreenHeight() - U.getDisplayUtils().dip2px(56 + 54) - U.getStatusBarUtil().getStatusBarHeight(U.app());
            mContainer.setLayoutParams(containerParams);

            srollDivider = srollDivider - U.getStatusBarUtil().getStatusBarHeight(U.app());
        }
    }


    @Override
    protected void onFragmentVisible() {
        super.onFragmentVisible();
        StatisticsAdapter.recordCountEvent("Metab", "expose", null);
        mPresenter.getHomePage(false);
        viewSelected(mPersonVp.getCurrentItem());
    }

    @Override
    protected void onFragmentInvisible(int from) {
        super.onFragmentInvisible(from);
        stopPlay();
        if (mProducationWallView != null) {
            mProducationWallView.stopPlay();
        }
        if (mPostWallView != null) {
            mPostWallView.unselected(1);
        }
        if (mFeedsWallView != null) {
            mFeedsWallView.unselected(1);
        }
    }

    private void initBaseContainArea() {
        mSmartRefresh = getRootView().findViewById(R.id.smart_refresh);
        mUserInfoArea = getRootView().findViewById(R.id.user_info_area);

        mImageBg = getRootView().findViewById(R.id.image_bg);
        mBottomBg = getRootView().findViewById(R.id.bottom_bg);
        mAppbar = getRootView().findViewById(R.id.appbar);
        mToolbar = getRootView().findViewById(R.id.toolbar);
        mToolbarLayout = getRootView().findViewById(R.id.toolbar_layout);
        mSrlNameTv = getRootView().findViewById(R.id.srl_name_tv);

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
                if (mPostWallView != null && mPersonVp.getCurrentItem() == 1) {
                    mPostWallView.getPosts(true);
                }
                if (mFeedsWallView != null && mPersonVp.getCurrentItem() == 2) {
                    mFeedsWallView.getFeeds(true);
                }
                if (mProducationWallView != null && mPersonVp.getCurrentItem() == 3) {
                    mProducationWallView.getProducations(true);
                }
            }

            @Override
            public void onLoadMore(@NonNull RefreshLayout refreshLayout) {
                super.onLoadMore(refreshLayout);
                if (mPhotoWallView != null && mPersonVp.getCurrentItem() == 0) {
                    mPhotoWallView.getMorePhotos();
                }
                if (mPostWallView != null && mPersonVp.getCurrentItem() == 1) {
                    mPostWallView.getMorePosts();
                }
                if (mFeedsWallView != null && mPersonVp.getCurrentItem() == 2) {
                    mFeedsWallView.getMoreFeeds();
                }
                if (mProducationWallView != null && mPersonVp.getCurrentItem() == 3) {
                    mProducationWallView.getMoreProducations();
                }
            }

            @Override
            public void onHeaderMoving(RefreshHeader header, boolean isDragging, float percent, int offset, int headerHeight, int maxDragHeight) {
                super.onHeaderMoving(header, isDragging, percent, offset, headerHeight, maxDragHeight);
                float scale = (float) offset / (float) U.getDisplayUtils().dip2px(300) + 1;
                if (Math.abs(scale - lastScale) >= 0.01) {
                    lastScale = scale;
                    mImageBg.setScaleX(scale);
                    mImageBg.setScaleY(scale);
                }
                if (offset > 0) {
                    mBottomBg.setTranslationY(offset);
                }
            }
        });

        if (mAppbarListener == null) {
            mAppbarListener = new AppBarLayout.OnOffsetChangedListener() {
                @Override
                public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                    // TODO: 2019-06-23  可以加效果，看产品需求
                    mImageBg.setTranslationY(verticalOffset);
                    if (verticalOffset < 0) {
                        mBottomBg.setTranslationY(verticalOffset);
                    }
                    if (lastVerticalOffset != verticalOffset) {
                        lastVerticalOffset = verticalOffset;

                        int srollLimit = appBarLayout.getTotalScrollRange();  // 总的滑动长度
                        if (verticalOffset == 0) {
                            // 展开状态
                            if (mToolbar.getVisibility() != View.GONE) {
                                mToolbar.setVisibility(View.GONE);
                                mToolbarLayout.setVisibility(View.GONE);
                            }
                        } else if (Math.abs(verticalOffset) >= srollDivider) {
                            // 完全收缩状态
                            if (mToolbar.getVisibility() != View.VISIBLE) {
                                mToolbar.setVisibility(View.VISIBLE);
                                mToolbarLayout.setVisibility(View.VISIBLE);
                            }

                            if (Math.abs(verticalOffset) >= srollLimit) {
                                mSrlNameTv.setAlpha(1);
                            } else {
                                mSrlNameTv.setAlpha((float) (Math.abs(verticalOffset) - srollDivider) / (float) (srollLimit - srollDivider));
                            }
                        } else {
                            // TODO: 2019/4/8 过程中，可以加动画，先直接显示
                            if (mToolbar.getVisibility() != View.GONE) {
                                mToolbar.setVisibility(View.GONE);
                                mToolbarLayout.setVisibility(View.GONE);
                            }
                        }
                    }
                }
            };
        }
        mAppbar.removeOnOffsetChangedListener(mAppbarListener);
        lastVerticalOffset = Integer.MAX_VALUE;
        mAppbar.addOnOffsetChangedListener(mAppbarListener);
    }

    private void initUserInfoArea() {
        mAvatarIv = getRootView().findViewById(R.id.avatar_iv);
        mLevelBg = getRootView().findViewById(R.id.level_bg);
        mLevelDesc = getRootView().findViewById(R.id.level_desc);

        mNameTv = getRootView().findViewById(R.id.name_tv);
        mHonorIv = getRootView().findViewById(R.id.honor_iv);
        mOpenHonorArea = getRootView().findViewById(R.id.open_honor_area);

        mBusinessIv = getRootView().findViewById(R.id.business_iv);
        mGuardView = getRootView().findViewById(R.id.guard_view);

        mAudioView = getRootView().findViewById(R.id.audio_view);
        mEditAudio = getRootView().findViewById(R.id.edit_audio);

        mGuardView.setClickListener(new Function1<UserInfoModel, Unit>() {
            @Override
            public Unit invoke(UserInfoModel userInfoModel) {
                if (userInfoModel == null) {
                    // 去守护
                    ARouter.getInstance().build(RouterConstants.ACTIVITY_WEB)
                            .withString(RouterConstants.KEY_WEB_URL, ApiManager.getInstance().findRealUrlByChannel("https://dev.app.inframe.mobi/user/protector?title=1&userID=" + MyUserInfoManager.INSTANCE.getUid()))
                            .navigation();
                } else {
                    // 跳到个人主页
                    if (userInfoModel.getUserId() == MyUserInfoManager.INSTANCE.getUid()) {
                        ARouter.getInstance().build(RouterConstants.ACTIVITY_WEB)
                                .withString(RouterConstants.KEY_WEB_URL, ApiManager.getInstance().findRealUrlByChannel("https://dev.app.inframe.mobi/user/protector?title=1&userID=" + MyUserInfoManager.INSTANCE.getUid()))
                                .navigation();
                    } else {
                        Bundle bundle = new Bundle();
                        bundle.putInt("bundle_user_id", userInfoModel.getUserId());
                        ARouter.getInstance()
                                .build(RouterConstants.ACTIVITY_OTHER_PERSON)
                                .with(bundle)
                                .navigation();
                    }
                }
                return null;
            }
        });

        mAvatarIv.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                ARouter.getInstance().build(RouterConstants.ACTIVITY_EDIT_INFO)
                        .navigation();
            }
        });

        mBusinessIv.setOnClickListener(new AnimateClickListener() {
            @Override
            public void click(View view) {
                if (mBusinessCardDialogView != null) {
                    mBusinessCardDialogView.dismiss(false);
                }
                mBusinessCardDialogView = new BusinessCardDialogView(getActivity(), MyUserInfo.toUserInfoModel(MyUserInfoManager.INSTANCE.getMyUserInfo()), meiLiCntTotal, mFansNum);
                mBusinessCardDialogView.showByDialog();
            }
        });

        mAudioView.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                if (mVoiceInfoModel != null) {
                    if (isPlay) {
                        // 暂停音乐
                        isPlay = false;
                        mAudioView.setPlay(false);
                        SinglePlayer.INSTANCE.stop(playTag);
                    } else {
                        // 播放音乐
                        isPlay = true;
                        if (mPostWallView != null) {
                            mPostWallView.stopPlay();
                        }
                        if (mFeedsWallView != null) {
                            mFeedsWallView.stopPlay();
                        }
                        if (mProducationWallView != null) {
                            mProducationWallView.stopPlay();
                        }
                        mAudioView.setPlay(true);
                        if (!TextUtils.isEmpty(mVoiceInfoModel.getVoiceURL())) {
                            SinglePlayer.INSTANCE.startPlay(playTag, mVoiceInfoModel.getVoiceURL());
                        } else {
                            MyLog.e("PersonFragment4", "非voiceInfo 空的url");
                        }
                    }
                } else {
                    MyLog.e("PersonFragment4", "空的voiceInfo");
                }
            }
        });

        mEditAudio.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                ARouter.getInstance().build(RouterConstants.ACTIVITY_VOICE_RECORD)
                        .withInt("from", 2)
                        .navigation();
            }
        });

        getRootView().findViewById(R.id.open_honor_tv).setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                ARouter.getInstance().build(RouterConstants.ACTIVITY_WEB)
                        .withString("url", ApiManager.getInstance().findRealUrlByChannel("https://app.inframe.mobi/user/vip?title=1"))
                        .greenChannel().navigation();
            }
        });

        mHonorIv.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                ARouter.getInstance().build(RouterConstants.ACTIVITY_WEB)
                        .withString("url", ApiManager.getInstance().findRealUrlByChannel("https://app.inframe.mobi/user/vip?title=1"))
                        .greenChannel().navigation();
            }
        });
    }

    private void initSettingArea() {
        mSettingIv = getRootView().findViewById(R.id.setting_iv);
        mSettingRedDot = getRootView().findViewById(R.id.setting_red_dot);

        mWalletIv = getRootView().findViewById(R.id.wallet_iv);


        mSettingIv.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                ARouter.getInstance()
                        .build(RouterConstants.ACTIVITY_SETTING)
                        .navigation();
            }
        });
        mWalletIv.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                ARouter.getInstance()
                        .build(RouterConstants.ACTIVITY_WALLET)
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
        mContainer = getRootView().findViewById(R.id.container);
        mPersonTab = getRootView().findViewById(R.id.person_tab);
        mPersonVp = getRootView().findViewById(R.id.person_vp);

        mPersonTab.setCustomTabView(R.layout.person_tab_view, R.id.tab_tv);
        mPersonTab.setSelectedIndicatorColors(Color.parseColor("#A6BCF2"));
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
                        mPhotoWallView = new PhotoWallView(PersonFragment5.this, PersonFragment5.this);
                        if (PersonFragment5.this.getFragmentVisible()) {
                            mPhotoWallView.getPhotos(false);
                        }
                    }
                    if (container.indexOfChild(mPhotoWallView) == -1) {
                        if (mPhotoWallView.getParent() != null) {
                            ((ViewGroup) mPhotoWallView.getParent()).removeView(mPhotoWallView);
                        }
                        container.addView(mPhotoWallView);
                    }
                    return mPhotoWallView;
                } else if (position == 1) {
                    // 帖子
                    UserInfoModel userInfoModel = MyUserInfo.toUserInfoModel(MyUserInfoManager.INSTANCE.getMyUserInfo());
                    if (mPostWallView == null) {
                        IPostModuleService postModuleService = ModuleServiceManager.getInstance().getPostsService();
                        mPostWallView = postModuleService.getPostsWall(PersonFragment5.this.getActivity(), userInfoModel, PersonFragment5.this);
                    }
                    View childView = (View) mPostWallView;
                    if (container.indexOfChild(childView) == -1) {
                        if (childView.getParent() != null) {
                            ((ViewGroup) childView.getParent()).removeView(childView);
                        }
                        container.addView(childView);
                    }
                    return mPostWallView;
                } else if (position == 2) {
                    // 神曲
                    UserInfoModel userInfoModel = MyUserInfo.toUserInfoModel(MyUserInfoManager.INSTANCE.getMyUserInfo());
                    if (mFeedsWallView == null) {
                        IFeedsModuleService feedsModuleService = ModuleServiceManager.getInstance().getFeedsService();
                        mFeedsWallView = feedsModuleService.getPersonFeedsWall(PersonFragment5.this, userInfoModel, PersonFragment5.this);
                    }
                    View childView = (View) mFeedsWallView;
                    if (container.indexOfChild(childView) == -1) {
                        if (childView.getParent() != null) {
                            ((ViewGroup) childView.getParent()).removeView(childView);
                        }
                        container.addView(childView);
                    }
                    return mFeedsWallView;
                } else if (position == 3) {
                    // 作品
                    UserInfoModel userInfoModel = MyUserInfo.toUserInfoModel(MyUserInfoManager.INSTANCE.getMyUserInfo());
                    if (mProducationWallView == null) {
                        mProducationWallView = new ProducationWallView(PersonFragment5.this, userInfoModel, PersonFragment5.this);
                    }
                    if (container.indexOfChild(mProducationWallView) == -1) {
                        if (mProducationWallView.getParent() != null) {
                            ((ViewGroup) mProducationWallView.getParent()).removeView(mProducationWallView);
                        }
                        container.addView(mProducationWallView);
                    }
                    return mProducationWallView;
                }
                return super.instantiateItem(container, position);
            }

            @Override
            public int getCount() {
                return 4;
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
                    return "帖子";
                } else if (position == 2) {
                    return "神曲";
                } else if (position == 3) {
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
                viewSelected(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    private void viewSelected(int position) {
        if (position == 0) {
            if (mPhotoWallView != null) {
                mSmartRefresh.setEnableLoadMore(mPhotoWallView.getMHasMore());
                mPhotoWallView.getPhotos(false);
            }
            if (mPostWallView != null) {
                mPostWallView.unselected(1);
            }
            if (mFeedsWallView != null) {
                mFeedsWallView.unselected(1);
            }
            if (mProducationWallView != null) {
                mProducationWallView.stopPlay();
            }
        } else if (position == 1) {
            if (mFeedsWallView != null) {
                mFeedsWallView.unselected(1);
            }
            if (mPostWallView != null) {
                mSmartRefresh.setEnableLoadMore(mPostWallView.isHasMore());
                mPostWallView.selected();
            }
            if (mProducationWallView != null) {
                mProducationWallView.stopPlay();
            }
        } else if (position == 2) {
            if (mFeedsWallView != null) {
                mSmartRefresh.setEnableLoadMore(mFeedsWallView.isHasMore());
                mFeedsWallView.selected();
            }
            if (mPostWallView != null) {
                mPostWallView.unselected(1);
            }
            if (mProducationWallView != null) {
                mProducationWallView.stopPlay();
            }
        } else if (position == 3) {
            if (mFeedsWallView != null) {
                mFeedsWallView.unselected(1);
            }
            if (mPostWallView != null) {
                mPostWallView.unselected(1);
            }
            if (mProducationWallView != null) {
                mSmartRefresh.setEnableLoadMore(mProducationWallView.getHasMore());
                mProducationWallView.getProducations(false);
            }
        }
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
    public void showHomePageInfo(List<RelationNumModel> relationNumModels, int meiLiCntTotal, VoiceInfoModel voiceInfoModel, List<UserInfoModel> guardsList) {
        mSmartRefresh.finishRefresh();
        showRelationNum(relationNumModels);
        showVoiceInfo(voiceInfoModel);
        showGuardView(guardsList);
        refreshUserInfoView();
        this.meiLiCntTotal = meiLiCntTotal;
    }

    private void showGuardView(List<UserInfoModel> guardsList) {
        mGuardView.bindData(guardsList);
    }

    private void showVoiceInfo(VoiceInfoModel voiceInfoModel) {
        mVoiceInfoModel = voiceInfoModel;
        if (voiceInfoModel != null) {
            if (voiceInfoModel.getAuditStatus() == VoiceInfoModel.EVAS_UN_AUDIT) {
                // 未审核
                mAudioView.bindData(voiceInfoModel.getDuration(), "审核中");
            } else {
                mAudioView.bindData(voiceInfoModel.getDuration());
            }
            mAudioView.setVisibility(View.VISIBLE);
            mEditAudio.setText("+编辑语音");
        } else {
            mEditAudio.setText("+添加声音签名");
            mAudioView.setVisibility(View.GONE);
        }
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

//        mPersonTagView.setFansNum(mFansNum);
        refreshRelationNum();
    }

    private void refreshRelationNum() {
        SpannableStringBuilder friendBuilder = new SpanUtils()
                .append(String.valueOf(mFriendNum)).setForegroundColor(U.getColor(R.color.black_trans_80)).setFontSize(24, true).setBold()
                .append(" 好友").setFontSize(14, true).setForegroundColor(U.getColor(R.color.black_trans_50))
                .create();
        mFriendsNumTv.setText(friendBuilder);

        SpannableStringBuilder fansBuilder = new SpanUtils()
                .append(String.valueOf(mFansNum)).setForegroundColor(U.getColor(R.color.black_trans_80)).setFontSize(24, true).setBold()
                .append(" 粉丝").setFontSize(14, true).setForegroundColor(U.getColor(R.color.black_trans_50))
                .create();
        mFansNumTv.setText(fansBuilder);

        SpannableStringBuilder focusBuilder = new SpanUtils()
                .append(String.valueOf(mFocusNum)).setForegroundColor(U.getColor(R.color.black_trans_80)).setFontSize(24, true).setBold()
                .append(" 关注").setFontSize(14, true).setForegroundColor(U.getColor(R.color.black_trans_50))
                .create();
        mFollowsNumTv.setText(focusBuilder);
    }

    @Override
    public void loadHomePageFailed() {
        mSmartRefresh.finishRefresh();
    }

    private void refreshUserInfoView() {
        if (MyUserInfoManager.INSTANCE.hasMyUserInfo()) {
            AvatarUtils.loadAvatarByUrl(mAvatarIv, AvatarUtils.newParamsBuilder(MyUserInfoManager.INSTANCE.getAvatar())
                    .setCircle(true)
                    .build());
            mNameTv.setText(MyUserInfoManager.INSTANCE.getNickName());
            mSrlNameTv.setText(MyUserInfoManager.INSTANCE.getNickName());

            if (MyUserInfoManager.INSTANCE.getHonorInfo() != null &&
                    MyUserInfoManager.INSTANCE.getHonorInfo().isHonor()) {
                mHonorIv.setVisibility(View.VISIBLE);
                mOpenHonorArea.setVisibility(View.GONE);
            } else {
                mHonorIv.setVisibility(View.GONE);
                mOpenHonorArea.setVisibility(View.VISIBLE);
            }

            ScoreStateModel ranking = MyUserInfoManager.INSTANCE.getRanking();
            if (ranking != null && LevelConfigUtils.getRaceCenterAvatarBg(ranking.getMainRanking()) != 0) {
                mLevelBg.setVisibility(View.VISIBLE);
                mLevelDesc.setVisibility(View.VISIBLE);
                mLevelBg.setBackground(U.getDrawable(LevelConfigUtils.getRaceCenterAvatarBg(ranking.getMainRanking())));
                mLevelDesc.setText(ranking.getRankingDesc());
            } else {
                mLevelBg.setVisibility(View.GONE);
                mLevelDesc.setVisibility(View.GONE);
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvnet(MyUserInfoEvent.UserInfoChangeEvent userInfoChangeEvent) {
        refreshUserInfoView();
        UserInfoModel userInfoModel = MyUserInfo.toUserInfoModel(MyUserInfoManager.INSTANCE.getMyUserInfo());
        if (mProducationWallView != null) {
            mProducationWallView.setUserInfoModel(userInfoModel);
        }
        if (mFeedsWallView != null) {
            mFeedsWallView.setUserInfoModel(userInfoModel);
        }
        if (mPostWallView != null) {
            mPostWallView.setUserInfoModel(userInfoModel);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(UploadMyVoiceInfo event) {
        showVoiceInfo(event.getModel());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(ChildViewPlayAudioEvent event) {
        stopPlay();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(UpgradeData.RedDotStatusEvent event) {
        updateSettingRedDot();
    }

    /**
     * 帖子发布成功跳转
     *
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(PostsPublishSucessEvent event) {
        mPersonVp.setCurrentItem(1, false);
        if (mPostWallView != null) {
            mPostWallView.getPosts(true);
        }
    }

    @Override
    public void destroy() {
        super.destroy();
        mAppbar.removeOnOffsetChangedListener(mAppbarListener);
        SinglePlayer.INSTANCE.release(playTag);
        SinglePlayer.INSTANCE.removeCallback(playTag);
        if (mPhotoWallView != null) {
            mPhotoWallView.destory();
        }
        if (mProducationWallView != null) {
            mProducationWallView.destory();
        }
        if (mPostWallView != null) {
            mPostWallView.destroy();
        }
        if (mFeedsWallView != null) {
            mFeedsWallView.destroy();
        }
        if (mBusinessCardDialogView != null) {
            mBusinessCardDialogView.dismiss(false);
        }
    }

    @Override
    public void onRequestSucess(boolean hasMore) {
        mSmartRefresh.finishRefresh();
        mSmartRefresh.setEnableLoadMore(hasMore);
        mSmartRefresh.finishLoadMore();
    }
}


