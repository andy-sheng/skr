package com.bytedance.labcv.effectsdk;

import android.graphics.PointF;

import java.lang.reflect.Array;
import java.util.Arrays;

/**
 * 人体关键点检测结果
 */
public class BefSkeletonInfo {

  /**
   * 人体关键点
   */
  private Skeleton[] skeletons;

  /**
   * 人体数目
   */

  private int skeletonNum;

  /**
   * 获取可检测的最多人体个数
   * @return 最大人体数目
   */
  public int getSkeletonNum() {
    return skeletonNum;
  }

  /**
   * 设置可检测的最大人体数目
   * @param skeletonNum
   */
  public void setSkeletonNum(int skeletonNum) {
    this.skeletonNum = skeletonNum;
  }

  /**
   * 获取全部的关键点数组
   * @return 人体关键点数组
   */
  public Skeleton[] getSkeletons() {
    if(skeletons == null)
      return new Skeleton[0];
    return skeletons;
  }

  @Override
  public String toString() {
    return "Skeleton Num: " + skeletonNum + ", Skeletons: " + Arrays.toString(skeletons);
  }

  /**
   * 单个人体关键点数据
   */
  public static class Skeleton
  {
    SkeletonPoint[] keypoints;

    /**
     * 获取人体关键点
     * @return 人体关键点数组
     */
    public SkeletonPoint[] getKeypoints() {
      if(keypoints == null)
        return new SkeletonPoint[0];
      return keypoints;
    }

    @Override
    public String toString() {
      return Arrays.toString(keypoints);
    }
  }

  /**
   * 人体关键点
   */
  public static class SkeletonPoint
  {
    float x;
    float y;
    boolean is_detect;


    public SkeletonPoint(float x, float y, boolean is_detect)
    {
      this.x = x;
      this.y = y;
      this.is_detect = is_detect;
    }

    public void setIs_detect(boolean is_detect) {
      this.is_detect = is_detect;
    }

    public boolean isDetect() {
      return is_detect;
    }

    public float getX() {
      return x;
    }

    public void setX(float x) {
      this.x = x;
    }

    public float getY() {
      return y;
    }

    public void setY(float y) {
      this.y = y;
    }

    public PointF asPoint() {
      return new PointF(x, y);
    }

    @Override
    public String toString() {
      return "FacePoint{" +
          "x=" + x +
          ", y=" + y +
          ", isdetect=" + String.valueOf(is_detect) +
          '}';
    }
  }

}
