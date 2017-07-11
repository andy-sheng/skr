package com.base.image.fresco;

import android.graphics.Bitmap;
import android.graphics.drawable.Animatable;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.ViewGroup;

import com.base.common.R;
import com.base.global.GlobalData;
import com.base.image.fresco.cache.MLCacheKeyFactory;
import com.base.image.fresco.config.ConfigConstants;
import com.base.image.fresco.image.BaseImage;
import com.base.log.MyLog;
import com.base.thread.ThreadUtils;
import com.base.utils.display.DisplayUtils;
import com.facebook.binaryresource.BinaryResource;
import com.facebook.binaryresource.FileBinaryResource;
import com.facebook.cache.common.CacheKey;
import com.facebook.common.executors.CallerThreadExecutor;
import com.facebook.common.references.CloseableReference;
import com.facebook.datasource.DataSource;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.backends.pipeline.PipelineDraweeControllerBuilder;
import com.facebook.drawee.controller.BaseControllerListener;
import com.facebook.drawee.generic.RoundingParams;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.common.Priority;
import com.facebook.imagepipeline.common.ResizeOptions;
import com.facebook.imagepipeline.core.ImagePipeline;
import com.facebook.imagepipeline.core.ImagePipelineFactory;
import com.facebook.imagepipeline.datasource.BaseBitmapDataSubscriber;
import com.facebook.imagepipeline.image.CloseableImage;
import com.facebook.imagepipeline.image.ImageInfo;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.facebook.imagepipeline.request.Postprocessor;

import java.io.File;

/**
 * Created by lan on 15-12-9.
 * 逐步完善
 */
public class FrescoWorker {
    public static final String TAG = FrescoWorker.class.getSimpleName();

    public static void loadImage(final SimpleDraweeView draweeView, final BaseImage baseImage) {
        loadImagePostBitmap(draweeView, baseImage, false);
    }

    public static void loadImagePostBitmap(final SimpleDraweeView draweeView, final BaseImage baseImage, boolean isPostBitmap) {
        if (draweeView == null) {
            MyLog.d(TAG, "draweeView is null");
            return;
        }

        if (baseImage == null || baseImage.getUri() == null) {
            MyLog.d(TAG, "baseImage is null ");
            return;
        }

        ThreadUtils.ensureUiThread();

//        MyLog.d(TAG, "loadImage start url = " + baseImage.getUri());
//        checkResizeOption(draweeView, ba、seImage);

        if (baseImage.getScaleType() != null) {
            draweeView.getHierarchy().setActualImageScaleType(baseImage.getScaleType());
        }
        if (baseImage.getFailureDrawable() != null) {
            draweeView.getHierarchy().setFailureImage(baseImage.getFailureDrawable(), baseImage.getFailureScaleType());
        }
        if (null != baseImage.getLoadingDrawable()) {
            draweeView.getHierarchy().setPlaceholderImage(baseImage.getLoadingDrawable(), baseImage.getLoadingScaleType());
        }

        if (null != baseImage.mProgressBarDrawable) {
            draweeView.getHierarchy().setProgressBarImage(baseImage.mProgressBarDrawable);
        }

        ImageRequestBuilder imageRequestBuilder = ImageRequestBuilder.newBuilderWithSource(baseImage.getUri());
        if (baseImage.getWidth() > 0 && baseImage.getHeight() > 0) {
            imageRequestBuilder.setResizeOptions(new ResizeOptions(baseImage.getWidth(), baseImage.getHeight()));
        }

        if (baseImage.getPostprocessor() != null) {
            imageRequestBuilder.setPostprocessor(baseImage.getPostprocessor());
        }

        imageRequestBuilder.setImageDecodeOptions(ConfigConstants.getImageDecodeOptions());
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
        } else {
            roundingParams.setCornersRadius(0);
        }
        draweeView.getHierarchy().setRoundingParams(roundingParams);
        final ImageRequest imageRequest = imageRequestBuilder
                .setProgressiveRenderingEnabled(false)
                .setRequestPriority(baseImage.requestPriority)
                .build();
        if (isPostBitmap) {
            ImagePipeline imagePipeline = Fresco.getImagePipeline();
            DataSource<CloseableReference<CloseableImage>> dataSource = imagePipeline.fetchDecodedImage(imageRequest, GlobalData.app());
            dataSource.subscribe(new BaseBitmapDataSubscriber() {
                @Override
                public void onNewResultImpl(@Nullable Bitmap bitmap) {
                    // You can use the bitmap in only limited ways No need to do any cleanup.
                    if (baseImage.getCallBack() != null) {
                        baseImage.getCallBack().process(bitmap);
                    }
                }

                @Override
                public void onFailureImpl(DataSource dataSource) {
                    // No cleanup required here.
                }
            }, CallerThreadExecutor.getInstance());
        }

        ImageRequest lowResRequest = null;
        if (null != baseImage.mLowImageUri) {
            ImageRequestBuilder lowBuilder = ImageRequestBuilder.newBuilderWithSource(baseImage.mLowImageUri);
            if (baseImage.getWidth() > 0 && baseImage.getHeight() > 0) {
                lowBuilder.setResizeOptions(new ResizeOptions(baseImage.getWidth(), baseImage.getHeight()));
            }
            lowResRequest = lowBuilder
                    .setAutoRotateEnabled(true)
                    .setProgressiveRenderingEnabled(false)
                    .setImageDecodeOptions(ConfigConstants.getImageDecodeOptions())
                    .setLowestPermittedRequestLevel(ImageRequest.RequestLevel.ENCODED_MEMORY_CACHE)
                    .build();
        }
        PipelineDraweeControllerBuilder builder = Fresco.newDraweeControllerBuilder()
                .setLowResImageRequest(lowResRequest)
                .setImageRequest(imageRequest)
                .setOldController(draweeView.getController())
                .setAutoPlayAnimations(baseImage.isAutoPlayAnimation())
                .setControllerListener(new BaseControllerListener<ImageInfo>() {
                    public void onFailure(String id, Throwable throwable) {
                        MyLog.w(TAG, "loadImage onFailure "
                                + "\n\t id = " + id
                                + "\n\t throwable = " + throwable
                                + "\n\t uri = " + baseImage.getUri());
                        deleteCache(baseImage.getUri());
                    }

                    @Override
                    public void onFinalImageSet(String s, ImageInfo imageInfo, Animatable animatable) {
                        if (baseImage.getCallBack() != null) {
                            baseImage.getCallBack().processWithInfo(imageInfo);
                        }
                    }
                });

        DraweeController draweeController = builder.build();
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


    private static void checkResizeOption(SimpleDraweeView draweeView, BaseImage baseImage) {
        if (baseImage.getWidth() > 0 && baseImage.getHeight() > 0) {
            return;
        }

        ViewGroup.LayoutParams lp = draweeView.getLayoutParams();
        if (baseImage.getWidth() <= 0) {
            if (lp != null && lp.width > 0) {
                baseImage.setWidth(lp.width);
            } else if (draweeView.getWidth() > 0) {
                baseImage.setWidth(draweeView.getWidth());
            } else {
                baseImage.setWidth(DisplayUtils.getScreenWidth());
            }
        }

        if (baseImage.getHeight() <= 0) {
            if (lp != null && lp.height > 0) {
                baseImage.setHeight(lp.height);
            } else if (draweeView.getHeight() > 0) {
                baseImage.setHeight(draweeView.getHeight());
            } else {
                baseImage.setHeight(DisplayUtils.getScreenWidth());
            }
        }
    }

    /**
     * 预加载图片
     *
     * @param url
     */
    public static void preLoadImg(String url, Postprocessor postprocessor, int width, int height) {
        if (TextUtils.isEmpty(url))
            return;
        MyLog.v("preLoadImg url=" + url);
        ImagePipeline imagePipeline = Fresco.getImagePipeline();
        ImageRequest imageRequest = ImageRequestBuilder.newBuilderWithSource(Uri.parse(url))
                .setResizeOptions(new ResizeOptions(width, height))
                .setLowestPermittedRequestLevel(ImageRequest.RequestLevel.FULL_FETCH)
                .setPostprocessor(postprocessor)
                .setRequestPriority(Priority.HIGH)
                .build();
        imagePipeline.prefetchToBitmapCache(imageRequest, null);
        imagePipeline.prefetchToDiskCache(imageRequest, null);

    }

    public static File getCacheFileFromFrescoDiskCache(String url) {
        Uri uri = Uri.parse(url);
        ImageRequest request = ImageRequest.fromUri(uri);
        if (null != request) {
            try {
                CacheKey cacheKey = MLCacheKeyFactory.getInstance().getEncodedCacheKey(request, null);
                if (ImagePipelineFactory.getInstance().getMainDiskStorageCache().hasKey(cacheKey)) {
                    BinaryResource resource = ImagePipelineFactory.getInstance().getMainDiskStorageCache().getResource(cacheKey);
                    File cacheFile = ((FileBinaryResource) resource).getFile();
                    return cacheFile;
                } else if (ImagePipelineFactory.getInstance().getSmallImageDiskStorageCache().hasKey(cacheKey)) {
                    BinaryResource resource = ImagePipelineFactory.getInstance().getSmallImageDiskStorageCache().getResource(cacheKey);
                    File cacheFile = ((FileBinaryResource) resource).getFile();
                    return cacheFile;
                }
            } catch (Exception e) {
                MyLog.e(e);
            }
        }
        return null;
    }

    public static void loadLocalImage(final SimpleDraweeView draweeView, final BaseImage baseImage) {
        loadLocalImage(draweeView, baseImage, false);
    }

    public static void loadLocalImage(final SimpleDraweeView draweeView, final BaseImage baseImage, boolean isAvoidReload) {
        if (draweeView == null) {
            MyLog.d(TAG, "draweeView is null");
            return;
        }

        if (baseImage == null || baseImage.getUri() == null) {
            MyLog.d(TAG, "baseImage is null ");
            return;
        }
        if (isAvoidReload) {
            String uriPathTag = null;
            if (draweeView.getTag(R.id.photo_dv) != null) {
                uriPathTag = (String) draweeView.getTag(R.id.photo_dv);
            }
            if (!TextUtils.isEmpty(uriPathTag)) {
                String uriPath = baseImage.getUri().toString();
                if (uriPathTag.equalsIgnoreCase(uriPath)) {
                    return;
                }
            }
        }

        if (baseImage.getScaleType() != null) {
            draweeView.getHierarchy().setActualImageScaleType(baseImage.getScaleType());
        }

        ImageRequestBuilder imageRequestBuilder = ImageRequestBuilder.newBuilderWithSource(baseImage.getUri());
        if (baseImage.getWidth() > 0 && baseImage.getHeight() > 0) {
            imageRequestBuilder.setResizeOptions(new ResizeOptions(baseImage.getWidth(), baseImage.getHeight()));
        }
        imageRequestBuilder.setImageDecodeOptions(ConfigConstants.getImageDecodeOptions());
        RoundingParams roundingParams = draweeView.getHierarchy().getRoundingParams();
        if (null == roundingParams) {
            roundingParams = new RoundingParams();
        }
        roundingParams.setRoundAsCircle(baseImage.isCircle());
        draweeView.getHierarchy().setRoundingParams(roundingParams);

        final ImageRequest imageRequest = imageRequestBuilder
                .setProgressiveRenderingEnabled(false)
                //.setLocalThumbnailPreviewsEnabled(true)
                .build();

        ImagePipeline imagePipeline = Fresco.getImagePipeline();
        imagePipeline.prefetchToBitmapCache(imageRequest, null);
        PipelineDraweeControllerBuilder builder = Fresco.newDraweeControllerBuilder()
                .setImageRequest(imageRequest)
                .setOldController(draweeView.getController())
                .setAutoPlayAnimations(true)
                .setControllerListener(new BaseControllerListener<ImageInfo>() {
                    public void onFailure(String id, Throwable throwable) {
                        MyLog.w(TAG, "loadImage onFailure "
                                + "\n\t id = " + id
                                + "\n\t throwable = " + throwable
                                + "\n\t uri = " + baseImage.getUri());
                        deleteCache(baseImage.getUri());
                    }

                    @Override
                    public void onFinalImageSet(String s, ImageInfo imageInfo, Animatable animatable) {
                        draweeView.setTag(R.id.photo_dv, baseImage.getUri().toString());
                        if (baseImage.getCallBack() != null) {
                            baseImage.getCallBack().processWithInfo(imageInfo);
                        }
                    }
                });

        DraweeController draweeController = builder.build();
        draweeView.setController(draweeController);
    }

}
