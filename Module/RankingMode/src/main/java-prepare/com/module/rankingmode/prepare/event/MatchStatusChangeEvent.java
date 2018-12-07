package com.module.rankingmode.prepare.event;

public class MatchStatusChangeEvent {
    public static final int MATCH_STATUS_START = 1; //开始匹配
    public static final int MATCH_STATUS_MATCHING = 3; //匹配中
    public static final int MATCH_STATUS_MATCH_SUCESS = 4; //匹配成功

    public int status; //匹配状态

    public MatchStatusChangeEvent(int status) {
        this.status = status;
    }
}
