package com.module.rankingmode.prepare.sence;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.common.core.account.UserAccountManager;
import com.common.log.MyLog;
import com.common.utils.HttpUtils;
import com.common.utils.SongResUtils;
import com.common.utils.U;
import com.common.view.ex.ExTextView;
import com.engine.EngineManager;
import com.engine.Params;
import com.jakewharton.rxbinding2.view.RxView;
import com.module.rankingmode.R;
import com.module.rankingmode.prepare.presenter.PrepareSongPresenter;
import com.module.rankingmode.prepare.model.PrepareData;
import com.module.rankingmode.prepare.sence.controller.MatchSenceContainer;
import com.module.rankingmode.prepare.sence.controller.MatchSenceController;

import java.io.File;
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
                    initMediaEngine();
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
        matchSenceController.getCommonTitleBar().getCenterTextView().setText(mPrepareData.getSongModel().getItemName());
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

//        prepareSongPresenter = new PrepareSongPresenter(onDownloadProgress, mPrepareData.getSongModel());
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

    private void initMediaEngine() {
        if (!EngineManager.getInstance().isInit()) {
            // 不能每次都初始化,播放伴奏
            EngineManager.getInstance().init("prepare",Params.getFromPref());
            EngineManager.getInstance().joinRoom("" + System.currentTimeMillis(), (int) UserAccountManager.getInstance().getUuidAsLong(), true);
            File accFile = SongResUtils.getAccFileByUrl(mPrepareData.getSongModel().getAcc());
            if(accFile!=null && accFile.exists()){
                EngineManager.getInstance().startAudioMixing(accFile.getAbsolutePath(), true, false, -1);
                EngineManager.getInstance().pauseAudioMixing();

                String c = U.getDateTimeUtils().formatVideoTime(0);
                String d = U.getDateTimeUtils().formatVideoTime(EngineManager.getInstance().getAudioMixingDuration());
                String info = String.format(U.app().getString(R.string.song_time_info), c, d);
                matchSenceController.getCommonTitleBar().getCenterSubTextView().setText(info);
            }
        }
    }
}
