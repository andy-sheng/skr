package com.module.playways.rank.room.score;

import com.alibaba.fastjson.annotation.JSONField;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class MachineScoreModel implements Serializable {
    @JSONField(name = "data")
    private List<MachineScoreItem> mDataList = new ArrayList<>();

    public List<MachineScoreItem> getDataList() {
        return mDataList;
    }

    public void setDataList(List<MachineScoreItem> dataList) {
        mDataList = dataList;
    }

    /**
     * 获取得分，算法为
     * curPostion 为当前播放的音频时间戳，一般为每一秒会取一次。
     * 则取 得分时间戳为  curPostion-1000<x<curPostion 的对应的得分即可
     * 保证 mMachineScoreModel 有序，采用二分法
     */
    public MachineScoreItem findMatchingScoreItemBy(long curPostion) {
        int b = 0, e = mDataList.size() - 1;
        while (b <= e) {
            int mid = (b + e) / 2;
            MachineScoreItem midItem = mDataList.get(mid);
            if (midItem.getTs() <= curPostion
                    && midItem.getTs() >= curPostion - 1000) {
                return midItem;
            } else if (midItem.getTs() > curPostion) {
                e = mid - 1;
            } else if (midItem.getTs() < curPostion - 1000) {
                b = mid + 1;
            }
        }
        return null;
    }
}
