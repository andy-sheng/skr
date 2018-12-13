package com.module.rankingmode.msg.process;

import com.common.log.MyLog;
import com.module.rankingmode.msg.BasePushInfo;
import com.module.rankingmode.msg.event.AppSwapEvent;
import com.module.rankingmode.msg.event.JoinActionEvent;
import com.module.rankingmode.msg.event.JoinNoticeEvent;
import com.module.rankingmode.msg.event.QuitGameEvent;
import com.module.rankingmode.msg.event.ReadyNoticeEvent;
import com.module.rankingmode.msg.event.RoundAndGameOverEvent;
import com.module.rankingmode.msg.event.RoundOverEvent;
import com.module.rankingmode.msg.event.SyncStatusEvent;
import com.module.rankingmode.prepare.model.JsonGameInfo;
import com.module.rankingmode.prepare.model.JsonGameReadyInfo;
import com.module.rankingmode.prepare.model.JsonRoundInfo;
import com.module.rankingmode.prepare.model.PlayerInfo;
import com.module.rankingmode.song.model.SongModel;
import com.zq.live.proto.Common.MusicInfo;
import com.zq.live.proto.Room.AppSwapMsg;
import com.zq.live.proto.Room.ERoomMsgType;
import com.zq.live.proto.Room.JoinActionMsg;
import com.zq.live.proto.Room.JoinNoticeMsg;
import com.zq.live.proto.Room.QuitGameMsg;
import com.zq.live.proto.Room.ReadyNoticeMsg;
import com.zq.live.proto.Room.RoomMsg;
import com.zq.live.proto.Room.RoundAndGameOverMsg;
import com.zq.live.proto.Room.RoundInfo;
import com.zq.live.proto.Room.RoundOverMsg;
import com.zq.live.proto.Room.SyncStatusMsg;

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
        } else if (msg.getMsgType() == ERoomMsgType.RM_QUIT_GAME) {
            processQuitGameMsg(basePushInfo, msg.getQuitGameMsg());
        } else if (msg.getMsgType() == ERoomMsgType.RM_APP_SWAP) {
            processAppSwapMsg(basePushInfo, msg.getAppSwapMsg());
        } else if (msg.getMsgType() == ERoomMsgType.RM_SYNC_STATUS) {
            processSyncStatusMsg(basePushInfo, msg.getSyncStatusMsg());
        }
    }

    @Override
    public ERoomMsgType[] acceptType() {
        return new ERoomMsgType[]{
                ERoomMsgType.RM_JOIN_ACTION, ERoomMsgType.RM_JOIN_NOTICE,
                ERoomMsgType.RM_READY_NOTICE, ERoomMsgType.RM_ROUND_OVER,
                ERoomMsgType.RM_ROUND_AND_GAME_OVER, ERoomMsgType.RM_QUIT_GAME,
                ERoomMsgType.RM_APP_SWAP, ERoomMsgType.RM_SYNC_STATUS
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
        List<PlayerInfo> playerInfos = new ArrayList<>();
        for (com.zq.live.proto.Room.PlayerInfo player : joinActionMsg.getPlayersList()){
            PlayerInfo playerInfo = new PlayerInfo();
            playerInfo.parse(player);
            playerInfos.add(playerInfo);
        }

        List<SongModel> songModels = new ArrayList<>();
        for (MusicInfo musicInfo : joinActionMsg.getCommonMusicInfoList()){
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
        JsonGameInfo jsonGameInfo = new JsonGameInfo();
        jsonGameInfo.parse(joinNoticeMsg);
        EventBus.getDefault().post(new JoinNoticeEvent(info, jsonGameInfo));
    }

    //准备游戏通知消息
    private void processReadyNoticeMsg(BasePushInfo info, ReadyNoticeMsg readyNoticeMsg) {
        if (readyNoticeMsg == null) {
            MyLog.d(TAG, "processReadyNoticeMsg" + " readyNoticeMsg == null");
            return;
        }

        JsonGameReadyInfo jsonGameReadyInfo = new JsonGameReadyInfo();
        jsonGameReadyInfo.parse(readyNoticeMsg);
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

        JsonRoundInfo currentRound = new JsonRoundInfo();
        currentRound.parse(roundOverMsgr.getCurrentRound());

        JsonRoundInfo nextRound = new JsonRoundInfo();
        nextRound.parse(roundOverMsgr.getNextRound());

        EventBus.getDefault().post(new RoundOverEvent(info, roundOverTimeMs, currentRound, nextRound));
    }

    //轮次和游戏结束通知消息
    private void processRoundAndGameOverMsg(BasePushInfo info, RoundAndGameOverMsg roundAndGameOverMsg) {
        if (roundAndGameOverMsg == null) {
            MyLog.d(TAG, "processRoundAndGameOverMsg" + " roundOverMsgr == null");
            return;
        }

        long roundOverTimeMs = roundAndGameOverMsg.getRoundOverTimeMs();

        EventBus.getDefault().post(new RoundAndGameOverEvent(info, roundOverTimeMs));
    }

    //退出游戏通知
    private void processQuitGameMsg(BasePushInfo info, QuitGameMsg quitGameMsg) {
        if (quitGameMsg == null) {
            MyLog.d(TAG, "processQuitGameMsg" + " quitGameMsg == null");
            return;
        }

        int quitUserId = quitGameMsg.getQuitUserID();
        long quitTimeMs = quitGameMsg.getQuitTimeMs();

        EventBus.getDefault().post(new QuitGameEvent(info, quitUserId, quitTimeMs));
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

        EventBus.getDefault().post(new SyncStatusEvent(info));
    }
}