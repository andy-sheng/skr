package com.common.core.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.common.core.account.UserAccount;
import com.common.dbhelper.MigrationHelper;
import com.common.log.MyLog;

import org.greenrobot.greendao.database.Database;

/**
 * Created by lan on 16/2/27.
 */
public class GreenOpenHelper extends DaoMaster.OpenHelper {
    public final String TAG = "Core.GreenOpenHelper";

    public GreenOpenHelper(Context context, String name) {
        super(context, name);
    }

    public GreenOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory) {
        super(context, name, factory);
    }

    @Override
    public void onUpgrade(Database db, int oldVersion, int newVersion) {

        if (oldVersion == 6) {
            UserInfoDBDao.dropTable(db, true);
        }
        MigrationHelper.migrate(db, new MigrationHelper.ReCreateAllTableListener() {
            @Override
            public void onCreateAllTables(Database db, boolean ifNotExists) {
                MyLog.d(TAG,"onCreateAllTables" + " db=" + db + " ifNotExists=" + ifNotExists);
                UserAccountDao.createTable(db, ifNotExists);
                UserInfoDBDao.createTable(db, ifNotExists);
                RemarkDBDao.createTable(db, ifNotExists);
            }

            @Override
            public void onDropAllTables(Database db, boolean ifExists) {
                MyLog.d(TAG,"onDropAllTables" + " db=" + db + " ifExists=" + ifExists);
                UserAccountDao.dropTable(db, ifExists);
                UserInfoDBDao.dropTable(db, ifExists);
                RemarkDBDao.dropTable(db, ifExists);
            }
        }, UserAccountDao.class,UserInfoDBDao.class,RemarkDBDao.class);
    }
}
