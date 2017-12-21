package com.wali.live.watchsdk.eventbus;

import com.wali.live.watchsdk.fans.adapter.FansMemberAdapter;
import com.wali.live.watchsdk.fans.model.FansGroupDetailModel;

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
}
