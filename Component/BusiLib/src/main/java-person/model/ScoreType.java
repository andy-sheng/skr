package model;

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
     * 晋级赛段位
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

}
