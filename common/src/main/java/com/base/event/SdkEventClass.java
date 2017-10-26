package com.base.event;

import com.base.activity.BaseRotateSdkActivity;

import org.greenrobot.eventbus.EventBus;

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

        private OrientEvent(int orientation) {
            this.orientation = orientation;
        }

        public boolean isLandscape() {
            return orientation == BaseRotateSdkActivity.ORIENTATION_LANDSCAPE_NORMAL || orientation == BaseRotateSdkActivity.ORIENTATION_LANDSCAPE_REVERSED;
        }
    }

    /**
     * 直播，观看页面的屏幕旋转事件
     */
    public static void postOrient(int orientation) {
        OrientEvent event = new OrientEvent(orientation);
        // 采用postSticky保证监听者在注册监听时即能收到一个横竖屏事件
        EventBus.getDefault().postSticky(event);
    }

    /**
     * 页面finish事件
     */
    public static class FinishActivityEvent {
        private FinishActivityEvent() {
        }
    }

    public static class BringFrontEvent {
        private BringFrontEvent() {
        }
    }

    public static void postBringFront() {
        BringFrontEvent event = new BringFrontEvent();
        EventBus.getDefault().postSticky(event);
    }

    public static class ScreenStateEvent {
        public static final int ACTION_SCREEN_OFF = 1;          //屏幕熄灭
        public static final int ACTION_SCREEN_ON = 2;           //屏幕点亮
        public static final int ACTION_USER_PRESENT = 3;        //屏幕解锁

        public int screenState;

        private ScreenStateEvent(int screenState) {
            this.screenState = screenState;
        }
    }

    public static void postScreenState(int screenState) {
        ScreenStateEvent event = new ScreenStateEvent(screenState);
        EventBus.getDefault().postSticky(event);
    }
}
