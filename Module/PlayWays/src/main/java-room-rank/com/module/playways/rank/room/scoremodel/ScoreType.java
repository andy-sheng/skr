package com.module.playways.rank.room.scoremodel;

public class ScoreType {
    /**
     * 未知
     */
    public static final int ST_UNKNOWN = 0;

    /**
     * 主段位
     */
    public static final int ST_RANKING = 1;

    /**
     * 子段位
     */
    public static final int ST_SUB_RANKING = 2;

    /**
     * 子段位当前星星数
     */
    public static final int ST_SUB_RANKING_STAR = 3;

    /**
     * 子段位星星数上限
     */
    public static final int ST_SUB_RANKING_TOTAL_STAR = 4;

    /**
     * 晋级赛开启状态值。可看作布尔值，scoreNow不为0表示处于某种晋级赛当中
     */
    public static final int ST_RANKING_UPGRADE = 5;

    /**
     * 晋级赛实际星星数
     */
    public static final int ST_RANKING_UPGRADE_STAR = 6;

    /**
     * 晋级赛星星数上限
     */
    public static final int ST_RANKING_UPGRADE_TOTAL_STAR = 7;

    /**
     * 实际战力值
     */
    public static final int ST_BATTLE_INDEX_REAL = 8;

    /**
     * 战力值上限
     */
    public static final int ST_BATTLE_INDEX_TOTAL = 9;

    /**
     * 战斗评价, sss or ss or s or a...
     */
    public static final int ST_BATTLE_RATING = 10;

    /**
     * 掉段保护需要的战力值。用作阈值，用scoreNow表示大小
     */
    public static final int ST_RANKING_PROTECT_TOTAL_BATTLE_INDEX = 11;

    /**
     * 总星星数
     */
    public static final int ST_STAR = 12;


}
