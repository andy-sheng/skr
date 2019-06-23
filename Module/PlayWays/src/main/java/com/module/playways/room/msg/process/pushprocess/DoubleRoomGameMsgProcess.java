package com.module.playways.room.msg.process.pushprocess;

import com.common.log.MyLog;
import com.module.playways.doubleplay.pushEvent.DoubleAddMusicEvent;
import com.module.playways.doubleplay.pushEvent.DoubleCombineRoomSycPushEvent;
import com.module.playways.doubleplay.pushEvent.DoubleDelMusicEvent;
import com.module.playways.doubleplay.pushEvent.DoubleEndCombineRoomPushEvent;
import com.module.playways.doubleplay.pushEvent.DoubleLoadMusicInfoPushEvent;
import com.module.playways.doubleplay.pushEvent.DoublePickPushEvent;
import com.module.playways.doubleplay.pushEvent.DoubleUnlockUserInfoPushEvent;
import com.module.playways.room.msg.BasePushInfo;
import com.module.playways.room.msg.process.IPushChatRoomMsgProcess;
import com.zq.live.proto.CombineRoom.AddMusicInfoMsg;
import com.zq.live.proto.CombineRoom.CombineRoomMsg;
import com.zq.live.proto.CombineRoom.CombineRoomSyncStatusMsg;
import com.zq.live.proto.CombineRoom.DelMusicInfoMsg;
import com.zq.live.proto.CombineRoom.ECombineRoomMsgType;
import com.zq.live.proto.CombineRoom.EndCombineRoomMsg;
import com.zq.live.proto.CombineRoom.LoadMusicInfoMsg;
import com.zq.live.proto.CombineRoom.PickMsg;
import com.zq.live.proto.CombineRoom.UnlockUserInfoMsg;

import org.greenrobot.eventbus.EventBus;

public class DoubleRoomGameMsgProcess implements IPushChatRoomMsgProcess<ECombineRoomMsgType, CombineRoomMsg> {
    public final static String TAG = "DoubleRoomGameMsgProcess";

    @Override
    public ECombineRoomMsgType[] acceptType() {
        return new ECombineRoomMsgType[]{
                ECombineRoomMsgType.DRM_PICK,
                ECombineRoomMsgType.DRM_END_COMBINE_ROOM,
                ECombineRoomMsgType.DRM_UNLOCK_USER_INFO,
                ECombineRoomMsgType.DRM_LOAD_MUSIC_INFO,
                ECombineRoomMsgType.DRM_SYNC_STATUS,
                ECombineRoomMsgType.DRM_ADD_MUSIC_INFO,
                ECombineRoomMsgType.DRM_DEL_MUSIC_INFO
        };
    }

    @Override
    public void processRoomMsg(ECombineRoomMsgType messageType, CombineRoomMsg msg) {
        MyLog.d(TAG, "processRoomMsg" + " messageType=" + messageType.getValue());
        BasePushInfo basePushInfo = BasePushInfo.parse(msg);
        MyLog.d(TAG, "processRoomMsg" + " timeMs=" + basePushInfo.getTimeMs());

        if (msg.getMsgType() == ECombineRoomMsgType.DRM_PICK) {
            processPickMsg(basePushInfo, msg.getPickMsg());
        } else if (msg.getMsgType() == ECombineRoomMsgType.DRM_END_COMBINE_ROOM) {
            processEndCombineRoomMsg(basePushInfo, msg.getEndCombineRoomMsg());
        } else if (msg.getMsgType() == ECombineRoomMsgType.DRM_UNLOCK_USER_INFO) {
            processUnlockUserInfoMsg(basePushInfo, msg.getUnlockUserInfoMsg());
        } else if (msg.getMsgType() == ECombineRoomMsgType.DRM_LOAD_MUSIC_INFO) {
            processLoadMusicInfoMsg(basePushInfo, msg.getLoadMusicInfoMsg());
        } else if (msg.getMsgType() == ECombineRoomMsgType.DRM_SYNC_STATUS) {
            processSysMsg(basePushInfo, msg.getSyncStatusMsg());
        } else if (msg.getMsgType() == ECombineRoomMsgType.DRM_ADD_MUSIC_INFO) {
            processAddMusicMsg(basePushInfo, msg.getAddMuicInfoMsg());
        } else if (msg.getMsgType() == ECombineRoomMsgType.DRM_DEL_MUSIC_INFO) {
            processDelMusicMsg(basePushInfo, msg.getDelMuicInfoMsg());
        } else {
            MyLog.d(TAG, "unknown msg messageType=" + messageType + " msg=" + msg);
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
            EventBus.getDefault().post(new DoubleCombineRoomSycPushEvent(basePushInfo, combineRoomSyncStatusMsg));
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
