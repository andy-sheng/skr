//
// Created by 昝晓飞 on 16/7/26.
//

#include "img_pre_process.h"
#include "log.h"
#include "../../../denoise/Denoise_export.h"
#include "watermark.h"
extern "C"{
#include <libavutil/mem.h>
#include <libyuv.h>
}

ImgPreProcess::ImgPreProcess(void):
    mRotateCropImage(NULL),
    mScaleImage(NULL),
    mBeautyImage(NULL),
    mMixerImage(NULL),
    mTempImage(NULL),
    mScaleBufferY(NULL),
    mScaleBufferU(NULL),
    mScaleBufferV(NULL),
    mTargetWidth(1280),
    mTargetHeight(720),
    mBeautyLevel(BEAUTY_LEVEL_1),
    mBeautySlient(false),
    mIsFrontCameraMirror(false),
    mIsInitFilter(false),
    mDebugScale(false),
    mDebugBeauty(false),
    mDebugMixer(false)
{
}

void ImgPreProcess::Release(void) {
    if (mRotateCropImage != NULL) {
        mRotateCropImage->ReleaseImageBuf();
        mRotateCropImage = NULL;
    }

    if (mScaleImage != NULL) {
        mScaleImage->ReleaseImageBuf();
        delete mScaleImage;
        mScaleImage = NULL;
    }

    if (mBeautyImage != NULL) {
        mBeautyImage->ReleaseImageBuf();
        delete mBeautyImage;
        mBeautyImage = NULL;
    }

    if (mMixerImage != NULL) {
        mMixerImage->ReleaseImageBuf();
        delete mMixerImage;
        mMixerImage = NULL;
    }

    if(mIsInitFilter) {
        ReleaseFilter();
        mIsInitFilter = false;
    }
    
    if (mScaleBufferY != NULL) {
        free(mScaleBufferY);
        mScaleBufferY = NULL;
    }
    if (mScaleBufferU != NULL) {
        free(mScaleBufferU);
        mScaleBufferU = NULL;
    }
    if (mScaleBufferV != NULL) {
       free(mScaleBufferV);
       mScaleBufferV = NULL;
    }

    if(mTempImage != NULL) {
        mTempImage->ReleaseImageBuf();
        delete mTempImage;
        mTempImage = NULL;
    }
}
/*
*对输入的图片原数据进行crop&rotate&scale&mirror处理,mirror是用户可选项
*@params srcImageBuf 作为源数据输入,只对数据信息做读操作,不做写操作
*@return 返回经过crop&rotate&scale&mirror处理后的数据,mirror是用户可选项
*返回值可能为null,需要对返回值做异常处理
*/
ImageBufFrame* ImgPreProcess::ProcessScale(const ImageBufFrame* srcImageBuf)
{
    if(mDebugScale) {
        LOGD("[ImgPreProcess][ProcessScale] begin ");
    }
    if(srcImageBuf == NULL || srcImageBuf->buf == NULL){
        LOGE("the input buf is null");
        return NULL;
    }

    bool isRotate = IsNeedRotate(srcImageBuf->orientation);  //是否是竖屏
    bool bScale = IsNeedScale(srcImageBuf->orientation, srcImageBuf->width, srcImageBuf->height);
    
    if(mDebugScale) {
        LOGD("[ImgPreProcess][ProcessScale] bScale:%d ", bScale);
        LOGD("[ImgPreProcess][ProcessScale] isRotate:%d ", isRotate);
    }

    // calculate rotate/crop image size
    int rotateCropWidth = mTargetWidth;
    int rotateCropHeight = mTargetHeight;
    if(bScale) {
        //iw ih 代表分辨率宽高,即目标分辨率,竖屏时需要把分辨率宽高做交换
        //比如 分辨率是360p时,iw*ih:640*480;横屏时为640*480;竖屏时为480*640
        int iw = srcImageBuf->width;
        int ih = srcImageBuf->height;
        if(isRotate) {
            iw = srcImageBuf->height;
            ih = srcImageBuf->width;
        }

        //以目标的小值为基准,即iw/vw > ih/vh 以ih为基准
        if(iw * mTargetHeight > mTargetWidth * ih) {
            rotateCropWidth = ih * mTargetWidth/mTargetHeight;
            rotateCropHeight = ih;
        } else {
            rotateCropWidth = iw;
            rotateCropHeight = iw * mTargetHeight/mTargetWidth;
        }
        //4的倍数
        rotateCropWidth = (rotateCropWidth + 3) / 4 * 4;
        rotateCropHeight = (rotateCropHeight + 3) / 4 * 4;
        // avoid minus crop
        if (rotateCropWidth > iw) {
            rotateCropWidth = iw;
        }
        if (rotateCropHeight > ih) {
            rotateCropHeight = ih;
        }
    }

    if (mRotateCropImage != NULL && (mRotateCropImage->width != rotateCropWidth ||
            mRotateCropImage->height != rotateCropHeight)) {
        mRotateCropImage->ReleaseImageBuf();
        delete mRotateCropImage;
        mRotateCropImage = NULL;

        if (mScaleBufferY != NULL) {
            free(mScaleBufferY);
            mScaleBufferY = NULL;
        }
        if (mScaleBufferU != NULL) {
            free(mScaleBufferU);
            mScaleBufferU = NULL;
        }
        if (mScaleBufferV != NULL) {
            free(mScaleBufferV);
            mScaleBufferV = NULL;
        }
    }

    if(mRotateCropImage == NULL) {
        mRotateCropImage = new ImageBufFrame(rotateCropWidth, rotateCropHeight,
                                             FMT_I420, 3,
                                             srcImageBuf->pts, srcImageBuf->dts,
                                             srcImageBuf->orientation,
                                             srcImageBuf->flags);
        mRotateCropImage->CreateStride();
        mRotateCropImage->buf_size = mRotateCropImage->width * mRotateCropImage->height * 3 / 2;
        mRotateCropImage->buf = (unsigned char*)av_malloc(mRotateCropImage->buf_size);

        //内存分配失败
        if(mRotateCropImage->buf == NULL){
            mRotateCropImage->ReleaseImageBuf();
            delete mRotateCropImage;
            mRotateCropImage = NULL;
            return NULL;
        }
    }

     int YSize = mRotateCropImage->width * mRotateCropImage->height;
     uint8* pDstY = mRotateCropImage->buf;           //Y分量的起始地址
     uint8* pDstU = mRotateCropImage->buf + YSize;  //U分量的起始地址
     uint8* pDstV = mRotateCropImage->buf + YSize + (YSize/4); //V分量的起始地址

     libyuv::RotationMode mode;  //图片方向,竖屏270(前置)、90(后置) 横屏0
     uint32 format = ConvertFormat(srcImageBuf->format);
     if (srcImageBuf->orientation == 90) {
          mode = libyuv::kRotate90;
     } else if (srcImageBuf->orientation == 270){
          mode = libyuv::kRotate270;
     } else if(srcImageBuf->orientation == 180){
          mode = libyuv::kRotate180;
     } else {
          mode = libyuv::kRotate0;
     }

     int crop_x, crop_y;
     int cropWidth, croptHeight;
     if(bScale){
        cropWidth = mRotateCropImage->width;
        croptHeight = mRotateCropImage->height;

        //rotateImage附值的时候如果是竖屏把target的width和height进行交换后进行了计算
        //这里需要计算crop的x,y,因此需要交换回去,计算crop量值
        if(isRotate) {
            cropWidth = mRotateCropImage->height;
            croptHeight = mRotateCropImage->width;
        }
        //计算crop的坐标起始位置,/2四周都会crop,而不是在某一边进行crop
        crop_x = (srcImageBuf->width - cropWidth) / 2;
        crop_y = (srcImageBuf->height - croptHeight) / 2;
     } else {
        //竖屏
        if(isRotate) {
            crop_x = (srcImageBuf->width - mTargetHeight) / 2;
            crop_y = (srcImageBuf->height - mTargetWidth) / 2;
            cropWidth = mTargetHeight;
            croptHeight = mTargetWidth;

            if (crop_x < 0 || crop_y < 0) {
                crop_x = (srcImageBuf->width - mTargetWidth) / 2;
                crop_y = (srcImageBuf->height - mTargetHeight) / 2;
                cropWidth = mTargetWidth;
                croptHeight = mTargetHeight;
             }
        } else {
            crop_x = (srcImageBuf->width - mTargetWidth) / 2;
            crop_y = (srcImageBuf->height - mTargetHeight) / 2;
            cropWidth = mTargetWidth;
            croptHeight = mTargetHeight;
            if (crop_x < 0 || crop_y < 0) {
                crop_x = (srcImageBuf->width - mTargetHeight) / 2;
                crop_y = (srcImageBuf->height - mTargetWidth) / 2;
                cropWidth = mTargetHeight;
                croptHeight = mTargetWidth;
            }
        }
     }

     if(mDebugScale) {
         LOGD("[ImgPreProcess][ProcessScale] mRotateCropImage->stride[0] %d:", mRotateCropImage->width);
         LOGD("[ImgPreProcess][ProcessScale] mRotateCropImage->stride[1] %d:", mRotateCropImage->width/2);
         LOGD("[ImgPreProcess][ProcessScale] crop_x %d:", crop_x);
         LOGD("[ImgPreProcess][ProcessScale] crop_y %d:", crop_y);
         LOGD("[ImgPreProcess][ProcessScale] cropWidth %d:", cropWidth);
         LOGD("[ImgPreProcess][ProcessScale] croptHeight %d:", croptHeight);
         LOGD("[ImgPreProcess][ProcessScale] mTargetWidth:%d ", mTargetWidth);
         LOGD("[ImgPreProcess][ProcessScale] mTargetHeight:%d ", mTargetHeight);
         LOGD("[ImgPreProcess][ProcessScale] srcImageBuf->width %d:", srcImageBuf->width);
         LOGD("[ImgPreProcess][ProcessScale] srcImageBuf->height %d:", srcImageBuf->height);
         LOGD("[ImgPreProcess][ProcessScale] mode %d:", mode);
         LOGD("[ImgPreProcess][ProcessScale] format %d:", format);
     }

     libyuv::ConvertToI420(srcImageBuf->buf, srcImageBuf->buf_size,
                           pDstY, mRotateCropImage->width,
                           pDstU, mRotateCropImage->width/2,
                           pDstV, mRotateCropImage->width/2,
                           crop_x, crop_y,
                           srcImageBuf->width, srcImageBuf->height,
                           cropWidth, croptHeight,
                           mode,
                           format);
     if(mDebugScale) {
         LOGE("[ImgPreProcess][ProcessScale] ConvertToI420 finished");
     }
     //front mirror
      if(mIsFrontCameraMirror){
        if (mScaleBufferY == NULL) {
            mScaleBufferY = (uint8*) malloc(YSize);
        }
        if (mScaleBufferU == NULL) {
            mScaleBufferU = (uint8*) malloc(YSize/4);
        }
        if (mScaleBufferV == NULL) {
            mScaleBufferV = (uint8*) malloc(YSize/4);
        }
        //将covert处理后的数据copy到buffer中,作为mirror的源数据,mirror处理后数据存储在pDstY,pDstU,pDstV
        memcpy(mScaleBufferY, pDstY, YSize);
        memcpy(mScaleBufferU, pDstU, YSize/4);
        memcpy(mScaleBufferV, pDstV, YSize/4);

        libyuv::I420Mirror(mScaleBufferY, mRotateCropImage->width,
                            mScaleBufferU, (mRotateCropImage->width/2),
                            mScaleBufferV, (mRotateCropImage->width/2),
                            pDstY, mRotateCropImage->width,
                            pDstU, (mRotateCropImage->width/2),
                            pDstV, (mRotateCropImage->width/2),
                            mRotateCropImage->width, mRotateCropImage->height);
      }

      ImageBufFrame* img;
      if (!bScale) {
          mRotateCropImage->pts = srcImageBuf->pts;
          mRotateCropImage->dts = srcImageBuf->dts;
          mRotateCropImage->flags = srcImageBuf->flags;

          img = mRotateCropImage;
      } else {
          if (mScaleImage != NULL && (mScaleImage->width != mTargetWidth ||
                                     mScaleImage->height != mTargetHeight)) {
              mScaleImage->ReleaseImageBuf();
              delete mScaleImage;
              mScaleImage = NULL;
          }
          if (mScaleImage == NULL) {
              mScaleImage = new ImageBufFrame(mTargetWidth, mTargetHeight,
                                              FMT_I420, 3,
                                              srcImageBuf->pts, srcImageBuf->dts,
                                              srcImageBuf->orientation, srcImageBuf->flags);
              mScaleImage->CreateStride();
              //YUV420 在内存中的大小width*height*3/2 Y=width*hegiht U = Y/4 V = Y/4
              mScaleImage->buf_size = mScaleImage->width * mScaleImage->height * 3 / 2;
              mScaleImage->buf = (unsigned char*)av_malloc(mScaleImage->buf_size);
              //内存分配失败
              if(mScaleImage->buf == NULL){
                  mScaleImage->ReleaseImageBuf();
                  delete mScaleImage;
                  mScaleImage = NULL;
                  if(mRotateCropImage != NULL) {
                      mRotateCropImage->ReleaseImageBuf();
                      delete mRotateCropImage;
                      mRotateCropImage = NULL;
                  }
                  return NULL;
              }
          }

          libyuv::FilterMode filterMode = libyuv::kFilterBox;
          int scaleYSize = mScaleImage->width * mScaleImage->height;
          uint8* pscaleDstY = mScaleImage->buf;
          uint8* pscaleDstU = mScaleImage->buf + scaleYSize;
          uint8* pscaleDstV = mScaleImage->buf + scaleYSize + (scaleYSize/4);

          //将pDstY,pDstU,pDstV作为原数据,即crop,rotate后的的数据,scale到pscaleDstY,pscaleDstU,pscaleDstV
          libyuv::I420Scale(pDstY, mRotateCropImage->width,
                               pDstU, (mRotateCropImage->width/2),
                               pDstV, (mRotateCropImage->width/2),
                               mRotateCropImage->width, mRotateCropImage->height,
                               pscaleDstY, mScaleImage->width,
                               pscaleDstU , (mScaleImage->width/2),
                               pscaleDstV, (mScaleImage->width/2),
                               mScaleImage->width, mScaleImage->height,
                               filterMode);
          pDstY = pscaleDstY;
          pDstU = pscaleDstU;
          pDstV = pscaleDstV;

          mScaleImage->pts = srcImageBuf->pts;
          mScaleImage->dts = srcImageBuf->dts;
          mScaleImage->flags = srcImageBuf->flags;

          img = mScaleImage;
      }

      if(mDebugScale) {
          LOGD("[ImgPreProcess][ProcessScale]finished");
      }

      pDstY = NULL;
      pDstU = NULL;
      pDstV = NULL;
      return img;
}

ImageBufFrame* ImgPreProcess::ConvertI420ToRGBA(const ImageBufFrame* srcImageBuf)
{
    int YSize = srcImageBuf->width * srcImageBuf->height;
    uint8* pDstY = srcImageBuf->buf;           //Y分量的起始地址
    uint8* pDstU = srcImageBuf->buf + YSize;  //U分量的起始地址
    uint8* pDstV = srcImageBuf->buf + YSize + (YSize/4); //V分量的起始地址

    if (mTempImage != NULL && mTempImage->buf_size != srcImageBuf->buf_size) {
        mTempImage->ReleaseImageBuf();
        delete mTempImage;
        mTempImage = NULL;
    }

    //I420ToRGBA
    if(mTempImage == NULL) {
        mTempImage = new ImageBufFrame(srcImageBuf->width, srcImageBuf->height, FMT_RGBA, srcImageBuf->channels,
                                       srcImageBuf->pts, srcImageBuf->dts, srcImageBuf->orientation, srcImageBuf->flags);

        mTempImage->CreateStride(); // not used with RGBA

        mTempImage->buf_size = 4 * YSize;
        mTempImage->buf = (unsigned char*)av_malloc(mTempImage->buf_size);
    }

    int ret = libyuv::I420ToRGBA(pDstY, srcImageBuf->width,
            pDstU, srcImageBuf->width/ 2,
            pDstV, srcImageBuf->width/ 2,
            (uint8*)mTempImage->buf, srcImageBuf->width* 4,
            srcImageBuf->width, srcImageBuf->height);


    pDstY = NULL;
    pDstU = NULL;
    pDstV = NULL;
    return mTempImage;
}

ImageBufFrame* ImgPreProcess::ProcessBeauty(const ImageBufFrame* srcImageBuf)
{
    if(this->mDebugBeauty) {
        LOGD("[ImgPreProcess][ProcessBeauty]begin");
    }

    if(srcImageBuf == NULL || srcImageBuf->buf == NULL) {
        LOGE("[ImgPreProcess][ProcessBeauty] the srcImageBuf is null");
        return NULL;
    }

    if(this->mDebugBeauty && mBeautyImage != NULL ) {
         LOGD("[ImgPreProcess][ProcessBeauty]mIsInitFilter %d:" , mIsInitFilter);
         LOGD("[ImgPreProcess][ProcessBeauty]srcImageBufs width %d:" , srcImageBuf->width);
         LOGD("[ImgPreProcess][ProcessBeauty]srcImageBufs height %d:" , srcImageBuf->height);
         LOGD("[ImgPreProcess][ProcessBeauty]mBeautyImage width %d:" , mBeautyImage->width);
         LOGD("[ImgPreProcess][ProcessBeauty]mBeautyImage height %d:" , mBeautyImage->height);
    }

    if(mBeautyImage != NULL && mIsInitFilter &&
            (mBeautyImage->width != srcImageBuf->width || mBeautyImage->height != srcImageBuf->height
            || mBeautyImage->buf_size != srcImageBuf->buf_size)) {
         if(this->mDebugBeauty) {
            LOGD("[ImgPreProcessWrap][ProcessBeauty] ReleaseFilter");
         }

         ReleaseFilter();
         mIsInitFilter = false;
    }

    if(!mIsInitFilter) {
        InitFilter(srcImageBuf->width, srcImageBuf->height, mBeautySlient);
        mIsInitFilter = true;
    }

    if(mBeautyImage != NULL &&
            (mBeautyImage->width != srcImageBuf->width || mBeautyImage->height != srcImageBuf->height
            || mBeautyImage->buf_size != srcImageBuf->buf_size)) {
        mBeautyImage->ReleaseImageBuf();
        mBeautyImage = NULL;
    }

    if(mBeautyImage == NULL) {
        mBeautyImage = new ImageBufFrame(srcImageBuf->width, srcImageBuf->height, srcImageBuf->format,
                                         srcImageBuf->channels, srcImageBuf->pts, srcImageBuf->dts,
                                         srcImageBuf->orientation, srcImageBuf->flags);

        mBeautyImage->buf_size = srcImageBuf->buf_size;
        mBeautyImage->buf = (unsigned char*)av_malloc(mBeautyImage->buf_size);

        if(mBeautyImage->buf == NULL) {
            mBeautyImage->ReleaseImageBuf();
            mBeautyImage = NULL;
            return NULL;
        }
        mBeautyImage->CreateStride(srcImageBuf->stride, srcImageBuf->strideNum);
        memcpy(mBeautyImage->buf, srcImageBuf->buf, mBeautyImage->buf_size);
    } else {
        mBeautyImage->width = srcImageBuf->width;
        mBeautyImage->height = srcImageBuf->height;
        mBeautyImage->format = srcImageBuf->format;
        mBeautyImage->pts = srcImageBuf->pts;
        mBeautyImage->dts = srcImageBuf->dts;
        mBeautyImage->orientation = srcImageBuf->orientation;
        mBeautyImage->flags = srcImageBuf->flags;

        mBeautyImage->CreateStride(srcImageBuf->stride, srcImageBuf->strideNum);
        memcpy(mBeautyImage->buf, srcImageBuf->buf, mBeautyImage->buf_size);
    }

    int YSize = mBeautyImage->width * mBeautyImage->height;
    uint8* pDstY = mBeautyImage->buf;
    uint8* pDstU = mBeautyImage->buf + YSize;
    uint8* pDstV = mBeautyImage->buf + YSize + (YSize/4);
    //pDstY,pDstU,pDstV即是输入也是输出;srcImageBuf不能用于数据写
    Denoise_Processing_image(pDstY, mBeautyImage->width,
                        pDstU, (mBeautyImage->width/2),
                        pDstV, (mBeautyImage->width/2),
                        mBeautyImage->width, mBeautyImage->height,
                        0.035f, 3.35f);

    if(this->mDebugBeauty) {
       LOGD("[ImgPreProcess][ProcessBeauty]end");
    }

    ImageBufFrame* img = mBeautyImage;

    pDstY = NULL;
    pDstU = NULL;
    pDstV = NULL;

    return img;
}


ImageBufFrame* ImgPreProcess::ProcessMixer(ImageBufFrame* *srcImageBufs, int srcNum, ImageMixerConfig* *mixerConfigs, int configNum)
{
    if(this->mDebugMixer) {
        LOGD("[ImgPreProcess][ProcessMixer]begin");
    }
    if(srcImageBufs == NULL || mixerConfigs == NULL || srcImageBufs[0] == NULL) {
        LOGE("[ImgPreProcess][ProcessMixer] the srcIamgeBufs or mixerConfigs is NULL)");
        return NULL;
    }

    //原数据发生变更
    if(mMixerImage != NULL &&
            (mMixerImage->width != srcImageBufs[0]->width || mMixerImage->height != srcImageBufs[0]->height
             || mMixerImage->buf_size != srcImageBufs[0]->buf_size)) {
        mMixerImage->ReleaseImageBuf();
        mMixerImage = NULL;
    }

    if(mMixerImage == NULL) {
        mMixerImage = new ImageBufFrame(srcImageBufs[0]->width, srcImageBufs[0]->height, srcImageBufs[0]->format,
                                         srcImageBufs[0]->channels, srcImageBufs[0]->pts, srcImageBufs[0]->dts,
                                         srcImageBufs[0]->orientation, srcImageBufs[0]->flags);

        mMixerImage->buf_size = srcImageBufs[0]->buf_size;
        mMixerImage->buf = (unsigned char*)av_malloc(mMixerImage->buf_size);

        if(mMixerImage->buf == NULL) {
            mMixerImage->ReleaseImageBuf();
            mMixerImage = NULL;
            return NULL;
        }
        mMixerImage->CreateStride(srcImageBufs[0]->stride, srcImageBufs[0]->strideNum);
        memcpy(mMixerImage->buf, srcImageBufs[0]->buf, mMixerImage->buf_size);
    } else {
        mMixerImage->width = srcImageBufs[0]->width;
        mMixerImage->height = srcImageBufs[0]->height;
        mMixerImage->format = srcImageBufs[0]->format;
        mMixerImage->pts = srcImageBufs[0]->pts;
        mMixerImage->dts = srcImageBufs[0]->dts;
        mMixerImage->orientation = srcImageBufs[0]->orientation;
        mMixerImage->flags = srcImageBufs[0]->flags;

        mMixerImage->CreateStride(srcImageBufs[0]->stride, srcImageBufs[0]->strideNum);
        memcpy(mMixerImage->buf, srcImageBufs[0]->buf, mMixerImage->buf_size);
    }

    for(int i = 1 ; i < srcNum; i++) {
        if(srcImageBufs[i] != NULL && mixerConfigs[i] != NULL) {
            WaterMarkImage* waterMarkImage = wmi_initNew(srcImageBufs[i]->buf, mixerConfigs[i]->x, mixerConfigs[i]->y,
                                             mixerConfigs[i]->w, mixerConfigs[i]->h, mixerConfigs[i]->alpha);
            if(this->mDebugMixer) {
                LOGD("[ImgPreProcess][ProcessMixer]srcImageBufs[0] buf %p:" , mMixerImage->buf);
                LOGD("[ImgPreProcess][ProcessMixer]srcImageBufs[0] width %d:" , mMixerImage->width);
                LOGD("[ImgPreProcess][ProcessMixer]srcImageBufs[0] height %d:" , mMixerImage->height);
                LOGD("[ImgPreProcess][ProcessMixer]srcImageBufs[%d] buf %p:" , i,  srcImageBufs[i]->buf);
                LOGD("[ImgPreProcess][ProcessMixer]srcImageBufs[%d] x :%d" , i, mixerConfigs[i]->x);
                LOGD("[ImgPreProcess][ProcessMixer]srcImageBufs[%d] y :%d" , i,  mixerConfigs[i]->y);
                LOGD("[ImgPreProcess][ProcessMixer]srcImageBufs[%d] w :%d" , i, mixerConfigs[i]->w);
                LOGD("[ImgPreProcess][ProcessMixer]srcImageBufs[%d] H :%d" , i, mixerConfigs[i]->h);
            }

            if(waterMarkImage == NULL) {
                LOGE("[ImgPreProcess][ProcessMixer]waterMarkImage is null");
                return mMixerImage;
            }
            wmi_add_to_videoNew(mMixerImage->buf,mMixerImage->width, mMixerImage->height, waterMarkImage);
            wmi_destory(waterMarkImage);
        }
    }

    int YSize = mMixerImage->width * mMixerImage->height;
    uint8* pDstY = mMixerImage->buf;
    uint8* pDstU = mMixerImage->buf + YSize;
    uint8* pDstV = mMixerImage->buf + YSize + (YSize/4);

    ImageBufFrame* img = mMixerImage;

    if(this->mDebugMixer) {
        LOGD("[ImgPreProcess][ProcessMixer]end");
    }
    return img;
}

ImageBufFrame* ImgPreProcess::ConvertI420ToNV21(const ImageBufFrame* srcImageBuf)
{
    int YSize = srcImageBuf->width * srcImageBuf->height;
    uint8* pDstY = srcImageBuf->buf;           //Y分量的起始地址
    uint8* pDstU = srcImageBuf->buf + YSize;  //U分量的起始地址
    uint8* pDstV = srcImageBuf->buf + YSize + (YSize/4); //V分量的起始地址
    //I420ToNV21
    if(mTempImage == NULL) {
        mTempImage = new ImageBufFrame(srcImageBuf->width, srcImageBuf->height, srcImageBuf->format, srcImageBuf->channels,
                                       srcImageBuf->pts, srcImageBuf->dts, srcImageBuf->orientation, srcImageBuf->flags);

        mTempImage->CreateStride();

        mTempImage->buf_size = srcImageBuf->buf_size;
        mTempImage->buf = (unsigned char*)av_malloc(srcImageBuf->buf_size);
    }
    int YNV21Size = mTempImage->width * mTempImage->height;
    uint8* pNV21DstY = mTempImage->buf;           //Y分量的起始地址
    uint8* pNV21DstYU = mTempImage->buf + YNV21Size;  //UV分量的起始地址

    libyuv::I420ToNV21(pDstY, mTempImage->width,
                       pDstU, mTempImage->width/2,
                       pDstV, mTempImage->width/2,
                       pNV21DstY, mTempImage->width,
                       pNV21DstYU, mTempImage->width,
                       mTempImage->width, mTempImage->height);
    pDstY = NULL;
    pDstU = NULL;
    pDstV = NULL;
    return mTempImage;
}
/**
* 用户设定的目标size
*/
void ImgPreProcess::SetTargetSize(int targetWidth, int targetHeight)
{
    mTargetWidth = targetWidth;
    mTargetHeight = targetHeight;

    return;
}

void ImgPreProcess::SetBeautyLevel(int beautyLevel)
{
    mBeautyLevel = beautyLevel;
    if(mBeautyLevel == BEAUTY_LEVEL_0) {
        mBeautySlient = true;
    }
    return;
}

void ImgPreProcess::SetIsFrontCameraMirror(bool isFrontCameraMirror)
{
    mIsFrontCameraMirror = isFrontCameraMirror;
    return;
}

/**
* 目标分辨率比视频小时 需要scale,比视频大时也需要scale
*TODO:目标width和height可以考虑优化
* @return true 代表需要scale,false代表不需要scale
*/
bool ImgPreProcess::IsNeedScale(int imgOrientation, int previewWidth, int previewHeight)
{
    //竖屏
    if(IsNeedRotate(imgOrientation)){
        if((mTargetWidth < previewHeight && mTargetHeight < previewWidth) ||
                (mTargetWidth > previewHeight || mTargetHeight > previewWidth)) {
            return true;
        }
    } else {
    //横屏
        if((mTargetWidth < previewWidth && mTargetHeight < previewHeight) ||
                (mTargetWidth > previewWidth || mTargetHeight > previewHeight)) {
            return true;
        }
    }

    return false;
}

/**
* imgOrientation 竖屏90，270 横屏0
* @return true 代表竖屏,false代表横屏
*/
bool ImgPreProcess::IsNeedRotate(int imgOrientation)
{
    return (imgOrientation%180);
}

uint32 ImgPreProcess::ConvertFormat(int format)
{
//#define FMT_OPAQUE  0x00
//#define FMT_NV21  0x01
//#define FMT_YV12  0x02
//#define FMT_I420  0x03
//#define FMT_AVC  0x100
//#define FMT_HEVC  0x101
    switch(format) {
        case FMT_NV21:
            return libyuv::FOURCC_NV21;
        case FMT_YV12:
            return libyuv::FOURCC_YV12;
        case FMT_I420:
            return libyuv::FOURCC_I420;
        case FMT_ARGB:
            return libyuv::FOURCC_ARGB;
        case FMT_RGBA:
            return libyuv::FOURCC_RGBA;
        default:
            return libyuv::FOURCC_NV21;
    }

}

/*
ImgPreProcess *GetImgPreProcessInstance()
{
   return ImgPreProcess::GetInstance();
}
*/
