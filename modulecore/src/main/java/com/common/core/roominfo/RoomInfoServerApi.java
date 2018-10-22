package com.common.core.roominfo;

import android.text.TextUtils;

import com.common.log.MyLog;
import com.common.milink.MiLinkClientAdapter;
import com.common.milink.command.MiLinkCommand;
import com.common.milink.constant.MiLinkConstant;
import com.mi.milink.sdk.aidl.PacketData;
import com.wali.live.proto.RoomInfo.HisRoomReq;
import com.wali.live.proto.RoomInfo.HisRoomRsp;
import com.wali.live.proto.RoomInfo.HistoryLiveReq;
import com.wali.live.proto.RoomInfo.HistoryLiveRsp;
import com.wali.live.proto.RoomInfo.RoomInfoReq;
import com.wali.live.proto.RoomInfo.RoomInfoRsp;

public class RoomInfoServerApi {
    /**
     * 根据uuid获得用户正在直播的房间信息
     * 查询别人房间信息,取道的是拉流地址
     *
     * @param uuid
     * @return 返回正在直播的liveShow, 如果房间不存在或者出错,　返回一个空值, 注意不是null.
     */
    public static HisRoomRsp getLiveShowByUserId(long uuid) {
        if (uuid <= 0) {
            MyLog.w("getLiveShowByUserId Illegal parameter");
            return null;
        }
        HisRoomReq request = new HisRoomReq.Builder().setZuid(uuid).build();
        PacketData packetData = new PacketData();
        packetData.setCommand(MiLinkCommand.COMMAND_GET_LIVE_ROOM);
        packetData.setData(request.toByteArray());
        PacketData result = MiLinkClientAdapter.getInstance().sendSync(packetData, MiLinkConstant.TIME_OUT);

        if (result != null && result.getData() != null) {
            try {
                HisRoomRsp response = HisRoomRsp.parseFrom(result.getData());
                return response;
            } catch (Exception e) {
                MyLog.e(e);
            }
        }
        return null;
    }


    /**
     * 房间id查房间状态,客户端拉流卡顿的时候用
     *
     * @param uuid       操作人id
     * @param zuid       直播人id
     * @param liveId     直播roomId
     * @param password   如果是加密类型，带密码 否则为空
     * @param isLater    true：优先返回当前正在直播的信息 ,不管是否跟liveId一致。没有直播时，再返回指定liveId的回放;
     *                   false:返回指定liveid的直播，如果改liveid的房间已关闭，返回liveid回放
     * @param isGameOnly true:只返回游戏信息
     * @return
     */
    public static RoomInfoRsp getRoomInfo(long uuid, long zuid, String liveId, String password, boolean isLater, boolean isGameOnly) {
        if (uuid <= 0 || zuid <= 0 || TextUtils.isEmpty(liveId)) {
            MyLog.w("getRoomInfo Illegal parameter");
            return null;
        }

        RoomInfoReq request = new RoomInfoReq.Builder()
                .setUuid(uuid)
                .setZuid(zuid)
                .setLiveId(liveId)
                .setPassword(password)
                .setGetLatestLive(isLater)
                .setGetGameInfoOnly(isGameOnly)
                .build();
        PacketData packetData = new PacketData();
        packetData.setCommand(MiLinkCommand.COMMAND_LIVE_ROOM_INFO);
        packetData.setData(request.toByteArray());
        PacketData result = MiLinkClientAdapter.getInstance().sendSync(packetData, MiLinkConstant.TIME_OUT);

        if (result != null && result.getData() != null) {
            try {
                RoomInfoRsp response = RoomInfoRsp.parseFrom(result.getData());
                return response;
            } catch (Exception e) {
                MyLog.e(e);
            }
        }

        return null;
    }

    /**
     * 查询回放列表
     *
     * @param uuid
     * @param zuid
     * @return
     */
    public static HistoryLiveRsp getHistoryShowList(long uuid, long zuid) {
        if (uuid <= 0 || zuid <= 0) {
            MyLog.w("getHistoryShowList Illegal parameter");
            return null;
        }
        HistoryLiveReq request = new HistoryLiveReq.Builder()
                .setUuid(uuid)
                .setZuid(zuid).build();
        PacketData packetData = new PacketData();
        packetData.setCommand(MiLinkCommand.COMMAND_LIST_HISTORY);
        packetData.setData(request.toByteArray());
        PacketData result = MiLinkClientAdapter.getInstance().sendSync(packetData, MiLinkConstant.TIME_OUT);

        if (result != null && result.getData() != null) {
            try {
                HistoryLiveRsp response = HistoryLiveRsp.parseFrom(result.getData());
                return response;
            } catch (Exception e) {
                MyLog.e(e);
            }
        }

        return null;
    }
}
