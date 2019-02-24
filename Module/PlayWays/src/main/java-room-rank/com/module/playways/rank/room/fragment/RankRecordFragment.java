package com.module.playways.rank.room.fragment;

import android.os.Bundle;

import android.support.annotation.Nullable;

import android.view.View;
import android.widget.ProgressBar;

import android.widget.RelativeLayout;

import com.alibaba.android.arouter.launcher.ARouter;
import com.alibaba.fastjson.JSON;
import com.common.base.BaseFragment;
import com.common.core.myinfo.MyUserInfoManager;
import com.common.core.share.SharePanel;
import com.common.core.share.ShareType;
import com.common.log.MyLog;

import com.common.rxretrofit.ApiManager;

import com.common.rxretrofit.ApiMethods;
import com.common.rxretrofit.ApiObserver;
import com.common.rxretrofit.ApiResult;
import com.common.utils.U;
import com.common.view.ex.ExImageView;
import com.common.view.ex.ExRelativeLayout;
import com.common.view.ex.ExTextView;
import com.jakewharton.rxbinding2.view.RxView;
import com.module.RouterConstants;
import com.module.playways.event.FinishPlayWayActivityEvent;
import com.module.playways.rank.room.RoomServerApi;
import com.module.playways.rank.room.model.RecordData;
import com.module.playways.RoomData;
import com.module.playways.rank.room.model.UserGameResultModel;
import com.module.playways.rank.room.model.VoteInfoModel;
import com.module.playways.rank.room.model.WinResultModel;
import com.module.playways.rank.room.model.score.ScoreResultModel;

import com.module.playways.rank.room.view.IVoteView;

import com.module.playways.rank.room.view.RecordItemView;
import com.module.playways.rank.room.view.RecordTitleView;
import com.module.rank.R;
import com.zq.level.view.NormalLevelView;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class RankRecordFragment extends BaseFragment implements IVoteView {

    public final static String TAG = "RankingRecordFragment";

    RelativeLayout mRankingRecord;

    ExImageView mIvTopImg;
    RecordItemView mRecordItemOne;
    RecordItemView mRecordItemTwo;
    RecordItemView mRecordItemThree;
    ExTextView mTvBack;
    ExTextView mTvShare;

    RecordTitleView mRecordTitleView;

    RoomData mRoomData;

    ExTextView mTvReload;

    ProgressBar mLoading;

    ExRelativeLayout mRlLoadingArea;

    View mLlLoadFailed;

    ExRelativeLayout mRlRecordArea;

    RecordData mRecordData;

    RoomServerApi mRoomServerApi = ApiManager.getInstance().createService(RoomServerApi.class);

    @Override
    public int initView() {
        return R.layout.ranking_record_fragment_layout;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        mRankingRecord = (RelativeLayout) mRootView.findViewById(R.id.ranking_record);
        mIvTopImg = (ExImageView) mRootView.findViewById(R.id.iv_top_img);
        mRecordItemOne = (RecordItemView) mRootView.findViewById(R.id.record_item_one);
        mRecordItemTwo = (RecordItemView) mRootView.findViewById(R.id.record_item_two);
        mRecordItemThree = (RecordItemView) mRootView.findViewById(R.id.record_item_three);
        mTvBack = (ExTextView) mRootView.findViewById(R.id.tv_back);
        mTvShare = (ExTextView) mRootView.findViewById(R.id.tv_share);
        mRecordTitleView = (RecordTitleView) mRootView.findViewById(R.id.record_title_view);

        mRecordData = mRoomData.getRecordData();

        mTvReload = (ExTextView) mRootView.findViewById(R.id.tv_reload);
        mLoading = (ProgressBar) mRootView.findViewById(R.id.loading);
        mRlLoadingArea = (ExRelativeLayout) mRootView.findViewById(R.id.rl_loading_area);
        mLlLoadFailed = mRootView.findViewById(R.id.ll_load_failed);
        mRlRecordArea = (ExRelativeLayout) mRootView.findViewById(R.id.rl_record_area);

        RxView.clicks(mTvReload)
                .throttleFirst(300, TimeUnit.MILLISECONDS)
                .subscribe(o -> {
                    toLoadingState();
                    getVoteInfo();
                });

        RxView.clicks(mTvBack)
                .throttleFirst(300, TimeUnit.MILLISECONDS)
                .subscribe(o -> {
                    EventBus.getDefault().post(new FinishPlayWayActivityEvent());
                    if (getActivity() != null) {
                        getActivity().finish();
                    }

                    ARouter.getInstance().build(RouterConstants.ACTIVITY_PLAY_WAYS)
                            .withInt("key_game_type", mRoomData.getGameType())
                            .withBoolean("selectSong", true)
                            .navigation();
                });

        RxView.clicks(mTvShare)
                .throttleFirst(300, TimeUnit.MILLISECONDS)
                .subscribe(o -> {
                    SharePanel sharePanel = new SharePanel(getActivity());
                    sharePanel.setShareContent("http://res-static.inframe.mobi/common/skr-share.png");
                    sharePanel.show(ShareType.IMAGE_RUL);

                });

        if (mRecordData == null || mRecordData.mVoteInfoModels == null || mRecordData.mVoteInfoModels.size() == 0) {
            toLoadingState();
            getVoteInfo();
        } else {
            bindData(mRecordData);
        }
        U.getSoundUtils().preLoad(TAG, R.raw.result_win, R.raw.result_lose);
        U.getSoundUtils().preLoad(NormalLevelView.TAG, R.raw.result_addstar,
                R.raw.result_deductstar, R.raw.song_pairbutton);
    }

    void bindData(RecordData recordData) {
        try {
            mRecordTitleView.setData(mRankingRecord, recordData, mRoomData);
            mRecordItemOne.setData(mRoomData, recordData, 0, 0xFFFF79A9);
            mRecordItemTwo.setData(mRoomData, recordData, 1, 0xFF85EAFF);
            mRecordItemThree.setData(mRoomData, recordData, 2, 0xFF85EAFF);
        } catch (Exception e) {
            MyLog.e(TAG, e);
        }
    }

    private void getVoteInfo() {
        ApiMethods.subscribe(mRoomServerApi.getVoteResult(mRoomData.getGameId()), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult result) {
                if (result.getErrno() == 0) {
                    List<VoteInfoModel> voteInfoModelList = JSON.parseArray(result.getData().getString("voteInfo"), VoteInfoModel.class);
                    List<ScoreResultModel> scoreResultModels = JSON.parseArray(result.getData().getString("userScoreResult"), ScoreResultModel.class);
                    List<UserGameResultModel> userGameResults = JSON.parseArray(result.getData().getString("userGameResult"), UserGameResultModel.class);

                    // TODO: 2019/2/21 结果会由 scoreResultModels 和 userGameResults来呈现
                    if (scoreResultModels != null && scoreResultModels.size() > 0) {
                        List<WinResultModel> winResultModels = new ArrayList<>();     // 保存3个人胜负平和投票、逃跑结果
                        ScoreResultModel myScoreResultModel = new ScoreResultModel();
                        for (ScoreResultModel scoreResultModel : scoreResultModels) {
                            WinResultModel model = new WinResultModel();
                            model.setUseID(scoreResultModel.getUserID());
                            model.setType(scoreResultModel.getWinType());
                            winResultModels.add(model);

                            if (scoreResultModel.getUserID() == MyUserInfoManager.getInstance().getUid()) {
                                myScoreResultModel = scoreResultModel;
                            }
                        }
                        MyLog.d(TAG, " getVoteResult " + " voteInfoModelList " + voteInfoModelList.toString());
                        MyLog.d(TAG, " getVoteResult " + " scoreResultModel " + myScoreResultModel.toString());
                        MyLog.d(TAG, " getVoteResult " + " winResultModels " + winResultModels.toString());
                        MyLog.d(TAG, " getVoteResult " + " userGameResults " + userGameResults.toString());

                        mRecordData = new RecordData(voteInfoModelList, myScoreResultModel, winResultModels,userGameResults);
                        toLoadSuccessState();
                    }
                } else {
                    MyLog.e(TAG, "getVoteResult result errno is " + result.getErrmsg());
                    toLoadFaildState();
                }
            }

            @Override
            public void onError(Throwable e) {
                MyLog.e(TAG, e);
                toLoadFaildState();
            }
        }, this);
    }

    private void toLoadingState() {
        mRlLoadingArea.setVisibility(View.VISIBLE);
        mLoading.setVisibility(View.VISIBLE);
        mLlLoadFailed.setVisibility(View.GONE);
        mRlRecordArea.setVisibility(View.GONE);
    }

    private void toLoadFaildState() {
        mRlLoadingArea.setVisibility(View.VISIBLE);
        mLoading.setVisibility(View.GONE);
        mLlLoadFailed.setVisibility(View.VISIBLE);
        mRlRecordArea.setVisibility(View.GONE);
    }

    private void toLoadSuccessState() {
        mRlLoadingArea.setVisibility(View.GONE);
        mLoading.setVisibility(View.GONE);
        mLlLoadFailed.setVisibility(View.GONE);
        mRlRecordArea.setVisibility(View.VISIBLE);

        bindData(mRecordData);
    }

    @Override
    public void destroy() {
        super.destroy();
        U.getSoundUtils().release(TAG);
        U.getSoundUtils().release(NormalLevelView.TAG);
    }

    @Override
    protected boolean onBackPressed() {
        if (getActivity() != null) {
            getActivity().finish();
        }
        return true;
    }

    @Override
    public void setData(int type, @Nullable Object data) {
        super.setData(type, data);
        mRoomData = (RoomData) data;
    }

    @Override
    public boolean useEventBus() {
        return false;
    }

    @Override
    public void voteSucess(long votedUserId) {

    }

    @Override
    public void voteFailed() {

    }

    @Override
    public void showRecordView(RecordData recordData) {
        if (recordData != null) {
            mRoomData.setRecordData(recordData);
        }
        bindData(recordData);
    }
}
