package com.zq.person.presenter;

import com.alibaba.fastjson.JSON;
import com.common.core.userinfo.UserInfoManager;
import com.common.core.userinfo.UserInfoModel;
import com.common.core.userinfo.UserInfoServerApi;
import com.common.mvp.RxLifeCyclePresenter;
import com.common.rxretrofit.ApiManager;
import com.common.rxretrofit.ApiMethods;
import com.common.rxretrofit.ApiObserver;
import com.common.rxretrofit.ApiResult;
import com.zq.person.view.IOtherPersonView;

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
}
