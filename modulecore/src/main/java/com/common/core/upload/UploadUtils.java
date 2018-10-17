package com.common.core.upload;

import android.text.TextUtils;

import com.common.core.avatar.AvatarUtils;
import com.common.log.MyLog;
import com.common.utils.U;
import com.ksyun.ks3.util.DateUtil;
import com.ksyun.ks3.util.Md5Utils;
import com.wali.live.proto.AuthUpload.AuthResponse;
import com.wali.live.proto.AuthUpload.AuthType;
import com.wali.live.proto.AuthUpload.FileInfo;

import java.io.File;

/**
 * 上传的工具类，上传的入口
 */
public class UploadUtils {

    public final static String TAG = "UploadUtils";

    public static boolean upload(UploadParams up, UploadCallBack callBuck) {
        boolean check = check(up);
        if (check) {
            File file = new File(up.localPath);
            if (file.exists() && file.isFile() && file.length() != 0) {
                long fileSize = file.length();
                String fileMd5 = null;
                try {
                    fileMd5 = Md5Utils.md5AsBase64(file);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (!TextUtils.isEmpty(fileMd5)) {
                    // 获取上传的bucket url，先到app业务服务器鉴权等，然后开始上传
                    // 目前用的是金山云的存储
                    AuthResponse response = UploadServerApi.getKs3AuthToken(up.getRid(), "PUT", fileMd5,
                            up.mimeType, DateUtil.GetUTCTime(), "",
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

                            Ks3FileUploader uploader = new Ks3FileUploader(up, mKs3AuthToken, fileInfo.getAcl(), response.getDate(), callBuck);
                            return uploader.startUpload();
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

    private static boolean check(UploadParams uploadParams) {
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

        /**
         * 上传文件类型(包含image/jpg, image/png, image/gif, image/bmp图片格式,图片类型可从FileUtils中获得
         * audio/音频类型
         * video/视频类型
         * 等
         */
        String mimeType;

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

        private long rid;

        public long getRid() {
            return rid;
        }

        private void setRid(long rid) {
            this.rid = rid;
        }

        public String getLocalPath() {
            return localPath;
        }

        public void setLocalPath(String localPath) {
            this.localPath = localPath;
        }

        public String getMimeType() {
            return mimeType;
        }

        public void setMimeType(String mimeType) {
            this.mimeType = mimeType;
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

        public static Builder newBuilder() {
            return new Builder();
        }

        public static class Builder {
            private UploadParams mUploadParams = new UploadParams();

            Builder() {
            }

            public UploadParams.Builder setLocalPath(String localPath) {
                this.mUploadParams.setLocalPath(localPath);
                return this;
            }

            public UploadParams.Builder setMimeType(String mimeType) {
                this.mUploadParams.mimeType = mimeType;
                return this;
            }

            public UploadParams.Builder setType(AuthType type) {
                this.mUploadParams.type = type;
                return this;
            }

            public UploadParams.Builder setUrl(String url) {
                this.mUploadParams.url = url;
                return this;
            }

            public UploadParams.Builder setBucketName(String bucketName) {
                this.mUploadParams.bucketName = bucketName;
                return this;
            }

            public UploadParams.Builder setObjectKey(String objectKey) {
                this.mUploadParams.objectKey = objectKey;
                return this;
            }

            public UploadParams build() {
                if (this.mUploadParams == null) {
                    this.mUploadParams = new UploadParams();
                }

                if (TextUtils.isEmpty(mUploadParams.getLocalPath())) {
                    throw new IllegalArgumentException("UploadParams.Build must setLocalPath not null");
                }

                if (TextUtils.isEmpty(mUploadParams.getMimeType())) {
                    throw new IllegalArgumentException("UploadParams.Build must setMimeType not null");
                }

                if (mUploadParams.getType() == null) {
                    throw new IllegalArgumentException("UploadParams.Build must set AuthType not null");
                }

                if (mUploadParams.getRid() == 0) {
                    mUploadParams.setRid(System.currentTimeMillis());
                }

                return this.mUploadParams;
            }
        }
    }

}
