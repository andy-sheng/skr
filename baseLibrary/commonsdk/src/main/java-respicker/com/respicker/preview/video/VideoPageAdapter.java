package com.respicker.preview.video;

import android.app.Activity;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

import com.common.utils.U;
import com.imagebrowse.ImageBrowseView;
import com.respicker.ResPicker;
import com.respicker.model.ImageItem;
import com.respicker.model.ResItem;
import com.respicker.model.VideoItem;

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
public class VideoPageAdapter extends PagerAdapter {

    private int screenWidth;
    private int screenHeight;
    private ResPicker imagePicker;
    private ArrayList<VideoItem> videos = new ArrayList<>();
    private Activity mActivity;

    public VideoPageAdapter(Activity activity, ArrayList<VideoItem> images) {
        this.mActivity = activity;
        this.videos = images;

        screenWidth = U.getDisplayUtils().getScreenWidth();
        screenHeight = U.getDisplayUtils().getScreenHeight();
        imagePicker = ResPicker.getInstance();
    }

    public void setData(ArrayList<VideoItem> images) {
        this.videos = images;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        VideoPreviewView imageBrowseView = new VideoPreviewView(mActivity);
        VideoItem videoItem = videos.get(position);
        imageBrowseView.bind(videoItem);

        container.addView(imageBrowseView);
        return imageBrowseView;
    }

    @Override
    public int getCount() {
        return videos.size();
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

}
