package com.module.home.game;

import android.text.TextUtils;

import com.alibaba.fastjson.JSON;
import com.common.core.myinfo.MyUserInfoManager;
import com.common.core.userinfo.UserInfoServerApi;
import com.common.log.MyLog;
import com.common.mvp.RxLifeCyclePresenter;
import com.common.rxretrofit.ApiManager;
import com.common.rxretrofit.ApiMethods;
import com.common.rxretrofit.ApiObserver;
import com.common.rxretrofit.ApiResult;
import com.common.utils.HandlerTaskTimer;
import com.common.utils.U;
import com.component.busilib.friends.RecommendModel;
import com.component.busilib.friends.GrabSongApi;
import com.component.busilib.friends.SpecialModel;
import com.engine.EngineManager;
import com.module.home.MainPageSlideApi;
import com.module.home.model.GameKConfigModel;
import com.module.home.model.SlideShowModel;

import java.util.List;

import io.reactivex.disposables.Disposable;

public class GamePresenter extends RxLifeCyclePresenter {

    MainPageSlideApi mMainPageSlideApi;
    UserInfoServerApi mUserInfoServerApi;
    GrabSongApi mGrabSongApi;

    long mLastUpdateCoinNum = 0;      //金币数量上次更新成功时间
    long mLastUpdateOperaArea = 0;    //广告位上次更新成功时间
    long mLastUpdateRecomendInfo = 0; //好友派对上次更新成功时间
    long mLastUpdateQuickInfo = 0;    //快速加入房间更新成功时间

    boolean mIsKConfig = false;  //标记是否拉到过游戏配置信息

    HandlerTaskTimer mRecommendTimer;

    IGameView mIGameView;

    public GamePresenter(IGameView iGameView) {
        this.mIGameView = iGameView;

        mMainPageSlideApi = ApiManager.getInstance().createService(MainPageSlideApi.class);
        mGrabSongApi = ApiManager.getInstance().createService(GrabSongApi.class);
        mUserInfoServerApi = ApiManager.getInstance().createService(UserInfoServerApi.class);
    }

    public void initGameKConfig() {
        if (mIsKConfig) {
            return;
        }
        mMainPageSlideApi = ApiManager.getInstance().createService(MainPageSlideApi.class);
        ApiMethods.subscribe(mMainPageSlideApi.getKConfig(), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult result) {
                if (result.getErrno() == 0) {
                    mIsKConfig = true;
                    GameKConfigModel gameKConfigModel = JSON.parseObject(result.getData().getString("common"), GameKConfigModel.class);
                    U.getPreferenceUtils().setSettingBoolean(EngineManager.PREF_KEY_TOKEN_ENABLE, gameKConfigModel.isAgoraTokenEnable());
                    mIGameView.setGameConfig(gameKConfigModel);
                } else {
                    mIsKConfig = false;
                }
            }

            @Override
            public void onError(Throwable e) {
                U.getToastUtil().showShort("网络异常");
            }
        },this,new ApiMethods.RequestControl("getKConfig", ApiMethods.ControlType.CancelThis));
    }

    public void initCoinNum(boolean isFlag) {
        long now = System.currentTimeMillis();
        if (!isFlag) {
            // 距离上次拉去已经超过30秒了
            if ((now - mLastUpdateCoinNum) < 30 * 1000) {
                return;
            }
        }

        ApiMethods.subscribe(mUserInfoServerApi.getCoinNum(MyUserInfoManager.getInstance().getUid()), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult result) {
                if (result.getErrno() == 0) {
                    int coinNum = result.getData().getIntValue("coin");
                    mIGameView.setGrabCoinNum(coinNum);
                }
            }
        }, this,new ApiMethods.RequestControl("getCoinNum", ApiMethods.ControlType.CancelThis));
    }

    public void initOperationArea(boolean isFlag) {
        long now = System.currentTimeMillis();
        if (!isFlag) {
            // 距离上次拉去已经超过30秒了
            if ((now - mLastUpdateOperaArea) < 30 * 1000) {
                return;
            }
        }

        String slideshow = U.getPreferenceUtils().getSettingString("slideshow", "");
        if (!TextUtils.isEmpty(slideshow)) {
            try {
                List<SlideShowModel> slideShowModelList = JSON.parseArray(slideshow, SlideShowModel.class);
                mIGameView.setBannerImage(slideShowModelList);
            } catch (Exception e) {
                MyLog.e(TAG, e);
            }
        }

        ApiMethods.subscribe(mMainPageSlideApi.getSlideList(), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult result) {
                if (result.getErrno() == 0) {
                    mLastUpdateOperaArea = System.currentTimeMillis();
                    List<SlideShowModel> slideShowModelList = JSON.parseArray(result.getData().getString("slideshow"), SlideShowModel.class);
                    U.getPreferenceUtils().setSettingString("slideshow", result.getData().getString("slideshow"));
                    mIGameView.setBannerImage(slideShowModelList);
                }
            }

            @Override
            public void onError(Throwable e) {
                U.getToastUtil().showShort("网络异常");
            }

            @Override
            public void onNetworkError(ErrorType errorType) {
                U.getToastUtil().showShort("网络超时");
            }
        },this,new ApiMethods.RequestControl("getSlideList", ApiMethods.ControlType.CancelThis));
    }

    public void initQuickRoom(boolean isFlag) {
        long now = System.currentTimeMillis();
        if (!isFlag) {
            // 半个小时更新一次吧
            if ((now - mLastUpdateQuickInfo) < 30 * 60 * 1000) {
                return;
            }
        }

        ApiMethods.subscribe(mGrabSongApi.getSepcialList(0, 20), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult obj) {
                if (obj.getErrno() == 0) {
                    mLastUpdateQuickInfo = System.currentTimeMillis();
                    List<SpecialModel> list = JSON.parseArray(obj.getData().getString("tags"), SpecialModel.class);
                    int offset = obj.getData().getIntValue("offset");
                    mIGameView.setQuickRoom(list, offset);
                }
            }
        }, this,new ApiMethods.RequestControl("getSepcialList", ApiMethods.ControlType.CancelThis));
    }

    public void initRecommendRoom(int interval) {
        if (interval <= 0) {
            interval = 15;
        }
        stopTimer();
        mRecommendTimer = HandlerTaskTimer.newBuilder()
                .take(-1)
                .interval(interval * 1000)
                .start(new HandlerTaskTimer.ObserverW() {
                    @Override
                    public void onNext(Integer integer) {
                        loadRecommendRoomData();
                    }
                });
    }

    public void stopTimer() {
        if (mRecommendTimer != null) {
            mRecommendTimer.dispose();
        }
    }

    private void loadRecommendRoomData() {
         ApiMethods.subscribe(mGrabSongApi.getRecommendRoomList(0, 50), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult obj) {
                if (obj.getErrno() == 0) {
                    mLastUpdateRecomendInfo = System.currentTimeMillis();
                    List<RecommendModel> list = JSON.parseArray(obj.getData().getString("rooms"), RecommendModel.class);
                    int offset = obj.getData().getIntValue("offset");
                    int totalNum = obj.getData().getIntValue("totalRoomsNum");
                    mIGameView.setRecommendInfo(list, offset, totalNum);
                }
            }
        }, this,new ApiMethods.RequestControl("getRecommendRoomList", ApiMethods.ControlType.CancelThis));
    }

    @Override
    public void destroy() {
        super.destroy();
        stopTimer();
    }
}
