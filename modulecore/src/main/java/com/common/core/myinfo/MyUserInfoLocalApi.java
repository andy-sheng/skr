package com.common.core.myinfo;

import com.common.core.db.GreenDaoManager;
import com.common.core.db.MyUserInfoDao;
import com.common.core.db.UserInfoDao;

import java.util.List;

public class MyUserInfoLocalApi {

    private static MyUserInfoDao getUserInfoDao() {
        return GreenDaoManager.getDaoSession().getMyUserInfoDao();
    }

    public static boolean insertOrReplace(MyUserInfo userInfo) {
        if (userInfo == null) {
            return false;
        }
        getUserInfoDao().insertOrReplaceInTx(userInfo);
        return true;
    }

    public static MyUserInfo getUserAccount(long uid) {
        List<MyUserInfo> userList = getUserInfoDao().queryBuilder()
                .where(MyUserInfoDao.Properties.Uid.eq(uid))
                .list();
        if (userList != null && !userList.isEmpty()) {
            return userList.get(0);
        }
        return null;
    }
}
