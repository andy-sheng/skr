package com.wali.live.modulewatch.barrage.model.barrage;

/**
 * Created by chengsimin on 16/6/14.
 */
public class SystemMsgModel {
    private long fromUser;
    private String content;
    private long startTime;
    private long endTime;

    public void setFromUser(long fromUser) {
        this.fromUser = fromUser;
    }

    public long getFromUser() {
        return fromUser;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getContent() {
        return content;
    }

    public void setStartTime(long startTime) {

        this.startTime = startTime;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public long getEndTime() {
        return endTime;
    }
}
