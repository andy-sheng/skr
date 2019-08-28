package com.module.playways.room.prepare.model;

import java.io.Serializable;

public abstract class BaseRoundInfoModel implements Serializable {
    public final String TAG = "BaseRoundInfoModel";
    public static final int TYPE_RANK = 1;
    public static final int TYPE_GRAB = 2;
    public static final int TYPE_RACE = 3;

    protected int roundSeq;// 本局轮次

    /**
     * 一唱到底 结束原因
     * 0未知
     * 1上个轮次结束
     * 2没人抢唱
     * 3当前玩家退出
     * 4多人灭灯
     * 5自己放弃演唱
     * <p>
     * 排位赛 结束原因
     * 0 未知
     * 1 正常
     * 2 玩家退出
     * 3 多人灭灯
     * 4
     */
    protected int overReason; // 结束的原因


    public BaseRoundInfoModel() {

    }

    public abstract int getType();


    public void setRoundSeq(int roundSeq) {
        this.roundSeq = roundSeq;
    }

    public int getRoundSeq() {
        return roundSeq;
    }

    public int getOverReason() {
        return overReason;
    }

    public void setOverReason(int overReason) {
        this.overReason = overReason;
    }



    @Override
    public boolean equals(Object o) {
        BaseRoundInfoModel that = (BaseRoundInfoModel) o;
        if (that == null) {
            return false;
        }

        if (roundSeq != that.roundSeq) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result = 0;
        result = 31 * result + roundSeq;
        return result;
    }

    public abstract void tryUpdateRoundInfoModel(BaseRoundInfoModel round, boolean notify);

    @Override
    public String toString() {
        return "RoundInfoModel{" +
                "type=" + getType() +
                ", roundSeq=" + roundSeq +
                ", overReason=" + overReason +
                '}';
    }

}