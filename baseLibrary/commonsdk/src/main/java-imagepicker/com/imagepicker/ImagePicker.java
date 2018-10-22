package com.imagepicker;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;

import com.common.image.fresco.BaseImageView;
import com.common.image.fresco.FrescoWorker;
import com.common.image.fresco.IFrescoCallBack;
import com.common.image.model.ImageFactory;
import com.common.utils.U;
import com.facebook.imagepipeline.image.ImageInfo;
import com.imagepicker.loader.ImageLoader;
import com.imagepicker.model.ImageFolder;
import com.imagepicker.model.ImageItem;
import com.imagepicker.view.CropImageView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ImagePicker {
    public static final int REQUEST_CODE_TAKE = 1001; // 照相
    public static final int REQUEST_CODE_CROP = 1002; // 裁剪
    public static final int RESULT_CODE_ITEMS = 1004;

    public static final String EXTRA_RESULT_ITEMS = "extra_result_items";
    public static final String EXTRA_IMAGE_ITEMS = "extra_image_items";

    private ImageLoader imageLoader;     //图片加载器
    private File takeImageFile; // 拍照保存路径
    private File cropCacheFolder; // 裁剪保存路径
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

    private List<ImageFolder> mImageFolders = new ArrayList<>();      //所有的图片文件夹
    private ArrayList<ImageItem> mSelectedImages = new ArrayList<>();   //选中的图片集合
    private List<OnImageSelectedListener> mImageSelectedListeners = new ArrayList<>();          // 图片选中的监听回调
    private int mCurrentImageFolderPosition = 0;  //当前选中的文件夹位置 0表示所有图片

    private static class ImagePickerHolder {
        private static final ImagePicker INSTANCE = new ImagePicker();
    }

    private ImagePicker() {
        imageLoader = new ImageLoader() {
            @Override
            public void displayImage(Activity activity, String path, BaseImageView imageView, int width, int height) {
                FrescoWorker.loadImage(imageView,ImageFactory.newLocalImage(path)
                        .setWidth(width)
                        .setHeight(height)
                        .build());
            }

            @Override
            public void displayImagePreview(Activity activity, String path, BaseImageView imageView, int width, int height) {
                FrescoWorker.loadImage(imageView,ImageFactory.newLocalImage(path)
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

    public void setSelectedImages(ArrayList<ImageItem> images) {
        if (images != null) {
            mSelectedImages.clear();
            mSelectedImages.addAll(images);
        }
    }

    public void setImageFolders(List<ImageFolder> imageFolders) {
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

    public boolean isShowCamera() {
        return showCamera;
    }

    public boolean isCrop() {
        return crop;
    }

    public int getOutPutX() {
        return outPutX;
    }

    public int getOutPutY() {
        return outPutY;
    }

    public int getFocusWidth() {
        return focusWidth;
    }

    public int getFocusHeight() {
        return focusHeight;
    }

    public boolean isSaveRectangle() {
        return isSaveRectangle;
    }

    public CropImageView.Style getCropStyle() {
        return cropStyle;
    }

    public ArrayList<ImageItem> getSelectedImages() {
        return mSelectedImages;
    }

    public boolean isMultiMode() {
        return multiMode;
    }

    public int getSelectLimit() {
        return selectLimit;
    }

    public ArrayList<ImageItem> getCurrentImageFolderItems() {
        return mImageFolders.get(mCurrentImageFolderPosition).getImages();
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

}
