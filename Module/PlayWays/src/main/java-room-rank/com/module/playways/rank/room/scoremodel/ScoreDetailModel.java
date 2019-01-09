package com.module.playways.rank.room.scoremodel;

import com.common.log.MyLog;

import java.util.List;

public class ScoreDetailModel {

    private RankLevelModel mRankScore;           //主段位
    private RankLevelModel mSubRankScore;        //子段位
    private UserScoreModel mRankStarScore;       //子段位星星数    (会有UserScoreItem)
    private TotalLimit mTotalStarLimit;          //子段位星星数上限
    private UserScoreModel mUpgradeScore;        //晋级赛段位(仅用是否为空来判断)
    private UserScoreModel mUpgradeStarScore;    //晋级赛星星数    (会有UserScoreItem)
    private TotalLimit mUpTotalStarLimit;        //晋级赛星星数上限
    private UserScoreModel mBattleRealScore;     //实际战力分数值  (会有UserScoreItem)
    private TotalLimit mBattleTotalLimit;        //战力分数值上限
    private int mBattleRatingScore;              //战斗评价, sss or ss or s or a...

    public RankLevelModel getRankScore() {
        return mRankScore;
    }

    public void setRankScore(RankLevelModel rankScore) {
        mRankScore = rankScore;
    }

    public RankLevelModel getSubRankScore() {
        return mSubRankScore;
    }

    public void setSubRankScore(RankLevelModel subRankScore) {
        mSubRankScore = subRankScore;
    }

    public UserScoreModel getRankStarScore() {
        return mRankStarScore;
    }

    public void setRankStarScore(UserScoreModel rankStarScore) {
        mRankStarScore = rankStarScore;
    }

    public TotalLimit getTotalStarLimit() {
        return mTotalStarLimit;
    }

    public void setTotalStarLimit(TotalLimit totalStarLimit) {
        mTotalStarLimit = totalStarLimit;
    }

    public UserScoreModel getUpgradeScore() {
        return mUpgradeScore;
    }

    public void setUpgradeScore(UserScoreModel upgradeScore) {
        mUpgradeScore = upgradeScore;
    }

    public UserScoreModel getUpgradeStarScore() {
        return mUpgradeStarScore;
    }

    public void setUpgradeStarScore(UserScoreModel upgradeStarScore) {
        mUpgradeStarScore = upgradeStarScore;
    }

    public TotalLimit getUpTotalStarLimit() {
        return mUpTotalStarLimit;
    }

    public void setUpTotalStarLimit(TotalLimit upTotalStarLimit) {
        mUpTotalStarLimit = upTotalStarLimit;
    }

    public UserScoreModel getBattleRealScore() {
        return mBattleRealScore;
    }

    public void setBattleRealScore(UserScoreModel battleRealScore) {
        mBattleRealScore = battleRealScore;
    }

    public TotalLimit getBattleTotalLimit() {
        return mBattleTotalLimit;
    }

    public void setBattleTotalLimit(TotalLimit battleTotalLimit) {
        mBattleTotalLimit = battleTotalLimit;
    }

    public int getBattleRatingScore() {
        return mBattleRatingScore;
    }

    public void setBattleRatingScore(int battleRatingScore) {
        mBattleRatingScore = battleRatingScore;
    }

    // 处理将服务器给的一堆list中有效数据提取出来
    public void parse(List<UserScoreModel> list) {
        if (list == null || list.size() == 0) {
            MyLog.e("List<UserScoreModel> == null");
            return;
        }

        for (UserScoreModel userScoreModel : list) {
            switch (userScoreModel.getScoreType()) {
                case ScoreType.ST_UNKNOWN:
                    break;
                case ScoreType.ST_RANKING: {
                    RankLevelModel rankLevelModel = new RankLevelModel();
                    rankLevelModel.setLevelNow(userScoreModel.getScoreNow());
                    rankLevelModel.setLevelBefore(userScoreModel.getScoreBefore());
                    rankLevelModel.setLevelNowDesc(userScoreModel.getScoreNowDesc());
                    rankLevelModel.setLevelBeforeDesc(userScoreModel.getScoreBeforeDesc());
                    this.setRankScore(rankLevelModel);
                    break;
                }
                case ScoreType.ST_SUB_RANKING: {
                    RankLevelModel rankLevelModel = new RankLevelModel();
                    rankLevelModel.setLevelNow(userScoreModel.getScoreNow());
                    rankLevelModel.setLevelBefore(userScoreModel.getScoreBefore());
                    rankLevelModel.setLevelNowDesc(userScoreModel.getScoreNowDesc());
                    rankLevelModel.setLevelBeforeDesc(userScoreModel.getScoreBeforeDesc());
                    this.setSubRankScore(rankLevelModel);
                    break;
                }
                case ScoreType.ST_SUB_RANKING_STAR: {
                    this.setRankStarScore(userScoreModel);
                    break;
                }
                case ScoreType.ST_SUB_RANKING_TOTAL_STAR: {
                    TotalLimit limit = new TotalLimit();
                    limit.setLimitNow(userScoreModel.getScoreNow());
                    limit.setLimitBefore(userScoreModel.getScoreBefore());
                    this.setTotalStarLimit(limit);
                    break;
                }
                case ScoreType.ST_RANKING_UPGRADE: {
                    this.setUpgradeScore(userScoreModel);
                    break;
                }
                case ScoreType.ST_RANKING_UPGRADE_STAR: {
                    this.setUpgradeStarScore(userScoreModel);
                    break;
                }
                case ScoreType.ST_RANKING_UPGRADE_TOTAL_STAR: {
                    TotalLimit limit = new TotalLimit();
                    limit.setLimitNow(userScoreModel.getScoreNow());
                    limit.setLimitBefore(userScoreModel.getScoreBefore());
                    this.setTotalStarLimit(limit);
                    break;
                }
                case ScoreType.ST_BATTLE_INDEX_REAL: {
                    this.setBattleRealScore(userScoreModel);
                    break;
                }
                case ScoreType.ST_BATTLE_INDEX_TOTAL: {
                    TotalLimit limit = new TotalLimit();
                    limit.setLimitNow(userScoreModel.getScoreNow());
                    limit.setLimitBefore(userScoreModel.getScoreBefore());
                    this.setTotalStarLimit(limit);
                    break;
                }
                case ScoreType.ST_BATTLE_RATING: {
                    this.setBattleRatingScore(userScoreModel.getScoreNow());
                    break;
                }
                default:
                    break;
            }
        }
    }

}
