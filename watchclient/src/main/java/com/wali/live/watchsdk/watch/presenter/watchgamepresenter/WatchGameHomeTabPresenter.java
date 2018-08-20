package com.wali.live.watchsdk.watch.presenter.watchgamepresenter;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.CallSuper;
import android.support.v4.content.FileProvider;
import android.text.TextUtils;

import com.base.global.GlobalData;
import com.base.log.MyLog;
import com.base.utils.MD5;
import com.base.utils.system.PackageUtils;
import com.mi.live.data.gamecenter.model.GameInfoModel;
import com.mi.live.data.room.model.RoomDataChangeEvent;
import com.thornbirds.component.IParams;
import com.wali.live.component.presenter.BaseSdkRxPresenter;
import com.wali.live.utils.FileUtils;
import com.wali.live.watchsdk.component.WatchComponentController;
import com.wali.live.watchsdk.watch.download.CustomDownloadManager;
import com.wali.live.watchsdk.watch.view.watchgameview.WatchGameHomeTabView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;

/**
 * Created by vera on 2018/8/8.
 */

public class WatchGameHomeTabPresenter extends BaseSdkRxPresenter<WatchGameHomeTabView.IView>
        implements WatchGameHomeTabView.IPresenter {
    private static final String TAG = "WatchGameHomeTabPresenter";

    GameInfoModel mGameInfoModel;

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
        String key = MD5.MD5_32(mGameInfoModel.getPackageUrl());
        if (!TextUtils.isEmpty(event.downloadKey)) {
            if (event.downloadKey.equals(key)) {
                mView.updateDownLoadUi(event.status, event.progress, event.reason, mGameInfoModel);
            }
        } else if (event.status == CustomDownloadManager.ApkStatusEvent.STATUS_LAUNCH) {
            // 安装应用
            if (event.packageName.equals(mGameInfoModel.getPackageName())) {
                mView.updateDownLoadUi(event.status, event.progress, event.reason, mGameInfoModel);
            }
        } else if (event.status == CustomDownloadManager.ApkStatusEvent.STATUS_REMOVE) {
            // 卸载应用
            if (event.packageName.equals(mGameInfoModel.getPackageName())) {
                mView.updateDownLoadUi(event.status, event.progress, event.reason, mGameInfoModel);
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(CustomDownloadManager.TaskEvent event) {
        String key = MD5.MD5_32(mGameInfoModel.getPackageUrl());
        if (event.downloadKey.equals(key)) {
            mView.notifyTaskRemove(event.status);
        }
    }


    @Override
    public void beginDownload() {
        CustomDownloadManager.Item item = new CustomDownloadManager.Item(mGameInfoModel.getPackageUrl(), mGameInfoModel.getGameName());
        CustomDownloadManager.getInstance().beginDownload(item,mView.getRealView().getContext());
    }

    @Override
    public void pauseDownload() {
        CustomDownloadManager.getInstance().pauseDownload(mGameInfoModel.getPackageUrl());
    }

    @Override
    public boolean tryInstall() {
        String apkPath = CustomDownloadManager.getInstance().getDownloadPath(mGameInfoModel.getPackageUrl());
        return PackageUtils.tryInstall(apkPath);
    }

    @Override
    public boolean tryLaunch() {
        return PackageUtils.tryLaunch(mGameInfoModel.getPackageName());
    }
}
