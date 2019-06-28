// Copyright (C) 2018 Beijing Bytedance Network Technology Co., Ltd.
package com.bytedance.labcv.effectsdk;

import android.graphics.PointF;
import java.util.Arrays;

/**
 * 人脸检测结果定义
 */
public class BefFaceInfo {

    private Face106[] face106s;

    private ExtraInfo[] extras;

    private FaceAttri[] attris;

    /**
     * 获取人脸属性结果
     * @return 人脸属性数组
     */
    public FaceAttri[] getAttris() {
        if( null == attris)
            return new FaceAttri[0];
        return attris;
    }

    /**
     * 获取人脸106关键点结果
     * @return 106关键点数组
     */
    public Face106[] getFace106s() {
        return face106s != null ? face106s : new Face106[0];
    }

    /**
     * 获取附加关键点信息
     * @return 附加关键点信息
     */
    public ExtraInfo[] getExtras() {
        if(null == extras)
            return new ExtraInfo[0];
        return extras;
    }


    @Override
    public String toString() {
        return "BefFaceInfo{" +
                "face106s=" + Arrays.toString(face106s) +
                ", extras=" + Arrays.toString(extras) +
                '}';
    }

    public static class FacePoint {
        float x;
        float y;

        FacePoint(float x, float y) {
            this.x = x;
            this.y = y;
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
                    '}';
        }
    }

    /**
     * 人脸位置框
     */
    public static class FaceRect {
        int left, right, top, bottom;

        FaceRect(int left, int top, int right, int bottom) {
            this.left = left;
            this.right = right;
            this.top = top;
            this.bottom = bottom;
        }

        public int getLeft() {
            return left;
        }

        public void setLeft(int left) {
            this.left = left;
        }

        public int getRight() {
            return right;
        }

        public void setRight(int right) {
            this.right = right;
        }

        public int getTop() {
            return top;
        }

        public void setTop(int top) {
            this.top = top;
        }

        public int getBottom() {
            return bottom;
        }

        public void setBottom(int bottom) {
            this.bottom = bottom;
        }

        @Override
        public String toString() {
            return "FaceRect{" +
                    "left=" + left +
                    ", top=" + top +
                    ", right=" + right +
                    ", bottom=" + bottom +
                    '}';
        }
    }

    /**
     * 人脸106关键点
     */
    public static class Face106 {
        /**
         * 人脸框
         */
        FaceRect rect;
        float score;                  // 置信度
        FacePoint[] points_array; // 人脸106关键点的数组
        float[] visibility_array;  // 对应点的能见度，点未被遮挡1.0, 被遮挡0.0
        float yaw;                    // 水平转角,真实度量的左负右正
        float pitch;                  // 俯仰角,真实度量的上负下正
        float roll;                   // 旋转角,真实度量的左负右正
        float eye_dist;               // 两眼间距
        int action;          // 动作
        int ID;                       // faceID: 每个检测到的人脸拥有唯一的faceID.人脸跟踪丢失以后重新被检测到,会有一个新的faceID

        public FaceRect getRect() {
            return rect;
        }


        public float getScore() {
            return score;
        }


        public FacePoint[] getPoints_array() {
            return points_array;
        }


        public float[] getVisibility_array() {
            return visibility_array;
        }


        public float getYaw() {
            return yaw;
        }


        public float getPitch() {
            return pitch;
        }


        public float getRoll() {
            return roll;
        }


        public float getEye_dist() {
            return eye_dist;
        }


        public int getAction() {
            return action;
        }


        public int getID() {
            return ID;
        }


        @Override
        public String toString() {
            return "Face106{" +
                    "rect=" + rect +
                    ", score=" + score +
                    ", points_array=" + Arrays.toString(points_array) +
                    ", visibility_array=" + Arrays.toString(visibility_array) +
                    ", yaw=" + yaw +
                    ", pitch=" + pitch +
                    ", roll=" + roll +
                    ", eye_dist=" + eye_dist +
                    ", action=" + action +
                    ", ID=" + ID +
                    '}';
        }
    }

    /**
     * 附加关键点信息
     */
    public static class ExtraInfo {
        int eye_count;                  // 检测到眼睛数量
        int eyebrow_count;              // 检测到眉毛数量
        int lips_count;                 // 检测到嘴唇数量
        int iris_count;                 // 检测到虹膜数量

        public final static int EYE_POINTS_NUM = 22;
        public final static int EYE_BROW_POINTS_NUM = 13;
        public final static int LIP_POINTS_NUM = 64;
        public final static int EYE_IRIS_POINTS_NUM = 20;

        FacePoint[] eye_left;        // 左眼关键点, length 22
        FacePoint[] eye_right;       // 右眼关键点, length 22
        FacePoint[] eyebrow_left;    // 左眉毛关键点, length 13
        FacePoint[] eyebrow_right;   // 右眉毛关键点, length 13
        FacePoint[] lips;            // 嘴唇关键点, length 64
        FacePoint[] left_iris;       // 左虹膜关键点, length 20
        FacePoint[] right_iris;      // 右虹膜关键点, length 20

        public FacePoint[] getLips() {
            if(null == lips)
                return new FacePoint[0];
            return lips;
        }

        public FacePoint[] getEye_left() {
            if(null == eye_left)
                return new FacePoint[0];
            return eye_left;
        }

        public FacePoint[] getEye_right() {
            if(null == eye_right)
                return new FacePoint[0];
            return eye_right;
        }

        public FacePoint[] getEyebrow_left() {
            if(null == eyebrow_left)
                return new FacePoint[0];
            return eyebrow_left;
        }

        public FacePoint[] getEyebrow_right() {
            if(null == eyebrow_right)
                return new FacePoint[0];
            return eyebrow_right;
        }

        public FacePoint[] getLeft_iris() {
            if(null == left_iris)
                return new FacePoint[0];
            return left_iris;
        }

        public FacePoint[] getRight_iris() {
            if(null == right_iris)
                return new FacePoint[0];
            return right_iris;
        }

        @Override
        public String toString() {
            return "ExtraInfo{" +
                    "eye_count=" + eye_count +
                    ", eyebrow_count=" + eyebrow_count +
                    ", lips_count=" + lips_count +
                    ", iris_count=" + iris_count +
                    ", eye_left=" + Arrays.toString(eye_left) +
                    ", eye_right=" + Arrays.toString(eye_right) +
                    ", eyebrow_left=" + Arrays.toString(eyebrow_left) +
                    ", eyebrow_right=" + Arrays.toString(eyebrow_right) +
                    ", lips=" + Arrays.toString(lips) +
                    ", left_iris=" + Arrays.toString(left_iris) +
                    ", right_iris=" + Arrays.toString(right_iris) +
                    '}';
        }
    }

    /**
     * 人脸属性结果
     */
    public static class FaceAttri
    {
        float age = 0.0f;
        float boy_prob = 0.0f;
        float attractive = 0.0f;
        float happy_score = 0.0f;
        int expression_type = 0;
        float[] exp_probs;
        int racial_type = 0;
        float[] rocial_probs;

        public float getAge() {
            return age;
        }

        public void setAge(float age) {
            this.age = age;
        }

        public float getBoy_prob() {
            return boy_prob;
        }

        public void setBoy_prob(float boy_prob) {
            this.boy_prob = boy_prob;
        }

        public float getHappy_score() {
            return happy_score;
        }

        public void setHappy_score(float happy_score) {
            this.happy_score = happy_score;
        }

        public int getExpression_type() {
            return expression_type;
        }

        public void setExpression_type(int expression_type) {
            this.expression_type = expression_type;
        }

        public float getAttractive() {
            return attractive;
        }

        public void setAttractive(float attractive) {
            this.attractive = attractive;
        }

        public float[] getExp_probs() {
            if(null == exp_probs)
                return new float[0];
            return exp_probs;
        }

        public void setExp_probs(float[] exp_probs) {
            this.exp_probs = exp_probs;
        }

        public float[] getRocial_probs() {
            if(null == rocial_probs)
                return new float[0];
            return rocial_probs;
        }

      public int getRacial_type() {
        return racial_type;
      }

      public void setRacial_type(int racial_type) {
            this.racial_type = racial_type;
        }

        public void setRocial_probs(float[] rocial_probs) {
            this.rocial_probs = rocial_probs;
        }
    }
}
