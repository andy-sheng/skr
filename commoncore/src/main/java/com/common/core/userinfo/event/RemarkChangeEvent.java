package com.common.core.userinfo.event;

public class RemarkChangeEvent {
    public int userId;
    public String remark;

    public RemarkChangeEvent(int userId, String remark) {
        this.userId = userId;
        this.remark = remark;
    }

    @Override
    public String toString() {
        return "RemarkChangeEvent{" +
                "userId=" + userId +
                ", remark='" + remark + '\'' +
                '}';
    }
}
