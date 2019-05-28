package com.module.playways.room.prepare.model;

import java.io.Serializable;

public enum ERoundOverReasonModel implements Serializable {
    /**
     * 未知
     */
    EROR_UNKNOWN(0),

    /**
     * 正常
     */
    EROR_NORMAL(1),

    /**
     * 玩家退出
     */
    EROR_ON_ROUND_USER_EXIT(2),

    /**
     * 足够多灭灯
     */
    EROR_ENOUGH_M_LIGHT(3);

    private final int value;

    ERoundOverReasonModel(int value) {
        this.value = value;
    }

    public static ERoundOverReasonModel valueOf(int value) {    //    手写的从int到enum的转换函数
        switch (value) {
            case 0:
                return EROR_UNKNOWN;
            case 1:
                return EROR_NORMAL;
            case 2:
                return EROR_ON_ROUND_USER_EXIT;
            case 3:
                return EROR_ENOUGH_M_LIGHT;
            default:
                return EROR_UNKNOWN;
        }
    }
}
