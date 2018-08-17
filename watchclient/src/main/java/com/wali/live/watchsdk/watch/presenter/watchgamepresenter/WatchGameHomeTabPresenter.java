package com.wali.live.watchsdk.watch.presenter.watchgamepresenter;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.CallSuper;
import android.support.v4.content.FileProvider;
import android.text.TextUtils;

import com.base.global.GlobalData;
import com.base.log.MyLog;
import com.base.preference.PreferenceUtils;
import com.base.utils.toast.ToastUtils;
import com.mi.live.data.gamecenter.model.GameInfoModel;
import com.mi.live.data.room.model.RoomDataChangeEvent;
import com.thornbirds.component.IParams;
import com.wali.live.component.presenter.BaseSdkRxPresenter;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.component.WatchComponentController;
import com.wali.live.watchsdk.editinfo.fragment.presenter.EditAvatarPresenter;
import com.wali.live.watchsdk.eventbus.EventClass;
import com.wali.live.watchsdk.watch.download.GameDownLoadUtil;
import com.wali.live.watchsdk.watch.view.watchgameview.WatchGameHomeTabView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

import static android.content.Context.DOWNLOAD_SERVICE;

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
        GameDownLoadUtil.getInstance().init(mGameInfoModel);
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
        }
    }

    @Override
    @CallSuper
    public void stopPresenter() {
        super.stopPresenter();
        EventBus.getDefault().unregister(this);
        GameDownLoadUtil.getInstance().destory();
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

    public void tryInstallApk() {
        Intent intent = new Intent(Intent.ACTION_VIEW);

        String filename = mGameInfoModel.getGameId() + ".apk";
        // 由于COLUMN_LOCAL_FILENAME废弃，生成固定的下载路径
        File file = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        String mDownloadFilename = Uri.withAppendedPath(Uri.fromFile(file), filename).getPath();

        Uri uri;
        // 判断版本大于等于7.0
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            // 待补充
            uri = FileProvider.getUriForFile(mView.getRealView().getContext(), EditAvatarPresenter.AUTHORITY, new File(mDownloadFilename));
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        } else {
            uri = Uri.fromFile(new File(mDownloadFilename));
        }

        intent.setDataAndType(uri, "application/vnd.android.package-archive");
        mView.getRealView().getContext().startActivity(intent);
    }


    public void tryLaunchApk() {
        String packageName = mGameInfoModel.getPackageName();
        Intent intent = mView.getRealView().getContext().getPackageManager().getLaunchIntentForPackage(packageName);
        if (intent != null) {
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mView.getRealView().getContext().startActivity(intent);
        } else {
            MyLog.w(TAG, "intent launch fail, packageName=" + packageName);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(EventClass.GameDownLoadEvent event) {
        if (null != event && event.gameId == mGameInfoModel.getGameId()) {
            mView.updateDownLoadUi(event.status, event.progress);
        }
    }

    @Override
    public void beginDownload() {
        GameDownLoadUtil.getInstance().beginDownload(mGameInfoModel);
    }
}
