package com.module.playways;

import com.common.core.myinfo.MyUserInfoManager;
import com.common.core.userinfo.model.UserInfoModel;
import com.common.utils.U;
import com.module.playways.grab.room.GrabRoomData;
import com.module.playways.grab.room.model.ChorusRoundInfoModel;
import com.module.playways.grab.room.model.GrabPlayerInfoModel;
import com.module.playways.grab.room.model.GrabRoundInfoModel;
import com.module.playways.grab.room.model.SPkRoundInfoModel;
import com.module.playways.mic.room.MicRoomData;
import com.module.playways.mic.room.model.MicPlayerInfoModel;
import com.module.playways.race.room.RaceRoomData;
import com.module.playways.race.room.model.RacePlayerInfoModel;
import com.module.playways.race.room.model.RaceRoundInfoModel;
import com.module.playways.room.prepare.model.BaseRoundInfoModel;

import java.util.List;

//import com.module.playways.room.room.RankRoomData;
//import com.module.playways.room.room.model.RankPlayerInfoModel;
//import com.module.playways.room.room.model.RankRoundInfoModel;

/**
 * 针对排位模式的数据处理
 */
public class RoomDataUtils {
//    /**
//     * 找到首轮演唱的轮次
//     *
//     * @param jsonRoundInfo
//     * @return
//     */
//    public static RankRoundInfoModel findFirstRoundInfo(List<RankRoundInfoModel> jsonRoundInfo) {
//        Collections.sort(jsonRoundInfo, new Comparator<RankRoundInfoModel>() {
//            @Override
//            public int compare(RankRoundInfoModel r1, RankRoundInfoModel r2) {
//                return r1.getRoundSeq() - r2.getRoundSeq();
//            }
//        });
//        return jsonRoundInfo.get(0);
//    }
//
//    /**
//     * 是否是同一个轮次
//     *
//     * @param infoModel1
//     * @param infoModel2
//     * @return
//     */
//    public static boolean roundInfoEqual(RankRoundInfoModel infoModel1, RankRoundInfoModel infoModel2) {
//        if (infoModel1 == null && infoModel2 == null) {
//            return true;
//        }
//        if (infoModel1 != null) {
//            return infoModel1.equals(infoModel2);
//        }
//        if (infoModel2 != null) {
//            return infoModel2.equals(infoModel1);
//        }
//        return false;
//    }

    /**
     * 轮次的seq是否大于
     * 1是否大于2
     *
     * @param infoModel1
     * @param infoModel2
     * @return
     */
    public static <T extends BaseRoundInfoModel> boolean roundSeqLarger(T infoModel1, T infoModel2) {
        if (infoModel2 == null) {
            // 已经是结束状态
            return false;
        }
        if (infoModel1 == null) {
            return true;
        }
        return infoModel1.getRoundSeq() > infoModel2.getRoundSeq();
    }

//    /**
//     * 轮次的seq是否大于
//     * 1是否大于等于2
//     *
//     * @param infoModel1
//     * @param infoModel2
//     * @return
//     */
//    public static boolean roundSeqLargerOrEqual(RankRoundInfoModel infoModel1, RankRoundInfoModel infoModel2) {
//        if (infoModel2 == null) {
//            // 已经是结束状态
//            return false;
//        }
//        if (infoModel1 == null) {
//            return true;
//        }
//        return infoModel1.getRoundSeq() >= infoModel2.getRoundSeq();
//    }


    public static <T extends BaseRoundInfoModel> int getSeqOfRoundInfo(T infoModel) {
        if (infoModel == null) {
            return 0;
        }
        return infoModel.getRoundSeq();
    }

//    /**
//     * 根据用户id 尝试找到该用户对应的轮次
//     *
//     * @param uid
//     * @return
//     */
//    public static RankRoundInfoModel getRoundInfoByUserId(RankRoomData roomData, int uid) {
//        for (RankRoundInfoModel infoModel : roomData.getRoundInfoModelList()) {
//            if (infoModel.getUserID() == uid) {
//                return infoModel;
//            }
//        }
//        return null;
//    }
//
//    /**
//     * 根据轮次信息 尝试找到该用户对应的轮次
//     *
//     * @return
//     */
//    public static RankRoundInfoModel getRoundInfoBySeq(RankRoomData roomData, int seq) {
//        for (RankRoundInfoModel infoModel : roomData.getRoundInfoModelList()) {
//            if (infoModel.getRoundSeq() == seq) {
//                return infoModel;
//            }
//        }
//        return null;
//    }

//    /**
//     * 根据id找songmodel
//     *
//     * @param
//     * @param uid
//     * @return
//     */
//    public static SongModel getPlayerSongInfoUserId(List<RankPlayerInfoModel> playerInfos, long uid) {
//        try {
//            for (RankPlayerInfoModel infoModel : playerInfos) {
//                if (infoModel.getUserInfo().getUserId() == uid) {
//                    return infoModel.getSongList().get(0);
//                }
//            }
//        } catch (Exception e) {
//            MyLog.e(e);
//        }
//
//        return null;
//    }

//    /**
//     * 以本地时间估算 距离 realRoundInfo 结束还有几秒
//     * 假设 realRoundInfo 还有3秒结束，返回3000
//     * 假设已经结束3秒了 返回 -3000
//     *
//     * @param roomData
//     * @param realRoundInfo
//     */
//    public static int estimateTs2End(RankRoomData roomData, RankRoundInfoModel realRoundInfo) {
//        if (realRoundInfo == null) {
//            MyLog.d("estimateTs2End realRoundInfo=" + realRoundInfo);
//            return 0;
//        }
//        long ts = realRoundInfo.getSingEndMs() + roomData.getGameStartTs() + roomData.getShiftTs();
//
//        return (int) (ts - System.currentTimeMillis());
//    }

    public static <T extends BaseRoundInfoModel> boolean isMyRound(T infoModel) {
        if (infoModel instanceof GrabRoundInfoModel) {
            return ((GrabRoundInfoModel) infoModel).singBySelf();
        }
//        if (infoModel instanceof RankRoundInfoModel) {
//            return ((RankRoundInfoModel) infoModel).getUserID() == MyUserInfoManager.getInstance().getUid();
//        }
        if (infoModel instanceof RaceRoundInfoModel) {
            return ((RaceRoundInfoModel) infoModel).isSingerNowBySelf();
        }
        return false;
    }
//
//    public static boolean isRobotRound(RankRoundInfoModel infoModel, List<RankPlayerInfoModel> playerInfoModels) {
//        if (infoModel != null) {
//            int uid = infoModel.getUserID();
//            for (PlayerInfoModel playerInfoModel : playerInfoModels) {
//                if (playerInfoModel.getUserInfo().getUserId() == uid) {
//                    return playerInfoModel.isSkrer();
//                }
//            }
//        }
//        return false;
//    }

    public static RacePlayerInfoModel getPlayerInfoById(RaceRoomData roomData, int uid) {
        return roomData.getPlayerOrWaiterInfoModel(uid);
    }

    //无论观众还是玩家都用这个函数，观众的话真实的名字，如果是玩家是系统给的昵称
    public static String getRaceDisplayNickName(RaceRoomData roomData, UserInfoModel userInfoModel) {
        RacePlayerInfoModel racePlayerInfoModel = RoomDataUtils.getPlayerInfoById(roomData, userInfoModel.getUserId());
        if (racePlayerInfoModel == null) {
            //观众
            return userInfoModel.getNickname();
        }

        return racePlayerInfoModel.getFakeUserInfo() != null ? racePlayerInfoModel.getFakeUserInfo().getNickName() : "";
    }

    //无论观众还是玩家都用这个函数，观众的话真实的avatar，如果是玩家话，如果揭面了展示真实的avatar，如果蒙面状态系统给的昵称
    public static String getRaceDisplayAvatar(RaceRoomData roomData, UserInfoModel userInfoModel) {
        if (userInfoModel.getUserId() == MyUserInfoManager.INSTANCE.getUid()) {
            return MyUserInfoManager.INSTANCE.getAvatar();
        }

        RacePlayerInfoModel racePlayerInfoModel = RoomDataUtils.getPlayerInfoById(roomData, userInfoModel.getUserId());
        if (racePlayerInfoModel == null) {
            //观众
            return userInfoModel.getAvatar();
        }

        if (roomData.isFakeForMe(userInfoModel.getUserId())) {
            return racePlayerInfoModel.getFakeUserInfo() != null ? racePlayerInfoModel.getFakeUserInfo().getAvatarUrl() : "";
        }

        return racePlayerInfoModel.getUserInfo().getAvatar();
    }

//    public static RankPlayerInfoModel getPlayerInfoById(RankRoomData roomData, int uid) {
//        for (RankPlayerInfoModel playerInfo : roomData.getPlayerAndWaiterInfoList()) {
//            if (playerInfo.getUserInfo().getUserId() == uid) {
//                return playerInfo;
//            }
//        }
//        return null;
//    }

    public static GrabPlayerInfoModel getPlayerInfoById(GrabRoomData roomData, int uid) {
        return roomData.getPlayerOrWaiterInfoModel(uid);
    }

    public static MicPlayerInfoModel getPlayerInfoById(MicRoomData roomData, int uid) {
        return roomData.getPlayerOrWaiterInfoModel(uid);
    }


    public static String getSaveAudioForAiFilePath() {
        String saveAudioForAiFilePath = U.getAppInfoUtils().getFilePathInSubDir("upload", BaseRoomData.Companion.getAUDIO_FOR_AI_PATH());
        return saveAudioForAiFilePath;
    }

    public static String getSaveMatchingSocreForAiFilePath() {
        String saveAudioForAiFilePath = U.getAppInfoUtils().getFilePathInSubDir("upload", BaseRoomData.Companion.getMATCHING_SCORE_FOR_AI_PATH());
        return saveAudioForAiFilePath;
    }

//    public static boolean isThisUserRound(RankRoundInfoModel infoModel, int userId) {
//        if (infoModel != null && infoModel.getUserID() == userId) {
//            return true;
//        }
//        return false;
//    }

    /**
     * 一般与push无关的，想要确定是本轮内的操作用这个判断
     * 与真实正在运行的轮次是否一致
     *
     * @param eventSeq
     * @param roomData
     * @return
     */
    public static <T extends BaseRoundInfoModel> boolean isCurrentRunningRound(int eventSeq, BaseRoomData<T> roomData) {
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

    /**
     * 一般push的消息都要用这个判断
     * 与正在运行的或者是将要运行的轮次是否一致
     *
     * @param eventSeq
     * @param roomData
     * @return
     */
    public static boolean isCurrentExpectingRound(int eventSeq, GrabRoomData roomData) {
        if (roomData != null) {
            if (roomData.getExpectRoundInfo() == null) {
                return false;
            }
            if (roomData.getExpectRoundInfo().getRoundSeq() == eventSeq) {
                return true;
            }
        }
        return false;
    }

    /**
     * 这个轮次的演唱者
     *
     * @param now
     * @param uid
     * @return
     */
    public static boolean isRoundSinger(GrabRoundInfoModel now, long uid) {
        if (now == null) {
            return false;
        }
        {
            List<SPkRoundInfoModel> list = now.getsPkRoundInfoModels();
            for (SPkRoundInfoModel infoModel : list) {
                if (infoModel.getUserID() == uid) {
                    return true;
                }
            }
        }
        {
            List<ChorusRoundInfoModel> list = now.getChorusRoundInfoModels();
            for (ChorusRoundInfoModel infoModel : list) {
                if (infoModel.getUserID() == uid) {
                    return true;
                }
            }
        }
        {
            if (now.getUserID() == uid) {
                return true;
            }
        }
        return false;

    }

}
