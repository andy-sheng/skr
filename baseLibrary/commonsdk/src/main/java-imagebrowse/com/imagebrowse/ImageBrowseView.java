package com.imagebrowse;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import com.common.image.model.BaseImage;
import com.common.log.MyLog;
import com.common.view.photodraweeview.OnPhotoTapListener;
import com.common.view.photodraweeview.OnViewTapListener;
import com.common.view.photodraweeview.PhotoDraweeView;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;

/**
 * 这个view过于复杂，在使用viewpager时就别重复使用了吧
 * 职责：保证清晰流畅的显示出图片 不管多大
 */
public class ImageBrowseView extends EnhancedImageView {


    public ImageBrowseView(Context context) {
        super(context);
    }

    public ImageBrowseView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ImageBrowseView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void createFrescoView() {
        setMPhotoDraweeView(new PhotoDraweeView(getContext()));
        /**
         * 点击照片回调
         */
        ((PhotoDraweeView) getMPhotoDraweeView()).setOnPhotoTapListener(new OnPhotoTapListener() {
            @Override
            public void onPhotoTap(View view, float x, float y) {
                MyLog.d(getTAG(), "onPhotoTap" + " view=" + view + " x=" + x + " y=" + y);
                if (getMClickListener() != null) {
                    getMClickListener().onClick(view);
                }

            }
        });
        /**
         * 点击黑色部分回调
         */
        ((PhotoDraweeView) getMPhotoDraweeView()).setOnViewTapListener(new OnViewTapListener() {
            @Override
            public void onViewTap(View view, float x, float y) {
                MyLog.d(getTAG(), "onViewTap" + " view=" + view + " x=" + x + " y=" + y);
                if (getMClickListener() != null) {
                    getMClickListener().onClick(view);
                }
            }
        });
    }

    @Override
    protected boolean useSubSampleView() {
        return true;
    }

    @Override
    protected void realLoadByFresco(BaseImage baseImage) {
        ((PhotoDraweeView) getMPhotoDraweeView()).load(baseImage);
    }

    public boolean hasLargerScale() {
        PhotoDraweeView mPhotoDraweeView = ((PhotoDraweeView) getMPhotoDraweeView());
        if (mPhotoDraweeView != null && mPhotoDraweeView.getVisibility() == View.VISIBLE) {
            return mPhotoDraweeView.getScale()>1;
        }

        SubsamplingScaleImageView mSubsamplingScaleImageView = getMSubsamplingScaleImageView();
        if (mSubsamplingScaleImageView != null && mSubsamplingScaleImageView.getVisibility() == View.VISIBLE) {
            return mSubsamplingScaleImageView.getScale()>mSubsamplingScaleImageView.getMinScale();
        }

        return false;
    }
}
