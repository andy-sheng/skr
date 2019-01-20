package com.module.playways.grab.songselect;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.RelativeLayout;

import com.alibaba.fastjson.JSON;
import com.common.base.BaseFragment;
import com.common.rxretrofit.ApiManager;
import com.common.rxretrofit.ApiMethods;
import com.common.rxretrofit.ApiObserver;
import com.common.rxretrofit.ApiResult;
import com.common.utils.FragmentUtils;
import com.common.utils.U;
import com.common.view.ex.ExImageView;
import com.common.view.recyclerview.RecyclerOnItemClickListener;
import com.common.view.titlebar.CommonTitleBar;
import com.component.busilib.constans.GameModeType;
import com.jakewharton.rxbinding2.view.RxView;
import com.module.playways.grab.prepare.GrabMatchFragment;
import com.module.playways.rank.prepare.model.PrepareData;
import com.module.playways.rank.song.model.SongModel;
import com.module.rank.R;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.functions.Consumer;

public class SpecialSelectFragment extends BaseFragment {

    RelativeLayout mMainActContainer;
    CommonTitleBar mTitleView;
    ExImageView mSelectLogoIv;
    RecyclerView mContentRv;
    SpecialSelectAdapter mSpecialSelectAdapter;
    LinearLayoutManager mLinearLayoutManager;

    @Override
    public int initView() {
        return R.layout.special_select_fragment_layout;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        mMainActContainer = (RelativeLayout) mRootView.findViewById(R.id.main_act_container);
        mTitleView = (CommonTitleBar) mRootView.findViewById(R.id.title_view);
        mSelectLogoIv = (ExImageView) mRootView.findViewById(R.id.select_logo_iv);
        mContentRv = (RecyclerView) mRootView.findViewById(R.id.content_rv);
        mLinearLayoutManager = new LinearLayoutManager(getContext());
        mContentRv.setLayoutManager(mLinearLayoutManager);
        mSpecialSelectAdapter = new SpecialSelectAdapter(new RecyclerOnItemClickListener<SpecialModel>() {
            @Override
            public void onItemClicked(View view, int position, SpecialModel model) {
                goMatchFragment(model.getTagID());
            }
        });
        mContentRv.setAdapter(mSpecialSelectAdapter);
        loadData();

        RxView.clicks(mTitleView.getLeftImageButton())
                .throttleFirst(500, TimeUnit.MILLISECONDS)
                .subscribe(new Consumer<Object>() {
                    @Override
                    public void accept(Object o) {
                        getActivity().finish();
                    }
                });
    }

    private void loadData() {
        GrabSongApi grabSongApi = ApiManager.getInstance().createService(GrabSongApi.class);
        ApiMethods.subscribe(grabSongApi.getSepcialList(0, 10), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult obj) {
                if (obj.getErrno() == 0) {
                    List<SpecialModel> list = JSON.parseArray(obj.getData().getString("tags"), SpecialModel.class);
                    if (list != null) {
                        mSpecialSelectAdapter.setDataList(list);
                    }
                }
            }
        });
    }

    @Override
    public boolean useEventBus() {
        return false;
    }

    public void goMatchFragment(int specialId) {
        PrepareData prepareData = new PrepareData();
        prepareData.setGameType(GameModeType.GAME_MODE_GRAB);
        SongModel songModel = new SongModel();
        songModel.setItemID(specialId);
        prepareData.setSongModel(songModel);
        U.getFragmentUtils().addFragment(FragmentUtils.newAddParamsBuilder(getActivity(), GrabMatchFragment.class)
                .setAddToBackStack(true)
                .addDataBeforeAdd(0, prepareData)
                .setHasAnimation(true)
                .build()
        );

    }
}
