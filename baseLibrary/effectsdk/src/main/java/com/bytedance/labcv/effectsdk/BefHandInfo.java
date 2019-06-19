package com.bytedance.labcv.effectsdk;

import android.graphics.PointF;
import android.graphics.Rect;

import java.util.Arrays;

/**
 * 人手检测结果定义
 */
public class BefHandInfo {
    // 检测到的hand的数量
    private int handCount = 0;

    // 检测到的手信息
    private BefHand[] hands;

    public int getHandCount() {
        return handCount;
    }

    public BefHand[] getHands() {
        return hands;
    }


    @Override
    public String toString() {
        return "BefHandInfo{" +
                "hands=" + Arrays.toString(hands) +
                ", handCount=" +handCount +
                '}';
    }

    /**
     * 单个人手模型
     *
     */
    public static class BefHand{

        // 手的id
        private int id;

        // 手部的矩形框
        private Rect rect;

        // 手部动作 bef_hand_types[]的index [0--20)
        private int action;

        // 手部旋转角度 仅手张开时比较准确
        private float rotAngle;

        // 手部动作置信度
        private float score;

        // 双手夹角
        private float rotAngleBothhand;

        // 手部关键点, 如果没有检测到，则置为0
        private BefKeyPoint[] keyPoints;

        // 手部扩展点，如果没有检测到，则置为0
        private BefKeyPoint[] keyPointsExt;
        // 动态手势 1 击拳 2 鼓掌
        private int seqAction;

        public int getId() {
            return id;
        }

        public Rect getRect() {
            return rect;
        }

        public int getAction() {
            return action;
        }

        public float getRotAngle() {
            return rotAngle;
        }

        public float getScore() {
            return score;
        }

        public float getRotAngleBothhand() {
            return rotAngleBothhand;
        }

        public BefKeyPoint[] getKeyPoints() {
            return keyPoints;
        }

        public BefKeyPoint[] getKeyPointsExt() {
            return keyPointsExt;
        }

        public int getSeqAction() {
            return seqAction;
        }

        @Override
        public String toString() {
            return "BefHand{" + "id ="+id + " rect = "+rect.toString() +
                    " action ="+action + " rotAngle ="+rotAngle + " score ="+ score + " rotAngleBothhand ="+rotAngleBothhand
                      +  "}";

        }
    }

    /**
     * 关键点
     */
    public static class BefKeyPoint{
        float x; // 对应 cols, 范围在 [0, width] 之间
        float y; // 对应 rows, 范围在 [0, height] 之间
        boolean is_detect; // 如果该值为 false, 则 x,y 无意义

        public BefKeyPoint(float x, float y, boolean is_detect) {
            this.x = x;
            this.y = y;
            this.is_detect = is_detect;
        }
        public PointF asPoint() {
            return new PointF(x, y);
        }

        @Override
        public String toString() {
            return "BefKeyPoint { x ="+x + " y ="+ y + " is_detect ="+is_detect + "}";
        }
    }



}


