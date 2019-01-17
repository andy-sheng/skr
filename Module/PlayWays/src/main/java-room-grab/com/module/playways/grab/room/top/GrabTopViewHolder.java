package com.module.playways.grab.room.top;

import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.common.core.avatar.AvatarUtils;
import com.common.image.fresco.BaseImageView;
import com.common.view.recyclerview.RecyclerOnItemClickListener;
import com.module.playways.grab.songselect.SpecialModel;
import com.module.rank.R;

public class GrabTopViewHolder extends RecyclerView.ViewHolder {

    BaseImageView mAvatarIv;

    GrabTopModel mModel;
    int mPosition;
    private RecyclerOnItemClickListener<SpecialModel> mItemClickListener;

    public GrabTopViewHolder(View itemView) {
        super(itemView);
        mAvatarIv = (BaseImageView) itemView.findViewById(R.id.avatar_iv);
    }

    public void bindData(GrabTopModel specialModel, int postion) {
        this.mModel = specialModel;
        this.mPosition = postion;
        AvatarUtils.loadAvatarByUrl(mAvatarIv, AvatarUtils.newParamsBuilder(mModel.getAvatar())
                .setCircle(true)
                .setBorderColorBySex(mModel.getSex() == 1)
                .build()
        );
    }

}
