package com.wali.live.livesdk.live.liveshow.presenter.adapter;

import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.base.image.fresco.FrescoWorker;
import com.base.image.fresco.image.ResImage;
import com.facebook.drawee.view.SimpleDraweeView;
import com.wali.live.common.listener.OnItemClickListener;
import com.wali.live.livesdk.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhangyuehuan on 16/9/8.
 *
 * @module 滤镜
 */
public class FilterItemAdapter extends RecyclerView.Adapter<FilterItemAdapter.ViewHolder>
        implements View.OnClickListener {

    private static final String TAG = FilterItemAdapter.class.getSimpleName();

    private List<FilterItem> mFilterItems = new ArrayList<>();

    private OnItemClickListener mClickListener;

    private int mCurrPosition; // 默认是0，普通

    private IFilterItemListener mListener;

    @Override
    public void onClick(View v) {
        Integer position = (Integer) v.getTag();
        if (position == null || mCurrPosition == position) {
            return;
        }
        if (mCurrPosition >= 0) {
            notifyItemChanged(mCurrPosition);
        }
        mCurrPosition = position;
        notifyItemChanged(mCurrPosition);
        if (mListener != null) {
            mListener.onItemSelected(mFilterItems.get(mCurrPosition).filter);
        }
    }

    public FilterItemAdapter(
            IFilterItemListener listener) {
        mListener = listener;
    }

    public void setItemData(List<FilterItem> sampleItemList, String currFilter) {
        if (sampleItemList != null && !sampleItemList.isEmpty()) {
            mFilterItems.clear();
            mFilterItems.addAll(sampleItemList);
        }
        if (!TextUtils.isEmpty(currFilter)) {
            mCurrPosition = mFilterItems.indexOf(currFilter);
        }
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return mFilterItems.size();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(
                R.layout.filter_item, viewGroup, false);
        return new ViewHolder(view, this);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        FilterItem filterItem = mFilterItems.get(position);
        FrescoWorker.loadImage(holder.mIconView, new ResImage(filterItem.iconRes));
        holder.mTitleView.setText(filterItem.titleRes);
        holder.itemView.setTag(position);
        // 滑动的时候防止view复用，造成的边界线错乱问题
        if (mCurrPosition == position) {
            holder.mForegroundView.setVisibility(View.VISIBLE);
        } else {
            holder.mForegroundView.setVisibility(View.GONE);
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private SimpleDraweeView mIconView;
        private TextView mTitleView;
        private View mForegroundView;

        public ViewHolder(View itemView, View.OnClickListener listener) {
            super(itemView);
            mIconView = (SimpleDraweeView) itemView.findViewById(R.id.icon_view);
            mTitleView = (TextView) itemView.findViewById(R.id.title_view);
            mForegroundView = itemView.findViewById(R.id.foreground_view);
            itemView.setOnClickListener(listener);
        }
    }

    public static class FilterItem implements Comparable<String> {
        private int titleRes;
        private int iconRes;
        private String filter;

        public FilterItem(
                @StringRes int titleRes,
                @DrawableRes int iconRes,
                @NonNull String filter) {
            this.titleRes = titleRes;
            this.iconRes = iconRes;
            this.filter = filter;
        }

        @Override
        public int compareTo(String another) {
            return filter.compareTo(another);
        }
    }

    public interface IFilterItemListener {

        void onItemSelected(String filter);

    }
}
