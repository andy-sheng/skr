package com.zq.person.presenter;

import com.alibaba.fastjson.JSON;
import com.common.core.userinfo.UserInfoManager;
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

import model.RelationNumMode;
import model.UserScoreModel;

public class OtherPersonPresenter extends RxLifeCyclePresenter {

    IOtherPersonView view;
    UserInfoServerApi mUserInfoServerApi;

    public OtherPersonPresenter(IOtherPersonView view) {
        this.view = view;
        this.mUserInfoServerApi = ApiManager.getInstance().createService(UserInfoServerApi.class);
    }

    public void getUserInfo(int uid) {
        UserInfoManager.getInstance().getUserInfoByUuid(uid, new UserInfoManager.ResultCallback<UserInfoModel>() {
            @Override
            public boolean onGetLocalDB(UserInfoModel model) {
                view.showUserInfo(model);
                return false;
            }

            @Override
            public boolean onGetServer(UserInfoModel model) {
                view.showUserInfo(model);
                return false;
            }
        });
    }

    public void getRelation(int uid) {
        ApiMethods.subscribe(mUserInfoServerApi.getRelation(uid), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult result) {
                if (result.getErrno() == 0) {
                    boolean isFriend = result.getData().getBoolean("isFriend");
                    boolean isFollow = result.getData().getBoolean("isFollow");
                    view.showUserRelation(isFriend, isFollow);
                }
            }
        }, this);

    }

    public void getHomePage(int userID) {
        ApiMethods.subscribe(mUserInfoServerApi.getHomePage(userID), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult result) {
                if (result.getErrno() == 0) {
                    UserInfoModel userInfoModel = JSON.parseObject(result.getData().getString("userBaseInfo"), UserInfoModel.class);
                    List<UserRankModel> userRankModels = JSON.parseArray(result.getData().getJSONObject("userRankInfo").getString("seqInfo"), UserRankModel.class);
                    List<RelationNumMode> relationNumModes = JSON.parseArray(result.getData().getJSONObject("userRelationCntInfo").getString("cnt"), RelationNumMode.class);
                    List<UserScoreModel> userScoreModels = JSON.parseArray(result.getData().getJSONObject("userScoreInfo").getString("userScore"), UserScoreModel.class);
                    boolean isFriend = result.getData().getJSONObject("userMateInfo").getBoolean("isFriend");
                    boolean isFollow = result.getData().getJSONObject("userMateInfo").getBoolean("isFollow");
                }
            }
        }, this);
    }
}
