package com.wali.live.watchsdk.sixin.data;

import android.text.TextUtils;

import com.base.global.GlobalData;
import com.base.log.MyLog;
import com.mi.live.data.account.MyUserInfoManager;
import com.mi.live.data.account.UserAccountManager;
import com.mi.live.data.assist.Attachment;
import com.mi.live.data.greendao.GreenDaoManager;
import com.wali.live.dao.SixinMessage;
import com.wali.live.dao.SixinMessageDao;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.dao.query.QueryBuilder;
import de.greenrobot.dao.query.WhereCondition;

import static com.mi.live.data.greendao.GreenDaoManager.getDaoSession;

/**
 * Created by anping on 16-10-10.
 */
public class SixinMessageLocalStore {
    private static String STAG = "SixinMessageLocalStore";

    //通过用户id 来获取所有的私信消息
    public static List<SixinMessage> getAllSixinMessageByUUid(long uuid, int targetType) {
        long userId = UserAccountManager.getInstance().getUuidAsLong();
        SixinMessageDao sixinMessageDao = getDaoSession(GlobalData.app()).getSixinMessageDao();
        QueryBuilder queryBuilder = sixinMessageDao.queryBuilder();
        queryBuilder.where(SixinMessageDao.Properties.LocaLUserId.eq(userId), SixinMessageDao.Properties.Target.eq(uuid), SixinMessageDao.Properties.TargetType.eq(targetType)
                , SixinMessageDao.Properties.ServerStoreStatus.notEq(SixinMessage.SERVER_STORE_STATUS_DELETE)
                , SixinMessageDao.Properties.MsgTyppe.notEq(SixinMessage.S_MSG_TYPE_DRAFT)
        ).orderAsc(SixinMessageDao.Properties.ReceivedTime).orderAsc(SixinMessageDao.Properties.Id).build();
        List<SixinMessage> sixinMessages = queryBuilder.list();
        if (sixinMessages == null || sixinMessages.size() <= 0) {
            return null;
        }
        return sixinMessages;
    }

    //timeOrId永远不要是Long.maxvalue 当sendTime!=Long.maxValue时，则timeOrId应该设置为sendTime
    //当发现sendTime为Long.maxValue时,则则timeOrId应该设置为 messageId.
    public static List<SixinMessage> getSixinMessagesByUUid(long uuid, int limit, long timeOrId, boolean compareByTime, int targetType) {
        long userId = UserAccountManager.getInstance().getUuidAsLong();
        SixinMessageDao sixinMessageDao = getDaoSession(GlobalData.app()).getSixinMessageDao();
        QueryBuilder queryBuilder = sixinMessageDao.queryBuilder();
        WhereCondition whereCondition = null;
        if (compareByTime) {
            whereCondition = queryBuilder.and(SixinMessageDao.Properties.LocaLUserId.eq(userId), SixinMessageDao.Properties.Target.eq(uuid), SixinMessageDao.Properties.TargetType.eq(targetType), SixinMessageDao.Properties.ReceivedTime.le(timeOrId));
        } else {
            whereCondition = queryBuilder.and(SixinMessageDao.Properties.LocaLUserId.eq(userId), SixinMessageDao.Properties.Target.eq(uuid), SixinMessageDao.Properties.TargetType.eq(targetType), SixinMessageDao.Properties.Id.le(timeOrId));
        }
        queryBuilder.where(whereCondition, SixinMessageDao.Properties.ServerStoreStatus.notEq(SixinMessage.SERVER_STORE_STATUS_DELETE), SixinMessageDao.Properties.MsgTyppe.notEq(SixinMessage.S_MSG_TYPE_DRAFT)).limit(limit).orderDesc(SixinMessageDao.Properties.ReceivedTime).orderDesc(SixinMessageDao.Properties.Id).build();
        List<SixinMessage> sixinMessages = queryBuilder.list();
        if (sixinMessages == null || sixinMessages.size() <= 0) {
            return null;
        }
        return sixinMessages;
    }


//    /**
//     * 现查询出seq不是0 的，0表示通知，查询完之后在拿出通知的最后再做merge
//     *
//     * @param uuid
//     * @param limit
//     * @param seq
//     * @param targetType
//     * @param excludeIds
//     * @return
//     */
//    public static List<SixinMessage> getSixinMessagesByUUidSortBySeq(long uuid, int limit, long seq, int targetType, List<Long> excludeIds) {
//        long userId = UserAccountManager.getInstance().getUuidAsLong();
//        SixinMessageDao sixinMessageDao = GreenDaoManager.getDaoSession(GlobalData.app()).getSixinMessageDao();
//        QueryBuilder queryBuilder = sixinMessageDao.queryBuilder();
//        WhereCondition whereCondition = null;
//        if (excludeIds != null && excludeIds.size() > 0) {
//            whereCondition = queryBuilder.and(SixinMessageDao.Properties.Id.notIn(excludeIds), SixinMessageDao.Properties.LocaLUserId.eq(userId), SixinMessageDao.Properties.Target.eq(uuid), SixinMessageDao.Properties.TargetType.eq(targetType), SixinMessageDao.Properties.MsgSeq.le(seq));
//        } else {
//            whereCondition = queryBuilder.and(SixinMessageDao.Properties.LocaLUserId.eq(userId), SixinMessageDao.Properties.Target.eq(uuid), SixinMessageDao.Properties.TargetType.eq(targetType), SixinMessageDao.Properties.MsgSeq.le(seq));
//        }
//        queryBuilder.where(whereCondition, SixinMessageDao.Properties.ServerStoreStatus.notEq(SixinMessage.SERVER_STORE_STATUS_DELETE), SixinMessageDao.Properties.MsgTyppe.notEq(SixinMessage.S_MSG_TYPE_DRAFT), SixinMessageDao.Properties.MsgSeq.notEq(0)).
//                limit(limit).orderDesc(SixinMessageDao.Properties.MsgSeq).orderDesc(SixinMessageDao.Properties.Id).build();
//        List<SixinMessage> sixinMessages = queryBuilder.list();
//
//        List<SixinMessage> notifyMessages = null;
//        if (sixinMessages == null || sixinMessages.size() <= 0) {
//            if (seq == Long.MAX_VALUE) {
//                notifyMessages = getNotifyMessageByTime(uuid, limit, Long.MAX_VALUE, 0, targetType, excludeIds);
//            }
//        } else {
//            long minTime = sixinMessages.get(sixinMessages.size() - 1).getReceivedTime();
//            if (seq == Long.MAX_VALUE && sixinMessages.get(sixinMessages.size() - 1).getMsgSeq() <= 1) {
//                minTime = 0;
//            }
//            notifyMessages = getNotifyMessageByTime(uuid, limit, seq == Long.MAX_VALUE ? seq : sixinMessages.get(0).getReceivedTime(), minTime, targetType, excludeIds);
//        }
//
//        sixinMessages = mergeGroupMessage(sixinMessages, notifyMessages);
//        if (sixinMessages == null || sixinMessages.size() <= 0) {
//            return null;
//        }
//        if (sixinMessages.size() > limit) {
//            sixinMessages = sixinMessages.subList(0, limit);
//        }
//        return sixinMessages;
//    }

    public static List<SixinMessage> getNotifyMessageByTime(long uuid, int limit, long maxTime, long minTime, int targetType, List<Long> excludeIds) {
        long userId = UserAccountManager.getInstance().getUuidAsLong();
        SixinMessageDao sixinMessageDao = getDaoSession(GlobalData.app()).getSixinMessageDao();
        QueryBuilder queryBuilder = sixinMessageDao.queryBuilder();
        WhereCondition whereCondition = null;
        if (excludeIds != null && excludeIds.size() > 0) {
            whereCondition = queryBuilder.and(SixinMessageDao.Properties.Id.notIn(excludeIds), SixinMessageDao.Properties.LocaLUserId.eq(userId), SixinMessageDao.Properties.Target.eq(uuid), SixinMessageDao.Properties.TargetType.eq(targetType), SixinMessageDao.Properties.ReceivedTime.ge(minTime), SixinMessageDao.Properties.ReceivedTime.le(maxTime));
        } else {
            whereCondition = queryBuilder.and(SixinMessageDao.Properties.LocaLUserId.eq(userId), SixinMessageDao.Properties.Target.eq(uuid), SixinMessageDao.Properties.TargetType.eq(targetType), SixinMessageDao.Properties.ReceivedTime.ge(minTime), SixinMessageDao.Properties.ReceivedTime.le(maxTime));
        }
        queryBuilder.where(whereCondition, SixinMessageDao.Properties.ServerStoreStatus.notEq(SixinMessage.SERVER_STORE_STATUS_DELETE), SixinMessageDao.Properties.MsgTyppe.notEq(SixinMessage.S_MSG_TYPE_DRAFT), SixinMessageDao.Properties.MsgSeq.eq(0)).
                limit(limit).orderDesc(SixinMessageDao.Properties.ReceivedTime).orderDesc(SixinMessageDao.Properties.Id).build();
        List<SixinMessage> sixinMessages = queryBuilder.list();
        if (sixinMessages == null || sixinMessages.size() <= 0) {
            return null;
        }
        return sixinMessages;
    }

    public static SixinMessage getSixinMessageBySenderMsgid(long cid) {
        SixinMessageDao sixinMessageDao = getDaoSession(GlobalData.app()).getSixinMessageDao();
        QueryBuilder queryBuilder = sixinMessageDao.queryBuilder();
        queryBuilder.where(SixinMessageDao.Properties.SenderMsgId.eq(cid)).build();
        List<SixinMessage> sixinMessages = queryBuilder.list();
        if (sixinMessages == null || sixinMessages.size() <= 0) {
            return null;
        }
        return sixinMessages.get(0);
    }

    public static SixinMessage getSixinMessageByMsgId(long cid) {
        SixinMessageDao sixinMessageDao = getDaoSession(GlobalData.app()).getSixinMessageDao();
        QueryBuilder queryBuilder = sixinMessageDao.queryBuilder();
        queryBuilder.where(SixinMessageDao.Properties.Id.eq(cid)).build();
        List<SixinMessage> sixinMessages = queryBuilder.list();
        if (sixinMessages == null || sixinMessages.size() <= 0) {
            return null;
        }
        return sixinMessages.get(0);
    }

    public static long insertSixinMessage(SixinMessage sixinMessage) {
        long msgId = 0;
        if (sixinMessage != null) {
            List<SixinMessage> sixinMessages = new ArrayList<>();
            sixinMessages.add(sixinMessage);
            insertSixinMessages(sixinMessages);
            msgId = sixinMessage.getId();
        }
        return msgId;
    }

    private static SixinMessage getSixinMessageInDb(SixinMessage sixinMessage) {
        long userId = UserAccountManager.getInstance().getUuidAsLong();
        if (sixinMessage != null && sixinMessage.getMsgSeq() != null && sixinMessage.getSenderMsgId() != null) {
            SixinMessageDao sixinMessageDao = getDaoSession(GlobalData.app()).getSixinMessageDao();
            QueryBuilder queryBuilder = sixinMessageDao.queryBuilder();

            //非通知消息的seq消息相等
            WhereCondition andCondition = queryBuilder.and(SixinMessageDao.Properties.MsgSeq.eq(sixinMessage.getMsgSeq()), SixinMessageDao.Properties.MsgSeq.notEq(Long.MAX_VALUE), SixinMessageDao.Properties.MsgSeq.notEq(0));
            //msgSeq 通知消息相等条件
            WhereCondition msgSeqIsNotification = queryBuilder.or(SixinMessageDao.Properties.MsgSeq.eq(0), SixinMessageDao.Properties.MsgSeq.eq(Long.MAX_VALUE));

            WhereCondition timeEquesCondition = queryBuilder.or(SixinMessageDao.Properties.SentTime.eq(sixinMessage.getSentTime()), SixinMessageDao.Properties.ReceivedTime.eq(sixinMessage.getReceivedTime()));

            WhereCondition notificationCondition = queryBuilder.and(SixinMessageDao.Properties.MsgSeq.eq(sixinMessage.getMsgSeq()), msgSeqIsNotification, SixinMessageDao.Properties.Body.eq(sixinMessage.getBody()), timeEquesCondition);


            WhereCondition orCondition = null;
            if (sixinMessage.getIsInbound()) {
                orCondition = queryBuilder.or(SixinMessageDao.Properties.SenderMsgId.eq(sixinMessage.getSenderMsgId()), andCondition, notificationCondition);
            } else {
                orCondition = queryBuilder.or(SixinMessageDao.Properties.SenderMsgId.eq(sixinMessage.getSenderMsgId()), andCondition);
            }
            WhereCondition whereCondition = queryBuilder.and(SixinMessageDao.Properties.LocaLUserId.eq(userId), SixinMessageDao.Properties.Target.eq(sixinMessage.getTarget()), SixinMessageDao.Properties.Sender.eq(sixinMessage.getSender())
                    , SixinMessageDao.Properties.TargetType.eq(sixinMessage.getTargetType()), orCondition);
            queryBuilder.where(whereCondition).build();
            List<SixinMessage> sixinMessages = queryBuilder.list();
            if (sixinMessages == null || sixinMessages.size() <= 0) {
                return null;
            }
            return sixinMessages.get(0);
        }
        return null;
    }

    /**
     * 插入一条消息，但是不更新未读数
     *
     * @param sixinMessage
     */
    public static void insertSixinMessagesWithoutUpdateUnReadCount(SixinMessage sixinMessage) {
        //做去重
        if (sixinMessage != null) {
            List<SixinMessage> insertSixinList = new ArrayList<>();
            SixinMessage sixinMessageInDb = getSixinMessageInDb(sixinMessage);
            if (sixinMessageInDb != null) {
                sixinMessageInDb.updateSixinMessage(sixinMessage);
                sixinMessage.updateSixinMessage(sixinMessageInDb);
                updateSixinMessage(sixinMessageInDb);
            } else {
                insertSixinList.add(sixinMessage);
            }
            getDaoSession(GlobalData.app()).getSixinMessageDao().insertInTx(insertSixinList);
            EventBus.getDefault().post(new SixinMessageBulkInsertEvent(insertSixinList, false, true));
        }
    }

    public static void insertSixinMessages(List<SixinMessage> sixinMessages) {
        //做去重
        insertSixinMessages(sixinMessages, true);
    }

    public static void insertSixinMessages(List<SixinMessage> sixinMessages, boolean needsNotifyDBInsertEvent) {
        //做去重
        if (sixinMessages != null && sixinMessages.size() > 0) {
            boolean hasNewMessage = false;
            List<SixinMessage> insertSixinList = new ArrayList<>();

            for (SixinMessage sixinMessage : sixinMessages) {
                MyLog.v(STAG, "sixinMessage=" + sixinMessage);
                SixinMessage sixinMessageInDb = getSixinMessageInDb(sixinMessage);
                if (sixinMessageInDb != null) {
                    sixinMessageInDb.updateSixinMessage(sixinMessage);
                    sixinMessage.updateSixinMessage(sixinMessageInDb);
                    updateSixinMessage(sixinMessageInDb, needsNotifyDBInsertEvent);
                } else {
                    if (sixinMessage.getIsInbound()) {
                        hasNewMessage = true;
                    }
                    if (sixinMessage.getMsgTyppe() != SixinMessage.S_MSG_TYPE_LEAVE_GROUP && sixinMessage.getMsgTyppe() != SixinMessage.S_MSG_TYPE_QUIT_GROUP) {
                        insertSixinList.add(sixinMessage);
                    }
                }
            }
            getDaoSession(GlobalData.app()).getSixinMessageDao().insertInTx(insertSixinList);
            EventBus.getDefault().post(new SixinMessageBulkInsertEvent(insertSixinList, hasNewMessage, needsNotifyDBInsertEvent));

        }
    }

    public static void updateSixinMessage(SixinMessage sixinMessage) {
        updateSixinMessage(sixinMessage, true);
    }

    public static void updateSixinMessage(SixinMessage sixinMessage, boolean needNotify) {
        if (sixinMessage != null) {
            getDaoSession(GlobalData.app()).getSixinMessageDao().update(sixinMessage);
            if (needNotify) {
                EventBus.getDefault().post(new SixinMessageUpdateEvent(sixinMessage));
            }
        }
    }

    public static void batchDeleteSixInMessage(List<Long> ids, long target, int targetType) {
        if (ids != null && ids.size() > 0) {
            getDaoSession(GlobalData.app()).getSixinMessageDao().deleteByKeyInTx(ids);
            EventBus.getDefault().post(new SixInMessageBulkDeleteEvent(ids, target, targetType));
        }
    }

    public static SixinMessage getLastSixInMessageByTarget(long target, int targetType, boolean includeDelete) {
        long userId = UserAccountManager.getInstance().getUuidAsLong();
        SixinMessageDao sixinMessageDao = getDaoSession(GlobalData.app()).getSixinMessageDao();
        QueryBuilder queryBuilder = sixinMessageDao.queryBuilder();
        if (includeDelete) {
            queryBuilder.where(SixinMessageDao.Properties.LocaLUserId.eq(userId), SixinMessageDao.Properties.Target.eq(target), SixinMessageDao.Properties.TargetType.eq(targetType)
                    , SixinMessageDao.Properties.MsgTyppe.notEq(SixinMessage.S_MSG_TYPE_DRAFT)
            ).orderDesc(SixinMessageDao.Properties.ReceivedTime).limit(1).build();
        } else {
            queryBuilder.where(SixinMessageDao.Properties.LocaLUserId.eq(userId), SixinMessageDao.Properties.Target.eq(target), SixinMessageDao.Properties.TargetType.eq(targetType), SixinMessageDao.Properties.ServerStoreStatus.notEq(SixinMessage.SERVER_STORE_STATUS_DELETE)
                    , SixinMessageDao.Properties.MsgTyppe.notEq(SixinMessage.S_MSG_TYPE_DRAFT)
            ).orderDesc(SixinMessageDao.Properties.ReceivedTime).limit(1).build();
        }
        List<SixinMessage> sixinMessages = queryBuilder.list();
        if (sixinMessages == null || sixinMessages.size() <= 0) {
            return null;
        }
        return sixinMessages.get(0);
    }


    public static SixinMessage getSixinMessageBySeqAndTarget(long target, int targetType, long seq) {
        long userId = UserAccountManager.getInstance().getUuidAsLong();
        SixinMessageDao sixinMessageDao = getDaoSession(GlobalData.app()).getSixinMessageDao();
        QueryBuilder queryBuilder = sixinMessageDao.queryBuilder();
        queryBuilder.where(SixinMessageDao.Properties.LocaLUserId.eq(userId), SixinMessageDao.Properties.Target.eq(target), SixinMessageDao.Properties.TargetType.eq(targetType), SixinMessageDao.Properties.MsgSeq.eq(seq)).build();
        List<SixinMessage> sixinMessages = queryBuilder.list();
        if (sixinMessages == null || sixinMessages.size() <= 0) {
            return null;
        }
        return sixinMessages.get(0);
    }


    public static List<SixinMessage> getSixinMessagesBetweenSeq(long target, int targetType, long minSeq, long maxSeq, List<Long> excludeIds) {
        long userId = UserAccountManager.getInstance().getUuidAsLong();
        SixinMessageDao sixinMessageDao = getDaoSession(GlobalData.app()).getSixinMessageDao();
        QueryBuilder queryBuilder = sixinMessageDao.queryBuilder();
        if (excludeIds != null && excludeIds.size() > 0) {
            queryBuilder.where(SixinMessageDao.Properties.Id.notIn(excludeIds), SixinMessageDao.Properties.LocaLUserId.eq(userId), SixinMessageDao.Properties.Target.eq(target), SixinMessageDao.Properties.MsgSeq.between(minSeq, maxSeq), SixinMessageDao.Properties.ServerStoreStatus.notEq(SixinMessage.SERVER_STORE_STATUS_DELETE)
                    , SixinMessageDao.Properties.MsgTyppe.notEq(SixinMessage.S_MSG_TYPE_DRAFT)
            ).build();
        } else {
            queryBuilder.where(SixinMessageDao.Properties.LocaLUserId.eq(userId), SixinMessageDao.Properties.Target.eq(target), SixinMessageDao.Properties.MsgSeq.between(minSeq, maxSeq), SixinMessageDao.Properties.ServerStoreStatus.notEq(SixinMessage.SERVER_STORE_STATUS_DELETE)
                    , SixinMessageDao.Properties.MsgTyppe.notEq(SixinMessage.S_MSG_TYPE_DRAFT)).build();
        }
        List<SixinMessage> sixinMessages = queryBuilder.list();
        if (sixinMessages == null || sixinMessages.size() <= 0) {
            return null;
        }
        return sixinMessages;
    }

    public static void deleteAll() {
        getDaoSession(GlobalData.app()).getSixinMessageDao().deleteAll();
    }

    public static void deleteMessageByRcvConversationEvent(List<Long> targets) {
        long userId = UserAccountManager.getInstance().getUuidAsLong();
        SixinMessageDao sixinMessageDao = getDaoSession(GlobalData.app()).getSixinMessageDao();
        QueryBuilder queryBuilder = sixinMessageDao.queryBuilder();
        queryBuilder.where(SixinMessageDao.Properties.LocaLUserId.eq(userId));
        if (targets.size() == 1) {
            queryBuilder.where(SixinMessageDao.Properties.Target.eq(targets.get(0)));
        } else if (targets.size() == 2) {
            queryBuilder.whereOr(SixinMessageDao.Properties.Target.eq(targets.get(0)), SixinMessageDao.Properties.Target.eq(targets.get(1)));
        } else {
            WhereCondition[] whereConditions = new WhereCondition[targets.size() - 2];
            for (int i = 2; i < targets.size(); i++) {
                whereConditions[i - 2] = SixinMessageDao.Properties.Target.eq(targets.get(i));
            }
            queryBuilder.whereOr(SixinMessageDao.Properties.Target.eq(targets.get(0)), SixinMessageDao.Properties.Target.eq(targets.get(1)), whereConditions);
        }
        queryBuilder.buildDelete().executeDeleteWithoutDetachingEntities();
    }

    /**
     * @param time
     * @param target
     * @param limit
     * @param isLt   是否是小于，不是小于则表示是大于
     * @return
     */
    public static List<SixinMessage> getImageMessagesByTime(long time, long target, int targetType, int limit, boolean isLt) {
        SixinMessageDao sixinMessageDao = getDaoSession(GlobalData.app()).getSixinMessageDao();
        QueryBuilder queryBuilder = sixinMessageDao.queryBuilder();
        WhereCondition timeCondition = isLt ? SixinMessageDao.Properties.ReceivedTime.le(time) : SixinMessageDao.Properties.ReceivedTime.ge(time);
        queryBuilder.where(SixinMessageDao.Properties.Target.eq(target), SixinMessageDao.Properties.TargetType.eq(targetType), timeCondition, SixinMessageDao.Properties.MsgTyppe.eq(SixinMessage.S_MSG_TYPE_PIC), SixinMessageDao.Properties.ServerStoreStatus.notEq(SixinMessage.SERVER_STORE_STATUS_DELETE)
                , SixinMessageDao.Properties.MsgTyppe.notEq(SixinMessage.S_MSG_TYPE_DRAFT)).limit(limit);
        if (!isLt) {
            queryBuilder.orderAsc(SixinMessageDao.Properties.ReceivedTime).build();
        } else {
            queryBuilder.orderDesc(SixinMessageDao.Properties.ReceivedTime).build();
        }
        List<SixinMessage> sixinMessages = queryBuilder.list();
        if (sixinMessages != null && sixinMessages.size() > 0) {
            return sixinMessages;
        }
        return null;
    }

    public static void updateSixMessageLocalPath(long msgId, String localPath) {
        if (!TextUtils.isEmpty(localPath)) {
            SixinMessage sixinMessage = getSixinMessageByMsgId(msgId);
            if (sixinMessage != null) {
                Attachment att = sixinMessage.getAtt();
                if (att != null) {
                    att.setLocalPath(localPath);
                    sixinMessage.setExt(att.toJSONString());
                    updateSixinMessage(sixinMessage);
                }
            }
        }
    }


    public static SixinMessage getDraftSixinMessageAndNotInsertToDB(String targetName, long target, int targetType, String message
            /*, AtMessage atMessage*/) {
        int msgType = SixinMessage.S_MSG_TYPE_DRAFT;
        long mySelfId = UserAccountManager.getInstance().getUuidAsLong();
        long cid = System.currentTimeMillis() + Attachment.generateAttachmentId();
        SixinMessage sixinMessage = new SixinMessage();
        sixinMessage.setBody(message);
        sixinMessage.setSentTime(Long.MAX_VALUE);
        sixinMessage.setReceivedTime(System.currentTimeMillis());
        sixinMessage.setIsInbound(false);
        sixinMessage.setMsgStatus(0);
        sixinMessage.setOutboundStatus(SixinMessage.OUTBOUND_STATUS_RECEIVED);
        sixinMessage.setTarget(target);
        if (targetType == SixinMessage.TARGET_TYPE_USER) {
            sixinMessage.setTargetName(targetName);
        } else if (targetType == SixinMessage.TARGET_TYPE_GROUP || targetType == SixinMessage.TARGET_TYPE_VFANS) {
            sixinMessage.setTargetName(MyUserInfoManager.getInstance().getNickname());
        }
        sixinMessage.setSender(mySelfId);
        sixinMessage.setMsgTyppe(msgType);
        sixinMessage.setLocaLUserId(mySelfId);
        sixinMessage.setSenderMsgId(cid);
        sixinMessage.setCertificationType(0);
        sixinMessage.setTargetType(targetType);
        sixinMessage.setMsgSeq(Long.MAX_VALUE);
        //TODO 群聊相关，拿掉
//        if (atMessage != null) {
//            Attachment attachment = new Attachment();
//            attachment.setObjectKey(new Gson().toJson(atMessage));
//            sixinMessage.setExt(attachment.toJSONString());
//        }
        return sixinMessage;
    }


    public static SixinMessage getTextSixinMessageAndNotInsertToDB(String targetName, long target, int targetType, String message, int foucsStatus, int certificationType) {
        int msgType = SixinMessage.S_MSG_TYPE_TEXT;
        long mySelfId = UserAccountManager.getInstance().getUuidAsLong();
        long cid = System.currentTimeMillis() + Attachment.generateAttachmentId();
        SixinMessage sixinMessage = new SixinMessage();
        sixinMessage.setBody(message);
        sixinMessage.setSentTime(Long.MAX_VALUE);
        sixinMessage.setReceivedTime(System.currentTimeMillis());
        sixinMessage.setIsInbound(false);
        sixinMessage.setMsgStatus(foucsStatus);
        sixinMessage.setOutboundStatus(SixinMessage.OUTBOUND_STATUS_UNSENT);
        sixinMessage.setTarget(target);
        if (targetType == SixinMessage.TARGET_TYPE_USER) {
            sixinMessage.setTargetName(targetName);
        } else if (targetType == SixinMessage.TARGET_TYPE_GROUP || targetType == SixinMessage.TARGET_TYPE_VFANS) {
            sixinMessage.setTargetName(MyUserInfoManager.getInstance().getNickname());
        }
        sixinMessage.setSender(mySelfId);
        sixinMessage.setMsgTyppe(msgType);
        sixinMessage.setLocaLUserId(mySelfId);
        sixinMessage.setSenderMsgId(cid);
        sixinMessage.setCertificationType(certificationType);
        sixinMessage.setTargetType(targetType);
        sixinMessage.setMsgSeq(Long.MAX_VALUE);
        return sixinMessage;
    }

    public static SixinMessage getAtSixinMessageAndNotInsertToDB(String targetName, long target,
                                                                 int targetType, String message,
                                                                 int foucsStatus,
                                                                 int certificationType
                                                                 /*,AtMessage atMessage*/) {
        int msgType = SixinMessage.S_MSG_TYPE_AT;
        long mySelfId = UserAccountManager.getInstance().getUuidAsLong();
        long cid = System.currentTimeMillis() + Attachment.generateAttachmentId();
        SixinMessage sixinMessage = new SixinMessage();
        sixinMessage.setBody(message);
        sixinMessage.setSentTime(Long.MAX_VALUE);
        sixinMessage.setReceivedTime(System.currentTimeMillis());
        sixinMessage.setIsInbound(false);
        sixinMessage.setMsgStatus(foucsStatus);
        sixinMessage.setOutboundStatus(SixinMessage.OUTBOUND_STATUS_UNSENT);
        sixinMessage.setTarget(target);
        if (targetType == SixinMessage.TARGET_TYPE_USER) {
            sixinMessage.setTargetName(targetName);
        } else if (targetType == SixinMessage.TARGET_TYPE_GROUP || targetType == SixinMessage.TARGET_TYPE_VFANS) {
            sixinMessage.setTargetName(MyUserInfoManager.getInstance().getNickname());
        }
        sixinMessage.setSender(mySelfId);
        sixinMessage.setMsgTyppe(msgType);
        sixinMessage.setLocaLUserId(mySelfId);
        sixinMessage.setSenderMsgId(cid);
        sixinMessage.setCertificationType(certificationType);
        sixinMessage.setTargetType(targetType);
        sixinMessage.setMsgSeq(Long.MAX_VALUE);
//        Attachment attachment = new Attachment();
//        attachment.setObjectKey(new Gson().toJson(atMessage));
//        sixinMessage.setExt(attachment.toJSONString());
        return sixinMessage;
    }

    public static List<SixinMessage> getSixInMessagesByType(List<Attachment> atts, String targetName, long target, int targetType, int foucsStatus, int certificationType, int msgType, int level) {
        if (target < 0 || atts == null || atts.size() == 0) {
            return null;
        }
        List<SixinMessage> sixinMessages = new ArrayList<>();
        int i = 0;
        for (Attachment att : atts) {
            i++;
            if (att == null) {
                continue;
            }
            long mySelfId = UserAccountManager.getInstance().getUuidAsLong();
            long cid = System.currentTimeMillis() + Attachment.generateAttachmentId();
            SixinMessage sixinMessage = new SixinMessage();
//TODO 文案暂时没拷贝需要该类时在拷贝
//            switch (msgType) {
//                case SixinMessage.S_MSG_TYPE_NOTIFY: {
//                    if (targetType == SixinMessage.TARGET_TYPE_USER) {
//                        sixinMessage.setBody(GlobalData.app().getString(com.mi.live.data.R.string.six_in_send_default_text));
//                    } else {
//                        sixinMessage.setBody("[" + GlobalData.app().getString(com.mi.live.data.R.string.group_notify_body) + "]");
//                    }
//                }
//                break;
//                case SixinMessage.S_MSG_TYPE_PIC: {
//                    if (targetType == SixinMessage.TARGET_TYPE_USER) {
//                        sixinMessage.setBody(GlobalData.app().getString(com.mi.live.data.R.string.six_in_send_pic_default_text));
//                    } else {
//                        sixinMessage.setBody("[" + com.base.global.GlobalData.app().getResources().getString(com.mi.live.data.R.string.sixin_conversation_photo) + "]");
//                    }
//                }
//                break;
//                case SixinMessage.S_MSG_TYPE_VOICE: {
//                    if (targetType == SixinMessage.TARGET_TYPE_USER) {
//                        sixinMessage.setBody(GlobalData.app().getString(com.mi.live.data.R.string.six_in_send_audio_default_text));
//                    } else {
//                        sixinMessage.setBody("[" + com.base.global.GlobalData.app().getResources().getString(com.mi.live.data.R.string.sixin_conversation_audio) + "]");
//                    }
//                }
//                break;
//                default:
//                    sixinMessage.setBody(GlobalData.app().getString(com.mi.live.data.R.string.six_in_send_audio_default_text));
//            }

            sixinMessage.setSentTime(Long.MAX_VALUE);
            sixinMessage.setReceivedTime(System.currentTimeMillis() + i);
            sixinMessage.setIsInbound(false);
            sixinMessage.setMsgStatus(foucsStatus);
            sixinMessage.setOutboundStatus(SixinMessage.OUTBOUND_STATUS_UNSENT);
            sixinMessage.setTarget(target);
            if (targetType == SixinMessage.TARGET_TYPE_USER) {
                sixinMessage.setTargetName(targetName);
            } else if (targetType == SixinMessage.TARGET_TYPE_GROUP || targetType == SixinMessage.TARGET_TYPE_VFANS) {
                sixinMessage.setTargetName(MyUserInfoManager.getInstance().getNickname());
            }
            sixinMessage.setSender(mySelfId);
            sixinMessage.setMsgTyppe(msgType);
            sixinMessage.setLocaLUserId(mySelfId);
            sixinMessage.setSenderMsgId(cid);
            sixinMessage.setExt(att.toJSONString());
            sixinMessage.setCertificationType(certificationType);
            sixinMessage.setTargetType(targetType);
            sixinMessage.setMsgSeq(Long.MAX_VALUE);
            if (targetType == SixinMessage.TARGET_TYPE_GROUP) {
                sixinMessage.setGroupLevel(level);
            }
            sixinMessages.add(sixinMessage);
        }
        insertSixinMessages(sixinMessages);
        return sixinMessages;
    }

    /**
     * 修改消息的状态未已读
     */
    public static void markSixinMessageAsRead(long msgId) {
        SixinMessageDao sixinMessageDao = getDaoSession(GlobalData.app()).getSixinMessageDao();
        long userId = UserAccountManager.getInstance().getUuidAsLong();
        QueryBuilder queryBuilder = sixinMessageDao.queryBuilder();
        queryBuilder.where(SixinMessageDao.Properties.LocaLUserId.eq(userId), SixinMessageDao.Properties.Id.eq(msgId));
        List<SixinMessage> sixinMessages = queryBuilder.build().list();
        if (sixinMessages != null && sixinMessages.size() > 0) {
            SixinMessage firstMsg = sixinMessages.get(0);
            if (firstMsg.getServerStoreStatus() != SixinMessage.INBOUND_STATUS_READ) {
                firstMsg.setServerStoreStatus(SixinMessage.INBOUND_STATUS_READ);
                updateSixinMessage(firstMsg);
            }
        }
    }

    /**
     * 获取草稿消息
     *
     * @param target
     * @param type
     * @return
     */
    public static SixinMessage getDraftSixinMessage(long target, int type) {
        SixinMessageDao sixinMessageDao = getDaoSession(GlobalData.app()).getSixinMessageDao();
        long userId = UserAccountManager.getInstance().getUuidAsLong();
        QueryBuilder queryBuilder = sixinMessageDao.queryBuilder();
        queryBuilder.where(SixinMessageDao.Properties.LocaLUserId.eq(userId), SixinMessageDao.Properties.Target.eq(target)
                , SixinMessageDao.Properties.TargetType.eq(type), SixinMessageDao.Properties.MsgTyppe.eq(SixinMessage.S_MSG_TYPE_DRAFT));
        List<SixinMessage> sixinMessages = queryBuilder.list();
        if (sixinMessages != null && sixinMessages.size() > 0) {
            return sixinMessages.get(0);
        }
        return null;
    }

    /**
     * 删除草稿消息
     */
    public static void deleteDraftSixinMessage(long target, int type) {
        SixinMessageDao sixinMessageDao = getDaoSession(GlobalData.app()).getSixinMessageDao();
        long userId = UserAccountManager.getInstance().getUuidAsLong();
        QueryBuilder queryBuilder = sixinMessageDao.queryBuilder();
        queryBuilder.where(SixinMessageDao.Properties.LocaLUserId.eq(userId), SixinMessageDao.Properties.Target.eq(target)
                , SixinMessageDao.Properties.TargetType.eq(type), SixinMessageDao.Properties.MsgTyppe.eq(SixinMessage.S_MSG_TYPE_DRAFT));
        List<SixinMessage> sixinMessages = queryBuilder.list();
        if (sixinMessages != null && sixinMessages.size() > 0) {
            List<Long> msgIds = new ArrayList<>();
            for (SixinMessage item : sixinMessages) {
                msgIds.add(item.getId());
            }
            sixinMessageDao.deleteInTx(sixinMessages);
            EventBus.getDefault().post(new SixInMessageBulkDeleteEvent(msgIds, target, type));
        }
    }

    public static class SixinMessageBulkInsertEvent {
        public List<SixinMessage> sixinMessages;

        public boolean hasNewMessage = false;

        public boolean needsUpdateUi = true;

        SixinMessageBulkInsertEvent(List<SixinMessage> sixinMessages, boolean hasNewMessage, boolean needsUpdateUi) {
            this.sixinMessages = sixinMessages;
            this.hasNewMessage = hasNewMessage;
            this.needsUpdateUi = needsUpdateUi;
        }
    }

    public static class SixinMessageUpdateEvent {
        public SixinMessage sixinMessage;

        SixinMessageUpdateEvent(SixinMessage sixinMessage) {
            this.sixinMessage = sixinMessage;
        }
    }

    public static class SixInMessageBulkDeleteEvent {
        public List<Long> msgIds;
        public long target;
        public int targetType;

        public SixInMessageBulkDeleteEvent(List<Long> ids, long target, int targetType) {
            this.msgIds = ids;
            this.target = target;
            this.targetType = targetType;
        }
    }


    public static class CleanGroupAllMessageEvent {
        public long groupId;

        CleanGroupAllMessageEvent(long groupId) {
            this.groupId = groupId;
        }
    }


}
