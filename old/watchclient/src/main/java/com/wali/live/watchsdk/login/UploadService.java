package com.wali.live.watchsdk.login;

import com.base.log.MyLog;
import com.base.thread.ThreadPool;
import com.mi.live.data.account.UserAccountManager;
import com.wali.live.proto.AccountProto;
import com.wali.live.watchsdk.ipc.service.ThirdPartLoginData;
import com.wali.live.watchsdk.task.UploadRunnable;

import java.io.Serializable;

/**
 * 上传用户资料
 * <p/>
 * Created by wuxiaoshan on 17-3-1.
 */
public class UploadService {
    private static final String TAG = UploadService.class.getSimpleName();

    public static void toUpload(UploadInfo uploadInfo) {
        MyLog.w(TAG, "toUpload uploadInfo.isFirstLogin()=" + uploadInfo.isFirstLogin() +
                " uploadInfo.hasInfoUpload()=" + uploadInfo.hasInfoUpload());
        if (!uploadInfo.isFirstLogin() && !uploadInfo.hasInfoUpload()) {
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
        public boolean avatarNeedDownload;
        public int channelId;

        public UploadInfo(AccountProto.MiSsoLoginRsp miSsoLoginRsp, int channelId) {
            loginStatus = miSsoLoginRsp.getLoginStatus();
            hasInnerAvatar = miSsoLoginRsp.getHasInnerAvatar();
            hasInnerNickName = miSsoLoginRsp.getHasInnerNickname();
            hasInnerSex = miSsoLoginRsp.getHasInnerSex();
            avatar = miSsoLoginRsp.getHeadimgurl();
            nickName = miSsoLoginRsp.getNickname();
            gender = miSsoLoginRsp.getSex();
            uuid = miSsoLoginRsp.getUuid();
            needEditUserInfo = miSsoLoginRsp.getIsSetGuide();
            avatarNeedDownload = true;
            this.channelId = channelId;
        }

        public UploadInfo(AccountProto.LoginRsp loginRsp, int channelId) {
            loginStatus = loginRsp.getLoginStatus();
            hasInnerAvatar = loginRsp.getHasInnerAvatar();
            hasInnerNickName = loginRsp.getHasInnerNickname();
            hasInnerSex = loginRsp.getHasInnerSex();
            avatar = loginRsp.getHeadimgurl();
            nickName = loginRsp.getNickname();
            gender = loginRsp.getSex();
            uuid = loginRsp.getUuid();
            avatarNeedDownload = true;
            needEditUserInfo = loginRsp.getIsSetGuide();
            this.channelId = channelId;
        }

        public UploadInfo(AccountProto.ThirdPartSignLoginRsp rsp, ThirdPartLoginData loginData) {
            loginStatus = rsp.getLoginStatus();
            hasInnerAvatar = !isFirstLogin();
            hasInnerNickName = !isFirstLogin();
            hasInnerSex = !isFirstLogin();
            avatar = loginData.getHeadUrl();
            nickName = loginData.getNickname();
            gender = loginData.getSex();
            uuid = rsp.getUuid();
            needEditUserInfo = false;
            avatarNeedDownload = true;
            this.channelId = loginData.getChannelId();
        }

        public UploadInfo(ThirdPartLoginData loginData) {
            hasInnerAvatar = false;
            hasInnerNickName = false;
            hasInnerSex = false;
            uuid = UserAccountManager.getInstance().getUuidAsLong();
            avatar = loginData.getHeadUrl();
            nickName = loginData.getNickname();
            gender = loginData.getSex();
            needEditUserInfo = false;
            avatarNeedDownload = true;
            this.channelId = loginData.getChannelId();
        }

        public boolean hasInfoUpload() {
            return !(hasInnerSex && hasInnerNickName && hasInnerAvatar);
        }

        public boolean isFirstLogin() {
            return loginStatus == FIRST_LOGIN_YES;
        }

        public String toString() {
            return "uuid=" + uuid + "\tloginStatus=" + loginStatus + "\thasInnerAvatar=" + hasInnerAvatar + "\thasInnerNickName=" + hasInnerNickName + "\thasInnerSex=" + hasInnerSex +
                    "\tavatar=" + avatar + "\tnickName=" + nickName + "\tgender=" + gender + "\tneedEditUserInfo=" + needEditUserInfo;
        }
    }
}
