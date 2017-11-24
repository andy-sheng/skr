package com.wali.live.watchsdk.eventbus;

import com.wali.live.watchsdk.fans.adapter.FansMemberAdapter;
import com.wali.live.watchsdk.fans.model.FansGroupDetailModel;

import java.util.List;

/**
 * Created by zyh on 2017/11/9.
 */
public class EventClass {
    public static class VfansDetailLoadResult {
        public long zuid;
        public FansGroupDetailModel groupDetailModel;

        public VfansDetailLoadResult(long zuid, FansGroupDetailModel groupDetailModel) {
            this.zuid = zuid;
            this.groupDetailModel = groupDetailModel;
        }
    }

    public static class UpdateMemberListEvent {
        public int loadingStart;
        public List<FansMemberAdapter.MemberItem> memberItems;

        public UpdateMemberListEvent(List<FansMemberAdapter.MemberItem> memberItems) {
            this.memberItems = memberItems;
            this.loadingStart = memberItems != null ? memberItems.size() : 0;
        }
    }
}
