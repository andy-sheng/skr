package com.module.playways.room.msg.manager;

import com.common.log.MyLog;
import com.module.playways.doubleplay.pushEvent.DoubleAddMusicEvent;
import com.module.playways.doubleplay.pushEvent.DoubleAgreeChangeSceneEvent;
import com.module.playways.doubleplay.pushEvent.DoubleAskChangeSceneEvent;
import com.module.playways.doubleplay.pushEvent.DoubleChangeGamePanelEvent;
import com.module.playways.doubleplay.pushEvent.DoubleChoiceGameItemEvent;
import com.module.playways.doubleplay.pushEvent.DoubleDelMusicEvent;
import com.module.playways.doubleplay.pushEvent.DoubleEndCombineRoomPushEvent;
import com.module.playways.doubleplay.pushEvent.DoubleEndGameEvent;
import com.module.playways.doubleplay.pushEvent.DoubleLoadMusicInfoPushEvent;
import com.module.playways.doubleplay.pushEvent.DoublePickPushEvent;
import com.module.playways.doubleplay.pushEvent.DoubleStartGameEvent;
import com.module.playways.doubleplay.pushEvent.DoubleSyncStatusV2Event;
import com.module.playways.doubleplay.pushEvent.DoubleUnlockUserInfoPushEvent;
import com.module.playways.room.msg.BasePushInfo;
import com.module.playways.room.msg.filter.PushMsgFilter;
import com.zq.live.proto.CombineRoom.AddMusicInfoMsg;
import com.zq.live.proto.CombineRoom.AgreeChangeSceneMsg;
import com.zq.live.proto.CombineRoom.ChangeGamePanelMsg;
import com.zq.live.proto.CombineRoom.ChoiceGameItemMsg;
import com.zq.live.proto.CombineRoom.CombineRoomMsg;
import com.zq.live.proto.CombineRoom.CombineRoomSyncStatusMsg;
import com.zq.live.proto.CombineRoom.CombineRoomSyncStatusV2Msg;
import com.zq.live.proto.CombineRoom.DelMusicInfoMsg;
import com.zq.live.proto.CombineRoom.ECombineRoomMsgType;
import com.zq.live.proto.CombineRoom.EndCombineRoomMsg;
import com.zq.live.proto.CombineRoom.EndGameMsg;
import com.zq.live.proto.CombineRoom.LoadMusicInfoMsg;
import com.zq.live.proto.CombineRoom.PickMsg;
import com.zq.live.proto.CombineRoom.ReqChangeSceneMsg;
import com.zq.live.proto.CombineRoom.StartGameMsg;
import com.zq.live.proto.CombineRoom.UnlockUserInfoMsg;

import org.greenrobot.eventbus.EventBus;

import java.util.HashSet;

/**
 * 处理所有的RoomMsg
 */
public class DoubleRoomMsgManager extends BaseMsgManager<ECombineRoomMsgType, CombineRoomMsg> {

    public final String TAG = "DoubleRoomMsgManager";

    private static class ChatRoomMsgAdapterHolder {
        private static final DoubleRoomMsgManager INSTANCE = new DoubleRoomMsgManager();
    }

    private DoubleRoomMsgManager() {

    }

    public static final DoubleRoomMsgManager getInstance() {
        return ChatRoomMsgAdapterHolder.INSTANCE;
    }

    /**
     * 处理消息分发
     *
     * @param msg
     */
    public void processRoomMsg(CombineRoomMsg msg) {
        boolean canGo = true;  //是否放行的flag
        for (PushMsgFilter filter : mPushMsgFilterList) {
            canGo = filter.doFilter(msg);
            if (!canGo) {
                MyLog.d(TAG, "processRoomMsg " + msg + "被拦截");
                return;
            }
        }
        processRoomMsg(msg.getMsgType(), msg);
    }

    private void processRoomMsg(ECombineRoomMsgType messageType, CombineRoomMsg msg) {
        BasePushInfo basePushInfo = BasePushInfo.parse(msg);
        MyLog.d(TAG, "processRoomMsg messageType=" + messageType+" timeMs=" + basePushInfo.getTimeMs() );
        if (messageType == ECombineRoomMsgType.DRM_PICK) {
            processPickMsg(basePushInfo, msg.getPickMsg());
        } else if (messageType == ECombineRoomMsgType.DRM_END_COMBINE_ROOM) {
            processEndCombineRoomMsg(basePushInfo, msg.getEndCombineRoomMsg());
        } else if (messageType == ECombineRoomMsgType.DRM_UNLOCK_USER_INFO) {
            processUnlockUserInfoMsg(basePushInfo, msg.getUnlockUserInfoMsg());
        } else if (messageType == ECombineRoomMsgType.DRM_LOAD_MUSIC_INFO) {
            processLoadMusicInfoMsg(basePushInfo, msg.getLoadMusicInfoMsg());
        } else if (messageType == ECombineRoomMsgType.DRM_SYNC_STATUS) {
            processSysMsg(basePushInfo, msg.getSyncStatusMsg());
        } else if (messageType == ECombineRoomMsgType.DRM_ADD_MUSIC_INFO) {
            processAddMusicMsg(basePushInfo, msg.getAddMuicInfoMsg());
        } else if (messageType == ECombineRoomMsgType.DRM_DEL_MUSIC_INFO) {
            processDelMusicMsg(basePushInfo, msg.getDelMuicInfoMsg());
        } else if (messageType == ECombineRoomMsgType.DRM_REQ_CHANGE_SCENE) {
            processChangeSceneMsg(basePushInfo, msg.getReqChangeSceneMsg());
        } else if (messageType == ECombineRoomMsgType.DRM_AGREE_CHANGE_SCENE) {
            processAgreeChangeSceneMsg(basePushInfo, msg.getAgreeChangeSceneMsg());
        } else if (messageType == ECombineRoomMsgType.DRM_CHOICE_GAME_TIEM) {
            processChoiceGameItemMsg(basePushInfo, msg.getChoiceGameItemMsg());
        } else if (messageType == ECombineRoomMsgType.DRM_START_GAME) {
            processStartGameMsg(basePushInfo, msg.getStartGameMsg());
        } else if (messageType == ECombineRoomMsgType.DRM_CHANGE_GAME_PANEL) {
            processChangeGamePanelMsg(basePushInfo, msg.getChangeGamePanelMsg());
        } else if (messageType == ECombineRoomMsgType.DRM_END_GAME) {
            processEndGamsMsg(basePushInfo, msg.getEndGameMsg());
        } else if (messageType == ECombineRoomMsgType.DRM_CR_SYNC_STATUS_V2) {
            processSyncStatusV2Msg(basePushInfo, msg.getSyncStatusV2Msg());
        } else {
            MyLog.d(TAG, "unknown msg messageType=" + messageType + " msg=" + msg);
        }
    }

    private void processChangeSceneMsg(BasePushInfo basePushInfo, ReqChangeSceneMsg reqChangeSceneMsg) {
        if (reqChangeSceneMsg != null) {
            EventBus.getDefault().post(new DoubleAskChangeSceneEvent(basePushInfo, reqChangeSceneMsg));
        } else {
            MyLog.e(TAG, "reqChangeSceneMsg" + " Msg=null");
        }
    }

    private void processAgreeChangeSceneMsg(BasePushInfo basePushInfo, AgreeChangeSceneMsg agreeChangeSceneMsg) {
        if (agreeChangeSceneMsg != null) {
            EventBus.getDefault().post(new DoubleAgreeChangeSceneEvent(basePushInfo, agreeChangeSceneMsg));
        } else {
            MyLog.e(TAG, "agreeChangeSceneMsg" + " Msg=null");
        }
    }

    private void processChoiceGameItemMsg(BasePushInfo basePushInfo, ChoiceGameItemMsg choiceGameItemMsg) {
        if (choiceGameItemMsg != null) {
            EventBus.getDefault().post(new DoubleChoiceGameItemEvent(basePushInfo, choiceGameItemMsg));
        } else {
            MyLog.e(TAG, "choiceGameItemMsg" + " Msg=null");
        }
    }

    private void processStartGameMsg(BasePushInfo basePushInfo, StartGameMsg startGameMsg) {
        if (startGameMsg != null) {
            EventBus.getDefault().post(new DoubleStartGameEvent(basePushInfo, startGameMsg));
        } else {
            MyLog.e(TAG, "startGameMsg" + " Msg=null");
        }
    }

    private void processChangeGamePanelMsg(BasePushInfo basePushInfo, ChangeGamePanelMsg changeGamePanelMsg) {
        if (changeGamePanelMsg != null) {
            EventBus.getDefault().post(new DoubleChangeGamePanelEvent(basePushInfo, changeGamePanelMsg));
        } else {
            MyLog.e(TAG, "processChangeGamePanelMsg" + " Msg=null");
        }
    }

    private void processEndGamsMsg(BasePushInfo basePushInfo, EndGameMsg endGameMsg) {
        if (endGameMsg != null) {
            EventBus.getDefault().post(new DoubleEndGameEvent(basePushInfo, endGameMsg));
        } else {
            MyLog.e(TAG, "processEndGamsMsg" + " Msg=null");
        }
    }

    private void processSyncStatusV2Msg(BasePushInfo basePushInfo, CombineRoomSyncStatusV2Msg combineRoomSyncStatusV2Msg) {
        if (combineRoomSyncStatusV2Msg != null) {
            EventBus.getDefault().post(new DoubleSyncStatusV2Event(basePushInfo, combineRoomSyncStatusV2Msg));
        } else {
            MyLog.e(TAG, "processSyncStatusV2Msg" + " Msg=null");
        }
    }

    private void processPickMsg(BasePushInfo basePushInfo, PickMsg pickMsg) {
        if (pickMsg != null) {
            EventBus.getDefault().post(new DoublePickPushEvent(basePushInfo, pickMsg));
        } else {
            MyLog.e(TAG, "processPickMsg" + " Msg=null");
        }
    }

    private void processEndCombineRoomMsg(BasePushInfo basePushInfo, EndCombineRoomMsg endCombineRoomMsg) {
        if (endCombineRoomMsg != null) {
            EventBus.getDefault().post(new DoubleEndCombineRoomPushEvent(basePushInfo, endCombineRoomMsg));
        } else {
            MyLog.e(TAG, "processEndCombineRoomMsg" + " Msg=null");
        }
    }

    private void processUnlockUserInfoMsg(BasePushInfo basePushInfo, UnlockUserInfoMsg unlockUserInfoMsg) {
        if (unlockUserInfoMsg != null) {
            EventBus.getDefault().post(new DoubleUnlockUserInfoPushEvent(basePushInfo, unlockUserInfoMsg));
        } else {
            MyLog.e(TAG, "processUnlockUserInfoMsg" + " Msg=null");
        }
    }

    private void processLoadMusicInfoMsg(BasePushInfo basePushInfo, LoadMusicInfoMsg loadMusicInfoMsg) {
        if (loadMusicInfoMsg != null) {
            EventBus.getDefault().post(new DoubleLoadMusicInfoPushEvent(basePushInfo, loadMusicInfoMsg));
        } else {
            MyLog.e(TAG, "processLoadMusicInfoMsg" + " Msg=null");
        }
    }

    private void processSysMsg(BasePushInfo basePushInfo, CombineRoomSyncStatusMsg combineRoomSyncStatusMsg) {
        if (combineRoomSyncStatusMsg != null) {
//            EventBus.getDefault().post(new DoubleCombineRoomSycPushEvent(basePushInfo, combineRoomSyncStatusMsg));
        } else {
            MyLog.e(TAG, "processSysMsg" + " Msg=null");
        }
    }

    private void processAddMusicMsg(BasePushInfo basePushInfo, AddMusicInfoMsg addMuicInfoMsg) {
        if (addMuicInfoMsg != null) {
            EventBus.getDefault().post(new DoubleAddMusicEvent(basePushInfo, addMuicInfoMsg));
        } else {
            MyLog.e(TAG, "processAddMusicMsg" + " addMuicInfoMsg=null");
        }
    }

    private void processDelMusicMsg(BasePushInfo basePushInfo, DelMusicInfoMsg delMuicInfoMsg) {
        if (delMuicInfoMsg != null) {
            EventBus.getDefault().post(new DoubleDelMusicEvent(basePushInfo, delMuicInfoMsg));
        } else {
            MyLog.e(TAG, "processDelMusicMsg" + " delMuicInfoMsg=null");
        }
    }
}
