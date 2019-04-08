package com.zq.person.holder;

import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.common.image.fresco.FrescoWorker;
import com.common.image.model.ImageFactory;
import com.common.image.model.oss.OssImgFactory;
import com.common.utils.ImageUtils;
import com.common.utils.U;
import com.common.view.DebounceViewClickListener;
import com.common.view.recyclerview.RecyclerOnItemClickListener;
import com.component.busilib.R;
import com.facebook.drawee.view.SimpleDraweeView;
import com.zq.person.model.PhotoModel;

public class PhotoViewHolder extends RecyclerView.ViewHolder {

    SimpleDraweeView mPhotoIv;
    PhotoModel mPhotoModel;
    int position;

    RecyclerOnItemClickListener mListener;

    public PhotoViewHolder(View itemView, RecyclerOnItemClickListener listener) {
        super(itemView);
        this.mListener = listener;
        mPhotoIv = (SimpleDraweeView) itemView.findViewById(R.id.photo_iv);

        itemView.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                if (mListener != null) {
                    mListener.onItemClicked(v, position, mPhotoModel);
                }
            }
        });
    }

    public void bindData(PhotoModel photoModel, int position) {
        this.mPhotoModel = photoModel;
        this.position = position;

        FrescoWorker.loadImage(mPhotoIv,
                ImageFactory.newHttpImage(mPhotoModel.getPicPath())
                        .setCornerRadius(U.getDisplayUtils().dip2px(8))
                        .setBorderWidth(U.getDisplayUtils().dip2px(2))
                        .addOssProcessors(OssImgFactory.newResizeBuilder().setW(ImageUtils.SIZE.SIZE_160.getW()).build())
                        .setBorderColor(Color.parseColor("#3B4E79")).build());
    }
}
