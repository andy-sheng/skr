package com.module.rankingmode.prepare.sence;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import com.common.log.MyLog;
import com.common.utils.SongResUtils;
import com.engine.EngineManager;
import com.module.rankingmode.R;
import com.module.rankingmode.prepare.model.PrepareData;
import com.zq.lyrics.LyricsManager;
import com.zq.lyrics.LyricsReader;
import com.zq.lyrics.event.LrcEvent;
import com.zq.lyrics.widget.AbstractLrcView;
import com.zq.lyrics.widget.ManyLyricsView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import static com.zq.lyrics.widget.AbstractLrcView.LRCPLAYERSTATUS_PLAY;

public class AuditionSence extends RelativeLayout{
    public static final String TAG = "AuditionSence";
//    MatchSenceController matchSenceController;

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

//    @Override
//    public void toShow(RelativeLayout parentViewGroup, PrepareData data) {
//        mPrepareData = data;
//        if(getParent()==null) {
//            //这里可能有动画啥的
//            setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
//            parentViewGroup.addView(this);
//        }
//
//        //从bundle里面拿音乐相关数据，然后开始试唱
//        SongModel songModel = mPrepareData.getSongModel();
//        String fileName = SongResUtils.getFileNameWithMD5(songModel.getLyric());
//        MyLog.d(TAG, "toShow" + " fileName=" + fileName + " song name is " + songModel.getItemName());
//        if(!EventBus.getDefault().isRegistered(this)){
//            EventBus.getDefault().register(this);
//        }
//
//        LyricsManager.getLyricsManager(getContext()).loadLyricsUtil(fileName, songModel.getItemName(), fileName.hashCode() + "");
//
////        matchSenceController.getCommonTitleBar().getCenterSubTextView().setText("试唱调音");
//
//        File accFile = SongResUtils.getAccFileByUrl(songModel.getAcc());
//        if(accFile != null){
//            String accFilePath = accFile.getAbsolutePath();
//            // 当前音乐在播放那就继续播放
//            if(accFilePath.equals(EngineManager.getInstance().getParams().getMixMusicFilePath())){
//                if(!EngineManager.getInstance().getParams().isMixMusicPlaying()) {
//                    EngineManager.getInstance().resumeAudioMixing();
//                }
//            }else{
//                EngineManager.getInstance().startAudioMixing(accFilePath,true,false,-1);
//            }
//        }else {
//            MyLog.e("what the fuck");
//        }
//    }

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

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(LrcEvent.RestartLrcEvent restartLrcEvent) {
        String fileName = SongResUtils.getFileNameWithMD5(mPrepareData.getSongModel().getLyric());
        LyricsManager.getLyricsManager(getContext()).loadLyricsUtil(fileName, mPrepareData.getSongModel().getItemName(), fileName.hashCode() + "");
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        EngineManager.getInstance().stopAudioMixing();
        EventBus.getDefault().unregister(this);
    }
}
