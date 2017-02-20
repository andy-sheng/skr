package com.base.version;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.util.Log;


import com.base.common.R;
import com.base.global.GlobalData;
import com.base.utils.toast.ToastUtils;

import java.lang.ref.WeakReference;


public class VersionCheckTask extends AsyncTask<Void, Void, Integer> {

    public final static String TAG = VersionCheckTask.class.getSimpleName();

    private WeakReference<Activity> mActivity; // 活动的名称
    private boolean mIsManualCheck = true; // 手动查询
    private ProgressDialog mProgressDialog; // 进度条
    private boolean mIsNeedDialog = true;

    public VersionCheckTask(final Activity act, boolean isManualCheck, boolean isNeedDialog) {
        mActivity = new WeakReference(act);
        mIsManualCheck = isManualCheck;
        mIsNeedDialog = isNeedDialog;
    }

    @Override
    protected void onPostExecute(Integer result) {
        super.onPostExecute(result);
        if (mIsManualCheck && mProgressDialog != null&&mIsNeedDialog) {
            mProgressDialog.dismiss();
        }
        if (result == VersionCheckManager.HAS_UPGRADE) {
            if (mIsManualCheck) {
                // 手动检查弹出更新框
                if (!mActivity.get().isFinishing()) {
                    VersionCheckManager.getInstance().showUpgradeDialog(mActivity, true, true);
                } else {
                    VersionCheckManager.getInstance().setShowUpgradeDialog(true);
                }
            } else {
                VersionCheckManager.getInstance().saveRemoteVersion(); // 将远程的版本号保存在SharedPreference里
//                EventBus.getDefault().post(new VersionCheckManager.NewVersion());
            }
            return;
        } else if (result == VersionCheckManager.HAS_FORCE_UPGRADE) {
            if (!mActivity.get().isFinishing()) {
//                VersionCheckManager.getInstance().showUpgradeDialog(mActivity, true, false); // 当需要强制升级的时候，不管是否是自动检测都弹出框，而且不能取消
                VersionCheckManager.getInstance().showUpgradeDialog(mActivity, true, true); // 当需要强制升级的时候，不管是否是自动检测都弹出框，而且不能取消
            } else {
                VersionCheckManager.getInstance().setShowUpgradeDialog(true);
            }
            return;
        } else if (result == VersionCheckManager.NO_UPGRADE) {
            if (mIsManualCheck&&mIsNeedDialog) {
                ToastUtils.showToast(GlobalData.app().getApplicationContext(), R.string.no_upgrading);
            }
        } else if (result == VersionCheckManager.CHECK_FAILED) {
            if (mIsManualCheck&&mIsNeedDialog) {
                ToastUtils.showToast(GlobalData.app(), R.string.check_failed);
            }
        } else if (result == VersionCheckManager.IS_UPGRADING) {
            if (mIsManualCheck) {
                ToastUtils.showToast(GlobalData.app(), R.string.is_upgrading);
            }
        }
        VersionCheckManager.getInstance().setShowUpgradeDialog(false);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        if (mIsManualCheck&&mIsNeedDialog) {
            mProgressDialog = ProgressDialog.show(mActivity.get(), null, GlobalData.app().getString(R.string.check_upgrading));
            mProgressDialog.setCancelable(true);
            mProgressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(final DialogInterface arg0) {
                    cancel(true);
                }
            });
            mProgressDialog.show();
        }
    }

    @Override
    protected Integer doInBackground(Void... params) {
        Log.d(TAG,"VersionCheckTask doInBackground");
        if (VersionCheckManager.getInstance().getShowUpgradeDialog()) {
            Log.d(TAG,"getShowUpgradeDialog == true");
            return VersionCheckManager.HAS_UPGRADE;
        }
        return VersionCheckManager.getInstance().checkNewVersion();
    }

    public static void checkUpdate(Activity activity){
        new VersionCheckTask((Activity) activity, true, true).execute();
    }
}
