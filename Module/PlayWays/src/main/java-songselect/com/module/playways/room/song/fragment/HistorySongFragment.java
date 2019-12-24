package com.module.playways.room.song.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;
import android.widget.RelativeLayout;

import com.alibaba.android.arouter.launcher.ARouter;
import com.common.base.BaseFragment;
import com.common.core.permission.SkrAudioPermission;
import com.common.utils.U;
import com.common.view.DebounceViewClickListener;
import com.common.view.ex.ExImageView;

import android.support.v7.widget.RecyclerView;

import com.common.view.recyclerview.RecyclerOnItemClickListener;
import com.component.busilib.callback.EmptyCallback;
import com.component.busilib.callback.ErrorCallback;
import com.component.busilib.callback.LoadingCallback;
import com.kingja.loadsir.callback.Callback;
import com.kingja.loadsir.core.LoadService;
import com.kingja.loadsir.core.LoadSir;
import com.module.RouterConstants;
import com.module.playways.audition.AudioRoomActivity;
import com.module.playways.room.song.adapter.SongSelectAdapter;
import com.module.playways.room.song.model.SongModel;
import com.module.playways.R;
import com.module.playways.room.song.presenter.SongTagDetailsPresenter;
import com.module.playways.room.song.view.ISongTagDetailView;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnRefreshLoadMoreListener;

import java.util.ArrayList;
import java.util.List;

import static com.module.playways.PlayWaysActivity.KEY_GAME_TYPE;

public class HistorySongFragment extends BaseFragment implements ISongTagDetailView {

    public static final int DEFAULT_SONG_COUNT = 30;  // 每次从服务器拉去歌曲数
    int offset = 0; //偏移量
    boolean hasMore = true; // 是否还有更多数据标记位
    List<SongModel> datas; // 歌曲的数据源

    RelativeLayout mMainActContainer;
    ExImageView mHistoryBack;
    SmartRefreshLayout mRefreshLayout;
    RecyclerView mHistoryRecycle;

    SongSelectAdapter songSelectAdapter;
    SongTagDetailsPresenter presenter;

    int mGameType;

    LoadService mLoadService;

    SkrAudioPermission mSkrAudioPermission = new SkrAudioPermission();

    @Override
    public int initView() {
        return R.layout.history_song_fragment_layout;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        mMainActContainer = (RelativeLayout) getRootView().findViewById(R.id.main_act_container);
        mHistoryBack = (ExImageView) getRootView().findViewById(R.id.history_back);
        mRefreshLayout = (SmartRefreshLayout) getRootView().findViewById(R.id.refreshLayout);
        mHistoryRecycle = (RecyclerView) getRootView().findViewById(R.id.history_recycle);

        mHistoryRecycle.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));

        Bundle bundle = getArguments();
        if (bundle != null) {
            mGameType = bundle.getInt(KEY_GAME_TYPE);
        }

        songSelectAdapter = new SongSelectAdapter(new RecyclerOnItemClickListener() {
            @Override
            public void onItemClicked(View view, int position, Object model) {
                jump((SongModel) model);
            }
        });
        mHistoryRecycle.setAdapter(songSelectAdapter);

        presenter = new SongTagDetailsPresenter(this);
        addPresent(presenter);
        presenter.getClickedMusicItmes(0, DEFAULT_SONG_COUNT);

        mRefreshLayout.setEnableRefresh(false);
        mRefreshLayout.setEnableLoadMore(true);
        mRefreshLayout.setEnableLoadMoreWhenContentNotFull(true);
        mRefreshLayout.setEnableOverScrollDrag(false);
        mRefreshLayout.setOnRefreshLoadMoreListener(new OnRefreshLoadMoreListener() {
            @Override
            public void onLoadMore(@NonNull RefreshLayout refreshLayout) {
                presenter.getClickedMusicItmes(offset, DEFAULT_SONG_COUNT);
            }

            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {
                mRefreshLayout.finishRefresh();
            }
        });

        mHistoryBack.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                U.getFragmentUtils().popFragment(HistorySongFragment.this);
            }
        });

        LoadSir mLoadSir = new LoadSir.Builder()
                .addCallback(new LoadingCallback(R.drawable.wulishigedan, "数据正在努力加载中..."))
                .addCallback(new EmptyCallback(R.drawable.wulishigedan, "你敢不敢唱首歌？", null))
                .addCallback(new ErrorCallback(R.drawable.wulishigedan, "请求出错了..."))
                .setDefaultCallback(LoadingCallback.class)
                .build();
        mLoadService = mLoadSir.register(mRefreshLayout, new Callback.OnReloadListener() {
            @Override
            public void onReload(View v) {
                presenter.getClickedMusicItmes(offset, DEFAULT_SONG_COUNT);
            }
        });

        U.getSoundUtils().preLoad(getTAG(), R.raw.normal_back);
    }

    void jump(SongModel songModel) {
        if (songModel == null) {
            return;
        }
        if (getActivity() instanceof AudioRoomActivity) {
            mSkrAudioPermission.ensurePermission(new Runnable() {
                @Override
                public void run() {
                    ARouter.getInstance().build(RouterConstants.ACTIVITY_AUDITION_ROOM)
                            .withSerializable("songModel", songModel)
                            .navigation();
                }
            }, true);
            return;
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        mSkrAudioPermission.onBackFromPermisionManagerMaybe(getActivity());
    }

    @Override
    public void destroy() {
        super.destroy();
        U.getSoundUtils().release(getTAG());
    }

    @Override
    public boolean useEventBus() {
        return false;
    }

    @Override
    public void loadSongsDetailItems(List<SongModel> list, int offset, boolean hasMore) {
        this.offset = offset;
        this.hasMore = hasMore;
        if (!hasMore) {
            mRefreshLayout.setEnableLoadMore(false);
            mRefreshLayout.finishLoadMore();
            if (datas == null || datas.size() == 0) {
                mLoadService.showCallback(EmptyCallback.class);
            }
            return;
        }
        mRefreshLayout.finishLoadMore();
        if (datas == null) {
            datas = new ArrayList<>();
        }
        if (songSelectAdapter != null && list != null) {
            mLoadService.showSuccess();
            datas.addAll(list);
            songSelectAdapter.setDataList(datas);
            songSelectAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void loadSongsDetailItemsFail() {
        mRefreshLayout.finishLoadMore();
        mLoadService.showCallback(ErrorCallback.class);
    }
}
