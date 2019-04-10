package com.module.home.persenter;

import com.alibaba.fastjson.JSON;
import com.common.core.myinfo.MyUserInfoManager;
import com.common.core.userinfo.UserInfoServerApi;
import com.common.core.userinfo.model.GameStatisModel;
import com.common.core.userinfo.model.UserInfoModel;
import com.common.core.userinfo.model.UserRankModel;
import com.common.mvp.RxLifeCyclePresenter;
import com.common.rxretrofit.ApiManager;
import com.common.rxretrofit.ApiMethods;
import com.common.rxretrofit.ApiObserver;
import com.common.rxretrofit.ApiResult;

import model.RelationNumModel;
import retrofit2.http.DELETE;

import com.common.utils.U;
import com.module.home.view.IPersonView;

import java.util.List;

import com.common.core.userinfo.model.UserLevelModel;
import com.zq.person.model.PhotoModel;

public class PersonCorePresenter extends RxLifeCyclePresenter {

    UserInfoServerApi userInfoServerApi;
    IPersonView view;
    long mLastUpdateTime = 0;  // 主页刷新时间
    int DEFUAT_CNT = 20;       // 默认拉取一页的数量

    public PersonCorePresenter(IPersonView view) {
        this.view = view;
        userInfoServerApi = ApiManager.getInstance().createService(UserInfoServerApi.class);
    }

    /**
     * @param userID
     * @param flag   是否立即更新
     */
    public void getHomePage(int userID, boolean flag) {
        long now = System.currentTimeMillis();
        if (!flag) {
            if ((now - mLastUpdateTime) < 60 * 1000) {
                return;
            }
        }

        getHomePage(userID);
    }

    private void getHomePage(int userID) {
        ApiMethods.subscribe(userInfoServerApi.getHomePage(userID), new ApiObserver<ApiResult>() {
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

                    view.showHomePageInfo(userInfoModel, relationNumModes, userRankModels, userLevelModels, userGameStatisModels);
                }
            }
        }, this);
    }

    public void getPhotos(int offset) {
        ApiMethods.subscribe(userInfoServerApi.getPhotos((int) MyUserInfoManager.getInstance().getUid(), offset, DEFUAT_CNT), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult result) {
                if (result.getErrno() == 0) {
                    if (result != null && result.getErrno() == 0) {
                        List<PhotoModel> list = JSON.parseArray(result.getData().getString("pic"), PhotoModel.class);
                        int newOffset = result.getData().getIntValue("offset");
                        int totalCount = result.getData().getIntValue("totalCount");
                        view.showPhoto(list, newOffset, totalCount);
                    }
                }
            }
        }, this);


    }
}
