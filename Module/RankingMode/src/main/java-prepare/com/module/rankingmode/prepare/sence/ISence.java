package com.module.rankingmode.prepare.sence;

import android.os.Bundle;
import android.widget.RelativeLayout;

import com.module.rankingmode.prepare.model.PrepareData;
import com.module.rankingmode.prepare.sence.controller.MatchSenceController;

/**
 * 场景基类
 */
public interface ISence {
    //新添加的场景
    void toShow(RelativeLayout parentViewGroup, PrepareData data);

    void toHide(RelativeLayout parentViewGroup);

    void toRemoveFromStack(RelativeLayout parentViewGroup);

    //每个场景有一个是不是可以往下跳的判断
    boolean isPrepareToNextSence();

    //不是新场景，之前添加过，现在重新显示
    void onResumeSence(RelativeLayout parentViewGroup);

    /**
     * 压栈的时候是否需要移除
     * @return
     */
    boolean removeWhenPush();

    void setSenceController(MatchSenceController matchSenceController);
}
