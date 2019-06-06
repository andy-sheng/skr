#ifndef KSYSTREAMERANDROIDSDK_COLOR_FORMAT_CONVERT_H
#define KSYSTREAMERANDROIDSDK_COLOR_FORMAT_CONVERT_H

#include <stdint.h>

int YUVAToI420(const uint8_t* src_yuva,
               int src_stride_yuva,
               uint8_t* dst_y,
               int dst_stride_y,
               uint8_t* dst_u,
               int dst_stride_u,
               uint8_t* dst_v,
               int dst_stride_v,
               int width,
               int height);

#endif //KSYSTREAMERANDROIDSDK_COLOR_FORMAT_CONVERT_H
