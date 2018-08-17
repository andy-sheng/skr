package com.wali.live.watchsdk.eventbus;

import android.support.annotation.NonNull;

import com.mi.live.data.gift.model.GiftCard;
import com.mi.live.data.push.model.BarrageMsg;
import com.wali.live.watchsdk.fans.adapter.FansMemberAdapter;

import java.util.List;

/**
 * Created by zyh on 2017/11/9.
 */
public class EventClass {
    public static class UpdateMemberListEvent {
        public int loadingStart;
        public List<FansMemberAdapter.MemberItem> memberItems;

        public UpdateMemberListEvent(List<FansMemberAdapter.MemberItem> memberItems) {
            this.memberItems = memberItems;
            this.loadingStart = memberItems != null ? memberItems.size() : 0;
        }
    }

    public static class AdminChangeEvent {
        public boolean isAdmin = false;

        public AdminChangeEvent(boolean isAdmin) {
            this.isAdmin = isAdmin;
        }

        public boolean isAdmin() {
            return isAdmin;
        }

        public void setAdmin(boolean admin) {
            isAdmin = admin;
        }
    }

    public static class ShowContestView {
        public static final int TYPE_SUCCESS_VIEW = 10;
        public static final int TYPE_FAIL_VIEW = 11;
        public static final int TYPE_LATE_VIEW = 12;
        public static final int TYPE_AWARD_VIEW = 13;
        public static final int TYPE_INVITE_SHARE_VIEW = 14;
        public static final int TYPE_WIN_SHARE_VIEW = 15;

        public static final int ACTION_SHOW = 1;
        public static final int ACTION_HIDE = 2;

        public int type;
        public int action;

        public ShowContestView(int type, int action) {
            this.type = type;
            this.action = action;
        }
    }

    public static class OauthResultEvent {
        public static final int EVENT_TYPE_CODE = 1;
        public static final int EVENT_TYPE_TOKEN = 2;
        private int eventFrom;   //区分来源
        private int eventType;
        private String accessToken;
        private String refreshToken;
        private String expiresIn;
        private String code;
        private String openId;

        public OauthResultEvent(int _eventType) {
            eventType = _eventType;
        }

//        public OauthResultEvent(int _eventType, String _accessToken, String _refreshToken, String _expiresIn, String _code, String _openId) {
//            eventType = _eventType;
//            accessToken = _accessToken;
//            refreshToken = _refreshToken;
//            expiresIn = _expiresIn;
//            code = _code;
//            openId = _openId;
//        }

        public OauthResultEvent(int _eventFrom, int _eventType, String _accessToken, String _refreshToken, String _expiresIn, String _code, String _openId) {
            eventFrom = _eventFrom;
            eventType = _eventType;
            accessToken = _accessToken;
            refreshToken = _refreshToken;
            expiresIn = _expiresIn;
            code = _code;
            openId = _openId;
        }

        public int getEventFrom() {
            return eventFrom;
        }

        public int getEventType() {
            return eventType;
        }

        public String getAccessToken() {
            return accessToken;
        }

        public String getRefreshToken() {
            return refreshToken;
        }

        public String getExpiresIn() {
            return expiresIn;
        }

        public String getCode() {
            return code;
        }

        public String getOpenId() {
            return openId;
        }
    }

    public static class StopPlayVideoEvent {
        public boolean isNeedToReset = false;
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

    public static class ChannelVideoCtrlEvent {
        public ChannelVideoCtrlEvent(boolean canPlay) {
            this.canPlay = canPlay;
        }

        public boolean canPlay = false;
    }

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

    public static class JumpHalfEditFragEvent {

        public JumpHalfEditFragEvent() {
        }
    }

    public static class JumpHalfFansFragEvent {

        public JumpHalfFansFragEvent() {
        }
    }

    public static class JumpHalfFollowsFragEvent {

        public JumpHalfFollowsFragEvent() {
        }
    }

    public static class JumpNoFocusChatThreadFragEvent {

        public JumpNoFocusChatThreadFragEvent() {
        }
    }

    public static class PersonalInfoChangeEvent {
        public boolean isAvatorChange;

        public PersonalInfoChangeEvent() {
        }
    }

    public static class HighLevelUserActionEvent {
        public BarrageMsg enterLiveBarrage;

        public HighLevelUserActionEvent(BarrageMsg enterLiveBarrage) {
            this.enterLiveBarrage = enterLiveBarrage;
        }
    }

    /**
     * 通知该房间是否允许播放VIP进场特效
     */
    public static class UpdateVipEnterRoomEffectSwitchEvent {
        //该房间是否禁止播放VIP进场特效， 0 不禁止， 1 禁止
        public static final int VIP_ENTER_ROOM_EFFECT_ALLOW = 0;
        public static final int VIP_ENTER_ROOM_EFFECT_FORBID = 1;
        public final long anchorId;
        public final boolean enableEffect;

        private UpdateVipEnterRoomEffectSwitchEvent(long anchorId, boolean enableEffect) {
            this.anchorId = anchorId;
            this.enableEffect = enableEffect;
        }

        public static UpdateVipEnterRoomEffectSwitchEvent newInstance(long anchorId, int noJoinAnimation) {
            return new UpdateVipEnterRoomEffectSwitchEvent(anchorId, noJoinAnimation == VIP_ENTER_ROOM_EFFECT_ALLOW);
        }
    }

    public static final class AddBarrageEvent {
        public final BarrageMsg barrageMsg;

        private AddBarrageEvent(BarrageMsg barrageMsg) {
            this.barrageMsg = barrageMsg;
        }

        public static AddBarrageEvent newInstance(@NonNull BarrageMsg barrageMsg) {
            return new AddBarrageEvent(barrageMsg);
        }

    }

    public static final class UpdateFastGiftInfoEvent {
        public int giftId;
        public String widgetIcon;
        public String linkUrl;

        public UpdateFastGiftInfoEvent(int giftId, String widgetIcon, String linkUrl) {
            this.giftId = giftId;
            this.widgetIcon = widgetIcon;
            this.linkUrl = linkUrl;
        }
    }

    public static class H5FirstPayEvent {
        public int gooid;
        public int gemCnt;
        public int giveGemCnt;
        public int goodPrice;
        public int payType;
        public int channel;

        public H5FirstPayEvent(int gooid, int gemCnt, int giveGemCnt, int goodPrice, int payType, int channel) {
            this.gooid = gooid;
            this.gemCnt = gemCnt;
            this.giveGemCnt = giveGemCnt;
            this.goodPrice = goodPrice;
            this.payType = payType;
            this.channel = channel;
        }
    }

    public static class GameDownLoadEvent {
        public long gameId;
        public int status;
        public int progress;

        public static final int STATUS_NONE = 0;
        public static final int DOWNLOAD = 1; //下载
        public static final int DOWNLOAD_RUNNING = 2; //下载中
        public static final int GAME_INSTALL = 3;//安装
        public static final int GAME_LUNCH = 4;//启动

        public GameDownLoadEvent(long gameId, int status, int progress) {
            this.gameId = gameId;
            this.status = status;
            this.progress = progress;
        }

    }
}
