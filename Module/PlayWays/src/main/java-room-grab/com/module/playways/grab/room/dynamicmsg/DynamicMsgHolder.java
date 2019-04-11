package com.module.playways.grab.room.dynamicmsg;

import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.common.image.fresco.FrescoWorker;
import com.common.image.model.ImageFactory;
import com.common.view.DebounceViewClickListener;
import com.common.view.recyclerview.RecyclerOnItemClickListener;
import com.facebook.drawee.drawable.ScalingUtils;
import com.facebook.drawee.view.SimpleDraweeView;
import com.module.rank.R;

public class DynamicMsgHolder extends RecyclerView.ViewHolder {

    SimpleDraweeView mDynamicEmojiIv;

    int position;
    DynamicModel mDynamicModel;

    public DynamicMsgHolder(View itemView, RecyclerOnItemClickListener<DynamicModel> mListener) {
        super(itemView);

        mDynamicEmojiIv = (SimpleDraweeView) itemView.findViewById(R.id.dynamic_emoji_iv);

        itemView.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                if (mListener != null) {
                    mListener.onItemClicked(v, position, mDynamicModel);
                }
            }
        });
    }

    public void bindData(int position, DynamicModel model) {
        this.position = position;
        this.mDynamicModel = model;

        FrescoWorker.loadImage(mDynamicEmojiIv,
                ImageFactory.newPathImage(mDynamicModel.getSmallEmojiURL())
                        .setScaleType(ScalingUtils.ScaleType.CENTER_INSIDE)
                        .build());

    }

}
