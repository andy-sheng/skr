package com.module.home.game.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.common.view.DebounceViewClickListener;
import com.common.view.recyclerview.RecyclerOnItemClickListener;

public class GrabCreateViewHolder extends RecyclerView.ViewHolder {
    int position;
    public GrabCreateViewHolder(View itemView, RecyclerOnItemClickListener mItemClickListener) {
        super(itemView);
        itemView.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                if (mItemClickListener != null) {
                    mItemClickListener.onItemClicked(v, position, null);
                }
            }
        });
    }

    public void bindData(int position) {
        this.position = position;
    }
}
