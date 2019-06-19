// Copyright (C) 2018 Beijing Bytedance Network Technology Co., Ltd.
package com.bytedance.labcv.effectsdk;

import android.content.Context;
import static com.bytedance.labcv.effectsdk.BytedEffectConstants.BytedResultCode.*;

import java.nio.ByteBuffer;

/**
 * 特效（包括滤镜、美颜、贴纸、美妆）入口
 */
public class RenderManager {

    private long mNativePtr;

    private volatile boolean mInited;

    static {
        System.loadLibrary("effect");
        System.loadLibrary("effect_proxy");
    }

    /**
     * 初始化特效句柄
     * @param context 应用上下文
     * @param modelDir 模型文件的根目录，注意不是模型文件的绝对路径，该目录下文件层次和目录名称必须和Demo中提供的完全一致
     * @param licensePath 授权文件绝对路径
     * @return 成功返回BEF_RESULT_SUC，其他返回值参考{@link BytedEffectConstants}
     *
     */
    public int init(Context context, String modelDir, String licensePath) {

        int ret = BEF_RESULT_SUC;
        if (!mInited) {
            ret = nativeInit(context, modelDir, licensePath);
            mInited = (ret == BEF_RESULT_SUC);
        }
        return ret;
    }

    /**
     * 释放特效相关句柄
     */
    public void release() {
        if (mInited) {
            nativeRelease();
        }
        mInited = false;
    }

    /**
     * 验证测试素材的license
     * @param context
     * @param modelDir
     * @param licensePath
     * @return 成功返回BEF_RESULT_SUC，否则返回对应的错误码，参考{@link BytedEffectConstants}
     */
    public int initTest(Context context, String modelDir, String licensePath) {

        int ret = BEF_RESULT_SUC;
        ret = nativeInitTest(context, modelDir, licensePath);
        mInited = (ret == BEF_RESULT_SUC);

        return ret;
    }

    /**
     * 设置美颜素材
     * @param resourcePath 素材绝对路径 如果传null或者空字符，则取消美颜效果
     * @return 成功返回BEF_RESULT_SUC， 其他返回值查看{@link BytedEffectConstants}
     */
    public boolean setBeauty(String resourcePath) {
        if (!mInited) {
            return false;
        }
        if (resourcePath == null) {
            resourcePath = "";
        }
        return nativeSetBeauty(resourcePath) == BEF_RESULT_SUC;
    }
    /**
     * 设置塑形素材
     * @param resourcePath 素材绝对路径 如果传null或者空字符，则取消塑形效果
     * @return 成功返回BEF_RESULT_SUC， 其他返回值查看{@link BytedEffectConstants}
     */
    public boolean setReshape(String resourcePath) {
        if (!mInited) {
            return false;
        }
        if (resourcePath == null) {
            resourcePath = "";
        }
        return nativeSetReshape(resourcePath) == BEF_RESULT_SUC;
    }

    /**
     * 设置滤镜素材
     * @param resourcePath 素材绝对路径 如果传null或者空字符，则取消滤镜效果
     * @return 成功返回BEF_RESULT_SUC， 其他返回值查看{@link BytedEffectConstants}
     */
    public boolean setFilter(String resourcePath) {
        if (!mInited) {
            return false;
        }
        if (resourcePath == null) {
            resourcePath = "";
        }
        return nativeSetFilter(resourcePath) == BEF_RESULT_SUC;
    }

    /**
     * 设置美妆素材
     * @param resourcePath 素材绝对路径 如果传null或者空字符，则取消美妆效果
     * @return 成功返回BEF_RESULT_SUC， 其他返回值查看{@link BytedEffectConstants}
     */
    public boolean setMakeUp(String resourcePath) {
        if (!mInited) {
            return false;
        }
        if (resourcePath == null) {
            resourcePath = "";
        }
        return nativeSetMakeUp(resourcePath) == BEF_RESULT_SUC;
    }

    /**
     * 设置贴纸素材
     * @param resourcePath 素材绝对路径 如果传null或者空字符，则取消贴纸效果
     * @return 成功返回BEF_RESULT_SUC， 其他返回值查看{@link BytedEffectConstants}
     */
    public boolean setSticker(String resourcePath) {
        if (!mInited) {
            return false;
        }
        if (resourcePath == null) {
            resourcePath = "";
        }
        return nativeSetSticker(resourcePath) == BEF_RESULT_SUC;
    }

    /**
     * 处理纹理 process texture
     * @param srcTextureId 输入纹理ID
     * @param dstTextureId 输出纹理ID
     * @param width 纹理宽度
     * @param height 纹理高度
     * @param rotation 纹理旋转角，参考{@link BytedEffectConstants.Rotation}
     * @return 成功返回BEF_RESULT_SUC， 其他返回值查看{@link BytedEffectConstants}
     */
    public boolean process(int srcTextureId, int dstTextureId, int width, int height, BytedEffectConstants.Rotation rotation) {
        if (!mInited) {
            return false;
        }
        double timestamp = System.nanoTime();
        return nativeProcess(srcTextureId, dstTextureId, width, height, rotation.id, timestamp) == BEF_RESULT_SUC;
    }

    /**
     * 处理像素数据
     * @param inputdata 输入数据
     * @param orient 旋转角，参考{@link BytedEffectConstants.Rotation}
     * @param in_pixformat 数据格式 参考{@link BytedEffectConstants}
     * @param imagew 图片宽度
     * @param imageh 图片高度
     * @param imagestride 图片步长
     * @param outdata 输出结果
     * @param out_pixformat 输出结果格式 参考{@link BytedEffectConstants}
     * @return 成功返回BEF_RESULT_SUC， 其他返回值查看{@link BytedEffectConstants}
     */
    public boolean processBuffer(ByteBuffer inputdata, BytedEffectConstants.Rotation orient, int in_pixformat, int imagew, int imageh, int imagestride, byte[] outdata, int out_pixformat) {
        if (!mInited) {
            return false;
        }
        double timestamp = System.nanoTime();
        int retStatus = nativeProcessBuffer(inputdata, orient.id, in_pixformat, imagew, imageh, imagestride, outdata, out_pixformat, timestamp);
        return retStatus == BEF_RESULT_SUC;
    }

    /**
     * 设置强度
     * @param intensitytype 类型
     * @param intensity
     * @return 成功返回BEF_RESULT_SUC， 其他返回值查看{@link BytedEffectConstants}
     */
    public boolean updateIntensity(int intensitytype, float intensity) {
        return nativeUpdateIntensity(intensitytype, intensity) == BEF_RESULT_SUC;
    }

    /**
     * 设置塑形参数
     * @param cheekintensity 瘦脸强度 0-1
     * @param eyeintensity 大眼参数 0-1
     * @return 成功返回BEF_RESULT_SUC， 其他返回值查看{@link BytedEffectConstants}
     */
    public boolean updateReshape(float cheekintensity, float eyeintensity) {
        return nativeUpdateReshape(cheekintensity, eyeintensity) == BEF_RESULT_SUC;
    }




    private native int nativeInitTest(Context context, String algorithmResourceDir, String license);

    private native int nativeInit(Context context, String algorithmResourceDir, String license);

    private native void nativeRelease();

    private native int nativeSetBeauty(String beautyType);

    private native int nativeSetReshape(String reshapeType);

    private native int nativeSetFilter(String filterPath);

    private native int nativeSetMakeUp(String filterPath);

    private native int nativeSetSticker(String filterPath);

    private native int nativeUpdateIntensity(int itype, float intensity);

    private native int nativeUpdateReshape(float cheekintensity, float eyeintensity);

    private native int nativeProcess(int srcTextureId, int dstTextureId, int width, int height, int rotation, double timeStamp);

    private native int nativeProcessBuffer(ByteBuffer inputdata, int rotation, int in_pixformat, int imagew, int imageh, int imagestride, byte[] outdata, int out_pixformat, double timestamp);

}
