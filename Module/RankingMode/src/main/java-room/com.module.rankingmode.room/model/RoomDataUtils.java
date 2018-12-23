package com.module.rankingmode.room.model;

import com.common.core.myinfo.MyUserInfoManager;
import com.common.log.MyLog;
import com.module.rankingmode.prepare.model.PlayerInfo;
import com.module.rankingmode.prepare.model.RoundInfoModel;
import com.module.rankingmode.song.model.SongModel;

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
     *
     * @param infoModel1
     * @param infoModel2
     * @return
     */
    public static boolean roundSeqLarger(RoundInfoModel infoModel1, RoundInfoModel infoModel2) {
        if (infoModel2 == null) {
            // 已经是结束状态
            return false;
        }
        if (infoModel1 == null) {
            return true;
        }
        return infoModel1.getRoundSeq() > infoModel2.getRoundSeq();
    }

    /**
     * 找到轮次等于 seq 的RoundInfoModel
     *
     * @param jsonRoundInfo
     * @param seq
     * @return
     */
    public static RoundInfoModel findRoundInfoBySeq(List<RoundInfoModel> jsonRoundInfo, int seq) {
        for (RoundInfoModel infoModel : jsonRoundInfo) {
            if (infoModel.getRoundSeq() == seq) {
                return infoModel;
            }
        }
        return null;
    }

    public static int getUidOfRoundInfo(RoundInfoModel infoModel) {
        if (infoModel == null) {
            return 0;
        }
        return infoModel.getUserID();
    }

    public static int getSeqOfRoundInfo(RoundInfoModel infoModel) {
        if (infoModel == null) {
            return 0;
        }
        return infoModel.getRoundSeq();
    }

    /**
     * 根据用户id 尝试找到该用户对应的轮次
     *
     * @param jsonRoundInfo
     * @param uid
     * @return
     */
    public static RoundInfoModel getRoundInfoByUserId(List<RoundInfoModel> jsonRoundInfo, int uid) {
        for (RoundInfoModel infoModel : jsonRoundInfo) {
            if (infoModel.getUserID() == uid) {
                return infoModel;
            }
        }
        return null;
    }

    /**
     * 根据id找songmodel
     *
     * @param
     * @param uid
     * @return
     */
    public static SongModel getPlayerInfoUserId(List<PlayerInfo> playerInfos, long uid) {
        try {
            for (PlayerInfo infoModel : playerInfos) {
                if (infoModel.getUserInfo().getUserId() == uid) {
                    return infoModel.getSongList().get(0);
                }
            }
        }catch (Exception e){
            MyLog.e(e);
        }

        return null;
    }

    /**
     * 以本地时间估算 距离 realRoundInfo 结束还有几秒
     * 假设 realRoundInfo 还有3秒结束，返回3000
     * 假设已经结束3秒了 返回 -3000
     *
     * @param roomData
     * @param realRoundInfo
     */
    public static int estimateTs2End(RoomData roomData, RoundInfoModel realRoundInfo) {
        if (realRoundInfo == null) {
            MyLog.d("estimateTs2End realRoundInfo=" + realRoundInfo);
            return 0;
        }
        long ts = realRoundInfo.getSingEndMs() + roomData.getGameStartTs() + roomData.getShiftTs();
        return (int) (ts - System.currentTimeMillis());
    }

    public static boolean isMyRound(RoundInfoModel infoModel) {
        return infoModel != null && infoModel.getUserID() == MyUserInfoManager.getInstance().getUid();
    }
}
