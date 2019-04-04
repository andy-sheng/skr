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
0-���������dB���ⲿ�ɾݴ˵��豸����������
1-���룻��ǰ��˷���ǿλ�ã����û�л�ȡ������-1��-1��boost��01xp��
2-���룻��ǰ�豸boost������Сֵ�����û�л�ȡ������-1
3-���룻��ǰ�豸boost�������ֵ�����û�л�ȡ������-1
4-���룻��ǰ�豸boost��һ���׵�ֵ�����û�л�ȡ������-1
boost:
0-�����boost�������飺+1��һ��̨�ף�-1��һ��̨�ף�0����
1-�����VAD�����0Ϊ������1Ϊ���� 
2-���/���룻vol���0~65535�����ʵʱ��ȡ����ģʽ������Ҳ������volֵ��
3-����������������0Ϊδ������1Ϊ������2Ϊ���ع���
4-���룻����ec�ĵ�����Ϣ��1������0�ǵ���
5-��������es��Ϣ��1es��0noes//��ʱû�õ�
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