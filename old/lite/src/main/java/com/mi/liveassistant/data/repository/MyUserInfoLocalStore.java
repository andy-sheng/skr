package com.mi.liveassistant.data.repository;


import com.mi.liveassistant.common.global.GlobalData;
import com.mi.liveassistant.common.log.MyLog;
import com.mi.liveassistant.dao.OwnUserInfo;
import com.mi.liveassistant.dao.OwnUserInfoDao;
import com.mi.liveassistant.greendao.GreenDaoManager;

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

    public void deleteAccount(long uid) {
        String sql = String.format("delete from %s WHERE %s=%s",
                mOwnUserInfoDao.getTablename(),
                OwnUserInfoDao.Properties.Uid.columnName,
                uid);
        mOwnUserInfoDao.getDatabase().execSQL(sql);
    }

    public void replaceAccount(OwnUserInfo account) {
        deleteAccount(account.getUid());
        mOwnUserInfoDao.insertOrReplaceInTx(account);
    }

    public OwnUserInfo getAccount(long uid) {
        try {
            List list = mOwnUserInfoDao.queryBuilder().where(OwnUserInfoDao.Properties.Uid.eq(uid)).list();
            if (!list.isEmpty()) {
                return (OwnUserInfo) list.get(0);
            }
        } catch (Exception e) {
            MyLog.e(TAG, "getAccount failed e=" + e);
        }
        return null;
    }
}
