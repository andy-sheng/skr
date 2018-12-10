package com.module.rankingmode.prepare.sence;

import android.content.Context;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.module.rankingmode.R;
import com.module.rankingmode.prepare.sence.controller.MatchSenceController;
import com.zq.lyrics.LyricsManager;
import com.zq.lyrics.LyricsReader;
import com.zq.lyrics.event.LrcEvent;
import com.zq.lyrics.widget.AbstractLrcView;
import com.zq.lyrics.widget.ManyLyricsView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class AuditionSence extends RelativeLayout implements ISence {
    MatchSenceController matchSenceController;

    ManyLyricsView mManyLyricsView;

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
    }

    @Override
    public void toShow(RelativeLayout parentViewGroup, Bundle bundle) {
        //这里可能有动画啥的
        setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        parentViewGroup.addView(this);

        //从bundle里面拿音乐相关数据，然后开始试唱
        String fileName = "shamoluotuo";
        if(!EventBus.getDefault().isRegistered(this)){
            EventBus.getDefault().register(this);
        }
        LyricsManager.getLyricsManager(getContext()).loadLyricsUtil(fileName, "沙漠骆驼", "5000", fileName.hashCode() + "");

        matchSenceController.getCommonTitleBar().getCenterSubTextView().setText("试唱调音");
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(LrcEvent.FinishLoadLrcEvent finishLoadLrcEvent) {
        LyricsReader lyricsReader = LyricsManager.getLyricsManager(getContext()).getLyricsUtil(finishLoadLrcEvent.hash);
        if (lyricsReader != null) {
            lyricsReader.setHash(finishLoadLrcEvent.hash);
            mManyLyricsView.initLrcData();
            mManyLyricsView.setLyricsReader(lyricsReader);
            if (mManyLyricsView.getLrcStatus() == AbstractLrcView.LRCSTATUS_LRC && mManyLyricsView.getLrcPlayerStatus() != AbstractLrcView.LRCPLAYERSTATUS_PLAY){
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
