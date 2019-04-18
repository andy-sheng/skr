package com.component.busilib.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

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

        MigrationHelper.migrate(db, new MigrationHelper.ReCreateAllTableListener() {
            @Override
            public void onCreateAllTables(Database db, boolean ifNotExists) {
                PhotoModelDBDao.createTable(db, ifNotExists);
            }

            @Override
            public void onDropAllTables(Database db, boolean ifExists) {
                PhotoModelDBDao.dropTable(db, ifExists);
            }
        }, PhotoModelDBDao.class);
    }
}
