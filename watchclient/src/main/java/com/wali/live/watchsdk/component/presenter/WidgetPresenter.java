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
import com.mi.live.data.repository.model.turntable.TurnTableConfigModel;
import com.mi.live.data.room.model.RoomBaseDataModel;
import com.mi.milink.sdk.aidl.PacketData;
import com.thornbirds.component.IEventController;
import com.thornbirds.component.IParams;
import com.thornbirds.component.Params;
import com.trello.rxlifecycle.ActivityEvent;
import com.wali.live.component.presenter.BaseSdkRxPresenter;
import com.wali.live.proto.LiveCommonProto;
import com.wali.live.proto.LiveMessageProto;
import com.wali.live.proto.LiveProto;
import com.wali.live.watchsdk.component.view.WidgetView;
import com.wali.live.watchsdk.eventbus.EventClass;
import com.wali.live.watchsdk.vip.manager.OperationAnimManager;
import com.wali.live.watchsdk.watch.presenter.watchgamepresenter.BaseEnterRoomSyncResPresenter;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

import static com.wali.live.component.BaseSdkController.MSG_BARRAGE_ADMIN;
import static com.wali.live.component.BaseSdkController.MSG_BARRAGE_FANS;
import static com.wali.live.component.BaseSdkController.MSG_BARRAGE_VIP;
import static com.wali.live.component.BaseSdkController.MSG_HIDE_BIG_TURN_TABLE_BTN;
import static com.wali.live.component.BaseSdkController.MSG_INPUT_VIEW_HIDDEN;
import static com.wali.live.component.BaseSdkController.MSG_INPUT_VIEW_SHOWED;
import static com.wali.live.component.BaseSdkController.MSG_ON_LIVE_SUCCESS;
import static com.wali.live.component.BaseSdkController.MSG_ON_ORIENT_LANDSCAPE;
import static com.wali.live.component.BaseSdkController.MSG_ON_ORIENT_PORTRAIT;
import static com.wali.live.component.BaseSdkController.MSG_ON_PK_START;
import static com.wali.live.component.BaseSdkController.MSG_ON_PK_STOP;
import static com.wali.live.component.BaseSdkController.MSG_SHOW_BIG_TURN_TABLE_BTN;

/**
 * Created by chenyong on 2017/03/24.
 *
 * @module 运营位操作类
 */
public class WidgetPresenter extends BaseEnterRoomSyncResPresenter
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
        super(controller, myRoomData, isAnchor);
        mUIHandler = new Handler(Looper.getMainLooper());
        mIsAnchor = isAnchor;
    }

    @Override
    public void startPresenter() {
        super.startPresenter();
        registerAction(MSG_ON_ORIENT_PORTRAIT);
        registerAction(MSG_ON_ORIENT_LANDSCAPE);
        registerAction(MSG_INPUT_VIEW_SHOWED);
        registerAction(MSG_INPUT_VIEW_HIDDEN);
        registerAction(MSG_ON_PK_START);
        registerAction(MSG_ON_PK_STOP);
    }

    @Override
    public void stopPresenter() {
        super.stopPresenter();
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
    @Override
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

    @Override
    protected void getAttachmentDelay(int time) {
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
    protected void optEvent(int event) {
        super.optEvent(event);
        if (mView == null) {
            MyLog.e(TAG, "onAction but mView is null, event=" + event);
            return;
        }

        switch (event) {
            case MSG_ON_ORIENT_PORTRAIT:
                mView.onOrientation(false);
                break;
            case MSG_ON_ORIENT_LANDSCAPE:
                mView.onOrientation(true);
                break;
            case MSG_ON_PK_START:
            case MSG_ON_PK_STOP:
                getRoomAttachment();
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
    }
}
