package com.mi.live.data.cache;

import android.text.TextUtils;

import com.base.global.GlobalData;
import com.base.log.MyLog;
import com.base.preference.PreferenceUtils;
import com.mi.live.data.repository.RoomStatusRepository;
import com.mi.live.data.repository.datasource.RoomStatusStore;
import com.wali.live.proto.LiveProto;

import java.util.LinkedHashMap;

import rx.Observer;
import rx.schedulers.Schedulers;

/**
 * Created by chengsimin on 16/7/4.
 */
public class RoomInfoGlobalCache {

    private static final java.lang.String TAG = "RoomInfoGlobalCache";

    public static final String PRE_KEY_THIRD_PARTY_ZHIBO_LIVE_ID_AND_USERID = "pre_key_third_party_zhibo_live_id_and_user_id"; //使用第三方app 直播的房间id

    private static final long MIN_INTERVAL_FOR_LEAVE_ROOM = 2 * 60 * 1000;

    private static RoomInfoGlobalCache sInstance;

    //保存 id——发送离开房间请求的时间
    private static LinkedHashMap<String, Long> sLeaveRoomReqMap = new LinkedHashMap<>();

    private RoomInfoGlobalCache() {
        String liveIdAndId = PreferenceUtils.getSettingString(GlobalData.app(), PRE_KEY_THIRD_PARTY_ZHIBO_LIVE_ID_AND_USERID, "");
        if (!TextUtils.isEmpty(liveIdAndId)) {
            mCurrentThirdPartyLiveId = liveIdAndId.split(";")[0];
        }
    }

    public static synchronized RoomInfoGlobalCache getsInstance() {
        if (sInstance == null) {
            sInstance = new RoomInfoGlobalCache();
        }
        return sInstance;
    }


    /**
     * 保存正在观看的房间id
     */
    private String mCurrentRoomId = ""; //保存当前房间id

    // 现在允许同时有一个无人机房间 且 进入一个主播的房间进行观看
    /**
     * 第三方直播的房间id
     */
    private String mCurrentThirdPartyLiveId = ""; //第三方app id 正在直播的id

    // 是不是答题直播间
    private boolean mIsContestRoom = false;

    public String getCurrentRoomId() {
        return mCurrentRoomId;
    }

    public void enterCurrentRoom(String roomid) {
        MyLog.w(TAG, "enterCurrentRoom roomid:" + roomid);
        mCurrentRoomId = roomid;
    }

    public void leaveCurrentRoom(String roomid) {
        if (roomid != null && roomid.equals(mCurrentRoomId)) {
            MyLog.w(TAG, "leaveCurrentRoom roomid:" + roomid);
            mCurrentRoomId = "";
        }
    }

    public void enterThirdPartyRoom(String roomid) {
        MyLog.w(TAG, "enterThirdPartyRoom roomid:" + roomid);
        mCurrentThirdPartyLiveId = roomid;
    }

    public void leaveThirdPartyRoom(String roomid) {
        if (roomid != null && roomid.equals(mCurrentThirdPartyLiveId)) {
            MyLog.w(TAG, "leaveThirdPartyRoom roomid:" + roomid);
            mCurrentThirdPartyLiveId = "";
        }
    }

    public void enterContestRoom(String roomid) {
        MyLog.w(TAG, "enterContestRoom roomid:" + roomid);
        mCurrentRoomId = roomid;
        mIsContestRoom = true;
    }

    public void leaveContestRoom(String roomid) {
        if (roomid != null && roomid.equals(mCurrentRoomId)) {
            MyLog.w(TAG, "leaveContestRoom roomid:" + roomid);
            mCurrentRoomId = "";
        }
        mIsContestRoom = false;
    }

    public boolean isContestRoom() {
        return mIsContestRoom;
    }

    /**
     * @param roomId
     * @return
     */
    private boolean canSendLeaveReq(String roomId) {
        if (TextUtils.isEmpty(roomId)) {
            return false;
        }
        synchronized (sLeaveRoomReqMap) {
            Long a = sLeaveRoomReqMap.get(roomId);
            long now = System.currentTimeMillis();
            // 保证两分钟内同一个roomid只允许发一次
            if (a == null || now - a > MIN_INTERVAL_FOR_LEAVE_ROOM) {
                // 限定一下大小
                if (sLeaveRoomReqMap.size() > 20) {
                    String firstKey = sLeaveRoomReqMap.keySet().iterator().next();
                    sLeaveRoomReqMap.remove(firstKey);
                }
                sLeaveRoomReqMap.put(roomId, now);
                return true;
            }
        }
        return false;
    }

    private RoomStatusRepository mRoomStatusRepository = new RoomStatusRepository(new RoomStatusStore());

    public void sendLeaveRoomIfNeed(long anchorId, String roomId) {
        //如果是这个消息是给第三方的，也不关心是否需要发离开房间请求
        if (!roomId.equals(mCurrentThirdPartyLiveId)) {
            if (!mCurrentRoomId.equals(roomId)) {
                MyLog.w(TAG, "not match current roomid");
                if (canSendLeaveReq(roomId)) {
                    // 发离开房间给服务器
                    mRoomStatusRepository.leaveRooom(anchorId, roomId)
                            .subscribeOn(Schedulers.io())
                            .subscribe(new Observer<LiveProto.LeaveLiveRsp>() {
                                @Override
                                public void onCompleted() {

                                }

                                @Override
                                public void onError(Throwable e) {

                                }

                                @Override
                                public void onNext(LiveProto.LeaveLiveRsp leaveLiveRsp) {

                                }
                            });
                }
            }
        }
    }
}
