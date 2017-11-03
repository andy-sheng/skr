package com.wali.live.watchsdk.component.presenter;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;

import com.base.log.MyLog;
import com.base.utils.Constants;
import com.base.utils.rx.RxRetryAssist;
import com.mi.live.data.api.ErrorCode;
import com.mi.live.data.api.LiveManager;
import com.mi.live.data.milink.MiLinkClientAdapter;
import com.mi.live.data.milink.command.MiLinkCommand;
import com.mi.live.data.push.IPushMsgProcessor;
import com.mi.live.data.push.model.BarrageMsg;
import com.mi.live.data.push.model.BarrageMsgType;
import com.mi.live.data.room.model.RoomBaseDataModel;
import com.mi.milink.sdk.aidl.PacketData;
import com.thornbirds.component.IEventController;
import com.thornbirds.component.IParams;
import com.wali.live.component.presenter.BaseSdkRxPresenter;
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
import rx.functions.Func1;
import rx.schedulers.Schedulers;

import static com.wali.live.component.BaseSdkController.MSG_INPUT_VIEW_HIDDEN;
import static com.wali.live.component.BaseSdkController.MSG_INPUT_VIEW_SHOWED;
import static com.wali.live.component.BaseSdkController.MSG_ON_LIVE_SUCCESS;
import static com.wali.live.component.BaseSdkController.MSG_ON_ORIENT_LANDSCAPE;
import static com.wali.live.component.BaseSdkController.MSG_ON_ORIENT_PORTRAIT;
import static com.wali.live.component.BaseSdkController.MSG_ON_PK_START;
import static com.wali.live.component.BaseSdkController.MSG_ON_PK_STOP;

/**
 * Created by chenyong on 2017/03/24.
 *
 * @module 运营位操作类
 */
public class WidgetPresenter extends BaseSdkRxPresenter<WidgetView.IView>
        implements WidgetView.IPresenter, IPushMsgProcessor {
    private static final String TAG = "WidgetPresenter";

    private static final int FROM_ALL = 0;
    private static final int FROM_WATCH = 1;
    private static final int FROM_LIVE = 2;

    protected RoomBaseDataModel mMyRoomData;
    private Handler mUIHandler;
    private boolean mIsAnchor;
    private long mAttachmentStamp = 0;
    private Subscription mWidgetSubscription;

    // TODO 改成不是proto的数据格式
    private List<LiveCommonProto.NewWidgetItem> mWidgetList = new ArrayList();

    @Override
    protected String getTAG() {
        return TAG;
    }

    public WidgetPresenter(
            @NonNull IEventController controller,
            @NonNull RoomBaseDataModel myRoomData,
            boolean isAnchor) {
        super(controller);
        mMyRoomData = myRoomData;
        mUIHandler = new Handler(Looper.getMainLooper());
        mIsAnchor = isAnchor;
    }

    @Override
    public void startPresenter() {
        super.startPresenter();
        registerAction(MSG_ON_ORIENT_PORTRAIT);
        registerAction(MSG_ON_ORIENT_LANDSCAPE);
        registerAction(MSG_ON_LIVE_SUCCESS);
        registerAction(MSG_INPUT_VIEW_SHOWED);
        registerAction(MSG_INPUT_VIEW_HIDDEN);
        registerAction(MSG_ON_PK_START);
        registerAction(MSG_ON_PK_STOP);
    }

    @Override
    public void stopPresenter() {
        super.stopPresenter();
        unregisterAllAction();
    }

    @Override
    public void destroy() {
        super.destroy();
        mView.destroyView();
        if (mUIHandler != null) {
            mUIHandler.removeCallbacksAndMessages(null);
            mUIHandler = null;
        }
    }

    public void reset() {
        mView.hideWidgetView();
        // 这里可以直接调用destroyView，因为切换房间，所有挂件信息会重新拉取
        mView.destroyView();
        if (mUIHandler != null) {
            mUIHandler.removeCallbacksAndMessages(null);
        }
    }

    /**
     * 设置运营位数据
     */
    @MainThread
    public void setWidgetList(List<LiveCommonProto.NewWidgetItem> list) {
        if (mWidgetList.containsAll(list) && mWidgetList.size() == list.size()) {
            return;
        }
        if (mWidgetList.size() > 0) {
            mWidgetList.clear();
        }
        mWidgetList.addAll(list);

        mView.hideWidgetView();
        if (mWidgetList.size() > 0) {
            mView.showWidgetView(mWidgetList);
        }
    }

    public void updateWidgetList(BarrageMsg.WidgetClickMessage msg) {
        if (msg != null) {
            mView.updateWidgetView(msg.widgetID, msg.counter);
        }
    }

    @Override
    public long getUid() {
        return mMyRoomData.getUid();
    }

    @Override
    public String getRoomId() {
        return mMyRoomData.getRoomId();
    }

    @Override
    public void process(BarrageMsg msg, RoomBaseDataModel roomBaseDataModel) {
        if (msg.getMsgType() == BarrageMsgType.B_MSG_TYPE_ATTACHMENT) {
            long attachmentStamp = msg.getSenderMsgId();
            if (mAttachmentStamp < attachmentStamp) {
                mAttachmentStamp = attachmentStamp;
                final BarrageMsg.attachMessageExt msgExt = (BarrageMsg.attachMessageExt) msg.getMsgExt();
                MyLog.w("WidgetCounterPresenter", "BarrageMsgType.B_MSG_TYPE_ATTACHMENT " +
                        "msg=" + msg.toString());
                final List<LiveCommonProto.NewWidgetItem> widgetList = new ArrayList<>();
                for (LiveMessageProto.NewWidgetMessageItem newWidgetMessageItem : msgExt.newWidgetList) {
                    if (!newWidgetMessageItem.getIsDelete()) {
                        int showType = newWidgetMessageItem.getShowType();
                        if (showType == FROM_ALL || (mIsAnchor && showType == FROM_LIVE) || (!mIsAnchor && showType == FROM_WATCH)) {
                            widgetList.add(newWidgetMessageItem.getNewWidgetItem());
                        }
                    }
                }
                if (mUIHandler != null) {
                    mUIHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            setWidgetList(widgetList);
                        }
                    });
                }
            }
        } else if (msg.getMsgType() == BarrageMsgType.B_MSG_TYPE_ATTACHMENT_COUNTER) {
            final BarrageMsg.WidgetClickMessage msgExt = (BarrageMsg.WidgetClickMessage) msg.getMsgExt();
            MyLog.w("WidgetCounterPresenter", "BarrageMsgType.B_MSG_TYPE_ATTACHMENT_COUNTER  msgExt.widgetID="
                    + msgExt.widgetID + " msgExt.counter=" + msgExt.counter);
            if (mUIHandler != null) {
                mUIHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        updateWidgetList(msgExt);
                    }
                });
            }
        }
    }

    public static Observable<LiveProto.GetRoomAttachmentRsp> getRoomAttachment(final String liveId, final long zuid, final int roomType) {
        return Observable.create(new Observable.OnSubscribe<LiveProto.GetRoomAttachmentRsp>() {
            @Override
            public void call(Subscriber<? super LiveProto.GetRoomAttachmentRsp> subscriber) {
                LiveProto.GetRoomAttachmentReq req = LiveProto.GetRoomAttachmentReq
                        .newBuilder()
                        .setIsGetWidget(true)
                        .setLiveid(liveId)
                        .setZuid(zuid)
                        .setIsGetAnimation(true)
                        .setRoomType(roomType)
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
        });
    }

    private void getAttachmentDelay(int time) {
        if (mWidgetSubscription != null) {
            mWidgetSubscription.unsubscribe();
        }
        mWidgetSubscription = Observable.timer(time, TimeUnit.SECONDS)
                .map(new Func1<Long, LiveProto.GetRoomWidgetRsp>() {
                    @Override
                    public LiveProto.GetRoomWidgetRsp call(Long value) {
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
                        PacketData response = MiLinkClientAdapter.getsInstance().sendSync(data, 10 * 1000);
                        LiveProto.GetRoomWidgetRsp rsp = null;
                        try {
                            rsp = LiveProto.GetRoomWidgetRsp.parseFrom(response.getData());
                            MyLog.w(TAG, "getRoomWidget response:" + rsp);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        return rsp;
                    }
                }).compose(this.<LiveProto.GetRoomWidgetRsp>bindUntilEvent(PresenterEvent.DESTROY))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<LiveProto.GetRoomWidgetRsp>() {
                    @Override
                    public void call(LiveProto.GetRoomWidgetRsp rsp) {
                        if (rsp == null || rsp.getRetCode() != ErrorCode.CODE_SUCCESS) {
                            MyLog.w(TAG, "getRoomWidget failed, errCode=" +
                                    (rsp != null ? rsp.getRetCode() : "null"));
                            return;
                        }
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

    @Override
    public int[] getAcceptMsgType() {
        return new int[]{
                BarrageMsgType.B_MSG_TYPE_ATTACHMENT,
                BarrageMsgType.B_MSG_TYPE_ATTACHMENT_COUNTER
        };
    }

    @Override
    public boolean onEvent(int event, IParams params) {
        if (mView == null) {
            MyLog.e(TAG, "onAction but mView is null, event=" + event);
            return false;
        }
        switch (event) {
            case MSG_ON_ORIENT_PORTRAIT:
                mView.onOrientation(false);
                return true;
            case MSG_ON_ORIENT_LANDSCAPE:
                mView.onOrientation(true);
                return true;
            case MSG_ON_LIVE_SUCCESS:
            case MSG_ON_PK_START:
            case MSG_ON_PK_STOP:
                if (!Constants.isGooglePlayBuild && !Constants.isIndiaBuild) {
                    int liveType = mMyRoomData.getLiveType();
                    MyLog.w(TAG, "live type=" + liveType + " event=" + event);
                    if (liveType != LiveManager.TYPE_LIVE_PRIVATE && liveType != LiveManager.TYPE_LIVE_TOKEN) {
                        getRoomAttachment(mMyRoomData.getRoomId(), mMyRoomData.getUid(), mMyRoomData.getLiveType())
                                .compose(bindUntilEvent(PresenterEvent.DESTROY))
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
                }
                break;
            case MSG_INPUT_VIEW_SHOWED:
                mView.adjustWidgetView(false);
                break;
            case MSG_INPUT_VIEW_HIDDEN:
                mView.adjustWidgetView(true);
                break;
            default:
                break;
        }
        return false;
    }
}
