package com.wali.live.watchsdk.watch.presenter.watchgamepresenter;

import android.os.Build;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.LruCache;

import com.base.global.GlobalData;
import com.base.log.MyLog;
import com.base.preference.PreferenceUtils;
import com.mi.live.data.manager.UserInfoManager;
import com.mi.live.data.query.model.ViewerModel;
import com.mi.live.data.room.model.RoomBaseDataModel;
import com.mi.live.data.room.model.RoomDataChangeEvent;
import com.mi.live.data.user.User;
import com.thornbirds.component.IParams;
import com.wali.live.component.presenter.BaseSdkRxPresenter;
import com.wali.live.event.UserActionEvent;
import com.wali.live.watchsdk.component.WatchComponentController;
import com.wali.live.watchsdk.watch.view.watchgameview.WatchGameViewerTabView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class ViewerRankPresenter extends BaseSdkRxPresenter<WatchGameViewerTabView.IView> implements WatchGameViewerTabView.IPresenter {

    private static final String TAG = "ViewerRankPresenter";

    private static final String PRE_KEY_LAST_UPDATE_VIEWERS_TIME = "pre_key_last_update_viewers_time";

    private RoomBaseDataModel mMyRoomData;

    private long lastTime = 0; //上次更新时间

    //保存 name 保存观众的昵称
    static final int MAX_SIZE = 1000;
    private LruCache<Long, String> mUserInfoCache = new LruCache<>(MAX_SIZE);

    public ViewerRankPresenter(@NonNull WatchComponentController controller) {
        super(controller);
        mMyRoomData = controller.getRoomBaseDataModel();
        initViewers(controller.getRoomBaseDataModel().getViewersList());
    }

    Subscription mViewerChangeSubscription;

    public void initViewers(final List<ViewerModel> dataList) {
        if (dataList == null || dataList.size() == 0) {
            return;
        }

        updateUi(dataList);
    }


    public void updateViewers(final List<ViewerModel> dataList) {

        if (dataList == null || dataList.size() == 0) {
            return;
        }

        long now = System.currentTimeMillis();
        if ((now - lastTime) < 3 * 1000) {
            return;
        } else {
            lastTime = now;
        }

        updateUi(dataList);
    }

    private void updateUi(final List<ViewerModel> dataList) {
        if (mViewerChangeSubscription != null && !mViewerChangeSubscription.isUnsubscribed()) {
            return;
        }

        mViewerChangeSubscription = Observable.create(new Observable.OnSubscribe<List<ViewerModel>>() {
            @Override
            public void call(Subscriber<? super List<ViewerModel>> subscriber) {

                final List<Long> uuidList = new ArrayList<>();
                //保存uid和观众model
                HashMap<Long, ViewerModel> mViwersMap = new HashMap<>();

                for (ViewerModel model : dataList) {
                    if (TextUtils.isEmpty(model.getNickName())) {
                        if (TextUtils.isEmpty(mUserInfoCache.get(model.getUid()))) {
                            uuidList.add(model.getUid());
                            mViwersMap.put(model.getUid(), model);
                        } else {
                            model.setNickName(mUserInfoCache.get(model.getUid()));
                        }
                    }
                }

                if (uuidList != null && uuidList.size() > 0) {
                    List<User> userServer = UserInfoManager.getUserListById(uuidList);
                    saveViewerInfo(userServer);
                    for (User user : userServer) {
                        ViewerModel model = mViwersMap.get(user.getUid());
                        if (model != null) {
                            model.setNickName(user.getNickname());
                        }
                    }
                }

                subscriber.onNext(dataList);
                subscriber.onCompleted();
            }
        }).flatMap(new Func1<List<ViewerModel>, Observable<List<ViewerModel>>>() {
            @Override
            public Observable<List<ViewerModel>> call(List<ViewerModel> list) {
                List<ViewerModel> temp = new ArrayList<>(list);
                Collections.sort(temp, new Comparator<ViewerModel>() {
                    @Override
                    public int compare(ViewerModel lhs, ViewerModel rhs) {

                        return rhs.getLevel() - lhs.getLevel();
                    }
                });
                return Observable.just(temp);
            }
        }).subscribeOn(Schedulers.io())
                .compose(this.<List<ViewerModel>>bindUntilEvent(PresenterEvent.DESTROY))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<List<ViewerModel>>() {

                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        MyLog.d(TAG, e);
                    }

                    @Override
                    public void onNext(List<ViewerModel> list) {
                        MyLog.d(TAG, "onNext" + " list=" + list);
                        mView.initViewers(list);
                    }
                });
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(RoomDataChangeEvent event) {
        switch (event.type) {
            case RoomDataChangeEvent.TYPE_CHANGE_USER_INFO_COMPLETE: {
                initViewers(event.source.getViewersList());
            }
            break;
            case RoomDataChangeEvent.TYPE_CHANGE_TICKET:
            case RoomDataChangeEvent.TYPE_CHANGE_VIEWER_COUNT: {

            }
            break;
            case RoomDataChangeEvent.TYPE_CHANGE_VIEWERS: {
                updateViewers(event.source.getViewersList());
            }
            break;
            default:
                break;
        }
    }

    @Override
    protected String getTAG() {
        return null;
    }

    @Override
    public boolean onEvent(int i, IParams iParams) {
        return false;
    }

    @Override
    public void startPresenter() {
        super.startPresenter();
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }

    @Override
    public void stopPresenter() {
        super.stopPresenter();
        if (mUserInfoCache != null) {
            mUserInfoCache.evictAll();
        }
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
    }

    public void saveViewerInfo(long uid, String nickName) {
        if (uid == 0 || TextUtils.isEmpty(nickName)) {
            return;
        }

        mUserInfoCache.put(uid, nickName);
        if (mUserInfoCache.size() > MAX_SIZE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                MyLog.w(TAG, "buddyCache size = " + mUserInfoCache.size() + ",trime");
                mUserInfoCache.trimToSize(MAX_SIZE / 2);
            }
        }
    }

    public void saveViewerInfo(List<User> list) {
        if (null != list && list.size() > 0) {
            for (User user : list) {
                saveViewerInfo(user.getUid(), user.getNickname());
            }
        }
    }

    @Override
    public void postAvatarEvent(int eventTypeRequestLookMoreViewer, int itemCount) {
        if (mMyRoomData.getViewerCnt() > itemCount) {
            UserActionEvent.post(eventTypeRequestLookMoreViewer, mMyRoomData, null);
        }
    }
}
