package com.module.home.game.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.common.view.AnimateClickListener;
import com.common.view.recyclerview.RecyclerOnItemClickListener;

public class GrabCreateViewHolder extends RecyclerView.ViewHolder {
    int position;

    public GrabCreateViewHolder(View itemView, RecyclerOnItemClickListener mItemClickListener) {
        super(itemView);
        itemView.setOnClickListener(new AnimateClickListener() {
            @Override
            public void click(View view) {
                if (mItemClickListener != null) {
                    mItemClickListener.onItemClicked(view, position, null);
                }
            }
        });
    }

    public void bindData(int position) {
        this.position = position;
    }
}
