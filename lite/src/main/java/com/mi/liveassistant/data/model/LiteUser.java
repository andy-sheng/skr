package com.mi.liveassistant.data.model;

import android.support.annotation.NonNull;

import com.mi.liveassistant.data.confuse.User;

/**
 * Created by lan on 2017/5/16.
 */
public class LiteUser {
    private User mInnerUser;

    public LiteUser(@NonNull User user) throws NullPointerException {
        if (user == null) {
            throw new NullPointerException();
        }
        mInnerUser = user;
    }

    public User getInnerUser() {
        return mInnerUser;
    }

    public long getUid() {
        return mInnerUser.getUid();
    }

    public long getAvatar() {
        return mInnerUser.getAvatar();
    }

    public String getNickname() {
        return mInnerUser.getNickname();
    }

    public String getSign() {
        return mInnerUser.getSign();
    }

    public int getGender() {
        return mInnerUser.getGender();
    }

    public int getLevel() {
        return mInnerUser.getLevel();
    }

    public boolean isFocused() {
        return mInnerUser.isFocused();
    }

    public boolean isBlock() {
        return mInnerUser.isBlock();
    }

    public boolean isBothwayFollowing() {
        return mInnerUser.isBothwayFollowing();
    }
}
