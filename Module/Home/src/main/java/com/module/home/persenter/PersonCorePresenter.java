package com.module.home.persenter;

import com.alibaba.fastjson.JSON;
import com.common.core.myinfo.MyUserInfo;
import com.common.core.myinfo.MyUserInfoLocalApi;
import com.common.core.myinfo.MyUserInfoManager;
import com.common.core.userinfo.UserInfoServerApi;
import com.common.core.userinfo.model.UserInfoModel;
import com.common.mvp.RxLifeCyclePresenter;
import com.common.rxretrofit.ApiManager;
import com.common.rxretrofit.ApiMethods;
import com.common.rxretrofit.ApiObserver;
import com.common.rxretrofit.ApiResult;

import com.component.person.model.RelationNumModel;

import com.module.home.view.IPersonView;

import java.util.List;

// TODO: 2019-06-19 需要优化，新的设计里面其实只有魅力值需要从服务器去取，等服务器接口优化
public class PersonCorePresenter extends RxLifeCyclePresenter {

    UserInfoServerApi mUserInfoServerApi;
    IPersonView mView;

    long mLastUpdateTime = 0;  // 主页刷新时间

    public PersonCorePresenter(IPersonView view) {
        this.mView = view;
        mUserInfoServerApi = ApiManager.getInstance().createService(UserInfoServerApi.class);
    }

    /**
     * @param flag 是否立即更新
     */
    public void getHomePage(boolean flag) {
        long now = System.currentTimeMillis();
        if (!flag) {
            if ((now - mLastUpdateTime) < 60 * 1000) {
                return;
            }
        }

        getHomePage((int) MyUserInfoManager.getInstance().getUid());
    }

    private void getHomePage(int userID) {
        ApiMethods.subscribe(mUserInfoServerApi.getHomePage(userID), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult result) {
                if (result.getErrno() == 0) {
                    mLastUpdateTime = System.currentTimeMillis();
                    UserInfoModel userInfoModel = JSON.parseObject(result.getData().getString("userBaseInfo"), UserInfoModel.class);
                    List<RelationNumModel> relationNumModes = JSON.parseArray(result.getData().getJSONObject("userRelationCntInfo").getString("cnt"), RelationNumModel.class);

                    MyUserInfo myUserInfo = MyUserInfo.parseFromUserInfoModel(userInfoModel);
                    MyUserInfoLocalApi.insertOrUpdate(myUserInfo);
                    MyUserInfoManager.getInstance().setMyUserInfo(myUserInfo, true, "getHomePage");

                    int meiLiCntTotal = result.getData().getIntValue("meiLiCntTotal");

                    mView.showHomePageInfo(relationNumModes, meiLiCntTotal);
                } else {
                    mView.loadHomePageFailed();
                }
            }

            @Override
            public void onNetworkError(ErrorType errorType) {
                super.onNetworkError(errorType);
                mView.loadHomePageFailed();
            }
        }, this);
    }

//    public void getRelationNums() {
//        ApiMethods.subscribe(mUserInfoServerApi.getRelationNum((int) MyUserInfoManager.getInstance().getUid()), new ApiObserver<ApiResult>() {
//            @Override
//            public void process(ApiResult result) {
//                if (result.getErrno() == 0) {
//                    List<RelationNumModel> relationNumModels = JSON.parseArray(result.getData().getString("cnt"), RelationNumModel.class);
//                    mView.showRelationNum(relationNumModels);
//                }
//            }
//        }, this);
//
//    }

    @Override
    public void destroy() {
        super.destroy();
    }
}
