package com.wali.live.common.photopicker;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;

import com.base.activity.BaseSdkActivity;
import com.base.fragment.BaseFragment;
import com.base.log.MyLog;
import com.base.permission.PermissionUtils;
import com.base.utils.CommonUtils;
import com.base.utils.IOUtils;
import com.live.module.common.R;
import com.trello.rxlifecycle.ActivityEvent;
import com.wali.live.common.photopicker.view.ClipImageLayout;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Create by zhangyuehuan 2016/6/15
 *
 * @module 裁剪图片的Activity
 */
public class ClipImageActivity extends BaseSdkActivity implements OnClickListener {
    private static final String TAG = ClipImageActivity.class.getSimpleName();

    public static final String IMAGE_PATH = "image_path";//原图路径key
    public static final String SAVE_CLIP_IMAGE_PATH = "save_clip_image_path";  //保存裁剪的路径key
    public static final String CROP_IMAGE_HEIGHT = "crop_image_height";
    public static final String RETURN_DATA = "return_data";
    public static final String RETURN_DATA_AS_BITMAP = "data";
    public static final String ACTION_INLINE_DATA = "inline_data";
    public static final int WIDTH = 1080;
    public static final int HEIGHT = 1920;

    public static final int REQUEST_CODE_CROP = 1002;//处理从裁剪图片返回

    private Bitmap.CompressFormat mOutputFormat = Bitmap.CompressFormat.JPEG;
    private Uri mSaveUri = null;
    private ContentResolver mContentResolver;
    String mImagePath;

    private ClipImageLayout mClipImageLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.crop_image_layout);

        mContentResolver = getContentResolver();
        //TODO 暂时拿掉存储卡的检查 zyh
//        CommonUtils.showStorageToast();
        mClipImageLayout = (ClipImageLayout) findViewById(R.id.clipImageLayout);

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            int cropImageHeight = extras.getInt(CROP_IMAGE_HEIGHT, 0);
            mImagePath = extras.getString(IMAGE_PATH);
            Uri imageUri = intent.getData();
            if (TextUtils.isEmpty(mImagePath) && null != imageUri) {
                mImagePath = imageUri.getPath();
            }
            mSaveUri = extras.getParcelable(MediaStore.EXTRA_OUTPUT);
            // 有的系统返回的图片是旋转了，有的没有旋转，所以处理
            int degree = readBitmapDegree(mImagePath);
            Bitmap bitmap = CommonUtils.extractThumbNail(mImagePath, HEIGHT, WIDTH, false);
            if (bitmap != null) {
                MyLog.w(TAG, " bitmap height =" + bitmap.getHeight() + "width =" + bitmap.getWidth());
                if (degree == 0) {
                    mClipImageLayout.setImageBitmap(bitmap);
                } else {
                    mClipImageLayout.setImageBitmap(rotateBitmap(degree, bitmap));
                }
            } else {
                finish();
            }
            if (cropImageHeight > 0) {
                mClipImageLayout.setClipImageHeight(cropImageHeight);//动态设置截取框的高度
            }
        }
        findViewById(R.id.ok_btn).setOnClickListener(this);
        findViewById(R.id.cancel_btn).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.ok_btn) {
            Bitmap bitmap = mClipImageLayout.clip();
            saveClick(bitmap);
        } else if (i == R.id.cancel_btn) {
            finish();
        } else {
        }
    }

    private void saveClick(Bitmap croppedImage) {
        Bundle myExtras = getIntent().getExtras();
        if (myExtras != null && (myExtras.getParcelable("data") != null || myExtras.getBoolean(RETURN_DATA))) {
            Bundle extras = new Bundle();
            extras.putParcelable(RETURN_DATA_AS_BITMAP, croppedImage);
            setResult(RESULT_OK, (new Intent()).setAction(ACTION_INLINE_DATA).putExtras(extras));
            finish();
        } else {
            saveOutput(croppedImage);
        }
    }

    private void saveOutput(Bitmap croppedImage) {
        showProgress(R.string.saving_image);
        Observable.just(croppedImage).map(new Func1<Bitmap, Boolean>() {
            @Override
            public Boolean call(Bitmap bitmap) {
                if (mSaveUri != null && bitmap != null) {
                    OutputStream outputStream = null;
                    try {
                        outputStream = mContentResolver.openOutputStream(mSaveUri);
                        if (outputStream != null) {
                            bitmap.compress(mOutputFormat, 90, outputStream);
                        }
                    } catch (IOException ex) {
                        MyLog.e(TAG, "Cannot open file: " + mSaveUri + ex);
                        setResult(RESULT_CANCELED);
                        finish();
                        return false;
                    } finally {
                        IOUtils.closeQuietly(outputStream);
                    }
                    Bundle extras = new Bundle();
                    Intent intent = new Intent(mSaveUri.toString());
                    intent.putExtras(extras);
                    intent.putExtra(IMAGE_PATH, mImagePath);
                    setResult(RESULT_OK, intent);
                } else {
                    MyLog.e(TAG, "not defined image url");
                }
                if (null != bitmap) {
                    bitmap.recycle();
                }
                finish();
                return true;
            }
        }).subscribeOn(Schedulers.io())
                .compose(this.<Boolean>bindUntilEvent(ActivityEvent.DESTROY))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Boolean>() {
                    @Override
                    public void call(Boolean o) {
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        MyLog.w(TAG, "saveOutput failed=" + throwable);
                    }
                });
    }

    // 读取图像的旋转度
    private int readBitmapDegree(String path) {
        int degree = 0;
        try {
            ExifInterface exifInterface = new ExifInterface(path);
            int orientation = exifInterface.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    degree = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    degree = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    degree = 270;
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return degree;
    }

    static public void openActivity(Activity baseAppActivity, Uri uriDataAndType, File extraOutputFile) {
        if (baseAppActivity == null || uriDataAndType == null || extraOutputFile == null) {
            return;
        }
        //启动裁剪activity
        if (PermissionUtils.checkSdcardAlertWindow(baseAppActivity)) {
            final Intent cropIntent = new Intent(baseAppActivity, ClipImageActivity.class);
            cropIntent.setDataAndType(uriDataAndType, "image/*");
            cropIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(extraOutputFile));
            baseAppActivity.startActivityForResult(cropIntent, ClipImageActivity.REQUEST_CODE_CROP);
        } else {
            PermissionUtils.requestPermissionDialog(baseAppActivity, PermissionUtils.PermissionType.WRITE_EXTERNAL_STORAGE);
        }
    }

    static public void openActivity(BaseFragment fragment, Uri uriDataAndType, File extraOutputFile) {
        if (fragment == null || fragment.getActivity() == null) {
            return;
        }
        //启动裁剪activity
        if (PermissionUtils.checkSdcardAlertWindow(fragment.getActivity())) {
            final Intent cropIntent = new Intent(fragment.getActivity(), ClipImageActivity.class);
            cropIntent.setDataAndType(uriDataAndType, "image/*");
            cropIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(extraOutputFile));
            fragment.startActivityForResult(cropIntent, ClipImageActivity.REQUEST_CODE_CROP);
        } else {
            PermissionUtils.requestPermissionDialog(fragment.getActivity(), PermissionUtils.PermissionType.WRITE_EXTERNAL_STORAGE);
        }

    }

    // 旋转图片
    private Bitmap rotateBitmap(int angle, Bitmap bitmap) {
        // 旋转图片 动作
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        // 创建新的图片
        Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0,
                bitmap.getWidth(), bitmap.getHeight(), matrix, false);
        return resizedBitmap;
    }

    @Override
    public boolean isStatusBarDark() {
        return false;
    }

    @Override
    public void finish() {
        super.finish();
        hideProgress();
    }
}
