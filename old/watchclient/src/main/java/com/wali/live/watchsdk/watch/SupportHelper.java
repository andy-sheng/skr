package com.wali.live.watchsdk.watch;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.base.log.MyLog;
import com.wali.live.watchsdk.R;

/**
 * 这个就是直接从游戏中心里面搬过来
 */
public class SupportHelper {
    public static final String
            JSON_LANGUAGE_ZH_CN = "zh-CN",
            JSON_LANGUAGE_ZH_TW = "zh-TW",
            JSON_LANGUAGE_EN = "en",
            JSON_LANGUAGE_JA = "ja",
            JSON_LANGUAGE_KO = "ko",
            JSON_LANGUAGE_RU = "ru";
    public static final String JSON_VPN = "vpn";
    public static final String JSON_GAME_PAD = "gamepad";
    public static final String JSON_MULTIPLAYER_LOCAL = "localMultiplayer";
    public static final String JSON_MULTIPLAYER_ONLINE = "multiplayer";
    public static final String JSON_GOOGLE_PLAY = "googleplay";
    public static final String JSON_EXTRA_DATA = "extraData";
    public static final String
            JSON_FEE_TYPE_AD = "ad",
            JSON_FEE_TYPE_IAP = "IAP",
            JSON_FEE_TYPE_DOWNLOAD = "download";

    public static SupportRes getSupportRes(String str) {
        if (!TextUtils.isEmpty(str)) {
            SupportHelper.SupportRes res = new SupportHelper.SupportRes();
            switch (str) {
                case JSON_VPN:
                    res.setImgRes(R.drawable.icon_vpn);
                    res.setNameRes(R.string.gameinfo_support_vpn);
                    return res;
                case JSON_GAME_PAD:
                    res.setImgRes(R.drawable.icon_gamepad);
                    res.setNameRes(R.string.gameinfo_support_gamepad);
                    return res;
                case JSON_MULTIPLAYER_LOCAL:
                    res.setImgRes(R.drawable.icon_multiplayer_local);
                    res.setNameRes(R.string.gameinfo_support_multi_local);
                    return res;
                case JSON_MULTIPLAYER_ONLINE:
                    res.setImgRes(R.drawable.icon_multiplayer_online);
                    res.setNameRes(R.string.gameinfo_support_multi_online);
                    return res;
                case JSON_GOOGLE_PLAY:
                    res.setImgRes(R.drawable.icon_google);
                    res.setNameRes(R.string.gameinfo_support_google);
                    return res;
                case JSON_LANGUAGE_ZH_CN:
                    res.setImgRes(R.drawable.icon_cn);
                    res.setNameRes(R.string.gameinfo_support_cn);
                    return res;
                case JSON_LANGUAGE_ZH_TW:
                    res.setImgRes(R.drawable.icon_tw);
                    res.setNameRes(R.string.gameinfo_support_tw);
                    return res;
                case JSON_LANGUAGE_EN:
                    res.setImgRes(R.drawable.icon_en);
                    res.setNameRes(R.string.gameinfo_support_en);
                    return res;
                case JSON_LANGUAGE_JA:
                    res.setImgRes(R.drawable.icon_ja);
                    res.setNameRes(R.string.gameinfo_support_ja);
                    return res;
                case JSON_LANGUAGE_KO:
                    res.setImgRes(R.drawable.icon_ko);
                    res.setNameRes(R.string.gameinfo_support_ko);
                    return res;
                case JSON_LANGUAGE_RU:
                    res.setImgRes(R.drawable.icon_ru);
                    res.setNameRes(R.string.gameinfo_support_ru);
                    return res;
                case JSON_EXTRA_DATA:
                    res.setImgRes(R.drawable.icon_extra_data);
                    res.setNameRes(R.string.gameinfo_support_extra_data);
                    return res;
                case JSON_FEE_TYPE_AD:
                    res.setImgRes(R.drawable.icon_ad);
                    res.setNameRes(R.string.gameinfo_support_ad);
                    return res;
                case JSON_FEE_TYPE_IAP:
                    res.setImgRes(R.drawable.icon_iap);
                    res.setNameRes(R.string.gameinfo_support_iap);
                    return res;
                case JSON_FEE_TYPE_DOWNLOAD:
                    res.setImgRes(R.drawable.icon_download);
                    res.setNameRes(R.string.gameinfo_support_download);
                    return res;
            }
        }
        return null;
    }

    /**
     * luan
     * @param str
     * @return
     */
    private static final String JSON_WIFI = "gameinfo_support_wifi";
    private static final String JSON_NO_WIFI = "gameinfo_support_no_wifi";
    public static SupportRes getSupportResByUrl(String str) {
        if (!TextUtils.isEmpty(str)) {
            SupportHelper.SupportRes res = new SupportHelper.SupportRes();
            if(str.equals(JSON_WIFI)) {
                res.setImgRes(R.drawable.icon_wifi);
                res.setNameRes(R.string.gameinfo_support_wifi);
                return res;
            } else if(str.equals(JSON_NO_WIFI)) {
                res.setImgRes(R.drawable.icon_no_wifi);
                res.setNameRes(R.string.gameinfo_support_no_wifi);
                return res;
            }

        }
        return null;
    }

    public static boolean contain(String str) {
        if (!TextUtils.isEmpty(str)) {
            switch (str) {
                case JSON_VPN:
                case JSON_GAME_PAD:
                case JSON_MULTIPLAYER_LOCAL:
                case JSON_MULTIPLAYER_ONLINE:
                case JSON_GOOGLE_PLAY:
                case JSON_LANGUAGE_ZH_CN:
                case JSON_LANGUAGE_ZH_TW:
                case JSON_LANGUAGE_EN:
                case JSON_LANGUAGE_JA:
                case JSON_LANGUAGE_KO:
                case JSON_LANGUAGE_RU:
                case JSON_EXTRA_DATA:
                case JSON_FEE_TYPE_AD:
                case JSON_FEE_TYPE_IAP:
                    return true;
            }
        }

        return false;
    }

    public static class SupportRes implements Parcelable {
        protected int nameRes;
        protected int imgRes;

        public int getNameRes() {
            return nameRes;
        }

        public void setNameRes(int nameRes) {
            this.nameRes = nameRes;
        }

        public int getImgRes() {
            return imgRes;
        }

        public void setImgRes(int imgRes) {
            this.imgRes = imgRes;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj != null && obj instanceof SupportRes) {
                SupportRes res = (SupportRes) obj;
                return res.imgRes == this.imgRes && res.nameRes == this.nameRes;
            }
            return false;
        }


        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(this.nameRes);
            dest.writeInt(this.imgRes);
        }

        public SupportRes() {
        }

        protected SupportRes(Parcel in) {
            this.nameRes = in.readInt();
            this.imgRes = in.readInt();
        }

        public static final Parcelable.Creator<SupportRes> CREATOR = new Parcelable.Creator<SupportRes>() {
            @Override
            public SupportRes createFromParcel(Parcel source) {
                return new SupportRes(source);
            }

            @Override
            public SupportRes[] newArray(int size) {
                return new SupportRes[size];
            }
        };
    }
}
