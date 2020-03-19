package com.common.core.userinfo;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Generated;

@Entity
public class NoRemindInfoDB {
    @Id
    private Long userId;

    @Generated(hash = 736398147)
    public NoRemindInfoDB(Long userId) {
        this.userId = userId;
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


}
