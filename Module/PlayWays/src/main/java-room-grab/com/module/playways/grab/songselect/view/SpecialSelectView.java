package com.module.playways.grab.songselect.view;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;

import com.alibaba.fastjson.JSON;
import com.common.core.permission.SkrAudioPermission;
import com.common.rxretrofit.ApiManager;
import com.common.rxretrofit.ApiMethods;
import com.common.rxretrofit.ApiObserver;
import com.common.rxretrofit.ApiResult;
import com.common.view.recyclerview.RecyclerOnItemClickListener;
import com.component.busilib.callback.EmptyCallback;
import com.component.busilib.callback.ErrorCallback;
import com.component.busilib.callback.LoadingCallback;
import com.kingja.loadsir.callback.Callback;
import com.kingja.loadsir.core.LoadService;
import com.kingja.loadsir.core.LoadSir;
import com.module.playways.grab.songselect.GrabSongApi;
import com.module.playways.grab.songselect.model.SpecialModel;
import com.module.playways.grab.songselect.adapter.SpecialSelectAdapter;
import com.module.rank.R;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnRefreshLoadMoreListener;

import java.util.List;

public class SpecialSelectView extends RelativeLayout {

    SmartRefreshLayout mRefreshLayout;
    RecyclerView mContentRv;
    SpecialSelectAdapter mSpecialSelectAdapter;

    LoadService mLoadService;

    int offset = 0;          //偏移量
    int DEFAULT_COUNT = 10;  // 每次拉去列表数目

    List<String> musicURLs;  //背景音乐
    SkrAudioPermission mSkrAudioPermission = new SkrAudioPermission();

    public SpecialSelectView(Context context) {
        super(context);
        initView();
    }

    public SpecialSelectView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public SpecialSelectView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView() {
        inflate(getContext(), R.layout.grab_special_view_layout, this);

        mRefreshLayout = (SmartRefreshLayout) this.findViewById(R.id.refreshLayout);
        mContentRv = (RecyclerView) this.findViewById(R.id.content_rv);

        mRefreshLayout.setEnableRefresh(false);
        mRefreshLayout.setEnableLoadMore(true);
        mRefreshLayout.setEnableLoadMoreWhenContentNotFull(false);
        mRefreshLayout.setEnableOverScrollDrag(false);
        mRefreshLayout.setOnRefreshLoadMoreListener(new OnRefreshLoadMoreListener() {
            @Override
            public void onLoadMore(@NonNull RefreshLayout refreshLayout) {
                loadData(offset, DEFAULT_COUNT);
            }

            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {
                mRefreshLayout.finishRefresh();
            }
        });
        mContentRv.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));

        mSpecialSelectAdapter = new SpecialSelectAdapter(new RecyclerOnItemClickListener<SpecialModel>() {
            @Override
            public void onItemClicked(View view, int position, SpecialModel model) {
                mSkrAudioPermission.ensurePermission(new Runnable() {
                    @Override
                    public void run() {
//                        goMatchFragment(model.getTagID());
//                        HashMap map = new HashMap();
//                        map.put("tagId2", String.valueOf(model.getTagID()));
//                        StatisticsAdapter.recordCountEvent(UserAccountManager.getInstance().getGategory(StatConstants.CATEGORY_GRAB),
//                                StatConstants.KEY_MATCH_START, map);
                    }
                }, true);

            }
        });
        mContentRv.setAdapter(mSpecialSelectAdapter);

        LoadSir mLoadSir = new LoadSir.Builder()
                .addCallback(new LoadingCallback(R.drawable.wulishigedan, "数据正在努力加载中..."))
                .addCallback(new EmptyCallback(R.drawable.wulishigedan, "你敢不敢唱首歌？"))
                .addCallback(new ErrorCallback(R.drawable.wulishigedan, "请求出错了..."))
                .setDefaultCallback(LoadingCallback.class)
                .build();
        mLoadService = mLoadSir.register(mRefreshLayout, new Callback.OnReloadListener() {
            @Override
            public void onReload(View v) {
                loadData(offset, DEFAULT_COUNT);
            }
        });

        loadData(offset, DEFAULT_COUNT);
    }

    private void loadData(int offset, int count) {
        GrabSongApi grabSongApi = ApiManager.getInstance().createService(GrabSongApi.class);
        ApiMethods.subscribe(grabSongApi.getSepcialList(offset, count), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult obj) {
                if (obj.getErrno() == 0) {
                    List<SpecialModel> list = JSON.parseArray(obj.getData().getString("tags"), SpecialModel.class);
                    int offset = obj.getData().getIntValue("offset");
                    refreshView(list, offset);
                } else {
                    mLoadService.showCallback(ErrorCallback.class);
                }
            }
        });
    }

    private void refreshView(List<SpecialModel> list, int offset) {
        this.offset = offset;
        mRefreshLayout.finishLoadMore();
        if (list != null) {
            mSpecialSelectAdapter.getDataList().addAll(list);
            mSpecialSelectAdapter.notifyDataSetChanged();
        }

        if (mSpecialSelectAdapter.getDataList() != null && mSpecialSelectAdapter.getDataList().size() > 0) {
            mLoadService.showSuccess();
        } else {
            mLoadService.showCallback(EmptyCallback.class);
        }
    }
}
