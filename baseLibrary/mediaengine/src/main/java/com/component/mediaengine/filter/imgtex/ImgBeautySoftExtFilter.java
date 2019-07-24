package com.component.mediaengine.filter.imgtex;

import android.opengl.GLES20;

import com.component.mediaengine.framework.CredtpModel;
import com.component.mediaengine.framework.ImgTexFormat;
import com.component.mediaengine.util.gles.GLRender;
import com.component.mediaengine.util.gles.GlUtil;

import java.nio.FloatBuffer;

/**
 * Beauty SoftExt filter.
 */
public class ImgBeautySoftExtFilter extends ImgTexFilter {
    private ImgTexFormat mInputFormat;

    public ImgBeautySoftExtFilter(GLRender glRender) {
        super(glRender, GlUtil.BASE_VERTEX_SHADER, CredtpModel.BEAUTY_SOFT_EXT_FILTER);
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
