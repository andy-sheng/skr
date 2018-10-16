package com.common.core.upload;

import android.text.TextUtils;

import com.common.log.MyLog;
import com.common.utils.U;
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

import org.apache.http.Header;

import java.io.File;
import java.util.concurrent.ConcurrentHashMap;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;

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


    public static final String AccessControl_Private = "private";
    public static final String AccessControl_PublicRead = "public-read";
    public static final String AccessControl_PublicReadWrite = "public-read-write";

    public static final String Ks3FileHost = "http://kssws.ks-cdn.com";

    public ConcurrentHashMap<Long, InitiateMultipartUploadResult> sInitiateMultipartUploadResultMap = new ConcurrentHashMap<Long, InitiateMultipartUploadResult>();
    public ConcurrentHashMap<Long, Ks3Client> sKs3ClientMap = new ConcurrentHashMap<Long, Ks3Client>();

    private UploadUtils.UploadParams mUploadParams; //上传参数
    private CannedAccessControlList mList;  //权限
    private String mToken;   //token
    private String authDate;    //服务器时间戳
    private long mMsgId;
    private UploadCallBack mUploadCallBack;

    private Ks3Client mKs3Client;

    public Ks3FileUploader(UploadUtils.UploadParams uploadParams, long msgId, String ks3AuthToken, String acl, String authDate, UploadCallBack uploadCallBack) {
        this.mUploadParams = uploadParams;
        this.mMsgId = msgId;
        this.mToken = ks3AuthToken;
        this.mList = generateObjectAcl(acl);
        this.authDate = authDate;
        this.mUploadCallBack = uploadCallBack;
    }

    private CannedAccessControlList generateObjectAcl(String acl) {
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
        // 参数无效
        if (null == mUploadParams || TextUtils.isEmpty(mUploadParams.getLocalPath())) {
            if (mUploadCallBack != null) {
                MyLog.w(TAG, "upload file failed, local path error");
            }
            return false;
        }

        // 文件不存在或不是一个文件
        File file = new File(mUploadParams.getLocalPath());
        if (!file.exists() || !file.isFile() || file.length() == 0) {
            if (mUploadCallBack != null) {
                MyLog.w(TAG, "upload file failed, local path file error");
            }
            return false;
        }

        // 根据指定的文件大小，选择用直接上传或者分块上传
        if (file.length() >= LARGE_FILE_SIZE) {
            PART_SIZE = LARGE_PAGE_SIZE;
        } else {
            PART_SIZE = SMALL_PAGE_SIZE;
        }
        MyLog.v(TAG, "Ks3FileUploader PART_SIZE=" + PART_SIZE);

        if (file.length() >= MULTI_UPLOAD_THREADHOLD) {
            doMultipartUpload(mUploadParams, getDefaultClient());
        } else {
            doSingleUpload(mUploadParams, getDefaultClient());
        }
        return true;
    }

    //上传单片文件
    private void doSingleUpload(final UploadUtils.UploadParams uploadParams, final Ks3Client client) {
        if (null == uploadParams || TextUtils.isEmpty(uploadParams.getLocalPath()) || !(new File(uploadParams.getLocalPath()).exists())) {
            MyLog.v(TAG, "failed to upload att because there is no res file or no client");
            return;
        }
        MyLog.v(TAG, "upload bucketName = " + uploadParams.toString() + " client = " + client.toString());
        Observable.create(new ObservableOnSubscribe<Object>() {
            @Override
            public void subscribe(ObservableEmitter<Object> emitter) throws Exception {
                final PutObjectRequest request = new PutObjectRequest(uploadParams.getBucketName(), uploadParams.getObjectKey(), new File(uploadParams.getLocalPath()));
                request.setCannedAcl(mList);
                request.setHttpMethod(com.ksyun.ks3.model.HttpMethod.PUT);
                request.setContentType(uploadParams.getMimeType());
                MyLog.d(TAG, "upload att.getMimeType() = " + uploadParams.getMimeType());
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
                            onUploadFinished(true, mUploadParams.getUrl());
                            if (mUploadCallBack != null) {
                                mUploadCallBack.onTaskSuccess();
                                mUploadCallBack = null;
                            }
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
                            onUploadFinished(false, response);
                            if (mUploadCallBack != null) {
                                mUploadCallBack.onTaskFailure();
                                mUploadCallBack = null;
                            }
                        }

                    }
                });
                emitter.onComplete();
            }
        }).subscribeOn(AndroidSchedulers.mainThread())
                .subscribe();
    }

    //上传分片文件
    private void doMultipartUpload(final UploadUtils.UploadParams uploadParams, final Ks3Client client) {
        if (null == client || null == uploadParams || TextUtils.isEmpty(uploadParams.getLocalPath()) || !(new File(uploadParams.getLocalPath()).exists())) {
            MyLog.v(TAG, "failed to upload att because there is no res file or no client");
            return;
        }
        MyLog.v(TAG, "upload bucketName = " + uploadParams.toString() + " client = " + client.toString());

        if (sInitiateMultipartUploadResultMap.containsKey(mMsgId)) {
            InitiateMultipartUploadResult initResult = sInitiateMultipartUploadResultMap.get(mMsgId);
            UploadPartRequestFactory requestFactory = new UploadPartRequestFactory(
                    initResult.getBucket(),
                    initResult.getKey(),
                    initResult.getUploadId(),
                    new File(uploadParams.getLocalPath()),
                    PART_SIZE);
            ListPartsRequest listRequest = new ListPartsRequest(
                    requestFactory.getBucketName(),
                    requestFactory.getObjectKey(),
                    requestFactory.getUploadId());
            listParts(listRequest, uploadParams, true, requestFactory);
            return;
        }

        Observable.create(new ObservableOnSubscribe<Object>() {
            @Override
            public void subscribe(ObservableEmitter<Object> emitter) throws Exception {
                final InitiateMultipartUploadRequest request;
                request = new InitiateMultipartUploadRequest(uploadParams.getBucketName(), uploadParams.getObjectKey());
                request.setCannedAcl(mList);
                request.setHttpMethod(HttpMethod.POST);
                request.setContentType(uploadParams.getMimeType());
                final long startTime = System.currentTimeMillis();
                client.initiateMultipartUpload(request,
                        new InitiateMultipartUploadResponceHandler() {
                            @Override
                            public void onSuccess(int statesCode, Header[] responceHeaders, InitiateMultipartUploadResult result, StringBuffer var4) {
                                beginMultiUpload(result, uploadParams);
                            }

                            @Override
                            public void onFailure(int statesCode, Ks3Error ks3Error, Header[] responceHeaders,
                                                  String response, Throwable throwable, StringBuffer var4) {

                            }
                        });
                emitter.onComplete();

            }
        }).subscribeOn(AndroidSchedulers.mainThread())
                .subscribe();

    }

    private void beginMultiUpload(InitiateMultipartUploadResult initResult, UploadUtils.UploadParams uploadParams) {
        sInitiateMultipartUploadResultMap.put(mMsgId, initResult);
        UploadPartRequestFactory localUploadPartRequestFactory = new UploadPartRequestFactory(
                initResult.getBucket(), initResult.getKey(),
                initResult.getUploadId(), new File(uploadParams.getLocalPath()), PART_SIZE);
        uploadpart(localUploadPartRequestFactory, uploadParams);
    }

    private void uploadpart(final UploadPartRequestFactory requestFactory, final UploadUtils.UploadParams uploadParams) {
        if (requestFactory == null) {
            return;
        }
        if (requestFactory.hasMoreRequests()) {
            Observable.create(new ObservableOnSubscribe<Object>() {
                @Override
                public void subscribe(ObservableEmitter<Object> emitter) throws Exception {
                    Ks3Client client = getDefaultClient();
                    final UploadPartRequest request = requestFactory.getNextUploadPartRequest();
                    request.setCannedAcl(mList);
                    request.setHttpMethod(HttpMethod.PUT);
                    request.setContentType(uploadParams.getMimeType());

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
                                uploadpart(requestFactory, uploadParams);
                            }
                        }

                        @Override
                        public void onFailure(int statesCode, Ks3Error ks3Error, Header[] responceHeaders,
                                              String response, Throwable throwable, StringBuffer var4) {
                            if (mUploadCallBack != null) {
                                mUploadCallBack.onTaskFailure();
                                mUploadCallBack = null;
                            }
                        }

                    });
                    emitter.onComplete();
                }
            }).subscribeOn(AndroidSchedulers.mainThread())
                    .subscribe();
        } else {
            ListPartsRequest listRequest = new ListPartsRequest(
                    requestFactory.getBucketName(),
                    requestFactory.getObjectKey(),
                    requestFactory.getUploadId());
            listParts(listRequest, uploadParams, false, null);
        }
    }

    private void listParts(final ListPartsRequest request, final UploadUtils.UploadParams up, final boolean isContinueUpload,
                           final UploadPartRequestFactory requestFactory) {
        Observable.create(new ObservableOnSubscribe<Object>() {
            @Override
            public void subscribe(ObservableEmitter<Object> emitter) throws Exception {
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
                                    uploadpart(requestFactory, up);
                                }
                            } else {
                                CompleteMultipartUploadRequest comRequest = new CompleteMultipartUploadRequest(result);
                                completeUploadPart(comRequest, up);
                            }
                        }

                    }

                    @Override
                    public void onFailure(int statesCode, Ks3Error ks3Error, Header[] responceHeaders,
                                          String response, Throwable throwable, StringBuffer var4) {

                        if (mUploadCallBack != null) {
                            mUploadCallBack.onTaskFailure();
                            mUploadCallBack = null;
                        }
                    }
                });
                emitter.onComplete();
            }
        }).subscribeOn(AndroidSchedulers.mainThread())
                .subscribe();
    }

    private void completeUploadPart(
            final CompleteMultipartUploadRequest request, final UploadUtils.UploadParams up) {
        Observable.create(new ObservableOnSubscribe<Object>() {
            @Override
            public void subscribe(ObservableEmitter<Object> emitter) throws Exception {
                Ks3Client client = getDefaultClient();
                client.completeMultipartUpload(request,
                        new CompleteMultipartUploadResponseHandler() {
                            @Override
                            public void onSuccess(int statesCode,
                                                  Header[] responceHeaders,
                                                  CompleteMultipartUploadResult result, StringBuffer var4) {
                                onUploadFinished(true, up.getUrl());
                                if (mUploadCallBack != null) {
                                    mUploadCallBack.onTaskSuccess();
                                    mUploadCallBack = null;
                                }
                            }

                            @Override
                            public void onFailure(int statesCode, Ks3Error ks3Error, Header[] responceHeaders,
                                                  String response, Throwable throwable, StringBuffer var4) {

                                onUploadFinished(false, response);
                                if (mUploadCallBack != null) {
                                    mUploadCallBack.onTaskFailure();
                                    mUploadCallBack = null;
                                }
                            }
                        });
            }
        }).subscribeOn(AndroidSchedulers.mainThread())
                .subscribe();
    }


    public Ks3Client getDefaultClient() {
        if (mKs3Client == null) {
            if (sKs3ClientMap.containsKey(mMsgId)) {
                mKs3Client = sKs3ClientMap.get(mMsgId);
            } else {
                mKs3Client = new Ks3Client(new AuthListener() {
                    @Override
                    public AuthResult onCalculateAuth(String httpMethod, String contentType,
                                                      String date, String contentMD5,
                                                      String Resource, String Headers) {
                        // 此处应由APP端向业务服务器发送post请求返回Token。需要注意该回调方法运行在非主线程
                        File file = new File(mUploadParams.getLocalPath());
                        if (file.length() >= Ks3FileUploader.MULTI_UPLOAD_THREADHOLD) {
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

                            String multipartKs3AuthToken = UploadServerApi.getMultipartKs3AuthToken(System.currentTimeMillis(), Resource == null ? "" : Resource, date == null ? "" : date
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
                }, U.app());

                // todo 更具编译的版本选择
//            if (Constants.isTestBuild) {
//                mKs3Client.setEndpoint("ks3-cn-beijing.ksyun.com");
//            } else {
//                mKs3Client.setEndpoint("ul.zb.mi.com");
//            }
                mKs3Client.setEndpoint("ks3-cn-beijing.ksyun.com");
                mKs3Client.setConfiguration(Ks3ClientConfiguration.getDefaultConfiguration());

                sKs3ClientMap.put(mMsgId, mKs3Client);
            }
        }
        return mKs3Client;
    }

    private boolean isAborted() {
        return !sKs3ClientMap.containsKey(mMsgId);
    }

    private void onUploadFinished(boolean isSuccess, String resultStr) {
        MyLog.w(TAG, " onUploadFinished isSuccess =" + isSuccess + " resultStr = " + resultStr);
        if (sKs3ClientMap.containsKey(mMsgId)) {
            if (null != mUploadCallBack) {
                if (isSuccess) {
                    mUploadCallBack.onTaskSuccess();
                } else {
                    mUploadCallBack.onTaskFailure();
                }
            }
        }

        if (sInitiateMultipartUploadResultMap.containsKey(mMsgId) && isSuccess) {
            sInitiateMultipartUploadResultMap.remove(mMsgId);
        }
    }
}

