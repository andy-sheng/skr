package com.module.rankingmode.prepare.sence;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.common.log.MyLog;
import com.common.utils.HttpUtils;
import com.common.view.ex.ExTextView;
import com.jakewharton.rxbinding2.view.RxView;
import com.module.rankingmode.R;
import com.module.rankingmode.prepare.presenter.PrepareSongPresenter;
import com.module.rankingmode.prepare.model.PrepareData;
import com.module.rankingmode.prepare.sence.controller.MatchSenceContainer;
import com.module.rankingmode.prepare.sence.controller.MatchSenceController;
import com.module.rankingmode.song.model.SongModel;

import java.util.concurrent.TimeUnit;

public class PrepareSongResSence extends RelativeLayout implements ISence {
    public static final String TAG = "PrepareSongResSence";
    MatchSenceController matchSenceController;

    ExTextView mToneTuningTv;   //试音调音
    ExTextView mMatchStatusTv;

    HttpUtils.OnDownloadProgress onDownloadProgress;

    PrepareSongPresenter prepareSongPresenter;

    Handler handler = new Handler();

    PrepareData mPrepareData;

    public PrepareSongResSence(Context context) {
        this(context, null);
    }

    public PrepareSongResSence(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PrepareSongResSence(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.prepare_songres_sence_layout, this);

        onDownloadProgress = new HttpUtils.OnDownloadProgress() {
            @Override
            public void onDownloaded(long downloaded, long totalLength) {
//                MyLog.d(TAG, "onDownloaded" + " downloaded=" + downloaded + " totalLength=" + totalLength);
            }

            @Override
            public void onCompleted(String localPath) {
                MyLog.d(TAG, "onCompleted" + " localPath=" + localPath);
                handler.post(() -> {
                    mMatchStatusTv.setText("开始匹配");
                    mToneTuningTv.setText("试唱调音");
                    mMatchStatusTv.setEnabled(true);
                    mToneTuningTv.setEnabled(true);
                });
            }

            @Override
            public void onCanceled() {
                MyLog.d(TAG, "onCanceled");
                handler.post(() -> {

                });
            }

            @Override
            public void onFailed() {
                MyLog.d(TAG, "onFailed");
                handler.post(() -> {

                });
            }
        };
    }

    @Override
    public void toShow(RelativeLayout parentViewGroup, PrepareData data) {
        mPrepareData = data;
        if(getParent()==null) {
            //这里可能有动画啥的
            setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            parentViewGroup.addView(this);
        }
        matchSenceController.getCommonTitleBar().getCenterTextView().setText("成都");
        matchSenceController.getCommonTitleBar().getCenterSubTextView().setText("准备竞演");

        mToneTuningTv = findViewById(R.id.tone_tuning_tv);
        mMatchStatusTv = findViewById(R.id.match_status_tv);

        RxView.clicks(mMatchStatusTv)
                .throttleFirst(300, TimeUnit.MILLISECONDS)
                .subscribe(o -> {
                    matchSenceController.toAssignSence(MatchSenceContainer.MatchSenceState.Matching, mPrepareData);
                });

        RxView.clicks(mToneTuningTv)
                .throttleFirst(300, TimeUnit.MILLISECONDS)
                .subscribe(o -> {
                    matchSenceController.toAssignSence(MatchSenceContainer.MatchSenceState.Audition, mPrepareData);
                });

        prepareSongPresenter = new PrepareSongPresenter(onDownloadProgress, mPrepareData.getSongModel());
        mMatchStatusTv.setText("加载歌曲中");
        mToneTuningTv.setText("加载歌曲中");
        mMatchStatusTv.setEnabled(false);
        mToneTuningTv.setEnabled(false);

        prepareSongPresenter.prepareRes();
    }

    @Override
    public void toHide(RelativeLayout parentViewGroup) {
        //可能有动画
        setVisibility(GONE);
    }

    @Override
    public boolean isPrepareToNextSence() {
        return true;
    }

    @Override
    public void toRemoveFromStack(RelativeLayout parentViewGroup) {
        parentViewGroup.removeView(this);
        prepareSongPresenter.cancelTask();
    }

    @Override
    public void onResumeSence(RelativeLayout parentViewGroup) {
        matchSenceController.getCommonTitleBar().getCenterTextView().setText("成都");
        matchSenceController.getCommonTitleBar().getCenterSubTextView().setText("准备竞演");
        setVisibility(VISIBLE);
    }

    @Override
    public boolean removeWhenPush() {
        return false;
    }

    @Override
    public void setSenceController(MatchSenceController matchSenceController) {
        this.matchSenceController = matchSenceController;
    }
}
