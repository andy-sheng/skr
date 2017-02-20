package com.mi.live.data.gift.model.giftEntity;

import android.text.TextUtils;

import com.base.log.MyLog;
import com.base.utils.FileIOUtils;
import com.wali.live.dao.Gift;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by chengsimin on 16/6/18.
 *
 * @module 礼物
 */
public class LightUpGift extends Gift {
    public static final String TAG = "LightUpGift";
    int effectDuration = 0;//特效播放时间，单位为s
    List<String> imageList = new ArrayList<>(5);//图片地址列表

    public void completeGiftInfo(String jsonConfigPath) {
        JSONObject jsonObject = null;
        try {
            MyLog.d(TAG, "jsonConfigPath:" + jsonConfigPath);
            String jsonStr = FileIOUtils.readFile(jsonConfigPath);
            MyLog.d(TAG, "jsonStr:" + jsonStr);
            jsonObject = new JSONObject(jsonStr);
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (jsonObject != null) {
            File resFile = new File(jsonConfigPath).getParentFile();
            JSONArray jsonArray = jsonObject.optJSONArray("imageList");
            int len = jsonArray.length();
            for (int i = 0; i < len; i++) {
                String pngName = jsonArray.optString(i);
                if (!TextUtils.isEmpty(pngName)) {
                    String pngFilePath = new File(resFile, pngName).getAbsolutePath();
                    MyLog.d(TAG, "pngFilePath:" + pngFilePath);
                    imageList.add(pngFilePath);
                }
            }
            effectDuration = jsonObject.optInt("effectDuration");
        }
    }

    public int getEffectDuration() {
        return effectDuration;
    }

    public List<String> getImageList() {
        return imageList;
    }

    public String getConfigJsonFileName() {
        // 有文件的子类覆盖这个方法
        return "likeEffectConfig.json";
    }

    public boolean needDownResource() {
        //  需要下载资源的子类覆盖这个方法
        return true;
    }

    @Override
    public String toString() {
        return "LightUpGift{" +
                "effectDuration=" + effectDuration +
                ", imageList=" + imageList +
                '}' +
                super.toString();
    }
}
