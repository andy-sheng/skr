package com.wali.live.modulechannel.event;

/**
 * Created by zhujianning on 18-10-18.
 */

public class ChannelEvent {

    public static class SelectChannelEvent {
        public long channelId;
        public int pagerScrollState;

        public SelectChannelEvent(long channelId) {
            this.channelId = channelId;
        }

        public SelectChannelEvent(long channelId, int pagerScrollState) {
            this.channelId = channelId;
            this.pagerScrollState = pagerScrollState;
        }
    }

    public static class LiveListActivityLiveCycle {
        public Event liveEvent;

        public LiveListActivityLiveCycle(Event liveEvent) {
            this.liveEvent = liveEvent;
        }

        public enum Event {
            RESUME, PAUSE;
        }
    }
}
