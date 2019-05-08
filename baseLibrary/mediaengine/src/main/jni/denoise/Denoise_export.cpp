#include <stdio.h>
#include <sys/time.h>
#include <time.h> 
#include <stdlib.h>
#include <math.h>
#ifdef ENABLE_NEON
#include <arm_neon.h>
#endif
#include "Denoise_export.h"
#include <ctime>
#include <string.h>
#ifdef ANDROID
#include <android/log.h>
#endif

#ifdef __cplusplus
#define cast_uint32_t static_cast<uint32_t>
#else
#define cast_uint32_t (uint32_t)
#endif

#ifdef ANDROID
#define LOGV(...) //__android_log_print(ANDROID_LOG_VERBOSE, "White", __VA_ARGS__)
#define LOGD(...) //__android_log_print(ANDROID_LOG_DEBUG , "White", __VA_ARGS__)
#define LOGI(...) //__android_log_print(ANDROID_LOG_INFO  , "White", __VA_ARGS__)
#define LOGW(...) //__android_log_print(ANDROID_LOG_WARN  , "White", __VA_ARGS__)
#define LOGE(...) //__android_log_print(ANDROID_LOG_ERROR  , "White", __VA_ARGS__)
#endif

// global variary
unsigned char* xgDiff;
unsigned char* ygDiff;
unsigned char* TempImageDataH ;
unsigned char* TempImageDataV ;
unsigned char* xgDiff1;
unsigned char* ImageDataY ;
short* fwTable[2]  ;
   
float  gsigma_s = 50.0 ;
float  gsigma_r = 0.20 ;
int    gmaxiter =  2  ;
bool   gslight = false ;

#define maxvalue 16384 // 2^14 = 16384

static inline float fasterpow2 (float p)
{
  float clipp = (p < -126) ? -126.0f : p;
  union { uint32_t i; float f; } v = { cast_uint32_t ( (1 << 23) * (clipp + 126.94269504f) ) };
  return v.f;
}

static inline float fasterexp (float p)
{
  return fasterpow2 (1.442695040f * p);
}

void set_sigma_r(float sigma_r)
{
	if(sigma_r == 0.0f)
	{
		sigma_r = 0.000001 ;
	}
	float ratio = gsigma_s / sigma_r;
	
	for(int i=0; i<gmaxiter; i++)
	{
		float sigma_H = gsigma_s * sqrt(3.0) * pow(2.0, gmaxiter - i - 1) / sqrt(pow(4.0, gmaxiter) - 1.0);
		//float a = exp(-sqrt(2.0) / sigma_H); 
		//float Alpha = log(a);
		float Alpha = -sqrt(2.0) / sigma_H;
		for(int j=0; j<256; j++)
		{
			float distance = 1.0 + ratio * float(j)/256.0f;
			fwTable[i][j] = (short)((fasterexp(distance*Alpha))*maxvalue);
		}
	}
}


static inline float fastpow2 (float p) 
{
  float offset = (p < 0) ? 1.0f : 0.0f;
  float clipp = (p < -126) ? -126.0f : p;
  int w = clipp;
  float z = clipp - w + offset;
  union { uint32_t i; float f; } v = { cast_uint32_t ( (1 << 23) * (clipp + 121.2740575f + 27.7280233f / (4.84252568f - z) - 1.49012907f * z) ) };

  return v.f;
}

static inline float fastexp (float p)
{
  return fastpow2 (1.442695040f * p);
}

void InitFilter(int nW, int nH, bool slight_beauty)
{
    int nWidth  = ((nW+15)>>4)<<4;
    int nHeight = (((nH+15)>>4)<<4) + 16;

	LOGD("white: nWidth:%d, nHeight:%d", nWidth, nHeight);

	xgDiff     = (unsigned char*)malloc(nWidth*nHeight*sizeof(unsigned char));
	xgDiff1    = (unsigned char*)malloc(nWidth*nHeight*sizeof(unsigned char));
	ygDiff     = (unsigned char*)malloc(nWidth*nHeight*sizeof(unsigned char));
	ImageDataY = (unsigned char*)malloc(nWidth*nHeight*sizeof(unsigned char));
	
	TempImageDataH = (unsigned char*)malloc(nWidth*nHeight*sizeof(unsigned char)*3/2);
	TempImageDataV = (unsigned char*)malloc(nWidth*nHeight*sizeof(unsigned char)*3/2);

	for(int i = 0; i < gmaxiter; i++){
		fwTable[i] = (short*)malloc(256*sizeof(short));
	}

	memset(xgDiff     ,   0x00, nWidth*nHeight*sizeof(char));
	memset(xgDiff1    ,   0x00, nWidth*nHeight*sizeof(char));
	memset(ygDiff     ,   0x00, nWidth*nHeight*sizeof(char));
	memset(ImageDataY ,   0x00, nWidth*nHeight*sizeof(char));

	memset(TempImageDataH,0x00,nWidth*nHeight*sizeof(unsigned char)*3/2);
	memset(TempImageDataV,0x00,nWidth*nHeight*sizeof(unsigned char)*3/2);
    
    if(slight_beauty) {
        gmaxiter = 1;
        gslight = slight_beauty;
    }
	
	for(int i = 0; i < gmaxiter; i++){
		memset(fwTable[i],0x00,256*sizeof(short));
	}	
}

void ReleaseFilter()
{
	if(xgDiff!= NULL)
	{
		free(xgDiff) ;
        xgDiff = NULL;
	}		

	if(xgDiff1!= NULL)
	{
		free(xgDiff1) ;
        xgDiff1 = NULL;
	}
	
	if(TempImageDataV!= NULL)
	{
		free(TempImageDataV) ;
        TempImageDataV = NULL;
	}		
	
	if(TempImageDataH!= NULL)
	{
		free(TempImageDataH) ;
        TempImageDataH = NULL;
	}		
	
	if(ygDiff!= NULL)
	{
		free(ygDiff) ;
        ygDiff = NULL;
	}	

	if(ImageDataY!= NULL)
	{
		free(ImageDataY) ;
        ImageDataY = NULL;
	}	

	for(int i = 0; i < gmaxiter; i++)
	{
		if(fwTable[i]!=NULL)
		{
			free(fwTable[i]);
			fwTable[i] = NULL;
		}
	}	
}

void GetDiffImgY(unsigned char* pImgData, unsigned char* xDiff, unsigned char* yDiff, int nWidth, int nHeight, float ratio)
{
	int   dim = 3 ;
	int   accum ;
    float faccm ;

	for(int y=0; y<nHeight; y++) 
	{
		for(int x=0; x<nWidth-1; x++) 
		{
			accum   = abs(pImgData[(y*nWidth+x)+1] - pImgData[(y*nWidth+x)]); //R
			xDiff[y*nWidth+x] = accum;
		}
	}

	for(int x=0; x<nWidth; x++) {
		for(int y=0; y<nHeight-1; y++) {
			accum   = abs(pImgData[(y*nWidth+x)] - pImgData[((y+1)*nWidth+x)]); //R
			yDiff[y*nWidth+x] = accum;
		}
	}

    return ;
}


void GetDiffImgY_NEON(unsigned char* pImgData, unsigned char* xDiff, unsigned char* yDiff, int nWidth, int nHeight, float ratio)
{
#ifdef ENABLE_NEON
	uint8x8_t HorPixel;
	uint8x8_t curPixel;
	uint8x8_t VerPixel;
	
	int x, y, pCurPixelIndex;
	uint8x8_t HtempRSub ;
	uint8x8_t VtempRSub ;
	
	/*for(int i = 0; i < nWidth*nHeight; i += 16)
	{
		curPixel = vld1_u8(pImgData + i         );
		HorPixel = vld1_u8(pImgData + i +      1);
		VerPixel = vld1_u8(pImgData + i + nWidth);
		
		HtempRSub = vabd_u8(curPixel,HorPixel);
		VtempRSub = vabd_u8(curPixel,VerPixel);
	
		vst1_u8(&xDiff[i],HtempRSub);
		vst1_u8(&yDiff[i],VtempRSub);

		curPixel = vld1_u8(pImgData + i + 8          );
		HorPixel = vld1_u8(pImgData + i + 8  +      1);
		VerPixel = vld1_u8(pImgData + i + 8  + nWidth);
		
		HtempRSub = vabd_u8(curPixel,HorPixel);
		VtempRSub = vabd_u8(curPixel,VerPixel);
	
		vst1_u8(&xDiff[i + 8],HtempRSub);
		vst1_u8(&yDiff[i + 8],VtempRSub);
	}*/

	for(int h = 0; h < nHeight; h++)
	{
		for(int w = 0; w < nWidth; w += 8)
		{
			curPixel = vld1_u8(pImgData + h * nWidth + w);
			if(w < nWidth - 8)
				HorPixel = vld1_u8(pImgData + h * nWidth + w + 1);
			else
				HorPixel = vld1_u8(pImgData + h * nWidth + w - 1);
			if(h < nHeight - 1)
				VerPixel = vld1_u8(pImgData + (h + 1) * nWidth + w);
			else
				VerPixel = vld1_u8(pImgData + (h - 1) * nWidth + w);

			HtempRSub = vabd_u8(curPixel, HorPixel);
			VtempRSub = vabd_u8(curPixel, VerPixel);

			vst1_u8(&xDiff[h * nWidth + w], HtempRSub);
			vst1_u8(&yDiff[h * nWidth + w], VtempRSub);
		}
	}

    return ;
#endif
}


void HorizontalFilterY(unsigned char* ImgData, short* pTable,int nWidth, int nHeight, unsigned char* xgDiff)
{
	short coeff ;
	int nPixelIndex ;

	for(int y = 0 ; y < nHeight; y++)
	{
		for(int x = 1 ; x < nWidth; x++)
		{
			nPixelIndex = y*nWidth + x; 
			coeff          = pTable[xgDiff[y*nWidth + x - 1]];
			ImgData[nPixelIndex] = ((ImgData[nPixelIndex]<<14) + coeff*(ImgData[nPixelIndex-1] - ImgData[nPixelIndex]))>>14;
		}
		
		for(int x = nWidth-2; x >= 0 ; x--)
		{
			nPixelIndex = y*nWidth + x; 
			coeff       = pTable[xgDiff[y*nWidth + x]];
			ImgData[nPixelIndex] = ((ImgData[nPixelIndex]<<14) + coeff*(ImgData[nPixelIndex+1] - ImgData[nPixelIndex]))>>14; // B
		}
	}

	return ;
}


void HorizontalFilterY_NEON(unsigned char* ImgData, short* pTable,int nWidth, int nHeight, unsigned char* xgDiff)
{
	short coeff ;
	int nPixelIndex ;
	
	for(int y = 0 ; y < nHeight; y++)
	{
		for(int x = 1 ; x < nWidth; x++)
		{
			nPixelIndex = y*nWidth + x;
			coeff          = pTable[xgDiff[y*nWidth + x - 1]];
			ImgData[nPixelIndex] = ((ImgData[nPixelIndex]<<14) + coeff*(ImgData[nPixelIndex-1] - ImgData[nPixelIndex]))>>14;
		}
		
		for(int x = nWidth-2; x >= 0 ; x--)
		{
			nPixelIndex = y*nWidth + x;
			coeff       = pTable[xgDiff[y*nWidth + x]];
			ImgData[nPixelIndex] = ((ImgData[nPixelIndex]<<14) + coeff*(ImgData[nPixelIndex+1] - ImgData[nPixelIndex]))>>14; // B
		}
	}

	return ;
}

void VerticalFilterY(unsigned char* ImgData, short* pTable,int nWidth, int nHeight,unsigned char* ygDiff)
{
	short coeff ;
	int nPixelIndex ;

	for(int x = 0 ; x < nWidth; x++)
	{
		for(int y = 1 ; y < nHeight; y++)
		{
			nPixelIndex = y*nWidth + x; 
			coeff       = pTable[ygDiff[(y-1)*nWidth + x]];
			ImgData[nPixelIndex] = ((ImgData[nPixelIndex]<<14) + coeff*(ImgData[(nPixelIndex-nWidth)] - ImgData[nPixelIndex]))>>14; // B
		}

		
		for(int y = nHeight -2 ; y >= 0 ; y--)
		{
			nPixelIndex = y*nWidth + x; 
			coeff       = pTable[ygDiff[y*nWidth + x]];
			ImgData[nPixelIndex] = ((ImgData[nPixelIndex]<<14) + coeff*(ImgData[(nPixelIndex+nWidth)] - ImgData[nPixelIndex]))>>14; // B
		}
	}

	return ;
}

void FilterY_NEON(unsigned char* ImgData, short* pTable,int nWidth, int nHeight,unsigned char* xygDiff)
{
#ifdef ENABLE_NEON

	int nPixelIndex ;
	short coeff ;

	int16x4_t coeffL;
	int16x4_t coeffH;
	
	/*for(int y = 0 ; y < nHeight - 1; y++)
	{

		for(int x = 0 ; x < nWidth; x +=8)
		{
			nPixelIndex = y*nWidth + x; 
			
			coeffL = vset_lane_s16(pTable[xygDiff[nPixelIndex+0]],coeffL,0);
			coeffL = vset_lane_s16(pTable[xygDiff[nPixelIndex+1]],coeffL,1);
			coeffL = vset_lane_s16(pTable[xygDiff[nPixelIndex+2]],coeffL,2);
			coeffL = vset_lane_s16(pTable[xygDiff[nPixelIndex+3]],coeffL,3);
			
			coeffH = vset_lane_s16(pTable[xygDiff[nPixelIndex+4]],coeffH,0);
			coeffH = vset_lane_s16(pTable[xygDiff[nPixelIndex+5]],coeffH,1);
			coeffH = vset_lane_s16(pTable[xygDiff[nPixelIndex+6]],coeffH,2);
			coeffH = vset_lane_s16(pTable[xygDiff[nPixelIndex+7]],coeffH,3);
			
			int16x8_t curPixel = vmovl_u8(vld1_u8(&ImgData[nPixelIndex + nWidth]));
			int16x8_t VerPixel = vmovl_u8(vld1_u8(&ImgData[nPixelIndex]));
			
			uint16x4_t CurPixelH = vget_high_s16(curPixel);
			uint16x4_t VerPixelH = vget_high_s16(VerPixel);
			
			uint16x4_t CurPixelL = vget_low_s16(curPixel);
			uint16x4_t VerPixelL = vget_low_s16(VerPixel);	

			uint16x4_t LP = vadd_s16(CurPixelL, vshrn_n_s32(vmull_s16(vsub_s16(VerPixelL, CurPixelL),coeffL),14));
			uint16x4_t HP = vadd_s16(CurPixelH, vshrn_n_s32(vmull_s16(vsub_s16(VerPixelH, CurPixelH),coeffH),14));

			uint8x8_t FP = vqmovn_u16(vcombine_s16(LP,HP));
			vst1_u8(&ImgData[nPixelIndex + nWidth], FP);
		}
	}	*/
	
	for(int y = nHeight -2 ; y >= 0 ; y--)
	{
		for(int x = 0 ; x < nWidth; x +=8)
		{
			nPixelIndex = y*nWidth + x; 
			
			coeffL = vset_lane_s16(pTable[xygDiff[nPixelIndex]],coeffL,0);
			coeffL = vset_lane_s16(pTable[xygDiff[nPixelIndex+1]],coeffL,1);
			coeffL = vset_lane_s16(pTable[xygDiff[nPixelIndex+2]],coeffL,2);
			coeffL = vset_lane_s16(pTable[xygDiff[nPixelIndex+3]],coeffL,3);
			
			coeffH = vset_lane_s16(pTable[xygDiff[nPixelIndex+4]],coeffH,0);
			coeffH = vset_lane_s16(pTable[xygDiff[nPixelIndex+5]],coeffH,1);
			coeffH = vset_lane_s16(pTable[xygDiff[nPixelIndex+6]],coeffH,2);
			coeffH = vset_lane_s16(pTable[xygDiff[nPixelIndex+7]],coeffH,3);
			
			int16x8_t curPixel = vmovl_u8(vld1_u8(&ImgData[nPixelIndex         ]));
			int16x8_t VerPixel = vmovl_u8(vld1_u8(&ImgData[nPixelIndex + nWidth]));
			
			uint16x4_t CurPixelH = vget_high_s16(curPixel);
			uint16x4_t VerPixelH = vget_high_s16(VerPixel);
			
			uint16x4_t CurPixelL = vget_low_s16(curPixel);
			uint16x4_t VerPixelL = vget_low_s16(VerPixel);	

			uint16x4_t LP = vadd_s16(CurPixelL, vshrn_n_s32(vmull_s16(vsub_s16(VerPixelL, CurPixelL),coeffL),14));
			uint16x4_t HP = vadd_s16(CurPixelH, vshrn_n_s32(vmull_s16(vsub_s16(VerPixelH, CurPixelH),coeffH),14));

			uint8x8_t FP = vqmovn_u16(vcombine_s16(LP,HP));
			vst1_u8(&ImgData[nPixelIndex], FP);
		}
	}
	for(int i = 0; i < nWidth; i += 8)
	{
	    vst1_u8(&ImgData[(nHeight - 1) * nWidth + i], vld1_u8(&ImgData[(nHeight - 2) * nWidth + i]));
	}

	return ;
#endif
}

void MatrixtransposeHV(unsigned char *Src, unsigned char* Dst, int nWidth, int nHeight)
{
	for(int j = 0 ; j < nHeight; j++)
	{
		for(int i = 0 ; i < nWidth; i++)
		{
			Dst[i*nHeight+nHeight-1-j] = Src[j*nWidth + i];
		}
	}
}

void MatrixtransposeVH(unsigned char *Src, unsigned char* Dst, int nWidth, int nHeight)
{
	for(int j = 0 ; j < nHeight; j++)
	{
		for(int i = 0 ; i < nWidth; i++)
		{
			Dst[j*nWidth + i] = Src[i*nHeight+nHeight-1-j];
		}
	}
}

void MatrixT(unsigned char* dstImg,unsigned char* srcImg,int width,int height)
{
#ifdef ENABLE_NEON
    uint8x8x4_t mat1;  
    uint8x8x4_t mat2;

    uint8x8x2_t temp1;
    uint8x8x2_t temp2;
    uint8x8x2_t temp3;
    uint8x8x2_t temp4;

    uint16x4x2_t temp5;
    uint16x4x2_t temp6;
    uint16x4x2_t temp7;
    uint16x4x2_t temp8;
    uint16x4x2_t temp9;
    uint16x4x2_t temp10;
    uint16x4x2_t temp11;
    uint16x4x2_t temp12;

    uint32x2x2_t temp13;
    uint32x2x2_t temp14;
    uint32x2x2_t temp15;
    uint32x2x2_t temp16;
    uint32x2x2_t temp17;
    uint32x2x2_t temp18;
    uint32x2x2_t temp19;
    uint32x2x2_t temp20;
	
	
    for(int i=0; i<height; i+=8)
    {
        for(int j=0; j<width; j+=8)
        {
            mat1.val[0]=vld1_u8(srcImg+i*width+j); 
            mat1.val[1]=vld1_u8(srcImg+(i+1)*width+j); 
            mat1.val[2]=vld1_u8(srcImg+(i+2)*width+j); 
            mat1.val[3]=vld1_u8(srcImg+(i+3)*width+j); 
            mat2.val[0]=vld1_u8(srcImg+(i+4)*width+j); 
            mat2.val[1]=vld1_u8(srcImg+(i+5)*width+j); 
            mat2.val[2]=vld1_u8(srcImg+(i+6)*width+j); 
            mat2.val[3]=vld1_u8(srcImg+(i+7)*width+j); 

			temp1=vtrn_u8(mat1.val[1],mat1.val[0]);
            temp2=vtrn_u8(mat1.val[3],mat1.val[2]);
            temp3=vtrn_u8(mat2.val[1],mat2.val[0]);
            temp4=vtrn_u8(mat2.val[3],mat2.val[2]);

			temp5.val[0]= vreinterpret_u16_u8(temp1.val[0]);
            temp5.val[1]= vreinterpret_u16_u8(temp1.val[1]);
            temp6.val[0]= vreinterpret_u16_u8(temp2.val[0]);
            temp6.val[1]= vreinterpret_u16_u8(temp2.val[1]);
            
			temp7.val[0]= vreinterpret_u16_u8(temp3.val[0]);
            temp7.val[1]= vreinterpret_u16_u8(temp3.val[1]);
            temp8.val[0]= vreinterpret_u16_u8(temp4.val[0]);
            temp8.val[1]= vreinterpret_u16_u8(temp4.val[1]);

            temp9 =vtrn_u16(temp6.val[0],temp5.val[0]);
            temp10=vtrn_u16(temp6.val[1],temp5.val[1]);
            temp11=vtrn_u16(temp8.val[0],temp7.val[0]);
            temp12=vtrn_u16(temp8.val[1],temp7.val[1]);

            temp13.val[0]= vreinterpret_u32_u16(temp9.val[0]);
            temp13.val[1]= vreinterpret_u32_u16(temp9.val[1]);
            temp14.val[0]= vreinterpret_u32_u16(temp10.val[0]);
            temp14.val[1]= vreinterpret_u32_u16(temp10.val[1]);
            temp15.val[0]= vreinterpret_u32_u16(temp11.val[0]);
            temp15.val[1]= vreinterpret_u32_u16(temp11.val[1]);
            temp16.val[0]= vreinterpret_u32_u16(temp12.val[0]);
            temp16.val[1]= vreinterpret_u32_u16(temp12.val[1]);			
			
			temp17=vtrn_u32(temp15.val[0],temp13.val[0]);
            temp18=vtrn_u32(temp15.val[1],temp13.val[1]);
            temp19=vtrn_u32(temp16.val[0],temp14.val[0]);
            temp20=vtrn_u32(temp16.val[1],temp14.val[1]);			
		
            temp1.val[0]= vreinterpret_u8_u32(temp17.val[0]);
            temp1.val[1]= vreinterpret_u8_u32(temp19.val[0]);
            temp2.val[0]= vreinterpret_u8_u32(temp18.val[0]);
            temp2.val[1]= vreinterpret_u8_u32(temp20.val[0]);
            temp3.val[0]= vreinterpret_u8_u32(temp17.val[1]);
            temp3.val[1]= vreinterpret_u8_u32(temp19.val[1]);
            temp4.val[0]= vreinterpret_u8_u32(temp18.val[1]);
            temp4.val[1]= vreinterpret_u8_u32(temp20.val[1]);			
			
            vst1_u8(&dstImg[(j+1)*height-8-i],temp1.val[0]);
            vst1_u8(&dstImg[(j+2)*height-8-i],temp1.val[1]);			
            vst1_u8(&dstImg[(j+3)*height-8-i],temp2.val[0]);
			vst1_u8(&dstImg[(j+4)*height-8-i],temp2.val[1]);
            vst1_u8(&dstImg[(j+5)*height-8-i],temp3.val[0]);
			vst1_u8(&dstImg[(j+6)*height-8-i],temp3.val[1]);
            vst1_u8(&dstImg[(j+7)*height-8-i],temp4.val[0]);
			vst1_u8(&dstImg[(j+8)*height-8-i],temp4.val[1]);
        }
    }
#endif
}


void MatrixRT(unsigned char* dstImg,unsigned char* srcImg,int width,int height)
{
#ifdef ENABLE_NEON
    uint8x8x4_t mat1;
    uint8x8x4_t mat2;

    uint8x8x2_t temp1;
    uint8x8x2_t temp2;
    uint8x8x2_t temp3;
    uint8x8x2_t temp4;

    uint16x4x2_t temp5;
    uint16x4x2_t temp6;
    uint16x4x2_t temp7;
    uint16x4x2_t temp8;
    uint16x4x2_t temp9;
    uint16x4x2_t temp10;
    uint16x4x2_t temp11;
    uint16x4x2_t temp12;

    uint32x2x2_t temp13;
    uint32x2x2_t temp14;
    uint32x2x2_t temp15;
    uint32x2x2_t temp16;
    uint32x2x2_t temp17;
    uint32x2x2_t temp18;
    uint32x2x2_t temp19;
    uint32x2x2_t temp20;
	
	
    for(int i=0; i<height; i+=8)
    {
        for(int j=0; j<width; j+=8)
        {
            mat1.val[0]=vld1_u8(srcImg+i*width+j); 
            mat1.val[1]=vld1_u8(srcImg+(i+1)*width+j); 
            mat1.val[2]=vld1_u8(srcImg+(i+2)*width+j); 
            mat1.val[3]=vld1_u8(srcImg+(i+3)*width+j); 
            mat2.val[0]=vld1_u8(srcImg+(i+4)*width+j); 
            mat2.val[1]=vld1_u8(srcImg+(i+5)*width+j); 
            mat2.val[2]=vld1_u8(srcImg+(i+6)*width+j); 
            mat2.val[3]=vld1_u8(srcImg+(i+7)*width+j); 
			
			temp1=vtrn_u8(mat1.val[0],mat1.val[1]); 
            temp2=vtrn_u8(mat1.val[2],mat1.val[3]);
            temp3=vtrn_u8(mat2.val[0],mat2.val[1]);
            temp4=vtrn_u8(mat2.val[2],mat2.val[3]);

			temp5.val[0]= vreinterpret_u16_u8(temp1.val[0]);
            temp5.val[1]= vreinterpret_u16_u8(temp1.val[1]);
            temp6.val[0]= vreinterpret_u16_u8(temp2.val[0]);
            temp6.val[1]= vreinterpret_u16_u8(temp2.val[1]);
            
			temp7.val[0]= vreinterpret_u16_u8(temp3.val[0]);
            temp7.val[1]= vreinterpret_u16_u8(temp3.val[1]);
            temp8.val[0]= vreinterpret_u16_u8(temp4.val[0]);
            temp8.val[1]= vreinterpret_u16_u8(temp4.val[1]);

            temp9 =vtrn_u16(temp5.val[0],temp6.val[0]);
            temp10=vtrn_u16(temp5.val[1],temp6.val[1]);
            temp11=vtrn_u16(temp7.val[0],temp8.val[0]);
            temp12=vtrn_u16(temp7.val[1],temp8.val[1]);
			
            temp13.val[0]= vreinterpret_u32_u16(temp9.val[0]);
            temp13.val[1]= vreinterpret_u32_u16(temp9.val[1]);
            temp14.val[0]= vreinterpret_u32_u16(temp10.val[0]);
            temp14.val[1]= vreinterpret_u32_u16(temp10.val[1]);
            temp15.val[0]= vreinterpret_u32_u16(temp11.val[0]);
            temp15.val[1]= vreinterpret_u32_u16(temp11.val[1]);
            temp16.val[0]= vreinterpret_u32_u16(temp12.val[0]);
            temp16.val[1]= vreinterpret_u32_u16(temp12.val[1]);			
			
			temp17=vtrn_u32(temp13.val[0],temp15.val[0]);
            temp18=vtrn_u32(temp13.val[1],temp15.val[1]);
            temp19=vtrn_u32(temp14.val[0],temp16.val[0]);
            temp20=vtrn_u32(temp14.val[1],temp16.val[1]);

            temp1.val[0]= vreinterpret_u8_u32(temp17.val[0]);
            temp1.val[1]= vreinterpret_u8_u32(temp19.val[0]);
            temp2.val[0]= vreinterpret_u8_u32(temp18.val[0]);
            temp2.val[1]= vreinterpret_u8_u32(temp20.val[0]);
            temp3.val[0]= vreinterpret_u8_u32(temp17.val[1]);
            temp3.val[1]= vreinterpret_u8_u32(temp19.val[1]);
            temp4.val[0]= vreinterpret_u8_u32(temp18.val[1]);
            temp4.val[1]= vreinterpret_u8_u32(temp20.val[1]);

            vst1_u8(&dstImg[(width-j-8)*height + i],temp4.val[1]);
            vst1_u8(&dstImg[(width-j-7)*height + i],temp4.val[0]);	
            vst1_u8(&dstImg[(width-j-6)*height + i],temp3.val[1]);
			vst1_u8(&dstImg[(width-j-5)*height + i],temp3.val[0]);
            vst1_u8(&dstImg[(width-j-4)*height + i],temp2.val[1]);
			vst1_u8(&dstImg[(width-j-3)*height + i],temp2.val[0]);
            vst1_u8(&dstImg[(width-j-2)*height + i],temp1.val[1]);
			vst1_u8(&dstImg[(width-j-1)*height + i],temp1.val[0]);
        }
    }
#endif
}

void SkinWhitening(unsigned char* pYuvData, int nWidth, int nHeight, float beta)
{
	unsigned char Table[256];
	
	float logbeta = log(beta);

	for(int i = 0 ; i < 256; i++)
	{
		float pixel = ((float)i)/256.0f ;
		Table[i] = (unsigned char)(256*log(pixel*(beta-1)+1)/logbeta);
	}
	for(int j = 0 ; j < nWidth*nHeight; j++)
	{
		pYuvData[j] = Table[pYuvData[j]];
	}

	return ;
}

void DomainFilter(unsigned char* pInARGBData, unsigned char* pOutARGBData, int nWidth, int nHeight)
{
	float ratio = gsigma_s / gsigma_r;

	long start,end;
	double duration;

	start = clock();		
	GetDiffImgY_NEON(pInARGBData, xgDiff, ygDiff, nWidth, nHeight, ratio);	
	end = clock();
	duration =  (double)(end - start) / CLOCKS_PER_SEC;
	LOGI("GetDiffImgY_NEON Time is: %f", duration);
    
    if(gslight) {
        FilterY_NEON(pInARGBData, fwTable[0], nWidth, nHeight, ygDiff);
        //FilterY_NEON(pInARGBData, fwTable[0], nWidth, nHeight, xgDiff);
        return;
    }

	start = clock();
	MatrixT(xgDiff1,xgDiff,nWidth,nHeight);
	end = clock();
	duration =  (double)(end - start) / CLOCKS_PER_SEC;
	LOGI("MatrixT Time is: %f", duration);
	
    start = clock();
    FilterY_NEON(pInARGBData, fwTable[0], nWidth, nHeight, ygDiff);
    end = clock();
    duration = (double)(end - start) / CLOCKS_PER_SEC;
    LOGI("FilterY_NEON Time is: %f", duration);
    
    start = clock();
    MatrixT(TempImageDataH, pInARGBData, nWidth, nHeight);
    end = clock();
    duration = (double)(end - start) / CLOCKS_PER_SEC;
    LOGI("MatrixT Time is: %f", duration);
    
    start = clock();
    FilterY_NEON(TempImageDataH, fwTable[0], nHeight, nWidth, xgDiff1);
    end = clock();
    duration = (double)(end - start) / CLOCKS_PER_SEC;
    LOGI("FilterY_NEON Time is: %f", duration);
    
    /*start = clock();
     FilterY_NEON(TempImageDataH, fwTable[1], nHeight, nWidth, xgDiff1);
     end = clock();
     duration = (double)(end - start) / CLOCKS_PER_SEC;
     LOGI("FilterY_NEON Time is: %f", duration);*/
    
    start = clock();
    MatrixRT(pInARGBData, TempImageDataH, nHeight, nWidth);
    end = clock();
    duration = (double)(end - start) / CLOCKS_PER_SEC;
    LOGI("MatrixRT Time is: %f", duration);
    
    /*start = clock();
     FilterY_NEON(pInARGBData, fwTable[1], nWidth, nHeight, ygDiff);
     end = clock();
     duration = (double)(end - start) / CLOCKS_PER_SEC;
     LOGI("FilterY_NEON Time is: %f", duration);*/

	return ;
}

void Denoise_Processing_image(unsigned char* InPutY, int stride_Y,
	                          unsigned char* InPutU, int stride_U,
							  unsigned char* InPutV, int stride_V,
							  int nWidth, int nHeight,
							  float SmoothLevel, float WhiteLevel)	
{
	// 设置磨皮参数，有效值为 0.01--0.20, 优化值为 0.035
    int nWidth_strip = ((nWidth+15)>>4)<<4;
    int nHeight_strip = ((nHeight+15)>>4)<<4;
    long start, end;
    double duration;
    start = clock();
	set_sigma_r(SmoothLevel);
	end = clock();
	duration = (double)(end - start) / CLOCKS_PER_SEC;
	LOGI("set_sigma_r time is: %f", duration);
	//LOGD("process white: nWidth:%d, nw:%d", nWidth_strip, nWidth);
	/*for(int j = 0; j < nHeight; j++)
	{
		for(int i = 0; i < nWidth; i += 8)
		{
			uint8x8_t Pixel = vld1_u8(InPutY+j*stride_Y+i);
			vst1_u8(&ImageDataY[j*nWidth_strip+i],Pixel);
		}
	}*/

    for(int j = 0; j < nHeight; j++)
    {
        memcpy(ImageDataY+j*nWidth_strip, InPutY+j*stride_Y, nWidth);
    }

	// 美白参数，优化值为 3.35
	start = clock();
	SkinWhitening(ImageDataY, nWidth_strip, nHeight_strip, WhiteLevel);
	end = clock();
	duration = (double)(end - start) / CLOCKS_PER_SEC;
	LOGI("SkinWhitening Time is: %f", duration);
	start = clock();
	DomainFilter(ImageDataY, ImageDataY, nWidth_strip, nHeight_strip);
	end = clock();
	duration = (double)(end - start) / CLOCKS_PER_SEC;
	static double mDomainFileterAverageTime = 0.0f;
	static int mFrameIndex = 0;
	mFrameIndex++;
	if(mFrameIndex == 1){
		mDomainFileterAverageTime = duration;
	}
	else{
		mDomainFileterAverageTime += duration;
		mDomainFileterAverageTime /= 2.0f;
	}

	LOGI("DomainFilter Average Time is: %f, Frame Index is: %d", mDomainFileterAverageTime, mFrameIndex);

	/*for(int j = 0; j < nHeight; j++)
	{
		for(int i = 0; i < nWidth; i += 8)
		{
			uint8x8_t Pixel = vld1_u8(ImageDataY+j*nWidth_strip+i);
			vst1_u8(InPutY+j*stride_Y+i,Pixel);
		}			
	}*/
    for(int j = 0; j < nHeight; j++)
    {
        memcpy(InPutY+j*stride_Y, ImageDataY+j*nWidth_strip, nWidth);
    }

}




