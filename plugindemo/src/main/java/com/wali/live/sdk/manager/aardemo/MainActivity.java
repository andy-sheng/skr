package com.wali.live.sdk.manager.aardemo;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.wali.live.sdk.manager.IMiLiveSdk;
import com.wali.live.sdk.manager.MiLiveSdkController;
import com.wali.live.sdk.manager.SdkUpdateHelper;
import com.wali.live.sdk.manager.aardemo.global.GlobalData;
import com.wali.live.sdk.manager.aardemo.notification.NotificationManger;
import com.wali.live.sdk.manager.aardemo.utils.StringUtils;
import com.wali.live.sdk.manager.aardemo.utils.ToastUtils;

public class MainActivity extends AppCompatActivity {
    private final static String TAG = "MainActivity";

    public static final int CHANNEL_ID = 50000;

    private int currentChannelId = CHANNEL_ID;

    private RecyclerView mRecyclerView;
    private MenuRecyclerAdapter mMenuRecyclerAdapter;
    private TextView mChannelTv;

    private SdkUpdateHelper mSdkUpdateHelper;
    private Handler mUiHander = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mChannelTv = (TextView) findViewById(R.id.channel_tv);

        mSdkUpdateHelper = new SdkUpdateHelper(new IMiLiveSdk.IUpdateListener() {
            @Override
            public void onNewVersionAvail(boolean isForce) {
                mUiHander.post(new Runnable() {
                    @Override
                    public void run() {
                        Log.w(TAG, "onNewVersionAvail");
                        showUpgradeDialog(MainActivity.this, true);
                    }
                });
            }

            @Override
            public void onNoNewerVersion() {
                Log.w(TAG, "onNoNewerVersion");
                ToastUtils.showToast(R.string.no_upgrading);
            }

            @Override
            public void onCheckVersionFailed() {
                Log.w(TAG, "onCheckVersionFailed");
                ToastUtils.showToast(GlobalData.app(), R.string.check_failed);
            }

            @Override
            public void onDuplicatedRequest() {
                Log.w(TAG, "onDuplicatedRequest");
                ToastUtils.showToast(GlobalData.app(), R.string.is_upgrading);
            }

            @Override
            public void onDownloadStart() {
                Log.w(TAG, "onDownloadStart");
            }

            @Override
            public void onDownloadProgress(@IntRange(from = 0, to = 100) int progress) {
                Log.w(TAG, "onDownloadProgress");
                String msg = GlobalData.app().getApplicationContext()
                        .getString(R.string.milive_upgrade_progress, progress);
                NotificationManger.getInstance().showDownloadNotification(msg);
            }

            @Override
            public void onDownloadSuccess(String path) {
                Log.w(TAG, "onDownloadSuccess");
                NotificationManger.getInstance().removeNotification(NotificationManger.UPDATE_DOWNLOADING);
                ToastUtils.showToast(GlobalData.app().getApplicationContext(), R.string.download_update_succeed);
                mSdkUpdateHelper.installUpdate();
            }

            @Override
            public void onDownloadFailed(int errCode) {
                Log.w(TAG, "onDownloadFailed");
                NotificationManger.getInstance().removeNotification(NotificationManger.UPDATE_DOWNLOADING);
                ToastUtils.showToast(GlobalData.app().getApplicationContext(), R.string.download_update_failed);
            }
        });

        mRecyclerView = (RecyclerView) findViewById(R.id.menu_recyclerview);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        mMenuRecyclerAdapter = new MenuRecyclerAdapter(this, mSdkUpdateHelper);
        mRecyclerView.setAdapter(mMenuRecyclerAdapter);
        mMenuRecyclerAdapter.setChannleClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                currentChannelId = currentChannelId == 50000 ? 50001 : 50000;
                MiLiveSdkController.getInstance().setChannelId(currentChannelId);
                mChannelTv.setText("宿主id:" + currentChannelId);
            }
        });
        GlobalData.setApplication(this.getApplication());
        //建议在 application里初始化这个
        MiLiveSdkController.getInstance().init(this.getApplication(), CHANNEL_ID, "TEST SECRET", new IMiLiveSdk.CallbackWrapper() {
            @Override
            public void notifyServiceNull(int aidlFlag) {
                ToastUtils.showToast("notifyServiceNull aidlFlag=" + aidlFlag);
            }

            @Override
            public void notifyAidlFailure(int aidlFlag) {
                ToastUtils.showToast("notifyAidlFailure aidlFlag=" + aidlFlag);
            }

            @Override
            public void notifyLogin(int var1) {
                if (var1 == IMiLiveSdk.ICallback.CODE_SUCCESS) {
                    ToastUtils.showToast("登录成功");
                }else{
                    ToastUtils.showToast("登录错误，错误码："+var1);
                }
            }

            @Override
            public void notifyLogoff(int var1) {
                if (var1 == IMiLiveSdk.ICallback.CODE_SUCCESS) {
                    ToastUtils.showToast("登出成功");
                }
            }

            @Override
            public void notifyWantLogin() {
                ToastUtils.showToast("用户触发了只有登录才有的操作,回调给宿主,宿主传递账号信息给插件");
                mMenuRecyclerAdapter.oauthLogin();
            }

            @Override
            public void notifyVerifyFailure(int var1) {
                ToastUtils.showToast("验证失败，errCode=" + var1);
            }

            @Override
            public void notifyOtherAppActive() {
                ToastUtils.showToast("有其他APP在活跃");
            }
        });
        MiLiveSdkController.getInstance().setLogEnabled(true);
    }

    public void showUpgradeDialog(final @NonNull Activity activity, final boolean canCancel) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        View updateView = LayoutInflater.from(GlobalData.app()).inflate(R.layout.upgrage_dialog_layout, null);
        final TextView version = (TextView) updateView.findViewById(R.id.version);
        version.setText(StringUtils.getString(R.string.app_version, mSdkUpdateHelper.getVersionNumber()));
        final TextView size = (TextView) updateView.findViewById(R.id.size);
        size.setText(StringUtils.getString(R.string.apksize, String.valueOf(mSdkUpdateHelper.getNewApkSize())));
        final TextView update_content = (TextView) updateView.findViewById(R.id.update_content);
        update_content.setText(StringUtils.getString(R.string.upgrade_description, mSdkUpdateHelper.getUpdateMsg()));
        builder.setView(updateView);
        if (canCancel) {
            builder.setPositiveButton(R.string.update_rightnow, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    mSdkUpdateHelper.downUpdate();
                }
            });
            builder.setNegativeButton(R.string.cancel_update, null);
        } else {
            builder.setPositiveButton(R.string.update_rightnow, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    mSdkUpdateHelper.downUpdate();
                }
            });
        }
        AlertDialog dialog = builder.create();
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }

    public int getCurrentChannelId() {
        return currentChannelId;
    }
}
