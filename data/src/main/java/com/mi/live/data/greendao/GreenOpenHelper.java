package com.mi.live.data.greendao;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.base.log.MyLog;
import com.wali.live.dao.ConversationDao;
import com.wali.live.dao.DaoMaster;
import com.wali.live.dao.GiftDao;
import com.wali.live.dao.GroupNotifyDao;
import com.wali.live.dao.SixinMessageDao;
import com.wali.live.dao.UserAccountDao;


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

        try {
            if (oldVersion < 61 && newVersion >= 61) {
                GiftDao.dropTable(db,true);
                GiftDao.createTable(db,false);
            }
        } catch (Exception e){

        }

        try {
            if (oldVersion < 57 && newVersion >= 57) {
                upgradeAccountFrom56To57(db);
            }
            if (oldVersion < 58 && newVersion >= 58) {
                ConversationDao.dropTable(db, true);
                ConversationDao.createTable(db, false);
            }
            if (oldVersion < 59 && newVersion >= 59) {
                SixinMessageDao.dropTable(db, true);
                SixinMessageDao.createTable(db, false);
            }
            if (oldVersion < 60 && newVersion >= 60) {
                GroupNotifyDao.dropTable(db, true);
                GroupNotifyDao.createTable(db, false);
            }
        } catch (Exception e) {
            MyLog.e(TAG, e);
        }
    }

    public static void upgradeAccountFrom56To57(SQLiteDatabase db) {
        try {
            String sql = "alter table USER_ACCOUNT add column miid LONG";
            db.execSQL(sql);
        } catch (Exception e) {
            MyLog.e(e);
            UserAccountDao.dropTable(db, true);
            UserAccountDao.createTable(db, true);
        }
    }
}