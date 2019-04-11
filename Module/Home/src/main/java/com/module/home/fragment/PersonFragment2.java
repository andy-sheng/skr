package com.module.home.fragment;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.SpannableStringBuilder;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.common.base.BaseFragment;
import com.common.core.avatar.AvatarUtils;
import com.common.core.myinfo.MyUserInfoManager;
import com.common.core.myinfo.event.MyUserInfoEvent;
import com.common.core.upgrade.UpgradeData;
import com.common.core.upgrade.UpgradeManager;
import com.common.core.userinfo.UserInfoManager;
import com.common.core.userinfo.event.RelationChangeEvent;
import com.common.core.userinfo.model.GameStatisModel;
import com.common.core.userinfo.model.UserLevelModel;
import com.common.core.userinfo.model.UserRankModel;
import com.common.log.MyLog;
import com.common.notification.event.FollowNotifyEvent;
import com.common.utils.FragmentUtils;
import com.common.utils.SpanUtils;
import com.common.utils.U;
import com.common.view.DebounceViewClickListener;
import com.common.view.ex.ExImageView;
import com.common.view.ex.ExRelativeLayout;
import com.common.view.ex.ExTextView;
import com.common.view.recyclerview.RecyclerOnItemClickListener;
import com.common.view.titlebar.CommonTitleBar;
import com.component.busilib.constans.GameModeType;
import com.component.busilib.manager.WeakRedDotManager;
import com.component.busilib.view.BitmapTextView;
import com.facebook.drawee.view.SimpleDraweeView;
import com.module.home.R;
import com.module.home.musictest.fragment.MusicTestFragment;
import com.module.home.persenter.PersonCorePresenter;
import com.module.home.setting.fragment.SettingFragment;
import com.module.home.view.IPersonView;
import com.respicker.ResPicker;
import com.respicker.activity.ResPickerActivity;
import com.respicker.model.ImageItem;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.header.ClassicsHeader;
import com.scwang.smartrefresh.layout.listener.OnRefreshLoadMoreListener;
import com.zq.level.view.NormalLevelView2;
import com.zq.person.adapter.PhotoAdapter;
import com.zq.person.model.AddPhotoModel;
import com.zq.person.model.PhotoModel;
import com.zq.relation.fragment.RelationFragment;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import java.util.List;
import model.RelationNumModel;

public class PersonFragment2 extends BaseFragment implements IPersonView, WeakRedDotManager.WeakRedDotListener {

    SmartRefreshLayout mSmartRefresh;
    ClassicsHeader mClassicsHeader;
    ExTextView mPhotoNumTv;
    RecyclerView mPhotoView;
    RelativeLayout mUserInfoArea;
    CommonTitleBar mTitlebar;
    ImageView mAvatarBg;
    SimpleDraweeView mAvatarIv;
    RelativeLayout mSettingArea;
    ImageView mSettingImgIv;
    ExImageView mSettingRedDot;
    ExTextView mNameTv;
    ExTextView mUseridTv;
    LinearLayout mRelationNumArea;
    RelativeLayout mFriendsArea;
    ExTextView mFriendsNumTv;
    ExImageView mFriendRedDot;
    RelativeLayout mFollowsArea;
    ExTextView mFollowsNumTv;
    RelativeLayout mFansArea;
    ExTextView mFansNumTv;
    ExImageView mFansRedDot;
    LinearLayout mFunctionArea;
    ExImageView mWalletIv;
    ExImageView mMusicTestIv;
    ExRelativeLayout mMedalLayout;
    ImageView mPaiweiImg;
    BitmapTextView mRankNumTv;
    ImageView mSingendImg;
    BitmapTextView mSingendNumTv;
    NormalLevelView2 mLevelView;
    ExTextView mLevelTv;

    AppBarLayout mAppbar;
    Toolbar mToolbar;
    TextView mSrlNameTv;

    PersonCorePresenter mPresenter;

    PhotoAdapter mPhotoAdapter;

    int offset = 0;
    int DEFAUAT_CNT = 20;       // 默认拉取一页的数量
    boolean mHasMore = false;

    int mFriendNum = 0;  // 好友数
    int mFansNum = 0;    // 粉丝数
    int mFocusNum = 0;   // 关注数

    int mFansRedDotValue = 0;
    int mFriendRedDotValue = 0;

    @Override
    public int initView() {
        return R.layout.person_fragment2_layout;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        initBaseContainArea();
        initUserInfoArea();
        initSettingArea();
        initRelationNumArea();
        initFunctionArea();
        initGameInfoArea();
        initPhotoArea();

        mPresenter = new PersonCorePresenter(this);
        addPresent(mPresenter);

        WeakRedDotManager.getInstance().addListener(this);
        mFansRedDotValue = U.getPreferenceUtils().getSettingInt(WeakRedDotManager.SP_KEY_NEW_FANS, 0);
        mFriendRedDotValue = U.getPreferenceUtils().getSettingInt(WeakRedDotManager.SP_KEY_NEW_FRIEND, 0);
        refreshPersonRedDot();
        refreshUserInfoView();
    }

    @Override
    protected void onFragmentVisible() {
        super.onFragmentVisible();
        mPresenter.getHomePage(false);
        mPresenter.getPhotos(0, DEFAUAT_CNT, false);
    }

    private void initBaseContainArea() {
        mSmartRefresh = (SmartRefreshLayout) mRootView.findViewById(R.id.smart_refresh);
        mClassicsHeader = (ClassicsHeader) mRootView.findViewById(R.id.classics_header);
        mUserInfoArea = (RelativeLayout) mRootView.findViewById(R.id.user_info_area);
        mTitlebar = (CommonTitleBar) mRootView.findViewById(R.id.titlebar);

        mAppbar = (AppBarLayout) mRootView.findViewById(R.id.appbar);
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
                mPresenter.getPhotos(offset, DEFAUAT_CNT, false);
            }

            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {
                mPresenter.getHomePage(true);
                mPresenter.getPhotos(0, DEFAUAT_CNT, true);
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
        mAvatarBg = (ImageView) mRootView.findViewById(R.id.avatar_bg);
        mAvatarIv = (SimpleDraweeView) mRootView.findViewById(R.id.avatar_iv);
        mNameTv = (ExTextView) mRootView.findViewById(R.id.name_tv);
        mUseridTv = (ExTextView) mRootView.findViewById(R.id.userid_tv);
    }

    private void initSettingArea() {
        mSettingArea = (RelativeLayout) mRootView.findViewById(R.id.setting_area);
        mSettingImgIv = (ImageView) mRootView.findViewById(R.id.setting_img_iv);
        mSettingRedDot = (ExImageView) mRootView.findViewById(R.id.setting_red_dot);

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
        updateSettingRedDot();
    }


    private void updateSettingRedDot() {
        if (UpgradeManager.getInstance().needShowRedDotTips()) {
            mSettingRedDot.setVisibility(View.VISIBLE);
        } else {
            mSettingRedDot.setVisibility(View.GONE);
        }
    }

    private void initRelationNumArea() {
        mRelationNumArea = (LinearLayout) mRootView.findViewById(R.id.relation_num_area);
        mFriendsArea = (RelativeLayout) mRootView.findViewById(R.id.friends_area);
        mFriendsNumTv = (ExTextView) mRootView.findViewById(R.id.friends_num_tv);
        mFriendRedDot = (ExImageView) mRootView.findViewById(R.id.friend_red_dot);
        mFollowsArea = (RelativeLayout) mRootView.findViewById(R.id.follows_area);
        mFollowsNumTv = (ExTextView) mRootView.findViewById(R.id.follows_num_tv);
        mFansArea = (RelativeLayout) mRootView.findViewById(R.id.fans_area);
        mFansNumTv = (ExTextView) mRootView.findViewById(R.id.fans_num_tv);
        mFansRedDot = (ExImageView) mRootView.findViewById(R.id.fans_red_dot);

        mFriendsArea.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                WeakRedDotManager.getInstance().updateWeakRedRot(WeakRedDotManager.FRIEND_RED_ROD_TYPE, 0);
                // 好友，双向关注
                openRelationFragment(RelationFragment.FROM_FRIENDS);
            }
        });

        mFansArea.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                WeakRedDotManager.getInstance().updateWeakRedRot(WeakRedDotManager.FANS_RED_ROD_TYPE, 0);
                // 粉丝，我关注的
                openRelationFragment(RelationFragment.FROM_FANS);
            }
        });

        mFollowsArea.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
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

    private void initFunctionArea() {
        mFunctionArea = (LinearLayout) mRootView.findViewById(R.id.function_area);
        mWalletIv = (ExImageView) mRootView.findViewById(R.id.wallet_iv);
        mMusicTestIv = (ExImageView) mRootView.findViewById(R.id.music_test_iv);

        mWalletIv.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                U.getFragmentUtils().addFragment(
                        FragmentUtils.newAddParamsBuilder(getActivity(), WalletFragment.class)
                                .setAddToBackStack(true)
                                .setHasAnimation(true)
                                .build());
            }
        });

        mMusicTestIv.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                U.getFragmentUtils().addFragment(
                        FragmentUtils.newAddParamsBuilder(getActivity(), MusicTestFragment.class)
                                .setAddToBackStack(true)
                                .setHasAnimation(true)
                                .build());
            }
        });
    }

    private void initGameInfoArea() {
        mMedalLayout = (ExRelativeLayout) mRootView.findViewById(R.id.medal_layout);
        mPaiweiImg = (ImageView) mRootView.findViewById(R.id.paiwei_img);
        mRankNumTv = (BitmapTextView) mRootView.findViewById(R.id.rank_num_tv);
        mSingendImg = (ImageView) mRootView.findViewById(R.id.singend_img);
        mSingendNumTv = (BitmapTextView) mRootView.findViewById(R.id.singend_num_tv);
        mLevelView = (NormalLevelView2) mRootView.findViewById(R.id.level_view);
        mLevelTv = (ExTextView) mRootView.findViewById(R.id.level_tv);
    }

    private void initPhotoArea() {
        mPhotoNumTv = (ExTextView) mRootView.findViewById(R.id.photo_num_tv);
        mPhotoView = (RecyclerView) mRootView.findViewById(R.id.photo_view);

        mPhotoView.setFocusableInTouchMode(false);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 3);
        mPhotoView.setLayoutManager(gridLayoutManager);
        mPhotoAdapter = new PhotoAdapter(new RecyclerOnItemClickListener() {
            @Override
            public void onItemClicked(View view, int position, Object model) {
                if (model instanceof AddPhotoModel) {
                    goAddPhotoFragment();
                }
            }
        }, true);
        mPhotoView.setAdapter(mPhotoAdapter);
    }

    void goAddPhotoFragment() {
        ResPicker.getInstance().setParams(ResPicker.newParamsBuilder()
                .setMultiMode(true)
                .setShowCamera(true)
                .setCrop(false)
                .setSelectLimit(9)
                .build()
        );
        ResPickerActivity.open(getActivity());
    }

    @Override
    public boolean onActivityResultReal(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK && requestCode == ResPickerActivity.REQ_CODE_RES_PICK) {
            List<ImageItem> imageItems = ResPicker.getInstance().getSelectedImageList();
            mPresenter.uploadPhotoList(imageItems);
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
            , List<UserLevelModel> userLevelModels, List<GameStatisModel> gameStatisModels) {
        mSmartRefresh.finishRefresh();
        showRelationNum(relationNumModels);
        showReginRank(userRankModels);
        showUserLevel(userLevelModels);
        showGameStatic(gameStatisModels);
    }

    private void refreshUserInfoView() {
        if (MyUserInfoManager.getInstance().hasMyUserInfo()) {
            AvatarUtils.loadAvatarByUrl(mAvatarIv, AvatarUtils.newParamsBuilder(MyUserInfoManager.getInstance()
                    .getAvatar())
                    .setCircle(true)
                    .build());
            mNameTv.setText(MyUserInfoManager.getInstance().getNickName());
            mUseridTv.setText("撕歌号：" + MyUserInfoManager.getInstance().getUid());
            mSrlNameTv.setText(MyUserInfoManager.getInstance().getNickName());
        }
    }

    @Override
    public void showRelationNum(List<RelationNumModel> list) {
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


    private void showReginRank(List<UserRankModel> list) {

    }


    private void showUserLevel(List<UserLevelModel> list) {
        int rank = 0;           //当前父段位
        int subRank = 0;        //当前子段位
        String levelDesc = "";
        // 展示段位信息
        for (UserLevelModel userLevelModel : list) {
            if (userLevelModel.getType() == UserLevelModel.RANKING_TYPE) {
                rank = userLevelModel.getScore();
            } else if (userLevelModel.getType() == UserLevelModel.SUB_RANKING_TYPE) {
                subRank = userLevelModel.getScore();
                levelDesc = userLevelModel.getDesc();
            }
        }
        mLevelView.bindData(rank, subRank);
        mLevelTv.setText(levelDesc);
    }


    private void showGameStatic(List<GameStatisModel> list) {
        for (GameStatisModel gameStatisModel : list) {
            if (gameStatisModel.getMode() == GameModeType.GAME_MODE_CLASSIC_RANK) {
                mRankNumTv.setText("" + gameStatisModel.getTotalTimes());
            } else if (gameStatisModel.getMode() == GameModeType.GAME_MODE_GRAB) {
                mSingendNumTv.setText("" + gameStatisModel.getTotalTimes());
            }
        }
    }

    @Override
    public void showRankView(UserRankModel userRankModel) {

    }

    @Override
    public void showPhoto(List<PhotoModel> list, int offset, int totalNum) {
        MyLog.d(TAG, "showPhoto" + " list=" + list + " offset=" + offset + " totalNum=" + totalNum);
        this.offset = offset;
        mSmartRefresh.finishRefresh();
        mSmartRefresh.finishLoadMore();

        mPhotoNumTv.setText("个人相册（" + totalNum + "）");
        mPhotoNumTv.setVisibility(View.VISIBLE);


        if (list != null && list.size() > 0) {
            mHasMore = true;
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
            }
        }
    }

    @Override
    public void insertPhoto(PhotoModel photoModel) {
        mPhotoAdapter.insertFirst(photoModel);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvnet(MyUserInfoEvent.UserInfoChangeEvent userInfoChangeEvent) {
        refreshUserInfoView();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(RelationChangeEvent event) {
        mPresenter.getRelationNums();
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(FollowNotifyEvent event) {
        mPresenter.getRelationNums();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(UpgradeData.RedDotStatusEvent event) {
        updateSettingRedDot();
    }

    @Override
    public int[] acceptType() {
        return new int[]{
                WeakRedDotManager.FANS_RED_ROD_TYPE,
                WeakRedDotManager.FRIEND_RED_ROD_TYPE
        };
    }

    @Override
    public void onWeakRedDotChange(int type, int value) {
        if (type == WeakRedDotManager.FANS_RED_ROD_TYPE) {
            mFansRedDotValue = value;
        } else if (type == WeakRedDotManager.FRIEND_RED_ROD_TYPE) {
            mFriendRedDotValue = value;
        }

        refreshPersonRedDot();
    }

    private void refreshPersonRedDot() {
        // 关注和粉丝红点
        if (mFansRedDotValue < 1) {
            mFansRedDot.setVisibility(View.GONE);
        } else {
            mFansRedDot.setVisibility(View.VISIBLE);
        }

        if (mFriendRedDotValue < 1) {
            mFriendRedDot.setVisibility(View.GONE);
        } else {
            mFriendRedDot.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void destroy() {
        super.destroy();
        WeakRedDotManager.getInstance().removeListener(this);
    }
}
