package com.module.home.game.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.common.view.DebounceViewClickListener;
import com.common.view.recyclerview.RecyclerOnItemClickListener;

public class GrabCreateViewHolder extends RecyclerView.ViewHolder {

    public GrabCreateViewHolder(View itemView, RecyclerOnItemClickListener mItemClickListener) {

        super(itemView);

        itemView.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {

            }
        });
    }

    public void bindData(int position) {

    }
}
