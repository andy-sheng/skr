package com.module.playways.grab.songselect;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.RelativeLayout;

import com.alibaba.android.arouter.launcher.ARouter;
import com.alibaba.fastjson.JSON;
import com.common.base.BaseFragment;
import com.common.core.account.UserAccountManager;
import com.common.core.permission.SkrAudioPermission;
import com.common.rxretrofit.ApiManager;
import com.common.rxretrofit.ApiMethods;
import com.common.rxretrofit.ApiObserver;
import com.common.rxretrofit.ApiResult;
import com.common.statistics.StatConstants;
import com.common.statistics.StatisticsAdapter;
import com.common.utils.U;
import com.common.view.DebounceViewClickListener;
import com.common.view.ex.ExImageView;
import com.common.view.recyclerview.RecyclerOnItemClickListener;
import com.common.view.titlebar.CommonTitleBar;
import com.component.busilib.callback.EmptyCallback;
import com.component.busilib.callback.ErrorCallback;
import com.component.busilib.callback.LoadingCallback;
import com.component.busilib.constans.GameModeType;
import com.jakewharton.rxbinding2.view.RxView;

import com.kingja.loadsir.callback.Callback;
import com.kingja.loadsir.core.LoadService;
import com.kingja.loadsir.core.LoadSir;
import com.module.RouterConstants;

import com.module.playways.rank.prepare.model.PrepareData;
import com.module.rank.R;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnRefreshLoadMoreListener;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.functions.Consumer;

public class SpecialSelectFragment extends BaseFragment {

    public final static String TAG = "SpecialSelectFragment";

    RelativeLayout mMainActContainer;
    ExImageView mIvBack;

    SmartRefreshLayout mRefreshLayout;
    RecyclerView mContentRv;
    SpecialSelectAdapter mSpecialSelectAdapter;

    LoadService mLoadService;

    int offset = 0;          //偏移量
    int DEFAULT_COUNT = 10;  // 每次拉去列表数目

    List<String> musicURLs;  //背景音乐
    SkrAudioPermission mSkrAudioPermission = new SkrAudioPermission();

    @Override
    public int initView() {
        return R.layout.special_select_fragment_layout;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        mMainActContainer = (RelativeLayout) mRootView.findViewById(R.id.main_act_container);
        mRefreshLayout = (SmartRefreshLayout) mRootView.findViewById(R.id.refreshLayout);
        mContentRv = (RecyclerView) mRootView.findViewById(R.id.content_rv);
        mIvBack = (ExImageView)mRootView.findViewById(R.id.iv_back);

        mRefreshLayout.setEnableRefresh(false);
        mRefreshLayout.setEnableLoadMore(true);
        mRefreshLayout.setEnableLoadMoreWhenContentNotFull(true);
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
        mContentRv.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));

        mSpecialSelectAdapter = new SpecialSelectAdapter(new RecyclerOnItemClickListener<SpecialModel>() {
            @Override
            public void onItemClicked(View view, int position, SpecialModel model) {
                mSkrAudioPermission.ensurePermission(new Runnable() {
                    @Override
                    public void run() {
                        U.getSoundUtils().play(SpecialSelectFragment.TAG, R.raw.normal_click, 500);
                        goMatchFragment(model.getTagID());
                        HashMap map = new HashMap();
                        map.put("tagId",model.getTagID());
                        StatisticsAdapter.recordCountEvent(UserAccountManager.getInstance().getGategory(StatConstants.CATEGORY_GRAB),
                                StatConstants.KEY_MATCH_START, map);
                    }
                }, true);

            }
        });
        mContentRv.setAdapter(mSpecialSelectAdapter);
        loadData(offset, DEFAULT_COUNT);
        getBackgroundMusic();

        mIvBack.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                U.getSoundUtils().play(SpecialSelectFragment.TAG, R.raw.normal_back, 500);
                if (getActivity() != null) {
                    getActivity().finish();
                }
            }
        });


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

        U.getSoundUtils().preLoad(TAG, R.raw.normal_back, R.raw.normal_click);

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

    @Override
    public void onResume() {
        super.onResume();
        StatisticsAdapter.recordCountEvent(UserAccountManager.getInstance().getGategory(StatConstants.CATEGORY_GRAB),
                StatConstants.KEY_SELECTSONG_EXPOSE, null);
        mSkrAudioPermission.onBackFromPermisionManagerMaybe();
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

    @Override
    public void destroy() {
        super.destroy();
        U.getSoundUtils().release(TAG);
    }

    @Override
    public boolean useEventBus() {
        return false;
    }

    public void goMatchFragment(int specialId) {
        PrepareData prepareData = new PrepareData();
        prepareData.setGameType(GameModeType.GAME_MODE_GRAB);
        prepareData.setTagId(specialId);

        if (musicURLs != null && musicURLs.size() > 0) {
            prepareData.setBgMusic(musicURLs.get(0));
        }

        ARouter.getInstance()
                .build(RouterConstants.ACTIVITY_GRAB_MATCH_ROOM)
                .withSerializable("prepare_data", prepareData)
                .navigation();
        if (getActivity() != null) {
            getActivity().finish();
        }
    }
}
