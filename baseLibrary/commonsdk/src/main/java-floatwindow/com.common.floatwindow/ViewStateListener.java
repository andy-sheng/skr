package com.common.floatwindow;

/**
 * Created by yhao on 2018/5/5
 * https://github.com/yhaolpz
 */
public interface ViewStateListener {
    void onPositionUpdate(int x, int y);

    void onShow();

    void onHide();

    /**
     *
     * @param dismissReason 0 是自动消失的 1是滑动消失的 2时间到了消失
     */
    void onDismiss(int dismissReason);

    void onMoveAnimStart();

    void onMoveAnimEnd();

    void onBackToDesktop();
}
