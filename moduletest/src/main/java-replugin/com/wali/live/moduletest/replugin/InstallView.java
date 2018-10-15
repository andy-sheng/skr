package com.wali.live.moduletest.replugin;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.qihoo360.replugin.RePlugin;
import com.qihoo360.replugin.model.PluginInfo;
import com.wali.live.moduletest.R;
import com.wali.live.moduletest.replugin.OpItemAdapter;
import com.wali.live.moduletest.replugin.PackageData;

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

        List<PluginInfo> infos = RePlugin.getPluginInfoList();
        for (final PluginInfo info : infos) {
            if (info != null) {
                PackageData packageData = new PackageData();
                packageData.setFromInstallView(true);
                packageData.setOldVersionCode(info.getVersion());
                packageData.setVersionCode(0);
                packageData.setAppName(info.getName());
                packageData.setPackageName(info.getPackageName());
                packageData.setStatus(PackageData.STATUS_INSTALLED);
//                info.
                mDataList.add(packageData);
            }
        }

        return mDataList;
    }


}
