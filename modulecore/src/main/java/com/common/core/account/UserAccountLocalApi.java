package com.common.core.account;

import com.common.core.db.GreenDaoManager;
import com.common.core.db.UserAccountDao;
import com.common.log.MyLog;

import org.greenrobot.greendao.query.WhereCondition;

import java.util.List;

/**
 * Created by chengsimin on 16/7/1.
 */
public class UserAccountLocalApi {

    public final static String TAG = "UserAccountLocalApi";

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
        // 未退出登陆的
        List<UserAccount> accountList = getAccountDao().queryBuilder()
                .where(UserAccountDao.Properties.ChannelId.eq(channelId),
                        UserAccountDao.Properties.IsLogOff.eq(0))
                .list();
        if (accountList != null && !accountList.isEmpty()) {
            return accountList.get(0);
        }
        return null;
    }

    public static void loginAccount(UserAccount account) {
        // 登陆一个账号
        if (account != null) {
            String sql = String.format("update %s set %s=%s where %s=%s",
                    UserAccountDao.TABLENAME
                    , UserAccountDao.Properties.IsLogOff
                    , 1
                    , UserAccountDao.Properties.ChannelId
                    , account.getChannelId());
            MyLog.d(TAG, "loginAccount" + " sql=" + sql);
            getAccountDao().getDatabase().execSQL(sql);
            insertOrReplace(account);
        }

    }

    public static void delete(UserAccount account) {
        getAccountDao().delete(account);
    }
}
