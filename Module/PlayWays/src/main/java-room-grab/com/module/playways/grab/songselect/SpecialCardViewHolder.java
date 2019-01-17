package com.module.playways.grab.songselect;

import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.common.view.ex.ExTextView;
import com.common.view.recyclerview.RecyclerOnItemClickListener;
import com.module.rank.R;

public class SpecialCardViewHolder extends RecyclerView.ViewHolder {

    ExTextView mSpecialTv;
    SpecialModel mSpecialModel;
    int mPosition;
    private RecyclerOnItemClickListener<SpecialModel> mItemClickListener;

    public SpecialCardViewHolder(View itemView) {
        super(itemView);
        mSpecialTv = (ExTextView) itemView.findViewById(R.id.special_tv);
        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mItemClickListener != null) {
                    mItemClickListener.onItemClicked(v, mPosition, mSpecialModel);
                }
            }
        });
    }

    public void bindData(SpecialModel specialModel, int postion) {
        this.mSpecialModel = specialModel;
        this.mPosition = postion;
        mSpecialTv.setText(this.mSpecialModel.getSpecialName());
    }

    public void setItemClickListener(RecyclerOnItemClickListener<SpecialModel> itemClickListener) {
        mItemClickListener = itemClickListener;
    }

    public RecyclerOnItemClickListener<SpecialModel> getItemClickListener() {
        return mItemClickListener;
    }
}
