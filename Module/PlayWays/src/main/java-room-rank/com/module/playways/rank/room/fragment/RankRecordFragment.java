package com.module.playways.rank.room.fragment;

import android.os.Bundle;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.util.Pair;
import android.widget.RelativeLayout;

import com.alibaba.android.arouter.launcher.ARouter;
import com.alibaba.fastjson.JSON;
import com.common.base.BaseFragment;
import com.common.core.myinfo.MyUserInfoManager;
import com.common.log.MyLog;
import com.common.rxretrofit.ApiMethods;
import com.common.rxretrofit.ApiObserver;
import com.common.rxretrofit.ApiResult;
import com.common.utils.U;
import com.common.view.ex.ExImageView;
import com.common.view.ex.ExTextView;
import com.jakewharton.rxbinding2.view.RxView;
import com.module.RouterConstants;
import com.module.playways.event.FinishPlayWayActivityEvent;
import com.module.playways.rank.room.model.RecordData;
import com.module.playways.RoomData;
import com.module.playways.rank.room.model.VoteInfoModel;
import com.module.playways.rank.room.model.WinResultModel;
import com.module.playways.rank.room.model.score.ScoreResultModel;
import com.module.playways.rank.room.presenter.EndGamePresenter;
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
    ExTextView mTvAgain;

    RecordTitleView mRecordTitleView;

    RoomData mRoomData;

    EndGamePresenter mEndGamePresenter;
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
        mTvAgain = (ExTextView) mRootView.findViewById(R.id.tv_again);
        mRecordTitleView = (RecordTitleView) mRootView.findViewById(R.id.record_title_view);
        RecordData recordData = mRoomData.getRecordData();
        RxView.clicks(mTvBack)
                .throttleFirst(300, TimeUnit.MILLISECONDS)
                .subscribe(o -> {
                    EventBus.getDefault().post(new FinishPlayWayActivityEvent());
                    getActivity().finish();
                });

        RxView.clicks(mTvAgain)
                .throttleFirst(300, TimeUnit.MILLISECONDS)
                .subscribe(o -> {
                    getActivity().finish();
                    ARouter.getInstance().build(RouterConstants.ACTIVITY_PLAY_WAYS)
                            .withInt("key_game_type", mRoomData.getGameType())
                            .withBoolean("selectSong", true)
                            .navigation();
                });

        if (recordData == null) {
            loadData();
        } else {
            bindData(recordData);
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

    private void loadData() {
        if(mEndGamePresenter==null){
            mEndGamePresenter = new EndGamePresenter(this);
            addPresent(mEndGamePresenter);
        }
        mEndGamePresenter.getVoteResult(mRoomData.getGameId(),0);
    }

    @Override
    public void destroy() {
        super.destroy();
        U.getSoundUtils().release(TAG);
        U.getSoundUtils().release(NormalLevelView.TAG);
    }

    @Override
    protected boolean onBackPressed() {
        getActivity().finish();
        return true;
    }

    @Override
    public void setData(int type, @Nullable Object data) {
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
        if(recordData!=null){
            mRoomData.setRecordData(recordData);
        }
        bindData(recordData);
    }
}
