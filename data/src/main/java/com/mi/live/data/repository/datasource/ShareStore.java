package com.mi.live.data.repository.datasource;

import com.base.log.MyLog;
import com.google.protobuf.InvalidProtocolBufferException;
import com.mi.live.data.milink.MiLinkClientAdapter;
import com.mi.live.data.milink.command.MiLinkCommand;
import com.mi.milink.sdk.aidl.PacketData;
import com.wali.live.proto.ShareProto;

import java.util.List;

import rx.Observable;
import rx.Subscriber;

/**
 * Created by zhangyuehuan on 16/7/13.
 */
public class ShareStore {
    final static String TAG = ShareStore.class.getSimpleName();

    /**
     * 获取分享文案尾部文案
     */
    public Observable<ShareProto.GetShareTagTailRsp> getTagTailForShare(final long uuid,
                                                                        final ShareProto.RoleType roleType,
                                                                        final List<ShareProto.ChannelType> channelTypeList, final ShareProto.PeriodType periodType) {
        return Observable.create(new Observable.OnSubscribe<ShareProto.GetShareTagTailRsp>() {
            @Override
            public void call(Subscriber<? super ShareProto.GetShareTagTailRsp> subscriber) {
                ShareProto.GetShareTagTailReq request = ShareProto.GetShareTagTailReq.newBuilder()
                        .setUuid(uuid)
                        .setRole(roleType)
                        .addAllChannel(channelTypeList)
                        .setPeriod(periodType)
                        .build();

                PacketData data = new PacketData();
                data.setCommand(MiLinkCommand.COMMAND_SHARE_TAG_TAIL);
                data.setData(request.toByteArray());
                MyLog.v(TAG, "getTagTailForShare request : \n" + request.toString());

                PacketData rspData = MiLinkClientAdapter.getsInstance().sendSync(data, 10 * 1000);
                if (null != rspData) {
                    try {
                        ShareProto.GetShareTagTailRsp rsp = ShareProto.GetShareTagTailRsp.parseFrom(rspData.getData());
                        MyLog.v(TAG, "getTagTailForShare response : \n" + rsp.toString());
                        subscriber.onNext(rsp);
                        subscriber.onCompleted();
                    } catch (InvalidProtocolBufferException e) {
                        MyLog.e(e);
                        subscriber.onError(e);
                    }
                } else {
                    MyLog.v(TAG, "getTagTailForShare rsp == null");
                }
            }
        });
    }
}
