package com.mi.live.data.gift.model.giftEntity;

import com.base.log.MyLog;
import com.base.utils.FileIOUtils;
import com.wali.live.dao.Gift;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

/**
 * Created by zjn on 17-1-3.
 * 表情礼物，sdk版本中只关注观众端效果，主播端效果暂不处理
 */
public class ExpressionGift extends Gift {

    public static final String TAG = "ExpressionGift";

    private String magicName;
    private int magicDuration;
    private int expressionId;

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
            magicName = jsonObject.optString("magicName");
            magicDuration = jsonObject.optInt("magicDuration");
            expressionId = jsonObject.optInt("expressionId");
        }
    }

    public String getMagicName() {
        return magicName;
    }

    public void setMagicName(String magicName) {
        this.magicName = magicName;
    }

    public int getMagicDuration() {
        return magicDuration;
    }

    public void setMagicDuration(int magicDuration) {
        this.magicDuration = magicDuration;
    }

    public int getExpressionId() {
        return expressionId;
    }

    public void setExpressionId(int expressionId) {
        this.expressionId = expressionId;
    }

    public String getConfigJsonFileName() {
        // 有文件的子类覆盖这个方法
        return "FaceMagicConfig.json";
    }

    public boolean needDownResource() {
        //  需要下载资源的子类覆盖这个方法
        return true;
    }

    @Override
    public String toString() {
        return new StringBuilder().append("ExpressionGift{").append(
                "magicName=").append(magicName).append(
                ", magicDuration='").append(magicDuration).append('\'').append(
                ", expressionId='").append(expressionId).append('\'').append(
                '}').append(super.toString()).toString();
    }
}
