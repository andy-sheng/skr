package com.wali.live.watchsdk.eventbus;

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

}
