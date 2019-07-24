package com.component.person.utils;

public class StringFromatUtils {

    public static String formatRank(int rankSeq) {
        if (rankSeq < 10000) {
            return String.valueOf(rankSeq);
        } else {
            float result = (float) (Math.round(((float) rankSeq / 10000) * 10)) / 10;
            return String.valueOf(result) + "w";
        }
    }
    public static String formatFansNum(int fansNum){
        if (fansNum < 10000) {
            return String.valueOf(fansNum);
        } else {
            float result = (float) (Math.round(((float) fansNum / 10000) * 10)) / 10;
            return String.valueOf(result) + "w";
        }
    }

    public static String formatCharmNum(int charmNum){
        if (charmNum < 1000000) {
            return String.valueOf(charmNum);
        } else {
            float result = (float) (Math.round(((float) charmNum / 10000) * 10)) / 10;
            return String.valueOf(result) + "w";
        }
    }

}
