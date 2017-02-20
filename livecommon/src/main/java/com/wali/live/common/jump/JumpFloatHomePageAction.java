package com.wali.live.common.jump;

import com.base.activity.BaseActivity;
import com.mi.live.data.room.model.RoomBaseDataModel;
import com.wali.live.common.jump.model.JumpFloatHomePageBean;

/**
 * Created by chengsimin on 16/9/18.
 */
public interface JumpFloatHomePageAction {
    void setContext(BaseActivity activity);

    void setRoomData(RoomBaseDataModel roomData);

    void jumpFloatHomePage(JumpFloatHomePageBean bean);

}
