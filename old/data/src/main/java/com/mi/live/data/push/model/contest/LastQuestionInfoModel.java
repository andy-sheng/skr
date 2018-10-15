package com.mi.live.data.push.model.contest;

import com.wali.live.proto.LiveSummitProto;

/**
 * Created by zyh on 2018/1/17.
 *
 * @module 最后一题
 */

public class LastQuestionInfoModel {
    private int totalJoinNum;        //总共参与答题人数
    private float totalBonus;        //总奖金
    private float myBonus;           //每人奖金
    private int winNum;              //获奖人数

    public LastQuestionInfoModel(int totalJoinNum, float totalBonus, float perBonus, int winNum) {
        this.totalJoinNum = totalJoinNum;
        this.totalBonus = totalBonus;
        this.myBonus = perBonus;
        this.winNum = winNum;
    }

    public LastQuestionInfoModel(LiveSummitProto.LastQuestionInfo lastQuestionInfo) {
        this.totalJoinNum = lastQuestionInfo.getTotalNum();
        this.totalBonus = lastQuestionInfo.getTotalBonus();
        this.myBonus = lastQuestionInfo.getPerBonus();
        this.winNum = lastQuestionInfo.getNum();
    }

    public int getTotalJoinNum() {
        return totalJoinNum;
    }

    public float getTotalBonus() {
        return totalBonus;
    }

    public float getMyBonus() {
        return myBonus;
    }

    public int getWinNum() {
        return winNum;
    }

    @Override
    public String toString() {
        return "LastQuestionInfoModel{" +
                "totalJoinNum=" + totalJoinNum +
                ", totalBonus=" + totalBonus +
                ", myBonus=" + myBonus +
                ", winNum=" + winNum +
                '}';
    }
}
