package com.zq.dialog;

import android.content.Context;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.alibaba.fastjson.JSON;
import com.common.base.BaseActivity;
import com.common.callback.Callback;
import com.common.core.account.UserAccountManager;
import com.common.core.avatar.AvatarUtils;
import com.common.core.myinfo.MyUserInfoManager;
import com.common.core.userinfo.UserInfoManager;
import com.common.core.userinfo.UserInfoServerApi;
import com.common.core.userinfo.event.RelationChangeEvent;
import com.common.core.userinfo.model.UserInfoModel;
import com.common.core.userinfo.model.UserLevelModel;
import com.common.flowlayout.FlowLayout;
import com.common.flowlayout.TagAdapter;
import com.common.flowlayout.TagFlowLayout;
import com.common.log.MyLog;
import com.common.notification.event.FollowNotifyEvent;
import com.common.rxretrofit.ApiManager;
import com.common.rxretrofit.ApiMethods;
import com.common.rxretrofit.ApiObserver;
import com.common.rxretrofit.ApiResult;
import com.common.utils.U;
import com.common.view.AnimateClickListener;
import com.common.view.DebounceViewClickListener;
import com.common.view.ex.ExImageView;
import com.common.view.ex.ExTextView;
import com.common.view.recyclerview.RecyclerOnItemClickListener;
import com.component.busilib.R;
import com.component.busilib.view.MarqueeTextView;
import com.facebook.drawee.view.SimpleDraweeView;
import com.imagebrowse.ImageBrowseView;
import com.imagebrowse.big.BigImageBrowseFragment;
import com.imagebrowse.big.DefaultImageBrowserLoader;
import com.module.ModuleServiceManager;
import com.module.common.ICallback;
import com.module.msg.IMsgService;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnRefreshLoadMoreListener;
import com.zq.level.view.NormalLevelView2;
import com.zq.live.proto.Common.ESex;
import com.zq.person.adapter.PhotoAdapter;
import com.zq.person.model.PhotoModel;
import com.zq.person.view.PersonMoreOpView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import model.RelationNumModel;

public class PersonInfoDialogView2 extends RelativeLayout {

    public final static String TAG = "PersonInfoDialogView2";

    Handler mUiHandler = new Handler();

    SmartRefreshLayout mSmartRefresh;
    View mOutView;
    CoordinatorLayout mCoordinator;

    AppBarLayout mAppbar;
    CollapsingToolbarLayout mToolbarLayout;
    RelativeLayout mUserInfoArea;
    ImageView mAvatarBg;
    SimpleDraweeView mAvatarIv;
    ExImageView mMoreBtn;
    RelativeLayout mNameArea;
    NormalLevelView2 mLevelView;
    ExTextView mNameTv;
    ImageView mSexIv;
    MarqueeTextView mSignTv;
    TagFlowLayout mFlowlayout;

    LinearLayout mFunctionArea;
    RelativeLayout mFollowArea;
    ImageView mFollowIv;

    Toolbar mToolbar;
    ImageView mSrlFollowIv;
    RelativeLayout mSrlAvatarArea;
    ImageView mSrlAvatarBg;
    SimpleDraweeView mSrlAvatarIv;

    RecyclerView mPhotoView;
    ExTextView mPhotoNumTv;
    ExTextView mEmptyMyPhoto;

    private static final int CHARMS_TAG = 1;
    private static final int LOCATION_TAG = 2;           //城市标签
    private static final int CONSTELLATION_TAG = 3;      //星座标签
    private static final int FANS_NUM_TAG = 4;      //粉丝数标签

    private List<String> mTags = new ArrayList<>();  //标签
    private HashMap<Integer, String> mHashMap = new HashMap();

    TagAdapter<String> mTagAdapter;

    PhotoAdapter mPhotoAdapter;

    int mUserId;
    UserInfoModel mUserInfoModel = new UserInfoModel();
    boolean isShowKick;
    boolean isFollow;
    boolean isFriend;

    PersonMoreOpView mPersonMoreOpView;

    Context mContext;
    UserInfoServerApi mUserInfoServerApi;

    int mOffset = 0;
    boolean mHasMore = false;
    int DEFAULT_CNT = 10;

    boolean hasInitHeight = false;
    boolean isAppBarCanScroll = true;   // AppBarLayout是否可以滚动

    PersonInfoDialog.PersonCardClickListener mClickListener;

    PersonInfoDialogView2(Context context, int userID, boolean showReport, boolean showKick) {
        super(context);
        initView();
        initData(context, userID, showReport, showKick);
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mUiHandler.removeCallbacksAndMessages(null);
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
    }

    public void setListener(PersonInfoDialog.PersonCardClickListener listener) {
        this.mClickListener = listener;
    }

    private void initView() {
        inflate(getContext(), R.layout.person_info_card_view_layout, this);

        initBaseContainInfo();
        initUserInfo();
        initOpretaArea();
        initPhotoArea();
    }

    private void initData(Context context, int userID, boolean showReport, boolean showKick) {
        mContext = context;
        mUserId = userID;
        isShowKick = showKick;

        // 多音和ai裁判
        if (mUserId == UserAccountManager.SYSTEM_GRAB_ID || mUserId == UserAccountManager.SYSTEM_RANK_AI) {
            isShowKick = false;
            mMoreBtn.setVisibility(GONE);
        }

        // 自己卡片的处理
        if (mUserId == MyUserInfoManager.getInstance().getUid()) {
            isShowKick = false;
            mMoreBtn.setVisibility(GONE);
            mToolbar.setVisibility(GONE);
            mFunctionArea.setVisibility(View.GONE);
            mSrlFollowIv.setVisibility(GONE);
        }

        mUserInfoServerApi = ApiManager.getInstance().createService(UserInfoServerApi.class);
        getHomePage(mUserId);
        getPhotos(0);
    }

    private void getHomePage(int userId) {
        ApiMethods.subscribe(mUserInfoServerApi.getHomePage(userId), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult result) {
                if (result.getErrno() == 0) {
                    UserInfoModel userInfoModel = JSON.parseObject(result.getData().getString("userBaseInfo"), UserInfoModel.class);
//                    List<UserRankModel> userRankModels = JSON.parseArray(result.getData().getJSONObject("userRankInfo").getString("seqInfo"), UserRankModel.class);
                    List<RelationNumModel> relationNumModes = JSON.parseArray(result.getData().getJSONObject("userRelationCntInfo").getString("cnt"), RelationNumModel.class);
                    List<UserLevelModel> userLevelModels = JSON.parseArray(result.getData().getJSONObject("userScoreInfo").getString("userScore"), UserLevelModel.class);
//                    List<GameStatisModel> userGameStatisModels = JSON.parseArray(result.getData().getJSONObject("userGameStatisticsInfo").getString("statistic"), GameStatisModel.class);

                    boolean isFriend = result.getData().getJSONObject("userMateInfo").getBooleanValue("isFriend");
                    boolean isFollow = result.getData().getJSONObject("userMateInfo").getBooleanValue("isFollow");

                    int meiLiCntTotal = result.getData().getIntValue("meiLiCntTotal");

                    if (isFollow) {
                        userInfoModel.setFollow(isFollow);
                        userInfoModel.setFriend(isFriend);
                        UserInfoManager.getInstance().insertUpdateDBAndCache(userInfoModel);
                    }
                    showUserInfo(userInfoModel);
                    showUserRelationNum(relationNumModes);
                    showUserLevel(userLevelModels);
                    showUserRelation(isFriend, isFollow);
                    showCharmsTag(meiLiCntTotal);
                }
            }
        }, (BaseActivity) mContext);
    }

    void getPhotos(final int offset) {
        getPhotos(offset, null);
    }

    void getPhotos(final int offset, final Callback<List<PhotoModel>> callback) {
        ApiMethods.subscribe(mUserInfoServerApi.getPhotos(mUserId, offset, DEFAULT_CNT), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult result) {
                mSmartRefresh.finishLoadMore();
                if (result != null && result.getErrno() == 0) {
                    List<PhotoModel> list = JSON.parseArray(result.getData().getString("pic"), PhotoModel.class);
                    int newOffset = result.getData().getIntValue("offset");
                    int totalCount = result.getData().getIntValue("totalCount");
                    if (offset == 0) {
                        addPhotos(list, newOffset, totalCount, true);
                    } else {
                        addPhotos(list, newOffset, totalCount, false);
                    }
                    if (callback != null) {
                        callback.onCallback(0, list);
                    }
                } else {
                    addPhotoFail();
                }
            }

            @Override
            public void onNetworkError(ErrorType errorType) {
                addPhotoFail();
                super.onNetworkError(errorType);
            }
        });
    }

    private void initBaseContainInfo() {
        mOutView = (View) this.findViewById(R.id.out_view);
        mOutView.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                if (mClickListener != null) {
                    mClickListener.onClickOut();
                }
            }
        });

        mSmartRefresh = (SmartRefreshLayout) this.findViewById(R.id.smart_refresh);
        mCoordinator = (CoordinatorLayout) this.findViewById(R.id.coordinator);
        mAppbar = (AppBarLayout) this.findViewById(R.id.appbar);
        mToolbarLayout = (CollapsingToolbarLayout) this.findViewById(R.id.toolbar_layout);

        mSmartRefresh.setEnableRefresh(false);
        mSmartRefresh.setEnableLoadMore(true);
        mSmartRefresh.setEnableLoadMoreWhenContentNotFull(false);
        mSmartRefresh.setEnableOverScrollDrag(true);
        mSmartRefresh.setOnRefreshLoadMoreListener(new OnRefreshLoadMoreListener() {
            @Override
            public void onLoadMore(@NonNull RefreshLayout refreshLayout) {
                getPhotos(mOffset);
            }

            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {
                mSmartRefresh.finishRefresh();
            }
        });

        mAppbar.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                if (verticalOffset == 0) {
                    // 展开状态
                    if (mToolbar.getVisibility() != GONE) {
                        mToolbar.setVisibility(GONE);
                    }
                } else if (Math.abs(verticalOffset) >= (appBarLayout.getTotalScrollRange() - U.getDisplayUtils().dip2px(70))) {
                    // 完全收缩状态
                    if (mToolbar.getVisibility() != VISIBLE) {
                        mToolbar.setVisibility(VISIBLE);
                    }
                } else {
                    // TODO: 2019/4/8 过程中，可以加动画，先直接显示
                    if (mToolbar.getVisibility() != GONE) {
                        mToolbar.setVisibility(GONE);
                    }
                }
            }
        });
    }

    private void initUserInfo() {
        mUserInfoArea = (RelativeLayout) this.findViewById(R.id.user_info_area);
        mAvatarBg = (ImageView) this.findViewById(R.id.avatar_bg);
        mAvatarIv = (SimpleDraweeView) this.findViewById(R.id.avatar_iv);
        mMoreBtn = (ExImageView) this.findViewById(R.id.more_btn);
        mNameTv = (ExTextView) this.findViewById(R.id.name_tv);
        mNameArea = (RelativeLayout) this.findViewById(R.id.name_area);
        mLevelView = (NormalLevelView2) this.findViewById(R.id.level_view);
        mNameTv = (ExTextView) this.findViewById(R.id.name_tv);
        mSexIv = (ImageView) this.findViewById(R.id.sex_iv);
        mSignTv = (MarqueeTextView) this.findViewById(R.id.sign_tv);
        mFlowlayout = (TagFlowLayout) this.findViewById(R.id.flowlayout);

        mTagAdapter = new TagAdapter<String>(mTags) {
            @Override
            public View getView(FlowLayout parent, int position, String o) {
                ExTextView tv = (ExTextView) LayoutInflater.from(getContext()).inflate(R.layout.person_card_tag_textview,
                        mFlowlayout, false);
                tv.setText(o);
                return tv;
            }
        };
        mFlowlayout.setAdapter(mTagAdapter);

        mAvatarIv.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                if (mClickListener != null) {
                    mClickListener.onClickAvatar(mUserInfoModel.getAvatar());
                }
            }
        });

        /**
         * 这段代码不要删除，线上调试用的，可以在线拉这个人的日志调试问题
         */
        if (MyLog.isDebugLogOpen()) {
            mAvatarIv.setOnLongClickListener(new OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    IMsgService msgService = ModuleServiceManager.getInstance().getMsgService();
                    if (msgService != null) {
                        msgService.sendSpecialDebugMessage(String.valueOf(mUserId), 1, "请求上传日志", new ICallback() {
                            @Override
                            public void onSucess(Object obj) {
                                U.getToastUtil().showLong("请求成功,稍等看该用户是否有返回");
                            }

                            @Override
                            public void onFailed(Object obj, int errcode, String message) {
                                U.getToastUtil().showLong("请求失败");
                            }
                        });
                    }
                    return false;
                }
            });
        }

        mMoreBtn.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                if (mPersonMoreOpView != null) {
                    mPersonMoreOpView.dismiss();
                }
                mPersonMoreOpView = new PersonMoreOpView(getContext(), false, isShowKick);
                mPersonMoreOpView.setListener(new PersonMoreOpView.Listener() {
                    @Override
                    public void onClickRemark() {
                        if (mPersonMoreOpView != null) {
                            mPersonMoreOpView.dismiss();
                        }
                        if (mClickListener != null) {
                            mClickListener.onClickRemark(mUserInfoModel);
                        }
                    }

                    @Override
                    public void onClickUnFollow() {

                    }

                    @Override
                    public void onClickReport() {
                        if (mPersonMoreOpView != null) {
                            mPersonMoreOpView.dismiss();
                        }
                        if (mClickListener != null) {
                            mClickListener.onClickReport(mUserId);
                        }
                    }

                    @Override
                    public void onClickKick() {
                        if (mPersonMoreOpView != null) {
                            mPersonMoreOpView.dismiss();
                        }
                        if (mClickListener != null) {
                            mClickListener.onClickKick(mUserInfoModel);
                        }
                    }
                });
                mPersonMoreOpView.showAt(mMoreBtn);
            }
        });
    }

    private void initOpretaArea() {
        mFunctionArea = (LinearLayout) this.findViewById(R.id.function_area);
        mFollowArea = (RelativeLayout) this.findViewById(R.id.follow_area);
        mFollowIv = (ImageView) this.findViewById(R.id.follow_iv);

        mToolbar = (Toolbar) this.findViewById(R.id.toolbar);
        mSrlFollowIv = (ImageView) this.findViewById(R.id.srl_follow_iv);
        mSrlAvatarArea = (RelativeLayout) this.findViewById(R.id.srl_avatar_area);
        mSrlAvatarBg = (ImageView) this.findViewById(R.id.srl_avatar_bg);
        mSrlAvatarIv = (SimpleDraweeView) this.findViewById(R.id.srl_avatar_iv);

        mFollowIv.setOnClickListener(new AnimateClickListener() {
            @Override
            public void click(View view) {
                if (mClickListener != null) {
                    mClickListener.onClickFollow(mUserId, isFriend, isFollow);
                }
            }
        });

        mSrlFollowIv.setOnClickListener(new AnimateClickListener() {
            @Override
            public void click(View view) {
                if (mClickListener != null) {
                    mClickListener.onClickFollow(mUserId, isFriend, isFollow);
                }
            }
        });

        mSrlAvatarIv.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                if (mClickListener != null) {
                    mClickListener.onClickAvatar(mUserInfoModel.getAvatar());
                }
            }
        });
    }

    private void initPhotoArea() {
        mPhotoView = (RecyclerView) this.findViewById(R.id.photo_view);
        mPhotoNumTv = (ExTextView) this.findViewById(R.id.photo_num_tv);
        mEmptyMyPhoto = (ExTextView) this.findViewById(R.id.empty_my_photo);

        mPhotoView.setFocusableInTouchMode(false);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 3);
        mPhotoView.setLayoutManager(gridLayoutManager);
        mPhotoAdapter = new PhotoAdapter(new RecyclerOnItemClickListener() {
            @Override
            public void onItemClicked(View view, final int position, Object model) {
                BigImageBrowseFragment.open(true, (FragmentActivity) getContext(), new DefaultImageBrowserLoader<PhotoModel>() {
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
                            getPhotos(mPhotoAdapter.getSuccessNum(), new Callback<List<PhotoModel>>() {

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
        }, PhotoAdapter.TYPE_PERSON_CARD);

        mPhotoView.setAdapter(mPhotoAdapter);
    }


    public void addPhotos(List<PhotoModel> list, int offset, int totalCount, boolean clear) {
        mSmartRefresh.finishLoadMore();
        this.mOffset = offset;

        if (clear) {
            mPhotoAdapter.getDataList().clear();
        }

        if (totalCount > 0) {
            mPhotoNumTv.setText("照片（" + totalCount + "）");
            mPhotoNumTv.setVisibility(VISIBLE);
        } else {
            mPhotoNumTv.setVisibility(GONE);
            if (mUserId == MyUserInfoManager.getInstance().getUid()) {
                mEmptyMyPhoto.setVisibility(VISIBLE);
            }
        }

        if (list != null && list.size() != 0) {
            if (!hasInitHeight) {
                setAppBarCanScroll(true);
                ViewGroup.LayoutParams layoutParams = mSmartRefresh.getLayoutParams();
                layoutParams.height = U.getDisplayUtils().dip2px(375);
                mSmartRefresh.setLayoutParams(layoutParams);
                hasInitHeight = true;
            }
            mHasMore = true;
            mSmartRefresh.setEnableLoadMore(true);
            mPhotoAdapter.getDataList().addAll(list);
            mPhotoAdapter.notifyDataSetChanged();
        } else {
            mHasMore = false;
            mSmartRefresh.setEnableLoadMore(false);//是否启用下加载功能
            if (mPhotoAdapter.getDataList() != null && mPhotoAdapter.getDataList().size() > 0) {
                // 没有更多了
            } else {
                // 没有数据
                setAppBarCanScroll(false);
            }
        }
    }

    public void addPhotoFail() {
        mSmartRefresh.finishLoadMore();
        if (mPhotoAdapter.getDataList() == null || mPhotoAdapter.getDataList().size() == 0) {
            setAppBarCanScroll(false);
        }
    }

    private void setAppBarCanScroll(final boolean canScroll) {
        if (isAppBarCanScroll == canScroll) {
            return;
        }
        if (mAppbar != null && mAppbar.getLayoutParams() != null) {
            CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) mAppbar.getLayoutParams();
            AppBarLayout.Behavior behavior = new AppBarLayout.Behavior();
            behavior.setDragCallback(new AppBarLayout.Behavior.DragCallback() {
                @Override
                public boolean canDrag(@NonNull AppBarLayout appBarLayout) {
                    isAppBarCanScroll = canScroll;
                    return canScroll;
                }
            });
            params.setBehavior(behavior);
            mAppbar.setLayoutParams(params);
        }
    }

    public void showUserInfo(UserInfoModel model) {
        if (model != null) {
            mUserInfoModel = model;
            AvatarUtils.loadAvatarByUrl(mAvatarIv,
                    AvatarUtils.newParamsBuilder(model.getAvatar())
                            .setCircle(true)
                            .build());
            AvatarUtils.loadAvatarByUrl(mSrlAvatarIv,
                    AvatarUtils.newParamsBuilder(model.getAvatar())
                            .setCircle(true)
                            .build());

            mNameTv.setText(model.getNicknameRemark());
            mSignTv.setText(model.getSignature());
            if (model.getSex() == ESex.SX_MALE.getValue()) {
                mSexIv.setVisibility(VISIBLE);
                mSexIv.setBackgroundResource(R.drawable.sex_man_icon);
            } else if (model.getSex() == ESex.SX_FEMALE.getValue()) {
                mSexIv.setVisibility(VISIBLE);
                mSexIv.setBackgroundResource(R.drawable.sex_woman_icon);
            } else {
                mSexIv.setVisibility(GONE);
            }

            if (model.getLocation() != null && !TextUtils.isEmpty(model.getLocation().getCity())) {
                mHashMap.put(LOCATION_TAG, model.getLocation().getCity());
            }

            if (!TextUtils.isEmpty(model.getBirthday())) {
                mHashMap.put(CONSTELLATION_TAG, model.getConstellation());
            }

            refreshTag();
        }
    }


    private void showUserRelationNum(List<RelationNumModel> relationNumModes) {
        int fansNum = 0;
        if (relationNumModes != null && relationNumModes.size() > 0) {
            for (RelationNumModel mode : relationNumModes) {
                if (mode.getRelation() == UserInfoManager.RELATION.FANS.getValue()) {
                    fansNum = mode.getCnt();
                }
            }
        }

        mHashMap.put(FANS_NUM_TAG, String.format(getResources().getString(R.string.fans_num_tag), fansNum));

        refreshTag();
    }

    private void showCharmsTag(int meiLiCntTotal) {
        mHashMap.put(CHARMS_TAG, String.format(getResources().getString(R.string.meili_tag), meiLiCntTotal));

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

    public void showUserRelation(final boolean isFriend, final boolean isFollow) {
        this.isFollow = isFollow;
        this.isFriend = isFriend;

        refreshFollow();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(RelationChangeEvent event) {
        if (event.useId == mUserId) {
            isFollow = event.isFollow;
            isFriend = event.isFriend;

            refreshFollow();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(FollowNotifyEvent event) {
        if (event.mUserInfoModel != null && event.mUserInfoModel.getUserId() == mUserId) {
            isFollow = event.mUserInfoModel.isFollow();
            isFriend = event.mUserInfoModel.isFriend();

            refreshFollow();
        }
    }

    // TODO: 2019/4/14 在卡片内，不提供取关功能
    private void refreshFollow() {
        mUserInfoModel.setFollow(isFollow);
        mUserInfoModel.setFriend(isFriend);
        if (isFriend) {
            mFollowIv.setBackgroundResource(R.drawable.person_card_friend);
            mSrlFollowIv.setBackgroundResource(R.drawable.person_card_friend);
            mFollowIv.setClickable(false);
            mSrlFollowIv.setClickable(false);
        } else if (isFollow) {
            mFollowIv.setBackgroundResource(R.drawable.person_card_followed);
            mSrlFollowIv.setBackgroundResource(R.drawable.person_card_followed);
            mFollowIv.setClickable(false);
            mSrlFollowIv.setClickable(false);
        } else {
            mFollowIv.setBackgroundResource(R.drawable.person_card_follow);
            mSrlFollowIv.setBackgroundResource(R.drawable.person_card_follow);
            mFollowIv.setClickable(true);
            mSrlFollowIv.setClickable(true);
        }
    }


    private void refreshTag() {
        mTags.clear();
        if (mHashMap != null) {

            if (!TextUtils.isEmpty(mHashMap.get(CHARMS_TAG))) {
                mTags.add(mHashMap.get(CHARMS_TAG));
            }

            if (!TextUtils.isEmpty(mHashMap.get(LOCATION_TAG))) {
                mTags.add(mHashMap.get(LOCATION_TAG));
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
}
