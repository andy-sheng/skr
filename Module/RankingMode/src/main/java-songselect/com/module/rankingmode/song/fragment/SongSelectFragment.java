package com.module.rankingmode.song.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.View;
import android.widget.RelativeLayout;

import com.common.base.BaseActivity;
import com.common.base.BaseFragment;
import com.common.base.FragmentDataListener;
import com.common.log.MyLog;
import com.common.utils.FragmentUtils;
import com.common.utils.U;
import com.common.view.ex.ExImageView;
import com.common.view.recyclerview.RecyclerOnItemClickListener;
import com.jakewharton.rxbinding2.view.RxView;
import com.module.rankingmode.R;
import com.module.rankingmode.prepare.fragment.PrepareResFragment;
import com.module.rankingmode.song.adapter.SongCardsAdapter;
import com.module.rankingmode.song.event.SwipCardEvent;
import com.module.rankingmode.song.layoutmanager.CardConfig;
import com.module.rankingmode.song.layoutmanager.OverLayCardLayoutManager;
import com.module.rankingmode.song.layoutmanager.TanTanCallback;
import com.module.rankingmode.song.model.SongCardModel;
import com.module.rankingmode.song.model.SongModel;
import com.module.rankingmode.song.presenter.SongTagDetailsPresenter;
import com.module.rankingmode.song.view.ISongTagDetailView;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class SongSelectFragment extends BaseFragment implements ISongTagDetailView {

    public static final int DEFAULT_FIRST_COUNT = 30; // 第一次从推荐页面拉去歌曲数
    public static final int DEFAULT_COUNT = 6;  // 从服务器拉去歌曲数

    RelativeLayout mMainActContainer;

    ExImageView mSelectBack;
    ExImageView mSelectBackIv;
    ExImageView mSelectClickedIv;

    RecyclerView mCardRecycleview;
    SongCardsAdapter adapter;

    SongTagDetailsPresenter presenter;

    List<SongCardModel> songCardModels; // 需要显示的数据
    List<SongCardModel> mDeleteList; // 已经滑走的数据

    int offset; //当前偏移量

    TanTanCallback callback;
    OverLayCardLayoutManager mOverLayCardLayoutManager;

    @Override
    public int initView() {
        return R.layout.song_select_fragment_layout;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        mMainActContainer = (RelativeLayout) mRootView.findViewById(R.id.main_act_container);
        mCardRecycleview = (RecyclerView) mRootView.findViewById(R.id.card_recycleview);

        mMainActContainer = (RelativeLayout) mRootView.findViewById(R.id.main_act_container);
        mCardRecycleview = (RecyclerView) mRootView.findViewById(R.id.card_recycleview);
        mSelectBackIv = (ExImageView) mRootView.findViewById(R.id.select_back_iv);
        mSelectClickedIv = (ExImageView) mRootView.findViewById(R.id.select_clicked_iv);
        mSelectBack = (ExImageView) mRootView.findViewById(R.id.select_back);


        RxView.clicks(mSelectBackIv)
                .throttleFirst(300, TimeUnit.MILLISECONDS)
                .subscribe(o -> {
                    backToLastCard();
                });

        RxView.clicks(mSelectClickedIv)
                .throttleFirst(300, TimeUnit.MILLISECONDS)
                .subscribe(o -> {
                    switchToClicked();
                });

        RxView.clicks(mSelectBack)
                .throttleFirst(300, TimeUnit.MILLISECONDS)
                .subscribe(o -> {
                    getActivity().finish();
                });

        mDeleteList = new ArrayList<>();
        songCardModels = new ArrayList<>();
        mOverLayCardLayoutManager = new OverLayCardLayoutManager();
        mCardRecycleview.setLayoutManager(mOverLayCardLayoutManager);
        adapter = new SongCardsAdapter(new RecyclerOnItemClickListener() {
            @Override
            public void onItemClicked(View view, int position, Object model) {
                SongModel songModel = (SongModel) model;
                U.getFragmentUtils().addFragment(FragmentUtils.newAddParamsBuilder((BaseActivity) getContext(), PrepareResFragment.class)
                        .setAddToBackStack(false)
                        .setNotifyHideFragment(SongSelectFragment.class)
                        .setHasAnimation(true)
                        .addDataBeforeAdd(0, songModel)
                        .setFragmentDataListener(new FragmentDataListener() {
                            @Override
                            public void onFragmentResult(int requestCode, int resultCode, Bundle bundle, Object obj) {

                            }
                        })
                        .build());
            }
        });

        mCardRecycleview.setAdapter(adapter);

        // 默认推荐
        presenter = new SongTagDetailsPresenter(this);
        addPresent(presenter);
        presenter.getRcomdMusicItems(0, DEFAULT_FIRST_COUNT);
    }

    @Override
    protected boolean onBackPressed() {
        U.getFragmentUtils().popFragment(new FragmentUtils.PopParams.Builder()
                .setPopFragment(SongSelectFragment.this)
                .setPopAbove(false)
                .build());

        getActivity().finish();
        return true;
    }

    // 返回上一张选歌卡片
    private void backToLastCard() {
        if (mDeleteList == null || mDeleteList.size() == 0) {
            U.getToastUtil().showShort("没有更多了");
            return;
        }

        SongCardModel songCardModel = mDeleteList.remove(0);

        if (songCardModels != null) {
            mOverLayCardLayoutManager.setDelete(true);
            songCardModels.add(songCardModel);
            adapter.notifyDataSetChanged();
        }
    }

    // TODO: 2018/12/17  切换到已点界面, 要不要保存当前记录的数据，取决从已点回来的逻辑 
    private void switchToClicked() {
        U.getFragmentUtils().addFragment(FragmentUtils.newAddParamsBuilder((BaseActivity) getContext(), HistorySongFragment.class)
                .setAddToBackStack(true)
                .setHasAnimation(true)
                .setEnterAnim(R.anim.slide_in_bottom)
                .setExitAnim(R.anim.slide_out_bottom)
                .setFragmentDataListener(new FragmentDataListener() {
                    @Override
                    public void onFragmentResult(int requestCode, int resultCode, Bundle bundle, Object obj) {

                    }
                })
                .build());
    }

    @Override
    public void loadSongsDetailItems(List<SongModel> list, int offset) {
        this.offset = offset; //保存当前偏移量
        SongCardModel songCardModel = new SongCardModel();
        for (int i = 0; i < list.size(); i++) {
            if (songCardModel == null) {
                songCardModel = new SongCardModel();
            }
            songCardModel.getList().add(list.get(i));

            if ((i + 1) % SongCardModel.MAX_COUNT == 0) {
                songCardModels.add(0, songCardModel);
                songCardModel = null;
            }
        }
        if (songCardModel != null) {
            songCardModels.add(0, songCardModel);
        }

        adapter.setmDataList(songCardModels);
        CardConfig.initConfig(getContext());

        callback = new TanTanCallback(mCardRecycleview, adapter, songCardModels, mDeleteList);

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(callback);
        itemTouchHelper.attachToRecyclerView(mCardRecycleview);
    }

    @Override
    public void loadSongsDetailItemsFail() {

    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public boolean useEventBus() {
        return true;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(SwipCardEvent event) {
        // TODO: 2018/12/17 要不要再去拉数据 当前是只去拉一页
        presenter.getRcomdMusicItems(offset, DEFAULT_COUNT);
    }

    @Override
    public void notifyToShow() {
        MyLog.d(TAG, "toStaskTop");
        mRootView.setVisibility(View.VISIBLE);
    }

    @Override
    public void notifyToHide() {
        MyLog.d(TAG, "pushIntoStash");
        mRootView.setVisibility(View.GONE);
    }
}
