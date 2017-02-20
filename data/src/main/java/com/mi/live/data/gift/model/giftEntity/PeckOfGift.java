package com.mi.live.data.gift.model.giftEntity;

import com.base.log.MyLog;
import com.base.utils.FileIOUtils;
import com.wali.live.dao.Gift;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by zjn on 16-9-8.
 * 彩蛋礼物包
 * @module 礼物橱窗
 */
public class PeckOfGift extends Gift{
    public static final String TAG = "PeckOfGift";

    private List<PeckOfGiftInfo> peckOfGiftInfoList = new ArrayList<>();

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
//            File resFile = new File(jsonConfigPath).getParentFile();
            JSONArray jsonArray = jsonObject.optJSONArray("giftList");
            if (jsonArray != null) {
                int len = jsonArray.length();
                for (int i = 0; i < len; i++) {
                    JSONObject obj = jsonArray.optJSONObject(i);
                    if (obj != null) {
                        int giftId = obj.optInt("giftId");
                        int probability = obj.optInt("probability");
                        peckOfGiftInfoList.add(new PeckOfGiftInfo(giftId, probability));
                    }
                }
            }
        }
    }

    public List<PeckOfGiftInfo> getPeckOfGiftInfoList() {
        return peckOfGiftInfoList;
    }

    public String getConfigJsonFileName() {
        // 有文件的子类覆盖这个方法-后面和产品对
        return "peckOfGiftConfig.json";
    }

    public boolean needDownResource() {
        //  需要下载资源的子类覆盖这个方法
        return true;
    }

    @Override
    public String toString() {
        return "PeckOfGift{" +
                "peckOfGiftInfoList=" + peckOfGiftInfoList +
                '}'+super.toString();
    }

    public static class PeckOfGiftInfo {
        @Override
        public String toString() {
            return "PeckOfGiftInfo{" +
                    "probability=" + probability +
                    ", giftId='" + giftId + '\'' +
                    '}';
        }

        private int giftId;
        private int probability;

        public int getGiftId() {
            return giftId;
        }

        public int getProbability() {
            return probability;
        }

        public PeckOfGiftInfo(int giftId, int probability) {
            this.giftId = giftId;
            this.probability = probability;
        }
    }
}
