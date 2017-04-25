package com.mi.liveassistant.account;

import android.text.TextUtils;


import com.mi.liveassistant.api.request.GetOwninfoRequest;
import com.mi.liveassistant.common.log.MyLog;
import com.mi.liveassistant.common.thread.ThreadPool;
import com.mi.liveassistant.dao.OwnUserInfo;
import com.mi.liveassistant.data.User;
import com.mi.liveassistant.data.repository.datasource.MyUserInfoLocalStore;
import com.mi.liveassistant.event.UserInfoEvent;
import com.mi.liveassistant.milink.MiLinkClientAdapter;
import com.mi.liveassistant.milink.constant.MiLinkConstant;
import com.mi.liveassistant.proto.UserProto;

import org.greenrobot.eventbus.EventBus;

import java.util.HashMap;
import java.util.Map;

import rx.Observable;
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

    Map<Integer,Long> mLastInfoTsMap = new HashMap<>();

    /**
     * MyUserInfoManager构造函数, 从
     */
    private MyUserInfoManager() {

    }

    public static MyUserInfoManager getInstance() {
        return sInstance;
    }

    public void init() {
//        // 从数据库得到个人信息
        Observable.just(null)
                .observeOn(Schedulers.from(ThreadPool.getUserInfoExecutor()))
                .subscribe(new Action1<Object>() {
                    @Override
                    public void call(Object o) {
                        User userInfo = readFromDB(UserAccountManager.getInstance().getUuidAsLong());
                        if(userInfo != null && userInfo.getUid() == UserAccountManager.getInstance().getUuidAsLong()) {
                            mMyInfo = userInfo;
                        }else{
                            syncSelfDetailInfo();
                        }
                    }
                });
    }

    /**
     * 尝试从数据库中　读取用户信息
     */
    private User readFromDB(long uuid) {
        User user = new User();
        OwnUserInfo ownUserInfo = MyUserInfoLocalStore.getInstance().getAccount(uuid);
        MyLog.w(TAG, "ownUserInfo:" + ownUserInfo);
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
        MyLog.d(TAG, "");
        return user;
    }

    /**
     * 保存数据到DB
     */
    private void saveInfoIntoDB(User user) {
        if (user == null) {
            return;
        }
        OwnUserInfo ownUserInfo = new OwnUserInfo();
//        ownUserInfo.setChannelid(channelId);
        ownUserInfo.setUid(user.getUid());
        ownUserInfo.setNickname(user.getNickname());
        ownUserInfo.setSign(user.getSign());
        ownUserInfo.setAvatar(user.getAvatar());
        ownUserInfo.setGender(user.getGender());
        ownUserInfo.setLevel(user.getLevel());
        ownUserInfo.setBadge(user.getBadge());
        ownUserInfo.setCertification(user.getCertification());
        ownUserInfo.setCertificationType(user.getCertificationType());
        ownUserInfo.setWaitingCertificationType(user.waitingCertificationType);
        ownUserInfo.setIsInspector(user.isInspector());
        ownUserInfo.setLiveTicketNum(user.getLiveTicketNum());
        ownUserInfo.setFansNum(user.getFansNum());
        ownUserInfo.setFollowNum(user.getFollowNum());
        ownUserInfo.setSendDiamondNum(user.getSendDiamondNum());
        ownUserInfo.setVodNum(user.getVodNum());
        ownUserInfo.setEarnNum(user.getEarnNum());
        ownUserInfo.setDiamondNum(user.getDiamondNum());
        ownUserInfo.setSendVirtualDiamondNum(user.getSentVirtualDiamondNum());
        ownUserInfo.setVirtualDiamondNum(user.getVirtualDiamondNum());
        ownUserInfo.setCoverPhotoJson(user.coverPhotoJson);
        ownUserInfo.setFirstAudit(user.firstAudit);
        ownUserInfo.setRedName(user.isRedName());
//        if (LocaleUtil.getSelectedLanguageIndex() != LocaleUtil.INDEX_ENGLISH) {
        ownUserInfo.setRegion(user.getRegion().toByteArray());
//        }
        MyUserInfoLocalStore.getInstance().replaceAccount(ownUserInfo);
    }

    /**
     * 同步自己的个人信息
     */
    public void syncSelfDetailInfo() {
        syncSelfDetailInfo(UserAccountManager.getInstance().getUuidAsLong());
    }

    /**
     * 同步自己的个人信息
     */
    public void syncSelfDetailInfo(final long uuid) {
        MyLog.w(TAG, "syncSelfDetailInfo,uuid="+uuid);
        if(uuid <= 0){
            return;
        }
        Observable.just(0)
                .map(new Func1<Integer, UserProto.GetOwnInfoRsp>() {
                    @Override
                    public UserProto.GetOwnInfoRsp call(Integer integer) {
                        GetOwninfoRequest request = new GetOwninfoRequest(uuid);
                        return request.syncRsp();
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.from(ThreadPool.getUserInfoExecutor()))
                .subscribe(new Action1<UserProto.GetOwnInfoRsp>() {
                    @Override
                    public void call(UserProto.GetOwnInfoRsp rsp) {
                        if (rsp == null || rsp.getErrorCode() != MiLinkConstant.ERROR_CODE_SUCCESS) {
                            MyLog.e(TAG, "rsp==null || rsp.getErrorCode()!=0");
                            User user = readFromDB(uuid);
                            if(user != null && user.getUid() == UserAccountManager.getInstance().getUuidAsLong()){
                                mMyInfo = user;
                            }
                            return;
                        }
                        MyLog.d(TAG,rsp.toString());
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
//                        mLastInfoTsMap.put(channelId, System.currentTimeMillis());
                        saveInfoIntoDB(user);
                        if(user!=null && user.getUid() == UserAccountManager.getInstance().getUuidAsLong()) {
                            mMyInfo = user;
                            EventBus.getDefault().post(new UserInfoEvent());
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        MyLog.e(TAG, throwable);
                        User user = readFromDB(uuid);
                        if(user != null && user.getUid() == UserAccountManager.getInstance().getUuidAsLong()){
                            mMyInfo = user;
                        }
                    }
                });
    }

    /**
     * 得到User
     */
    public User getUser() {
        MyLog.w(TAG,"getUser");
        if (MiLinkClientAdapter.getsInstance().isTouristMode()) {
            return mMyInfo;
        }
        if (mMyInfo == null || mMyInfo.getUid() <= 0) {
            MyLog.w(TAG + " getUser mMyInfo == null || mMyInfo.getUid() <= 0");
            Observable.just(null)
                    .observeOn(Schedulers.from(ThreadPool.getUserInfoExecutor()))
                    .subscribe(new Action1<Object>() {
                        @Override
                        public void call(Object o) {
                            User userInfo = readFromDB(UserAccountManager.getInstance().getUuidAsLong());
                            if(userInfo != null && userInfo.getUid() == UserAccountManager.getInstance().getUuidAsLong()) {
                                mMyInfo = userInfo;
                            }
                        }
                    });
        }
        return mMyInfo;
    }

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
//        Long lastInfoTs = mLastInfoTsMap.get(channelId);
//        lastInfoTs = lastInfoTs == null ? 0 : lastInfoTs;
//        if (mMyInfo == null
//                || mMyInfo.getUid() <= 0
//                || TextUtils.isEmpty(mMyInfo.getNickname())
//                || (System.currentTimeMillis() - lastInfoTs > 5 * 60 * 1000)) {
            syncSelfDetailInfo(UserAccountManager.getInstance().getUuidAsLong());
//        }
    }

    /**
     * 登出时删除用户信息
     */
    public void deleteUser() {
        Observable.just(UserAccountManager.getInstance().getUuidAsLong())
                .map(new Func1<Long, Long>() {
                    @Override
                    public Long call(Long uuid) {
                        MyUserInfoLocalStore.getInstance().deleteAccount(uuid);
                        return uuid;
                    }
                }).subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe(new Action1<Long>() {
                    @Override
                    public void call(Long uuid) {
                        MyLog.w(TAG, "delete userInfo success,uuid=" + uuid);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        MyLog.e(TAG, throwable);
                    }
                });
        mMyInfo = new User(); //清空內存中的值
    }

    public void deleteCache(){
        mMyInfo = new User();
    }

}
