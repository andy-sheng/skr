#ifndef _DENOISE_EXPORT_H
#define _DENOISE_EXPORT_H

void InitFilter(int nW, int nH,bool slight_beauty);

void ReleaseFilter();

// 函数说明：InPutY  InPutU  InPutU 分别为指向 Y/U/V 像素分量的指针
// stride_y  stride_U stride_V 分别为 Y/U/V 分量的 stride
// nWidth  nHeight 为图像的实际宽度和高度
void Denoise_Processing_image(unsigned char* InPutY, int stride_Y,
	                          unsigned char* InPutU, int stride_U,
							  unsigned char* InPutV, int stride_V,
							  int nWidth, int nHeight,
							  float SmoothLevel, float WhiteLevel);
#endif







