package com.wali.live.watchsdk.eventbus;

import com.wali.live.watchsdk.fans.model.FansGroupDetailModel;

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
}
