package com.mi.live.data.event;

import com.mi.live.data.manager.model.LiveRoomManagerModel;

import java.util.List;

/**
 * Created by yurui on 2016/12/1.
 */

public class LiveRoomManagerEvent {
    public boolean reqResult;
    public boolean managerEnable;
    public List<LiveRoomManagerModel> managerList;
    public boolean showToast = true;

    public LiveRoomManagerEvent(List<LiveRoomManagerModel> managerList, boolean reqResult, boolean managerEnable) {
        this.managerList = managerList;
        this.reqResult = reqResult;
        this.managerEnable = managerEnable;
    }

    public LiveRoomManagerEvent(List<LiveRoomManagerModel> managerList, boolean reqResult, boolean managerEnable, boolean showToast) {
        this.managerList = managerList;
        this.reqResult = reqResult;
        this.managerEnable = managerEnable;
        this.showToast = showToast;
    }
}
