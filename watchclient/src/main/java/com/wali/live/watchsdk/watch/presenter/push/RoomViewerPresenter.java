package com.wali.live.watchsdk.watch.presenter.push;

import android.text.TextUtils;

import com.base.global.GlobalData;
import com.base.log.MyLog;
import com.mi.live.data.account.MyUserInfoManager;
import com.mi.live.data.account.UserAccountManager;
import com.mi.live.data.manager.LiveRoomCharacterManager;
import com.mi.live.data.milink.MiLinkClientAdapter;
import com.mi.live.data.push.IPushMsgProcessor;
import com.mi.live.data.push.model.BarrageMsg;
import com.mi.live.data.push.model.BarrageMsgType;
import com.mi.live.data.query.model.ViewerModel;
import com.mi.live.data.room.model.RoomBaseDataModel;
import com.wali.live.common.barrage.manager.LiveRoomChatMsgManager;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.watchtop.view.WatchTopInfoBaseView;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;


/**
 * for watch/repaly/live
 * Created by chengsimin on 16/7/5.
 */
public class RoomViewerPresenter implements IPushMsgProcessor {
    private static final String TAG = "RoomViewerPresenter";
    LiveRoomChatMsgManager mRoomChatMsgManager;

    public RoomViewerPresenter(LiveRoomChatMsgManager mRoomChatMsgManager) {
        this.mRoomChatMsgManager = mRoomChatMsgManager;
    }

    @Override
    public void process(BarrageMsg msg, RoomBaseDataModel roomBaseDataModel) {
        if (msg.getMsgType() == BarrageMsgType.B_MSG_TYPE_JOIN) {
            BarrageMsg.JoinRoomMsgExt ext = (BarrageMsg.JoinRoomMsgExt) msg.getMsgExt();
            MyLog.d(TAG, "B_MSG_TYPE_JOIN ext = " + ext.viewerCount + " , " + ext.viewerList.size());
            //按照产品的建议。直播页的进入房间信息都show出来
            if (!mRoomChatMsgManager.isHideSysMsg()) {
                mRoomChatMsgManager.addChatMsg(msg, true);
            }
            if (roomBaseDataModel.canUpdateLastUpdateViewerCount(msg.getSentTime())) {
                roomBaseDataModel.setViewerCnt(ext.viewerCount);
                updateAvatarView(roomBaseDataModel, ext.viewerList, 0);
            }
            if (LiveRoomCharacterManager.getInstance().isManager(msg.getSender())) {
                MyLog.v(TAG + " manager online:" + msg.getSender());
                //管理员在线的push
                LiveRoomCharacterManager.getInstance().setManagerOnline(msg.getSender(), true);
                roomBaseDataModel.notifyManagersChange();
            }
        } else if (msg.getMsgType() == BarrageMsgType.B_MSG_TYPE_LEAVE) {
            BarrageMsg.LeaveRoomMsgExt ext = (BarrageMsg.LeaveRoomMsgExt) msg.getMsgExt();
            MyLog.d(TAG, "B_MSG_TYPE_LEAVE ext = " + ext.viewerCount + " , " + ext.viewerList.size());
            if (roomBaseDataModel.canUpdateLastUpdateViewerCount(msg.getSentTime())) {
                roomBaseDataModel.setViewerCnt(ext.viewerCount);
                updateAvatarView(roomBaseDataModel, ext.viewerList, msg.getSender());
            }
            if (LiveRoomCharacterManager.getInstance().isManager(msg.getSender())) {
                MyLog.v(TAG + " manager offline:" + msg.getSender());
                //管理员离开房间下线的push
                LiveRoomCharacterManager.getInstance().setManagerOnline(msg.getSender(), false);
                roomBaseDataModel.notifyManagersChange();
            }
        } else if (msg.getMsgType() == BarrageMsgType.B_MSG_TYPE_VIEWER_CHANGE) {
            MyLog.d(TAG, "viewer change");
            BarrageMsg.ViewerChangeMsgExt ext = (BarrageMsg.ViewerChangeMsgExt) msg.getMsgExt();
            if (roomBaseDataModel.canUpdateLastUpdateViewerCount(msg.getSentTime())) {
                roomBaseDataModel.setViewerCnt(ext.viewerCount);
                updateAvatarView(roomBaseDataModel, ext.viewerList, 0);
            }
        } else if (msg.getMsgType() == BarrageMsgType.B_MSG_TYPE_KICK_VIEWER_BARRAGE) {
            BarrageMsg.KickMessageExt ext = (BarrageMsg.KickMessageExt) msg.getMsgExt();
            MyLog.d(TAG, "viewer kicked barrage," + ext.toString());
            if (ext == null || TextUtils.isEmpty(ext.getLiveid()) || roomBaseDataModel == null) {
                return;
            }
            if (ext.getLiveid().equals(roomBaseDataModel.getRoomId())) {
                String sysMsg = ext.buildUpSysMessage(roomBaseDataModel.getUid(), UserAccountManager.getInstance().getUuidAsLong());
                if (!TextUtils.isEmpty(sysMsg))
                    mRoomChatMsgManager.sendLocalSystemMsg(GlobalData.app().getString(R.string.sys_msg), sysMsg, roomBaseDataModel.getRoomId(), roomBaseDataModel.getUid());
            }
        }
    }

    @Override
    public int[] getAcceptMsgType() {
        return new int[]{
                BarrageMsgType.B_MSG_TYPE_JOIN,
                BarrageMsgType.B_MSG_TYPE_LEAVE,
                BarrageMsgType.B_MSG_TYPE_VIEWER_CHANGE,
                BarrageMsgType.B_MSG_TYPE_KICK_VIEWER_BARRAGE
        };
    }


    /**
     * 记得需要更新viewerList，只有前10，和被移除的观众
     */
    protected void updateAvatarView(RoomBaseDataModel roomData, List<ViewerModel> viewerList, long leaveUid) {
        if (roomData.getViewerCnt() == 0) {
            roomData.getViewersList().clear();
            roomData.notifyViewersChange("updateAvatarView cnt==0");
        } else {
            if (viewerList != null && !viewerList.isEmpty()) {
                if (roomData.getViewersList().size() > 250) {
                    // 超过了一定缓存直接清空，容错。
                    roomData.getViewersList().clear();
                }

                LinkedList<ViewerModel> temp = new LinkedList();
                for (ViewerModel protoViewer : viewerList) {
                    temp.add(protoViewer);
                }

                // 如果观众都一样则也没必须要刷新
                boolean hasChange = false;
                if (roomData.getViewersList().size() == temp.size()) {
                    List<ViewerModel> nowList = new ArrayList<>(roomData.getViewersList());
                    for (int i = 0; i < nowList.size() && i < temp.size(); i++) {
                        if (!nowList.get(i).equals(temp.get(i))) {
                            hasChange = true;
                            break;
                        }
                    }
                } else {
                    hasChange = true;
                }
                if (!hasChange) {
                    return;
                }
                if (roomData.getViewersList().size() <= WatchTopInfoBaseView.EXACT_VIEWER_NUM || roomData.getViewerCnt() <= WatchTopInfoBaseView.EXACT_VIEWER_NUM) {
                    // 用户没有拉取更多，每次全量更新
                    roomData.getViewersList().clear();
                    roomData.getViewersList().addAll(temp);
                } else {
                    // 用户可能拉取更多了
                    for (ViewerModel v : temp) {
                        roomData.getViewersList().remove(v);
                    }
                    // 加到最前面
                    roomData.getViewersList().addAll(0, temp);
                }
                //            MyLog.d(TAG, "updateAvatarView viewerList:" + temp);
//            MyLog.d(TAG, "updateAvatarView delUidList:" + delUidList);
//            MyLog.d(TAG, "updateAvatarView roomData.getViewersList() before:" + roomData.getViewersList());
                if (leaveUid != 0) {
                    roomData.getViewersList().remove(new ViewerModel(leaveUid));
                }

                // 用户已经滑动拉取更多了
                if (roomData.getViewersList().size() > 10 && !MiLinkClientAdapter.getsInstance().isTouristMode()) {
                    // 保证自己在观众列表中
                    ViewerModel self = new ViewerModel(MyUserInfoManager.getInstance().getUser().getUid()
                            , MyUserInfoManager.getInstance().getUser().getLevel()
                            , MyUserInfoManager.getInstance().getUser().getAvatar()
                            , MyUserInfoManager.getInstance().getUser().getCertificationType(), MyUserInfoManager.getInstance().getUser().isRedName());
                    if (!roomData.getViewersList().contains(self)) {
                        roomData.getViewersList().add(self);
                    }
                }
                roomData.notifyViewersChange("updateAvatarView normal");
            }
        }
    }

    @Override
    public void start() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void pause() {

    }

    @Override
    public void stop() {

    }

    @Override
    public void destroy() {

    }
}