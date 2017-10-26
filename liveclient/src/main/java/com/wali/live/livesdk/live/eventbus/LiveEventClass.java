package com.wali.live.livesdk.live.eventbus;

import android.support.annotation.NonNull;

import com.mi.live.data.query.model.MessageRule;

/**
 * Created by chenyong on 2017/2/13.
 */

public class LiveEventClass {
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
