package com.module.playways.grab.room.top;

import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.common.core.avatar.AvatarUtils;
import com.common.image.fresco.BaseImageView;
import com.common.utils.U;
import com.common.view.ex.ExTextView;
import com.common.view.recyclerview.RecyclerOnItemClickListener;
import com.module.playways.grab.songselect.SpecialModel;
import com.module.rank.R;

public class GrabTopViewHolder extends RecyclerView.ViewHolder {

    BaseImageView mAvatarIv;
    ExTextView mDescTv;

    GrabTopModel mModel;
    int mPosition;
    private RecyclerOnItemClickListener<SpecialModel> mItemClickListener;

    public GrabTopViewHolder(View itemView) {
        super(itemView);
        mAvatarIv =  itemView.findViewById(R.id.avatar_iv);
        mDescTv =  itemView.findViewById(R.id.desc_tv);

    }

    public void bindData(GrabTopModel specialModel, int postion) {
        this.mModel = specialModel;
        this.mPosition = postion;

        AvatarUtils.loadAvatarByUrl(mAvatarIv, AvatarUtils.newParamsBuilder(mModel.getAvatar())
                .setCircle(true)
                .setBorderWidth(U.getDisplayUtils().dip2px(2))
                .setBorderColorBySex(mModel.getSex() == 1)
                .build()
        );
        mDescTv.setText(String.valueOf(specialModel.getStatus()));
    }

    public GrabTopModel getModel() {
        return mModel;
    }
}
