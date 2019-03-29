package com.module.home.game;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.alibaba.android.arouter.launcher.ARouter;
import com.common.base.BaseFragment;
import com.common.core.account.event.AccountEvent;
import com.common.core.myinfo.event.MyUserInfoEvent;
import com.common.core.permission.SkrAudioPermission;
import com.common.core.userinfo.model.UserRankModel;
import com.common.log.MyLog;
import com.common.utils.FragmentUtils;
import com.common.utils.U;
import com.component.busilib.friends.RecommendModel;
import com.component.busilib.friends.GrabFriendsRoomFragment;
import com.component.busilib.friends.SpecialModel;
import com.module.RouterConstants;
import com.module.home.R;
import com.module.home.game.adapter.GameAdapter;
import com.module.home.game.model.BannerModel;
import com.module.home.game.model.QuickJoinRoomModel;
import com.module.home.game.model.RecommendRoomModel;
import com.module.home.model.GameKConfigModel;
import com.module.home.model.SlideShowModel;
import com.module.home.widget.UserInfoTileView2;
import com.module.rank.IRankingModeService;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

public class GameFragment2 extends BaseFragment implements IGameView {
    public final static String TAG = "Game2Fragment";

    UserInfoTileView2 mUserInfoTitle;
    RecyclerView mRecyclerView;

    GameAdapter mGameAdapter;

    GamePresenter mGamePresenter;

    SkrAudioPermission mSkrAudioPermission;

    int mRecommendInterval = 0;

    @Override
    public int initView() {
        return R.layout.game2_fragment_layout;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        mUserInfoTitle = (UserInfoTileView2) mRootView.findViewById(R.id.user_info_title);
        mRecyclerView = (RecyclerView) mRootView.findViewById(R.id.recycler_view);
        mSkrAudioPermission = new SkrAudioPermission();

        mGameAdapter = new GameAdapter(getContext(), new GameAdapter.GameAdapterListener() {
            @Override
            public void createRoom() {
                MyLog.d(TAG, "createRoom");
                // TODO: 2019/3/29 创建房间
                mSkrAudioPermission.ensurePermission(new Runnable() {
                    @Override
                    public void run() {
                        IRankingModeService iRankingModeService = (IRankingModeService) ARouter.getInstance().build(RouterConstants.SERVICE_RANKINGMODE).navigation();
                        if (iRankingModeService != null) {
                            iRankingModeService.tryGoCreateRoom();
                        }
                    }
                }, true);
            }

            @Override
            public void selectSpecial(SpecialModel specialModel) {
                MyLog.d(TAG, "selectSpecial" + " specialModel=" + specialModel);
                // TODO: 2019/3/29 选择专场，进入快速匹配
                if (specialModel != null) {
                    mSkrAudioPermission.ensurePermission(new Runnable() {
                        @Override
                        public void run() {
                            IRankingModeService iRankingModeService = (IRankingModeService) ARouter.getInstance().build(RouterConstants.SERVICE_RANKINGMODE).navigation();
                            if (iRankingModeService != null) {
                                iRankingModeService.tryGoGrabMatch(specialModel.getTagID());
                            }
                        }
                    }, true);
                } else {

                }
            }

            @Override
            public void enterRoom(RecommendModel friendRoomModel) {
                MyLog.d(TAG, "enterRoom" + " friendRoomModel=" + friendRoomModel);
                if (friendRoomModel != null) {
                    mSkrAudioPermission.ensurePermission(new Runnable() {
                        @Override
                        public void run() {
                            IRankingModeService iRankingModeService = (IRankingModeService) ARouter.getInstance().build(RouterConstants.SERVICE_RANKINGMODE).navigation();
                            if (iRankingModeService != null) {
                                iRankingModeService.tryGoGrabRoom(friendRoomModel.getRoomInfo().getRoomID());
                            }
                        }
                    }, true);
                } else {

                }
            }

            @Override
            public void moreRoom() {
                MyLog.d(TAG, "moreRoom");
                U.getFragmentUtils().addFragment(FragmentUtils.newAddParamsBuilder(getActivity(), GrabFriendsRoomFragment.class)
                        .setAddToBackStack(true)
                        .setHasAnimation(true)
                        .build());

            }
        });
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        mRecyclerView.setAdapter(mGameAdapter);

        mGamePresenter = new GamePresenter(this);
        addPresent(mGamePresenter);

        mGamePresenter.initOperationArea(true);
        mGamePresenter.initQuickRoom(true);
        mGamePresenter.initRankInfo(true);
        mGamePresenter.initRecommendRoom(true, mRecommendInterval);
        mGamePresenter.initGameKConfig();
    }

    @Override
    public boolean useEventBus() {
        return true;
    }


    @Override
    protected void onFragmentVisible() {
        super.onFragmentVisible();
        mGamePresenter.initOperationArea(false);
        mGamePresenter.initQuickRoom(false);
        mGamePresenter.initRankInfo(false);
        mGamePresenter.initRecommendRoom(false, mRecommendInterval);
        mGamePresenter.initGameKConfig();
    }

    @Override
    protected void onFragmentInvisible() {
        super.onFragmentInvisible();
        mGamePresenter.stopTimer();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(MyUserInfoEvent.UserInfoChangeEvent userInfoChangeEvent) {
        mUserInfoTitle.showBaseInfo();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(AccountEvent.SetAccountEvent event) {
        mGamePresenter.initOperationArea(true);
        mGamePresenter.initQuickRoom(true);
        mGamePresenter.initRankInfo(true);
        mGamePresenter.initRecommendRoom(true, mRecommendInterval);
    }

    public void setQuickRoom(List<SpecialModel> list, int offset) {
        if (list == null || list.size() == 0) {
            MyLog.w(TAG, "initQuickRoom 为null");
            return;
        }

        QuickJoinRoomModel quickJoinRoomModel = new QuickJoinRoomModel(list, offset);
        if (mGameAdapter.getPositionObject(2) != null && mGameAdapter.getPositionObject(2) instanceof com.module.home.game.model.RecommendRoomModel) {
            mGameAdapter.getDataList().remove(mGameAdapter.getPositionObject(2));
        }
        mGameAdapter.getDataList().add(quickJoinRoomModel);
        mGameAdapter.notifyDataSetChanged();

    }

    @Override
    public void setRankInfo(UserRankModel userRankModel) {
        if (userRankModel != null) {
            mUserInfoTitle.showRankView(userRankModel);
        } else {
            MyLog.w(TAG, "setRankInfo" + " userRankModel = null");
        }
    }

    @Override
    public void setGameConfig(GameKConfigModel gameKConfigModel) {
        mRecommendInterval = gameKConfigModel.getHomepagetickerinterval();
        mGamePresenter.initRecommendRoom(false, mRecommendInterval);
    }

    public void setRecommendInfo(List<RecommendModel> list, int offset, int totalNum) {
        if (list == null || list.size() == 0) {
            MyLog.w(TAG, "initFriendRoom 为null");
            return;
        }

        RecommendRoomModel recommendRoomModel = new RecommendRoomModel(list, offset, totalNum);
        if (mGameAdapter.getPositionObject(1) != null && mGameAdapter.getPositionObject(1) instanceof com.module.home.game.model.RecommendRoomModel) {
            mGameAdapter.getDataList().remove(mGameAdapter.getPositionObject(1));
        }
        mGameAdapter.getDataList().add(1, recommendRoomModel);
        mGameAdapter.notifyDataSetChanged();
    }


    public void setBannerImage(List<SlideShowModel> slideShowModelList) {
        if (slideShowModelList == null || slideShowModelList.size() == 0) {
            MyLog.w(TAG, "initOperationArea 为null");
            return;
        }
        BannerModel bannerModel = new BannerModel(slideShowModelList);
        if (mGameAdapter.getPositionObject(0) != null && mGameAdapter.getPositionObject(0) instanceof BannerModel) {
            mGameAdapter.getDataList().remove(mGameAdapter.getPositionObject(0));
        }
        mGameAdapter.getDataList().add(0, bannerModel);
        mGameAdapter.notifyDataSetChanged();
    }

}
