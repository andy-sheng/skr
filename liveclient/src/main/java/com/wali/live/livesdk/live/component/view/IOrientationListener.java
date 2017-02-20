package com.wali.live.livesdk.live.component.view;

/**
 * Created by yangli on 2017/2/18.
 *
 * @module 基础架构, 屏幕旋转
 */
public interface IOrientationListener {

    /**
     * 屏幕旋转事件回调
     */
    void onOrientation(boolean isLandscape);

}
