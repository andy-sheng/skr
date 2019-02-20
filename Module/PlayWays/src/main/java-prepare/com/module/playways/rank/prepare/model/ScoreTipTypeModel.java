package com.module.playways.rank.prepare.model;

import java.io.Serializable;

public enum  ScoreTipTypeModel implements Serializable {
    /**
     * 未知
     */
    ST_UNKNOWN(0),

    /**
     * 丢人了
     */
    ST_TOO_BAD(1),

    /**
     * 不错哦
     */
    ST_NOT_BAD(2),

    /**
     * 太棒了
     */
    ST_VERY_GOOD(3),

    /**
     * 超完美
     */
    ST_NICE_PERFECT(4);

    private final int value;

    ScoreTipTypeModel(int value) {
        this.value = value;
    }
}
