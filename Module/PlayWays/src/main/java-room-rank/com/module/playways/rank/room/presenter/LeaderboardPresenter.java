package com.module.playways.rank.room.presenter;

import com.alibaba.fastjson.JSON;
import com.common.core.userinfo.UserInfoServerApi;
import com.common.core.userinfo.model.RankInfoModel;
import com.common.core.userinfo.model.UserRankModel;
import com.common.mvp.RxLifeCyclePresenter;
import com.common.rxretrofit.ApiManager;
import com.common.rxretrofit.ApiMethods;
import com.common.rxretrofit.ApiObserver;
import com.common.rxretrofit.ApiResult;
import com.common.utils.U;
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

    public void setRankMode(int rankMode) {
        mOffset = 0;
        mRankMode = rankMode;
        mRankInfoModelList.clear();

        getLeaderBoardInfo();
        getOwnInfo(rankMode);
    }

    public void getLeaderBoardInfo() {
        if (!U.getNetworkUtils().hasNetwork()) {
            mILeaderBoardView.noNetWork();
            return;
        }

        ApiMethods.subscribe(mUserInfoServerApi.getReginRankList(mRankMode, mOffset, mLimit), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult result) {
                if (result.getErrno() == 0) {
                    List<RankInfoModel> rankInfoModelList = JSON.parseArray(result.getData().getString("users"), RankInfoModel.class);
                    if (rankInfoModelList != null && rankInfoModelList.size() > 0) {
                        int listSize = rankInfoModelList.size();

                        if (mOffset == 0) {
                            List<RankInfoModel> threeInfo = null;
                            threeInfo = new ArrayList<>(3);
                            for (int i = 0; i < (rankInfoModelList.size() >= 3 ? 3 : rankInfoModelList.size()); i++) {
                                threeInfo.add(rankInfoModelList.get(i));
                            }

                            mILeaderBoardView.showFirstThreeRankInfo(threeInfo);

                            for (RankInfoModel rankInfoModel :
                                    threeInfo) {
                                rankInfoModelList.remove(rankInfoModel);
                            }
                        }

                        mRankInfoModelList.addAll(rankInfoModelList);
                        mILeaderBoardView.showRankList(mRankInfoModelList, true);
                        mOffset += listSize;
                    } else {
                        mILeaderBoardView.showRankList(mRankInfoModelList, false);
                    }
                }
            }

            @Override
            public void onError(Throwable e) {
                super.onError(e);
                mILeaderBoardView.noNetWork();
            }
        }, this);
    }

    public void getOwnInfo(int rankMode) {
        if (!U.getNetworkUtils().hasNetwork()) {
            mILeaderBoardView.noNetWork();
            return;
        }

        ApiMethods.subscribe(mUserInfoServerApi.getMyRegion(rankMode), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult result) {
                if (result.getErrno() == 0) {
                    UserRankModel userRankModel = JSON.parseObject(result.getData().toString(), UserRankModel.class);
                    if (userRankModel != null) {
                        mILeaderBoardView.showOwnRankInfo(userRankModel);
                    }
                }
            }
        }, this);
    }
}

