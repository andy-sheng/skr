package com.module.rankingmode.room.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;

import com.common.base.BaseFragment;
import com.module.rankingmode.room.presenter.EndGamePresenter;

// 游戏结束页
public class EndGameFragment extends BaseFragment {

    EndGamePresenter presenter;

    @Override
    public int initView() {
        return 0;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        presenter = new EndGamePresenter();
        addPresent(presenter);
    }
}
