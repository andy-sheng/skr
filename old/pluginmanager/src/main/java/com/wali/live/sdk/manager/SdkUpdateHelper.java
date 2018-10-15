package com.wali.live.sdk.manager;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.support.annotation.Keep;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.wali.live.sdk.manager.global.GlobalData;
import com.wali.live.sdk.manager.http.HttpUtils;
import com.wali.live.sdk.manager.log.Logger;
import com.wali.live.sdk.manager.version.VersionCheckManager;
import com.wali.live.watchsdk.ipc.service.MiLiveSdkServiceProxy;

import java.util.concurrent.ExecutorService;

/**
 * Created by yangli on 17-2-21.
 */
@Keep
public class SdkUpdateHelper {
    public static final int HAS_UPGRADE = VersionCheckManager.HAS_UPGRADE;
    public static final int NO_UPGRADE = VersionCheckManager.NO_UPGRADE;
    public static final int CHECK_FAILED = VersionCheckManager.CHECK_FAILED;
    public static final int IS_UPGRADING = VersionCheckManager.IS_UPGRADING;
    public static final int HAS_FORCE_UPGRADE = VersionCheckManager.HAS_FORCE_UPGRADE;

    private
    @Nullable
    ExecutorService mExecutor;
    private
    @NonNull
    VersionCheckManager mVersionManager = VersionCheckManager.getInstance();

    private
    @Nullable
    IMiLiveSdk.IUpdateListener mUpdateListener;

    public SdkUpdateHelper(IMiLiveSdk.IUpdateListener updateListener) {
        mUpdateListener = updateListener;
        mExecutor = HttpUtils.ONLINE_FILE_TASK_EXECUTOR;
    }

    public int getLocalInstallApkVersion() {
        try {
            PackageInfo packageInfo = GlobalData.app().getPackageManager().getPackageInfo(
                    VersionCheckManager.PACKAGE_NAME, PackageManager.GET_META_DATA);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
        }
        // 如果版本为0，置空服务，防止下次apk重装出问题
        return 0;
    }

    public String getVersionNumber() {
        return mVersionManager.getVersionNumberByTransfer();
    }

    public int getNewApkSize() {
        return mVersionManager.getAdditionalSize();
    }

    public String getUpdateMsg() {
        return mVersionManager.getUpdateMessage();
    }

    public void setCheckTime() {
        mVersionManager.setCheckTime();
    }

    public int checkUpdateSync() {
        return mVersionManager.checkNewVersion();
    }

    public void downUpdateSync() {
        mVersionManager.startDownload(mUpdateListener);
    }

    public void installUpdateSync() {
        mVersionManager.installLocalPackage();
    }

    /**
     * @notice 测试环境使用，在功能正式上线后请不要调用
     */
    public void setStaging() {
        mVersionManager.setStaging(true);
    }

    /**
     * @notice 测试环境使用，在功能正式上线后请不要调用
     */
    public void checkStaging() {
        setStaging();
        checkUpdate();
    }

    public void checkUpdate() {
        if (mExecutor == null) {
            return;
        }
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                int ret = mVersionManager.checkNewVersion();
                switch (ret) {
                    case HAS_UPGRADE:
                        if (mUpdateListener != null) {
                            mUpdateListener.onNewVersionAvail(false);
                        }
                        break;
                    case HAS_FORCE_UPGRADE:
                        if (mUpdateListener != null) {
                            mUpdateListener.onNewVersionAvail(true);
                        }
                        break;
                    case NO_UPGRADE:
                        if (mUpdateListener != null) {
                            mUpdateListener.onNoNewerVersion();
                        }
                        break;
                    case CHECK_FAILED:
                        if (mUpdateListener != null) {
                            mUpdateListener.onCheckVersionFailed();
                        }
                        break;
                    case IS_UPGRADING:
                        if (mUpdateListener != null) {
                            mUpdateListener.onDuplicatedRequest();
                        }
                        break;
                    default:
                        break;
                }
            }
        });
    }

    public void downUpdate() {
        if (mExecutor == null) {
            return;
        }
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                mVersionManager.startDownload(mUpdateListener);
            }
        });
    }

    public void installUpdate() {
        if (mExecutor == null) {
            return;
        }
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                mVersionManager.installLocalPackage();
            }
        });
    }

    /**
     * 针对android N系统，Uri.from(File file) 权限问题新增的接口
     *
     * @param auth
     */
    public void installUpdateN(final String auth) {
        if (mExecutor == null) {
            return;
        }
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                mVersionManager.installLocalPackageN(auth);
            }
        });
    }

    public void installUpdateSyncN(final String auth) {
        mVersionManager.installLocalPackageN(auth);
    }
}
