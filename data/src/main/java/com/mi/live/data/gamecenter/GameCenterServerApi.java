package com.mi.live.data.gamecenter;

import com.base.log.MyLog;
import com.google.protobuf.InvalidProtocolBufferException;
import com.mi.live.data.milink.MiLinkClientAdapter;
import com.mi.milink.sdk.aidl.PacketData;
import com.wali.live.proto.GameCenterProto;

public class GameCenterServerApi {

    public final static String TAG = "GameCenterServerApi";

    public static GameCenterProto.GetGameInfoRsp getGameInfoRsp(long uId, long gameId) {
        GameCenterProto.GetGameInfoReq request = GameCenterProto.GetGameInfoReq.newBuilder()
                .setUuid(uId)
                .setGameId(gameId)
                .build();
        PacketData data = new PacketData();
        data.setCommand("game.list.summaryPage");
        data.setData(request.toByteArray());
        PacketData response = MiLinkClientAdapter.getsInstance().sendSync(data, 10 * 1000);
        GameCenterProto.GetGameInfoRsp rsp = null;
        if (response != null) {
            try {
                rsp = GameCenterProto.GetGameInfoRsp.parseFrom(response.getData());
            } catch (InvalidProtocolBufferException e) {
                MyLog.d(TAG, e);
            }
        }
        return rsp;
    }
}
