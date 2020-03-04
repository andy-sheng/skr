package com.module.playways;

import android.app.Activity;
import android.content.Context;
import android.support.v4.app.Fragment;

import com.alibaba.android.arouter.facade.template.IProvider;

/**
 * channel module 对外提供服务的接口
 */
public interface IPlaywaysModeService extends IProvider {
    Object getData(int type, Object object);

    Class getLeaderboardFragmentClass();

    void tryGoGrabRoom(int roomID, int inviteType);

    void tryGoCreateRoom();

    void tryGoGrabMatch(int tagId);

    void tryGoNewGrabMatch(Activity activity);

    void tryGoBattleMatch(int tagId);

    //    private Integer roomID;
//    private Long createdTimeMs;
//    private Long passedTimeMs;
//    private List<UserInfoModel> users;
//    private LocalCombineRoomConfig config;
    void jumpToDoubleRoom(Object o);

    void jumpToDoubleRoomFromDoubleRoomInvite(Object o);

    void createDoubleRoom();

    void jumpMicRoom(int roomID);

    void jumpMicRoomBySuggest(int roomID);

    IFriendRoomView getFriendRoomView(Context context);

    IPartyRoomView getPartyRoomView(Context context);

    void tryGoPartyRoom(int roomID, int joinSrc, int roomType);

    void tryToRelayRoomByOuterInvite(Object o);

    void tryToRelayRoomByRedPacketInvite(Object o);

    void acceptRelayRoomInvite(int ownerId, int roomID, long ts, int inviteType);

    boolean canShowRelayInvite(int peerUserID, int inviteType);

    void refuseJoinRelayRoom(int peerUserID, int refuseType, int inviteType);

    void tryInviteToRelay(int userID, boolean isFriend, boolean isFree);

    void idleHandler();

    void tryGoRelayHome();

    Fragment getPartyGameFragment();

    boolean isInRoom(int type, int roomID);
}
