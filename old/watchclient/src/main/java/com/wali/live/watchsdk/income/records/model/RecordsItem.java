package com.wali.live.watchsdk.income.records.model;

import android.util.SparseArray;

import com.base.utils.language.LocaleUtil;
import com.wali.live.proto.PayProto;

import java.util.List;
import java.util.Locale;

/**
 * Created by zhaomin on 17-6-27.
 */
public class RecordsItem {

    public static final int LAN_CODE_SIMPLE_CHINESE = 0; // 语言代号客户端的语言，0:中文简体，1:英文，2:繁体中文
    public static final int LAN_CODE_COMPLEX_CHINESE = 2;
    public static final int LAN_CODE_ENGLISH = 1;

    public static final int PROFIT_TYPE_INCOME = 0; // 收入
    public static final int PROFIT_TYPE_COST = 1;  // 支出

    public static final int SOURCE_TYPE_WECHAT = 500; // 来自微信兑换

    private boolean mIsDayHint;

    private int day;

    private int profitType; // 收入Or支出 0=收入、1=支出

    private long profitChangeNum;

    private int sourceType; //来源类型 100=来自娱乐直播，200=来自游戏直播，300=来自小视频，400=来自宠爱团，500=微信兑换，600=兑换钻石

    private String text;

    public RecordsItem(boolean mIsDayHint, int day) {
        this.mIsDayHint = mIsDayHint;
        this.day = day;
    }

    public RecordsItem(boolean mIsDayHint, int day, String text) {
        this.mIsDayHint = mIsDayHint;
        this.day = day;
        this.text = text;
    }

    public RecordsItem(PayProto.ProfitDayDetailInfo info, int day) {
        this.day = day;
        profitType = info.getProfitType();
        List<PayProto.Languages> languages = info.getLanguageList();
        SparseArray<String> sparse = new SparseArray<String>();
        if (languages != null && !languages.isEmpty()) {
            for (PayProto.Languages lan : languages) {
                sparse.put(lan.getLanguageCode(), lan.getText());
            }
        }
        Locale locale = LocaleUtil.getLocale();
        if (locale.equals(Locale.TRADITIONAL_CHINESE)) {
            text = sparse.get(LAN_CODE_COMPLEX_CHINESE);
            profitChangeNum = info.getProfitChangeRMB();
        } else if (locale.equals(Locale.SIMPLIFIED_CHINESE)) {
            text = sparse.get(LAN_CODE_SIMPLE_CHINESE);
            profitChangeNum = info.getProfitChangeRMB();
        } else {
            text = sparse.get(LAN_CODE_ENGLISH);
            profitChangeNum = info.getProfitChangeDollar();
        }
        sourceType = info.getSourceType();
    }

    public boolean ismIsDayHint() {
        return mIsDayHint;
    }

    public void setmIsDayHint(boolean mIsDayHint) {
        this.mIsDayHint = mIsDayHint;
    }

    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
    }

    public int getProfitType() {
        return profitType;
    }

    public void setProfitType(int profitType) {
        this.profitType = profitType;
    }

    public long getProfitChangeNum() {
        return profitChangeNum;
    }

    public void setProfitChangeNum(int profitChangeNum) {
        this.profitChangeNum = profitChangeNum;
    }

    public int getSourceType() {
        return sourceType;
    }

    public void setSourceType(int sourceType) {
        this.sourceType = sourceType;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
