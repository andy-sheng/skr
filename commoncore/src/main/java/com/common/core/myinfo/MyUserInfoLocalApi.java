package com.common.core.myinfo;

import com.common.core.db.GreenDaoManager;
import com.common.core.db.UserInfoDBDao;
import com.common.core.userinfo.UserInfoDB;
import com.common.core.userinfo.utils.UserInfoDataUtils;
import com.common.log.MyLog;

/**
 * 操作本地关系数据库,提供给RelationManager使用
 */
public class MyUserInfoLocalApi {
    //Relation中的relative  0为默认值, 1为双方未关注, 2为我关注该用户, 3为该用户关注我, 4为双方关注
    public static final String TAG = "MyUserInfoLocalApi";

    private static UserInfoDBDao getUserInfoDao() {
        return GreenDaoManager.getDaoSession().getUserInfoDBDao();
    }

    /**
     * 插入或者更新 db数据
     * <p>
     * 由于relation表存储关系和关系人信息,注意默认值引起的影响
     *
     * @param myUserInfoModel
     * @return
     */
    public static int insertOrUpdate(MyUserInfo myUserInfoModel) {
        MyLog.d(TAG, "insertOrUpdate");
        if (myUserInfoModel == null) {
            MyLog.w(TAG, "insertOrUpdate relation == null");
            return 0;
        }

        MyUserInfo myUserInfoInDB = getUserInfoByUUid(myUserInfoModel.getUserId());
        if (myUserInfoInDB != null) {
            UserInfoDataUtils.fill(myUserInfoModel, myUserInfoInDB);
        } else {
        }
        getUserInfoDao().insertOrReplaceInTx(MyUserInfo.toUserInfoDB(myUserInfoModel));
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
            MyUserInfo userInfo = getUserInfoByUUid(uid);
            if (userInfo != null) {
                MyLog.w(TAG, "deleteUserInfo in DB");
                getUserInfoDao().deleteByKey(uid);
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
    public static MyUserInfo getUserInfoByUUid(long uid) {
        if (uid <= 0) {
            return null;
        }
        UserInfoDB userInfoDB = getUserInfoDao().queryBuilder().where(UserInfoDBDao.Properties.UserId.eq(uid)).unique();
        if (userInfoDB == null) {
            return null;
        } else {
            return MyUserInfo.parseFromDB(userInfoDB);
        }
    }
//
//    /**
//     * 查询某些人关系的集合
//     *
//     * @param longs
//     * @return
//     */
//    public static List<UserInfoModel> getUserInfoByUUidList(List<Long> longs) {
//        if (longs == null || longs.isEmpty()) {
//            MyLog.w(TAG, "getRelationByUUidList but uids == null");
//            return null;
//        }
//
//        List<UserInfoDB> dbList = getUserInfoDao().queryBuilder().where(
//                UserInfoDao.Properties.UserId.in(longs)
//        ).list();
//        List<UserInfoModel> l = new ArrayList<>();
//        for (UserInfoDB db : dbList) {
//            l.add(UserInfoModel.parseFromDB(db));
//        }
//        return l;
//    }
//
//    /**
//     * 获取好友列表
//     *
//     * @param relative 关系类别
//     * @param isBlock  是否包含黑名单
//     * @return
//     */
//    public static List<UserInfoModel> getFriendUserInfoList(int relative, boolean isBlock) {
//        List<UserInfoDB> dbList = null;
//        if (isBlock) {
//            dbList = getUserInfoDao().queryBuilder().where(
//                    UserInfoDao.Properties.Relative.eq(relative)
//            ).build().list();
//        } else {
//            dbList = getUserInfoDao().queryBuilder().where(
//                    UserInfoDao.Properties.Relative.eq(relative),
//                    UserInfoDao.Properties.Block.eq(false)
//            ).build().list();
//        }
//
//        List<UserInfoModel> l = new ArrayList<>();
//        for (UserInfoDB db : dbList) {
//            l.add(UserInfoModel.parseFromDB(db));
//        }
//        return l;
//    }
//
//    /**
//     * 获取黑名单
//     *
//     * @returnf
//     */
//    public static List<UserInfoModel> getBockerList() {
//        List<UserInfoDB> dbList = getUserInfoDao().queryBuilder().where(
//                UserInfoDao.Properties.Block.eq(true)
//        ).build().list();
//        List<UserInfoModel> l = new ArrayList<>();
//        for (UserInfoDB db : dbList) {
//            l.add(UserInfoModel.parseFromDB(db));
//        }
//        return l;
//    }

}
