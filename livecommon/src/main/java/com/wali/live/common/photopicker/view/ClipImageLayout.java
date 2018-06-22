package com.wali.live.common.photopicker.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import com.live.module.common.R;


/**
 * ClipImageLayout
 * Create by zhangyuehuan 2016/6/15
 */
public class ClipImageLayout extends RelativeLayout {
    private ClipZoomImageView mZoomImageView;
    private ClipImageBorderView mClipBorderView;

    public ClipImageLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        inflate(context, R.layout.clip_image_layout, this);
        mZoomImageView = (ClipZoomImageView) findViewById(R.id.clip_zoom_iv);
        mClipBorderView = (ClipImageBorderView) findViewById(R.id.clip_border_view);
    }

    public void hideBorderView() {
        mClipBorderView.setVisibility(GONE);
    }

    /*
    * 设置高度单位px
    * */
    public void setClipImageHeight(int height) {
        mClipBorderView.setClipHeight(height);
        mZoomImageView.setClipHeight(height);
    }

    public void setImageDrawable(Drawable drawable) {
        mZoomImageView.setImageDrawable(drawable);
    }

    public void setImageBitmap(Bitmap bitmap) {
        mZoomImageView.setImageBitmap(bitmap);
    }

    /**
     * 裁切图片
     *
     * @return
     */
    public Bitmap clip() {
        return mZoomImageView.clip();
    }
}
