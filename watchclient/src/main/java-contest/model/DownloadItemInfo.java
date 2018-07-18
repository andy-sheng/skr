package model;

/**
 * Created by wanglinzhang on 2018/2/1.
 */

public class DownloadItemInfo {
    private String mDownloadUrl;
    private String mPackageName;
    private String mName;

    public DownloadItemInfo(String url, String pkgName, String appName) {
        /*just for debug code, this package is small */
        //this.mDownloadUrl = "https://wap.game.xiaomi.com/index.php?c=app&v=download&app_id=53573&channel=meng_1242_11_android";
        //this.mPackageName = "com.duowan.mcbox.mconline";
        this.mDownloadUrl = url;
        this.mPackageName = pkgName;
        this.mName = appName;
    }

    public String getDownloadUrl() {
        return mDownloadUrl;
    }

    public String getPackageName() {
        return mPackageName;
    }

    public String getName() {
        return mName;
    }
}
