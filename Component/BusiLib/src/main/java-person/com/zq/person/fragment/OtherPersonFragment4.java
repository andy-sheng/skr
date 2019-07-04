package com.zq.person.fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.alibaba.android.arouter.launcher.ARouter;
import com.common.base.BaseFragment;
import com.common.core.avatar.AvatarUtils;
import com.common.core.myinfo.MyUserInfo;
import com.common.core.myinfo.MyUserInfoManager;
import com.common.core.userinfo.UserInfoManager;
import com.common.core.userinfo.event.RelationChangeEvent;
import com.common.core.userinfo.event.RemarkChangeEvent;
import com.common.core.userinfo.model.GameStatisModel;
import com.common.core.userinfo.model.UserInfoModel;
import com.common.core.userinfo.model.UserLevelModel;
import com.common.core.userinfo.model.UserRankModel;
import com.common.flowlayout.FlowLayout;
import com.common.flowlayout.TagAdapter;
import com.common.flowlayout.TagFlowLayout;
import com.common.image.fresco.FrescoWorker;
import com.common.image.model.ImageFactory;
import com.common.image.model.oss.OssImgFactory;
import com.common.utils.FragmentUtils;
import com.common.utils.ImageUtils;
import com.common.utils.U;
import com.common.view.AnimateClickListener;
import com.common.view.DebounceViewClickListener;
import com.common.view.ex.ExImageView;
import com.common.view.ex.ExTextView;
import com.common.view.viewpager.NestViewPager;
import com.common.view.viewpager.SlidingTabLayout;
import com.component.busilib.R;
import com.dialog.view.TipsDialogView;
import com.facebook.drawee.drawable.ScalingUtils;
import com.facebook.drawee.view.SimpleDraweeView;
import com.imagebrowse.big.BigImageBrowseFragment;
import com.module.ModuleServiceManager;
import com.module.RouterConstants;
import com.module.home.IHomeService;
import com.orhanobut.dialogplus.DialogPlus;
import com.orhanobut.dialogplus.OnDismissListener;
import com.orhanobut.dialogplus.ViewHolder;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshHeader;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.header.ClassicsHeader;
import com.scwang.smartrefresh.layout.listener.OnRefreshLoadMoreListener;
import com.scwang.smartrefresh.layout.listener.SimpleMultiPurposeListener;
import com.zq.dialog.BusinessCardDialogView;
import com.zq.level.view.NormalLevelView2;
import com.zq.live.proto.Common.ESex;
import com.zq.person.StringFromatUtils;
import com.zq.person.model.TagModel;
import com.zq.person.presenter.OtherPersonPresenter;
import com.zq.person.view.EditRemarkView;
import com.zq.person.view.IOtherPersonView;
import com.zq.person.view.OtherPhotoWallView;
import com.zq.person.view.PersonMoreOpView;
import com.zq.person.view.ProducationWallView;
import com.zq.person.view.RequestCallBack;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import model.RelationNumModel;

import static com.zq.person.activity.OtherPersonActivity.BUNDLE_USER_ID;

public class OtherPersonFragment4 extends BaseFragment implements IOtherPersonView, RequestCallBack {

    public static final String PERSON_CENTER_TOP_ICON = "http://res-static.inframe.mobi/app/person_center_top_bg.png";

    public static final int RELATION_FOLLOWED = 1; // 已关注关系
    public static final int RELATION_UN_FOLLOW = 2; // 未关注关系

    private static final int CHARM_TAG = 0;            // 魅力值
    private static final int FANS_NUM_TAG = 1;         // 粉丝数标签
    private static final int LOCATION_TAG = 2;         // 地区标签  省

    private List<TagModel> mTags = new ArrayList<>();  //标签
    private HashMap<Integer, String> mHashMap = new HashMap();

    TagAdapter mTagAdapter;
    int fansNum = 0; // 粉丝数
    int charmNum = 0; // 魅力值

    boolean isAppbarCanSrcoll = true;  // AppBarLayout是否可以滚动

    UserInfoModel mUserInfoModel = new UserInfoModel();
    int mUserId;

    DialogPlus mDialogPlus;

    OtherPersonPresenter mPresenter;

    SimpleDraweeView mImageBg;
    SmartRefreshLayout mSmartRefresh;
    AppBarLayout mAppbar;
    CollapsingToolbarLayout mToolbarLayout;
    ConstraintLayout mUserInfoArea;

    ExImageView mIvBack;
    ExImageView mMoreBtn;
    PersonMoreOpView mPersonMoreOpView;

    ImageView mAvatarBg;
    SimpleDraweeView mAvatarIv;
    NormalLevelView2 mLevelView;
    ExTextView mNameTv;
    ImageView mSexIv;
    ImageView mBusinessCard;
    ExTextView mSignTv;
    TagFlowLayout mFlowlayout;
    TextView mUseridTv;

    Toolbar mToolbar;
    TextView mSrlNameTv;

    SlidingTabLayout mPersonTab;
    NestViewPager mPersonVp;
    PagerAdapter mPersonTabAdapter;

    OtherPhotoWallView mOtherPhotoWallView;
    ProducationWallView mProducationWallView;

    LinearLayout mFunctionArea;
    ExTextView mFollowIv;
    ExTextView mMessageIv;

    DialogPlus mEditRemarkDialog;

    @Override
    public int initView() {
        return R.layout.other_person_fragment_layout;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        initBaseContainArea();
        initTopArea();
        initUserInfoArea();
        initPersonTabArea();
        initFunctionArea();

        mPresenter = new OtherPersonPresenter(this);
        addPresent(mPresenter);

        Bundle bundle = getArguments();
        if (bundle != null) {
            mUserId = bundle.getInt(BUNDLE_USER_ID);
            mUserInfoModel.setUserId(mUserId);
            mPresenter.getHomePage(mUserId);
        }

        if (mUserId == MyUserInfoManager.getInstance().getUid()) {
            mFunctionArea.setVisibility(View.GONE);
            mMoreBtn.setVisibility(View.GONE);
        }
    }


    private void initBaseContainArea() {
        mImageBg = (SimpleDraweeView) mRootView.findViewById(R.id.image_bg);
        mSmartRefresh = (SmartRefreshLayout) mRootView.findViewById(R.id.smart_refresh);
        mAppbar = (AppBarLayout) mRootView.findViewById(R.id.appbar);
        mToolbarLayout = (CollapsingToolbarLayout) mRootView.findViewById(R.id.toolbar_layout);
        mUserInfoArea = (ConstraintLayout) mRootView.findViewById(R.id.user_info_area);
        mToolbar = (Toolbar) mRootView.findViewById(R.id.toolbar);
        mSrlNameTv = (TextView) mRootView.findViewById(R.id.srl_name_tv);

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
                mPresenter.getHomePage(mUserId);
                if (mOtherPhotoWallView != null && mPersonVp.getCurrentItem() == 0) {
                    mOtherPhotoWallView.getPhotos(true);
                }
                if (mProducationWallView != null && mPersonVp.getCurrentItem() == 1) {
                    mProducationWallView.getProducations(true);
                }
            }

            @Override
            public void onLoadMore(@NonNull RefreshLayout refreshLayout) {
                if (mOtherPhotoWallView != null && mPersonVp.getCurrentItem() == 0) {
                    mOtherPhotoWallView.getMorePhotos();
                }
                if (mProducationWallView != null && mPersonVp.getCurrentItem() == 1) {
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
            }
        });

        mAppbar.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                // TODO: 2019-06-23 也可以加效果，看产品怎么说
                mImageBg.setTranslationY(verticalOffset);
                if (verticalOffset == 0) {
                    // 展开状态
                    if (mToolbar.getVisibility() != View.GONE) {
                        mToolbar.setVisibility(View.GONE);
                    }
                } else if (Math.abs(verticalOffset) >= appBarLayout.getTotalScrollRange()) {
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

    private void initTopArea() {
        mIvBack = (ExImageView) mRootView.findViewById(R.id.iv_back);
        mMoreBtn = (ExImageView) mRootView.findViewById(R.id.more_btn);

        mIvBack.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                if (getActivity() != null) {
                    getActivity().finish();
                }
            }
        });

        mMoreBtn.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                if (mPersonMoreOpView != null) {
                    mPersonMoreOpView.dismiss();
                }
                mPersonMoreOpView = new PersonMoreOpView(getContext(), mUserInfoModel.getUserId(), mUserInfoModel.isFollow(), false);
                mPersonMoreOpView.setListener(new PersonMoreOpView.Listener() {
                    @Override
                    public void onClickRemark() {
                        if (mPersonMoreOpView != null) {
                            mPersonMoreOpView.dismiss();
                        }
                        // TODO: 2019/5/22 修改备注昵称
                        showRemarkDialog();
                    }

                    @Override
                    public void onClickUnFollow() {
                        if (mPersonMoreOpView != null) {
                            mPersonMoreOpView.dismiss();
                        }
                        unFollow(mUserInfoModel);
                    }

                    @Override
                    public void onClickReport() {
                        if (mPersonMoreOpView != null) {
                            mPersonMoreOpView.dismiss();
                        }

                        IHomeService channelService = (IHomeService) ARouter.getInstance().build(RouterConstants.SERVICE_HOME).navigation();
                        Class<BaseFragment> baseFragmentClass = (Class) channelService.getData(3, null);
                        U.getFragmentUtils().addFragment(
                                FragmentUtils.newAddParamsBuilder(getActivity(), baseFragmentClass)
                                        .setAddToBackStack(true)
                                        .setHasAnimation(true)
                                        .addDataBeforeAdd(0, 1)
                                        .addDataBeforeAdd(1, mUserId)
                                        .setEnterAnim(R.anim.slide_in_bottom)
                                        .setExitAnim(R.anim.slide_out_bottom)
                                        .build());
                    }

                    @Override
                    public void onClickKick() {

                    }

                    @Override
                    public void onClickBlack(boolean isInBlack) {
                        if (mPersonMoreOpView != null) {
                            mPersonMoreOpView.dismiss();
                        }

                        if (isInBlack) {
                            UserInfoManager.getInstance().removeBlackList(mUserId, new UserInfoManager.ResponseCallBack() {
                                @Override
                                public void onServerSucess(Object o) {
                                    U.getToastUtil().showShort("移除黑名单成功");
                                }

                                @Override
                                public void onServerFailed() {

                                }
                            });
                        } else {
                            UserInfoManager.getInstance().addToBlacklist(mUserId, new UserInfoManager.ResponseCallBack() {
                                @Override
                                public void onServerSucess(Object o) {
                                    U.getToastUtil().showShort("加入黑名单成功");
                                }

                                @Override
                                public void onServerFailed() {

                                }
                            });
                        }

                    }
                });
                mPersonMoreOpView.showAt(mMoreBtn);
            }
        });
    }

    private void showRemarkDialog() {
        EditRemarkView editRemarkView = new EditRemarkView(getActivity(), mUserInfoModel.getNickname(), mUserInfoModel.getNicknameRemark(null));
        editRemarkView.setListener(new EditRemarkView.Listener() {
            @Override
            public void onClickCancel() {
                if (mEditRemarkDialog != null) {
                    mEditRemarkDialog.dismiss();
                }
                U.getKeyBoardUtils().hideSoftInputKeyBoard(getActivity());
            }

            @Override
            public void onClickSave(String remarkName) {
                if (mEditRemarkDialog != null) {
                    mEditRemarkDialog.dismiss();
                }
                U.getKeyBoardUtils().hideSoftInputKeyBoard(getActivity());
                if (TextUtils.isEmpty(remarkName) && TextUtils.isEmpty(mUserInfoModel.getNicknameRemark())) {
                    // 都为空
                    return;
                } else if (!TextUtils.isEmpty(mUserInfoModel.getNicknameRemark()) && (mUserInfoModel.getNicknameRemark()).equals(remarkName)) {
                    // 相同
                    return;
                } else {
                    UserInfoManager.getInstance().updateRemark(remarkName, mUserId);
                }
            }
        });

        mEditRemarkDialog = DialogPlus.newDialog(getContext())
                .setContentHolder(new ViewHolder(editRemarkView))
                .setContentBackgroundResource(R.color.transparent)
                .setOverlayBackgroundResource(R.color.black_trans_50)
                .setInAnimation(R.anim.fade_in)
                .setOutAnimation(R.anim.fade_out)
                .setExpanded(false)
                .setGravity(Gravity.BOTTOM)
                .setOnDismissListener(new OnDismissListener() {
                    @Override
                    public void onDismiss(@NonNull DialogPlus dialog) {
                        U.getKeyBoardUtils().hideSoftInputKeyBoard(getActivity());
                    }
                })
                .create();
        mEditRemarkDialog.show();

    }

    private void initUserInfoArea() {
        mAvatarBg = (ImageView) mRootView.findViewById(R.id.avatar_bg);
        mAvatarIv = (SimpleDraweeView) mRootView.findViewById(R.id.avatar_iv);
        mLevelView = (NormalLevelView2) mRootView.findViewById(R.id.level_view);
        mNameTv = (ExTextView) mRootView.findViewById(R.id.name_tv);
        mSexIv = (ImageView) mRootView.findViewById(R.id.sex_iv);
        mBusinessCard = (ImageView) mRootView.findViewById(R.id.business_card);
        mUseridTv = (ExTextView) mRootView.findViewById(R.id.userid_tv);
        mSignTv = (ExTextView) mRootView.findViewById(R.id.sign_tv);
        mFlowlayout = (TagFlowLayout) mRootView.findViewById(R.id.flowlayout);

        mTagAdapter = new TagAdapter<TagModel>(mTags) {
            @Override
            public View getView(FlowLayout parent, int position, TagModel tagModel) {
                if (tagModel.getType() != CHARM_TAG) {
                    ExTextView tv = (ExTextView) LayoutInflater.from(getContext()).inflate(R.layout.other_person_tag_textview,
                            mFlowlayout, false);
                    tv.setText(tagModel.getContent());
                    return tv;
                } else {
                    ExTextView tv = (ExTextView) LayoutInflater.from(getContext()).inflate(R.layout.other_person_charm_tag,
                            mFlowlayout, false);
                    tv.setText(tagModel.getContent());
                    return tv;
                }
            }
        };
        mFlowlayout.setAdapter(mTagAdapter);

        mAvatarIv.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                BigImageBrowseFragment.open(false, getActivity(), mUserInfoModel.getAvatar());
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
        BusinessCardDialogView businessCardDialogView = new BusinessCardDialogView(getContext(), mUserInfoModel, fansNum, charmNum);
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


    private void setAppBarCanScroll(final boolean canScroll) {
        if (isAppbarCanSrcoll == canScroll) {
            return;
        }
        if (mAppbar != null && mAppbar.getLayoutParams() != null) {
            CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) mAppbar.getLayoutParams();
            AppBarLayout.Behavior behavior = new AppBarLayout.Behavior();
            behavior.setDragCallback(new AppBarLayout.Behavior.DragCallback() {
                @Override
                public boolean canDrag(@NonNull AppBarLayout appBarLayout) {
                    isAppbarCanSrcoll = canScroll;
                    return canScroll;
                }
            });
            params.setBehavior(behavior);
            mAppbar.setLayoutParams(params);
        }
    }

    private void initPersonTabArea() {
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
                    if (mOtherPhotoWallView == null) {
                        mOtherPhotoWallView = new OtherPhotoWallView(OtherPersonFragment4.this, mUserId, OtherPersonFragment4.this, null);
                    }
                    if (container.indexOfChild(mOtherPhotoWallView) == -1) {
                        container.addView(mOtherPhotoWallView);
                    }
                    mOtherPhotoWallView.getPhotos(false);
                    return mOtherPhotoWallView;
                } else if (position == 1) {
                    // 作品
                    if (mProducationWallView == null) {
                        mProducationWallView = new ProducationWallView(OtherPersonFragment4.this, mUserInfoModel, OtherPersonFragment4.this);
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
                    if (mOtherPhotoWallView != null) {
                        mOtherPhotoWallView.getPhotos(false);
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

    private void initFunctionArea() {
        mFunctionArea = mRootView.findViewById(R.id.function_area);
        mFollowIv = mRootView.findViewById(R.id.follow_iv);
        mMessageIv = mRootView.findViewById(R.id.message_iv);

        mFollowIv.setOnClickListener(new AnimateClickListener() {
            @Override
            public void click(View view) {
                if (!U.getNetworkUtils().hasNetwork()) {
                    U.getToastUtil().showShort("网络异常，请检查网络后重试!");
                    return;
                }
                if (mUserInfoModel != null) {
                    Integer tag = (Integer) mFollowIv.getTag();
                    if (tag != null) {
                        if (tag == RELATION_FOLLOWED) {
                            unFollow(mUserInfoModel);
                        } else if (tag == RELATION_UN_FOLLOW) {
                            UserInfoManager.getInstance().mateRelation(mUserInfoModel.getUserId(), UserInfoManager.RA_BUILD, mUserInfoModel.isFriend());
                        }
                    }
                }
            }
        });

        mMessageIv.setOnClickListener(new AnimateClickListener() {
            @Override
            public void click(View view) {
                if (mUserInfoModel != null) {
                    boolean needPop = ModuleServiceManager.getInstance().getMsgService().startPrivateChat(getContext(),
                            String.valueOf(mUserInfoModel.getUserId()),
                            mUserInfoModel.getNicknameRemark(),
                            mUserInfoModel.isFriend()
                    );
                    if (needPop) {
                        U.getFragmentUtils().popFragment(OtherPersonFragment4.this);
                    }
                }
            }
        });
    }

    @Override
    public boolean useEventBus() {
        return true;
    }

    @Override
    public void showHomePageInfo(UserInfoModel userInfoModel, List<RelationNumModel> relationNumModels,
                                 List<UserRankModel> userRankModels, List<UserLevelModel> userLevelModels,
                                 List<GameStatisModel> gameStatisModels,
                                 boolean isFriend, boolean isFollow,
                                 int meiLiCntTotal) {
        mSmartRefresh.finishRefresh();
        showUserInfo(userInfoModel);
        showRelationNum(relationNumModels);
        showReginRank(userRankModels);
        showUserRelation(isFriend, isFollow);
        showUserLevel(userLevelModels);
        showCharms(meiLiCntTotal);
    }

    private void showCharms(int meiLiCntTotal) {
        charmNum = meiLiCntTotal;

        mHashMap.put(CHARM_TAG, "魅力 " + StringFromatUtils.formatCharmNum(meiLiCntTotal));
        refreshTag();
    }

    public void showUserLevel(List<UserLevelModel> list) {
        int mainRank = 0;
        int subRank = 0;
        for (UserLevelModel userLevelModel : list) {
            if (userLevelModel.getType() == UserLevelModel.RANKING_TYPE) {
                mainRank = userLevelModel.getScore();
            } else if (userLevelModel.getType() == UserLevelModel.SUB_RANKING_TYPE) {
                subRank = userLevelModel.getScore();
            }
        }

        mLevelView.bindData(mainRank, subRank);
    }

    @Override
    public void getHomePageFail() {
        mSmartRefresh.finishRefresh();
    }

    public void showUserInfo(UserInfoModel model) {
        this.mUserInfoModel = model;
        if (mProducationWallView != null) {
            mProducationWallView.setUserInfoModel(model);
        }
        AvatarUtils.loadAvatarByUrl(mAvatarIv,
                AvatarUtils.newParamsBuilder(model.getAvatar())
                        .setBorderColor(Color.WHITE)
                        .setBorderWidth(U.getDisplayUtils().dip2px(2f))
                        .setCircle(true)
                        .build());

        mNameTv.setText(model.getNicknameRemark());
        mUseridTv.setText("ID:" + model.getUserId());
        if (model.getSex() == ESex.SX_MALE.getValue()) {
            mSexIv.setVisibility(View.VISIBLE);
            mSexIv.setBackgroundResource(R.drawable.sex_man_icon);
        } else if (model.getSex() == ESex.SX_FEMALE.getValue()) {
            mSexIv.setVisibility(View.VISIBLE);
            mSexIv.setBackgroundResource(R.drawable.sex_woman_icon);
        } else {
            mSexIv.setVisibility(View.GONE);
        }

        mSrlNameTv.setText(model.getNicknameRemark());
        mSignTv.setText(model.getSignature());

        if (model.getLocation() != null && !TextUtils.isEmpty(model.getLocation().getProvince())) {
            mHashMap.put(LOCATION_TAG, model.getLocation().getProvince());
        } else {
            mHashMap.put(LOCATION_TAG, "火星");
        }

        refreshTag();
    }

    private void refreshTag() {
        mTags.clear();
        if (mHashMap != null) {
            if (!TextUtils.isEmpty(mHashMap.get(CHARM_TAG))) {
                mTags.add(new TagModel(CHARM_TAG, mHashMap.get(CHARM_TAG)));
            }

            if (!TextUtils.isEmpty(mHashMap.get(FANS_NUM_TAG))) {
                mTags.add(new TagModel(FANS_NUM_TAG, mHashMap.get(FANS_NUM_TAG)));
            }

            if (!TextUtils.isEmpty(mHashMap.get(LOCATION_TAG))) {
                mTags.add(new TagModel(LOCATION_TAG, mHashMap.get(LOCATION_TAG)));
            }

        }
        mTagAdapter.setTagDatas(mTags);
        mTagAdapter.notifyDataChanged();
    }


    public void showRelationNum(List<RelationNumModel> list) {
        if (list != null && list.size() > 0) {
            for (RelationNumModel mode : list) {
                if (mode.getRelation() == UserInfoManager.RELATION.FANS.getValue()) {
                    fansNum = mode.getCnt();
                }
            }
        }

        mHashMap.put(FANS_NUM_TAG, "粉丝 " + StringFromatUtils.formatFansNum(fansNum));

        refreshTag();
    }


    public void showReginRank(List<UserRankModel> list) {
//        mMedalIv.setBackground(getResources().getDrawable(R.drawable.paihang));
//        UserRankModel reginRankModel = new UserRankModel();
//        UserRankModel countryRankModel = new UserRankModel();
//        if (list != null && list.size() > 0) {
//            for (UserRankModel model : list) {
//                if (model.getCategory() == UserRankModel.REGION) {
//                    reginRankModel = model;
//                }
//                if (model.getCategory() == UserRankModel.COUNTRY) {
//                    countryRankModel = model;
//                }
//            }
//        }
//
//        if (reginRankModel != null && reginRankModel.getRankSeq() != 0) {
//            mRankText.setText(reginRankModel.getRegionDesc() + "第" + String.valueOf(reginRankModel.getRankSeq()) + "位");
//        } else if (countryRankModel != null && countryRankModel.getRankSeq() != 0) {
//            mRankText.setText(countryRankModel.getRegionDesc() + "第" + String.valueOf(countryRankModel.getRankSeq()) + "位");
//        } else {
//            mRankText.setText(getResources().getString(R.string.default_rank_text));
//        }
    }

    public void showUserRelation(boolean isFriend, boolean isFollow) {
        mUserInfoModel.setFriend(isFriend);
        mUserInfoModel.setFollow(isFollow);
        if (isFriend) {
            mFollowIv.setClickable(false);
            mFollowIv.setAlpha(0.5f);
            mFollowIv.setText("互关");
            mFollowIv.setTag(RELATION_FOLLOWED);
        } else if (isFollow) {
            mFollowIv.setClickable(false);
            mFollowIv.setAlpha(0.5f);
            mFollowIv.setText("已关注");
            mFollowIv.setTag(RELATION_FOLLOWED);
        } else {
            mFollowIv.setClickable(true);
            mFollowIv.setAlpha(1f);
            mFollowIv.setText("关注Ta");
            mFollowIv.setTag(RELATION_UN_FOLLOW);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(RelationChangeEvent event) {
        if (event.useId == mUserInfoModel.getUserId()) {
            showUserRelation(event.isFriend, event.isFollow);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(RemarkChangeEvent event) {
        if (event.userId == mUserInfoModel.getUserId()) {
            mNameTv.setText(mUserInfoModel.getNicknameRemark());
        }
    }

    private void unFollow(final UserInfoModel userInfoModel) {
        TipsDialogView tipsDialogView = new TipsDialogView.Builder(getContext())
                .setTitleTip("取消关注")
                .setMessageTip("是否取消关注")
                .setConfirmTip("取消关注")
                .setCancelTip("不了")
                .setConfirmBtnClickListener(new AnimateClickListener() {
                    @Override
                    public void click(View view) {
                        if (mDialogPlus != null) {
                            mDialogPlus.dismiss();
                        }
                        UserInfoManager.getInstance().mateRelation(userInfoModel.getUserId(), UserInfoManager.RA_UNBUILD, userInfoModel.isFriend());
                    }
                })
                .setCancelBtnClickListener(new AnimateClickListener() {
                    @Override
                    public void click(View view) {
                        if (mDialogPlus != null) {
                            mDialogPlus.dismiss();
                        }
                    }
                })
                .build();

        mDialogPlus = DialogPlus.newDialog(getContext())
                .setContentHolder(new ViewHolder(tipsDialogView))
                .setGravity(Gravity.BOTTOM)
                .setContentBackgroundResource(R.color.transparent)
                .setOverlayBackgroundResource(R.color.black_trans_80)
                .setExpanded(false)
                .setOnDismissListener(new OnDismissListener() {
                    @Override
                    public void onDismiss(@NonNull DialogPlus dialog) {

                    }
                })
                .create();
        mDialogPlus.show();
    }

    @Override
    public void destroy() {
        super.destroy();
        if (mDialogPlus != null) {
            mDialogPlus.dismiss();
        }
        if (mPersonMoreOpView != null) {
            mPersonMoreOpView.dismiss();
        }
        if (mEditRemarkDialog != null) {
            mEditRemarkDialog.dismiss(false);
        }
    }

    @Override
    public void onRequestSucess() {
        mSmartRefresh.finishLoadMore();
    }
}
