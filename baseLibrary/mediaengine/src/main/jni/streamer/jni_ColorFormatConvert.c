#include "jni_ColorFormatConvert.h"
#include "color_format_convert.h"

#include <libyuv.h>
#include <log.h>

jint Java_com_zq_mediaengine_encoder_ColorFormatConvert_YUVAToI420
        (JNIEnv *env, jobject obj, jobject jsrc, jint rowStride,
         jint width, jint height, jobject jdest)
{
    uint8_t* src = (uint8_t*)(*env)->GetDirectBufferAddress(env, jsrc);
    uint8_t* dest = (uint8_t*)(*env)->GetDirectBufferAddress(env, jdest);

    uint8_t* dst_y = dest;
    uint8_t* dst_u = dest + width * height;
    uint8_t* dst_v = dst_u + (width / 2) * (height / 2);
    int ret = YUVAToI420(src, rowStride,
                         dst_y, width,
                         dst_u, width / 2,
                         dst_v, width / 2,
                         width, height);
    if (ret < 0) {
        LOGE("convert YUVA to I420 failed");
        return -1;
    }
    return 0;
}

jint Java_com_zq_mediaengine_encoder_ColorFormatConvert_RGBAToI420
  (JNIEnv *env, jobject obj, jobject jsrc, jint rowStride, 
   jint width, jint height, jobject jdest)
{
    uint8_t* src = (uint8_t*)(*env)->GetDirectBufferAddress(env, jsrc);
    uint8_t* dest = (uint8_t*)(*env)->GetDirectBufferAddress(env, jdest);

    uint8_t* dst_y = dest;
    uint8_t* dst_u = dest + width * height;
    uint8_t* dst_v = dst_u + (width / 2) * (height / 2);
    int ret = ABGRToI420(src, rowStride,
                         dst_y, width,
                         dst_u, width / 2,
                         dst_v, width / 2,
                         width, height);
    if (ret < 0) {
        LOGE("convert RGBA to I420 failed");
        return -1;
    }
    return 0;
}

jint Java_com_zq_mediaengine_encoder_ColorFormatConvert_I420ToRGBA
 (JNIEnv *env, jobject obj, jobject jsrc, jint rowStride,
   jint width, jint height, jobject jdest)
{
    uint8_t* src = (uint8_t*)(*env)->GetDirectBufferAddress(env, jsrc);
    uint8_t* dest = (uint8_t*)(*env)->GetDirectBufferAddress(env, jdest);

    uint8_t* src_y = src;
    uint8_t* src_u = src + width * height;
    uint8_t* src_v = src_u + (width / 2) * (height / 2);
    int ret = I420ToABGR(src_y, width,
                         src_u, width / 2,
                         src_v, width / 2,
                         dest, rowStride,
                         width, height);
    if (ret < 0) {
        LOGE("convert I420 to RGBA failed");
        return -1;
    }
    return 0;
}
