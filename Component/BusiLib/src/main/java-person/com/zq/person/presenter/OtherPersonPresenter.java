package com.zq.person.presenter;

import com.alibaba.fastjson.JSON;
import com.common.core.userinfo.UserInfoManager;
import com.common.core.userinfo.model.GameStatisModel;
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

import com.common.core.userinfo.model.UserLevelModel;

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
                    List<GameStatisModel> userGameStatisModels = JSON.parseArray(result.getData().getJSONObject("userGameStatisticsInfo").getString("statistic"), GameStatisModel.class);

                    boolean isFriend = result.getData().getJSONObject("userMateInfo").getBooleanValue("isFriend");
                    boolean isFollow = result.getData().getJSONObject("userMateInfo").getBooleanValue("isFollow");
                    boolean isBlacked = result.getData().getJSONObject("userMateInfo").getBooleanValue("isBlacked");

                    if (isFollow) {
                        userInfoModel.setFriend(isFriend);
                        userInfoModel.setFollow(isFollow);
                        UserInfoManager.getInstance().insertUpdateDBAndCache(userInfoModel);
                    }

                    int meiLiCntTotal = result.getData().getIntValue("meiLiCntTotal");

                    view.showHomePageInfo(userInfoModel, relationNumModes, userRankModels, userLevelModels, userGameStatisModels, isFriend, isFollow, isBlacked, meiLiCntTotal);
                } else {
                    view.getHomePageFail();
                }
            }

            @Override
            public void onNetworkError(ErrorType errorType) {
                super.onNetworkError(errorType);
                view.getHomePageFail();
            }
        }, this);
    }
}
