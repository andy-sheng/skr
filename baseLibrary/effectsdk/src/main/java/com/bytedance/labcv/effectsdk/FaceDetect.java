// Copyright (C) 2018 Beijing Bytedance Network Technology Co., Ltd.
package com.bytedance.labcv.effectsdk;

import android.content.Context;
import android.util.Log;

import java.nio.ByteBuffer;
import static com.bytedance.labcv.effectsdk.BytedEffectConstants.BytedResultCode.*;

/**
 * 人脸检测入口
 */
public class FaceDetect {

    private long mNativePtr;

    private long mAttriNativePtr;

    private volatile boolean mInited = false;

    private volatile boolean mInitedExtra = false;

    private volatile boolean mInitedAttri = false;

    // 106关键点检测配置
    private int mFaceDetectConfig = -1;

    // 属性检测配置
    private int mFaceAttriConfig = -1; // for jni detect get

    static {
        System.loadLibrary("effect_proxy");
    }

    /**
     * 初始化人脸106关键点检测句柄
     * @param modelPath 模型文件路径
     * @param config 人脸检测算法的配置
     *       config = 模型类型（必须设置,目前只有一种模式BytedEffectConstants.BEF_DETECT_SMALL_MODEL）
     *               |检测模式（缺省值为BEF_DETECT_MODE_VIDEO,参考{@link BytedEffectConstants.DetectMode}）
     *               |可检测的特征（必须设置, 参考{@link BytedEffectConstants.FaceAction}）
     * @param license 授权文件
     * @return 成功返回BEF_RESULT_SUC，否则返回对应的错误码
     */

    public int init(Context context, String modelPath, int config, String license) {
        int ret = nativeInit(config, modelPath);
        if (ret != BEF_RESULT_SUC) {
            mInited = false;
            return ret;
        }

        if (ret != BEF_RESULT_SUC) {
            mInited = false;
        }

        /**
         *  初始化后必须checklicense，否则后续调用都会失败
         */
        ret = nativeCheckLicense(context, license);
        if (ret != BEF_RESULT_SUC) {
            mInited = false;
            return ret;
        }

        /**
         * 设置最多检测10张脸，24帧检测一次
         */
        setParam(10, 24);
        mInited = true;
        return ret;
    }

    /**
     * 设置240/280关键点检测模型
     * @param context Android 上下文内容
     * @param extraModelpath 模型文件路径
     * @param extraType 模型类型 BEF_MOBILE_FACE_240_DETECT 或 BEF_MOBILE_FACE_280_DETECT
     * @return 成功返回BEF_RESULT_SUC，否则返回对应的错误码，查看{@link BytedEffectConstants.BytedResultCode}
     */
    public int initExtra(Context context, String extraModelpath, int extraType)
    {
        int ret =  mInited ?BEF_RESULT_SUC : BEF_RESULT_FAIL;
        if(BEF_RESULT_SUC == ret)
        {
            ret = nativeInitExtra(extraType, extraModelpath);
        }
         mInitedExtra = true;
        return ret;
    }

    /**
     * 初始化人脸属性检测句柄
     * @param context Android上下文
     * @param faceAttriModelPath 人脸属性模型文件路径
     * @param faceAttrLicense 人脸属性检测授权文件
     * @return 成功返回BEF_RESULT_SUC，否则返回对应的错误码，查看{@link BytedEffectConstants.BytedResultCode}
     */
    public int initAttri(Context context, String faceAttriModelPath, String faceAttrLicense)
    {
        int ret =  mInited ? BEF_RESULT_SUC : BEF_RESULT_FAIL;
        if(BEF_RESULT_SUC == ret)
        {
            // config 为预留字段 目前没有使用 传0即可
            ret = nativeInitAttri(0, faceAttriModelPath, context, faceAttrLicense);
        }
        mInitedAttri = true;
        return ret;
    }

    /**
     * 设置人脸属性检测参数
     * @param attriConfig
     */
    public void setAttriDetectConfig(int attriConfig)
    {
        mFaceAttriConfig =  attriConfig;
    }

    /**
     * 获取人脸属性检测配置
     * @return 人脸属性检测配置
     */
    public int getFaceAttriConfig() {
        return mFaceAttriConfig;
    }

    /**
     * 获取人脸检测配置
     * @return 人脸检测配置
     */
    public int getFaceDetectConfig() {
        return mFaceDetectConfig;
    }

    /**
     * 设置人脸检测算法配置，该配置必须是Init中使用的配置的子集
     * @param mFaceDetectConfig 人脸检测配置
     */
    public void setFaceDetectConfig(int mFaceDetectConfig) {
        this.mFaceDetectConfig = mFaceDetectConfig;
    }


    /**
     * 人脸检测器是否初始化
     * @return 已初始化返回true,否则返回false
     */
    public boolean isInited() {
        return mInited;
    }

    /**
     * 是否已加载附加关键点模型
     * @return 已加载返回true,否则返回false
     */
    public boolean isInitedExtra() {
        return mInitedExtra;
    }

    /**
     * 是否已加载人脸属性检测模型
     * @return 已加载返回true,否则返回false
     */
    public boolean isInitedAttri() {return mInitedAttri;}

    /**
     * 检测人脸关键点
     * @param buffer 图片数据
     * @param pixel_format 图片数据格式
     * @param image_width 图片宽度
     * @param image_height 图片高度
     * @param image_stride 图片每一行的步长
     * @param orientation 图片旋转角度
     *
     * @return 人脸检测结果
     */
    public BefFaceInfo detectFace(ByteBuffer buffer, BytedEffectConstants.PixlFormat pixel_format, int image_width, int image_height, int image_stride, BytedEffectConstants.Rotation orientation) {
        if (!mInited) {
            return null;
        }
        BefFaceInfo info = new BefFaceInfo();
        int result = nativeDetect(buffer, pixel_format.getValue(), image_width,
                image_height, image_stride, orientation.id, mFaceDetectConfig, info);
        if (result != BEF_RESULT_SUC) {
            Log.e(BytedEffectConstants.TAG, "nativeDetect return "+result);
            return null;
        }
        return info;
    }

    /**
     *销毁人脸关键点检测、人脸属性检测句柄
     */
    public void release() {
        if (mInited) {
            nativeRelease();
        }
        if(mInitedAttri)
        {
            nativeReleaseAttri();
        }
        mInited = false;
        mInitedExtra =false;
        mInitedAttri = false;
    }

    /**
     * 设置人脸检测参数
     * @param maxFaceNum 最大人脸数
     * @param detectInterval 人脸检测间隔的帧数
     * @return 成功返回
     */
    public int setParam(int maxFaceNum , int detectInterval)
    {
        return nativeSetParam(maxFaceNum, detectInterval);
    }

    private native int nativeInit(int config, String modelPath);
    private native int nativeInitExtra(int config, String modelPath);
    private native int nativeInitAttri(int config, String modelPath, Context context, String license);
    private native int nativeCheckLicense(Context context, String license);
    private native int nativeSetParam(int maxFaceNum, int detectInterval);
    private native int nativeDetect(ByteBuffer buffer, int pixel_format,
                                     int image_width,
                                     int image_height,
                                     int image_stride,
                                     int orientation,
                                     long detect_config,
                                     BefFaceInfo faceInfo);
    private native void nativeRelease();
    private native void nativeReleaseAttri();
}
