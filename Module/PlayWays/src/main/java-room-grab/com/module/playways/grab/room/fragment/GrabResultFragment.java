package com.module.playways.grab.room.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.widget.LinearLayout;
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
import com.common.view.ex.ExRelativeLayout;
import com.common.view.ex.ExTextView;
import com.component.busilib.constans.GameModeType;
import com.component.busilib.view.BitmapTextView;
import com.jakewharton.rxbinding2.view.RxView;
import com.module.RouterConstants;
import com.module.playways.grab.room.GrabResultData;
import com.module.playways.grab.room.GrabRoomData;
import com.module.playways.grab.room.GrabRoomServerApi;
import com.module.playways.grab.room.model.GrabResultInfoModel;
import com.module.playways.room.prepare.model.PrepareData;
import com.module.playways.room.room.model.score.ScoreResultModel;
import com.module.playways.room.room.model.score.ScoreStateModel;
import com.module.rank.R;
import com.zq.level.view.LevelStarProgressBar;
import com.zq.level.view.NormalLevelView2;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 一唱到底结果页面
 */
public class GrabResultFragment extends BaseFragment {

    public final static String TAG = "GrabResultFragment";

    GrabRoomData mRoomData;
    GrabResultData mGrabResultData;
    ScoreStateModel mScoreStateModel;

    RelativeLayout mSingEndRecord;
    ExRelativeLayout mResultArea;
    ExRelativeLayout mGrabResultArea;
    ExTextView mLevelDescTv;
    LevelStarProgressBar mLevelProgress;
    BitmapTextView mSongNum;
    BitmapTextView mSongEndPer;
    BitmapTextView mBaodengNum;
    NormalLevelView2 mLevelView;
    LinearLayout mLlBottomArea;
    ExTextView mTvBack;
    ExTextView mTvAgain;
    ExTextView mTvShare;

    Handler mUiHandler = new Handler();

    @Override
    public int initView() {
        return R.layout.grab_result_fragment_layout;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        mSingEndRecord = (RelativeLayout) mRootView.findViewById(R.id.sing_end_record);
        mResultArea = (ExRelativeLayout) mRootView.findViewById(R.id.result_area);
        mGrabResultArea = (ExRelativeLayout) mRootView.findViewById(R.id.grab_result_area);
        mLevelDescTv = (ExTextView) mRootView.findViewById(R.id.level_desc_tv);
        mLevelProgress = (LevelStarProgressBar) mRootView.findViewById(R.id.level_progress);
        mSongNum = (BitmapTextView) mRootView.findViewById(R.id.song_num);
        mSongEndPer = (BitmapTextView) mRootView.findViewById(R.id.song_end_per);
        mBaodengNum = (BitmapTextView) mRootView.findViewById(R.id.baodeng_num);
        mLevelView = (NormalLevelView2) mRootView.findViewById(R.id.level_view);
        mLlBottomArea = (LinearLayout) mRootView.findViewById(R.id.ll_bottom_area);
        mTvBack = (ExTextView) mRootView.findViewById(R.id.tv_back);
        mTvAgain = (ExTextView) mRootView.findViewById(R.id.tv_again);
        mTvShare = (ExTextView) mRootView.findViewById(R.id.tv_share);

        mGrabResultData = mRoomData.getGrabResultData();
        if (mGrabResultData == null) {
            /**
             * 游戏结束会由sync或者push触发
             * push触发的话带着结果数据
             */
            syncFromServer();
        } else {
            bindData();
        }


        RxView.clicks(mTvBack)
                .throttleFirst(300, TimeUnit.MILLISECONDS)
                .subscribe(o -> {
                    if (getActivity() != null) {
                        getActivity().finish();
                    }
                });

        RxView.clicks(mTvShare)
                .throttleFirst(300, TimeUnit.MILLISECONDS)
                .subscribe(o -> {
                    SharePanel sharePanel = new SharePanel(getActivity());
                    sharePanel.setShareContent("http://res-static.inframe.mobi/common/skr-share.png");
                    sharePanel.show(ShareType.IMAGE_RUL);
                });

        RxView.clicks(mTvAgain)
                .throttleFirst(300, TimeUnit.MILLISECONDS)
                .subscribe(o -> {
                    PrepareData prepareData = new PrepareData();
                    prepareData.setGameType(GameModeType.GAME_MODE_GRAB);
                    prepareData.setTagId(mRoomData.getTagId());
                    ARouter.getInstance()
                            .build(RouterConstants.ACTIVITY_GRAB_MATCH_ROOM)
                            .withSerializable("prepare_data", prepareData)
                            .navigation();

                    if (getActivity() != null) {
                        getActivity().finish();
                    }
                });

        U.getSoundUtils().preLoad(TAG, R.raw.grab_gameover);

        mUiHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                U.getSoundUtils().play(GrabResultFragment.TAG, R.raw.grab_gameover, 500);
            }
        }, 500);
    }

    @Override
    public void destroy() {
        super.destroy();
//        U.getSoundUtils().release(TAG);
        mUiHandler.removeCallbacksAndMessages(null);
    }

    private void bindData() {
        if (mGrabResultData == null) {
            MyLog.w(TAG, "bindData" + " grabResultData = null");
            return;
        }

        if (mGrabResultData != null) {
            for (ScoreResultModel scoreResultModel : mGrabResultData.getScoreResultModels()) {
                if (scoreResultModel.getUserID() == MyUserInfoManager.getInstance().getUid()) {
                    mScoreStateModel = scoreResultModel.getSeq(3);
                }
            }

            if (mScoreStateModel != null && mGrabResultData.getGrabResultInfoModel() != null) {
                mLevelView.bindData(mScoreStateModel.getMainRanking(), mScoreStateModel.getSubRanking());
                mLevelDescTv.setText(mScoreStateModel.getRankingDesc());
                int progress = 0;
                if (mScoreStateModel.getMaxExp() != 0) {
                    progress = mScoreStateModel.getCurrExp() * 100 / mScoreStateModel.getMaxExp();
                }
                mLevelProgress.setCurProgress(progress);
                mSongNum.setText(String.valueOf(mGrabResultData.getGrabResultInfoModel().getWholeTimeSingCnt()) + "");
                mSongEndPer.setText(String.valueOf(mGrabResultData.getGrabResultInfoModel().getWholeTimeSingRatio()) + "");
                mBaodengNum.setText(String.valueOf(mGrabResultData.getGrabResultInfoModel().getOtherBlightCntTotal()) + "");
            }
        } else {
            MyLog.w(TAG, "bindData 数据为空了");
        }
    }

    private void syncFromServer() {
        GrabRoomServerApi getStandResult = ApiManager.getInstance().createService(GrabRoomServerApi.class);
        ApiMethods.subscribe(getStandResult.getStandResult(mRoomData.getGameId()), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult result) {
                if (result.getErrno() == 0) {
                    GrabResultInfoModel resultInfoModel = JSON.parseObject(result.getData().getString("resultInfo"), GrabResultInfoModel.class);
                    List<ScoreResultModel> scoreResultModels = JSON.parseArray(result.getData().getString("userScoreResult"), ScoreResultModel.class);
                    if (resultInfoModel != null && scoreResultModels != null) {
                        mGrabResultData = new GrabResultData(resultInfoModel, scoreResultModels);
                        mRoomData.setGrabResultData(mGrabResultData);
                        bindData();
                    } else {
                        MyLog.d(TAG, "syncFromServer" + " info=null");
                    }

                }
            }
        }, this);
    }

    @Override
    public void setData(int type, @Nullable Object data) {
        super.setData(type, data);
        if (type == 0) {
            mRoomData = (GrabRoomData) data;
        }
    }

    @Override
    protected boolean onBackPressed() {
        if (getActivity() != null) {
            getActivity().finish();
        }
        return true;
    }

    @Override
    public boolean useEventBus() {
        return false;
    }
}
