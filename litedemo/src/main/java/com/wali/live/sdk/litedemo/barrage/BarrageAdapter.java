package com.wali.live.sdk.litedemo.barrage;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.wali.live.sdk.litedemo.R;

/**
 * Created by lan on 17/5/3.
 */
public class BarrageAdapter extends RecyclerView.Adapter<BarrageAdapter.BarrageHolder> {
    @Override
    public BarrageHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_barrage_item, parent, false);
        return new BarrageHolder(view);
    }

    @Override
    public void onBindViewHolder(BarrageHolder holder, int position) {
        holder.bindView();
    }

    @Override
    public int getItemCount() {
        return 0;
    }

    public static class BarrageHolder extends RecyclerView.ViewHolder {
        private TextView mBarrageTv;

        public BarrageHolder(View itemView) {
            super(itemView);
        }

        public void bindView() {
        }
    }
}
