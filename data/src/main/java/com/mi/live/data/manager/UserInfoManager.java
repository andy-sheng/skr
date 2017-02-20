package com.mi.live.data.manager;

import android.text.TextUtils;

import com.base.global.GlobalData;
import com.base.log.MyLog;
import com.base.preference.PreferenceUtils;
import com.google.protobuf.InvalidProtocolBufferException;
import com.mi.live.data.account.UserAccountManager;
import com.mi.live.data.api.ErrorCode;
import com.mi.live.data.api.LiveManager;
import com.mi.live.data.data.LiveShow;
import com.mi.live.data.manager.model.LiveRoomManagerModel;
import com.mi.live.data.milink.MiLinkClientAdapter;
import com.mi.live.data.milink.command.MiLinkCommand;
import com.mi.live.data.milink.constant.MiLinkConstant;
import com.mi.live.data.preference.PreferenceKeys;
import com.mi.live.data.user.User;
import com.mi.live.data.user.UserCache;
import com.mi.milink.sdk.aidl.PacketData;
import com.wali.live.proto.LiveCommonProto;
import com.wali.live.proto.LiveProto;
import com.wali.live.proto.UserProto;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lan on 15-11-18.
 */
public class UserInfoManager {
    private static final String TAG = UserInfoManager.class.getSimpleName();

    /**
     * 获取一个用户的信息
     *
     * @param needPullLiveInfo 是否需要拉取直播信息
     * @uuid 用户id
     */
    public static User getUserInfoByUuid(long uuid, boolean needPullLiveInfo) {
        if (uuid <= 0) {
            return null;
        }
        User user = new User();
        user.setUid(uuid);
        UserProto.GetHomepageReq req = UserProto.GetHomepageReq.newBuilder().setZuid(uuid).setGetLiveInfo(needPullLiveInfo).build();
        PacketData data = new PacketData();
        data.setCommand(MiLinkCommand.COMMAND_GET_HOMEPAGE);
        data.setData(req.toByteArray());
        MyLog.w(TAG + " getUserInfoByUuid request : \n" + req.toString());
        PacketData rspData = MiLinkClientAdapter.getsInstance().sendSync(data, MiLinkConstant.TIME_OUT);
        if (rspData != null) {
            try {
                UserProto.GetHomepageResp rsp = UserProto.GetHomepageResp.parseFrom(rspData.getData());
                MyLog.w(TAG + " getUserInfoByUuid response : \n" + rsp.toString());

                if (rsp.getRetCode() == 0) {
                    user.parse(rsp.getPersonalInfo());
                    user.parse(rsp.getPersonalData());
                    LiveCommonProto.UserRoomInfo roomInfo = LiveCommonProto.UserRoomInfo.parseFrom(rsp.getRoomInfo());
                    user.setTVRoomId(roomInfo.getTvRoomid());
                    user.setRoomType(roomInfo.getType());
                    user.setmAppType(roomInfo.getAppType());
                    user.setSellerStatus(rsp.getPersonalInfo().getSellerStatus());
                    if (rsp.getRankTopThreeListList() != null) {
                        user.setRankTopThreeList(rsp.getRankTopThreeListList());
                    }

                    if (!TextUtils.isEmpty(rsp.getViewUrl())) {
                        user.setViewUrl(rsp.getViewUrl());
                    }

                    if (!TextUtils.isEmpty(rsp.getRoomId())) {
                        user.setRoomId(rsp.getRoomId());
                    }

                    UserCache.addUser(user);
                    return user;
                }
            } catch (InvalidProtocolBufferException e) {
                e.printStackTrace();
            }
        }
        return user;
    }

    public static List<User> getUserListById(List<Long> uuidList) {

        List<User> users = new ArrayList<User>();

        if (uuidList == null || uuidList.size() == 0) {
            return users;
        }

        UserProto.MutiGetUserInfoReq req = UserProto.MutiGetUserInfoReq.newBuilder()
                .addAllZuid(uuidList)
                .build();
        PacketData data = new PacketData();
        data.setCommand(MiLinkCommand.COMMAND_GET_USER_LIST_BY_ID);
        data.setData(req.toByteArray());
        MyLog.w(TAG, "getUserListById request : \n" + req.toString());

        PacketData rspData = MiLinkClientAdapter.getsInstance().sendSync(data, MiLinkConstant.TIME_OUT);
        if (rspData == null) {
            return users;
        }

        try {
            UserProto.MutiGetUserInfoRsp rsp = UserProto.MutiGetUserInfoRsp.parseFrom(rspData.getData());
            MyLog.w(TAG, " getUserListById response : \n" + rsp.toString());
            List<UserProto.PersonalInfo> userList = rsp.getPersonalInfoList();
            if (userList == null || userList.size() == 0) {
                return users;
            }
            for (UserProto.PersonalInfo personInfo : userList) {
                users.add(new User(personInfo));
            }

        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
        return users;
    }


    public static User getUserInfoById(long uuid) {

        UserProto.GetUserInfoByIdRsp rsp = getUserInfoByIdReq(uuid);
        if (rsp == null) {
            return null;
        }
        UserProto.PersonalInfo userInfo = rsp.getPersonalInfo();
        if (rsp.getErrorCode() == ErrorCode.CODE_SUCCESS && userInfo != null) {
            return new User(userInfo);

        }
        return null;
    }


    /**
     * MiLinkCommand : zhibo.user.getuserinfobyid
     * 由ID获取用户信息
     */
    public static UserProto.GetUserInfoByIdRsp getUserInfoByIdReq(long uuid) {
        UserProto.GetUserInfoByIdReq req = UserProto.GetUserInfoByIdReq.newBuilder()
                .setZuid(uuid)
                .build();

        return getUserInfoRspFromServer(req);
    }

    private static UserProto.GetUserInfoByIdRsp getUserInfoRspFromServer(UserProto.GetUserInfoByIdReq req) {
        PacketData data = new PacketData();
        data.setCommand(MiLinkCommand.COMMAND_GET_USER_INFO_BY_ID);
        data.setData(req.toByteArray());
        MyLog.w(TAG, "getUserInfoRspFromServer request : \n" + req.toString());

        PacketData rspData = MiLinkClientAdapter.getsInstance().sendSync(data, MiLinkConstant.TIME_OUT);
        if (rspData != null) {
            try {
                UserProto.GetUserInfoByIdRsp rsp = UserProto.GetUserInfoByIdRsp.parseFrom(rspData.getData());
                MyLog.w(TAG, " getUserInfoRspFromServer response : \n" + rsp.toString());
                return rsp;
            } catch (InvalidProtocolBufferException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * MiLinkCommand : zhibo.user.getpersonaldata
     * 获取个人数据
     */
    public static UserProto.GetPersonalDataByIdRsp getPersonalDataByUuid(long uuid) {
        UserProto.GetPersonalDataByIdReq req = UserProto.GetPersonalDataByIdReq.newBuilder()
                .setZuid(uuid)
                .build();

        return getPersonalDataRspFromServer(req);
    }

    private static UserProto.GetPersonalDataByIdRsp getPersonalDataRspFromServer(UserProto.GetPersonalDataByIdReq req) {
        PacketData data = new PacketData();
        data.setCommand(MiLinkCommand.COMMAND_GET_PERRSONAL_DATA);
        data.setData(req.toByteArray());
        MyLog.w(TAG, " getPersonalDataRspFromServer request : \n" + req.toString());

        PacketData rspData = MiLinkClientAdapter.getsInstance().sendSync(data, MiLinkConstant.TIME_OUT);
        if (rspData != null) {
            try {
                UserProto.GetPersonalDataByIdRsp rsp = UserProto.GetPersonalDataByIdRsp.parseFrom(rspData.getData());
                MyLog.w(TAG, " getPersonalDataRspFromServer response : \n" + rsp.toString());
                return rsp;
            } catch (InvalidProtocolBufferException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * MiLinkCommand : zhibo.user.uploadusersetting
     * 上传设置
     */
    public static UserProto.UploadUserSettingRsp uploadUserSettingReq(boolean isPushable) {
        UserProto.UploadUserSettingReq req = UserProto.UploadUserSettingReq.newBuilder()
                .setZuid(UserAccountManager.getInstance().getUuidAsLong())
                .setIsPushable(isPushable)
                .build();

        return UploadUserSettingRspToServer(req);
    }

    private static UserProto.UploadUserSettingRsp UploadUserSettingRspToServer(UserProto.UploadUserSettingReq req) {
        PacketData data = new PacketData();
        data.setCommand(MiLinkCommand.COMMAND_UPLOAD_USER_SETTING);
        data.setData(req.toByteArray());
        MyLog.w(TAG, " UploadUserSettingRspToServer request : \n" + req.toString());

        PacketData rspData = MiLinkClientAdapter.getsInstance().sendSync(data, MiLinkConstant.TIME_OUT);
        if (rspData != null) {
            try {
                UserProto.UploadUserSettingRsp rsp = UserProto.UploadUserSettingRsp.parseFrom(rspData.getData());
                MyLog.w(TAG, " UploadUserSettingRspToServer response : \n" + rsp.toString());
                return rsp;
            } catch (InvalidProtocolBufferException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * MiLinkCommand : zhibo.user.search
     * 搜索
     */
    public static UserProto.SearchUserInfoRsp searchUserInfoReq(String keyword, int offset, int limit) {
        if (TextUtils.isEmpty(keyword) || limit <= 0 || offset < 0) {
            return null;
        }
        UserProto.SearchUserInfoReq req = UserProto.SearchUserInfoReq.newBuilder()
                .setKeyword(keyword)
                .setOffset(offset)
                .setLimit(limit)
                .build();
        PacketData data = new PacketData();
        data.setCommand(MiLinkCommand.COMMAND_SEARCH);
        data.setData(req.toByteArray());
        MyLog.d(TAG, "SearchUserInfoReq request : \n" + req.toString());

        PacketData rspData = MiLinkClientAdapter.getsInstance().sendSync(data, MiLinkConstant.TIME_OUT);
        if (rspData != null) {
            try {
                UserProto.SearchUserInfoRsp rsp = UserProto.SearchUserInfoRsp.parseFrom(rspData.getData());
                MyLog.d(TAG, "searchUserInfoRep response : \n" + rsp.toString());
                return rsp;
            } catch (InvalidProtocolBufferException e) {
                MyLog.e(e.toString());
            }
        }
        return null;
    }

    /**
     * MiLinkCommand :zhibo.user.uploaduserpro
     * 头像，姓名，签名，性别，头像md5
     * 更新用户信息
     */
    public static UserProto.UploadUserPropertiesRsp uploadUserInfoReq(Long avatar, String nickName, String sign, Integer gender, String avatarMd5) {
        UserProto.UploadUserPropertiesReq.Builder builder = UserProto.UploadUserPropertiesReq.newBuilder();
        builder.setZuid(UserAccountManager.getInstance().getUuidAsLong());
        if (avatar != null) {
            builder.setAvatar(avatar);
        }
        if (!TextUtils.isEmpty(nickName)) {
            builder.setNickname(nickName);
        }
        if (!TextUtils.isEmpty(sign)) {
            builder.setSign(sign);
        }
        if (gender != null) {
            builder.setGender(gender);
        }
        if (!TextUtils.isEmpty(avatarMd5)) {
            builder.setAvatarMd5(avatarMd5);
        }
        return UploadUserInfoRspToServer(builder);
    }

    public static UserProto.UploadUserPropertiesRsp uploadUserInfoReq(Boolean hasAvatar, Boolean hasNickName, Boolean hasGender, String nickName, Integer gender) {
        UserProto.UploadUserPropertiesReq.Builder builder = UserProto.UploadUserPropertiesReq.newBuilder();
        builder.setZuid(UserAccountManager.getInstance().getUuidAsLong());
        if (!hasAvatar) {
            builder.setAvatar(System.currentTimeMillis());
        }
        if (!hasNickName && !TextUtils.isEmpty(nickName)) {
            builder.setNickname(nickName);
        }
        if (!hasGender && (gender != null)) {
            builder.setGender(gender);
        }
        return UploadUserInfoRspToServer(builder);
    }

    private static UserProto.UploadUserPropertiesRsp UploadUserInfoRspToServer(UserProto.UploadUserPropertiesReq.Builder builder) {
        PacketData data = new PacketData();
        data.setCommand(MiLinkCommand.COMMAND_UPLOAD_USER_INFO);
        data.setData(builder.build().toByteArray());
        MyLog.w(TAG, "UploadUserInfoRspToServer request : \n" + builder.build().toString());

        PacketData rspData = MiLinkClientAdapter.getsInstance().sendSync(data, MiLinkConstant.TIME_OUT);
        MyLog.w(TAG, "UploadUserInfoRspToServer rspData =" + rspData);
        if (rspData != null) {
            try {
                UserProto.UploadUserPropertiesRsp rsp = UserProto.UploadUserPropertiesRsp.parseFrom(rspData.getData());
                MyLog.w(TAG, "UploadUserInfoRspToServer response : \n" + rsp.toString());
                return rsp;
            } catch (InvalidProtocolBufferException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * 上传推送设置
     */
    public static boolean uploadPushSetting(String key, boolean value) {
        UserProto.UploadUserSettingReq.Builder requestBuilder = UserProto.UploadUserSettingReq.newBuilder()
                .setZuid(UserAccountManager.getInstance().getUuidAsLong());
        requestBuilder.setIsNotifyNoDisturb(PreferenceUtils.getSettingBoolean(GlobalData.app().getApplicationContext(), PreferenceKeys.SETTING_NOTI_NO_DISTURB, false))
                .setIsNotifyNoShake(!PreferenceUtils.getSettingBoolean(GlobalData.app().getApplicationContext(), PreferenceKeys.PREFERENCE_OPEN_MESSAGE_VIBRATE, true))
                .setIsNotifyNoSound(!PreferenceUtils.getSettingBoolean(GlobalData.app().getApplicationContext(), PreferenceKeys.PREFERENCE_OPEN_MESSAGE_VOICE, true))
                .setIsPushable(PreferenceUtils.getSettingBoolean(GlobalData.app().getApplicationContext(), PreferenceKeys.PREFERENCE_KEY_SETTING_LIVE_NOTI_PUSH, true));
        switch (key) {
            // 开播提醒
            case PreferenceKeys.PREFERENCE_KEY_SETTING_LIVE_NOTI_PUSH:
                requestBuilder.setIsPushable(value);
                break;

            // 声音
            case PreferenceKeys.PREFERENCE_OPEN_MESSAGE_VOICE:
                requestBuilder.setIsNotifyNoSound(!value);
                break;

            // 振动
            case PreferenceKeys.PREFERENCE_OPEN_MESSAGE_VIBRATE:
                requestBuilder.setIsNotifyNoShake(!value);
                break;

            // 免打扰
            case PreferenceKeys.SETTING_NOTI_NO_DISTURB:
                requestBuilder.setIsNotifyNoDisturb(value);
                break;
        }
        PacketData packetData = new PacketData();
        packetData.setCommand(MiLinkCommand.COMMAND_UPLOAD_OWN_SETTING);
        packetData.setData(requestBuilder.build().toByteArray());
        PacketData responseData = MiLinkClientAdapter.getsInstance().sendSync(packetData, MiLinkConstant.TIME_OUT);
        try {
            if (responseData != null) {
                UserProto.UploadUserSettingRsp response = UserProto.UploadUserSettingRsp.parseFrom(responseData.getData());
                MyLog.w(TAG + " uploadSetting result:" + response.getErrorCode());
                if (response.getErrorCode() == 0) {
                    PreferenceUtils.setSettingBoolean(GlobalData.app(), key, value);
                }
                return response.getErrorCode() == 0;
            }
        } catch (InvalidProtocolBufferException e) {
            MyLog.e(e);
        }
        return false;
    }

    /**
     * 获取推送设置
     * 和服务器端同步设置，存在preference中
     */
    public static void getPushSetting() {
        UserProto.GetOwnSettingReq request = UserProto.GetOwnSettingReq.newBuilder().setZuid(UserAccountManager.getInstance().getUuidAsLong()).build();
        PacketData packetData = new PacketData();
        packetData.setCommand(MiLinkCommand.COMMAND_GET_OWN_SETTING);
        packetData.setData(request.toByteArray());
        PacketData responseData = MiLinkClientAdapter.getsInstance().sendSync(packetData, MiLinkConstant.TIME_OUT);
        try {
            if (responseData != null) {
                UserProto.GetOwnSettingRsp response = UserProto.GetOwnSettingRsp.parseFrom(responseData.getData());
                MyLog.w(TAG + " uploadSetting result:" + response.getRetCode());
                if (response.getRetCode() == 0) {
                    PreferenceUtils.setSettingBoolean(GlobalData.app(), PreferenceKeys.PREFERENCE_KEY_SETTING_LIVE_NOTI_PUSH, response.getIsPushable());
                    PreferenceUtils.setSettingBoolean(GlobalData.app(), PreferenceKeys.PREFERENCE_OPEN_MESSAGE_VOICE, !response.getIsNotifyNoSound());
                    PreferenceUtils.setSettingBoolean(GlobalData.app(), PreferenceKeys.PREFERENCE_OPEN_MESSAGE_VIBRATE, !response.getIsNotifyNoShake());
                    PreferenceUtils.setSettingBoolean(GlobalData.app(), PreferenceKeys.SETTING_NOTI_NO_DISTURB, response.getIsNotifyNoDisturb());
                }
            }
        } catch (InvalidProtocolBufferException e) {
            MyLog.e(e);
        }
    }

    /**
     * 根据uuid获得用户正在直播的房间信息
     *
     * @param uuid
     * @return 返回正在直播的liveShow, 如果房间不存在或者出错,　返回一个空值, 注意不是null.
     */
    public static LiveShow getLiveShowByUserId(long uuid) {
        if (uuid <= 0) {
            return new LiveShow();
        }
        UserProto.HisRoomReq request = UserProto.HisRoomReq.newBuilder().setZuid(uuid).build();
        PacketData packetData = new PacketData();
        packetData.setCommand(MiLinkCommand.COMMAND_GET_LIVE_ROOM);
        packetData.setData(request.toByteArray());
        PacketData responseData = MiLinkClientAdapter.getsInstance().sendSync(packetData, MiLinkConstant.TIME_OUT);
        try {
            if (responseData != null) {
                UserProto.HisRoomRsp response = UserProto.HisRoomRsp.parseFrom(responseData.getData());
                MyLog.w(TAG + " getLiveShowByUserId response : " + response.toString());
                if (response.getRetCode() == 0) {
                    LiveShow liveShow = new LiveShow();
                    liveShow.setUid(uuid);
                    liveShow.setLiveId(response.getLiveId());
                    liveShow.setUrl(response.getViewUrl());
                    liveShow.setLiveType(response.getType());
                    return liveShow;
                } else {
                    return new LiveShow();
                }

            } else {
                return new LiveShow();
            }
        } catch (InvalidProtocolBufferException e) {
            MyLog.e(e);
        }
        return new LiveShow();
    }

    public static LiveShow getLiveShowByUserIdAndLiveId(long uuid, String liveId) {
        if (uuid <= 0 || TextUtils.isEmpty(liveId)) {
            return new LiveShow();
        }
        LiveShow liveShow = new LiveShow();
        LiveProto.RoomInfoRsp rsp = LiveManager.roomInfoRsp(uuid, liveId);
        if (rsp == null) {
            return liveShow;
        }
        liveShow.setUid(uuid);
        liveShow.setLiveId(liveId);
        liveShow.setUrl(rsp.getDownStreamUrl());
        liveShow.hasReplayVideo = (!TextUtils.isEmpty(rsp.getPlaybackUrl())); // 获取是否具有回放的地址
        return liveShow;
    }

    public static int ADD_MANAGER = 1;

    public static int REMOVE_MANAGER = 2;

    /**
     * MiLinkCommand :zhibo.user.adminsetting
     * 管理员设置
     */
    public static boolean setManager(long managerUid, boolean isEnable, String roomId) {
        UserProto.AdminSettingReq request = UserProto.AdminSettingReq.newBuilder().setAdminUid(managerUid).setOperation(isEnable ? ADD_MANAGER : REMOVE_MANAGER).setRoomId(roomId).build();
        MyLog.w(TAG, "setManager request:" + request.toString());
        PacketData packetData = new PacketData();
        packetData.setCommand(MiLinkCommand.COMMAND_ADMIN_SETTING);
        packetData.setData(request.toByteArray());
        PacketData responseData = MiLinkClientAdapter.getsInstance().sendSync(packetData, MiLinkConstant.TIME_OUT);
        try {
            if (responseData != null) {
                UserProto.AdminSettingRsp response = UserProto.AdminSettingRsp.parseFrom(responseData.getData());
                MyLog.w(TAG + " setManager result:" + response.getRetCode());
                return response.getRetCode() == 0;
            }
        } catch (InvalidProtocolBufferException e) {
            MyLog.e(e);
        }
        return false;
    }

    /**
     * 查询管理员列表
     */
    public static List<LiveRoomManagerModel> getMyManagerList(long zuid) {
        List<LiveRoomManagerModel> list = new ArrayList<>();
        UserProto.AdminListReq request = UserProto.AdminListReq.newBuilder().setZuid(zuid).build();
        PacketData packetData = new PacketData();
        packetData.setCommand(MiLinkCommand.COMMAND_GET_ADMIN_LIST);
        packetData.setData(request.toByteArray());
        MyLog.v(TAG, " getMyManagerList request : \n" + request.toString());

        PacketData responseData = MiLinkClientAdapter.getsInstance().sendSync(packetData, MiLinkConstant.TIME_OUT);
        MyLog.v(TAG, " getViewerList responseData=" + responseData);
        try {
            if (responseData != null) {
                MyLog.v(TAG, "getViewerList getMnsCode:" + responseData.getMnsCode());
                UserProto.AdminListRsp response = UserProto.AdminListRsp.parseFrom(responseData.getData());
                if (response.getUserInfosList() != null) {
                    for (UserProto.PersonalInfo data : response.getUserInfosList()) {
                        LiveRoomManagerModel manager = new LiveRoomManagerModel(data.getZuid());
                        manager.level = data.getLevel();
                        manager.avatar = data.getAvatar();
                        manager.certificationType = data.getCertificationType();
                        manager.isInRoom = false;
                        LiveRoomCharactorManager.getInstance().setManager(manager, true);
                        list.add(new LiveRoomManagerModel(data));
                    }
                }
                //EventBus.getDefault().post(new EventClass.LiveRoomManagerEvent(list, true, true, false));
                return list;
            }
        } catch (InvalidProtocolBufferException e) {
            MyLog.e(e);
        }
        return list;

    }

}
