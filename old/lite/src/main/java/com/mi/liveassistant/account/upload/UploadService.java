package com.mi.liveassistant.account.upload;

import com.mi.liveassistant.common.thread.ThreadPool;
import com.mi.liveassistant.proto.AccountProto;

import java.io.Serializable;

/**
 * 上传用户资料
 * <p/>
 * Created by wuxiaoshan on 17-3-1.
 */
public class UploadService {
    private static final String TAG = UploadService.class.getSimpleName();

    public static void toUpload(UploadInfo uploadInfo) {
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

        public UploadInfo(AccountProto.ThirdPartSignLoginRsp rsp, String name, String headUrl, int sex, int channelId) {
            loginStatus = rsp.getLoginStatus();
            hasInnerAvatar = !isFirstLogin();

            //TODO 昵称和性别其实都不用传了，暂时和app保持一致
            hasInnerNickName = !isFirstLogin();
            hasInnerSex = !isFirstLogin();

            avatar = headUrl;
            nickName = name;
            gender = sex;
            uuid = rsp.getUuid();
            needEditUserInfo = false;
            avatarNeedDownload = true;

            this.channelId = channelId;
        }

        public boolean isFirstLogin() {
            return loginStatus == FIRST_LOGIN_YES;
        }

        public String toString() {
            return "uuid=" + uuid + "\t" +
                    "loginStatus=" + loginStatus + "\t" +
                    "hasInnerAvatar=" + hasInnerAvatar + "\t" +
                    "hasInnerNickName=" + hasInnerNickName + "\t" +
                    "hasInnerSex=" + hasInnerSex + "\t" +
                    "avatar=" + avatar + "\t" +
                    "nickName=" + nickName + "\t" +
                    "gender=" + gender + "\t" +
                    "needEditUserInfo=" + needEditUserInfo;
        }
    }
}
