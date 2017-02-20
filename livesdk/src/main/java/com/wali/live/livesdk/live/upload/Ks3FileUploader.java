package com.wali.live.livesdk.live.upload;

import android.text.TextUtils;

import com.base.global.GlobalData;
import com.base.log.MyLog;
import com.base.thread.ThreadPool;
import com.base.utils.Constants;
import com.ksyun.ks3.exception.Ks3Error;
import com.ksyun.ks3.model.HttpMethod;
import com.ksyun.ks3.model.PartETag;
import com.ksyun.ks3.model.acl.CannedAccessControlList;
import com.ksyun.ks3.model.result.CompleteMultipartUploadResult;
import com.ksyun.ks3.model.result.InitiateMultipartUploadResult;
import com.ksyun.ks3.model.result.ListPartsResult;
import com.ksyun.ks3.services.AuthListener;
import com.ksyun.ks3.services.AuthResult;
import com.ksyun.ks3.services.Ks3Client;
import com.ksyun.ks3.services.Ks3ClientConfiguration;
import com.ksyun.ks3.services.handler.CompleteMultipartUploadResponseHandler;
import com.ksyun.ks3.services.handler.InitiateMultipartUploadResponceHandler;
import com.ksyun.ks3.services.handler.ListPartsResponseHandler;
import com.ksyun.ks3.services.handler.PutObjectResponseHandler;
import com.ksyun.ks3.services.handler.UploadPartResponceHandler;
import com.ksyun.ks3.services.request.CompleteMultipartUploadRequest;
import com.ksyun.ks3.services.request.InitiateMultipartUploadRequest;
import com.ksyun.ks3.services.request.ListPartsRequest;
import com.ksyun.ks3.services.request.PutObjectRequest;
import com.ksyun.ks3.services.request.UploadPartRequest;
import com.mi.live.data.assist.Attachment;
import com.wali.live.livesdk.R;
import com.wali.live.proto.AuthUploadFileProto;
import com.wali.live.statistics.StatisticUtils;
import com.wali.live.statistics.StatisticsKey;

import org.apache.http.Header;

import java.io.File;
import java.net.URL;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Ks3上传控制器，做金山云上传工作，这个类只供UploadFileLoader使用，对其包里的类不可见
 */
public class Ks3FileUploader {
    private static final String TAG = Ks3FileUploader.class.getSimpleName();

    private static final long SMALL_PAGE_SIZE = 500 * 1024;
    private static final long LARGE_PAGE_SIZE = 5 * 1024 * 1024; //5M

    public static long PART_SIZE = SMALL_PAGE_SIZE;
    public static final long MULTI_UPLOAD_THREADHOLD =  20 * 1024 * 1024; //1M
    public static final long LARGE_FILE_SIZE = 5 * 1024 * 1024;

    public static final ConcurrentHashMap<Long, InitiateMultipartUploadResult> sInitiateMultipartUploadResultMap = new ConcurrentHashMap<Long, InitiateMultipartUploadResult>();
    public static final ConcurrentHashMap<Long, Ks3Client> sKs3ClientMap = new ConcurrentHashMap<Long, Ks3Client>();
    public static final ConcurrentHashMap<Long, AuthUploadFileProto.FileInfo> sFileInfoMap = new ConcurrentHashMap<Long, AuthUploadFileProto.FileInfo>();

    public static final String AccessControl_Private = "private";
    public static final String AccessControl_PublicRead = "public-read";
    public static final String AccessControl_PublicReadWrite = "public-read-write";

    public static final String Ks3FileHost = "http://kssws.ks-cdn.com";
    private CannedAccessControlList mList = CannedAccessControlList.PublicRead;

    private Ks3Client mKs3Client;
    private Attachment mAtt;
    private String mBucketName = "";
    private String mObjId = "";
    private long mMsgId;
    private UploadCallBack mUploadCallBack;
    private String mToken;
    private String authDate;
    private int type;

    public Ks3FileUploader(final Attachment att, final String bucketName, final String objId, final String acl, final long msgId, String ks3AuthToken,
                           final UploadCallBack uploadCallBack, final String authDate, int type) {
        this.mAtt = att;
        this.mBucketName = bucketName; //上传文件的文件夹，
        this.mObjId = objId;  //上传到金山云的文件名
        this.mMsgId = msgId;
        this.mToken = ks3AuthToken;              //金山云上传token
        this.mList = generateObjectAcl(acl);
        this.mUploadCallBack = uploadCallBack;
        this.authDate = authDate;
        this.type = type;
    }

    private Ks3Client getDefaultClient() { //使用服务器的时间，这样做可能有问题，不能用map
        if (mKs3Client == null) {
            if (sKs3ClientMap.contains(mMsgId)) {
                mKs3Client = sKs3ClientMap.get(mMsgId);
            } else {
                mKs3Client = new Ks3Client(new AuthListener() {
                    @Override
                    public AuthResult onCalculateAuth(String httpMethod, String contentType,
                                                      String date, String contentMD5,
                                                      String Resource, String Headers) {
                        // 此处应由APP端向业务服务器发送post请求返回Token。需要注意该回调方法运行在非主线程
                        if (mAtt.getFileSize() >= Ks3FileUploader.MULTI_UPLOAD_THREADHOLD) {
                            String aclData = "";
                            try {
                                if (!TextUtils.isEmpty(Headers) && Headers.contains("x-kss-acl")) {
                                    if (Headers.contains(AccessControl_PublicReadWrite)) {
                                        aclData = AccessControl_Private;
                                    } else if (Headers.contains(AccessControl_PublicRead)) {
                                        aclData = AccessControl_PublicRead;
                                    } else {
                                        aclData = AccessControl_Private;
                                    }
                                }
                            } catch (Exception e) {
                                MyLog.e(e);
                            }
                            String multipartKs3AuthToken = FileUploadSenderWorker.getMultipartKs3AuthToken(mAtt.getAttId(), Resource == null ? "" : Resource, date == null ? "" : date
                                    , httpMethod == null ? "" : httpMethod, contentMD5 == null ? "" : contentMD5, contentType == null ? "" : contentType, aclData);
                            if (!TextUtils.isEmpty(multipartKs3AuthToken)) {
                                mToken = multipartKs3AuthToken;
                                authDate = date;
                            }
                        }
                        AuthResult result;
                        result = new AuthResult(mToken, authDate);
                        return result;
                    }
                }, GlobalData.app());

                if (Constants.isTestBuild) {
                    mKs3Client.setEndpoint("ks3-cn-beijing.ksyun.com");
                } else {
                    mKs3Client.setEndpoint("ul.zb.mi.com");
                }
                mKs3Client.setConfiguration(Ks3ClientConfiguration.getDefaultConfiguration());
            }
            sKs3ClientMap.put(mMsgId, mKs3Client);
        }
        return mKs3Client;
    }

    public CannedAccessControlList generateObjectAcl(String acl) {
        if (TextUtils.isEmpty(acl)) {
            return CannedAccessControlList.PublicRead;
        }
        String cannedAclHeader = acl;
        if (cannedAclHeader.equalsIgnoreCase(AccessControl_Private)) {
            return CannedAccessControlList.Private;
        } else if (cannedAclHeader.equalsIgnoreCase(AccessControl_PublicReadWrite)) {
            return CannedAccessControlList.PublicReadWrite;
        }
        return CannedAccessControlList.PublicRead;
    }

    public boolean startUpload() {
        if (null == mAtt || TextUtils.isEmpty(mAtt.getLocalPath())) {
            if (mUploadCallBack != null) {
                mUploadCallBack.onPostExecute(false, GlobalData.app().getString(R.string.file_upload_failed_path_error), getResourceId());
            }
            return false;
        }

        // 文件不存在或者不是一个文件
        File file = new File(mAtt.getLocalPath());
        if (!file.exists() || !file.isFile() || file.length() == 0) {
            if (mUploadCallBack != null) {
                mUploadCallBack.onPostExecute(false, GlobalData.app().getString(R.string.file_upload_failed_file_error), getResourceId());
            }
            return false;
        }

        // 根据指定的文件大小，选择用直接上传或者分块上传
        if (mAtt.getFileSize() >= LARGE_FILE_SIZE) {
            PART_SIZE = LARGE_PAGE_SIZE;
        } else {
            PART_SIZE = SMALL_PAGE_SIZE;
        }
        MyLog.v(TAG, "Ks3FileUploader PART_SIZE=" + PART_SIZE);

        if (mAtt.getFileSize() >= MULTI_UPLOAD_THREADHOLD) {
            doMultipartUpload(mBucketName, mAtt, getDefaultClient(), type);//发现分片上传不work
        } else {
            doSingleUpload(mBucketName, mAtt, getDefaultClient());
        }
        return true;
    }

    //上传单片文件
    private void doSingleUpload(final String bucketName, final Attachment att, final Ks3Client client) {
        if (null == client || null == att || TextUtils.isEmpty(att.getLocalPath()) || !(new File(att.getLocalPath()).exists())) {
            MyLog.v(TAG, "failed to upload att because there is no res file or no client");
            return;
        }
        MyLog.v(TAG, "upload bucketName = " + bucketName + " att = " + att.toString() + " client = " + client.toString() + " objectId = " + mObjId);
        ThreadPool.runOnUi(new Runnable() {
            @Override
            public void run() {

                final PutObjectRequest request = new PutObjectRequest(bucketName, mObjId, new File(att.getLocalPath()));
                request.setCannedAcl(mList);
                request.setHttpMethod(com.ksyun.ks3.model.HttpMethod.PUT);
                request.setContentType(att.getMimeType());
                MyLog.d(TAG, "upload att.getMimeType() = " + att.getMimeType());
                //final long startTime = System.currentTimeMillis();
                client.putObject(request, new PutObjectResponseHandler() {

                    @Override
                    public void onTaskProgress(double progress) {
                        if (isAborted()) {
                            request.abort();
                        } else {
                            if (mUploadCallBack != null) {
                                mUploadCallBack.onTaskProgress(progress);
                            }
                        }
                    }

                    @Override
                    public void onTaskSuccess(int statesCode, Header[] responceHeaders, StringBuffer var3) {
                        if (isAborted()) {
                            request.abort();
                        } else {
                            onUploadFinished(true, att.getUrl());
                            MyLog.v(TAG, "Upload file success, att url is " + att.getUrl());
                            if (mUploadCallBack != null) {
                                mUploadCallBack.onTaskSuccess(statesCode, responceHeaders, var3);
                                mUploadCallBack = null;
                            }
                            StatisticUtils.addToMiLinkMonitor(String.format(StatisticsKey.KEY_KS3_UPLOAD_FILE, getHost(att.getUrl())), StatisticUtils.SUCCESS);
                        }
                    }

                    @Override
                    public void onTaskStart() {
                        if (mUploadCallBack != null) {
                            mUploadCallBack.onTaskStart();
                        }
                    }

                    @Override
                    public void onTaskFinish() {
                    }

                    @Override
                    public void onTaskCancel() {
                        if (mUploadCallBack != null) {
                            mUploadCallBack.onTaskCancel();
                        }
                    }

                    @Override
                    public void onTaskFailure(int statesCode, Ks3Error ks3Error,
                                              Header[] responceHeaders, String response, Throwable paramThrowable, StringBuffer var6) {
                        MyLog.w(TAG, "onTaskFailure " + (null != paramThrowable ? paramThrowable.getMessage() : "") + var6);
                        if (isAborted()) {
                            request.abort();
                        } else {

                            if (ks3Error != null) {
                                StatisticUtils.addToMiLinkMonitor(String.format(StatisticsKey.KEY_KS3_UPLOAD_FILE, getHost(att.getUrl())), ks3Error.getErrorCode(), att.getUrl());
                            } else {
                                StatisticUtils.addToMiLinkMonitor(String.format(StatisticsKey.KEY_KS3_UPLOAD_FILE, getHost(att.getUrl())), StatisticUtils.FAILED, att.getUrl());
                            }

                            onUploadFinished(false, response);
                            if (mUploadCallBack != null) {
                                mUploadCallBack.onTaskFailure(statesCode, ks3Error, responceHeaders, response, paramThrowable, var6);
                                mUploadCallBack = null;
                            }
                        }
                    }
                });
            }
        });
    }

    private void doMultipartUpload(final String bucketName, final Attachment att, final Ks3Client client, int type) {
        //判读是否初始化成功，如果曾经初始化成功，则直接进行上传
        if (sInitiateMultipartUploadResultMap.containsKey(mMsgId)) {
            InitiateMultipartUploadResult initResult = sInitiateMultipartUploadResultMap.get(mMsgId);
            UploadPartRequestFactory requestFactory = new UploadPartRequestFactory(
                    initResult.getBucket(),
                    initResult.getKey(),
                    initResult.getUploadId(),
                    new File(att.getLocalPath()),
                    PART_SIZE);
            ListPartsRequest listRequest = new ListPartsRequest(
                    requestFactory.getBucketName(),
                    requestFactory.getObjectKey(),
                    requestFactory.getUploadId());
            mBucketName = requestFactory.getBucketName();
            mObjId = requestFactory.getObjectKey();
            listParts(listRequest, att, true, requestFactory, type);
            return;
        }

        InitiateMultipartUploadRequest request;
        request = new InitiateMultipartUploadRequest(bucketName, mObjId);
        request.setCannedAcl(mList);
        initiateMultipartUpload(request, att, client, type);
    }

    private void initiateMultipartUpload(final InitiateMultipartUploadRequest request, final Attachment att, final Ks3Client client, final int type) {
        ThreadPool.runOnUi(new Runnable() {
            @Override
            public void run() {
                request.setCannedAcl(mList);
                request.setHttpMethod(HttpMethod.POST);
                request.setContentType(att.getMimeType());
                final long startTime = System.currentTimeMillis();
                client.initiateMultipartUpload(request,
                        new InitiateMultipartUploadResponceHandler() {
                            @Override
                            public void onSuccess(int statesCode, Header[] responceHeaders, InitiateMultipartUploadResult result, StringBuffer var4) {
                                beginMultiUpload(result, att);
                            }

                            @Override
                            public void onFailure(int statesCode, Ks3Error ks3Error, Header[] responceHeaders,
                                                  String response, Throwable throwable, StringBuffer var4) {
                                if (mUploadCallBack != null) {
                                    mUploadCallBack.onFailure(statesCode, responceHeaders, response.getBytes(), throwable);
                                    mUploadCallBack = null;
                                }
                                MyLog.v(TAG, "sliceFile error:" + ks3Error.getErrorCode() + " " + ks3Error.getErrorMessage() + " " + att.getLocalPath());
                                if (ks3Error != null) {
                                    StatisticUtils.addToMiLinkMonitor(String.format(StatisticsKey.KEY_KS3_UPLOAD_SCLICE, getHost(att.getUrl())), ks3Error.getErrorCode(), att.getUrl());
                                } else {
                                    StatisticUtils.addToMiLinkMonitor(String.format(StatisticsKey.KEY_KS3_UPLOAD_SCLICE, getHost(att.getUrl())), StatisticUtils.FAILED, att.getUrl());
                                }
                            }
                        });
            }
        });
    }

    private void beginMultiUpload(InitiateMultipartUploadResult initResult, final Attachment att) {
        sInitiateMultipartUploadResultMap.put(mMsgId, initResult);
        UploadPartRequestFactory localUploadPartRequestFactory = new UploadPartRequestFactory(
                initResult.getBucket(), initResult.getKey(),
                initResult.getUploadId(), new File(att.getLocalPath()), PART_SIZE);
        uploadpart(localUploadPartRequestFactory, att);
    }

    private void uploadpart(final UploadPartRequestFactory requestFactory, final Attachment att) {
        if (requestFactory == null) {
            return;
        }
        if (requestFactory.hasMoreRequests()) {
            ThreadPool.runOnUi(new Runnable() {

                @Override
                public void run() {

                    Ks3Client client = getDefaultClient();
                    final UploadPartRequest request = requestFactory.getNextUploadPartRequest();
                    request.setCannedAcl(mList);
                    request.setHttpMethod(HttpMethod.PUT);
                    request.setContentType(att.getMimeType());

                    final long startTime = System.currentTimeMillis();
                    MyLog.v(TAG, "upload upload part=" + request.toString());

                    client.uploadPart(request, new UploadPartResponceHandler() {
                        double progressInFile = 0;

                        @Override
                        public void onTaskProgress(double progress) {
                            if (isAborted()) {
                                request.abort();
                            } else {
                                long uploadedInpart = (long) (progress / 100 * request.contentLength);
                                long uploadedInFile = uploadedInpart + requestFactory.getUploadedSize();
                                progressInFile = Double.valueOf(request.getFile().length() > 0 ?
                                        uploadedInFile * 1.0 / request.getFile().length() * 100.0 :
                                        -1.0);
                                if (mUploadCallBack != null) {
                                    mUploadCallBack.onTaskProgress(progressInFile);
                                }
                            }
                        }

                        @Override
                        public void onSuccess(int statesCode, Header[] responceHeaders, PartETag result, StringBuffer var4) {
                            if (isAborted()) {
                                request.abort();
                            } else {
                                uploadpart(requestFactory, att);
                                StatisticUtils.addToMiLinkMonitor(String.format(StatisticsKey.KEY_KS3_SLICE_FILE, getHost(att.getUrl())), StatisticUtils.SUCCESS);
                            }
                        }

                        @Override
                        public void onFailure(int statesCode, Ks3Error ks3Error, Header[] responceHeaders,
                                              String response, Throwable throwable, StringBuffer var4) {
                            if (mUploadCallBack != null) {
                                mUploadCallBack.onFailure(statesCode, responceHeaders, response.getBytes(), throwable);
                                mUploadCallBack = null;
                            }
                            if (ks3Error != null) {
                                StatisticUtils.addToMiLinkMonitor(String.format(StatisticsKey.KEY_KS3_SLICE_FILE, getHost(att.getUrl())), ks3Error.getErrorCode(), att.getUrl());
                            } else {
                                StatisticUtils.addToMiLinkMonitor(String.format(StatisticsKey.KEY_KS3_SLICE_FILE, getHost(att.getUrl())), StatisticUtils.FAILED, att.getUrl());
                            }
                        }

                    });
                }
            });
        } else {
            ListPartsRequest listRequest = new ListPartsRequest(
                    requestFactory.getBucketName(),
                    requestFactory.getObjectKey(),
                    requestFactory.getUploadId());
            listParts(listRequest, att, false, null, type);
        }
    }

    private void listParts(final ListPartsRequest request, final Attachment att, final boolean isContinueUpload,
                           final UploadPartRequestFactory requestFactory, final int type) {
        ThreadPool.runOnUi(new Runnable() {

            @Override
            public void run() {
                Ks3Client client = getDefaultClient();
                client.listParts(request, new ListPartsResponseHandler() {

                    @Override
                    public void onSuccess(int statesCode, Header[] responceHeaders, ListPartsResult result, StringBuffer var4) {
                        if (isAborted()) {
                            request.abort();
                        } else {
                            if (isContinueUpload && requestFactory != null) {
                                if (null != result.getParts()) {
                                    int size = result.getParts().size();
                                    requestFactory.setHasUploadPart(size);
                                    uploadpart(requestFactory, att);
                                }
                            } else {
                                CompleteMultipartUploadRequest comRequest = new CompleteMultipartUploadRequest(result);
                                completeUploadPart(comRequest, att);
                            }
                        }
                    }

                    @Override
                    public void onFailure(int statesCode, Ks3Error ks3Error, Header[] responceHeaders,
                                          String response, Throwable throwable, StringBuffer var4) {
                        if (mUploadCallBack != null) {
                            mUploadCallBack.onFailure(statesCode, responceHeaders, response.getBytes(), throwable);
                            mUploadCallBack = null;
                        }
                        if (ks3Error != null) {
                            StatisticUtils.addToMiLinkMonitor(String.format(StatisticsKey.KEY_KS3_UPLOAD_SCLICE, getHost(att.getUrl())), ks3Error.getErrorCode(), att.getUrl());
                        } else {
                            StatisticUtils.addToMiLinkMonitor(String.format(StatisticsKey.KEY_KS3_UPLOAD_SCLICE, getHost(att.getUrl())), StatisticUtils.FAILED, att.getUrl());
                        }
                    }

                });
            }
        });
    }

    private void completeUploadPart(
            final CompleteMultipartUploadRequest request, final Attachment att) {
        ThreadPool.runOnUi(new Runnable() {
            @Override
            public void run() {
                Ks3Client client = getDefaultClient();
                client.completeMultipartUpload(request,
                        new CompleteMultipartUploadResponseHandler() {
                            @Override
                            public void onSuccess(int statesCode,
                                                  Header[] responceHeaders,
                                                  CompleteMultipartUploadResult result, StringBuffer var4) {
                                onUploadFinished(true, att.getUrl());
                                if (mUploadCallBack != null) {
                                    mUploadCallBack.onTaskSuccess(statesCode, responceHeaders, result, var4);
                                    mUploadCallBack = null;
                                }
                                StatisticUtils.addToMiLinkMonitor(String.format(StatisticsKey.KEY_KS3_UPLOAD_SCLICE, getHost(att.getUrl())), StatisticUtils.SUCCESS);
                            }

                            @Override
                            public void onFailure(int statesCode, Ks3Error ks3Error, Header[] responceHeaders,
                                                  String response, Throwable throwable, StringBuffer var4) {

                                onUploadFinished(false, response);
                                if (mUploadCallBack != null) {
                                    mUploadCallBack.onFailure(statesCode, responceHeaders, response.getBytes(), throwable);
                                    mUploadCallBack = null;
                                }
                                if (ks3Error != null) {
                                    StatisticUtils.addToMiLinkMonitor(String.format(StatisticsKey.KEY_KS3_UPLOAD_SCLICE, getHost(att.getUrl())), ks3Error.getErrorCode(), att.getUrl());
                                } else {
                                    StatisticUtils.addToMiLinkMonitor(String.format(StatisticsKey.KEY_KS3_UPLOAD_SCLICE, getHost(att.getUrl())), StatisticUtils.FAILED, att.getUrl());
                                }
                            }
                        });
            }
        });

    }

    private void onUploadFinished(boolean isSuccess, String resultStr) {
        if (sKs3ClientMap.containsKey(mMsgId)) {
            if (null != mUploadCallBack) {
                mUploadCallBack.onPostExecute(isSuccess, resultStr, getResourceId());
            }
        }
        if (null != mAtt && sFileInfoMap.containsKey(mAtt.getAttId())) {
            sFileInfoMap.remove(mAtt.getAttId());
        }
        if (sInitiateMultipartUploadResultMap.containsKey(mMsgId) && isSuccess) {
            sInitiateMultipartUploadResultMap.remove(mMsgId);
        }
    }

    private boolean isAborted() {
        return !sKs3ClientMap.containsKey(mMsgId);
    }

    public String getResourceId() {
        StringBuffer sb = new StringBuffer();
        sb.append(Ks3FileHost).append("/").append(mBucketName).append("/").append(mObjId);
        return sb.toString();
    }

    private static String getHost(String urlAsString) {
        try {
            URL url = new URL(urlAsString);
            return url.getHost();
        } catch (Exception e) {
            MyLog.e(e);
        }
        return "";
    }
}

