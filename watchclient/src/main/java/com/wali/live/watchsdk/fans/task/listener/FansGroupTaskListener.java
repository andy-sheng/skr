package com.wali.live.watchsdk.fans.task.listener;

import com.wali.live.proto.VFansCommonProto;

/**
 * Created by lan on 2017/11/17.
 */
public interface FansGroupTaskListener {
    void finishTask(VFansCommonProto.GroupJobType jobType);
}
