package com.module.playways.grab.room.dynamicmsg;

import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.common.view.recyclerview.DiffAdapter;
import com.common.view.recyclerview.RecyclerOnItemClickListener;
import com.module.playways.R;

public class DynamicMsgAdapter extends DiffAdapter<DynamicModel, DynamicMsgHolder> {

    RecyclerOnItemClickListener<DynamicModel> mListener;

    public DynamicMsgAdapter(RecyclerOnItemClickListener<DynamicModel> listener) {
        this.mListener = listener;
    }

    @NonNull
    @Override
    public DynamicMsgHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.dynamic_msg_item_layout, parent, false);
        DynamicMsgHolder viewHolder = new DynamicMsgHolder(view, mListener);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull DynamicMsgHolder holder, int position) {
        DynamicModel model = mDataList.get(position);
        holder.bindData(position, model);
    }

    @Override
    public int getItemCount() {
        return mDataList.size();
    }
}
