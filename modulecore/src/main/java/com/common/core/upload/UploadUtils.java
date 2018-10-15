package com.common.core.upload;

import android.text.TextUtils;

import com.common.log.MyLog;
import com.common.utils.U;
import com.ksyun.ks3.util.DateUtil;
import com.ksyun.ks3.util.Md5Utils;
import com.wali.live.proto.AuthUpload.AuthResponse;
import com.wali.live.proto.AuthUpload.AuthType;
import com.wali.live.proto.AuthUpload.FileInfo;

import java.io.File;
import java.io.IOException;

public class UploadUtils {

    public final static String TAG = "UploadUtils";

    public boolean upload(UploadParams up) {
        boolean check = check(up);
        if (check) {
            File file = new File(up.localPath);
            if (file.exists() && file.isFile() && file.length() != 0) {
                long fileSize = file.length();
                String fileMd5 = null;
                try {
                    fileMd5 = Md5Utils.md5AsBase64(file);
                } catch (IOException e) {
                }
                if (!TextUtils.isEmpty(fileMd5)) {
                    // 获取上传的bucket url，先到app业务服务器鉴权等，然后开始上传
                    // 目前用的是金山云的存储
                    long rid = System.currentTimeMillis();
                    AuthResponse response = UploadServerApi.getKs3AuthToken(rid, "PUT", fileMd5,
                            up.mineType, DateUtil.GetUTCTime(), "",
                            up.getSuffixFromFilePath(), up.type);
                    if (response != null) {
                        String mKs3AuthToken = response.getAuthorization();
                        FileInfo fileInfo = response.getFileInfo();
                        if (null != fileInfo) {
                            MyLog.d(TAG, "start to upload file, file info = " + fileInfo.toString());
                            switch (up.type) {
                                case USER_PIC:
                                case USER_VIDEO:
                                    up.setUrl(fileInfo.getDownloadUrl());
                                    break;
                                case HEAD:
                                case LOG:
                                case PIC:
                                case ANIMATION:
                                case USER_ID:
                                case USER_WALLPAPER:
                                case DEFAULT:
                                    up.setUrl(fileInfo.getUrl());
                                    break;
                            }

                            up.setObjectKey(fileInfo.getObjectKey());
                            if (!TextUtils.isEmpty(fileInfo.getBucket())) {
                                up.setBucketName(fileInfo.getBucket());
                            }
//                            Ks3FileUploader uploader = new Ks3FileUploader(att, att.bucketName, fileInfo.getObjectKey(),
//                                    fileInfo.getAcl(), att.getAttId(), mKs3AuthToken, callBack, response.getDate(), type);
//                            return uploader.startUpload();
                        } else {
                            MyLog.e(TAG, "failed to upload the att because file info is null!");
                            return false;
                        }
                    }
                }
            } else {

            }
        } else {

        }
        return false;
    }

    private boolean check(UploadParams uploadParams) {
        if (uploadParams == null) {
            return false;
        }
        if (TextUtils.isEmpty(uploadParams.localPath)) {
            return false;
        }
        return true;
    }

    public static class UploadParams {
        String localPath;

        String mineType;

        /**
         * 取值在 {@link AuthType} 中
         */
        AuthType type;

        /**
         * 从服务端返回
         **/
        String url;
        String bucketName;
        String objectKey;


        public String getLocalPath() {
            return localPath;
        }

        public void setLocalPath(String localPath) {
            this.localPath = localPath;
        }

        public String getMineType() {
            return mineType;
        }

        public void setMineType(String mineType) {
            this.mineType = mineType;
        }

        public AuthType getType() {
            return type;
        }

        public void setType(AuthType type) {
            this.type = type;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getBucketName() {
            return bucketName;
        }

        public void setBucketName(String bucketName) {
            this.bucketName = bucketName;
        }

        public String getObjectKey() {
            return objectKey;
        }

        public void setObjectKey(String objectKey) {
            this.objectKey = objectKey;
        }

        public String getSuffixFromFilePath() {
            return U.getFileUtils().getSuffixFromFilePath(localPath);
        }

        public static class Builder {

        }
    }

}
