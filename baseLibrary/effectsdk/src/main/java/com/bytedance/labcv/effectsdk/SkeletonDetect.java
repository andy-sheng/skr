package com.bytedance.labcv.effectsdk;

import android.content.Context;
import android.util.Log;

import static com.bytedance.labcv.effectsdk.BytedEffectConstants.BytedResultCode.*;


import java.nio.ByteBuffer;

/**
 * 人体关键点入口
 */
public class SkeletonDetect {
  private long mNativePtr;

  private volatile boolean mInited = false;

  static {
    System.loadLibrary("effect_proxy");
  }

  /**
   * 初始化骨骼检测句柄
   * @param modelPath 模型文件路径
   * @param licensePath 授权文件路径
   * @return 成功返回BEF_RESULT_SUC，否则返回对应的错误码，参考{@link BytedEffectConstants}
   */

  public int init(Context context, String modelPath, String licensePath) {
    int ret = nativeInit( modelPath);
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
    ret = nativeCheckLicense(context, licensePath);
    if (ret != BEF_RESULT_SUC) {
      mInited = false;
      return ret;
    }
    mInited = true;
    return ret;
  }

  /**
   * 骨骼检测句柄是否初始化成功
   * @return true or false
   */
  public boolean isInited() {
    return mInited;
  }

  /**
   * 骨骼检测
   * @param buffer 图片数据
   * @param pixel_format 图片数据格式
   * @param image_width 图片宽度
   * @param image_height 图片高度
   * @param image_stride 图片数据行宽
   * @param orientation 图片方向
   * @param skeletonNum 可检测最多人体数
   * @return 成功返回BEF_RESULT_SUC，否则返回对应的错误码
   */
  public BefSkeletonInfo detectSkeleton(ByteBuffer buffer, BytedEffectConstants.PixlFormat pixel_format, int image_width, int image_height, int image_stride, BytedEffectConstants.Rotation orientation, int skeletonNum) {
    if (!mInited) {
      return null;
    }

    BefSkeletonInfo skeletonInfo =  new BefSkeletonInfo();
    skeletonInfo.setSkeletonNum(skeletonNum);
    int result = nativeDetect(buffer, pixel_format.getValue(), image_width,
        image_height, image_stride, orientation.id, skeletonInfo);
    if (result != BEF_RESULT_SUC) {
      Log.e(BytedEffectConstants.TAG, "nativeDetect return "+result);
      return null;
    }
    return skeletonInfo;
  }

  /**
   * 销毁骨骼检测句柄
   */
  public void release() {
    if (mInited) {
      nativeRelease();
    }
    mInited = false;
  }

  /**
   * Set skeleton network size 设置骨骼的网络大小
   *    设置的大小，建议值（128， 224），可以使用 96, 168
   *     需要按照 128/224相同比例的设置参数，
   *      设置参数影响性能
   * @param width 网络的宽度
   * @param height 网络的高度
   * @return 成功返回BEF_RESULT_SUC， 否则返回对应的错误码
   */
  public int setParam(int width , int height)
  {
    return nativeSetParam(width, height);
  }

  private native int nativeInit(String modelDir);
  private native int nativeCheckLicense(Context context, String license);
  private native int nativeSetParam(int width, int height);
  private native int nativeDetect(ByteBuffer buffer, int pixel_format,
                                  int image_width,
                                  int image_height,
                                  int image_stride,
                                  int orientation,
                                  BefSkeletonInfo faceInfo);
  private native void nativeRelease();
}
