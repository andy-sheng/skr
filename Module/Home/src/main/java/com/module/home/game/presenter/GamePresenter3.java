package com.module.home.game.presenter;

import com.alibaba.fastjson.JSON;
import com.common.log.MyLog;
import com.common.mvp.RxLifeCyclePresenter;
import com.common.rxretrofit.ApiManager;
import com.common.rxretrofit.ApiMethods;
import com.common.rxretrofit.ApiObserver;
import com.common.rxretrofit.ApiResult;
import com.common.utils.U;
import com.engine.Params;
import com.module.home.MainPageSlideApi;
import com.module.home.game.view.IGameView3;
import com.module.home.model.GameKConfigModel;

public class GamePresenter3 extends RxLifeCyclePresenter {

    MainPageSlideApi mMainPageSlideApi;
    IGameView3 mIGameView;

    boolean mIsKConfig = false;  //标记是否拉到过游戏配置信息

    public GamePresenter3(IGameView3 iGameView3) {
        this.mIGameView = iGameView3;
        mMainPageSlideApi = ApiManager.getInstance().createService(MainPageSlideApi.class);
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
                    U.getPreferenceUtils().setSettingBoolean(Params.PREF_KEY_TOKEN_ENABLE, gameKConfigModel.isAgoraTokenEnable());
                    mIGameView.setGameConfig(gameKConfigModel);

                    GameKConfigModel.HomepagesitefirstBean homepagesitefirstBean = gameKConfigModel.getHomepagesitefirst();
                    if (homepagesitefirstBean != null && homepagesitefirstBean.isEnable()) {
                        mIGameView.showRedOperationView(homepagesitefirstBean);
                    } else {
                        MyLog.w(TAG, "initGameKConfig first operation area is empty");
                        mIGameView.hideRedOperationView();
                    }
                } else {
                    mIsKConfig = false;
                    mIGameView.hideRedOperationView();
                }
            }

            @Override
            public void onError(Throwable e) {
                U.getToastUtil().showShort("网络异常");
            }
        }, this, new ApiMethods.RequestControl("getKConfig", ApiMethods.ControlType.CancelThis));
    }

}
