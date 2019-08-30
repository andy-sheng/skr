package com.module.playways.room.room.presenter;

import com.alibaba.fastjson.JSON;
import com.common.core.userinfo.UserInfoServerApi;
import com.module.playways.room.room.model.RankInfoModel;
import com.component.person.model.UserRankModel;
import com.common.mvp.RxLifeCyclePresenter;
import com.common.rxretrofit.ApiManager;
import com.common.rxretrofit.ApiMethods;
import com.common.rxretrofit.ApiObserver;
import com.common.rxretrofit.ApiResult;
import com.common.utils.U;
import com.module.playways.room.room.view.ILeaderBoardView;

import java.util.ArrayList;
import java.util.List;

public class LeaderboardPresenter extends RxLifeCyclePresenter {

    public final String TAG = "LeaderboardPresenter";

    UserInfoServerApi mUserInfoServerApi;

    private ILeaderBoardView mILeaderBoardView;

    private int mOffset = 0;
    private int mLimit = 20;
    List<RankInfoModel> mRankInfoModelList;

    public LeaderboardPresenter(ILeaderBoardView ILeaderBoardView) {
        mILeaderBoardView = ILeaderBoardView;
        mRankInfoModelList = new ArrayList<>();
        this.mUserInfoServerApi = ApiManager.getInstance().createService(UserInfoServerApi.class);
    }

    public void reset() {
        mOffset = 0;
        mRankInfoModelList.clear();
    }

    public void getLeaderBoardInfo(int rankMode) {
        if (!U.getNetworkUtils().hasNetwork()) {
            mILeaderBoardView.noNetWork();
            return;
        }

        ApiMethods.subscribe(mUserInfoServerApi.getReginRankList(rankMode, mOffset, mLimit), new ApiObserver<ApiResult>() {
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

