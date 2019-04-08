package com.zq.person.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.common.base.BaseFragment;
import com.common.core.account.UserAccountManager;
import com.common.core.avatar.AvatarUtils;
import com.common.core.myinfo.MyUserInfoManager;
import com.common.core.userinfo.model.UserInfoModel;
import com.common.core.userinfo.model.UserLevelModel;
import com.common.flowlayout.FlowLayout;
import com.common.flowlayout.TagAdapter;
import com.common.flowlayout.TagFlowLayout;
import com.common.log.MyLog;
import com.common.utils.U;
import com.common.view.ex.ExTextView;
import com.common.view.recyclerview.RecyclerOnItemClickListener;
import com.component.busilib.R;
import com.facebook.drawee.view.SimpleDraweeView;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnRefreshLoadMoreListener;
import com.zq.person.adapter.PhotoAdapter;
import com.zq.person.model.PhotoModel;
import com.zq.person.presenter.PersonInfoCardPresenter;
import com.zq.person.view.IPersonCardView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PersonInfoCardFragment extends BaseFragment implements IPersonCardView {

    public final static String TAG = "PersonInfoCardFragment";
    public static final String CARD_USER_ID = "card_user_id";
    public static final String CARD_SHOW_KICK = "card_show_kick";

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

    PersonInfoCardPresenter mPresenter;

    int mUserId;
    boolean isShowKick;
    boolean isFollow;
    boolean isFriend;

    int mOffset = 0;
    int DEFAULT_CNT = 10;

    boolean hasInitHeight = false;

    @Override
    public int initView() {
        return R.layout.person_info_card_fragment_layout;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        initBaseContainInfo();
        initUserInfo();
        initOpretaArea();
        initPhotoArea();

        Bundle bundle = getArguments();
        if (bundle != null) {
            mUserId = bundle.getInt(CARD_USER_ID);
            isShowKick = bundle.getBoolean(CARD_SHOW_KICK);
        }

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

        mPresenter = new PersonInfoCardPresenter(this);
        if (mUserId > 0) {
            mPresenter.getHomePage(mUserId);
            mPresenter.getPhotos(mUserId, 0, DEFAULT_CNT);
        } else {
            MyLog.w(TAG, "initData" + " mUserId 不合法");
        }
    }

    private void initBaseContainInfo() {
        mSmartRefresh = (SmartRefreshLayout) mRootView.findViewById(R.id.smart_refresh);
        mCoordinator = (CoordinatorLayout) mRootView.findViewById(R.id.coordinator);
        mAppbar = (AppBarLayout) mRootView.findViewById(R.id.appbar);
        mToolbarLayout = (CollapsingToolbarLayout) mRootView.findViewById(R.id.toolbar_layout);

        mSmartRefresh.setEnableLoadMore(true);
        mSmartRefresh.setEnableRefresh(false);

        mSmartRefresh.setOnRefreshListener(new OnRefreshLoadMoreListener() {
            @Override
            public void onLoadMore(@NonNull RefreshLayout refreshLayout) {
                mSmartRefresh.finishLoadMore();
                mPresenter.getPhotos(mUserId, mOffset, DEFAULT_CNT);
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
        mUserInfoArea = (RelativeLayout) mRootView.findViewById(R.id.user_info_area);
        mAvatarBg = (ImageView) mRootView.findViewById(R.id.avatar_bg);
        mAvatarIv = (SimpleDraweeView) mRootView.findViewById(R.id.avatar_iv);
        mReport = (ExTextView) mRootView.findViewById(R.id.report);
        mNameTv = (ExTextView) mRootView.findViewById(R.id.name_tv);
        mLevelTv = (ExTextView) mRootView.findViewById(R.id.level_tv);
        mFlowlayout = (TagFlowLayout) mRootView.findViewById(R.id.flowlayout);

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
    }

    private void initOpretaArea() {
        mOpretaArea = (RelativeLayout) mRootView.findViewById(R.id.opreta_area);
        mMessageIv = (ImageView) mRootView.findViewById(R.id.message_iv);
        mFollowIv = (ImageView) mRootView.findViewById(R.id.follow_iv);
        mKickIv = (ImageView) mRootView.findViewById(R.id.kick_iv);
        mSrlAvatarArea = (RelativeLayout) mRootView.findViewById(R.id.srl_avatar_area);
        mSrlAvatarBg = (ImageView) mRootView.findViewById(R.id.srl_avatar_bg);
        mSrlAvatarIv = (SimpleDraweeView) mRootView.findViewById(R.id.srl_avatar_iv);
    }

    private void initPhotoArea() {
        mPhotoView = (RecyclerView) mRootView.findViewById(R.id.photo_view);

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

    @Override
    public boolean useEventBus() {
        return false;
    }

    @Override
    public void showUserInfo(UserInfoModel model) {
        if (model != null) {
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

    @Override
    public void showUserLevel(List<UserLevelModel> list) {
        String rankDesc = "";
        for (UserLevelModel userLevelModel : list) {
            if (userLevelModel.getType() == UserLevelModel.SUB_RANKING_TYPE) {
                rankDesc = userLevelModel.getDesc();
            }
        }
        mLevelTv.setText(rankDesc);
    }

    @Override
    public void showUserRelation(boolean isFriend, boolean isFollow) {
        this.isFollow = isFollow;
        this.isFriend = isFriend;
    }

    @Override
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
}
