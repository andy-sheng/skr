package com.zq.mediaengine.util.gles;

import android.graphics.PointF;

import java.nio.FloatBuffer;

/**
 * Utils for texture transform.
 *
 * @hide
 */
public class TexTransformUtil {
    protected static final int SIZEOF_FLOAT = 4;
    public static final int COORDS_PER_VERTEX = 2;
    public static final int COORDS_STRIDE = COORDS_PER_VERTEX * SIZEOF_FLOAT;
    public static final int COORDS_COUNT = 4;

    public static final float TEX_COORDS[] = {
            0.0f, 0.0f,     // 0 bottom left
            1.0f, 0.0f,     // 1 bottom right
            0.0f, 1.0f,     // 2 top left
            1.0f, 1.0f,     // 3 top right
    };
    public static final FloatBuffer TEX_COORDS_BUF =
            GlUtil.createFloatBuffer(TEX_COORDS);

    public static final float TEX_MIRROR_COORDS[] = {
            1.0f, 0.0f,     // 1 bottom right
            0.0f, 0.0f,     // 0 bottom left
            1.0f, 1.0f,     // 3 top right
            0.0f, 1.0f,     // 2 top left
    };
    public static final FloatBuffer TEX_MIRROR_COORDS_BUF =
            GlUtil.createFloatBuffer(TEX_MIRROR_COORDS);

    public static final float VERTEX_COORDS[] = {
            -1.0f, -1.0f,   // 0 bottom left
            1.0f, -1.0f,   // 1 bottom right
            -1.0f, 1.0f,   // 2 top left
            1.0f, 1.0f,   // 3 top right
    };
    public static final FloatBuffer VERTEX_COORDS_BUF =
            GlUtil.createFloatBuffer(VERTEX_COORDS);

    public static final float VERTEX_MIRROR_COORDS[] = {
            1.0f, -1.0f,   // 1 bottom right
            -1.0f, -1.0f,   // 0 bottom left
            1.0f, 1.0f,   // 3 top right
            -1.0f, 1.0f,   // 2 top left
    };
    public static final FloatBuffer VERTEX_MIRROR_COORDS_BUF =
            GlUtil.createFloatBuffer(VERTEX_MIRROR_COORDS);

    private TexTransformUtil() {
    }

    public static FloatBuffer getTexCoordsBuf() {
        return TEX_COORDS_BUF;
    }

    public static FloatBuffer getTexMirrorCoordsBuf() {
        return TEX_MIRROR_COORDS_BUF;
    }

    public static FloatBuffer getVFlipTexCoordsBuf() {
        return getTexCoordsBuf(0, 0, 0, false, true);
    }

    public static FloatBuffer getHFlipTexCoordsBuf() {
        return getTexCoordsBuf(0, 0, 0, true, false);
    }

    public static FloatBuffer getRotateTexCoordsBuf(int degrees) {
        return getTexCoordsBuf(0, 0, degrees, false, false);
    }

    public static FloatBuffer getCropTexCoordsBuf(float cx, float cy) {
        return getTexCoordsBuf(cx, cy, 0, false, false);
    }

    public static FloatBuffer getTexCoordsBuf(float left, float top, float right, float bottom,
                                              int rotateDegrees, boolean flipHorizontal,
                                              boolean flipVertical) {
        float[] tmp = new float[TEX_COORDS.length];
        float[] array = new float[TEX_COORDS.length];
        System.arraycopy(TEX_COORDS, 0, tmp, 0, TEX_COORDS.length);
        System.arraycopy(TEX_COORDS, 0, array, 0, TEX_COORDS.length);

        float cxLeft = left;
        float cxRight = right;
        float cyBottom = bottom;
        float cyTop = top;
        boolean flipX = flipHorizontal;
        boolean flipY = flipVertical;

        if (rotateDegrees % 180 != 0) {
            //横屏
            cxLeft = bottom;
            cxRight = top;
            cyBottom = left;
            cyTop = right;

            flipX = flipVertical;
            flipY = flipHorizontal;
        }

        if (left != 0 || top != 0 || right != 0 || bottom != 0) {
            tmp[0] = cxLeft;
            tmp[2] = 1.0f - cxRight;
            tmp[4] = cxLeft;
            tmp[6] = 1.0f - cxRight;
            //bottom
            tmp[1] = cyBottom;
            tmp[3] = cyBottom;
            //top
            tmp[5] = 1 - cyTop;
            tmp[7] = 1 - cyTop;
        }

        if (flipX) {
            for (int i = 0; i < tmp.length; i += 2) {
                tmp[i] = flip(tmp[i]);
            }
        }

        if (flipY) {
            for (int i = 0; i < tmp.length; i += 2) {
                tmp[i + 1] = flip(tmp[i + 1]);
            }
        }

        switch (rotateDegrees) {
            case 180:
                System.arraycopy(tmp, 6, array, 0, 2);
                System.arraycopy(tmp, 4, array, 2, 2);
                System.arraycopy(tmp, 2, array, 4, 2);
                System.arraycopy(tmp, 0, array, 6, 2);
                break;
            case 90:
                System.arraycopy(tmp, 4, array, 0, 2);
                System.arraycopy(tmp, 0, array, 2, 2);
                System.arraycopy(tmp, 6, array, 4, 2);
                System.arraycopy(tmp, 2, array, 6, 2);
                break;
            case 270:
                System.arraycopy(tmp, 2, array, 0, 2);
                System.arraycopy(tmp, 6, array, 2, 2);
                System.arraycopy(tmp, 0, array, 4, 2);
                System.arraycopy(tmp, 4, array, 6, 2);
                break;
            default:
                System.arraycopy(tmp, 0, array, 0, tmp.length);
                break;
        }

        return GlUtil.createFloatBuffer(array);
    }

    public static FloatBuffer getTexCoordsBuf(float cropX, float cropY, int rotateDegrees,
                                              boolean flipHorizontal, boolean flipVertical) {
        float[] tmp = new float[TEX_COORDS.length];
        float[] array = new float[TEX_COORDS.length];
        System.arraycopy(TEX_COORDS, 0, tmp, 0, TEX_COORDS.length);
        System.arraycopy(TEX_COORDS, 0, array, 0, TEX_COORDS.length);

        float cx = cropX;
        float cy = cropY;
        boolean flipX = flipHorizontal;
        boolean flipY = flipVertical;
        if (rotateDegrees % 180 != 0) {
            cx = cropY;
            cy = cropX;
            flipX = flipVertical;
            flipY = flipHorizontal;
        }

        if (cx != 0 || cy != 0) {
            for (int i = 0; i < tmp.length; i += 2) {
                tmp[i] = crop(tmp[i], cx);
                tmp[i + 1] = crop(tmp[i + 1], cy);
            }
        }

        if (flipX) {
            for (int i = 0; i < tmp.length; i += 2) {
                tmp[i] = flip(tmp[i]);
            }
        }

        if (flipY) {
            for (int i = 0; i < tmp.length; i += 2) {
                tmp[i + 1] = flip(tmp[i + 1]);
            }
        }

        switch (rotateDegrees) {
            case 180:
                System.arraycopy(tmp, 6, array, 0, 2);
                System.arraycopy(tmp, 4, array, 2, 2);
                System.arraycopy(tmp, 2, array, 4, 2);
                System.arraycopy(tmp, 0, array, 6, 2);
                break;
            case 90:
                System.arraycopy(tmp, 4, array, 0, 2);
                System.arraycopy(tmp, 0, array, 2, 2);
                System.arraycopy(tmp, 6, array, 4, 2);
                System.arraycopy(tmp, 2, array, 6, 2);
                break;
            case 270:
                System.arraycopy(tmp, 2, array, 0, 2);
                System.arraycopy(tmp, 6, array, 2, 2);
                System.arraycopy(tmp, 0, array, 4, 2);
                System.arraycopy(tmp, 4, array, 6, 2);
                break;
            default:
                System.arraycopy(tmp, 0, array, 0, tmp.length);
                break;
        }

        return GlUtil.createFloatBuffer(array);
    }

    private static float crop(float v, float c) {
        return (v == 0) ? c : (1 - c);
    }

    private static float flip(float v) {
        return 1.f - v;
    }

    public static FloatBuffer getVertexCoordsBuf() {
        return VERTEX_COORDS_BUF;
    }

    public static FloatBuffer getVertexMirrorCoordsBuf() {
        return VERTEX_MIRROR_COORDS_BUF;
    }

    /**
     * calculate crop value for center-crop render mode
     *
     * @param sar source aspect radio
     * @param dar dest aspect radio
     * @return the crop value
     */
    public static PointF calCrop(float sar, float dar) {
        PointF pointF = new PointF();
        if (sar > dar) {
            pointF.x = 1.0f - dar / sar;
            pointF.y = 0;
        } else {
            pointF.x = 0;
            pointF.y = 1.0f - sar / dar;
        }
        pointF.x /= 2.0f;
        pointF.y /= 2.0f;
        return pointF;
    }

    /**
     * Returns the array of vertices.
     * <p>
     * To avoid allocations, this returns internal state.  The caller must not modify it.
     */
    public static FloatBuffer getVertexArray() {
        return VERTEX_COORDS_BUF;
    }

    /**
     * Returns the number of vertices stored in the vertex array.
     */
    public static int getVertexCount() {
        return COORDS_COUNT;
    }

    /**
     * Returns the width, in bytes, of the data for each vertex.
     */
    public static int getVertexStride() {
        return COORDS_STRIDE;
    }

    /**
     * Returns the number of position coordinates per vertex.  This will be 2 or 3.
     */
    public static int getCoordsPerVertex() {
        return COORDS_PER_VERTEX;
    }

}