package com.mi.live.data.data;

import java.util.Date;

/**
 * 上次发送的弹幕信息
 *
 * Created by wuxiaoshan on 16-7-9.
 */
public class LastBarrage {

    private long lastSendTime;

    private long createTime;

    private String lastSendContent;

    public long getLastSendTime() {
        return lastSendTime;
    }

    public LastBarrage(Date date){
        createTime = date.getTime();
    }

    public void setLastSendTime(long lastSendTime) {
        this.lastSendTime = lastSendTime;
    }

    public String getLastSendContent() {
        return lastSendContent;
    }

    public void setLastSendContent(String lastSendContent) {
        this.lastSendContent = lastSendContent;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }
}
