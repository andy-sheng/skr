package com.wali.live.watchsdk.watch;

import com.mi.live.data.room.model.RoomBaseDataModel;
import com.wali.live.common.gift.presenter.GiftMallPresenter;
import com.wali.live.watchsdk.component.WatchComponentController;
import com.wali.live.watchsdk.watch.model.RoomInfo;

/**
 * 规定了 WatchSdkActivityInterface 对外暴露的数据接口
 */
public interface WatchSdkActivityInterface {
    RoomInfo getRoomInfo();

    RoomBaseDataModel getRoomBaseData();

    WatchComponentController getController();

    GiftMallPresenter getGiftMallPresenter();
}
