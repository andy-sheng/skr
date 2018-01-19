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
}
