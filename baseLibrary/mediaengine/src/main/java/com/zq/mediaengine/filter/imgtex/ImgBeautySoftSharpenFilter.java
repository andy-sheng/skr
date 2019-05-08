package com.zq.mediaengine.filter.imgtex;

import android.opengl.GLES20;

import com.zq.mediaengine.framework.CredtpModel;
import com.zq.mediaengine.framework.ImgTexFormat;
import com.zq.mediaengine.util.gles.GLRender;
import com.zq.mediaengine.util.gles.GlUtil;

import java.nio.FloatBuffer;

/**
 * Beauty soft sharpen filter
 */
public class ImgBeautySoftSharpenFilter extends ImgTexFilter {
    private ImgTexFormat mInputFormat;

    public ImgBeautySoftSharpenFilter(GLRender glRender) {
        super(glRender, GlUtil.BASE_VERTEX_SHADER, CredtpModel.BEAUTY_SHARPEN_FILTER);
    }

    @Override
    public void onFormatChanged(ImgTexFormat format) {
        mInputFormat = format;
    }

    @Override
    protected void onInitialized() {
        int singleStepOffsetLoc = getUniformLocation("singleStepOffset");
        float[] texSize = (new float[]{2.0f / mInputFormat.width, 2.0f / mInputFormat.height});
        GLES20.glUniform2fv(singleStepOffsetLoc, 1, FloatBuffer.wrap(texSize));
        GlUtil.checkGlError("glUniform2fv");
    }
}