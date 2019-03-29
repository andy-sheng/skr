package com.module.home.game;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.alibaba.android.arouter.launcher.ARouter;
import com.alibaba.fastjson.JSON;
import com.common.base.BaseFragment;
import com.common.core.account.event.AccountEvent;
import com.common.core.avatar.AvatarUtils;
import com.common.core.myinfo.event.MyUserInfoEvent;
import com.common.core.permission.SkrAudioPermission;
import com.common.core.userinfo.model.UserLevelModel;
import com.common.core.userinfo.model.UserRankModel;
import com.common.image.fresco.BaseImageView;
import com.common.log.MyLog;
import com.common.rxretrofit.ApiManager;
import com.common.rxretrofit.ApiMethods;
import com.common.rxretrofit.ApiObserver;
import com.common.rxretrofit.ApiResult;
import com.common.utils.FragmentUtils;
import com.common.utils.U;
import com.common.view.DebounceViewClickListener;
import com.component.busilib.friends.RecommendModel;
import com.component.busilib.friends.GrabFriendsRoomFragment;
import com.component.busilib.friends.SpecialModel;
import com.module.RouterConstants;
import com.module.home.MainPageSlideApi;
import com.module.home.R;
import com.module.home.game.adapter.GameAdapter;
import com.module.home.game.model.BannerModel;
import com.module.home.game.model.QuickJoinRoomModel;
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
    BaseImageView mIvOpFirst;

    GameAdapter mGameAdapter;

    GamePresenter mGamePresenter;

    SkrAudioPermission mSkrAudioPermission;

    @Override
    public int initView() {
        return R.layout.game2_fragment_layout;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        mUserInfoTitle = (UserInfoTileView2) mRootView.findViewById(R.id.user_info_title);
        mRecyclerView = (RecyclerView) mRootView.findViewById(R.id.recycler_view);
        mIvOpFirst = (BaseImageView) mRootView.findViewById(R.id.iv_op_first);
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
        mGamePresenter.initRecommendRoom(true);
    }

    @Override
    public void showOp(GameKConfigModel gameKConfigModel) {
        if(gameKConfigModel == null){
            return;
        }

        GameKConfigModel.HomepagesitefirstBean homepagesitefirstBean = gameKConfigModel.getHomepagesitefirst();
        if (homepagesitefirstBean != null && homepagesitefirstBean.isEnable()) {
            AvatarUtils.loadAvatarByUrl(mIvOpFirst,
                    AvatarUtils.newParamsBuilder(homepagesitefirstBean.getPic())
                            .setWidth(U.getDisplayUtils().dip2px(48f))
                            .setHeight(U.getDisplayUtils().dip2px(53f))
                            .build());
            mIvOpFirst.setVisibility(View.VISIBLE);
            mIvOpFirst.setOnClickListener(new DebounceViewClickListener() {
                @Override
                public void clickValid(View v) {
                    ARouter.getInstance().build(RouterConstants.ACTIVITY_SCHEME)
                            .withString("uri", homepagesitefirstBean.getSchema())
                            .navigation();
                }
            });
        } else {
            MyLog.w(TAG, "initGameKConfig first operation area is empty");
            mIvOpFirst.setVisibility(View.GONE);
        }
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
        mGamePresenter.initRecommendRoom(false);
        mGamePresenter.getGameKConfig();
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
        mGamePresenter.initRecommendRoom(true);
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

    public void setRecommendInfo(List<RecommendModel> list, int offset, int totalNum) {
        if (list == null || list.size() == 0) {
            MyLog.w(TAG, "initFriendRoom 为null");
            return;
        }

        com.module.home.game.model.RecommendRoomModel recommendRoomModel = new com.module.home.game.model.RecommendRoomModel(list, offset, totalNum);
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

//    private void initGameKConfig() {
//        if (mIsKConfig) {
//            return;
//        }
//        mMainPageSlideApi = ApiManager.getInstance().createService(MainPageSlideApi.class);
//        ApiMethods.subscribe(mMainPageSlideApi.getKConfig(), new ApiObserver<ApiResult>() {
//            @Override
//            public void process(ApiResult result) {
//                if (result.getErrno() == 0) {
//                    mIsKConfig = true;
//
//                } else {
//
//                }
//            }
//
//            @Override
//            public void onError(Throwable e) {
//                U.getToastUtil().showShort("网络异常");
//            }
//        });
//    }

}
