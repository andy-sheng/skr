package com.module.rankingmode.prepare.sence;

import android.content.Context;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.module.rankingmode.R;
import com.module.rankingmode.prepare.sence.controller.MatchSenceController;

public class PrepareSongResSence extends RelativeLayout implements ISence {
    MatchSenceController matchSenceController;
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

    private void init(){
        inflate(getContext(), R.layout.prepare_songres_sence_layout, this);
    }

    @Override
    public void toShow(RelativeLayout parentViewGroup, Bundle bundle) {
        //这里可能有动画啥的
        setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        parentViewGroup.addView(this);
        matchSenceController.getCommonTitleBar().getCenterSubTextView().setText("准备竞演");
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
    }

    @Override
    public void onResumeSence(RelativeLayout parentViewGroup) {
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
