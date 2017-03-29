package com.wali.live.watchsdk.component.presenter;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.base.log.MyLog;
import com.base.utils.Constants;
import com.base.utils.rx.RxRetryAssist;
import com.mi.live.data.api.LiveManager;
import com.mi.live.data.milink.MiLinkClientAdapter;
import com.mi.live.data.milink.command.MiLinkCommand;
import com.mi.live.data.push.IPushMsgProcessor;
import com.mi.live.data.push.model.BarrageMsg;
import com.mi.live.data.push.model.BarrageMsgType;
import com.mi.live.data.room.model.RoomBaseDataModel;
import com.mi.milink.sdk.aidl.PacketData;
import com.wali.live.component.ComponentController;
import com.wali.live.component.presenter.ComponentPresenter;
import com.wali.live.proto.LiveCommonProto;
import com.wali.live.proto.LiveMessageProto;
import com.wali.live.proto.LiveProto;
import com.wali.live.watchsdk.component.view.WidgetView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by chenyong on 2017/03/24.
 * <p>
 * Generated using create_component_view.py
 *
 * @module 运营位操作类
 */
public class WidgetPresenter extends ComponentPresenter<WidgetView.IView>
        implements WidgetView.IPresenter, IPushMsgProcessor {

    private static final int FROM_ALL = 0;
    private static final int FROM_WATCH = 1;
    private static final int FROM_LIVE = 2;

    private static final String TAG = "WidgetPresenter";

    protected RoomBaseDataModel mMyRoomData;
    private Handler mUIHandler;
    private boolean mIsAnchor;
    private long mAttachmentStamp = 0;
    private Subscription mWidgetSubscription;

    // TODO 改成不是proto的数据格式
    private List<LiveCommonProto.NewWidgetItem> mWidgetList = new ArrayList();

    public WidgetPresenter(@NonNull IComponentController componentController,
                           @NonNull RoomBaseDataModel myRoomData,
                           boolean isAnchor) {
        super(componentController);
        registerAction(ComponentController.MSG_ON_LIVE_SUCCESS);
        mMyRoomData = myRoomData;
        mUIHandler = new Handler(Looper.getMainLooper());
        mIsAnchor = isAnchor;
    }

    /**
     * 设置运营位数据
     */
    @MainThread
    public void setWidgetList(List<LiveCommonProto.NewWidgetItem> list) {
        if (list.size() <= 0) {
            return;
        }
        if (mWidgetList.containsAll(list) && mWidgetList.size() == list.size()) {
            return;
        }

        if (mWidgetList.size() > 0) {
            mWidgetList.clear();
        }
        mWidgetList.addAll(list);

        hideWidget();
        if (mWidgetList != null && mWidgetList.size() > 0) {
            mView.showWidgetView(mWidgetList);
        }
    }

    // 隐藏运营位数据
    public void hideWidget() {
        mView.hideWidgetView();
    }

    public void destroy() {
        super.destroy();
        mView.destroyView();
        if (mUIHandler != null) {
            mUIHandler.removeCallbacksAndMessages(null);
            mUIHandler = null;
        }
    }

    @Override
    protected IAction createAction() {
        return new Action();
    }

    @Override
    public long getUid() {
        return mMyRoomData.getUid();
    }

    @Override
    public void process(BarrageMsg msg, RoomBaseDataModel roomBaseDataModel) {
        if (msg.getMsgType() == BarrageMsgType.B_MSG_TYPE_ATTACHMENT) {
            long attachmentStamp = msg.getSenderMsgId();
            if (mAttachmentStamp < attachmentStamp) {
                mAttachmentStamp = attachmentStamp;
                final BarrageMsg.attachMessageExt msgExt = (BarrageMsg.attachMessageExt) msg.getMsgExt();
                final List<LiveCommonProto.NewWidgetItem> widgetList = new ArrayList<>();
                for (LiveMessageProto.NewWidgetMessageItem newWidgetMessageItem : msgExt.newWidgetList) {
                    if (!newWidgetMessageItem.getIsDelete()) {
                        int showType = newWidgetMessageItem.getShowType();
                        if (showType == FROM_ALL || (mIsAnchor && showType == FROM_LIVE) || (!mIsAnchor && showType == FROM_WATCH)) {
                            widgetList.add(newWidgetMessageItem.getNewWidgetItem());
                        }
                    }
                }
                mUIHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        setWidgetList(widgetList);
                    }
                });
            }
        }
    }

    private void getRoomAttachment() {
        Observable.create(new Observable.OnSubscribe<Object>() {
            @Override
            public void call(Subscriber<? super Object> subscriber) {
                LiveProto.GetRoomAttachmentReq req = LiveProto.GetRoomAttachmentReq
                        .newBuilder()
                        .setIsGetWidget(true)
                        .setLiveid(mMyRoomData.getRoomId())
                        .setZuid(mMyRoomData.getUid())
                        .setIsGetAnimation(true)
                        .setRoomType(mMyRoomData.getLiveType())
                        .setIsGetIconConfig(false)
                        .build();
                PacketData data = new PacketData();
                data.setCommand(MiLinkCommand.COMMAND_ROOM_ATTACHMENT);
                data.setData(req.toByteArray());
                data.setNeedCached(true);
                MyLog.w(TAG, "getRoomAttachment request:" + req.toString());
                try {
                    PacketData response = MiLinkClientAdapter.getsInstance().sendSync(data, 10 * 1000);
                    if (response != null && response.getData() != null) {
                        LiveProto.GetRoomAttachmentRsp rsp = LiveProto.GetRoomAttachmentRsp.parseFrom(response.getData());
                        MyLog.w(TAG, "getRoomAttachment response:" + rsp);
                        if (rsp != null && rsp.getRetCode() == 0) {
                            subscriber.onNext(rsp);
                            subscriber.onCompleted();
                        } else {
                            subscriber.onError(new Throwable("getRoomAttachment retCode != 0"));
                        }
                    } else {
                        subscriber.onError(new Throwable("getRoomAttachment response is null "));
                    }
                } catch (Exception e) {
                    subscriber.onError(e);
                }
            }
        }).compose(this.bindUntilEvent(PresenterEvent.DESTROY))
                .retryWhen(new RxRetryAssist(3, 5, true)) // 重试3次，间隔5秒
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Object>() {
                    @Override
                    public void call(Object o) {
                        LiveProto.GetRoomAttachmentRsp rsp = (LiveProto.GetRoomAttachmentRsp) o;
                        setWidgetList(rsp.getNewWidgetInfo().getWidgetItemList());
                        if (rsp.getNewWidgetInfo().hasPullInterval()) {
                            getAttachmentDelay(rsp.getNewWidgetInfo().getPullInterval());
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        MyLog.e(TAG, throwable);
                    }
                });
    }

    private void getAttachmentDelay(int time) {
        if (mWidgetSubscription != null) {
            mWidgetSubscription.unsubscribe();
        }
        mWidgetSubscription = Observable.timer(time, TimeUnit.SECONDS).compose(this.bindUntilEvent(PresenterEvent.DESTROY))
                .subscribe(new Action1<Object>() {
                    @Override
                    public void call(Object o) {
                        Observable.create((new Observable.OnSubscribe<Object>() {
                            @Override
                            public void call(Subscriber<? super Object> subscriber) {
                                LiveProto.GetRoomWidgetReq req = LiveProto.GetRoomWidgetReq
                                        .newBuilder()
                                        .setLiveid(mMyRoomData.getRoomId())
                                        .setZuid(mMyRoomData.getUid())
                                        .setRoomType(mMyRoomData.getLiveType())
                                        .build();
                                PacketData data = new PacketData();
                                data.setCommand(MiLinkCommand.COMMAND_ROOM_WIDGET);
                                data.setData(req.toByteArray());
                                data.setNeedCached(true);
                                MyLog.w(TAG, "getRoomWidget request:" + req.toString());
                                try {
                                    PacketData response = MiLinkClientAdapter.getsInstance().sendSync(data, 10 * 1000);
                                    LiveProto.GetRoomWidgetRsp rsp = LiveProto.GetRoomWidgetRsp.parseFrom(response.getData());
                                    MyLog.w(TAG, "getRoomWidget response:" + rsp);
                                    if (rsp != null && rsp.getRetCode() == 0) {
                                        subscriber.onNext(rsp);
                                        subscriber.onCompleted();
                                    } else {
                                        subscriber.onError(new Throwable("getRoomWidget retCode != 0"));
                                    }
                                } catch (Exception e) {
                                    subscriber.onError(e);
                                }
                            }
                        })).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Action1<Object>() {
                            @Override
                            public void call(Object o) {
                                LiveProto.GetRoomWidgetRsp rsp = (LiveProto.GetRoomWidgetRsp) o;
                                if (rsp.getNewWidgetInfo().hasPullInterval() && rsp.getNewWidgetInfo().getPullInterval() > 0) {
                                    if (mAttachmentStamp < rsp.getTimestamp()) {
                                        mAttachmentStamp = rsp.getTimestamp();
                                        setWidgetList(rsp.getNewWidgetInfo().getWidgetItemList());
                                        getAttachmentDelay(rsp.getNewWidgetInfo().getPullInterval());
                                    }
                                }
                            }
                        }, new Action1<Throwable>() {
                            @Override
                            public void call(Throwable throwable) {
                                MyLog.e(TAG, throwable);
                            }
                        });
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        MyLog.e(TAG, throwable);
                    }
                });
    }

    @Override
    public int[] getAcceptMsgType() {
        return new int[]{
                BarrageMsgType.B_MSG_TYPE_ATTACHMENT
        };
    }

    public class Action implements IAction {
        @Override
        public boolean onAction(int source, @Nullable Params params) {
            if (mView == null) {
                MyLog.e(TAG, "onAction but mView is null, source=" + source);
                return false;
            }
            switch (source) {
                case ComponentController.MSG_ON_LIVE_SUCCESS:
                    if (!Constants.isGooglePlayBuild && !Constants.isIndiaBuild) {
                        int liveType = mMyRoomData.getLiveType();
                        if (liveType != LiveManager.TYPE_LIVE_PRIVATE && liveType != LiveManager.TYPE_LIVE_TOKEN) {
                            getRoomAttachment();
                        }
                    }
                    break;
                default:
                    break;
            }
            return false;
        }
    }
}
