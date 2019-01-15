package com.imagepicker;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;

import com.common.image.fresco.BaseImageView;
import com.common.image.fresco.FrescoWorker;
import com.common.image.model.ImageFactory;
import com.common.utils.U;
import com.imagepicker.loader.ImageLoader;
import com.imagepicker.model.ResFolder;
import com.imagepicker.model.ImageItem;
import com.imagepicker.view.CropImageView;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ImagePicker {
    public static final int REQUEST_CODE_TAKE = 1001; // 照相
    public static final int RESULT_CODE_ITEMS = 1004;

    public static final String EXTRA_SELECTED_IMAGE_POSITION = "selected_image_position";

    private ImageLoader imageLoader;     //图片加载器
    private File takeImageFile; // 拍照保存路径
    private File cropCacheFolder; // 裁剪保存路径

    private Params mParams = new Params(); // 配置的参数

    private boolean mIsOrigin = false; // 是否需要原图标记
    private ArrayList<ResFolder> mImageFolders = new ArrayList<>();      //所有的图片文件夹
    private ArrayList<ImageItem> mSelectedImages = new ArrayList<>();   //选中的图片集合
    private List<OnImageSelectedListener> mImageSelectedListeners = new ArrayList<>(); //图片选中的监听回调
    private int mCurrentImageFolderPosition = 0;  //当前选中的文件夹位置 0表示所有图片

    private static class ImagePickerHolder {
        private static final ImagePicker INSTANCE = new ImagePicker();
    }

    private ImagePicker() {
        imageLoader = new ImageLoader() {
            @Override
            public void displayImage(Activity activity, String path, BaseImageView imageView, int width, int height) {
                FrescoWorker.loadImage(imageView, ImageFactory.newLocalImage(path)
                        .setWidth(width)
                        .setHeight(height)
                        .build());
            }

            @Override
            public void displayImagePreview(Activity activity, String path, BaseImageView imageView, int width, int height) {
                FrescoWorker.loadImage(imageView, ImageFactory.newLocalImage(path)
                        .setWidth(width)
                        .setHeight(height)
                        .build());
            }

            @Override
            public void clearMemoryCache() {

            }
        };
    }

    public static final ImagePicker getInstance() {
        return ImagePickerHolder.INSTANCE;
    }

    public ImageLoader getImageLoader() {
        return imageLoader;
    }

    public void setCurrentImageFolderPosition(int position) {
        mCurrentImageFolderPosition = position;
    }

    public void setImageFolders(List<ResFolder> imageFolders) {
        if (imageFolders != null) {
            mImageFolders.clear();
            mImageFolders.addAll(imageFolders);
        }
    }

    public File getCropCacheFolder() {
        if (cropCacheFolder == null) {
            cropCacheFolder = new File(U.app().getCacheDir() + "/ImagePicker/cropTemp/");
        }
        return cropCacheFolder;
    }

    public File getTakeImageFile() {
        return takeImageFile;
    }

    public void setParams(Params p) {
        mParams = p;
    }

    public Params getParams() {
        return mParams;
    }

    public ArrayList<ImageItem> getSelectedImages() {
        return mSelectedImages;
    }


    public boolean isOrigin() {
        return mIsOrigin;
    }

    public void setOrigin(boolean isOrigin) {
        mIsOrigin = isOrigin;
    }

    public ArrayList<ResFolder> getImageFolders() {
        return mImageFolders;
    }

    public ArrayList<ImageItem> getCurrentImageFolderItems() {
        /**
         * 内存回收时 这里会空指针
         */
        if (mCurrentImageFolderPosition < mImageFolders.size()) {
            return mImageFolders.get(mCurrentImageFolderPosition).getImages();
        } else {
            return new ArrayList<>();
        }
    }

    /**
     * 拍照的方法
     */
    public void takePicture(Activity activity, int requestCode) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        takePictureIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        if (takePictureIntent.resolveActivity(activity.getPackageManager()) != null) {
            if (U.getDeviceUtils().existSDCard()) {
                takeImageFile = new File(Environment.getExternalStorageDirectory(), "/DCIM/camera/");
            } else {
                takeImageFile = Environment.getDataDirectory();
            }
            takeImageFile = U.getFileUtils().createFileByTs(takeImageFile, "IMG_", ".jpg");
            if (takeImageFile != null) {
                // 默认情况下，即不需要指定intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
                // 照相机有自己默认的存储路径，拍摄的照片将返回一个缩略图。如果想访问原始图片，
                // 可以通过dat extra能够得到原始图片位置。即，如果指定了目标uri，data就没有数据，
                // 如果没有指定uri，则data就返回有数据！

                Uri uri;
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.M) {
                    uri = Uri.fromFile(takeImageFile);
                } else {

                    /**
                     * 7.0 调用系统相机拍照不再允许使用Uri方式，应该替换为FileProvider
                     * 并且这样可以解决MIUI系统上拍照返回size为0的情况
                     */
                    uri = FileProvider.getUriForFile(activity, U.app().getPackageName() + ".provider", takeImageFile);
                    //加入uri权限 要不三星手机不能拍照
                    List<ResolveInfo> resInfoList = activity.getPackageManager().queryIntentActivities(takePictureIntent, PackageManager.MATCH_DEFAULT_ONLY);
                    for (ResolveInfo resolveInfo : resInfoList) {
                        String packageName = resolveInfo.activityInfo.packageName;
                        activity.grantUriPermission(packageName, uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    }
                }

                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
            }
        }
        activity.startActivityForResult(takePictureIntent, requestCode);
    }

    public void addSelectedImageItem(int position, ImageItem imageItem) {
        mSelectedImages.add(imageItem);
        for (OnImageSelectedListener l : mImageSelectedListeners) {
            l.onImageSelectedAdd(position, imageItem);
        }
    }

    public void removeSelectedImageItem(int position, ImageItem imageItem) {
        mSelectedImages.remove(imageItem);
        for (OnImageSelectedListener l : mImageSelectedListeners) {
            l.onImageSelectedRemove(position, imageItem);
        }
    }

    public void clearSelectedImages() {
        if (mSelectedImages != null) {
            mSelectedImages.clear();
        }
    }

    public void reset() {
        mImageSelectedListeners.clear();
        mImageFolders.clear();
        mSelectedImages.clear();
        mCurrentImageFolderPosition = 0;
    }


    /**
     * 图片选中的监听
     */
    public interface OnImageSelectedListener {
        void onImageSelectedAdd(int position, ImageItem item);

        void onImageSelectedRemove(int position, ImageItem item);
    }

    public void addOnImageSelectedListener(OnImageSelectedListener l) {
        mImageSelectedListeners.add(l);
    }

    public void removeOnImageSelectedListener(OnImageSelectedListener l) {
        if (mImageSelectedListeners == null) return;
        mImageSelectedListeners.remove(l);
    }


    //低内存时保存状态
    public void onSaveInstanceState(Bundle bunlde) {
        bunlde.putSerializable("params", mParams);
        bunlde.putSerializable("takeImageFile", takeImageFile);
        bunlde.putSerializable("cropCacheFolder", cropCacheFolder);
        bunlde.putSerializable("mSelectedImages", mSelectedImages);
    }

    public void onRestoreInstanceState(Bundle bunlde) {
        Params params = (Params) bunlde.getSerializable("params");
        if (params != null) {
            mParams = params;
        }
        takeImageFile = (File) bunlde.getSerializable("takeImageFile");
        cropCacheFolder = (File) bunlde.getSerializable("cropCacheFolder");
        List<ImageItem> selectedImages = (ArrayList<ImageItem>) bunlde.getSerializable("mSelectedImages");
        if (selectedImages != null && !selectedImages.isEmpty()) {
            mSelectedImages.clear();
            mSelectedImages.addAll(selectedImages);
        }
    }

    public static Params.Builder newParamsBuilder() {
        return new Params.Builder();
    }

    /**
     * 可由调用方配置的参数
     */
    public static class Params implements Serializable {
        private boolean showCamera = true;   //显示相机
        private boolean multiMode = true;    //图片选择模式
        private int selectLimit = 9;         //多选时最大选择图片数量
        private boolean crop = true;         //裁剪
        private int outPutX = 800;           //裁剪保存宽度
        private int outPutY = 800;           //裁剪保存高度
        private int focusWidth = U.getDisplayUtils().dip2px(280);         //焦点框的宽度
        private int focusHeight = U.getDisplayUtils().dip2px(280);        //焦点框的高度
        private boolean isSaveRectangle = true;  //裁剪后的图片是否是矩形，否者跟随裁剪框的形状
        private CropImageView.Style cropStyle = CropImageView.Style.RECTANGLE; //裁剪框的形状
        private boolean includeGif = false;  //图片选取时，是否包括gif，默认不包括

        private Params() {

        }

        public boolean isShowCamera() {
            return showCamera;
        }

        public void setShowCamera(boolean showCamera) {
            this.showCamera = showCamera;
        }

        public boolean isMultiMode() {
            return multiMode;
        }

        public void setMultiMode(boolean multiMode) {
            this.multiMode = multiMode;
        }

        public int getSelectLimit() {
            return selectLimit;
        }

        public void setSelectLimit(int selectLimit) {
            this.selectLimit = selectLimit;
        }

        public boolean isCrop() {
            return crop;
        }

        public void setCrop(boolean crop) {
            this.crop = crop;
        }

        public int getOutPutX() {
            return outPutX;
        }

        public void setOutPutX(int outPutX) {
            this.outPutX = outPutX;
        }

        public int getOutPutY() {
            return outPutY;
        }

        public void setOutPutY(int outPutY) {
            this.outPutY = outPutY;
        }

        public int getFocusWidth() {
            return focusWidth;
        }

        public void setFocusWidth(int focusWidth) {
            this.focusWidth = focusWidth;
        }

        public int getFocusHeight() {
            return focusHeight;
        }

        public void setFocusHeight(int focusHeight) {
            this.focusHeight = focusHeight;
        }

        public boolean isSaveRectangle() {
            return isSaveRectangle;
        }

        public void setSaveRectangle(boolean saveRectangle) {
            isSaveRectangle = saveRectangle;
        }

        public CropImageView.Style getCropStyle() {
            return cropStyle;
        }

        public void setCropStyle(CropImageView.Style cropStyle) {
            this.cropStyle = cropStyle;
        }

        public boolean isIncludeGif() {
            return includeGif;
        }

        public void setIncludeGif(boolean includeGif) {
            this.includeGif = includeGif;
        }

        public static class Builder {
            Params mParams = new Params();

            Builder() {
            }

            public Builder setShowCamera(boolean showCamera) {
                mParams.setShowCamera(showCamera);
                return this;
            }

            public Builder setMultiMode(boolean multiMode) {
                mParams.setMultiMode(multiMode);
                return this;
            }

            public Builder setSelectLimit(int selectLimit) {
                mParams.setSelectLimit(selectLimit);
                return this;
            }

            public Builder setCrop(boolean crop) {
                mParams.setCrop(crop);
                return this;
            }

            public Builder setOutPutX(int outPutX) {
                mParams.setOutPutX(outPutX);
                return this;
            }

            public Builder setOutPutY(int outPutY) {
                mParams.setOutPutY(outPutY);
                return this;
            }

            public Builder setFocusWidth(int focusWidth) {
                mParams.setFocusWidth(focusWidth);
                return this;
            }

            public Builder setFocusHeight(int focusHeight) {
                mParams.setFocusHeight(focusHeight);
                return this;
            }

            public Builder setIsSaveRectangle(boolean isSaveRectangle) {
                mParams.setSaveRectangle(isSaveRectangle);
                return this;
            }

            public Builder setCropStyle(CropImageView.Style cropStyle) {
                mParams.setCropStyle(cropStyle);
                return this;
            }

            public Builder setIncludeGif(boolean includeGif) {
                mParams.setIncludeGif(includeGif);
                return this;
            }

            public Params build() {
                return mParams;
            }
        }
    }

}
