package com.mi.liveassistant.data.repository;


import com.mi.liveassistant.common.global.GlobalData;
import com.mi.liveassistant.common.log.MyLog;
import com.mi.liveassistant.dao.UserAccount;
import com.mi.liveassistant.dao.UserAccountDao;
import com.mi.liveassistant.greendao.GreenDaoManager;

import java.util.List;

/**
 * Created by zjn on 16-10-30.
 */
public class AccountLocalStore {
    public final static String TAG = AccountLocalStore.class.getSimpleName();

    private static AccountLocalStore sInstance;

    private UserAccountDao mUserAccountDao;

    private AccountLocalStore() {
        mUserAccountDao = GreenDaoManager.getDaoSession(GlobalData.app()).getUserAccountDao();
    }

    public static AccountLocalStore getInstance() {
        if (sInstance == null) {
            synchronized (AccountLocalStore.class) {
                if (sInstance == null) {
                    sInstance = new AccountLocalStore();
                }
            }
        }
        return sInstance;
    }

    public void deleteAll() {
        mUserAccountDao.deleteAll();
    }

    public void deleteAccount(String uuid) {
        String sql = String.format("delete from %s WHERE %s=%s",
                mUserAccountDao.getTablename(),
                UserAccountDao.Properties.Uuid.columnName,
                uuid);
        mUserAccountDao.getDatabase().execSQL(sql);
    }

    public void replaceAccount(UserAccount account) {
        deleteAccount(account.getUuid());
        mUserAccountDao.insertOrReplaceInTx(account);
    }

    public UserAccount getAccount(String uuid) {
        try {
            List list = mUserAccountDao.queryBuilder().where(UserAccountDao.Properties.Uuid.eq(uuid)).list();
            if (!list.isEmpty()) {
                return (UserAccount) list.get(0);
            }
        } catch (Exception e) {
            MyLog.e(TAG, "getAccount failed e=" + e);
        }
        return null;
    }

    public UserAccount getAccount() {
        try {
            List list = mUserAccountDao.queryBuilder().list();
            if (!list.isEmpty()) {
                return (UserAccount) list.get(0);
            }
        } catch (Exception e) {
            MyLog.e(TAG, "getAccount failed e=" + e);
        }
        return null;
    }
}
