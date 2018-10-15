package com.wali.live.watchsdk.editinfo.fragment.presenter;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.text.TextUtils;

import com.base.global.GlobalData;
import com.base.log.MyLog;
import com.base.mvp.BaseRxPresenter;
import com.base.utils.sdcard.SDCardUtils;
import com.base.utils.toast.ToastUtils;
import com.mi.live.data.api.ErrorCode;
import com.mi.live.data.assist.Attachment;
import com.wali.live.common.MessageType;
import com.wali.live.proto.UserProto;
import com.wali.live.task.TaskCallBackWrapper;
import com.wali.live.upload.UploadTask;
import com.wali.live.utils.AttachmentUtils;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.editinfo.fragment.request.UploadInfoRequest;

import java.io.File;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by wangmengjie on 17-8-16.
 *
 * @module 编辑头像表现
 */
public class EditAvatarPresenter extends BaseRxPresenter<IEditAvatarView> {
    public static final String AUTHORITY = "com.wali.live.watchsdk.editinfo.fileprovider";

    private static final String PATH = Environment.getExternalStorageDirectory() + SDCardUtils.IMAGE_DIR_PATH + "/";

    private Uri mTakePicUri;
    private Uri mSelectPicUri;
    private Uri mCropPicUri;

    public EditAvatarPresenter(IEditAvatarView view) {
        super(view);
    }

    public void setSelectPicUri(Uri selectPicUri) {
        mSelectPicUri = selectPicUri;
    }

    public Uri getCropPicUri() {
        return mCropPicUri;
    }

    public Intent setupTakePicIntent() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File file = new File(PATH, System.currentTimeMillis() + ".jpg");
        MyLog.d(TAG, "takePic path = " + file.getPath());
        mTakePicUri = FileProvider.getUriForFile(GlobalData.app(), AUTHORITY, file);
        MyLog.d(TAG, "mTakePicUri = " + mTakePicUri);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, mTakePicUri);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        return intent;
    }

    public Intent setupSelectPicIntent() {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_PICK);
        intent.setType("image/*");
        return intent;
    }

    public Intent setupCropPicIntent(boolean isFromTake) {
        Uri uri = isFromTake ? mTakePicUri : mSelectPicUri;
        if (uri == null) {
            MyLog.d(TAG, "mTakePicUri == null");
            return null;
        }
        File file = new File(PATH, System.currentTimeMillis() + ".jpg");
        MyLog.d(TAG, "cropPic path = " + file.getPath());
        Intent intent = new Intent("com.android.camera.action.CROP");
        mCropPicUri = FileProvider.getUriForFile(GlobalData.app(), AUTHORITY, file);
        intent.setDataAndType(uri, "image/*");
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        intent.putExtra("crop", "true");
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        intent.putExtra("return-data", false);
        intent.putExtra("noFaceDetection", false);
        intent.putExtra("outputFormat", "JPEG");
        return intent;
    }

    public void uploadAvatar(String path) {
        if (TextUtils.isEmpty(path)) {
            ToastUtils.showToast(R.string.change_avatar_failed);
            return;
        }
        File file = new File(path);
        if (!file.exists()) {
            ToastUtils.showToast(R.string.change_avatar_failed);
            return;
        }
        mView.showProgressDialog(R.string.modify_avatar_tip);

        Attachment avatarAtt = new Attachment();
        avatarAtt.setType(Attachment.TYPE_IMAGE);
        avatarAtt.setLocalPath(path);

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);

        avatarAtt.setWidth(options.outWidth);
        avatarAtt.setHeight(options.outHeight);
        avatarAtt.setMimeType(AttachmentUtils.getMimeType(MessageType.IMAGE, avatarAtt.getLocalPath()));
        UploadTask.uploadPhoto(avatarAtt,
                Attachment.AUTH_TYPE_AVATAR,
                new UploadAvatarTaskCallback(avatarAtt));
    }

    private class UploadAvatarTaskCallback extends TaskCallBackWrapper {
        private Attachment mAvatarAtt;

        public UploadAvatarTaskCallback(Attachment avatarAtt) {
            mAvatarAtt = avatarAtt;
        }

        @Override
        public void process(Object object) {
            if (object instanceof Boolean) {
                boolean result = (boolean) object;
                MyLog.d(TAG, "result = " + result);
                if (result && mAvatarAtt != null && !TextUtils.isEmpty(mAvatarAtt.getUrl())) {
                    //更新头像时间戳
                    long now = System.currentTimeMillis();
                    String avatarMd5 = mAvatarAtt.getMd5();
                    uploadAvatarTimestamp(now, avatarMd5);
                } else {
                    if (mView != null) {
                        mView.hideProgressDialog();
                        ToastUtils.showToast(R.string.change_avatar_failed);
                    }
                }
            }
        }
    }

    private Subscription mEditSubscription;

    private void uploadAvatarTimestamp(final long now, final String avatarMd5) {
        if (mEditSubscription != null && !mEditSubscription.isUnsubscribed()) {
            mEditSubscription.unsubscribe();
        }
        mEditSubscription = Observable.just(0)
                .map(new Func1<Integer, UserProto.UploadUserPropertiesRsp>() {
                    @Override
                    public UserProto.UploadUserPropertiesRsp call(Integer integer) {
                        return new UploadInfoRequest().uploadAvatar(now, avatarMd5).syncRsp();
                    }
                }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<UserProto.UploadUserPropertiesRsp>() {
                    @Override
                    public void call(UserProto.UploadUserPropertiesRsp rsp) {
                        MyLog.d(TAG, "uploadAvatar onNext");
                        if (mView == null) {
                            return;
                        }
                        mView.hideProgressDialog();
                        if (rsp != null && rsp.getRetCode() == ErrorCode.CODE_SUCCESS) {
                            mView.editSuccess(now);
                        } else {
                            mView.editFailure();
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        MyLog.e(TAG, "uploadAvatarTimestamp failed=" + throwable);
                    }
                });
    }

    public void destroy() {
        //因为uploadTask耗时操作，防止内存泄露，这里view置空
        mView = null;
    }
}
