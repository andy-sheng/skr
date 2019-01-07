package com.module.playways.rank.room.score;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;
import com.common.utils.U;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import okio.BufferedSink;
import okio.Okio;
import okio.Sink;

public class ScoreSaveHelper {

    @JSONField(name = "data")
    private List<MachineScoreItem> mDataList = new ArrayList<>();


    public void add(MachineScoreItem machineScoreItem) {
        mDataList.add(machineScoreItem);
    }

    public void save(String filePath) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("data", mDataList);

        File file = new File(filePath);
        BufferedSink bufferedSink = null;
        try {
            Sink sink = Okio.sink(file);
            bufferedSink = Okio.buffer(sink);
            bufferedSink.writeString(jsonObject.toJSONString(), Charset.forName("UTF-8"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            if (null != bufferedSink) {
                bufferedSink.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public boolean vilid() {
        return mDataList.size() > 2;
    }

    public boolean isScoreEnough() {
        return true;
    }
}
