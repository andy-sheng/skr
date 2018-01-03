package com.wali.live.watchsdk.component.view;

import com.mi.live.data.push.model.BarrageMsg;

/**
 * Created by zyh on 2017/12/30.
 *
 * @module 特权动画的抽象接口
 */

public interface IAnimView {
    int[] getAcceptType();

    boolean isAccepted(BarrageMsg barrageMsg);

    boolean onStart();

    boolean onEnd();

    void reset();

    void onDestroy();
}
