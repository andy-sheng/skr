package com.component.mediaengine.filter.imgtex;

import com.component.mediaengine.framework.CredtpModel;
import com.component.mediaengine.framework.ImgTexFormat;
import com.component.mediaengine.util.gles.GLRender;

/**
 * Beauty skin detect filter.
 *
 * @hide
 */

public class ImgBeautySkinDetectFilter extends ImgTexFilter {
    public ImgBeautySkinDetectFilter(GLRender glRender) {
        super(glRender, BASE_VERTEX_SHADER, CredtpModel.BEAUTY_SKIN_DETECT_FILTER);
    }

    @Override
    protected void onFormatChanged(ImgTexFormat format) {
        mInited = false;
    }
}
