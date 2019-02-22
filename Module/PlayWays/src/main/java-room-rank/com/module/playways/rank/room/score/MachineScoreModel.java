package com.module.playways.rank.room.score;

import com.alibaba.fastjson.annotation.JSONField;
import com.common.log.MyLog;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class MachineScoreModel implements Serializable {
    public final static String TAG = "MachineScoreModel";
    @JSONField(name = "data")
    private List<MachineScoreItem> mDataList = new ArrayList<>();

    @JSONField(name = "averageScore")
    private int mAverageScore = -1;

    @JSONField(name = "totalScore")
    private int mTotalScore = -1;

    public List<MachineScoreItem> getDataList() {
        return mDataList;
    }

    public void setDataList(List<MachineScoreItem> dataList) {
        mDataList = dataList;
    }

    public int getAverageScore() {
        return mAverageScore;
    }

    public void compute() {
        if (mTotalScore < 0) {
            int t = 0;
            for (MachineScoreItem machineScoreItem : mDataList) {
                MyLog.d(TAG, "compute 行数:" + machineScoreItem.no + " 得分:" + machineScoreItem.score);
                t += machineScoreItem.score;
            }
            mTotalScore = t;
            if (mDataList.size() > 0) {
                mAverageScore = mTotalScore / mDataList.size();
            } else {
                mAverageScore = 0;
            }
            MyLog.d(TAG, "平均分:" + mAverageScore);
        }
    }

    /**
     * 获取得分，算法为
     * curPostion 为当前播放的音频时间戳，一般为每一秒会取一次。
     * 则取 得分时间戳为  curPostion-1000<x<curPostion 的对应的得分即可
     * 保证 mMachineScoreModel 有序，采用二分法
     */
    public MachineScoreItem findMatchingScoreItemByTs(long curPostion) {
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

    public MachineScoreItem findMatchingScoreItemByNo(int lineNo) {
        int b = 0, e = mDataList.size() - 1;
        while (b <= e) {
            int mid = (b + e) / 2;
            MachineScoreItem midItem = mDataList.get(mid);
            if (midItem.getNo() == lineNo) {
                return midItem;
            } else if (midItem.getNo() > lineNo) {
                e = mid - 1;
            } else if (midItem.getNo() < lineNo) {
                b = mid + 1;
            }
        }
        return null;
    }

    public int tryGetTotalScoreByLine(int lineNo) {
        int curTotal = 0;
        for (int i = 0; i < mDataList.size(); i++) {
            MachineScoreItem machineScoreItem = mDataList.get(i);
            if (machineScoreItem.getNo() > lineNo) {
                break;
            } else {
                curTotal += machineScoreItem.getScore();
            }
        }
        return curTotal;
    }

    public void reset() {
        mDataList.clear();
        mAverageScore = -1;
        mTotalScore = -1;
    }

    public int getScoreLineNum() {
        return mDataList.size();
    }
}
