package com.wali.live.watchsdk.sixin.data;

import com.base.global.GlobalData;
import com.mi.live.data.account.UserAccountManager;
import com.mi.live.data.greendao.GreenDaoManager;
import com.wali.live.dao.Conversation;
import com.wali.live.dao.ConversationDao;
import com.wali.live.dao.SixinMessage;
import com.wali.live.watchsdk.R;

import org.greenrobot.eventbus.EventBus;

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
        addPermanentConversationIfNeed(conversations);
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

    public static class ConversationInsertEvent {
        public Conversation conversation;

        public ConversationInsertEvent(Conversation conversation) {
            this.conversation = conversation;
        }
    }
}
