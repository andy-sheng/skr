package com.wali.live.moduletest.replugin;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.common.log.MyLog;
import com.common.utils.PermissionUtil;
import com.common.utils.U;
import com.qihoo360.replugin.RePlugin;
import com.qihoo360.replugin.model.PluginInfo;
import com.tbruyelle.rxpermissions2.RxPermissions;
import com.wali.live.moduletest.R;
import com.wali.live.moduletest.replugin.OpItemAdapter;
import com.wali.live.moduletest.replugin.PackageData;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class UninstallView extends RelativeLayout {

    public final static String TAG = "UninstallView";
    TextView mRefreshBtn;

    RecyclerView mListRv;

    OpItemAdapter mOpItemAdapter;

    public UninstallView(Context context) {
        super(context);
        init();
    }

    public UninstallView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public UninstallView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }


    private void init() {
        inflate(getContext(), R.layout.test_droidplugin_sub_view, this);
        mRefreshBtn = (TextView) findViewById(R.id.refresh_btn);
        mRefreshBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                loadData();
            }
        });
        mListRv = (RecyclerView) findViewById(R.id.list_rv);
        mListRv.setLayoutManager(new LinearLayoutManager(getContext()));
        mOpItemAdapter = new OpItemAdapter(getContext());
        mListRv.setAdapter(mOpItemAdapter);

        loadData();

    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
    }

    private void loadData() {
        if (U.getPermissionUtils().checkExternalStorage((Activity) getContext())) {
            Observable.create(new ObservableOnSubscribe<List<PackageData>>() {
                @Override
                public void subscribe(ObservableEmitter<List<PackageData>> emitter) throws Exception {
                    List<PackageData> list = loadInner();
                    emitter.onNext(list);
                    emitter.onComplete();
                }
            })
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Observer<List<PackageData>>() {
                        @Override
                        public void onSubscribe(Disposable d) {

                        }

                        @Override
                        public void onNext(List<PackageData> list) {
                            mOpItemAdapter.setData(list);
                        }

                        @Override
                        public void onError(Throwable e) {

                        }

                        @Override
                        public void onComplete() {

                        }
                    });

        } else {
            U.getPermissionUtils().requestExternalStorage(new PermissionUtil.RequestPermission() {
                @Override
                public void onRequestPermissionSuccess() {
                    MyLog.d(TAG, "onRequestPermissionSuccess");
                }

                @Override
                public void onRequestPermissionFailure(List<String> permissions) {
                    MyLog.d(TAG, "onRequestPermissionFailure" + " permissions=" + permissions);
                }

                @Override
                public void onRequestPermissionFailureWithAskNeverAgain(List<String> permissions) {
                    MyLog.d(TAG, "onRequestPermissionFailureWithAskNeverAgain" + " permissions=" + permissions);
                }
            }, (Activity) getContext());
        }
    }

    private List<PackageData> loadInner() {
        File file = Environment.getExternalStorageDirectory();

        List<File> apks = new ArrayList<File>(10);
        File[] files = file.listFiles();
        if (files != null) {
            for (File apk : files) {
                if (apk.exists() && apk.getPath().toLowerCase().endsWith(".apk")) {
                    apks.add(apk);
                }
            }
        }

        PackageManager pm = getContext().getPackageManager();
        List<PackageData> mDataList = new ArrayList<>();

        for (final File apk : apks) {
            try {
                if (apk.exists() && apk.getPath().toLowerCase().endsWith(".apk")) {
                    final PackageInfo info = pm.getPackageArchiveInfo(apk.getPath(), 0);
                    if (info != null) {
                        PackageData packageData = new PackageData();
                        packageData.setSdcardPath(apk.getPath());
                        String appName = (String) pm.getApplicationLabel(info.applicationInfo);
                        packageData.setAppName(appName);
                        packageData.setPackageName(info.applicationInfo.packageName);
                        packageData.setVersionName(info.versionName);
                        PluginInfo pi = RePlugin.getPluginInfo(info.applicationInfo.packageName);
                        if (pi != null) {
                            packageData.setStatus(PackageData.STATUS_INSTALLED);
                            packageData.setVersionCode(info.versionCode);
                            packageData.setOldVersionCode(pi.getVersion());
                        } else {
                            packageData.setStatus(PackageData.STATUS_UNINSTALL);
                        }
                        mDataList.add(packageData);
                    }
                }
            } catch (Exception e) {
            }
        }
        return mDataList;
    }


}
