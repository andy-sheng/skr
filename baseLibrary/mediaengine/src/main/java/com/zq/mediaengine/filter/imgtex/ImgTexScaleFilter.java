package com.zq.mediaengine.filter.imgtex;

import android.graphics.PointF;

import com.zq.mediaengine.framework.ImgTexFormat;
import com.zq.mediaengine.util.gles.GLRender;
import com.zq.mediaengine.util.gles.TexTransformUtil;

import java.nio.FloatBuffer;

/**
 * Gpu image filter to rotate/crop/scale video frame,
 * and covert image format to RGBA if needed.
 */
public class ImgTexScaleFilter extends ImgTexFilter {

    private FloatBuffer mTexCoordsBuf = TexTransformUtil.getTexCoordsBuf();
    private ImgTexFormat mTargetFormat;
    private ImgTexFormat mInputFormat;

    public ImgTexScaleFilter(GLRender glRender) {
        super(glRender);
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

    @Override
    protected ImgTexFormat getSrcPinFormat() {
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
        PointF crop = TexTransformUtil.calCrop(sar, dar);

        mTexCoordsBuf = TexTransformUtil.getCropTexCoordsBuf(crop.x, crop.y);
    }
}
