package com.zq.person.fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.common.base.BaseFragment;
import com.common.callback.Callback;
import com.common.core.avatar.AvatarUtils;
import com.common.core.myinfo.MyUserInfoManager;
import com.common.core.userinfo.UserInfoManager;
import com.common.core.userinfo.UserInfoServerApi;
import com.common.core.userinfo.event.RelationChangeEvent;
import com.common.core.userinfo.model.GameStatisModel;
import com.common.core.userinfo.model.UserInfoModel;
import com.common.core.userinfo.model.UserLevelModel;
import com.common.core.userinfo.model.UserRankModel;
import com.common.flowlayout.FlowLayout;
import com.common.flowlayout.TagAdapter;
import com.common.flowlayout.TagFlowLayout;
import com.common.log.MyLog;
import com.common.rxretrofit.ApiManager;
import com.common.rxretrofit.ApiResult;
import com.common.utils.SpanUtils;
import com.common.utils.U;
import com.common.view.AnimateClickListener;
import com.common.view.DebounceViewClickListener;
import com.common.view.ex.ExImageView;
import com.common.view.ex.ExRelativeLayout;
import com.common.view.ex.ExTextView;
import com.common.view.recyclerview.RecyclerOnItemClickListener;
import com.common.view.titlebar.CommonTitleBar;
import com.component.busilib.R;
import com.component.busilib.constans.GameModeType;
import com.component.busilib.view.BitmapTextView;
import com.dialog.view.TipsDialogView;
import com.facebook.drawee.view.SimpleDraweeView;
import com.imagebrowse.ImageBrowseView;
import com.imagebrowse.big.BigImageBrowseFragment;
import com.imagebrowse.big.DefaultImageBrowserLoader;
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
import com.zq.person.view.IOtherPersonView;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import model.RelationNumModel;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.http.DELETE;

public class OtherPersonFragment2 extends BaseFragment implements IOtherPersonView {

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

    UserInfoModel mUserInfoModel;
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
    ImageView mAvatarBg;
    SimpleDraweeView mAvatarIv;
    ExTextView mNameTv;
    ImageView mSexIv;
    ExTextView mUseridTv;
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

    RecyclerView mPhotoView;
    ExTextView mPhotoNumTv;

    LinearLayout mFunctionArea;
    ImageView mFollowIv;
    ImageView mMessageIv;

    @Override
    public int initView() {
        return R.layout.other_person_fragment2_layout;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        initBaseContainArea();
        initUserInfoArea();
        initMedalInfoArea();
        initPhotoArea();
        initFunctionArea();

        mPresenter = new OtherPersonPresenter(this);
        addPresent(mPresenter);

        Bundle bundle = getArguments();
        if (bundle != null) {
            mUserId = bundle.getInt(BUNDLE_USER_ID);
            mPresenter.getHomePage(mUserId);
            mPresenter.getPhotos(mUserId, 0, DEFAUAT_CNT);
        }

        if (mUserId == MyUserInfoManager.getInstance().getUid()) {
            mFunctionArea.setVisibility(View.GONE);
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
        mSmartRefresh.setEnableLoadMore(true);
        mSmartRefresh.setEnableLoadMoreWhenContentNotFull(false);
        mSmartRefresh.setEnableOverScrollDrag(true);
        mClassicsHeader.setBackgroundColor(Color.parseColor("#7088FF"));
        mSmartRefresh.setRefreshHeader(mClassicsHeader);
        mSmartRefresh.setOnRefreshLoadMoreListener(new OnRefreshLoadMoreListener() {
            @Override
            public void onLoadMore(@NonNull RefreshLayout refreshLayout) {
                mPresenter.getPhotos(mUserId, offset, DEFAUAT_CNT);
            }

            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {
                mPresenter.getHomePage(mUserId);
                mPresenter.getPhotos(mUserId, 0, DEFAUAT_CNT);
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

    private void initUserInfoArea() {
        mIvBack = (ExImageView) mRootView.findViewById(R.id.iv_back);
        mAvatarBg = (ImageView) mRootView.findViewById(R.id.avatar_bg);
        mAvatarIv = (SimpleDraweeView) mRootView.findViewById(R.id.avatar_iv);
        mNameTv = (ExTextView) mRootView.findViewById(R.id.name_tv);
        mSexIv = (ImageView)mRootView.findViewById(R.id.sex_iv);
        mUseridTv = (ExTextView) mRootView.findViewById(R.id.userid_tv);
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

        mIvBack.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                U.getFragmentUtils().popFragment(OtherPersonFragment2.this);
            }
        });

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

    private void initPhotoArea() {
        mPhotoView = (RecyclerView) mRootView.findViewById(R.id.photo_view);
        mPhotoNumTv = (ExTextView) mRootView.findViewById(R.id.photo_num_tv);

        mPhotoView.setFocusableInTouchMode(false);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 3);
        mPhotoView.setLayoutManager(gridLayoutManager);
        mPhotoAdapter = new PhotoAdapter(new RecyclerOnItemClickListener() {
            @Override
            public void onItemClicked(View view, final int position, Object model) {
                BigImageBrowseFragment.open(true, getActivity(), new DefaultImageBrowserLoader<PhotoModel>() {
                    @Override
                    public void init() {

                    }

                    @Override
                    public void load(ImageBrowseView imageBrowseView, int position, PhotoModel item) {
                        if (TextUtils.isEmpty(item.getPicPath())) {
                            imageBrowseView.load(item.getLocalPath());
                        } else {
                            imageBrowseView.load(item.getPicPath());
                        }
                    }

                    @Override
                    public int getInitCurrentItemPostion() {
                        return position;
                    }

                    @Override
                    public List<PhotoModel> getInitList() {
                        return mPhotoAdapter.getDataList();
                    }

                    @Override
                    public void loadMore(boolean backward, int position, PhotoModel data, final Callback<List<PhotoModel>> callback) {
                        if (backward) {
                            // 向后加载
                            mPresenter.getPhotos(mUserId, mPhotoAdapter.getSuccessNum(), DEFAUAT_CNT, new Callback<List<PhotoModel>>() {

                                @Override
                                public void onCallback(int r, List<PhotoModel> list) {
                                    if (callback != null && list != null) {
                                        callback.onCallback(0, list);
                                    }
                                }
                            });
                        }
                    }

                    @Override
                    public boolean hasMore(boolean backward, int position, PhotoModel data) {
                        if (backward) {
                            return mHasMore;
                        }
                        return false;
                    }

                    @Override
                    public boolean hasMenu() {
                        return false;
                    }
                });
            }
        }, PhotoAdapter.TYPE_OTHER_PERSON_CENTER);
        mPhotoView.setAdapter(mPhotoAdapter);
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
                    if ((int) mFollowIv.getTag() == RELATION_FOLLOWED) {
                        unFollow(mUserInfoModel);
                    } else if ((int) mFollowIv.getTag() == RELATION_UN_FOLLOW) {
                        UserInfoManager.getInstance().mateRelation(mUserInfoModel.getUserId(), UserInfoManager.RA_BUILD, mUserInfoModel.isFriend());
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
                            mUserInfoModel.getNickname(),
                            mUserInfoModel.isFriend()
                    );
                    if(needPop){
                        U.getFragmentUtils().popFragment(OtherPersonFragment2.this);
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
        offset = newOffset;
        mSmartRefresh.finishLoadMore();

        if (totalNum > 0) {
            mPhotoNumTv.setText("个人相册（" + totalNum + "）");
            mPhotoNumTv.setVisibility(View.VISIBLE);
        } else {
            mPhotoNumTv.setVisibility(View.GONE);
        }

        if (clear) {
            mPhotoAdapter.getDataList().clear();
        }

        if (list != null && list.size() > 0) {
            mHasMore = true;
            setAppBarCanScroll(true);
            mSmartRefresh.setEnableLoadMore(true);
            mPhotoAdapter.getDataList().addAll(list);
            mPhotoAdapter.notifyDataSetChanged();
        } else {
            mHasMore = false;
            mSmartRefresh.setEnableLoadMore(false);
            if (mPhotoAdapter.getDataList() != null && mPhotoAdapter.getDataList().size() > 0) {
                // 没有更多了
            } else {
                // 没有数据
                setAppBarCanScroll(false);
            }
        }
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

        mNameTv.setText(model.getNickname());
        mSexIv.setBackgroundResource(model.getSex() == ESex.SX_MALE.getValue() ? R.drawable.sex_man_icon : R.drawable.sex_woman_icon);
        mSrlNameTv.setText(model.getNickname());
        mUseridTv.setText("撕歌号：" + model.getUserId());

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
                if (mode.getRelation() == UserInfoManager.RELATION_FANS) {
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
            mFollowIv.setBackgroundResource(R.drawable.other_person_friend);
            mFollowIv.setTag(RELATION_FOLLOWED);
        } else if (isFollow) {
            mFollowIv.setBackgroundResource(R.drawable.other_person_followed);
            mFollowIv.setTag(RELATION_FOLLOWED);
        } else {
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
            mUserInfoModel.setFriend(event.isFriend);
            mUserInfoModel.setFollow(event.isFollow);
            if (event.type == RelationChangeEvent.FOLLOW_TYPE) {
                if (event.isFriend) {
                    mFollowIv.setBackgroundResource(R.drawable.other_person_friend);
                } else if (event.isFollow) {
                    mFollowIv.setBackgroundResource(R.drawable.other_person_followed);
                }
                mFollowIv.setTag(RELATION_FOLLOWED);
            } else if (event.type == RelationChangeEvent.UNFOLLOW_TYPE) {
                mFollowIv.setBackgroundResource(R.drawable.other_person_follow);
                mFollowIv.setTag(RELATION_UN_FOLLOW);
            }
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
    }
}
