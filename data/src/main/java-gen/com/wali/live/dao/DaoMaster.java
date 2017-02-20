package com.wali.live.dao;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import de.greenrobot.dao.AbstractDaoMaster;
import de.greenrobot.dao.identityscope.IdentityScopeType;

import com.wali.live.dao.UserAccountDao;
import com.wali.live.dao.OwnUserInfoDao;
import com.wali.live.dao.GiftDao;
import com.wali.live.dao.RelationDao;
import com.wali.live.dao.RegionCnDao;
import com.wali.live.dao.RegionEnDao;
import com.wali.live.dao.RegionTwDao;
import com.wali.live.dao.LoadingBannerDao;
import com.wali.live.dao.WatchHistoryInfoDao;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.
/** 
 * Master of DAO (schema version 56): knows all DAOs.
*/
public class DaoMaster extends AbstractDaoMaster {
    public static final int SCHEMA_VERSION = 56;

    /** Creates underlying database table using DAOs. */
    public static void createAllTables(SQLiteDatabase db, boolean ifNotExists) {
        UserAccountDao.createTable(db, ifNotExists);
        OwnUserInfoDao.createTable(db, ifNotExists);
        GiftDao.createTable(db, ifNotExists);
        RelationDao.createTable(db, ifNotExists);
        RegionCnDao.createTable(db, ifNotExists);
        RegionEnDao.createTable(db, ifNotExists);
        RegionTwDao.createTable(db, ifNotExists);
        LoadingBannerDao.createTable(db, ifNotExists);
        WatchHistoryInfoDao.createTable(db, ifNotExists);
    }
    
    /** Drops underlying database table using DAOs. */
    public static void dropAllTables(SQLiteDatabase db, boolean ifExists) {
        UserAccountDao.dropTable(db, ifExists);
        OwnUserInfoDao.dropTable(db, ifExists);
        GiftDao.dropTable(db, ifExists);
        RelationDao.dropTable(db, ifExists);
        RegionCnDao.dropTable(db, ifExists);
        RegionEnDao.dropTable(db, ifExists);
        RegionTwDao.dropTable(db, ifExists);
        LoadingBannerDao.dropTable(db, ifExists);
        WatchHistoryInfoDao.dropTable(db, ifExists);
    }
    
    public static abstract class OpenHelper extends SQLiteOpenHelper {

        public OpenHelper(Context context, String name, CursorFactory factory) {
            super(context, name, factory, SCHEMA_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            Log.i("greenDAO", "Creating tables for schema version " + SCHEMA_VERSION);
            createAllTables(db, false);
        }
    }
    
    /** WARNING: Drops all table on Upgrade! Use only during development. */
    public static class DevOpenHelper extends OpenHelper {
        public DevOpenHelper(Context context, String name, CursorFactory factory) {
            super(context, name, factory);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.i("greenDAO", "Upgrading schema from version " + oldVersion + " to " + newVersion + " by dropping all tables");
            dropAllTables(db, true);
            onCreate(db);
        }
    }

    public DaoMaster(SQLiteDatabase db) {
        super(db, SCHEMA_VERSION);
        registerDaoClass(UserAccountDao.class);
        registerDaoClass(OwnUserInfoDao.class);
        registerDaoClass(GiftDao.class);
        registerDaoClass(RelationDao.class);
        registerDaoClass(RegionCnDao.class);
        registerDaoClass(RegionEnDao.class);
        registerDaoClass(RegionTwDao.class);
        registerDaoClass(LoadingBannerDao.class);
        registerDaoClass(WatchHistoryInfoDao.class);
    }
    
    public DaoSession newSession() {
        return new DaoSession(db, IdentityScopeType.Session, daoConfigMap);
    }
    
    public DaoSession newSession(IdentityScopeType type) {
        return new DaoSession(db, type, daoConfigMap);
    }
    
}
