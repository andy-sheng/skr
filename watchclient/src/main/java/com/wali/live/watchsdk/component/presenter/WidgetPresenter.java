package com.wali.live.watchsdk.component.presenter;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.base.log.MyLog;
import com.mi.live.data.account.UserAccountManager;
import com.mi.live.data.milink.MiLinkClientAdapter;
import com.mi.live.data.milink.command.MiLinkCommand;
import com.mi.live.data.push.IPushMsgProcessor;
import com.mi.live.data.push.model.BarrageMsg;
import com.mi.live.data.push.model.BarrageMsgType;
import com.mi.live.data.room.model.RoomBaseDataModel;
import com.mi.milink.sdk.aidl.PacketData;
import com.wali.live.component.presenter.ComponentPresenter;
import com.wali.live.proto.LiveCommonProto;
import com.wali.live.proto.LiveMessageProto;
import com.wali.live.proto.LiveProto;
import com.wali.live.watchsdk.component.view.WidgetView;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.Subscriber;
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

    // TODO 改成不是proto的数据格式
    private List<LiveCommonProto.NewWidgetItem> mWidgetList = new ArrayList();

    public WidgetPresenter(@NonNull IComponentController componentController,
                           @NonNull RoomBaseDataModel myRoomData) {
        super(componentController);
        mMyRoomData = myRoomData;
        mUIHandler = new Handler(Looper.getMainLooper());
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
        mUIHandler.removeCallbacksAndMessages(null);
        mUIHandler = null;
    }

    @Override
    protected IAction createAction() {
        return new Action();
    }

    @Override
    public long getUid() {
        return mMyRoomData.getUid();
    }

    // TODO chenyong1 增加水位
    @Override
    public void process(BarrageMsg msg, RoomBaseDataModel roomBaseDataModel) {
        if (msg.getMsgType() == BarrageMsgType.B_MSG_TYPE_ATTACHMENT) {
            final BarrageMsg.attachMessageExt msgExt = (BarrageMsg.attachMessageExt) msg.getMsgExt();
            final List<LiveCommonProto.NewWidgetItem> widgetList = new ArrayList<>();
            for (LiveMessageProto.NewWidgetMessageItem newWidgetMessageItem : msgExt.newWidgetList) {
                if (!newWidgetMessageItem.getIsDelete()) {
                    boolean isAnchor = mMyRoomData.getUid() == UserAccountManager.getInstance().getUuidAsLong();
                    int showType = newWidgetMessageItem.getShowType();
                    if (showType == FROM_ALL || (isAnchor && showType == FROM_LIVE) || (!isAnchor && showType == FROM_WATCH)) {
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

    /**
     * 拉取该房间运营位信息
     */
    public static Observable<LiveProto.GetRoomWidgetRsp> getRoomWidget(final String roomId, final long zuid, final int roomType) {
        return Observable.create(
                new Observable.OnSubscribe<LiveProto.GetRoomWidgetRsp>() {
                    @Override
                    public void call(Subscriber<? super LiveProto.GetRoomWidgetRsp> subscriber) {
                        LiveProto.GetRoomWidgetReq req = LiveProto.GetRoomWidgetReq
                                .newBuilder()
                                .setLiveid(roomId)
                                .setZuid(zuid)
                                .setRoomType(roomType)
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
                }).subscribeOn(Schedulers.io());
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
                default:
                    break;
            }
            return false;
        }
    }
}
