package com.wali.live.watchsdk.component.presenter.panel;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.AnyThread;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.base.activity.BaseActivity;
import com.base.activity.BaseSdkActivity;
import com.base.fragment.BaseFragment;
import com.base.fragment.utils.FragmentNaviUtils;
import com.base.log.MyLog;
import com.base.thread.ThreadPool;
import com.base.utils.CommonUtils;
import com.mi.live.data.user.User;
import com.thornbirds.component.IEventController;
import com.thornbirds.component.IParams;
import com.wali.live.component.presenter.BaseSdkRxPresenter;
import com.wali.live.dao.Conversation;
import com.wali.live.dao.SixinMessage;
import com.wali.live.event.EventClass;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.component.presenter.adapter.ConversationAdapter;
import com.wali.live.watchsdk.component.view.panel.MessagePanel;
import com.wali.live.watchsdk.fans.GroupNotifyFragment;
import com.wali.live.watchsdk.recipient.RecipientsSelectFragment;
import com.wali.live.watchsdk.sixin.PopComposeMessageFragment;
import com.wali.live.watchsdk.sixin.data.ConversationLocalStore;
import com.wali.live.watchsdk.sixin.pojo.SixinTarget;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;

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
public class MessagePresenter extends BaseSdkRxPresenter<MessagePanel.IView>
        implements MessagePanel.IPresenter {
    private static final String TAG = "MessagePanelPresenter";

    public static final int TARGET_666 = 666; // VIP客服
    public static final int TARGET_777 = 777;
    public static final int TARGET_888 = 888;
    public static final int TARGET_999 = 999;
    public static final int TARGET_126 = INTERACT_CONVERSATION_TARGET; // 互动通知
    public static final int TARGET_OFFICIAL_DEFAULT = 100000; // 小米直播团队
    public static final int TARGET_OFFICIAL = TARGET_OFFICIAL_DEFAULT; // 小米直播官方

    public static final int MODE_NONE = -1;
    public static final int MODE_FOCUS = 0;
    public static final int MODE_UN_FOCUS = 1;

    private volatile ConversationPuller mFocusPuller;
    private volatile ConversationPuller mUnFocusPuller;
    private int mMode = MODE_NONE;

    private SwitchFocusInterceptor mSwitchFocusInterceptor;

    private final boolean isChineseLocal = CommonUtils.isChinese(); // 标记是否是中国区域
    /**
     * 对私信列表进行排序,要求规则是中文版本 999置顶显示, 英文版本777 置顶显示
     * 互动通知126置顶,其余的是小米直播客服、VIP优先排列在顶部
     */
    private final Comparator<ConversationAdapter.ConversationItem> mConversationComparator =
            new Comparator<ConversationAdapter.ConversationItem>() {
                @Override
                public int compare(ConversationAdapter.ConversationItem t0, ConversationAdapter.ConversationItem t1) {
                    if (t0.uid == TARGET_126) {
                        return -1;
                    } else if (t1.uid == TARGET_126) {
                        return 1;
                    }
                    if (isChineseLocal) {
                        if (t0.uid == TARGET_999) {
                            return -1;
                        } else if (t1.uid == TARGET_999) {
                            return 1;
                        }
                    } else {
                        if (t0.uid == TARGET_777) {
                            return -1;
                        } else if (t1.uid == TARGET_777) {
                            return 1;
                        }
                    }
                    if (t0.uid == TARGET_666) {
                        return -1;
                    } else if (t1.uid == TARGET_666) {
                        return 1;
                    }
                    return t0.receivedTime >= t1.receivedTime ? -1 : 1;
                }
            };

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
    protected final String getTAG() {
        return TAG;
    }

    public MessagePresenter(@NonNull IEventController controller) {
        super(controller);
    }

    @Override
    public void startPresenter() {
        super.startPresenter();
        startPresenter(MODE_FOCUS);
    }

    public void startPresenter(int mode) {
        super.startPresenter();
        EventBus.getDefault().register(this);
        if (mode == MODE_FOCUS) {
            switchToFocusMode();
        } else if (mode == MODE_UN_FOCUS) {
            switchToUnFocusMode();
        }
    }

    @Override
    public void stopPresenter() {
        super.stopPresenter();
        EventBus.getDefault().unregister(this);
        if (mFocusPuller != null) {
            mFocusPuller.reset();
        }
        if (mUnFocusPuller != null) {
            mUnFocusPuller.reset();
        }
        mMode = MODE_NONE;
    }

    @Override
    public void onBackBtnClick() {
        if (mMode == MODE_UN_FOCUS) {
            switchToFocusMode();
        } else {
            mController.postEvent(MSG_HIDE_BOTTOM_PANEL);
        }
    }

    @Override
    public void onRightBtnClick(final Context context) {
        if (mMode == MODE_FOCUS) {
            Bundle bundle = new Bundle();
            bundle.putString(RecipientsSelectFragment.SELECT_TITLE,
                    context.getString(R.string.choose_talk_title));
            bundle.putInt(RecipientsSelectFragment.SELECT_MODE,
                    RecipientsSelectFragment.SELECT_MODE_SINGLE_CLICK);
            bundle.putBoolean(RecipientsSelectFragment.INTENT_SHOW_LEVEL_SEX, false);
            bundle.putBoolean(RecipientsSelectFragment.INTENT_SHOW_BOTH_WAY, false);
            bundle.putInt(RecipientsSelectFragment.KEY_REQUEST_CODE, RecipientsSelectFragment.REQUEST_CODE_PICK_USER);
            bundle.putBoolean(BaseFragment.PARAM_FORCE_PORTRAIT, true);
            FragmentNaviUtils.addFragmentAndResetArgumentToBackStack((BaseActivity) context,
                    R.id.main_act_container, RecipientsSelectFragment.class, bundle, true, R.anim.slide_right_in, 0);
        }
    }

    private void markAsReadAsync(final long target) {
        ThreadPool.runOnPool(new Runnable() {
            @Override
            public void run() {
                ConversationLocalStore.markConversationAsRead(target, SixinMessage.TARGET_TYPE_USER);
            }
        });
    }

    @Override
    public void onConversationClick(
            @NonNull Context context, @NonNull ConversationAdapter.ConversationItem item) {
        if (item.uid == Conversation.UNFOCUS_CONVERSATION_TARGET) {
            if (item.unreadCount > 0) {
                item.unreadCount = 0;
                markAsReadAsync(item.uid);
            }
            if (mSwitchFocusInterceptor != null) {
                mSwitchFocusInterceptor.switchToUnFocus();
            } else {
                switchToUnFocusMode();
            }
        } else if (item.uid == Conversation.VFANS_NOTIFY_CONVERSATION_TARGET) {
            GroupNotifyFragment.openFragment((BaseActivity) context);
        } else {
            PopComposeMessageFragment.open((BaseActivity) context, item.getSixinTarget(), true);
        }
    }

    private void switchToFocusMode() {
        if (mMode == MODE_FOCUS) {
            return;
        }
        MyLog.w(TAG, "switchToFocusMode");
        mMode = MODE_FOCUS;
        mView.onEnterFocusMode();
        if (mFocusPuller == null) {
            mFocusPuller = new FocusConversationPuller();
        }
        mFocusPuller.syncAllConversions();
    }

    private void switchToUnFocusMode() {
        if (mMode == MODE_UN_FOCUS) {
            return;
        }
        MyLog.w(TAG, "switchToUnFocusMode");
        mMode = MODE_UN_FOCUS;
        mView.onEnterUnFocusMode();
        if (mUnFocusPuller == null) {
            mUnFocusPuller = new UnFocusConversationPuller();
        }
        mUnFocusPuller.syncAllConversions();
    }

    @Subscribe(threadMode = ThreadMode.POSTING)
    public void onEvent(final ConversationLocalStore.ConversationInsertEvent event) {
        if (event == null || event.conversation == null) {
            return;
        }
        updateFromSingleItem(event.conversation);
    }

    @Subscribe(threadMode = ThreadMode.POSTING)
    public void onEvent(ConversationLocalStore.ConversationUpdateEvent event) {
        if (event == null || event.conversation == null) {
            return;
        }
        updateFromSingleItem(event.conversation);
    }

    @AnyThread
    private final void updateFromSingleItem(@NonNull final Conversation conversation) {
        updateFromList(Arrays.asList(conversation));
    }

    @AnyThread
    private void updateFromList(@NonNull final List<Conversation> conversations) {
        final ConversationPuller unFocusPuller = mUnFocusPuller;
        if (unFocusPuller != null) {
            unFocusPuller.updateFromList(conversations);
        }
        final ConversationPuller focusPuller = mFocusPuller;
        if (focusPuller != null) {
            focusPuller.updateFromList(conversations);
        }
    }

    @Subscribe(threadMode = ThreadMode.POSTING)
    public void onEvent(ConversationLocalStore.ConversationListUpdateEvent event) {
        if (event == null || event.conversations == null || event.conversations.isEmpty()) {
            return;
        }
        updateFromList(event.conversations);
    }

    @Subscribe(threadMode = ThreadMode.POSTING)
    public void onEvent(ConversationLocalStore.ConversationBulkDeleteByTargetEvent event) {
        if (event == null || event.mTargets == null || event.mTargets.isEmpty()) {
            return;
        }
        final ConversationPuller focusPuller = mFocusPuller;
        if (focusPuller != null) {
            focusPuller.deleteFromTargetIdList(event.mTargets);
        }
        final ConversationPuller unFocusPuller = mUnFocusPuller;
        if (unFocusPuller != null) {
            unFocusPuller.deleteFromTargetIdList(event.mTargets);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(EventClass.OnActivityResultEvent event) {
        switch (event.requestCode) {
            case RecipientsSelectFragment.REQUEST_CODE_PICK_USER:
                Intent intent = event.data;
                if (intent != null && mView != null && mView.getRealView() != null
                        && (mView.getRealView().getContext()) instanceof BaseActivity) {
                    User user = (User) intent.getSerializableExtra(RecipientsSelectFragment.RESULT_SINGLE_OBJECT);
                    PopComposeMessageFragment.open((BaseActivity) mView.getRealView().getContext(),
                            new SixinTarget(user, SixinMessage.MSG_STATUE_BOTHFOUCS, 0), true);
                }
                break;
            default:
                break;
        }
    }

    @Override
    public final boolean onEvent(int event, IParams params) {
        return false;
    }

    private abstract class ConversationPuller {
        protected final String TAG = getTAG();

        private final ArrayList<ConversationAdapter.ConversationItem> mConversationCacheLock =
                new ArrayList<>();
        private final int mActiveMode;

        private boolean mNeedSyncAll = true;
        private Subscription mSyncSubscription;

        protected abstract String getTAG();

        public final void reset() {
            mNeedSyncAll = true;
        }

        public ConversationPuller(int mode) {
            mActiveMode = mode;
        }

        protected abstract List<Conversation> onQueryFromLocalStore();

        protected final void onSyncAllDone(
                @NonNull List<ConversationAdapter.ConversationItem> conversationList) {
            if (mView == null || mActiveMode != mMode) {
                return;
            }
            mView.onNewConversationList(conversationList);
        }

        @MainThread
        public final void syncAllConversions() {
            if ((mSyncSubscription != null && !mSyncSubscription.isUnsubscribed())) {
                return;
            }
            MyLog.d(TAG, "syncAllConversions");
            final boolean needSyncAll = mNeedSyncAll;
            mSyncSubscription = Observable.just(0)
                    .delay(100, TimeUnit.MILLISECONDS)
                    .map(new Func1<Integer, List<Conversation>>() {
                        @Override
                        public List<Conversation> call(Integer integer) {
                            if (needSyncAll) {
                                synchronized (mConversationCacheLock) {
                                    mConversationCacheLock.clear();
                                }
                                return onQueryFromLocalStore();
                            } else {
                                return new ArrayList<>();
                            }
                        }
                    }).map(new Func1<List<Conversation>, List<ConversationAdapter.ConversationItem>>() {
                        @Override
                        public List<ConversationAdapter.ConversationItem> call(List<Conversation> conversations) {
                            final ConversationAdapter.ConversationItem guard = new ConversationAdapter.ConversationItem();
                            synchronized (mConversationCacheLock) {
                                mConversationCacheLock.ensureCapacity(mConversationCacheLock.size() + conversations.size());
                                for (Conversation conversation : conversations) {
                                    if (conversation.getTarget() == TARGET_126) { // 不显示互动通知  TODO-YangLi 后续需要时再加
                                        continue;
                                    }
                                    guard.uid = conversation.getTarget();
                                    int elemIndex = mConversationCacheLock.indexOf(guard);
                                    if (elemIndex != -1) {
                                        mConversationCacheLock.get(elemIndex).updateFrom(conversation);
                                        continue;
                                    }
                                    int index = mNeedLocalAvatarItemId.indexOf((int) conversation.getTarget());
                                    int localAvatarResId = index != -1 ? mLocalAvatarResId.get(index) : 0;
                                    mConversationCacheLock.add(new ConversationAdapter.ConversationItem(conversation, localAvatarResId));
                                }
                                Collections.sort(mConversationCacheLock, mConversationComparator);
                                return (ArrayList) mConversationCacheLock.clone();
                            }
                        }
                    }).subscribeOn(Schedulers.io())
                    .compose(MessagePresenter.this.<List<ConversationAdapter.ConversationItem>>
                            bindUntilEvent(PresenterEvent.STOP))
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Action1<List<ConversationAdapter.ConversationItem>>() {
                        @Override
                        public void call(List<ConversationAdapter.ConversationItem> conversationItems) {
                            MyLog.d(TAG, "syncAllConversions done");
                            mNeedSyncAll = false;
                            onSyncAllDone(conversationItems);
                        }
                    }, new Action1<Throwable>() {
                        @Override
                        public void call(Throwable throwable) {
                            MyLog.e(TAG, "syncAllConversions failed, exception=" + throwable);
                        }
                    });
        }

        private void insertNewItemLocked(@NonNull Conversation conversation) {
            int index = mNeedLocalAvatarItemId.indexOf((int) conversation.getTarget());
            int localAvatarResId = index != -1 ? mLocalAvatarResId.get(index) : 0;
            ConversationAdapter.ConversationItem newItem =
                    new ConversationAdapter.ConversationItem(conversation, localAvatarResId);
            int elemIndex = 0;
            for (ConversationAdapter.ConversationItem elem : mConversationCacheLock) {
                if (mConversationComparator.compare(newItem, elem) != 1) {
                    break;
                }
                ++elemIndex;
            }
            mConversationCacheLock.add(elemIndex, newItem);
        }

        protected abstract boolean canBelongToThisCache(@NonNull Conversation conversation);

        protected final void onUpdateFromListDone(
                @Nullable List<ConversationAdapter.ConversationItem> conversationList) {
            if (mView == null || mActiveMode != mMode || conversationList == null) {
                return;
            }
            mView.onNewConversationList(conversationList);
        }

        @AnyThread
        public final void updateFromList(@NonNull final List<Conversation> conversations) {
            MyLog.d(TAG, "updateFromList");
            Observable.just(0)
                    .map(new Func1<Integer, List<ConversationAdapter.ConversationItem>>() {
                        @Override
                        public List<ConversationAdapter.ConversationItem> call(Integer i) {
                            final ConversationAdapter.ConversationItem guard = new ConversationAdapter.ConversationItem();
                            boolean needUpdate = false;
                            synchronized (mConversationCacheLock) {
                                for (Conversation conversation : conversations) {
                                    if (conversation.getTarget() == TARGET_126) { // 不显示互动通知  TODO-YangLi 后续需要时再加
                                        continue;
                                    }
                                    guard.uid = conversation.getTarget();
                                    int elemIndex = mConversationCacheLock.indexOf(guard);
                                    final boolean belongToThisCache = canBelongToThisCache(conversation);
                                    MyLog.d(TAG, "updateFromList uid=" + guard.uid + ", belongToThisCache=" + belongToThisCache);
                                    if (elemIndex != -1) {
                                        // Cache中有，若类型与Cache类型不冲突，则更新，否则从Cache中移除此元素
                                        // 关注状态的变化，会导致一个回话在关注列表和未关注列表之间转移
                                        if (belongToThisCache) {
                                            mConversationCacheLock.get(elemIndex).updateFrom(conversation);
                                        } else {
                                            mConversationCacheLock.remove(elemIndex);
                                        }
                                        needUpdate = true;
                                    } else if (belongToThisCache) {
                                        // Cache中没有，且类型与Cache类型不冲突，则加入此元素
                                        insertNewItemLocked(conversation);
                                        needUpdate = true;
                                    }
                                }
                                return needUpdate ? (List) mConversationCacheLock.clone() : null;
                            }
                        }
                    }).subscribeOn(Schedulers.io())
                    .compose(MessagePresenter.this.<List<ConversationAdapter.ConversationItem>>bindUntilEvent(PresenterEvent.STOP))
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Action1<List<ConversationAdapter.ConversationItem>>() {
                        @Override
                        public void call(List<ConversationAdapter.ConversationItem> conversationList) {
                            MyLog.d(TAG, "updateFromList done");
                            onUpdateFromListDone(conversationList);
                        }
                    }, new Action1<Throwable>() {
                        @Override
                        public void call(Throwable throwable) {
                            MyLog.e(TAG, "updateFromList failed, exception=" + throwable);
                        }
                    });
        }

        @AnyThread
        public final void deleteFromTargetIdList(@NonNull final List<Long> targetIds) {
            MyLog.d(TAG, "deleteFromIdList");
            Observable.just(0)
                    .map(new Func1<Integer, List<ConversationAdapter.ConversationItem>>() {
                        @Override
                        public List<ConversationAdapter.ConversationItem> call(Integer i) {
                            final ConversationAdapter.ConversationItem guard = new ConversationAdapter.ConversationItem();
                            boolean needUpdate = false;
                            synchronized (mConversationCacheLock) {
                                for (Long uid : targetIds) {
                                    guard.uid = uid;
                                    int elemIndex = mConversationCacheLock.indexOf(guard);
                                    if (elemIndex != -1) {
                                        // Cache中有，则从Cache中移除此元素
                                        mConversationCacheLock.remove(elemIndex);
                                        needUpdate = true;
                                    }
                                }
                                return needUpdate ? (List) mConversationCacheLock.clone() : null;
                            }
                        }
                    }).subscribeOn(Schedulers.io())
                    .compose(MessagePresenter.this.<List<ConversationAdapter.ConversationItem>>bindUntilEvent(PresenterEvent.STOP))
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Action1<List<ConversationAdapter.ConversationItem>>() {
                        @Override
                        public void call(List<ConversationAdapter.ConversationItem> conversationList) {
                            MyLog.d(TAG, "deleteFromIdList done");
                            onUpdateFromListDone(conversationList);
                        }
                    }, new Action1<Throwable>() {
                        @Override
                        public void call(Throwable throwable) {
                            MyLog.e(TAG, "deleteFromIdList failed, exception=" + throwable);
                        }
                    });
        }
    }

    private class FocusConversationPuller extends ConversationPuller {

        @Override
        protected final String getTAG() {
            return MessagePresenter.TAG + "-FocusConversationPuller";
        }

        public FocusConversationPuller() {
            super(MODE_FOCUS);
        }

        @Override
        protected final boolean canBelongToThisCache(@NonNull Conversation conversation) {
            return !conversation.getIsNotFocus();
        }

        @Override
        protected final List<Conversation> onQueryFromLocalStore() {
            return ConversationLocalStore.getAllConversation(true);
        }
    }

    private class UnFocusConversationPuller extends ConversationPuller {

        @Override
        protected final String getTAG() {
            return MessagePresenter.TAG + "-UnFocusConversationPuller";
        }

        public UnFocusConversationPuller() {
            super(MODE_UN_FOCUS);
        }

        @Override
        protected final boolean canBelongToThisCache(@NonNull Conversation conversation) {
            return conversation.getIsNotFocus();
        }

        @Override
        protected final List<Conversation> onQueryFromLocalStore() {
            return ConversationLocalStore.getAllConversation(false);
        }
    }

    public void setSwitchFocusInterceptor(SwitchFocusInterceptor switchFocusInterceptor) {
        mSwitchFocusInterceptor = switchFocusInterceptor;
    }

    public interface SwitchFocusInterceptor {
        void switchToUnFocus();
    }
}
