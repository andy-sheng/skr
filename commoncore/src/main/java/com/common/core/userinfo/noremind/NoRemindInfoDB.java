package com.common.core.userinfo.noremind;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Keep;

@Entity
public class NoRemindInfoDB {
    @Id
    private Long userId;

    private int msgType;

    @Keep
    public NoRemindInfoDB(Long userId, int msgType) {
        this.userId = userId;
        this.msgType = msgType;
    }

    @Keep
    public NoRemindInfoDB() {
    }

    public Long getUserId() {
        return this.userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public int getMsgType() {
        return this.msgType;
    }

    public void setMsgType(int msgType) {
        this.msgType = msgType;
    }

}
