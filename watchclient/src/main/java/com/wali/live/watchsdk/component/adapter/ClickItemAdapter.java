package com.wali.live.watchsdk.component.adapter;

import android.content.res.Resources;
import android.support.annotation.IdRes;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yangli on 2017/4/24.
 *
 * @module 列表显示适配器
 */
public abstract class ClickItemAdapter<ITEM extends ClickItemAdapter.BaseItem,
        HOLDER extends ClickItemAdapter.BaseHolder, LISTENER> extends RecyclerView.Adapter<HOLDER> {
    protected LayoutInflater mInflater;
    protected List<ITEM> mItems = new ArrayList<>(0);
    protected LISTENER mListener;

    public ClickItemAdapter() {
    }

    public abstract HOLDER newViewHolder(ViewGroup parent, int viewType);

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    @Override
    public HOLDER onCreateViewHolder(ViewGroup parent, int viewType) {
        if (mInflater == null) {
            mInflater = LayoutInflater.from(parent.getContext());
        }
        return newViewHolder(parent, viewType);
    }

    @Override
    public void onBindViewHolder(HOLDER holder, int position) {
        holder.bindView(mItems.get(position), mListener);
    }

    public void insertItemData(int index, ITEM item) {
        mItems.add(index, item);
        notifyItemInserted(index);
    }

    public void setItemData(List<? extends ITEM> items) {
        mItems.clear();
        if (items != null) {
            mItems.addAll(items);
        }
        notifyDataSetChanged();
    }

    public void setClickListener(LISTENER listener) {
        mListener = listener;
    }

    public static abstract class BaseHolder<ITEM extends BaseItem, LISTENER>
            extends RecyclerView.ViewHolder {

        protected final <T extends View> T $(@IdRes int resId) {
            return (T) itemView.findViewById(resId);
        }

        protected final Resources getResources() {
            return itemView.getResources();
        }

        public BaseHolder(View view) {
            super(view);
        }

        public BaseHolder(View view, int width, int height) {
            this(view);
            RecyclerView.LayoutParams params =
                    new RecyclerView.LayoutParams(width, height);
            itemView.setLayoutParams(params);
        }

        public abstract void bindView(ITEM item, LISTENER listener);
    }

    public static abstract class BaseItem {
    }
}
