package com.module.rankingmode.room.model;

import com.module.rankingmode.prepare.model.RoundInfoModel;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class RoomDataUtils {
    /**
     * 找到首轮演唱的轮次
     *
     * @param jsonRoundInfo
     * @return
     */
    public static RoundInfoModel findFirstRoundInfo(List<RoundInfoModel> jsonRoundInfo) {
        Collections.sort(jsonRoundInfo, new Comparator<RoundInfoModel>() {
            @Override
            public int compare(RoundInfoModel r1, RoundInfoModel r2) {
                return r1.getRoundSeq() - r2.getRoundSeq();
            }
        });
        return jsonRoundInfo.get(0);
    }

    /**
     * 是否是同一个轮次
     *
     * @param infoModel1
     * @param infoModel2
     * @return
     */
    public static boolean roundInfoEqual(RoundInfoModel infoModel1, RoundInfoModel infoModel2) {
        if (infoModel1 == null && infoModel2 == null) {
            return true;
        }
        if (infoModel1 != null) {
            return infoModel1.equals(infoModel2);
        }
        if (infoModel2 != null) {
            return infoModel2.equals(infoModel1);
        }
        return false;
    }

    /**
     * 轮次的seq是否大于
     * 1是否大于2
     * @param infoModel1
     * @param infoModel2
     * @return
     */
    public static boolean roundSeqLarger(RoundInfoModel infoModel1, RoundInfoModel infoModel2) {
        if (infoModel2 == null) {
            // 已经是结束状态
            return false;
        }
        if(infoModel1 == null){
            return true;
        }
        return infoModel1.getRoundSeq() > infoModel2.getRoundSeq();
    }
}
