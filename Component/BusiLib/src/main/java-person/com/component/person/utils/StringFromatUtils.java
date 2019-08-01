package com.component.person.utils;

public class StringFromatUtils {

    /**
     * 格式化数字  > 10000  1.2w
     *
     * @param num
     * @return
     */
    public static String formatTenThousand(int num){
        if (num < 10000) {
            return String.valueOf(num);
        } else {
            float result = (float) (Math.round(((float) num / 10000) * 10)) / 10;
            return String.valueOf(result) + "w";
        }
    }

    /**
     * 格式化数字  > 1000000 100.1w
     * @param charmNum
     * @return
     */
    public static String formatMillion(int charmNum){
        if (charmNum < 1000000) {
            return String.valueOf(charmNum);
        } else {
            float result = (float) (Math.round(((float) charmNum / 10000) * 10)) / 10;
            return String.valueOf(result) + "w";
        }
    }
}
