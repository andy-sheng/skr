package com.module.playways.rank.room.fragment;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.common.base.BaseFragment;
import com.common.core.avatar.AvatarUtils;
import com.common.core.myinfo.Location;
import com.common.core.myinfo.MyUserInfoManager;
import com.common.core.myinfo.event.MyUserInfoEvent;
import com.common.core.permission.SkrLocationPermission;
import com.common.core.userinfo.model.RankInfoModel;
import com.common.core.userinfo.model.UserRankModel;
import com.common.utils.FragmentUtils;
import com.common.utils.LbsUtils;
import com.common.utils.NetworkUtils;
import com.common.permission.PermissionUtils;
import com.common.utils.U;
import com.common.view.DebounceViewClickListener;
import com.common.view.ex.ExImageView;
import com.common.view.ex.ExTextView;
import com.facebook.drawee.view.SimpleDraweeView;
import com.jakewharton.rxbinding2.view.RxView;
import com.module.playways.rank.room.adapter.LeaderBoardAdapter;
import com.module.playways.rank.room.presenter.LeaderboardPresenter;
import com.module.playways.rank.room.view.ILeaderBoardView;
import com.module.rank.R;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnRefreshLoadMoreListener;
import com.zq.level.view.NormalLevelView2;
import com.zq.live.proto.Common.ESex;
import com.zq.person.fragment.OtherPersonFragment;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import io.reactivex.functions.Consumer;

/**
 * 排行榜
 */
public class LeaderboardFragment extends BaseFragment implements ILeaderBoardView {

    public final static String TAG = "LeaderboardFragment";

    RecyclerView mRecyclerView;

    LeaderBoardAdapter mLeaderBoardAdapter;

    LeaderboardPresenter mLeaderboardPresenter;

    SimpleDraweeView mSdvRightChampainIcon;
    ExTextView mTvRightChanpainName;
    NormalLevelView2 mRightChanpainLevelView;
    ExTextView mRightChanpainLevelTv;

    SimpleDraweeView mSdvLeftChampainIcon;
    ExTextView mTvLeftChanpainName;
    NormalLevelView2 mLeftChanpainLevelView;
    ExTextView mLeftChanpainLevelTv;

    SimpleDraweeView mSdvChampainIcon;
    ExTextView mTvChanpainName;
    NormalLevelView2 mChanpainLevelView;
    ExTextView mChanpainLevelTv;

    TextView mTvArea;
    ExImageView mIvBack;

    LinearLayout mLlAreaContainer;
    ExTextView mTvOtherArea;

    SmartRefreshLayout mRefreshLayout;
    boolean mHasMore = true;

    ImageView mIvRankLeft;
    ImageView mIvRank;
    ImageView mIvRankRight;

    PopupWindow mPopupWindow;

    View mOwnInfoItem;

    int mRankMode = UserRankModel.COUNTRY;

    SkrLocationPermission mSkrLocationPermission = new SkrLocationPermission();

    @Override
    public int initView() {
        return R.layout.leader_board_fragment_layout;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        mRecyclerView = (RecyclerView) mRootView.findViewById(R.id.recycler_view);
        mLeaderBoardAdapter = new LeaderBoardAdapter();
        mLeaderboardPresenter = new LeaderboardPresenter(this);
        addPresent(mLeaderboardPresenter);
        mRecyclerView.setAdapter(mLeaderBoardAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        mOwnInfoItem = mRootView.findViewById(R.id.own_info_item);
        mOwnInfoItem.setBackgroundColor(Color.parseColor("#6868A1"));

        mSdvRightChampainIcon = (SimpleDraweeView) mRootView.findViewById(R.id.sdv_right_champain_icon);
        mTvRightChanpainName = (ExTextView) mRootView.findViewById(R.id.tv_right_chanpain_name);
        mRightChanpainLevelView = (NormalLevelView2) mRootView.findViewById(R.id.right_chanpain_level_view);
        mRightChanpainLevelTv = (ExTextView) mRootView.findViewById(R.id.right_chanpain_level_tv);

        mSdvLeftChampainIcon = (SimpleDraweeView) mRootView.findViewById(R.id.sdv_left_champain_icon);
        mTvLeftChanpainName = (ExTextView) mRootView.findViewById(R.id.tv_left_chanpain_name);
        mLeftChanpainLevelView = (NormalLevelView2) mRootView.findViewById(R.id.left_chanpain_level_view);
        mLeftChanpainLevelTv = (ExTextView) mRootView.findViewById(R.id.left_chanpain_level_tv);

        mSdvChampainIcon = (SimpleDraweeView) mRootView.findViewById(R.id.sdv_champain_icon);
        mTvChanpainName = (ExTextView) mRootView.findViewById(R.id.tv_chanpain_name);
        mChanpainLevelView = (NormalLevelView2) mRootView.findViewById(R.id.chanpain_level_view);
        mChanpainLevelTv = (ExTextView) mRootView.findViewById(R.id.chanpain_level_tv);

        mIvRankLeft = (ImageView) mRootView.findViewById(R.id.iv_rank_left);
        mIvRank = (ImageView) mRootView.findViewById(R.id.iv_rank);
        mIvRankRight = (ImageView) mRootView.findViewById(R.id.iv_rank_right);
        mRefreshLayout = mRootView.findViewById(R.id.refreshLayout);
        mTvArea = (ExTextView) mRootView.findViewById(R.id.tv_area);
        mIvBack = (ExImageView) mRootView.findViewById(R.id.iv_back);

        mLlAreaContainer = (LinearLayout) getActivity().getLayoutInflater().inflate(R.layout.area_select_popup_window_layout, null);
        mTvOtherArea = (ExTextView) mLlAreaContainer.findViewById(R.id.tv_other_area);

        U.getSoundUtils().preLoad(TAG, R.raw.normal_back);

        mPopupWindow = new PopupWindow(mLlAreaContainer);
        mPopupWindow.setOutsideTouchable(true);
        mRefreshLayout.setEnableRefresh(false);
        mRefreshLayout.setEnableLoadMore(true);
        mRefreshLayout.setEnableLoadMoreWhenContentNotFull(true);
        mRefreshLayout.setEnableOverScrollDrag(false);
        mRefreshLayout.setOnRefreshLoadMoreListener(new OnRefreshLoadMoreListener() {
            @Override
            public void onLoadMore(@NonNull RefreshLayout refreshLayout) {
                if (mHasMore) {
                    mLeaderboardPresenter.getLeaderBoardInfo();
                } else {
                    U.getToastUtil().showShort("没有更多数据了");
                }
            }

            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {
                mRefreshLayout.finishRefresh();
            }
        });

        mOwnInfoItem.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                gotoPersonFragment((int) MyUserInfoManager.getInstance().getUid());
            }
        });

        mTvArea.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                if (mPopupWindow.isShowing()) {
                    mPopupWindow.dismiss();
                } else {
                    mPopupWindow.setWidth(mTvArea.getMeasuredWidth());
                    mPopupWindow.setHeight(300);
                    mPopupWindow.showAsDropDown(mTvArea);
                }

//                        mLlAreaContainer.setVisibility(mLlAreaContainer.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
                Drawable drawable = null;

                if (mPopupWindow.isShowing()) {
                    drawable = getResources().getDrawable(R.drawable.paihangbang_xuanzediquxialaicon_down);
                    drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
                } else {
                    drawable = getResources().getDrawable(R.drawable.paihangbang_xuanzediquxialaicon);
                    drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
                }

                mTvArea.setCompoundDrawables(null, null, drawable, null);
                if (mRankMode == UserRankModel.COUNTRY) {
                    if (MyUserInfoManager.getInstance().hasLocation()) {
                        mTvOtherArea.setText(getAreaFromLocation(MyUserInfoManager.getInstance().getLocation()));
                    } else {
                        mTvOtherArea.setText("地域榜");
                    }
                } else if (mRankMode == UserRankModel.REGION) {
                    mTvOtherArea.setText("全国榜");
                }
                mTvOtherArea.setSelected(false);
            }
        });

        mTvOtherArea.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                mPopupWindow.dismiss();
                Drawable drawable = getResources().getDrawable(R.drawable.paihangbang_xuanzediquxialaicon);
                drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
                mTvArea.setCompoundDrawables(null, null, drawable, null);

                if (!MyUserInfoManager.getInstance().hasLocation()) {
                    tryGetLocation();
                    return;
                }

                if (!U.getNetworkUtils().hasNetwork()) {
                    noNetWork();
                    return;
                }

                if (mRankMode == UserRankModel.REGION) {
                    mTvArea.setText("全国榜");
                    mTvOtherArea.setText(getAreaFromLocation(MyUserInfoManager.getInstance().getLocation()));
                    mLeaderboardPresenter.setRankMode(UserRankModel.COUNTRY);
                    mRankMode = UserRankModel.COUNTRY;
                } else if (mRankMode == UserRankModel.COUNTRY) {
                    if (MyUserInfoManager.getInstance().hasLocation()) {
                        mTvArea.setText(getAreaFromLocation(MyUserInfoManager.getInstance().getLocation()));
                        mLeaderboardPresenter.setRankMode(UserRankModel.REGION);
                        mRankMode = UserRankModel.REGION;
                    } else {
                        mSkrLocationPermission.ensurePermission(new Runnable() {
                            @Override
                            public void run() {
                                MyUserInfoManager.getInstance().uploadLocation(new LbsUtils.Callback() {
                                    @Override
                                    public void onReceive(LbsUtils.Location location) {
                                        mTvArea.setText(getAreaFromLocation(MyUserInfoManager.getInstance().getLocation()));
                                        mLeaderboardPresenter.setRankMode(UserRankModel.REGION);
                                        mRankMode = UserRankModel.REGION;
                                    }
                                });
                            }
                        }, true);
                    }
                }
            }
        });

        mIvBack.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
//                U.getSoundUtils().play(LeaderboardFragment.TAG, R.raw.normal_back, 500);
                finish();
            }
        });

        setRankMode();
        if (!MyUserInfoManager.getInstance().hasLocation() && U.getPreferenceUtils().getSettingBoolean("tips_location_permission_in_rank", false)) {
            tryGetLocation();
        }
    }

    private void setRankMode() {
        if (MyUserInfoManager.getInstance().hasLocation()) {
            mTvArea.setText(getAreaFromLocation(MyUserInfoManager.getInstance().getLocation()));
            mLeaderboardPresenter.setRankMode(UserRankModel.REGION);
            mRankMode = UserRankModel.REGION;
            mTvOtherArea.setText("全国榜");
        } else {
            mLeaderboardPresenter.setRankMode(UserRankModel.COUNTRY);
            mRankMode = UserRankModel.COUNTRY;
//            mTvArea.setCompoundDrawables(null, null, null, null);
            mTvArea.setText("全国榜");
            mTvOtherArea.setText("地域榜");
        }
    }

    private String getAreaFromLocation(Location location) {
        if (!TextUtils.isEmpty(location.getDistrict())) {
            return location.getDistrict() + "榜";
        } else {
            return "未知位置";
        }
    }

    private void tryGetLocation() {
        boolean hasLocationPermmistion = U.getPermissionUtils().checkLocation(getActivity());

        if (!hasLocationPermmistion) {
            U.getPermissionUtils().requestLocation(new PermissionUtils.RequestPermission() {
                @Override
                public void onRequestPermissionSuccess() {
                    MyUserInfoManager.getInstance().uploadLocation();
                }

                @Override
                public void onRequestPermissionFailure(List<String> permissions) {

                }

                @Override
                public void onRequestPermissionFailureWithAskNeverAgain(List<String> permissions) {

                }
            }, getActivity());
        } else {
            MyUserInfoManager.getInstance().uploadLocation();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mSkrLocationPermission.onBackFromPermisionManagerMaybe();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvnet(MyUserInfoEvent.UserInfoChangeEvent userInfoChangeEvent) {
        if (MyUserInfoManager.getInstance().hasLocation()) {
            setRankMode();
        }
    }

    @Override
    public void showRankList(List<RankInfoModel> rankInfoModel, boolean hasMore) {
        mRefreshLayout.setEnableLoadMore(hasMore);
        mHasMore = hasMore;
        mRefreshLayout.finishLoadMore();
        mLeaderBoardAdapter.setDataList(rankInfoModel);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(NetworkUtils.NetworkChangeEvent event) {
        if (event.type != -1) {
            if (mLeaderBoardAdapter.getDataList() == null && mLeaderBoardAdapter.getDataList().size() == 0) {
                setRankMode();
            }
        }
    }

    @Override
    public void showOwnRankInfo(UserRankModel userRankModel) {
        ExTextView tvRank = (ExTextView) mOwnInfoItem.findViewById(R.id.tv_rank);
        tvRank.setTextColor(U.getColor(R.color.white_trans_70));
        SimpleDraweeView sdvIcon = (SimpleDraweeView) mOwnInfoItem.findViewById(R.id.sdv_icon);
        ExTextView tvName = (ExTextView) mOwnInfoItem.findViewById(R.id.tv_name);
        ExTextView tvSegment = (ExTextView) mOwnInfoItem.findViewById(R.id.tv_segment);
        NormalLevelView2 normalLevelView = (NormalLevelView2) mOwnInfoItem.findViewById(R.id.level_view);

        if (userRankModel.getRankSeq() == 0) {
            tvRank.setVisibility(View.GONE);
        } else {
            tvRank.setVisibility(View.VISIBLE);
            tvRank.setText(formatRank(userRankModel.getRankSeq()));
        }
        normalLevelView.bindData(userRankModel.getMainRanking(), userRankModel.getSubRanking());

        tvName.setText(MyUserInfoManager.getInstance().getNickName());
        tvSegment.setText(userRankModel.getLevelDesc());
        AvatarUtils.loadAvatarByUrl(sdvIcon,
                AvatarUtils.newParamsBuilder(MyUserInfoManager.getInstance().getAvatar())
                        .setCircle(true)
                        .setBorderWidth(U.getDisplayUtils().dip2px(2))
                        .setBorderColorBySex(MyUserInfoManager.getInstance().getSex() == ESex.SX_MALE.getValue())
                        .build());
    }

    private String formatRank(int rankSeq) {
        if (rankSeq < 10000) {
            return String.valueOf(rankSeq);
        } else {
            float result = (float) (Math.round(((float) rankSeq / 10000) * 10)) / 10;
            return String.valueOf(result) + "w";
        }
    }


    @Override
    public void showFirstThreeRankInfo(List<RankInfoModel> rankInfoModelList) {
        for (int i = 0; i < 3; i++) {
            if (rankInfoModelList.size() > i) {
                setTopThreeInfo(rankInfoModelList.get(i));
                continue;
            }

            setEmptyTopInfo(i);
        }
    }

    public void setEmptyTopInfo(int seq) {
        switch (seq) {
            case 1:
                mTvRightChanpainName.setText("虚位以待");
                mSdvRightChampainIcon.setImageResource(R.drawable.zanwu_dierming);
                mRightChanpainLevelView.setVisibility(View.GONE);
                mRightChanpainLevelTv.setText("");
                mSdvRightChampainIcon.setOnClickListener(null);
                break;
            case 2:
                mTvLeftChanpainName.setText("虚位以待");
                mSdvLeftChampainIcon.setImageResource(R.drawable.zanwu_disanming);
                mLeftChanpainLevelView.setVisibility(View.GONE);
                mLeftChanpainLevelTv.setText("");
                mSdvLeftChampainIcon.setOnClickListener(null);
                break;
        }
    }

    @Override
    public void noNetWork() {
        U.getToastUtil().showShort("网络异常");
        mRefreshLayout.finishLoadMore();
    }

    private void setTopThreeInfo(RankInfoModel rankInfoModel) {
        if (rankInfoModel.getRankSeq() == 1) {
            AvatarUtils.loadAvatarByUrl(mSdvChampainIcon,
                    AvatarUtils.newParamsBuilder(rankInfoModel.getAvatar())
                            .setCircle(true)
                            .setBorderWidth(U.getDisplayUtils().dip2px(3))
                            .setBorderColor(0xFFFFD958)
                            .build());

            RxView.clicks(mSdvChampainIcon).subscribe(new Consumer<Object>() {
                @Override
                public void accept(Object o) {
                    gotoPersonFragment(rankInfoModel.getUserID());
                }
            });
            mTvChanpainName.setText(rankInfoModel.getNickname());
            mChanpainLevelView.bindData(rankInfoModel.getMainRanking(), rankInfoModel.getSubRanking());
            mChanpainLevelTv.setText(rankInfoModel.getLevelDesc());
        } else if (rankInfoModel.getRankSeq() == 2) {
            AvatarUtils.loadAvatarByUrl(mSdvRightChampainIcon,
                    AvatarUtils.newParamsBuilder(rankInfoModel.getAvatar())
                            .setCircle(true)
                            .setBorderWidth(U.getDisplayUtils().dip2px(3))
                            .setBorderColor(0xFFA2C9DA)
                            .build());

            RxView.clicks(mSdvRightChampainIcon).subscribe(new Consumer<Object>() {
                @Override
                public void accept(Object o) {
                    gotoPersonFragment(rankInfoModel.getUserID());
                }
            });
            mTvRightChanpainName.setText(rankInfoModel.getNickname());
            mRightChanpainLevelView.setVisibility(View.VISIBLE);
            mRightChanpainLevelView.bindData(rankInfoModel.getMainRanking(), rankInfoModel.getSubRanking());
            mRightChanpainLevelTv.setText(rankInfoModel.getLevelDesc());
        } else if (rankInfoModel.getRankSeq() == 3) {
            AvatarUtils.loadAvatarByUrl(mSdvLeftChampainIcon,
                    AvatarUtils.newParamsBuilder(rankInfoModel.getAvatar())
                            .setCircle(true)
                            .setBorderWidth(U.getDisplayUtils().dip2px(3))
                            .setBorderColor(0xFFEEB874)
                            .build());

            RxView.clicks(mSdvLeftChampainIcon).subscribe(new Consumer<Object>() {
                @Override
                public void accept(Object o) {
                    gotoPersonFragment(rankInfoModel.getUserID());
                }
            });
            mTvLeftChanpainName.setText(rankInfoModel.getNickname());
            mLeftChanpainLevelView.setVisibility(View.VISIBLE);
            mLeftChanpainLevelView.bindData(rankInfoModel.getMainRanking(), rankInfoModel.getSubRanking());
            mLeftChanpainLevelTv.setText(rankInfoModel.getLevelDesc());
        }
    }

    public void gotoPersonFragment(int uid) {
        Bundle bundle = new Bundle();
        bundle.putSerializable(OtherPersonFragment.BUNDLE_USER_ID, uid);
        U.getFragmentUtils().addFragment(FragmentUtils
                .newAddParamsBuilder((FragmentActivity) getActivity(), OtherPersonFragment.class)
                .setUseOldFragmentIfExist(false)
                .setBundle(bundle)
                .setAddToBackStack(true)
                .setHasAnimation(true)
                .build());
    }

    @Override
    public void destroy() {
        super.destroy();
        mPopupWindow.dismiss();
        U.getSoundUtils().release(TAG);
    }

    @Override
    public boolean useEventBus() {
        return true;
    }
}
