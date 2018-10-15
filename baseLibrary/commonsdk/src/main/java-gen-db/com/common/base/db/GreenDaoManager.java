package com.common.base.db;

import android.content.Context;

import com.common.utils.U;

/**
 * Created by linjinbin on 15/11/9.
 */
public class GreenDaoManager {
    private static final String DB_NAME = "base.db";

    private static DaoMaster daoMaster;
    private static DaoSession daoSession;

    /**
     * 获取DaoMaster实例
     */
    public static synchronized DaoMaster getDaoMaster(Context context) {
        if (daoMaster == null) {
            GreenOpenHelper helper = new GreenOpenHelper(context, DB_NAME, null);
            daoMaster = new DaoMaster(helper.getWritableDatabase());
        }
        return daoMaster;
    }

    /**
     * 获取DaoSession实例
     */
    public static synchronized DaoSession getDaoSession(Context context) {
        if (daoSession == null) {
            if (daoMaster == null) {
                daoMaster = getDaoMaster(context);
            }
            daoSession = daoMaster.newSession();
        }
        return daoSession;
    }
}
