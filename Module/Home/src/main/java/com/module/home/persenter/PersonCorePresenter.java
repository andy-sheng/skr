package com.module.home.persenter;

import com.alibaba.fastjson.JSON;
import com.common.core.userinfo.UserInfoServerApi;
import com.common.core.userinfo.model.GameStatisModel;
import com.common.core.userinfo.model.UserInfoModel;
import com.common.core.userinfo.model.UserRankModel;
import com.common.log.MyLog;
import com.common.mvp.RxLifeCyclePresenter;
import com.common.rxretrofit.ApiManager;
import com.common.rxretrofit.ApiMethods;
import com.common.rxretrofit.ApiObserver;
import com.common.rxretrofit.ApiResult;

import model.RelationNumModel;

import com.module.home.view.IPersonView;

import java.util.List;

import com.common.core.userinfo.model.UserLevelModel;

public class PersonCorePresenter extends RxLifeCyclePresenter {

    UserInfoServerApi userInfoServerApi;
    IPersonView view;
    long lastUpdateTime = 0;

    public PersonCorePresenter(IPersonView view) {
        this.view = view;
        userInfoServerApi = ApiManager.getInstance().createService(UserInfoServerApi.class);
    }

    public void getHomePage(int userID) {
        long now = System.currentTimeMillis();
        if ((now - lastUpdateTime) < 60 * 1000) {
            return;
        }

        lastUpdateTime = now;
        ApiMethods.subscribe(userInfoServerApi.getHomePage(userID), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult result) {
                if (result.getErrno() == 0) {
                    UserInfoModel userInfoModel = JSON.parseObject(result.getData().getString("userBaseInfo"), UserInfoModel.class);
                    List<UserRankModel> userRankModels = JSON.parseArray(result.getData().getJSONObject("userRankInfo").getString("seqInfo"), UserRankModel.class);
                    List<RelationNumModel> relationNumModes = JSON.parseArray(result.getData().getJSONObject("userRelationCntInfo").getString("cnt"), RelationNumModel.class);
                    List<UserLevelModel> userLevelModels = JSON.parseArray(result.getData().getJSONObject("userScoreInfo").getString("userScore"), UserLevelModel.class);
                    List<GameStatisModel> userGameStatisModels = JSON.parseArray(result.getData().getJSONObject("userGameStatisticsInfo").getString("statistic"), GameStatisModel.class);
//                    boolean isFriend = result.getData().getJSONObject("userMateInfo").getBoolean("isFriend");
//                    boolean isFollow = result.getData().getJSONObject("userMateInfo").getBoolean("isFollow");

                    view.showUserInfo(userInfoModel);
                    view.showReginRank(userRankModels);
                    view.showRelationNum(relationNumModes);
                    view.showUserLevel(userLevelModels);
                    view.showGameStatic(userGameStatisModels);
                }
            }
        }, this);
    }
}
