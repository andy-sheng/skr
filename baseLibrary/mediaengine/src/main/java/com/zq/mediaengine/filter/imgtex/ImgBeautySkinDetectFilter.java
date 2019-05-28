package com.zq.mediaengine.filter.imgtex;

import com.zq.mediaengine.framework.CredtpModel;
import com.zq.mediaengine.framework.ImgTexFormat;
import com.zq.mediaengine.util.gles.GLRender;

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
