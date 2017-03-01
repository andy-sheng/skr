package com.mi.live.data.event;


import com.mi.live.data.base.BaseRotateSdkActivity;

/**
 * Created by lan on 15-4-15.
 *
 * @module sdk event
 */
public abstract class SdkEventClass {
    /**
     * 屏幕旋转事件
     */
    public static class OrientEvent {
        public int orientation = BaseRotateSdkActivity.ORIENTATION_DEFAULT;

        public OrientEvent(int orientation) {
            this.orientation = orientation;
        }
        public boolean isLandscape(){
            return orientation  == BaseRotateSdkActivity.ORIENTATION_LANDSCAPE_NORMAL || orientation==BaseRotateSdkActivity.ORIENTATION_LANDSCAPE_REVERSED;
        }
    }

    /**
     * 页面finish事件
     */
    public static class FinishActivityEvent {

    }
}
