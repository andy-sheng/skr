package com.wali.live.livesdk.live.eventbus;

import android.support.annotation.NonNull;

import com.mi.live.data.query.model.MessageRule;

/**
 * Created by chenyong on 2017/2/13.
 */

public class LiveEventClass {

    public static class SystemEvent {
        public int type;
        public Object obj1;
        public Object obj2;
        public static final int EVENT_TYPE_ACTION_NEW_OUTGOING_CALL = 1; // 打电话
        public static final int EVENT_TYPE_ACTION_PHONE_STATE_CHANGED_RING = 2; // 来电了
        public static final int EVENT_TYPE_ACTION_PHONE_STATE_CHANGED_IDLE = 3; // 来电了

        public SystemEvent(int type, Object obj1, Object obj2) {
            this.type = type;
            this.obj1 = obj1;
            this.obj2 = obj2;
        }
    }

    public static class ScreenStateEvent {
        public static final int ACTION_SCREEN_OFF = 1;          //屏幕熄灭
        public static final int ACTION_SCREEN_ON = 2;           //屏幕点亮
        public static final int ACTION_USER_PRESENT = 3;        //屏幕解锁

        public int screenState;

        public ScreenStateEvent(int screenState) {
            this.screenState = screenState;
        }
    }

    public static class HidePrepareGameLiveEvent {
    }

    public static class LiveCoverEvent {
        public String url;

        public LiveCoverEvent(@NonNull String url) {
            this.url = url;
        }
    }

    /**
     * 更新发送弹幕频率限制成功事件
     */
    public static class UpdateMsgRuleEvent {

        private String TAG = UpdateMsgRuleEvent.class.getSimpleName();

        private boolean isUpdated;
        private long uuid;
        private String roomId;
        private MessageRule msgRule;

        public UpdateMsgRuleEvent(boolean isUpdated, long uuid, String roomId, MessageRule msgRule) {
            this.isUpdated = isUpdated;
            this.uuid = uuid;
            this.roomId = roomId;
            this.msgRule = msgRule;
        }

        public long getUuid() {
            return uuid;
        }

        public String getRoomId() {
            return roomId;
        }

        public MessageRule getMsgRule() {
            return msgRule;
        }

        public boolean isUpdated() {
            return isUpdated;
        }

        @Override
        public String toString() {
            return TAG + " isUpdated=" + isUpdated + " uuid=" + uuid + " roomId=" + roomId + " " + msgRule.toString();
        }
    }


}
