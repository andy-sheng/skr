package com.module.rankingmode.room.quickmsg;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.common.view.recyclerview.DiffAdapter;
import com.common.view.recyclerview.RecyclerOnItemClickListener;
import com.module.rankingmode.R;

public class QuickMsgAdapter extends DiffAdapter<QuickMsgModel,RecyclerView.ViewHolder> {

    RecyclerOnItemClickListener mRecyclerOnItemClickListener;

    public QuickMsgAdapter(RecyclerOnItemClickListener l) {
        mRecyclerOnItemClickListener = l;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.quick_msg_view_holder, parent, false);
        QuickMsgHolder viewHolder = new QuickMsgHolder(view);
        viewHolder.setListener(mRecyclerOnItemClickListener);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        QuickMsgModel quickMsgModel = mDataList.get(position);
        if(holder instanceof QuickMsgHolder){
            QuickMsgHolder quickMsgHolder = (QuickMsgHolder) holder;
            quickMsgHolder.bind(position,quickMsgModel);
        }
    }

    @Override
    public int getItemCount() {
        return mDataList.size();
    }
}
