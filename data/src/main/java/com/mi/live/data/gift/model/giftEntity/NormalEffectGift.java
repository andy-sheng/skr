package com.mi.live.data.gift.model.giftEntity;

import com.base.log.MyLog;
import com.base.utils.FileIOUtils;
import com.wali.live.dao.Gift;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by chengsimin on 16/6/18.
 *
 * @module 礼物
 */
public class NormalEffectGift extends Gift {
    public static final String TAG = "LightUpGift";
    List<Flag> flags = new ArrayList<>();
    List<BigContinue> bigCons = new ArrayList<>();//小礼物连击触发大礼物

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
            JSONArray jsonArray = jsonObject.optJSONArray("configList");
            if (jsonArray != null) {
                int len = jsonArray.length();
                for (int i = 0; i < len; i++) {
                    JSONObject obj = jsonArray.optJSONObject(i);
                    if (obj != null) {
                        int startCount = obj.optInt("startCount");
                        String giftImage = new File(resFile, obj.optString("giftImage")).getAbsolutePath();
                        flags.add(new Flag(startCount, giftImage));
                    }
                }
                Collections.sort(flags, new Comparator<Flag>() {
                    @Override
                    public int compare(Flag lhs, Flag rhs) {
                        return lhs.startCount - rhs.startCount;
                    }
                });
            }
            JSONArray jsonArrayBig = jsonObject.optJSONArray("bigConfigList");
            if (jsonArrayBig != null) {
                int len = jsonArray.length();
                for (int i = 0; i < len; i++) {
                    JSONObject obj = jsonArrayBig.optJSONObject(i);
                    if (obj != null) {
                        int startCount = obj.optInt("startCount");
                        int giftId = obj.optInt("giftId");
                        bigCons.add(new BigContinue(startCount, giftId));
                    }
                }
                Collections.sort(bigCons, new Comparator<BigContinue>() {
                    @Override
                    public int compare(BigContinue lhs, BigContinue rhs) {
                        return lhs.startCount - rhs.startCount;
                    }
                });
            }
        }
    }


    public List<Flag> getFlags() {
        return flags;
    }

    public String getConfigJsonFileName() {
        // 有文件的子类覆盖这个方法
        return "giftConfig.json";
    }

    public boolean needDownResource() {
        //  需要下载资源的子类覆盖这个方法
        return true;
    }

    public List<BigContinue> getBigCons() {
        return bigCons;
    }

    @Override
    public String toString() {
        return "NormalEffectGift{" +
                "flags=" + flags + "BigContinue" + bigCons +
                '}' + super.toString();
    }

    public static class Flag {
        @Override
        public String toString() {
            return "Flag{" +
                    "startCount=" + startCount +
                    ", giftImage='" + giftImage + '\'' +
                    '}';
        }

        public int startCount;
        public String giftImage;

        public Flag(int startCount, String giftImage) {
            this.startCount = startCount;
            this.giftImage = giftImage;
        }
    }

    public static class BigContinue {
        @Override
        public String toString() {
            return "BigContinue{" +
                    "startCount=" + startCount +
                    ", giftId='" + giftId + '\'' +
                    '}';
        }

        public int startCount;
        public int giftId;

        public BigContinue(int startCount, int giftId) {
            this.startCount = startCount;
            this.giftId = giftId;
        }
    }
}
