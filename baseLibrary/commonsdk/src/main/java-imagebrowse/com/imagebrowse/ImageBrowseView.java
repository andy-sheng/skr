package com.imagebrowse;

import android.content.Context;
import android.util.AttributeSet;

import com.common.image.model.BaseImage;
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
    }

    @Override
    protected boolean useSubSampleView(){
        return true;
    }

    @Override
    protected void realLoadByFresco(BaseImage baseImage){
        ((PhotoDraweeView)mPhotoDraweeView).load(baseImage);
    }

}
