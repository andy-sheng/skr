package com.wali.live.watchsdk.contest.model;

/**
 * Created by wanglinzhang on 2018/1/29.
 */

public class MyDownloadModel {
    private String mPackageName;
    private String mDownloadURL;
    private String mAppId;
    private String mName;
    private String mAppIconRUL;
    private boolean mHasCardByDownload;
    private boolean mHasCardByOpen;

    public MyDownloadModel() {
        mName = "小米枪战";
        //mDownloadURL="https://wap.game.xiaomi.com/index.php?c=app&v=download&app_id=59061&channel=meng_1242_11_android";
        mDownloadURL="https://wap.game.xiaomi.com/index.php?c=app&v=download&app_id=53573&channel=meng_1242_11_android";
        //mAppId = "59061";
        mAppId = "53573";
        //mPackageName = "com.ak.mi";
        mPackageName = "com.duowan.mcbox.mconline";
        mHasCardByDownload = true;
        mHasCardByOpen = false;

    }
    public String getAppId() {
        return mAppId;
    }

    public void setId(String id) {
        mAppId = id;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public String getPackageName() {
        return mPackageName;
    }

    public void setPackageName(String name) {
        mPackageName = name;
    }

    public String getDownloadURL() {
        return mDownloadURL;
    }

    public void setDownloadURL(String url) {
        mDownloadURL = url;
    }

    public void setCardByDownloadApp(boolean has) {
        mHasCardByDownload = has;
    }

    public  void setCardByOpenAPP(boolean has) {
        mHasCardByDownload = has;
    }
    public boolean hasCardByDownloadApp() {
        return mHasCardByDownload;
    }

    public boolean hasCardByOpenApp() {
        return mHasCardByOpen;
    }
}
