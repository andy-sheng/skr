package com.zq.person.presenter;

import com.alibaba.fastjson.JSON;
import com.common.core.userinfo.model.UserInfoModel;
import com.common.core.userinfo.UserInfoServerApi;
import com.common.core.userinfo.model.UserRankModel;
import com.common.mvp.RxLifeCyclePresenter;
import com.common.rxretrofit.ApiManager;
import com.common.rxretrofit.ApiMethods;
import com.common.rxretrofit.ApiObserver;
import com.common.rxretrofit.ApiResult;
import com.zq.person.view.IOtherPersonView;

import java.util.List;

import model.RelationNumModel;
import model.UserLevelModel;

public class OtherPersonPresenter extends RxLifeCyclePresenter {

    IOtherPersonView view;
    UserInfoServerApi mUserInfoServerApi;

    public OtherPersonPresenter(IOtherPersonView view) {
        this.view = view;
        this.mUserInfoServerApi = ApiManager.getInstance().createService(UserInfoServerApi.class);
    }

    public void getHomePage(int userID) {
        ApiMethods.subscribe(mUserInfoServerApi.getHomePage(userID), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult result) {
                if (result.getErrno() == 0) {
                    UserInfoModel userInfoModel = JSON.parseObject(result.getData().getString("userBaseInfo"), UserInfoModel.class);
                    List<UserRankModel> userRankModels = JSON.parseArray(result.getData().getJSONObject("userRankInfo").getString("seqInfo"), UserRankModel.class);
                    List<RelationNumModel> relationNumModes = JSON.parseArray(result.getData().getJSONObject("userRelationCntInfo").getString("cnt"), RelationNumModel.class);
                    List<UserLevelModel> userLevelModels = JSON.parseArray(result.getData().getJSONObject("userScoreInfo").getString("userScore"), UserLevelModel.class);
                    boolean isFriend = result.getData().getJSONObject("userMateInfo").getBoolean("isFriend");
                    boolean isFollow = result.getData().getJSONObject("userMateInfo").getBoolean("isFollow");

                    view.showUserInfo(userInfoModel);
                    view.showRelationNum(relationNumModes);
                    view.showUserLevel(userLevelModels);
                    view.showReginRank(userRankModels);
                    view.showUserRelation(isFriend, isFollow);
                }
            }
        }, this);
    }
}
