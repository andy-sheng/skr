package com.mi.live.data.data;

import com.mi.live.data.user.User;
import com.wali.live.dao.Relation;
import com.wali.live.proto.LiveManagerProto;
import com.wali.live.proto.RankProto.RankUser;
import com.wali.live.proto.RelationProto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by yurui on 2/23/16.
 */
public class UserListData implements Serializable {
    /*以下是用户基础信息*/
    public long userId;
    public long avatar;
    public String userNickname;          // 昵称
    public String signature;             // 签名
    public int gender;                   // 性别
    public int level;                    // 等级
    //public int badge;                    // 徽章
    public int mTicketNum;               // 星票
    public int certificationType;        //认证类型
    public boolean mIsPking;             //是否pk中
    public boolean mIsShowing;           //是否在直播
    public int mViewerNum;               //pk观众数


    public boolean isFollowing;          // 是否关注 [仅在查询别人的粉丝、关注列表时需要]
    public boolean isPushable;           // 是否推送 [仅在查询关注列表时需要]
    public boolean isBothway;            // 是否双向关注 [判断双向关注]

    public long mWatchTime;

    public int mPosition = -1;           //列表中的pos 从0开始  星票排名用

    public UserListData() {
    }

    public UserListData(RankUser userInfo) {

        userId = userInfo.getUuid();
        avatar = userInfo.getAvatar();
        userNickname = userInfo.getNickname();
        mTicketNum = userInfo.getTicket();
        //0,未关注；1,已关注；2，双向关注
        int relation = userInfo.getRelation();
        level = userInfo.getLevel();
        gender = userInfo.getGender();
        certificationType = userInfo.getCertificationType();
        switch (relation) {
            case 0:
                isFollowing = false;
                isBothway = false;
                break;
            case 1:
                isFollowing = true;
                isBothway = false;
                break;
            case 2:
                isFollowing = true;
                isBothway = true;
                break;
        }

    }


    public UserListData(RelationProto.UserInfo userInfo) {
        userId = userInfo.getUserId();
        avatar = userInfo.getAvatar();
        userNickname = userInfo.getNickname();
        signature = userInfo.getSignature();
        gender = userInfo.getGender();
//        badge = userInfo.getBadge();
        certificationType = userInfo.getCertificationType();
        level = userInfo.getLevel();
        if (userInfo.hasIsFollowing()) {
            isFollowing = userInfo.getIsFollowing();
        }
        if (userInfo.hasIsPushable()) {
            isPushable = userInfo.getIsPushable();
        }
        if (userInfo.hasIsBothway()) {
            isBothway = userInfo.getIsBothway();
        }
        if (userInfo.hasIsPking()) {
            mIsPking = userInfo.getIsPking();
        }
        if (userInfo.hasIsShowing()) {
            mIsShowing = userInfo.getIsShowing();
        }
        if (userInfo.hasViewerCnt()) {
            mViewerNum = userInfo.getViewerCnt();
        }
    }

    public UserListData(LiveManagerProto.UserInfo userInfo) {
        userId = userInfo.getZuid();
        avatar = userInfo.getAvatar();
        userNickname = userInfo.getNickname();
        signature = userInfo.getSign();
        gender = userInfo.getGender();
        certificationType = userInfo.getCertificationType();
        level = userInfo.getLevel();

    }

    public UserListData(Relation userInfo) {
        userId = userInfo.getUserId();
        avatar = userInfo.getAvatar();
        userNickname = userInfo.getUserNickname();
        signature = userInfo.getSignature();

        gender = userInfo.getGender();
        certificationType = userInfo.getCertificationType();
        level = userInfo.getLevel();
        mTicketNum = userInfo.getMTicketNum();

        isFollowing = userInfo.getIsFollowing();
        isBothway = userInfo.getIsBothway();
    }

    public Relation toRelation() {

        Relation relation = new Relation();
        relation.setUserId(this.userId);
        relation.setAvatar(this.avatar);
        relation.setUserNickname(this.userNickname);
        relation.setSignature(this.signature);

        relation.setGender(this.gender);
        relation.setCertificationType(this.certificationType);
        relation.setLevel(this.level);
        relation.setMTicketNum(this.mTicketNum);

        relation.setIsFollowing(this.isFollowing);
        relation.setIsBothway(this.isBothway);
        return relation;
    }


    public static List<Object> parseUserList(RelationProto.FollowingListResponse response, boolean isMyFollow) {
        List<Object> list = new ArrayList<>();
        for (RelationProto.UserInfo userInfo : response.getUsersList()) {
            UserListData item = new UserListData(userInfo);
            if (isMyFollow) {
                item.isFollowing = true;
                //TODO
                //AvatarUtils.updateMyFollowAvatarTimeStamp(item.userId, item.avatar);
            }
            list.add(item);
        }
        return list;
    }

    public static List<Object> parseUserList(RelationProto.PkUserListResponse response) {
        List<Object> list = new ArrayList<>();
        for (RelationProto.UserInfo userInfo : response.getUsersList()) {
            UserListData item = new UserListData(userInfo);
            if (item.mIsShowing) {
                list.add(item);
            }
        }
        return list;
    }

    public static List<Object> parseUserList(RelationProto.MicUserListResponse response) {
        List<Object> list = new ArrayList<>();
        for (RelationProto.UserInfo userInfo : response.getUsersList()) {
            UserListData item = new UserListData(userInfo);
            list.add(item);
        }
        return list;
    }

    public static List<Object> parseUserList(RelationProto.FollowerListResponse response) {
        List<Object> list = new ArrayList<>();
        for (RelationProto.UserInfo userInfo : response.getUsersList()) {
            UserListData item = new UserListData(userInfo);
            list.add(item);
        }
        return list;
    }

    public static List<UserListData> parseList(RelationProto.FollowerListResponse response) {
        List<UserListData> list = new ArrayList<>();
        for (RelationProto.UserInfo userInfo : response.getUsersList()) {
            UserListData item = new UserListData(userInfo);
            list.add(item);
        }
        return list;
    }

    public static List<Object> parseUserList(RelationProto.BlockerListResponse response) {
        List<Object> list = new ArrayList<>();
        for (RelationProto.UserInfo userInfo : response.getUsersList()) {
            UserListData item = new UserListData(userInfo);
            list.add(item);
        }
        return list;
    }

    public static List<Object> parseUserList(List<Relation> relationList) {
        List<Object> list = new ArrayList<>();
        for (Relation relation : relationList) {
            UserListData item = new UserListData(relation);
            list.add(item);
        }
        return list;
    }

    public User toUser() {
        User user = new User();
        user.setUid(userId);
        user.setAvatar(avatar);
        user.setNickname(userNickname);
        user.setSign(signature);
        user.setGender(gender);
        user.setLevel(level);
        user.setCertificationType(certificationType);
        return user;
    }

    @Override
    public boolean equals(Object o) {
        UserListData data = (UserListData) o;
        return this.userId == data.userId;
    }
}
