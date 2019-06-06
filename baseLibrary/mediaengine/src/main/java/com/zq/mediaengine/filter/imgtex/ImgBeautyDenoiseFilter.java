package com.zq.mediaengine.filter.imgtex;

import android.opengl.GLES20;

import com.zq.mediaengine.framework.CredtpModel;
import com.zq.mediaengine.framework.ImgTexFormat;
import com.zq.mediaengine.util.gles.GLRender;
import com.zq.mediaengine.util.gles.GlUtil;

/**
 * Beauty Denoise filter.
 */
public class ImgBeautyDenoiseFilter extends ImgTexFilter {
    private ImgTexFormat mInputFormat;

    public ImgBeautyDenoiseFilter(GLRender glRender) {
        super(glRender, GlUtil.BASE_VERTEX_SHADER, CredtpModel.BEAUTY_DENOISE_FILTER);
    }

    @Override
    public void onFormatChanged(ImgTexFormat format) {
        mInputFormat = format;
    }

    @Override
    protected void onInitialized() {
        int textureWidthLoc = GLES20.glGetUniformLocation(mProgramId, "textureWidth");
        GlUtil.checkLocation(textureWidthLoc, "textureWidth");
        int textureHighLoc = GLES20.glGetUniformLocation(mProgramId, "textureHigh");
        GlUtil.checkLocation(textureHighLoc, "textureHigh");
        GLES20.glUniform1f(textureWidthLoc, mInputFormat.width);
        GlUtil.checkGlError("glUniform1f");
        //noinspection SuspiciousNameCombination
        GLES20.glUniform1f(textureHighLoc, mInputFormat.height);
        GlUtil.checkGlError("glUniform1f");
    }
}
