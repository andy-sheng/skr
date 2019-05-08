//
// Created by 昝晓飞 on 16/7/26.
//

#ifndef KSYSTREAMERANDROID_IMAGEBUFDEF_H
#define KSYSTREAMERANDROID_IMAGEBUFDEF_H
#include <stdint.h>
#include <stdbool.h>
#include <malloc.h>
#include <string.h>
#define FMT_OPAQUE  0x00
#define FMT_NV21  0x01
#define FMT_YV12  0x02
#define FMT_I420  0x03
#define FMT_ARGB  0x04
#define FMT_RGBA 0x05
#define FMT_BGR8 0x06
#define FMT_AVC  0x100
#define FMT_HEVC  0x101

class  ImageBufFrame
{
public:
    int width;
    int height;
    int* stride;
    int strideNum;
    int64_t pts;
    int64_t dts;
    int channels;
    int flags;
    int format;
    int orientation;
    int buf_size;
    uint8_t* buf;
    
    ImageBufFrame():
        width(0),
        height(0),
        stride(NULL),
        strideNum(0),
        pts(0),
        dts(0),
        channels(2),
        flags(0),
        format(1),
        orientation(0),
        buf_size(0),
        buf(NULL)
    {}
    ImageBufFrame(int width, int height, int format, int channels, int64_t pts, int64_t dts,
                  int orientation, int flags):
        width(0),
        height(0),
        stride(NULL),
        strideNum(0),
        pts(0),
        dts(0),
        channels(2),
        flags(0),
        format(0),
        orientation(0),
        buf_size(0),
        buf(NULL)
    {
        this->width = width;
        this->height = height;
        this->format = format;
        this->channels = channels;
        this->pts = pts;
        this->dts = dts;
        this->orientation = orientation;
        this->flags = flags;
    }

    void ReleaseImageBuf(void) {
        if (this->buf != NULL) {
            free(this->buf);
            this->buf = NULL;
        }

        if (this->stride != NULL) {
            free(this->stride);
            this->stride = NULL;
        }
    }

    //must be called after width and format has value
    void CreateStride() {
        int strideNum = 3;
        if(format == FMT_BGR8) {
            strideNum = 4;
        }
        int* strides = NULL;
        strides = (int*)malloc(sizeof(int)*strideNum);
        if(strides != NULL) {
            if (format != FMT_BGR8) {
                strides[0] = this->width;
                strides[1] = this->width / 2;
                strides[2] = this->width / 2;
            } else {
                strides[0] = this->width;
                strides[1] = 0;
                strides[2] = 0;
                strides[3] = 0;
            }
        }
        this->stride = strides;
        this->strideNum = strideNum;
    }

    void CreateStride(int* srcStride, int srcStrideNum) {
        if((srcStride == NULL || srcStrideNum <= 0) && (this->stride == NULL || this->strideNum <= 0)) {
            this->CreateStride();
            return;
        }

        if(srcStrideNum != this->strideNum && this->stride != NULL) {
            free(this->stride);
            this->stride = NULL;
        }

        if(this->stride == NULL) {
            stride = (int*)malloc(sizeof(int)*srcStrideNum);
        }
        memcpy(stride, srcStride, srcStrideNum);
    }
};
#endif //KSYSTREAMERANDROID_IMAGEBUFDEF_H
