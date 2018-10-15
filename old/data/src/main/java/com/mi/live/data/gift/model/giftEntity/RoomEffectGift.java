package com.mi.live.data.gift.model.giftEntity;

import com.base.log.MyLog;
import com.base.utils.FileIOUtils;
import com.wali.live.dao.Gift;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;

/**
 * Created by chengsimin on 16/6/18.
 *
 * @module 礼物
 */
public class RoomEffectGift extends Gift {
    int effectDuration = 0;//特效播放时间，单位为s
    String roomEffectAnimation;//webp动画地址


    public void completeGiftInfo(String jsonConfigPath) {
        JSONObject jsonObject = null;
        try {
            MyLog.d(TAG,"jsonConfigPath:"+jsonConfigPath);
            String jsonStr = FileIOUtils.readFile(jsonConfigPath);
            MyLog.d(TAG,"jsonStr:"+jsonStr);
            jsonObject = new JSONObject(jsonStr);
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (jsonObject != null) {
            File resFile = new File(jsonConfigPath).getParentFile();
            roomEffectAnimation = new File(resFile, jsonObject.optString("roomEffectAnimation")).getAbsolutePath();
            effectDuration = jsonObject.optInt("effectDuration");
        }
    }

    public int getEffectDuration() {
        return effectDuration;
    }

    public String getRoomEffectAnimation() {
        return roomEffectAnimation;
    }

    public String getConfigJsonFileName() {
        // 有文件的子类覆盖这个方法
        return "roomEffectConfig.json";
    }

    public boolean needDownResource() {
        //  需要下载资源的子类覆盖这个方法
        return true;
    }

    @Override
    public String toString() {
        return "RoomEffectGift{" +
                "effectDuration=" + effectDuration +
                ", roomEffectAnimation='" + roomEffectAnimation + '\'' +
                '}' +
                super.toString();
    }
}
