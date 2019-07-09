// Copyright (C) 2019 Beijing Bytedance Network Technology Co., Ltd.
#ifndef bef_effect_ai_api_h
#define bef_effect_ai_api_h

#include "bef_effect_ai_public_define.h"


#if defined(__ANDROID__) || defined(TARGET_OS_ANDROID)
#include <jni.h>
#endif


/**
 * @brief Create effect handle.
 * @param handle      Effect handle that will be created.
 * @return            If succeed return BEF_EFFECT_RESULT_SUC, other value please see bef_effect_ai_public_define.h.
 */
BEF_SDK_API bef_effect_result_t bef_effect_ai_create(bef_effect_handle_t *handle);

/**
 * @param handle      Effect handle that will  destroy
 */
BEF_SDK_API void bef_effect_ai_destroy(bef_effect_handle_t handle);



/**
 * @brief Initialize effect handle.
 * @param handle     Effect handle
 * @param width      Texture width
 * @param height     Texture height
 * @param strModeDir  Resource folder
 * @return           If succeed return BEF_EFFECT_RESULT_SUC, other value please see bef_effect_ai_public_define.h
 */
BEF_SDK_API bef_effect_result_t bef_effect_ai_init(bef_effect_handle_t handle, int width, int height, const char *strModeDir, const char* deviceName);

/**
 * @brief Set frame size.
 * @param handle     Effect handle
 * @param width      Texture width
 * @param height     Texture height
 * @return           If succeed return BEF_EFFECT_RESULT_SUC,  other value please see bef_effect_ai_public_define.h
 */
BEF_SDK_API bef_effect_result_t bef_effect_ai_set_width_height(bef_effect_handle_t handle, int width, int height);

/**
 * @brief Set device rotation, which is used to operate geometries.
 * @param handle      Effect handle that  initialized
 * @param quaternion  device quaternion
 * @return            if succeed return IES_RESULT_SUC,  other value please see bef_effect_base_define.h
 */
BEF_SDK_API bef_effect_result_t bef_effect_ai_set_device_rotation(bef_effect_handle_t handle, float *quaternion);

/**
 * @brief Set camera orientation, which is used for detection.
 * @param handle      Effect handle that  initialized
 * @param orientation  Camera clock wise
 * @return            if succeed return IES_RESULT_SUC,  other value please see bef_effect_base_define.h
 */
BEF_SDK_API bef_effect_result_t bef_effect_ai_set_orientation(bef_effect_handle_t handle, bef_ai_rotate_type orientation);

/**
 * @brief Setup beauty-face-filter with a specified string.
 * @param handle      Effect handle
 * @param strBeautyName The name of beauty will apply
 * @return            If succeed return BEF_EFFECT_RESULT_SUC,  other value please see bef_effect_ai_public_define.h
 */
BEF_SDK_API bef_effect_result_t bef_effect_ai_set_beauty(bef_effect_handle_t handle, const char *strBeautyName);

/**
 * @brief Setup beauty-face-filter with a specified string.
 * @param handle      Effect handle
 * @param strMakeupName The path of makeup resource will apply
 * @return            If succeed return BEF_EFFECT_RESULT_SUC,  other value please see bef_effect_define.h
 */
BEF_SDK_API bef_effect_result_t bef_effect_ai_set_buildin_makeup(bef_effect_handle_t handle, const char* strMakeupName);


/**
 * @brief Setup reshape-face-filter with a specified string.
 * @param handle          Effect handle
 * @param strPath    The absolute path of effect package.
 * @return                If succeed return BEF_EFFECT_RESULT_SUC, other value please see bef_effect_ai_public_define.h
 */
BEF_SDK_API bef_effect_result_t bef_effect_ai_set_reshape_face(bef_effect_handle_t handle, const char *strPath);

/**
 * @brief Update reshape-face-filter parameters
 * @param handle          Effect handle
 * @param fIntensity      Filter intensity, range in [0.0, 1.0]
 * @return                if succeed return BEF_EFFECT_RESULT_SUC, other value please see bef_effect_ai_public_define.h
 */
BEF_SDK_API bef_effect_result_t bef_effect_ai_update_reshape_face(bef_effect_handle_t handle, const float fIntensity);

/**
 * @brief Update reshape-face-filter parameters
 * @param handle          Effect handle
 * @param eyeIntensity    eye intensity, range in [0.0, 1.0]
  * @param cheekIntensity cheek intensity, range in [0.0, 1.0]
 * @return                if succeed return BEF_EFFECT_RESULT_SUC, other value please see bef_effect_ai_public_define.h
 */
BEF_SDK_API bef_effect_result_t bef_effect_ai_update_reshape_face_intensity(bef_effect_handle_t handle, const float eyeIntensity, const float cheekIntensity);


/**
 @param handle              effect handle
 @param leftFilterPath current filter path
 @param rightFilterPath    next filter path
 @parm  direction           the direction that the next filter will appear
 @param position            the borderline of left-filter and right-filter in x-axis.
 @return            if succeed return BEF_EFFECT_RESULT_SUC, other value please see bef_effect_ai_public_define.h
 */
BEF_SDK_API bef_effect_result_t bef_effect_ai_switch_color_filter_v2(bef_effect_handle_t handle, const char *leftFilterPath, const char *rightFilterPath, float position);
/**
 * Set color filter with a specified string.
 * @param handle    Effect handle
 * @param strPath   The absolute path of effect package.
 * @return          If succeed return BEF_EFFECT_RESULT_SUC, other value please see bef_effect_ai_public_define.h
 */
BEF_SDK_API bef_effect_result_t bef_effect_ai_set_color_filter_v2(bef_effect_handle_t handle, const char *strPath);

/**
 * @brief Set effect with a specified string.
 * @param handle    Effect handle
 * @param strPath   The absolute path of effect package.
 * @return          If succeed return BEF_EFFECT_RESULT_SUC, other value please see bef_effect_define.h
 */
BEF_SDK_API bef_effect_result_t bef_effect_ai_set_effect(bef_effect_handle_t handle, const char *strPath);

/**
 * @param handle          Effect handle
 * @param textureid_src   texture source
 * @return                if succeed return BEF_EFFECT_RESULT_SUC, other value please see bef_effect_ai_public_define.h
 */
BEF_SDK_API bef_effect_result_t bef_effect_ai_algorithm_texture(bef_effect_handle_t handle, unsigned int textureid_src, double timeStamp);

/**
 * @param [in] handle Created effect handle
 *                    初始化的特效句柄，需要再OpenGL环境中使用
 * @param [in] img_in 数据buffer
 * @param [in] fmt_in 数据buffer的格式，支持RGBA,BGRA(ios支持）,RGB,NV21,NV12,YUV420
 * @param [in] image_width 数据buffer图像的宽度
 * @param [in] image_height 数据buffer图像的长度
 * @param [in] image_stride 数据buffer的步长（一行的占用的字节数）
 * @param [in] timestamp     时间戳
 * @return If succeed return BEF_RESULT_SUC, other value please refer bef_effect_ai_public_define.h
 *         成功返回 BEF_RESULT_SUC, 失败返回相应错误码, 具体请参考 bef_effect_ai_public_define.h
 */
BEF_SDK_API bef_effect_result_t
byted_effect_algorithm_buffer(
                              bef_effect_handle_t handle,
                              const unsigned char *img_in,
                              bef_ai_pixel_format fmt_in,
                              int image_width,
                              int image_height,
                              int image_stride,
                              double timestamp
                              );

/**
 * @breif            Draw srcTexture with effects to dstTexture.
 * @param handle     Effect handle
 * @param srcTexture source texture
 * @param dstTexture distination texture
 * @return           if succeed return IES_RESULT_SUC,  other value please see bef_effect_base_define.h
 */
BEF_SDK_API bef_effect_result_t bef_effect_ai_process_texture(bef_effect_handle_t handle, unsigned int srcTexture, unsigned int dstTexture, double timeStamp);

/**
 * @breif Draw source buffer with effects to destination buffer.
 *        绘制特效
 * @param [in] handle Created effect handle
 *                    初始化的特效句柄
 * @param [in] img_in 输入图片buffer
 * @param [in] fmt_in 输入图片buffer的格式，支持RGBA,BGRA（ios支持）,RGB,NV12,NV21,YUV420
 * @param [in] image_width 输入图片buffer的宽度
 * @param [in] image_height 输入图片buffer的高度
 * @param [in] image_stride 输入图片buffer的步长（一行字节数）
 * @param [in] img_out 输出图片数据
 * @param [in] fmt_out 输出图片数据格式，支持RGBA,BGRA（ios支持）,RGB,NV12,NV21,YUV420
 * @param [in] timestamp   Current timestamp
 *                         时间戳
 * @return If succeed return BEF_RESULT_SUC, other value please see bef_effect_ai_public_define.h
 *         成功返回 BEF_RESULT_SUC, 失败返回相应错误码, 具体请参考 bef_effect_ai_public_define.h
 */
BEF_SDK_API bef_effect_result_t
byted_effect_ai_process_buffer(bef_effect_handle_t handle,
                            const unsigned char *img_in,
                            bef_ai_pixel_format fmt_in,
                            int image_width,
                            int image_height,
                            int image_stride,
                            unsigned char *img_out,
                            bef_ai_pixel_format fmt_out,
                            double timestamp
                            );

/**
 * @param handle      Effect handle that will be created
 * @param fIntensity  Filter smooth intensity, range in [0.0, 1.0]
 * if fIntensity is 0 , this filter would not work.
 * @return            if succeed return BEF_EFFECT_RESULT_SUC,  other value please see bef_effect_ai_public_define.h
 */
BEF_SDK_API bef_effect_result_t bef_effect_ai_set_intensity(bef_effect_handle_t handle, bef_intensity_type intensityType, float fIntensity);

/**
 * @brief SDK授权
 * @param [in] handle Created effect detect handle
 *                    已创建的句柄
 * @param [in] license 授权文件字符串
 * @param [in] length  授权文件字符串长度
 * @return If succeed return BEF_RESULT_SUC, other value please refer bef_effect_ai_public_define.h
 *         成功返回 BEF_RESULT_SUC, 授权码非法返回BEF_RESULT_INVALID_LICENSE，其它失败返回相应错误码, 具体请参考 bef_effect_ai_public_define.h
 */
#ifdef __ANDROID__
BEF_SDK_API bef_effect_result_t
bef_effect_ai_check_license(
                           JNIEnv* env,
                           jobject context,
                           bef_effect_handle_t handle,
                           const char *licensePath
                           );
#else
BEF_SDK_API bef_effect_result_t
bef_effect_ai_check_license(
                           bef_effect_handle_t handle,
                           const char *licensePath
                           );
#endif


#endif /* bef_effect_ai_h */
