//
// Created by 昝晓飞 on 16/7/26.
//

#ifndef KSYSTREAMERANDROID_IMG_PRE_PROCESS_H
#define KSYSTREAMERANDROID_IMG_PRE_PROCESS_H
#include <stdint.h>
#include "framework/ImageBufDef.h"
#include "framework/ImageMixerConfig.h"
#include "libyuv.h"

#define  BEAUTY_LEVEL_0  0
#define  BEAUTY_LEVEL_1  1

class ImgPreProcess {
public:
    ImgPreProcess( void );
    virtual ~ImgPreProcess( void ) {};

    void Release(void);

    ImageBufFrame* ProcessScale(const ImageBufFrame* srcImageBuf);
    ImageBufFrame* ProcessBeauty(const ImageBufFrame* srcImageBuf);
    ImageBufFrame* ProcessMixer(ImageBufFrame* *srcImageBufs, int srcNum, ImageMixerConfig* *mixerConfigs, int configNum);

    void SetTargetSize(int targetWidth, int targetHeight);
    void SetBeautyLevel(int beautyLevel);
    void SetIsFrontCameraMirror(bool isFrontCameraMirror);

    ImageBufFrame* ConvertI420ToRGBA(const ImageBufFrame* srcImageBuf);
    //for test
     ImageBufFrame* ConvertI420ToNV21(const ImageBufFrame* srcImageBuf);
     void DebugScale(bool isDebug) {this->mDebugScale = isDebug;}
     void DebugBeatuy(bool isDebug) {this->mDebugBeauty = isDebug;}
     void DebugMixer(bool isDebug) {this->mDebugMixer = isDebug;}
private:
    bool IsNeedScale(int imgOrientation, int previewWidth, int previewHeight);
    bool IsNeedRotate(int imgOrientation);
	uint32 ConvertFormat(int format);

    ImageBufFrame* mRotateCropImage;   //有内存 作为ProcessScale处理的中间数据存储，在多次scale过程中可以复用
    ImageBufFrame* mScaleImage;    //有内存 作为ProcessScale处理的中间数据存储，在多次scale过程中可以复用
    ImageBufFrame* mBeautyImage;   //有内存 作为ProcessBeauty处理的中间数据存储，在多次beatuy过程中可以复用
    ImageBufFrame* mMixerImage;      //有内存 作为ProcessMixer处理的中间数据存储，在多次mixer过程中可以复用
    ImageBufFrame* mTempImage;     //用于做测试使用


    uint8* mScaleBufferY;    //有内存
    uint8* mScaleBufferU;    //有内存
    uint8* mScaleBufferV;    //有内存

    int mTargetWidth;
    int mTargetHeight;
    int mBeautyLevel;
    bool mBeautySlient;
    bool mIsFrontCameraMirror;
    bool mIsInitFilter;

    //for test
    bool mDebugScale;
    bool mDebugBeauty;
    bool mDebugMixer;

};

#endif //KSYSTREAMERANDROID_IMG_PRE_PROCESS_H
