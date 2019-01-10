package com.module.playways.rank.msg.process;

import com.common.log.MyLog;
import com.module.playways.rank.msg.event.ExitGameEvent;
import com.module.playways.rank.msg.event.MachineScoreEvent;
import com.module.playways.rank.msg.event.SyncStatusEvent;
import com.module.playways.rank.msg.event.VoteResultEvent;
import com.module.playways.rank.msg.BasePushInfo;
import com.module.playways.rank.msg.event.AppSwapEvent;
import com.module.playways.rank.msg.event.JoinActionEvent;
import com.module.playways.rank.msg.event.JoinNoticeEvent;
import com.module.playways.rank.msg.event.ReadyNoticeEvent;
import com.module.playways.rank.msg.event.RoundAndGameOverEvent;
import com.module.playways.rank.msg.event.RoundOverEvent;
import com.module.playways.rank.prepare.model.GameInfoModel;
import com.module.playways.rank.prepare.model.GameReadyModel;
import com.module.playways.rank.prepare.model.OnlineInfoModel;
import com.module.playways.rank.prepare.model.RoundInfoModel;
import com.module.playways.rank.prepare.model.PlayerInfoModel;

import com.module.playways.rank.room.scoremodel.ScoreDetailModel;
import com.module.playways.rank.room.scoremodel.UserScoreModel;

import com.module.playways.rank.room.model.VoteInfoModel;
import com.module.playways.rank.song.model.SongModel;
import com.zq.live.proto.Common.MusicInfo;
import com.zq.live.proto.Room.AppSwapMsg;
import com.zq.live.proto.Room.ERoomMsgType;
import com.zq.live.proto.Room.ExitGameAfterPlayMsg;
import com.zq.live.proto.Room.ExitGameBeforePlayMsg;
import com.zq.live.proto.Room.ExitGameOutRoundMsg;
import com.zq.live.proto.Room.JoinActionMsg;
import com.zq.live.proto.Room.JoinNoticeMsg;
import com.zq.live.proto.Room.MachineScore;
import com.zq.live.proto.Room.OnlineInfo;
import com.zq.live.proto.Room.ReadyNoticeMsg;
import com.zq.live.proto.Room.RoomMsg;
import com.zq.live.proto.Room.RoundAndGameOverMsg;
import com.zq.live.proto.Room.RoundOverMsg;
import com.zq.live.proto.Room.SyncStatusMsg;
import com.zq.live.proto.Room.UserScoreRecord;
import com.zq.live.proto.Room.VoteInfo;
import com.zq.live.proto.Room.VoteResultMsg;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

public class ChatRoomGameMsgProcess implements IPushChatRoomMsgProcess {

    public final static String TAG = "ChatRoomGameMsgProcess";

    @Override
    public void processRoomMsg(ERoomMsgType messageType, RoomMsg msg) {
        MyLog.d(TAG, "processRoomMsg" + " messageType=" + messageType.getValue());

        BasePushInfo basePushInfo = BasePushInfo.parse(msg);

        if (msg.getMsgType() == ERoomMsgType.RM_JOIN_ACTION) {
            processJoinActionMsg(basePushInfo, msg.getJoinActionMsg());
        } else if (msg.getMsgType() == ERoomMsgType.RM_JOIN_NOTICE) {
            processJoinNoticeMsg(basePushInfo, msg.getJoinNoticeMsg());
        } else if (msg.getMsgType() == ERoomMsgType.RM_READY_NOTICE) {
            processReadyNoticeMsg(basePushInfo, msg.getReadyNoticeMsg());
        } else if (msg.getMsgType() == ERoomMsgType.RM_ROUND_OVER) {
            processRoundOverMsg(basePushInfo, msg.getRoundOverMsg());
        } else if (msg.getMsgType() == ERoomMsgType.RM_ROUND_AND_GAME_OVER) {
            processRoundAndGameOverMsg(basePushInfo, msg.getRoundAndGameOverMsg());
        } else if (msg.getMsgType() == ERoomMsgType.RM_APP_SWAP) {
            processAppSwapMsg(basePushInfo, msg.getAppSwapMsg());
        } else if (msg.getMsgType() == ERoomMsgType.RM_SYNC_STATUS) {
            processSyncStatusMsg(basePushInfo, msg.getSyncStatusMsg());
        } else if (msg.getMsgType() == ERoomMsgType.RM_EXIT_GAME_BEFORE_PLAY) {
            processExitGameBeforePlay(basePushInfo, msg.getExitGameBeforePlayMsg());
        } else if (msg.getMsgType() == ERoomMsgType.RM_EXIT_GAME_AFTER_PLAY) {
            processExitGameAfterPlay(basePushInfo, msg.getExitGameAfterPlayMsg());
        } else if (msg.getMsgType() == ERoomMsgType.RM_EXIT_GAME_OUT_ROUND) {
            processExitGameOutRound(basePushInfo, msg.getExitGameOutRoundMsg());
        } else if (msg.getMsgType() == ERoomMsgType.RM_VOTE_RESULT) {
            processVoteResult(basePushInfo, msg.getVoteResultMsg());
        } else if (msg.getMsgType() == ERoomMsgType.RM_ROUND_MACHINE_SCORE) {
            processMachineScore(basePushInfo, msg.getMachineScore());
        }
    }

    @Override
    public ERoomMsgType[] acceptType() {
        return new ERoomMsgType[]{
                ERoomMsgType.RM_JOIN_ACTION, ERoomMsgType.RM_JOIN_NOTICE,
                ERoomMsgType.RM_READY_NOTICE, ERoomMsgType.RM_SYNC_STATUS,
                ERoomMsgType.RM_ROUND_OVER, ERoomMsgType.RM_ROUND_AND_GAME_OVER,
                ERoomMsgType.RM_APP_SWAP, ERoomMsgType.RM_EXIT_GAME_BEFORE_PLAY,
                ERoomMsgType.RM_EXIT_GAME_AFTER_PLAY, ERoomMsgType.RM_EXIT_GAME_OUT_ROUND,
                ERoomMsgType.RM_VOTE_RESULT, ERoomMsgType.RM_ROUND_MACHINE_SCORE
        };
    }

    //加入游戏指令消息
    private void processJoinActionMsg(BasePushInfo info, JoinActionMsg joinActionMsg) {
        if (joinActionMsg == null) {
            MyLog.d(TAG, "processJoinActionMsg" + " joinActionMsg == null");
            return;
        }

        int gameId = joinActionMsg.getGameID();
        long gameCreateMs = joinActionMsg.getCreateTimeMs();
        List<PlayerInfoModel> playerInfos = new ArrayList<>();
        for (com.zq.live.proto.Room.PlayerInfo player : joinActionMsg.getPlayersList()) {
            PlayerInfoModel playerInfo = new PlayerInfoModel();
            playerInfo.parse(player);
            playerInfos.add(playerInfo);
        }

        List<SongModel> songModels = new ArrayList<>();
        for (MusicInfo musicInfo : joinActionMsg.getCommonMusicInfoList()) {
            SongModel songModel = new SongModel();
            songModel.parse(musicInfo);
            songModels.add(songModel);
        }

        EventBus.getDefault().post(new JoinActionEvent(info, gameId, gameCreateMs, playerInfos, songModels));
    }

    //加入游戏通知消息
    private void processJoinNoticeMsg(BasePushInfo info, JoinNoticeMsg joinNoticeMsg) {
        if (joinNoticeMsg == null) {
            MyLog.d(TAG, "processJoinNoticeMsg" + " joinNoticeMsg == null");
            return;
        }
        GameInfoModel jsonGameInfo = new GameInfoModel();
        jsonGameInfo.parse(joinNoticeMsg);
        EventBus.getDefault().post(new JoinNoticeEvent(info, jsonGameInfo));
    }

    //准备游戏通知消息
    private void processReadyNoticeMsg(BasePushInfo info, ReadyNoticeMsg readyNoticeMsg) {
        if (readyNoticeMsg == null) {
            MyLog.d(TAG, "processReadyNoticeMsg" + " readyNoticeMsg == null");
            return;
        }

        GameReadyModel jsonGameReadyInfo = new GameReadyModel();
        jsonGameReadyInfo.parse(readyNoticeMsg);
        MyLog.d(TAG, " processReadyNoticeMsg " + " startTime = " + jsonGameReadyInfo.getJsonGameStartInfo().getStartTimeMs());
        EventBus.getDefault().post(new ReadyNoticeEvent(info, jsonGameReadyInfo));
    }

//    //准备并开始游戏通知消息
//    private void processReadyAndStartNoticeMsg(BasePushInfo info, ReadyAndStartNoticeMsg readyAndStartNoticeMsg) {
//        if (readyAndStartNoticeMsg == null) {
//            MyLog.d(TAG, "processReadyAndStartNoticeMsg" + " readyAndStartNoticeMsg == null");
//            return;
//        }
//
//        int readyUserID = readyAndStartNoticeMsg.getReadyUserID();   //准备用户ID
//        long readyTimeMs = readyAndStartNoticeMsg.getReadyTimeMs();  //准备的毫秒时间戳
//        long startTimeMS = readyAndStartNoticeMsg.getStartTimeMS();  //开始的毫秒时间戳
//        int firstUserID = readyAndStartNoticeMsg.getFirstUserID();   //第一个用户ID
//        int firstMusicID = readyAndStartNoticeMsg.getFirstMusicID(); //第一首歌曲ID
//
//        EventBus.getDefault().post(new ReadyAndStartNoticeEvent(info, readyUserID, readyTimeMs, startTimeMS, firstUserID, firstMusicID));
//
//    }

    //游戏轮次结束通知消息
    private void processRoundOverMsg(BasePushInfo info, RoundOverMsg roundOverMsgr) {
        if (roundOverMsgr == null) {
            MyLog.d(TAG, "processRoundOverMsg" + " roundOverMsgr == null");
            return;
        }

        long roundOverTimeMs = roundOverMsgr.getRoundOverTimeMs();

        RoundInfoModel currentRound = new RoundInfoModel();
        currentRound.parse(roundOverMsgr.getCurrentRound());

        RoundInfoModel nextRound = new RoundInfoModel();
        nextRound.parse(roundOverMsgr.getNextRound());

        EventBus.getDefault().post(new RoundOverEvent(info, roundOverTimeMs, currentRound, nextRound, roundOverMsgr.getExitUserID()));
    }

    //轮次和游戏结束通知消息
    private void processRoundAndGameOverMsg(BasePushInfo info, RoundAndGameOverMsg roundAndGameOverMsg) {
        if (roundAndGameOverMsg == null) {
            MyLog.d(TAG, "processRoundAndGameOverMsg" + " roundOverMsgr == null");
            return;
        }

        long roundOverTimeMs = roundAndGameOverMsg.getRoundOverTimeMs();

        // TODO: 2018/12/27 新增投票打分信息和分值信息
        List<VoteInfoModel> voteInfoModels = new ArrayList<>();
        for (VoteInfo voteInfo : roundAndGameOverMsg.getVoteInfoList()) {
            VoteInfoModel voteInfoModel = new VoteInfoModel();
            voteInfoModel.parse(voteInfo);
            voteInfoModels.add(voteInfoModel);
        }

        List<UserScoreModel> userScoreModels = new ArrayList<>();
        for (UserScoreRecord userScoreRecord : roundAndGameOverMsg.getUserScoreRecordList()) {
            UserScoreModel userScoreModel = new UserScoreModel();
            userScoreModel.parse(userScoreRecord);
            userScoreModels.add(userScoreModel);
        }
        MyLog.d(TAG, " processRoundAndGameOverMsg " + "roundOverTimeMs" + roundOverTimeMs);

        ScoreDetailModel scoreDetailModel = new ScoreDetailModel();
        scoreDetailModel.parse(userScoreModels);
        EventBus.getDefault().post(new RoundAndGameOverEvent(info, roundOverTimeMs, voteInfoModels, scoreDetailModel));
    }

    //app进程后台通知
    private void processAppSwapMsg(BasePushInfo info, AppSwapMsg appSwapMsg) {
        if (appSwapMsg == null) {
            MyLog.d(TAG, "processAppSwapMsg" + " appSwapMsg == null");
            return;
        }

        int swapUserId = appSwapMsg.getSwapUserID();
        long swapTimeMs = appSwapMsg.getSwapTimsMs();
        boolean swapOut = appSwapMsg.getSwapOut();
        boolean swapIn = appSwapMsg.getSwapIn();

        EventBus.getDefault().post(new AppSwapEvent(info, swapUserId, swapTimeMs, swapOut, swapIn));
    }

    //状态同步信令
    private void processSyncStatusMsg(BasePushInfo info, SyncStatusMsg syncStatusMsg) {
        if (syncStatusMsg == null) {
            MyLog.d(TAG, "processSyncStatusMsg" + " syncStatusMsg == null");
            return;
        }

        long syncStatusTimes = syncStatusMsg.getSyncStatusTimeMs();
        long gameOverTimeMs = syncStatusMsg.getGameOverTimeMs();

        List<OnlineInfoModel> onLineInfos = new ArrayList<>();
        for (OnlineInfo onlineInfo : syncStatusMsg.getOnlineInfoList()) {
            OnlineInfoModel jsonOnLineInfo = new OnlineInfoModel();
            jsonOnLineInfo.parse(onlineInfo);
            onLineInfos.add(jsonOnLineInfo);
        }

        RoundInfoModel currentInfo = new RoundInfoModel();
        currentInfo.parse(syncStatusMsg.getCurrentRound());

        RoundInfoModel nextInfo = new RoundInfoModel();
        nextInfo.parse(syncStatusMsg.getNextRound());

        MyLog.d(TAG, " processSyncStatusMsg " + "gameOverTimeMs =" + gameOverTimeMs);
        EventBus.getDefault().post(new SyncStatusEvent(info, syncStatusTimes, gameOverTimeMs, onLineInfos, currentInfo, nextInfo));
    }

    //退出游戏通知, 游戏开始前
    private void processExitGameBeforePlay(BasePushInfo basePushInfo, ExitGameBeforePlayMsg exitGameBeforePlayMsg) {
        if (exitGameBeforePlayMsg == null) {
            MyLog.d(TAG, "processExitGameBeforePlay" + " exitGameBeforePlayMsg == null");
            return;
        }

        int exitUserID = exitGameBeforePlayMsg.getExitUserID();
        long exitTimeMs = exitGameBeforePlayMsg.getExitTimeMs();

        EventBus.getDefault().post(new ExitGameEvent(basePushInfo, ExitGameEvent.EXIT_GAME_BEFORE_PLAY, exitUserID, exitTimeMs));
    }

    //退出游戏通知，游戏开始后
    private void processExitGameAfterPlay(BasePushInfo basePushInfo, ExitGameAfterPlayMsg exitGameAfterPlayMsg) {
        if (exitGameAfterPlayMsg == null) {
            MyLog.d(TAG, "processExitGameAfterPlay" + " exitGameAfterPlayMsg == null");
            return;
        }
        int exitUserID = exitGameAfterPlayMsg.getExitUserID();
        long exitTimeMs = exitGameAfterPlayMsg.getExitTimeMs();

        EventBus.getDefault().post(new ExitGameEvent(basePushInfo, ExitGameEvent.EXIT_GAME_AFTER_PLAY, exitUserID, exitTimeMs));
    }

    //退出游戏通知，游戏中非自己轮次
    private void processExitGameOutRound(BasePushInfo basePushInfo, ExitGameOutRoundMsg exitGameOutRoundMsg) {
        if (exitGameOutRoundMsg == null) {
            MyLog.d(TAG, "processExitGameOutRound" + " exitGameOutRoundMsg == null");
            return;
        }

        int exitUserID = exitGameOutRoundMsg.getExitUserID();
        long exitTimeMs = exitGameOutRoundMsg.getExitTimeMs();

        EventBus.getDefault().post(new ExitGameEvent(basePushInfo, ExitGameEvent.EXIT_GAME_OUT_ROUND, exitUserID, exitTimeMs));
    }

    //游戏投票结果消息
    private void processVoteResult(BasePushInfo basePushInfo, VoteResultMsg voteResultMsg) {

        List<VoteInfoModel> voteInfoModels = new ArrayList<>();
        for (VoteInfo voteInfo : voteResultMsg.getVoteInfoList()) {
            VoteInfoModel voteInfoModel = new VoteInfoModel();
            voteInfoModel.parse(voteInfo);
            voteInfoModels.add(voteInfoModel);
        }

        List<UserScoreModel> userScoreModels = new ArrayList<>();
        for (UserScoreRecord userScoreRecord : voteResultMsg.getUserScoreRecordList()) {
            UserScoreModel userScoreModel = new UserScoreModel();
            userScoreModel.parse(userScoreRecord);
            userScoreModels.add(userScoreModel);
        }

        ScoreDetailModel scoreDetailModel = new ScoreDetailModel();
        scoreDetailModel.parse(userScoreModels);

        EventBus.getDefault().post(new VoteResultEvent(basePushInfo, voteInfoModels, scoreDetailModel));
    }

    // 处理机器打分
    private void processMachineScore(BasePushInfo basePushInfo, MachineScore machineScore) {
        // TODO: 2019/1/4  完善再补充
        MachineScoreEvent machineScoreEvent = new MachineScoreEvent(basePushInfo, machineScore.userID, machineScore.no, machineScore.score);
        EventBus.getDefault().post(machineScoreEvent);
    }

}