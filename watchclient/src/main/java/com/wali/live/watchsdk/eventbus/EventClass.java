package com.wali.live.watchsdk.eventbus;

import com.wali.live.watchsdk.fans.model.GroupDetailModel;

/**
 * Created by zyh on 2017/11/9.
 */

public class EventClass {
    public static class VfansDetailLoadResult {
        public long zuid;
        public GroupDetailModel groupDetailModel;

        public VfansDetailLoadResult(long zuid, GroupDetailModel groupDetailModel) {
            this.zuid = zuid;
            this.groupDetailModel = groupDetailModel;
        }
    }
}
