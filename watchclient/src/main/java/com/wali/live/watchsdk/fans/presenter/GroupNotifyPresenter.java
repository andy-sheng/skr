package com.wali.live.watchsdk.fans.presenter;

import android.text.TextUtils;

import com.base.log.MyLog;
import com.base.mvp.BaseRxPresenter;
import com.base.mvp.IRxView;
import com.base.utils.toast.ToastUtils;
import com.mi.live.data.account.UserAccountManager;
import com.mi.live.data.api.ErrorCode;
import com.wali.live.dao.Conversation;
import com.wali.live.dao.SixinMessage;
import com.wali.live.proto.VFansCommonProto;
import com.wali.live.proto.VFansProto;
import com.wali.live.watchsdk.fans.model.notification.GroupNotifyBaseModel;
import com.wali.live.watchsdk.fans.push.GroupNotifyLocalStore;
import com.wali.live.watchsdk.fans.push.data.FansNotifyRepository;
import com.wali.live.watchsdk.fans.push.event.GroupNotifyUpdateEvent;
import com.wali.live.watchsdk.fans.push.type.GroupNotifyType;
import com.wali.live.watchsdk.sixin.data.ConversationLocalStore;

import org.greenrobot.eventbus.EventBus;

import java.util.Iterator;
import java.util.List;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by zyh on 2017/11/22.
 */

public class GroupNotifyPresenter extends BaseRxPresenter<GroupNotifyPresenter.IView> {

    public GroupNotifyPresenter(GroupNotifyPresenter.IView view) {
        super(view);
    }

    public void syncFansNotify() {
        Observable.just(0)
                .map(new Func1<Integer, Object>() {
                    @Override
                    public Object call(Integer integer) {
                        FansNotifyRepository.syncFansNotify();
                        return null;
                    }
                }).compose(mView.bindLifecycle())
                .subscribeOn(Schedulers.io())
                .subscribe();
    }

    public void loadDataFromDB() {
        Observable.just(0)
                .map(new Func1<Integer, List<GroupNotifyBaseModel>>() {
                    @Override
                    public List<GroupNotifyBaseModel> call(Integer integer) {
                        GroupNotifyUpdateEvent event = GroupNotifyLocalStore.getInstance().
                                getGroupNotifyBaseModelListEventFromDB();
                        List<GroupNotifyBaseModel> list = event.allGroupNotifyList;
                        if (list != null && !list.isEmpty()) {
                            Iterator<GroupNotifyBaseModel> iterator = list.iterator();
                            while (iterator.hasNext()) {
                                GroupNotifyBaseModel item = iterator.next();
                                if (item.getNotificationType() == GroupNotifyType.BE_GROUP_MEM_NOTIFY) {
                                    iterator.remove();
                                }
                            }
                        }
                        return list;
                    }
                }).subscribeOn(Schedulers.io())
                .compose(mView.<List<GroupNotifyBaseModel>>bindLifecycle())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<GroupNotifyBaseModel>>() {
                    @Override
                    public void call(List<GroupNotifyBaseModel> models) {
                        if (mView != null) {
                            mView.setGroupNotifyData(models);
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        MyLog.e(TAG, throwable);
                    }
                });
    }

    public void handleJoinGroup(final GroupNotifyBaseModel model) {
        Observable.just(0)
                .map(new Func1<Integer, VFansProto.HandleJoinGroupRsp>() {
                    @Override
                    public VFansProto.HandleJoinGroupRsp call(Integer integer) {
                        return FansNotifyRepository.handleJoinGroup(model.getGroupId(),
                                UserAccountManager.getInstance().getUuidAsLong(),
                                model.getCandidate(), VFansCommonProto.ApplyJoinResult.PASS, false);
                    }
                }).subscribeOn(Schedulers.io())
                .compose(mView.<VFansProto.HandleJoinGroupRsp>bindLifecycle())
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<VFansProto.HandleJoinGroupRsp>() {
                    @Override
                    public void call(VFansProto.HandleJoinGroupRsp rsp) {
                        if (rsp != null) {
                            if (rsp.getErrCode() == ErrorCode.CODE_SUCCESS && mView != null) {
                                if (GroupNotifyLocalStore.getInstance().deleteAllApplyJoinAndInsert(model.getCandidate(),
                                        model.getGroupId(), VFansCommonProto.ApplyJoinResult.PASS)) {
                                    //TODO 先简单点，一发现有数据更新，抛出事件，这个事件带着所有通知。
                                    GroupNotifyUpdateEvent event = GroupNotifyLocalStore.getInstance()
                                            .getGroupNotifyBaseModelListEventFromDB();
                                    EventBus.getDefault().post(event);
                                }
                            } else if (rsp.getErrCode() == ErrorCode.CODE_HANDEL_JOIN_NOTIFY) {
                                //已经处理过的消息直接删除
                                GroupNotifyLocalStore.getInstance().delete(model);
                            } else if (!TextUtils.isEmpty(rsp.getErrMsg())) {
                                ToastUtils.showToast(rsp.getErrMsg());
                            }
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        MyLog.e(throwable);
                    }
                });
    }

    public void clearAllGroupNotify() {
        Observable.just(0).map(new Func1<Integer, Object>() {
            @Override
            public Object call(Integer integer) {
                GroupNotifyLocalStore.getInstance().deleteAllGroupNotify();
                return null;
            }
        }).subscribeOn(Schedulers.io())
                .compose(mView.bindLifecycle())
                .subscribe();
    }

    public static void markConversationAsRead() {
        Observable.just("").observeOn(Schedulers.io()).subscribe(new Action1<String>() {
            @Override
            public void call(String s) {
                ConversationLocalStore.markConversationAsRead(Conversation.VFANS_NOTIFY_CONVERSATION_TARGET,
                        SixinMessage.TARGET_TYPE_USER);
            }
        });
    }

    public interface IView extends IRxView {
        void setGroupNotifyData(List<GroupNotifyBaseModel> models);
    }
}
