package com.mi.live.data.repository.datasource;

import com.base.global.GlobalData;
import com.mi.live.data.greendao.GreenDaoManager;
import com.wali.live.dao.UserAccount;
import com.wali.live.dao.UserAccountDao;

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

    public void deleteAccount(int channelId) {
        String sql = String.format("delete from %s WHERE %s=%s",
                mUserAccountDao.getTablename(),
                UserAccountDao.Properties.Channelid.columnName,
                channelId);
        mUserAccountDao.getDatabase().execSQL(sql);
    }

    public void replaceAccount(UserAccount account) {
        deleteAccount(account.getChannelid());
        mUserAccountDao.insertOrReplaceInTx(account);

        String sql = String.format("update %s set %s='%s', %s='%s',%s='%s' where %s='%s' and %s!=%s",
                mUserAccountDao.getTablename(),
                UserAccountDao.Properties.PassToken.columnName,
                account.getPassToken(),
                UserAccountDao.Properties.ServiceToken.columnName,
                account.getServiceToken(),
                UserAccountDao.Properties.SSecurity.columnName,
                account.getSSecurity(),
                UserAccountDao.Properties.Uuid.columnName,
                account.getUuid(),
                UserAccountDao.Properties.Channelid.columnName,
                account.getChannelid()
                );
        mUserAccountDao.getDatabase().execSQL(sql);
    }

    public UserAccount getAccount(int channelId) {
        List list = mUserAccountDao.queryBuilder().where(UserAccountDao.Properties.Channelid.eq(channelId)).list();
        if (list.isEmpty()) {
            return null;
        } else {
            return (UserAccount) list.get(0);
        }
    }
}
