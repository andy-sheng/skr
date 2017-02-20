package com.mi.live.data.repository;

import com.mi.live.data.account.UserAccountManager;
import com.mi.live.data.repository.datasource.LiveShowStore;
import com.mi.live.data.repository.datasource.RoomStatusStore;
import com.wali.live.proto.LiveProto;

import rx.Observable;
import rx.Subscriber;

/**
 * Created by chengsimin on 16/7/6.
 */
public class RoomStatusRepository {

    private final RoomStatusStore roomStatusStore;

    public RoomStatusRepository(RoomStatusStore roomStatusStore) {
        this.roomStatusStore = roomStatusStore;
    }

    public Observable<LiveProto.LeaveLiveRsp> leaveRooom(long anchorId, String roomid){
        return roomStatusStore.leaveRooom(anchorId,roomid);
    }

    public Observable<LiveProto.EnterLiveRsp> enterRooom(long anchorId, String roomid){
        return roomStatusStore.enterRoom(anchorId,roomid);
    }
}
