package com.mi.liveassistant.room.user.callback;

import com.mi.liveassistant.data.User;

/**
 * Created by lan on 17/5/2.
 */
public interface IUserCallback {
    void notifyFail(int errCode);

    void notifySuccess(User user);
}
