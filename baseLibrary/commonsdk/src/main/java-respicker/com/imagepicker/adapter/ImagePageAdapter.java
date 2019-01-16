package com.imagepicker.adapter;

import android.app.Activity;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

import com.common.utils.U;
import com.imagebrowse.ImageBrowseView;
import com.imagepicker.ResPicker;
import com.imagepicker.model.ImageItem;
import com.imagepicker.model.ResItem;

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
    private ResPicker imagePicker;
    private ArrayList<ResItem> images = new ArrayList<>();
    private Activity mActivity;
    public PhotoViewClickListener listener;

    public ImagePageAdapter(Activity activity, ArrayList<ResItem> images) {
        this.mActivity = activity;
        this.images = images;

        screenWidth = U.getDisplayUtils().getScreenWidth();
        screenHeight = U.getDisplayUtils().getScreenHeight();
        imagePicker = ResPicker.getInstance();
    }

    public void setData(ArrayList<ResItem> images) {
        this.images = images;
    }

    public void setPhotoViewClickListener(PhotoViewClickListener listener) {
        this.listener = listener;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        ImageBrowseView imageBrowseView = new ImageBrowseView(mActivity);
        ResItem imageItem = images.get(position);
        imageBrowseView.load(imageItem.getPath());

//        photoView.setOnPhotoTapListener(new OnPhotoTapListener() {
//            @Override
//            public void onPhotoTap(View view, float x, float y) {
//                if (listener != null) {
//                    listener.OnPhotoTapListener(view, 0, 0);
//                }
//            }
//        });
        imageBrowseView.setViewLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                U.getToastUtil().showShort("长按事件");
                return false;
            }
        });
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
