package com.imagebrowse

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.Animatable
import android.net.Uri
import android.os.Handler
import android.text.TextUtils
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView

import com.common.base.R
import com.common.image.fresco.BaseImageView
import com.common.image.fresco.FrescoWorker
import com.common.image.fresco.IFrescoCallBack
import com.common.image.fresco.cache.MyCacheKeyFactory
import com.common.image.model.BaseImage
import com.common.image.model.HttpImage
import com.common.image.model.ImageFactory
import com.common.image.model.LocalImage
import com.common.log.MyLog
import com.common.utils.FileUtils
import com.common.utils.HttpUtils
import com.common.utils.StringUtils
import com.common.utils.U
import com.davemorrissey.labs.subscaleview.ImageSource
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView
import com.facebook.binaryresource.FileBinaryResource
import com.facebook.drawee.drawable.ScalingUtils
import com.facebook.imagepipeline.core.ImagePipelineFactory
import com.facebook.imagepipeline.image.ImageInfo
import com.facebook.imagepipeline.image.QualityInfo
import com.facebook.imagepipeline.request.ImageRequest

import java.io.File
import java.io.IOException

import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.ObservableOnSubscribe
import io.reactivex.schedulers.Schedulers
import pl.droidsonroids.gif.GifDrawable
import pl.droidsonroids.gif.GifImageView

/**
 * 这个view过于复杂，在使用viewpager时就别重复使用了吧
 * 职责：保证清晰流畅的显示出图片 不管多大
 */
open class EnhancedImageView : RelativeLayout {

    val TAG = "EnhancedImageView"

    protected var mPhotoDraweeView: BaseImageView? = null
    protected var mGifImageView: GifImageView? = null
    protected var mGifFromFile: GifDrawable? = null
    protected var mSubsamplingScaleImageView: SubsamplingScaleImageView? = null

    protected var mLongClickListener: View.OnLongClickListener? = null //长按事件的监听
    protected var mClickListener: View.OnClickListener? = null //点击事件的监听

    internal var mDebugLogView: TextView? = null

    protected var mUiHandler = Handler()

    protected var mBaseImage: BaseImage? = null

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
    }

    override fun setOnClickListener(l: View.OnClickListener?) {
        this.mClickListener = l
    }

    override fun setOnLongClickListener(l: View.OnLongClickListener?) {
        this.mLongClickListener = l
    }

    override fun onDetachedFromWindow() {
        MyLog.d(TAG, "onDetachedFromWindow ")
        super.onDetachedFromWindow()
        recycleResWhenDetachedFromWindow()
    }

    open fun recycleResWhenDetachedFromWindow() {
        mUiHandler.removeCallbacksAndMessages(null)
        mSubsamplingScaleImageView?.recycle()
        if (mBaseImage != null) {
            val uri = mBaseImage!!.uri
            if (uri != null) {
                val path = uri.toString()
                if (path.startsWith("https://") || path.startsWith("http://")) {
                    if (path.endsWith(".gif")) {
                        U.getHttpUtils().cancelDownload(path)
                    }
                }
            }
        }
        mGifFromFile?.recycle()
    }

    fun getCurDisplayView(): View? {
        if (mPhotoDraweeView?.visibility == View.VISIBLE) {
            return mPhotoDraweeView
        }
        if (mGifImageView?.visibility == View.VISIBLE) {
            return mGifImageView
        }
        if (mSubsamplingScaleImageView?.visibility == View.VISIBLE) {
            return mSubsamplingScaleImageView
        }
        return null
    }

    private fun init() {
    }

    protected open fun useSubSampleView(): Boolean {
        return false
    }

    fun load(path: String?) {

        if (path == null) {
            return
        }
        MyLog.d(TAG, "load path=$path")
        if (mDebugLogView == null && MyLog.isDebugLogOpen()) {
            mDebugLogView = TextView(context)
            val lp = RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 300)
            lp.topMargin = 400
            mDebugLogView!!.setTextColor(Color.RED)
            mDebugLogView!!.gravity = RelativeLayout.CENTER_IN_PARENT
            addView(mDebugLogView, lp)
        }
        if (mDebugLogView != null) {
            mDebugLogView!!.text = "path=$path"
        }

        val baseImage = ImageFactory.newPathImage(path)
                .setScaleType(ScalingUtils.ScaleType.FIT_CENTER)
                .setFailureDrawable(U.app().resources.getDrawable(R.drawable.load_img_error))
                .setLoadingDrawable(U.app().resources.getDrawable(R.drawable.loading_place_holder_img))
                //                    .setProgressBarDrawable(new ImageBrowseProgressBar())
                .setTapToRetryEnabled(true)
                //                    .setOssProcessors(OssPsFactory.newResizeBuilder().setW(360).build(),OssPsFactory.newCropBuilder().setH(180).build())
                .build<BaseImage>()
        load(baseImage)
    }

    fun load(baseImage: BaseImage?) {
        if (baseImage == null || baseImage.uri == null) {
            return
        }
        var path: String? = baseImage.uri.toString()
        if (path!!.startsWith("http://") || path.startsWith("https://")) {
            val uri = Uri.parse(path)
            if (uri.path!!.endsWith(".gif")) {
                // gif直接走自有逻辑
                downloadGiftByHttpUtils(baseImage as HttpImage)
            } else {
                //其余情况，先用fresco渐变加载，保证体验
                loadHttpByFresco(baseImage)
            }
        } else {
            //本地
            path = baseImage.uri.path
            val fileType = U.getFileUtils().getImageFileType(path)
            if (!TextUtils.isEmpty(fileType) && fileType == "gif") {
                loadByGif(path, baseImage)
            } else {
                loadLocalByFresco(baseImage)
            }
        }
    }

    protected open fun createFrescoView() {
        mPhotoDraweeView = BaseImageView(context)
    }

    private fun showFrescoViewIfNeed() {
        addLog("showFrescoViewIfNeed width=$width height=$height")
        mGifImageView?.visibility = View.GONE
        if (mPhotoDraweeView == null) {
            createFrescoView()
            val lp = RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
//            mPhotoDraweeView!!.layout(0,0,width,height)
            addView(mPhotoDraweeView, 0, lp)
            mPhotoDraweeView!!.setOnLongClickListener(this.mLongClickListener)
            mPhotoDraweeView!!.setOnClickListener(this.mClickListener)
        }
        mPhotoDraweeView?.visibility = View.VISIBLE
    }

    private fun showGifViewIfNeed() {
        addLog("showGifViewIfNeed width=$width height=$height")
        mPhotoDraweeView?.visibility = View.GONE
        if (mGifImageView == null) {
            mGifImageView = GifImageView(context)
            val lp = RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            addView(mGifImageView, 0, lp)
            mGifImageView!!.setOnLongClickListener(this.mLongClickListener)
            mGifImageView!!.setOnClickListener {
                // 直接设置mClickListener 也没生效 不知道为啥
                this.mClickListener?.onClick(it)
            }
        }

        mGifImageView?.visibility = View.VISIBLE
    }

    private fun showSubSampleViewIfNeed() {
        addLog("showSubSampleViewIfNeed")
        mGifImageView?.visibility = View.GONE
        if (mSubsamplingScaleImageView == null) {
            mSubsamplingScaleImageView = SubsamplingScaleImageView(context)
            val lp = RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            addView(mSubsamplingScaleImageView, 0, lp)
            mSubsamplingScaleImageView!!.setOnLongClickListener(this.mLongClickListener)
            mSubsamplingScaleImageView!!.setOnClickListener(this.mClickListener)
        }
        mSubsamplingScaleImageView?.visibility = View.VISIBLE
    }

    private fun loadHttpByFresco(httpImage: BaseImage?) {
        if (httpImage == null) {
            return
        }
        showFrescoViewIfNeed()
        val preCallback = httpImage.callBack
        httpImage.callBack = object : IFrescoCallBack {
            override fun processWithInfo(info: ImageInfo?, animatable: Animatable?) {
                preCallback?.processWithInfo(info, animatable)
                if (!useSubSampleView()) {
                    return
                }
                // 加载完成
                val file = FrescoWorker.getCacheFileFromFrescoDiskCache(httpImage.uri)
                if (file != null && file.exists()) {
                    val wh = U.getImageUtils().getImageWidthAndHeightFromFile(file.absolutePath)

                    addLog("load processWithInfo wh " + wh[0] + " " + wh[1])
                    // 如果是图特别大，用 subsample加载
                    val b1 = wh[0] != 0 && wh[0] > U.getDisplayUtils().screenWidth * 1.5
                    val b2 = wh[1] != 0 && wh[1] > U.getDisplayUtils().screenHeight * 1.5
                    if (b1 || b2) {
                        loadBySubSampleView(file.absolutePath)
                    }
                }
            }

            override fun processWithFailure() {
                preCallback?.processWithFailure()
            }
        }
//        FrescoWorker.preLoadImg(httpImage as HttpImage?, object : FrescoWorker.ImageLoadCallBack {
//
//            override fun loadSuccess(bitmap: Bitmap?) {
//                MyLog.d(TAG, "loadSuccess bitmap=$bitmap")
//            }
//
//            override fun onProgressUpdate(progress: Float) {
//                //显示下载进度条
//                //MyLog.d(TAG, "onProgressUpdate" + " progress=" + progress);
//            }
//
//            override fun loadFail() {
//                MyLog.d(TAG, "loadFail")
//            }
//        }, true)
        realLoadByFresco(httpImage)
    }

    private fun loadLocalByFresco(localImage: BaseImage?) {
        if (localImage == null) {
            return
        }
        showFrescoViewIfNeed()
        val preCallback = localImage.callBack

        localImage.callBack = object : IFrescoCallBack {
            override fun processWithInfo(info: ImageInfo?, animatable: Animatable?) {
                preCallback?.processWithInfo(info, animatable)
                if (!useSubSampleView()) {
                    return
                }
                // 加载完成
                val file = File(localImage.uri.path!!)
                if (file != null && file.exists()) {
                    val wh = U.getImageUtils().getImageWidthAndHeightFromFile(file.absolutePath)

                    MyLog.d(TAG, "load processWithInfo wh " + wh[0] + " " + wh[1])
                    // 如果是图特别大，用 subsample加载
                    val b1 = wh[0] != 0 && wh[0] > U.getDisplayUtils().screenWidth * 1.5
                    val b2 = wh[1] != 0 && wh[1] > U.getDisplayUtils().screenHeight * 1.5
                    if (b1 || b2) {
                        loadBySubSampleView(file.absolutePath)
                    } else {
                        MyLog.d(TAG, "no need SubSampleView")
                    }
                }
            }

            override fun processWithFailure() {
                preCallback?.processWithFailure()
            }
        }
        realLoadByFresco(localImage)
    }

    protected open fun realLoadByFresco(baseImage: BaseImage) {
        FrescoWorker.loadImage(mPhotoDraweeView, baseImage)
    }

    private fun loadBySubSampleView(localFilePath: String) {
        showSubSampleViewIfNeed()
        mSubsamplingScaleImageView!!.setOnImageEventListener(object : SubsamplingScaleImageView.DefaultOnImageEventListener() {
            override fun onPreviewLoadError(e: Exception?) {
                // 改用fresco
                addLog(" subSampleTouchView onPreviewLoadError")
            }

            override fun onImageLoadError(e: Exception?) {
                addLog(" subSampleTouchView onPreviewLoadError")
            }

            override fun onTileLoadError(e: Exception?) {
                addLog(" subSampleTouchView onPreviewLoadError")
            }

            override fun onReady() {

            }

            override fun onImageLoaded() {
                addLog(" subSampleTouchView onImageLoaded")
                if (mPhotoDraweeView != null) {
                    mPhotoDraweeView!!.visibility = View.GONE
                }
            }
        })
        //竖长图
        //        MyLog.d(TAG,"loadBySubSampleView degree=" + U.getBitmapUtils().getBitmapDegree(localFilePath)+ " localFilePath:"+localFilePath);
        mSubsamplingScaleImageView!!.orientation = U.getBitmapUtils().getBitmapDegree(localFilePath)
        mSubsamplingScaleImageView!!.setImage(ImageSource.uri(localFilePath))
    }

//    private fun getGifSaveFile(baseImage: BaseImage): File {
//        /**
//         * 保证
//         * http://song-static.inframe.mobi/posts/1604228/0490bcb1370da5a0.gif 与
//         * http://song-static.inframe.mobi/posts/1604228/0490bcb1370da5a0.gif?xxxx
//         * 的key相同
//         * 存在fresco 加载 gif 时相同的目录
//         */
//        //先试试原图是否加载了
//        val request = ImageRequest.fromUri(baseImage.uri)
//        val cacheKey = MyCacheKeyFactory.getEncodedCacheKey(request, null)
//        val resource = ImagePipelineFactory.getInstance().mainFileCache.getResource(cacheKey)
//        val cacheFile = (resource as FileBinaryResource).file
//        MyLog.d(TAG, "getGifSaveFile baseImage = $baseImage cacheFile=${cacheFile.path}")
//        return cacheFile
//    }

    //下载gif
    private fun downloadGiftByHttpUtils(baseImage: HttpImage) {
        MyLog.d(TAG, "downloadGiftByHttpUtils")
        FrescoWorker.preLoadImg(baseImage, object : FrescoWorker.ImageLoadCallBack {
            override fun loadSuccess(bitmap: Bitmap?) {
                val file = FrescoWorker.getCacheFileFromFrescoDiskCache(baseImage.uri)
                if (file != null) {
                    loadByGif(file.path, baseImage)
                } else {
                    loadHttpByFresco(baseImage)
                }
            }

            override fun onProgressUpdate(progress: Float) {

            }

            override fun loadFail() {
                addLog("download Gif failed,turn fresco load gif")
                loadHttpByFresco(baseImage)
            }

        }, false)
//        val file = getGifSaveFile(baseImage)
//        if (file.exists()) {
//            //已经有了，不需要下载
//            loadByGif(file.absolutePath, baseImage)
//            return
//        }
//        Observable.create(ObservableOnSubscribe<Any> { emitter ->
//            U.getHttpUtils().downloadFileSync(baseImage.uri.toString(), getGifSaveFile(baseImage), true, object : HttpUtils.OnDownloadProgress {
//                override fun onDownloaded(downloaded: Long, totalLength: Long) {
//                    MyLog.d(TAG, "onDownloaded downloaded=$downloaded totalLength=$totalLength")
//                }
//
//                override fun onCompleted(localPath: String) {
//                    MyLog.d(TAG, "onCompleted localPath=$localPath")
//                    mUiHandler.post { loadByGif(localPath, baseImage) }
//                }
//
//                override fun onCanceled() {
//                    MyLog.d(TAG, "onCanceled")
//                    mUiHandler.post { loadHttpByFresco(baseImage) }
//                }
//
//                override fun onFailed() {
//                    MyLog.d(TAG, "onFailed")
//                    mUiHandler.post { loadHttpByFresco(baseImage) }
//                }
//            })
//            emitter.onComplete()
//        }).subscribeOn(Schedulers.io())
//                .subscribe()

    }

    private fun loadByGif(localFilePath: String, baseImage: BaseImage?) {
        addLog("loadByGif localFile=$localFilePath")
        // 如果是 gif ,直接用android-gif-drawable 加载,不废话了
        showGifViewIfNeed()
        mGifFromFile?.recycle()
        try {
            mGifFromFile = GifDrawable(localFilePath!!)
            val width = mGifFromFile?.intrinsicWidth
            val height = mGifFromFile?.intrinsicHeight
            val imageInfo = object : ImageInfo {
                override fun getHeight(): Int {
                    return height ?: 0
                }

                override fun getQualityInfo(): QualityInfo? {
                    return null
                }

                override fun getWidth(): Int {
                    return width ?: 0
                }
            }
            baseImage?.callBack?.processWithInfo(imageInfo, null)
            if (!mGifFromFile!!.isRecycled) {
                mGifImageView!!.setImageDrawable(mGifFromFile)
            }
        } catch (e: IOException) {
            // 失败了
            loadLocalByFresco(mBaseImage)
        }

    }

    internal fun addLog(text: String) {
        MyLog.d(TAG, text)
        if (mDebugLogView != null) {
            mDebugLogView!!.text = mDebugLogView!!.text.toString() + "\n" + text
        }
    }
}
