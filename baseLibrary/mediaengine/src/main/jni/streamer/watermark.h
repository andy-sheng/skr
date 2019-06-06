#ifndef __WATERMARK_H__
#define __WATERMARK_H__

#include <stdint.h>

typedef struct WaterMarkImage {
    int x;
    int y;
    int width;
    int height;
    int alpha;
    uint8_t *alphaY;
    uint8_t *dataY;
    int strideY;
    uint8_t *alphaC;
    uint8_t *dataU;
    uint8_t *dataV;
    int strideC;
} WaterMarkImage;

WaterMarkImage* wmi_initNew(uint8_t* buf, int x, int y, int w, int h, int alpha);
void wmi_destory(WaterMarkImage* img);

void wmi_add_to_videoNew(uint8_t* buf, int width, int height,  WaterMarkImage* logo);

#endif
