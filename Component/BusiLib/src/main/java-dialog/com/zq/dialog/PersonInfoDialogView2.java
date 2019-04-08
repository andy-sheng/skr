package com.zq.dialog;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.alibaba.fastjson.JSON;
import com.common.base.BaseActivity;
import com.common.core.account.UserAccountManager;
import com.common.core.avatar.AvatarUtils;
import com.common.core.myinfo.MyUserInfoManager;
import com.common.core.userinfo.UserInfoServerApi;
import com.common.core.userinfo.event.RelationChangeEvent;
import com.common.core.userinfo.model.GameStatisModel;
import com.common.core.userinfo.model.UserInfoModel;
import com.common.core.userinfo.model.UserLevelModel;
import com.common.core.userinfo.model.UserRankModel;
import com.common.flowlayout.FlowLayout;
import com.common.flowlayout.TagAdapter;
import com.common.flowlayout.TagFlowLayout;
import com.common.rxretrofit.ApiManager;
import com.common.rxretrofit.ApiMethods;
import com.common.rxretrofit.ApiObserver;
import com.common.rxretrofit.ApiResult;
import com.common.utils.U;
import com.common.view.DebounceViewClickListener;
import com.common.view.ex.ExTextView;
import com.common.view.recyclerview.RecyclerOnItemClickListener;
import com.component.busilib.R;
import com.facebook.drawee.view.SimpleDraweeView;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnRefreshLoadMoreListener;
import com.zq.person.adapter.PhotoAdapter;
import com.zq.person.model.PhotoModel;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PersonInfoDialogView2 extends RelativeLayout {

    public final static String TAG = "PersonInfoDialogView2";

    SmartRefreshLayout mSmartRefresh;
    CoordinatorLayout mCoordinator;
    RecyclerView mPhotoView;
    AppBarLayout mAppbar;
    CollapsingToolbarLayout mToolbarLayout;

    RelativeLayout mUserInfoArea;   // 个人信息
    ImageView mAvatarBg;
    SimpleDraweeView mAvatarIv;
    ExTextView mReport;
    ExTextView mNameTv;
    ExTextView mLevelTv;
    TagFlowLayout mFlowlayout;

    RelativeLayout mOpretaArea;     // 私信，关注，踢人
    ImageView mMessageIv;
    ImageView mFollowIv;
    ImageView mKickIv;
    RelativeLayout mSrlAvatarArea;
    ImageView mSrlAvatarBg;
    SimpleDraweeView mSrlAvatarIv;

    private static final int LOCATION_TAG = 0;           //区域标签
    private static final int AGE_TAG = 1;                //年龄标签
    private static final int CONSTELLATION_TAG = 2;      //星座标签

    private List<String> mTags = new ArrayList<>();  //标签
    private HashMap<Integer, String> mHashMap = new HashMap();

    TagAdapter<String> mTagAdapter;

    PhotoAdapter mPhotoAdapter;

    int mUserId;
    UserInfoModel mUserInfoModel = new UserInfoModel();
    boolean isShowKick;
    boolean isFollow;
    boolean isFriend;

    Context mContext;
    UserInfoServerApi mUserInfoServerApi;

    int mOffset = 0;
    int DEFAULT_CNT = 10;

    boolean hasInitHeight = false;

    PersonInfoDialog.PersonCardClickListener mClickListener;

    PersonInfoDialogView2(Context context, int userID, boolean showReport, boolean showKick) {
        super(context);
        initView();
        initData(context, userID, showReport, showKick);
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }

    public void setListener(PersonInfoDialog.PersonCardClickListener listener) {
        this.mClickListener = listener;
    }

    private void initView() {
        inflate(getContext(), R.layout.person_info_card_fragment_layout, this);

        initBaseContainInfo();
        initUserInfo();
        initOpretaArea();
        initPhotoArea();
    }

    private void initData(Context context, int userID, boolean showReport, boolean showKick) {
        mContext = context;
        mUserId = userID;
        isShowKick = showKick;

        mReport.setVisibility(showReport ? VISIBLE : GONE);
        mKickIv.setVisibility(showKick ? VISIBLE : GONE);

        // 多音和ai裁判
        if (mUserId == UserAccountManager.SYSTEM_GRAB_ID || mUserId == UserAccountManager.SYSTEM_RANK_AI) {
            mReport.setVisibility(View.GONE);
            mKickIv.setVisibility(View.GONE);
        }

        // 自己卡片的处理
        if (mUserId == MyUserInfoManager.getInstance().getUid()) {
            mReport.setVisibility(View.GONE);
            mOpretaArea.setVisibility(View.GONE);
            mKickIv.setVisibility(View.GONE);
            mFollowIv.setVisibility(View.GONE);
            mMessageIv.setVisibility(View.GONE);
            // TODO: 2019/4/8 可能需要调整布局
        }

        mUserInfoServerApi = ApiManager.getInstance().createService(UserInfoServerApi.class);
        getHomePage(mUserId);
//        getPhotos(mUserId, 0, DEFAULT_CNT);
    }

    private void getHomePage(int userId) {
        ApiMethods.subscribe(mUserInfoServerApi.getHomePage(userId), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult result) {
                if (result.getErrno() == 0) {
                    UserInfoModel userInfoModel = JSON.parseObject(result.getData().getString("userBaseInfo"), UserInfoModel.class);
//                    List<UserRankModel> userRankModels = JSON.parseArray(result.getData().getJSONObject("userRankInfo").getString("seqInfo"), UserRankModel.class);
//                    List<RelationNumModel> relationNumModes = JSON.parseArray(result.getData().getJSONObject("userRelationCntInfo").getString("cnt"), RelationNumModel.class);
                    List<UserLevelModel> userLevelModels = JSON.parseArray(result.getData().getJSONObject("userScoreInfo").getString("userScore"), UserLevelModel.class);
//                    List<GameStatisModel> userGameStatisModels = JSON.parseArray(result.getData().getJSONObject("userGameStatisticsInfo").getString("statistic"), GameStatisModel.class);

                    boolean isFriend = result.getData().getJSONObject("userMateInfo").getBoolean("isFriend");
                    boolean isFollow = result.getData().getJSONObject("userMateInfo").getBoolean("isFollow");

                    showUserInfo(userInfoModel);
                    showUserLevel(userLevelModels);
                    showUserRelation(isFriend, isFollow);
                }
            }
        }, (BaseActivity) mContext);
    }

    public void getPhotos(int userID, int offset, int cnt) {

        ApiMethods.subscribe(mUserInfoServerApi.getPhotos(userID, offset, cnt), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult result) {
                if (result.getErrno() == 0) {
                    List<PhotoModel> list = JSON.parseArray(result.getData().getString("pic"), PhotoModel.class);
                    int newOffset = result.getData().getIntValue("offset");
                    showPhotos(list, newOffset);
                }

            }
        }, (BaseActivity) mContext);
    }


    private void initBaseContainInfo() {
        ViewGroup personCardMainContainer = this.findViewById(R.id.person_card_main_containner);
        personCardMainContainer.setOnClickListener(new DebounceViewClickListener() {
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

        mSmartRefresh.setEnableLoadMore(true);
        mSmartRefresh.setEnableRefresh(false);

        mSmartRefresh.setOnRefreshListener(new OnRefreshLoadMoreListener() {
            @Override
            public void onLoadMore(@NonNull RefreshLayout refreshLayout) {
                mSmartRefresh.finishLoadMore();
                getPhotos(mUserId, mOffset, DEFAULT_CNT);
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
                    mKickIv.setVisibility(View.VISIBLE);
                    mSrlAvatarArea.setVisibility(View.GONE);
                    mOpretaArea.setBackground(null);
                } else if (Math.abs(verticalOffset) >= appBarLayout.getTotalScrollRange()) {
                    // 完全收缩状态
                    mKickIv.setVisibility(View.GONE);
                    mSrlAvatarArea.setVisibility(View.VISIBLE);
                    mOpretaArea.setBackground(getResources().getDrawable(R.drawable.person_info_top_bg));
                } else {
                    // TODO: 2019/4/8 过程中，可以加动画，先直接显示
                    mKickIv.setVisibility(View.VISIBLE);
                    mSrlAvatarArea.setVisibility(View.GONE);
                    mOpretaArea.setBackground(null);
                }
            }
        });
    }

    private void initUserInfo() {
        mUserInfoArea = (RelativeLayout) this.findViewById(R.id.user_info_area);
        mAvatarBg = (ImageView) this.findViewById(R.id.avatar_bg);
        mAvatarIv = (SimpleDraweeView) this.findViewById(R.id.avatar_iv);
        mReport = (ExTextView) this.findViewById(R.id.report);
        mNameTv = (ExTextView) this.findViewById(R.id.name_tv);
        mLevelTv = (ExTextView) this.findViewById(R.id.level_tv);
        mFlowlayout = (TagFlowLayout) this.findViewById(R.id.flowlayout);

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

        mReport.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                if (mClickListener != null) {
                    mClickListener.onClickReport(mUserId);
                }
            }
        });

        mAvatarIv.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                if (mClickListener != null) {
                    mClickListener.onClickAvatar(mUserInfoModel.getAvatar());
                }
            }
        });
    }

    private void initOpretaArea() {
        mOpretaArea = (RelativeLayout) this.findViewById(R.id.opreta_area);
        mMessageIv = (ImageView) this.findViewById(R.id.message_iv);
        mFollowIv = (ImageView) this.findViewById(R.id.follow_iv);
        mKickIv = (ImageView) this.findViewById(R.id.kick_iv);
        mSrlAvatarArea = (RelativeLayout) this.findViewById(R.id.srl_avatar_area);
        mSrlAvatarBg = (ImageView) this.findViewById(R.id.srl_avatar_bg);
        mSrlAvatarIv = (SimpleDraweeView) this.findViewById(R.id.srl_avatar_iv);

        mKickIv.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                if (mClickListener != null) {
                    mClickListener.onClickKick(mUserId);
                }
            }
        });

        mFollowIv.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                if (mClickListener != null) {
                    mClickListener.onClickFollow(mUserId, isFriend, isFollow);
                }
            }
        });

        mMessageIv.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                if (mClickListener != null) {
                    mClickListener.onClickMessage(mUserInfoModel);
                }
            }
        });
    }

    private void initPhotoArea() {
        mPhotoView = (RecyclerView) this.findViewById(R.id.photo_view);

        mPhotoView.setFocusableInTouchMode(false);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 3);
        mPhotoView.setLayoutManager(gridLayoutManager);
        mPhotoAdapter = new PhotoAdapter(new RecyclerOnItemClickListener() {
            @Override
            public void onItemClicked(View view, int position, Object model) {
                // TODO: 2019/4/8 看大图
            }
        });
        mPhotoView.setAdapter(mPhotoAdapter);
    }


    public void showPhotos(List<PhotoModel> list, int offset) {
        this.mOffset = offset;

        if (list != null && list.size() != 0) {
            if (!hasInitHeight) {
                ViewGroup.LayoutParams layoutParams = mSmartRefresh.getLayoutParams();
                layoutParams.height = U.getDisplayUtils().dip2px(375);
                mSmartRefresh.setLayoutParams(layoutParams);
                hasInitHeight = true;
            }
            mPhotoAdapter.getDataList().addAll(list);
            mPhotoAdapter.setDataList(list);
            mPhotoAdapter.notifyDataSetChanged();
        } else {
            if (mPhotoAdapter.getDataList() != null && mPhotoAdapter.getDataList().size() > 0) {
                // 没有更多了
                mSmartRefresh.setEnableRefresh(false);//是否启用下拉刷新功能
                mSmartRefresh.setEnableLoadMore(false);//是否启用上拉加载功能
            } else {
                // 没有数据
                // TODO: 2019/4/8 禁止整个布局的滑动
                mSmartRefresh.setEnableRefresh(false);//是否启用下拉刷新功能
                mSmartRefresh.setEnableLoadMore(false);//是否启用上拉加载功能
                if (mAppbar != null && mAppbar.getLayoutParams() != null) {
                    CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) mAppbar.getLayoutParams();
                    AppBarLayout.Behavior behavior = new AppBarLayout.Behavior();
                    behavior.setDragCallback(new AppBarLayout.Behavior.DragCallback() {
                        @Override
                        public boolean canDrag(@NonNull AppBarLayout appBarLayout) {
                            return false;
                        }
                    });
                    params.setBehavior(behavior);
                    mAppbar.setLayoutParams(params);
                }
            }
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

            mNameTv.setText(model.getNickname());

            if (model.getLocation() != null && !TextUtils.isEmpty(model.getLocation().getCity())) {
                mHashMap.put(LOCATION_TAG, model.getLocation().getCity());
            } else {
                mHashMap.put(LOCATION_TAG, "未知星球");
            }

            if (!TextUtils.isEmpty(model.getBirthday())) {
                mHashMap.put(AGE_TAG, String.format(U.app().getString(R.string.age_tag), model.getAge()));
            }

            refreshTag();
        }
    }

    public void showUserLevel(List<UserLevelModel> list) {
        String rankDesc = "";
        for (UserLevelModel userLevelModel : list) {
            if (userLevelModel.getType() == UserLevelModel.SUB_RANKING_TYPE) {
                rankDesc = userLevelModel.getDesc();
            }
        }
        mLevelTv.setText(rankDesc);
    }

    public void showUserRelation(final boolean isFriend, final boolean isFollow) {
        this.isFollow = isFollow;
        this.isFriend = isFriend;
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
        }
        mTagAdapter.setTagDatas(mTags);
        mTagAdapter.notifyDataChanged();
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(RelationChangeEvent event) {

    }
}
