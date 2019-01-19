package com.module.playways.rank.room.model.score;

import com.zq.live.proto.Room.EFightForceWhy;
import com.zq.live.proto.Room.EStarWhy;

import java.io.Serializable;
import java.util.List;

// 战绩变动信息
public class ScoreChangeModel implements Serializable {

    private int userID;                       //用户id
    private List<ScoreItemModel> items;        //用户星星或战绩变动记录

    public int getUserID() {
        return userID;
    }

    public void setUserID(int userID) {
        this.userID = userID;
    }

    public List<ScoreItemModel> getItems() {
        return items;
    }

    public void setItems(List<ScoreItemModel> items) {
        this.items = items;
    }

    // 整个星星数的变化 （表示星星变化才有）
    public int getStarTotalChange() {
        int total = 0;
        if (items != null && items.size() > 0) {
            for (ScoreItemModel scoreItemModel : items) {
                total = total + scoreItemModel.getScore();
            }
        }

        return total;
    }

    // 战力值引起的星星的变化 （表示星星变化才有）
    public int getStarBattleChange() {
        int battle = 0;
        if (items != null && items.size() > 0) {
            for (ScoreItemModel scoreItemModel : items) {
                if (scoreItemModel.getIndex() == EStarWhy.FullFightForce.getValue()) {
                    battle = battle + scoreItemModel.getScore();
                }

                if (scoreItemModel.getIndex() == EStarWhy.RankProtectionIncr.getValue()) {
                    battle = battle + scoreItemModel.getScore();
                }
            }
        }

        return battle;
    }

    // 非战力值引起的星星的变化 （表示星星变化才有）
    public int getStarNormalChange() {
        int normal = 0;
        if (items != null && items.size() > 0) {
            for (ScoreItemModel scoreItemModel : items) {
                if (scoreItemModel.getIndex() != EStarWhy.FullFightForce.getValue()
                        && scoreItemModel.getIndex() != EStarWhy.RankProtectionIncr.getValue()) {
                    normal = normal + scoreItemModel.getScore();
                }
            }
        }

        return normal;
    }

    // 是否战力值满，兑换星星（表示战力变化才有）
    public boolean isExchangeStar() {
        if (items != null && items.size() > 0) {
            for (ScoreItemModel scoreItemModel : items) {
                if (scoreItemModel.getIndex() == EFightForceWhy.ExchangeStarDecr.getValue()) {
                    return true;
                }
            }
        }
        return false;
    }

    // 是否掉段保护，兑换星星（表示战力变化才有）
    public boolean isProtectRank() {
        if (items != null && items.size() > 0) {
            for (ScoreItemModel scoreItemModel : items) {
                if (scoreItemModel.getIndex() == EFightForceWhy.ProtectRankingDecr.getValue()) {
                    return true;
                }
            }
        }
        return false;
    }

    // 真实战力变化
    public int getBattleChange() {
        int total = 0;
        if (items != null && items.size() > 0) {
            for (ScoreItemModel scoreItemModel : items) {
                if (scoreItemModel.getIndex() != EFightForceWhy.ProtectRankingDecr.getValue()
                        && scoreItemModel.getIndex() != EFightForceWhy.ExchangeStarDecr.getValue()) {
                    total = total + scoreItemModel.getScore();
                }
            }
        }
        return total;
    }

}
