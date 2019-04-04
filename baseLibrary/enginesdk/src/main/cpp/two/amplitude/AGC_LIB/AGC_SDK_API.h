#ifndef AGC_SDK_API_H
#define AGC_SDK_API_H

#include "AGC_control.h"

#ifdef __cplusplus
extern "C"
{
#endif

void AGCReset_API(AGC_ID *mAGC);
void AGCCalcu_API(AGC_ID *mAGC);
/*
gainmod_dB:
0-输出；反馈dB，外部可据此调设备或数字增益
1-输入；当前麦克风增强位置，如果没有获取到请置-1：-1无boost，01xp，
2-输入；当前设备boost级别最小值，如果没有获取到请置-1
3-输入；当前设备boost级别最大值，如果没有获取到请置-1
4-输入；当前设备boost的一个阶的值，如果没有获取到请置-1
boost:
0-输出；boost调整建议：+1上一个台阶，-1下一个台阶，0不动
1-输出；VAD结果：0为噪声，1为语音 
2-输出/输入；vol结果0~65535；如果实时获取音量模式，则它也做输入vol值用
3-输出；削顶检测结果：0为未过顶，1为过顶，2为严重过顶
4-输入；来自ec的单讲信息，1单讲，0非单讲
5-输出；输出es信息，1es，0noes//暂时没用到
*/
void AGCRun_API(AGC_ID *mAGC, short *input, int inLen, short *output, float *gainmod_dB, int *boost);

void GetNoiseCalcu_API(AGC_ID *mAGC);
void GetNoiseReset_API(AGC_ID *mAGC);
float GetNoiseandAvgRun_API(AGC_ID *mAGC,short *input, int inLen);
void GetNoiseRun_API(AGC_ID *mAGC,short *input, int inLen);
int NoiseRepairRun_API(AGC_ID *mAGC,short *input, int inLen, short *output);
int NoiseRepairRun_API_10ms_tmod(AGC_ID *mAGC,float *input, int inLen, float *output,float threshold);
void NoiseRepairCalcu_API(AGC_ID *mAGC);
void NoiseRepairReset_API(AGC_ID *mAGC);
/* Initialize AGC module, call this once before using AGC module */
int AGCInit_API(AGC_ID *mAGC);

/* Uninitialize AGC module, call this once after using AGC module */
int AGCUninit_API(AGC_ID *mAGC);


void VADReset_API(VAD_ID *mAGC);
void VADCalcu_API(VAD_ID *mAGC);
int VADRun_API(VAD_ID *mAGC, short *input, int inLen);
int VAD2Run_API(VAD_ID *mAGC, short *input, int inLen,float vippesvdb,float period);
int VAD2mod3Run_API(VAD_ID *mAGC, short *input, int inLen,float vippesvdb,float period);
int VAD2mod4Run_API(VAD_ID *mAGC, short *input, int inLen,float vippesvdb,float period);
void PESVResetCalcu_API(PESV_ID *mPESV);//if samplerate has changed reset must be done
void PESVRun_API(PESV_ID *mPESV,short *x,int xlen);
void PESV_API(PESV_ID *mPESV,float *peakdb,float *peaklq,float *rmsdb,float *rmslq);



extern const float NUM16[7][3];
extern const float DEN16[7][3];
extern const float NUM48[7][3];
extern const float DEN48[7][3];
extern const float NUM441[7][3];
extern const float DEN441[7][3];
extern const float NUM32[7][3];
extern const float DEN32[7][3];
extern const float NUM24[7][3];
extern const float DEN24[7][3];
extern const float NUM8[7][3];
extern const float DEN8[7][3];

#ifdef __cplusplus
}
#endif

#endif