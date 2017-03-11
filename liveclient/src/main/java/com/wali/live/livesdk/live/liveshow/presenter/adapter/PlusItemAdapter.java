package com.wali.live.livesdk.live.liveshow.presenter.adapter;

import android.support.annotation.DrawableRes;
import android.support.annotation.IdRes;
import android.support.annotation.StringRes;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.base.global.GlobalData;
import com.wali.live.livesdk.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by xzy on 16-12-8.
 *
 * @module 直播加面板按钮显示适配器
 */
public class PlusItemAdapter extends RecyclerView.Adapter<PlusItemAdapter.PlusHolder> {
    private LayoutInflater mInflater;
    private ArrayList<PlusItem> mPlusItems = new ArrayList<>(0);
    private int mItemPlusWidth = GlobalData.screenWidth >> 1;
    private View.OnClickListener mListener;

    @Override
    public PlusHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (mInflater == null) {
            mInflater = LayoutInflater.from(parent.getContext());
        }
        View view = mInflater.inflate(R.layout.plus_item, null);
        return new PlusHolder(view, mItemPlusWidth);
    }

    @Override
    public void onBindViewHolder(PlusHolder holder, int position) {
        holder.bindView(mPlusItems.get(position), mListener);
    }

    @Override
    public int getItemCount() {
        return mPlusItems.size();
    }

    public void setPlusData(List<PlusItem> plusItems) {
        mPlusItems.clear();
        if (plusItems != null) {
            mPlusItems.addAll(plusItems);
        }
        notifyDataSetChanged();
    }

    public void setOnClickListener(View.OnClickListener listener) {
        mListener = listener;
    }

    public static class PlusHolder extends RecyclerView.ViewHolder {
        TextView mTvPlus;

        public PlusHolder(View view, int width) {
            super(view);
            mTvPlus = (TextView) view.findViewById(R.id.tv_plus);
            RecyclerView.LayoutParams params =
                    new RecyclerView.LayoutParams(width, width);
            itemView.setLayoutParams(params);
        }

        public void bindView(PlusItem plusItem, View.OnClickListener listener) {
            mTvPlus.setId(plusItem.idRes);
            mTvPlus.setText(plusItem.titleRes);
            mTvPlus.setCompoundDrawablesWithIntrinsicBounds(0, plusItem.iconRes, 0, 0);
            mTvPlus.setEnabled(plusItem.isClickable);
            mTvPlus.setOnClickListener(listener);
        }
    }

    public static class PlusItem {
        private int idRes;
        private int titleRes;
        private int iconRes;
        private boolean isClickable = true;

        public PlusItem(
                @IdRes int idRes,
                @StringRes int titleRes,
                @DrawableRes int iconRes) {
            this.idRes = idRes;
            this.titleRes = titleRes;
            this.iconRes = iconRes;
        }

        public PlusItem(
                @IdRes int idRes,
                @StringRes int titleRes,
                @DrawableRes int iconRes,
                boolean isClickable) {
            this.idRes = idRes;
            this.titleRes = titleRes;
            this.iconRes = iconRes;
            this.isClickable = isClickable;
        }
    }
}
