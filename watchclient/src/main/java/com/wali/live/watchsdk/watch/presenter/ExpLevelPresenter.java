package com.wali.live.watchsdk.watch.presenter;

import com.base.log.MyLog;
import com.base.presenter.RxLifeCyclePresenter;
import com.google.protobuf.InvalidProtocolBufferException;
import com.mi.live.data.account.UserAccountManager;
import com.mi.live.data.milink.MiLinkClientAdapter;
import com.mi.live.data.milink.command.MiLinkCommand;
import com.mi.live.data.milink.constant.MiLinkConstant;
import com.mi.milink.sdk.aidl.PacketData;
import com.wali.live.proto.ExpLevelProto;
import com.wali.live.watchsdk.ipc.service.ShareInfo;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by zyh on 2017/4/27.
 */

public class ExpLevelPresenter extends RxLifeCyclePresenter {
    private final static String TAG = "ExpLevelPresenter";

    public static final int SHARE_TYPE = 6;
    public static final int WX = 1;
    public static final int QQ = 2;
    public static final int WEIBO = 3;
    public static final int FACEBOOK = 4;
    public static final int TWITTER = 5;
    public static final int INSTAGRAM = 6;
    public static final int WHATSAPP = 7;
    public static final int MILIAO = 8;
    public static final int MILIAO_FEEDS = 9;

    /**
     * 注意：历史问题，客户端定义的type和服务器加经验值type不一致，加经验值需要转换一下。
     */
    public int parseShareTypeToExpType(int shareType) {
        switch (shareType) {
            case ShareInfo.TYPE_WECHAT:
            case ShareInfo.TYPE_MOMENT:
                return WX;
            case ShareInfo.TYPE_QQ:
            case ShareInfo.TYPE_QZONE:
                return QQ;
            case ShareInfo.TYPE_WEIBO:
                return WEIBO;
            case ShareInfo.TYPE_FACEBOOK:
                return FACEBOOK;
            case ShareInfo.TYPE_TWITTER:
                return TWITTER;
            case ShareInfo.TYPE_INSTAGRAM:
                return INSTAGRAM;
            case ShareInfo.TYPE_WHATSAPP:
                return WHATSAPP;
            case ShareInfo.TYPE_MILIAO:
                return MILIAO;
            case ShareInfo.TYPE_MILIAO_FEEDS:
                return MILIAO_FEEDS;
        }
        return -1;
    }

    /**
     * 直播间分享成功 并且更新经验值。
     */
    public void updateExperience(final int type, final int snsType) {
        Observable.just(0)
                .map(new Func1<Integer, ExpLevelProto.UpdateExpRsp>() {
                    @Override
                    public ExpLevelProto.UpdateExpRsp call(Integer integer) {
                        int expShareType = parseShareTypeToExpType(snsType);
                        if (expShareType != -1) {
                            return null;
                        }
                        ExpLevelProto.UpdateExpReq.Builder builder = ExpLevelProto.UpdateExpReq.newBuilder();
                        builder.setUuid(UserAccountManager.getInstance().getUuidAsLong())
                                .setType(type).setValue(expShareType);
                        PacketData packetData = new PacketData();
                        packetData.setCommand(MiLinkCommand.COMMAND_EXPLEVEL_UPDATE);
                        packetData.setData(builder.build().toByteArray());
                        MyLog.w(TAG, "updateExperience request : \n" + builder.build().toString());
                        PacketData rspData = MiLinkClientAdapter.getsInstance().sendSync(packetData, MiLinkConstant.TIME_OUT);
                        if (rspData != null) {
                            try {
                                ExpLevelProto.UpdateExpRsp rsp = ExpLevelProto.UpdateExpRsp.parseFrom(rspData.getData());
                                MyLog.w(TAG, "updateExperience rp : \n" + rsp.toString());
                                return rsp;
                            } catch (InvalidProtocolBufferException e) {
                                MyLog.e("ExpLevelPresenter", e);
                            }
                        }
                        return null;
                    }
                }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(this.<ExpLevelProto.UpdateExpRsp>bindUntilEvent(PresenterEvent.DESTROY))
                .subscribe(new Action1<ExpLevelProto.UpdateExpRsp>() {
                    @Override
                    public void call(ExpLevelProto.UpdateExpRsp rsp) {
                        if (rsp != null) {
                            MyLog.w("ExpLevelPresenter", "updateExperience");
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        MyLog.e(TAG, "updateExperience failed=" + throwable);
                    }
                });
    }
}
