package com.mi.liveassistant.upload;

import android.text.TextUtils;

import com.ksyun.ks3.util.DateUtil;
import com.ksyun.ks3.util.Md5Utils;
import com.mi.liveassistant.attachment.Attachment;
import com.mi.liveassistant.common.global.GlobalData;
import com.mi.liveassistant.common.log.MyLog;
import com.mi.liveassistant.common.network.Network;
import com.mi.liveassistant.proto.AuthUploadFileProto;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public class UploadFileLoader {
    private static final String TAG = UploadFileLoader.class.getSimpleName();

    private static final UploadFileLoader instance = new UploadFileLoader();

    public static UploadFileLoader getInstance() {
        return instance;
    }

    public boolean startUploadFile(Attachment att, UploadCallBack callBack, int type) {
        if (att == null || TextUtils.isEmpty(att.getLocalPath())) {
            return false;
        }

        if (!Network.hasNetwork(GlobalData.app())) {
            MyLog.v(TAG, "no available network, upload type = " + type);
            return false;
        }

        if (att.needUpload() && !att.isLocalPathEmpty()) {
            String fileMd5;
            try {
                File file = new File(att.getLocalPath());
                if (file.exists() && file.isFile() && file.length() != 0) {
                    att.setFileSize(file.length());
                    fileMd5 = Md5Utils.md5AsBase64(file);
                    MyLog.d(TAG, "start upload file, file Md5 = " + fileMd5);
                } else {
                    MyLog.d(TAG, "file has been deleted or not exist");
                    return false;
                }
            } catch (FileNotFoundException e) {
                MyLog.v(TAG, e);
                return false;
            } catch (IOException e) {
                MyLog.v(TAG, e);
                return false;
            }

            String httpVerb = "PUT";
            AuthUploadFileProto.AuthResponse response = FileUploadSenderWorker.getKs3AuthToken(att.getAttId(), httpVerb, fileMd5, att.getMimeType(), DateUtil.GetUTCTime(), "", att.getSuffixFromLocalPath(), type);

            if (null != response) {
                att.setMd5(fileMd5);
                String mKs3AuthToken = response.getAuthorization();
                AuthUploadFileProto.FileInfo fileInfo = response.getFileInfo();
                if (null != fileInfo) {

                    MyLog.d(TAG, "start to upload file, file info = " + fileInfo.toString());
                    //获取MVP生成的URL
                    if (type == Attachment.AUTH_TYPE_USER_PIC || type == Attachment.AUTH_TYPE_USER_VIDEO) {
                        att.setUrl(fileInfo.getDownloadUrl());
                    } else {
                        att.setUrl(fileInfo.getUrl());
                    }
                    att.setObjectKey(fileInfo.getObjectKey());
                    if (!TextUtils.isEmpty(fileInfo.getBucket())) {
                        att.setBucketName(fileInfo.getBucket());
                    }
                    Ks3FileUploader uploader = new Ks3FileUploader(att, att.bucketName, fileInfo.getObjectKey(),
                            fileInfo.getAcl(), att.getAttId(), mKs3AuthToken, callBack, response.getDate(), type);
                    return uploader.startUpload();
                } else {
                    MyLog.e(TAG, "failed to upload the att because file info is null!");
                    return false;
                }
            } else {
                return false;
            }
        } else {
            //之前已经上传成功，只是消息没有发送成功
            MyLog.v(TAG, "upload failed, the attachment has been uploaded to the ks3 cloud, upload type=" + type);
            return false;
        }
    }
}
