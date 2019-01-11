package com.module.playways.rank.room.presenter;

import com.alibaba.fastjson.JSON;
import com.common.core.myinfo.MyUserInfoManager;
import com.common.core.userinfo.UserInfoServerApi;
import com.common.core.userinfo.model.RankInfoModel;
import com.common.core.userinfo.model.UserRankModel;
import com.common.mvp.RxLifeCyclePresenter;
import com.common.rxretrofit.ApiManager;
import com.common.rxretrofit.ApiMethods;
import com.common.rxretrofit.ApiObserver;
import com.common.rxretrofit.ApiResult;
import com.module.playways.rank.room.view.ILeaderBoardView;

import java.util.ArrayList;
import java.util.List;

public class LeaderboardPresenter extends RxLifeCyclePresenter {

    public final static String TAG = "LeaderboardPresenter";

    UserInfoServerApi mUserInfoServerApi;

    private ILeaderBoardView mILeaderBoardView;

    private int mOffset = 0;

    private int mLimit = 20;

    int mRankMode = UserRankModel.COUNTRY;

    List<RankInfoModel> mRankInfoModelList;

    public LeaderboardPresenter(ILeaderBoardView ILeaderBoardView) {
        mILeaderBoardView = ILeaderBoardView;
        mRankInfoModelList = new ArrayList<>();
        this.mUserInfoServerApi = ApiManager.getInstance().createService(UserInfoServerApi.class);
    }

    public void setRankMode(int rankMode){
        mOffset = 0;
        mRankMode = rankMode;
        mILeaderBoardView.clear();
        mRankInfoModelList.clear();

        getLeaderBoardInfo();
        getOwnInfo();
    }

    public void getLeaderBoardInfo() {
        ApiMethods.subscribe(mUserInfoServerApi.getReginRankList(mRankMode, mOffset, mLimit), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult result) {
                if (result.getErrno() == 0) {
                    List<RankInfoModel> rankInfoModelList = JSON.parseArray(result.getData().getString("users"), RankInfoModel.class);
                    if(rankInfoModelList != null && rankInfoModelList.size() > 0){
                        if(mOffset == 0){
                            List<RankInfoModel> threeInfo = null;
                            threeInfo = new ArrayList<>(3);
                            for (int i = 0; i < (rankInfoModelList.size() >= 3 ? 3 : rankInfoModelList.size()); i++){
                                threeInfo.add(rankInfoModelList.get(i));
                            }

                            mILeaderBoardView.showFirstThreeRankInfo(threeInfo);
                        }

                        mRankInfoModelList.addAll(rankInfoModelList);
                        mILeaderBoardView.showRankList(mRankInfoModelList, rankInfoModelList.size() == mLimit);
                        mOffset += rankInfoModelList.size();
                    }else {
                        mILeaderBoardView.showRankList(new ArrayList<>(), false);
                        mILeaderBoardView.showFirstThreeRankInfo(new ArrayList<>());
                    }

                }
            }
        });
    }

    public void getOwnInfo(){
        ApiMethods.subscribe(mUserInfoServerApi.getReginRank((int) MyUserInfoManager.getInstance().getUid()), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult result) {
                if (result.getErrno() == 0) {
                    List<UserRankModel> userRankModels = JSON.parseArray(result.getData().getString("seqInfo"), UserRankModel.class);
                    for (UserRankModel userRankModel :
                            userRankModels) {
                        if (mRankMode == userRankModel.getCategory()){
                            mILeaderBoardView.showOwnRankInfo(userRankModel);
                        }
                    }
                }
            }
        });
    }
}
