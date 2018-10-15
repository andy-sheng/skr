package com.wali.live.watchsdk.feedback.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.feedback.holder.ReportViewHolder;
import com.wali.live.watchsdk.feedback.listener.OnItemListener;

import java.util.List;

/**
 * Created by zhujianning on 18-7-5.
 */

public class ReportReasonAdapter extends RecyclerView.Adapter {
    private int ITEM_TYPE_NORMAL = 0;
    private int ITEM_TYPE_OTHER = 1;

    private List<String> mReasonList;

    private OnItemListener mOnItemListener;

    public void setReasonList(List<String> list) {
        mReasonList = list;
        notifyDataSetChanged();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.new_report_dialog_item, parent, false);
        ReportViewHolder reportViewHolder = new ReportViewHolder(view);
        reportViewHolder.setListener(mOnItemListener);
        return reportViewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        if(holder instanceof ReportViewHolder) {
            ((ReportViewHolder) holder).bind(mReasonList.get(position), position, position == getItemCount() - 1);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (position == mReasonList.size() - 1) {
            return ITEM_TYPE_OTHER;
        }
        return ITEM_TYPE_NORMAL;
    }


    @Override
    public int getItemCount() {
        if (mReasonList != null) {
            return mReasonList.size();
        }
        return 0;
    }

    public void setListener(OnItemListener onItemListener) {
        this.mOnItemListener = onItemListener;
    }
}
