package com.imagepicker.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.common.base.R;
import com.common.image.fresco.BaseImageView;
import com.common.utils.U;
import com.imagepicker.ResPicker;
import com.imagepicker.model.ResFolder;

import java.util.ArrayList;
import java.util.List;


/**
 * ================================================
 * 作    者：jeasonlzy（廖子尧 Github地址：https://github.com/jeasonlzy0216
 * 版    本：1.0
 * 创建日期：2016/5/19
 * 描    述：
 * 修订历史：
 * ================================================
 */
public class ImageFolderAdapter extends BaseAdapter {

    private ResPicker imagePicker;
    private Activity mActivity;
    private LayoutInflater mInflater;
    private int mImageSize;
    private List<ResFolder> mImageFolders = new ArrayList<>();
    private int lastSelected = 0;

    public ImageFolderAdapter(Activity activity) {
        mActivity = activity;

        imagePicker = ResPicker.getInstance();

        // 算出每个图片的大小
        int screenWidth = U.getDisplayUtils().getScreenWidth();
        float densityDpi = U.getDisplayUtils().getDensityDpi();
        int cols = (int) (screenWidth / densityDpi);
        cols = cols < 3 ? 3 : cols;
        int columnSpace = (int) (2 * activity.getResources().getDisplayMetrics().density); // 间距
        mImageSize = (screenWidth - columnSpace * (cols - 1)) / cols;


        mInflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void refreshData(List<ResFolder> folders) {
        if (folders != null && folders.size() > 0) {
            mImageFolders = folders;
        } else {
            mImageFolders.clear();
        }
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mImageFolders.size();
    }

    @Override
    public ResFolder getItem(int position) {
        return mImageFolders.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.adapter_folder_list_item, parent, false);
            holder = new ViewHolder(convertView);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        ResFolder folder = getItem(position);
        holder.folderName.setText(folder.getName());
        holder.imageCount.setText(mActivity.getString(R.string.ip_folder_image_count, folder.getImages().size()));
        imagePicker.getImageLoader().displayImage(mActivity, folder.getCover().getPath(), holder.cover, mImageSize, mImageSize);

        if (lastSelected == position) {
            holder.folderCheck.setVisibility(View.VISIBLE);
        } else {
            holder.folderCheck.setVisibility(View.INVISIBLE);
        }

        return convertView;
    }

    public void setSelectIndex(int i) {
        if (lastSelected == i) {
            return;
        }
        lastSelected = i;
        notifyDataSetChanged();
    }

    public int getSelectIndex() {
        return lastSelected;
    }

    private class ViewHolder {
        BaseImageView cover;
        TextView folderName;
        TextView imageCount;
        ImageView folderCheck;

        public ViewHolder(View view) {
            cover = (BaseImageView) view.findViewById(R.id.iv_cover);
            folderName = (TextView) view.findViewById(R.id.tv_folder_name);
            imageCount = (TextView) view.findViewById(R.id.tv_image_count);
            folderCheck = (ImageView) view.findViewById(R.id.iv_folder_check);
            view.setTag(this);
        }
    }
}
