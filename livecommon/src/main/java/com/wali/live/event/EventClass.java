package com.wali.live.event;

import com.wali.live.common.model.CommentModel;
import com.wali.live.receiver.NetworkReceiver;

import org.json.JSONObject;

import java.util.List;

public abstract class EventClass {
    /**
     * 网络变化的事件
     */
    public static class NetWorkChangeEvent {
        private NetworkReceiver.NetState netState;

        public NetWorkChangeEvent(NetworkReceiver.NetState state) {
            this.netState = state;
        }

        public NetworkReceiver.NetState getNetState() {
            return netState;
        }
    }

    public static class ReleasePlayerEvent {
        public String className;

        public ReleasePlayerEvent(String name) {
            this.className = name;
        }
    }

    public static class FeedsVideoEvent {
        public static final int TYPE_START = 1;
        public static final int TYPE_STOP = 2;
        public static final int TYPE_COMPLETION = 3;
        public static final int TYPE_FULLSCREEN = 4;
        public static final int TYPE_PLAYING = 5;
        public static final int TYPE_ERROR = 6;

        public static final int TYPE_ON_CLICK_ROTATE = 1000;
        public static final int TYPE_SET_SEEK = 1001;
        public static final int TYPE_ON_CLICK_BTN = 1002;
        public static final int TYPE_ON_CLOSE_ENDLIVE = 1003;
        public static final int TYPE_ON_FEEDS_PLAY_ACT_DESTORY = 1004;
        public boolean mIsCinemaMode;
        public int mType;
        public long data;
        public int errorCode;
        public long mData;
        public String mTag;

        public FeedsVideoEvent(boolean cinemaMode, int type) {
            mIsCinemaMode = cinemaMode;
            mType = type;
        }

        public FeedsVideoEvent(int type, int errorCode) {
            mType = type;
            this.errorCode = errorCode;
        }
    }

    public static class PayEvent {
        public int eventType;

        public static final int EVENT_TYPE_PAY_DIAMOND_CACHE_CHANGE = 1;
        public static final int EVENT_TYPE_PAY_EXCHANGEABLE_DIAMOND_CHANGE = 2;

        public PayEvent(int type) {
            this.eventType = type;
        }
    }

    public static class ShowRechargeRedPoint {
    }

    public static class GooglePlayConsumeEvent {
        public JSONObject receipt;

        public GooglePlayConsumeEvent(JSONObject receipt) {
            this.receipt = receipt;
        }
    }

    public static class RefreshGameLiveCommentEvent {
        public List<CommentModel> barrageMsgs;
        public CommentModel barrageMsg;
        public String token;

        public RefreshGameLiveCommentEvent(List<CommentModel> barrageMsgs, String token) {
            this.barrageMsgs = barrageMsgs;
            this.token = token;
        }

        public RefreshGameLiveCommentEvent(CommentModel barrageMsg, String token) {
            this.barrageMsg = barrageMsg;
            this.token = token;
        }
    }

    /**
     * 不区分打电话，还是接电话，均分为响铃RINGING，接机OFFHOOK，和空闲IDLE
     */
    public static class PhoneStateEvent {
        public static final int TYPE_PHONE_STATE_IDLE = 1;
        public static final int TYPE_PHONE_STATE_RING = 2;
        public static final int TYPE_PHONE_STATE_OFFHOOK = 3;

        public int type;

        public PhoneStateEvent(int type) {
            this.type = type;
        }
    }
}
