package com.module.playways.rank.room.fragment;

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
import com.common.core.userinfo.model.RankInfoModel;
import com.common.core.userinfo.model.UserInfoModel;
import com.common.core.userinfo.model.UserRankModel;
import com.common.log.MyLog;
import com.common.utils.FragmentUtils;
import com.common.utils.NetworkUtils;
import com.common.utils.PermissionUtils;
import com.common.utils.U;
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
import com.zq.level.view.NormalLevelView;
import com.zq.live.proto.Common.ESex;
import com.zq.person.fragment.OtherPersonFragment;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Predicate;

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
    NormalLevelView mRightChanpainLevelView;

    SimpleDraweeView mSdvLeftChampainIcon;
    ExTextView mTvLeftChanpainName;
    NormalLevelView mLeftChanpainLevelView;

    SimpleDraweeView mSdvChampainIcon;
    ExTextView mTvChanpainName;
    NormalLevelView mChanpainLevelView;

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

        mSdvRightChampainIcon = (SimpleDraweeView) mRootView.findViewById(R.id.sdv_right_champain_icon);
        mTvRightChanpainName = (ExTextView) mRootView.findViewById(R.id.tv_right_chanpain_name);
        mRightChanpainLevelView = (NormalLevelView) mRootView.findViewById(R.id.right_chanpain_level_view);

        mSdvLeftChampainIcon = (SimpleDraweeView) mRootView.findViewById(R.id.sdv_left_champain_icon);
        mTvLeftChanpainName = (ExTextView) mRootView.findViewById(R.id.tv_left_chanpain_name);
        mLeftChanpainLevelView = (NormalLevelView) mRootView.findViewById(R.id.left_chanpain_level_view);

        mSdvChampainIcon = (SimpleDraweeView) mRootView.findViewById(R.id.sdv_champain_icon);
        mTvChanpainName = (ExTextView) mRootView.findViewById(R.id.tv_chanpain_name);
        mChanpainLevelView = (NormalLevelView) mRootView.findViewById(R.id.chanpain_level_view);

        mIvRankLeft = (ImageView) mRootView.findViewById(R.id.iv_rank_left);
        mIvRank = (ImageView) mRootView.findViewById(R.id.iv_rank);
        mIvRankRight = (ImageView) mRootView.findViewById(R.id.iv_rank_right);
        mRefreshLayout = mRootView.findViewById(R.id.refreshLayout);
        mTvArea = (ExTextView) mRootView.findViewById(R.id.tv_area);
        mIvBack = (ExImageView) mRootView.findViewById(R.id.iv_back);

        mLlAreaContainer = (LinearLayout) getActivity().getLayoutInflater().inflate(R.layout.area_select_popup_window_layout, null);
        mTvOtherArea = (ExTextView) mLlAreaContainer.findViewById(R.id.tv_other_area);

        U.getSoundUtils().preLoad(TAG, R.raw.general_back);

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

        RxView.clicks(mOwnInfoItem).subscribe(new Consumer<Object>() {
            @Override
            public void accept(Object o) {
                gotoPersonFragment((int) MyUserInfoManager.getInstance().getUid(), MyUserInfoManager.getInstance().getNickName(), MyUserInfoManager.getInstance().getAvatar());
            }
        });

        RxView.clicks(mTvArea)
                .throttleFirst(300, TimeUnit.MILLISECONDS)
                .subscribe(new Consumer<Object>() {
                    @Override
                    public void accept(Object o) {
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


        RxView.clicks(mTvOtherArea)
                .throttleFirst(300, TimeUnit.MILLISECONDS)
                .subscribe(new Consumer<Object>() {
                    @Override
                    public void accept(Object o) {
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
                            mTvArea.setText(getAreaFromLocation(MyUserInfoManager.getInstance().getLocation()));
                            mLeaderboardPresenter.setRankMode(UserRankModel.REGION);
                            mRankMode = UserRankModel.REGION;
                        }
                    }
                });

        RxView.clicks(mIvBack)
                .throttleFirst(300, TimeUnit.MILLISECONDS)
                .subscribe(new Consumer<Object>() {
                    @Override
                    public void accept(Object o) {
                        U.getSoundUtils().play(TAG, R.raw.general_back, 500);
                        finish();
                    }
                });

        setRankMode();
        if (!MyUserInfoManager.getInstance().hasLocation()) {
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
            mTvArea.setCompoundDrawables(null, null, null, null);
            mTvArea.setText("全国榜");
            mTvOtherArea.setText("地域榜");
        }
    }

    private String getAreaFromLocation(Location location) {
        if (!TextUtils.isEmpty(location.getDistrict())) {
            return location.getDistrict();
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
        ExTextView tvRank = (ExTextView) mRootView.findViewById(R.id.tv_rank);
        SimpleDraweeView sdvIcon = (SimpleDraweeView) mRootView.findViewById(R.id.sdv_icon);
        ExTextView tvName = (ExTextView) mRootView.findViewById(R.id.tv_name);
        ExTextView tvSegment = (ExTextView) mRootView.findViewById(R.id.tv_segment);
        NormalLevelView normalLevelView = (NormalLevelView) mRootView.findViewById(R.id.level_view);

        if (userRankModel.getRankSeq() == 0) {
            tvRank.setVisibility(View.GONE);
        } else {
            tvRank.setText(formatRank(userRankModel.getRankSeq()));
        }
        normalLevelView.bindData(userRankModel.getMainRanking(), userRankModel.getSubRanking(), userRankModel.getMaxStar(), userRankModel.getStarCnt());

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
                mSdvRightChampainIcon.setBackground(U.getDrawable(R.drawable.zanwu_dierming));
                mRightChanpainLevelView.setVisibility(View.GONE);
                break;
            case 2:
                mTvLeftChanpainName.setText("虚位以待");
                mSdvLeftChampainIcon.setBackground(U.getDrawable(R.drawable.zanwu_disanming));
                mLeftChanpainLevelView.setVisibility(View.GONE);
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
                    gotoPersonFragment(rankInfoModel.getUserID(), rankInfoModel.getNickname(), rankInfoModel.getAvatar());
                }
            });
            mTvChanpainName.setText(rankInfoModel.getNickname());
            mChanpainLevelView.bindData(rankInfoModel.getMainRanking(), rankInfoModel.getSubRanking(), rankInfoModel.getMaxStar(), rankInfoModel.getStarCnt());
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
                    gotoPersonFragment(rankInfoModel.getUserID(), rankInfoModel.getNickname(), rankInfoModel.getAvatar());
                }
            });
            mTvRightChanpainName.setText(rankInfoModel.getNickname());
            mRightChanpainLevelView.setVisibility(View.VISIBLE);
            mRightChanpainLevelView.bindData(rankInfoModel.getMainRanking(), rankInfoModel.getSubRanking(), rankInfoModel.getMaxStar(), rankInfoModel.getStarCnt());
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
                    gotoPersonFragment(rankInfoModel.getUserID(), rankInfoModel.getNickname(), rankInfoModel.getAvatar());
                }
            });
            mTvLeftChanpainName.setText(rankInfoModel.getNickname());
            mLeftChanpainLevelView.setVisibility(View.VISIBLE);
            mLeftChanpainLevelView.bindData(rankInfoModel.getMainRanking(), rankInfoModel.getSubRanking(), rankInfoModel.getMaxStar(), rankInfoModel.getStarCnt());
        }
    }

    public void gotoPersonFragment(int uid, String nickName, String avatar) {
        UserInfoModel userInfoModel = new UserInfoModel();
        userInfoModel.setUserId(uid);
        userInfoModel.setNickname(nickName);
        Bundle bundle = new Bundle();
        bundle.putSerializable(OtherPersonFragment.BUNDLE_USER_MODEL, userInfoModel);
        U.getFragmentUtils().addFragment(FragmentUtils
                .newAddParamsBuilder((FragmentActivity) getActivity(), OtherPersonFragment.class)
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
