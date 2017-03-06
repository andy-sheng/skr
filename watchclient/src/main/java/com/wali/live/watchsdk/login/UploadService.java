package com.wali.live.watchsdk.login;

import android.app.IntentService;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.IBinder;
import android.text.TextUtils;

import com.base.global.GlobalData;
import com.base.log.MyLog;
import com.base.thread.ThreadPool;
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
import com.wali.live.watchsdk.task.UploadRunnable;

import java.io.Serializable;

import rx.Observable;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * 上传用户资料
 * <p/>
 * Created by wuxiaoshan on 17-3-1.
 */
public class UploadService {

    private static final String TAG = UploadService.class.getSimpleName();

    public static void toUpload(UploadInfo uploadInfo){
        if (!uploadInfo.isFirstLogin()) {
            return;
        }
        ThreadPool.runOnPool(new UploadRunnable(uploadInfo));
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
        public int channelId;

        public UploadInfo(AccountProto.MiSsoLoginRsp miSsoLoginRsp,int channelId) {
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
            this.channelId = channelId;
        }

        public UploadInfo(AccountProto.LoginRsp loginRsp,int channelId) {
            loginStatus = loginRsp.getLoginStatus();
            hasInnerAvatar = loginRsp.getHasInnerAvatar();
            hasInnerNickName = loginRsp.getHasInnerNickname();
            hasInnerSex = loginRsp.getHasInnerSex();
            avatar = loginRsp.getHeadimgurl();
            nickName = loginRsp.getNickname();
            gender = loginRsp.getSex();
            uuid = loginRsp.getUuid();
            avatarNeedDowload = true;
            needEditUserInfo = loginRsp.getIsSetGuide();
            this.channelId = channelId;
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
