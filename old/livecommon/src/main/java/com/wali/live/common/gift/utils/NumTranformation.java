package com.wali.live.common.gift.utils;

/**
 * Created by zjn on 16-10-17.
 *
 * @module 礼物
 */
public class NumTranformation {

    /**
     * 如果输入小数点后为0 返回整数部分否则返回原来数值
     * @param values
     * @return
     */
    public static String getShowValues(float values) {
        //当前输入为0
        if (Float.compare(values, 0) == 0) {
            return String.valueOf((int) values);
        } else if(Float.compare(values - ((int) values), 0) == 0) {
            //输入的float如4.0,5.0
            return String.valueOf((int) values);
        }

        return String.valueOf(values);
    }
}
