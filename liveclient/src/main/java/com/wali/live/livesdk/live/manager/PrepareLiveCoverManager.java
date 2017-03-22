package com.wali.live.livesdk.live.manager;

import android.app.Activity;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;

import com.base.activity.BaseSdkActivity;
import com.base.fragment.BaseFragment;
import com.base.fragment.FragmentDataListener;
import com.base.fragment.MyRxFragment;
import com.base.global.GlobalData;
import com.base.log.MyLog;
import com.base.permission.PermissionUtils;
import com.base.thread.ThreadPool;
import com.base.utils.sdcard.SDCardUtils;
import com.base.utils.toast.ToastUtils;
import com.mi.live.data.assist.Attachment;
import com.mi.live.data.config.GetConfigManager;
import com.wali.live.common.MessageType;
import com.wali.live.livesdk.R;
import com.wali.live.livesdk.live.image.ClipImageActivity;
import com.wali.live.livesdk.live.image.PhotoPickerFragment;
import com.wali.live.livesdk.live.utils.ImageUtils;
import com.wali.live.livesdk.live.viewmodel.PhotoItem;
import com.wali.live.task.TaskCallBackWrapper;
import com.wali.live.upload.UploadTask;
import com.wali.live.utils.AsyncTaskUtils;
import com.wali.live.utils.AttachmentUtils;

import java.io.File;
import java.util.HashMap;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by yurui on 4/26/16.
 *
 * @module 预览页选择封面的manager
 */
public class PrepareLiveCoverManager {
    private static final String TAG = PrepareLiveCoverManager.class.getSimpleName();

    public static final int REQUEST_CODE_TAKE_PHOTO = 1001;//处理从拍照返回
    public static final int REQUEST_CODE_CROP = 1002;//处理从裁剪图片返回

    private String mTakePhotoPath = null;   //拍照的图片保存的路径
    private String mCropTempFile = null;        //保存裁剪后的临时图片文件路径

    private BaseFragment mFragment;
    private BaseSdkActivity mActivity;
    private LoadFinishListener mLoadFinishListener;
    private boolean mUploadPhoto = true; //是否需要上传photo;
    private boolean needCrop = true; //是否需要剪裁photo;
    private int mClipPhotoHeight;

    public PrepareLiveCoverManager(BaseFragment fragment) {
        mFragment = fragment;
    }

    public void onDestroy() {
        mActivity = null;
        mFragment = null;
    }

    public PrepareLiveCoverManager(BaseSdkActivity activity) {
        mActivity = activity;
    }

    public void setLoadFinishListener(LoadFinishListener listener) {
        mLoadFinishListener = listener;
    }

    /**
     * 上传photo
     */
    public void uploadPhoto(String filePath) {
        MyLog.w(TAG, "uploadPhoto filePath=" + filePath);
        if (TextUtils.isEmpty(filePath) || ((mFragment == null || mFragment.getActivity() == null) && mActivity == null)) {
            ToastUtils.showToast(GlobalData.app(), R.string.upload_failed);
            return;
        }
        File file = new File(filePath);
        if (!file.exists()) {
            ToastUtils.showToast(GlobalData.app(), R.string.upload_failed);
            return;
        }
        if (mFragment != null) {
            ((BaseSdkActivity) mFragment.getActivity()).showProgress(R.string.uploading);
        } else if (null != mActivity) {
            mActivity.showProgress(R.string.uploading);
        }
        //构造一个Attachment  用于上传图片
        Attachment avatarAtt = new Attachment();
        avatarAtt.setType(Attachment.TYPE_IMAGE);
        avatarAtt.setLocalPath(filePath);
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, options);
        avatarAtt.setWidth(options.outWidth);
        avatarAtt.setHeight(options.outHeight);
        avatarAtt.setMimeType(AttachmentUtils.getMimeType(MessageType.IMAGE, avatarAtt.getLocalPath()));
        UploadTask.uploadPhoto(avatarAtt, Attachment.AUTH_TYPE_USER_PIC, new UploadPhotoTaskCallback(avatarAtt));
    }

    /**
     * 上传photo的回调
     */
    private class UploadPhotoTaskCallback extends TaskCallBackWrapper {

        private Attachment mAtt;

        public UploadPhotoTaskCallback(Attachment attachment) {
            mAtt = attachment;
        }

        @Override
        public void processWithMore(Object... objects) {
            super.processWithMore(objects);
        }

        @Override
        public void process(Object object) {
            if ((mFragment == null || mFragment.getActivity() == null) && mActivity == null) {
                return;
            }
            if (object instanceof Boolean) {
                boolean result = (boolean) object;
                MyLog.w(TAG + " UploadPhotoTaskCallback result == " + result);
                if (result && mAtt != null && !TextUtils.isEmpty(mAtt.getUrl())) {
                    MyLog.w(TAG + " UploadPhotoTaskCallback mAvatarAttachment.getUrl() : " + mAtt.getUrl());
                    //io线程解析drawable 用于回调ui展示
                    AsyncTaskUtils.exeIOTask(new AsyncTask<Object, Object, Drawable>() {
                        @Override
                        protected Drawable doInBackground(Object... params) {
                            Drawable drawable = null;
                            File file = new File(mAtt.getLocalPath());
                            if (file.exists()) {
                                drawable = GetConfigManager.file2Drawable(file);
                            } else {
                                MyLog.w(TAG + " doInBackground file not exits");
                            }
                            deleteTmpFile();
                            return drawable;
                        }

                        @Override
                        protected void onPostExecute(Drawable result) {
                            if (mFragment != null && mFragment.getActivity() != null) {
                                ((BaseSdkActivity) mFragment.getActivity()).hideProgress();
                            } else if (null != mActivity) {
                                mActivity.hideProgress();
                            }
                            if (mLoadFinishListener != null) {
                                mLoadFinishListener.onLoadFinishUI(mAtt, result);
                            }
                        }
                    });
                } else {
                    if (mFragment != null) {
                        ((BaseSdkActivity) mFragment.getActivity()).hideProgress();
                    } else if (null != mActivity) {
                        mActivity.hideProgress();
                    }
                    ToastUtils.showToast(R.string.upload_failed);
                }
            }
        }
    }

    public void onClickTakePicButton(MyRxFragment fragment) {
        onClickTakePicButton((BaseSdkActivity) fragment.getActivity());
    }

    /**
     * 点击拍照按钮
     */
    public void onClickTakePicButton(final BaseSdkActivity activity) {
        MyLog.w(TAG, "onClickTakePicButton");
        PermissionUtils.checkPermissionByType(activity, PermissionUtils.PermissionType.CAMERA, new PermissionUtils.IPermissionCallback() {
            @Override
            public void okProcess() {
                final Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                String dirPath = Environment.getExternalStorageDirectory().getPath() + SDCardUtils.IMAGE_DIR_PATH;
                File dir = new File(dirPath);
                if (!dir.exists()) {
                    dir.mkdirs();
                }
                File file = new File(dirPath, System.currentTimeMillis() + ".jpg");
                mTakePhotoPath = file.getAbsolutePath();
                intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));
                Observable.create(new Observable.OnSubscribe<Void>() {
                    @Override
                    public void call(Subscriber<? super Void> subscriber) {
                        //TODO 通知LiveSdkActivity是方向机资源
//                        EventBus.getDefault().post(new LiveEventClass.CameraEvent(LiveEventClass.CameraEvent.EVENT_TYPE_PAUSE));
                        subscriber.onNext(null);
                        subscriber.onCompleted();
                    }
                }).subscribeOn(Schedulers.from(ThreadPool.getEngineExecutor()))
                        .observeOn(AndroidSchedulers.mainThread())
                        .compose(activity.bindUntilEvent())
                        .subscribe(new Subscriber<Object>() {
                            @Override
                            public void onCompleted() {
                            }

                            @Override
                            public void onError(Throwable e) {
                            }

                            @Override
                            public void onNext(Object v) {
                                if (mFragment != null) {
                                    mFragment.startActivityForResult(intent, REQUEST_CODE_TAKE_PHOTO);
                                } else if (mActivity != null) {
                                    mActivity.startActivityForResult(intent, REQUEST_CODE_TAKE_PHOTO);
                                }
                            }
                        });
            }
        });
    }

    /**
     * 点击选择照片按钮
     */
    public void onClickSelectPicButton() {
        MyLog.w(TAG, "onClickSelectPicButton");
        Bundle bundle = new Bundle();
        bundle.putBoolean(PhotoPickerFragment.EXTRA_PREVIEW_END_TO_SEND, true);
        bundle.putInt(PhotoPickerFragment.EXTRA_UI_TYPE, PhotoPickerFragment.UI_TYPE_ADD_PHOTO);
        bundle.putInt(ClipImageActivity.CROP_IMAGE_HEIGHT, mClipPhotoHeight);
        if (needCrop) {
            bundle.putBoolean(PhotoPickerFragment.EXTRA_NEED_CLIP, true);
        } else {
            bundle.putBoolean(PhotoPickerFragment.EXTRA_NEED_CLIP, false);
        }

        PhotoPickerFragment.openFragment(mFragment != null ? (BaseSdkActivity) mFragment.getActivity() : mActivity, mSelectPhotoFragmentDataListener, bundle);
    }

    /**
     * 这个回调是启动PhotoPickerFragment页面返回的回调, 模拟Activity的onActivityResult
     */
    private final FragmentDataListener mSelectPhotoFragmentDataListener = new FragmentDataListener() {
        @Override
        public void onFragmentResult(int requestCode, int resultCode, Bundle data) {
            MyLog.w(TAG, "onFragmentResult requestCode=" + requestCode +
                    "resultCode=" + resultCode);
            if (requestCode == PhotoPickerFragment.REQUEST_SELECT_PHOTO) {    //处理从选择图片页面返回
                if (resultCode == Activity.RESULT_OK) {
                    if (data != null) {
                        HashMap<String, PhotoItem> selectSet = (HashMap<String, PhotoItem>) data.getSerializable(PhotoPickerFragment.EXTRA_SELECT_SET);
                        int index = 0;
                        for (HashMap.Entry<String, PhotoItem> entry : selectSet.entrySet()) {
                            if (index >= 1) {     //只取第一张图片
                                break;
                            } else {
                                index++;
                            }
                            PhotoItem photoItem = entry.getValue();
                            MyLog.w(TAG + "entry.getKey() == " + entry.getKey() + " handleRequestCodeSelectPhoto : photoItem.getLocalPath() : " + photoItem.getLocalPath());
                            final String filePath = photoItem.getLocalPath();
                            if (!TextUtils.isEmpty(filePath)) {
                                final File file = new File(filePath);
                                if (file != null && file.isFile() && file.exists()) {
                                    //启动裁剪图片
                                    if (needCrop) {
                                        startCropActivity(Uri.fromFile(file));
                                    } else {
                                        if (mUploadPhoto) {
                                            uploadPhoto(file.getPath());
                                        } else if (mLoadFinishListener != null) {
                                            mLoadFinishListener.onSelectedFinished(file.getPath());
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        MyLog.w(TAG + " handleRequestCodeSelectPhoto data == null");
                    }
                } else {
                    MyLog.w(TAG + " handleRequestCodeSelectPhoto resultCode != RESULT_OK");
                }
            } else if (requestCode == ClipImageActivity.REQUEST_CODE_CROP) {
                String savePath = data.getString(ClipImageActivity.SAVE_CLIP_IMAGE_PATH, "");
                if (!TextUtils.isEmpty(savePath)) {
                    if (mUploadPhoto) {
                        uploadPhoto(savePath);
                    } else if (mLoadFinishListener != null) {
                        mLoadFinishListener.onSelectedFinished(savePath);
                    }
                }
            }
        }
    };

    /**
     * 启动裁剪图片的activity
     *
     * @param uri
     */
    private void startCropActivity(final Uri uri) {
        MyLog.w(TAG, "startCropActivity uri=" + uri);
        //删除上次的裁剪文件
        if (!TextUtils.isEmpty(mCropTempFile)) {
            File file = new File(mCropTempFile);
            if (file.exists() && file.isFile()) {
                file.delete();
                mCropTempFile = null;
            }
        }
        //创建文件夹
        final String dirPath = Environment.getExternalStorageDirectory() + ImageUtils.AVATAR_TEMP_DIR;
        File dir = new File(dirPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        mCropTempFile = dirPath + "cropTemp" + System.currentTimeMillis() + ".jpg";
        File cropTmpFile = new File(mCropTempFile);
        //启动裁剪activity
        final Intent cropIntent = new Intent(mFragment != null ? mFragment.getActivity() : mActivity, ClipImageActivity.class);
        cropIntent.setDataAndType(uri, "image/*");
        cropIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(cropTmpFile));
        cropIntent.putExtra(ClipImageActivity.CROP_IMAGE_HEIGHT, mClipPhotoHeight);
        if (mFragment != null) {
            mFragment.startActivityForResult(cropIntent, ClipImageActivity.REQUEST_CODE_CROP);
        } else if (mActivity != null) {
            mActivity.startActivityForResult(cropIntent, ClipImageActivity.REQUEST_CODE_CROP);
        }
        MyLog.w("mCurrentSavePath is: " + mCropTempFile);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        MyLog.w(TAG, "request=" + requestCode + "resultCode=" + resultCode);
        if (resultCode != Activity.RESULT_OK) {
            return;
        }
        if (requestCode == REQUEST_CODE_TAKE_PHOTO) {
            if (needCrop) {
                //处理从拍照返回
                if (!TextUtils.isEmpty(mTakePhotoPath) && new File(mTakePhotoPath).exists()) {
                    //启动裁剪图片
                    startCropActivity(Uri.fromFile(new File(mTakePhotoPath)));
                } else {
                    MyLog.w(TAG + " handleRequestCodeTakePhoto mCapturedImagePath == null");
                }
            } else {
                if (mUploadPhoto) {
                    uploadPhoto(mTakePhotoPath);
                } else if (mLoadFinishListener != null) {
                    mLoadFinishListener.onSelectedFinished(mTakePhotoPath);
                }
            }
        } else if (requestCode == ClipImageActivity.REQUEST_CODE_CROP) {
            MyLog.w(TAG, "ClipImageActivity.REQUEST_CODE_CROP");
            //处理拍照、裁剪图片后返回的
            if (data != null) {
                String action = data.getAction();
                Uri tempUri = Uri.parse(action);
                if (tempUri != null) {
                    if (mUploadPhoto) {
                        uploadPhoto(tempUri.getPath());
                    } else if (mLoadFinishListener != null) {
                        mLoadFinishListener.onSelectedFinished(tempUri.getPath());
                    }
                }
            }
        }
    }

    public String getPhotoPath() {
        return mTakePhotoPath;
    }

    /**
     * 删除临时文件
     */
    public void deleteTmpFile() {
        MyLog.w(TAG + " deleteTmpFile()");
        //删除拍照保存的图片
        if (!TextUtils.isEmpty(mTakePhotoPath)) {
            File file = new File(mTakePhotoPath);
            if (file.exists() && file.isFile()) {
                file.delete();
                mTakePhotoPath = null;
            }
        }
        //删除裁剪保存的图片
        if (!TextUtils.isEmpty(mCropTempFile)) {
            File file = new File(mCropTempFile);
            if (file.exists() && file.isFile()) {
                file.delete();
                mCropTempFile = null;
            }
        }
    }

    public interface LoadFinishListener {

        void onLoadFinishUI(final Attachment att, final Drawable drawable);

        /**
         * 选完图的回调 直接返回图片地址
         *
         * @param filePath
         */
        void onSelectedFinished(String filePath);
    }
}
