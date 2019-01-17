package com.module.playways.grab.room.top;

import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.common.view.recyclerview.DiffAdapter;
import com.common.view.recyclerview.RecyclerOnItemClickListener;
import com.module.playways.grab.songselect.SpecialCardViewHolder;
import com.module.playways.grab.songselect.SpecialModel;
import com.module.rank.R;

public class GrabTopAdapter extends DiffAdapter<GrabTopModel, GrabTopViewHolder> {


    public GrabTopAdapter() {
    }

    @NonNull
    @Override
    public GrabTopViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.grab_top_view_holder_layout, parent, false);
        GrabTopViewHolder itemHolder = new GrabTopViewHolder(view);
        return itemHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull GrabTopViewHolder holder, int position) {
        GrabTopModel specialModel = mDataList.get(position);
        holder.bindData(specialModel, position);
    }


    @Override
    public int getItemCount() {
        return mDataList.size();
    }
}
