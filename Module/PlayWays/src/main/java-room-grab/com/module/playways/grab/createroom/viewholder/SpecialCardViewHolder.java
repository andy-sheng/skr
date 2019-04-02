package com.module.playways.grab.createroom.viewholder;


import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.common.image.fresco.FrescoWorker;
import com.common.image.model.ImageFactory;
import com.common.utils.U;
import com.common.view.AnimateClickListener;
import com.common.view.recyclerview.RecyclerOnItemClickListener;
import com.component.busilib.friends.SpecialModel;
import com.facebook.drawee.drawable.ScalingUtils;
import com.facebook.drawee.view.SimpleDraweeView;
import com.module.rank.R;

public class SpecialCardViewHolder extends RecyclerView.ViewHolder {
    public final static String TAG = "SpecialCardViewHolder";
    SimpleDraweeView mBackground;

    SpecialModel mSpecialModel;
    int mPosition;
    private RecyclerOnItemClickListener<SpecialModel> mItemClickListener;

    public SpecialCardViewHolder(View itemView) {
        super(itemView);

        mBackground = (SimpleDraweeView) itemView.findViewById(R.id.background);

        itemView.setOnClickListener(new AnimateClickListener(500) {
            @Override
            public void click(View view) {
                if (mItemClickListener != null) {
                    mItemClickListener.onItemClicked(view, mPosition, mSpecialModel);
                }
            }
        });
    }

    public void bindData(SpecialModel specialModel, int postion) {
        this.mSpecialModel = specialModel;
        this.mPosition = postion;
        FrescoWorker.loadImage(mBackground, ImageFactory.newHttpImage(mSpecialModel.getBgImage2())
                .setLoadingDrawable(U.getDrawable(R.drawable.grab_img_btn_loading2))
                .setLoadingScaleType(ScalingUtils.ScaleType.FIT_XY)
                .setScaleType(ScalingUtils.ScaleType.FIT_XY)
                .build());
    }

    public void setItemClickListener(RecyclerOnItemClickListener<SpecialModel> itemClickListener) {
        mItemClickListener = itemClickListener;
    }

    public RecyclerOnItemClickListener<SpecialModel> getItemClickListener() {
        return mItemClickListener;
    }
}
