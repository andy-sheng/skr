package com.component.busilib.db;

import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;

import org.greenrobot.greendao.AbstractDao;
import org.greenrobot.greendao.Property;
import org.greenrobot.greendao.internal.DaoConfig;
import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.database.DatabaseStatement;

import com.component.person.photo.manager.ClubPhotoModelDB;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.
/** 
 * DAO for table "CLUB_PHOTO_MODEL_DB".
*/
public class ClubPhotoModelDBDao extends AbstractDao<ClubPhotoModelDB, String> {

    public static final String TABLENAME = "CLUB_PHOTO_MODEL_DB";

    /**
     * Properties of entity ClubPhotoModelDB.<br/>
     * Can be used for QueryBuilder and for referencing column names.
     */
    public static class Properties {
        public final static Property LocalPath = new Property(0, String.class, "localPath", true, "LOCAL_PATH");
        public final static Property Status = new Property(1, Integer.class, "status", false, "STATUS");
    }


    public ClubPhotoModelDBDao(DaoConfig config) {
        super(config);
    }
    
    public ClubPhotoModelDBDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
    }

    /** Creates the underlying database table. */
    public static void createTable(Database db, boolean ifNotExists) {
        String constraint = ifNotExists? "IF NOT EXISTS ": "";
        db.execSQL("CREATE TABLE " + constraint + "\"CLUB_PHOTO_MODEL_DB\" (" + //
                "\"LOCAL_PATH\" TEXT PRIMARY KEY NOT NULL ," + // 0: localPath
                "\"STATUS\" INTEGER);"); // 1: status
    }

    /** Drops the underlying database table. */
    public static void dropTable(Database db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "\"CLUB_PHOTO_MODEL_DB\"";
        db.execSQL(sql);
    }

    @Override
    protected final void bindValues(DatabaseStatement stmt, ClubPhotoModelDB entity) {
        stmt.clearBindings();
 
        String localPath = entity.getLocalPath();
        if (localPath != null) {
            stmt.bindString(1, localPath);
        }
 
        Integer status = entity.getStatus();
        if (status != null) {
            stmt.bindLong(2, status);
        }
    }

    @Override
    protected final void bindValues(SQLiteStatement stmt, ClubPhotoModelDB entity) {
        stmt.clearBindings();
 
        String localPath = entity.getLocalPath();
        if (localPath != null) {
            stmt.bindString(1, localPath);
        }
 
        Integer status = entity.getStatus();
        if (status != null) {
            stmt.bindLong(2, status);
        }
    }

    @Override
    public String readKey(Cursor cursor, int offset) {
        return cursor.isNull(offset + 0) ? null : cursor.getString(offset + 0);
    }    

    @Override
    public ClubPhotoModelDB readEntity(Cursor cursor, int offset) {
        ClubPhotoModelDB entity = new ClubPhotoModelDB( //
            cursor.isNull(offset + 0) ? null : cursor.getString(offset + 0), // localPath
            cursor.isNull(offset + 1) ? null : cursor.getInt(offset + 1) // status
        );
        return entity;
    }
     
    @Override
    public void readEntity(Cursor cursor, ClubPhotoModelDB entity, int offset) {
        entity.setLocalPath(cursor.isNull(offset + 0) ? null : cursor.getString(offset + 0));
        entity.setStatus(cursor.isNull(offset + 1) ? null : cursor.getInt(offset + 1));
     }
    
    @Override
    protected final String updateKeyAfterInsert(ClubPhotoModelDB entity, long rowId) {
        return entity.getLocalPath();
    }
    
    @Override
    public String getKey(ClubPhotoModelDB entity) {
        if(entity != null) {
            return entity.getLocalPath();
        } else {
            return null;
        }
    }

    @Override
    public boolean hasKey(ClubPhotoModelDB entity) {
        return entity.getLocalPath() != null;
    }

    @Override
    protected final boolean isEntityUpdateable() {
        return true;
    }
    
}