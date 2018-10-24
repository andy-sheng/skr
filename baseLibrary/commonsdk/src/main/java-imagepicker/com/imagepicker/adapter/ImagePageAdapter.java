package com.imagepicker.adapter;

import android.app.Activity;
import android.net.Uri;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

import com.common.image.model.ImageFactory;
import com.common.utils.U;
import com.common.view.photodraweeview.OnPhotoTapListener;
import com.common.view.photodraweeview.PhotoDraweeView;
import com.imagebrowse.ImageBrowseView;
import com.imagepicker.ImagePicker;
import com.imagepicker.model.ImageItem;

import java.io.File;
import java.util.ArrayList;


/**
 * ================================================
 * 作    者：jeasonlzy（廖子尧 Github地址：https://github.com/jeasonlzy0216
 * 版    本：1.0
 * 创建日期：2016/5/19
 * 描    述：
 * 修订历史：
 * ================================================
 */
public class ImagePageAdapter extends PagerAdapter {

    private int screenWidth;
    private int screenHeight;
    private ImagePicker imagePicker;
    private ArrayList<ImageItem> images = new ArrayList<>();
    private Activity mActivity;
    public PhotoViewClickListener listener;

    public ImagePageAdapter(Activity activity, ArrayList<ImageItem> images) {
        this.mActivity = activity;
        this.images = images;

        screenWidth = U.getDisplayUtils().getScreenWidth();
        screenHeight = U.getDisplayUtils().getScreenHeight();
        imagePicker = ImagePicker.getInstance();
    }

    public void setData(ArrayList<ImageItem> images) {
        this.images = images;
    }

    public void setPhotoViewClickListener(PhotoViewClickListener listener) {
        this.listener = listener;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        ImageBrowseView imageBrowseView = new ImageBrowseView(mActivity);
        ImageItem imageItem = images.get(position);

        imageBrowseView.load(imageItem.getPath());

//        photoView.setOnPhotoTapListener(new OnPhotoTapListener() {
//            @Override
//            public void onPhotoTap(View view, float x, float y) {
//                if (listener != null) {
//                    listener.OnPhotoTapListener(view, 0, 0);
//                }
//            }
//        });
        container.addView(imageBrowseView);
        return imageBrowseView;
    }

    @Override
    public int getCount() {
        return images.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }

    public interface PhotoViewClickListener {
        void OnPhotoTapListener(View view, float v, float v1);
    }
}
