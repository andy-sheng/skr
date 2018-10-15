package com.common.core.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.common.core.account.UserAccount;
import com.common.dbhelper.MigrationHelper;

import org.greenrobot.greendao.database.Database;

/**
 * Created by lan on 16/2/27.
 */
public class GreenOpenHelper extends DaoMaster.OpenHelper {
    private static final String TAG = GreenOpenHelper.class.getSimpleName();

    public GreenOpenHelper(Context context, String name) {
        super(context, name);
    }

    public GreenOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory) {
        super(context, name, factory);
    }

    @Override
    public void onUpgrade(Database db, int oldVersion, int newVersion) {
//        if (oldVersion < 2 && newVersion >= 2) {
//            try {
//                String sql = "alter table '" + UserAccountDao.TABLENAME + "' add COLUMN 'BUSI_KEY' STRING DEFAULT ''";
//                db.execSQL(sql);
//            } catch (Exception e) {
//                UserAccountDao.dropTable(db, true);
//                UserAccountDao.createTable(db, false);
//            }
//        }

        MigrationHelper.migrate(db,UserAccountDao.class);
    }
}
