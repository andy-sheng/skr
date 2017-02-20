package com.mi.live.data.event;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by lan on 15-12-7.
 *
 * @module sdk event
 */
public class SdkEventController {
    /**
     * 直播，观看页面的屏幕旋转事件
     */
    public static void postOrient(int orientation) {
        SdkEventClass.OrientEvent event = new SdkEventClass.OrientEvent(orientation);
        // 采用postSticky保证监听者在注册监听时即能收到一个横竖屏事件
        EventBus.getDefault().postSticky(event);
    }
}
