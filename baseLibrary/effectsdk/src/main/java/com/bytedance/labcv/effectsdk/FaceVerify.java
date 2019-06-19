package com.bytedance.labcv.effectsdk;

import android.content.Context;
import android.util.Log;

import java.nio.ByteBuffer;

import static com.bytedance.labcv.effectsdk.BytedEffectConstants.BytedResultCode.BEF_RESULT_SUC;

/**
 * 人脸比对入口
 * FaceVerify Instance
 */
public class FaceVerify {
    private long mNativePtr;

    private volatile boolean mInited = false;

    static {
        System.loadLibrary("effect_proxy");
    }

    /**
     * 初始化人脸比对
     * @param context 上下文
     * @param faceModel 人脸106点模型的文件路径
     * @param faceVerifyModel 人脸比对模型文件的路径
     * @param maxFaceNum  最大人脸数  不超过10
     * @param license 授权文件的路径
     * @return 成功返回BEF_RESULT_SUC，否则返回对应的错误码，参考{@link BytedEffectConstants}
     */
    public int init(Context context, String faceModel,String faceVerifyModel,int maxFaceNum, String license){
        int ret = nativeCreateHandle(faceModel, faceVerifyModel, maxFaceNum);
        if (ret != BEF_RESULT_SUC) {
            mInited = false;
            return ret;
        }

        /**
         *  初始化后必须checklicense，否则后续调用都会失败
         */
        ret = nativeCheckLicense(context, license);

        if (ret != BEF_RESULT_SUC) {
            mInited = false;
            return ret;
        }
        mInited = true;
        return ret;

    }

    /**
     * 人脸特征提取
     * @param buffer 图片数据
     * @param pixel_format 图片数据格式
     * @param image_width 图片宽度
     * @param image_height 图片高度
     * @param image_stride 图片每一行的步长
     * @param orientation 图片旋转角度 旋转多少度图中人脸为正
     *
     * @return 人脸特征结果
     */
    public BefFaceFeature extractFeature(ByteBuffer buffer, BytedEffectConstants.PixlFormat pixel_format, int image_width, int image_height, int image_stride, BytedEffectConstants.Rotation orientation){
        if (!mInited) {
            return null;
        }
        BefFaceFeature info = new BefFaceFeature();
        int result = nativeExtractFeature(buffer, pixel_format.getValue(), image_width,
                image_height, image_stride, orientation.id,  info);
        if (result != BEF_RESULT_SUC) {
            Log.e(BytedEffectConstants.TAG, "nativeVerifyFace return "+result);
            return null;
        }
        return info;

    }

    /**
     * 人脸特征比对
     * @param feature1 人脸特征向量
     * @param feature2 人脸特征向量
     * @return 特征距离
     */
    public double verify(float[] feature1, float[] feature2){
        return nativeVerify(feature1, feature2);
    }

    /**
     *  计算相似度
     * @param d 特征距离
     * @return 相似度值
     */
    public double distToScore(double d){
        return nativeDistanceToScore(d);
    }

    /**
     *销毁人脸比对句柄
     */
    public void release() {
        if (mInited) {
            nativeRelease();
        }
        mInited = false;
    }

    private native int nativeCreateHandle(String faceModelPath,String faceVerifyModel, int maxfaceNum);
    private native int nativeCheckLicense(Context context, String license);
    private native int nativeExtractFeature(ByteBuffer buffer,
                                    int pixel_format,
                                    int image_width,
                                    int image_height,
                                    int image_stride,
                                    int orientation,
                                    BefFaceFeature faceFeature);

    private native double nativeVerify(float[] feature1, float[] feature2);
    private native double nativeDistanceToScore(double dist);
    private native void nativeRelease();
}
