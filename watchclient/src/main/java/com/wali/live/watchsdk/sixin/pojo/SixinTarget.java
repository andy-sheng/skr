package com.wali.live.watchsdk.sixin.pojo;

import com.mi.live.data.user.User;

import java.io.Serializable;

/**
 * Created by lan on 2017/10/30.
 */
public class SixinTarget implements Serializable {
    private User mTargetUser;

    private int mFocusState;
    private int mTargetType;  //0：单聊 1：群聊  2：宠爱团

    public SixinTarget(User target) {
        mTargetUser = target;
    }

    public SixinTarget(User target, int focusState, int targetType) {
        this(target);
        mFocusState = focusState;
        mTargetType = targetType;
    }

    public void setFocusState(int focusState) {
        mFocusState = focusState;
    }

    public void setTargetType(int targetType) {
        mTargetType = targetType;
    }

    public User getTargetUser() {
        return mTargetUser;
    }

    public long getUid() {
        return mTargetUser.getUid();
    }

    public String getNickname() {
        return mTargetUser.getNickname();
    }

    public int getCertificationType() {
        return mTargetUser.getCertificationType();
    }

    public int getFocusState() {
        return mFocusState;
    }

    public int getTargetType() {
        return mTargetType;
    }
}
