package com.module.feeds.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.common.dbhelper.MigrationHelper;
import com.common.log.MyLog;

import org.greenrobot.greendao.database.Database;

/**
 * Created by lan on 16/2/27.
 */
public class GreenOpenHelper
        extends DaoMaster.OpenHelper {
    public final String TAG = "Feeds.GreenOpenHelper";

    public GreenOpenHelper(Context context, String name) {
        super(context, name);
    }

    public GreenOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory) {
        super(context, name, factory);
    }

    @Override
    public void onUpgrade(Database db, int oldVersion, int newVersion) {
        MigrationHelper.migrate(db, new MigrationHelper.ReCreateAllTableListener() {
            @Override
            public void onCreateAllTables(Database db, boolean ifNotExists) {
                MyLog.d(TAG, "onCreateAllTables" + " db=" + db + " ifNotExists=" + ifNotExists);
                FeedsDraftDBDao.createTable(db, ifNotExists);
                FeedCollectDBDao.createTable(db, ifNotExists);
            }

            @Override
            public void onDropAllTables(Database db, boolean ifExists) {
                MyLog.d(TAG, "onDropAllTables" + " db=" + db + " ifExists=" + ifExists);
                FeedsDraftDBDao.dropTable(db, ifExists);
                FeedCollectDBDao.dropTable(db, ifExists);
            }
        }, FeedsDraftDBDao.class, FeedCollectDBDao.class);
    }
}
