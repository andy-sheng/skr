package com.module.home.game;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.common.base.BaseFragment;
import com.common.core.account.event.AccountEvent;
import com.common.core.myinfo.event.MyUserInfoEvent;
import com.common.core.userinfo.model.UserLevelModel;
import com.common.core.userinfo.model.UserRankModel;
import com.common.log.MyLog;
import com.component.busilib.friends.FriendRoomModel;
import com.component.busilib.friends.SpecialModel;
import com.module.home.R;
import com.module.home.game.adapter.GameAdapter;
import com.module.home.game.model.BannerModel;
import com.module.home.game.model.QuickJoinRoomModel;
import com.module.home.game.model.RecommendRoomModel;
import com.module.home.model.SlideShowModel;
import com.module.home.widget.UserInfoTileView2;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

public class Game2Fragment extends BaseFragment implements IGameView {

    public final static String TAG = "Game2Fragment";

    UserInfoTileView2 mUserInfoTitle;
    RecyclerView mRecyclerView;

    GameAdapter mGameAdapter;

    GamePresenter mGamePresenter;

    @Override
    public int initView() {
        return R.layout.game2_fragment_layout;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        mUserInfoTitle = (UserInfoTileView2) mRootView.findViewById(R.id.user_info_title);
        mRecyclerView = (RecyclerView) mRootView.findViewById(R.id.recycler_view);

        mGameAdapter = new GameAdapter(getContext(), new GameAdapter.GameAdapterListener() {
            @Override
            public void createRoom() {
                MyLog.d(TAG, "createRoom");
                // TODO: 2019/3/29 创建房间
            }

            @Override
            public void selectSpecial(SpecialModel specialModel) {
                MyLog.d(TAG, "selectSpecial" + " specialModel=" + specialModel);
                // TODO: 2019/3/29 选择专场，进入快速匹配
            }

            @Override
            public void enterRoom(FriendRoomModel friendRoomModel) {
                MyLog.d(TAG, "enterRoom" + " friendRoomModel=" + friendRoomModel);
                // TODO: 2019/3/29 加入好友房
//                if (model != null) {
//                    mSkrAudioPermission.ensurePermission(new Runnable() {
//                        @Override
//                        public void run() {
//                            FriendRoomModel model1 = (FriendRoomModel) model;
//                            GrabRoomServerApi roomServerApi = ApiManager.getInstance().createService(GrabRoomServerApi.class);
//                            HashMap<String, Object> map = new HashMap<>();
//                            map.put("roomID", model1.getRoomInfo().getRoomID());
//                            RequestBody body = RequestBody.create(MediaType.parse(APPLICATION_JSON), JSON.toJSONString(map));
//                            ApiMethods.subscribe(roomServerApi.joinGrabRoom(body), new ApiObserver<ApiResult>() {
//                                @Override
//                                public void process(ApiResult result) {
//                                    if (result.getErrno() == 0) {
//                                        JoinGrabRoomRspModel grabCurGameStateModel = JSON.parseObject(result.getData().toString(), JoinGrabRoomRspModel.class);
//                                        //先跳转
//                                        ARouter.getInstance().build(RouterConstants.ACTIVITY_GRAB_ROOM)
//                                                .withSerializable("prepare_data", grabCurGameStateModel)
//                                                .navigation();
//                                        Activity activity = getActivity();
//                                        if (activity != null) {
//                                            activity.finish();
//                                        }
//                                    } else {
//                                        U.getToastUtil().showShort(result.getErrmsg());
//                                    }
//                                }
//
//                                @Override
//                                public void onNetworkError(ErrorType errorType) {
//                                    super.onNetworkError(errorType);
//                                }
//                            });
//                        }
//                    }, true);
//                }
            }

            @Override
            public void moreRoom() {
                MyLog.d(TAG, "moreRoom");
                // TODO: 2019/3/29 更多房间 

            }
        });
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        mRecyclerView.setAdapter(mGameAdapter);

        mGamePresenter = new GamePresenter(this);
        addPresent(mGamePresenter);

        mGamePresenter.initOperationArea(true);
        mGamePresenter.initFriendRoom(true);
        mGamePresenter.initQuickRoom(true);
        mGamePresenter.initRankInfo(true);
        mGamePresenter.initScoreDetail(true);
    }

    @Override
    public boolean useEventBus() {
        return true;
    }


    @Override
    protected void onFragmentVisible() {
        super.onFragmentVisible();
        mGamePresenter.initOperationArea(false);
        mGamePresenter.initFriendRoom(false);
        mGamePresenter.initQuickRoom(false);
        mGamePresenter.initRankInfo(false);
        mGamePresenter.initScoreDetail(false);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(MyUserInfoEvent.UserInfoChangeEvent userInfoChangeEvent) {
        mUserInfoTitle.showBaseInfo();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(AccountEvent.SetAccountEvent event) {
        mGamePresenter.initFriendRoom(true);
        mGamePresenter.initOperationArea(true);
        mGamePresenter.initQuickRoom(true);
    }

    public void setQuickRoom(List<SpecialModel> list, int offset) {
        if (list == null || list.size() == 0) {
            MyLog.w(TAG, "initQuickRoom 为null");
            return;
        }

        QuickJoinRoomModel quickJoinRoomModel = new QuickJoinRoomModel(list, offset);
        if (mGameAdapter.getPositionObject(2) != null && mGameAdapter.getPositionObject(2) instanceof RecommendRoomModel) {
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

        }
    }

    @Override
    public void setScoreInfo(List<UserLevelModel> userLevelModels) {
        if (userLevelModels != null) {
            int level = 0;           //当前父段位
            int subLevel = 0;        //当前子段位
            String levelDesc = "";   //父段位描述
            for (UserLevelModel userLevelModel : userLevelModels) {
                if (userLevelModel.getType() == UserLevelModel.RANKING_TYPE) {
                    level = userLevelModel.getScore();
                    levelDesc = userLevelModel.getDesc();
                } else if (userLevelModel.getType() == UserLevelModel.SUB_RANKING_TYPE) {
                    subLevel = userLevelModel.getScore();
                }
            }
            mUserInfoTitle.showScoreView(level, subLevel, levelDesc);
        } else {

        }

    }

    public void setFriendRoom(List<FriendRoomModel> list, int offset, int totalNum) {
        if (list == null || list.size() == 0) {
            MyLog.w(TAG, "initFriendRoom 为null");
            return;
        }

        RecommendRoomModel recommendRoomModel = new RecommendRoomModel(list, offset, totalNum);
        if (mGameAdapter.getPositionObject(1) != null && mGameAdapter.getPositionObject(1) instanceof RecommendRoomModel) {
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
