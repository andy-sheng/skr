package com.common.upload

import android.os.Bundle
import android.text.TextUtils
import com.alibaba.fastjson.JSON
import com.alibaba.sdk.android.oss.*
import com.alibaba.sdk.android.oss.callback.OSSCompletedCallback
import com.alibaba.sdk.android.oss.callback.OSSProgressCallback
import com.alibaba.sdk.android.oss.common.OSSLog
import com.alibaba.sdk.android.oss.common.auth.OSSStsTokenCredentialProvider
import com.alibaba.sdk.android.oss.common.utils.BinaryUtil
import com.alibaba.sdk.android.oss.internal.OSSAsyncTask
import com.alibaba.sdk.android.oss.model.ObjectMetadata
import com.alibaba.sdk.android.oss.model.PutObjectRequest
import com.alibaba.sdk.android.oss.model.PutObjectResult
import com.common.log.MyLog
import com.common.rxretrofit.ApiManager
import com.common.statistics.StatisticsAdapter
import com.common.utils.HandlerTaskTimer
import com.common.utils.U
import io.reactivex.Observable
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import top.zibin.luban.CompressionPredicate
import top.zibin.luban.Luban
import top.zibin.luban.OnCompressListener
import java.io.File
import java.io.IOException
import java.util.*

class UploadOssParams {
    var mOssSavaDir: String? = null
    var mBucketName: String? = null
    var mDir = ""
    var mCallbackUrl: String? = null
    var mCallbackBody: String? = null
    var mCallbackBodyType: String? = null
    var accessKeyId: String? = null
    var accessKeySecret: String? = null
    var securityToken: String? = null
    var endpoint: String? = null
    var recordTs = 0L
}

/**
 * 注意 upload 的callback 回调不一定是主线程
 */
class UploadTask(private val mUploadParams: UploadParams) {

    companion object {
        var lastUploadOssParams: UploadOssParams? = null

        init {
            if (MyLog.isDebugLogOpen()) {
                OSSLog.enableLog()  //调用此方法即可开启日志
            }
        }
    }

    val TAG = "UploadTask"
    private var ossParams = UploadOssParams()

    private var mOss: OSS? = null
    private var mTask: OSSAsyncTask<*>? = null
    private var mMonitorTimer: HandlerTaskTimer? = null

    fun startUpload(uploadCallback: UploadCallback?): UploadTask {
        val file = File(mUploadParams.filePath)
        if (file != null && file.exists()) {
            MyLog.w(TAG, "startUpload fileLength:" + file.length())
        } else {
            MyLog.e(TAG, "file==null 或者 文件不存在")
            uploadCallback?.onFailureNotInUiThread("文件不存在：" + file.absolutePath)
            return this
        }
        // 在移动端建议使用STS的方式初始化OSSClient，更多信息参考：[访问控制]
        val uploadAppServerApi = ApiManager.getInstance().createService(UploadAppServerApi::class.java)
        ossParams.mOssSavaDir = mUploadParams.fileType.ossSavaDir
        if (ossParams.mOssSavaDir == lastUploadOssParams?.mOssSavaDir) {
            val timeOk = (System.currentTimeMillis() - (lastUploadOssParams?.recordTs
                    ?: 0L) < 90 * 1000L)
            if (timeOk) {
                ossParams = lastUploadOssParams!!
                Observable.create<Bundle> {
                    whenGetOssParams(uploadCallback)
                    it.onComplete()
                }.subscribeOn(Schedulers.io())
                        .subscribe()
                return this
            }
        }
        uploadAppServerApi.getSTSToken(ossParams.mOssSavaDir)
                .subscribeOn(Schedulers.io())
                .subscribe(Consumer { data ->
                    val has = data.containsKey("statusCode")
                    if (!has) {
                        uploadCallback?.onFailureNotInUiThread("has == false")
                        return@Consumer
                    }
                    val code = data.getIntValue("statusCode")
                    if (code == 200) {
                        ossParams.accessKeyId = data.getString("accessKeyId")
                        ossParams.accessKeySecret = data.getString("accessKeySecret")
                        ossParams.securityToken = data.getString("securityToken")

                        val uploadParams = data.getJSONObject("uploadParams")
                        ossParams.endpoint = uploadParams.getString("endpoint")

                        ossParams.mBucketName = uploadParams.getString("bucketName")
                        ossParams.mDir = uploadParams.getString("dir")

                        val callback = uploadParams.getJSONObject("callback")
                        ossParams.mCallbackUrl = callback.getString("callbackUrl")
                        ossParams.mCallbackBody = callback.getString("callbackBody")
                        ossParams.mCallbackBodyType = callback.getString("callbackBodyType")
                        ossParams.recordTs = System.currentTimeMillis()
                        // 记录起这次的参数
                        lastUploadOssParams = ossParams
                        whenGetOssParams(uploadCallback)
                    }
                }, Consumer { throwable ->
                    MyLog.e(throwable)
                    uploadCallback?.onFailureNotInUiThread("Throwable")
                })
        return this
    }

    fun cancel() {
        MyLog.d(TAG, "cancel")
        mTask?.cancel()
    }

    private fun whenGetOssParams(uploadCallback: UploadCallback?) {
        val credentialProvider = OSSStsTokenCredentialProvider(ossParams.accessKeyId, ossParams.accessKeySecret, ossParams.securityToken)

        val conf = ClientConfiguration()
        conf.connectionTimeout = 15 * 1000 // 连接超时，默认15秒
        conf.socketTimeout = 15 * 1000 // socket超时，默认15秒
        conf.maxConcurrentRequest = 5 // 最大并发请求书，默认5个
        conf.maxErrorRetry = 2 // 失败后最大重试次数，默认2次

        mOss = OSSClient(U.app(), ossParams.endpoint, credentialProvider, conf)

        if (mUploadParams.isNeedCompress) {
            val fileName = U.getFileUtils().getFileNameFromFilePath(mUploadParams.filePath)
            val targetFileName = U.getAppInfoUtils().getFilePathInSubDir("upload", "temp_" + fileName!!)
            // 需要压缩
            Luban.with(U.app())
                    .load(mUploadParams.filePath)
                    .ignoreBy(100)
                    .setTargetDir(targetFileName)
                    .filter(CompressionPredicate { path ->
                        if (path.toLowerCase().endsWith(".gif")) {
                            return@CompressionPredicate false
                        }
                        if (path.toLowerCase().endsWith(".zip")) {
                            false
                        } else true
                    })
                    .setCompressListener(object : OnCompressListener {
                        override fun onStart() {

                        }

                        override fun onSuccess(file: File) {
                            MyLog.d(TAG, "压缩成功" + " file=" + file.absolutePath)
                            upload(file.absolutePath, uploadCallback)
                        }

                        override fun onError(e: Throwable) {
                            MyLog.d(TAG, "压缩失败")
                            upload(mUploadParams.filePath, uploadCallback)
                        }
                    })
                    .launch()
        } else {
            upload(mUploadParams.filePath, uploadCallback)
        }
    }

    private fun upload(filePath: String, uploadCallback: UploadCallback?) {
        val uploadStartMs = System.currentTimeMillis()
        val request = createRequest(filePath)

        request.progressCallback = OSSProgressCallback { request, currentSize, totalSize ->
            uploadCallback?.onProgressNotInUiThread(currentSize, totalSize)
        }

        mTask?.cancel()
        if (mUploadParams.isNeedMonitor) {
            startMonitor(uploadCallback)
        }
        mTask = mOss?.asyncPutObject(request, object : OSSCompletedCallback<PutObjectRequest, PutObjectResult> {
            override fun onSuccess(request: PutObjectRequest, result: PutObjectResult) {
                cancelMonitor("onSuccess")
                if (uploadCallback != null) {
                    // 只有设置了servercallback，这个值才有数据
                    val serverCallbackReturnJson = result.serverCallbackReturnBody
                    MyLog.w(TAG, "serverCallbackReturnJson:$serverCallbackReturnJson")
                    val jo = JSON.parseObject(serverCallbackReturnJson)
                    val url = jo.getString("url")
                    if (!TextUtils.isEmpty(url)) {
                        uploadCallback.onSuccessNotInUiThread(url)
                        // 上传成功打点
                        StatisticsAdapter.recordCalculateEvent("upload", "success", System.currentTimeMillis() - uploadStartMs, null)
                    } else {
                        uploadCallback.onFailureNotInUiThread("上传失败")
                        // 上传失败打点
                        val param = HashMap<String, String>()
                        param["reason"] = "url为空"
                        StatisticsAdapter.recordCalculateEvent("upload", "failed", System.currentTimeMillis() - uploadStartMs, param)
                    }
                }

            }

            override fun onFailure(request: PutObjectRequest, clientException: ClientException?, serviceException: ServiceException?) {
                cancelMonitor("onFailure")
                if (uploadCallback != null) {
                    val sb = StringBuilder()
                    if (serviceException != null) {
                        sb.append("error=").append(serviceException.errorCode).append(" serverMsg=").append(serviceException.message)
                    }
                    if (clientException != null) {
                        sb.append(" clientMsg=").append(clientException.message)
                    }
                    uploadCallback.onFailureNotInUiThread(sb.toString())
                    // 上传失败打点
                    val param = HashMap<String, String>()
                    param["reason"] = sb.toString()
                    StatisticsAdapter.recordCalculateEvent("upload", "failed", System.currentTimeMillis() - uploadStartMs, param)
                }

            }
        })
    }


    /**
     * 创建普通上传
     *
     * @return
     */
    private fun createRequest(filePath: String): PutObjectRequest {
        val mObjectId: String
        if (ossParams.mDir.isNotEmpty() && !ossParams.mDir.endsWith("/")) {
            ossParams.mDir += "/"
        }
        if (TextUtils.isEmpty(mUploadParams.fileName)) {
            val ext = U.getFileUtils().getSuffixFromFilePath(filePath)
            var fileName = U.getMD5Utils().MD5_16(System.currentTimeMillis().toString() + filePath)
            if (!TextUtils.isEmpty(ext)) {
                fileName = "$fileName.$ext"
            }
            if (TextUtils.isEmpty(ossParams.mDir)) {
                mObjectId = mUploadParams.fileType.ossSavaDir + fileName
            } else {
                mObjectId = ossParams.mDir + fileName
            }
        } else {
            mObjectId = ossParams.mDir + mUploadParams.fileName
        }
        // 构造上传请求
        val put = PutObjectRequest(ossParams.mBucketName, mObjectId, filePath)

        // 文件元信息的设置是可选的
        val metadata = ObjectMetadata()
        /**
         * 在Web服务中Content-Type用来设定文件的类型，决定以什么形式、什么编码读取这个文件。
         * 某些情况下，对于上传的文件需要设定Content-Type，否则文件不能以自己需要的形式和编码来读取。
         * 使用SDK上传文件时，如果不指定Content-Type，SDK会帮您根据后缀自动添加Content-Type。
         */
        //       metadata.setContentType("application/octet-stream"); // 设置content-type
        try {
            metadata.contentMD5 = BinaryUtil.calculateBase64Md5(filePath) // 校验MD5
        } catch (e: IOException) {
            e.printStackTrace()
        }

        put.metadata = metadata

        put.callbackParam = object : HashMap<String, String?>() {
            init {
                put("callbackUrl", ossParams.mCallbackUrl)
                //                put("callbackHost", "oss-cn-hangzhou.aliyuncs.com");
                put("callbackBodyType", ossParams.mCallbackBodyType)
                put("callbackBody", ossParams.mCallbackBody)
            }
        }
        //        put.setCallbackVars(new HashMap<String, String>() {
        //            {
        //                put("x:var1", "value1");
        //                put("x:var2", "value2");
        //            }
        //        });
        return put
    }

    /**
     * 开启监听器，因为有可能失败但是两个回调都不走
     * 对于一些特别依赖回调的业务可以开启
     */
    internal fun startMonitor(uploadCallback: UploadCallback?) {
        MyLog.d(TAG, "startMonitor")
        cancelMonitor("startMonitor")
        mMonitorTimer = HandlerTaskTimer.newBuilder().interval(5000)
                .take(15)
                .start(object : HandlerTaskTimer.ObserverW() {
                    var num = 0

                    override fun onNext(integer: Int) {
                        MyLog.d(TAG, "onNext integer=$integer")
                        num++
                        if (!U.getNetworkUtils().hasNetwork()) {
                            uploadCallback?.onFailureNotInUiThread("没有网络了")
                            return
                        }
                        if (num >= 10) {
                            uploadCallback?.onFailureNotInUiThread("上传很久了，还没有回调，认为失败")
                        }
                    }
                })
    }

    internal fun cancelMonitor(from: String) {
        MyLog.d(TAG, "cancelMonitor from=$from")
        mMonitorTimer?.dispose()
    }

}
