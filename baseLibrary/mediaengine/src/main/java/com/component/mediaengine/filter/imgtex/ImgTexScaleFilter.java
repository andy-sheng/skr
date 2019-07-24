package com.component.mediaengine.filter.imgtex;

import android.graphics.RectF;
import android.util.Log;

import com.component.mediaengine.framework.ImgTexFormat;
import com.component.mediaengine.util.gles.GLRender;
import com.component.mediaengine.util.gles.GlUtil;
import com.component.mediaengine.util.gles.TexTransformUtil;

import java.nio.FloatBuffer;

/**
 * Gpu image filter to rotate/crop/scale video frame,
 * and covert image format to RGBA if needed.
 */
public class ImgTexScaleFilter extends ImgTexFilter {
    private static final String TAG = "ImgTexScaleFilter";
    public static final int SCALING_MODE_FULL_FILL = 0;
    public static final int SCALING_MODE_BEST_FIT = 1;
    public static final int SCALING_MODE_CENTER_CROP = 2;

    private int mRenderScalingMode;

    private FloatBuffer mVertexCoordsBuf = TexTransformUtil.getVertexCoordsBuf();
    private FloatBuffer mTexCoordsBuf = TexTransformUtil.getTexCoordsBuf();
    private ImgTexFormat mTargetFormat;
    private ImgTexFormat mInputFormat;
    private int mRotateDegrees = 0;

    public ImgTexScaleFilter(GLRender glRender) {
        super(glRender);
        mRenderScalingMode = SCALING_MODE_CENTER_CROP;
    }

    /**
     * Set output target resolution
     *
     * @param w target width
     * @param h target height
     */
    public void setTargetSize(int w, int h) {
        mTargetFormat = new ImgTexFormat(ImgTexFormat.COLOR_RGBA, w, h);
        if (mInputFormat != null) {
            calculate(mInputFormat);
        }
    }

    /**
     * Set scaling mode for specific input pin.
     *
     * @param mode scaling mode, see {@link #SCALING_MODE_FULL_FILL},
     *             {@link #SCALING_MODE_BEST_FIT},
     *             {@link #SCALING_MODE_CENTER_CROP}
     */
    public void setScalingMode(int mode) {
        mRenderScalingMode = mode;
        if (mInputFormat != null) {
            calculate(mInputFormat);
        }
    }

    /**
     * Set rotate degrees in anti-clockwise of current video.
     *
     * @param degrees Degrees in anti-clockwise, only 0, 90, 180, 270 accepted.
     * @throws IllegalArgumentException
     */
    public void setRotateDegrees(int degrees) {
        mRotateDegrees = degrees;
        if (mInputFormat != null) {
            calculate(mInputFormat);
        }
    }

    @Override
    public ImgTexFormat getSrcPinFormat() {
        return mTargetFormat;
    }

    @Override
    public void onFormatChanged(final ImgTexFormat format) {
        mInputFormat = format;
        calculate(format);
    }

    @Override
    protected FloatBuffer getTexCoords() {
        return mTexCoordsBuf;
    }

    @Override
    protected FloatBuffer getVertexCoords() {
        return mVertexCoordsBuf;
    }

    private void calculate(final ImgTexFormat format) {
        if (mTargetFormat == null ||
                mTargetFormat.width == 0 || mTargetFormat.height == 0 ||
                format.width == 0 || format.height == 0) {
            return;
        }

        int sw = format.width;
        int sh = format.height;
        float sar, dar;
        sar = (float) sw / (float) sh;
        dar = (float) mTargetFormat.width / (float) mTargetFormat.height;

        float cropX, cropY;
        RectF rectF = new RectF(0.f, 0.f, 1.f, 1.f);
        if (mRenderScalingMode == SCALING_MODE_BEST_FIT) {
            if (sar > dar) {
                cropX = 0;
                cropY = (1.0f - dar / sar) / 2;
            } else {
                cropY = 0;
                cropX = (1.0f - sar / dar) / 2;
            }
            Log.d(TAG, "sar=" + sar + " dar=" + dar + " cropX=" + cropX + " cropY=" + cropY);
            rectF = new RectF(rectF.left + cropX, rectF.top + cropY,
                    rectF.right - cropX, rectF.bottom - cropY);
            Log.d(TAG, "rectF=" + rectF);
        }
        mVertexCoordsBuf = genVertexCoordsBuf(rectF);

        cropX = 0;
        cropY = 0;
        if (mRenderScalingMode == SCALING_MODE_CENTER_CROP) {
            if (sar > dar) {
                cropX = (1.0f - dar / sar) / 2;
                cropY = 0;
            } else {
                cropX = 0;
                cropY = (1.0f - sar / dar) / 2;
            }
        }
        mTexCoordsBuf = TexTransformUtil.getTexCoordsBuf(cropX, cropY, mRotateDegrees, mMirror, mFlipVertical);
    }

    private FloatBuffer genVertexCoordsBuf(RectF rect) {
        float vertexArray[] = {
                -1 + 2 * rect.left,  1 - 2 * rect.bottom,   // 0 bottom left
                -1 + 2 * rect.right, 1 - 2 * rect.bottom,   // 1 bottom right
                -1 + 2 * rect.left,  1 - 2 * rect.top,      // 2 top left
                -1 + 2 * rect.right, 1 - 2 * rect.top,      // 3 top right
        };
        return GlUtil.createFloatBuffer(vertexArray);
    }
}
