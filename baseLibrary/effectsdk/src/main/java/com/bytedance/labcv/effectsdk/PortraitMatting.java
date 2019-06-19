package com.bytedance.labcv.effectsdk;

import android.content.Context;
import android.util.Log;

import java.nio.ByteBuffer;

import static com.bytedance.labcv.effectsdk.BytedEffectConstants.BytedResultCode.BEF_RESULT_FAIL;
import static com.bytedance.labcv.effectsdk.BytedEffectConstants.BytedResultCode.BEF_RESULT_SUC;

/**
 * 人体分割入口
 */
public class PortraitMatting {


  static {
    System.loadLibrary("effect_proxy");
  }

  /**
   * 人体分割结果， 单通道灰度图
   */
  public class MattingMask
  {
    private int width;
    private int height;
    private byte[] buffer;

    public byte[] getBuffer() {
      return buffer;
    }

    public int getWidth() {
      return width;
    }

    public int getHeight()
    {
      return height;
    }

    @Override
    public String toString() {
      return String.format("l: %d w:%d, h:%d", buffer.length, width, height);
    }
  }

  private long mNativePtr;
  private boolean inited = false;

  /**
   *  初始化人体分割句柄
   * @param context 应用上下文
   * @param modelpath 模型文件绝对路径
   * @param modelType 使用模型类型，
   *         {@link BytedEffectConstants}
   *         BEF_PORTAITMATTING_LARGE_MODEL(0), 大模型，耗时比小模型稍长；
   *         BEF_PORTAITMATTING_SMALL_MODEL(1); 小模型,较快；
   * @param licensePath 授权文件绝对路径
   * @return 成功返回BEF_RESULT_SUC，其他返回值参考{@link BytedEffectConstants}
   *
   */
  public int init(Context context, String modelpath, BytedEffectConstants.PortraitMatting modelType, String licensePath)
  {
    int retStatu = BEF_RESULT_FAIL;
    if(!inited)
    {
      retStatu = nativeCreateHandle();
      if(retStatu == BEF_RESULT_SUC)
        retStatu = nativeCheckLicense(context, licensePath);
      if(retStatu == BEF_RESULT_SUC)
      {
        retStatu = nativeInit(modelpath, modelType.getValue());
        setParam(BytedEffectConstants.PorraitMattingParamType.BEF_MP_EdgeMode, 1);
      }
      inited = retStatu == BEF_RESULT_SUC;
    }
    return retStatu;
  }

  /**
   *  释放人像分割句柄
   */
  public void release()
  {
    if(inited)
    {
      nativeRelease();
    }
    inited = false;
  }

  /**
   *  人体分割接口
   * @param imgdata 输入图像数据
   * @param pixel_format  输入图像数据格式
   * @param width  输入图像宽
   * @param height 输入图像高
   * @param stride 输入图像步长
   * @param orientation 输入图像方向
   * @param needflipalpha 输出图像是否需要翻转
   * @return @link MattingMask Matting 结果， 单通道灰度图
   */
  public MattingMask detectMatting(ByteBuffer imgdata,BytedEffectConstants.PixlFormat pixel_format,
                           int width, int height, int stride,BytedEffectConstants.Rotation orientation,
                           boolean needflipalpha)
  {
    int result = BEF_RESULT_FAIL;
    MattingMask mattingMask = new MattingMask();
    result = nativeMatting(imgdata, pixel_format.getValue(), width, height, stride, orientation.id, needflipalpha, mattingMask);
    if (result != BEF_RESULT_SUC) {
      Log.e(BytedEffectConstants.TAG, "nativeMatting return "+result);
      return null;
    }
    return mattingMask;
  }


  /**
   * 设置 sdk 参数
   * @param paramType @link BytedEffectConstants.PorraitMattingParamType sdk设置参数类型
   * @param paramValue sdk设置参数值，具体 @see BytedEffectConstants.PorraitMattingParamType
   * @return 成功返回BEF_RESULT_SUC，否则返回对应的错误码，参考{@link BytedEffectConstants}
   */
  public int setParam(BytedEffectConstants.PorraitMattingParamType paramType , int paramValue)
  {
    return nativeSetParam(paramType.getValue(), paramValue);
  }


  private native int nativeCreateHandle();
  private native int nativeInit(String modelPath, int modleType);
  private native int nativeCheckLicense(Context context, String licensePath);
  private native int nativeMatting(ByteBuffer imgdata,int pixelformat,
                                       int width, int height, int stride, int orientation,
                                       boolean needflipalpha, MattingMask mattingMask);
  private native int nativeSetParam(int paramType, int paramValue);
  private native int nativeRelease();
}
