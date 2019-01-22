package com.module.playways.rank.room.model;

// 胜负平结果
public class WinResultModel {
    public static final int InvalidEWinType = 0;   // 无效占位
    public static final int Win = 1;       //胜
    public static final int Lose = 2;      //负
    public static final int Draw = 3;      //平
    public static final int Flee = 4;      //逃跑
    public static final int NotVote = 5;   //未投票

    int useID;   //用户id
    int type;    //结果类型

    public int getUseID() {
        return useID;
    }

    public void setUseID(int useID) {
        this.useID = useID;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "WinResultModel{" +
                "useID=" + useID +
                ", type=" + type +
                '}';
    }
}
