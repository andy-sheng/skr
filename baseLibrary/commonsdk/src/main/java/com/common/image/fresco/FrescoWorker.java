package com.common.image.fresco;

import android.app.ActionBar;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.Animatable;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.content.CursorLoader;
import android.text.TextUtils;
import android.view.ViewGroup;

import com.common.base.R;
import com.common.image.fresco.cache.MLCacheKeyFactory;
import com.common.image.fresco.processor.BlurPostprocessor;
import com.common.image.model.BaseImage;
import com.common.image.model.HttpImage;
import com.common.image.model.ImageFactory;
import com.common.log.MyLog;
import com.common.utils.U;
import com.facebook.binaryresource.BinaryResource;
import com.facebook.binaryresource.FileBinaryResource;
import com.facebook.cache.common.CacheKey;
import com.facebook.common.executors.CallerThreadExecutor;
import com.facebook.common.executors.UiThreadImmediateExecutorService;
import com.facebook.common.references.CloseableReference;
import com.facebook.datasource.DataSource;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.backends.pipeline.PipelineDraweeControllerBuilder;
import com.facebook.drawee.controller.BaseControllerListener;
import com.facebook.drawee.drawable.ScalingUtils;
import com.facebook.drawee.generic.RoundingParams;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.common.ImageDecodeOptions;
import com.facebook.imagepipeline.common.ResizeOptions;
import com.facebook.imagepipeline.core.ImagePipeline;
import com.facebook.imagepipeline.core.ImagePipelineFactory;
import com.facebook.imagepipeline.datasource.BaseBitmapDataSubscriber;
import com.facebook.imagepipeline.image.CloseableImage;
import com.facebook.imagepipeline.image.ImageInfo;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;

import java.io.File;

/**
 * Created by lan on 15-12-9.
 * 逐步完善
 */
public class FrescoWorker {
    public final static String TAG = "FrescoWorker";

    /**
     * 异步得到bitmap
     *
     * @param baseImage
     * @param loadCallBack
     */
    public static void getBitmapFromImage(BaseImage baseImage, ImageLoadCallBack loadCallBack) {
        ImageRequest imageRequest = getImageRequest(baseImage).build();
        ImagePipeline imagePipeline = Fresco.getImagePipeline();
        DataSource<CloseableReference<CloseableImage>> dataSource = imagePipeline.fetchDecodedImage(imageRequest, U.app());
        dataSource.subscribe(new BaseBitmapDataSubscriber() {
            @Override
            public void onNewResultImpl(@Nullable Bitmap bitmap) {
                // You can use the bitmap in only limited ways No need to do any cleanup.
                if (loadCallBack != null) {
                    loadCallBack.loadSuccess(bitmap);
                }
            }

            @Override
            public void onProgressUpdate(DataSource<CloseableReference<CloseableImage>> dataSource) {
                if (dataSource != null && loadCallBack != null) {
                    loadCallBack.onProgressUpdate(dataSource.getProgress());
                }
            }

            @Override
            public void onFailureImpl(DataSource dataSource) {
                // No cleanup required here.
                if (loadCallBack != null) {
                    loadCallBack.loadFail();
                }
            }
        }, CallerThreadExecutor.getInstance());
    }

    private static ImageRequestBuilder getImageRequest(BaseImage baseImage) {
        ImageRequestBuilder imageRequestBuilder = ImageRequestBuilder.newBuilderWithSource(baseImage.getUri());
        /**
         * resize时的 宽与高
         */
        if (baseImage.getWidth() > 0 && baseImage.getHeight() > 0) {
            imageRequestBuilder.setResizeOptions(new ResizeOptions(baseImage.getWidth(), baseImage.getHeight()));
        }
        /**
         * 后处理
         */
        if (baseImage.getPostprocessor() != null) {
            imageRequestBuilder.setPostprocessor(baseImage.getPostprocessor());
        }

        imageRequestBuilder.setImageDecodeOptions(getImageDecodeOptions());

        /**
         * 支持图片渐进式加载 & 加载优先级
         */
        imageRequestBuilder
                .setProgressiveRenderingEnabled(baseImage.isProgressiveRenderingEnabled())
                .setRequestPriority(baseImage.getRequestPriority())
                // 图片请求会在访问本地图片时先返回一个缩略图
                .setLocalThumbnailPreviewsEnabled(true)
        ;
        return imageRequestBuilder;
    }

    private static ImageDecodeOptions getImageDecodeOptions() {
        ImageDecodeOptions decodeOptions = ImageDecodeOptions.newBuilder()
//              .setBackgroundColor(Color.TRANSPARENT) //图片的背景颜色
                .setDecodeAllFrames(true)              //解码所有帧
                .setDecodePreviewFrame(true)           //解码预览框
//              .setForceOldAnimationCode(true)        //使用以前动画
//              .setFrom(options)                      //使用已经存在的图像解码
//              .setMinDecodeIntervalMs(intervalMs)    //最小解码间隔（分位单位）
                .setUseLastFrameForPreview(true)       //使用最后一帧进行预览
                .build();
        return decodeOptions;
    }

    /**
     * 使用 fresco 加载图片
     *
     * @param draweeView
     * @param baseImage  请使用 {@link com.common.image.model.ImageFactory 构造}
     */
    public static void loadImage(final SimpleDraweeView draweeView, final BaseImage baseImage) {
        if (draweeView == null) {
            MyLog.d(TAG, "draweeView is null");
            return;
        }

        if (baseImage == null || baseImage.getUri() == null) {
            MyLog.d(TAG, "baseImage is null ");
            return;
        }

        U.getThreadUtils().ensureUiThread();

        // 避免重新加载的优化，不确定是否有用，先去掉。
//        if (isAvoidReload) {
//            String uriPathTag = null;
//            if (draweeView.getTag(R.id.photo_dv) != null) {
//                uriPathTag = (String) draweeView.getTag(R.id.photo_dv);
//            }
//            if (!TextUtils.isEmpty(uriPathTag)) {
//                String uriPath = baseImage.getUri().toString();
//                if (uriPathTag.equalsIgnoreCase(uriPath)) {
//                    return;
//                }
//            }
//        }
        /**
         FIT_XY           无视宽高比填充满
         FIT_START        保持宽高比，缩放，直到一边到界
         FIT_CENTER       同上，但是最后居中
         FIT_END          同上上，但与显示边界右或下对齐
         CENTER           居中无缩放
         CENTER_INSIDE    使的图片都在边界内，与FIT_CENTER不同的是，只会缩小不会放大，默认是这个吧
         CENTER_CROP      保持宽高比，缩小或放大，使两边都大于等于边界，居中。
         FOCUS_CROP       同CENTER_CROP，但中心点可以设置
         FIT_BOTTOM_START
         */
        if (baseImage.getScaleType() != null) {
            draweeView.getHierarchy().setActualImageScaleType(baseImage.getScaleType());
        }
        /**
         * 失败时显示的图
         */
        if (baseImage.getFailureDrawable() != null) {
            draweeView.getHierarchy().setFailureImage(baseImage.getFailureDrawable(), baseImage.getFailureScaleType());
        }
        /**
         * loading时显示的图
         */
        if (null != baseImage.getLoadingDrawable()) {
            draweeView.getHierarchy().setPlaceholderImage(baseImage.getLoadingDrawable(), baseImage.getLoadingScaleType());
        }
        /**
         * loading时的进度条
         */
        if (null != baseImage.getProgressBarDrawable()) {
            draweeView.getHierarchy().setProgressBarImage(baseImage.getProgressBarDrawable());
        }

        /**
         * 设置边框 是否是圆形
         */
        RoundingParams roundingParams = draweeView.getHierarchy().getRoundingParams();
        if (null == roundingParams) {
            roundingParams = new RoundingParams();
        }
        roundingParams.setRoundAsCircle(baseImage.isCircle());

        if (baseImage.getBorderWidth() > 0) {
            roundingParams.setBorderWidth(baseImage.getBorderWidth());
            roundingParams.setBorderColor(baseImage.getBorderColor());
        } else {
            roundingParams.setBorderWidth(0);
        }
        if (baseImage.getCornerRadius() > 0) {
            roundingParams.setCornersRadius(baseImage.getCornerRadius());
        } else if (baseImage.getCornerRadii() != null) {
            roundingParams.setCornersRadii(baseImage.getCornerRadii());
        } else {
            roundingParams.setCornersRadius(0);
        }
        draweeView.getHierarchy().setRoundingParams(roundingParams);

        ImageRequest imageRequest = getImageRequest(baseImage)
                .build();

        ImageRequest lowResRequest = null;
        if (null != baseImage.getLowImageUri()) {
            ImageRequestBuilder lowBuilder = ImageRequestBuilder.newBuilderWithSource(baseImage.getLowImageUri());
            if (baseImage.getWidth() > 0 && baseImage.getHeight() > 0) {
                lowBuilder.setResizeOptions(new ResizeOptions(baseImage.getWidth(), baseImage.getHeight()));
            }
            lowResRequest = lowBuilder
                    .setAutoRotateEnabled(true)
                    .setProgressiveRenderingEnabled(false)
                    .setImageDecodeOptions(getImageDecodeOptions())
                    .setLowestPermittedRequestLevel(ImageRequest.RequestLevel.ENCODED_MEMORY_CACHE)
                    .build();
        }

        PipelineDraweeControllerBuilder builder = Fresco.newDraweeControllerBuilder()
                .setLowResImageRequest(lowResRequest)
                .setImageRequest(imageRequest)
                .setOldController(draweeView.getController())
                //只有设置tapToRetryEnabled为true，才会出现点击重试的图层，并且重试超过4次之后，就将显示失败的图层
                .setTapToRetryEnabled(baseImage.isTapToRetryEnabled())
                .setAutoPlayAnimations(baseImage.isAutoPlayAnimation())
                .setControllerListener(new BaseControllerListener<ImageInfo>() {
                    public void onFailure(String id, Throwable throwable) {
                        MyLog.w(TAG, "loadImage onFailure "
                                + "\n\t id = " + id
                                + "\n\t throwable = " + throwable
                                + "\n\t uri = " + baseImage.getUri());
                        deleteCache(baseImage.getUri());
                        if (baseImage.getCallBack() != null) {
                            baseImage.getCallBack().processWithFailure();
                        }
                    }

                    @Override
                    public void onFinalImageSet(String s, ImageInfo imageInfo, Animatable animatable) {
                        if (baseImage.getCallBack() != null) {
                            baseImage.getCallBack().processWithInfo(imageInfo);
                        }
                    }
                });

        DraweeController draweeController = builder.build();
        draweeController.setHierarchy(draweeView.getHierarchy());
        draweeView.setController(draweeController);
    }

    public static void deleteCache(String url) {
        Uri uri = Uri.parse(url);
        ImageRequest request = ImageRequest.fromUri(uri);
        ImagePipeline imagePipeline = Fresco.getImagePipeline();
        imagePipeline.evictFromDiskCache(request);
        imagePipeline.evictFromMemoryCache(uri);
        imagePipeline.evictFromCache(uri);
    }

    public static void deleteCache(Uri uri) {
        ImageRequest request = ImageRequest.fromUri(uri);
        ImagePipeline imagePipeline = Fresco.getImagePipeline();
        if (imagePipeline != null && null != request && null != request.getSourceUri()) {
            if (request != null) {
                imagePipeline.evictFromDiskCache(request);
                MyLog.w(TAG, "evictFromDiskCache request=" + request.getSourceUri().toString());
            }
            imagePipeline.evictFromDiskCache(request.getSourceUri());
            imagePipeline.evictFromMemoryCache(request.getSourceUri());
            imagePipeline.evictFromCache(request.getSourceUri());
            MyLog.w(TAG, "deleteCache uri=" + request.getSourceUri().toString());
        } else {
            MyLog.w(TAG, "deleteCache but imagePipeline is null!");
        }
    }

    public static void deleteCache(ImageRequest request) {
        ImagePipeline imagePipeline = Fresco.getImagePipeline();
        if (imagePipeline != null && null != request && null != request.getSourceUri()) {
            if (request != null) {
                imagePipeline.evictFromDiskCache(request);
                MyLog.w(TAG, "evictFromDiskCache request=" + request.getSourceUri().toString());
            }
            imagePipeline.evictFromDiskCache(request.getSourceUri());
            imagePipeline.evictFromMemoryCache(request.getSourceUri());
            imagePipeline.evictFromCache(request.getSourceUri());
            MyLog.w(TAG, "deleteCache uri=" + request.getSourceUri().toString());
        } else {
            MyLog.w(TAG, "deleteCache but imagePipeline is null!");
        }
    }

    /**
     * 加载 图片
     *
     * @param draweeView 图片加载的view
     * @param uri        图片远程uri
     * @param resDefault 默认图片
     */
    @Deprecated
    public static void preLoadImg(SimpleDraweeView draweeView, int width, int height, String uri, int resDefault) {
        if (draweeView == null) {
            return;
        }

        BaseImage avatarImg;
        if (TextUtils.isEmpty(uri)) {
            if (resDefault != 0) {
                avatarImg = ImageFactory.newResImage(resDefault).build();
            } else {
                avatarImg = ImageFactory.newResImage(R.drawable.ic_default_image).build();
            }
        } else {
            avatarImg = ImageFactory.newHttpImage(uri)
                    .setWidth(width)
                    .setHeight(height)
                    .setScaleType(ScalingUtils.ScaleType.CENTER_CROP)
                    .build();

        }

        ViewGroup.LayoutParams layoutParams = draweeView.getLayoutParams();
        layoutParams.height = width;
        layoutParams.width = height;
        draweeView.setLayoutParams(layoutParams);
        FrescoWorker.loadImage(draweeView, avatarImg);
    }

    /**
     * 从URI获取本地路径
     *
     * @return
     */
    public static String getAbsoluteImagePath(String contentUri) {

        //如果是对媒体文件，在android开机的时候回去扫描，然后把路径添加到数据库中。
        //由打印的contentUri可以看到：2种结构。正常的是：content://那么这种就要去数据库读取path。
        //另外一种是Uri是 file:///那么这种是 Uri.fromFile(File file);得到的
        String[] projection = {MediaStore.Images.Media.DATA};
        String urlpath;
        CursorLoader loader = new CursorLoader(U.app(), Uri.parse(contentUri), projection, null, null, null);
        Cursor cursor = loader.loadInBackground();
        try {
            int column_index = cursor.getColumnIndex(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            urlpath = cursor.getString(column_index);
            //如果是正常的查询到数据库。然后返回结构
            return urlpath;
        } catch (Exception e) {

            e.printStackTrace();
            // TODO: handle exception
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        //如果是文件。Uri.fromFile(File file)生成的uri。那么下面这个方法可以得到结果
        urlpath = Uri.parse(contentUri).getPath();
        return urlpath;
    }

    /**
     * 预加载图片
     */
    public static void preLoadImg(HttpImage httpImage, final ImageLoadCallBack callback, boolean isToMemory) {
        ImageRequest imageRequest = getImageRequest(httpImage)
                .setCacheChoice(ImageRequest.CacheChoice.DEFAULT)
                .setLowestPermittedRequestLevel(ImageRequest.RequestLevel.FULL_FETCH)
                .build();
        ImagePipeline imagePipeline = Fresco.getImagePipeline();
        if (null != callback) {
            DataSource<CloseableReference<CloseableImage>> dataSource = imagePipeline.fetchDecodedImage(imageRequest, U.app());
            dataSource.subscribe(new BaseBitmapDataSubscriber() {
                @Override
                public void onNewResultImpl(@Nullable Bitmap bitmap) {
                    // You can use the bitmap in only limited ways No need to do any cleanup.
                    if (callback != null) {
                        callback.loadSuccess(bitmap);
                    }
                }

                @Override
                public void onNewResult(DataSource<CloseableReference<CloseableImage>> dataSource) {
                    super.onNewResult(dataSource);
                    if (!dataSource.isFinished()) {
                        return;
                    }
                    CloseableReference<CloseableImage> ref = dataSource.getResult();
                    if (ref != null) {
                        CloseableReference.closeSafely(ref);
                    }
                    MyLog.d(TAG, "onNewResult close dataSource");
                }

                @Override
                public void onProgressUpdate(DataSource<CloseableReference<CloseableImage>> dataSource) {
                    super.onProgressUpdate(dataSource);
                    if (dataSource != null && callback != null) {
                        float progress = dataSource.getProgress();
                        callback.onProgressUpdate(progress);
                    }
                }

                @Override
                public void onFailureImpl(DataSource dataSource) {
                    // No cleanup required here.
                    if (callback != null) {
                        callback.loadFail();
                    }
                    Throwable t = dataSource.getFailureCause();
                    MyLog.w(TAG, "onFailureImpl throwable=" + t.toString());
                }
            }, UiThreadImmediateExecutorService.getInstance());
        }
        if (isToMemory) {
            imagePipeline.prefetchToBitmapCache(imageRequest, null);
        } else {
            imagePipeline.prefetchToDiskCache(imageRequest, null);
        }

    }


    /**
     * 得到图片的文件路径
     *
     * @param uri
     * @return
     */
    public static File getCacheFileFromFrescoDiskCache(Uri uri) {
        File cacheFile = null;
        //先试试原图是否加载了
        ImageRequest request = ImageRequest.fromUri(uri);
        if (null != request) {
            try {
                CacheKey cacheKey = MLCacheKeyFactory.getInstance().getEncodedCacheKey(request, null);
                if (ImagePipelineFactory.getInstance().getMainFileCache().hasKey(cacheKey)) {
                    BinaryResource resource = ImagePipelineFactory.getInstance().getMainFileCache().getResource(cacheKey);
                    cacheFile = ((FileBinaryResource) resource).getFile();
                    return cacheFile;
                } else if (ImagePipelineFactory.getInstance().getSmallImageFileCache().hasKey(cacheKey)) {
                    BinaryResource resource = ImagePipelineFactory.getInstance().getSmallImageFileCache().getResource(cacheKey);
                    cacheFile = ((FileBinaryResource) resource).getFile();
                    return cacheFile;
                }
            } catch (Exception e) {
                MyLog.e(e);
            }
        }
        return null;
    }

    public static File getCacheFileFromFrescoDiskCache(String url) {
        Uri uri = Uri.parse(url);
        return getCacheFileFromFrescoDiskCache(uri);
    }

    /**
     * 图片加载回调
     */
    public interface ImageLoadCallBack {
        void loadSuccess(Bitmap bitmap);

        void onProgressUpdate(float progress);

        void loadFail();
    }

}
