package com.wali.live.utils;

import android.graphics.drawable.Drawable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.view.View;

import com.base.global.GlobalData;
import com.base.utils.CommonUtils;
import com.base.utils.span.SpanUtils;
import com.live.module.common.R;
import com.mi.live.data.config.GetConfigManager;

/**
 * Created by yurui on 2/27/16.
 */
public class ItemDataFormatUtils {
    public static final String TAG = ItemDataFormatUtils.class.getName();

    public static final int WALL_PAPER_BIG_SIZE = 480;
    public static final int WALL_PAPER_SMALL_SIZE = 160;

    public static GetConfigManager.LevelItem getLevelItem(int lvl) {
        return GetConfigManager.getInstance().getLevelItem(lvl);
    }


    public static Drawable getCertificationImgSource(int certificationType) {
        if (certificationType > 0) {
            return GetConfigManager.getInstance().getCertificationTypeDrawable(certificationType).certificationDrawable;
        } else {
            return null;
        }
    }

    //    public static Drawable getCertificationImgSourceLiveComment(int certificationType) {
//        if (certificationType > 0) {
//            return GetConfigManager.getInstance().getCertificationTypeDrawable(certificationType).certificationDrawableLiveComment;
//        } else {
//            return null;
//        }
//    }

    public static Drawable getLevelSmallImgSource(int level) {
        if (level >= 0) {
            return GetConfigManager.getInstance().getLevelSmallDrawable(level);
        } else {
            return null;
        }
    }

    public static Drawable getSmallRedName() {
        return GlobalData.app().getResources().getDrawable(R.drawable.red_icon);
    }

    //    public static Drawable getRedName() {
//        return GlobalData.app().getResources().getDrawable(R.drawable.card_icon_red);
//    }
//
//
//    public static String formatViewerCount(long count) {
//        try {
//            int languageType = CommonUtils.getLanguageInfo();
//            switch (languageType) {
//                case CommonUtils.LANGUAGE_CHINESE:
//                    return getCountForChinese(count);
//                case CommonUtils.LANGUAGE_CHINESE_TAIWAN:
//                    return getCountForChinese(count);
//                case CommonUtils.LANGUAGE_ENGLISH:
//                    return getCountForEnglish(count);
//                default:
//                    return getCountForChinese(count);
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return "";
//    }
//
//    public static String formatFansCount(long count) {
//        try {
//            int languageType = CommonUtils.getLanguageInfo();
//            switch (languageType) {
//                case CommonUtils.LANGUAGE_ENGLISH:
//                    return getCountForEnglish_0(count);
//                default:
//                    return getCountForChinese_0(count);
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return "";
//    }
//
//    /*
//        @style@originaljpg
//        @style@160jpg
//        @style@320jpg
//        @style@480jpg
//        @style@560jpg
//        @style@640jpg
//    */
//    public static String getWallPaperUrlByJson(long uuid, String coverPhotoJson, int size) {
//        String sizeStr;
//        switch (size) {
//            case 160:
//                sizeStr = "@style@160jpg";
//                break;
//            case 320:
//                sizeStr = "@style@320jpg";
//                break;
//            case 480:
//                sizeStr = "@style@480jpg";
//                break;
//            case 560:
//                sizeStr = "@style@560jpg";
//                break;
//            case 640:
//                sizeStr = "@style@640jpg";
//                break;
//            default:
//                sizeStr = "@style@originaljpg";
//                break;
//        }
//        if (!TextUtils.isEmpty(coverPhotoJson)) {
//            try {
//                JSONObject jsonObject = new JSONObject(coverPhotoJson);
//                if (jsonObject.has("ts")) {
//                    long ts = jsonObject.getLong("ts");
//                    if (ts > 0) {
//                        return "http://zbupic.zb.mi.com/" + uuid + "" + sizeStr + "?ts=" + ts;
//                    }
//                } else if (jsonObject.has("img")) {
//                    int index = jsonObject.getInt("img");
//                    String url = "";
//                    List<GetConfigManager.WallPaper> list = GetConfigManager.getInstance().getWallPaperUrls();
//                    for (GetConfigManager.WallPaper wallPaper : list) {
//                        if (wallPaper.key == index) {
//                            MyLog.v(TAG + " getWallPaperUrlByJson:wallPaper.url=" + wallPaper.url + sizeStr);
//                            return wallPaper.url + sizeStr;
//                        } else if (wallPaper.key == 1) {
//                            url = wallPaper.url + sizeStr;
//                        }
//                    }
//                    MyLog.v(TAG + " getWallPaperUrlByJson:url=" + url);
//                    return url;
//                } else {
//                    List<GetConfigManager.WallPaper> list = GetConfigManager.getInstance().getWallPaperUrls();
//                    for (GetConfigManager.WallPaper wallPaper : list) {
//                        if (wallPaper.key == 1) {
//                            return wallPaper.url + sizeStr;
//                        }
//                    }
//                }
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//        return "";
//    }
//
//    public static String getWallPaperUrlBySize(String url, int size) {
//        String sizeStr;
//        switch (size) {
//            case 160:
//                sizeStr = "@style@160jpg";
//                break;
//            case 320:
//                sizeStr = "@style@320jpg";
//                break;
//            case 480:
//                sizeStr = "@style@480jpg";
//                break;
//            case 560:
//                sizeStr = "@style@560jpg";
//                break;
//            case 640:
//                sizeStr = "@style@640jpg";
//                break;
//            default:
//                sizeStr = "@style@originaljpg";
//                break;
//        }
//        MyLog.v(TAG + " getWallPaperUrlBySize:" + url + sizeStr);
//        return url + sizeStr;
//    }
//
//    public static String getCountForEnglish(long count) {
//
//        if (count < 1000) {
//            return "" + count;
//        } else if (count >= 1000 && count < 1000000) {
//            DecimalFormat format = new DecimalFormat("0.00" + GlobalData.app().getResources().getString(R.string.ten_thousand));
//            return format.format(new BigDecimal(count / 1000.0));
//        } else {
//            DecimalFormat format = new DecimalFormat("0.00" + GlobalData.app().getResources().getString(R.string.million));
//            return format.format(new BigDecimal(count / 1000000.0));
//        }
//    }
//
//    public static String getCountForChinese(long count) {
//        if (count < 10000) {
//            return "" + count;
//        } else if (count >= 10000 && count < 100000000) {
//            DecimalFormat format = new DecimalFormat("0.00" + GlobalData.app().getResources().getString(R.string.ten_thousand));
//            return format.format(new BigDecimal(count / 10000.0));
//        } else {
//            DecimalFormat format = new DecimalFormat("0.00" + GlobalData.app().getResources().getString(R.string.hundred_million));
//            return format.format(new BigDecimal(count / 100000000));
//        }
//    }
//
//    public static String getCountForEnglish_0(long count) {
//        String result;
//        if (count < 1000) {
//            result = "" + count;
//        } else if (count >= 1000 && count < 1000000) {
//            DecimalFormat format = new DecimalFormat("0.0");
//            result = format.format(new BigDecimal(count / 1000.0));
//            if (result.endsWith(".0")) {
//                result = result.substring(0, result.lastIndexOf(".0"));
//            }
//            result += GlobalData.app().getResources().getString(R.string.ten_thousand);
//        } else {
//            DecimalFormat format = new DecimalFormat("0.0");
//            result = format.format(new BigDecimal(count / 1000000.0));
//            if (result.endsWith(".0")) {
//                result = result.substring(0, result.lastIndexOf(".0"));
//            }
//            result += GlobalData.app().getResources().getString(R.string.million);
//        }
//        return result;
//    }
//
//    public static String getCountForChinese_0(long count) {
//        String result;
//        if (count < 10000) {
//            result = "" + count;
//        } else if (count >= 10000 && count < 100000000) {
//            DecimalFormat format = new DecimalFormat("0.0");
//            result = format.format(new BigDecimal(count / 10000.0));
//            if (result.endsWith(".0")) {
//                result = result.substring(0, result.lastIndexOf(".0"));
//            }
//            result += GlobalData.app().getResources().getString(R.string.ten_thousand);
//        } else {
//            DecimalFormat format = new DecimalFormat("0.0");
//            result = format.format(new BigDecimal(count / 100000000));
//            if (result.endsWith(".0")) {
//                result = result.substring(0, result.lastIndexOf(".0"));
//            }
//            result += GlobalData.app().getResources().getString(R.string.hundred_million);
//        }
//        return result;
//    }
//
//    //1小时24分钟前  2天钱 26分钟前
//    public static String formatTimePass(long time) {
//        long sec = Math.abs(System.currentTimeMillis() - time) / 1000;
//        if (sec < 60) {
//            return "1" + GlobalData.app().getResources().getString(R.string.minute) + GlobalData.app().getResources().getString(R.string.before);
//        } else if (sec < 3600) {
//            return (sec / 60) + GlobalData.app().getResources().getString(R.string.minute) + GlobalData.app().getResources().getString(R.string.before);
//        } else if (sec < 3600 * 24) {
//            long min = sec / 60;
//            return (min / 60) + GlobalData.app().getResources().getString(R.string.hour) +
//                    ((min % 60 == 0) ? "" : (min % 60) + GlobalData.app().getResources().getString(R.string.minute))
//                    + GlobalData.app().getResources().getString(R.string.before);
//        } else {
//            return (sec / (3600 * 24)) + GlobalData.app().getResources().getString(R.string.day) + GlobalData.app().getResources().getString(R.string.before);
//        }
//    }
//
    //like 01:03:15
    public static String formatVideodisplayTime(long timeMs) {
        timeMs /= 1000;
        final long hour = timeMs / 3600;
        final long min = (timeMs - (3600 * hour)) / 60;
        final long sec = timeMs - (3600 * hour) - (60 * min);
        final StringBuilder displayText = new StringBuilder("");
        if (hour > 0) {
            displayText.append(hour);
            displayText.append(":");
        }

        if (min < 10) {
            displayText.append("0").append(min);
        } else {
            displayText.append(min);
        }

        if (sec < 10) {
            displayText.append(":0").append(sec);
        } else {
            displayText.append(":").append(sec);
        }
        return displayText.toString();
    }

    public static SpannableStringBuilder getLiveTitle(String className, String text) {
        SpannableStringBuilder title = new SpannableStringBuilder(text);
        int len = title.length();
        int l = -1;
        for (int i = 0; i < len; i++) {
            if (title.charAt(i) == '#') {
                if (l == -1) {
                    l = i;
                } else {
                    String str;
                    if (l + 1 == i) {
                        str = "";
                    } else {
                        str = title.toString().substring(l + 1, i);
                    }
                    // 加点击事件
                    title.setSpan(new SpanUtils.MyClickableSpan(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (CommonUtils.isFastDoubleClick()) {
                                return;
                            }
                            //这里没有话题页，所以暂时拿掉点击事件跳转
                        }
                    }), l, i + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    title.setSpan(new ForegroundColorSpan(GlobalData.app().getResources().getColor(R.color.color_e5aa1e)), l, i + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    l = -1;
                }
            }

        }
        return title;
    }
//
//
//    public static SpannableStringBuilder getLiveTitle(SpannableStringBuilder title, ForegroundColorSpan foregroundColorSpan, String className, String text) {
//        title.clear();
//        title.clearSpans();
//        title.append(text);
//        int len = title.length();
//        int l = -1;
//        for (int i = 0; i < len; i++) {
//            if (title.charAt(i) == '#') {
//                if (l == -1) {
//                    l = i;
//                } else {
//                    String str;
//                    if (l + 1 == i) {
//                        str = "";
//                    } else {
//                        str = title.toString().substring(l + 1, i);
//                    }
////                    if(!TextUtils.isEmpty(str)) {
//                    // 加点击事件
//                    title.setSpan(new SpanUtils.MyClickableSpan(v -> {
//                        if (CommonUtils.isFastDoubleClick()) {
//                            return;
//                        }
//                        EventBus.getDefault().post(new EventClass.TopicClickEvent(className, str));
//                    }), l, i + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
////                    }
//                    title.setSpan(foregroundColorSpan, l, i + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//                    l = -1;
//                }
//            }
//
//        }
//        return title;
//    }
//
//
//    /**
//     * 用来处理用户名的特殊颜色
//     *
//     * @param context
//     * @param ssb
//     * @param s
//     * @param targetStr
//     * @param targetId
//     * @param color
//     */
//    private static void setTextForegroundColorSpan(Activity context, SpannableStringBuilder ssb, SpannableStringBuilder s, String targetStr, long targetId, int color) {
//        int length = s.length();
//        if (!TextUtils.isEmpty(targetStr)) {
//            int i = 0;
//            while (i >= 0 && i < length) {
//                i = s.toString().indexOf(targetStr, i);
//                if (i >= 0) {
//                    final long id = targetId;
//                    ssb.setSpan(new SpanUtils.MyClickableSpan(view -> {
//                        if (CommonUtils.isFastDoubleClick()) {
//                            return;
//                        }
//                        if (view instanceof ClickPreventableTextView) {
//                            if (((ClickPreventableTextView) view).ignoreSpannableClick())
//                                return;
//                            ((ClickPreventableTextView) view).preventNextClick();
//                        }
//                        PersonInfoActivity.openActivity(context, id);
//                    }), i, i + targetStr.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//
//                    ssb.setSpan(new ForegroundColorSpan(color), i, i + targetStr.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//
//                    i += targetStr.length();
//                }
//            }
//        }
//    }
}
