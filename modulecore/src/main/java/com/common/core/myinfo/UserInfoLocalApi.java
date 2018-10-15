package com.common.core.myinfo;

import com.common.core.account.UserAccount;
import com.common.core.db.GreenDaoManager;
import com.common.core.db.UserInfoDao;

import java.util.List;

public class UserInfoLocalApi {

    private static UserInfoDao getUserInfoDao() {
        return GreenDaoManager.getDaoSession().getUserInfoDao();
    }

    public static boolean insertOrReplace(UserInfo userInfo) {
        if (userInfo == null) {
            return false;
        }
        getUserInfoDao().insertOrReplaceInTx(userInfo);
        return true;
    }

    public static UserInfo getUserAccount(long uid) {
        List<UserInfo> userList = getUserInfoDao().queryBuilder()
                .where(UserInfoDao.Properties.Uid.eq(uid))
                .list();
        if (userList != null && !userList.isEmpty()) {
            return userList.get(0);
        }
        return null;
    }
}
