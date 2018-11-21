package com.common.core.userinfo;

import android.text.TextUtils;

import com.common.core.db.GreenDaoManager;
import com.common.core.db.UserInfoDao;
import com.common.core.userinfo.event.UserInfoDBChangeEvent;
import com.common.log.MyLog;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import static com.common.core.userinfo.event.UserInfoDBChangeEvent.EVENT_DB_INSERT;
import static com.common.core.userinfo.event.UserInfoDBChangeEvent.EVENT_DB_REMOVE;
import static com.common.core.userinfo.event.UserInfoDBChangeEvent.EVENT_DB_UPDATE;
import static com.common.core.userinfo.event.UserInfoDBChangeEvent.EVENT_INIT;

/**
 * 操作本地关系数据库,提供给RelationManager使用
 */
public class UserInfoLocalApi {
    //Relation中的relative  0为默认值, 1为双方未关注, 2为我关注该用户, 3为该用户关注我, 4为双方关注
    private static final String TAG = "UserInfoLocalApi";

    private static UserInfoDao getUserInfoDao() {
        return GreenDaoManager.getDaoSession().getUserInfoDao();
    }

    /**
     * 插入或者更新 db数据
     * <p>
     * 这个函数每次插入的时候都会清空本地的数据，所以Relation这个标最好所有字段都跟服务器
     * 保持一z致，不要有额外的其他字段，否则以后从服务器拉取了数据回来本地之前的数据会被覆盖
     *
     * @param userInfoList
     * @return
     */
    public static int insertOrUpdate(List<UserInfo> userInfoList) {
        MyLog.d(TAG, "insertOrUpdate" + " relationList.size=" + userInfoList.size());
        if (userInfoList == null || userInfoList.isEmpty()) {
            MyLog.w(TAG + " insertOrUpdate relationList == null");
            return 0;
        }
        getUserInfoDao().insertOrReplaceInTx(userInfoList);
        // 默认插入list都是从服务器直接获取,即一次初始化
        EventBus.getDefault().post(new UserInfoDBChangeEvent(EVENT_INIT, null));
        return userInfoList.size();
    }

    /**
     * 插入或者更新 db数据
     * <p>
     * 由于relation表存储关系和关系人信息,注意默认值引起的影响
     *
     * @param userInfo
     * @param relationChange 关系是否改变
     * @param blockChange    拉黑状态是否改变
     * @return
     */
    public static int insertOrUpdate(UserInfo userInfo, boolean relationChange, boolean blockChange) {
        MyLog.d(TAG, "insertOrUpdate");
        if (userInfo == null) {
            MyLog.w(TAG, "insertOrUpdate relation == null");
            return 0;
        }

        UserInfo userInfoDB = getUserInfoByUUid(userInfo.getUserId());
        if (userInfoDB != null) {
            userInfo.fill(userInfoDB);
            EventBus.getDefault().post(new UserInfoDBChangeEvent(EVENT_DB_UPDATE, userInfo));
        } else {
            EventBus.getDefault().post(new UserInfoDBChangeEvent(EVENT_DB_INSERT, userInfo));
        }
        getUserInfoDao().insertOrReplaceInTx(userInfo);
        return 1;
    }

    /**
     * 全部删除
     *
     * @return
     */
    public static void deleteAll() {
        getUserInfoDao().deleteAll();
    }

    /**
     * 删除指定关系人
     *
     * @param uid
     * @return
     */
    public static boolean deleUserInfoByUUid(long uid) {
        if (uid > 0) {
            UserInfo userInfo = getUserInfoByUUid(uid);
            if (userInfo != null) {
                MyLog.w(TAG, "deleteUserInfo in DB");
                getUserInfoDao().deleteByKey(uid);
                EventBus.getDefault().post(new UserInfoDBChangeEvent(EVENT_DB_REMOVE, userInfo));
                return true;
            }
            MyLog.w(TAG, "deleteRelation but not exist in DB");
        }
        return false;
    }

    /**
     * 查询和某人的关系
     *
     * @param uid
     * @return
     */
    public static UserInfo getUserInfoByUUid(long uid) {
        if (uid <= 0) {
            return null;
        }
        return getUserInfoDao().queryBuilder().where(UserInfoDao.Properties.UserId.eq(uid)).unique();
    }

    /**
     * 查询某些人关系的集合
     *
     * @param longs
     * @return
     */
    public static List<UserInfo> getUserInfoByUUidList(List<Long> longs) {
        if (longs == null || longs.isEmpty()) {
            MyLog.w(TAG, "getRelationByUUidList but uids == null");
            return null;
        }

        return getUserInfoDao().queryBuilder().where(
                UserInfoDao.Properties.UserId.in(longs)
        ).list();
    }

    /**
     * 获取好友列表
     *
     * @param relative 关系类别
     * @param isBlock  是否包含黑名单
     * @return
     */
    public static List<UserInfo> getFriendUserInfoList(int relative, boolean isBlock) {
        List<UserInfo> userInfos = null;
        if (isBlock) {
            userInfos = getUserInfoDao().queryBuilder().where(
                    UserInfoDao.Properties.Relative.eq(relative)
            ).build().list();
        } else {
            userInfos = getUserInfoDao().queryBuilder().where(
                    UserInfoDao.Properties.Relative.eq(relative),
                    UserInfoDao.Properties.Block.eq(false)
            ).build().list();
        }

        return userInfos;
    }

    /**
     * 获取黑名单
     *
     * @return
     */
    public static List<UserInfo> getBockerList() {
        List<UserInfo> userInfos = null;
        userInfos = getUserInfoDao().queryBuilder().where(
                UserInfoDao.Properties.Block.eq(true)
        ).build().list();
        return userInfos;
    }

}
