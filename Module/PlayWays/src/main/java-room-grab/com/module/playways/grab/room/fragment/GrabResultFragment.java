package com.module.playways.grab.room.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.text.SpannableStringBuilder;
import android.view.View;
import android.widget.TextView;

import com.alibaba.android.arouter.launcher.ARouter;
import com.alibaba.fastjson.JSON;
import com.common.base.BaseFragment;
import com.common.log.MyLog;
import com.common.rxretrofit.ApiManager;
import com.common.rxretrofit.ApiMethods;
import com.common.rxretrofit.ApiObserver;
import com.common.rxretrofit.ApiResult;
import com.common.utils.SpanUtils;
import com.common.utils.U;
import com.common.view.AnimateClickListener;
import com.common.view.ex.ExTextView;
import com.component.busilib.constans.GameModeType;
import com.component.level.view.LevelStarProgressBar;
import com.component.level.view.NormalLevelView2;
import com.module.RouterConstants;
import com.module.playways.grab.room.GrabResultData;
import com.module.playways.grab.room.GrabRoomData;
import com.module.playways.grab.room.GrabRoomServerApi;
import com.module.playways.grab.room.model.NumericDetailModel;
import com.module.playways.race.room.model.LevelResultModel;
import com.module.playways.room.prepare.model.PrepareData;
import com.module.playways.R;
import com.module.playways.room.room.model.score.ScoreStateModel;

import java.util.List;

/**
 * 一唱到底结果页面 (无作品)
 */
public class GrabResultFragment extends BaseFragment {

    public final String TAG = "GrabResultFragment";

    NormalLevelView2 mLevelView;
    ExTextView mLevelDescTv;
    LevelStarProgressBar mLevelProgress;
    TextView mChangeTv;
    TextView mDescTv;

    GrabRoomData mRoomData;
    GrabResultData mGrabResultData;

    ConstraintLayout mGrabNumArea;
    TextView mGrabNumTv;

    ConstraintLayout mBurstArea;
    TextView mBurstNumTv;

    ConstraintLayout mFlowerArea;
    TextView mFlowerNumTv;

    ConstraintLayout mCharmArea;
    TextView mCharmNumTv;

    ConstraintLayout mCoinArea;
    TextView mCoinNumTv;

    ConstraintLayout mHzArea;
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

        mLevelView = getRootView().findViewById(R.id.level_view);
        mLevelDescTv = getRootView().findViewById(R.id.level_desc_tv);
        mLevelProgress = getRootView().findViewById(R.id.level_progress);
        mChangeTv = getRootView().findViewById(R.id.change_tv);
        mDescTv = getRootView().findViewById(R.id.desc_tv);

        mGrabNumArea = getRootView().findViewById(R.id.grab_num_area);
        mGrabNumTv = getRootView().findViewById(R.id.grab_num_tv);

        mBurstArea = getRootView().findViewById(R.id.burst_area);
        mBurstNumTv = getRootView().findViewById(R.id.burst_num_tv);

        mFlowerArea = getRootView().findViewById(R.id.flower_area);
        mFlowerNumTv = getRootView().findViewById(R.id.flower_num_tv);

        mCharmArea = getRootView().findViewById(R.id.charm_area);
        mCharmNumTv = getRootView().findViewById(R.id.charm_num_tv);

        mCoinArea = getRootView().findViewById(R.id.coin_area);
        mCoinNumTv = getRootView().findViewById(R.id.coin_num_tv);

        mHzArea = getRootView().findViewById(R.id.hz_area);
        mHzNumTv = getRootView().findViewById(R.id.hz_num_tv);

        mTvBack = getRootView().findViewById(R.id.tv_back);
        mTvAgain = getRootView().findViewById(R.id.tv_again);

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
                U.getSoundUtils().play(TAG, R.raw.grab_gameover, 500);
            }
        }, 500);
    }

    @Override
    public void destroy() {
        super.destroy();
        U.getSoundUtils().release(TAG);
        mUiHandler.removeCallbacksAndMessages(null);
    }

    private void bindData() {
        if (mGrabResultData == null) {
            MyLog.w(TAG, "bindData" + " grabResultData = null");
            return;
        }

        if (mGrabResultData != null) {
            NumericDetailModel standModel = mGrabResultData.getNumericDetailModel(NumericDetailModel.RNT_SUCCESS_STAND);
            bindData(mGrabNumArea, mGrabNumTv, standModel, "", "首");
            NumericDetailModel blightModel = mGrabResultData.getNumericDetailModel(NumericDetailModel.RNT_GET_BLIGHT);
            bindData(mBurstArea, mBurstNumTv, blightModel, "", "次");
            NumericDetailModel flowerModel = mGrabResultData.getNumericDetailModel(NumericDetailModel.RNT_GIFT_FLOWER);
            bindData(mFlowerArea, mFlowerNumTv, flowerModel, "", "朵");
            NumericDetailModel meiliModel = mGrabResultData.getNumericDetailModel(NumericDetailModel.RNT_MEILI);
            bindData(mCharmArea, mCharmNumTv, meiliModel, "+", "点");
            NumericDetailModel coinModel = mGrabResultData.getNumericDetailModel(NumericDetailModel.RNT_COIN);
            bindData(mCoinArea, mCoinNumTv, coinModel, "+", "枚");
            NumericDetailModel hzModel = mGrabResultData.getNumericDetailModel(NumericDetailModel.RNT_HONGZHUAN);
            bindData(mHzArea, mHzNumTv, hzModel, "+", "枚");

            if (mGrabResultData.mLevelResultModel != null) {
                mDescTv.setText("距离下次升段还需" + mGrabResultData.mLevelResultModel.getGap() + "积分");
                if (mGrabResultData.mLevelResultModel.getGap() >= 0) {
                    mChangeTv.setText("+" + mGrabResultData.mLevelResultModel.getGap());
                } else {
                    mChangeTv.setText(String.valueOf(mGrabResultData.mLevelResultModel.getGap()));
                }
                ScoreStateModel stateModel = mGrabResultData.mLevelResultModel.getLastState();
                if (stateModel != null) {
                    mLevelDescTv.setText(stateModel.getRankingDesc());
                    int progress = 0;
                    if (stateModel.getMaxExp() != 0) {
                        progress = stateModel.getCurrExp() * 100 / stateModel.getMaxExp();
                    }
                    mLevelProgress.setCurProgress(progress);
                    mLevelView.bindData(stateModel.getMainRanking(), stateModel.getSubRanking());
                }
            }
        } else {
            MyLog.w(TAG, "bindData 数据为空了");
        }
    }

    private void bindData(ConstraintLayout group, TextView textView, NumericDetailModel model, String before, String after) {
        if (model == null) {
            group.setVisibility(View.GONE);
            return;
        }

        if (model.isNeedShow()) {
            group.setVisibility(View.VISIBLE);
            SpannableStringBuilder stringBuilder = new SpanUtils()
                    .append(before).setFontSize(12, true).setForegroundColor(U.getColor(R.color.white_trans_50))
                    .append(String.valueOf(model.getNumericVal())).setFontSize(32, true).setForegroundColor(U.getColor(R.color.white_trans_80))
                    .append(after).setFontSize(12, true).setForegroundColor(U.getColor(R.color.white_trans_50))
                    .create();
            textView.setText(stringBuilder);
        } else {
            group.setVisibility(View.GONE);
            return;
        }
    }


    private void syncFromServer() {
        if (mRoomData != null) {
            GrabRoomServerApi getStandResult = ApiManager.getInstance().createService(GrabRoomServerApi.class);
            ApiMethods.subscribe(getStandResult.getStandResult(mRoomData.getGameId()), new ApiObserver<ApiResult>() {
                @Override
                public void process(ApiResult result) {
                    if (result.getErrno() == 0) {
                        List<NumericDetailModel> models = JSON.parseArray(result.getData().getString("numericDetail"), NumericDetailModel.class);
                        LevelResultModel levelResultModel = JSON.parseObject(result.getData().getString("userScoreChange"), LevelResultModel.class);
                        if (models != null) {
                            mGrabResultData = new GrabResultData(models, levelResultModel);
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
    public boolean onBackPressed() {
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
