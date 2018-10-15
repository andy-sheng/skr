package com.wali.live.event;

import org.greenrobot.eventbus.EventBus;

/**
 * 组件点击事件
 */
public class UserActionEvent {
    public static final int EVENT_TYPE_REQUEST_LOOK_USER_INFO = 1;
    public static final int EVENT_TYPE_REQUEST_LOOK_USER_TICKET = 2;
    public static final int EVENT_TYPE_REQUEST_WANT_FOLLOW_USER = 3;
    public static final int EVENT_TYPE_TOUCH_DOWN_COMMENT_RC = 4;
    public static final int EVENT_TYPE_REQUEST_LOOK_MORE_VIEWER = 5;
    public static final int EVENT_TYPE_REQUEST_SET_MANAGER = 6;
    public static final int EVENT_TYPE_REQUEST_CLOSE_SUPPORT_SELECT_VIEW = 7;
    public static final int EVENT_TYPE_REQUEST_OPEN_SUPPORT_SELECT_VIEW = 8;
    public static final int EVENT_TYPE_REQUEST_SWITCH_OTHER_ANCHOR = 9;         // 切换到别的主播
    public static final int EVENT_TYPE_REQUEST_PK = 10;                         // 请求和某个主播PK
    public static final int EVENT_TYPE_REQUEST_LINK_MIC = 11;                   // 主播请求和这个观众进行连麦
    public static final int EVENT_TYPE_REQUEST_LINE_ACCEPT = 12;                // 观众同意连麦 obj1 为user   obj2 为mode
    public static final int EVENT_TYPE_REQUEST_LINE_CANCEL = 13;                // 观众拒绝连麦 obj1 为user  obj2 为mode
    public static final int EVENT_TYPE_REQUEST_LINE_MIC_STOP = 14;              // 停止连麦

    public static final int EVENT_TYPE_CLICK_ATTACHMENT = 15;
    public static final int EVENT_TYPE_CLICK_SELECT_GIFT = 20;

    public static final int EVENT_TYPE_CLICK_PUSH_IMG = 19;
    public static final int EVENT_TYPE_CLICK_HOTSPOT = 21;
    public static final int EVENT_TYPE_CLICK_SUPPORT_WIDGET = 22;

    public int type;
    public Object obj1;
    public Object obj2;
    public Object obj3;
    public Object obj4;

    public UserActionEvent(int type, Object obj1, Object obj2) {
        this.type = type;
        this.obj1 = obj1;
        this.obj2 = obj2;
    }

    public UserActionEvent(int type, Object obj1, Object obj2, Object obj3) {
        this.type = type;
        this.obj1 = obj1;
        this.obj2 = obj2;
        this.obj3 = obj3;
    }

    public UserActionEvent(int type, Object obj1, Object obj2, Object obj3, Object obj4) {
        this.type = type;
        this.obj1 = obj1;
        this.obj2 = obj2;
        this.obj3 = obj3;
        this.obj4 = obj4;
    }

    public static void post(int type, Object obj1, Object obj2) {
        EventBus.getDefault().post(new UserActionEvent(type, obj1, obj2));
    }

    public static void post(int type, Object obj1, Object obj2, Object obj3) {
        EventBus.getDefault().post(new UserActionEvent(type, obj1, obj2, obj3));
    }

    public static void post(int type, Object obj1, Object obj2, Object obj3, Object obj4) {
        EventBus.getDefault().post(new UserActionEvent(type, obj1, obj2, obj3, obj4));
    }

    public static class SwitchAnchor {
        public long anchorId;

        public SwitchAnchor(long anchorId) {
            this.anchorId = anchorId;
        }

    }
}

