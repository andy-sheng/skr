#include <stdint.h>
#include <stdlib.h>
#include <string.h>
#include <libyuv/convert.h>
#include "watermark.h"

void addLogoOneComponent(uint8_t* pBuffer, const int32_t iStride,
                         const int32_t iLogoX, const int32_t iLogoW,
                         const int32_t iLogoH, const uint8_t* pLogoValue, 
                         const uint8_t* pLogoAlpha, const int32_t iLogoWeight)
{
    for (int32_t j = 0; j<iLogoH; ++j) {
        uint8_t* data = pBuffer + j*iStride;
        int32_t width = iLogoW;
        if ((iLogoX+iLogoW) > iStride) {
            width = iStride - iLogoX;
        }
        for (int32_t i = 0; i < width; ++i) {
            int idx = j*iLogoW + i;
            if (pLogoValue[idx] && pLogoAlpha[idx]) {
                int32_t logoAlpha = (iLogoWeight*pLogoAlpha[idx]) >> 8;
                *data = ((int32_t)pLogoValue[idx] * logoAlpha + (int32_t)(*data) * (255 - logoAlpha)) >> 8;
            }
            ++data;
        }
    }
}

WaterMarkImage* wmi_initNew(uint8_t* buf, int x, int y, int w, int h, int alpha)
{
	if (buf == NULL)
        return NULL;

    WaterMarkImage* wmi = (WaterMarkImage*)calloc(1, sizeof(WaterMarkImage));
    if (wmi == NULL) {
        return NULL;
    }

    wmi->alpha = alpha;
    wmi->x = x;
    wmi->y = y;
	wmi->width = w/2*2;
	wmi->height = h/2*2;

    int sizeY = w*h;
    wmi->strideY = w;
	wmi->dataY = (uint8_t*)malloc(wmi->strideY*h);
    wmi->alphaY = (uint8_t*)malloc(wmi->strideY*h);

    int hC = (h+1) / 2;
    wmi->strideC = (w+1) / 2;
	wmi->dataU = (uint8_t*)malloc(wmi->strideC*hC);
    wmi->dataV = (uint8_t*)malloc(wmi->strideC*hC);
    wmi->alphaC = (uint8_t*)malloc(wmi->strideC*hC);

    // restore alpha data
    for (int i=0; i<sizeY; i++) {
        wmi->alphaY[i] = uint8_t(buf[i*4+3]);
    }
    for (int j=0; j<(h/2); j++) {
        int len = wmi->strideC*j;
        for (int i=0; i<wmi->strideC; i++) {
            wmi->alphaC[i+len] = ((int)wmi->alphaY[2*i+2*j*wmi->strideY] +
								  (int)wmi->alphaY[(2*i+1)+2*j*wmi->strideY] +
								  (int)wmi->alphaY[2*i+(2*j+1)*wmi->strideY] +
								  (int)wmi->alphaY[(2*i+1)+(2*j+1)*wmi->strideY]) >> 2;
        }
    }

    int ret = libyuv::ARGBToI420((const uint8*)buf, w * 4, 
								 wmi->dataY, wmi->strideY, 
								 wmi->dataU, wmi->strideC, 
								 wmi->dataV, wmi->strideC,
								 wmi->width, wmi->height);
    if (ret) {
        free(wmi);
        return NULL;
    }
    return wmi;
}

void wmi_destory(WaterMarkImage* wmi)
{
    if (wmi == NULL)
        return;
    
    if (wmi->dataY) {
        free(wmi->dataY);
        wmi->dataY = NULL;
    }
    if (wmi->dataU) {
        free(wmi->dataU);
        wmi->dataU = NULL;
    }
    if (wmi->dataV) {
        free(wmi->dataV);
        wmi->dataV = NULL;
    }
    if (wmi->alphaY) {
        free(wmi->alphaY);
        wmi->alphaY = NULL;
    }
    if (wmi->alphaC) {
        free(wmi->alphaC);
        wmi->alphaC = NULL;
    }
    free(wmi);
}

void wmi_add_to_videoNew(uint8_t* buf, int width, int height,  WaterMarkImage* logo)
{
	if (logo->x >= width || logo->y >= height)
        return;

    int YSize = width * height;
	uint8_t* pVideoY = buf + logo->x + logo->y * width;
    uint8_t* pVideoU = buf + YSize + (logo->x >> 1) + (logo->y >> 1)*(width >> 1);
    uint8_t* pVideoV = buf + YSize + (YSize >> 2) + (logo->x >> 1) + (logo->y >> 1)*(width >> 1);

    int32_t logoH = logo->height;
    if ((logo->y+logo->height) >= height) {
        logoH = height - logo->y - 1;
    }
    addLogoOneComponent(pVideoY, width, logo->x, logo->strideY, logoH, logo->dataY, logo->alphaY, logo->alpha);
    addLogoOneComponent(pVideoU, width/2, logo->x/2, logo->strideC, logoH/2, logo->dataU, logo->alphaC, logo->alpha);
    addLogoOneComponent(pVideoV, width/2, logo->x/2, logo->strideC, logoH/2, logo->dataV, logo->alphaC, logo->alpha);
}

