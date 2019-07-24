package com.component.person.holder;

import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;

import com.common.image.fresco.FrescoWorker;
import com.common.image.model.ImageFactory;
import com.common.image.model.oss.OssImgFactory;
import com.common.utils.ImageUtils;
import com.common.utils.U;
import com.common.view.DebounceViewClickListener;
import com.common.view.ex.ExFrameLayout;
import com.common.view.ex.ExImageView;
import com.common.view.ex.ExTextView;
import com.common.view.recyclerview.RecyclerOnItemClickListener;
import com.component.busilib.R;
import com.facebook.drawee.view.SimpleDraweeView;
import com.component.person.adapter.PhotoAdapter;
import com.component.person.model.PhotoModel;

public class PhotoViewHolder extends RecyclerView.ViewHolder {

    public final String TAG = "PhotoViewHolder";

    SimpleDraweeView mPhotoIv;
    ExTextView mUploadTipsTv;
    ExImageView mIvBlackBg;
    PhotoModel mPhotoModel;
    ExTextView mTvErrorTips;
    ExFrameLayout mErrorContainer;
    int position;

    RecyclerOnItemClickListener mListener;
    PhotoAdapter.PhotoManageListener mPhotoManageListener;

    /**
     * 这里传入 position 的话 insert delete 会导致postion不准
     */

    public PhotoViewHolder(View itemView, RecyclerOnItemClickListener listener) {
        super(itemView);
        this.mListener = listener;
        mPhotoIv = (SimpleDraweeView) itemView.findViewById(R.id.photo_iv);
        mUploadTipsTv = itemView.findViewById(R.id.upload_tips_tv);
        mIvBlackBg = (ExImageView) itemView.findViewById(R.id.iv_black_bg);
        mTvErrorTips = (ExTextView) itemView.findViewById(R.id.tv_error_tips);
        mErrorContainer = (ExFrameLayout) itemView.findViewById(R.id.error_container);

        itemView.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                if (mListener != null) {
                    mListener.onItemClicked(v, position, mPhotoModel);
                }
            }
        });

        mIvBlackBg.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                if (mPhotoManageListener != null) {
                    if (mPhotoModel.getStatus() == PhotoModel.STATUS_FAILED) {
                        mPhotoManageListener.reupload(mPhotoModel);
                    } else if (mPhotoModel.getStatus() == PhotoModel.STATUS_FAILED_SEXY) {
                        mPhotoManageListener.delete(mPhotoModel);
                    } else if (mPhotoModel.getStatus() == PhotoModel.STATUS_FAILED_LIMIT) {
                        mPhotoManageListener.delete(mPhotoModel);
                    }
                }
            }
        });
    }

    public void setPhotoManageListener(PhotoAdapter.PhotoManageListener photoManageListener) {
        mPhotoManageListener = photoManageListener;
    }

    public void bindData(PhotoModel photoModel, int position) {
        //MyLog.d(TAG, "bindData" + " photoModel=" + photoModel + " position=" + position);

        this.mPhotoModel = photoModel;
        this.position = position;

        String path = mPhotoModel.getPicPath();
        if (TextUtils.isEmpty(path)) {
            path = mPhotoModel.getLocalPath();
        }

        FrescoWorker.loadImage(mPhotoIv,
                ImageFactory.newPathImage(path)
                        .setCornerRadius(U.getDisplayUtils().dip2px(8))
                        .setBorderWidth(U.getDisplayUtils().dip2px(2))
                        .setFailureDrawable(U.app().getResources().getDrawable(R.drawable.load_img_error))
                        .setLoadingDrawable(U.app().getResources().getDrawable(R.drawable.loading_place_holder_img))
                        .setLowImageUri(ImageUtils.SIZE.SIZE_160)
                        .addOssProcessors(OssImgFactory.newResizeBuilder().setW(ImageUtils.SIZE.SIZE_320.getW()).build())
                        .setBorderColor(Color.parseColor("#3B4E79")).build());
        mUploadTipsTv.setVisibility(View.VISIBLE);
        mUploadTipsTv.setTextColor(Color.WHITE);
        mErrorContainer.setVisibility(View.GONE);

        if (mPhotoModel.getStatus() == PhotoModel.STATUS_DELETE) {
            mUploadTipsTv.setText("删除");
        } else if (mPhotoModel.getStatus() == PhotoModel.STATUS_UPLOADING) {
            mUploadTipsTv.setText("正在上传");
        } else if (mPhotoModel.getStatus() == PhotoModel.STATUS_WAIT_UPLOAD) {
            mUploadTipsTv.setText("等待上传");
        } else if (mPhotoModel.getStatus() == PhotoModel.STATUS_FAILED) {
            mUploadTipsTv.setVisibility(View.GONE);
            mErrorContainer.setVisibility(View.VISIBLE);
            mIvBlackBg.setImageDrawable(U.getDrawable(R.drawable.photo_chonglai));
            mTvErrorTips.setText("上传失败");
        } else if (mPhotoModel.getStatus() == PhotoModel.STATUS_SUCCESS) {
            mUploadTipsTv.setVisibility(View.GONE);
        } else if (mPhotoModel.getStatus() == PhotoModel.STATUS_FAILED_SEXY) {
            mUploadTipsTv.setVisibility(View.GONE);
            mErrorContainer.setVisibility(View.VISIBLE);
            mIvBlackBg.setImageDrawable(U.getDrawable(R.drawable.photo_shanchu));
            mTvErrorTips.setText("图片敏感");
        } else if (mPhotoModel.getStatus() == PhotoModel.STATUS_FAILED_LIMIT) {
            mUploadTipsTv.setVisibility(View.GONE);
            mErrorContainer.setVisibility(View.VISIBLE);
            mIvBlackBg.setImageDrawable(U.getDrawable(R.drawable.photo_shanchu));
            mTvErrorTips.setText("超过上限");
        }
    }
}
