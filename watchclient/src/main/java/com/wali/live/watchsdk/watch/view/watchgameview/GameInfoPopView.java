package com.wali.live.watchsdk.watch.view.watchgameview;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.base.global.GlobalData;
import com.base.image.fresco.BaseImageView;
import com.base.image.fresco.FrescoWorker;
import com.base.image.fresco.image.BaseImage;
import com.base.image.fresco.image.HttpImage;
import com.base.utils.MD5;
import com.base.utils.system.PackageUtils;
import com.base.utils.toast.ToastUtils;
import com.mi.live.data.gamecenter.model.GameInfoModel;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.statistics.MilinkStatistics;
import com.wali.live.watchsdk.watch.download.CustomDownloadManager;
import com.wali.live.watchsdk.watch.download.GameDownloadOptControl;
import com.wali.live.watchsdk.watch.model.WatchGameInfoConfig;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import static com.wali.live.component.BaseSdkController.MSG_PLAYER_PAUSE;
import static com.wali.live.watchsdk.statistics.item.GameWatchDownloadStatisticItem.GAME_WATCH_BIZTYPE_DOWNLOAD_COMPLETED;
import static com.wali.live.watchsdk.statistics.item.GameWatchDownloadStatisticItem.GAME_WATCH_BIZTYPE_POP_CLICK;
import static com.wali.live.watchsdk.statistics.item.GameWatchDownloadStatisticItem.GAME_WATCH_BIZTYPE_POP_EXPOSURE;
import static com.wali.live.watchsdk.statistics.item.GameWatchDownloadStatisticItem.GAME_WATCH_TYPE_CLICK;
import static com.wali.live.watchsdk.statistics.item.GameWatchDownloadStatisticItem.GAME_WATCH_TYPE_EXPOSURE;
import static com.wali.live.watchsdk.watch.download.CustomDownloadManager.InstallOrLaunchEvent.STATTUS_INSTALL;
import static com.wali.live.watchsdk.watch.download.CustomDownloadManager.InstallOrLaunchEvent.STATTUS_LAUNCH;
import static com.wali.live.watchsdk.watch.download.CustomDownloadManager.InstallOrLaunchEvent.SUCCESS;

/**
 * Created by liuting on 18-8-20.
 */

public class GameInfoPopView extends RelativeLayout {
    private GameInfoModel mGameInfoModel;
    private int mApkStatus = -1;

    private BaseImageView mGameIconIv;
    private View mGameIconShadow;
    private ProgressBar mDownloadProgressBar;
    private TextView mBottomText;

    private boolean mIsDownloadByGc;

    private OnInstallOrLaunchListener mOnInstallOrLaunchListener;

    public GameInfoPopView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
    }

    private void init(Context context) {
        inflate(context, R.layout.game_info_pop_layout, this);

        mGameIconIv = (BaseImageView) findViewById(R.id.game_icon_iv);
        mGameIconShadow = findViewById(R.id.game_icon_shadow);
        mDownloadProgressBar = (ProgressBar) findViewById(R.id.game_download_progress);
        mBottomText = (TextView) findViewById(R.id.bottom_text);

        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mGameInfoModel == null || TextUtils.isEmpty(mGameInfoModel.getPackageUrl())) {
                    return;
                }

                CustomDownloadManager.Item item = new CustomDownloadManager.Item(mGameInfoModel.getPackageUrl(), mGameInfoModel.getGameName());

                if (mApkStatus == CustomDownloadManager.ApkStatusEvent.STATUS_NO_DOWNLOAD) {
                    // 状态是未下载
                    clickDownloadStatistic();
//                    CustomDownloadManager.getInstance().beginDownload(item, GlobalData.app());
                    GameDownloadOptControl.tryDownloadGame(GameDownloadOptControl.TYPE_GAME_BEGIN_DOWNLOAD, mGameInfoModel);

                } else if (mApkStatus == CustomDownloadManager.ApkStatusEvent.STATUS_DOWNLOADING) {
                    // 正在下载中的包再次点击不作暂停处理
                } else if (mApkStatus == CustomDownloadManager.ApkStatusEvent.STATUS_PAUSE_DOWNLOAD) {
                    GameDownloadOptControl.tryDownloadGame(GameDownloadOptControl.TYPE_GAME_CONTINUE_DOWNLOAD, mGameInfoModel);
                } else if (mApkStatus == CustomDownloadManager.ApkStatusEvent.STATUS_PAUSE_DOWNLOAD) {

//                    CustomDownloadManager.getInstance().beginDownload(item, GlobalData.app());
                    GameDownloadOptControl.tryDownloadGame(GameDownloadOptControl.TYPE_GAME_PAUSE_DOWNLOAD, mGameInfoModel);

                } else if (mApkStatus == CustomDownloadManager.ApkStatusEvent.STATUS_DOWNLOAD_COMPELED) {
                    if (mOnInstallOrLaunchListener != null) {
                        mOnInstallOrLaunchListener.onInstallorLaunch();
                    }

                    GameDownloadOptControl.tryDownloadGame(GameDownloadOptControl.TYPE_GAME_DOWNLOAD_COMPELED, mGameInfoModel);
                } else if (mApkStatus == CustomDownloadManager.ApkStatusEvent.STATUS_LAUNCH) {
                    if (mOnInstallOrLaunchListener != null) {
                        mOnInstallOrLaunchListener.onInstallorLaunch();
                    }

                    GameDownloadOptControl.tryDownloadGame(GameDownloadOptControl.TYPE_GAME_INSTALL_FINISH, mGameInfoModel);
                }
            }
        });
    }

    public void setGameInfoModel(GameInfoModel gameInfoModel, int apkStatus, boolean isDownloadBygc) {
        if (gameInfoModel == null) {
            return;
        }

        mIsDownloadByGc = isDownloadBygc;

        if (mGameInfoModel != null && TextUtils.equals(mGameInfoModel.getPackageName(), gameInfoModel.getPackageName())) {
            if (mApkStatus != apkStatus) {
                // 游戏数据相同只是状态不同
                int lastVisibility = getVisibility();
                handleStatus(apkStatus, 0);
                if (lastVisibility != VISIBLE && getVisibility() == VISIBLE) {
                    // 从不可见到可见　曝光打点
                    onExposureStatistic();
                }
            }
            return;
        } else {
            // 更新游戏信息
            mGameInfoModel = gameInfoModel;
            handleStatus(apkStatus, 0);

            loadGameIcon(gameInfoModel.getIconUrl());

            // 游戏信息变更　曝光打点
            onExposureStatistic();
        }
    }

    private void loadGameIcon(String iconUrl) {
        if (TextUtils.isEmpty(iconUrl)) {
            return;
        }

        BaseImage baseImage = new HttpImage(GameInfoModel.getUrlWithPrefix(iconUrl, 480));
        baseImage.setWidth(getResources().getDimensionPixelSize(R.dimen.view_dimen_130));
        baseImage.setHeight(getResources().getDimensionPixelSize(R.dimen.view_dimen_130));
        baseImage.setCornerRadius(getResources().getDimensionPixelSize(R.dimen.view_dimen_20));

        FrescoWorker.loadImage(mGameIconIv, baseImage);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onDownloadEvent(CustomDownloadManager.ApkStatusEvent event) {
        if (mGameInfoModel == null || event == null) {
            return;
        }
        boolean apkEquals = false;

        if (mIsDownloadByGc) {
            if (mGameInfoModel.getGameId() == event.gameId
                    && mGameInfoModel.getPackageName().equals(event.packageName)) {
                apkEquals = true;
            }
        } else {
            if (!TextUtils.isEmpty(event.downloadKey) && !TextUtils.isEmpty(mGameInfoModel.getPackageUrl())) {
                // 根据downloadKey比较是不是同一个apk
                String key = MD5.MD5_32(mGameInfoModel.getPackageUrl());
                if (event.downloadKey.equals(key)) {
                    apkEquals = true;
                }
            } else if (!TextUtils.isEmpty(event.packageName)) {
                // 根据包名比较是不是同一个apk
                if (event.packageName.equals(mGameInfoModel.getPackageName())) {
                    apkEquals = true;
                }
            }
        }

        if (apkEquals) {
            int lastVisibility = getVisibility();
            handleStatus(event.status, event.progress);
            if (lastVisibility != VISIBLE && getVisibility() == VISIBLE) {
                // 从不可见到可见　曝光打点
                onExposureStatistic();
            }
        }
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(CustomDownloadManager.InstallOrLaunchEvent event) {
        if (event.mGameInfoModel == null) {
            return;
        }

        if (event.mGameInfoModel.getGameId() == mGameInfoModel.getGameId()) {
            if (event.type == STATTUS_INSTALL) {
                if (event.status == SUCCESS) {
                    if (mOnInstallOrLaunchListener != null) {
                        mOnInstallOrLaunchListener.onInstallorLaunch();
                    }
                }
            } else if (event.type == STATTUS_LAUNCH) {
                if (event.status == SUCCESS) {
                    if (mOnInstallOrLaunchListener != null) {
                        mOnInstallOrLaunchListener.onInstallorLaunch();
                    }
                }
            }
        }
    }

    private void handleStatus(int apkStatus, int progress) {
        mApkStatus = apkStatus;
        if (mApkStatus == CustomDownloadManager.ApkStatusEvent.STATUS_REMOVE) {
            // 游戏被卸载 重新检查状态
            checkInstalledOrUpdate();
        }

        if (mApkStatus == CustomDownloadManager.ApkStatusEvent.STATUS_DOWNLOADING) {
            setVisibility(VISIBLE);

            mGameIconShadow.setVisibility(VISIBLE);
            mDownloadProgressBar.setVisibility(VISIBLE);
            mDownloadProgressBar.setProgress(progress);
            mBottomText.setText(R.string.game_info_pop_download);

        } else if (mApkStatus == CustomDownloadManager.ApkStatusEvent.STATUS_LAUNCH) {
            // 已经安装了该游戏 隐藏这个浮窗
            setVisibility(GONE);

        } else {
            setVisibility(VISIBLE);

            mGameIconShadow.setVisibility(GONE);
            mDownloadProgressBar.setVisibility(GONE);
            mBottomText.setText(R.string.game_info_pop_tip);
        }
    }

    private void checkInstalledOrUpdate() {
        if (mGameInfoModel == null) {
            return;
        }
        String packageName = mGameInfoModel.getPackageName();
        if (TextUtils.isEmpty(packageName)) {
            // 无效的包名
            return;
        }

        if (mIsDownloadByGc) {
            GameDownloadOptControl.tryQueryGameDownStatus(mGameInfoModel);
        } else {
            if (PackageUtils.isInstallPackage(packageName)) {
                // 已经安装 点击则启动
                mApkStatus = CustomDownloadManager.ApkStatusEvent.STATUS_LAUNCH;
            } else {
                String apkPath = CustomDownloadManager.getInstance().getDownloadPath(mGameInfoModel.getPackageUrl());
                if (PackageUtils.isCompletedPackage(apkPath, mGameInfoModel.getPackageName())) {
                    // 存在包 点击则安装
                    mApkStatus = CustomDownloadManager.ApkStatusEvent.STATUS_DOWNLOAD_COMPELED;
                } else {
                    // 下载不完全 点击则下载
                    mApkStatus = CustomDownloadManager.ApkStatusEvent.STATUS_NO_DOWNLOAD;
                }
            }
        }
    }

    public void onExposureStatistic() {
        if (mGameInfoModel != null && getVisibility() == VISIBLE) {
            String url = mGameInfoModel.getPackageUrl();
            WatchGameInfoConfig.InfoItem infoItem = WatchGameInfoConfig.sGameInfoMap.get(url);
            if (infoItem != null) {
                MilinkStatistics.getInstance().statisticGameWatchDownload(GAME_WATCH_TYPE_EXPOSURE,
                        GAME_WATCH_BIZTYPE_POP_EXPOSURE, infoItem.anchorId, infoItem.channelId, infoItem.packageName);
            }
        }
    }


    private void clickDownloadStatistic() {
        String url = mGameInfoModel.getPackageUrl();
        WatchGameInfoConfig.InfoItem infoItem = WatchGameInfoConfig.sGameInfoMap.get(url);
        if (infoItem != null) {
            MilinkStatistics.getInstance().statisticGameWatchDownload(GAME_WATCH_TYPE_CLICK,
                    GAME_WATCH_BIZTYPE_POP_CLICK, infoItem.anchorId, infoItem.channelId, infoItem.packageName);
        }
    }

    public void setOnInstallOrLaunchListener(OnInstallOrLaunchListener listener) {
        mOnInstallOrLaunchListener = listener;
    }

    public interface OnInstallOrLaunchListener {
        void onInstallorLaunch();
    }
}
