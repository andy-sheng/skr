package com.wali.live.dao;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import de.greenrobot.dao.AbstractDao;
import de.greenrobot.dao.Property;
import de.greenrobot.dao.internal.DaoConfig;

import com.wali.live.dao.Conversation;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.
/** 
 * DAO for table CONVERSATION.
*/
public class ConversationDao extends AbstractDao<Conversation, Long> {

    public static final String TABLENAME = "CONVERSATION";

    /**
     * Properties of entity Conversation.<br/>
     * Can be used for QueryBuilder and for referencing column names.
    */
    public static class Properties {
        public final static Property Id = new Property(0, Long.class, "id", true, "_id");
        public final static Property Target = new Property(1, long.class, "target", false, "TARGET");
        public final static Property UnreadCount = new Property(2, Integer.class, "unreadCount", false, "UNREAD_COUNT");
        public final static Property SendTime = new Property(3, Long.class, "sendTime", false, "SEND_TIME");
        public final static Property ReceivedTime = new Property(4, Long.class, "receivedTime", false, "RECEIVED_TIME");
        public final static Property Content = new Property(5, String.class, "content", false, "CONTENT");
        public final static Property LastMsgSeq = new Property(6, Long.class, "lastMsgSeq", false, "LAST_MSG_SEQ");
        public final static Property TargetName = new Property(7, String.class, "targetName", false, "TARGET_NAME");
        public final static Property MsgId = new Property(8, Long.class, "msgId", false, "MSG_ID");
        public final static Property MsgType = new Property(9, Integer.class, "msgType", false, "MSG_TYPE");
        public final static Property IgnoreStatus = new Property(10, Integer.class, "ignoreStatus", false, "IGNORE_STATUS");
        public final static Property LocaLUserId = new Property(11, long.class, "locaLUserId", false, "LOCA_LUSER_ID");
        public final static Property IsNotFocus = new Property(12, boolean.class, "isNotFocus", false, "IS_NOT_FOCUS");
        public final static Property Ext = new Property(13, String.class, "ext", false, "EXT");
        public final static Property CertificationType = new Property(14, Integer.class, "certificationType", false, "CERTIFICATION_TYPE");
        public final static Property TargetType = new Property(15, int.class, "targetType", false, "TARGET_TYPE");
        public final static Property Icon = new Property(16, String.class, "icon", false, "ICON");
        public final static Property InputMode = new Property(17, Integer.class, "inputMode", false, "INPUT_MODE");
    };


    public ConversationDao(DaoConfig config) {
        super(config);
    }
    
    public ConversationDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
    }

    /** Creates the underlying database table. */
    public static void createTable(SQLiteDatabase db, boolean ifNotExists) {
        String constraint = ifNotExists? "IF NOT EXISTS ": "";
        db.execSQL("CREATE TABLE " + constraint + "'CONVERSATION' (" + //
                "'_id' INTEGER PRIMARY KEY AUTOINCREMENT ," + // 0: id
                "'TARGET' INTEGER NOT NULL ," + // 1: target
                "'UNREAD_COUNT' INTEGER," + // 2: unreadCount
                "'SEND_TIME' INTEGER," + // 3: sendTime
                "'RECEIVED_TIME' INTEGER," + // 4: receivedTime
                "'CONTENT' TEXT," + // 5: content
                "'LAST_MSG_SEQ' INTEGER," + // 6: lastMsgSeq
                "'TARGET_NAME' TEXT," + // 7: targetName
                "'MSG_ID' INTEGER," + // 8: msgId
                "'MSG_TYPE' INTEGER," + // 9: msgType
                "'IGNORE_STATUS' INTEGER," + // 10: ignoreStatus
                "'LOCA_LUSER_ID' INTEGER NOT NULL ," + // 11: locaLUserId
                "'IS_NOT_FOCUS' INTEGER NOT NULL ," + // 12: isNotFocus
                "'EXT' TEXT," + // 13: ext
                "'CERTIFICATION_TYPE' INTEGER," + // 14: certificationType
                "'TARGET_TYPE' INTEGER NOT NULL ," + // 15: targetType
                "'ICON' TEXT," + // 16: icon
                "'INPUT_MODE' INTEGER);"); // 17: inputMode
        // Add Indexes
        db.execSQL("CREATE INDEX " + constraint + "IDX_CONVERSATION_TARGET ON CONVERSATION" +
                " (TARGET);");
        db.execSQL("CREATE INDEX " + constraint + "IDX_CONVERSATION_LOCA_LUSER_ID ON CONVERSATION" +
                " (LOCA_LUSER_ID);");
        db.execSQL("CREATE INDEX " + constraint + "IDX_CONVERSATION_IS_NOT_FOCUS ON CONVERSATION" +
                " (IS_NOT_FOCUS);");
        db.execSQL("CREATE INDEX " + constraint + "IDX_CONVERSATION_TARGET_TYPE ON CONVERSATION" +
                " (TARGET_TYPE);");
    }

    /** Drops the underlying database table. */
    public static void dropTable(SQLiteDatabase db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "'CONVERSATION'";
        db.execSQL(sql);
    }

    /** @inheritdoc */
    @Override
    protected void bindValues(SQLiteStatement stmt, Conversation entity) {
        stmt.clearBindings();
 
        Long id = entity.getId();
        if (id != null) {
            stmt.bindLong(1, id);
        }
        stmt.bindLong(2, entity.getTarget());
 
        Integer unreadCount = entity.getUnreadCount();
        if (unreadCount != null) {
            stmt.bindLong(3, unreadCount);
        }
 
        Long sendTime = entity.getSendTime();
        if (sendTime != null) {
            stmt.bindLong(4, sendTime);
        }
 
        Long receivedTime = entity.getReceivedTime();
        if (receivedTime != null) {
            stmt.bindLong(5, receivedTime);
        }
 
        String content = entity.getContent();
        if (content != null) {
            stmt.bindString(6, content);
        }
 
        Long lastMsgSeq = entity.getLastMsgSeq();
        if (lastMsgSeq != null) {
            stmt.bindLong(7, lastMsgSeq);
        }
 
        String targetName = entity.getTargetName();
        if (targetName != null) {
            stmt.bindString(8, targetName);
        }
 
        Long msgId = entity.getMsgId();
        if (msgId != null) {
            stmt.bindLong(9, msgId);
        }
 
        Integer msgType = entity.getMsgType();
        if (msgType != null) {
            stmt.bindLong(10, msgType);
        }
 
        Integer ignoreStatus = entity.getIgnoreStatus();
        if (ignoreStatus != null) {
            stmt.bindLong(11, ignoreStatus);
        }
        stmt.bindLong(12, entity.getLocaLUserId());
        stmt.bindLong(13, entity.getIsNotFocus() ? 1l: 0l);
 
        String ext = entity.getExt();
        if (ext != null) {
            stmt.bindString(14, ext);
        }
 
        Integer certificationType = entity.getCertificationType();
        if (certificationType != null) {
            stmt.bindLong(15, certificationType);
        }
        stmt.bindLong(16, entity.getTargetType());
 
        String icon = entity.getIcon();
        if (icon != null) {
            stmt.bindString(17, icon);
        }
 
        Integer inputMode = entity.getInputMode();
        if (inputMode != null) {
            stmt.bindLong(18, inputMode);
        }
    }

    /** @inheritdoc */
    @Override
    public Long readKey(Cursor cursor, int offset) {
        return cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0);
    }    

    /** @inheritdoc */
    @Override
    public Conversation readEntity(Cursor cursor, int offset) {
        Conversation entity = new Conversation( //
            cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0), // id
            cursor.getLong(offset + 1), // target
            cursor.isNull(offset + 2) ? null : cursor.getInt(offset + 2), // unreadCount
            cursor.isNull(offset + 3) ? null : cursor.getLong(offset + 3), // sendTime
            cursor.isNull(offset + 4) ? null : cursor.getLong(offset + 4), // receivedTime
            cursor.isNull(offset + 5) ? null : cursor.getString(offset + 5), // content
            cursor.isNull(offset + 6) ? null : cursor.getLong(offset + 6), // lastMsgSeq
            cursor.isNull(offset + 7) ? null : cursor.getString(offset + 7), // targetName
            cursor.isNull(offset + 8) ? null : cursor.getLong(offset + 8), // msgId
            cursor.isNull(offset + 9) ? null : cursor.getInt(offset + 9), // msgType
            cursor.isNull(offset + 10) ? null : cursor.getInt(offset + 10), // ignoreStatus
            cursor.getLong(offset + 11), // locaLUserId
            cursor.getShort(offset + 12) != 0, // isNotFocus
            cursor.isNull(offset + 13) ? null : cursor.getString(offset + 13), // ext
            cursor.isNull(offset + 14) ? null : cursor.getInt(offset + 14), // certificationType
            cursor.getInt(offset + 15), // targetType
            cursor.isNull(offset + 16) ? null : cursor.getString(offset + 16), // icon
            cursor.isNull(offset + 17) ? null : cursor.getInt(offset + 17) // inputMode
        );
        return entity;
    }
     
    /** @inheritdoc */
    @Override
    public void readEntity(Cursor cursor, Conversation entity, int offset) {
        entity.setId(cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0));
        entity.setTarget(cursor.getLong(offset + 1));
        entity.setUnreadCount(cursor.isNull(offset + 2) ? null : cursor.getInt(offset + 2));
        entity.setSendTime(cursor.isNull(offset + 3) ? null : cursor.getLong(offset + 3));
        entity.setReceivedTime(cursor.isNull(offset + 4) ? null : cursor.getLong(offset + 4));
        entity.setContent(cursor.isNull(offset + 5) ? null : cursor.getString(offset + 5));
        entity.setLastMsgSeq(cursor.isNull(offset + 6) ? null : cursor.getLong(offset + 6));
        entity.setTargetName(cursor.isNull(offset + 7) ? null : cursor.getString(offset + 7));
        entity.setMsgId(cursor.isNull(offset + 8) ? null : cursor.getLong(offset + 8));
        entity.setMsgType(cursor.isNull(offset + 9) ? null : cursor.getInt(offset + 9));
        entity.setIgnoreStatus(cursor.isNull(offset + 10) ? null : cursor.getInt(offset + 10));
        entity.setLocaLUserId(cursor.getLong(offset + 11));
        entity.setIsNotFocus(cursor.getShort(offset + 12) != 0);
        entity.setExt(cursor.isNull(offset + 13) ? null : cursor.getString(offset + 13));
        entity.setCertificationType(cursor.isNull(offset + 14) ? null : cursor.getInt(offset + 14));
        entity.setTargetType(cursor.getInt(offset + 15));
        entity.setIcon(cursor.isNull(offset + 16) ? null : cursor.getString(offset + 16));
        entity.setInputMode(cursor.isNull(offset + 17) ? null : cursor.getInt(offset + 17));
     }
    
    /** @inheritdoc */
    @Override
    protected Long updateKeyAfterInsert(Conversation entity, long rowId) {
        entity.setId(rowId);
        return rowId;
    }
    
    /** @inheritdoc */
    @Override
    public Long getKey(Conversation entity) {
        if(entity != null) {
            return entity.getId();
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
