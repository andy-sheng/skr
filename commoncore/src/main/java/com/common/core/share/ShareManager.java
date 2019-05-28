package com.common.core.share;

import android.app.Activity;

import com.common.umeng.UmengInit;
import com.umeng.socialize.PlatformConfig;
import com.umeng.socialize.ShareAction;
import com.umeng.socialize.UMShareListener;
import com.umeng.socialize.bean.SHARE_MEDIA;

public class ShareManager {
    static boolean hasInit = false;

    public static final String WX_APP_ID = "wxf88af8c2cd665bc4";

    public static void init() {
        if (!hasInit) {
            //ge各个平台注册的key
            PlatformConfig.setWeixin(WX_APP_ID, "4a4930544a8fdbeb94b4dad5f5fb87c0");
//            PlatformConfig.setSinaWeibo("3921700954", "04b48b094faeb16683c32669824ebdad", "http://sns.whalecloud.com");
            PlatformConfig.setQQZone("101548016", "684f0e15ac56eba34b6eb742c63fce52");
//        PlatformConfig.setYixin("yxc0614e80c9304c11b0391514d09f13bf");
//        PlatformConfig.setTwitter("3aIN7fuF685MuZ7jtXkQxalyi", "MK6FEYG63eWcpDFgRYw4w9puJhzDl0tyuqWjZ3M7XJuuG7mMbO");
//        PlatformConfig.setAlipay("2015111700822536");
//        PlatformConfig.setLaiwang("laiwangd497e70d4", "d497e70d4c3e4efeab1381476bac4c5e");
//        PlatformConfig.setPinterest("1439206");
//        PlatformConfig.setKakao("e4f60e065048eb031e235c806b31c70f");
//        PlatformConfig.setDing("dingoalmlnohc0wggfedpk");
//        PlatformConfig.setVKontakte("5764965","5My6SNliAaLxEm3Lyd9J");
//        PlatformConfig.setDropbox("oz8v5apet3arcdy","h7p2pjbzkkxt02a");
            hasInit = true;
        }
    }

    public static void openSharePanel(Activity activity,UMShareListener l) {
        init();
        new ShareAction(activity).withText("hello").setDisplayList(SHARE_MEDIA.SINA, SHARE_MEDIA.QQ, SHARE_MEDIA.WEIXIN)
                .setCallback(l).open();
    }
}
