// Copyright (C) 2019 Beijing Bytedance Network Technology Co., Ltd.
#ifndef _BEF_EFFECT_FACE_DETECT_AI_H_
#define _BEF_EFFECT_FACE_DETECT_AI_H_

#if defined(__ANDROID__) || defined(TARGET_OS_ANDROID)
#include<jni.h>
#endif

#include "bef_effect_ai_public_define.h"


// Config when creating detect handle
#define BEF_DETECT_LARGE_MODEL 0x00100000  // higher accuracy 初始化参数, 更准
#define BEF_DETECT_SMALL_MODEL 0x00200000  // faster detection algorithm  初始化参数, 更快

// Set detect mode
#define BEF_DETECT_MODE_VIDEO  0x00020000  // video detect, 视频检测
#define BEF_DETECT_MODE_IMAGE  0x00040000  // image detect, 图片检测

// Actioin definition
#define BEF_FACE_DETECT 0x00000001  // 106 key points face detect, 106 点人脸检测
#define BEF_EYE_BLINK   0x00000002  // eye blink, 眨眼
#define BEF_MOUTH_AH    0x00000004  // mouth open, 嘴巴大张
#define BEF_HEAD_YAW    0x00000008  // shake head, 摇头
#define BEF_HEAD_PITCH  0x00000010  // nod, 点头
#define BEF_BROW_JUMP   0x00000020  // wiggle eyebrow, 眉毛挑动
#define BEF_MOUTH_POUT  0x00000040  // 嘴巴嘟嘴
#define BEF_DETECT_FULL 0x0000007F  // 检测上面所有的特征

#define TT_MOBILE_FACE_240_DETECT 0x00000700  // 检测二级关键点: 眉毛, 眼睛, 嘴巴，初始化+预测参数
#define TT_MOBILE_FACE_280_DETECT 0x00000F00  // 检测二级关键点: 眉毛, 眼睛, 嘴巴，虹膜，初始化+预测参数


#define BEF_FACE_OK                      0   // 结果正确
#define BEF_FACE_E_INTERNAL             -101 // 未知错误
#define BEF_FACE_E_NOT_INITED           -102 // 未初始化相关资源
#define BEF_FACE_E_MALLOC               -103 // 申请内存失败
#define BEF_FACE_E_INVALID_PARAM        -104 // 无效参数
#define BEF_FACE_E_INFERENCE            -105 // CNN预测错误
#define BEF_FACE_E_IMAGE_PROC           -106 // 图像处理错误
#define BEF_FACE_E_INVALID_CONFIG       -107 // 无效的配置
#define BEF_FACE_E_INVALID_HANDLE       -108 // 无效的句柄
#define BEF_FACE_E_INVALID_MODEL        -109 // 无效的模型
#define BEF_FACE_E_INVALID_PIXEL_FORMAT -110 // 无效的图像格式
#define BEF_FACE_E_INVALID_POINTER      -111 // 无效的指针
#define BEF_FACE_E_REQUIRE_FEATURE_NOT_INIT  -112 // 特征无初始化

//bef_effect_public_face_define
#define BEF_MAX_FACE_NUM  10

// 眼睛,眉毛,嘴唇详细检测结果, 280点结果
typedef struct bef_ai_face_ext_info_t {
    int eye_count;                  // 检测到眼睛数量
    int eyebrow_count;              // 检测到眉毛数量
    int lips_count;                 // 检测到嘴唇数量
    int iris_count;                 // 检测到虹膜数量
    
    bef_ai_fpoint eye_left[22];        // 左眼关键点
    bef_ai_fpoint eye_right[22];       // 右眼关键点
    bef_ai_fpoint eyebrow_left[13];    // 左眉毛关键点
    bef_ai_fpoint eyebrow_right[13];   // 右眉毛关键点
    bef_ai_fpoint lips[64];            // 嘴唇关键点
    bef_ai_fpoint left_iris[20];       // 左虹膜关键点
    bef_ai_fpoint right_iris[20];      // 右虹膜关键点
} bef_ai_face_ext_info;


// 供106点使用
typedef struct bef_ai_face_106_st {
    bef_ai_rect rect;                // 代表面部的矩形区域
    float score;                  // 置信度
    bef_ai_fpoint points_array[106]; // 人脸106关键点的数组
    float visibility_array[106];  // 对应点的能见度，点未被遮挡1.0, 被遮挡0.0
    float yaw;                    // 水平转角,真实度量的左负右正
    float pitch;                  // 俯仰角,真实度量的上负下正
    float roll;                   // 旋转角,真实度量的左负右正
    float eye_dist;               // 两眼间距
    int ID;                       // faceID: 每个检测到的人脸拥有唯一的faceID.人脸跟踪丢失以后重新被检测到,会有一个新的faceID
    unsigned int action;          // 动作, 定义在 bef_ai_effect_face_detect.h 里
    unsigned int tracking_cnt;
} bef_ai_face_106, *p_bef_ai_face_106;


// @brief 检测结果
typedef struct bef_ai_face_info_st {
    bef_ai_face_106 base_infos[BEF_MAX_FACE_NUM];          // 检测到的人脸信息
    bef_ai_face_ext_info extra_infos[BEF_MAX_FACE_NUM];    // 眼睛，眉毛，嘴唇关键点等额外的信息
    int face_count;                                     // 检测到的人脸数目
} bef_ai_face_info, *p_bef_ai_face_info;

// brief 算法格外参数设置
typedef struct bef_ai_face_image_st {
    bef_ai_face_106 base_info;         // 检测到的人脸信息
    bef_ai_face_ext_info extra_info;   // 眼睛，眉毛，嘴唇关键点等额外的信息
    unsigned int texture_id;          // 基于人脸位置的截图（已补充额头部分、已旋正）
    bef_ai_pixel_format pixel_format;  // 截图格式，目前均为RGBA
    int image_width;                // 截图像素宽度
    int image_height;               // 截图像素高度
    int image_stride;               // 截图行跨度
} bef_ai_face_image_st, *p_bef_ai_face_image_st;

typedef struct bef_ai_photo_face_image_info_st {
    bef_ai_face_image_st image_infos[BEF_MAX_FACE_NUM];    // 检测到的人脸信息和截图
    int face_count;                                     // 检测到的人脸数目
} bef_ai_photo_face_image_info_st, *p_bef_ai_photo_face_image_info_st;

typedef struct bef_ai_face_filter_range_st { // 过滤条件范围
    float min_value;        // 过滤条件最小值
    float max_value;        // 过滤条件最大值
} bef_ai_face_filter_range_st;

typedef struct bef_ai_face_filter_policy_st { // 结果过滤条件
    bef_ai_face_filter_range_st yaw_range; // yaw角度范围
    bef_ai_face_filter_range_st roll_range; // roll角度范围
    bef_ai_face_filter_range_st pitch_range; // pitch角度范围
    float min_face_size; // 人脸最短边最小值
    float max_bounding_out_of_image_ratio; //人脸裁剪点超出原图范围相对于人脸大小的最大比例
    float max_bounding_out_of_image_count; //人脸裁剪点超出原图范围相对于人脸大小的最大比例
    
} bef_ai_face_filter_policy_st;



/**
 * @brief 创建人脸检测的句柄
 * @param [in] config Config of face detect algorithm, should be BEF_DETECT_LARGE_MODEL or BEF_DETECT_SMALL_MODEL
 *                    人脸检测算法的配置
 * @param [in] strModelPath 模型文件所在路径
 * @param [out] handle Created face detect handle
 *                     创建的人脸检测句柄
 * @return If succeed return BEF_RESULT_SUC, other value please see bef_effect_ai_public_define.h
 *         成功返回 BEF_RESULT_SUC, 失败返回相应错误码, 具体请参考 bef_effect_ai_public_define.h
 */
BEF_SDK_API bef_effect_result_t
bef_effect_ai_face_detect_create(
  unsigned long long config,
  const char * strModelPath,
  bef_effect_handle_t *handle
);

/**
 * @brief 人脸检测
 * @param [in] handle Created face detect handle
 *                    已创建的人脸检测句柄
 * @param [in] image Image base address
 *                   输入图片的数据指针
 * @param [in] pixel_format Pixel format of input image
 *                          输入图片的格式
 * @param [in] image_width  Image width
 *                          输入图像的宽度 (以像素为单位)
 * @param [in] image_height Image height
 *                          输入图像的高度 (以像素为单位)
 * @param [in] image_stride Image stride in each row
 *                          输入图像每一行的步长 (以像素为单位)
 * @param [in] orientation Image orientation
 *                         输入图像的转向，具体请参考 bef_effect_ai_public_define.h 中的 bef_rotate_type
 * @param [in] detect_config Config of face detect, for example, BEF_FACE_DETECT | BEF_DETECT_EYEBALL | BEF_BROW_JUMP
 *                           人脸检测相关的配置
 * @return If succeed return BEF_RESULT_SUC, other value please see bef_effect_ai_public_define.h
 *         成功返回 BEF_RESULT_SUC, 失败返回相应错误码, 具体请参考 bef_effect_ai_public_define.h
 */
BEF_SDK_API bef_effect_result_t
bef_effect_ai_face_detect(
  bef_effect_handle_t handle,
  const unsigned char *image,
  bef_ai_pixel_format pixel_format,
  int image_width,
  int image_height,
  int image_stride,
  bef_ai_rotate_type orientation,
  unsigned long long detect_config,
  bef_ai_face_info *p_face_info
);

typedef enum {
  // 设置tracker每多少帧进行一次detect(默认值有人脸时30, 无人脸时30/3=10), 值越大, cpu占用率越低, 但检测出新人脸的时间越长.
  BEF_FACE_PARAM_FACE_DETECT_INTERVAL = 1, // default 30
  // 设置检测到的最大人脸数目N (默认值10), 持续跟踪已检测到的N个人脸直到人脸数小于N再继续做检测. 值越大, 但相应耗时越长.
  BEF_FACE_PARAM_MAX_FACE_NUM = 2, // default 10
} bef_face_detect_type;


/**
 * @brief Set face detect parameter based on type 设置人脸检测的相关参数
 * @param [in] handle Created face detect handle
 *                    已创建的人脸检测句柄
 * @param [in] type Face detect type that needs to be set, check bef_face_detect_type for the detailed
 *                  需要设置的人体检测类型，可参考 bef_face_detect_type
 * @param [in] value Type value, check bef_face_detect_type for the detailed
 *                   参数值, 具体请参数 bef_face_detect_type 枚举中的说明
 * @return If succeed return BEF_RESULT_SUC, other value please refer bef_effect_ai_public_define.h
 *         成功返回 BEF_RESULT_SUC, 失败返回相应错误码, 具体请参考 bef_effect_ai_public_define.h
 */
BEF_SDK_API bef_effect_result_t
bef_effect_ai_face_detect_setparam(
  bef_effect_handle_t handle,
  bef_face_detect_type type,
  float value
);
/*
 *@brief 初始化handle
 *@param [in] config 指定240模型的模型参数，创建240或者280
 *Config-240，TT_MOBILE_FACE_240_DETECT
 *Config-280，TT_MOBILE_FACE_280_DETECT
 *Config-240 快速模式, TT_MOBILE_FACE_240_DETECT | TT_MOBILE_FACE_240_DETECT_FASTMODE
 *Config-280 快速模式, TT_MOBILE_FACE_280_DETECT | TT_MOBILE_FACE_240_DETECT_FASTMODE
 *@param [in] param_path 模型的文件路径
 */
BEF_SDK_API bef_effect_result_t
bef_effect_ai_face_detect_add_extra_model(
		bef_effect_handle_t handle,
		unsigned long long config, // 配置config，创建240或者280
		// Config-240，TT_MOBILE_FACE_240_DETECT
		// Config-280，TT_MOBILE_FACE_280_DETECT
		// Config-240 快速模式, TT_MOBILE_FACE_240_DETECT | TT_MOBILE_FACE_240_DETECT_FASTMODE
		// Config-280 快速模式, TT_MOBILE_FACE_280_DETECT | TT_MOBILE_FACE_240_DETECT_FASTMODE
		const char *param_path
		);

/**
 * @param [in] handle Destroy the created face detect handle
 *                    销毁创建的人脸检测句柄
 */
BEF_SDK_API void
bef_effect_ai_face_detect_destroy(
  bef_effect_handle_t handle
);

/**
 * @brief 人脸检测授权
 * @param [in] handle Created face detect handle
 *                    已创建的人脸检测句柄
 * @param [in] license 授权文件字符串
 * @param [in] length  授权文件字符串长度
 * @return If succeed return BEF_RESULT_SUC, other value please refer bef_effect_ai_public_define.h
 *         成功返回 BEF_RESULT_SUC, 授权码非法返回 BEF_RESULT_INVALID_LICENSE ，其它失败返回相应错误码, 具体请参考 bef_effect_ai_public_define.h
 */
#if defined(__ANDROID__) || defined(TARGET_OS_ANDROID)
BEF_SDK_API bef_effect_result_t bef_effect_ai_face_check_license(JNIEnv *env, jobject context,
                                                                 bef_effect_handle_t handle,
                                                                 const char *licensePath);
#else
#ifdef __APPLE__
BEF_SDK_API bef_effect_result_t bef_effect_ai_face_check_license(bef_effect_handle_t handle, const char *licensePath);
#endif
#endif


#endif // _BEF_EFFECT_FACE_DETECT_AI_H_
