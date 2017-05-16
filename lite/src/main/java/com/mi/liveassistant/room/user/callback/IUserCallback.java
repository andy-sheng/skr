package com.mi.liveassistant.room.user.callback;

import com.mi.liveassistant.data.model.LiteUser;

/**
 * Created by lan on 17/5/2.
 */
public interface IUserCallback {
    void notifyFail(int errCode);

    void notifySuccess(LiteUser user);
}
