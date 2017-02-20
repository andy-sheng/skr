package com.wali.live.dao;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import de.greenrobot.dao.AbstractDao;
import de.greenrobot.dao.Property;
import de.greenrobot.dao.internal.DaoConfig;

import com.wali.live.dao.LoadingBanner;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.
/** 
 * DAO for table LOADING_BANNER.
*/
public class LoadingBannerDao extends AbstractDao<LoadingBanner, Long> {

    public static final String TABLENAME = "LOADING_BANNER";

    /**
     * Properties of entity LoadingBanner.<br/>
     * Can be used for QueryBuilder and for referencing column names.
    */
    public static class Properties {
        public final static Property BannerId = new Property(0, Long.class, "bannerId", true, "BANNER_ID");
        public final static Property PicUrl = new Property(1, String.class, "picUrl", false, "PIC_URL");
        public final static Property SkipUrl = new Property(2, String.class, "skipUrl", false, "SKIP_URL");
        public final static Property LastUpdateTs = new Property(3, Long.class, "lastUpdateTs", false, "LAST_UPDATE_TS");
        public final static Property StartTime = new Property(4, Integer.class, "startTime", false, "START_TIME");
        public final static Property EndTime = new Property(5, Integer.class, "endTime", false, "END_TIME");
        public final static Property ShareIconUrl = new Property(6, String.class, "shareIconUrl", false, "SHARE_ICON_URL");
        public final static Property ShareTitle = new Property(7, String.class, "shareTitle", false, "SHARE_TITLE");
        public final static Property ShareDesc = new Property(8, String.class, "shareDesc", false, "SHARE_DESC");
        public final static Property LastShowTime = new Property(9, Long.class, "lastShowTime", false, "LAST_SHOW_TIME");
        public final static Property LocalPath = new Property(10, String.class, "localPath", false, "LOCAL_PATH");
    };


    public LoadingBannerDao(DaoConfig config) {
        super(config);
    }
    
    public LoadingBannerDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
    }

    /** Creates the underlying database table. */
    public static void createTable(SQLiteDatabase db, boolean ifNotExists) {
        String constraint = ifNotExists? "IF NOT EXISTS ": "";
        db.execSQL("CREATE TABLE " + constraint + "'LOADING_BANNER' (" + //
                "'BANNER_ID' INTEGER PRIMARY KEY ," + // 0: bannerId
                "'PIC_URL' TEXT," + // 1: picUrl
                "'SKIP_URL' TEXT," + // 2: skipUrl
                "'LAST_UPDATE_TS' INTEGER," + // 3: lastUpdateTs
                "'START_TIME' INTEGER," + // 4: startTime
                "'END_TIME' INTEGER," + // 5: endTime
                "'SHARE_ICON_URL' TEXT," + // 6: shareIconUrl
                "'SHARE_TITLE' TEXT," + // 7: shareTitle
                "'SHARE_DESC' TEXT," + // 8: shareDesc
                "'LAST_SHOW_TIME' INTEGER," + // 9: lastShowTime
                "'LOCAL_PATH' TEXT);"); // 10: localPath
    }

    /** Drops the underlying database table. */
    public static void dropTable(SQLiteDatabase db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "'LOADING_BANNER'";
        db.execSQL(sql);
    }

    /** @inheritdoc */
    @Override
    protected void bindValues(SQLiteStatement stmt, LoadingBanner entity) {
        stmt.clearBindings();
 
        Long bannerId = entity.getBannerId();
        if (bannerId != null) {
            stmt.bindLong(1, bannerId);
        }
 
        String picUrl = entity.getPicUrl();
        if (picUrl != null) {
            stmt.bindString(2, picUrl);
        }
 
        String skipUrl = entity.getSkipUrl();
        if (skipUrl != null) {
            stmt.bindString(3, skipUrl);
        }
 
        Long lastUpdateTs = entity.getLastUpdateTs();
        if (lastUpdateTs != null) {
            stmt.bindLong(4, lastUpdateTs);
        }
 
        Integer startTime = entity.getStartTime();
        if (startTime != null) {
            stmt.bindLong(5, startTime);
        }
 
        Integer endTime = entity.getEndTime();
        if (endTime != null) {
            stmt.bindLong(6, endTime);
        }
 
        String shareIconUrl = entity.getShareIconUrl();
        if (shareIconUrl != null) {
            stmt.bindString(7, shareIconUrl);
        }
 
        String shareTitle = entity.getShareTitle();
        if (shareTitle != null) {
            stmt.bindString(8, shareTitle);
        }
 
        String shareDesc = entity.getShareDesc();
        if (shareDesc != null) {
            stmt.bindString(9, shareDesc);
        }
 
        Long lastShowTime = entity.getLastShowTime();
        if (lastShowTime != null) {
            stmt.bindLong(10, lastShowTime);
        }
 
        String localPath = entity.getLocalPath();
        if (localPath != null) {
            stmt.bindString(11, localPath);
        }
    }

    /** @inheritdoc */
    @Override
    public Long readKey(Cursor cursor, int offset) {
        return cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0);
    }    

    /** @inheritdoc */
    @Override
    public LoadingBanner readEntity(Cursor cursor, int offset) {
        LoadingBanner entity = new LoadingBanner( //
            cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0), // bannerId
            cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1), // picUrl
            cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2), // skipUrl
            cursor.isNull(offset + 3) ? null : cursor.getLong(offset + 3), // lastUpdateTs
            cursor.isNull(offset + 4) ? null : cursor.getInt(offset + 4), // startTime
            cursor.isNull(offset + 5) ? null : cursor.getInt(offset + 5), // endTime
            cursor.isNull(offset + 6) ? null : cursor.getString(offset + 6), // shareIconUrl
            cursor.isNull(offset + 7) ? null : cursor.getString(offset + 7), // shareTitle
            cursor.isNull(offset + 8) ? null : cursor.getString(offset + 8), // shareDesc
            cursor.isNull(offset + 9) ? null : cursor.getLong(offset + 9), // lastShowTime
            cursor.isNull(offset + 10) ? null : cursor.getString(offset + 10) // localPath
        );
        return entity;
    }
     
    /** @inheritdoc */
    @Override
    public void readEntity(Cursor cursor, LoadingBanner entity, int offset) {
        entity.setBannerId(cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0));
        entity.setPicUrl(cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1));
        entity.setSkipUrl(cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2));
        entity.setLastUpdateTs(cursor.isNull(offset + 3) ? null : cursor.getLong(offset + 3));
        entity.setStartTime(cursor.isNull(offset + 4) ? null : cursor.getInt(offset + 4));
        entity.setEndTime(cursor.isNull(offset + 5) ? null : cursor.getInt(offset + 5));
        entity.setShareIconUrl(cursor.isNull(offset + 6) ? null : cursor.getString(offset + 6));
        entity.setShareTitle(cursor.isNull(offset + 7) ? null : cursor.getString(offset + 7));
        entity.setShareDesc(cursor.isNull(offset + 8) ? null : cursor.getString(offset + 8));
        entity.setLastShowTime(cursor.isNull(offset + 9) ? null : cursor.getLong(offset + 9));
        entity.setLocalPath(cursor.isNull(offset + 10) ? null : cursor.getString(offset + 10));
     }
    
    /** @inheritdoc */
    @Override
    protected Long updateKeyAfterInsert(LoadingBanner entity, long rowId) {
        entity.setBannerId(rowId);
        return rowId;
    }
    
    /** @inheritdoc */
    @Override
    public Long getKey(LoadingBanner entity) {
        if(entity != null) {
            return entity.getBannerId();
        } else {
            return null;
        }
    }

    /** @inheritdoc */
    @Override    
    protected boolean isEntityUpdateable() {
        return true;
    }
    
}
