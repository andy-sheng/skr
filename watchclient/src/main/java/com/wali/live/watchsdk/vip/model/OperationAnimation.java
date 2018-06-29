package com.wali.live.watchsdk.vip.model;

import com.base.log.MyLog;
import com.base.utils.FileIOUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;

public class OperationAnimation {
    private String TAG = "OperationAnimation";
    private int animationId = 0;
    private String animResUrl;       //资源下载链接
    int effectDuration = 0;          //特效播放时间，单位为ms
    private String topWebpPath;      //顶部webp动画地址
    private String bottomWebpPath;   //底部webp动画地址
    private String topIconPath;      //顶部入场icon
    private String bottomIconPath;   //底部入场icon
    private String bottomBackPath;   //底部入场弹幕背景
    private int nobelType;           //添加个贵族特权
    private String bottomText = "";
    private boolean isRound = false;
    private boolean needTopUserInfo = false;

    public void completeAnimInfo(String path) {
        JSONObject jsonObject = null;
        try {
            MyLog.d(TAG, "jsonPath:" + path);
            String jsonStr = FileIOUtils.readFile(path);
            jsonObject = new JSONObject(jsonStr);
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        MyLog.d(TAG, "jsonObject : " + jsonObject);
        if (jsonObject != null) {
            File resFile = new File(path).getParentFile();
            bottomText = jsonObject.optString("bottomText", "");
            topWebpPath = new File(resFile, jsonObject.optString("topWebpName")).getAbsolutePath();
            bottomWebpPath = new File(resFile, jsonObject.optString("bottomWebpName")).getAbsolutePath();
            topIconPath = new File(resFile, jsonObject.optString("topIconName")).getAbsolutePath();
            bottomIconPath = new File(resFile, jsonObject.optString("bottomIconName")).getAbsolutePath();
            bottomBackPath = new File(resFile, jsonObject.optString("bottomBackground")).getAbsolutePath();
            effectDuration = jsonObject.optInt("duration", 4_000);
            nobelType = jsonObject.optInt("nobelType", 0);
            isRound = "round".equals(jsonObject.optString("topShape", "six"));
            needTopUserInfo = 1 == jsonObject.optInt("needTopUserInfo", 0);
        }
    }

    public String getBottomText() {
        return bottomText;
    }

    public String getAnimResUrl() {
        return animResUrl;
    }

    public void setAnimResUrl(String animResUrl) {
        this.animResUrl = animResUrl;
    }

    public int getEffectDuration() {
        return effectDuration;
    }

    public void setEffectDuration(int effectDuration) {
        this.effectDuration = effectDuration;
    }

    public int getAnimationId() {
        return animationId;
    }

    public void setAnimationId(int animationId) {
        this.animationId = animationId;
    }

    public String getTopWebpPath() {
        return topWebpPath;
    }

    public void setTopWebpPath(String topWebpPath) {
        this.topWebpPath = topWebpPath;
    }

    public String getBottomWebpPath() {
        return bottomWebpPath;
    }

    public void setBottomWebpPath(String bottomWebpPath) {
        this.bottomWebpPath = bottomWebpPath;
    }

    public String getTopIconPath() {
        return topIconPath;
    }

    public void setTopIconPath(String topIconPath) {
        this.topIconPath = topIconPath;
    }

    public String getBottomIconPath() {
        return bottomIconPath;
    }

    public void setBottomIconPath(String bottomIconPath) {
        this.bottomIconPath = bottomIconPath;
    }

    public String getBottomBackPath() {
        return bottomBackPath;
    }

    public void setBottomBackPath(String bottomBackPath) {
        this.bottomBackPath = bottomBackPath;
    }

    public int getNobelType() {
        return nobelType;
    }

    @Override
    public String toString() {
        return "OperationAnimation{" +
                "TAG='" + TAG + '\'' +
                ", animationId=" + animationId +
                ", animResUrl='" + animResUrl + '\'' +
                ", effectDuration=" + effectDuration +
                ", topWebpPath='" + topWebpPath + '\'' +
                ", bottomWebpPath='" + bottomWebpPath + '\'' +
                ", topIconPath='" + topIconPath + '\'' +
                ", bottomIconPath='" + bottomIconPath + '\'' +
                ", bottomBackPath='" + bottomBackPath + '\'' +
                ", nobelType=" + nobelType +
                '}';
    }

    public boolean isRound() {
        return isRound;
    }

    public boolean isNeedTopUserInfo() {
        return needTopUserInfo;
    }
}
