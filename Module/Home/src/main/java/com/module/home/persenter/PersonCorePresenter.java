package com.module.home.persenter;

import android.os.Handler;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.common.anim.ObjectPlayControlTemplate;
import com.common.callback.Callback;
import com.common.core.myinfo.MyUserInfo;
import com.common.core.myinfo.MyUserInfoLocalApi;
import com.common.core.myinfo.MyUserInfoManager;
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

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import model.RelationNumModel;
import okhttp3.MediaType;
import okhttp3.RequestBody;

import com.common.upload.UploadCallback;
import com.common.upload.UploadParams;
import com.common.upload.UploadTask;
import com.common.utils.U;
import com.module.home.view.IPersonView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.common.core.userinfo.model.UserLevelModel;
import com.respicker.model.ImageItem;
import com.zq.person.model.PhotoModel;
import com.zq.person.photo.PhotoDataManager;
import com.zq.person.photo.PhotoLocalApi;

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
                    List<UserRankModel> userRankModels = JSON.parseArray(result.getData().getJSONObject("userRankInfo").getString("seqInfo"), UserRankModel.class);
                    List<RelationNumModel> relationNumModes = JSON.parseArray(result.getData().getJSONObject("userRelationCntInfo").getString("cnt"), RelationNumModel.class);
                    List<UserLevelModel> userLevelModels = JSON.parseArray(result.getData().getJSONObject("userScoreInfo").getString("userScore"), UserLevelModel.class);
                    List<GameStatisModel> userGameStatisModels = JSON.parseArray(result.getData().getJSONObject("userGameStatisticsInfo").getString("statistic"), GameStatisModel.class);
//                    boolean isFriend = result.getData().getJSONObject("userMateInfo").getBoolean("isFriend");
//                    boolean isFollow = result.getData().getJSONObject("userMateInfo").getBoolean("isFollow");

                    MyUserInfo myUserInfo = MyUserInfo.parseFromUserInfoModel(userInfoModel);
                    MyUserInfoLocalApi.insertOrUpdate(myUserInfo);
                    MyUserInfoManager.getInstance().setMyUserInfo(myUserInfo, true, "getHomePage");

                    int meiLiCntTotal = result.getData().getIntValue("meiLiCntTotal");

                    mView.showHomePageInfo(relationNumModes, userRankModels, userLevelModels, userGameStatisModels, meiLiCntTotal);
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
