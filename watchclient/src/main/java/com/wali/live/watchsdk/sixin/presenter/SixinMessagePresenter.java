package com.wali.live.watchsdk.sixin.presenter;

import com.base.log.MyLog;
import com.base.mvp.BaseRxPresenter;
import com.mi.live.data.milink.MiLinkClientAdapter;
import com.wali.live.dao.Conversation;
import com.wali.live.dao.SixinMessage;
import com.wali.live.watchsdk.sixin.cache.SendingMessageCache;
import com.wali.live.watchsdk.sixin.data.ConversationLocalStore;
import com.wali.live.watchsdk.sixin.data.SixinMessageCloudStore;
import com.wali.live.watchsdk.sixin.data.SixinMessageLocalStore;
import com.wali.live.watchsdk.sixin.message.SixinMessageModel;
import com.wali.live.watchsdk.sixin.pojo.SixinTarget;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by lan on 2017/10/29.
 */
public class SixinMessagePresenter extends BaseRxPresenter<ISixinMessageView> {
    public final static int PAGE_MESSAGE_COUNT = 10; //分页加载十个消息

    private SixinTarget mSixinTarget;
    private SixinMessageCloudStore mSixinMessageCloudStore;

    public SixinMessagePresenter(ISixinMessageView view, SixinTarget sixinTarget) {
        super(view);
        mSixinTarget = sixinTarget;
        mSixinMessageCloudStore = new SixinMessageCloudStore();
    }

    public void firstLoadDataFromDB() {
        Observable
                .create(new Observable.OnSubscribe<List<SixinMessageModel>>() {
                    @Override
                    public void call(Subscriber<? super List<SixinMessageModel>> subscriber) {
                        List<SixinMessage> messageList = SixinMessageLocalStore.getSixinMessagesByUUid(mSixinTarget.getUid(), PAGE_MESSAGE_COUNT, Long.MAX_VALUE, true, mSixinTarget.getTargetType());
                        if (messageList == null) {
                            subscriber.onError(new Exception("messageList is null"));
                        }
                        MyLog.d(TAG, "messageList size=" + messageList);
                        List<SixinMessageModel> messageModelList = new ArrayList<>();
                        for (SixinMessage sixinMessage : messageList) {
                            SixinMessageModel item = new SixinMessageModel(sixinMessage);
                            messageModelList.add(item);
                        }
                        Collections.sort(messageModelList);
                        subscriber.onNext(messageModelList);
                        subscriber.onCompleted();

                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(mView.<List<SixinMessageModel>>bindUntilEvent())
                .subscribe(new Action1<List<SixinMessageModel>>() {
                    @Override
                    public void call(List<SixinMessageModel> messageModelList) {
                        if (messageModelList != null) {
                            mView.loadData(messageModelList);
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        MyLog.e(TAG, throwable);
                    }
                });
    }

    public void sendMessage(final String message) {
        Observable
                .just(message)
                .observeOn(Schedulers.io())
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String s) {
                        SixinMessage sixinMessage = SixinMessageLocalStore.getTextSixinMessageAndNotInsertToDB(
                                mSixinTarget.getNickname(), mSixinTarget.getUid(), mSixinTarget.getTargetType(), message,
                                mSixinTarget.getFocusState(), mSixinTarget.getCertificationType());
                        SixinMessageLocalStore.insertSixinMessage(sixinMessage);

                        if (!sixinMessage.getIsInbound() && MiLinkClientAdapter.getsInstance().isMiLinkLogined()) {
                            SendingMessageCache.put(sixinMessage.getId(), System.currentTimeMillis());
                        }

                        mSixinMessageCloudStore.send(sixinMessage);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        MyLog.e(TAG, throwable);
                    }
                });
    }

    public void notifyMessage(final List<SixinMessage> messageList) {
        Observable
                .create(new Observable.OnSubscribe<List<SixinMessageModel>>() {
                    @Override
                    public void call(Subscriber<? super List<SixinMessageModel>> subscriber) {
                        List<SixinMessageModel> messageModelList = new ArrayList<>();
                        for (SixinMessage sixinMessage : messageList) {
                            if (sixinMessage.getTarget() == mSixinTarget.getUid()) {
                                messageModelList.add(new SixinMessageModel(sixinMessage));
                            }
                        }
                        subscriber.onNext(messageModelList);
                        subscriber.onCompleted();
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(mView.<List<SixinMessageModel>>bindUntilEvent())
                .subscribe(new Subscriber<List<SixinMessageModel>>() {
                    @Override
                    public void onCompleted() {
                        markConversationAsRead();
                    }

                    @Override
                    public void onError(Throwable e) {
                        MyLog.e(TAG, e);
                    }

                    @Override
                    public void onNext(List<SixinMessageModel> messageModelList) {
                        MyLog.d(TAG, "notifyMessage size=" + messageModelList.size());
                        if (messageModelList != null) {
                            mView.addData(messageModelList);
                        }
                    }
                });
    }

    public void pullOldMessage(final SixinMessageModel firstMessageModel) {
        Observable
                .create(new Observable.OnSubscribe<List<SixinMessageModel>>() {
                    @Override
                    public void call(Subscriber<? super List<SixinMessageModel>> subscriber) {
                        List<SixinMessage> messageList = SixinMessageLocalStore.getSixinMessagesByUUid(mSixinTarget.getUid(), PAGE_MESSAGE_COUNT,
                                firstMessageModel.getReceiveTime(), true, mSixinTarget.getTargetType());
                        if (messageList == null) {
                            subscriber.onError(new Exception("pullOldMessage messageList is null"));
                        }
                        MyLog.d(TAG, "pullOldMessage messageList size=" + messageList);
                        List<SixinMessageModel> messageModelList = new ArrayList<>();
                        for (SixinMessage sixinMessage : messageList) {
                            SixinMessageModel item = new SixinMessageModel(sixinMessage);
                            messageModelList.add(item);
                        }
                        subscriber.onNext(messageModelList);
                        subscriber.onCompleted();
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(mView.<List<SixinMessageModel>>bindUntilEvent())
                .subscribe(new Action1<List<SixinMessageModel>>() {
                    @Override
                    public void call(List<SixinMessageModel> messageModelList) {
                        MyLog.d(TAG, "pullOldMessage size=" + messageModelList.size());
                        mView.stopRefreshing();
                        if (messageModelList != null) {
                            mView.addOldData(messageModelList);
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        MyLog.d(TAG, throwable);
                        mView.stopRefreshing();
                    }
                });
    }

    public void markConversationAsRead() {
        Observable
                .just(mSixinTarget.getUid())
                .observeOn(Schedulers.io())
                .subscribeOn(Schedulers.io())
                .subscribe(new Action1<Long>() {
                    @Override
                    public void call(Long aLong) {
                        Conversation conversation = ConversationLocalStore.getConversationByTarget(mSixinTarget.getUid(), mSixinTarget.getTargetType());
                        if (conversation == null) {
                            return;
                        }

                        ConversationLocalStore.markConversationAsRead(mSixinTarget.getUid(), mSixinTarget.getTargetType());
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        MyLog.e(throwable);
                    }
                });
    }
}
