package com.mi.liveassistant.greendao;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.mi.liveassistant.common.log.MyLog;
import com.mi.liveassistant.dao.DaoMaster;


/**
 * Created by lan on 16/2/27.
 */
public class GreenOpenHelper extends DaoMaster.OpenHelper {
    private static final String TAG = GreenOpenHelper.class.getSimpleName();

    public GreenOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory) {
        super(context, name, factory);
    }

    /**
     * 注意尽量不要使用drop和create
     *
     * @param db
     * @param oldVersion
     * @param newVersion
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        MyLog.w(TAG, "Upgrading schema from version " + oldVersion + " to " + newVersion);

    }
}
