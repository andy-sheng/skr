package com.module.home.persenter;

import com.alibaba.fastjson.JSON;
import com.common.core.userinfo.UserInfoServerApi;
import com.common.core.userinfo.model.UserInfoModel;
import com.common.core.userinfo.model.UserRankModel;
import com.common.mvp.RxLifeCyclePresenter;
import com.common.rxretrofit.ApiManager;
import com.common.rxretrofit.ApiMethods;
import com.common.rxretrofit.ApiObserver;
import com.common.rxretrofit.ApiResult;

import model.RelationNumMode;

import com.module.home.view.IPersonView;

import java.util.List;

import model.UserScoreModel;

public class PersonCorePresenter extends RxLifeCyclePresenter {

    UserInfoServerApi userInfoServerApi;
    IPersonView view;

    public PersonCorePresenter(IPersonView view) {
        this.view = view;
        userInfoServerApi = ApiManager.getInstance().createService(UserInfoServerApi.class);
    }

    public void getHomePage(int userID) {
        ApiMethods.subscribe(userInfoServerApi.getHomePage(userID), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult result) {
                if (result.getErrno() == 0) {
                    UserInfoModel userInfoModel = JSON.parseObject(result.getData().getString("userBaseInfo"), UserInfoModel.class);
                    List<UserRankModel> userRankModels = JSON.parseArray(result.getData().getJSONObject("userRankInfo").getString("seqInfo"), UserRankModel.class);
                    List<RelationNumMode> relationNumModes = JSON.parseArray(result.getData().getJSONObject("userRelationCntInfo").getString("cnt"), RelationNumMode.class);
                    List<UserScoreModel> userScoreModels = JSON.parseArray(result.getData().getJSONObject("userScoreInfo").getString("userScore"), UserScoreModel.class);
                    boolean isFriend = result.getData().getJSONObject("userMateInfo").getBoolean("isFriend");
                    boolean isFollow = result.getData().getJSONObject("userMateInfo").getBoolean("isFollow");

                    view.showUserInfo(userInfoModel);
                    view.showReginRank(userRankModels);
                    view.showRelationNum(relationNumModes);
                    view.showUserScore(userScoreModels);
                }
            }
        }, this);
    }
}
