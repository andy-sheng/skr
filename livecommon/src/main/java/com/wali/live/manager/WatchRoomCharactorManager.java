package com.wali.live.manager;

import com.base.global.GlobalData;
import com.base.preference.PreferenceUtils;
import com.base.utils.Constants;
import com.mi.live.data.account.MyUserInfoManager;
import com.mi.live.data.api.BanSpeakerUtils;
import com.mi.live.data.manager.LiveRoomCharactorManager;
import com.mi.live.data.preference.PreferenceKeys;
import com.mi.live.data.manager.model.LiveRoomManagerModel;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by yurui on 3/3/16.
 * <p>
 * 只有管理员才需要
 */
public class WatchRoomCharactorManager {
    private static WatchRoomCharactorManager sInstance;

    private LiveRoomManagerModel mManager;

    private Map<Long,Long> speakerBanMap = new HashMap<>();

    public static WatchRoomCharactorManager getInstance() {
        synchronized (WatchRoomCharactorManager.class) {
            if (sInstance == null) {
                sInstance = new WatchRoomCharactorManager();
            }
        }
        return sInstance;
    }

    public boolean isManager(long uuid) {
        return mManager != null && mManager.uuid == uuid;
    }

    /**
     * 判断自己是不是管理员
     * @return
     */
    public boolean isManager() {
        return mManager != null && mManager.uuid == MyUserInfoManager.getInstance().getUser().getUid();
    }

    /**
     * 判断管理员是否有踢人权限
     *
     * @return
     */
    public boolean judgeManagerHaveClickPermission() {
        int permission = PreferenceUtils.getSettingInt(GlobalData.app().getApplicationContext(), PreferenceKeys.PRE_KIK_PERMISSION_ADMIN, 0);
        return permission == Constants.HAVE_KICK_VIEWER_PERMISSION;
    }

    /**
     * 判断榜一是否有踢人权限
     *
     * @return
     */
    public boolean judgeTop1HaveClickPermission() {
        int permission = PreferenceUtils.getSettingInt(GlobalData.app().getApplicationContext(), PreferenceKeys.PRE_KIK_PERMISSION_TOP1, 0);
        return permission == Constants.HAVE_KICK_VIEWER_PERMISSION;
    }

    public boolean isBanSpeaker(long uuid) {
        return speakerBanMap.containsKey(uuid);
    }

    /**
     * 获取被禁言的时间
     *
     * @param uuid
     * @return
     */
    public Long getBannedTime(long uuid) {
        return speakerBanMap.get(uuid);
    }

    public void clear() {
        mManager = null;
        speakerBanMap.clear();
    }

    public void setManager(LiveRoomManagerModel manager) {
        mManager = manager;
    }

    public void banSpeaker(long uuid, boolean ebable) {
        if (ebable) {
            if (!speakerBanMap.containsKey(uuid)) {
                speakerBanMap.put(uuid, new Date().getTime());
            }
        } else {
            if (speakerBanMap.containsKey(uuid)) {
                speakerBanMap.remove(uuid);
            }
        }
    }

    private boolean isLoading = false;

    public static void initBanSpeakerList(final long uuid, final long zuid, final String liveId) {
        if (!getInstance().isLoading) {
            getInstance().isLoading = true;
            getInstance().speakerBanMap.clear();

            Observable.create(
                new Observable.OnSubscribe<List<Long>>() {
                    @Override
                    public void call(Subscriber<? super List<Long>> subscriber) {
                        subscriber.onNext(BanSpeakerUtils.getBanSpeakerList(uuid, zuid, liveId));
                        subscriber.onCompleted();
                    }
                }
            )
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(new Subscriber<List<Long>>() {
                @Override
                public void onCompleted() {
                    getInstance().isLoading = false;
                }

                @Override
                public void onError(Throwable e) {

                }

                @Override
                public void onNext(List<Long> result) {
                    if (result != null && result.size() > 0) {
                        for (Long id : result) {
                            if (!getInstance().speakerBanMap.containsKey(id)) {
                                getInstance().speakerBanMap.put(id, new Date().getTime());
                            }
                        }
                    }
                }
            });
        }
    }

    public boolean isInspector() {
        return MyUserInfoManager.getInstance().getUser().isInspector();
    }

    /**
     * 判断是否有管理员权限
     *
     * @param anchorId 主播ID
     * @return
     */
    public boolean hasManagerPower(long anchorId) {
        return isInspector() || LiveRoomCharactorManager.getInstance().isTopRank(anchorId, MyUserInfoManager.getInstance().getUser().getUid()) || isManager(MyUserInfoManager.getInstance().getUser().getUid());
    }

    /**
     * 是否是榜一
     *
     * @param anchorId 主播ID
     * @return
     */
    public boolean isTop1(long anchorId,long uuid) {
        return LiveRoomCharactorManager.getInstance().isTopRank(anchorId, uuid);
    }

    /**
     * 判断是否有踢人权限
     *
     * @param anchorId
     * @param uuid
     * @return
     */
    public boolean haveKickPermission(long anchorId,long uuid){
        if(isManager(uuid)&&judgeManagerHaveClickPermission()){
            return true;
        }else if(isTop1(anchorId,uuid)&&judgeTop1HaveClickPermission()){
            return true;
        }
        return false;
    }
}