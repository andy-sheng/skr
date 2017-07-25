package com.mi.live.data.api;

import com.base.log.MyLog;
import com.google.protobuf.InvalidProtocolBufferException;
import com.mi.live.data.account.UserAccountManager;
import com.mi.live.data.data.LiveShow;
import com.mi.live.data.data.UserListData;
import com.mi.live.data.milink.MiLinkClientAdapter;
import com.mi.live.data.milink.command.MiLinkCommand;
import com.mi.live.data.milink.constant.MiLinkConstant;
import com.mi.live.data.query.model.MessageRule;
import com.mi.milink.sdk.aidl.PacketData;
import com.wali.live.proto.LiveCommonProto;
import com.wali.live.proto.LiveManagerProto;
import com.wali.live.proto.LiveProto;
import com.wali.live.proto.LiveProto.HistoryDeleteReq;
import com.wali.live.proto.LiveProto.HistoryDeleteRsp;
import com.wali.live.proto.LiveProto.HistoryLiveReq;
import com.wali.live.proto.LiveProto.HistoryLiveRsp;
import com.wali.live.proto.LiveProto.ViewerTopReq;
import com.wali.live.proto.LiveProto.ViewerTopRsp;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lan on 15-11-27.
 */
public class LiveManager {
    private static final String TAG = LiveManager.class.getSimpleName();

    // Live.proto里的直播类型定义
    public static final int TYPE_LIVE_PUBLIC = 0;
    public static final int TYPE_LIVE_PRIVATE = 1;
    public static final int TYPE_LIVE_TOKEN = 2;
    public static final int TYPE_LIVE_TICKET = 3;
    public static final int TYPE_LIVE_VR = 5;
    public static final int TYPE_LIVE_GAME = 6;

    /**
     * MiLinkCommand : zhibo.live.historydelete
     * 查询房间状态
     */
    public static HistoryDeleteRsp historyDeleteRsp(String liveId) {
        HistoryDeleteReq req = HistoryDeleteReq.newBuilder()
                .setZuid(UserAccountManager.getInstance().getUuidAsLong())
                .setLiveId(liveId)
                .build();

        return historyDeleteRspFromServer(req);
    }

    private static HistoryDeleteRsp historyDeleteRspFromServer(HistoryDeleteReq req) {
        PacketData data = new PacketData();
        data.setCommand(MiLinkCommand.COMMAND_LIVE_HISTORY_DELETE);
        data.setData(req.toByteArray());
        MyLog.v(TAG, "historyDelete request : \n" + req.toString());

        PacketData rspData = MiLinkClientAdapter.getsInstance().sendSync(data, MiLinkConstant.TIME_OUT);
        if (rspData != null) {
            try {
                HistoryDeleteRsp rsp = HistoryDeleteRsp.parseFrom(rspData.getData());
                MyLog.v(TAG, "historyDelete response : \n" + rsp.toString());

                return rsp;
            } catch (InvalidProtocolBufferException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * MiLinkCommand : zhibo.live.viewertop
     * 房间人员查看
     */
    public static ViewerTopRsp viewerTopRsp(long ownerId, String liveId) {
        ViewerTopReq req = ViewerTopReq.newBuilder()
                .setUuid(UserAccountManager.getInstance().getUuidAsLong())
                .setZuid(ownerId)
                .setLiveId(liveId)
                .build();

        return viewerTopRspFromServer(req);
    }

    private static ViewerTopRsp viewerTopRspFromServer(ViewerTopReq req) {
        PacketData data = new PacketData();
        data.setCommand(MiLinkCommand.COMMAND_LIVE_VIEWER_TOP);
        data.setData(req.toByteArray());
        MyLog.v(TAG, "viewerTop request : \n" + req.toString());

        PacketData rspData = MiLinkClientAdapter.getsInstance().sendSync(data, MiLinkConstant.TIME_OUT);
        if (rspData != null) {
            try {
                ViewerTopRsp rsp = ViewerTopRsp.parseFrom(rspData.getData());
                MyLog.v(TAG, "viewerTop response : \n" + rsp.toString());

                return rsp;
            } catch (InvalidProtocolBufferException e) {
                MyLog.e(e);
            }
        }
        return null;
    }

    /**
     * 查询排序后的房间用户列表
     */
    public static List<Object> getViewerList(long zuid, String roomId) {
        List<Object> list = new ArrayList<>();
        LiveManagerProto.RoomUsersRequest request = LiveManagerProto.RoomUsersRequest.newBuilder().setZuid(zuid).setRoomId(roomId).build();
        PacketData packetData = new PacketData();
        packetData.setCommand(MiLinkCommand.COMMAND_ROOM_VIEWER);
        packetData.setData(request.toByteArray());
        MyLog.v(TAG, "getViewerList request : \n" + request.toString());

        PacketData responseData = MiLinkClientAdapter.getsInstance().sendSync(packetData, MiLinkConstant.TIME_OUT);
        MyLog.v(TAG, "getViewerList responseData=" + responseData);
        try {
            if (responseData != null) {
                MyLog.v(TAG, "getViewerList getMnsCode:" + responseData.getMnsCode());
                LiveManagerProto.RoomUsersResponse response = LiveManagerProto.RoomUsersResponse.parseFrom(responseData.getData());
                if (response.getUsersList() != null) {
                    for (LiveManagerProto.UserInfo data : response.getUsersList()) {
                        list.add(new UserListData(data));
                    }
                }
                return list;
            }
        } catch (InvalidProtocolBufferException e) {
            MyLog.e(e);
        }
        return list;

    }

    /**
     * 查询排序后的房间用户列表
     */
    public static boolean isInLiveRoom(long zuid, String liveId, long managerId) {
        LiveProto.IsInRoomReq request = LiveProto.IsInRoomReq.newBuilder().setZuid(zuid).setLiveId(liveId).addViewerId(managerId).build();
        PacketData data = new PacketData();
        data.setCommand(MiLinkCommand.COMMAND_LIVE_ISINROOM);
        data.setData(request.toByteArray());
        MyLog.e(TAG, "isInLiveRoom request : \n" + request.toString());

        PacketData rspData = MiLinkClientAdapter.getsInstance().sendSync(data, MiLinkConstant.TIME_OUT);
        if (rspData != null) {
            try {
                LiveProto.IsInRoomRsp rsp = LiveProto.IsInRoomRsp.parseFrom(rspData.getData());
                MyLog.e(TAG, "isInLiveRoom response : \n" + rsp.toString());
                if (rsp.getRetCode() == 0) {
                    if (rsp.getViewerIdList() != null) {
                        return rsp.getViewerIdList().contains(managerId);
                    }
                }
            } catch (InvalidProtocolBufferException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public static LiveProto.PKBeginRsp pkBeginSync(long myUid, String myRoomId, int myInitTicket, long pkUid, String pkRoomId, int pkInitTicket) {
        LiveProto.PKBeginReq request = LiveProto.PKBeginReq.newBuilder()
                .setUuid(myUid)
                .setMyPkInitTicket(myInitTicket)
                .setPkuid(pkUid)
                .setPkLiveId(pkRoomId)
                .setOtherPkInitTicket(pkInitTicket)
                .build();
        PacketData data = new PacketData();
        data.setCommand(MiLinkCommand.COMMAND_LIVE_PKBEGIN);
        data.setData(request.toByteArray());
        MyLog.e(TAG, "pkBeginSync request : \n" + request.toString());
        PacketData rspData = MiLinkClientAdapter.getsInstance().sendSync(data, MiLinkConstant.TIME_OUT);
        LiveProto.PKBeginRsp rsp = null;
        if (rspData != null) {
            try {
                rsp = LiveProto.PKBeginRsp.parseFrom(rspData.getData());
                MyLog.e(TAG, "pkBeginSync response : \n" + rsp.toString());
            } catch (InvalidProtocolBufferException e) {
                e.printStackTrace();
            }
        }
        return rsp;
    }

    public static LiveProto.PKEndRsp pkEndSync(long myUid, long pkUid, String pkRoomId) {
        LiveProto.PKEndReq request = LiveProto.PKEndReq.newBuilder()
                .setUuid(myUid)
                .setPkuid(pkUid)
                .setPkLiveId(pkRoomId)
                .build();
        PacketData data = new PacketData();
        data.setCommand(MiLinkCommand.COMMAND_LIVE_PKEND);
        data.setData(request.toByteArray());
        MyLog.e(TAG, "pkEndSync request : \n" + request.toString());
        PacketData rspData = MiLinkClientAdapter.getsInstance().sendSync(data, MiLinkConstant.TIME_OUT);
        LiveProto.PKEndRsp rsp = null;
        if (rspData != null) {
            try {
                rsp = LiveProto.PKEndRsp.parseFrom(rspData.getData());
                MyLog.e(TAG, "pkEndSync response : \n" + rsp.toString());
            } catch (InvalidProtocolBufferException e) {
                e.printStackTrace();
            }
        }
        return rsp;
    }

    public static LiveProto.GetPKInfoRsp pkQueryPkInfoSync(long myUid) {
        LiveProto.GetPKInfoReq request = LiveProto.GetPKInfoReq.newBuilder()
                .setZuid(myUid)
                .build();
        PacketData data = new PacketData();
        data.setCommand(MiLinkCommand.COMMAND_LIVE_PKGETINFO);
        data.setData(request.toByteArray());
        MyLog.e(TAG, "pkQueryPkInfoSync request : \n" + request.toString());
        PacketData rspData = MiLinkClientAdapter.getsInstance().sendSync(data, MiLinkConstant.TIME_OUT);
        LiveProto.GetPKInfoRsp rsp = null;
        if (rspData != null) {
            try {
                rsp = LiveProto.GetPKInfoRsp.parseFrom(rspData.getData());
                MyLog.e(TAG, "pkQueryPkInfoSync response : \n" + rsp.toString());
            } catch (InvalidProtocolBufferException e) {
                e.printStackTrace();
            }
        }
        return rsp;
    }


    public static boolean beginLineMic(long zuid, String liveId, long micuid, float scaleX, float scaleY, float scaleW, float scaleH) {
        LiveProto.MicBeginReq.Builder builder = LiveProto.MicBeginReq.newBuilder()
                .setZuid(zuid).setLiveId(liveId).setMicInfo(
                        LiveCommonProto.MicInfo.newBuilder().setMicuid(micuid).setSubViewPos(
                                LiveCommonProto.MicSubViewPos.newBuilder().setTopXScale(scaleX).setTopYScale(scaleY).setWidthScale(scaleW).setHeightScale(scaleH)));
        LiveProto.MicBeginReq req = builder.build();
        PacketData data = new PacketData();
        data.setCommand(MiLinkCommand.COMMAND_LIVE_MIC_BEGIN);
        data.setData(req.toByteArray());
        MyLog.v(TAG, "beginLineMic request : \n" + req.toString());

        PacketData rspData = MiLinkClientAdapter.getsInstance().sendSync(data, MiLinkConstant.TIME_OUT);
        if (rspData != null) {
            try {
                LiveProto.MicBeginRsp rsp = LiveProto.MicBeginRsp.parseFrom(rspData.getData());
                if (rsp != null) {
                    MyLog.v(TAG, "beginLive response : \n" + rsp.toString());
                    return rsp.getRetCode() == 0;
                }
            } catch (InvalidProtocolBufferException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public static boolean endLineMic(long zuid, String liveId, long micuid) {
        LiveProto.MicEndReq.Builder builder = LiveProto.MicEndReq.newBuilder()
                .setZuid(zuid).setLiveId(liveId).setMicuid(micuid);
        LiveProto.MicEndReq req = builder.build();
        PacketData data = new PacketData();
        data.setCommand(MiLinkCommand.COMMAND_LIVE_MIC_END);
        data.setData(req.toByteArray());
        MyLog.v(TAG, "endLineMic request : \n" + req.toString());

        PacketData rspData = MiLinkClientAdapter.getsInstance().sendSync(data, MiLinkConstant.TIME_OUT);
        if (rspData != null) {
            try {
                LiveProto.MicEndRsp rsp = LiveProto.MicEndRsp.parseFrom(rspData.getData());
                if (rsp != null) {
                    MyLog.v(TAG, "endLineMic response : \n" + rsp.toString());
                    return rsp.getRetCode() == 0;
                }
            } catch (InvalidProtocolBufferException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * 更新房间内发言频率、是否重复
     *
     * @param uuid
     * @param liveId
     * @param messageRule
     * @return
     */
    public static boolean updateMsgRule(long uuid, String liveId, MessageRule messageRule) {
        LiveProto.UpdateMsgRuleReq.Builder builder = LiveProto.UpdateMsgRuleReq.newBuilder();
        builder.setLiveId(liveId);
        builder.setZuid(uuid);
        LiveCommonProto.MsgRule.Builder msgRuleBuilder = LiveCommonProto.MsgRule.newBuilder();
        msgRuleBuilder.setSpeakPeriod(messageRule.getSpeakPeriod());
        msgRuleBuilder.setUnrepeatable(messageRule.isUnrepeatable());
        builder.setMsgRule(msgRuleBuilder.build());
        LiveProto.UpdateMsgRuleReq req = builder.build();
        PacketData data = new PacketData();
        data.setCommand(MiLinkCommand.COMMAND_UPDATE_MSGRULE);
        data.setData(req.toByteArray());
        MyLog.w(TAG, "update message rule request : \n" + req.toString());
        PacketData rspData = MiLinkClientAdapter.getsInstance().sendSync(data, MiLinkConstant.TIME_OUT);
        if (rspData != null) {
            try {
                LiveProto.UpdateMsgRuleRsp rsp = LiveProto.UpdateMsgRuleRsp.parseFrom(rspData.getData());
                if (rsp != null) {
                    MyLog.w(TAG, "update message rule response : \n" + rsp.toString());
                    if (rsp.getRetCode() == ErrorCode.CODE_SUCCESS) {
                        return true;
                    }
                }
            } catch (InvalidProtocolBufferException e) {
                MyLog.e(TAG, e);
            }
        }
        return false;
    }

    /**
     * MiLinkCommand : zhibo.live.history
     */
    public static HistoryLiveRsp historyRsp(long playerId) {
        HistoryLiveReq req = HistoryLiveReq.newBuilder()
                .setUuid(UserAccountManager.getInstance().getUuidAsLong())
                .setZuid(playerId)
                .build();

        return historyRspFromServer(req);
    }

    private static HistoryLiveRsp historyRspFromServer(HistoryLiveReq req) {
        PacketData data = new PacketData();
        data.setCommand(MiLinkCommand.COMMAND_LIST_HISTORY);
        data.setData(req.toByteArray());
        MyLog.v(TAG, "history request : \n" + req.toString());

        PacketData rspData = MiLinkClientAdapter.getsInstance().sendSync(data, MiLinkConstant.TIME_OUT);
        if (rspData != null) {
            try {
                HistoryLiveRsp rsp = HistoryLiveRsp.parseFrom(rspData.getData());
                MyLog.v(TAG, "history response : \n" + rsp.toString());

                return rsp;
            } catch (InvalidProtocolBufferException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * 把列表服务里的类型转换为房间服务的类型<br/>
     * 建议不要传入{@link LiveShow#LIVETYPE_BACKPLAY}和{@link LiveShow#LIVETYPE_PK}
     *
     * @param listLiveType 列表服务里的type，在{@link LiveShow}里定义
     */
    public static int mapLiveTypeFromListToRoom(int listLiveType) {
        switch (listLiveType) {
            case LiveShow.LIVETYPE_PUBLIC:
                return LiveManager.TYPE_LIVE_PUBLIC;
            case LiveShow.LIVETYPE_PRIVATE:
                return LiveManager.TYPE_LIVE_PRIVATE;
            case LiveShow.LIVETYPE_TOKEN:
                return LiveManager.TYPE_LIVE_TOKEN;
            case LiveShow.LIVETYPE_TICKET:
                return LiveManager.TYPE_LIVE_TICKET;
            case LiveShow.LIVETYPE_VR:
                return LiveManager.TYPE_LIVE_VR;
            case LiveShow.LIVETYPE_GAME:
                return LiveManager.TYPE_LIVE_GAME;
            default:
                return LiveManager.TYPE_LIVE_PUBLIC;
        }
    }

    /**
     * 把房间服务的直播类型转换为列表服务的直播类型
     *
     * @param roomLiveType {@link LiveManager}中的常量
     * @return {@link LiveShow}中的常量
     */
    public static int mapLiveTypeFromRoomToList(int roomLiveType) {
        switch (roomLiveType) {
            case LiveManager.TYPE_LIVE_PUBLIC:
                return LiveShow.LIVETYPE_PUBLIC;
            case LiveManager.TYPE_LIVE_PRIVATE:
                return LiveShow.LIVETYPE_PRIVATE;
            case LiveManager.TYPE_LIVE_TOKEN:
                return LiveShow.LIVETYPE_TOKEN;
            case LiveManager.TYPE_LIVE_TICKET:
                return LiveShow.LIVETYPE_TICKET;
            case LiveManager.TYPE_LIVE_VR:
                return LiveShow.LIVETYPE_VR;
            case LiveManager.TYPE_LIVE_GAME:
                return LiveShow.LIVETYPE_GAME;
            default:
                return LiveShow.LIVETYPE_PUBLIC;
        }
    }
}
