package com.wali.live.event;

import android.content.Intent;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.mi.live.data.gift.model.GiftCard;
import com.wali.live.common.model.CommentModel;
import com.wali.live.pay.constant.PayWay;
import com.wali.live.proto.PayProto;
import com.wali.live.receiver.NetworkReceiver;

import java.util.List;

public abstract class EventClass {
    /**
     * 网络变化的事件
     */
    public static class NetWorkChangeEvent {
        private NetworkReceiver.NetState netState;
        private String netId;

        public NetWorkChangeEvent(NetworkReceiver.NetState state, String netId) {
            this.netState = state;
            this.netId = netId;
        }

        public NetworkReceiver.NetState getNetState() {
            return netState;
        }

        public String getNetworkId() {
            return TextUtils.isEmpty(netId) ? ("" + netState) : (netState + "_" + netId);
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
        public static final int TYPE_SMALL_TO_FULLSCREEN = 4;
        public static final int TYPE_FULLSCREEN_TO_SMALL = 5;
        public static final int TYPE_PLAYING = 6;
        public static final int TYPE_ERROR = 7;

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

    /**
     * 发送弹幕频率限制更改事件
     */
    public static class MsgRuleChangedEvent {
        private String roomId;

        private int speakPeriod;

        private int oriSpeakPeriod;

        private boolean unrepeatable;

        public MsgRuleChangedEvent(String roomId, int speakPeriod, int oriSpeakPeriod, boolean unrepeatable) {
            this.roomId = roomId;
            this.speakPeriod = speakPeriod;
            this.oriSpeakPeriod = oriSpeakPeriod;
            this.unrepeatable = unrepeatable;
        }

        public String getRoomId() {
            return roomId;
        }

        public int getSpeakPeriod() {
            return speakPeriod;
        }

        public int getOriSpeakPeriod() {
            return oriSpeakPeriod;
        }

        public boolean isUnrepeatable() {
            return unrepeatable;
        }
    }

    public static class PayPush {
        @Nullable
        public PayProto.PayPush payPush;

        public PayPush(@Nullable PayProto.PayPush payPush) {
            this.payPush = payPush;
        }
    }

    public static class CloseWebEvent {
    }

    public static class LoadingEndEvent {
    }

    public static class GiftCardPush {
        public Object obj1;

        public GiftCardPush(Object obj1) {
            this.obj1 = obj1;
        }
    }

    public static class RechargeCheckOrderEvent {
        public PayWay payWay;
        public String orderId;
        public String payId;
        public String receipt;
        public String transactionId;
        public boolean showTip;

        public RechargeCheckOrderEvent(PayWay payWay, String orderId, String payId, String receipt, String transactionId, boolean showTip) {
            this.payWay = payWay;
            this.orderId = orderId;
            this.payId = payId;
            this.receipt = receipt;
            this.transactionId = transactionId;
            this.showTip = showTip;
        }
    }

    /**
     * 极有可能通过MyInfoFragment发出UserInfoEvent
     */
    public static class WithdrawEvent {
        public int eventType;

        public static final int EVENT_TYPE_ACCOUNT_TICKET_CHANGE = 1;// 账户尚票发生变化
        public static final int EVENT_TYPE_ACCOUNT_BIND_CHANGE = 2;// 绑定账户发生变化

        public WithdrawEvent(int type) {
            this.eventType = type;
        }
    }

    /**
     * 根据tag判断是否给其的event
     */
    public static class ItemClickEvent {
        public String tag;

        public ItemClickEvent(String tag) {
            this.tag = tag;
        }
    }

    public static class ShowRechargeProgressEvent {
    }

    public static class HideRechargeProgressEvent {
    }

    public static class ChangeCancel {
        public ChangeCancel(long userId) {
            this.userId = userId;
        }

        public long userId;
    }

    public static class OnActivityResultEvent {
        public int requestCode;
        public int resultCode;
        public Intent data;

        public OnActivityResultEvent(int requestCode, int resultCode, Intent data) {
            this.requestCode = requestCode;
            this.resultCode = resultCode;
            this.data = data;
        }
    }

    public static class ChangeBottomInvitee {
        public ChangeBottomInvitee(long userId, long avatar, boolean checked) {
            this.userId = userId;
            this.avatar = avatar;
            this.checked = checked;
        }

        public final long userId;
        public final long avatar;
        public final boolean checked;
    }

    public static class ShareEvent {
        public static final int TYPE_NONE = 0;
        public static final int TYPE_SUCCESS = 0;
        public static final int TYPE_FAILED = 0;
        public int snsType = -1;        //分享的type
        public int state = TYPE_NONE;   //分享的结果

        public ShareEvent(int state, int type) {
            this.snsType = type;
            this.state = state;
        }

        public int getSnsType() {
            return snsType;
        }

        public int getState() {
            return state;
        }
    }

    public static class KickEvent {

    }

    /**
     * h5页面上, 用户没有登录的事件
     */
    public static class H5UnloginEvent {

    }

    /**
     * 跳转兑换钻石事件
     * ps:因为山叔把支付相关的放在LiveCommon里面，
     * 点击兑换钻石需要跳转WebView,但是WebView相关的在WatchClient module下面，
     * 相关文件太多，所以用EventBus将事件抛道上层。
     */
    public static class H5ExchangeEvent {
    }

    /**
     * 直播观众端展示发弹幕气泡
     */
    public static final class ShowBarragePopEvent {

    }

    public static class GiftCardChangeEvent {
        public GiftCard card;
        public long timeStamp;

        public GiftCardChangeEvent(GiftCard card, long timeStamp) {
            this.card = card;
            this.timeStamp = timeStamp;
        }
    }
}
