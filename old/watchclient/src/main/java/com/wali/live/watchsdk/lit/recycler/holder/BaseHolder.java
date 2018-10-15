package com.wali.live.watchsdk.lit.recycler.holder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.wali.live.watchsdk.lit.recycler.viewmodel.BaseViewModel;

/**
 * Created by lan on 16/6/28.
 */
public abstract class BaseHolder<VM extends BaseViewModel> extends RecyclerView.ViewHolder {
    protected final String TAG = getTAG();

    protected VM mViewModel;
    protected int mPosition;

    public BaseHolder(View itemView) {
        super(itemView);
        initView();
    }

    protected String getTAG() {
        return getClass().getSimpleName();
    }

    public <H extends BaseHolder> H get() {
        return (H) this;
    }

    protected abstract void initView();

    public void bindNull() {
        bindView();
    }

    public void bindModel(VM viewModel) {
        mViewModel = viewModel;
        bindView();
    }

    public void bindModel(VM viewModel, int position) {
        mPosition = position;
        bindModel(viewModel);
    }

    protected abstract void bindView();

    protected <V extends View> V $(int id) {
        return (V) itemView.findViewById(id);
    }

    protected <V extends View> V $(ViewGroup parent, int id) {
        return (V) (parent.findViewById(id));
    }
}
