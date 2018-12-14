package com.module.rankingmode.prepare.sence;

import android.content.Context;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.common.log.MyLog;
import com.common.utils.SongResUtils;
import com.engine.EngineManager;
import com.module.rankingmode.R;
import com.module.rankingmode.prepare.model.PrepareData;
import com.module.rankingmode.prepare.sence.controller.MatchSenceController;
import com.module.rankingmode.song.model.SongModel;
import com.zq.lyrics.LyricsManager;
import com.zq.lyrics.LyricsReader;
import com.zq.lyrics.event.LrcEvent;
import com.zq.lyrics.widget.AbstractLrcView;
import com.zq.lyrics.widget.ManyLyricsView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;

import static com.zq.lyrics.widget.AbstractLrcView.LRCPLAYERSTATUS_PLAY;

public class AuditionSence extends RelativeLayout implements ISence {
    public static final String TAG = "AuditionSence";
    MatchSenceController matchSenceController;

    ManyLyricsView mManyLyricsView;

    PrepareData mPrepareData;

    public AuditionSence(Context context) {
        this(context, null);
    }

    public AuditionSence(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AuditionSence(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init(){
        inflate(getContext(), R.layout.audition_sence_layout, this);
        mManyLyricsView = findViewById(R.id.many_lyrics_view);
        mManyLyricsView.setOnLrcClickListener(new ManyLyricsView.OnLrcClickListener() {
            @Override
            public void onLrcPlayClicked(int progress) {
                MyLog.d(TAG, "onLrcPlayClicked");
                EngineManager.getInstance().setAudioMixingPosition(progress);
                mManyLyricsView.seekto(progress);
            }
        });

        mManyLyricsView.setOnLyricViewTapListener(new ManyLyricsView.OnLyricViewTapListener() {
            @Override
            public void onDoubleTap() {
                if(EngineManager.getInstance().getParams().isMixMusicPlaying()){
                    EngineManager.getInstance().pauseAudioMixing();
                }

                mManyLyricsView.pause();
            }

            @Override
            public void onSigleTap() {
                if(!EngineManager.getInstance().getParams().isMixMusicPlaying()){
                    EngineManager.getInstance().resumeAudioMixing();
                }

                if(mManyLyricsView.getLrcPlayerStatus() != LRCPLAYERSTATUS_PLAY){
                    MyLog.d(TAG, "LRC onSigleTap " + mManyLyricsView.getLrcStatus());
                    mManyLyricsView.resume();
                }
            }
        });
    }

    @Override
    public void toShow(RelativeLayout parentViewGroup, PrepareData data) {
        mPrepareData = data;
        if(getParent()==null) {
            //这里可能有动画啥的
            setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            parentViewGroup.addView(this);
        }

        //从bundle里面拿音乐相关数据，然后开始试唱
        SongModel songModel = mPrepareData.getSongModel();
        String fileName = SongResUtils.getFileNameWithMD5(songModel.getLyric());
        MyLog.d(TAG, "toShow" + " fileName=" + fileName + " song name is " + songModel.getItemName());
        if(!EventBus.getDefault().isRegistered(this)){
            EventBus.getDefault().register(this);
        }

        LyricsManager.getLyricsManager(getContext()).loadLyricsUtil(fileName, songModel.getItemName(), fileName.hashCode() + "");

        matchSenceController.getCommonTitleBar().getCenterSubTextView().setText("试唱调音");

        File accFile = SongResUtils.getAccFileByUrl(songModel.getAcc());
        String accFilePath = accFile.getAbsolutePath();
        // 当前音乐在播放那就继续播放
        if(accFilePath.equals(EngineManager.getInstance().getParams().getMIxMusicFilePath())){
            if(!EngineManager.getInstance().getParams().isMixMusicPlaying()) {
                EngineManager.getInstance().resumeAudioMixing();
            }
        }else{
            EngineManager.getInstance().startAudioMixing(accFilePath,true,false,-1);
        }
        if(accFile != null){

        }else {
            MyLog.e("what the fuck");
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(LrcEvent.FinishLoadLrcEvent finishLoadLrcEvent) {
        LyricsReader lyricsReader = LyricsManager.getLyricsManager(getContext()).getLyricsUtil(finishLoadLrcEvent.hash);
        if (lyricsReader != null) {
            lyricsReader.setHash(finishLoadLrcEvent.hash);
            mManyLyricsView.initLrcData();
            mManyLyricsView.setLyricsReader(lyricsReader);
            if (mManyLyricsView.getLrcStatus() == AbstractLrcView.LRCSTATUS_LRC && mManyLyricsView.getLrcPlayerStatus() != LRCPLAYERSTATUS_PLAY){
                mManyLyricsView.play(0);
            }
        }
    }

    @Override
    public void toHide(RelativeLayout parentViewGroup) {
        //可能有动画
        setVisibility(GONE);
    }

    @Override
    public void toRemoveFromStack(RelativeLayout parentViewGroup) {
        parentViewGroup.removeView(this);

    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        EngineManager.getInstance().stopAudioMixing();
        EventBus.getDefault().unregister(this);
    }

    //每个场景有一个是不是可以往下跳的判断
    @Override
    public boolean isPrepareToNextSence() {
        return true;
    }

    @Override
    public void onResumeSence(RelativeLayout parentViewGroup) {
        matchSenceController.getCommonTitleBar().getCenterSubTextView().setText("试唱调音");
        setVisibility(VISIBLE);
    }

    @Override
    public boolean removeWhenPush() {
        return true;
    }

    @Override
    public void setSenceController(MatchSenceController matchSenceController) {
        this.matchSenceController = matchSenceController;
    }
}
