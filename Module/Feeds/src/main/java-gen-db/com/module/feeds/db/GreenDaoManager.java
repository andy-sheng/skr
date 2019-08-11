package com.module.feeds.db;

import com.common.utils.U;

/**
 * Created by linjinbin on 15/11/9.
 */
public class GreenDaoManager {
    private static final String DB_NAME = "feeds.db";

    private static DaoMaster daoMaster;
    private static DaoSession daoSession;

    /**
     * 获取DaoMaster实例
     */
    private static synchronized DaoMaster getDaoMaster() {
        if (daoMaster == null) {
            GreenOpenHelper helper = new GreenOpenHelper(U.app(), DB_NAME, null);
            daoMaster = new DaoMaster(helper.getWritableDatabase());
        }
        return daoMaster;
    }

    /**
     * 获取DaoSession实例
     */
    public static synchronized DaoSession getDaoSession() {
        if (daoSession == null) {
            if (daoMaster == null) {
                daoMaster = getDaoMaster();
            }
            daoSession = daoMaster.newSession();
        }
        return daoSession;
    }
}
