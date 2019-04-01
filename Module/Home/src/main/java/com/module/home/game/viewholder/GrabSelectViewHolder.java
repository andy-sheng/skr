package com.module.home.game.viewholder;

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
import com.module.home.R;

public class GrabSelectViewHolder extends RecyclerView.ViewHolder {

    public final static String TAG = "GrabSelectViewHolder";

    SimpleDraweeView mBackground;

    SpecialModel mSpecialModel;
    int mPosition;

    public GrabSelectViewHolder(View itemView, RecyclerOnItemClickListener mItemClickListener) {
        super(itemView);

        mBackground = (SimpleDraweeView) itemView.findViewById(R.id.background);

        itemView.setOnClickListener(new AnimateClickListener() {
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

        FrescoWorker.loadImage(mBackground, ImageFactory.newHttpImage(mSpecialModel.getBgImage1())
                .setLoadingDrawable(U.getDrawable(R.drawable.grab_img_btn_loading1))
                .setScaleType(ScalingUtils.ScaleType.FIT_XY)
                .build());
    }
}
