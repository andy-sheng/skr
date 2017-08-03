package com.mi.live.data.account;

import android.text.TextUtils;

import com.base.log.MyLog;
import com.base.thread.ThreadPool;
import com.base.utils.language.LocaleUtil;
import com.mi.live.data.account.channel.HostChannelManager;
import com.mi.live.data.account.event.UserInfoEvent;
import com.mi.live.data.api.request.GetOwnInfoRequest;
import com.mi.live.data.milink.MiLinkClientAdapter;
import com.mi.live.data.milink.constant.MiLinkConstant;
import com.mi.live.data.repository.datasource.MyUserInfoLocalStore;
import com.mi.live.data.user.User;
import com.wali.live.dao.OwnUserInfo;
import com.wali.live.proto.UserProto;

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
    private Map<Integer, Long> mLastInfoTsMap = new HashMap<>();

    private MyUserInfoManager() {
    }

    public static MyUserInfoManager getInstance() {
        return sInstance;
    }

    /**
     * 从数据库得到个人信息
     */
    public void init() {
        Observable.just(null)
                .observeOn(Schedulers.from(ThreadPool.getUserInfoExecutor()))
                .subscribe(new Action1<Object>() {
                    @Override
                    public void call(Object o) {
                        User userInfo = readFromDB(HostChannelManager.getInstance().getChannelId());
                        if (userInfo != null && userInfo.getUid() == UserAccountManager.getInstance().getUuidAsLong()) {
                            mMyInfo = userInfo;
                        } else {
                            syncSelfDetailInfo();
                        }
                    }
                });
    }

    /**
     * 尝试从数据库中　读取用户信息
     */
    private User readFromDB(int channelId) {
        User user = new User();
        OwnUserInfo ownUserInfo = MyUserInfoLocalStore.getInstance().getAccount(channelId);
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
        MyLog.w(TAG, "ownUserInfo:" + user.toString());
        return user;
    }

    /**
     * 保存数据到DB
     */
    private void saveInfoIntoDB(User user, final int channelId) {
        if (user == null) {
            return;
        }
        OwnUserInfo ownUserInfo = new OwnUserInfo();
        ownUserInfo.setChannelid(channelId);
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
        if (LocaleUtil.getSelectedLanguageIndex() != LocaleUtil.INDEX_ENGLISH) {
            ownUserInfo.setRegion(user.getRegion().toByteArray());
        }
        MyUserInfoLocalStore.getInstance().replaceAccount(ownUserInfo, channelId);
    }

    /**
     * 同步自己的个人信息
     */
    public void syncSelfDetailInfo() {
        final long uuid = UserAccountManager.getInstance().getUuidAsLong();
        final int channelId = HostChannelManager.getInstance().getChannelId();
        MyLog.w(TAG, "syncSelfDetailInfo,uuid=" + uuid + " channelId=" + channelId);
        if (uuid <= 0) {
            return;
        }
        Observable.just(0)
                .map(new Func1<Integer, UserProto.GetOwnInfoRsp>() {
                    @Override
                    public UserProto.GetOwnInfoRsp call(Integer integer) {
                        GetOwnInfoRequest request = new GetOwnInfoRequest(uuid);
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
                            User user = readFromDB(channelId);
                            if (user != null && user.getUid() == UserAccountManager.getInstance().getUuidAsLong()) {
                                mMyInfo = user;
                            }
                            return;
                        }
                        MyLog.d(TAG, rsp.toString());
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
                        mLastInfoTsMap.put(channelId, System.currentTimeMillis());
                        saveInfoIntoDB(user, channelId);
                        if (channelId == HostChannelManager.getInstance().getChannelId() && user != null
                                && user.getUid() == UserAccountManager.getInstance().getUuidAsLong()) {
                            // TODO 应该这里出问题了
                            mMyInfo = user;
                            EventBus.getDefault().post(new UserInfoEvent());
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        MyLog.e(TAG, throwable);
                        User user = readFromDB(channelId);
                        if (user != null && user.getUid() == UserAccountManager.getInstance().getUuidAsLong()) {
                            mMyInfo = user;
                        }
                    }
                });
    }

    /**
     * 得到User
     */
    public User getUser() {
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
                            User userInfo = readFromDB(HostChannelManager.getInstance().getChannelId());
                            if (userInfo != null && userInfo.getUid() == UserAccountManager.getInstance().getUuidAsLong()) {
                                mMyInfo = userInfo;
                            }
                        }
                    });
        }
        return mMyInfo;
    }

    public long getAvatar() {
        return mMyInfo != null ? mMyInfo.getAvatar() : 0;
    }

    public long getUuid() {
        if (mMyInfo != null && mMyInfo.getUid() != 0) {
            return mMyInfo.getUid();
        }
        return UserAccountManager.getInstance().getUuidAsLong();
    }

    public void setLevel(int level) {
        mMyInfo.setLevel(level);
    }

    public int getLevel() {
        return mMyInfo != null ? mMyInfo.getLevel() : 0;
    }

    public void setNickname(String nickname) {
        mMyInfo.setNickname(nickname);
        EventBus.getDefault().post(new UserInfoEvent());
    }

    public String getNickname() {
        if (mMyInfo != null && !TextUtils.isEmpty(mMyInfo.getNickname())) {
            return mMyInfo.getNickname();
        }
        return UserAccountManager.getInstance().getNickname();
    }

    public synchronized void setDiamonds(int diamondNum, int virtualGemCnt) {
        mMyInfo.setDiamondNum(diamondNum);
        mMyInfo.setVirtualDiamondNum(virtualGemCnt);
        EventBus.getDefault().post(new UserInfoEvent());
    }

    public synchronized void setDiamondNum(int diamondNum) {
        mMyInfo.setDiamondNum(diamondNum);
        EventBus.getDefault().post(new UserInfoEvent());
    }

    public synchronized int getDiamondNum() {
        return mMyInfo.getDiamondNum();
    }

    public synchronized int getVirtualDiamondNum() {
        return mMyInfo.getVirtualDiamondNum();
    }

    public synchronized void setVirtualDiamondNum(int vDiamondNum) {
        MyLog.w(TAG, "set virtual diamond to:" + vDiamondNum);
        mMyInfo.setVirtualDiamondNum(vDiamondNum);
        EventBus.getDefault().post(new UserInfoEvent()); //发送event
    }

    public void updateUserInfoIfNeed() {
        Long lastInfoTs = mLastInfoTsMap.get(HostChannelManager.getInstance().getChannelId());
        lastInfoTs = lastInfoTs == null ? 0 : lastInfoTs;
        if (mMyInfo == null
                || mMyInfo.getUid() <= 0
                || TextUtils.isEmpty(mMyInfo.getNickname())
                || (System.currentTimeMillis() - lastInfoTs > 5 * 60 * 1000)) {
            syncSelfDetailInfo();
        }
    }

    /**
     * 登出时删除用户信息
     */
    public void deleteUser() {
        Observable.just(HostChannelManager.getInstance().getChannelId())
                .map(new Func1<Integer, Integer>() {
                    @Override
                    public Integer call(Integer channelId) {
                        MyUserInfoLocalStore.getInstance().deleteAccount(channelId);
                        return channelId;
                    }
                }).subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe(new Action1<Integer>() {
                    @Override
                    public void call(Integer channelId) {
                        MyLog.w(TAG, "delete userInfo success,channelId=" + channelId);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        MyLog.e(TAG, throwable);
                    }
                });
        mMyInfo = new User(); //清空內存中的值
    }

    public void deleteCache() {
        mMyInfo = new User();
    }
}
