package com.module.playways.grab.createroom.view;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;

import com.alibaba.fastjson.JSON;
import com.common.core.account.UserAccountManager;
import com.common.core.permission.SkrAudioPermission;
import com.common.rxretrofit.ApiManager;
import com.common.rxretrofit.ApiMethods;
import com.common.rxretrofit.ApiObserver;
import com.common.rxretrofit.ApiResult;
import com.common.statistics.StatConstants;
import com.common.statistics.StatisticsAdapter;
import com.common.view.recyclerview.RecyclerOnItemClickListener;
import com.component.busilib.callback.EmptyCallback;
import com.component.busilib.callback.ErrorCallback;
import com.component.busilib.callback.LoadingCallback;
import com.kingja.loadsir.callback.Callback;
import com.kingja.loadsir.core.LoadService;
import com.kingja.loadsir.core.LoadSir;
import com.component.busilib.friends.GrabSongApi;
import com.component.busilib.friends.SpecialModel;
import com.module.playways.grab.createroom.adapter.SpecialSelectAdapter;
import com.module.rank.R;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnRefreshLoadMoreListener;

import java.util.HashMap;
import java.util.List;

import io.reactivex.disposables.Disposable;

/**
 * 歌曲专场选择
 */
public class SpecialSelectView extends RelativeLayout {

    SmartRefreshLayout mRefreshLayout;
    RecyclerView mContentRv;
    SpecialSelectAdapter mSpecialSelectAdapter;

    LoadService mLoadService;

    int offset = 0;          //偏移量
    int DEFAULT_COUNT = 10;  // 每次拉去列表数目

    Disposable mDisposable;

    List<String> musicURLs;  //背景音乐
    SkrAudioPermission mSkrAudioPermission = new SkrAudioPermission();

    SpecialSelectListner mSpecialSelectListner;

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

    public void setSpecialSelectListner(SpecialSelectListner specialSelectListner) {
        mSpecialSelectListner = specialSelectListner;
    }

    private void initView() {
        inflate(getContext(), R.layout.grab_select_view_layout, this);

        mRefreshLayout = (SmartRefreshLayout) this.findViewById(R.id.refreshLayout);
        mContentRv = (RecyclerView) this.findViewById(R.id.content_rv);

        mRefreshLayout.setEnableRefresh(false);
        mRefreshLayout.setEnableLoadMore(true);
        mRefreshLayout.setEnableLoadMoreWhenContentNotFull(true);
        mRefreshLayout.setEnableOverScrollDrag(false);
        mRefreshLayout.setOnRefreshLoadMoreListener(new OnRefreshLoadMoreListener() {
            @Override
            public void onLoadMore(@NonNull RefreshLayout refreshLayout) {
                loadData(offset, DEFAULT_COUNT, true);
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
                        HashMap map = new HashMap();
                        map.put("tagId2", String.valueOf(model.getTagID()));
                        StatisticsAdapter.recordCountEvent(UserAccountManager.getInstance().getGategory(StatConstants.CATEGORY_GRAB),
                                StatConstants.KEY_MATCH_START, map);
                        if (mSpecialSelectListner != null) {
                            mSpecialSelectListner.onClickSpecial(model, musicURLs);
                        }
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
                loadData(0, DEFAULT_COUNT, false);
            }
        });

        loadData(0, DEFAULT_COUNT, false);
        getBackgroundMusic();
    }

    private void getBackgroundMusic() {
        GrabSongApi grabSongApi = ApiManager.getInstance().createService(GrabSongApi.class);
        ApiMethods.subscribe(grabSongApi.getSepcialBgVoice(), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult result) {
                if (result.getErrno() == 0) {
                    musicURLs = JSON.parseArray(result.getData().getString("musicURL"), String.class);
                }
            }
        });
    }

    private void loadData(int offset, int count, boolean isLoadMore) {
        GrabSongApi grabSongApi = ApiManager.getInstance().createService(GrabSongApi.class);
        mDisposable = ApiMethods.subscribeWith(grabSongApi.getSepcialList(offset, count), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult obj) {
                if (obj.getErrno() == 0) {
                    List<SpecialModel> list = JSON.parseArray(obj.getData().getString("tags"), SpecialModel.class);
                    int offset = obj.getData().getIntValue("offset");
                    refreshView(list, offset, isLoadMore);
                } else {
                    mLoadService.showCallback(ErrorCallback.class);
                }
            }
        });
    }

    private void refreshView(List<SpecialModel> list, int offset, boolean isLoadMore) {
        this.offset = offset;
        if (list != null) {
            mRefreshLayout.finishLoadMore();
            if (!isLoadMore) {
                mSpecialSelectAdapter.getDataList().clear();
            }
            mSpecialSelectAdapter.getDataList().addAll(list);
            mSpecialSelectAdapter.notifyDataSetChanged();
        } else {
            mRefreshLayout.setEnableLoadMore(false);
            mRefreshLayout.finishLoadMore();
        }

        if (mSpecialSelectAdapter.getDataList() != null && mSpecialSelectAdapter.getDataList().size() > 0) {
            mLoadService.showSuccess();
        } else {
            mLoadService.showCallback(EmptyCallback.class);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mDisposable != null) {
            mDisposable.dispose();
        }
    }

    // 选择专场回调
    public interface SpecialSelectListner {
        void onClickSpecial(SpecialModel model, List<String> music);
    }
}
