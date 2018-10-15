package com.wali.live.moduletest.droidplugin;

import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.morgoo.droidplugin.pm.PluginManager;
import com.wali.live.moduletest.R;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class InstallView extends RelativeLayout {

    public final static String TAG = "UninstallView";
    TextView mRefreshBtn;

    RecyclerView mListRv;

    OpItemAdapter mOpItemAdapter;

    public InstallView(Context context) {
        super(context);
        init();
    }

    public InstallView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public InstallView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    boolean mHasAddServiceConnection = false;

    ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

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
        if (PluginManager.getInstance().isConnected()) {
            loadData();
        } else {
            if (!mHasAddServiceConnection) {
                PluginManager.getInstance().addServiceConnection(mServiceConnection);
                mHasAddServiceConnection = true;
            }
        }

    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (!mHasAddServiceConnection) {
            PluginManager.getInstance().addServiceConnection(mServiceConnection);
            mHasAddServiceConnection = true;
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        PluginManager.getInstance().removeServiceConnection(mServiceConnection);
        mHasAddServiceConnection = false;
    }

    private void loadData() {
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

    }

    private List<PackageData> loadInner() {
        List<PackageData> mDataList = new ArrayList<>();

        try {
            final List<PackageInfo> infos = PluginManager.getInstance().getInstalledPackages(0);
            final PackageManager pm = getContext().getPackageManager();
            for (final PackageInfo info : infos) {
                if (info != null) {
                    PackageData packageData = new PackageData();
                    packageData.setFromInstallView(true);
                    String appName = (String) pm.getApplicationLabel(info.applicationInfo);
                    packageData.setAppName(appName);
                    packageData.setPackageName(info.applicationInfo.packageName);
                    packageData.setVersionName(info.versionName);
                    mDataList.add(packageData);
                }
            }

        } catch (RemoteException e) {
            e.printStackTrace();
        }

        return mDataList;
    }


}
