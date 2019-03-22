package com.module.playways.rank.song.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.common.base.BaseFragment;
import com.common.rxretrofit.ApiResult;
import com.common.view.titlebar.CommonTitleBar;
import com.module.playways.rank.song.adapter.SongSelectAdapter;
import com.module.rank.R;
import com.orhanobut.dialogplus.DialogPlus;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.subjects.PublishSubject;

public class GrabSearchSongFragment extends BaseFragment {

    CommonTitleBar mTitlebar;

    RecyclerView mSearchResult;
    LinearLayoutManager mLinearLayoutManager;
    SongSelectAdapter mSongSelectAdapter;

    int mGameType;
    String mKeyword;
    DialogPlus mSearchFeedbackDialog;

    CompositeDisposable mCompositeDisposable;
    PublishSubject<String> mPublishSubject;
    DisposableObserver<ApiResult> mDisposableObserver;

    @Override
    public int initView() {
        return R.layout.grab_search_song_fragment_layout;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {

    }

    @Override
    public boolean useEventBus() {
        return false;
    }
}
