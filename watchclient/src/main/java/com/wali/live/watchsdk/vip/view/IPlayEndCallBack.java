package com.wali.live.watchsdk.vip.view;

import android.os.Handler;

import com.mi.live.data.push.model.BarrageMsg;

/**
 * Created by anping on 16-7-29.
 */
public interface IPlayEndCallBack {
    void endPlay(BarrageMsg barrageMsg);

    SuperLevelUserEnterAnimControlView.AnimationConfig getAnim(int type);

    Handler getUiHandle();

    void onNoRes();
}
