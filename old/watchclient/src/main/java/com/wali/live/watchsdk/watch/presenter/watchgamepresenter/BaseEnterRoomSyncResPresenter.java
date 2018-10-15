package com.wali.live.watchsdk.watch.presenter.watchgamepresenter;

import android.support.annotation.MainThread;
import android.support.annotation.NonNull;

import com.base.log.MyLog;
import com.base.utils.Constants;
import com.base.utils.rx.RxRetryAssist;
import com.mi.live.data.api.LiveManager;
import com.mi.live.data.milink.MiLinkClientAdapter;
import com.mi.live.data.milink.command.MiLinkCommand;
import com.mi.live.data.push.IPushMsgProcessor;
import com.mi.live.data.push.model.BarrageMsg;
import com.mi.live.data.repository.model.turntable.TurnTableConfigModel;
import com.mi.live.data.room.model.RoomBaseDataModel;
import com.mi.milink.sdk.aidl.PacketData;
import com.thornbirds.component.IEventController;
import com.thornbirds.component.IParams;
import com.thornbirds.component.Params;
import com.wali.live.component.presenter.BaseSdkRxPresenter;
import com.wali.live.proto.LiveCommonProto;
import com.wali.live.proto.LiveProto;
import com.wali.live.watchsdk.component.view.WidgetView;
import com.wali.live.watchsdk.eventbus.EventClass;
import com.wali.live.watchsdk.vip.manager.OperationAnimManager;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
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
 * Created by zhujianning on 18-8-9.
 * 从WidgetPresenter中抽离一些方法,如果后面游戏直播间需要运营位直接使用WidgetPresenter
 */

public class BaseEnterRoomSyncResPresenter extends BaseSdkRxPresenter<WidgetView.IView> {
    private static final String TAG = "BaseEnterRoomSyncResPresenter";

    protected static final int WIDGET_SPEEDY_GIFT_POSITION = 7;//首充运营位

    protected RoomBaseDataModel mMyRoomData;

    public BaseEnterRoomSyncResPresenter(@NonNull IEventController controller,
                                         @NonNull RoomBaseDataModel myRoomData,
                                         boolean isAnchor) {
        super(controller);
        mMyRoomData = myRoomData;
    }

    @Override
    protected String getTAG() {
        return TAG;
    }

    @Override
    public void startPresenter() {
        super.startPresenter();
        registerAction(MSG_ON_LIVE_SUCCESS);
    }

    @Override
    public void stopPresenter() {
        super.stopPresenter();
        unregisterAllAction();
    }

    @Override
    public boolean onEvent(int event, IParams iParams) {
        switch (event) {
            case MSG_ON_LIVE_SUCCESS:
                syncRoyalRes();
                getRoomAttachment();
                break;
            case MSG_ON_ORIENT_PORTRAIT:
            case MSG_ON_ORIENT_LANDSCAPE:
            case MSG_ON_PK_START:
            case MSG_ON_PK_STOP:
            case MSG_INPUT_VIEW_SHOWED:
            case MSG_INPUT_VIEW_HIDDEN:
                optEvent(event);
                return true;
            default:
                break;

        }
        return false;
    }

    protected void optEvent(int event) {

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
                        .setIsGetRoomExtraCtrl(true)
                        .setIsGetIconConfig(false)
                        .setIsGetTurntable(true)
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

    protected void getRoomAttachment() {
        if (!Constants.isGooglePlayBuild && !Constants.isIndiaBuild) {
            int liveType = mMyRoomData.getLiveType();
            if (liveType != LiveManager.TYPE_LIVE_PRIVATE && liveType != LiveManager.TYPE_LIVE_TOKEN) {
                getRoomAttachment(mMyRoomData.getRoomId(), mMyRoomData.getUid(), mMyRoomData.getLiveType())
                        .retryWhen(new RxRetryAssist(3, 5, true)) // 重试3次，间隔5秒
                        .compose(bindUntilEvent(PresenterEvent.DESTROY))
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Action1<Object>() {
                            @Override
                            public void call(Object o) {
                                LiveProto.GetRoomAttachmentRsp rsp = (LiveProto.GetRoomAttachmentRsp) o;
                                setWidgetList(rsp.getNewWidgetInfo().getWidgetItemList());
                                //粉丝团飘萍计数
                                if (rsp.hasVfansCounter()) {
                                    mController.postEvent(MSG_BARRAGE_FANS, new Params().putItem(rsp.hasVfansCounter()));
                                }
                                //管理员飘萍计数
                                if (rsp.hasCounter()) {
                                    mController.postEvent(MSG_BARRAGE_ADMIN, new Params().putItem(rsp.getCounter()));
                                }
                                // vip用户计数
                                if (rsp.hasVipCounter()) {
                                    mController.postEvent(MSG_BARRAGE_VIP, new Params().putItem(rsp.getVipCounter()));
                                }
                                if (rsp.getNewWidgetInfo().hasPullInterval()) {
                                    getAttachmentDelay(rsp.getNewWidgetInfo().getPullInterval());
                                }

                                if (rsp.hasAnimationConfig() && rsp.getAnimationConfig().hasNoJoinAnimation()) {
                                    int noJoinAnimation = rsp.getAnimationConfig().getNoJoinAnimation();
                                    EventBus.getDefault().post(EventClass.UpdateVipEnterRoomEffectSwitchEvent.newInstance(mMyRoomData.getUid(), noJoinAnimation));
                                }

                                //快速送礼物
                                getFastGiftInfo(rsp);

                                //大转盘
                                if(rsp.getTurntableConfigList() != null
                                        && !rsp.getTurntableConfigList().isEmpty()) {
                                    TurnTableConfigModel data = new TurnTableConfigModel(rsp.getTurntableConfigList().get(0));
                                    postEvent(MSG_SHOW_BIG_TURN_TABLE_BTN, new Params().putItem(data));
                                } else {
                                    postEvent(MSG_HIDE_BIG_TURN_TABLE_BTN);
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
    }

    @MainThread
    public void setWidgetList(List<LiveCommonProto.NewWidgetItem> list) {

    }

    protected void getAttachmentDelay(int time) {}

    protected void getFastGiftInfo(LiveProto.GetRoomAttachmentRsp rsp) {
        if(rsp == null) {
            return;
        }

        LiveCommonProto.NewWidgetInfo info = rsp.getNewWidgetInfo();
        if(info == null) {
            return;
        }

        LiveCommonProto.NewWidgetUnit widgetUnit = null;
        for(LiveCommonProto.NewWidgetItem item : info.getWidgetItemList()) {
            if(item != null
                    && item.getPosition() == WIDGET_SPEEDY_GIFT_POSITION) {
                List<LiveCommonProto.NewWidgetUnit> units = item.getWidgetUintList();
                if(units != null
                        && !units.isEmpty()) {
                    widgetUnit = units.get(0);
                    break;
                }
            }
        }

        EventBus.getDefault().post(new EventClass.UpdateFastGiftInfoEvent(rsp.hasSpeedyGiftConfig() ? rsp.getSpeedyGiftConfig().getGiftId() : -1
                , widgetUnit != null ? widgetUnit.getIcon() : null
                , widgetUnit != null ? widgetUnit.getLinkUrl() : null));
    }

    protected void syncRoyalRes() {
        Observable.create(new Observable.OnSubscribe<Object>() {
            @Override
            public void call(Subscriber<? super Object> subscriber) {
                OperationAnimManager.pullResListFromServer(false);
                subscriber.onCompleted();
            }
        })
                .subscribeOn(Schedulers.io())
                .compose(this.<Object>bindUntilEvent(PresenterEvent.DESTROY))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Object>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        MyLog.d(TAG, e);
                    }

                    @Override
                    public void onNext(Object o) {

                    }
                });


    }
}
