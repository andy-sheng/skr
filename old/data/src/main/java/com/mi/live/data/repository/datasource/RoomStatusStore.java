package com.mi.live.data.repository.datasource;

import com.mi.live.data.account.UserAccountManager;
import com.mi.live.data.milink.MiLinkClientAdapter;
import com.mi.live.data.milink.command.MiLinkCommand;
import com.base.log.MyLog;
import com.mi.milink.sdk.aidl.PacketData;
import com.wali.live.proto.LiveProto;

import rx.Observable;
import rx.Subscriber;

/**
 * Created by chengsimin on 16/7/6.
 */
public class RoomStatusStore {
    private static final String TAG = "RoomStatusStore";

    public Observable<LiveProto.LeaveLiveRsp> leaveRooom(final long anchorId, final String roomid) {
        return Observable.create(new Observable.OnSubscribe<LiveProto.LeaveLiveRsp>() {
            @Override
            public void call(Subscriber<? super LiveProto.LeaveLiveRsp> subscriber) {
                LiveProto.LeaveLiveReq req = LiveProto.LeaveLiveReq.newBuilder()
                        .setUuid(UserAccountManager.getInstance().getUuidAsLong())
                        .setZuid(anchorId)
                        .setLiveId(roomid)
                        .build();
                PacketData data = new PacketData();
                data.setCommand(MiLinkCommand.COMMAND_LIVE_LEAVE);
                data.setData(req.toByteArray());
                data.setNeedCached(false);
                MyLog.w(TAG, "leaveRooom request:" + req.toString());
                PacketData response = MiLinkClientAdapter.getsInstance().sendSync(data, 10 * 1000);
                LiveProto.LeaveLiveRsp rsp = null;
                try {
                    rsp = LiveProto.LeaveLiveRsp.parseFrom(response.getData());
                    MyLog.w(TAG, "leaveRooom response:" + rsp.toString());
                    subscriber.onNext(rsp);
                    subscriber.onCompleted();
                } catch (Exception e) {
                    MyLog.e(TAG, "leaveRooom onError:" + e.getMessage());
                    subscriber.onError(e);
                }
            }
        });
    }


    public Observable<LiveProto.EnterLiveRsp> enterRoom(final long anchorId, final String roomid) {
        return Observable.create(new Observable.OnSubscribe<LiveProto.EnterLiveRsp>() {
            @Override
            public void call(Subscriber<? super LiveProto.EnterLiveRsp> subscriber) {
                LiveProto.EnterLiveReq req = LiveProto.EnterLiveReq.newBuilder()
                        .setUuid(UserAccountManager.getInstance().getUuidAsLong())
                        .setZuid(anchorId)
                        .setLiveId(roomid)
                        .build();
                PacketData data = new PacketData();
                data.setCommand(MiLinkCommand.COMMAND_LIVE_ENTER);
                data.setData(req.toByteArray());
                data.setNeedCached(true);
                MyLog.w(TAG, "enterRoom request:" + req.toString());
                PacketData response = MiLinkClientAdapter.getsInstance().sendSync(data, 10 * 1000);
                LiveProto.EnterLiveRsp rsp = null;
                try {
                    rsp = LiveProto.EnterLiveRsp.parseFrom(response.getData());
                    MyLog.w(TAG, "enterRoom response:" + rsp.toString());
                    subscriber.onNext(rsp);
                    subscriber.onCompleted();
                } catch (Exception e) {
                    MyLog.e(TAG, "enterRoom onError:" + e.getMessage());
                    subscriber.onError(e);
                }
            }
        });
    }
}
