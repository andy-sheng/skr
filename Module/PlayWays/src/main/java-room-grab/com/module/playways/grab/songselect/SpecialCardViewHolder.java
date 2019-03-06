package com.module.playways.grab.songselect;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;

import com.common.view.ex.ExTextView;
import com.common.view.recyclerview.RecyclerOnItemClickListener;
import com.module.rank.R;

public class SpecialCardViewHolder extends RecyclerView.ViewHolder {

    ImageView mBackground;
    ExTextView mSpecialTv;
    ExTextView mIntroductionTv;


    SpecialModel mSpecialModel;
    int mPosition;
    private RecyclerOnItemClickListener<SpecialModel> mItemClickListener;

    public SpecialCardViewHolder(View itemView) {
        super(itemView);
        mBackground = (ImageView) itemView.findViewById(R.id.background);
        mSpecialTv = (ExTextView) itemView.findViewById(R.id.special_tv);
        mIntroductionTv = (ExTextView) itemView.findViewById(R.id.introduction_tv);

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
        if (postion % 3 == 0) {
            mBackground.setBackgroundResource(R.drawable.zhuanchang_blue);
        }else if (postion % 3 == 1){
            mBackground.setBackgroundResource(R.drawable.zhuanchang_red);
        }else if (postion % 3 == 2){
            mBackground.setBackgroundResource(R.drawable.zhuanchang_yellow);
        }
        mSpecialTv.setText(this.mSpecialModel.getTagName());
        mIntroductionTv.setText(this.mSpecialModel.getIntroduction());
    }

    public void setItemClickListener(RecyclerOnItemClickListener<SpecialModel> itemClickListener) {
        mItemClickListener = itemClickListener;
    }

    public RecyclerOnItemClickListener<SpecialModel> getItemClickListener() {
        return mItemClickListener;
    }
}
