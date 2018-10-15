package com.mi.live.data.repository.datasource;

import com.base.log.MyLog;
import com.mi.live.data.milink.MiLinkClientAdapter;
import com.mi.live.data.milink.command.MiLinkCommand;
import com.mi.milink.sdk.aidl.PacketData;
import com.wali.live.proto.LiveMessageProto;

import javax.inject.Inject;

import rx.Observable;
import rx.Subscriber;

/**
 * Created by chengsimin on 16/6/13.
 */
public class RoomMessageStore {
    public static final String TAG = "RoomMessageStore";

    @Inject
    public RoomMessageStore() {

    }

    public Observable<LiveMessageProto.SyncRoomMessageResponse> roomMessage(final long fromUserId, final String roomId, final long lastSyncImportantTs, final long lastSyncNormalTs) {
        return Observable.create(new Observable.OnSubscribe<LiveMessageProto.SyncRoomMessageResponse>() {
            @Override
            public void call(Subscriber<? super LiveMessageProto.SyncRoomMessageResponse> subscriber) {
                LiveMessageProto.SyncRoomMessageRequest req = LiveMessageProto.SyncRoomMessageRequest.newBuilder()
                        .setFromUser(fromUserId)
                        .setRoomId(roomId)
                        .setLastSyncImportantTs(lastSyncImportantTs)
                        .setLastSyncNormalTs(lastSyncNormalTs)
                        .build();
                PacketData data = new PacketData();
                data.setCommand(MiLinkCommand.COMMAND_PULL_ROOM_MESSAGE);
                data.setData(req.toByteArray());
                data.setNeedCached(false);
                MyLog.d(TAG, "pullRoomMessage request:" + req.toString());
                PacketData response = MiLinkClientAdapter.getsInstance().sendSync(data, 10 * 1000);
                LiveMessageProto.SyncRoomMessageResponse rsp = null;
                try {
                    rsp = LiveMessageProto.SyncRoomMessageResponse.parseFrom(response.getData());
                    MyLog.d(TAG, "pullRoomMessage response:" + rsp.toString());
                    subscriber.onNext(rsp);
                    subscriber.onCompleted();
                } catch (Exception e) {
                    subscriber.onError(new Exception(e.getCause()));
                }
            }
        });
    }
}
