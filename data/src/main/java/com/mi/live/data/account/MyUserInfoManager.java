package com.mi.live.data.account;

import android.text.TextUtils;

import com.base.log.MyLog;
import com.base.utils.language.LocaleUtil;
import com.google.protobuf.InvalidProtocolBufferException;
import com.mi.live.data.account.event.UserInfoEvent;
import com.mi.live.data.milink.MiLinkClientAdapter;
import com.mi.live.data.milink.command.MiLinkCommand;
import com.mi.live.data.milink.constant.MiLinkConstant;
import com.mi.live.data.repository.datasource.MyUserInfoLocalStore;
import com.mi.live.data.user.User;
import com.mi.milink.sdk.aidl.PacketData;
import com.wali.live.dao.OwnUserInfo;
import com.wali.live.proto.UserProto;

import org.greenrobot.eventbus.EventBus;

import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.Subscription;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * 保存个人详细信息，我的信息的管理, 其实是对User的decorate
 * Created by yaojian on 16-2-24.
 *
 * @module 我的用户信息管理
 */
public class MyUserInfoManager {
    private final static String TAG = "MyUserInfoManager";

    private User mMyInfo = new User();

    private final static MyUserInfoManager sInstance = new MyUserInfoManager();

    /**
     * MyUserInfoManager构造函数, 从
     */
    private MyUserInfoManager() {

    }

    public static MyUserInfoManager getInstance() {
        return sInstance;
    }

    public void init() {
        // 从数据库得到个人信息
        Observable.just(null)
                .subscribeOn(Schedulers.io())
                .subscribe(new Action1<Object>() {
                    @Override
                    public void call(Object o) {
                        mMyInfo = readFromDB();
                    }
                });
        // 如果数据
//        EventBus.getDefault().register(this);
    }

    /**
     * 尝试从数据库中　读取用户信息
     */
    private User readFromDB() {
        User user = new User();
        OwnUserInfo ownUserInfo = MyUserInfoLocalStore.getInstance().getAccount(HostChannelManager.getInstance().getCurrentChannelId());
        MyLog.w(TAG,"ownUserInfo:"+ownUserInfo);
        if (ownUserInfo != null) {
            user.setUid(ownUserInfo.getUid());
            user.setNickname(ownUserInfo.getNickname());
            user.setSign(ownUserInfo.getSign());
            user.setAvatar(ownUserInfo.getAvatar());
            user.setGender(ownUserInfo.getGender());
            user.setLevel(ownUserInfo.getLevel());
            user.setBadge(ownUserInfo.getBadge());
            user.setCertification(ownUserInfo.getCertification());
            user.setCertificationType(ownUserInfo.getCertificationType());
            user.waitingCertificationType = ownUserInfo.getWaitingCertificationType();
            user.setInspector(ownUserInfo.getIsInspector());
            user.setLiveTicketNum(ownUserInfo.getLiveTicketNum());
            user.setFansNum(ownUserInfo.getFansNum());
            user.setFollowNum(ownUserInfo.getFollowNum());
            user.setSendDiamondNum(ownUserInfo.getSendDiamondNum());
            user.setVodNum(ownUserInfo.getVodNum());
            user.setEarnNum(ownUserInfo.getEarnNum());
            user.setDiamondNum(ownUserInfo.getDiamondNum());
            user.setSentVirtualDiamondNum(ownUserInfo.getSendVirtualDiamondNum());
            user.setVirtualDiamondNum(ownUserInfo.getVirtualDiamondNum());
            user.coverPhotoJson = ownUserInfo.getCoverPhotoJson();
            try {
                if (ownUserInfo.getRegion() != null) {
                    user.setRegion(UserProto.Region.parseFrom(ownUserInfo.getRegion()));
                } else {
                    user.setRegion(null);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            Boolean b = ownUserInfo.getFirstAudit();
            if (b != null) {
                user.firstAudit = b.booleanValue();
            }
            user.firstAudit = ownUserInfo.getFirstAudit();
            user.setRedName(ownUserInfo.getRedName() == null ? false : ownUserInfo.getRedName());
        }
        MyLog.d(TAG,"");
        return user;
    }

    /**
     * 保存数据到DB
     */
    private void saveInfoIntoDB() {
        if (mMyInfo == null) {
            return;
        }
        OwnUserInfo ownUserInfo = new OwnUserInfo();
        ownUserInfo.setChannelid(HostChannelManager.getInstance().getCurrentChannelId());
        ownUserInfo.setUid(mMyInfo.getUid());
        ownUserInfo.setNickname(mMyInfo.getNickname());
        ownUserInfo.setSign(mMyInfo.getSign());
        ownUserInfo.setAvatar(mMyInfo.getAvatar());
        ownUserInfo.setGender(mMyInfo.getGender());
        ownUserInfo.setLevel(mMyInfo.getLevel());
        ownUserInfo.setBadge(mMyInfo.getBadge());
        ownUserInfo.setCertification(mMyInfo.getCertification());
        ownUserInfo.setCertificationType(mMyInfo.getCertificationType());
        ownUserInfo.setWaitingCertificationType(mMyInfo.waitingCertificationType);
        ownUserInfo.setIsInspector(mMyInfo.isInspector());
        ownUserInfo.setLiveTicketNum(mMyInfo.getLiveTicketNum());
        ownUserInfo.setFansNum(mMyInfo.getFansNum());
        ownUserInfo.setFollowNum(mMyInfo.getFollowNum());
        ownUserInfo.setSendDiamondNum(mMyInfo.getSendDiamondNum());
        ownUserInfo.setVodNum(mMyInfo.getVodNum());
        ownUserInfo.setEarnNum(mMyInfo.getEarnNum());
        ownUserInfo.setDiamondNum(mMyInfo.getDiamondNum());
        ownUserInfo.setSendVirtualDiamondNum(mMyInfo.getSentVirtualDiamondNum());
        ownUserInfo.setVirtualDiamondNum(mMyInfo.getVirtualDiamondNum());
        ownUserInfo.setCoverPhotoJson(mMyInfo.coverPhotoJson);
        ownUserInfo.setFirstAudit(mMyInfo.firstAudit);
        ownUserInfo.setRedName(mMyInfo.isRedName());
        if (LocaleUtil.getSelectedLanguageIndex() != LocaleUtil.INDEX_ENGLISH) {
            ownUserInfo.setRegion(mMyInfo.getRegion().toByteArray());
        }
        MyUserInfoLocalStore.getInstance().replaceAccount(ownUserInfo);
    }


    Subscription mSyncSubscription;

    /**
     * 同步自己的个人信息
     */
    public void syncSelfDetailInfo() {
        MyLog.w(TAG,"syncSelfDetailInfo");
        if (mSyncSubscription != null && !mSyncSubscription.isUnsubscribed()) {
            return;
        }
        mSyncSubscription = Observable.create(new Observable.OnSubscribe<UserProto.GetOwnInfoRsp>() {
            @Override
            public void call(Subscriber<? super UserProto.GetOwnInfoRsp> subscriber) {
                long uid = UserAccountManager.getInstance().getUuidAsLong();
                if (uid <= 0) {
                    subscriber.onError(new Exception("uid<=0"));
                    return;
                }
                UserProto.GetOwnInfoReq req = UserProto
                        .GetOwnInfoReq
                        .newBuilder()
                        .setZuid(uid)
                        .build();
                PacketData data = new PacketData();
                data.setCommand(MiLinkCommand.COMMAND_GET_OWN_INFO);
                data.setData(req.toByteArray());
                MyLog.d(TAG + "syncMyOwnerInfo request : \n" + req.toString());
                PacketData packetData = MiLinkClientAdapter.getsInstance().sendSync(data, MiLinkConstant.TIME_OUT);
                if (packetData != null) {
                    try {
                        UserProto.GetOwnInfoRsp rsp = UserProto.GetOwnInfoRsp.parseFrom(packetData.getData());
                        if (rsp == null || rsp.getErrorCode() != 0) {
                            subscriber.onError(new Exception("rsp==null || rsp.getErrorCode()!=0"));
                        }
                        subscriber.onNext(rsp);
                        subscriber.onCompleted();
                    } catch (InvalidProtocolBufferException e) {
                        subscriber.onError(new Exception("rsp is null"));
                    }
                } else {
                    subscriber.onError(new Exception("uid<=0"));
                    return;
                }
            }
        })
                .map(new Func1<UserProto.GetOwnInfoRsp, User>() {
                    @Override
                    public User call(UserProto.GetOwnInfoRsp rsp) {
                        User user = new User();
                        if (rsp.getPersonalInfo() != null) {
                            user.parse(rsp.getPersonalInfo());
                        }
                        if (rsp.getPersonalData() != null) {
                            user.parse(rsp.getPersonalData());
                        }
                        if (rsp.getRankTopThreeListList() != null) {
                            user.setRankTopThreeList(rsp.getRankTopThreeListList());
                        }
                        return user;
                    }
                })
                .subscribeOn(Schedulers.io())
                .subscribe(new Observer<User>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        MyLog.e(TAG, e);
                        readFromDB();
                    }

                    @Override
                    public void onNext(User user) {
                        mLastInfoTs = System.currentTimeMillis();
                        mMyInfo = user;
                        saveInfoIntoDB();
                        EventBus.getDefault().post(new UserInfoEvent());
                    }
                });
    }

    /**
     * 得到User
     */
    public User getUser() {
        if (mMyInfo == null || mMyInfo.getUid() <= 0) {
            MyLog.w(TAG + " getUser mMyInfo == null || mMyInfo.getUid() <= 0");
            Observable.just(null)
                    .subscribeOn(Schedulers.io())
                    .subscribe(new Action1<Object>() {
                        @Override
                        public void call(Object o) {
                            mMyInfo = readFromDB();
                        }
                    });
        }
        return mMyInfo;
    }


//    // 不能乱删除哦
//    public void deleteUser() {
//        OwnUserInfoDao ownUserInfoDao = GreenDaoManager.getDaoSession(GlobalData.app()).getOwnUserInfoDao();
//        //清空所有数据
//        ownUserInfoDao.deleteAll();
//
//        mMyInfo = new User(); //清空內存中的值
//    }

    long mLastInfoTs = 0;

    public void setLevel(int level) {
        mMyInfo.setLevel(level);
    }

    public void setNickname(String nickname) {
        mMyInfo.setNickname(nickname);
        EventBus.getDefault().post(new UserInfoEvent());
    }

    public void setDiamonds(int deduct, int virtualGemCnt) {
        mMyInfo.setDiamondNum(deduct);
        mMyInfo.setVirtualDiamondNum(virtualGemCnt);
        EventBus.getDefault().post(new UserInfoEvent());
    }

    public void setDiamondNum(int diamondNum) {
        mMyInfo.setDiamondNum(diamondNum);
        EventBus.getDefault().post(new UserInfoEvent());
    }

    public void updateUserInfoIfNeed() {
        if (mMyInfo == null
                || mMyInfo.getUid() <= 0
                || TextUtils.isEmpty(mMyInfo.getNickname())
                || (System.currentTimeMillis() - mLastInfoTs > 5 * 60 * 1000)) {
            syncSelfDetailInfo();
        }
    }
}
