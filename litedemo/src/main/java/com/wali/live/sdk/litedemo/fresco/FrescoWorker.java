package com.wali.live.sdk.litedemo.fresco;

import android.graphics.drawable.Animatable;
import android.util.Log;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.backends.pipeline.PipelineDraweeControllerBuilder;
import com.facebook.drawee.controller.BaseControllerListener;
import com.facebook.drawee.generic.GenericDraweeHierarchy;
import com.facebook.drawee.generic.RoundingParams;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.DraweeView;
import com.facebook.imagepipeline.common.ResizeOptions;
import com.facebook.imagepipeline.image.ImageInfo;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.wali.live.sdk.litedemo.fresco.config.FrescoConfig;
import com.wali.live.sdk.litedemo.fresco.image.BaseImage;

/**
 * Created by lan on 15-12-9.
 */
public class FrescoWorker {
    public static final String TAG = FrescoWorker.class.getSimpleName();

    public static void loadImage(final DraweeView<GenericDraweeHierarchy> draweeView, final BaseImage baseImage) {
        if (draweeView == null) {
            Log.d(TAG, "draweeView is null");
            return;
        }
        if (baseImage == null || baseImage.getUri() == null) {
            Log.d(TAG, "baseImage is null ");
            return;
        }

        if (baseImage.getScaleType() != null) {
            draweeView.getHierarchy().setActualImageScaleType(baseImage.getScaleType());
        }

        ImageRequestBuilder imageRequestBuilder = ImageRequestBuilder.newBuilderWithSource(baseImage.getUri());
        if (baseImage.getWidth() > 0 && baseImage.getHeight() > 0) {
            imageRequestBuilder.setResizeOptions(new ResizeOptions(baseImage.getWidth(), baseImage.getHeight()));
        }

        if (baseImage.getPostprocessor() != null) {
            imageRequestBuilder.setPostprocessor(baseImage.getPostprocessor());
        }
        imageRequestBuilder.setImageDecodeOptions(FrescoConfig.getImageDecodeOptions());

        RoundingParams roundingParams = draweeView.getHierarchy().getRoundingParams();
        if (roundingParams == null) {
            roundingParams = new RoundingParams();
        }
        roundingParams.setRoundAsCircle(baseImage.isCircle());
        if (baseImage.getCornerRadius() > 0) {
            roundingParams.setCornersRadius(baseImage.getCornerRadius());
        } else {
            roundingParams.setCornersRadius(0);
        }
        draweeView.getHierarchy().setRoundingParams(roundingParams);

        ImageRequest imageRequest = imageRequestBuilder
                .setProgressiveRenderingEnabled(false)
                .build();
        PipelineDraweeControllerBuilder builder = Fresco.newDraweeControllerBuilder()
                .setImageRequest(imageRequest)
                .setOldController(draweeView.getController())
                .setAutoPlayAnimations(true)
                .setControllerListener(new BaseControllerListener<ImageInfo>() {
                    public void onFailure(String id, Throwable throwable) {
                        Log.d(TAG, "loadImage onFailure "
                                + "\n\t id = " + id
                                + "\n\t throwable = " + throwable
                                + "\n\t uri = " + baseImage.getUri());
                    }

                    @Override
                    public void onFinalImageSet(String s, ImageInfo imageInfo, Animatable animatable) {
                    }
                });
        DraweeController draweeController = builder.build();
        draweeView.setController(draweeController);
    }
}
