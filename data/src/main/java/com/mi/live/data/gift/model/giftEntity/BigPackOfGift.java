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
 * Created by zjn on 16-10-10.
 * 大礼包
 *
 * @module 礼物
 */
public class BigPackOfGift extends Gift {

    public static final String TAG = "bigPackOfGift";

    private List<PackOfGiftInfo> packOfGiftInfoList = new ArrayList<>();

    public void completeGiftInfo(String jsonConfigPath) {
        if (!packOfGiftInfoList.isEmpty()) {
            return;
        }
        JSONObject jsonObject = null;
        try {
            MyLog.d(TAG, "jsonConfigPath:" + jsonConfigPath);
            String jsonStr = FileIOUtils.readFile(jsonConfigPath);
            MyLog.d(TAG, "jsonStr:" + jsonStr);
            jsonObject = new JSONObject(jsonStr);
        } catch (JSONException | IOException e) {
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
                        int continueNum = obj.optInt("giftSendNum");
//                        int continueNum = obj.optInt("probability");
//                        giftInfoMap.put(giftId, continueNum);
                        packOfGiftInfoList.add(new PackOfGiftInfo(giftId, continueNum));
                        MyLog.d(TAG, "packOfGiftInfoList:" + packOfGiftInfoList.toString());
                    }
                }
            }
        }
    }

    public List<PackOfGiftInfo> getPackOfGiftInfoList() {
        return packOfGiftInfoList;
    }

    public String getConfigJsonFileName() {
        // 有文件的子类覆盖这个方法-后面和产品对
        return "bigPackOfGiftConfig.json";
//        return "peckOfGiftConfig.json";
    }

    public boolean needDownResource() {
        //  需要下载资源的子类覆盖这个方法
        return true;
    }

    @Override
    public String toString() {
        return "bigPackOfGift{" +
                "packOfGiftInfoList=" + packOfGiftInfoList +
                '}' + super.toString();
    }

    public static class PackOfGiftInfo {
        private int giftId;
        private int giftSendNum;

        public PackOfGiftInfo(int giftId, int giftSendNum) {
            this.giftId = giftId;
            this.giftSendNum = giftSendNum;
        }

        public int getGiftId() {
            return giftId;
        }

        public void setGiftId(int giftId) {
            this.giftId = giftId;
        }

        public int getGiftSendNum() {
            return giftSendNum;
        }

        public void setGiftSendNum(int giftSendNum) {
            this.giftSendNum = giftSendNum;
        }
    }
}
