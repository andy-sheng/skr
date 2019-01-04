package com.common.core.userinfo;

import com.common.core.db.GreenDaoManager;
import com.common.core.db.UserInfoDBDao;
import com.common.core.userinfo.event.UserInfoDBChangeEvent;
import com.common.log.MyLog;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import static com.common.core.userinfo.event.UserInfoDBChangeEvent.EVENT_DB_INSERT;
import static com.common.core.userinfo.event.UserInfoDBChangeEvent.EVENT_DB_REMOVE;
import static com.common.core.userinfo.event.UserInfoDBChangeEvent.EVENT_DB_UPDATE;
import static com.common.core.userinfo.event.UserInfoDBChangeEvent.EVENT_INIT;

/**
 * 操作本地关系数据库,提供给RelationManager使用
 */
public class UserInfoLocalApi {
    //Relation中的relative  0为未关注，1为已关注 2为互关
    private static final String TAG = "UserInfoLocalApi";

    public static final int UN_FOLLOW = 0;    // 未关注
    public static final int ONE_FOLLOW = 1;   // 已关注
    public static final int INTER_FOLLOW = 2; //互相关注

    private static UserInfoDBDao getUserInfoDao() {
        return GreenDaoManager.getDaoSession().getUserInfoDBDao();
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
    public static int insertOrUpdate(List<UserInfoModel> userInfoList) {
        MyLog.d(TAG, "insertOrUpdate" + " relationList.size=" + userInfoList.size());
        if (userInfoList == null || userInfoList.isEmpty()) {
            MyLog.w(TAG + " insertOrUpdate relationList == null");
            return 0;
        }
        List<UserInfoDB> userInfoDBList = new ArrayList<>();
        for (UserInfoModel userInfoModel : userInfoList) {
            UserInfoDB userInfoDB = UserInfoModel.toUserInfoDB(userInfoModel);
            userInfoDBList.add(userInfoDB);
        }
        getUserInfoDao().insertOrReplaceInTx(userInfoDBList);
        // 默认插入list都是从服务器直接获取,即一次初始化
        EventBus.getDefault().post(new UserInfoDBChangeEvent(EVENT_INIT, null));
        return userInfoList.size();
    }

    /**
     * 插入或者更新 db数据
     * <p>
     * 由于relation表存储关系和关系人信息,注意默认值引起的影响
     *
     * @param userInfoModel
     * @param relationChange 关系是否改变
     * @param blockChange    拉黑状态是否改变
     * @return
     */
    public static int insertOrUpdate(UserInfoModel userInfoModel, boolean relationChange, boolean blockChange) {
        MyLog.d(TAG, "insertOrUpdate");
        if (userInfoModel == null) {
            MyLog.w(TAG, "insertOrUpdate relation == null");
            return 0;
        }

        UserInfoModel userInfoModelInDB = getUserInfoByUUid(userInfoModel.getUserId());
        if (userInfoModelInDB != null) {
            UserInfoDataUtils.fill(userInfoModel, userInfoModelInDB);
            EventBus.getDefault().post(new UserInfoDBChangeEvent(EVENT_DB_UPDATE, userInfoModel));
        } else {
            EventBus.getDefault().post(new UserInfoDBChangeEvent(EVENT_DB_INSERT, userInfoModel));
        }
        getUserInfoDao().insertOrReplaceInTx(UserInfoModel.toUserInfoDB(userInfoModel));
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
            UserInfoModel userInfo = getUserInfoByUUid(uid);
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
    public static UserInfoModel getUserInfoByUUid(long uid) {
        if (uid <= 0) {
            return null;
        }
        UserInfoDB userInfoDB = getUserInfoDao().queryBuilder().where(UserInfoDBDao.Properties.UserId.eq(uid)).unique();
        if (userInfoDB == null) {
            return null;
        } else {
            return UserInfoModel.parseFromDB(userInfoDB);
        }
    }

    /**
     * 查询某些人关系的集合
     *
     * @param longs
     * @return
     */
    public static List<UserInfoModel> getUserInfoByUUidList(List<Long> longs) {
        if (longs == null || longs.isEmpty()) {
            MyLog.w(TAG, "getRelationByUUidList but uids == null");
            return null;
        }

        List<UserInfoDB> dbList = getUserInfoDao().queryBuilder().where(
                UserInfoDBDao.Properties.UserId.in(longs)
        ).list();
        List<UserInfoModel> l = new ArrayList<>();
        for (UserInfoDB db : dbList) {
            l.add(UserInfoModel.parseFromDB(db));
        }
        return l;
    }

    /**
     * 获取好友列表
     *
     * @param relative 关系类别
     * @param isBlock  是否包含黑名单
     * @return
     */
    public static List<UserInfoModel> getFriendUserInfoList(int relative, boolean isBlock) {
        List<UserInfoDB> dbList = null;
        if (isBlock) {
            dbList = getUserInfoDao().queryBuilder().where(
                    UserInfoDBDao.Properties.Relative.eq(relative)
            ).build().list();
        } else {
            dbList = getUserInfoDao().queryBuilder().where(
                    UserInfoDBDao.Properties.Relative.eq(relative),
                    UserInfoDBDao.Properties.Block.eq(false)
            ).build().list();
        }

        List<UserInfoModel> l = new ArrayList<>();
        for (UserInfoDB db : dbList) {
            l.add(UserInfoModel.parseFromDB(db));
        }
        return l;
    }

    /**
     * 获取黑名单
     *
     * @returnf
     */
    public static List<UserInfoModel> getBockerList() {
        List<UserInfoDB> dbList = getUserInfoDao().queryBuilder().where(
                UserInfoDBDao.Properties.Block.eq(true)
        ).build().list();
        List<UserInfoModel> l = new ArrayList<>();
        for (UserInfoDB db : dbList) {
            l.add(UserInfoModel.parseFromDB(db));
        }
        return l;
    }

}
