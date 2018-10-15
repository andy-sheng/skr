package com.wali.live.livesdk.live.fragment.presenter;

import android.support.annotation.NonNull;

import com.base.log.MyLog;
import com.base.presenter.RxLifeCyclePresenter;
import com.google.protobuf.InvalidProtocolBufferException;
import com.mi.live.data.account.UserAccountManager;
import com.mi.live.data.milink.MiLinkClientAdapter;
import com.mi.live.data.milink.command.MiLinkCommand;
import com.mi.live.data.milink.constant.MiLinkConstant;
import com.mi.milink.sdk.aidl.PacketData;
import com.wali.live.proto.LiveProto;
import com.wali.live.proto.RankProto;
import com.wali.live.utils.relation.RelationUtils;

import java.util.List;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by zyh on 2017/07/18.
 *
 * @module 主播端结束页的presenter
 */
public class AnchorEndlivePresenter extends RxLifeCyclePresenter {
    private static final String TAG = "AnchorEndlivePresenter";
    @NonNull
    IAnchorEndLiveView mView;

    public AnchorEndlivePresenter(@NonNull IAnchorEndLiveView view) {
        mView = view;
    }

    @Override
    public void destroy() {
        super.destroy();
    }

    public void getTopThree(final String roomId) {
        Observable.just(0)
                .map(new Func1<Integer, List<RankProto.RankUser>>() {
                    @Override
                    public List<RankProto.RankUser> call(Integer integer) {
                        return RelationUtils.getRankRoomList(roomId, 3, 0);
                    }
                }).subscribeOn(Schedulers.io())
                .compose(this.<List<RankProto.RankUser>>bindUntilEvent(PresenterEvent.DESTROY))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<RankProto.RankUser>>() {
                    @Override
                    public void call(List<RankProto.RankUser> list) {
                        if (mView != null && list != null) {
                            mView.onGetTopThree(list);
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        MyLog.e(TAG, "getTopThree failed=" + throwable);
                    }
                });
    }

    public void deleteHistory(final String liveId) {
        Observable.just(0)
                .map(new Func1<Integer, LiveProto.HistoryDeleteRsp>() {
                    @Override
                    public LiveProto.HistoryDeleteRsp call(Integer integer) {
                        LiveProto.HistoryDeleteReq req = LiveProto.HistoryDeleteReq.newBuilder()
                                .setZuid(UserAccountManager.getInstance().getUuidAsLong())
                                .setLiveId(liveId)
                                .build();
                        PacketData packetData = new PacketData();
                        packetData.setCommand(MiLinkCommand.COMMAND_LIVE_HISTORY_DELETE);
                        packetData.setData(req.toByteArray());
                        PacketData rspData = MiLinkClientAdapter.getsInstance().sendSync(packetData, MiLinkConstant.TIME_OUT);
                        if (rspData != null) {
                            try {
                                LiveProto.HistoryDeleteRsp rsp = LiveProto.HistoryDeleteRsp.parseFrom(rspData.getData());
                                return rsp;
                            } catch (InvalidProtocolBufferException e) {
                                MyLog.e(e);
                            }
                        }
                        return null;
                    }
                }).subscribeOn(Schedulers.io())
                .compose(this.<LiveProto.HistoryDeleteRsp>bindUntilEvent(PresenterEvent.DESTROY))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<LiveProto.HistoryDeleteRsp>() {
                    @Override
                    public void call(LiveProto.HistoryDeleteRsp rsp) {
                        MyLog.e(TAG, "rsp=" + rsp != null ? rsp.toString() : "null");
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        MyLog.e(TAG, "deleteHistory failed=" + throwable);
                    }
                });
    }

    public interface IAnchorEndLiveView {
        void onGetTopThree(List<RankProto.RankUser> list);
    }
}
