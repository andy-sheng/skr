package com.wali.live.watchsdk.videodetail;

import android.support.annotation.Nullable;

import com.mi.live.data.room.model.RoomBaseDataModel;
import com.wali.live.component.ComponentController;

/**
 * Created by yangli on 2017/5/26.
 */
public class VideoDetailController extends ComponentController {

    protected RoomBaseDataModel mMyRoomData;

    @Nullable
    @Override
    protected String getTAG() {
        return "VideoDetailController";
    }

    public VideoDetailController(RoomBaseDataModel myRoomData) {
        mMyRoomData = myRoomData;
    }
}
