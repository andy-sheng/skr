package com.imagebrowse;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.common.base.R;
import com.common.image.fresco.FrescoWorker;
import com.common.image.fresco.IFrescoCallBack;
import com.common.image.model.BaseImage;
import com.common.image.model.HttpImage;
import com.common.image.model.ImageFactory;
import com.common.image.model.LocalImage;
import com.common.log.MyLog;
import com.common.utils.DisplayUtils;
import com.common.utils.HttpUtils;
import com.common.utils.ImageUtils;
import com.common.utils.U;
import com.common.view.photodraweeview.PhotoDraweeView;
import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.facebook.imagepipeline.image.ImageInfo;

import java.io.File;
import java.io.IOException;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.schedulers.Schedulers;
import pl.droidsonroids.gif.GifDrawable;
import pl.droidsonroids.gif.GifImageView;

/**
 * 这个view过于复杂，在使用viewpager时就别重复使用了吧
 * 职责：保证清晰流畅的显示出图片 不管多大
 */
public class ImageBrowseView extends ExImageView {


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
