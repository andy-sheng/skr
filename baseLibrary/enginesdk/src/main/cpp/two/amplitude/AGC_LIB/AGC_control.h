#ifndef AGC_CONTROL_H
#define AGC_CONTROL_H

#include "../Dynamic_LIB/Dynamic_SDK_API.h"
#include "../Level_LIB/Level_SDK_API.h"
#include "../../filter/Filter_LIB/Filter_SDK_API.h"
#include "../../naturemix/NatureMix_LIB/NatureMix_SDK_API.h"
#include "../DCC_LIB/DCC_SDK_API.h"
#include "SKR_AGC_defines.h"
#include "SKR_AGC_typedefs.h"
#include "../../Delay/Delay_LIB/Delay_SDK_API.h"
#include "../../pitch/pitch_Normalized_CrossCorrelation_LIB/pitch_NCC_SDK_API.h"
#include "../../resample/Resample_LIB/Resample_SDK_API.h"

#include <stdio.h>

/* define this to (1) to use trae_filter insteadof SKR_Filter */
/* trae_filter uses SSE2 intrinsic whenever SSE2 instructions are available */
/* This is originally developed to cope with Intel's complain about x87 instructions */
/* in critical path. It turns out to boost filter performance a little bit, though ;) */
//#define AGC_USE_TRAE_FILTER        (0)

#ifdef __cplusplus
extern "C"
{
#endif


typedef struct VAD_Mono_channel_memory{
	Level_s LevelForAGC;
	float memnoiseVAD_db;
	int memUpwatchVAD;
	float memnewnoiseVAD_db[NOISE_UP_THRESHOLDVAD];
	float mempreavgdb[MEMPREAVGDB];
	int memSpeech;
	float memminnoise_db;
	float memmax_db;
	float mempeakavg_db;//让平均电平通过峰值检波(充电时间为0)得出平稳后的平均电平
	int memSilenceCountDown;//音尾保护

	int chanel;
	int samplerate;
}VAD_ID;




typedef struct PESV_channel_memory{
	//VWLevel_s mvwl_rms;//now we had use peak only, pesv_rms can be removed...
	VWLevel_s mvwl_peak;
	Level_s mlevel;
	VAD_ID m_AGCVAD;
	DCC_ID mDCC;

	int chanel;
	int samplerate;
	int needDC;
}PESV_ID;



#define PRMS (300)



typedef struct AGC_Mono_channel_memory{
	THE_MIC mymic;//include vmic
	ECNLP thenlp;
	Dynamic_ID DyForAGC;
	Level_s LevelForAGC;
	THE_MIC lastmic;
	int lastvvol;

	PESV_ID mPESV;
	Resample_ID mrs;
	pitch_NCC_ID mpitch;
	APD_ID mavg;
	int mempre0len;
	int memPeriodCountDown;//this is for pavg
	int memthePCD;//theperoid countdown

	Filter_s FilterForAGC;
	//Level_s LevelForDAGC;

		
	//float memgainmodifed_dB;
	
	float memnoise_db,memnoiseVAD_db,memmaxsomeindb;
	int memUpwatch,memUpwatchVAD,memUpsomeinwatch;
	float memnewnoise_db[NOISE_UP_THRESHOLD],memnewnoiseVAD_db[NOISE_UP_THRESHOLDVAD],memsominavgdb[MAXSOMEIN_UPDATE_WINDOW];

	int memholdlowernoisewatch;
	float memminxdb;
	
	int memUPintevalwatch;
	int memDownintevalwatch;
	int memvolholdwatch;
	float memmicvol;


	//float mymic.rvolnoise[7][21];//80-100//7 step boost
	////float mymic.rvolnoise[21];//80-100
	//float mymic.bigsteprvol[7];


	int memDown;
	int memLowVol;
	int memNegativeFB;
	int memUP;
	int memSaturated;
	int memSpeech;

	float memminnoise_db;
	float memboost_dB;
	float memlastboost_dB;
	float memmax_db;
	float memmaxavg_db;
	float mempeakavg_db;//让平均电平通过峰值检波(充电时间为0)得出平稳后的平均电平
	int memSilenceCountDown;//音尾保护
    
    int memDYNoiseGateSilenceCountDown;
	
	int memThisIsCut;//not use RunAPI's boost and gainmod_dB
	short memtherealnoise[SKR_MAX_FRAME_SAMPLE_STEREO];
	short memtherealnoise_down[SKR_MAX_FRAME_SAMPLE_STEREO];
	short memtherealnoise_up[SKR_MAX_FRAME_SAMPLE_STEREO];

	int memFirstSet;
	int memFirstAdaptUP;
	int memFirstAdaptDOWN;
	int memLongAllZero;
	int memlongmute;
	float memrvolokmin;
	int memrvoloktimes;
	int memmutetimes;
	float memrvolmutemax;
	int memboostupdevicenormal;//After boostup,if memboostupdevicenormal is once > 10,we don't check LongAllZero problem caused by abnormal boost position 
	float memboost_dBlimit;
	//float memdigitalboostgain;//for variable gain curve
	
	/*vol*/
	float GainModMax_dB;//>0对前一级的反馈调整限于THEMAXOF和THEMINOF之间，由memgainmodifed指示当前已调增益位置
	float GainModMin_dB;//<0
	float Noise_db;//认为这以下为噪声，这期间不做反馈调整
	float TargetU_db;//这是目标电平区间上限
	float TargetD_db;//这是目标电平区间下限
	float UpSpeed_dB;//这是个速率，所以大于0，这个速度不能超过GainModMax_dB与0dB的距离
	float DownSpeed_dB;
	int UPIntevalTime_ms;
	int DownIntevalTime_ms;

	int memworkingwatch_ms;//this is a new parameter,maybe agc should work aggressively at beginning and softly then
	int memvvolchangetime;
	int memsamemicliky;//for dagc it is mem; for aagc it is set
	//FILE *memfrlastmicinf;

	//int UP_UPIntevalTime_ms;
	//int Down_UPIntevalTime_ms;

	//int UP_DownIntevalTime_ms;
	//int Down_DownIntevalTime_ms;

	float (*gainmicmod)(THE_MIC *amic,float boost_dB,float vol,float Gain);
	/*boost*/
	int N20ms_ForDown;//#define N_Down 200
	int N20ms_ForUP;//#define N_UP 130
	float SmallVol_Threshold;//#define K 0.6
	int N20msSmallVol_Threshold;//#define M 130
	int N20msNegMod_Threshold;//#define F 20 
	int N20msSatu_Threshold;//#define X 30

	int chanel;
	int samplerate;
	int postmoddB;//depending on the device,usually it should less than 0dB,only when the device's postproc *0.x...
	float vvolmaxdB;
	float vvolmindB;
	float vvolfstdB;

	int vol;
	float boostinfo[4];//pos,min,max,step
	//int micvol;
	
	int DyKind;//see defines
	int FeedbackKind;//see setmic()
	int FBSimplex;//if fb==0 it must be 0
	int SimpVADBypass;
	int PostAGC;//0 means not PostAGC,in this condition you should set (min,fst,max) before calcuAPI;
				//1 means PostAGC,you should set postmoddB//postAGC min = -postmoddB ,max = 0,now
	//int levelhpBypass;//是否预处理



}AGC_ID;




#ifdef __cplusplus
}
#endif

#endif