package com.module.rankingmode.prepare.sence.controller;

import android.content.Context;
import android.os.Bundle;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import com.common.log.MyLog;
import com.common.view.titlebar.CommonTitleBar;
import com.module.rankingmode.prepare.model.PrepareData;
import com.module.rankingmode.prepare.sence.AuditionSence;
import com.module.rankingmode.prepare.sence.FastMatchSuccessSence;
import com.module.rankingmode.prepare.sence.FastMatchingSence;
import com.module.rankingmode.prepare.sence.ISence;
import com.module.rankingmode.prepare.sence.PrepareSongResSence;

import java.util.Stack;

public class MatchSenceContainer extends RelativeLayout implements MatchSenceController {
    Stack<ISence> senceQueue = new Stack<>();

    MatchSenceState currentMatchSenceState = MatchSenceState.empty;

    CommonTitleBar commonTitleBar;

    OnSenceStateChangeListener onStateChangeListener;

    public MatchSenceContainer(Context context) {
        this(context, null);
    }

    public MatchSenceContainer(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MatchSenceContainer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setOnStateChangeListener(OnSenceStateChangeListener onStateChangeListener) {
        this.onStateChangeListener = onStateChangeListener;
    }

    @Override
    public boolean interceptBackPressed() {
        if (senceQueue.size() != 0) {
            popSence();
            return true;
        }

        return false;
    }

    @Override
    public int getSenceSize() {
        return senceQueue.size();
    }

    @Override
    public void toAssignSence(MatchSenceState matchSenceState, PrepareData data) {
        if (senceQueue.size() != 0) {
            ISence iSence = senceQueue.peek();
            if (!iSence.isPrepareToNextSence()) {
                MyLog.e("sense is not prepared");
                return;
            }

            ISence preISence = senceQueue.peek();
            if (preISence != null) {
                preISence.toHide(this);
                if (preISence.removeWhenPush()) {
                    senceQueue.pop();
                    preISence.toRemoveFromStack(this);
                }
            }
        }

        ISence iSence = createSence(matchSenceState, getContext(), this);
        iSence.toShow(this, data);
        updateMatchSenceState(iSence);
        senceQueue.push(iSence);
    }

    @Override
    public void toNextSence(PrepareData data) {
        toAssignSence(MatchSenceState.values()[currentMatchSenceState.getValue() + 1], data);
    }

    @Override
    public void popSence() {
        if (senceQueue.size() > 0) {
            ISence iSence = senceQueue.pop();
            iSence.toRemoveFromStack(this);

            if (senceQueue.size() > 0) {
                ISence preISence = senceQueue.peek();
                if (preISence != null) {
                    preISence.onResumeSence(this);
                    updateMatchSenceState(preISence);
                }
            } else {
                updateMatchSenceState(null);
            }
        }
    }

    private void updateMatchSenceState(ISence currentSence) {
        if (currentSence == null) {
            currentMatchSenceState = MatchSenceState.empty;
        } else if (currentSence instanceof PrepareSongResSence) {
            currentMatchSenceState = MatchSenceState.PrepareSongRes;
        } else if (currentSence instanceof AuditionSence) {
            currentMatchSenceState = MatchSenceState.Audition;
        } else if (currentSence instanceof FastMatchingSence) {
            currentMatchSenceState = MatchSenceState.Matching;
        } else if (currentSence instanceof FastMatchSuccessSence) {
            currentMatchSenceState = MatchSenceState.MatchingSucess;
        }

        if(onStateChangeListener != null){
            onStateChangeListener.onChange(currentMatchSenceState);
        }
    }

    @Override
    public CommonTitleBar getCommonTitleBar() {
        return commonTitleBar;
    }

    @Override
    public void setCommonTitleBar(CommonTitleBar commonTitleBar) {
        this.commonTitleBar = commonTitleBar;
    }

    public MatchSenceState getCurrentMatchState() {
        return currentMatchSenceState;
    }

    public interface OnSenceStateChangeListener {
        void onChange(MatchSenceState matchSenceState);
    }

    public enum MatchSenceState {
        empty(0), PrepareSongRes(1), Audition(2), Matching(3), MatchingSucess(4);

        private int value;

        private MatchSenceState(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    public static ISence createSence(MatchSenceState matchSenceState, Context context, MatchSenceController matchSenceController) {
        ISence iSence = null;
        switch (matchSenceState) {
            case empty:
                break;
            case PrepareSongRes:
                iSence = new PrepareSongResSence(context);
                break;
            case Audition:
                iSence = new AuditionSence(context);
                break;
            case Matching:
                iSence = new FastMatchingSence(context);
                break;
            case MatchingSucess:
                iSence = new FastMatchSuccessSence(context);
                break;
        }

        if (iSence != null) {
            iSence.setSenceController(matchSenceController);
        }

        return iSence;
    }
}
