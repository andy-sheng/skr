package com.module.playways.grab.room.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.constraint.Group;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

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
import com.common.view.AnimateClickListener;
import com.common.view.ex.ExRelativeLayout;
import com.common.view.ex.ExTextView;
import com.component.busilib.constans.GameModeType;
import com.component.busilib.view.BitmapTextView;
import com.dialog.view.StrokeTextView;
import com.module.RouterConstants;
import com.module.playways.grab.room.GrabResultData;
import com.module.playways.grab.room.GrabRoomData;
import com.module.playways.grab.room.GrabRoomServerApi;
import com.module.playways.grab.room.model.GrabResultInfoModel;
import com.module.playways.room.prepare.model.PrepareData;
import com.module.playways.room.room.model.score.ScoreResultModel;
import com.module.playways.room.room.model.score.ScoreStateModel;
import com.module.playways.R;
import com.zq.level.view.LevelStarProgressBar;
import com.zq.level.view.NormalLevelView2;

import java.util.List;

/**
 * 一唱到底结果页面 (无作品)
 */
public class GrabResultFragment extends BaseFragment {

    public final static String TAG = "GrabResultFragment";

    GrabRoomData mRoomData;
    GrabResultData mGrabResultData;
    ScoreStateModel mScoreStateModel;

    Group mGrabNumArea;
    TextView mGrabNumTv;
    Group mBurstArea;
    TextView mBurstNumTv;
    Group mFlowerArea;
    TextView mFlowerNumTv;
    Group mCharmArea;
    TextView mCharmNumTv;
    Group mCoinArea;
    TextView mCoinNumTv;
    Group mHzArea;
    TextView mHzNumTv;
    ExTextView mTvBack;
    ExTextView mTvAgain;

    Handler mUiHandler = new Handler();

    @Override
    public int initView() {
        return R.layout.grab_result_fragment_layout;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {

        mGrabNumArea = mRootView.findViewById(R.id.grab_num_area);
        mGrabNumTv = mRootView.findViewById(R.id.grab_num_tv);

        mBurstArea = mRootView.findViewById(R.id.burst_area);
        mBurstNumTv = mRootView.findViewById(R.id.burst_num_tv);

        mFlowerArea = mRootView.findViewById(R.id.flower_area);
        mFlowerNumTv = mRootView.findViewById(R.id.flower_num_tv);

        mCharmArea = mRootView.findViewById(R.id.charm_area);
        mCharmNumTv = mRootView.findViewById(R.id.charm_num_tv);

        mCoinArea = mRootView.findViewById(R.id.coin_area);
        mCoinNumTv = mRootView.findViewById(R.id.coin_num_tv);

        mHzArea = mRootView.findViewById(R.id.hz_area);
        mHzNumTv = mRootView.findViewById(R.id.hz_num_tv);

        mTvBack = mRootView.findViewById(R.id.tv_back);
        mTvAgain = mRootView.findViewById(R.id.tv_again);

        if (mRoomData != null) {
            mGrabResultData = mRoomData.getGrabResultData();
        }

        if (mGrabResultData == null) {
            /**
             * 游戏结束会由sync或者push触发
             * push触发的话带着结果数据
             */
            syncFromServer();
        } else {
            bindData();
        }


        mTvBack.setOnClickListener(new AnimateClickListener() {
            @Override
            public void click(View view) {
                if (getActivity() != null) {
                    getActivity().finish();
                }
            }
        });

//        mTvShare.setOnClickListener(new AnimateClickListener() {
//            @Override
//            public void click(View view) {
//                SharePanel sharePanel = new SharePanel(getActivity());
//                sharePanel.setShareContent("http://res-static.inframe.mobi/common/skr-share.png");
//                sharePanel.show(ShareType.IMAGE_RUL);
//            }
//        });

        mTvAgain.setOnClickListener(new AnimateClickListener() {
            @Override
            public void click(View view) {
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
        U.getSoundUtils().release(GrabResultFragment.TAG);
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
                // TODO: 2019-07-02 等信令更新
            }
        } else {
            MyLog.w(TAG, "bindData 数据为空了");
        }
    }

    private void syncFromServer() {
        if (mRoomData != null) {
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
        } else {
            MyLog.d(TAG, "syncFromServer" + " mRoomData == null Why?");
        }
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
