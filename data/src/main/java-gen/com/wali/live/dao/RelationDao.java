package com.wali.live.dao;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import de.greenrobot.dao.AbstractDao;
import de.greenrobot.dao.Property;
import de.greenrobot.dao.internal.DaoConfig;

import com.wali.live.dao.Relation;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.
/** 
 * DAO for table RELATION.
*/
public class RelationDao extends AbstractDao<Relation, Long> {

    public static final String TABLENAME = "RELATION";

    /**
     * Properties of entity Relation.<br/>
     * Can be used for QueryBuilder and for referencing column names.
    */
    public static class Properties {
        public final static Property UserId = new Property(0, Long.class, "userId", true, "USER_ID");
        public final static Property Avatar = new Property(1, Long.class, "avatar", false, "AVATAR");
        public final static Property UserNickname = new Property(2, String.class, "userNickname", false, "USER_NICKNAME");
        public final static Property Signature = new Property(3, String.class, "signature", false, "SIGNATURE");
        public final static Property Gender = new Property(4, Integer.class, "gender", false, "GENDER");
        public final static Property Level = new Property(5, Integer.class, "level", false, "LEVEL");
        public final static Property MTicketNum = new Property(6, Integer.class, "mTicketNum", false, "M_TICKET_NUM");
        public final static Property CertificationType = new Property(7, Integer.class, "certificationType", false, "CERTIFICATION_TYPE");
        public final static Property IsFollowing = new Property(8, Boolean.class, "isFollowing", false, "IS_FOLLOWING");
        public final static Property IsBothway = new Property(9, Boolean.class, "isBothway", false, "IS_BOTHWAY");
    };


    public RelationDao(DaoConfig config) {
        super(config);
    }
    
    public RelationDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
    }

    /** Creates the underlying database table. */
    public static void createTable(SQLiteDatabase db, boolean ifNotExists) {
        String constraint = ifNotExists? "IF NOT EXISTS ": "";
        db.execSQL("CREATE TABLE " + constraint + "'RELATION' (" + //
                "'USER_ID' INTEGER PRIMARY KEY ," + // 0: userId
                "'AVATAR' INTEGER," + // 1: avatar
                "'USER_NICKNAME' TEXT," + // 2: userNickname
                "'SIGNATURE' TEXT," + // 3: signature
                "'GENDER' INTEGER," + // 4: gender
                "'LEVEL' INTEGER," + // 5: level
                "'M_TICKET_NUM' INTEGER," + // 6: mTicketNum
                "'CERTIFICATION_TYPE' INTEGER," + // 7: certificationType
                "'IS_FOLLOWING' INTEGER," + // 8: isFollowing
                "'IS_BOTHWAY' INTEGER);"); // 9: isBothway
    }

    /** Drops the underlying database table. */
    public static void dropTable(SQLiteDatabase db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "'RELATION'";
        db.execSQL(sql);
    }

    /** @inheritdoc */
    @Override
    protected void bindValues(SQLiteStatement stmt, Relation entity) {
        stmt.clearBindings();
 
        Long userId = entity.getUserId();
        if (userId != null) {
            stmt.bindLong(1, userId);
        }
 
        Long avatar = entity.getAvatar();
        if (avatar != null) {
            stmt.bindLong(2, avatar);
        }
 
        String userNickname = entity.getUserNickname();
        if (userNickname != null) {
            stmt.bindString(3, userNickname);
        }
 
        String signature = entity.getSignature();
        if (signature != null) {
            stmt.bindString(4, signature);
        }
 
        Integer gender = entity.getGender();
        if (gender != null) {
            stmt.bindLong(5, gender);
        }
 
        Integer level = entity.getLevel();
        if (level != null) {
            stmt.bindLong(6, level);
        }
 
        Integer mTicketNum = entity.getMTicketNum();
        if (mTicketNum != null) {
            stmt.bindLong(7, mTicketNum);
        }
 
        Integer certificationType = entity.getCertificationType();
        if (certificationType != null) {
            stmt.bindLong(8, certificationType);
        }
 
        Boolean isFollowing = entity.getIsFollowing();
        if (isFollowing != null) {
            stmt.bindLong(9, isFollowing ? 1l: 0l);
        }
 
        Boolean isBothway = entity.getIsBothway();
        if (isBothway != null) {
            stmt.bindLong(10, isBothway ? 1l: 0l);
        }
    }

    /** @inheritdoc */
    @Override
    public Long readKey(Cursor cursor, int offset) {
        return cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0);
    }    

    /** @inheritdoc */
    @Override
    public Relation readEntity(Cursor cursor, int offset) {
        Relation entity = new Relation( //
            cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0), // userId
            cursor.isNull(offset + 1) ? null : cursor.getLong(offset + 1), // avatar
            cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2), // userNickname
            cursor.isNull(offset + 3) ? null : cursor.getString(offset + 3), // signature
            cursor.isNull(offset + 4) ? null : cursor.getInt(offset + 4), // gender
            cursor.isNull(offset + 5) ? null : cursor.getInt(offset + 5), // level
            cursor.isNull(offset + 6) ? null : cursor.getInt(offset + 6), // mTicketNum
            cursor.isNull(offset + 7) ? null : cursor.getInt(offset + 7), // certificationType
            cursor.isNull(offset + 8) ? null : cursor.getShort(offset + 8) != 0, // isFollowing
            cursor.isNull(offset + 9) ? null : cursor.getShort(offset + 9) != 0 // isBothway
        );
        return entity;
    }
     
    /** @inheritdoc */
    @Override
    public void readEntity(Cursor cursor, Relation entity, int offset) {
        entity.setUserId(cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0));
        entity.setAvatar(cursor.isNull(offset + 1) ? null : cursor.getLong(offset + 1));
        entity.setUserNickname(cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2));
        entity.setSignature(cursor.isNull(offset + 3) ? null : cursor.getString(offset + 3));
        entity.setGender(cursor.isNull(offset + 4) ? null : cursor.getInt(offset + 4));
        entity.setLevel(cursor.isNull(offset + 5) ? null : cursor.getInt(offset + 5));
        entity.setMTicketNum(cursor.isNull(offset + 6) ? null : cursor.getInt(offset + 6));
        entity.setCertificationType(cursor.isNull(offset + 7) ? null : cursor.getInt(offset + 7));
        entity.setIsFollowing(cursor.isNull(offset + 8) ? null : cursor.getShort(offset + 8) != 0);
        entity.setIsBothway(cursor.isNull(offset + 9) ? null : cursor.getShort(offset + 9) != 0);
     }
    
    /** @inheritdoc */
    @Override
    protected Long updateKeyAfterInsert(Relation entity, long rowId) {
        entity.setUserId(rowId);
        return rowId;
    }
    
    /** @inheritdoc */
    @Override
    public Long getKey(Relation entity) {
        if(entity != null) {
            return entity.getUserId();
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
