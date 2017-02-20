package com.mi.live.data.query;

import android.text.TextUtils;

import com.base.log.MyLog;
import com.google.protobuf.InvalidProtocolBufferException;
import com.mi.live.data.milink.MiLinkClientAdapter;
import com.mi.live.data.milink.command.MiLinkCommand;
import com.mi.live.data.milink.constant.MiLinkConstant;
import com.mi.live.data.user.User;
import com.mi.live.data.user.UserCache;
import com.mi.milink.sdk.aidl.PacketData;
import com.wali.live.proto.LiveCommonProto;
import com.wali.live.proto.UserProto;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Func1;

/**
 * Created by chengsimin on 16/9/20.
 */
public class PersonInfoQuery {
    public final static String TAG = PersonInfoQuery.class.getSimpleName();

    public static Observable<User> queryPersonInfo(final long uuid, final boolean needPullLiveInfo) {
        return Observable.create(new Observable.OnSubscribe<UserProto.GetHomepageResp>() {
            @Override
            public void call(Subscriber<? super UserProto.GetHomepageResp> subscriber) {
                if (uuid <= 0) {
                    subscriber.onError(new Exception("queryId < 0!"));
                    return;
                }

                UserProto.GetHomepageReq req = UserProto.GetHomepageReq.newBuilder()
                        .setZuid(uuid)
                        .setGetLiveInfo(needPullLiveInfo)
                        .build();
                PacketData data = new PacketData();
                data.setCommand(MiLinkCommand.COMMAND_GET_HOMEPAGE);
                data.setData(req.toByteArray());
                MyLog.w(TAG + " GetUserInfoByIdReq request : \n" + req.toString());

                PacketData rspData = MiLinkClientAdapter.getsInstance().sendSync(data, MiLinkConstant.TIME_OUT);
                if (rspData != null) {
                    try {
                        UserProto.GetHomepageResp rsp = UserProto.GetHomepageResp.parseFrom(rspData.getData());
                        if (rsp.getRetCode() != 0) {
                            subscriber.onError(new Exception("rsp.getRetCode:" + rsp.getRetCode()));
                            return;
                        }
                        MyLog.w(TAG + " GetUserInfoByIdReq response : \n" + rsp.toString());
                        subscriber.onNext(rsp);
                        subscriber.onCompleted();
                        return;
                    } catch (InvalidProtocolBufferException e) {
                        e.printStackTrace();
                        subscriber.onError(e);
                    }
                } else {
                    subscriber.onError(new NullPointerException());
                }
            }
        })
                .map(new Func1<UserProto.GetHomepageResp, User>() {
                    @Override
                    public User call(UserProto.GetHomepageResp rsp) {
                        User user = new User();
                        user.parse(rsp.getPersonalInfo());
                        user.parse(rsp.getPersonalData());
                        try {
                            user.setRoomType(LiveCommonProto.UserRoomInfo.parseFrom(rsp.getRoomInfo()).getType());
                        } catch (InvalidProtocolBufferException e) {
                            e.printStackTrace();
                        }
                        user.setSellerStatus(rsp.getPersonalInfo().getSellerStatus());
                        if (rsp.getRankTopThreeListList() != null) {
                            user.setRankTopThreeList(rsp.getRankTopThreeListList());
                        }

                        if (!TextUtils.isEmpty(rsp.getViewUrl())) {
                            user.setViewUrl(rsp.getViewUrl());
                        }

                        if (!TextUtils.isEmpty(rsp.getRoomId())) {
                            user.setRoomId(rsp.getRoomId());
                        }
                        UserCache.addUser(user);
                        return user;
                    }
                });
    }
}
