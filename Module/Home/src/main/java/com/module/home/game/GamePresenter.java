package com.module.home.game;

import android.text.TextUtils;

import com.alibaba.fastjson.JSON;
import com.common.core.myinfo.MyUserInfoManager;
import com.common.core.userinfo.UserInfoServerApi;
import com.common.core.userinfo.model.UserLevelModel;
import com.common.core.userinfo.model.UserRankModel;
import com.common.log.MyLog;
import com.common.mvp.RxLifeCyclePresenter;
import com.common.rxretrofit.ApiManager;
import com.common.rxretrofit.ApiMethods;
import com.common.rxretrofit.ApiObserver;
import com.common.rxretrofit.ApiResult;
import com.common.utils.U;
import com.component.busilib.friends.RecommendModel;
import com.component.busilib.friends.GrabSongApi;
import com.component.busilib.friends.SpecialModel;
import com.module.home.MainPageSlideApi;
import com.module.home.model.SlideShowModel;

import java.util.List;

public class GamePresenter extends RxLifeCyclePresenter {

    MainPageSlideApi mMainPageSlideApi;
    UserInfoServerApi mUserInfoServerApi;
    GrabSongApi mGrabSongApi;

    long mLastUpdateOperaArea = 0;    //广告位上次更新成功时间
    long mLastUpdateRecommendInfo = 0;//房间推荐上次更新成功时间
    long mLastUpdateQuickInfo = 0;    //快速加入房间更新成功时间
    long mLastUpdateRankInfo = 0;     //排名信息上次更新成功时间

    IGameView mIGameView;

    public GamePresenter(IGameView iGameView) {
        this.mIGameView = iGameView;

        mMainPageSlideApi = ApiManager.getInstance().createService(MainPageSlideApi.class);
        mGrabSongApi = ApiManager.getInstance().createService(GrabSongApi.class);
        mUserInfoServerApi = ApiManager.getInstance().createService(UserInfoServerApi.class);
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
        });
    }


    public void initQuickRoom(boolean isFlag) {
        long now = System.currentTimeMillis();
        if (!isFlag) {
            // 半个小时更新一次吧
            if ((now - mLastUpdateQuickInfo) < 30 * 60 * 1000) {
                return;
            }
        }

        ApiMethods.subscribe(mGrabSongApi.getSepcialList(0, 10), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult obj) {
                if (obj.getErrno() == 0) {
                    mLastUpdateQuickInfo = System.currentTimeMillis();
                    List<SpecialModel> list = JSON.parseArray(obj.getData().getString("tags"), SpecialModel.class);
                    int offset = obj.getData().getIntValue("offset");
                    mIGameView.setQuickRoom(list, offset);
                }
            }
        }, this);
    }

    public void initRecommendRoom(boolean isFlag) {
        long now = System.currentTimeMillis();
        if (!isFlag) {
            // 距离上次拉去已经超过30秒了
            if ((now - mLastUpdateRecommendInfo) < 30 * 1000) {
                return;
            }
        }

        ApiMethods.subscribe(mGrabSongApi.getRecommendRoomList(0, 10), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult obj) {
                if (obj.getErrno() == 0) {
                    mLastUpdateRecommendInfo = System.currentTimeMillis();
                    List<RecommendModel> list = JSON.parseArray(obj.getData().getString("rooms"), RecommendModel.class);
                    int offset = obj.getData().getIntValue("offset");
                    int totalNum = obj.getData().getIntValue("totalRoomsNum");
                    mIGameView.setRecommendInfo(list, offset, totalNum);
                }
            }
        }, this);

    }

    public void initRankInfo(boolean isFlag) {
        long now = System.currentTimeMillis();
        if (!isFlag) {
            // 距离上次拉去已经超过30秒了
            if ((now - mLastUpdateRankInfo) < 30 * 1000) {
                return;
            }
        }

        ApiMethods.subscribe(mUserInfoServerApi.getReginDiff(), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult result) {
                if (result.getErrno() == 0) {
                    mLastUpdateRankInfo = System.currentTimeMillis();
                    UserRankModel userRankModel = JSON.parseObject(result.getData().getString("diff"), UserRankModel.class);
                    mIGameView.setRankInfo(userRankModel);
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
        }, this);
    }


}
