package com.wali.live.watchsdk.contest.rank.holder;

import android.view.View;

import com.wali.live.watchsdk.contest.rank.model.ContestRankItemModel;
import com.wali.live.watchsdk.lit.recycler.holder.BaseHolder;

import java.util.List;

/**
 * Created by lan on 2018/1/11.
 */
public abstract class BaseArrayHolder<VM extends ContestRankItemModel> extends BaseHolder<VM> {
    protected List<VM> mViewModels;

    public BaseArrayHolder(View itemView) {
        super(itemView);
    }

    public void bindModels(List<VM> viewModels) {
        mViewModels = viewModels;
        bindView();
    }

    public void bindModels(List<VM> viewModels, int position) {
        mPosition = position;
        bindModels(viewModels);
    }
}
