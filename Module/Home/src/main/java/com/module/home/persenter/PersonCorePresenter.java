package com.module.home.persenter;

import com.alibaba.fastjson.JSON;
import com.common.core.myinfo.MyUserInfoManager;
import com.common.core.userinfo.UserInfoServerApi;
import com.common.core.userinfo.model.UserRankModel;
import com.common.mvp.RxLifeCyclePresenter;
import com.common.rxretrofit.ApiManager;
import com.common.rxretrofit.ApiMethods;
import com.common.rxretrofit.ApiObserver;
import com.common.rxretrofit.ApiResult;
import com.module.home.model.RelationNumMode;
import com.module.home.view.IPersonView;

import java.util.List;

public class PersonCorePresenter extends RxLifeCyclePresenter {

    UserInfoServerApi userInfoServerApi;
    IPersonView view;

    public PersonCorePresenter(IPersonView view) {
        this.view = view;
        userInfoServerApi = ApiManager.getInstance().createService(UserInfoServerApi.class);
    }

    public void getRelationNum(int userID) {
        ApiMethods.subscribe(userInfoServerApi.getRelationNum(userID), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult result) {
                if (result.getErrno() == 0) {
                    List<RelationNumMode> list = JSON.parseArray(result.getData().getString("cnt"), RelationNumMode.class);
                    view.showRelationNum(list);
                }
            }
        }, this);
    }


    public void getReginRank(int userID) {
        ApiMethods.subscribe(userInfoServerApi.getReginRank(userID), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult result) {
                if (result.getErrno() == 0) {
                    List<UserRankModel> userRankModels = JSON.parseArray(result.getData().getString("seqInfo"), UserRankModel.class);
                    view.showReginRank(userRankModels);
                }
            }
        }, this);
    }
}
