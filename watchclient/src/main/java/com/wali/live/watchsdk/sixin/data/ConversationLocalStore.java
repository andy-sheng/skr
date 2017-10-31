package com.wali.live.watchsdk.sixin.data;

import android.database.Cursor;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;

import com.base.global.GlobalData;
import com.base.log.MyLog;
import com.base.utils.span.SpanUtils;
import com.mi.live.data.account.MyUserInfoManager;
import com.mi.live.data.account.UserAccountManager;
import com.mi.live.data.config.GetConfigManager;
import com.mi.live.data.greendao.GreenDaoManager;
import com.wali.live.dao.Conversation;
import com.wali.live.dao.ConversationDao;
import com.wali.live.dao.SixinMessage;
import com.wali.live.watchsdk.R;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.dao.query.QueryBuilder;

/**
 * Created by anping on 16-10-10.
 */
public class ConversationLocalStore {
    private static final String TAG = "ConversationLocalStore";
    private final static int MAX_QUERY_NUM_COUNT = 40;

    public static final int TARGET_100000 = 100000;
    public static final int TARGET_126 = 126;

    public static boolean SHOW_VFAN_GROUP = false; //是否显示宠爱团的对话列表
    public static final int FEED_TYPE_WORKS = 6;      //作品


    //由于直播的原因，对话列表的私信可能会很多，所以getAllConversaion基本不会使用
    public static List<Conversation> getAllConversation(boolean showFocus) {
        long userId = UserAccountManager.getInstance().getUuidAsLong();
        ConversationDao dao = GreenDaoManager.getDaoSession(GlobalData.app()).getConversationDao();
        QueryBuilder queryBuilder = dao.queryBuilder();
        queryBuilder.where(ConversationDao.Properties.LocaLUserId.eq(userId), ConversationDao.Properties.IsNotFocus.eq(!showFocus));
        if (!SHOW_VFAN_GROUP) {
            queryBuilder.where(ConversationDao.Properties.TargetType.notEq(SixinMessage.TARGET_TYPE_VFANS));
        }
        queryBuilder.where(ConversationDao.Properties.TargetType.notEq(SixinMessage.TARGET_TYPE_GROUP));
        queryBuilder.where(ConversationDao.Properties.Target.notEq(Conversation.GROUP_NOTIFY_CONVERSATION_TARGET));
        queryBuilder.orderDesc(ConversationDao.Properties.ReceivedTime).build();
        List<Conversation> conversations = queryBuilder.list();
        if (conversations == null) {
            conversations = new ArrayList<>();
        }
//        addPermanentConversationIfNeed(conversations);
        return conversations;
    }

    //加入常驻置顶的会话，
    private static void addPermanentConversationIfNeed(List<Conversation> conversations) {
        boolean hasInteract = false;

        for (Conversation conversation : conversations) {
            if (conversation.getTarget() == TARGET_126) {
                hasInteract = true;
            }
        }
        if (!hasInteract) {
            addFakeConversation(TARGET_126, R.string.message_interact_notify);
        }
    }

    private static void addFakeConversation(int targetId, int targetNameResId) {
        Conversation conversation = new Conversation();
        conversation.setTarget(targetId);
        conversation.setLocaLUserId(UserAccountManager.getInstance().getUuidAsLong());
        conversation.setReceivedTime(System.currentTimeMillis());
        conversation.setUnreadCount(0);
        conversation.setTargetName(GlobalData.app().getString(targetNameResId));
        conversation.setIsNotFocus(false);
        conversation.setIgnoreStatus(Conversation.NOT_IGNORE);
        conversation.setSendTime(System.currentTimeMillis());
        conversation.setLastMsgSeq(0l);
        conversation.setMsgId(0l);
        String content = GlobalData.app().getString(R.string.currently_none_message);
        conversation.setContent(content);
        conversation.setMsgType(SixinMessage.S_MSG_TYPE_TEXT);
        conversation.setTargetType(SixinMessage.TARGET_TYPE_USER);
        long id = GreenDaoManager.getDaoSession(GlobalData.app()).getConversationDao().insert(conversation);
        conversation.setId(id);
        EventBus.getDefault().post(new ConversationInsertEvent(conversation));
    }

    public static void insertOrUpdateConversationByMessage(SixinMessage sixinMessage, boolean needUpdateUnreadCount) {
        long target = sixinMessage.getTarget();
        Conversation conversation = getConversationByTarget(target, sixinMessage.getTargetType());
        if (conversation == null) {
            conversation = new Conversation();
            updateConversationBySixinMessage(conversation, sixinMessage);
//            if (TextUtils.isEmpty(conversation.getTargetName()) && (sixinMessage.getTargetType() == SixinMessage.TARGET_TYPE_GROUP || sixinMessage.getTargetType() == SixinMessage.TARGET_TYPE_VFANS)) {
//                conversation.setTargetName("");
//                SixinGroup sixinGroup = new GroupManagerLocalStore().getSixinGroupBeanInDB(sixinMessage.getTargetUser());
//                if (sixinGroup != null) {
//                    conversation.setTargetName(sixinGroup.getName());
//                    String icon = TextUtils.isEmpty(sixinGroup.getAvatar()) ? "" + sixinGroup.getOwnerId() : sixinGroup.getAvatar();
//                    conversation.setIcon(icon);
//                }
//            }
            if (needUpdateUnreadCount && sixinMessage.getIsInbound() && sixinMessage.getTargetType() == SixinMessage.TARGET_TYPE_USER) {//只有单聊消息才需要收到多少条消息则认为有多少条未读，但是群聊消息则需要服务器告诉
                conversation.setUnreadCount(1);
            }
            long cid = GreenDaoManager.getDaoSession(GlobalData.app()).getConversationDao().insert(conversation);
            conversation.setId(cid);
            if (sixinMessage.getIsInbound()) {
                conversation.setCertificationType(sixinMessage.getCertificationType());
            }
            EventBus.getDefault().post(new ConversationInsertEvent(conversation));
        } else {
//            if (TextUtils.isEmpty(conversation.getTargetName()) && (sixinMessage.getTargetType() == SixinMessage.TARGET_TYPE_GROUP || sixinMessage.getTargetType() == SixinMessage.TARGET_TYPE_VFANS)) {
//                conversation.setTargetName("");
//                SixinGroup sixinGroup = new GroupManagerLocalStore().getSixinGroupBeanInDB(sixinMessage.getTargetUser());
//                if (sixinGroup != null) {
//                    conversation.setTargetName(sixinGroup.getName());
//                    String icon = TextUtils.isEmpty(sixinGroup.getAvatar()) ? "" + sixinGroup.getOwnerId() : sixinGroup.getAvatar();
//                    conversation.setIcon(icon);
//                }
//            }

            if (needUpdateUnreadCount && sixinMessage.getIsInbound() && sixinMessage.getIsRead() != SixinMessage.IS_READ.READ && sixinMessage.getTargetType() == SixinMessage.TARGET_TYPE_USER) {//只有单聊消息才需要收到多少条消息则认为有多少条未读，但是群聊消息则需要服务器告诉
                if (conversation.getUnreadCount() == null) {
                    conversation.setUnreadCount(1);
                } else {
                    conversation.setUnreadCount(1 + conversation.getUnreadCount());
                }
            }

            if (conversation.getMsgType() != SixinMessage.S_MSG_TYPE_DRAFT) {
                if (conversation.getTargetType() == SixinMessage.TARGET_TYPE_USER) {
                    if (sixinMessage.getReceivedTime() > conversation.getReceivedTime() || (sixinMessage.getSentTime().equals(conversation.getSendTime()) && sixinMessage.getId() > conversation.getMsgId())) {
                        updateConversationBySixinMessage(conversation, sixinMessage);
                    }
                } else if ((conversation.getTargetType() == SixinMessage.TARGET_TYPE_VFANS || conversation.getTargetType() == SixinMessage.TARGET_TYPE_GROUP)) {
                    if (sixinMessage.getMsgSeq() == 0 || conversation.getLastMsgSeq() == 0) { //如果其中一方是通知消息就需要使用时间来做比较
                        if (sixinMessage.getMsgSeq() == 0) {
                            if (sixinMessage.getReceivedTime() > conversation.getReceivedTime() || (sixinMessage.getSentTime().equals(conversation.getSendTime()) && sixinMessage.getId() > conversation.getMsgId())) {
                                updateConversationBySixinMessage(conversation, sixinMessage);
                            }
                        } else {
                            updateConversationBySixinMessage(conversation, sixinMessage);
                        }
                    } else {
                        if (sixinMessage.getMsgSeq() > conversation.getLastMsgSeq() || (sixinMessage.getSentTime().equals(conversation.getSendTime()) && sixinMessage.getId() > conversation.getMsgId())) {
                            updateConversationBySixinMessage(conversation, sixinMessage);
                        }
                    }
                }
            } else if (sixinMessage.getReceivedTime() < Long.MAX_VALUE && sixinMessage.getSentTime() > conversation.getReceivedTime()) {
                conversation.setReceivedTime(sixinMessage.getSentTime());
            }

            updateConversation(conversation);
        }
        insertOrUpdateUnFoucsRobotConversation(conversation, 1, needUpdateUnreadCount);
    }

    public static void insertOrUpdateUnFoucsRobotConversation(Conversation conversation, int unreadCountInConversation, boolean needUpdateUnReadCount) {
        if (conversation != null && conversation.getIsNotFocus()) {
//            boolean isInTrashbin = TrashBinStore.isInTrashBin(conversation.getTarget());
            boolean isInTrashbin = false; // TODO-YangLi 加入垃圾箱逻辑
            Conversation robot = getConversationByTarget(Conversation.UNFOCUS_CONVERSATION_TARGET, SixinMessage.TARGET_TYPE_USER);
            if (robot == null) {
                long userId = UserAccountManager.getInstance().getUuidAsLong();
                robot = new Conversation();
                robot.setTarget(Conversation.UNFOCUS_CONVERSATION_TARGET);
                robot.setLocaLUserId(userId);
                if (needUpdateUnReadCount && !isInTrashbin) {
                    robot.setUnreadCount(unreadCountInConversation);
                } else {
                    robot.setUnreadCount(0);
                }
                robot.setIsNotFocus(false);
                robot.setTargetName(GlobalData.app().getString(com.mi.live.data.R.string.unfocus_robot_name));
                robot.setIgnoreStatus(Conversation.IGNOE_BUT_SHOW_UNREAD);
                if (isInTrashbin) {
                    robot.setReceivedTime(0l);
                    robot.setSendTime(0l);
                } else {
                    robot.setReceivedTime(conversation.getReceivedTime());
                    robot.setSendTime(conversation.getSendTime());
                }

                robot.setLastMsgSeq(conversation.getLastMsgSeq());
                robot.setMsgId(conversation.getMsgId());
                robot.setTargetType(SixinMessage.TARGET_TYPE_USER);
                if (!isInTrashbin) {
                    robot.setContent(conversation.getContent());
                    robot.setMsgType(conversation.getMsgType());
                    robot.setSendTime(conversation.getSendTime());
                    robot.setReceivedTime(conversation.getReceivedTime());
                } else {
                    robot.setContent("");
                    robot.setMsgType(SixinMessage.S_MSG_TYPE_TEXT);
                    robot.setReceivedTime(0l);
                    robot.setSendTime(0l);
                }
                if (!TextUtils.isEmpty(conversation.getExt())) {
                    try {
                        JSONObject jsonObject = new JSONObject(conversation.getExt());
                        long sender = jsonObject.optLong(Conversation.EXT_SENDER);
                        if (!isInTrashbin) {
                            if (sender == MyUserInfoManager.getInstance().getUuid()) {
                                String me = GlobalData.app().getString(com.mi.live.data.R.string.me);
                                robot.setContent(me + ": " + conversation.getContent());
                            } else {
                                robot.setContent(conversation.getTargetName() + ": " + conversation.getContent());
                            }
                        }
                        if (needUpdateUnReadCount && conversation.getIsNotFocus() && isInTrashbin) {
                            //如果是在垃圾箱中，则需要更新下 垃圾箱未读数
                            jsonObject.put(Conversation.EXT_RUBBISH_UNREAD_COUNT, conversation.getUnreadCount());
//                            EventBus.getDefault().post(new NotifyTrashBinCountEvent(conversation.getUnreadCount())); TODO-YangLi 加入垃圾箱逻辑
                        }
                        robot.setExt(jsonObject.toString());
                    } catch (Exception e) {
                        MyLog.e(e);
                    }
                }
                long cid = GreenDaoManager.getDaoSession(GlobalData.app()).getConversationDao().insert(robot);
                robot.setId(cid);
                EventBus.getDefault().post(new ConversationInsertEvent(robot));
            } else {
                if (!isInTrashbin && conversation.getReceivedTime() > robot.getReceivedTime() || (conversation.getSendTime().equals(robot.getSendTime()) && conversation.getMsgId() >= robot.getMsgId())) {
                    robot.setSendTime(conversation.getSendTime());
                    robot.setReceivedTime(conversation.getReceivedTime());
                    robot.setContent(conversation.getContent());
                    robot.setLastMsgSeq(conversation.getLastMsgSeq());
                    robot.setMsgType(conversation.getMsgType());
                    robot.setMsgId(conversation.getMsgId());
                    if (!TextUtils.isEmpty(conversation.getExt())) {
                        try {
                            JSONObject jsonObject = new JSONObject(conversation.getExt());
                            long sender = jsonObject.optLong(Conversation.EXT_SENDER);
                            if (sender == MyUserInfoManager.getInstance().getUuid()) {
                                String me = GlobalData.app().getString(com.mi.live.data.R.string.me);
                                robot.setContent(me + ": " + conversation.getContent());
                            } else {
                                robot.setContent(conversation.getTargetName() + ": " + conversation.getContent());
                            }
                            if (TextUtils.isEmpty(conversation.getContent())) {
                                robot.setContent("");
                            }
                            robot.setExt(jsonObject.toString());
                        } catch (Exception e) {
                            MyLog.e(e);
                        }
                    }
                }

                if (needUpdateUnReadCount && conversation.getIsNotFocus() && isInTrashbin) {
                    //如果是在垃圾箱中，则需要更新下 垃圾箱未读数
                    try {
                        JSONObject jsonObject1 = new JSONObject(robot.getExt());
                        int unreadCount = jsonObject1.optInt(Conversation.EXT_RUBBISH_UNREAD_COUNT, 0);
                        jsonObject1.put(Conversation.EXT_RUBBISH_UNREAD_COUNT, unreadCountInConversation + unreadCount);
                        robot.setExt(jsonObject1.toString());
                        MyLog.v("testData NotifyTrashBinCountEvent post" + unreadCount + unreadCountInConversation);
//                        EventBus.getDefault().post(new NotifyTrashBinCountEvent(unreadCount + unreadCountInConversation)); TODO-YangLi 加入垃圾箱逻辑
                    } catch (Exception e) {
                        MyLog.e(e);
                    }
                }

                if (!isInTrashbin && needUpdateUnReadCount && conversation.getUnreadCount() != null && conversation.getUnreadCount() > 0) {
                    int count = robot.getUnreadCount() == null ? conversation.getUnreadCount() : robot.getUnreadCount() + unreadCountInConversation;
                    robot.setUnreadCount(count);
                }

                updateConversation(robot);
            }
        }
    }

    public static void updateConversation(Conversation conversation) {
        GreenDaoManager.getDaoSession(GlobalData.app()).getConversationDao().update(conversation);
        EventBus.getDefault().post(new ConversationUpdateEvent(conversation));
    }

    public static void markConversationAsRead(long target, int targetType) {
        Conversation conversation = getConversationByTarget(target, targetType);
        if (conversation != null && conversation.getUnreadCount() != null && conversation.getUnreadCount() > 0) {
            conversation.setUnreadCount(0);
            conversation.updateOrInsertExt(Conversation.EXT_HAS_SOME_BODY_AT_ME, false);
            updateConversation(conversation);
            long unreadCount = getAllConversationUnReadCount();
            EventBus.getDefault().post(new NotifyUnreadCountChangeEvent(unreadCount));
        }
    }

    public static Conversation getConversationByTarget(long target, int targetType) {
        long userId = UserAccountManager.getInstance().getUuidAsLong();
        ConversationDao conversationDao = GreenDaoManager.getDaoSession(GlobalData.app()).getConversationDao();
        QueryBuilder queryBuilder = conversationDao.queryBuilder();
        queryBuilder.where(ConversationDao.Properties.LocaLUserId.eq(userId), ConversationDao.Properties.Target.eq(target), ConversationDao.Properties.TargetType.eq(targetType)).build();
        List<Conversation> conversations = queryBuilder.list();
        if (conversations == null || conversations.size() <= 0) {
            return null;
        }
        return conversations.get(0);
    }

    public static void updateConversationBySixinMessage(Conversation conversation, SixinMessage sixinMessage) {
        boolean isNewConversation = conversation.getLocaLUserId() == 0;  //localUser不可能等于0 哈，除非是新建的数据

        conversation.setTarget(sixinMessage.getTarget());
        if ((sixinMessage.getTargetType() == SixinMessage.TARGET_TYPE_USER) && (!TextUtils.isEmpty(sixinMessage.getTargetName()))) {
            conversation.setTargetName(sixinMessage.getTargetName());
        }
        conversation.setContent(converAttMsgBody(sixinMessage));
        conversation.setLastMsgSeq(sixinMessage.getMsgSeq());
        conversation.setReceivedTime(sixinMessage.getReceivedTime());
        conversation.setSendTime(sixinMessage.getSentTime());
        conversation.setMsgId(sixinMessage.getId());
        conversation.setMsgType(sixinMessage.getMsgTyppe());
        conversation.setLocaLUserId(sixinMessage.getLocaLUserId());
        conversation.setTargetType(sixinMessage.getTargetType());
        if (conversation.getCertificationType() == null || !conversation.getCertificationType().equals(sixinMessage.getCertificationType())) {
            conversation.setCertificationType(sixinMessage.getCertificationType());
        }
        List<Long> whiteList = GetConfigManager.getInstance().getSixinSystemServiceNumWhiteList();
        boolean isInWhiteList = false;
        if (whiteList != null && whiteList.size() > 0) {
            if (whiteList.contains(conversation.getTarget())) {
                isInWhiteList = true;
            }
        }

        try {
            conversation.updateOrInsertExt(Conversation.EXT_SENDER, sixinMessage.getSender());
            conversation.updateOrInsertExt(Conversation.EXT_IS_BLOCK, conversation.isBlock());
            conversation.updateOrInsertExt(Conversation.EXT_TARGET, conversation.getTarget());
//            if (sixinMessage.getIsInbound() && sixinMessage.getMsgTyppe() == SixinMessage.S_MSG_TYPE_AT && sixinMessage.getAtt() != null) {
//                AtMessage atMessage = new AtMessage(sixinMessage.getAtt());
//                if (atMessage != null && atMessage.getMemberIds() != null && atMessage.getMemberIds().size() > 0 && atMessage.getMemberIds().indexOfKey(MyUserInfoManager.getInstance().getUid()) >= 0) {
//                    conversation.updateOrInsertExt(Conversation.EXT_HAS_SOME_BODY_AT_ME, true);
//                }
//            }
            if (sixinMessage.getAtt() != null) {
                conversation.updateOrInsertExt(Conversation.EXT_ATT_ID, sixinMessage.getAtt().attId);
            } else {
                conversation.updateOrInsertExt(Conversation.EXT_ATT_ID, 0);
            }

            if (!isInWhiteList) {
                if (isNewConversation) {
//                    Relation relation = RelationLocalStore.getRelationByUUid(conversation.getTargetUser());//关系已经是老的了
//                    if (relation == null) {
                    conversation.setIsNotFocus(sixinMessage.getMsgStatus() == SixinMessage.MSG_STATUS_UNFOUCS);
                    conversation.updateOrInsertExt(Conversation.EXT_FOCUS_STATE, sixinMessage.getMsgStatus());
//                    } else {
//                        conversation.setIsNotFocus(relation.getFocusStatue() == SixinMessage.MSG_STATUS_UNFOUCS);
//                        conversation.updateOrInsertExt(Conversation.EXT_FOCUS_STATE, relation.getFocusStatue());
//                    }
                } else {
                    conversation.setIsNotFocus(conversation.getIsNotFocus());
                }
            } else {
                conversation.setIsNotFocus(false);
                conversation.updateOrInsertExt(Conversation.EXT_FOCUS_STATE, SixinMessage.MSG_STATUE_BOTHFOUCS);
            }
        } catch (Exception e) {
            MyLog.e(e);
        }

        if (conversation.getIgnoreStatus() == null) {
            conversation.setIgnoreStatus(Conversation.NOT_IGNORE);
        }
    }

    public static String converAttMsgBody(SixinMessage sixinMessage) {
        String result = sixinMessage.getBody();

        if (sixinMessage.getMsgTyppe() != SixinMessage.S_MSG_TYPE_TEXT) {
            switch (sixinMessage.getMsgTyppe()) {
                case SixinMessage.S_MSG_TYPE_PIC: {
                    result = "[" + GlobalData.app().getResources().getString(R.string.sixin_conversation_photo) + "]";
                }
                break;
//                case SixinMessage.S_MSG_TYPE_VOICE: {
//                    result = "[" + GlobalData.app().getResources().getString(R.string.sixin_conversation_audio) + "]";
//                }
//                break;
//                case SixinMessage.S_MSG_TYPE_INVITE_INTO_GROUP: {
//                    result = GlobalData.app().getString(R.string.sixin_conversation_invite_group);
//                }
//                break;
                case SixinMessage.S_MSG_TYPE_LIST: {
                    result = GlobalData.app().getString(R.string.vip_customer_question_text);
                }
                break;
//                case SixinMessage.S_MSG_TYPE_QUIT_GROUP:
//                case SixinMessage.S_MSG_TYPE_ENTER_GROUP:
//                case SixinMessage.S_MSG_TYPE_LEAVE_GROUP: {
//                    result = "[" + GlobalData.app().getString(R.string.sixin_conversation_sys_message) + "] " + result;
//                }
//                break;
                default:
                    SpannableStringBuilder spannableStringBuilder = SpanUtils.converseSechemaAsString(result);
                    result = spannableStringBuilder.toString();
            }
        } else {
            SpannableStringBuilder spannableStringBuilder = SpanUtils.converseSechemaAsString(result);
            result = spannableStringBuilder.toString();
        }

        if ((sixinMessage.getTargetType() == SixinMessage.TARGET_TYPE_VFANS || sixinMessage.getTargetType() == SixinMessage.TARGET_TYPE_GROUP) && !sixinMessage.getMsgTyppe().equals(SixinMessage.S_MSG_TYPE_QUIT_GROUP) && !sixinMessage.getMsgTyppe().equals(SixinMessage.S_MSG_TYPE_ENTER_GROUP) && !sixinMessage.getMsgTyppe().equals(SixinMessage.S_MSG_TYPE_LEAVE_GROUP)) {
            if (!sixinMessage.getIsInbound()) {
                result = GlobalData.app().getResources().getString(com.mi.live.data.R.string.me) + ": " + result;
            } else {
                result = sixinMessage.getTargetName() + ": " + result;
            }
        }
        return result;
    }

    /**
     * 获取所有需要计入未读的对话列表的未读数(只要使用在主页私信tab的未读数提示)
     * 由于feed和关注通知都整合到了私信中，故此处计数应加上前两者未读数目
     *
     * @return
     */
    public static long getAllConversationUnReadCount() {
        long uuid = MyUserInfoManager.getInstance().getUuid();
        if (uuid == 0) {
            uuid = UserAccountManager.getInstance().getUuidAsLong();
        }
        String sql = "select sum(" + ConversationDao.Properties.UnreadCount.columnName + ")  from " + ConversationDao.TABLENAME + "  where " + ConversationDao.Properties.LocaLUserId.columnName + "=" + uuid +
                " and " + ConversationDao.Properties.Target.columnName + " <> " + Conversation.UNFOCUS_CONVERSATION_TARGET +
                " and " + ConversationDao.Properties.IsNotFocus.columnName + " = 0" +
                (SHOW_VFAN_GROUP ? " " : " and  " + ConversationDao.Properties.TargetType.columnName + " <> " + SixinMessage.TARGET_TYPE_VFANS) +
                " and " + ConversationDao.Properties.IgnoreStatus.columnName + " = " + Conversation.NOT_IGNORE +
                " and " + ConversationDao.Properties.TargetType.columnName + " <> " + SixinMessage.TARGET_TYPE_GROUP +
                " and " + ConversationDao.Properties.Target.columnName + " <> " + Conversation.GROUP_NOTIFY_CONVERSATION_TARGET;

        MyLog.v(" sixincount sql " + sql);
        ConversationDao conversationDao = GreenDaoManager.getDaoSession(GlobalData.app()).getConversationDao();
        Cursor c = null;
        long count = 0;
        try {
            c = conversationDao.getDatabase().rawQuery(sql, null);
            if (c != null && c.moveToFirst()) {
                count = c.getLong(0);
            }
        } catch (Exception e) {
            MyLog.e(e);
        } finally {
            if (c != null) {
                c.close();
            }
        }
        return count;
    }

    public static List<Conversation> getConversationByTargets(List<Long> targets) {
        if (targets != null && targets.size() > 0) {
            List<Long> targetsCopy = new ArrayList<>(targets);
            int location = 0;
            List<Conversation> conversations = new ArrayList<>();
            while (location < targetsCopy.size()) {
                List<Long> ranges = targetsCopy.subList(location, (location + MAX_QUERY_NUM_COUNT) > targetsCopy.size() ? targetsCopy.size() : location + MAX_QUERY_NUM_COUNT);
                ConversationDao dao = GreenDaoManager.getDaoSession(GlobalData.app()).getConversationDao();
                QueryBuilder queryBuilder = dao.queryBuilder();
                queryBuilder.where(ConversationDao.Properties.Target.in(ranges));
                queryBuilder.build();
                conversations.addAll(queryBuilder.list());
                location = ranges.size() + location;
            }
            if (conversations != null && conversations.size() > 0) {
                MyLog.v("SixinMessageManage load by targets:" + conversations.size());
                return conversations;
            }
        }
        return null;
    }

    public static void updateConversations(List<Conversation> conversations) {
        GreenDaoManager.getDaoSession(GlobalData.app()).getConversationDao().updateInTx(conversations);
        EventBus.getDefault().post(new ConversationListUpdateEvent(conversations));
    }

    public static class ConversationInsertEvent {
        public Conversation conversation;

        public ConversationInsertEvent(Conversation conversation) {
            this.conversation = conversation;
        }
    }

    public static class ConversationUpdateEvent {
        public Conversation conversation;

        public ConversationUpdateEvent(Conversation conversation) {
            this.conversation = conversation;
        }
    }

    public static class ConversationListUpdateEvent {
        public List<Conversation> conversations;

        public ConversationListUpdateEvent(List<Conversation> conversations) {
            this.conversations = conversations;
        }
    }

    public static class NotifyUnreadCountChangeEvent {
        public long unreadCount;

        public NotifyUnreadCountChangeEvent(long unreadCount) {
            this.unreadCount = unreadCount;
        }
    }
}
