package com.wali.live.watchsdk.vip.view;

import com.mi.live.data.push.model.BarrageMsg;

/**
 * Created by anping on 16-7-29.
 */
public interface ISuperLevelView {
    String TAG = "ISuperLevelView";

    void play(); //view 自己需要实现的播放动画的能力

    boolean acceptBarrage(BarrageMsg barrageMsg); //是否拦截处理这个弹幕

    /**
     * {@linkplain #acceptBarrage(BarrageMsg)}返回true才会执行
     */
    boolean onStart(BarrageMsg barrageMsg); //弹幕启动播放后的回调函数

    boolean onEnd(BarrageMsg barrageMsg);// 弹幕播放完毕之后的回调函数

    void setAnchorId(long anchorId);// 设置主播id

    void setFatherViewCallBack(IPlayEndCallBack playEndCallBack);

    void onDestroy();
}
