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
import com.base.log.MyLog;
import com.base.utils.MD5;
import com.base.utils.system.PackageUtils;
import com.base.utils.toast.ToastUtils;
import com.mi.live.data.gamecenter.model.GameInfoModel;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.watch.download.CustomDownloadManager;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * Created by liuting on 18-8-20.
 */

public class GameInfoPopView extends RelativeLayout{
    private GameInfoModel mGameInfoModel;
    private int mApkStatus = -1;

    private BaseImageView mGameIconIv;
    private View mGameIconShadow;
    private ProgressBar mDownloadProgressBar;
    private TextView mBottomText;

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

                if (mApkStatus == CustomDownloadManager.ApkStatusEvent.STATUS_NO_DOWNLOAD
                        || mApkStatus == CustomDownloadManager.ApkStatusEvent.STATUS_REMOVE) {
                    // 状态是未下载或者刚刚卸载 点击重新下载
                    CustomDownloadManager.getInstance().beginDownload(item, GlobalData.app());

                } else if (mApkStatus == CustomDownloadManager.ApkStatusEvent.STATUS_DOWNLOADING) {
                    // 正在下载中的包再次点击不作暂停处理
                } else if (mApkStatus == CustomDownloadManager.ApkStatusEvent.STATUS_PAUSE_DOWNLOAD) {

                    CustomDownloadManager.getInstance().beginDownload(item, GlobalData.app());

                } else if (mApkStatus == CustomDownloadManager.ApkStatusEvent.STATUS_DOWNLOAD_COMPELED) {
                    if (PackageUtils.tryInstall(CustomDownloadManager.getInstance().getDownloadPath(mGameInfoModel.getPackageUrl()))) {

                    } else {
                        // 安装失败，重新下载
                        ToastUtils.showToast("apk包解析失败，重新下载");
                        mApkStatus = CustomDownloadManager.ApkStatusEvent.STATUS_NO_DOWNLOAD;
                        CustomDownloadManager.getInstance().beginDownload(item, GlobalData.app());
                    }
                } else if (mApkStatus == CustomDownloadManager.ApkStatusEvent.STATUS_LAUNCH) {
                    if (PackageUtils.tryLaunch(mGameInfoModel.getPackageName())) {

                    } else {
                        ToastUtils.showToast("启动失败");
                    }
                }
            }
        });
    }

    public void setGameInfoModel(GameInfoModel gameInfoModel, int apkStatus) {
        if (gameInfoModel == null) {
            return;
        }

        mApkStatus = apkStatus;
        handleStatus(apkStatus, 0);

        if (mGameInfoModel != null && TextUtils.equals(mGameInfoModel.getIconUrl(), gameInfoModel.getIconUrl())) {
            return;
        }
        mGameInfoModel = gameInfoModel;

        loadGameIcon(gameInfoModel.getIconUrl());
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

    @Subscribe (threadMode = ThreadMode.MAIN)
    public void onDownloadEvent(CustomDownloadManager.ApkStatusEvent event) {
        if (mGameInfoModel == null || event == null) {
            return;
        }
        boolean apkEquals = false;
        if (!TextUtils.isEmpty(event.downloadKey) && !TextUtils.isEmpty(mGameInfoModel.getPackageUrl())) {
            // 根据downloadKey比较是不是同一个apk
            String key = MD5.MD5_32(mGameInfoModel.getPackageUrl());
            if (event.downloadKey.equals(key)) {
                apkEquals = true;
            }
        } else if (!TextUtils.isEmpty(event.packageName)){
            // 根据包名比较是不是同一个apk
            if (event.packageName.equals(mGameInfoModel.getPackageName())) {
                apkEquals = true;
            }
        }

        if (apkEquals) {
            mApkStatus = event.status;
            handleStatus(mApkStatus, event.progress);
        }
    }

    private void handleStatus(int apkStatus, int progress) {
        if (apkStatus == CustomDownloadManager.ApkStatusEvent.STATUS_DOWNLOADING){
            setVisibility(VISIBLE);

            mGameIconShadow.setVisibility(VISIBLE);
            mDownloadProgressBar.setVisibility(VISIBLE);
            mDownloadProgressBar.setProgress(progress);
            mBottomText.setText(R.string.game_info_pop_download);

        } else if(apkStatus == CustomDownloadManager.ApkStatusEvent.STATUS_LAUNCH){
            // 已经安装了该游戏 隐藏这个浮窗
            setVisibility(GONE);

        } else {
            setVisibility(VISIBLE);

            mGameIconShadow.setVisibility(GONE);
            mDownloadProgressBar.setVisibility(GONE);
            mBottomText.setText(R.string.game_info_pop_tip);
        }
    }
}
