// Copyright (C) 2018 Beijing Bytedance Network Technology Co., Ltd.
package com.bytedance.labcv.effectsdk;

public class BytedEffectConstants {

    public static final String TAG = "bef_effect_ai";

    /**
     * 可检测的最多人体数目
     */
    public final static int BEF_SKELETON_MAX_NUM = 2;

    /**
     * faster face detection algorithm  更快的人脸检测模型
     */
    public static final int BEF_DETECT_SMALL_MODEL = 0x00200000;

    /**
     * 错误码枚举
     */
    public static class BytedResultCode {

        /**
         * 成功返回
         */
        public static final int BEF_RESULT_SUC = 0;

        /**
         * 内部错误
         */
        public static final int BEF_RESULT_FAIL = -1;

        /**
         * 文件没找到
         */
        public static final int BEF_RESULT_FILE_NOT_FIND = -2;

        /**
         * 数据错误
         */
        public static final int BEF_RESULT_FAIL_DATA_ERROR = -3;

        /**
         * 无效的句柄
         */
        public static final int BEF_RESULT_INVALID_HANDLE = -4;

        /**
         * 无效的授权
         */
        public static final int BEF_RESULT_INVALID_LICENSE = -114;

        /**
         * 无效的图片格式
         */
        public static final int BEF_RESULT_INVALID_IMAGE_FORMAT = -7;

        /**
         * 模型加载失败
         */
        public static final int BEF_RESULT_MODEL_LOAD_FAILURE = -8;

    }


    /**
     * 图像格式
     */
    public enum PixlFormat {
        RGBA8888(0),
        BGRA8888(1),
        BGR888(2),
        RGB888(3),
        BEF_AI_PIX_FMT_YUV420P(5),// 暂不支持直接输入yuv格式数据
        BEF_AI_PIX_FMT_NV12(6), //暂不支持直接输入yuv格式数据
        BEF_AI_PIX_FMT_NV21(7); // 暂不支持直接输入yuv格式数据
        private int value;

        PixlFormat(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }


    /**
     * 图像旋转角
     */
    public enum Rotation {
        /**
         * 图像不需要旋转，图像中的人脸为正脸
         */
        CLOCKWISE_ROTATE_0(0),
        /**
         * 图像需要顺时针旋转90度，使图像中的人脸为正
         */
        CLOCKWISE_ROTATE_90(1),
        /**
         * 图像需要顺时针旋转180度，使图像中的人脸为正
         */
        CLOCKWISE_ROTATE_180(2),
        /**
         * 图像需要顺时针旋转270度，使图像中的人脸为正
         */
        CLOCKWISE_ROTATE_270(3);

        public int id = 0;

        Rotation(int id) {
            this.id = id;
        }
    }

    /**
     * 检测模式枚举
     */
    public static class DetectMode {
        /**
         * video mode 视频模式
         */
        public static final int BEF_DETECT_MODE_VIDEO = 0x00020000;
        /**
         * image detect 图片模式
         */
        public static final int BEF_DETECT_MODE_IMAGE = 0x00040000;
    }


    /**
     * 人脸动作枚举
     */
    public static class FaceAction {

        /**
         * 106 key points face detect, 106 点人脸检测
         */
        public static final int BEF_FACE_DETECT = 0x00000001;
        /**
         * eye blink, 眨眼
         */
        public static final int BEF_EYE_BLINK = 0x00000002;
        /**
         * mouth open, 嘴巴大张
         */
        public static final int BEF_MOUTH_AH = 0x00000004;
        /**
         * shake head, 摇头
         */
        public static final int BEF_HEAD_YAW = 0x00000008;
        /**
         * nod, 点头
         */
        public static final int BEF_HEAD_PITCH = 0x00000010;
        /**
         * brow jump, 眉毛挑动
         */
        public static final int BEF_BROW_JUMP = 0x00000020;
        /**
         * pout, 嘴巴嘟嘴
         */
        public static final int BEF_MOUTH_POUT = 0x00000040;
        /**
         * 检测上面所有的动作
         */
        public static final int BEF_DETECT_FULL = 0x0000007F;

    }

    /**
     * 人脸附加关键点模型类型
     */
    public class FaceExtraModel {
        /**
         * 检测二级关键点: 眉毛, 眼睛, 嘴巴
         */
        public static final int BEF_MOBILE_FACE_240_DETECT = 0x00000700;
        /**
         * 检测二级关键点: 眉毛, 眼睛, 嘴巴，虹膜
         */
        public static final int BEF_MOBILE_FACE_280_DETECT = 0x00000F00;
    }


    /**
     * 强度类型
     */
    public enum IntensityType {
        /**
         * 调节滤镜
         */
        Filter(12),
        /**
         * 调节美白
         */
        BeautyWhite(1),
        /**
         * 调节磨皮
         */
        BeautySmooth(2),
        /**
         * 同时调节瘦脸和大眼
         */
        FaceReshape(3),
        /**
         * 调节锐化
         */
        BeautySharp(9),
        /**
         * 唇色
         */
        MakeUpLip(17),
        /**
         * 腮红
         */
        MakeUpBlusher(18);

        private int id;

        public int getId() {
            return id;
        }

        IntensityType(int id) {
            this.id = id;
        }
    }

    /**
     * 手势相关模型类型
     */
    public enum HandModelType {
        /**
         * 检测手，必须加载
         */
        BEF_HAND_MODEL_DETECT(0x0001),
        /**
         * 检测手位置框，必须加载
         */
        BEF_HAND_MODEL_BOX_REG(0x0002),
        /**
         * 手势分类，可选
         */
        BEF_HAND_MODEL_GESTURE_CLS(0x0004),
        /**
         * 手关键点，可选
         */
        BEF_HAND_MODEL_KEY_POINT(0x0008);
        private int value;

        HandModelType(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }




    /**
     * 人脸属性值枚举
     */
    public static class FaceAttribute {
        /**
         * 年龄
         */
        public static final int BEF_FACE_ATTRIBUTE_AGE = 0x00000001;
        /**
         * 性别
         */
        public static final int BEF_FACE_ATTRIBUTE_GENDER = 0x00000002;
        /**
         * 表情
         */
        public static final int BEF_FACE_ATTRIBUTE_EXPRESSION = 0x00000004;
        /**
         * 颜值
         */
        public static final int BEF_FACE_ATTRIBUTE_ATTRACTIVE = 0x00000008;
        /**
         * 开心程度
         */
        public static final int BEF_FACE_ATTRIBUTE_HAPPINESS = 0x00000010;
        /**
         * 肤色
         */
        public static final int BEF_FACE_ATTRIBUTE_RACIAL = 0x00000020;
    }

    /**
     * 人脸肤色枚举
     */
    public static class FaceRacial {
        /**
         * 白种人
         */
        public static final int BEF_FACE_ATTRIBUTE_WHITE = 0;
        /**
         * 黄种人
         */
        public static final int BEF_FACE_ATTRIBUTE_YELLOW = 1;
        /**
         * 印度人
         */
        public static final int BEF_FACE_ATTRIBUTE_INDIAN = 2;
        /**
         * 黑种人
         */
        public static final int BEF_FACE_ATTRIBUTE_BLACK = 3;
        /**
         * 支持的肤色个数
         */
        public static final int BEF_FACE_ATTRIBUTE_NUM_RACIAL = 4;
    }


    /**
     * 人脸属性 表情值枚举
     */
    public static class FaceExpression {
        /**
         * 生气
         */
        public static final int BEF_FACE_ATTRIBUTE_ANGRY = 0;
        /**
         * 厌恶
         */
        public static final int BEF_FACE_ATTRIBUTE_DISGUST = 1;
        /**
         * 害怕
         */
        public static final int BEF_FACE_ATTRIBUTE_FEAR = 2;
        /**
         * 高兴
         */
        public static final int BEF_FACE_ATTRIBUTE_HAPPY = 3;
        /**
         * 伤心
         */
        public static final int BEF_FACE_ATTRIBUTE_SAD = 4;
        /**
         * 吃惊
         */
        public static final int BEF_FACE_ATTRIBUTE_SURPRISE = 5;
        /**
         * 平静
         */
        public static final int BEF_FACE_ATTRIBUTE_NEUTRAL = 6;
        /**
         * 支持的表情个数
         */
        public static final int BEF_FACE_ATTRIBUTE_NUM_EXPRESSION = 7;
    }


    /**
     * 手势检测参数类型
     */
    public enum HandParamType {
        /**
         * 设置最多的手的个数，默认为1，目前最多设置为2
         */
        BEF_HAND_MAX_HAND_NUM(2),
        /**
         * 设置检测的最短边长度, 默认192
         */
        BEF_HAND_DETECT_MIN_SIDE(3),
        /**
         * 设置分类平滑参数，默认0.7， 数值越大分类越稳定
         */
        BEF_HAND_CLS_SMOOTH_FACTOR(4),
        /**
         * 设置是否使用类别平滑，默认1，使用类别平滑；不使用平滑，设置为0
         */
        BEF_HAND_USE_ACTION_SMOOTH(5),
        /**
         * 降级模式，在低端机型使用，可缩短算法执行时间，但准确率也会降低。默认为0，使用高级模式；如设置为1，则使用降级模式。
         */
        BEF_HAND_ALGO_LOW_POWER_MODE(6),
        /**
         * 自动降级模式，如果检测阈值超过BEF_HAND_ALGO_TIME_ELAPSED_THRESHOLD，则走降级模式，否则，走高级模式。
         * 默认为0，使用高级模式；如设置为1，则使用自动降级模式
         */
        BEF_HAND_ALGO_AUTO_MODE(7),
        /**
         * 算法耗时阈值，默认为 15ms
         */
        BEF_HAND_ALGO_TIME_ELAPSED_THRESHOLD(8),
        /**
         * 设置运行时测试算法的执行的次数, 默认是 150 次
         */
        BEF_HAND_ALGO_MAX_TEST_FRAME(9),
        /**
         * 设置是否使用双手手势， 默认为true
         */
        BEF_HAND_IS_USE_DOUBLE_GESTURE(10),
        /**
         * 设置回归模型的输入初始框的放大比列
         */
        BEF_HNAD_ENLARGE_FACTOR_REG(11);


        private int value;

        HandParamType(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

    }

    /**
     * 人体分割参数
     */
    public enum PortraitMatting
    {
        BEF_PORTAITMATTING_LARGE_MODEL(0),
        BEF_PORTAITMATTING_SMALL_MODEL(1);

        private int value;
        PortraitMatting(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    public enum PorraitMattingParamType
    {
        /**
         *  算法参数，用来设置边界的模式
         *       - 0: 不加边界
         *       - 1: 加边界
         *       - 2: 加边界, 其中, 2 和 3 策略不太一样，但效果上差别不大，可随意取一个
         */
        BEF_MP_EdgeMode(0),
        /**
         * 算法参数，设置调用多少次强制做预测，目前设置 15 即可
         */
        BEF_MP_FrashEvery(1),
        /**
         * 返回短边的长度, 默认值为128, 需要为16的倍数；
         */
        BEF_MP_OutputMinSideLen(2);

        private int value;

        PorraitMattingParamType(int v){this.value = v;}

        public int getValue(){return value;}
    }
}
