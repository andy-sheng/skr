package com.module.rankingmode.msg.process;

import com.common.log.MyLog;
import com.module.rankingmode.msg.event.AppSwapEvent;
import com.module.rankingmode.msg.event.JoinActionEvent;
import com.module.rankingmode.msg.event.JoinNoticeEvent;
import com.module.rankingmode.msg.event.QuitGameEvent;
import com.module.rankingmode.msg.event.ReadyAndStartNoticeEvent;
import com.module.rankingmode.msg.event.ReadyNoticeEvent;
import com.module.rankingmode.msg.event.RoomInOutEvent;
import com.module.rankingmode.msg.event.RoundAndGameOverEvent;
import com.module.rankingmode.msg.event.RoundOverEvent;
import com.zq.live.proto.Common.MusicInfo;
import com.zq.live.proto.Common.UserInfo;
import com.zq.live.proto.Room.AppSwapMsg;
import com.zq.live.proto.Room.ERoomMsgType;
import com.zq.live.proto.Room.JoinActionMsg;
import com.zq.live.proto.Room.JoinNoticeMsg;
import com.zq.live.proto.Room.QuitGameMsg;
import com.zq.live.proto.Room.ReadyAndStartNoticeMsg;
import com.zq.live.proto.Room.ReadyNoticeMsg;
import com.zq.live.proto.Room.RoomInOutMsg;
import com.zq.live.proto.Room.RoomMsg;
import com.zq.live.proto.Room.RoundAndGameOverMsg;
import com.zq.live.proto.Room.RoundOverMsg;
import com.zq.live.proto.Room.SyncStatusMsg;

import org.greenrobot.eventbus.EventBus;

public class ChatRoomGameMsgProcess implements IPushChatRoomMsgProcess {

    public final static String TAG = "ChatRoomGameMsgProcess";

    @Override
    public void processRoomMsg(ERoomMsgType messageType, RoomMsg msg) {

        if (msg.getMsgType() == ERoomMsgType.RM_JOIN_ACTION) {
            processJoinActionMsg(msg.getJoinActionMsg());
        } else if (msg.getMsgType() == ERoomMsgType.RM_JOIN_NOTICE) {
            processJoinNoticeMsg(msg.getJoinNoticeMsg());
        } else if (msg.getMsgType() == ERoomMsgType.RM_READY_NOTICE) {
            processReadyNoticeMsg(msg.getReadyNoticeMsg());
        } else if (msg.getMsgType() == ERoomMsgType.RM_READY_AND_START_NOTICE) {
            processReadyAndStartNoticeMsg(msg.getReadyAndStartNoticeMsg());
        } else if (msg.getMsgType() == ERoomMsgType.RM_ROUND_OVER) {
            processRoundOverMsg(msg.getRoundOverMsg());
        } else if (msg.getMsgType() == ERoomMsgType.RM_ROUND_AND_GAME_OVER) {
            processRoundAndGameOverMsg(msg.getRoundAndGameOverMsg());
        } else if (msg.getMsgType() == ERoomMsgType.RM_QUIT_GAME) {
            processQuitGameMsg(msg.getQuitGameMsg());
        } else if (msg.getMsgType() == ERoomMsgType.RM_APP_SWAP) {
            processAppSwapMsg(msg.getAppSwapMsg());
        } else if (msg.getMsgType() == ERoomMsgType.RM_SYNC_STATUS) {
            processSyncStatusMsg(msg.getSyncStatusMsg());
        } else if (msg.getMsgType() == ERoomMsgType.RM_ROOM_IN_OUT) {
            processRoomInOutMsg(msg.getRoomInOutMsg());
        }
    }

    @Override
    public ERoomMsgType[] acceptType() {
        return new ERoomMsgType[]{
                ERoomMsgType.RM_JOIN_ACTION, ERoomMsgType.RM_JOIN_NOTICE,
                ERoomMsgType.RM_READY_NOTICE, ERoomMsgType.RM_READY_AND_START_NOTICE,
                ERoomMsgType.RM_ROUND_OVER, ERoomMsgType.RM_ROUND_AND_GAME_OVER,
                ERoomMsgType.RM_QUIT_GAME, ERoomMsgType.RM_APP_SWAP,
                ERoomMsgType.RM_SYNC_STATUS, ERoomMsgType.RM_ROOM_IN_OUT
        };
    }

    //加入游戏指令消息
    private void processJoinActionMsg(JoinActionMsg joinActionMsg) {
        if (joinActionMsg == null) {
            MyLog.d(TAG, "processJoinActionMsg" + " joinActionMsg == null");
            return;
        }

        int gameId = joinActionMsg.getGameID();
        long gameCreateMs = joinActionMsg.getGameCreateMS();

        EventBus.getDefault().post(new JoinActionEvent(gameId, gameCreateMs));
    }

    //加入游戏通知消息
    private void processJoinNoticeMsg(JoinNoticeMsg joinNoticeMsg) {
        if (joinNoticeMsg == null) {
            MyLog.d(TAG, "processJoinNoticeMsg" + " joinNoticeMsg == null");
            return;
        }

        long joinTimeMs = joinNoticeMsg.getJoinTimeMs();
        UserInfo userInfo = joinNoticeMsg.getUserInfo();
        MusicInfo musicInfo = joinNoticeMsg.getMusicInfo();

        EventBus.getDefault().post(new JoinNoticeEvent(joinTimeMs, userInfo, musicInfo));
    }

    //准备游戏通知消息
    private void processReadyNoticeMsg(ReadyNoticeMsg readyNoticeMsg) {
        if (readyNoticeMsg == null) {
            MyLog.d(TAG, "processReadyNoticeMsg" + " readyNoticeMsg == null");
            return;
        }

        long readyTimeMs = readyNoticeMsg.getReadyTimeMs();
        int readyUserID = readyNoticeMsg.getReadyUserID();

        EventBus.getDefault().post(new ReadyNoticeEvent(readyTimeMs, readyUserID));
    }

    //准备并开始游戏通知消息
    private void processReadyAndStartNoticeMsg(ReadyAndStartNoticeMsg readyAndStartNoticeMsg) {
        if (readyAndStartNoticeMsg == null) {
            MyLog.d(TAG, "processReadyAndStartNoticeMsg" + " readyAndStartNoticeMsg == null");
            return;
        }

        int readyUserID = readyAndStartNoticeMsg.getReadyUserID();   //准备用户ID
        long readyTimeMs = readyAndStartNoticeMsg.getReadyTimeMs();  //准备的毫秒时间戳
        long startTimeMS = readyAndStartNoticeMsg.getStartTimeMS();  //开始的毫秒时间戳
        int firstUserID = readyAndStartNoticeMsg.getFirstUserID();   //第一个用户ID
        int firstMusicID = readyAndStartNoticeMsg.getFirstMusicID(); //第一首歌曲ID

        EventBus.getDefault().post(new ReadyAndStartNoticeEvent(readyUserID, readyTimeMs, startTimeMS, firstUserID, firstMusicID));

    }

    //游戏轮次结束通知消息
    private void processRoundOverMsg(RoundOverMsg roundOverMsgr) {
        if (roundOverMsgr == null) {
            MyLog.d(TAG, "processRoundOverMsg" + " roundOverMsgr == null");
            return;
        }

        long roundOverTimeMs = roundOverMsgr.getRoundOverTimeMs();
        int nextRoundSeq = roundOverMsgr.getNextRoundSeq();
        int nextUserId = roundOverMsgr.getNextUserID();
        int nextMusicId = roundOverMsgr.getNextMusicID();

        EventBus.getDefault().post(new RoundOverEvent(roundOverTimeMs, nextRoundSeq, nextUserId, nextMusicId));
    }

    //轮次和游戏结束通知消息
    private void processRoundAndGameOverMsg(RoundAndGameOverMsg roundAndGameOverMsg) {
        if (roundAndGameOverMsg == null) {
            MyLog.d(TAG, "processRoundAndGameOverMsg" + " roundOverMsgr == null");
            return;
        }

        long roundOverTimeMs = roundAndGameOverMsg.getRoundOverTimeMs();

        EventBus.getDefault().post(new RoundAndGameOverEvent(roundOverTimeMs));
    }

    //退出游戏通知
    private void processQuitGameMsg(QuitGameMsg quitGameMsg) {
        if (quitGameMsg == null) {
            MyLog.d(TAG, "processQuitGameMsg" + " quitGameMsg == null");
            return;
        }

        int quitUserId = quitGameMsg.getQuitUserID();
        long quitTimeMs = quitGameMsg.getQuitTimeMs();

        EventBus.getDefault().post(new QuitGameEvent(quitUserId, quitTimeMs));
    }

    //app进程后台通知
    private void processAppSwapMsg(AppSwapMsg appSwapMsg) {
        if (appSwapMsg == null) {
            MyLog.d(TAG, "processAppSwapMsg" + " appSwapMsg == null");
            return;
        }

        int swapUserId = appSwapMsg.getSwapUserID();
        long swapTimeMs = appSwapMsg.getSwapTimsMs();
        boolean swapOut = appSwapMsg.getSwapOut();
        boolean swapIn = appSwapMsg.getSwapIn();

        EventBus.getDefault().post(new AppSwapEvent(swapUserId, swapTimeMs, swapOut, swapIn));
    }

    //状态同步信令
    private void processSyncStatusMsg(SyncStatusMsg syncStatusMsg) {
        if (syncStatusMsg == null) {
            MyLog.d(TAG, "processSyncStatusMsg" + " syncStatusMsg == null");
            return;
        }

    }

    // 进出消息
    private void processRoomInOutMsg(RoomInOutMsg roomInOutMsg) {
        if (roomInOutMsg == null) {
            MyLog.d(TAG, "processRoomInOutMsg" + " roomInOutMsg == null");
            return;
        }

        boolean isEnter = roomInOutMsg.getIsEnter();
        EventBus.getDefault().post(new RoomInOutEvent(isEnter));
    }
}