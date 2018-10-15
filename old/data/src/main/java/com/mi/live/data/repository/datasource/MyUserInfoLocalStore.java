package com.mi.live.data.repository.datasource;

import com.base.global.GlobalData;
import com.base.log.MyLog;
import com.mi.live.data.greendao.GreenDaoManager;
import com.wali.live.dao.OwnUserInfo;
import com.wali.live.dao.OwnUserInfoDao;
import com.wali.live.dao.UserAccount;
import com.wali.live.dao.UserAccountDao;

import java.util.List;

/**
 * Created by zjn on 16-10-30.
 */
public class MyUserInfoLocalStore {

    public final static String TAG = MyUserInfoLocalStore.class.getSimpleName();

    private static MyUserInfoLocalStore sInstance;

    private OwnUserInfoDao mOwnUserInfoDao;

    private MyUserInfoLocalStore() {
        mOwnUserInfoDao = GreenDaoManager.getDaoSession(GlobalData.app()).getOwnUserInfoDao();
    }

    public static MyUserInfoLocalStore getInstance() {
        if (sInstance == null) {
            synchronized (MyUserInfoLocalStore.class) {
                if (sInstance == null) {
                    sInstance = new MyUserInfoLocalStore();
                }
            }
        }
        return sInstance;
    }

    public void deleteAll() {
        mOwnUserInfoDao.deleteAll();
    }

    public void deleteAccount(int channelId) {
        String sql = String.format("delete from %s WHERE %s=%s",
                mOwnUserInfoDao.getTablename(),
                OwnUserInfoDao.Properties.Channelid.columnName,
                channelId);
        mOwnUserInfoDao.getDatabase().execSQL(sql);
    }

    public void replaceAccount(OwnUserInfo account, int channelId) {
        deleteAccount(channelId);
        mOwnUserInfoDao.insertOrReplaceInTx(account);
    }

    public OwnUserInfo getAccount(int channelId) {
        try {
            List list = mOwnUserInfoDao.queryBuilder().where(OwnUserInfoDao.Properties.Channelid.eq(channelId)).list();
            if (!list.isEmpty()) {
                return (OwnUserInfo) list.get(0);
            }
        } catch (Exception e) {
            MyLog.e(TAG, "getAccount failed e=" + e);
        }
        return null;
    }
}
