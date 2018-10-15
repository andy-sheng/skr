package com.common.core.account;

import com.common.core.db.GreenDaoManager;
import com.common.core.db.UserAccountDao;

import org.greenrobot.greendao.query.WhereCondition;

import java.util.List;

/**
 * Created by chengsimin on 16/7/1.
 */
public class UserAccountLocalApi {

    private static UserAccountDao getAccountDao() {
        return GreenDaoManager.getDaoSession().getUserAccountDao();
    }

    public static boolean insertOrReplace(UserAccount account) {
        if (account == null) {
            return false;
        }
        getAccountDao().insertOrReplaceInTx(account);
        return true;
    }

    public static UserAccount getUserAccount(long channelId) {
        List<UserAccount> accountList = getAccountDao().queryBuilder()
                .where(UserAccountDao.Properties.ChannelId.eq(channelId))
                .list();
        if (accountList != null && !accountList.isEmpty()) {
            return accountList.get(0);
        }
        return null;
    }
}
