package com.mi.live.data.greendao;

import android.content.Context;

import com.wali.live.dao.DaoMaster;
import com.wali.live.dao.DaoSession;

/**
 * Created by linjinbin on 15/11/9.
 */
public class GreenDaoManager {
    private static final String DB_NAME = "live";

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
