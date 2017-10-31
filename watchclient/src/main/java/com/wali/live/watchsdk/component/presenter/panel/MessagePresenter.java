package com.wali.live.watchsdk.component.presenter.panel;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.util.Pair;

import com.base.activity.BaseActivity;
import com.base.fragment.BaseFragment;
import com.base.fragment.utils.FragmentNaviUtils;
import com.base.global.GlobalData;
import com.base.log.MyLog;
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
import com.wali.live.watchsdk.recipient.RecipientsSelectFragment;
import com.wali.live.watchsdk.sixin.PopComposeMessageFragment;
import com.wali.live.watchsdk.sixin.data.ConversationLocalStore;
import com.wali.live.watchsdk.sixin.pojo.SixinTarget;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
public class MessagePresenter extends BaseSdkRxPresenter<MessagePanel.IView>
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

    private final ArrayList<ConversationAdapter.ConversationItem> mItemCacheLock = new ArrayList<>();

    private boolean isChineseLocal = CommonUtils.isChinese(); // 标记是否是中国区域
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
    protected String getTAG() {
        return TAG;
    }

    public MessagePresenter(@NonNull IEventController controller) {
        super(controller);
    }

    @Override
    public void startPresenter() {
        super.startPresenter();
        EventBus.getDefault().register(this);
    }

    @Override
    public void stopPresenter() {
        super.stopPresenter();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onBackBtnClick() {
        mController.postEvent(MSG_HIDE_BOTTOM_PANEL);
    }

    @Override
    public void onRecipientSelect() {
        if (mView != null && mView.getRealView() != null && (mView.getRealView().getContext()) instanceof BaseActivity) {
            Bundle bundle = new Bundle();
            bundle.putString(RecipientsSelectFragment.SELECT_TITLE, GlobalData.app().getResources()
                    .getString(R.string.choose_talk_title));
            bundle.putInt(RecipientsSelectFragment.SELECT_MODE,
                    RecipientsSelectFragment.SELECT_MODE_SINGLE_CLICK);
            bundle.putBoolean(RecipientsSelectFragment.INTENT_SHOW_LEVEL_SEX, false);
            bundle.putBoolean(RecipientsSelectFragment.INTENT_SHOW_BOTH_WAY, false);
            bundle.putInt(RecipientsSelectFragment.KEY_REQUEST_CODE, RecipientsSelectFragment.REQUEST_CODE_PICK_USER);
            bundle.putBoolean(BaseFragment.PARAM_FORCE_PORTRAIT, true);
            FragmentNaviUtils.addFragmentAndResetArgumentToBackStack((BaseActivity) mView.getRealView().getContext(),
                    R.id.main_act_container, RecipientsSelectFragment.class, bundle, true, R.anim.slide_right_in, 0);
        }
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
                        final ConversationAdapter.ConversationItem guard = new ConversationAdapter.ConversationItem();
                        synchronized (mItemCacheLock) {
                            mItemCacheLock.ensureCapacity(mItemCacheLock.size() + conversations.size());
                            for (Conversation elem : conversations) {
                                guard.id = elem.getId();
                                int elemIndex = mItemCacheLock.indexOf(guard);
                                if (elemIndex != -1) {
                                    mItemCacheLock.get(elemIndex).updateFrom(elem);
                                    continue;
                                }
                                int index = mNeedLocalAvatarItemId.indexOf((int) elem.getTarget());
                                int localAvatarResId = index != -1 ? mLocalAvatarResId.get(index) : 0;
                                mItemCacheLock.add(new ConversationAdapter.ConversationItem(elem, localAvatarResId));
                            }
                            Collections.sort(mItemCacheLock, mConversationComparator);
                            return (ArrayList) mItemCacheLock.clone();
                        }
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

    @Subscribe(threadMode = ThreadMode.POSTING)
    public void onEvent(final ConversationLocalStore.ConversationInsertEvent event) {
        if (event == null || event.conversation == null) {
            return;
        }
        onAddOrUpdateItem(event.conversation);
    }

    @Subscribe(threadMode = ThreadMode.POSTING)
    public void onEvent(ConversationLocalStore.ConversationUpdateEvent event) {
        onAddOrUpdateItem(event.conversation);
    }

    @Subscribe(threadMode = ThreadMode.POSTING)
    public void onEvent(ConversationLocalStore.NotifyUnreadCountChangeEvent event) {
//        onAddOrUpdateItem(event.conversation);
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

    private void onAddOrUpdateItem(final Conversation conversation) {
        MyLog.d(TAG, "onAddOrUpdateItem");
        Observable.just(0)
                .map(new Func1<Integer, Pair<Integer, ConversationAdapter.ConversationItem>>() {
                    @Override
                    public Pair<Integer, ConversationAdapter.ConversationItem> call(Integer i) {
                        final ConversationAdapter.ConversationItem guard = new ConversationAdapter.ConversationItem();
                        guard.id = conversation.getId();
                        synchronized (mItemCacheLock) {
                            int elemIndex = mItemCacheLock.indexOf(guard);
                            if (elemIndex != -1) {
                                mItemCacheLock.get(elemIndex).updateFrom(conversation);
                                return Pair.create(elemIndex, null);
                            } else {
                                int index = mNeedLocalAvatarItemId.indexOf((int) conversation.getTarget());
                                int localAvatarResId = index != -1 ? mLocalAvatarResId.get(index) : 0;
                                ConversationAdapter.ConversationItem newItem =
                                        new ConversationAdapter.ConversationItem(conversation, localAvatarResId);
                                elemIndex = 0;
                                for (ConversationAdapter.ConversationItem elem : mItemCacheLock) {
                                    if (mConversationComparator.compare(newItem, elem) != -1) {
                                        break;
                                    }
                                    ++elemIndex;
                                }
                                mItemCacheLock.add(elemIndex, newItem);
                                return Pair.create(elemIndex, newItem);
                            }
                        }
                    }
                }).subscribeOn(Schedulers.io())
                .compose(this.<Pair<Integer, ConversationAdapter.ConversationItem>>bindUntilEvent(PresenterEvent.DESTROY))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Pair<Integer, ConversationAdapter.ConversationItem>>() {
                    @Override
                    public void call(Pair<Integer, ConversationAdapter.ConversationItem> result) {
                        if (mView == null) {
                            return;
                        }
                        MyLog.d(TAG, "onAddOrUpdateItem done");
                        mView.onNewConversationUpdate(result.first, result.second);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        MyLog.e(TAG, "onAddOrUpdateItem failed, exception=" + throwable);
                    }
                });
    }

    @Override
    public boolean onEvent(int event, IParams
            params) {
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
