package com.engine.statistics;


import java.text.SimpleDateFormat;
import java.util.Date;

public class SUtils
{
    private static SimpleDateFormat sFmt = null;
    private static Date sDate = null;

    private SUtils(){

    }

    public static String transTime(long ms) {
        if (null == sFmt) {
            sFmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        }
        if (null == sDate) {
            sDate = new Date();
        }

        sDate.setTime(ms);
        String dataMSStr = sFmt.format(sDate);
        return dataMSStr;
    }

}