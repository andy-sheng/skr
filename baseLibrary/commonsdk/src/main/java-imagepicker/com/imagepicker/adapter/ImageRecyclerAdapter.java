package com.imagepicker.adapter;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.Toast;

import com.common.base.R;
import com.common.image.model.BaseImage;
import com.common.image.model.ImageFactory;
import com.common.utils.PermissionUtils;
import com.common.utils.U;
import com.imagebrowse.EnhancedImageView;
import com.imagepicker.ImagePicker;
import com.imagepicker.model.ImageItem;
import com.imagepicker.view.SuperCheckBox;

import java.util.ArrayList;
import java.util.List;

/**
 * 加载相册图片的RecyclerView适配器
 * <p>
 * 用于替换原项目的GridView，使用局部刷新解决选中照片出现闪动问题
 * <p>
 * 替换为RecyclerView后只是不再会导致全局刷新，
 * <p>
 * 但还是会出现明显的重新加载图片，可能是picasso图片加载框架的问题
 * <p>
 * Author: nanchen
 * Email: liushilin520@foxmail.com
 * Date: 2017-04-05  10:04
 */

public class ImageRecyclerAdapter extends RecyclerView.Adapter<ViewHolder> {


    private static final int ITEM_TYPE_CAMERA = 0;  //第一个条目是相机
    private static final int ITEM_TYPE_NORMAL = 1;  //第一个条目不是相机
    private ImagePicker mImagePicker;
    private Activity mActivity;
    private ArrayList<ImageItem> images = new ArrayList<>();       //当前需要显示的所有的图片数据
    private ArrayList<ImageItem> mSelectedImages; //全局保存的已经选中的图片数据
    private boolean isShowCamera;         //是否显示拍照按钮
    private int mImageSize;               //每个条目的大小
    private LayoutInflater mInflater;
    private OnImageItemClickListener listener;   //图片被点击的监听

    /**
     * 构造方法
     */
    public ImageRecyclerAdapter(Activity activity) {
        this.mActivity = activity;

        // 算出每个图片的大小
        int screenWidth = U.getDisplayUtils().getScreenWidth();
        float densityDpi = U.getDisplayUtils().getDensityDpi();
        int cols = (int) (screenWidth / densityDpi);
        cols = cols < 3 ? 3 : cols;
        int columnSpace = (int) (2 * activity.getResources().getDisplayMetrics().density); // 间距
        mImageSize = (screenWidth - columnSpace * (cols - 1)) / cols;

        mImagePicker = ImagePicker.getInstance();
        isShowCamera = mImagePicker.getParams().isShowCamera();
        mSelectedImages = mImagePicker.getSelectedImages();
        mInflater = LayoutInflater.from(activity);
    }

    public void setOnImageItemClickListener(OnImageItemClickListener listener) {
        this.listener = listener;
    }

    public void refreshData(ArrayList<ImageItem> images) {
        if (images == null || images.size() == 0) {
            this.images = new ArrayList<>();
        } else {
            this.images = images;
        }
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == ITEM_TYPE_CAMERA) {
            return new CameraViewHolder(mInflater.inflate(R.layout.adapter_camera_item, parent, false));
        }
        return new ImageViewHolder(mInflater.inflate(R.layout.adapter_image_list_item, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if (holder instanceof CameraViewHolder) {
            ((CameraViewHolder) holder).bindCamera();
        } else if (holder instanceof ImageViewHolder) {
            ((ImageViewHolder) holder).bind(position);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (isShowCamera) {
            return position == 0 ? ITEM_TYPE_CAMERA : ITEM_TYPE_NORMAL;
        }
        return ITEM_TYPE_NORMAL;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return isShowCamera ? images.size() + 1 : images.size();
    }

    public ImageItem getItem(int position) {
        if (isShowCamera) {
            if (position == 0) return null;
            return images.get(position - 1);
        } else {
            return images.get(position);
        }
    }

    private class ImageViewHolder extends ViewHolder {

        View rootView;
        EnhancedImageView ivThumb;
        View mask;
        View checkView;
        SuperCheckBox cbCheck;
        ImageItem imageItem;
        int position;

        ImageViewHolder(View itemView) {
            super(itemView);
            rootView = itemView;
            ivThumb = (EnhancedImageView) itemView.findViewById(R.id.iv_thumb);
            mask = itemView.findViewById(R.id.mask);
            checkView = itemView.findViewById(R.id.checkView);
            cbCheck = (SuperCheckBox) itemView.findViewById(R.id.cb_check);
            itemView.setLayoutParams(new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, mImageSize)); //让图片是个正方形

            ivThumb.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        listener.onImageItemClick(rootView, imageItem, position);
                    }
                }
            });

            checkView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    cbCheck.setChecked(!cbCheck.isChecked());
                    int selectLimit = mImagePicker.getParams().getSelectLimit();
                    if (cbCheck.isChecked() && mSelectedImages.size() >= selectLimit) {
                        Toast.makeText(mActivity.getApplicationContext(), mActivity.getString(R.string.ip_select_limit, selectLimit), Toast.LENGTH_SHORT).show();
                        cbCheck.setChecked(false);
                        mask.setVisibility(View.GONE);
                    } else {
                        if (cbCheck.isChecked()) {
                            mImagePicker.addSelectedImageItem(position, imageItem);
                        }else{
                            mImagePicker.removeSelectedImageItem(position, imageItem);
                        }
                        mask.setVisibility(View.VISIBLE);
                    }
                }
            });

        }

        void bind(final int position) {
            this.position = position;
            this.imageItem = getItem(position);

            //根据是否多选，显示或隐藏checkbox
            if (mImagePicker.getParams().isMultiMode()) {
                cbCheck.setVisibility(View.VISIBLE);
                boolean checked = mSelectedImages.contains(imageItem);
                if (checked) {
                    mask.setVisibility(View.VISIBLE);
                    cbCheck.setChecked(true);
                } else {
                    mask.setVisibility(View.GONE);
                    cbCheck.setChecked(false);
                }
            } else {
                cbCheck.setVisibility(View.GONE);
            }
            BaseImage baseImage = ImageFactory.newLocalImage(imageItem.getPath())
                    .setWidth(300)
                    .setHeight(300)
            .build();
            ivThumb.load(baseImage);
//            mImagePicker.getImageLoader().displayImage(mActivity,, ivThumb, mImageSize, mImageSize); //显示图片
        }

    }

    private class CameraViewHolder extends ViewHolder {

        View mItemView;

        CameraViewHolder(View itemView) {
            super(itemView);
            mItemView = itemView;
        }

        void bindCamera() {
            mItemView.setLayoutParams(new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, mImageSize)); //让图片是个正方形
            mItemView.setTag(null);
            mItemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!U.getPermissionUtils().checkCamera(mActivity)) {
                        U.getPermissionUtils().requestCamera(new PermissionUtils.RequestPermission() {
                            @Override
                            public void onRequestPermissionSuccess() {
                                mImagePicker.takePicture(mActivity, ImagePicker.REQUEST_CODE_TAKE);
                            }

                            @Override
                            public void onRequestPermissionFailure(List<String> permissions) {

                            }

                            @Override
                            public void onRequestPermissionFailureWithAskNeverAgain(List<String> permissions) {

                            }
                        }, mActivity);
                    } else {
                        mImagePicker.takePicture(mActivity, ImagePicker.REQUEST_CODE_TAKE);
                    }
                }
            });
        }
    }

    public interface OnImageItemClickListener {
        void onImageItemClick(View view, ImageItem imageItem, int position);
    }
}
