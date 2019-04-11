package com.respicker;

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
import com.respicker.loader.ImageLoader;
import com.respicker.model.ImageItem;
import com.respicker.model.ResFolder;
import com.respicker.model.ResItem;
import com.respicker.model.VideoItem;
import com.respicker.view.CropImageView;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ResPicker {
    public static final int REQUEST_CODE_TAKE = 1001; // 照相
    public static final int RESULT_CODE_ITEMS = 1004;


    private ImageLoader mImageLoader;     //图片加载器
    private File takeImageFile; // 拍照保存路径
    private File mCropCacheFolder; // 裁剪保存路径

    private Params mParams = new Params(); // 配置的参数

    private boolean mIsOrigin = false; // 是否需要原图标记
    private ArrayList<ResFolder> mResFolders = new ArrayList<>();      //所有的资源文件夹
    private ArrayList<ResItem> mSelectedResList = new ArrayList<>();   //选中的资源集合
    private List<OnResSelectedListener> mResSelectedListeners = new ArrayList<>(); //资源选中的监听回调
    private int mCurrentResFolderPosition = 0;  //当前选中的文件夹位置 0表示所有图片

    private static class ResPickerHolder {
        private static final ResPicker INSTANCE = new ResPicker();
    }

    private ResPicker() {
        mImageLoader = new ImageLoader() {
            @Override
            public void displayImage(Activity activity, String path, BaseImageView imageView, int width, int height) {
                FrescoWorker.loadImage(imageView, ImageFactory.newPathImage(path)
                        .setWidth(width)
                        .setHeight(height)
                        .build());
            }

            @Override
            public void displayImagePreview(Activity activity, String path, BaseImageView imageView, int width, int height) {
                FrescoWorker.loadImage(imageView, ImageFactory.newPathImage(path)
                        .setWidth(width)
                        .setHeight(height)
                        .build());
            }

            @Override
            public void clearMemoryCache() {

            }
        };
    }

    public static final ResPicker getInstance() {
        return ResPickerHolder.INSTANCE;
    }

    public ImageLoader getImageLoader() {
        return mImageLoader;
    }

    public void setCurrentResFolderPosition(int position) {
        mCurrentResFolderPosition = position;
    }

    public void setResFolders(List<ResFolder> imageFolders) {
        if (imageFolders != null) {
            mResFolders.clear();
            mResFolders.addAll(imageFolders);
        }
    }

    public File getCropCacheFolder() {
        if (mCropCacheFolder == null) {
            mCropCacheFolder = new File(U.app().getCacheDir() + "/ImagePicker/cropTemp/");
        }
        return mCropCacheFolder;
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


    /**当前已选择的文件夹开始 begin**/
    public ArrayList<ResItem> getSelectedResList() {
        return mSelectedResList;
    }

    public ArrayList<ImageItem> getSelectedImageList() {
        ArrayList<ImageItem> imageItemList = new ArrayList<>();
        for (ResItem resItem : mSelectedResList) {
            if (resItem instanceof ImageItem) {
                imageItemList.add((ImageItem) resItem);
            }
        }
        return imageItemList;
    }

    public ImageItem getSingleSelectedImage() {
        return (ImageItem) getSelectedImageList().get(0);
    }


    public ArrayList<VideoItem> getSelectedVideoList() {
        ArrayList<VideoItem> itemList = new ArrayList<>();
        for (ResItem resItem : mSelectedResList) {
            if (resItem instanceof VideoItem) {
                itemList.add((VideoItem) resItem);
            }
        }
        return itemList;
    }

    public VideoItem getSingleSelectedVideo() {
        return (VideoItem) getSelectedVideoList().get(0);
    }

    /**当前已选择的文件夹开始 end**/

    public boolean isOrigin() {
        return mIsOrigin;
    }

    public void setOrigin(boolean isOrigin) {
        mIsOrigin = isOrigin;
    }

    public ArrayList<ResFolder> getResFolders() {
        return mResFolders;
    }


    /**当前可选择的文件 begin**/
    public ArrayList<ResItem> getCurrentResFolderItems() {
        /**
         * 内存回收时 这里会空指针
         */
        if (mCurrentResFolderPosition < mResFolders.size()) {
            return mResFolders.get(mCurrentResFolderPosition).getResItems();
        } else {
            return new ArrayList<>();
        }
    }

    public ArrayList<ImageItem> getCurrentResFolderImageItems() {
        if (mCurrentResFolderPosition < mResFolders.size()) {
            return mResFolders.get(mCurrentResFolderPosition).getImageItems();
        } else {
            return new ArrayList<>();
        }
    }

    public ArrayList<VideoItem> getCurrentResFolderVideoItems() {
        if (mCurrentResFolderPosition < mResFolders.size()) {
            return mResFolders.get(mCurrentResFolderPosition).getVideoItems();
        } else {
            return new ArrayList<>();
        }
    }
    /**当前可选择的文件 end**/

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

    public void addSelectedResItem(int position, ResItem imageItem) {
        mSelectedResList.add(imageItem);
        for (OnResSelectedListener l : mResSelectedListeners) {
            l.onResSelectedAdd(position, imageItem);
        }
    }

    public void removeSelectedResItem(int position, ResItem imageItem) {
        mSelectedResList.remove(imageItem);
        for (OnResSelectedListener l : mResSelectedListeners) {
            l.onResSelectedRemove(position, imageItem);
        }
    }

    public void clearSelectedRes() {
        if (mSelectedResList != null) {
            mSelectedResList.clear();
        }
    }

    public void reset() {
        mResSelectedListeners.clear();
        mResFolders.clear();
        mSelectedResList.clear();
        mCurrentResFolderPosition = 0;
    }


    /**
     * 图片选中的监听
     */
    public interface OnResSelectedListener {
        void onResSelectedAdd(int position, ResItem item);

        void onResSelectedRemove(int position, ResItem item);
    }

    public void addOnResSelectedListener(OnResSelectedListener l) {
        mResSelectedListeners.add(l);
    }

    public void removeOnResSelectedListener(OnResSelectedListener l) {
        if (mResSelectedListeners == null) {
            return;
        }
        mResSelectedListeners.remove(l);
    }

    //低内存时保存状态
    public void onSaveInstanceState(Bundle bunlde) {
        bunlde.putSerializable("params", mParams);
        bunlde.putSerializable("takeImageFile", takeImageFile);
        bunlde.putSerializable("cropCacheFolder", mCropCacheFolder);
        bunlde.putSerializable("mSelectedImages", mSelectedResList);
    }

    public void onRestoreInstanceState(Bundle bunlde) {
        Params params = (Params) bunlde.getSerializable("params");
        if (params != null) {
            mParams = params;
        }
        takeImageFile = (File) bunlde.getSerializable("takeImageFile");
        mCropCacheFolder = (File) bunlde.getSerializable("cropCacheFolder");
        List<ResItem> selectedImages = (ArrayList<ResItem>) bunlde.getSerializable("mSelectedImages");
        if (selectedImages != null && !selectedImages.isEmpty()) {
            mSelectedResList.clear();
            mSelectedResList.addAll(selectedImages);
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
        private boolean includeImage = true;  //是否选取图片
        private boolean includeGif = false;  //图片选取时，是否包括gif，webp等，默认不包括
        private boolean includeVideo = false;  //是否包括视频

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

        public boolean isIncludeImage() {
            return includeImage;
        }

        public void setIncludeImage(boolean includeImage) {
            this.includeImage = includeImage;
        }

        public boolean isIncludeVideo() {
            return includeVideo;
        }

        public void setIncludeVideo(boolean includeVideo) {
            this.includeVideo = includeVideo;
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

            public Builder setIncludeImage(boolean includeImage) {
                mParams.setIncludeImage(includeImage);
                return this;
            }

            public Builder setIncludeVideo(boolean includeVideo) {
                mParams.setIncludeVideo(includeVideo);
                return this;
            }

            public Params build() {
                return mParams;
            }
        }
    }

}
