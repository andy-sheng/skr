package com.wali.live.watchsdk.watch.presenter.watchgamepresenter;

import android.support.annotation.CallSuper;
import android.text.TextUtils;

import com.base.log.MyLog;
import com.base.utils.MD5;
import com.base.utils.system.PackageUtils;
import com.mi.live.data.gamecenter.model.GameInfoModel;
import com.mi.live.data.room.model.RoomDataChangeEvent;
import com.thornbirds.component.IParams;
import com.wali.live.component.presenter.BaseSdkRxPresenter;
import com.wali.live.watchsdk.component.WatchComponentController;
import com.wali.live.watchsdk.statistics.MilinkStatistics;
import com.wali.live.watchsdk.watch.download.CustomDownloadManager;
import com.wali.live.watchsdk.watch.download.GameDownloadOptControl;
import com.wali.live.watchsdk.watch.model.WatchGameInfoConfig;
import com.wali.live.watchsdk.watch.view.watchgameview.WatchGameHomeTabView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import static com.wali.live.component.BaseSdkController.MSG_PLAYER_PAUSE;
import static com.wali.live.watchsdk.statistics.item.GameWatchDownloadStatisticItem.GAME_WATCH_BIZTYPE_GAME_HOME_PAGE_CLICK;
import static com.wali.live.watchsdk.statistics.item.GameWatchDownloadStatisticItem.GAME_WATCH_TYPE_CLICK;
import static com.wali.live.watchsdk.watch.download.CustomDownloadManager.ApkStatusEvent.STATUS_DOWNLOADING;
import static com.wali.live.watchsdk.watch.download.CustomDownloadManager.ApkStatusEvent.STATUS_DOWNLOAD_COMPELED;
import static com.wali.live.watchsdk.watch.download.CustomDownloadManager.ApkStatusEvent.STATUS_INSTALLING;
import static com.wali.live.watchsdk.watch.download.CustomDownloadManager.ApkStatusEvent.STATUS_LAUNCH;
import static com.wali.live.watchsdk.watch.download.CustomDownloadManager.ApkStatusEvent.STATUS_PAUSE_DOWNLOAD;

/**
 * Created by vera on 2018/8/8.
 */

public class WatchGameHomeTabPresenter extends BaseSdkRxPresenter<WatchGameHomeTabView.IView>
        implements WatchGameHomeTabView.IPresenter {
    private static final String TAG = "WatchGameHomeTabPresenter";

    GameInfoModel mGameInfoModel;

    private boolean mIsDownLoadByGc;

    public WatchGameHomeTabPresenter(WatchComponentController controller) {
        super(controller);
        mGameInfoModel = controller.getRoomBaseDataModel().getGameInfoModel();
    }

    @Override
    protected String getTAG() {
        return TAG;
    }

    @Override
    public boolean onEvent(int event, IParams params) {
        return false;
    }

    @CallSuper
    public void startPresenter() {
        super.startPresenter();
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
        if (mView != null) {
            mView.updateUi(mGameInfoModel);
            if (mGameInfoModel != null) {
                CustomDownloadManager.getInstance().addMonitorUrl(mGameInfoModel.getPackageUrl());
            }
        }
    }

    @Override
    @CallSuper
    public void stopPresenter() {
        super.stopPresenter();
        EventBus.getDefault().unregister(this);
        if (mGameInfoModel != null) {
            CustomDownloadManager.getInstance().removeMonitorUrl(mGameInfoModel.getPackageUrl());
        }
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(RoomDataChangeEvent event) {
        switch (event.type) {
            case RoomDataChangeEvent.TYPE_CHANGE_GAME_INFO: {
                if (mView != null) {
                    mView.updateUi(mGameInfoModel);
                }
            }
            break;
            default:
                break;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(CustomDownloadManager.ApkStatusEvent event) {
        MyLog.d(TAG, "event:" + event.gameId + "," + event.progress);

        if (mGameInfoModel == null) {
            return;
        }

        if(event.isByGame) {
            mIsDownLoadByGc= true;
            if(mGameInfoModel.getGameId() == event.gameId) {
                mView.updateDownLoadUi(event.status, event.progress, event.reason, mGameInfoModel);
                switch (event.status) {
                    case STATUS_DOWNLOADING:
                    case STATUS_PAUSE_DOWNLOAD:
                    case STATUS_INSTALLING:
                    case STATUS_LAUNCH:
                        break;
                    case STATUS_DOWNLOAD_COMPELED:
                        //TODO-
//                        if (PackageUtils.tryLaunch(mGameInfoModel.getPackageName())) {
//                            postEvent(MSG_PLAYER_PAUSE);
//                        } else {
//                        }
                        break;
                }
            }
        } else {
            if (!TextUtils.isEmpty(event.downloadKey)) {
                String key = MD5.MD5_32(mGameInfoModel.getPackageUrl());
                if (event.downloadKey.equals(key)) {
                    mView.updateDownLoadUi(event.status, event.progress, event.reason, mGameInfoModel);
                    // 下载完成，尝试自动安装
                    if (event.status == STATUS_DOWNLOAD_COMPELED) {
                        //取暂停视频播放
                        String apkPath = CustomDownloadManager.getInstance().getDownloadPath(mGameInfoModel.getPackageUrl());
                        if (PackageUtils.tryInstall(apkPath)) {
                            postEvent(MSG_PLAYER_PAUSE);
                        } else {
                        }
                        // 下载完成都不再监听 关于这个包的事件
                        CustomDownloadManager.getInstance().removeMonitorUrl(mGameInfoModel.getPackageUrl());
                    }
                }
            } else if (event.status == CustomDownloadManager.ApkStatusEvent.STATUS_LAUNCH) {
                // 安装应用
                if (TextUtils.isEmpty(event.packageName)) {
                    return;
                }
                if (event.packageName.equals(mGameInfoModel.getPackageName())) {
                    mView.updateDownLoadUi(event.status, event.progress, event.reason, mGameInfoModel);
                }
            } else if (event.status == CustomDownloadManager.ApkStatusEvent.STATUS_REMOVE) {
                // 卸载应用
                if (TextUtils.isEmpty(event.packageName)) {
                    return;
                }
                if (event.packageName.equals(mGameInfoModel.getPackageName())) {
                    mView.updateDownLoadUi(event.status, event.progress, event.reason, mGameInfoModel);
                }
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(CustomDownloadManager.TaskEvent event) {
        if (mIsDownLoadByGc || mGameInfoModel == null || event.downloadKey == null) {
            return;
        }
        String key = MD5.MD5_32(mGameInfoModel.getPackageUrl());
        if (event.downloadKey.equals(key)) {
            mView.notifyTaskRemove(event.status);
        }
    }


    @Override
    public void beginDownload(boolean isFirst) {
        int type = GameDownloadOptControl.TYPE_GAME_CONTINUE_DOWNLOAD;

        if (isFirst) {
            clickDownloadStatistic();
            type = GameDownloadOptControl.TYPE_GAME_BEGIN_DOWNLOAD;
        }

        //TODO
//        CustomDownloadManager.Item item = new CustomDownloadManager.Item(mGameInfoModel.getPackageUrl(), mGameInfoModel.getGameName());
//        CustomDownloadManager.getInstance().beginDownload(item, mView.getRealView().getContext());
        GameDownloadOptControl.tryDownloadGame(type, mGameInfoModel);
    }

    @Override
    public void pauseDownload() {
//        CustomDownloadManager.getInstance().pauseDownload(mGameInfoModel.getPackageUrl());
        GameDownloadOptControl.tryDownloadGame(GameDownloadOptControl.TYPE_GAME_PAUSE_DOWNLOAD, mGameInfoModel);
    }

    @Override
    public void tryInstall() {

        if(!mIsDownLoadByGc) {
//取暂停视频播放
            String apkPath = CustomDownloadManager.getInstance().getDownloadPath(mGameInfoModel.getPackageUrl());
            if (PackageUtils.tryInstall(apkPath)) {
                postEvent(MSG_PLAYER_PAUSE);
            }
        } else {
        }
    }

    @Override
    public boolean tryLaunch() {
        if(PackageUtils.tryLaunch(mGameInfoModel.getPackageName())){
            postEvent(MSG_PLAYER_PAUSE);
            return true;
        }else {
            return false;
        }
    }

    private void clickDownloadStatistic() {
        if (mGameInfoModel == null) {
            return;
        }
        String url = mGameInfoModel.getPackageUrl();
        WatchGameInfoConfig.InfoItem infoItem = WatchGameInfoConfig.sGameInfoMap.get(url);
        if (infoItem != null) {
            MilinkStatistics.getInstance().statisticGameWatchDownload(GAME_WATCH_TYPE_CLICK,
                    GAME_WATCH_BIZTYPE_GAME_HOME_PAGE_CLICK, infoItem.anchorId, infoItem.channelId, infoItem.packageName);
        }
    }
}
