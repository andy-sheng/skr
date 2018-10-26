package com.wali.live.modulewatch.watch.assist;


import com.wali.live.modulewatch.watch.model.roominfo.RoomBaseDataModel;
import com.wali.live.modulewatch.watch.model.roominfo.RoomInfo;
import com.wali.live.modulewatch.watch.normal.WatchComponentController;

/**
 * 规定了 WatchSdkActivityInterface 对外暴露的数据接口
 */
public interface WatchSdkActivityInterface {
    RoomInfo getRoomInfo();

    RoomBaseDataModel getRoomBaseData();

    WatchComponentController getController();

//    GiftMallPresenter getGiftMallPresenter();

    boolean isDisplayLandscape();
}
