#include "jni_ColorFormatConvert.h"
#include "color_format_convert.h"

#include <libyuv.h>
#include <log.h>
#include <include/android_nio_utils.h>

jint Java_com_zq_mediaengine_util_ColorFormatConvert_YUVAToI420
        (JNIEnv *env, jobject obj, jobject jsrc, jint rowStride,
         jint width, jint height, jobject jdest)
{
    AutoBufferPointer abpSrc(env, jsrc, JNI_FALSE);
    AutoBufferPointer abpDest(env, jdest, JNI_TRUE);
    uint8_t* src = (uint8_t*)abpSrc.pointer();
    uint8_t* dest = (uint8_t*)abpDest.pointer();

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

jint Java_com_zq_mediaengine_util_ColorFormatConvert_RGBAToI420
  (JNIEnv *env, jobject obj, jobject jsrc, jint rowStride, 
   jint width, jint height, jobject jdest)
{
    AutoBufferPointer abpSrc(env, jsrc, JNI_FALSE);
    AutoBufferPointer abpDest(env, jdest, JNI_TRUE);
    uint8_t* src = (uint8_t*)abpSrc.pointer();
    uint8_t* dest = (uint8_t*)abpDest.pointer();

    uint8_t* dst_y = dest;
    uint8_t* dst_u = dest + width * height;
    uint8_t* dst_v = dst_u + (width / 2) * (height / 2);
    int ret = libyuv::ABGRToI420(src, rowStride,
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

jint Java_com_zq_mediaengine_util_ColorFormatConvert_I420ToRGBA
 (JNIEnv *env, jobject obj, jobject jsrc, jint rowStride,
   jint width, jint height, jobject jdest)
{
    AutoBufferPointer abpSrc(env, jsrc, JNI_FALSE);
    AutoBufferPointer abpDest(env, jdest, JNI_TRUE);
    uint8_t* src = (uint8_t*)abpSrc.pointer();
    uint8_t* dest = (uint8_t*)abpDest.pointer();

    uint8_t* src_y = src;
    uint8_t* src_u = src + width * height;
    uint8_t* src_v = src_u + (width / 2) * (height / 2);
    int ret = libyuv::I420ToABGR(src_y, width,
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
