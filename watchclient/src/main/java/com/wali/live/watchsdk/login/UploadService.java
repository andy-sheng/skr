package com.wali.live.watchsdk.login;

import android.app.IntentService;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.IBinder;
import android.text.TextUtils;

import com.base.global.GlobalData;
import com.base.log.MyLog;
import com.base.utils.CommonUtils;
import com.mi.live.data.account.MyUserInfoManager;
import com.mi.live.data.assist.Attachment;
import com.mi.live.data.user.User;
import com.wali.live.common.MessageType;
import com.wali.live.proto.AccountProto;
import com.wali.live.task.TaskCallBackWrapper;
import com.wali.live.upload.UploadTask;
import com.wali.live.utils.AttachmentUtils;
import com.wali.live.watchsdk.request.UploadUserInfoRequest;

import java.io.Serializable;

import rx.Observable;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * 上传用户资料
 * <p/>
 * Created by wuxiaoshan on 17-3-1.
 */
public class UploadService extends IntentService {

    private static final String TAG = UploadService.class.getSimpleName();

    public static final int DEFAULT_GENDER = 0;

    public static final String UPLOAD_INFO = "uploadInfo";

    UploadInfo mUploadInfo;

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     */
    public UploadService() {
        super("uploadService");
        MyLog.d(TAG, "init uploadService");
    }
    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
        MyLog.d(TAG, "onStart");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        MyLog.d(TAG, "onStartCommand");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        MyLog.d(TAG, "onDestroy");
    }

    @Override
    public IBinder onBind(Intent intent) {
        MyLog.d(TAG, "onBind");
        return super.onBind(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        MyLog.d(TAG, "onHandleIntent");
        UploadInfo uploadInfo = (UploadInfo) intent.getSerializableExtra(UPLOAD_INFO);
        uploadUserInfo(uploadInfo);
    }

    public void uploadUserInfo(UploadInfo uploadInfo) {
        if (!uploadInfo.needEditUserInfo && !uploadInfo.isFirstLogin()) {
            return;
        } else if (uploadInfo.needEditUserInfo) {
            if (!uploadInfo.hasInnerSex && uploadInfo.gender <= 0) {
                uploadInfo.gender = DEFAULT_GENDER;
            }
        }
        MyLog.w(TAG, "uploadUserInfo start,UploadUserInfo:"+uploadInfo.toString());
        mUploadInfo = uploadInfo;
        if (!uploadInfo.hasInnerAvatar && !TextUtils.isEmpty(uploadInfo.avatar)) {
            String localImgUrl = uploadInfo.avatar;
            if (uploadInfo.avatarNeedDowload) {
                localImgUrl = CommonUtils.downloadImg(uploadInfo.avatar);
            }
            generateAtt(localImgUrl);
        } else {
            upload(0,null);
        }
    }

    private void generateAtt(String localPath) {
        MyLog.d(TAG, "generateAtt localPath =" + localPath);
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(localPath, options);

        Attachment imgAtt = new Attachment();
        imgAtt.setType(Attachment.TYPE_IMAGE);
        imgAtt.setLocalPath(localPath);
        imgAtt.setWidth(options.outWidth);
        imgAtt.setHeight(options.outHeight);

        imgAtt.setMimeType(AttachmentUtils.getMimeType(MessageType.IMAGE, imgAtt.getLocalPath()));
        uploadAttachment(imgAtt);
    }

    private void uploadAttachment(final Attachment imgAt) {
        MyLog.d(TAG, "uploadAttachment");
        UploadTask.uploadPhoto(imgAt, Attachment.AUTH_TYPE_AVATAR, new TaskCallBackWrapper() {
                    @Override
                    public void process(Object object) {
                        MyLog.d(TAG, "object = " + object);
                        final long avatar;
                        final String avatarMd5;
                        if ((Boolean) object) {
                            avatar = Long.valueOf(System.currentTimeMillis());
                            avatarMd5 = imgAt.getMd5();
                            MyLog.w(TAG, "upload avartar to ksy success  mImgAtt url =" + imgAt.getUrl());
                        } else {
                            avatar = 0;
                            avatarMd5 = null;
                            MyLog.w(TAG, "upload avartar to ksy failure");
                        }
                        Observable.just(0)
                                .observeOn(Schedulers.io())
                                .subscribe(new Action1<Integer>() {
                                    @Override
                                    public void call(Integer integer) {
                                        upload(avatar,avatarMd5);
                                    }
                                });


                    }
                }
        );
    }

    private void upload(long avatar,String avatarMd5) {
        String nickName = null;
        int gender = 0;
        if (!mUploadInfo.hasInnerNickName) {
            nickName = mUploadInfo.nickName;
        }
        if (!mUploadInfo.hasInnerSex) {
            gender = mUploadInfo.gender;
        }
        if (TextUtils.isEmpty(nickName) && avatar == 0 && mUploadInfo.hasInnerSex) {
            return;
        }
        UploadUserInfoRequest request = new UploadUserInfoRequest(mUploadInfo.uuid, nickName, gender, avatar, avatarMd5, !mUploadInfo.hasInnerSex);
        int retCode = request.sendRequest();
        //新用户第一次无论信息上传成功与否，写入本地,容错getOwnerInfo7003的问题。
        if (mUploadInfo.isFirstLogin()) {
            User user = new User();
            user.setAvatar(avatar);
            user.setUid(mUploadInfo.uuid);
            user.setGender(gender);
            user.setNickname(nickName);
            MyUserInfoManager.getInstance().setUser(user, true);
        }
        MyLog.w(TAG, "upload user info retCode=" + retCode);
    }

    public static void startService(UploadInfo uploadInfo) {
        if (!uploadInfo.isFirstLogin()) {
            return;
        }
        Intent intent = new Intent(GlobalData.app().getApplicationContext(), UploadService.class);
        intent.putExtra(UPLOAD_INFO, uploadInfo);
        GlobalData.app().startService(intent);
    }

    public static class UploadInfo implements Serializable {

        public final static int FIRST_LOGIN_YES = 1;

        public int loginStatus;
        public boolean hasInnerAvatar;
        public boolean hasInnerNickName;
        public boolean hasInnerSex;
        public String avatar;
        public String nickName;
        public int gender;
        public long uuid;
        public boolean needEditUserInfo;
        public boolean avatarNeedDowload;

        public UploadInfo(AccountProto.MiSsoLoginRsp miSsoLoginRsp) {
            loginStatus = miSsoLoginRsp.getLoginStatus();
            hasInnerAvatar = miSsoLoginRsp.getHasInnerAvatar();
            hasInnerNickName = miSsoLoginRsp.getHasInnerNickname();
            hasInnerSex = miSsoLoginRsp.getHasInnerSex();
            avatar = miSsoLoginRsp.getHeadimgurl();
            nickName = miSsoLoginRsp.getNickname();
            gender = miSsoLoginRsp.getSex();
            uuid = miSsoLoginRsp.getUuid();
            needEditUserInfo = miSsoLoginRsp.getIsSetGuide();
            avatarNeedDowload = true;
        }

        public UploadInfo(AccountProto.LoginRsp loginRsp) {
            loginStatus = loginRsp.getLoginStatus();
            hasInnerAvatar = loginRsp.getHasInnerAvatar();
            hasInnerNickName = loginRsp.getHasInnerNickname();
            hasInnerSex = loginRsp.getHasInnerSex();
            avatar = loginRsp.getHeadimgurl();
            nickName = loginRsp.getNickname();
            gender = loginRsp.getSex();
            uuid = loginRsp.getUuid();
            needEditUserInfo = loginRsp.getIsSetGuide();
        }

        public boolean isFirstLogin() {
            return loginStatus == FIRST_LOGIN_YES;
        }

        public String toString(){
            return "uuid="+uuid+"\tloginStatus="+loginStatus+"\thasInnerAvatar="+hasInnerAvatar+"\thasInnerNickName="+hasInnerNickName+"\thasInnerSex="+hasInnerSex+
                    "\tavatar="+avatar+"\tnickName="+nickName+"\tgender="+gender+"\tneedEditUserInfo="+needEditUserInfo;
        }
    }

}
