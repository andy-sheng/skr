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
        mPhotoDraweeView = new PhotoDraweeView(getContext());
        /**
         * 点击照片回调
         */
        ((PhotoDraweeView) mPhotoDraweeView).setOnPhotoTapListener(new OnPhotoTapListener() {
            @Override
            public void onPhotoTap(View view, float x, float y) {
                MyLog.d(TAG, "onPhotoTap" + " view=" + view + " x=" + x + " y=" + y);
                if (mClickListener != null) {
                    mClickListener.onClick(view);
                }

            }
        });
        /**
         * 点击黑色部分回调
         */
        ((PhotoDraweeView) mPhotoDraweeView).setOnViewTapListener(new OnViewTapListener() {
            @Override
            public void onViewTap(View view, float x, float y) {
                MyLog.d(TAG, "onViewTap" + " view=" + view + " x=" + x + " y=" + y);
                if (mClickListener != null) {
                    mClickListener.onClick(view);
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
        ((PhotoDraweeView) mPhotoDraweeView).load(baseImage);
    }
}
