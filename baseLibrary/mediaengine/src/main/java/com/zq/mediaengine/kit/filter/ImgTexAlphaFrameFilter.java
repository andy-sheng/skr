package com.zq.mediaengine.kit.filter;

import android.util.Log;

import com.zq.mediaengine.filter.imgtex.ImgTexFilter;
import com.zq.mediaengine.framework.ImgTexFormat;
import com.zq.mediaengine.util.gles.GLRender;

public class ImgTexAlphaFrameFilter extends ImgTexFilter {
    private static final String TAG = "ImgTexAlphaFrameFilter";

    private static final String VERTEX_SHADER =
            "uniform mat4 uTexMatrix;\n" +
                    "attribute vec4 aPosition;\n" +
                    "attribute vec4 aTextureCoord;\n" +
                    "varying highp vec2 vTextureCoord;\n" +
                    "varying highp vec2 vTextureCoord2;\n" +
                    "void main() {\n" +
                    "    gl_Position = aPosition;\n" +
                    "    highp vec2 coord = (uTexMatrix * aTextureCoord).xy;\n" +
                    "    vTextureCoord = vec2(coord.x*0.5 + 0.5, coord.y);\n" +
                    "    vTextureCoord2 = vec2(coord.x*0.5, coord.y);\n" +
                    "}\n";

//    private static final String VERTEX_SHADER =
//            "uniform mat4 uTexMatrix;\n" +
//                    "attribute vec4 aPosition;\n" +
//                    "attribute vec4 aTextureCoord;\n" +
//                    "varying highp vec2 vTextureCoord;\n" +
//                    "varying highp vec2 vTextureCoord2;\n" +
//                    "void main() {\n" +
//                    "    gl_Position = aPosition;\n" +
//                    "    highp vec2 coord = (uTexMatrix * aTextureCoord).xy;\n" +
//                    "    vTextureCoord = vec2(coord.x, coord.y*0.5);\n" +
//                    "    vTextureCoord2 = vec2(coord.x, coord.y*0.5 + 0.5);\n" +
//                    "}\n";

    private static final String FRAGMENT_SHADER =
            "precision mediump float;\n" +
                    "varying highp vec2 vTextureCoord;\n" +
                    "varying highp vec2 vTextureCoord2;\n" +
                    "void main() {\n" +
                    "    vec4 color1 = texture2D(sTexture, vTextureCoord);\n" +
                    "    vec4 color2 = texture2D(sTexture, vTextureCoord2);\n" +
                    "    gl_FragColor = vec4(color1.rgb, color2.r);\n" +
                    "}\n";

    public ImgTexAlphaFrameFilter(GLRender glRender) {
        super(glRender, VERTEX_SHADER, FRAGMENT_SHADER);
    }

    @Override
    protected ImgTexFormat getOutFormat(ImgTexFormat inFormat) {
        Log.d(TAG, "getOutFormat " + inFormat.width + "x" + inFormat.height);
        return new ImgTexFormat(ImgTexFormat.COLOR_RGBA, inFormat.width / 2, inFormat.height);
    }
}
