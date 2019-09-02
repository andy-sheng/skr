package com.module.home.persenter;

import com.alibaba.fastjson.JSON;
import com.common.core.myinfo.event.MyUserInfoEvent;
import com.common.core.userinfo.UserInfoServerApi;
import com.component.person.model.ScoreDetailModel;
import com.component.person.model.UserRankModel;
import com.common.mvp.RxLifeCyclePresenter;
import com.common.rxretrofit.ApiManager;
import com.common.rxretrofit.ApiMethods;
import com.common.rxretrofit.ApiObserver;
import com.common.rxretrofit.ApiResult;
import com.common.utils.U;
import com.component.person.model.ScoreStateModel;
import com.module.home.view.IPkInfoView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class PkInfoPresenter extends RxLifeCyclePresenter {

    UserInfoServerApi userInfoServerApi;
    IPkInfoView mView;
    long mLastUpdateTime = 0;  // 主页刷新时间
    long mLastRankUpdateTime = 0;  //排名刷新时间

    public PkInfoPresenter(IPkInfoView view) {
        this.mView = view;
        userInfoServerApi = ApiManager.getInstance().createService(UserInfoServerApi.class);
        addToLifeCycle();
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }

    /**
     * @param userID
     * @param flag   是否立即更新
     */
    public void getLevelPage(long userID, boolean flag) {
        long now = System.currentTimeMillis();
        if (!flag) {
            if ((now - mLastUpdateTime) < 60 * 1000) {
                return;
            }
        }

        getLevelPage(userID);
        getRankLevel();
    }

    private void getLevelPage(long userID) {
        ApiMethods.subscribe(userInfoServerApi.getLevelDetail(userID), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult result) {
                if (result.getErrno() == 0) {
                    mLastUpdateTime = System.currentTimeMillis();
                    ScoreDetailModel scoreDetailModel = JSON.parseObject(result.getData().toJSONString(), ScoreDetailModel.class);
//                    ScoreStateModel stateModel = JSON.parseObject(result.getData().getString("ranking"), ScoreStateModel.class);
//                    long raceTicketCnt = result.getData().getLongValue("raceTicketCnt");
//                    long standLightCnt = result.getData().getLongValue("standLightCnt");
                    mView.showUserLevel(scoreDetailModel.getScoreStateModel());
                    mView.showGameStatic(scoreDetailModel.getRaceTicketCnt(), scoreDetailModel.getStandLightCnt());
                }
            }
        }, this);
    }

    private void getRankLevel() {
        ApiMethods.subscribe(userInfoServerApi.getReginDiff(), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult result) {
                if (result.getErrno() == 0) {
                    mLastRankUpdateTime = System.currentTimeMillis();
                    UserRankModel userRankModel = JSON.parseObject(result.getData().getString("diff"), UserRankModel.class);
                    mView.showRankView(userRankModel);
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

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(MyUserInfoEvent.UserInfoChangeEvent userInfoChangeEvent) {
        mView.refreshBaseInfo();
    }

    @Override
    public void destroy() {
        super.destroy();
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
    }
}
