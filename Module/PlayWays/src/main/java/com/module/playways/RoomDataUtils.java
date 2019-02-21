package com.module.playways;

import com.common.core.myinfo.MyUserInfoManager;
import com.common.log.MyLog;
import com.common.utils.U;
import com.module.playways.rank.prepare.model.PlayerInfoModel;
import com.module.playways.rank.prepare.model.RoundInfoModel;
import com.module.playways.rank.song.model.SongModel;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * 针对排位模式的数据处理
 */
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
     * 轮次的seq是否大于
     * 1是否大于等于2
     *
     * @param infoModel1
     * @param infoModel2
     * @return
     */
    public static boolean roundSeqLargerOrEqual(RoundInfoModel infoModel1, RoundInfoModel infoModel2) {
        if (infoModel2 == null) {
            // 已经是结束状态
            return false;
        }
        if (infoModel1 == null) {
            return true;
        }
        return infoModel1.getRoundSeq() >= infoModel2.getRoundSeq();
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
     * @param uid
     * @return
     */
    public static RoundInfoModel getRoundInfoByUserId(RoomData roomData, int uid) {
        for (RoundInfoModel infoModel : roomData.getRoundInfoModelList()) {
            if (infoModel.getUserID() == uid) {
                return infoModel;
            }
        }
        return null;
    }

    /**
     * 根据轮次信息 尝试找到该用户对应的轮次
     *
     * @return
     */
    public static RoundInfoModel getRoundInfoBySeq(RoomData roomData, int seq) {
        for (RoundInfoModel infoModel : roomData.getRoundInfoModelList()) {
            if (infoModel.getRoundSeq() == seq) {
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
    public static SongModel getPlayerSongInfoUserId(List<PlayerInfoModel> playerInfos, long uid) {
        try {
            for (PlayerInfoModel infoModel : playerInfos) {
                if (infoModel.getUserInfo().getUserId() == uid) {
                    return infoModel.getSongList().get(0);
                }
            }
        } catch (Exception e) {
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

    public static boolean isRobotRound(RoundInfoModel infoModel, List<PlayerInfoModel> playerInfoModels) {
        if (infoModel != null) {
            int uid = infoModel.getUserID();
            for (PlayerInfoModel playerInfoModel : playerInfoModels) {
                if (playerInfoModel.getUserInfo().getUserId() == uid) {
                    return playerInfoModel.isSkrer();
                }
            }
        }
        return false;
    }

    public static PlayerInfoModel getPlayerInfoById(RoomData roomData, long uid) {
        for (PlayerInfoModel playerInfo :
                roomData.getPlayerInfoList()) {
            if (playerInfo.getUserInfo().getUserId() == uid) {
                return playerInfo;
            }
        }

        return null;
    }

    /**
     * @param roundInfoModel
     * @return
     */
    public static long getSongDuration(RoundInfoModel roundInfoModel) {
        if (roundInfoModel == null) {
            return 0;
        }

        return roundInfoModel.getSingEndMs() - roundInfoModel.getSingBeginMs();
    }

    public static String getSaveAudioForAiFilePath() {
        String saveAudioForAiFilePath = U.getAppInfoUtils().getFilePathInSubDir("upload", RoomData.AUDIO_FOR_AI_PATH);
        return saveAudioForAiFilePath;
    }

    public static String getSaveMatchingSocreForAiFilePath() {
        String saveAudioForAiFilePath = U.getAppInfoUtils().getFilePathInSubDir("upload", RoomData.MATCHING_SCORE_FOR_AI_PATH);
        return saveAudioForAiFilePath;
    }

    public static boolean isThisUserRound(RoundInfoModel infoModel, int userId) {
        if (infoModel != null && infoModel.getUserID() == userId) {
            return true;
        }
        return false;
    }

    /**
     * 与真实轮次是否一致
     * @param eventSeq
     * @param roomData
     * @return
     */
    public static boolean isCurrentRound(int eventSeq, RoomData roomData) {
        if (roomData != null) {
            if (roomData.getRealRoundInfo() == null) {
                return false;
            }

            if (roomData.getRealRoundInfo().getRoundSeq() == eventSeq) {
                return true;
            }
        }
        return false;
    }

    public static RoundInfoModel getRoundInfoFromRoundInfoListInGrabMode(RoomData roomData, RoundInfoModel roundInfoModel) {
        if (roundInfoModel == null) {
            return null;
        }
        for (RoundInfoModel roundInfo : roomData.getRoundInfoModelList()) {
            if (roundInfo.getRoundSeq() == roundInfoModel.getRoundSeq()) {
                roundInfo.tryUpdateGrabByRoundInfoModel(roundInfoModel,false);
                return roundInfo;
            }
        }
        return null;
    }

    public static RoundInfoModel getRoundInfoFromRoundInfoListInRankMode(RoomData roomData, RoundInfoModel roundInfoModel) {
        if (roundInfoModel == null) {
            return null;
        }

        for (RoundInfoModel roundInfo : roomData.getRoundInfoModelList()) {
            if (roundInfo.getRoundSeq() == roundInfoModel.getRoundSeq()) {
                roundInfo.tryUpdateRankRoundInfoModel(roundInfoModel,false);
                return roundInfo;
            }
        }

        return null;
    }
}
