package com.common.core.userinfo.noremind;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Generated;

@Entity
public class NoRemindInfoDB {
    @Id
    private Long userId;

    private int msgType;

    @Generated(hash = 1521383658)
    public NoRemindInfoDB(Long userId, int msgType) {
        this.userId = userId;
        this.msgType = msgType;
    }

    @Generated(hash = 2074774081)
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
