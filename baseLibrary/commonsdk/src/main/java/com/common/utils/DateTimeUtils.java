package com.common.utils;

import android.content.Context;
import android.provider.Settings;

import com.common.base.R;
import com.common.log.MyLog;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * 通过 U.getDateTimeUtils() 调用
 */
public class DateTimeUtils {
    public final static String TAG = "DateTimeUtils";


    public static final String DATETIME_FORMAT_SECOND = "yyyy.MM.dd HH:mm:ss";
    public static final String DATETIME_FORMAT_LONG = "M/d/yyyy HH:mm";
    public static final String DATETIME_FORMAT_SHORT = "M/d HH:mm";
    public static final String DATE_FORMAT_STR = "yyyy.MM.dd";

    public static final String TIME_VALIDATION_EXPRESSION = "[0-2]*[0-9]:[0-5]*[0-9]";

    public static final long MILLIS_IN_WEEK = 7l * 24 * 60 * 60 * 1000;
    public static final long MILILIS_ONE_DAY = 24l * 60 * 60 * 1000;

    // 1970.1.1是周四。
    private static final long OFFSET_IN_WEEK = 3 * 24 * 60 * 60 * 1000;

    public static final long MILLI_SECONDS_ONE_MINUTE = 60 * 1000;        //一分钟的毫秒值
    public static final long MILLI_SECONDS_ONE_HOUR = 60 * MILLI_SECONDS_ONE_MINUTE;      //一小时的毫秒值
    public static final long MILLI_SECONDS_ONE_DAY = 24 * MILLI_SECONDS_ONE_HOUR;           //一天的毫秒值
    public static final long MILLI_SECONDS_ONE_MONTH = 30 * MILLI_SECONDS_ONE_DAY;          //一月的的毫秒值. 先假设一个月30天, 有bug, 没有考虑31天或者闰年的情况! 没办法, SB产品要的急!
    public static final long MILLI_SECONDS_ONE_YEAR = 24 * MILLI_SECONDS_ONE_MONTH;             //一年的毫秒值

    DateTimeUtils() {
    }

    public boolean isYesterday(long rowTime) {
        boolean bret = false;
        if (rowTime > 0) {

            Date date = new Date(rowTime);
            Calendar current = Calendar.getInstance();

            Calendar today = Calendar.getInstance(); // 今天
            int day = current.get(Calendar.DAY_OF_MONTH);
            int month = current.get(Calendar.MONTH);
            today.set(Calendar.YEAR, current.get(Calendar.YEAR));
            today.set(Calendar.MONTH, month);
            today.set(Calendar.DAY_OF_MONTH, day);
            // Calendar.HOUR——12小时制的小时数 Calendar.HOUR_OF_DAY——24小时制的小时数
            today.set(Calendar.HOUR_OF_DAY, 0);
            today.set(Calendar.MINUTE, 0);
            today.set(Calendar.SECOND, 0);
            long gap = today.getTimeInMillis() - rowTime;
            if (gap > 0 && gap < MILILIS_ONE_DAY) {
                bret = true;
            }
        }

        return bret;
    }

    public boolean isThisWeek(final long time) {
        return (System.currentTimeMillis() - time) < MILLIS_IN_WEEK;
    }

    public boolean isThisYear(final long time) {
        final Calendar today = Calendar.getInstance();
        today.setTime(new Date());

        final Calendar otherday = Calendar.getInstance();
        otherday.setTime(new Date(time));

        return otherday.get(Calendar.YEAR) == today.get(Calendar.YEAR);
    }

    /**
     * 格式化日期
     *
     * @param date 日期
     * @return 年-月-日
     */
    public String formatDateString(Date date) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        return format.format(date);
    }

    /**
     * 特殊的格式化日期，将日变为00
     *
     * @param date 日期
     * @return 年-月-00
     */
    public String formatSpecailDateString(Date date) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM");
        return format.format(date) + "-00";
    }

    /**
     * @param context
     * @param rowTime 某个时间戳
     * @return 1. 今天 HH:mm
     * 2. 明天 HH:mm
     * 3. 昨天 HH:mm
     * 4. xx月xx日
     */
    public String formatTimeStringForNotice(final Context context, final long rowTime) {
        long now = System.currentTimeMillis();
        long dayDiff = getDayDiff(now, rowTime);
        String time = formatTimeStringForDate(rowTime, "HH:mm");

        String result;
        if (dayDiff == 0) {
            result = context.getString(R.string.date_today) + " " + time;
        } else if (dayDiff == 1) {
            result = context.getString(R.string.date_tomorrow) + " " + time;
        } else if (dayDiff == -1) {
            result = context.getString(R.string.date_yesterday) + " " + time;
        } else {
            result = formatTimeStringForDate(rowTime, context.getString(R.string.times_mm_dd_format)) + " " + time;
        }
        return result;
    }

    public String formatTimeStringForDate(final long rowTime, final String formatStr) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(formatStr);
        return simpleDateFormat.format(new Date(rowTime));
    }

    public String formatDetailTimeStringNow() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return simpleDateFormat.format(new Date(System.currentTimeMillis()));
    }

    /**
     * 12:03:23.122
     *
     * @param rowTime
     * @return
     */
    public String formatTimeStringForDate(final long rowTime) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss.SSS");
        return simpleDateFormat.format(new Date(rowTime));
    }

    /**
     * 格式化时间
     *
     * @param videoTime 默认是毫秒单位，视频的时间
     * @return 返回结果：
     * 小于1小时的 00:00
     * 大于1小时的 00:00:00
     */
    public String formatVideoTime(long videoTime) {
        MyLog.d(TAG, "formatVideoTime, videoTime = " + videoTime);

        int seconds = 0;
        int minutes = 0;
        int hours = 0;
        if (videoTime < 0) {
            videoTime = 0;
        }
        videoTime /= 1000;

        seconds = (int) (videoTime % 60);
        String result = String.format("%02d", seconds);

        videoTime /= 60;
        minutes = (int) (videoTime % 60);
        result = String.format("%02d", minutes) + ":" + result;

        videoTime /= 60;
        if (videoTime > 0) {
            if (videoTime <= Integer.MAX_VALUE) {
                hours = (int) videoTime;
                result = String.format("%02d", hours) + ":" + result;
            } else {
                MyLog.d(TAG, "formatVideoTime : time over limit, videoTime = " + videoTime);
            }
        }
        return result;
    }

    /**
     * 返回一个时间　相对于另一个时间的可读的　时间表示
     * （1）       60分钟内，显示时间：XX分钟前，例：6分钟前、12分钟前
     * （2）       1天内，显示时间：XX小时前，例：1小时前
     * （3）       1个月内，显示时间：XX天前，例：2天前（特例：超过24小时，显示“昨天”）
     * （4）       1年内，显示时间：XX月前，例：3个月前
     * （5）       超过1年，显示时间：2016年5月12日
     *
     * @param timeToFormat 　要转换的时间
     * @param timeToBase   　基准时间
     * @return
     */
    public String formatHumanableDate(final long timeToFormat, final long timeToBase) {
        MyLog.v(TAG + " formatHumanableDate timeToFormat == " + timeToFormat + " timeToBase == " + timeToBase);
        if (timeToFormat < 0 || timeToBase < 0) {
            MyLog.e(TAG + " formatHumanableDate timeToFormat or timeToBase < 0, timeToFormat == " + timeToFormat + " timeToBase == " + timeToBase);
            return "";
        }

        long timeSpan = Math.abs(timeToBase - timeToFormat);
        MyLog.v(TAG + " formatHumanableDate timeSpan == " + timeSpan);
        if (timeSpan < MILLI_SECONDS_ONE_MINUTE) {        //一分钟之内, 显示刚刚
            return U.app().getResources().getString(R.string.justnow);
        } else if (timeSpan < MILLI_SECONDS_ONE_HOUR) {        //一小时之内, 显示　多少分钟前
            long tmp = timeSpan / MILLI_SECONDS_ONE_MINUTE;
            return U.app().getResources().getQuantityString(R.plurals.minute_ago, (int) tmp, tmp);
        } else if (timeSpan < MILLI_SECONDS_ONE_DAY) {         //一天之内, 显示　多少小时前
            long tmp = timeSpan / MILLI_SECONDS_ONE_HOUR;
            return U.app().getResources().getQuantityString(R.plurals.hour_ago, (int) tmp, tmp);
        } else if (timeSpan < MILLI_SECONDS_ONE_MONTH) {       //一个月之内, 显示　多少天前
            float value = (float) timeSpan / (float) MILLI_SECONDS_ONE_DAY;
            //这里是为了解决跨天的问题, 比如value为2.55天有可能跨天了
            Calendar calendar = Calendar.getInstance();
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            float hourPercent = (float) hour / (float) 24;
            value += hourPercent;
            int tmp = (int) value;
            return U.app().getResources().getQuantityString(R.plurals.days_ago, tmp, tmp);
        } else if (timeSpan < MILLI_SECONDS_ONE_YEAR) {       //一年之内, 显示　几个月前
            long value = timeSpan / MILLI_SECONDS_ONE_MONTH;
            return U.app().getResources().getQuantityString(R.plurals.months_ago, (int) value, value);
        } else {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(new Date(timeToFormat));
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            return String.format(U.app().getResources().getString(R.string.year_month_day), year, (month + 1), day);
        }
    }

    /**
     * 返回一个时间　相对于另一个时间的可读的　时间表示
     * （1）       60分钟内，显示时间：XX分钟前，例：6分钟前、12分钟前
     * （2）       1天内，显示时间：XX小时前，例：1小时前
     * （3）       15天内，显示时间：XX天前，例：2天前（特例：超过24小时，显示“昨天”）
     * (4)       超过15天，返回空
     *
     * @param timeToFormat 　要转换的时间
     * @param timeToBase   　基准时间
     * @return
     */
    public String formatHumanableDateForSkr(final long timeToFormat, final long timeToBase) {
        MyLog.v(TAG + " formatHumanableDate timeToFormat == " + timeToFormat + " timeToBase == " + timeToBase);
        if (timeToFormat < 0 || timeToBase < 0) {
            MyLog.e(TAG + " formatHumanableDate timeToFormat or timeToBase < 0, timeToFormat == " + timeToFormat + " timeToBase == " + timeToBase);
            return "";
        }

        long timeSpan = Math.abs(timeToBase - timeToFormat);
        MyLog.v(TAG + " formatHumanableDate timeSpan == " + timeSpan);
        if (timeSpan < MILLI_SECONDS_ONE_MINUTE) {        //一分钟之内, 显示刚刚
            return U.app().getResources().getString(R.string.justnow);
        } else if (timeSpan < MILLI_SECONDS_ONE_HOUR) {        //一小时之内, 显示　多少分钟前
            long tmp = timeSpan / MILLI_SECONDS_ONE_MINUTE;
            return U.app().getResources().getQuantityString(R.plurals.minute_ago, (int) tmp, tmp);
        } else if (timeSpan < MILLI_SECONDS_ONE_DAY) {         //一天之内, 显示　多少小时前
            long tmp = timeSpan / MILLI_SECONDS_ONE_HOUR;
            return U.app().getResources().getQuantityString(R.plurals.hour_ago, (int) tmp, tmp);
        } else if (timeSpan < 4 * MILLI_SECONDS_ONE_DAY) {       //一个月之内, 显示　多少天前
            float value = (float) timeSpan / (float) MILLI_SECONDS_ONE_DAY;
            //这里是为了解决跨天的问题, 比如value为2.55天有可能跨天了
            Calendar calendar = Calendar.getInstance();
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            float hourPercent = (float) hour / (float) 24;
            value += hourPercent;
            int tmp = (int) value;
            return U.app().getResources().getQuantityString(R.plurals.days_ago, tmp, tmp);
        } else {
            return "";
        }
    }

    /**
     * 返回时间戳相差几天 今天返回0 昨天返回-1 明天返回1
     *
     * @param TSL
     * @param TSR
     * @return
     */
    public long getDayDiff(long TSL, long TSR) {
        return adjustTimeZoneMillis(TSR) / MILILIS_ONE_DAY - adjustTimeZoneMillis(TSL) / MILILIS_ONE_DAY;
    }

    /**
     * 用于补足时区
     *
     * @notice Date，Calender类已经添加时区，不需要调用此函数矫正，只提供给getDayDiff使用
     */
    private long adjustTimeZoneMillis(long millis) {
        Calendar cal = Calendar.getInstance();
        int zoneOffset = cal.get(Calendar.ZONE_OFFSET);
        int dstOffset = cal.get(Calendar.DST_OFFSET);
        return millis + (zoneOffset + dstOffset);
    }

    private final static int[] dayArr = new int[]{20, 19, 21, 20, 21, 22, 23, 23, 23, 24, 23, 22};
    private final static String[] constellationArr = new String[]{"摩羯座", "水瓶座", "双鱼座", "白羊座", "金牛座", "双子座", "巨蟹座", "狮子座", "处女座", "天秤座", "天蝎座", "射手座", "摩羯座"};

    /**
     * 得到星座
     *
     * @param month
     * @param day
     * @return
     */
    public String getConstellation(int month, int day) {
        if (month <= 0 || month > 12) {
            return "";
        }
        if (day <= 0 || day > 31) {
            return "";
        }
        return day < dayArr[month - 1] ? constellationArr[month - 1] : constellationArr[month];
    }


    /**
     * 融云时间计算方式
     *
     * @param dateMillis
     * @param showTime
     * @param context
     * @return
     */
    public String getDateTimeString(long dateMillis, boolean showTime, Context context) {
        if (dateMillis <= 0L) {
            return "";
        } else {
            String formatDate = null;
            Date date = new Date(dateMillis);
            int type = judgeDate(date);
            long time = java.lang.System.currentTimeMillis();
            Calendar calendarCur = Calendar.getInstance();
            Calendar calendardate = Calendar.getInstance();
            calendardate.setTimeInMillis(dateMillis);
            calendarCur.setTimeInMillis(time);
            int month = calendardate.get(Calendar.MONTH);
            int year = calendardate.get(Calendar.YEAR);
            int weekInMonth = calendardate.get(Calendar.WEEK_OF_MONTH);
            int monthCur = calendarCur.get(Calendar.MONTH);
            int yearCur = calendarCur.get(Calendar.YEAR);
            int weekInMonthCur = calendarCur.get(Calendar.WEEK_OF_MONTH);
            switch (type) {
                case 6:
                    formatDate = getTimeString(dateMillis, context);
                    break;
                case 15:
                    String formatString = "昨天";
                    if (showTime) {
                        formatDate = formatString + " " + getTimeString(dateMillis, context);
                    } else {
                        formatDate = formatString;
                    }
                    break;
                case 2014:
                    if (year == yearCur) {
                        if (month == monthCur && weekInMonth == weekInMonthCur) {
                            formatDate = getWeekDay(context, calendardate.get(Calendar.DAY_OF_WEEK));
                        } else if (context.getResources().getConfiguration().locale.getCountry().equals("CN")) {
                            formatDate = formatDate(date, "M月d日");
                        } else {
                            formatDate = formatDate(date, "M/d");
                        }
                    } else if (context.getResources().getConfiguration().locale.getCountry().equals("CN")) {
                        formatDate = formatDate(date, "yyyy年M月d日");
                    } else {
                        formatDate = formatDate(date, "M/d/yy");
                    }

                    if (showTime) {
                        formatDate = formatDate + " " + getTimeString(dateMillis, context);
                    }
            }

            return formatDate;
        }
    }

    public String formatDate(Date date, String fromat) {
        SimpleDateFormat sdf = new SimpleDateFormat(fromat);
        return sdf.format(date);
    }

    private int judgeDate(Date date) {
        Calendar calendarToday = Calendar.getInstance();
        calendarToday.set(Calendar.HOUR_OF_DAY, 0);
        calendarToday.set(Calendar.MINUTE, 0);
        calendarToday.set(Calendar.SECOND, 0);
        calendarToday.set(Calendar.MILLISECOND, 0);
        Calendar calendarYesterday = Calendar.getInstance();
        calendarYesterday.add(Calendar.DATE, -1);
        calendarYesterday.set(Calendar.HOUR_OF_DAY, 0);
        calendarYesterday.set(Calendar.MINUTE, 0);
        calendarYesterday.set(Calendar.SECOND, 0);
        calendarYesterday.set(Calendar.MILLISECOND, 0);
        Calendar calendarTomorrow = Calendar.getInstance();
        calendarTomorrow.add(Calendar.DATE, 1);
        calendarTomorrow.set(Calendar.HOUR_OF_DAY, 0);
        calendarTomorrow.set(Calendar.MINUTE, 0);
        calendarTomorrow.set(Calendar.SECOND, 0);
        calendarTomorrow.set(Calendar.MILLISECOND, 0);
        Calendar calendarTarget = Calendar.getInstance();
        calendarTarget.setTime(date);
        if (calendarTarget.before(calendarYesterday)) {
            return 2014;
        } else if (calendarTarget.before(calendarToday)) {
            return 15;
        } else {
            return calendarTarget.before(calendarTomorrow) ? 6 : 2014;
        }
    }

    private String getTimeString(long dateMillis, Context context) {
        if (dateMillis <= 0L) {
            return "";
        } else {
            Date date = new Date(dateMillis);
            String formatTime = null;
            if (isTime24Hour(context)) {
                formatTime = formatDate(date, "HH:mm");
            } else {
                Calendar calendarTime = Calendar.getInstance();
                calendarTime.setTimeInMillis(dateMillis);
                int hour = calendarTime.get(Calendar.HOUR);
                if (calendarTime.get(Calendar.AM_PM) == 0) {
                    if (hour < 6) {
                        if (hour == 0) {
                            hour = 12;
                        }

                        formatTime = "凌晨";
                    } else if (hour >= 6 && hour < 12) {
                        formatTime = "上午";
                    }
                } else if (hour == 0) {
                    formatTime = "中午";
                    hour = 12;
                } else if (hour >= 1 && hour <= 5) {
                    formatTime = "下午";
                } else if (hour >= 6 && hour <= 11) {
                    formatTime = "晚上";
                }

                int minuteInt = calendarTime.get(Calendar.MINUTE);
                String minuteStr = Integer.toString(minuteInt);
                String timeStr = null;
                if (minuteInt < 10) {
                    minuteStr = "0" + minuteStr;
                }

                timeStr = Integer.toString(hour) + ":" + minuteStr;
                if (context.getResources().getConfiguration().locale.getCountry().equals("CN")) {
                    formatTime = formatTime + timeStr;
                } else {
                    formatTime = timeStr + " " + formatTime;
                }
            }

            return formatTime;
        }
    }

    public static boolean isTime24Hour(Context context) {
        String timeFormat = Settings.System.getString(context.getContentResolver(), Settings.System.TIME_12_24);
        return timeFormat != null && timeFormat.equals("24");
    }

    private static String getWeekDay(Context context, int dayInWeek) {
        String weekDay = "";
        switch (dayInWeek) {
            case 1:
                weekDay = "星期天";
                break;
            case 2:
                weekDay = "星期一";
                break;
            case 3:
                weekDay = "星期二";
                break;
            case 4:
                weekDay = "星期三";
                break;
            case 5:
                weekDay = "星期四";
                break;
            case 6:
                weekDay = "星期五";
                break;
            case 7:
                weekDay = "星期六";
        }

        return weekDay;
    }

}
