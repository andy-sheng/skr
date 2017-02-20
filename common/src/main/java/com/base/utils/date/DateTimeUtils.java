package com.base.utils.date;

import android.content.Context;
import android.text.format.DateFormat;
import android.text.format.DateUtils;

import com.base.common.R;
import com.base.global.GlobalData;
import com.base.log.MyLog;
import com.base.utils.CommonUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by MK on 15-4-13.
 */
public class DateTimeUtils {
    private static final String TAG = DateTimeUtils.class.getSimpleName();

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

    public static String getRelativeDateTimeString(final Context c, final long time) {
        String timeStr = getRelativeDateTimeString(c, time, DateUtils.MINUTE_IN_MILLIS / 2,
                DateUtils.HOUR_IN_MILLIS);
        return timeStr != null ? timeStr.replace(" ", "") : timeStr;
    }

    public static String getRelativeDateTimeString(final Context c, final long time, final long minResolution, final long transitionResolution) {
        final long now = System.currentTimeMillis();
        final long d = now - time;
        if ((d >= 0) && (d < minResolution)) {
            return DateUtils.getRelativeTimeSpanString(time, now, 0).toString();
        } else if ((d >= 0) && (d < transitionResolution)) {
            return DateUtils.getRelativeTimeSpanString(time, now, minResolution).toString();
        } else {
            return DateUtils.getRelativeTimeSpanString(c, time, false).toString();
        }
    }

    public static boolean isYesterday(long rowTime) {
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

    public static String formatTimeString(final Context context, final long rowTime) {
        return formatTimeString(context, rowTime, true, true);
    }

    public static String formatTimeString(final Context context, final long rowTime, final boolean hasYear, final boolean isHasDetailTime) {

        return formatTimeString(context, rowTime, hasYear, isHasDetailTime, true);
    }

    public static String formatTimeString(final Context context, final long rowTime,
                                          final boolean hasYear, final boolean isHasDetailTime, final boolean isShowThisWeek) {
        final int sixty_seconds = 60 * 1000;
        final long d = System.currentTimeMillis() - rowTime;
        if ((d > 0) && (d < DateUtils.HOUR_IN_MILLIS)) {
            if (d <= sixty_seconds) {
                return context.getString(R.string.within_s_secs);
            } else {
                return context.getString(R.string.within_1_hour, (long) Math
                        .abs(d / DateUtils.MINUTE_IN_MILLIS));
            }
        } else {
            // 如果是今天，显示24小时制时间；如果和当前时间在同周内，显示周几；否则显示日期。
            final int format12Or24 = CommonUtils.isChineseLocale(context) ? DateUtils.FORMAT_24HOUR
                    : DateUtils.FORMAT_12HOUR;
            if (DateUtils.isToday(rowTime)) {
                // 国际版显示 12 小时制 am/pm
                return DateUtils.formatDateTime(context, rowTime, DateUtils.FORMAT_SHOW_TIME
                        | format12Or24);
            }

            if (isYesterday(rowTime)) {
                return context.getString(R.string.yesterday_time,
                        DateUtils.formatDateTime(context, rowTime, DateUtils.FORMAT_SHOW_TIME
                                | format12Or24));
            } else if (isShowThisWeek && isThisWeek(rowTime)) {
                if (isHasDetailTime) {//星期几＋hh:mm
                    return DateUtils.formatDateTime(context, rowTime, DateUtils.FORMAT_SHOW_WEEKDAY | DateUtils.FORMAT_SHOW_TIME);
                }
                //星期几
                return DateUtils.formatDateTime(context, rowTime, DateUtils.FORMAT_SHOW_WEEKDAY);
            } else {
                if (hasYear) {
                    return DateUtils.formatDateTime(context, rowTime, DateUtils.FORMAT_SHOW_DATE);
                } else {
                    return DateUtils.formatDateTime(context, rowTime, DateUtils.FORMAT_NO_YEAR);
                }
            }
        }
    }

    private static SimpleDateFormat twentyFourhourDateFormat = new SimpleDateFormat("HH:mm");


    public static boolean isToday(final Date date) {
        final Calendar today = Calendar.getInstance();
        today.setTime(new Date());

        final Calendar otherday = Calendar.getInstance();
        otherday.setTime(date);

        return (otherday.get(Calendar.YEAR) == today.get(Calendar.YEAR))
                && (otherday.get(Calendar.MONTH) == today.get(Calendar.MONTH))
                && (otherday.get(Calendar.DAY_OF_MONTH) == today.get(Calendar.DAY_OF_MONTH));
    }

    public static int getHour() {
        final Calendar today = Calendar.getInstance();
        return today.get(Calendar.HOUR_OF_DAY);
    }

    /**
     * 判断当前时间是不是落在一个时间段内
     *
     * @param startTime 开始时间，hh：mm格式
     * @param endTime   结束时间，hh：mm格式
     * @return
     */
    public static boolean isBetweenTime(final String startTime, final String endTime) {
        if (!startTime.matches(TIME_VALIDATION_EXPRESSION)
                || !endTime.matches(TIME_VALIDATION_EXPRESSION)) {
            return false;
        }
        final int startHour = Integer.parseInt(startTime.split(":")[0]);
        final int startMin = Integer.parseInt(startTime.split(":")[1]);

        final int endHour = Integer.parseInt(endTime.split(":")[0]);
        final int endMin = Integer.parseInt(endTime.split(":")[1]);

        final int startMinInDay = (startHour * 60) + startMin;
        final int endMinInDay = (endHour * 60) + endMin;

        final int curHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        final int curMin = Calendar.getInstance().get(Calendar.MINUTE);
        final int curMinInDay = (curHour * 60) + curMin;

        if ((curMinInDay >= startMinInDay)
                && ((curMinInDay < endMinInDay) || (endMinInDay < startMinInDay))) {
            return true;
        }
        return (curMinInDay <= startMinInDay) && (curMinInDay < endMinInDay)
                && (endMinInDay < startMinInDay);

    }

    public static boolean isThisWeek(final long time) {
        return (System.currentTimeMillis() - time) < MILLIS_IN_WEEK;
    }

    public static boolean isThisYear(final long time) {
        final Calendar today = Calendar.getInstance();
        today.setTime(new Date());

        final Calendar otherday = Calendar.getInstance();
        otherday.setTime(new Date(time));

        return otherday.get(Calendar.YEAR) == today.get(Calendar.YEAR);
    }

    /**
     * 格式化videoTime, 返回结果：00:00 / 00:00:00
     * <p>
     * videoTime 默认是毫秒单位
     */
    public static String formatVideoTime(long videoTime) {
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

    // chenyong1 增加四舍五入功能
    public static String formatLocalVideoTime(long videoTime) {
        MyLog.d(TAG, "formatLocalVideoTime, videoTime = " + videoTime);

        int seconds = 0;
        int minutes = 0;
        int hours = 0;
        if (videoTime < 0) {
            videoTime = 0;
        }
        float totalTime = videoTime / 1000f;
        videoTime = Math.round(totalTime);

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

    public static String[] getLiveYears() {
        MyLog.d(TAG, "getLiveYears");
        return null;
    }

    public static String[] getLiveMonths() {
        MyLog.d(TAG, "getLiveMonths");
        return null;
    }

    public static String[] getLiveDays() {
        MyLog.d(TAG, "getLiveDays");
        return null;
    }

    public static String[] getLiveHours() {
        MyLog.d(TAG, "getLiveHours");
        String[] hours = new String[24];
        for (int i = 0; i < hours.length; i++) {
            hours[i] = String.format("%02d", i);
        }
        return hours;
    }

    public static String[] getLiveMinutes() {
        MyLog.d(TAG, "getLiveMinutes");
        String[] minutes = new String[60];
        for (int i = 0; i < minutes.length; i++) {
            minutes[i] = String.format("%02d", i);
        }
        return minutes;
    }

    public static String formatTimeStringForCompose(final Context context, final long rowTime) {
        // 如果是今天，显示24小时制时间；如果和当前时间在同周内，显示周几；否则显示日期。
        if (isThisYear(rowTime)) {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM/dd HH:mm");
            return simpleDateFormat.format(new Date(rowTime));
        } else {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm");
            return simpleDateFormat.format(new Date(rowTime));
        }
    }

    public static String getDateFromRowTime(final long rowTime) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(GlobalData.app().getString(R.string.michannel_date_format));
        return simpleDateFormat.format(new Date(rowTime));
    }

    public static String getTimeFromRowTime(final long rowTime) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(GlobalData.app().getString(R.string.michannel_time_format));
        return simpleDateFormat.format(new Date(rowTime));
    }

    public static String formatTimeStringForConversation(final Context context, final long rowTime) {
        // 如果是今天，显示24小时制时间；如果和当前时间在同周内，显示周几；否则显示日期。
        final int format12Or24 = CommonUtils.isChineseLocale() ? DateUtils.FORMAT_24HOUR
                : DateUtils.FORMAT_12HOUR;
        if (DateUtils.isToday(rowTime)) {
            // 国际版显示 12 小时制 am/pm
            return DateUtils.formatDateTime(context, rowTime, DateUtils.FORMAT_SHOW_TIME
                    | format12Or24);
        }
        if (isThisYear(rowTime)) {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM/dd");
            return simpleDateFormat.format(new Date(rowTime));
        } else {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yy/MM/dd");
            return simpleDateFormat.format(new Date(rowTime));
        }
    }


    public static String formatTimeStringForNotice(final Context context, final long rowTime) {
        long now = System.currentTimeMillis();
        long dayDiff = getDayDiff(now, rowTime);
        String time = formatTimeStringForDate(rowTime, context.getString(R.string.michannel_time_format));

        String result;
        if (dayDiff == 0) {
            result = context.getString(R.string.date_today) + " " + time;
        } else if (dayDiff == 1) {
            result = context.getString(R.string.date_tomorrow) + " " + time;
        } else {
            result = formatTimeStringForDate(rowTime, context.getString(R.string.times_mm_dd_format)) + " " + time;
        }
        return result;
    }

    public static String formatTimeStringForDate(final long rowTime, final String formatStr) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(formatStr);
        return simpleDateFormat.format(new Date(rowTime));
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
    public static String formatHumanableDate(final long timeToFormat, final long timeToBase) {
        MyLog.v(TAG + " formatHumanableDate timeToFormat == " + timeToFormat + " timeToBase == " + timeToBase);
        if (timeToFormat < 0 || timeToBase < 0) {
            MyLog.e(TAG + " formatHumanableDate timeToFormat or timeToBase < 0, timeToFormat == " + timeToFormat + " timeToBase == " + timeToBase);
            return "";
        }

        long timeSpan = Math.abs(timeToBase - timeToFormat);
        MyLog.v(TAG + " formatHumanableDate timeSpan == " + timeSpan);
        if (timeSpan < MILLI_SECONDS_ONE_MINUTE) {        //一分钟之内, 显示刚刚
            return GlobalData.app().getResources().getString(R.string.justnow);
        } else if (timeSpan < MILLI_SECONDS_ONE_HOUR) {        //一小时之内, 显示　多少分钟前
            long tmp = timeSpan / MILLI_SECONDS_ONE_MINUTE;
            return GlobalData.app().getResources().getQuantityString(R.plurals.minute_ago, (int) tmp, tmp);
        } else if (timeSpan < MILLI_SECONDS_ONE_DAY) {         //一天之内, 显示　多少小时前
            long tmp = timeSpan / MILLI_SECONDS_ONE_HOUR;
            return GlobalData.app().getResources().getQuantityString(R.plurals.hour_ago, (int) tmp, tmp);
        } else if (timeSpan < MILLI_SECONDS_ONE_MONTH) {       //一个月之内, 显示　多少天前
            float value = (float) timeSpan / (float) MILLI_SECONDS_ONE_DAY;
            //这里是为了解决跨天的问题, 比如value为2.55天有可能跨天了
            Calendar calendar = Calendar.getInstance();
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            float hourPercent = (float) hour / (float) 24;

            value += hourPercent;


            int tmp = (int) value;

            return GlobalData.app().getResources().getQuantityString(R.plurals.days_ago, tmp, tmp);
        } else if (timeSpan < MILLI_SECONDS_ONE_YEAR) {       //一年之内, 显示　几个月前
            long value = timeSpan / MILLI_SECONDS_ONE_MONTH;
            return GlobalData.app().getResources().getQuantityString(R.plurals.months_ago, (int) value, value);
        } else {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(new Date(timeToFormat));
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            return String.format(GlobalData.app().getResources().getString(R.string.year_month_day), year, (month + 1), day);
        }
    }

    /**
     * 时间一年内，显示，月－日 具体时间：08－22 17:47
     * 时间超过一年，显示，年－月－日 具体时间：2015-08－22 17:47
     *
     * @param timeToFormat
     * @param timeToBase
     * @return
     */
    public static String formatFeedsJournalCreateData(final long timeToFormat, final long timeToBase) {
        MyLog.v(TAG + " formatFeedsJournalCreateData timeToFormat == " + timeToFormat + " timeToBase == " + timeToBase);
        if (timeToFormat < 0 || timeToBase < 0) {
            MyLog.e(TAG + " formatFeedsJournalCreateData timeToFormat or timeToBase < 0, timeToFormat == " + timeToFormat + " timeToBase == " + timeToBase);
            return "";
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date(timeToFormat));
        int yearToFormat = calendar.get(Calendar.YEAR);
        int monthToFormat = calendar.get(Calendar.MONTH);
        int dayToFormat = calendar.get(Calendar.DAY_OF_MONTH);
        int hourToFormat = calendar.get(Calendar.HOUR_OF_DAY);
        int minuteToFormat = calendar.get(Calendar.MINUTE);

        calendar.clear();
        calendar.setTime(new Date(timeToBase));
        int yearToBase = calendar.get(calendar.YEAR);

        if (yearToBase == yearToFormat) {      //同一年,
            return (String) DateFormat.format(GlobalData.app().getResources().getString(R.string.month_day_hour_minute), new Date(timeToFormat));


//            return String.format(GlobalData.app().getResources().getString(R.string.month_day_hour_minute), (monthToFormat + 1), dayToFormat, hourToFormat, minuteToFormat);
        } else {      //不是同一年

            return (String) DateFormat.format(GlobalData.app().getResources().getString(R.string.year_month_day_hour_minute), new Date(timeToFormat));

//            return String.format(GlobalData.app().getResources().getString(R.string.year_month_day_hour_minute), yearToFormat, (monthToFormat + 1), dayToFormat, hourToFormat, minuteToFormat);
        }

    }


    /**
     * 返回一个时间　相对于另一个时间的可读的　时间表示
     * 一年内
     * (1) 60分钟内，显示时间：XX分钟前，例：6分钟前、12分钟前
     * (2)大于60分钟，小于当天23：59，显示时间：XX小时前，例：1小时前
     * (3)大于当天23:59，小于第二天24：00，显示时间：昨天 xx:xx，例：昨天 22：22
     * (4)大于第二天24:00，显示时间：月－日，例：8-17
     * 超过一年
     * (5)超过1年，显示时间：2016年5月12日
     *
     * @param timeToFormat 　要转换的时间
     * @param timeToBase   　基准时间
     * @return
     */
    public static String formatFeedsHumanableDate(final long timeToFormat, final long timeToBase) {
        MyLog.v(TAG + " formatFeedsHumanableDate timeToFormat == " + timeToFormat + " timeToBase == " + timeToBase);
        if (timeToFormat < 0 || timeToBase < 0) {
            MyLog.e(TAG + " formatFeedsHumanableDate timeToFormat or timeToBase < 0, timeToFormat == " + timeToFormat + " timeToBase == " + timeToBase);
            return "";
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date(timeToFormat));
        int yearToFormat = calendar.get(Calendar.YEAR);
        int monthToFormat = calendar.get(Calendar.MONTH);
        int dayToFormat = calendar.get(Calendar.DAY_OF_MONTH);
        int hourToFormat = calendar.get(Calendar.HOUR_OF_DAY);
        int minuteToFormat = calendar.get(Calendar.MINUTE);

        calendar.clear();
        calendar.setTime(new Date(timeToBase));
        int yearToBase = calendar.get(calendar.YEAR);
        int timeToBaseMonth = calendar.get(Calendar.MONTH);
        int timeToBaseDay = calendar.get(Calendar.DAY_OF_MONTH);
        int timeToBaseHour = calendar.get(Calendar.HOUR_OF_DAY);
        int timeToBaseMinute = calendar.get(Calendar.MINUTE);

        if (yearToBase == yearToFormat) {     //同一年里
            long timeSpan = Math.abs(timeToBase - timeToFormat);
            MyLog.v(TAG + " formatHumanableDate timeSpan == " + timeSpan);

            if (timeSpan < MILLI_SECONDS_ONE_MINUTE) {    //一分钟之内, 显示刚刚
                return GlobalData.app().getResources().getString(R.string.justnow);
            }

            if (timeSpan < MILLI_SECONDS_ONE_HOUR) {      //一小时之内, 显示　多少分钟前
                long tmp = timeSpan % MILLI_SECONDS_ONE_MINUTE == 0 ? timeSpan / MILLI_SECONDS_ONE_MINUTE : timeSpan / MILLI_SECONDS_ONE_MINUTE + 1;
                return GlobalData.app().getResources().getQuantityString(R.plurals.minute_ago, (int) tmp, tmp);
            }

            //在一年之内, 1小时之外
            //年月日相同, 说明是同一天之内, 那么需要显示"1小时前"
            if ((monthToFormat == timeToBaseMonth) && (dayToFormat == timeToBaseDay)) {
                long tmp = timeSpan % MILLI_SECONDS_ONE_HOUR == 0 ? timeSpan / MILLI_SECONDS_ONE_HOUR : timeSpan / MILLI_SECONDS_ONE_HOUR + 1;
                return GlobalData.app().getResources().getQuantityString(R.plurals.hour_ago, (int) tmp, tmp);
            } else {      //不是同一天
                try {
                    //现将timeToBase基准到零点
                    String tmp = yearToBase + "-" + (timeToBaseMonth + 1) + "-" + timeToBaseDay;
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                    Date date = dateFormat.parse(tmp);
                    long lingdian = date.getTime();
                    long lingdiantimeSpan = Math.abs(timeToFormat - lingdian);
                    if (lingdiantimeSpan < MILILIS_ONE_DAY) {     //是昨天, 显示"昨天 22：22"
                        return GlobalData.app().getResources().getString(R.string.yesterday_hour_minute, hourToFormat, minuteToFormat);
                    } else {      //大于1天, 显示"8-17"
                        return GlobalData.app().getResources().getString(R.string.month_day, (monthToFormat + 1), dayToFormat);
                    }

                } catch (ParseException e) {
                    MyLog.w(TAG, e);
                }

            }

            //默认返回
            calendar.clear();
            calendar.setTime(new Date(timeToFormat));
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            return String.format(GlobalData.app().getResources().getString(R.string.year_month_day), year, (month + 1), day);
        } else {      //不是同一年
            return String.format(GlobalData.app().getResources().getString(R.string.year_month_day), yearToFormat, (monthToFormat + 1), dayToFormat);
        }

    }

    /**
     * 获取下个小时
     */
    public static Date getNextHourInDate() {
        Calendar currentTime = Calendar.getInstance();
        currentTime.set(Calendar.MINUTE, 0);
        currentTime.set(Calendar.SECOND, 0);
        currentTime.add(Calendar.HOUR, 1);
        return currentTime.getTime();
    }

    //返回时间戳相差几天 今天返回0 昨天返回-1 明天返回1
    public static long getDayDiff(long TSL, long TSR) {
        return adjustTimeZoneMillis(TSR) / MILILIS_ONE_DAY - adjustTimeZoneMillis(TSL) / MILILIS_ONE_DAY;
    }
    
    /**
     * 用于补足时区
     *
     * @notice Date，Calender类已经添加时区，不需要调用此函数矫正，只提供给getDayDiff使用
     */
    private static long adjustTimeZoneMillis(long millis) {
        Calendar cal = Calendar.getInstance();
        int zoneOffset = cal.get(Calendar.ZONE_OFFSET);
        int dstOffset = cal.get(Calendar.DST_OFFSET);
        return millis + (zoneOffset + dstOffset);
    }
}
