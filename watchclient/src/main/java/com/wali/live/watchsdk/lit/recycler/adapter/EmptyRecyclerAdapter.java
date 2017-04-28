package com.wali.live.watchsdk.lit.recycler.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.base.global.GlobalData;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.lit.recycler.holder.BaseHolder;
import com.wali.live.watchsdk.lit.recycler.holder.EmptyHolder;
import com.wali.live.watchsdk.lit.recycler.viewmodel.EmptyModel;

/**
 * Created by lan on 16/3/2.
 */
public abstract class EmptyRecyclerAdapter extends RecyclerView.Adapter<BaseHolder> {
    private static final int TYPE_EMPTY = 0xffff;

    private EmptyModel mEmptyModel;
    private boolean mIsEmpty = false;

    public EmptyRecyclerAdapter() {
        mEmptyModel = new EmptyModel(GlobalData.app().getString(R.string.empty_tips),
                R.drawable.home_empty_icon);
    }

    @Override
    public BaseHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (mIsEmpty) {
            if (viewType == TYPE_EMPTY) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.empty_view, parent, false);
                return new EmptyHolder(view);
            }
            return null;
        }
        return onCreateHolder(parent, viewType);
    }

    @Override
    public void onBindViewHolder(BaseHolder holder, int position) {
        if (mIsEmpty) {
            holder.bindModel(mEmptyModel);
            return;
        }
        onBindHolder(holder, position);
    }

    @Override
    public int getItemViewType(int position) {
        if (mIsEmpty) {
            return TYPE_EMPTY;
        }
        return getItemType(position);
    }

    @Override
    public int getItemCount() {
        mIsEmpty = isEmpty();
        return mIsEmpty ? 1 : getDataCount();
    }

    protected abstract int getDataCount();

    protected abstract BaseHolder onCreateHolder(ViewGroup parent, int viewType);

    protected abstract void onBindHolder(BaseHolder holder, int position);

    protected abstract int getItemType(int position);

    // 如果有自己的判空逻辑，请重写该方法
    protected boolean isEmpty() {
        return getDataCount() == 0;
    }

    public void setEmptyTips(String tips) {
        mEmptyModel.setText(tips);
        if (isEmpty()) {
            notifyDataSetChanged();
        }
    }

    public void setIconId(int iconId) {
        mEmptyModel.setIconId(iconId);
        if (isEmpty()) {
            notifyDataSetChanged();
        }
    }
}
