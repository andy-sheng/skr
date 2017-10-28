package com.wali.live.watchsdk.component.presenter.panel;

import android.support.annotation.NonNull;
import android.util.Log;

import com.base.log.MyLog;
import com.thornbirds.component.IEventController;
import com.thornbirds.component.IParams;
import com.thornbirds.component.presenter.ComponentPresenter;
import com.wali.live.dao.Conversation;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.component.presenter.adapter.ConversationAdapter;
import com.wali.live.watchsdk.component.view.panel.MessagePanel;
import com.wali.live.watchsdk.sixin.data.ConversationLocalStore;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

import static com.wali.live.component.BaseSdkController.MSG_HIDE_BOTTOM_PANEL;
import static com.wali.live.dao.Conversation.GROUP_NOTIFY_CONVERSATION_TARGET;
import static com.wali.live.dao.Conversation.INTERACT_CONVERSATION_TARGET;
import static com.wali.live.dao.Conversation.UNFOCUS_CONVERSATION_TARGET;
import static com.wali.live.dao.Conversation.VFANS_NOTIFY_CONVERSATION_TARGET;

/**
 * Created by yangli on 2017/10/27.
 *
 * @module 私信面板表现
 */
public class MessagePresenter extends ComponentPresenter<MessagePanel.IView>
        implements MessagePanel.IPresenter {
    private static final String TAG = "MessagePresenter";

    public static final int TARGET_666 = 666; // VIP客服
    public static final int TARGET_777 = 777;
    public static final int TARGET_888 = 888;
    public static final int TARGET_999 = 999;
    public static final int TARGET_126 = INTERACT_CONVERSATION_TARGET; // 互动通知
    public static final int TARGET_OFFICIAL_DEFAULT = 100000; // 小米直播团队
    public static final int TARGET_OFFICIAL = TARGET_OFFICIAL_DEFAULT; // 小米直播官方

    private Subscription mSyncSubscription;

    private final List<Integer> mNeedLocalAvatarItemId = new ArrayList<>();
    private final List<Integer> mLocalAvatarResId = new ArrayList<>();

    {
        mNeedLocalAvatarItemId.add(TARGET_OFFICIAL_DEFAULT);          // 小米直播官方
        mNeedLocalAvatarItemId.add(GROUP_NOTIFY_CONVERSATION_TARGET); // 群通知
        mNeedLocalAvatarItemId.add(INTERACT_CONVERSATION_TARGET);     // 互动通知
        mNeedLocalAvatarItemId.add(TARGET_999);                       // 客服
        mNeedLocalAvatarItemId.add(UNFOCUS_CONVERSATION_TARGET);      // 陌生人
        mNeedLocalAvatarItemId.add(TARGET_666);                       // VIP客服
        mNeedLocalAvatarItemId.add(VFANS_NOTIFY_CONVERSATION_TARGET); // VIP客服

        mLocalAvatarResId.add(R.drawable.chat_list_head_official);
        mLocalAvatarResId.add(R.drawable.chat_list_head_group_notification);
        mLocalAvatarResId.add(R.drawable.chat_list_head_interactive_notification);
        mLocalAvatarResId.add(R.drawable.chat_list_head_customer_service);
        mLocalAvatarResId.add(R.drawable.chat_list_head_stranger);
        mLocalAvatarResId.add(R.drawable.chat_list_head_customer_service_vip);
        mLocalAvatarResId.add(R.drawable.chat_list_head_pet_group);
    }

    @Override
    protected String getTAG() {
        return TAG;
    }

    public MessagePresenter(@NonNull IEventController controller) {
        super(controller);
    }

    @Override
    public void onBackBtnClick() {
        mController.postEvent(MSG_HIDE_BOTTOM_PANEL);
    }

    @Override
    public void syncAllConversions() {
        if (mSyncSubscription != null && !mSyncSubscription.isUnsubscribed()) {
            mSyncSubscription.unsubscribe();
        }
        MyLog.d(TAG, "syncAllConversions");
        mSyncSubscription = Observable.just(0)
                .map(new Func1<Integer, List<Conversation>>() {
                    @Override
                    public List<Conversation> call(Integer integer) {
                        return ConversationLocalStore.getAllConversation(true);
                    }
                }).map(new Func1<List<Conversation>, List<ConversationAdapter.ConversationItem>>() {
                    @Override
                    public List<ConversationAdapter.ConversationItem> call(List<Conversation> conversations) {
                        ArrayList<ConversationAdapter.ConversationItem> itemList = new ArrayList<>();
                        itemList.ensureCapacity(conversations.size());
                        for (Conversation elem : conversations) {
                            int index = mNeedLocalAvatarItemId.indexOf((int) elem.getTarget());
                            int localAvatarResId = index != -1 ? mLocalAvatarResId.get(index) : 0;
                            itemList.add(new ConversationAdapter.ConversationItem(elem, localAvatarResId));
                        }
                        return itemList;
                    }
                }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<ConversationAdapter.ConversationItem>>() {
                    @Override
                    public void call(List<ConversationAdapter.ConversationItem> conversationItems) {
                        if (mView == null) {
                            return;
                        }
                        MyLog.d(TAG, "syncAllConversions done");
                        mView.onNewConversationList(conversationItems);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        MyLog.e(TAG, "syncAllConversions failed, exception=" + throwable);
                    }
                });
    }

    @Override
    public boolean onEvent(int event, IParams params) {
        if (mView == null) {
            Log.e(TAG, "onAction but mView is null, event=" + event);
            return false;
        }
        switch (event) {
            default:
                break;
        }
        return false;
    }
}
