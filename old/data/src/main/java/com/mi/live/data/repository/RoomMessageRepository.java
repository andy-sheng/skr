package com.mi.live.data.repository;

import com.mi.live.data.repository.datasource.RoomMessageStore;
import com.wali.live.proto.LiveMessageProto;

import javax.inject.Inject;

import rx.Observable;

/**
 * Created by chengsimin on 16/6/13.
 */
public class RoomMessageRepository {

    private final RoomMessageStore roomMessageStore;

    @Inject
    public RoomMessageRepository(RoomMessageStore roomMessageStore) {
        this.roomMessageStore = roomMessageStore;
    }

    public Observable<LiveMessageProto.SyncRoomMessageResponse> pullRoomMessage(long fromUserId, String roomId, long lastSyncImportantTs, long lastSyncNormalTs) {
        return this.roomMessageStore.roomMessage(fromUserId, roomId, lastSyncImportantTs, lastSyncNormalTs);
    }
}
