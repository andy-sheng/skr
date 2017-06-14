package com.wali.live.watchsdk.component.viewmodel;

import android.text.TextUtils;

import com.base.log.MyLog;

import org.json.JSONObject;

/**
 * Created by lan on 17/4/11.
 */
public class GameViewModel {
    private static final String TAG = GameViewModel.class.getSimpleName();

    private static final String ICON_URL_PREFIX = "http://t1.g.mi.com/thumbnail/webp/w240/%s";
    private static final String DOWNLOAD_URL_PREFIX = "https://wap.game.xiaomi.com/index.php?c=app&v=download&app_id=%s&channel=meng_1242_11_android";

    private String mGameId;
    private String mName;
    private String mPackageName;
    private String mIconUrl;
    private String mDownloadUrl;
    private String mClassName;
    private float mRatingScore;
    private int mPrice;
    private int mDownloadCount;

    public GameViewModel(String json) throws Exception {
        parse(json);
    }

    public void parse(String json) throws Exception {
        JSONObject jsonObject = new JSONObject(json);
        mGameId = jsonObject.optString("gameId");
        mName = jsonObject.optString("displayName");
        mPackageName = jsonObject.optString("packageName");
        mIconUrl = String.format(ICON_URL_PREFIX, jsonObject.optString("icon"));
        // mDownloadUrl = jsonObject.optString("gameApk");
        mDownloadUrl = String.format(DOWNLOAD_URL_PREFIX, mGameId);
        mClassName = jsonObject.optString("className");
        mRatingScore = generateRatingScore(jsonObject.optString("ratingScore"));
        mPrice = generatePrice(jsonObject.optString("price"));
        mDownloadCount = jsonObject.optInt("downloadCount");
    }

    private float generateRatingScore(String ratingScoreStr) throws Exception {
        if (TextUtils.isEmpty(ratingScoreStr)) {
            return 0;
        }
        return Float.valueOf(ratingScoreStr);
    }

    private int generatePrice(String priceStr) throws Exception {
        if (TextUtils.isEmpty(priceStr)) {
            return 0;
        }
        return Integer.valueOf(priceStr);
    }

    public boolean isValid() {
        if (TextUtils.isEmpty(mName) || TextUtils.isEmpty(mIconUrl)
                || TextUtils.isEmpty(mDownloadUrl) || mPrice != 0) {
            return false;
        }
        return true;
    }

    public String getGameId() {
        return mGameId;
    }

    public String getName() {
        return mName;
    }

    public String getPackageName() {
        return mPackageName;
    }

    public String getIconUrl() {
        return mIconUrl;
    }

    public String getDownloadUrl() {
        return mDownloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        if (!TextUtils.isEmpty(downloadUrl)) {
            MyLog.w(TAG, "gameViewModel " + mGameId + ":" + downloadUrl);
            mDownloadUrl = downloadUrl;
        }
    }

    public String getClassName() {
        return mClassName;
    }

    public float getRatingScore() {
        return mRatingScore;
    }

    public int getPrice() {
        return mPrice;
    }

    public int getDownloadCount() {
        return mDownloadCount;
    }
}
