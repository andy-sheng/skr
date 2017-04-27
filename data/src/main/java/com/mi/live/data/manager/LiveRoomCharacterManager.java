package com.mi.live.data.manager;

import android.support.v4.util.LongSparseArray;
import android.text.TextUtils;

import com.base.activity.RxActivity;
import com.base.global.GlobalData;
import com.base.log.MyLog;
import com.base.preference.PreferenceUtils;
import com.base.utils.Constants;
import com.mi.live.data.api.BanSpeakerUtils;
import com.mi.live.data.api.LiveManager;
import com.mi.live.data.event.LiveRoomManagerEvent;
import com.mi.live.data.manager.model.LiveRoomManagerModel;
import com.mi.live.data.preference.PreferenceKeys;
import com.mi.live.data.user.User;
import com.trello.rxlifecycle.ActivityEvent;
import com.wali.live.proto.AccountProto;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by yurui on 3/3/16.
 * <p>
 * 只有房主才需要
 */
public class LiveRoomCharacterManager {

    private static final String TAG = LiveRoomCharacterManager.class.getSimpleName();

    private static LiveRoomCharacterManager sInstance;

    public final static int MANAGER_CNT = 5;

    private List<LiveRoomManagerModel> managerList = new ArrayList<>();  //房主需要知道的list
    //    private List<Long> speakerBanList = new ArrayList<>();          //房主和管理员都需要知道的list
    protected LongSparseArray<Long> topRankMap = new LongSparseArray<>();         //房间id 用户id   直播房间排名第一的人会有管理员权限

    private List<User> speakerBanList = new ArrayList<>();          //房主和管理员都需要知道的list

    private Map<Long, Long> speakerBanMap = new HashMap<>();

    private Subscription mSpeakerBanSubscription;

    public static LiveRoomCharacterManager getInstance() {
        synchronized (LiveRoomCharacterManager.class) {
            if (sInstance == null) {
                sInstance = new LiveRoomCharacterManager();
            }
        }
        return sInstance;
    }

    public void setTopRank(long zuid, long uid) {
        topRankMap.put(zuid, uid);
    }

    public void removeTopRank(long zuid, long uid) {
        if (isTopRank(zuid, uid)) {
            topRankMap.remove(zuid);
        }
    }

    public boolean isTopRank(long zuid, long uid) {
        if (zuid > 0 && topRankMap.get(zuid) != null) {
            if (uid == topRankMap.get(zuid)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 获取主播榜一用户
     *
     * @param anchorId
     * @return
     */
    public Long getTop1Uuid(long anchorId) {
        if (anchorId > 0 && topRankMap.get(anchorId) != null) {
            return topRankMap.get(anchorId);
        }
        return null;
    }

    public boolean isManager(long uuid) {
        List<LiveRoomManagerModel> managerListCopy = new ArrayList<>(managerList);
        for (LiveRoomManagerModel m : managerListCopy) {
            if (m.uuid == uuid) {
                return true;
            }
        }
        return false;
    }

    public LiveRoomManagerModel getManager(long uuid) {
        List<LiveRoomManagerModel> managerListCopy = new ArrayList<>(managerList);
        for (LiveRoomManagerModel m : managerListCopy) {
            if (m.uuid == uuid) {
                return m;
            }
        }
        return null;
    }

    /**
     * 判断主播是否有踢人权限
     *
     * @return
     */
    public boolean haveKickPermission() {
        int permission = PreferenceUtils.getSettingInt(GlobalData.app().getApplicationContext(), PreferenceKeys.PRE_KIK_PERMISSION_ANCHOR, 0);
        return permission == Constants.HAVE_KICK_VIEWER_PERMISSION;
    }

    public int getManagerCount() {
        return managerList.size();
    }

    public List<LiveRoomManagerModel> getRoomManagers() {
        return managerList;
    }

    public List<User> getSpeakerBanList() {
        return speakerBanList;
    }

    public boolean isBanSpeaker(long uuid) {
        for (User user : speakerBanList) {
            if (user.getUid() == uuid) {
                return true;
            }
        }
        return false;
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
        managerList.clear();
        speakerBanList.clear();
        speakerBanMap.clear();
        if (mSpeakerBanSubscription != null && mSpeakerBanSubscription.isUnsubscribed()) {
            mSpeakerBanSubscription.unsubscribe();
        }
    }

    public void setManager(LiveRoomManagerModel manager, boolean isManager) {

        if (isManager) {
            if (managerList.size() < LiveRoomCharacterManager.MANAGER_CNT && !isManager(manager.uuid)) {
                managerList.add(manager);
            }
        } else {
            managerList.remove(manager);
        }
    }

    public void removeManager(long uuid) {
        for (int i = 0; i < managerList.size(); i++) {
            LiveRoomManagerModel manager = managerList.get(i);
            if (manager.uuid == uuid) {
                managerList.remove(i);
                break;
            }
        }
    }

    public void setManagerOnline(long uuid, boolean isOnline) {
        List<LiveRoomManagerModel> managerListCopy = new ArrayList<>(managerList);
        for (LiveRoomManagerModel m : managerListCopy) {
            if (m.uuid == uuid) {
                m.isInRoom = isOnline;
                break;
            }
        }
    }

    public void initBanSpeakerList(RxActivity activity, final long uuid, final long zuid, final String liveId) {
        speakerBanList.clear();
        speakerBanMap.clear();
        mSpeakerBanSubscription = Observable.just(null)
                .compose(activity.bindUntilEvent())
                .observeOn(Schedulers.io())
                .flatMap(new Func1<Object, Observable<List<Long>>>() {
                    @Override
                    public Observable<List<Long>> call(Object s) {
                        return Observable.just(BanSpeakerUtils.getBanSpeakerList(uuid, zuid, liveId));
                    }
                })
                .observeOn(Schedulers.io())
                .flatMap(new Func1<List<Long>, Observable<List<User>>>() {
                    @Override
                    public Observable<List<User>> call(List<Long> longs) {
                        return Observable.just(UserInfoManager.getUserListById(longs));
                    }
                }).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<List<User>>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(List<User> longs) {
                        List<User> copy = new ArrayList<>(speakerBanList);
                        copy.addAll(longs);
                        speakerBanList = copy;
                        for (User user : longs) {
                            speakerBanMap.put(user.getUid(), new Date().getTime());
                        }
                    }
                });
    }

    public void banSpeaker(User user, boolean ebable) {
        int index = -1;
        for (int i = 0; i < speakerBanList.size(); i++) {
            User speakerBan = speakerBanList.get(i);
            if (speakerBan.getUid() == user.getUid()) {
                index = i;
                break;
            }
        }
        if (ebable) {
            if (index == -1) {
                speakerBanList.add(user);
                speakerBanMap.put(user.getUid(), new Date().getTime());
            }
        } else {
            if (index != -1)
                speakerBanList.remove(index);
            if (speakerBanMap.containsKey(user.getUid())) {
                speakerBanMap.remove(user.getUid());
            }
        }
    }

    public static void setManagerRxTask(final RxActivity rxActivity, final User user, final String liveId, final long zuid, final boolean managerEnable) {
        if (TextUtils.isEmpty(liveId) || user == null || rxActivity == null) {
            return;
        }
        final int FAILED = -1,//请求失败
                CANCEL = 0,//取消成功
                OFFLINE = 1,//设置成功但不在线
                ONLINE = 2;//设置成功在线
        Observable.just("")
                .observeOn(Schedulers.io())
                .flatMap(new Func1<String, Observable<Integer>>() {
                    @Override
                    public Observable<Integer> call(String item) {
                        boolean result = UserInfoManager.setManager(user.getUid(), managerEnable, liveId);
                        int res = FAILED;
                        if (result) {
                            res = CANCEL;
                            if (managerEnable) {
                                boolean isInRoom = LiveManager.isInLiveRoom(zuid, liveId, user.getUid());
                                if (isInRoom) {
                                    res = ONLINE;
                                } else {
                                    res = OFFLINE;
                                }
                            }
                        }
                        return Observable.just(res);
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .compose(rxActivity.bindUntilEvent(ActivityEvent.DESTROY))
                .subscribe(new Observer<Object>() {

                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        EventBus.getDefault().post(new LiveRoomManagerEvent(null, false, !managerEnable));
                    }

                    @Override
                    public void onNext(Object o) {
                        Integer result = (Integer) o;
                        if (rxActivity == null || rxActivity.isFinishing()) {
                            return;
                        }
                        if (result > FAILED) {
                            LiveRoomManagerModel manager = new LiveRoomManagerModel(user.getUid());
                            manager.level = user.getLevel();
                            manager.avatar = user.getAvatar();
                            manager.certificationType = user.getCertificationType();

                            manager.isInRoom = result == ONLINE;

                            LiveRoomCharacterManager.getInstance().setManager(manager, managerEnable);
                            List<LiveRoomManagerModel> list = new ArrayList<>();
                            list.add(manager);
                            EventBus.getDefault().post(new LiveRoomManagerEvent(list, true, managerEnable));
                        } else {
                            EventBus.getDefault().post(new LiveRoomManagerEvent(null, false, !managerEnable));
                        }
                    }
                });
    }

    /**
     * 取消管理员
     *
     * @param uuid
     * @param liveId
     * @return Runnable
     */
//    public static Runnable cancelManager(final long uuid, final String liveId) {
//        Runnable runnable = new Runnable() {
//            @Override
//            public void run() {
//                final Boolean result = doInBackground();
//            }
//
//            protected Boolean doInBackground(Void... params) {
//                boolean result = UserInfoManager.setManager(uuid, false, liveId);
//                if (result) {
//                    LiveRoomCharactorManager.getInstance().removeManager(uuid);
//                    LiveRoomCharactorManager.LiveRoomManager manager = new LiveRoomCharactorManager.LiveRoomManager(uuid);
//                    List<LiveRoomCharactorManager.LiveRoomManager> list = new ArrayList<>();
//                    list.add(manager);
//                    EventBus.getDefault().post(new EventClass.LiveRoomManagerEvent(list, true, false));
//                }
//                return result;
//            }
//        };
//        return runnable;
//    }

    /**
     * 取消管理员
     *
     * @param uuid
     * @param liveId
     * @return
     */
    public static boolean cancelManager(final long uuid, final String liveId) {
        boolean result = UserInfoManager.setManager(uuid, false, liveId);
        if (result) {
            LiveRoomCharacterManager.getInstance().removeManager(uuid);
            LiveRoomManagerModel manager = new LiveRoomManagerModel(uuid);
            List<LiveRoomManagerModel> list = new ArrayList<>();
            list.add(manager);
            EventBus.getDefault().post(new LiveRoomManagerEvent(list, true, false));
        }
        return result;
    }

    public void clearManagerCache(){
        managerList.clear();
    }

}
