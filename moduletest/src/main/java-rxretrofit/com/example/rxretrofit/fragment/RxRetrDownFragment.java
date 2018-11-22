package com.example.rxretrofit.fragment;

import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.common.base.BaseFragment;
import com.common.rxretrofit.download.DownInfo;
import com.common.rxretrofit.download.cache.DownLoadCache;
import com.example.rxretrofit.adapter.DownAdapter;
import com.wali.live.moduletest.R;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class RxRetrDownFragment extends BaseFragment {

    RecyclerView mTestDownRecycleView;

    List<DownInfo> listData = new ArrayList<>();

    @Override
    public int initView() {
        return R.layout.rxretrofit_test_down_fragment_layout;
    }

    @Override
    public boolean useEventBus() {
        return false;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        mTestDownRecycleView = mRootView.findViewById(R.id.test_down_recycle_view);

        DownAdapter downAdapter = new DownAdapter(getContext());
        mTestDownRecycleView.setLayoutManager(new LinearLayoutManager(getContext()));
        mTestDownRecycleView.setAdapter(downAdapter);

        listData = DownLoadCache.getInstance().queryAll();
        if (listData.isEmpty()){
            for (int i = 0; i < 4; i++) {
                File outputFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                        "test" + i + ".mp4");
                DownInfo apkApi = new DownInfo("http://clips.vorwaerts-gmbh.de/big_buck_bunny.mp4");
                apkApi.setId(i);
                apkApi.setUpdateProgress(true);
                apkApi.setSavePath(outputFile.getAbsolutePath());
                DownLoadCache.getInstance().save(apkApi);
            }
            listData = DownLoadCache.getInstance().queryAll();
        }
        downAdapter.setData(listData);
    }
}
