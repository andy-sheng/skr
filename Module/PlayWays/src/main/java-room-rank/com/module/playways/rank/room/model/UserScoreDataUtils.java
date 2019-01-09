package com.module.playways.rank.room.model;

import com.common.log.MyLog;
import com.zq.live.proto.Room.ScoreItem;
import com.zq.live.proto.Room.UserScoreRecord;

import java.util.ArrayList;
import java.util.List;

import model.ScoreType;
import com.module.playways.rank.room.scoremodel.UserScoreItem;
import com.module.playways.rank.room.scoremodel.UserScoreModel;
import com.module.playways.rank.room.scoremodel.RankLevelModel;
import com.module.playways.rank.room.scoremodel.ScoreDetailModel;
import com.module.playways.rank.room.scoremodel.TotalLimit;

public class UserScoreDataUtils {

    public static void transform(UserScoreModel userScoreModel, UserScoreRecord userScoreRecord) {
        if (userScoreModel == null || userScoreRecord == null) {
            MyLog.e("UserScoreModel or UserScoreRecord == null");
            return;
        }

        userScoreModel.setUserID(userScoreRecord.getUserID());
        userScoreModel.setScoreType(userScoreRecord.getScoreType().getValue());
        userScoreModel.setScoreNow(userScoreRecord.getScoreNow());
        userScoreModel.setScoreBefore(userScoreRecord.getScoreBefore());
        userScoreModel.setScoreTypeDesc(userScoreRecord.getScoreTypeDesc());
        userScoreModel.setScoreNowDesc(userScoreRecord.getScoreNowDesc());
        userScoreModel.setScoreBeforeDesc(userScoreRecord.getScoreBeforeDesc());
        List<UserScoreItem> userScoreItemList = new ArrayList<>();
        for (ScoreItem scoreItem : userScoreRecord.getItemsList()) {
            UserScoreItem userScoreItem = new UserScoreItem();
            transform(userScoreItem, scoreItem);
            userScoreItemList.add(userScoreItem);
        }
        userScoreModel.setItems(userScoreItemList);

    }

    public static void transform(UserScoreItem userScoreItem, ScoreItem scoreItem) {
        if (userScoreItem == null || scoreItem == null) {
            MyLog.e("UserScoreItem or ScoreItem == null");
            return;
        }

        userScoreItem.setWhy(scoreItem.getWhy());
        userScoreItem.setScore(scoreItem.getScore());
    }

    // 处理将服务器给的一堆list中有效数据提取出来
    public static void transform(ScoreDetailModel scoreDetailModel, List<UserScoreModel> list) {
        if (scoreDetailModel == null || list == null || list.size() == 0) {
            MyLog.e("ScoreDetailModel or List<UserScoreModel> == null");
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
                    scoreDetailModel.setRankScore(rankLevelModel);
                    break;
                }
                case ScoreType.ST_SUB_RANKING: {
                    RankLevelModel rankLevelModel = new RankLevelModel();
                    rankLevelModel.setLevelNow(userScoreModel.getScoreNow());
                    rankLevelModel.setLevelBefore(userScoreModel.getScoreBefore());
                    rankLevelModel.setLevelNowDesc(userScoreModel.getScoreNowDesc());
                    rankLevelModel.setLevelBeforeDesc(userScoreModel.getScoreBeforeDesc());
                    scoreDetailModel.setSubRankScore(rankLevelModel);
                    break;
                }
                case ScoreType.ST_SUB_RANKING_STAR: {
                    scoreDetailModel.setRankStarScore(userScoreModel);
                    break;
                }
                case ScoreType.ST_SUB_RANKING_TOTAL_STAR: {
                    TotalLimit limit = new TotalLimit();
                    limit.setLimitNow(userScoreModel.getScoreNow());
                    limit.setLimitBefore(userScoreModel.getScoreBefore());
                    scoreDetailModel.setTotalStarLimit(limit);
                    break;
                }
                case ScoreType.ST_RANKING_UPGRADE: {
                    scoreDetailModel.setUpgradeScore(userScoreModel);
                    break;
                }
                case ScoreType.ST_RANKING_UPGRADE_STAR: {
                    scoreDetailModel.setUpgradeStarScore(userScoreModel);
                    break;
                }
                case ScoreType.ST_RANKING_UPGRADE_TOTAL_STAR: {
                    TotalLimit limit = new TotalLimit();
                    limit.setLimitNow(userScoreModel.getScoreNow());
                    limit.setLimitBefore(userScoreModel.getScoreBefore());
                    scoreDetailModel.setTotalStarLimit(limit);
                    break;
                }
                case ScoreType.ST_BATTLE_INDEX_REAL: {
                    scoreDetailModel.setBattleRealScore(userScoreModel);
                    break;
                }
                case ScoreType.ST_BATTLE_INDEX_TOTAL: {
                    TotalLimit limit = new TotalLimit();
                    limit.setLimitNow(userScoreModel.getScoreNow());
                    limit.setLimitBefore(userScoreModel.getScoreBefore());
                    scoreDetailModel.setTotalStarLimit(limit);
                    break;
                }
                case ScoreType.ST_BATTLE_RATING: {
                    scoreDetailModel.setBattleRatingScore(userScoreModel.getScoreNow());
                    break;
                }
                default:
                    break;
            }
        }
    }
}
