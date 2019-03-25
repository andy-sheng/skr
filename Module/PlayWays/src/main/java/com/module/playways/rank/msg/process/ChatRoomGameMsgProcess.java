package com.module.playways.rank.msg.process;

import com.common.core.myinfo.MyUserInfoManager;
import com.common.log.MyLog;
import com.module.playways.rank.msg.BasePushInfo;
import com.module.playways.rank.msg.event.AccBeginEvent;
import com.module.playways.rank.msg.event.AppSwapEvent;
import com.module.playways.rank.msg.event.ExitGameEvent;
import com.module.playways.rank.msg.event.JoinActionEvent;
import com.module.playways.rank.msg.event.JoinNoticeEvent;
import com.module.playways.rank.msg.event.MachineScoreEvent;
import com.module.playways.rank.msg.event.PkBurstLightMsgEvent;
import com.module.playways.rank.msg.event.PkLightOffMsgEvent;

import com.module.playways.rank.msg.event.QCoinChangeEvent;

import com.module.playways.rank.msg.event.QChangeMusicTagEvent;

import com.module.playways.rank.msg.event.QExitGameMsgEvent;
import com.module.playways.rank.msg.event.QGameBeginEvent;
import com.module.playways.rank.msg.event.QGetSingChanceMsgEvent;
import com.module.playways.rank.msg.event.QJoinActionEvent;
import com.module.playways.rank.msg.event.QJoinNoticeEvent;
import com.module.playways.rank.msg.event.QKickUserReqEvent;
import com.module.playways.rank.msg.event.QKickUserResultEvent;
import com.module.playways.rank.msg.event.QLightBurstMsgEvent;
import com.module.playways.rank.msg.event.QLightOffMsgEvent;
import com.module.playways.rank.msg.event.QNoPassSingMsgEvent;
import com.module.playways.rank.msg.event.QRoundAndGameOverMsgEvent;
import com.module.playways.rank.msg.event.QRoundOverMsgEvent;
import com.module.playways.rank.msg.event.QSyncStatusMsgEvent;
import com.module.playways.rank.msg.event.QWantSingChanceMsgEvent;
import com.module.playways.rank.msg.event.ReadyNoticeEvent;
import com.module.playways.rank.msg.event.RoundAndGameOverEvent;
import com.module.playways.rank.msg.event.RoundOverEvent;
import com.module.playways.rank.msg.event.SyncStatusEvent;
import com.module.playways.rank.msg.event.VoteResultEvent;
import com.zq.live.proto.Room.AppSwapMsg;
import com.zq.live.proto.Room.ERoomMsgType;
import com.zq.live.proto.Room.ExitGameAfterPlayMsg;
import com.zq.live.proto.Room.ExitGameBeforePlayMsg;
import com.zq.live.proto.Room.ExitGameOutRoundMsg;
import com.zq.live.proto.Room.JoinActionMsg;
import com.zq.live.proto.Room.JoinNoticeMsg;
import com.zq.live.proto.Room.MachineScore;
import com.zq.live.proto.Room.PKBLightMsg;
import com.zq.live.proto.Room.PKMLightMsg;
import com.zq.live.proto.Room.QBLightMsg;

import com.zq.live.proto.Room.QCoinChangeMsg;

import com.zq.live.proto.Room.QChangeMusicTag;

import com.zq.live.proto.Room.QExitGameMsg;
import com.zq.live.proto.Room.QGameBeginMsg;
import com.zq.live.proto.Room.QGetSingChanceMsg;
import com.zq.live.proto.Room.QJoinActionMsg;
import com.zq.live.proto.Room.QJoinNoticeMsg;
import com.zq.live.proto.Room.QKickUserRequestMsg;
import com.zq.live.proto.Room.QKickUserResultMsg;
import com.zq.live.proto.Room.QMLightMsg;
import com.zq.live.proto.Room.QNoPassSingMsg;
import com.zq.live.proto.Room.QRoundAndGameOverMsg;
import com.zq.live.proto.Room.QRoundOverMsg;
import com.zq.live.proto.Room.QSyncStatusMsg;
import com.zq.live.proto.Room.QWantSingChanceMsg;
import com.zq.live.proto.Room.ReadyNoticeMsg;
import com.zq.live.proto.Room.RoomMsg;
import com.zq.live.proto.Room.RoundAndGameOverMsg;
import com.zq.live.proto.Room.RoundOverMsg;
import com.zq.live.proto.Room.SyncStatusMsg;
import com.zq.live.proto.Room.VoteResultMsg;

import org.greenrobot.eventbus.EventBus;


public class ChatRoomGameMsgProcess implements IPushChatRoomMsgProcess {

    public final static String TAG = "ChatRoomGameMsgProcess";

    @Override
    public void processRoomMsg(ERoomMsgType messageType, RoomMsg msg) {
        MyLog.d(TAG, "processRoomMsg" + " messageType=" + messageType.getValue());
        BasePushInfo basePushInfo = BasePushInfo.parse(msg);
        MyLog.d(TAG, "processRoomMsg" + " timeMs=" + basePushInfo.getTimeMs());

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
        } else if (msg.getMsgType() == ERoomMsgType.RM_ROUND_ACC_BEGIN) {
            processAccBeigin(basePushInfo);
        } else if (msg.getMsgType() == ERoomMsgType.RM_Q_WANT_SING_CHANCE) {
            processQWantSingChanceMsg(basePushInfo, msg.getQWantSingChanceMsg());
        } else if (msg.getMsgType() == ERoomMsgType.RM_Q_GET_SING_CHANCE) {
            processQGetSingChanceMsg(basePushInfo, msg.getQGetSingChanceMsg());
        } else if (msg.getMsgType() == ERoomMsgType.RM_Q_SYNC_STATUS) {
            processQSyncStatusMsg(basePushInfo, msg.getQSyncStatusMsg());
        } else if (msg.getMsgType() == ERoomMsgType.RM_Q_ROUND_OVER) {
            processQRoundOverMsg(basePushInfo, msg.getQRoundOverMsg());
        } else if (msg.getMsgType() == ERoomMsgType.RM_Q_ROUND_AND_GAME_OVER) {
            processQRoundAndGameOverMsg(basePushInfo, msg.getQRoundAndGameOverMsg());
        } else if (msg.getMsgType() == ERoomMsgType.RM_Q_NO_PASS_SING) {
            processQNoPassSingMsg(basePushInfo, msg.getQNoPassSingMsg());
        } else if (msg.getMsgType() == ERoomMsgType.RM_Q_EXIT_GAME) {
            processQExitGameMsg(basePushInfo, msg.getQExitGameMsg());
        } else if (msg.getMsgType() == ERoomMsgType.RM_PK_BLIGHT) {
            processPkBurstLightMsg(basePushInfo, msg.getPkBLightMsg());
        } else if (msg.getMsgType() == ERoomMsgType.RM_PK_MLIGHT) {
            processPkLightOffMsg(basePushInfo, msg.getPkMLightMsg());
        } else if (msg.getMsgType() == ERoomMsgType.RM_Q_BLIGHT) {
            processGrabLightBurstMsg(basePushInfo, msg.getQBLightMsg());
        } else if (msg.getMsgType() == ERoomMsgType.RM_Q_MLIGHT) {
            processGrabLightOffMsg(basePushInfo, msg.getQMLightMsg());
        } else if (msg.getMsgType() == ERoomMsgType.RM_Q_JOIN_NOTICE) {
            processGrabJoinNoticeMsg(basePushInfo, msg.getQJoinNoticeMsg());
        } else if (msg.getMsgType() == ERoomMsgType.RM_Q_JOIN_ACTION) {
            processGrabJoinActionMsg(basePushInfo, msg.getQJoinActionMsg());
        } else if (msg.getMsgType() == ERoomMsgType.RM_Q_KICK_USER_REQUEST) {
            processGrabKickRequest(basePushInfo, msg.getQKickUserRequestMsg());
        } else if (msg.getMsgType() == ERoomMsgType.RM_Q_KICK_USER_RESULT) {
            processGrabKickResult(basePushInfo, msg.getQKickUserResultMsg());
        } else if (msg.getMsgType() == ERoomMsgType.RM_Q_GAME_BEGIN) {
            processGrabGameBegin(basePushInfo, msg.getQGameBeginMsg());
        } else if (msg.getMsgType() == ERoomMsgType.RM_Q_COIN_CHANGE) {
            processGrabCoinChange(basePushInfo, msg.getQCoinChangeMsg());
        } else if (msg.getMsgType() == ERoomMsgType.RM_Q_CHANGE_MUSIC_TAG) {
            processChangeMusicTag(basePushInfo, msg.getQChangeMusicTag());
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
                ERoomMsgType.RM_VOTE_RESULT, ERoomMsgType.RM_ROUND_MACHINE_SCORE,
                ERoomMsgType.RM_ROUND_ACC_BEGIN, ERoomMsgType.RM_Q_WANT_SING_CHANCE,
                ERoomMsgType.RM_Q_GET_SING_CHANCE, ERoomMsgType.RM_Q_SYNC_STATUS,
                ERoomMsgType.RM_Q_ROUND_OVER, ERoomMsgType.RM_Q_ROUND_AND_GAME_OVER,
                ERoomMsgType.RM_Q_NO_PASS_SING, ERoomMsgType.RM_Q_EXIT_GAME,
                ERoomMsgType.RM_PK_BLIGHT, ERoomMsgType.RM_PK_MLIGHT,
                ERoomMsgType.RM_Q_BLIGHT, ERoomMsgType.RM_Q_MLIGHT,
                ERoomMsgType.RM_Q_JOIN_NOTICE, ERoomMsgType.RM_Q_JOIN_ACTION,
                ERoomMsgType.RM_Q_KICK_USER_REQUEST, ERoomMsgType.RM_Q_KICK_USER_RESULT,
                ERoomMsgType.RM_Q_GAME_BEGIN, ERoomMsgType.RM_Q_COIN_CHANGE
        };
    }

    //加入游戏指令消息
    private void processJoinActionMsg(BasePushInfo info, JoinActionMsg joinActionMsg) {
        if (joinActionMsg != null) {
            JoinActionEvent joinActionEvent = new JoinActionEvent(info, joinActionMsg);
            EventBus.getDefault().post(joinActionEvent);
        } else {
            MyLog.w(TAG, "processJoinActionMsg" + " info=" + info + " joinActionMsg = null");
        }
    }

    //加入游戏通知消息
    private void processJoinNoticeMsg(BasePushInfo info, JoinNoticeMsg joinNoticeMsg) {
        if (joinNoticeMsg != null) {
            JoinNoticeEvent joinNoticeEvent = new JoinNoticeEvent(info, joinNoticeMsg);
            EventBus.getDefault().post(joinNoticeEvent);
        } else {
            MyLog.w(TAG, "processJoinNoticeMsg" + " info=" + info + " joinNoticeMsg = null");
        }
    }

    //准备游戏通知消息
    private void processReadyNoticeMsg(BasePushInfo info, ReadyNoticeMsg readyNoticeMsg) {
        if (readyNoticeMsg != null) {
            ReadyNoticeEvent readyNoticeEvent = new ReadyNoticeEvent(info, readyNoticeMsg);
            EventBus.getDefault().post(readyNoticeEvent);
        } else {
            MyLog.w(TAG, "processReadyNoticeMsg" + " info=" + info + " readyNoticeMsg = null");
        }
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
        if (roundOverMsgr != null) {
            RoundOverEvent roundOverEvent = new RoundOverEvent(info, roundOverMsgr);
            EventBus.getDefault().post(roundOverEvent);
        } else {
            MyLog.w(TAG, "processRoundOverMsg" + " info=" + info + " roundOverMsgr = null");
        }
    }

    //轮次和游戏结束通知消息
    private void processRoundAndGameOverMsg(BasePushInfo info, RoundAndGameOverMsg roundAndGameOverMsg) {
        if (roundAndGameOverMsg != null) {
            RoundAndGameOverEvent roundAndGameOverEvent = new RoundAndGameOverEvent(info, roundAndGameOverMsg);
            EventBus.getDefault().post(roundAndGameOverEvent);
        } else {
            MyLog.w(TAG, "processRoundAndGameOverMsg" + " info=" + info + " roundAndGameOverMsg = null");
        }
    }

    //app进程后台通知
    private void processAppSwapMsg(BasePushInfo info, AppSwapMsg appSwapMsg) {
        if (appSwapMsg != null) {
            AppSwapEvent appSwapEvent = new AppSwapEvent(info, appSwapMsg);
            EventBus.getDefault().post(appSwapEvent);
        } else {
            MyLog.w(TAG, "processAppSwapMsg" + " info=" + info + " appSwapMsg = null");
        }
    }

    //状态同步信令
    private void processSyncStatusMsg(BasePushInfo info, SyncStatusMsg syncStatusMsg) {
        if (syncStatusMsg != null) {
            SyncStatusEvent syncStatusEvent = new SyncStatusEvent(info, syncStatusMsg);
            EventBus.getDefault().post(syncStatusEvent);
        } else {
            MyLog.w(TAG, "processSyncStatusMsg" + " info=" + info + " syncStatusMsg = null");
        }
    }

    //退出游戏通知, 游戏开始前
    private void processExitGameBeforePlay(BasePushInfo info, ExitGameBeforePlayMsg exitGameBeforePlayMsg) {
        if (exitGameBeforePlayMsg != null) {
            ExitGameEvent exitGameEvent = new ExitGameEvent(info, exitGameBeforePlayMsg);
            EventBus.getDefault().post(exitGameEvent);
        } else {
            MyLog.w(TAG, "processExitGameBeforePlay" + " basePushInfo=" + info + " exitGameBeforePlayMsg = null");
        }
    }

    //退出游戏通知，游戏开始后
    private void processExitGameAfterPlay(BasePushInfo info, ExitGameAfterPlayMsg exitGameAfterPlayMsg) {
        if (exitGameAfterPlayMsg != null) {
            ExitGameEvent exitGameEvent = new ExitGameEvent(info, exitGameAfterPlayMsg);
            EventBus.getDefault().post(exitGameEvent);
        } else {
            MyLog.w(TAG, "processExitGameAfterPlay" + " basePushInfo=" + info + " exitGameAfterPlayMsg = null");
        }
    }

    //退出游戏通知，游戏中非自己轮次
    private void processExitGameOutRound(BasePushInfo info, ExitGameOutRoundMsg exitGameOutRoundMsg) {
        if (exitGameOutRoundMsg != null) {
            ExitGameEvent exitGameEvent = new ExitGameEvent(info, exitGameOutRoundMsg);
            EventBus.getDefault().post(exitGameEvent);
        } else {
            MyLog.w(TAG, "processExitGameOutRound" + " basePushInfo=" + info + " exitGameOutRoundMsg = null");
        }
    }

    //游戏投票结果消息
    private void processVoteResult(BasePushInfo basePushInfo, VoteResultMsg voteResultMsg) {
        if (voteResultMsg != null) {
            VoteResultEvent voteResultEvent = new VoteResultEvent(basePushInfo, voteResultMsg);
            EventBus.getDefault().post(voteResultEvent);
        } else {
            MyLog.w(TAG, "processVoteResult" + " basePushInfo=" + basePushInfo + " voteResultMsg = null");
        }
    }

    // 处理机器打分
    private void processMachineScore(BasePushInfo basePushInfo, MachineScore machineScore) {
        if (machineScore != null) {
            MachineScoreEvent machineScoreEvent = new MachineScoreEvent(basePushInfo, machineScore);
            EventBus.getDefault().post(machineScoreEvent);
        } else {
            MyLog.w(TAG, "processMachineScore" + " basePushInfo=" + basePushInfo + " machineScore = null");
        }
    }

    private void processQWantSingChanceMsg(BasePushInfo basePushInfo, QWantSingChanceMsg qWantSingChanceMsg) {
        if (qWantSingChanceMsg != null) {
            QWantSingChanceMsgEvent machineScoreEvent = new QWantSingChanceMsgEvent(basePushInfo, qWantSingChanceMsg);
            EventBus.getDefault().post(machineScoreEvent);
        } else {
            MyLog.w(TAG, "processQWantSingChanceMsg" + " basePushInfo=" + basePushInfo + " qWantSingChanceMsg = null");
        }
    }

    private void processQGetSingChanceMsg(BasePushInfo basePushInfo, QGetSingChanceMsg qGetSingChanceMsg) {
        if (qGetSingChanceMsg != null) {
            QGetSingChanceMsgEvent machineScoreEvent = new QGetSingChanceMsgEvent(basePushInfo, qGetSingChanceMsg);
            EventBus.getDefault().post(machineScoreEvent);
        } else {
            MyLog.w(TAG, "processQGetSingChanceMsg" + " basePushInfo=" + basePushInfo + " qGetSingChanceMsg = null");
        }
    }

    private void processQSyncStatusMsg(BasePushInfo basePushInfo, QSyncStatusMsg qSyncStatusMsg) {
        if (qSyncStatusMsg != null) {
            QSyncStatusMsgEvent machineScoreEvent = new QSyncStatusMsgEvent(basePushInfo, qSyncStatusMsg);
            EventBus.getDefault().post(machineScoreEvent);
        } else {
            MyLog.w(TAG, "processQSyncStatusMsg" + " basePushInfo=" + basePushInfo + " qSyncStatusMsg = null");
        }
    }

    private void processQRoundOverMsg(BasePushInfo basePushInfo, QRoundOverMsg qRoundOverMsg) {
        if (qRoundOverMsg != null) {
            QRoundOverMsgEvent machineScoreEvent = new QRoundOverMsgEvent(basePushInfo, qRoundOverMsg);
            EventBus.getDefault().post(machineScoreEvent);
        } else {
            MyLog.w(TAG, "processQRoundOverMsg" + " basePushInfo=" + basePushInfo + " qRoundOverMsg = null");
        }
    }

    private void processQRoundAndGameOverMsg(BasePushInfo basePushInfo, QRoundAndGameOverMsg qRoundAndGameOverMsg) {
        if (qRoundAndGameOverMsg != null) {
            QRoundAndGameOverMsgEvent machineScoreEvent = new QRoundAndGameOverMsgEvent(basePushInfo, qRoundAndGameOverMsg);
            EventBus.getDefault().post(machineScoreEvent);
        } else {
            MyLog.w(TAG, "processQRoundAndGameOverMsg" + " basePushInfo=" + basePushInfo + " qRoundAndGameOverMsg = null");
        }
    }

    private void processQNoPassSingMsg(BasePushInfo basePushInfo, QNoPassSingMsg qNoPassSingMsg) {
        if (qNoPassSingMsg != null) {
            QNoPassSingMsgEvent machineScoreEvent = new QNoPassSingMsgEvent(basePushInfo, qNoPassSingMsg);
            EventBus.getDefault().post(machineScoreEvent);
        } else {
            MyLog.w(TAG, "processQNoPassSingMsg" + " basePushInfo=" + basePushInfo + " qNoPassSingMsg = null");
        }
    }

    private void processQExitGameMsg(BasePushInfo basePushInfo, QExitGameMsg qExitGameMsg) {
        if (qExitGameMsg != null) {
            QExitGameMsgEvent machineScoreEvent = new QExitGameMsgEvent(basePushInfo, qExitGameMsg);
            EventBus.getDefault().post(machineScoreEvent);
        } else {
            MyLog.w(TAG, "processQExitGameMsg" + " basePushInfo=" + basePushInfo + " qExitGameMsg = null");
        }
    }

    private void processPkBurstLightMsg(BasePushInfo basePushInfo, PKBLightMsg qNoPassSingMsg) {
        if (qNoPassSingMsg != null) {
            PkBurstLightMsgEvent machineScoreEvent = new PkBurstLightMsgEvent(basePushInfo, qNoPassSingMsg);
            EventBus.getDefault().post(machineScoreEvent);
        } else {
            MyLog.w(TAG, "processPkBurstLightMsg" + " basePushInfo=" + basePushInfo + " qNoPassSingMsg = null");
        }
    }

    private void processPkLightOffMsg(BasePushInfo basePushInfo, PKMLightMsg qExitGameMsg) {
        if (qExitGameMsg != null) {
            PkLightOffMsgEvent machineScoreEvent = new PkLightOffMsgEvent(basePushInfo, qExitGameMsg);
            EventBus.getDefault().post(machineScoreEvent);
        } else {
            MyLog.w(TAG, "processPkLightOffMsg" + " basePushInfo=" + basePushInfo + " qExitGameMsg = null");
        }
    }


    private void processGrabLightOffMsg(BasePushInfo basePushInfo, QMLightMsg msg) {
        if (msg != null) {
            QLightOffMsgEvent event = new QLightOffMsgEvent(basePushInfo, msg);
            EventBus.getDefault().post(event);
        } else {
            MyLog.w(TAG, "processPkLightOffMsg" + " basePushInfo=" + basePushInfo + " qExitGameMsg = null");
        }
    }

    private void processGrabLightBurstMsg(BasePushInfo basePushInfo, QBLightMsg msg) {
        if (msg != null) {
            QLightBurstMsgEvent event = new QLightBurstMsgEvent(basePushInfo, msg);
            EventBus.getDefault().post(event);
        } else {
            MyLog.w(TAG, "processPkLightOffMsg" + " basePushInfo=" + basePushInfo + " qExitGameMsg = null");
        }
    }

    private void processGrabJoinNoticeMsg(BasePushInfo basePushInfo, QJoinNoticeMsg msg) {
        if (msg != null) {
            QJoinNoticeEvent event = new QJoinNoticeEvent(basePushInfo, msg);
            EventBus.getDefault().post(event);
        } else {
            MyLog.w(TAG, "processPkLightOffMsg" + " basePushInfo=" + basePushInfo + " qExitGameMsg = null");
        }
    }

    private void processGrabJoinActionMsg(BasePushInfo basePushInfo, QJoinActionMsg msg) {
        if (msg != null) {
            QJoinActionEvent event = new QJoinActionEvent(basePushInfo, msg);
            EventBus.getDefault().post(event);
        } else {
            MyLog.w(TAG, "processPkLightOffMsg" + " basePushInfo=" + basePushInfo + " qExitGameMsg = null");
        }
    }

    private void processAccBeigin(BasePushInfo basePushInfo) {
        if (basePushInfo != null) {
            AccBeginEvent machineScoreEvent = new AccBeginEvent(basePushInfo, basePushInfo.getSender().getUserID());
            EventBus.getDefault().post(machineScoreEvent);
        } else {
            MyLog.w(TAG, "processAccBeigin" + " basePushInfo = null ");
        }
    }

    private void processGrabKickResult(BasePushInfo basePushInfo, QKickUserResultMsg qKickUserResultMsg) {
        if (basePushInfo != null && qKickUserResultMsg != null) {
            // 过滤，被踢人 也可以放在收事件的地方，但是觉得没有必要
            if (MyUserInfoManager.getInstance().getUid() == qKickUserResultMsg.getKickUserID()) {
                QKickUserResultEvent qKickUserResultEvent = new QKickUserResultEvent(basePushInfo, qKickUserResultMsg);
                EventBus.getDefault().post(qKickUserResultEvent);
                return;
            }
            // 过滤下, 所有投同意票
            if (qKickUserResultMsg.getGiveYesVoteUserIDsList() != null) {
                for (Integer integer : qKickUserResultMsg.getGiveYesVoteUserIDsList()) {
                    if (integer == MyUserInfoManager.getInstance().getUid()) {
                        QKickUserResultEvent qKickUserResultEvent = new QKickUserResultEvent(basePushInfo, qKickUserResultMsg);
                        EventBus.getDefault().post(qKickUserResultEvent);
                        return;
                    }
                }
            }

            // 过滤下, 所有投不同意票
            if (qKickUserResultMsg.getGiveNoVoteUserIDsList() != null) {
                for (Integer integer : qKickUserResultMsg.getGiveNoVoteUserIDsList()) {
                    if (integer == MyUserInfoManager.getInstance().getUid()) {
                        QKickUserResultEvent qKickUserResultEvent = new QKickUserResultEvent(basePushInfo, qKickUserResultMsg);
                        EventBus.getDefault().post(qKickUserResultEvent);
                        return;
                    }
                }
            }

            // 过滤下, 所有未知票
            if (qKickUserResultMsg.getGiveUnknownVoteUserIDsList() != null) {
                for (Integer integer : qKickUserResultMsg.getGiveUnknownVoteUserIDsList()) {
                    if (integer == MyUserInfoManager.getInstance().getUid()) {
                        QKickUserResultEvent qKickUserResultEvent = new QKickUserResultEvent(basePushInfo, qKickUserResultMsg);
                        EventBus.getDefault().post(qKickUserResultEvent);
                        return;
                    }
                }
            }
        } else {
            MyLog.w(TAG, "processGrabKickResult" + " basePushInfo = null or qKickUserSuccessMsg = null");
        }
    }

    private void processGrabKickRequest(BasePushInfo basePushInfo, QKickUserRequestMsg qKickUserRequestMsg) {
        if (basePushInfo != null && qKickUserRequestMsg != null) {
            // 过滤下,所有投票者
            for (Integer integer : qKickUserRequestMsg.getOtherOnlineUserIDsList()) {
                if (integer == MyUserInfoManager.getInstance().getUid()) {
                    QKickUserReqEvent qKickUserReqEvent = new QKickUserReqEvent(basePushInfo, qKickUserRequestMsg);
                    EventBus.getDefault().post(qKickUserReqEvent);
                    return;
                }
            }
        } else {
            MyLog.w(TAG, "processGrabKickRequest" + " basePushInfo = null or qKickUserRequestMsg = null");
        }
    }

    private void processGrabGameBegin(BasePushInfo basePushInfo, QGameBeginMsg qGameBeginMsg) {
        if (basePushInfo != null && qGameBeginMsg != null) {
            // 过滤下,所有投票者
            QGameBeginEvent event = new QGameBeginEvent(basePushInfo, qGameBeginMsg);
            EventBus.getDefault().post(event);
        } else {
            MyLog.w(TAG, "processGrabKickRequest" + " basePushInfo = null or qKickUserRequestMsg = null");
        }
    }

    private void processGrabCoinChange(BasePushInfo basePushInfo, QCoinChangeMsg qCoinChangeMsg) {
        if (basePushInfo != null && qCoinChangeMsg != null) {
            QCoinChangeEvent event = new QCoinChangeEvent(basePushInfo, qCoinChangeMsg);
            EventBus.getDefault().post(event);
        } else {
            MyLog.w(TAG, "processGrabKickRequest" + " basePushInfo = null or qKickUserRequestMsg = null");
        }
    }

    private void processChangeMusicTag(BasePushInfo basePushInfo, QChangeMusicTag qChangeMusicTag) {
        if (basePushInfo != null && qChangeMusicTag != null) {
            QChangeMusicTagEvent event = new QChangeMusicTagEvent(basePushInfo, qChangeMusicTag);
            EventBus.getDefault().post(event);
        } else {
            MyLog.w(TAG, "processChangeMusicTag" + " basePushInfo = null or QChangeMusicTag = null");
        }
    }
}