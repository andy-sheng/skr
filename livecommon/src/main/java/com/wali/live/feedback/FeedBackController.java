package com.wali.live.feedback;

import android.os.Environment;

import com.base.global.GlobalData;
import com.base.log.MyLog;
import com.base.utils.IOUtils;
import com.base.utils.network.Network;
import com.base.utils.sdcard.SDCardUtils;
import com.mi.live.data.assist.Attachment;
import com.wali.live.task.ITaskCallBack;
import com.wali.live.upload.UploadTask;
import com.wali.live.utils.AttachmentUtils;

import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.zip.ZipOutputStream;

/**
 * Created by xionganping on 16-03-06.
 *
 * @module 用户反馈页面
 */
public class FeedBackController{
    private static final String uploadFilePath = GlobalData.app().getExternalCacheDir() + "/assistlog.zip";
    private static final String logDirRoot = "/Xiaomi/WALI_LIVE_SDK/logs/com.mi.liveassistant/";
    private static final String ks3DirRoot = "/Xiaomi/WALI_LIVE_SDK/ksyLog/";
    private static final String galileoDirRoot = "/voip-data/com.mi.liveassistant/";
    public static final String logAnrRoot = "/data/anr";

    public static final String FEED_BACK_URL = "http://dzb.g.mi.com/vuf.do?";
    public static final int SD_CARD_BUSY = 1;
    public static final int LOG_FILE_SIZE_TOO_LARGE = SD_CARD_BUSY + 1;
    public static final int LOG_FILE_PAHT_EMPTY = LOG_FILE_SIZE_TOO_LARGE + 1;
    public static final int LOG_FILE_NOT_EXIST = LOG_FILE_PAHT_EMPTY + 1;
    public static final int NETWORK_ERROR = LOG_FILE_NOT_EXIST + 1;
    public static final int GET_BUCKET_OBJECT_ID_FAILURE = NETWORK_ERROR + 1;
    public static final int WAIT_FOR_UPLOAD_CALLBACK = GET_BUCKET_OBJECT_ID_FAILURE + 1;
    public static final int LOG_FILE_UPLOAD_SUCCESS = WAIT_FOR_UPLOAD_CALLBACK + 1;
    public static final int LOG_FILE_UPLOAD_FAILURE = LOG_FILE_UPLOAD_SUCCESS + 1;
    public static final int ZIP_UPLOAD_FILE_SUCCESS = LOG_FILE_UPLOAD_FAILURE + 1;

    public static int zipUploadFile(ArrayList<String> imagePaths, long lengthLimit) {

        if (SDCardUtils.isSDCardBusy()) {
            return SD_CARD_BUSY;
        }

        FileOutputStream fos = null;
        ZipOutputStream gos = null;

        try {
            //拷贝 anr 文件
            //copyAnrToMiliaoLogs(logDirRoot);
            // 将log文件夹中的所有文件打包并
            fos = new FileOutputStream(uploadFilePath, false);
            gos = new ZipOutputStream(fos);
            File logDir = new File(Environment.getExternalStorageDirectory(), logDirRoot);
            File ks3Dir = new File(Environment.getExternalStorageDirectory(), ks3DirRoot);
            File galileoDir = new File(Environment.getExternalStorageDirectory(), galileoDirRoot);
            File anrDir = new File("", logAnrRoot);
            // 将图片压缩在zip包里
            if (imagePaths != null && imagePaths.size() > 0) {
                for (String imagePath : imagePaths) {
                    File file = new File(imagePath);
                    IOUtils.zip(gos, file, file.getName(), null);
                }
            }
            IOUtils.zip(gos, logDir, null, new FileFilter() {
                @Override
                public boolean accept(File pathname) {
                    return pathname.isFile() && (pathname.getName().endsWith(".txt") || pathname.getName().endsWith(".log"));
                }
            });
            if (ks3Dir != null && ks3Dir.exists()) {
                File[] ks3Files = ks3Dir.listFiles();
                if (ks3Files != null && ks3Files.length > 0) {
                    for (File file : ks3Files) {
                        if (file != null && file.isFile()) {
                            IOUtils.zip(gos, file, file.getName(), null);
                        }
                    }
                }
            }
            if (galileoDir != null && galileoDir.exists()) {
                File[] galileoFiles = galileoDir.listFiles();
                if (galileoFiles != null && galileoFiles.length > 0) {
                    for (File file : galileoFiles) {
                        if (file != null && file.isFile() &&
                                (file.getName().startsWith("trace") || file.getName().endsWith(".dmp"))) {
                            IOUtils.zip(gos, file, file.getName(), null);
                        }
                    }
                }
            }

            if (anrDir != null && anrDir.exists()) {
                File[] anrFiles = anrDir.listFiles();
                if (anrFiles != null && anrFiles.length > 0) {
                    for (File file : anrFiles) {
                        if (file != null && file.isFile()) {
                            IOUtils.zip(gos, file, file.getName(), null);
                        }
                    }
                }
            }

            gos.flush();
        } catch (IOException e) {
            MyLog.e(e);
            return LOG_FILE_NOT_EXIST;
        } finally {
            IOUtils.closeQuietly(gos);
            IOUtils.closeQuietly(fos);
        }
        File logFile = new File(uploadFilePath);
        if (!logFile.exists()) {
            return LOG_FILE_NOT_EXIST;
        }
        MyLog.v("networkprobe nettestok start zip file success");

        if (!Network.isWIFIConnected(GlobalData.app())) {
            if (lengthLimit != 0 && logFile.length() > lengthLimit) {
                MyLog.v("networkprobe 非wifi日志文件太大放弃自动上传" + logFile.length() + "/" + lengthLimit);
                logFile.delete();
                return LOG_FILE_SIZE_TOO_LARGE;
            }
        }

        return ZIP_UPLOAD_FILE_SUCCESS;
    }


    public static void uploadLogFile(String phonenumber, String logDescription, ITaskCallBack iTaskCallBack) {

        File file = new File(uploadFilePath);
        if (!file.exists()) {
            return;
        }
        Attachment att = new Attachment();
        att.localPath = uploadFilePath;
        att.filename = file.getName();
        att.fileSize = file.length();
        //att.mimeType = AttachmentUtils.getMimeType(MessageType.OFFLINE_FILE, uploadFilePath);
        att.attId = AttachmentUtils.generateAttachmentId();
        att.authType = Attachment.AUTH_TYPE_FEED_BACK;
        int dotSuffixIndex = att.localPath.lastIndexOf(".");
        String suffix = "";
        if (dotSuffixIndex > 0) {
            suffix = att.localPath.substring(dotSuffixIndex);
        }
        att.mimeType = "file/" + suffix;
//        ITaskCallBack callBack = new TaskCallBackWrapper() {
//
//            @Override
//            public void process(Object result) {
//                int notifyServerResult = LOG_FILE_UPLOAD_FAILURE;
//                if (null != result) {
//                    boolean bret = Boolean.valueOf(String.valueOf(result));
//                    if (bret) {
//                        notifyUserUploadResult(LOG_FILE_UPLOAD_SUCCESS);
//                        //new File(uploadFilePath).delete();
//                        return;
//                    }
//                }
//                notifyUserUploadResult(notifyServerResult);
//            }
//        };
        UploadTask.uploadFeedBack(att, iTaskCallBack, phonenumber, logDescription);

    }

}
