package com.zq.person.fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.view.PagerAdapter;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.common.base.BaseFragment;
import com.common.core.avatar.AvatarUtils;
import com.common.core.myinfo.MyUserInfoManager;
import com.common.core.userinfo.UserInfoManager;
import com.common.core.userinfo.event.RelationChangeEvent;
import com.common.core.userinfo.model.GameStatisModel;
import com.common.core.userinfo.model.UserInfoModel;
import com.common.core.userinfo.model.UserLevelModel;
import com.common.core.userinfo.model.UserRankModel;
import com.common.flowlayout.FlowLayout;
import com.common.flowlayout.TagAdapter;
import com.common.flowlayout.TagFlowLayout;
import com.common.utils.FragmentUtils;
import com.common.utils.U;
import com.common.view.AnimateClickListener;
import com.common.view.DebounceViewClickListener;
import com.common.view.ex.ExImageView;
import com.common.view.ex.ExRelativeLayout;
import com.common.view.ex.ExTextView;
import com.common.view.titlebar.CommonTitleBar;
import com.common.view.viewpager.NestViewPager;
import com.common.view.viewpager.SlidingTabLayout;
import com.component.busilib.R;
import com.component.busilib.constans.GameModeType;
import com.component.busilib.view.BitmapTextView;
import com.dialog.view.TipsDialogView;
import com.facebook.drawee.view.SimpleDraweeView;
import com.imagebrowse.big.BigImageBrowseFragment;
import com.module.ModuleServiceManager;
import com.orhanobut.dialogplus.DialogPlus;
import com.orhanobut.dialogplus.OnDismissListener;
import com.orhanobut.dialogplus.ViewHolder;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.header.ClassicsHeader;
import com.scwang.smartrefresh.layout.listener.OnRefreshLoadMoreListener;
import com.zq.level.view.NormalLevelView2;
import com.zq.live.proto.Common.ESex;
import com.zq.person.adapter.PhotoAdapter;
import com.zq.person.model.PhotoModel;
import com.zq.person.presenter.OtherPersonPresenter;
import com.zq.person.view.EditRemarkView;
import com.zq.person.view.IOtherPersonView;
import com.zq.person.view.OtherPhotoWallView;
import com.zq.person.view.PersonMoreOpView;
import com.zq.person.view.ProducationWallView;
import com.zq.report.fragment.ReportFragment;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import model.RelationNumModel;

import static com.zq.report.fragment.ReportFragment.FORM_PERSON;
import static com.zq.report.fragment.ReportFragment.REPORT_FROM_KEY;
import static com.zq.report.fragment.ReportFragment.REPORT_USER_ID;

public class OtherPersonFragment3 extends BaseFragment implements IOtherPersonView {

    public static final String BUNDLE_USER_ID = "bundle_user_id";

    public static final int RELATION_FOLLOWED = 1; // 已关注关系
    public static final int RELATION_UN_FOLLOW = 2; // 未关注关系

    private static final int LOCATION_TAG = 0;         //地区标签  省/市
    private static final int AGE_TAG = 1;              //年龄标签
    private static final int CONSTELLATION_TAG = 2;    //星座标签
    private static final int FANS_NUM_TAG = 3;         // 粉丝数标签

    private List<String> mTags = new ArrayList<>();  //标签
    private HashMap<Integer, String> mHashMap = new HashMap();

    TagAdapter mTagAdapter;

    PhotoAdapter mPhotoAdapter;
    int offset;  // 拉照片偏移量
    int DEFAUAT_CNT = 20;       // 默认拉取一页的数量

    boolean mHasMore = false;
    boolean isAppbarCanSrcoll = true;  // AppBarLayout是否可以滚动

    UserInfoModel mUserInfoModel = new UserInfoModel();
    int mUserId;

    DialogPlus mDialogPlus;

    OtherPersonPresenter mPresenter;

    SmartRefreshLayout mSmartRefresh;
    ClassicsHeader mClassicsHeader;
    AppBarLayout mAppbar;
    CollapsingToolbarLayout mToolbarLayout;
    RelativeLayout mUserInfoArea;
    CommonTitleBar mTitlebar;
    Toolbar mToolbar;
    TextView mSrlNameTv;

    ExImageView mIvBack;
    ExImageView mMoreBtn;
    PersonMoreOpView mPersonMoreOpView;

    ImageView mAvatarBg;
    SimpleDraweeView mAvatarIv;
    ExTextView mNameTv;
    ImageView mSexIv;
    ExTextView mUseridTv;
    ExTextView mSignTv;
    TagFlowLayout mFlowlayout;
    ExRelativeLayout mGameLayout;
    ImageView mPaiweiImg;
    BitmapTextView mRankNumTv;
    ImageView mSingendImg;
    BitmapTextView mSingendNumTv;
    NormalLevelView2 mLevelView;
    ExTextView mLevelTv;

    RelativeLayout mRankArea;
    ExTextView mRankText;
    ExImageView mRankDiffIv;
    ExImageView mMedalIv;

    SlidingTabLayout mPersonTab;
    NestViewPager mPersonVp;
    PagerAdapter mPersonTabAdapter;

    OtherPhotoWallView mOtherPhotoWallView;
    ProducationWallView mProducationWallView;

    LinearLayout mFunctionArea;
    ImageView mFollowIv;
    ImageView mMessageIv;

    DialogPlus mEditRemarkDialog;

    @Override
    public int initView() {
        return R.layout.other_person_fragment3_layout;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        initBaseContainArea();
        initTopArea();
        initUserInfoArea();
        initMedalInfoArea();
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

    private void initMedalInfoArea() {
        mRankArea = (RelativeLayout) mRootView.findViewById(R.id.rank_area);
        mRankText = (ExTextView) mRootView.findViewById(R.id.rank_text);
        mRankDiffIv = (ExImageView) mRootView.findViewById(R.id.rank_diff_iv);
        mMedalIv = (ExImageView) mRootView.findViewById(R.id.medal_iv);
    }


    private void initBaseContainArea() {
        mSmartRefresh = (SmartRefreshLayout) mRootView.findViewById(R.id.smart_refresh);
        mClassicsHeader = (ClassicsHeader) mRootView.findViewById(R.id.classics_header);
        mAppbar = (AppBarLayout) mRootView.findViewById(R.id.appbar);
        mToolbarLayout = (CollapsingToolbarLayout) mRootView.findViewById(R.id.toolbar_layout);
        mUserInfoArea = (RelativeLayout) mRootView.findViewById(R.id.user_info_area);
        mTitlebar = (CommonTitleBar) mRootView.findViewById(R.id.titlebar);
        mToolbar = (Toolbar) mRootView.findViewById(R.id.toolbar);
        mSrlNameTv = (TextView) mRootView.findViewById(R.id.srl_name_tv);

        if (U.getDeviceUtils().hasNotch(getContext())) {
            mTitlebar.setVisibility(View.VISIBLE);
        } else {
            mTitlebar.setVisibility(View.GONE);
        }

        mSmartRefresh.setEnableRefresh(true);
        mSmartRefresh.setEnableLoadMore(false);
        mSmartRefresh.setEnableLoadMoreWhenContentNotFull(false);
        mSmartRefresh.setEnableOverScrollDrag(true);
        mClassicsHeader.setBackgroundColor(Color.parseColor("#7088FF"));
        mSmartRefresh.setRefreshHeader(mClassicsHeader);
        mSmartRefresh.setOnRefreshLoadMoreListener(new OnRefreshLoadMoreListener() {
            @Override
            public void onLoadMore(@NonNull RefreshLayout refreshLayout) {
            }

            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {
                mPresenter.getHomePage(mUserId);
                if (mOtherPhotoWallView != null) {
                    mOtherPhotoWallView.getPhotos();
                }
                if (mProducationWallView != null) {
                    mProducationWallView.getProducations();
                }
            }
        });

        mAppbar.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                if (verticalOffset == 0) {
                    // 展开状态
                    mToolbar.setVisibility(View.GONE);
                } else if (Math.abs(verticalOffset) >= appBarLayout.getTotalScrollRange()) {
                    // 完全收缩状态
                    mToolbar.setVisibility(View.VISIBLE);
                } else {
                    // TODO: 2019/4/8 过程中，可以加动画，先直接显示
                    mToolbar.setVisibility(View.GONE);
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
                U.getFragmentUtils().popFragment(OtherPersonFragment3.this);
            }
        });

        mMoreBtn.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                if (mPersonMoreOpView != null) {
                    mPersonMoreOpView.dismiss();
                }
                mPersonMoreOpView = new PersonMoreOpView(getContext(), mUserInfoModel.isFollow());
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
                        Bundle bundle = new Bundle();
                        bundle.putInt(REPORT_FROM_KEY, FORM_PERSON);
                        bundle.putInt(REPORT_USER_ID, mUserId);
                        U.getFragmentUtils().addFragment(
                                FragmentUtils.newAddParamsBuilder(getActivity(), ReportFragment.class)
                                        .setBundle(bundle)
                                        .setAddToBackStack(true)
                                        .setHasAnimation(true)
                                        .setEnterAnim(com.component.busilib.R.anim.slide_in_bottom)
                                        .setExitAnim(com.component.busilib.R.anim.slide_out_bottom)
                                        .build());
                    }
                });
                mPersonMoreOpView.showAt(mMoreBtn);
            }
        });
    }

    private void showRemarkDialog() {
        EditRemarkView editRemarkView = new EditRemarkView(getContext(), mUserInfoModel.getNicknameRemark());
        editRemarkView.setListener(new EditRemarkView.Listener() {
            @Override
            public void onClickCancel() {
                if (mEditRemarkDialog != null) {
                    mEditRemarkDialog.dismiss();
                }
            }

            @Override
            public void onClickSave(String remarkName) {
                if (mEditRemarkDialog != null) {
                    mEditRemarkDialog.dismiss();
                }
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

        mEditRemarkDialog = DialogPlus.newDialog(getContext()).setContentHolder(new ViewHolder(editRemarkView))
                .setContentBackgroundResource(R.color.transparent)
                .setOverlayBackgroundResource(R.color.black_trans_50)
                .setExpanded(false)
                .setGravity(Gravity.CENTER)
                .setOnDismissListener(new OnDismissListener() {
                    @Override
                    public void onDismiss(@NonNull DialogPlus dialog) {
                        U.getKeyBoardUtils().hideSoftInputKeyBoard(getActivity());
                    }
                })
                .create();
        U.getKeyBoardUtils().

                showSoftInputKeyBoard(getActivity());
        mEditRemarkDialog.show();

    }

    private void initUserInfoArea() {
        mAvatarBg = (ImageView) mRootView.findViewById(R.id.avatar_bg);
        mAvatarIv = (SimpleDraweeView) mRootView.findViewById(R.id.avatar_iv);
        mNameTv = (ExTextView) mRootView.findViewById(R.id.name_tv);
        mSexIv = (ImageView) mRootView.findViewById(R.id.sex_iv);
        mUseridTv = (ExTextView) mRootView.findViewById(R.id.userid_tv);
        mSignTv = (ExTextView) mRootView.findViewById(R.id.sign_tv);
        mFlowlayout = (TagFlowLayout) mRootView.findViewById(R.id.flowlayout);
        mGameLayout = (ExRelativeLayout) mRootView.findViewById(R.id.game_layout);
        mPaiweiImg = (ImageView) mRootView.findViewById(R.id.paiwei_img);
        mRankNumTv = (BitmapTextView) mRootView.findViewById(R.id.rank_num_tv);
        mSingendImg = (ImageView) mRootView.findViewById(R.id.singend_img);
        mSingendNumTv = (BitmapTextView) mRootView.findViewById(R.id.singend_num_tv);
        mLevelView = (NormalLevelView2) mRootView.findViewById(R.id.level_view);
        mLevelTv = (ExTextView) mRootView.findViewById(R.id.level_tv);

        mTagAdapter = new TagAdapter<String>(mTags) {
            @Override
            public View getView(FlowLayout parent, int position, String o) {
                ExTextView tv = (ExTextView) LayoutInflater.from(getContext()).inflate(R.layout.person_tag_textview,
                        mFlowlayout, false);
                tv.setText(o);
                return tv;
            }
        };
        mFlowlayout.setAdapter(mTagAdapter);

        mAvatarIv.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                BigImageBrowseFragment.open(false, getActivity(), mUserInfoModel.getAvatar());
            }
        });
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
                    mOtherPhotoWallView = new OtherPhotoWallView(OtherPersonFragment3.this, mUserId, null);
                    container.addView(mOtherPhotoWallView);
                    mOtherPhotoWallView.getPhotos();
                    return mOtherPhotoWallView;
                } else if (position == 1) {
                    // 作品
                    mProducationWallView = new ProducationWallView(OtherPersonFragment3.this, mUserId);
                    container.addView(mProducationWallView);
                    mProducationWallView.getProducations();
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
    }

    private void initFunctionArea() {
        mFunctionArea = (LinearLayout) mRootView.findViewById(R.id.function_area);
        mFollowIv = (ImageView) mRootView.findViewById(R.id.follow_iv);
        mMessageIv = (ImageView) mRootView.findViewById(R.id.message_iv);


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
                        U.getFragmentUtils().popFragment(OtherPersonFragment3.this);
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
    public void showHomePageInfo(UserInfoModel userInfoModel, List<RelationNumModel> relationNumModels, List<UserRankModel> userRankModels, List<UserLevelModel> userLevelModels, List<GameStatisModel> gameStatisModels, boolean isFriend, boolean isFollow) {
        mSmartRefresh.finishRefresh();
        showUserInfo(userInfoModel);
        showRelationNum(relationNumModels);
        showUserLevel(userLevelModels);
        showReginRank(userRankModels);
        showGameStatic(gameStatisModels);
        showUserRelation(isFriend, isFollow);
    }

    @Override
    public void addPhotos(List<PhotoModel> list, int newOffset, int totalNum, boolean clear) {

    }

    @Override
    public void addPhotosFail() {
        mSmartRefresh.finishLoadMore();
        if (mPhotoAdapter.getDataList() == null || mPhotoAdapter.getDataList().size() == 0) {
            setAppBarCanScroll(false);
        }
    }

    public void showUserInfo(UserInfoModel model) {
        this.mUserInfoModel = model;

        AvatarUtils.loadAvatarByUrl(mAvatarIv,
                AvatarUtils.newParamsBuilder(model.getAvatar())
                        .setCircle(true)
                        .build());

        mNameTv.setText(model.getNicknameRemark());
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
        mUseridTv.setText("撕歌号：" + model.getUserId());
        mSignTv.setText(model.getSignature());

        if (model.getLocation() != null && !TextUtils.isEmpty(model.getLocation().getCity())) {
            mHashMap.put(LOCATION_TAG, model.getLocation().getCity());
        } else {
            mHashMap.put(LOCATION_TAG, "未知星球");
        }

        if (!TextUtils.isEmpty(model.getBirthday())) {
            mHashMap.put(AGE_TAG, String.format(getString(R.string.age_tag), model.getAge()));
        }

        if (!TextUtils.isEmpty(model.getBirthday())) {
            mHashMap.put(CONSTELLATION_TAG, model.getConstellation());
        }

        refreshTag();
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

            if (!TextUtils.isEmpty(mHashMap.get(CONSTELLATION_TAG))) {
                mTags.add(mHashMap.get(CONSTELLATION_TAG));
            }

            if (!TextUtils.isEmpty(mHashMap.get(FANS_NUM_TAG))) {
                mTags.add(mHashMap.get(FANS_NUM_TAG));
            }

        }
        mTagAdapter.setTagDatas(mTags);
        mTagAdapter.notifyDataChanged();
    }


    public void showRelationNum(List<RelationNumModel> list) {
        int fansNum = 0;
        if (list != null && list.size() > 0) {
            for (RelationNumModel mode : list) {
                if (mode.getRelation() == UserInfoManager.RELATION.FANS.getValue()) {
                    fansNum = mode.getCnt();
                }
            }
        }

        mHashMap.put(FANS_NUM_TAG, String.format(getResources().getString(R.string.fans_num_tag), fansNum));

        refreshTag();
    }


    public void showReginRank(List<UserRankModel> list) {
        mMedalIv.setBackground(getResources().getDrawable(R.drawable.paihang));
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
            mRankText.setText(reginRankModel.getRegionDesc() + "第" + String.valueOf(reginRankModel.getRankSeq()) + "位");
        } else if (countryRankModel != null && countryRankModel.getRankSeq() != 0) {
            mRankText.setText(countryRankModel.getRegionDesc() + "第" + String.valueOf(countryRankModel.getRankSeq()) + "位");
        } else {
            mRankText.setText(getResources().getString(R.string.default_rank_text));
        }
    }


    public void showUserLevel(List<UserLevelModel> list) {
        // 展示段位信息
        int rank = 0;           //当前父段位
        int subRank = 0;        //当前子段位
        String rankDesc = "";       //段位描述
        for (UserLevelModel userLevelModel : list) {
            if (userLevelModel.getType() == UserLevelModel.RANKING_TYPE) {
                rank = userLevelModel.getScore();
            } else if (userLevelModel.getType() == UserLevelModel.SUB_RANKING_TYPE) {
                subRank = userLevelModel.getScore();
                rankDesc = userLevelModel.getDesc();
            }
        }
        mLevelView.bindData(rank, subRank);
        mLevelTv.setText(rankDesc);
    }


    public void showUserRelation(boolean isFriend, boolean isFollow) {
        mUserInfoModel.setFriend(isFriend);
        mUserInfoModel.setFollow(isFollow);
        if (isFriend) {
            mFollowIv.setClickable(false);
            mFollowIv.setAlpha(0.5f);
            mFollowIv.setBackgroundResource(R.drawable.other_person_friend);
            mFollowIv.setTag(RELATION_FOLLOWED);
        } else if (isFollow) {
            mFollowIv.setClickable(false);
            mFollowIv.setAlpha(0.5f);
            mFollowIv.setBackgroundResource(R.drawable.other_person_followed);
            mFollowIv.setTag(RELATION_FOLLOWED);
        } else {
            mFollowIv.setClickable(true);
            mFollowIv.setAlpha(1f);
            mFollowIv.setBackgroundResource(R.drawable.other_person_follow);
            mFollowIv.setTag(RELATION_UN_FOLLOW);
        }
    }


    public void showGameStatic(List<GameStatisModel> list) {
        for (GameStatisModel gameStatisModel : list) {
            if (gameStatisModel.getMode() == GameModeType.GAME_MODE_CLASSIC_RANK) {
                mRankNumTv.setText("" + gameStatisModel.getTotalTimes());
            } else if (gameStatisModel.getMode() == GameModeType.GAME_MODE_GRAB) {
                mSingendNumTv.setText("" + gameStatisModel.getTotalTimes());
            }
        }
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(RelationChangeEvent event) {
        if (event.useId == mUserInfoModel.getUserId()) {
            showUserRelation(event.isFriend, event.isFollow);
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
}
