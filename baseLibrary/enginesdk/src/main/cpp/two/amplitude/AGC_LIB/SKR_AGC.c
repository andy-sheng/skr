#include "AGC_control.h"
#include "SKR_AGC_functions.h"
#include "SKR_AGC_tables.h"
#include "AGC_SDK_API.h"
#include "../Dynamic_LIB/Dynamic_SDK_API.h"
#include "../Dynamic_LIB/SKR_Dynamic_functions.h"
#include "../Level_LIB/Level_SDK_API.h"
#include "../../common/functions.h"
#include <stdlib.h>
#include <assert.h>
#include <stdio.h>

//for TRAE dump ----not common！

//#include "../../../../configs/TRAE_settings.h"//if there is no this file,just delete this line and #define SEECLIPPING 0 is OK
#define SEECLIPPING (0)//TRAE_DEBUG_AGCDUMP//当dump数据时打开之-----由于目前trae的dump已经不再看这个数据项了，所以关闭之，如果想看则单独改一下1，这样就可以将SKR与trae彻底解耦
//即如果TRAE选择dump数据则，SEECLIPPING置1，否则为0，此处耦合仅是调试上的耦合。即SEECLIPPING只在调试时有用

//#define SEELEVEL
#ifdef SEELEVEL
#include <stdio.h>
#endif

#define DEBUGAGC (0)



void Options_for_TRAE_NoiseRepair(AGC_ID *mAGC)
{

	mAGC->LevelForAGC.LevelAttackms = 0.5;//暂时没用
	mAGC->LevelForAGC.LevelRealeasems = 100.0;

}

void Options_for_TRAE_AGC(AGC_ID *mAGC)
{

	/*前处理部分;RadioLimitMod*/
	mAGC->DyForAGC.neverclipping = 0;
	mAGC->DyForAGC.SideChain = NOUSE;
	mAGC->DyForAGC.Bypass = 0;
	mAGC->LevelForAGC.LevelAttackms = 0.5;//暂时没用
	mAGC->LevelForAGC.LevelRealeasems = 300.0;

	StaticCurvReset(&mAGC->DyForAGC.CurveOption,mAGC->DyKind);

	if ((mAGC->DyKind & DY_VARIBLECUR) == 0)
	{
		//mAGC->DyForAGC.CurveOption.b_db = -2.8;//v2beta:-1.2


		if (mAGC->DyKind & DY_NLP)
		{
			mAGC->DyForAGC.DynamicAttackms = 1.25;
			mAGC->DyForAGC.DynamicReleasems = 0.0005;
			//mAGC->DyForAGC.CurveOption.k = 1.0;//v1:1.15;//v2beta:1.17
		} 
		else
		{
			mAGC->DyForAGC.DynamicAttackms = 1.25;
			mAGC->DyForAGC.DynamicReleasems = 70.0;
			mAGC->DyForAGC.CurveOption.k = 1.0;//v1:1.15;//v2beta:1.17
		}


	}
	else
	{
		//mAGC->DyForAGC.CurveOption.k = 1.05;
		//mAGC->DyForAGC.DynamicAttackms = 20.0;
		//mAGC->DyForAGC.DynamicReleasems = 40.0;
		mAGC->DyForAGC.CurveOption.k = 1.0;
		mAGC->DyForAGC.DynamicAttackms = 1.0;
		mAGC->DyForAGC.DynamicReleasems = 110.0;
	}

	/*Feedback vol:*/
	mAGC->UPIntevalTime_ms = 500;//660;
	mAGC->DownIntevalTime_ms = 80;

	//mAGC->UpSpeed_dB = 1.03;
	//mAGC->DownSpeed_dB = 1.22;
	if (mAGC->samplerate>=32000)
	{
		mAGC->DownSpeed_dB = 0.45;//for music...//in fact intevaltime should also be modified
		mAGC->UpSpeed_dB = 0.53;
	} 
	else
	{
		mAGC->DownSpeed_dB = 1.15;
		mAGC->UpSpeed_dB = 1.03;
	}

	mAGC->GainModMax_dB = 80.0;
	mAGC->GainModMin_dB = -10.0;
	mAGC->Noise_db = -35.0;
	//mAGC->TargetU_db = -3.8;
	
	if (mAGC->mymic.capability & CAPABILITY_VVOL)
	{
		mAGC->TargetD_db = -9.0-TARD_VMICMOD_DB;
        mAGC->TargetU_db = -3.8 - 0.4;
	}
	else
	{
		mAGC->TargetD_db = -9.0;
        mAGC->TargetU_db = -3.8;
	}
	/*Feedback boost:*/
	mAGC->N20ms_ForDown = 400;//#define N_Down 200
	mAGC->N20ms_ForUP = 240;//#define N_UP 130
	mAGC->SmallVol_Threshold = 0.33;//#define K 0.6
	mAGC->N20msSmallVol_Threshold = 65;//#define M 130
	mAGC->N20msNegMod_Threshold = 4;//#define F 20 
	mAGC->N20msSatu_Threshold = 5;//#define X 30
}



void AGCReset_API(AGC_ID *mAGC)
{
	int i;
	int j;

	mAGC->memvvolchangetime = 0;
	mAGC->memrvoloktimes = 0;
	mAGC->memmutetimes = 0;
	mAGC->memrvolmutemax = 0;
	mAGC->memrvolokmin = 1;
	mAGC->memlongmute = 0;
	mAGC->memworkingwatch_ms = 0;
	mAGC->memsamemicliky = 0;
	//mAGC->memfrlastmicinf = NULL;
	//mAGC->memgainmodifed_dB = FIRSTVIRTUALVOL_DB;//用户每次自己调整上一级的音量时，都应让memgainmodifed_dB重置，从而保证对用户新的音量配置有新的可调范围
	mAGC->memnoise_db = 0.0;
	mAGC->memnoiseVAD_db = 0.0;
	mAGC->memmaxsomeindb = -90.0;
	mAGC->memUpwatch = 0;
	mAGC->memUpwatchVAD = 0;
	mAGC->memUpsomeinwatch = 0;
	mAGC->memUPintevalwatch = 0;
	mAGC->memDownintevalwatch = 0;
	mAGC->memvolholdwatch = 0;
	mAGC->memmicvol = 1.0;//如果获取不了vol,由gain_dB对外工作，此时认为它是处于满（for ec短路...）
	//mAGC->memmicvolSS = 1.0;
	mAGC->memDown = 0;
	mAGC->memLowVol = 0;
	mAGC->memNegativeFB = 0;
	mAGC->memUP = 0;
	mAGC->memSaturated = 0;
	mAGC->memSpeech = 1;
	//mAGC->memCS = 1;
	mAGC->thenlp.memCS = 0;
	mAGC->thenlp.memCSSilenceCountDown = 0;
	mAGC->thenlp.memmicvolSS = 1.0;

	mAGC->memThisIsCut = 0;
	for (i = 0;i<SKR_MAX_FRAME_SAMPLE_STEREO;i++)
	{
		mAGC->memtherealnoise[i] = 0;
		mAGC->memtherealnoise_down[i] = 0;
		mAGC->memtherealnoise_up[i] = 0;
	}
	for (j = 0;j<7;j++)
	{
		for (i = 0;i<21;i++)
		{
			mAGC->mymic.rvolnoise[j][i] = 123;//says not be coverd
		}
		mAGC->mymic.bigsteprvol[j] = 1.1;//says not be coverd
	}

	

	//mAGC->memSS = 0;
	mAGC->memminnoise_db = 0.0;
	mAGC->memmaxavg_db = -95.0;
	mAGC->mempeakavg_db = -95.0;
	mAGC->memmax_db = -95.0;
	mAGC->memboost_dB = 0.0;
	mAGC->memlastboost_dB = -200.0; 
	mAGC->memLongAllZero = 0;
	mAGC->memboostupdevicenormal = 0;
	mAGC->memboost_dBlimit = 100.0;
	//mAGC->memdigitalboostgain = DIGITALGAIN_NOBOOST;
	
	/*AGCVAD:音尾保护*/
	mAGC->memSilenceCountDown = SPEECH_END_PROTECTION;
    
    mAGC->memDYNoiseGateSilenceCountDown = SPEECH_END_PROTECTION_GATE;//60ms

	/*capspeech音尾保护*/
	//mAGC->memCSSilenceCountDown = CS_END_PROTECTION;

	/*SS保护*/
	//mAGC->memSSSilenceCountDown = 0;//SS_END_PROTECTION;

	mAGC->memFirstSet = 0;
	mAGC->memFirstAdaptUP = 0;
	mAGC->memFirstAdaptDOWN = FSTAPDOWN;//we 200ms is ok for get noise

	for (i=0;i<NOISE_UP_THRESHOLD;i++)
	{
		mAGC->memnewnoise_db[i] = 0.0;
	}
	for (i=0;i<NOISE_UP_THRESHOLDVAD;i++)
	{
		mAGC->memnewnoiseVAD_db[i] = 0.0;
	}
	for (i=0;i<MAXSOMEIN_UPDATE_WINDOW;i++)
	{
		mAGC->memsominavgdb[i] = 0.0;
	}

	mAGC->memholdlowernoisewatch = 0;
	mAGC->memminxdb = 0;


	LevelReset_API(&mAGC->LevelForAGC);
	DynamicReset_API(&mAGC->DyForAGC);

	FilterReset_API(&mAGC->FilterForAGC);

	
	mAGC->mymic.vboost_dB = 0.0;

	mAGC->thenlp.memSSdown_gain = idB(SSGAIN_MINFIRST_DB);
	mAGC->thenlp.memSSdown_watch = 0;


	mAGC->lastvvol = -1;
	mAGC->lastmic.capability = -12345;
	for (j = 0;j<7;j++)
	{
		for (i = 0;i<21;i++)
		{
			mAGC->lastmic.rvolnoise[j][i] = 123;//says not be coverd
		}
		mAGC->lastmic.bigsteprvol[j] = 1.1;//says not be coverd
	}



	ResampleReset_API(&mAGC->mrs);
	pitch_NCCReset_API(&mAGC->mpitch);
	PitchAVGReset_API(&mAGC->mavg);
	mAGC->mempre0len = 88888888;
	mAGC->memPeriodCountDown = 0;//first 8s
	mAGC->memthePCD = 0;
}

void AGCCalcu_API(AGC_ID *mAGC)
{
	float *numlp,*denlp,*numhp,*denhp;
	int orderlp,orderhp;



	mAGC->mrs.insamplerate = mAGC->samplerate;
	mAGC->mrs.outsamplerate = 8000;
	mAGC->mrs.chanel = 1;
	mAGC->mrs.LinearOption = 0;
	mAGC->mrs.DDSampling = 0;
	mAGC->mrs.filterOrder = 10;
	if (mAGC->samplerate == 44100)
	{
		mAGC->mrs.LinearOption = 2;
	}
	ResampleCalcu_API(&mAGC->mrs);

	mAGC->mpitch.ELPThreshold = 35;
	mAGC->mpitch.ZperiodThreshod = 1.0;//ZPTHRESHOLD;//0.62 maybe too small
	pitch_NCCCalcu_API(&mAGC->mpitch);

	mAGC->mavg.giveup_behind = 1;
	mAGC->mavg.giveup_front = 1;
	mAGC->mavg.pitchwindowlen = 1;//200ms
	mAGC->mavg.contine = mAGC->mavg.pitchwindowlen + 1;//pVAD->mavg.contine must >= pVAD->mavg.pitchwindowlen
	mAGC->mavg.bigerthanfrontpitch = 20;
	mAGC->mavg.smallerthanfrontpitch = 20;
	mAGC->mavg.bigerthanbehindpitch = 20;
	mAGC->mavg.smallerthanbehindpitch = 20;


	mAGC->DyForAGC.Chanel = mAGC->chanel;
	mAGC->DyForAGC.samplerate = mAGC->samplerate;
	mAGC->LevelForAGC.channel = mAGC->chanel;
	mAGC->LevelForAGC.samplerate = mAGC->samplerate;
    
    if (mAGC->PostAGC == 1 && NORMIC_NOTUSEVBOOST ==mAGC->FeedbackKind)
    {
        if (mAGC->postmoddB>0)
        {
            mAGC->postmoddB = 0;
            assert(0);
        }
        //just let vvolmax>vvolmin to avoid assert in SetMicProperty,in this case the vvolmin and the vvolmax will be reconfig after SetMicProperty
        SetMicProperty(&mAGC->mymic,mAGC->FeedbackKind,mAGC->boostinfo,10,0);
    }
    else
    {
        SetMicProperty(&mAGC->mymic,mAGC->FeedbackKind,mAGC->boostinfo,mAGC->vvolmaxdB,mAGC->vvolmindB);
    }

	

	if (mAGC->PostAGC == 1 && NORMIC_NOTUSEVBOOST ==mAGC->FeedbackKind)
	{
		if (mAGC->postmoddB>0)
		{
			mAGC->postmoddB = 0;
			assert(0);
		}
		mAGC->mymic.vvolmin = idB(mAGC->postmoddB);
		mAGC->mymic.vvolmax = idB(0);
	}

	Options_for_TRAE_AGC(mAGC);
	DynamicCalcu_API(&mAGC->DyForAGC);
	LevelCalcu_API(&mAGC->LevelForAGC);
	/*hp预处理的H(z)*/

	mAGC->FilterForAGC.chanel = mAGC->chanel;
	mAGC->FilterForAGC.filtertype = 1;
	switch(mAGC->samplerate)
	{
	case 16000:
		FilterCalcu_API_FromMatlabTable(&mAGC->FilterForAGC,NUM16,DEN16,6,mAGC->FilterForAGC.filtertype);
		break;
	case 48000:
		FilterCalcu_API_FromMatlabTable(&mAGC->FilterForAGC,NUM48,DEN48,6,mAGC->FilterForAGC.filtertype);
		break;
	case 44100:
		FilterCalcu_API_FromMatlabTable(&mAGC->FilterForAGC,NUM441,DEN441,6,mAGC->FilterForAGC.filtertype);
		break;
	case 32000:
		FilterCalcu_API_FromMatlabTable(&mAGC->FilterForAGC,NUM32,DEN32,6,mAGC->FilterForAGC.filtertype);
		break;
	case 8000:
		FilterCalcu_API_FromMatlabTable(&mAGC->FilterForAGC,NUM8,DEN8,6,mAGC->FilterForAGC.filtertype);
		break;
	case 24000:
		FilterCalcu_API_FromMatlabTable(&mAGC->FilterForAGC,NUM24,DEN24,6,mAGC->FilterForAGC.filtertype);
		break;
	default:assert(0);
	}




	if (mAGC->samplerate>=32000)
	{
		mAGC->gainmicmod = GainMicMod_slow;
	}
	else
	{
		mAGC->gainmicmod = GainMicMod_mid;
	}

	mAGC->thenlp.SSdown_gainmax = idB(20.0);
	mAGC->thenlp.SSdown_gainstep = idB(2.0);
	mAGC->thenlp.SSdown_gainThreshold = 4;
	
	mAGC->mPESV.chanel = mAGC->chanel;;
	mAGC->mPESV.samplerate = mAGC->samplerate;
	mAGC->mPESV.needDC = 0;
	PESVResetCalcu_API(&mAGC->mPESV);

}

void AGCRun_API(AGC_ID *mAGC, short *input, int inLen, short *output, float *gainmod_dB, int *boost)
{
	int bubble;
	int i;
	float newvol;
	float avergex_db = 0;
    float maxx_db = -140.0;
	float capavergex_db = 0;
	float oricapavgdb = 0;
	float avgsubstax_db = 0;
	float avgsubendx_db = 0;
	int boostupinit = 0;
	float x_db[SKR_MAX_FRAME_SAMPLE_MONO];
	//float lastmicvol;
	int inRlen;
	int noisechanged;
	int boostup;
	int boostdown;
	//float outmicvol;//0---1.0
	//float T1;
	int clipping = 0;
	int somein;
	float Gain;
	short inputhped[SKR_MAX_FRAME_SAMPLE_STEREO];
	short pre_input[SKR_MAX_FRAME_SAMPLE_STEREO];
	float pre_gain = 1.0;
	short *pinput;
	short inputLPed[SKR_MAX_FRAME_SAMPLE_STEREO];
	short inputHPed[SKR_MAX_FRAME_SAMPLE_STEREO];
#if DEBUGAGC	
	short justdebug[SKR_MAX_FRAME_SAMPLE_MONO];
#endif
	int boostmove;
	int rboostmove,vboostmove;
	int NeedAGCVAD;
	float theboostdB;
	float ori_avergex_db;
	float ori_noise_db;

	float someinthreshold_db;
    int rvol;
	int rbooststep;
    float tarDdy0mod = 0.0;
    float tarUdy0mod = 0.0;
    float t2dbdy0mod = 10.0;
    
    float noisegate_db;
	float ctrnoise_db; 

	//int thisframe,j;//thisframe = 0-zeroinput
	int inabs;
	int maxinabs;
    
    int noise_notallowupvol = 0;
    int noise_downvol = 0;

	float inputdbmod;//
	int SCagressive = 0;

	//FILE *frlastmicinf = NULL;
	//THE_MIC mAGC->lastmic;
	int samemiclikely = 0;
	//int samedevtmpfdagc = 0;
	//int mAGC->lastvvol = -1;

	int volupholdtime;
	int voldownholdtime;	

	//short *signal[1];

	short in8k[300];
	int out8klen;
	short inR[SKR_MAX_FRAME_SAMPLE_MONO];
	short *pinR;
	int thepitch;
	float pitchavg;
	float periodavg;
	float periodthreshold;

	float peakdb,peaklq;
	float rmsdb,rmslq;
	int toosmall = 0;
	int memrmicvol;

	if ((mAGC->FeedbackKind == 0) && mAGC->SimpVADBypass && ((mAGC->DyKind & DY_ENABLE) == 0))
	{
		if (input != output)
		{
			for (i=0;i<inLen;i++)
			{
				output[i] = input[i];
			}
		} 
		return;
	}
		

	
	if (ZEROINPUT_THRESHOLD>0)
	{
		for (i = 0,maxinabs = 0;i<inLen;i++)
		{
			inabs = abs(input[i]);
			if (inabs > maxinabs)
			{
				maxinabs = inabs;
			}
		}

		if (maxinabs < ZEROINPUT_THRESHOLD && mAGC->memFirstSet == 1)//make sure firstset is set..
		{
			//thisframe = 0;

			boost[1] = 0;//vad
			MicAdjust(&mAGC->mymic,boost,0,0,&mAGC->memmicvol);//fb
			//for (i=0;i<inLen;i++)
			//{
			//	output[i] = input[i]*0.9;//dy
			//}
			return;
		}
	}

	if (mAGC->DyKind & DY_NS)//"ns"
	{
		pre_gain *= DY_NS_IDB;

	}

	if (mAGC->FBSimplex)
	{
		pre_gain *= mAGC->thenlp.memSSdown_gain;
	}

	if (mAGC->PostAGC == 1 && NORMIC_NOTUSEVBOOST ==mAGC->FeedbackKind)
	{
		pre_gain *= idB(-mAGC->postmoddB);
	}

	if (pre_gain == 1.0)
	{
		pinput = input;
	}
	else 
	{
		//assert(mAGC->SimpVADBypass);//because this time the input is not the same
		if (pre_gain>1.0)
		{
			for (i=0;i<inLen;i++)
			{
				pre_input[i] = stoshort(input[i]*pre_gain);
			}
		} 
		else
		{
			for (i=0;i<inLen;i++)
			{
				pre_input[i] = (short)(input[i]*pre_gain);
			}
		}
		pinput = pre_input;
	}
	

#ifdef SEELEVEL
	FILE *fwx_db;
	short *xf32_db;
	fwx_db=fopen("xf32_db.pcm", "ab");
#endif
	
	if (mAGC->SimpVADBypass == 1 && mAGC->FeedbackKind == 0 && (mAGC->DyKind == DY_ENABLE||mAGC->DyKind == (DY_STATIC_RADIOLIMIT|DY_ENABLE)))
	{
		LevelRun_API(&mAGC->LevelForAGC,pinput,inLen,x_db);
		DynamicRun_API(&mAGC->DyForAGC,pinput,x_db,inLen,output,x_db);
		return;
	}

	if (mAGC->chanel == 2)
	{
		inRlen = inLen/2;
	}
	else if(mAGC->chanel == 1)
	{
		inRlen = inLen;
	}
	else
	{
		assert(0);
	}
	if (mAGC->SimpVADBypass == 0 || mAGC->FBSimplex > 0 || (mAGC->DyKind & DY_NLP)||(mAGC->DyKind & DY_VAD_INTERNAL_CTRL)||(mAGC->DyKind & DY_BUBBLE_DTXCNG))
	{
		NeedAGCVAD = 1;
	} 
	else
	{
		NeedAGCVAD = 0;
	}

	{

		//LevelRun_API(&mAGC->LevelForAGC,inputvboosted,inLen,x_db);//先看下..高通后boost的噪声条件？等等等等..AGC高通需要慎重
		LevelRun_API(&mAGC->LevelForAGC,pinput,inLen,x_db);
	}

#ifdef SEELEVEL
	xf32_db = (short *)malloc(inRlen*sizeof(short));
	for (i = 0;i<inRlen;i++)
	{
		xf32_db[i] = (short)(x_db[i]*500);
	}
	fwrite(xf32_db, sizeof(short), inRlen, fwx_db);	
#endif

	{
		for (i = 0;i<inRlen/2;i++)
		{
			avgsubstax_db += x_db[i];
		}
		avgsubstax_db = 2*avgsubstax_db/inRlen;

		for (;i<inRlen;i++)
		{
			avgsubendx_db += x_db[i];
		}
		avgsubendx_db = 2*avgsubendx_db/inRlen;

		/*for (i = 0;i<inRlen;i++)
		{
			avergex_db += x_db[i];
		}
		avergex_db = avergex_db/inRlen;*/
		
		mAGC->mempeakavg_db = avergex_db = (avgsubendx_db + avgsubstax_db)/2;
	}
    {
        for(i = 0;i<inRlen;i++)
        {
            if(x_db[i]>maxx_db)
            {
                maxx_db = x_db[i];
            }
        }
    }
    mAGC->memmax_db = maxx_db;

    if (mAGC->thenlp.SS == 0||mAGC->memFirstAdaptDOWN>0)
    {
		noise_db3(avergex_db,inRlen,&mAGC->memnoise_db,&mAGC->memUpwatch,mAGC->memnewnoise_db,NOISE_UP_THRESHOLD);
    }
    
	
	

	if (mAGC->thenlp.SS == 0||mAGC->memFirstAdaptDOWN>0)//mAGC->memFirstAdaptDOWN is used for avoid farvad is longtime 1
	{
		noise_db3(maxx_db,inRlen,&mAGC->memnoiseVAD_db,&mAGC->memUpwatchVAD,mAGC->memnewnoiseVAD_db,NOISE_UP_THRESHOLDVAD);
	}



	//T1,memSpeech,boost[1],memSilenceCountDown后面的数字and模拟agc都不依赖此输出，且数模agc都不更改其输入
	if (NeedAGCVAD)
	{
		boost[1] = AGCVAD(&mAGC->memSpeech,&mAGC->memSilenceCountDown,mAGC->memnoiseVAD_db,avgsubstax_db,avgsubendx_db,avergex_db);
	}
	else
	{
		boost[1] = 1;
	}

	//if (mAGC->FBSimplex == 0)
	//{
	//	boost[4] = 0;
	//}

	ESDetect_Simplex(&mAGC->thenlp,boost[4],boost[1]);

	//add sth period and pesv...
	{
		FilterRun_API(&mAGC->FilterForAGC,input,inLen,inputhped);
		//for (i=0;i<inLen;i++)
		//{
		//	inputhped[i] = input[i];
		//}
		PESVRun_API(&mAGC->mPESV,input,inLen);
		PESV_API(&mAGC->mPESV,&peakdb,&peaklq,&rmsdb,&rmslq);

		if (mAGC->chanel == 2)
		{
			ChanelConvert(1,2,inLen,inputhped,NULL,inR,inR);
			pinR = inR;
		}
		else
		{
			pinR = inputhped;
		}
		if (mAGC->samplerate != 8000)
		{
			ResampleRun_API(&mAGC->mrs,pinR,inRlen,in8k,&out8klen);
			thepitch = pitch_NCCRun_API(&mAGC->mpitch,in8k,out8klen);
		} 
		else
		{
			thepitch = pitch_NCCRun_API(&mAGC->mpitch,pinR,inRlen);
		}

		if (thepitch>35&&thepitch<75)
		{
			mAGC->memthePCD = 200;
		}
		mAGC->memthePCD -= 20;
		if (mAGC->memthePCD<0)
		{
			mAGC->memthePCD = 0;
		}
#if DEBUGAGC
		for (i=0;i<inLen;i++)
		{
			justdebug[i] = thepitch * 100;
		}
		fwrite_SKR(justdebug, 2, inLen, "thepitch.pcm");
#endif
		//pitchavg = PitchAVGRun_API(&mAGC->mavg,thepitch);
		periodavg = PeriodAVGRun_API(&mAGC->mavg,thepitch,mAGC->mpitch.sideoutZperiod);
#if DEBUGAGC		
		for (i = 0; i < inLen; i++)
		{
			justdebug[i] = periodavg * 100;
		}
		fwrite_SKR(justdebug, 2, inLen, "pavg.pcm");

		for (i = 0; i < inLen; i++)
		{
			if (mAGC->memPeriodCountDown>0)
			{
				justdebug[i] = 10000;
			} 
			else
			{
				justdebug[i] = 0;
			}
			
		}
		fwrite_SKR(justdebug, 2, inLen, "pcountdown.pcm");
#endif	
	{
		mAGC->memPeriodCountDown -=20;
		if (mAGC->memPeriodCountDown<0)
		{
			mAGC->memPeriodCountDown = 0;
		}
	}


	}
	

	/*FB*/
	if (mAGC->FeedbackKind != 0)
	{
		
		if (mAGC->FBSimplex == 1)
		{
			if(gainmod_dB[6]<=0)//gainmod_dB[6] is dnnoise_sub_nsdb
			{
				ctrnoise_db = gainmod_dB[6]+dB(mAGC->thenlp.memSSdown_gain);
			}
			else
			{
				ctrnoise_db = mAGC->memnoiseVAD_db;
				//assert(0);//now noisectr is get from cap because that's most continuous
			}
		} 
		else
		{
			if(gainmod_dB[6]<=0)
			{
				ctrnoise_db = gainmod_dB[6];
			}
			else
			{
				ctrnoise_db = mAGC->memnoiseVAD_db;
				if (mAGC->PostAGC == 0)
				{
					//assert(0);
				}
			}
		}
		if (mAGC->memFirstSet == 0)
		{
			if (mAGC->FeedbackKind == RMIC_XPBOOST_USEVBOOST)
			{
				if ( mAGC->lastmic.capability == mAGC->mymic.capability 
					&& mAGC->lastmic.rstep_dB == mAGC->mymic.rstep_dB
					&& mAGC->lastmic.rboostmin_dB == mAGC->mymic.rboostmin_dB
					&& mAGC->lastmic.rboostmax_dB == mAGC->mymic.rboostmax_dB
					)//because vboost is all the same we don't check it....but if boostfailed????
				{
					mAGC->memsamemicliky = samemiclikely = 1;
				} 
			}

		}
		//if(mAGC->thenlp.SS == 0)
		{
			mAGC->memFirstAdaptDOWN -= 20;//this is prevent wrongly down vol.because getting the right noise need some time
		}
		if (mAGC->memFirstAdaptDOWN <0)
		{
			mAGC->memFirstAdaptDOWN = 0;
		}
		if (mAGC->memFirstAdaptDOWN>0)
		{
			if (ctrnoise_db>VOL_MINNOISE_DB)
			{
				ctrnoise_db = VOL_MINNOISE_DB;//in first 
			}
			
		}
		

		if ((mAGC->mymic.capability & CAPABILITY_RVOL) && (mAGC->memmicvol>0.15) && ctrnoise_db > VOL_MINNOISE_DB - 4)
		{
			SCagressive = 1;
		}

		SCDetect_Simplex(&mAGC->thenlp,mAGC->memmicvol,SCagressive);
		FB_Simplex(&mAGC->thenlp,mAGC->FBSimplex);
		
		oricapavgdb = capavergex_db = gainmod_dB[5];//:more

		if (SCagressive == 1 && mAGC->thenlp.SSA != 0)
		{
			capavergex_db += 0.5;
		}

		//temp:when capavergex_db is big,and we can't get esinfo we simply think es=1 in this condition, but we only set "thenlp.capavgx_db_nlpmod"
		//if (capavergex_db + SS_FB_MOD_DB > mAGC->TargetU_db && capavergex_db > avergex_db + 20 && mAGC->FBSimplex)
		//{
		//	mAGC->thenlp.capavgx_db_nlpmod = SS_FB_MOD_DB;
		//}


        if(mAGC->DyKind & DY_NS)
        {
            ori_avergex_db = avergex_db - dB(DY_NS_IDB);
            ori_noise_db = mAGC->memnoise_db - dB(DY_NS_IDB);
            
        }
        else
        {
            ori_avergex_db = avergex_db;
            ori_noise_db = mAGC->memnoise_db;
        }
        
		inputdbmod = 0;
		if (mAGC->mymic.capability & CAPABILITY_RVOL)
		{
			inputdbmod += RVOLMOD;
		}
		if (mAGC->mymic.capability & CAPABILITY_VVOL)
		{
			inputdbmod += VVOLMOD;
		}

        //if(!(mAGC->mymic.capability & CAPABILITY_RVOL))//that's because rvol has pregain
        {
            ori_avergex_db = THEMINOF(avergex_db + inputdbmod,0);
            
            ori_noise_db = THEMINOF(mAGC->memnoise_db + inputdbmod,0);
        }

        
        /*noise for boost is used in vvol to prevent agc to find the speaker who is too far from mic*/
        if (mAGC->mymic.capability & CAPABILITY_VVOL) {
            if(dB(mAGC->memmicvol) < 9.0)
            {
                if (ctrnoise_db < VOL_MINNOISE_DB)//-40+10
                {
                    noise_notallowupvol = 0;
                }
                else
                {
                    noise_notallowupvol = 1;
                }
            }
            else if(dB(mAGC->memmicvol) >= 9.0 && dB(mAGC->memmicvol) <= 15.0)
            {
                if (ctrnoise_db < VOL_MINNOISE_DB - 2)
                {
                    noise_notallowupvol = 0;
                }
                else
                {
                    noise_notallowupvol = 1;
                }
            }
            else
            {
                if (ctrnoise_db < VOL_MINNOISE_DB - 3)//
                {
                    noise_notallowupvol = 0;
                }
                else
                {
                    noise_notallowupvol = 1;
                }
            }
            ///////////////////////////////////
            if(dB(mAGC->memmicvol) > 6.0 && ctrnoise_db > VOL_MINNOISE_DB + 7)
            {
                noise_downvol = 1;
            }
            else
            {
                noise_downvol = 0;
            }

        }
        else if(mAGC->mymic.capability & CAPABILITY_RVOL)
        {
			if (mAGC->FeedbackKind == RMIC_XPBOOST_USEVBOOST)
			{
				//////////////////////////////mem,rvol-noise////////////////////////////////////////////
				rvol = (int)(mAGC->memmicvol*100);
				if (mAGC->mymic.capability & CAPABILITY_RSUBLEVEL)//w7
				{
					rbooststep = (mAGC->memboost_dB - mAGC->mymic.rboostmin_dB)/mAGC->mymic.rstep_dB;
					if (rbooststep < 0)
					{
						rbooststep = 0;
						assert(0);
					}
					if (rbooststep > 6)
					{
						rbooststep = 6;
						assert(0);
					}
				} 
				else if(mAGC->mymic.capability & CAPABILITY_VBOOST)//xpboost
				{
					rbooststep = mAGC->mymic.vboost_dB/mAGC->mymic.vstep_dB;
				}
				else//noboost
				{
					rbooststep = 0;
				}

				if (rvol >= 80 && rvol <= 100)
				{
					mAGC->mymic.rvolnoise[rbooststep][rvol - 80] = ctrnoise_db;
				}

				for (i=20;i>=1;i--)
				{
					if (mAGC->mymic.rvolnoise[rbooststep][i] > -39.5 && mAGC->mymic.rvolnoise[rbooststep][i] < 0 && mAGC->mymic.rvolnoise[rbooststep][i] - mAGC->mymic.rvolnoise[rbooststep][i-1] > 8.5)
					{
						mAGC->mymic.bigsteprvol[rbooststep] = (i + 80)/100.0;
					}
				}
				//if (mAGC->mymic.bigsteprvol[rbooststep]<0.86)//now,we only concern vol>85
				//{
				//	mAGC->mymic.bigsteprvol[rbooststep] = 1.01;
				//}
			}

            if(mAGC->memmicvol < 0.4)
            {
                if (ctrnoise_db < VOL_MINNOISE_DB)//-40+10
                {
                    noise_notallowupvol = 0;
                }
                else
                {
                    noise_notallowupvol = 1;
                }
            }
            else if(mAGC->memmicvol >= 0.4 && mAGC->memmicvol <= 0.75)
            {
                if (ctrnoise_db < VOL_MINNOISE_DB - 1)
                {
                    noise_notallowupvol = 0;
                }
                else
                {
                    noise_notallowupvol = 1;
                }
            }
            else
            {
                if (ctrnoise_db < VOL_MINNOISE_DB - 2)//
                {
                    noise_notallowupvol = 0;
                }
                else
                {
                    noise_notallowupvol = 1;
                }
            }
            ///////////////////////////////////

			noise_downvol = 0;

			if (mAGC->memmicvol > 0.45)
			{
				if (ctrnoise_db > VOL_MINNOISE_DB + 7)
				{
					noise_downvol = 1;
				}
			} 
			else if(mAGC->memmicvol > 0.3)
			{
				if(ctrnoise_db > VOL_MINNOISE_DB + 9)
				{
					noise_downvol = 1;
				}
			}


            //noise_downvol = noise_notallowupvol = 0;//del this line to effect to rvol
        }
        else
        {
            assert(0);//no vol..
        }

		//RTLOG("ori_avergex_db ============================== %f\n",ori_avergex_db);
///////////////////point out fbgain///////////////////////////////////////////////////////

		if (mAGC->mymic.capability & CAPABILITY_RVOL)
		{
			someinthreshold_db = -40.0;
			if (mAGC->memnoise_db < -55 || ctrnoise_db < VOL_MINNOISE_DB - 10 || mAGC->memmicvol<0.05)
			{
				//this is to say if noise is so small we don't use the "someinthreshold_db".
				//very very low noise may be caused by very low vol in some "very low" mic
				//we don't worry even it is really small bubble, because the noise now is so low....so we up vol will not cause problem
				someinthreshold_db = -95.0;

			}
		} 
		else
		{
			someinthreshold_db = -38.0;//vvol's first set maybe larger than rvol
			if (/*mAGC->memnoise_db < VVOL_BUBBLE_THRESHOLD || */ctrnoise_db < VOL_MINNOISE_DB - VVOL_BUBBLE_MOD||mAGC->memworkingwatch_ms<BEGINING_TIME/*at first we don't use the "someinthreshold_db" */)
			{
				//this is to say if noise is so small we don't use the "someinthreshold_db"
				//very very low noise may be caused by very low vol in some "very low" mic
				//we don't worry even it is really small bubble, because the noise now is so low....so we up vol will not cause problem
				someinthreshold_db = STHOLDDB;

			}
		}

		if (
			   (//ori_avergex_db condition
				  (
					  (ori_avergex_db <= ori_noise_db + T1_DB)//
					  && 
					  (ori_avergex_db < mAGC->TargetU_db)
				  ) 
				  || 
				  (ori_avergex_db<someinthreshold_db)//we have do firstset,so if also ori_avergex_db<-36.0 it must be bubble in very low noise
			   )
			   &&
               ( //extern condition
				  (noise_downvol !=1 ) //noise_downvol == 1 says we must do fb
			      &&
				  (capavergex_db + mAGC->thenlp.capavgx_db_nlpmod < mAGC->TargetU_db)//">" says we must do fb
			   )
			)
		{
			*gainmod_dB = 0;
			//mAGC->memUPintevalwatch = 0;
			//mAGC->memUPintevalwatch -= 5;
			//mAGC->memUPintevalwatch = THEMAXOF(0,mAGC->memUPintevalwatch);
			//mAGC->memDownintevalwatch = 0;
			somein = 0;
		} 
		else
		{
			somein = 1;
			if (mAGC->memworkingwatch_ms < BEGINING_TIME && mAGC->thenlp.SS == 0)
			{
				mAGC->memworkingwatch_ms += 20;
			}
			if (peakdb<0)
			{
				if (maxx_db<peakdb - 18)
				{
					if (mAGC->mymic.capability & CAPABILITY_RVOL)
					{
						if (mAGC->memmicvol>0.4)
						{
							toosmall = 1;
						}
					}
					else if(mAGC->mymic.capability & CAPABILITY_VVOL)
					{
						if (dB(mAGC->memmicvol) > 7.0)
						{
							toosmall = 1;
						}
					}
					
				}
			}

			periodthreshold = 1.5;
			if (mAGC->mymic.capability & CAPABILITY_RVOL)
			{
				if (mAGC->memmicvol < 0.4)
				{
					periodthreshold = 1.4;
				}
			}
			else if (mAGC->mymic.capability & CAPABILITY_VVOL)
			{
				if (dB(mAGC->memmicvol) < 7.0)
				{
					periodthreshold = 1.35;
				}
			}

			if (periodavg>periodthreshold && !toosmall)
			{
				mAGC->memPeriodCountDown = 900;
			}

			if (mAGC->mymic.capability & CAPABILITY_RVOL)
			{
				if (mAGC->memmicvol < 0.1)
				{
					mAGC->memPeriodCountDown = 1900;//that is to say if <0.1 we don't use memPeriodCountDown
				}
			}
			else if (mAGC->mymic.capability & CAPABILITY_VVOL)
			{
				if (dB(mAGC->memmicvol) < 4.0)
				{
					mAGC->memPeriodCountDown = 1900;
				}
			}

			if (mAGC->memthePCD>80 && peakdb<-18)
			{
				mAGC->memPeriodCountDown = 1000;
			}

			//return;

			if (mAGC->mymic.capability & CAPABILITY_RVOL)// for there is boost 
			{
				Max_dbCalcu(&mAGC->memmaxsomeindb,&mAGC->memUpsomeinwatch,mAGC->memsominavgdb,ori_avergex_db,MAXSOMEIN_UPDATE_WINDOW);
			} 
			else
			{
				if (mAGC->thenlp.SS == 0)//when ss == 1 we don't update memmaxsomeindb
				{				
					//if (mAGC->memworkingwatch_ms >= BEGINING_TIME)
					//{
					//	if (ori_avergex_db > -24)
					//	{
					//		Max_dbCalcu(&mAGC->memmaxsomeindb,&mAGC->memUpsomeinwatch,mAGC->memsominavgdb,ori_avergex_db,MAXSOMEIN_UPDATE_WINDOW);//3MAXSOMEIN_UPDATE_WINDOW is wrong
					//	}
					//} 
					//else
					{
						Max_dbCalcu(&mAGC->memmaxsomeindb,&mAGC->memUpsomeinwatch,mAGC->memsominavgdb,ori_avergex_db,MAXSOMEIN_UPDATE_WINDOW);
					}
				}
			}

						
            if(!(mAGC->DyKind & DY_VARIBLECUR))
            {
                tarDdy0mod = 4.0;
                tarUdy0mod = 1.5;
                t2dbdy0mod = 9.0;
            }
            else
            {
                tarDdy0mod = 3.0;
                tarUdy0mod = 0.0;
                t2dbdy0mod = 6.0;
            }
            
            
            if (
                ((((ori_avergex_db <= (ori_noise_db + T2_DB + t2dbdy0mod) && ori_avergex_db < mAGC->TargetD_db + tarDdy0mod + mAGC->thenlp.TargetD_db_nlpmod) || ori_avergex_db < SMALLDB + mAGC->thenlp.Smalldb_nlpmod)&&(capavergex_db + mAGC->thenlp.capavgx_db_nlpmod)<(mAGC->TargetU_db)) && mAGC->thenlp.ESA == 0)
                &&
                (noise_notallowupvol == 0)

                )//we redefine T2_DB...now it is T1 + old T2
			{
				if(mAGC->thenlp.SS == 0&&mAGC->memPeriodCountDown>0)
                {
                    mAGC->memUPintevalwatch += 20;
                    if (mAGC->memmaxsomeindb<-9.0 /*&& mAGC->memthePCD > 0*/)
                    {
                        mAGC->memUPintevalwatch += 40;
                    }
                    if (mAGC->memmaxsomeindb<-12.0 /*&& mAGC->memthePCD > 0*/)
                    {
                        mAGC->memUPintevalwatch += 20;
                    }
                    if (mAGC->memmaxsomeindb<-18.0)
                    {
                        mAGC->memUPintevalwatch += 20;
                    }

			  if (thepitch>35&&thepitch<75)
			  {
				  mAGC->memUPintevalwatch += 80;
			  }
			  /*
			  else
			  {
				  mAGC->memUPintevalwatch -= 10;
				  if (mAGC->memUPintevalwatch < 0)
				  {
					  mAGC->memUPintevalwatch = 0;
				  }
			  }
				*/

                }

				if (mAGC->memUPintevalwatch > mAGC->UPIntevalTime_ms + mAGC->thenlp.UPIntevalTime_ms_nlpmod)
				{
					mAGC->memUPintevalwatch = 0;
					if(mAGC->memmaxsomeindb<-2.0)




					//if((mAGC->memnoiseVAD_db<VOL_MINNOISE_DB))//V1:像下调一样，不去限制；因为前一级会有制约
					//V0:if (mAGC->memgainmodifed_dB + mAGC->UpSpeed_dB < mAGC->GainModMax_dB)//如果调整后能保证不过调那就可以调
					{

						if (mAGC->mymic.capability & CAPABILITY_VVOL)
						{
							if (mAGC->memFirstAdaptUP > 4)
							{
								mAGC->memFirstAdaptUP = 5;//prevent overflow
								if (ctrnoise_db<-80.0)//The noise is so small,that we can't upvol by larger step
								{
									*gainmod_dB = mAGC->UpSpeed_dB*2.8;//we don't set it too larger for safe..."cut problem" may cause memnoiseVAD_db wrong
								}
								else if (ctrnoise_db<-70.0)
								{
									*gainmod_dB = mAGC->UpSpeed_dB*2.3;
								}
								else if(ctrnoise_db<-60.0)
								{
									*gainmod_dB = mAGC->UpSpeed_dB*1.6;
								}
								else
								{
									*gainmod_dB = mAGC->UpSpeed_dB*1.1;
								}
								if (peakdb<-15)
								{
									*gainmod_dB = *gainmod_dB * 1.4;
								}
								else if (peakdb<-12)
								{
									*gainmod_dB = *gainmod_dB * 1.2;
								}
								if (mAGC->memthePCD>0)
								{
									if (mAGC->memmaxsomeindb < -23)
									{
										*gainmod_dB = *gainmod_dB * 5;
									}
									else if (mAGC->memmaxsomeindb < -18)
									{
										*gainmod_dB = *gainmod_dB * 4;
									}
									else if (mAGC->memmaxsomeindb < -12)
									{
										*gainmod_dB = *gainmod_dB * 3;
									}
								}

							} 
							else
							{
								mAGC->memFirstAdaptUP ++;

								if (ctrnoise_db<-80.0)//The noise is so small,that we can't upvol by larger step
								{
									*gainmod_dB = mAGC->UpSpeed_dB*3.0;//we don't set it too larger for safe..."cut problem" may cause memnoiseVAD_db wrong
								}
								else if (ctrnoise_db<-70.0)
								{
									*gainmod_dB = mAGC->UpSpeed_dB*2.3;
								}
								else if(ctrnoise_db<-60.0)
								{
									*gainmod_dB = mAGC->UpSpeed_dB*1.8;
								}
								else
								{
									*gainmod_dB = mAGC->UpSpeed_dB*1.6;
								}

								if (peakdb<-15)
								{
									*gainmod_dB = *gainmod_dB * 1.2;
								}
								else if (peakdb<-12)
								{
									*gainmod_dB = *gainmod_dB * 1.1;
								}
								if (mAGC->memthePCD>0)
								{
									if (mAGC->memmaxsomeindb<-23)
									{
										*gainmod_dB = *gainmod_dB * 4;
									}
									else if(mAGC->memmaxsomeindb<-18)
									{
										*gainmod_dB = *gainmod_dB * 3;
									}
									else if(mAGC->memmaxsomeindb<-12)
									{
										*gainmod_dB = *gainmod_dB * 2;
									}									
								}

								if(peakdb >-7 && peakdb < 0)
								{
									*gainmod_dB = mAGC->UpSpeed_dB * 0.5;
								}
								if(peakdb >-4 && peakdb < 0)
								{
									*gainmod_dB = mAGC->UpSpeed_dB * 0.3;
								}
								
							}
						} 
						else //rvol..
						{
							if ((mAGC->mymic.capability & CAPABILITY_VBOOST) == 0 && (mAGC->mymic.capability & CAPABILITY_RBOOST) == 0)//when there is no boost,rvol is usually very sensitive
							{
								*gainmod_dB = mAGC->UpSpeed_dB;
								if (mAGC->memmicvol>0.5)
								{
									*gainmod_dB = mAGC->UpSpeed_dB*0.7;
								}
							} 
							else
							{
								if (mAGC->memFirstAdaptUP > 4)
								{
									mAGC->memFirstAdaptUP = 5;//prevent overflow
									if (ctrnoise_db<-80.0)//The noise is so small,that we can't upvol by larger step
									{
										*gainmod_dB = mAGC->UpSpeed_dB*1.21;//we don't set it too larger for safe..."cut problem" may cause memnoiseVAD_db wrong
									}
									else if (ctrnoise_db<-70.0)
									{
										*gainmod_dB = mAGC->UpSpeed_dB*1.12;
									}
									else if(ctrnoise_db<-60.0)
									{
										*gainmod_dB = mAGC->UpSpeed_dB*1.05;
									}
									else
									{
										*gainmod_dB = mAGC->UpSpeed_dB;
									}
									if (mAGC->memmicvol>0.5 && peakdb > -7 && peakdb < 0)
									{
										*gainmod_dB = mAGC->UpSpeed_dB*0.7;
									}
									if (mAGC->memthePCD>0)
									{
										if (peakdb < -15)
										{
											*gainmod_dB = *gainmod_dB * 1.5;
										}
										else if (peakdb < -12)
										{
											*gainmod_dB = *gainmod_dB * 1.1;
										}
									}
								} 
								else
								{
									mAGC->memFirstAdaptUP ++;

									if (ctrnoise_db<-80.0)//The noise is so small,that we can't upvol by larger step
									{
										*gainmod_dB = mAGC->UpSpeed_dB*1.28;//we don't set it too larger for safe..."cut problem" may cause memnoiseVAD_db wrong
									}
									else if (ctrnoise_db<-70.0)
									{
										*gainmod_dB = mAGC->UpSpeed_dB*1.17;
									}
									else if(ctrnoise_db<-60.0)
									{
										*gainmod_dB = mAGC->UpSpeed_dB*1.12;
									}
									else
									{
										*gainmod_dB = mAGC->UpSpeed_dB;
									}
									if (mAGC->memmicvol>0.5 && peakdb > -7 && peakdb < 0)
									{
										*gainmod_dB = mAGC->UpSpeed_dB*0.7;
									}
									if (mAGC->memthePCD>0)
									{
										if (peakdb < -15)
										{
											*gainmod_dB = *gainmod_dB * 1.5;
										}
										else if (peakdb < -12)
										{
											*gainmod_dB = *gainmod_dB * 1.1;
										}
									}
								}
							}
							
						}

										
					} 
					else
					{
						*gainmod_dB = 0;
					}
				} 
				else
				{
					*gainmod_dB = 0;
				}
				
			} 
			else
			{	
				//mAGC->memUPintevalwatch = 0;
				mAGC->memUPintevalwatch -= 20;
				//mAGC->memUPintevalwatch -= 80;
				mAGC->memUPintevalwatch = THEMAXOF(0,mAGC->memUPintevalwatch);

				if (
                    
                    (ori_avergex_db > mAGC->TargetU_db + tarUdy0mod||(capavergex_db + mAGC->thenlp.capavgx_db_nlpmod)>mAGC->TargetU_db + tarUdy0mod)
                    ||
                    (noise_downvol == 1)
                    
                    )
				{
					mAGC->memDownintevalwatch += 20;

					if (mAGC->memDownintevalwatch > mAGC->DownIntevalTime_ms + mAGC->thenlp.DownIntevalTime_ms_nlpmod)
					{
						mAGC->memDownintevalwatch = 0;
						
						if (mAGC->mymic.capability & CAPABILITY_VVOL)
						{
							if (ctrnoise_db>-35.0)//The noise is so big,that we may need down vol by larger step
							{
								//*gainmod_dB = -mAGC->DownSpeed_dB*1.6;
								*gainmod_dB = -mAGC->DownSpeed_dB;
							} 
							else if(ctrnoise_db>-40.0)
							{
								if (ori_avergex_db > -0.4 || capavergex_db > -0.4)
								{
									*gainmod_dB = -mAGC->DownSpeed_dB*0.8;
								} 
								else
								{
									*gainmod_dB = -mAGC->DownSpeed_dB*0.7;
								}
							}
							else
							{
								if (ori_avergex_db > -0.4 || capavergex_db > -0.4)
								{
									*gainmod_dB = -mAGC->DownSpeed_dB*0.7;
								} 
								else
								{
									*gainmod_dB = -mAGC->DownSpeed_dB*0.6;
								}
								
							}

							if(peakdb<-7)
							{
								*gainmod_dB = -mAGC->DownSpeed_dB*0.3;
							}

							if(capavergex_db > 5.5)
							{
								*gainmod_dB = THEMINOF(-1.7f,*gainmod_dB);
							}
							if (capavergex_db > 7)
							{
								*gainmod_dB = THEMINOF(-3.2f, *gainmod_dB);
							}

							if (mAGC->memworkingwatch_ms<BEGINING_TIME*0.2 )//first 5s 
							{
								if (ori_avergex_db > -0.5 || capavergex_db > -0.5)
								{
									*gainmod_dB = -mAGC->DownSpeed_dB*3.1;
								} 
								else
								{
									*gainmod_dB = -mAGC->DownSpeed_dB*1.8;
								}
								if (capavergex_db > 8)
								{
									*gainmod_dB = -4;
								}

							}
							else if (mAGC->memworkingwatch_ms<BEGINING_TIME*0.3)//first 5s 
							{
								if (ori_avergex_db > -0.4 || capavergex_db > -0.4)
								{
									*gainmod_dB = -mAGC->DownSpeed_dB*2.5;
								} 
								else
								{
									*gainmod_dB = -mAGC->DownSpeed_dB*1.5;
								}
								if (capavergex_db > 7)
								{
									*gainmod_dB = -3.f;
								}
							}
							else if (mAGC->memworkingwatch_ms<BEGINING_TIME)
							{
								*gainmod_dB = -mAGC->DownSpeed_dB*1.3;
								if (capavergex_db > 6)
								{
									*gainmod_dB = -2.f;
								}
							}

							if (mAGC->memvvolchangetime<4)
							{
								mAGC->memvvolchangetime++;
								if (capavergex_db>-3)
								{
									*gainmod_dB = -capavergex_db-4;
								}
							}

						} 
						else
						{
							if (ctrnoise_db>-35.0||(((capavergex_db + mAGC->thenlp.capavgx_db_nlpmod)> -2.8)&&(mAGC->thenlp.SSA != 0)))//The noise is so big,that we may need down vol by larger step
							{
								*gainmod_dB = -mAGC->DownSpeed_dB*1.25;
							} 
							else if(ctrnoise_db>-40.0)
							{
								*gainmod_dB = -mAGC->DownSpeed_dB*1.1;
							}
							else
							{
								*gainmod_dB = -mAGC->DownSpeed_dB;
							}
							if(peakdb<-6)
							{
								*gainmod_dB = -mAGC->DownSpeed_dB*0.6;
							}
						}


					} 
					else
					{
						*gainmod_dB = 0;
					}
				}
				else
				{
					//mAGC->memDownintevalwatch = 0;
					mAGC->memDownintevalwatch -= 10;
					mAGC->memDownintevalwatch = THEMAXOF(0,mAGC->memDownintevalwatch);

					*gainmod_dB = 0;
				}
					
			}
		}
	
#ifdef SEELEVEL
		free(xf32_db);
		fclose(fwx_db);	
#endif
		
		//mAGC->memgainmodifed_dB += *gainmod_dB; //启用，for virtualmic
		Gain = idB(*gainmod_dB);//之后考虑优化


///////////////////point out boost and memboost_dBlimit(rboost's Allzero)///////////////////////////////////////////////////////
		
		if ((mAGC->mymic.capability & CAPABILITY_VBOOST) ||(mAGC->mymic.capability & CAPABILITY_RBOOST))
		{
			if ((mAGC->mymic.capability & CAPABILITY_RSUBLEVEL) || (mAGC->mymic.capability & CAPABILITY_VBOOST))
			{
				/*boostup初始值（xp不动）*/
				if (mAGC->memmaxsomeindb+24.0 < mAGC->TargetD_db)
				{
					boostupinit = 2;
				} 
				else if(mAGC->memmaxsomeindb+16.0 < mAGC->TargetD_db)
				{
					boostupinit = 1;
				}
				else 
				{
					boostupinit = 0;
				}
			}

			if (mAGC->memlastboost_dB != mAGC->memboost_dB)
			{
				if (mAGC->mymic.capability & CAPABILITY_RSUBLEVEL)
				{
					BoostChangeFeedbackParameter(mAGC,mAGC->memboost_dB/(mAGC->mymic.rboostmax_dB - mAGC->mymic.rboostmin_dB)); 
				}

				if (mAGC->mymic.capability & CAPABILITY_VBOOST)
				{
					BoostChangeFeedbackParameter(mAGC,mAGC->mymic.vboost_dB/(mAGC->mymic.vboostmax_dB - mAGC->mymic.vboostmin_dB)); 
				}
				mAGC->memlastboost_dB = mAGC->memboost_dB;
			}

			if (ori_avergex_db<-80.5)
			{
				if(mAGC->memboostupdevicenormal<30)//memboostupdevicenormal once >0.6s we believe this device can be work normally at this boost position,and we don't check allzero problem before next boostup
				{
					if (mAGC->mymic.capability & CAPABILITY_RSUBLEVEL)//w7  
					{
						if(mAGC->memboost_dB/(mAGC->mymic.rboostmax_dB - mAGC->mymic.rboostmin_dB)>0.8)//0.3->0.8
						{
							mAGC->memLongAllZero ++;
						}
					}
					else if(mAGC->mymic.capability & CAPABILITY_RBOOST)//xp 
					{
						if(mAGC->memboost_dB > 0.5)
						{
							mAGC->memLongAllZero ++;
						}
					}

					if (mAGC->memLongAllZero>20)
					{
						mAGC->memLongAllZero = 0;
						boostmove = -1;//boost may cause "allzero" inputvboosted
						if (mAGC->mymic.capability & CAPABILITY_RSUBLEVEL)//w7  
						{
							mAGC->memboost_dBlimit = mAGC->memboost_dB - mAGC->mymic.rstep_dB;
						}
						else if(mAGC->mymic.capability & CAPABILITY_RBOOST)//xp 
						{
							mAGC->memboost_dBlimit = 0.0;
						}
					}
				}
			}
			else
			{
				mAGC->memLongAllZero = 0;
				if (mAGC->memboostupdevicenormal<35)
				{
					mAGC->memboostupdevicenormal++;
				}
			}

			//bool MicBoostDown(unsigned int nCapVol,float feedback)
			mAGC->memDown++;
			if (mAGC->memDown>0)
			{
				if (mAGC->memDown>mAGC->N20ms_ForDown)
				{
					mAGC->memDown = 0;
					mAGC->memLowVol = 0;
					mAGC->memNegativeFB = 0;
				}

				/*削顶检测,可以考虑DDresample优化*/
				if (somein == 1&&mAGC->memmicvol<0.55)//if (mAGC->memSpeech == 1)不是要语音才判断，而是认为有输入就判断，0.35>SmallVol_Threshold=0.3是要提前判断是否有过顶，而不是等到vol压的很低时再决策boost，之所以不是在全vol下检测过顶是因为我们允许当前boost有个别过顶
				{
					clipping = ClippingDetec(pinput, inLen,mAGC->chanel, mAGC->samplerate);
					//clipping = ClippingDetec(pinput, inLen/2,mAGC->chanel, mAGC->samplerate)*0.8 + ClippingDetec((inputvboosted+inLen/2), inLen/2,mAGC->chanel, mAGC->samplerate)*0.8;////碰麦克导致的过顶。。一般碰麦导致的较短，语音稍长。so拆成两个子帧增大语音过顶的权重
				}

				if (mAGC->memmicvol<mAGC->SmallVol_Threshold)
				{
					mAGC->memLowVol++;
				}


				//if ( clipping == 1)
				if (*gainmod_dB <0/*|| clipping == 1*/)//是不是应该只有小于时再统计
				{
					mAGC->memNegativeFB++;
				}
				mAGC->memNegativeFB += clipping;//增大了过顶信息的判决权重
			}

			if (mAGC->memLowVol>mAGC->N20msSmallVol_Threshold && mAGC->memNegativeFB>mAGC->N20msNegMod_Threshold )//if (mAGC->memLowVol>mAGC->N20msSmallVol_Threshold && mAGC->memNegativeFB>mAGC->N20msNegMod_Threshold || clipping == 1)
			{
				boostdown = -1;
				mAGC->memDown = -100;//一旦降一个step就过掉一百帧，可能进来的信号还是未降时采进来的
				mAGC->memLowVol = 0;
				mAGC->memNegativeFB = 0;
			}
			else
			{
				boostdown = 0;
			}

			mAGC->memUP++;
			if (mAGC->memUP>0)
			{
				mAGC->memminnoise_db = THEMINOF(mAGC->memminnoise_db,ori_noise_db);//for if boost up
				mAGC->memmaxavg_db = THEMAXOF(mAGC->memmaxavg_db,ori_avergex_db);

				if (mAGC->memUP>mAGC->N20ms_ForUP)
				{
					mAGC->memUP = 0;
					mAGC->memSaturated = boostupinit;
					mAGC->memminnoise_db = 0.0; 
					//if ( mAGC->memmaxavg_db+20.0<mAGC->TargetD_db)
					//{
					//	mAGC->memSaturated++;
					//}
					mAGC->memmaxavg_db = -95.0;
				}

				if (*gainmod_dB> 0 && mAGC->memmicvol>0.95)//micvol到顶。boost段代码放在mic更新前
				{
					mAGC->memSaturated++;
					if ( mAGC->memmaxavg_db+20.0<mAGC->TargetD_db)
					{
						mAGC->memSaturated++;
					}
					//mAGC->memmaxavg_db = -95.0;
				}
			}

			if (mAGC->memSaturated>mAGC->N20msSatu_Threshold && mAGC->memminnoise_db<BOOST_MINNOISE_DB)
			{
				boostup = 1;
				mAGC->memUP = -100;
				mAGC->memSaturated = boostupinit;
				mAGC->memminnoise_db = 0.0; 
				mAGC->memmaxavg_db = -95.0;
			}
			else
			{
				boostup = 0;
			}

			boostmove = boostup + boostdown;
			if (mAGC->memFirstSet == 0)
			{
				if (samemiclikely == 1)
				{
					if (mAGC->lastmic.rboost_dB > mAGC->memboost_dB || mAGC->lastmic.vboost_dB > mAGC->mymic.vboost_dB)
					{
						boostmove = 1;
					}
				}
			}
			///////////////////calcu "boost" solution///////////////////////////////////////////////////////
			theboostdB = boostdB(&mAGC->mymic,boostmove,mAGC->memboost_dB,&rboostmove,&vboostmove,mAGC->memboost_dBlimit);
			mAGC->memboost_dB += theboostdB;
		} 
		else
		{
			rboostmove = vboostmove = boostmove = 0;
			mAGC->memboost_dB = 0.0;
		}
		mAGC->mymic.rboost_dB = mAGC->memboost_dB;
	
///////////////////calcu "vol" solution///////////////////////////////////////////////////////

		newvol = mAGC->memmicvol * Gain * mAGC->gainmicmod(&mAGC->mymic,mAGC->memboost_dB,mAGC->memmicvol,Gain);//boost[2] = (unsigned int)(boost[2] * Gain + 0.5f );//if vol is 0,feedback_vol is not useful,maybe 0 is just the user's need

		//////////////////////////////////////////////////////////////////////////
		////some micvol is not linear:for example Gain(vol90-vol80 ) << Gain(vol100-vol90);vol100_dB - vol50_dB is not 6dB but more than 20dB even 30dB.... 
		////so if the noise is big enough we must step by vol_x instead SpeeddB..and the x is 2now (one mic vol100 is 12dB bigger than vol90..and (vol70 - vol60)dB is less than 2dB)
		////if the noisedb can't update timely...we use "else" to decrease 
		///maybe we can also limit the step when memvol is low than 75
		if (mAGC->mymic.capability & CAPABILITY_RVOL)
		{
			if (ctrnoise_db > -58)
			{
				if (newvol>mAGC->memmicvol)
				{
					newvol = THEMINOF(newvol,mAGC->memmicvol + 0.06);
				}
				if (newvol<mAGC->memmicvol)
				{
					newvol = THEMAXOF(newvol,mAGC->memmicvol - 0.06);
				}
			} 
			else
			{
				if (newvol>mAGC->memmicvol)
				{
					newvol = THEMINOF(newvol,mAGC->memmicvol + 0.12);
				}
				if (newvol<mAGC->memmicvol)
				{
					newvol = THEMAXOF(newvol,mAGC->memmicvol - 0.12);
				}
			}
			
			if (mAGC->memmicvol>0.92)
			{
				if (ctrnoise_db > -60)
				{
					if (newvol>mAGC->memmicvol)
					{
						newvol = THEMINOF(newvol,mAGC->memmicvol + 0.01);
					}
					if (newvol<mAGC->memmicvol)
					{
						newvol = THEMAXOF(newvol,mAGC->memmicvol - 0.04);
					}
				} 
				else
				{
					if (newvol>mAGC->memmicvol)
					{
						newvol = THEMINOF(newvol,mAGC->memmicvol + 0.03);
					}
					if (newvol<mAGC->memmicvol)
					{
						newvol = THEMAXOF(newvol,mAGC->memmicvol - 0.04);
					}
				}

			}
			else if (mAGC->memmicvol>0.87)
			{
				if (ctrnoise_db > -60)
				{
					if (ctrnoise_db > -50)
					{
						if (newvol>mAGC->memmicvol)
						{
							newvol = THEMINOF(newvol,mAGC->memmicvol + 0.01);
						}
						if (newvol<mAGC->memmicvol)
						{
							newvol = THEMAXOF(newvol,mAGC->memmicvol - 0.04);
						}
					} 
					else if (ctrnoise_db > -55)
					{
						if (newvol>mAGC->memmicvol)
						{
							newvol = THEMINOF(newvol,mAGC->memmicvol + 0.02);
						}
						if (newvol<mAGC->memmicvol)
						{
							newvol = THEMAXOF(newvol,mAGC->memmicvol - 0.04);
						}
					}
					else
					{
						if (newvol>mAGC->memmicvol)
						{
							newvol = THEMINOF(newvol,mAGC->memmicvol + 0.025);
						}
						if (newvol<mAGC->memmicvol)
						{
							newvol = THEMAXOF(newvol,mAGC->memmicvol - 0.04);
						}
					}

				} 
				else
				{
					if (newvol>mAGC->memmicvol)
					{
						newvol = THEMINOF(THEMINOF(newvol,mAGC->memmicvol + 0.04),0.95);
					}
					if (newvol<mAGC->memmicvol)
					{
						newvol = THEMAXOF(newvol,mAGC->memmicvol - 0.05);
					}
				}
				if (newvol >= mAGC->mymic.bigsteprvol[rbooststep] - 0.2 && mAGC->memmicvol < mAGC->mymic.bigsteprvol[rbooststep] - 0.2)
				{
					newvol = mAGC->memmicvol;
				}
				else if(mAGC->memsamemicliky == 1)
				{
					if (newvol >= mAGC->lastmic.bigsteprvol[rbooststep] - 0.2 && mAGC->memmicvol < mAGC->lastmic.bigsteprvol[rbooststep] - 0.2)
					{
						if (mAGC->memmicvol>=0.85&&ctrnoise_db>-58)
						{
							newvol = mAGC->memmicvol;//......
						}
					}
					
				}

			}
			else if (mAGC->memmicvol>0.82)
			{
				if (ctrnoise_db > -56)
				{
					if (newvol>mAGC->memmicvol)
					{
						newvol = THEMINOF(newvol,mAGC->memmicvol + 0.025);
					}
					if (newvol<mAGC->memmicvol)
					{
						newvol = THEMAXOF(newvol,mAGC->memmicvol - 0.04);
					}
				} 
				else
				{
					if (newvol>mAGC->memmicvol)
					{
						newvol = THEMINOF(THEMINOF(newvol,mAGC->memmicvol + 0.05),0.95);
					}
					if (newvol<mAGC->memmicvol)
					{
						newvol = THEMAXOF(newvol,mAGC->memmicvol - 0.05);
					}
				}

			}
			else if ((mAGC->memmicvol>0.75 || newvol > 0.75))
			{
				if (ctrnoise_db > -54)
				{
					if (newvol>mAGC->memmicvol)
					{
						newvol = THEMINOF(newvol,mAGC->memmicvol + 0.035);
					}
					if (newvol<mAGC->memmicvol)
					{
						newvol = THEMAXOF(newvol,mAGC->memmicvol - 0.05);
					}
				} 
				else
				{
					if (newvol>mAGC->memmicvol)
					{
						newvol = THEMINOF(THEMINOF(newvol,mAGC->memmicvol + 0.10),0.85);
					}
					if (newvol<mAGC->memmicvol)
					{
						newvol = THEMAXOF(newvol,mAGC->memmicvol - 0.05);
					}
				}
			}
		}
		
		if (mAGC->memworkingwatch_ms >= BEGINING_TIME)//
		{
			if (mAGC->mymic.capability & CAPABILITY_RVOL)
			{
				volupholdtime = RVOL_UP_HOLD_STEADY_TIME;//4000
				voldownholdtime = RVOL_DOWN_HOLD_STEADY_TIME;//200
			} 
			else
			{
				//infact dagc work
				volupholdtime = VVOL_UP_HOLD_STEADY_TIME;//old inteval is 4s 2s but is another way "UP_UPIntevalTime_ms""UP_DownIntevalTime_ms"..
				voldownholdtime = VVOL_DOWN_HOLD_STEADY_TIME;
			}

			if (capavergex_db > 10.0)
			{
				voldownholdtime = 360;
			}
			else if (capavergex_db > 7.0)
			{
				voldownholdtime = 560;
			}
		} 
		else
		{
			if (mAGC->mymic.capability & CAPABILITY_RVOL)
			{
				volupholdtime = RVOL_UP_HOLD_BEGINING_TIME;//it seems also very long because we worry about catching noise in some device ...
				voldownholdtime = RVOL_DOWN_HOLD_BEGINING_TIME;
			} 
			else
			{
				volupholdtime = VVOL_UP_HOLD_BEGINING_TIME;
				voldownholdtime = VVOL_DOWN_HOLD_BEGINING_TIME;
			}

			if (capavergex_db > 10.0)
			{
				voldownholdtime = 220;
			}
			else if (capavergex_db > 7.0)
			{
				voldownholdtime = 340;
			}
		}



		mAGC->memvolholdwatch -= 20;
		if (mAGC->memvolholdwatch < 0)
		{
			mAGC->memvolholdwatch = 0;
		}
		if (newvol > mAGC->memmicvol)
		{
			if (mAGC->memthePCD > 100)
			{
				mAGC->memvolholdwatch -= 20;
				if (mAGC->memvolholdwatch < 0)
				{
					mAGC->memvolholdwatch = 0;
				}
			}

			if (mAGC->memvolholdwatch == 0)//upvol action need 4000ms after last volchange
			{
				if(mAGC->mymic.capability & CAPABILITY_RVOL)
				{
					mAGC->memmicvol = newvol;
					if((int)(newvol*100) > (int)(mAGC->memmicvol*100))
					{
						mAGC->memvolholdwatch = volupholdtime;//4000;
					}
				}
				else
				{
					mAGC->memmicvol = newvol;
					mAGC->memvolholdwatch = volupholdtime;//4000;
				}

			}

		}
		else if(newvol < mAGC->memmicvol)
		{
			if (mAGC->memvolholdwatch < volupholdtime-voldownholdtime || mAGC->thenlp.SSA != 0)//downvol action need 200ms after last volchange
			{
				if(mAGC->mymic.capability & CAPABILITY_RVOL)
				{
					mAGC->memmicvol = newvol;
					if((int)(newvol*100) < (int)(mAGC->memmicvol*100))
					{
						mAGC->memvolholdwatch = volupholdtime;//4000;
					}
				}
				else
				{
					mAGC->memmicvol = newvol;
					mAGC->memvolholdwatch = volupholdtime;//4000;
				}
			}

		}
			
		if (rboostmove!=0 || vboostmove!=0)
		{
			mAGC->memmicvol *= volSmoothgain(theboostdB);
		}

		//firset need not smooth,so it is here
		//mAGC->memmicvol = boost[2]/65535.0;//此处获得实时同步，否则则自身经计算同步
		if (mAGC->memFirstSet == 0)
		{
			if (mAGC->mymic.capability & CAPABILITY_RVOL)
			{
				//if (gainmod_dB[4]>0)//w7
				//{
				//	if (mAGC->memboost_dB/(gainmod_dB[3]-gainmod_dB[2])<0.3)
				//	{
				//		//*boost = 1;
				//	}
				//}
				//else if (gainmod_dB[1]>-0.5)//xp
				//{
				//	if (mAGC->memboost_dB<0.5)
				//	{
				//		mAGC->memdigitalboostgain = DIGITALGAIN_BOOST_XP_FIRST;//6dB
				//	}
				//}
				if (samemiclikely == 1)
				{
					if (mAGC->lastmic.rboost_dB > mAGC->mymic.rboost_dB || mAGC->lastmic.vboost_dB > mAGC->mymic.vboost_dB)
					{
						mAGC->memmicvol = 1.0;
						mAGC->memworkingwatch_ms += (int)(BEGINING_TIME*0.2);//if we re
					}
					else if (mAGC->lastmic.rvol>0 && mAGC->lastmic.rvol<=65535)
					{
						mAGC->memmicvol = THEMAXOF(mAGC->lastmic.rvol/65535.0,0.40);//vol will 10 less than lastvol
						mAGC->memworkingwatch_ms += (int)(BEGINING_TIME*0.1);//if we re
					}
					else
					{
						mAGC->memmicvol = 0.5;
					//	assert(0);
					}
				}
				else
				{
					//if ()
					{
						mAGC->memmicvol = 0.5;
					}			
				}
			}
			else if (mAGC->mymic.capability & CAPABILITY_VVOL)//之后micvol会无区分的归一化
			{
				if (mAGC->memsamemicliky == 1 && mAGC->lastvvol>0 )
				{
					if ((mAGC->lastvvol/65535.0)>=mAGC->mymic.vvolmin && (mAGC->lastvvol/65535.0)<=mAGC->mymic.vvolmax)
					{
						//mAGC->memmicvol = THEMAXOF(mAGC->lastvvol/65535.0, idB(mAGC->vvolfstdB/3));//just like fstrvol >40
						mAGC->memmicvol = mAGC->lastvvol / 65535.0;//just history
						mAGC->memmicvol = THEMINOF(mAGC->memmicvol, idB(19));//if devchanged history vvol may too big...(just like fstvvol>fst,fstvol must <19)
						mAGC->memworkingwatch_ms += (int)(BEGINING_TIME*0.7);//if we recover the mic successful..
						assert(mAGC->memmicvol>=mAGC->mymic.vvolmin && mAGC->memmicvol<=mAGC->mymic.vvolmax);
					}
					else
					{
						mAGC->memmicvol = idB(mAGC->vvolfstdB);
					}
				} 
				else
				{
					if (mAGC->PostAGC == 1)
					{
						mAGC->vvolfstdB = (dB(mAGC->mymic.vvolmax) + dB(mAGC->mymic.vvolmin))/2;
					} 
					else
					{
						if (mAGC->vvolfstdB<dB(mAGC->mymic.vvolmin))
						{
							mAGC->vvolfstdB = dB(mAGC->mymic.vvolmin);
							assert(0);
						} 
						else if(mAGC->vvolfstdB>dB(mAGC->mymic.vvolmax))
						{
							mAGC->vvolfstdB = dB(mAGC->mymic.vvolmax);
							assert(0);
						}
					}

					mAGC->memmicvol = idB(mAGC->vvolfstdB);//andr and ios should also read lastmicinf...
				}
				
				
			}

			mAGC->memFirstSet = 1;
		}


		if (mAGC->mymic.capability & CAPABILITY_RVOL)
		{
			memrmicvol = mAGC->mymic.rvol;
			if (oricapavgdb > -78)
			{
				mAGC->memrvoloktimes++;
			}

			if (mAGC->memrvoloktimes > 100)//2s
			{
				if (mAGC->memrvolokmin > mAGC->memmicvol)
				{
					mAGC->memrvolokmin = mAGC->memmicvol;//says the rvol is ok
					mAGC->memrvoloktimes = 0;
				}
			}

			if (oricapavgdb <= -80.0)
			{
				mAGC->memlongmute++;
			}
			else
			{
				mAGC->memlongmute = 0;
			}
			if (mAGC->memlongmute > 25)//25*20ms
			{
				mAGC->memlongmute = 0;
				if (mAGC->memmicvol < mAGC->memrvolokmin)
				{
					if (mAGC->memmicvol > mAGC->memrvolmutemax)
					{
						mAGC->memrvolmutemax = mAGC->memmicvol;
					}					
				}
				if (mAGC->memmicvol < THEMINOF(mAGC->memrvolokmin,0.4))
				{
					mAGC->memmicvol += 0.01;
				}
				if (mAGC->memmutetimes < 5)//just avoid overflow
				{
					mAGC->memmutetimes++;
				}
			}
			if (mAGC->memmutetimes > 3)
			{
				if (mAGC->memmicvol < mAGC->memrvolmutemax + 0.013)//if cause mute 3 times,vol max bigger than memrvolmutemax
				{
					mAGC->memmicvol = mAGC->memrvolmutemax + 0.013;
				}
			}
		}
#if DEBUGAGC
		if (Gain > 1)
		{
			for (i = 0; i < inLen; i++)
			{
				justdebug[i] = dB(mAGC->memmicvol) * 1000;
			}
		}
		else
		{
			for (i = 0; i < inLen; i++)
			{
				justdebug[i] = 0;
			}
		}
		fwrite_SKR(justdebug, 2, inLen, "gain.pcm");
#endif
		MicAdjust(&mAGC->mymic,boost,rboostmove,vboostmove,&mAGC->memmicvol);

		if (mAGC->mymic.capability & CAPABILITY_RVOL)
		{
			if (memrmicvol != mAGC->mymic.rvol)
			{
				mAGC->memrvoloktimes = 0;
			}
		}


/////////////////////////////after fb/////////////////////////////////////////////
		if (rboostmove>0)
		{
			mAGC->memboostupdevicenormal = 0;//after boostup we must recheck if device can be work normally
		}

		if (mAGC->thenlp.SSA != 0 && *gainmod_dB <0)
		{
			mAGC->thenlp.memmicvolSS = mAGC->memmicvol;
			mAGC->thenlp.memSSdown_watch ++;
			if (mAGC->thenlp.memSSdown_watch > mAGC->thenlp.SSdown_gainThreshold)
			{
				mAGC->thenlp.memSSdown_watch = 0;
				if(mAGC->memmicvol >mAGC->mymic.rvolmin+0.01)//
				{
					mAGC->thenlp.memSSdown_gain *= mAGC->thenlp.SSdown_gainstep;
					mAGC->thenlp.memSSdown_gain = THEMINOF(mAGC->thenlp.memSSdown_gain,mAGC->thenlp.SSdown_gainmax);
				}

			}
		}

	}
	else
	{
		*gainmod_dB = 0.0;
		*boost = 0;
	}

#if	SEECLIPPING
	boost[3] = ClippingDetec(pinput, inLen,mAGC->chanel, mAGC->samplerate);
	//boost[3] = clipping;//这样只是输出进行检测时的过顶，会漏掉vol>0.4的时候，因为那时并未检测
#endif
	/*DY*/
	if (mAGC->DyKind & DY_ENABLE)
	{
        if(mAGC->DyKind & DY_NSGATE_ONLY)
        {
            noisegate_db = -40.0;
            
            if(mAGC->FeedbackKind & NORMIC_NOTUSEVBOOST)
            {
                if(mAGC->memmicvol>18.0)
                {
                    noisegate_db = -42.0;
                }
                if(mAGC->memmicvol>22.0)
                {
                    //noisegate_db = -46.0;
                }
            }
            
            if(maxx_db > noisegate_db)
            {
                mAGC->memDYNoiseGateSilenceCountDown = SPEECH_END_PROTECTION_GATE;
            }
            else
            {
                if(mAGC->memDYNoiseGateSilenceCountDown > 0)
                {
                    mAGC->memDYNoiseGateSilenceCountDown--;
                }
                
                if(mAGC->memDYNoiseGateSilenceCountDown == 0)
                {
                    if(mAGC->mymic.capability & CAPABILITY_VVOL)
                    {
                        for (i=0;i<inLen;i++)
                        {
                            //output[i] = pinput[i]/mAGC->memmicvol;
                            output[i] = (short)(pinput[i]*0.03125);//-30dB
                        }
                    }
                    else
                    {
                        for (i=0;i<inLen;i++)
                        {
                            output[i] = (short)(pinput[i]*0.25);//-12dB
                        }
                    }

                }

            }
                
            return;
        }
        

		



		if (mAGC->DyKind & DY_VAD_INTERNAL_CTRL)
		{
			UpdateDy(&mAGC->DyForAGC,mAGC->DyKind,mAGC->thenlp.ES,mAGC->memnoise_db,avergex_db,boost[1],(mAGC->DyKind & DY_VARIBLECUR_BUBBLE)/*,mAGC->samplerate*/);
		} 
		else
		{
			UpdateDy(&mAGC->DyForAGC,mAGC->DyKind,mAGC->thenlp.ES,mAGC->memnoise_db,avergex_db,1,(mAGC->DyKind & DY_VARIBLECUR_BUBBLE)/*,mAGC->samplerate*/);
		}
		

		{
			DynamicRun_API(&mAGC->DyForAGC,pinput,x_db,inLen,output,x_db);
		}

		if(mAGC->DyKind & DY_NSGATE)
		{
			noisegate_db = -40.0;

			if(mAGC->FeedbackKind & NORMIC_NOTUSEVBOOST)
			{
				if(mAGC->memmicvol>18.0)
				{
					noisegate_db = -42.0;
				}
				if(mAGC->memmicvol>22.0)
				{
					//noisegate_db = -46.0;
				}
			}

			if(maxx_db > noisegate_db)
			{
				mAGC->memDYNoiseGateSilenceCountDown = SPEECH_END_PROTECTION_GATE;
			}
			else
			{
				if(mAGC->memDYNoiseGateSilenceCountDown > 0)
				{
					mAGC->memDYNoiseGateSilenceCountDown--;
				}

				if(mAGC->memDYNoiseGateSilenceCountDown == 0)
				{
					if(mAGC->mymic.capability & CAPABILITY_VVOL)
					{
						for (i=0;i<inLen;i++)
						{
							//output[i] = pinput[i]/mAGC->memmicvol;
							output[i] = (short)(pinput[i]*0.03125);//-30dB
						}
					}
					else
					{
						for (i=0;i<inLen;i++)
						{
							output[i] = (short)(pinput[i]*0.25);//-12dB
						}
					}

				}

			}

			return;
		}
	}
	else
	{
		if (input != output)//we should output input, because dy=0 says the input[i]==output[i],and add the memSSdown_gain at agc's outside
		{
			for (i=0;i<inLen;i++)
			{
				output[i] = input[i];
			}
		}
	}


}

void GetNoiseCalcu_API(AGC_ID *mAGC)
{
	float *numlp,*denlp,*numhp,*denhp;
	int orderlp,orderhp;


	mAGC->LevelForAGC.channel = mAGC->chanel;
	mAGC->LevelForAGC.samplerate = mAGC->samplerate;




	mAGC->LevelForAGC.LevelAttackms = 0.5;
	mAGC->LevelForAGC.LevelRealeasems = 300;

	LevelCalcu_API(&mAGC->LevelForAGC);





}
void GetNoiseReset_API(AGC_ID *mAGC)
{
	AGCReset_API(mAGC);
}
float GetNoiseandAvgRun_API(AGC_ID *mAGC,short *input, int inLen)
{
	int i;
	float maxx_db = -140.0;
	float x_db[SKR_MAX_FRAME_SAMPLE_MONO];
	int inRlen;
	int localnoisechanged;
	int hold;
	int vad;
	float avgsubstax_db = 0;
	float avgsubendx_db = 0;
	float avergex_db = 0;

	if (mAGC->chanel == 2)
	{
		inRlen = inLen/2;
	}
	else if(mAGC->chanel == 1)
	{
		inRlen = inLen;
	}
	else
	{
		assert(0);
	}

	LevelRun_API(&mAGC->LevelForAGC,input,inLen,x_db);

	for(i = 0;i<inRlen;i++)
	{
		if(x_db[i]>maxx_db)
		{
			maxx_db = x_db[i];
		}
	}

	{
		for (i = 0;i<inRlen/2;i++)
		{
			avgsubstax_db += x_db[i];
		}
		avgsubstax_db = 2*avgsubstax_db/inRlen;

		for (;i<inRlen;i++)
		{
			avgsubendx_db += x_db[i];
		}
		avgsubendx_db = 2*avgsubendx_db/inRlen;

		/*for (i = 0;i<inRlen;i++)
		{
			avergex_db += x_db[i];
		}
		avergex_db = avergex_db/inRlen;*/
		
		avergex_db = (avgsubendx_db + avgsubstax_db)/2;
	}


	noise_db3(maxx_db,inRlen,&mAGC->memnoiseVAD_db,&mAGC->memUpwatchVAD,mAGC->memnewnoiseVAD_db,NOISE_UP_THRESHOLDVAD);
	
	
	return avergex_db;

}
void GetNoiseRun_API(AGC_ID *mAGC,short *input, int inLen)
{
	int i;
	float maxx_db = -140.0;
	float x_db[SKR_MAX_FRAME_SAMPLE_MONO];
	int inRlen;
	int localnoisechanged;
	int hold;

	if (mAGC->chanel == 2)
	{
		inRlen = inLen/2;
	}
	else if(mAGC->chanel == 1)
	{
		inRlen = inLen;
	}
	else
	{
		assert(0);
	}

	LevelRun_API(&mAGC->LevelForAGC,input,inLen,x_db);

	for(i = 0;i<inRlen;i++)
	{
		if(x_db[i]>maxx_db)
		{
			maxx_db = x_db[i];
		}
	}



	noise_db3(maxx_db,inRlen,&mAGC->memnoiseVAD_db,&mAGC->memUpwatchVAD,mAGC->memnewnoiseVAD_db,NOISE_UP_THRESHOLDVAD);
	


}



void NoiseRepairCalcu_API(AGC_ID *mAGC)
{
	float *numlp,*denlp,*numhp,*denhp;
	int orderlp,orderhp;


	mAGC->LevelForAGC.channel = mAGC->chanel;
	mAGC->LevelForAGC.samplerate = mAGC->samplerate;




	Options_for_TRAE_NoiseRepair(mAGC);

	LevelCalcu_API(&mAGC->LevelForAGC);





}
void NoiseRepairReset_API(AGC_ID *mAGC)
{
	AGCReset_API(mAGC);
}
int NoiseRepairRun_API(AGC_ID *mAGC,short *input, int inLen, short *output)
{
	int i;
	float maxx_db = -140.0;
	float x_db[SKR_MAX_FRAME_SAMPLE_MONO];
	int inRlen;
	int localnoisechanged;
	int hold;
	
	if (mAGC->chanel == 2)
	{
		inRlen = inLen/2;
	}
	else if(mAGC->chanel == 1)
	{
		inRlen = inLen;
	}
	else
	{
		assert(0);
	}

	LevelRun_API(&mAGC->LevelForAGC,input,inLen,x_db);

    for(i = 0;i<inRlen;i++)
    {
        if(x_db[i]>maxx_db)
        {
            maxx_db = x_db[i];
        }
    }
    
	
	mAGC->memThisIsCut = noise_db4(maxx_db,&mAGC->memnoiseVAD_db,&mAGC->memUpwatchVAD,mAGC->memnewnoiseVAD_db,NOISE_UP_THRESHOLDVAD,&localnoisechanged,&mAGC->memholdlowernoisewatch,&mAGC->memminxdb,&hold,input,inLen,mAGC->memtherealnoise_down,mAGC->memtherealnoise_up,mAGC->memtherealnoise/*output when localnoisechanged*/);
	
	//if (mAGC->memThisIsCut == 0 && localnoisechanged == 1)//save the real noise before noise cut
	//{
	//	for(i = 0;i<inLen;i++)
	//	{
	//		mAGC->memtherealnoise[i] = input[i];
	//		//debug
	//		if (input[i] == 291)
	//		{
	//			input[i] = 291;
	//		}
	//	}
	//}
	
	//if(1)// 
	if(mAGC->memThisIsCut == 1)
	{
		for (i = 0;i<inLen;i++)
		{
			output[i] = mAGC->memtherealnoise[i];

		}

	} 
	else
	{
		if (input!=output)
		{
			for (i = 0;i<inLen;i++)
			{
				output[i] = input[i];
			}
		}
	}
	//return localnoisechanged;
	return mAGC->memThisIsCut;
	//return hold;
}




int NoiseRepairRun_API_10ms_tmod2(AGC_ID *mAGC,float *input, int inLen, float *output,float threshold)
{
	int i;
	float maxx_db = -140.0;
	float x_db[SKR_MAX_FRAME_SAMPLE_MONO];
	int inRlen;
	int localnoisechanged;
	int hold;
	short inputstoshort[SKR_MAX_FRAME_SAMPLE_STEREO];//simply stoshort is enough for noiseproc
	int cut = 1;

	printf("fdsfdsafdsafdafdsafdsa!\n");
	
	
	if (mAGC->chanel == 2)
	{
		inRlen = inLen/2;
	}
	else if(mAGC->chanel == 1)
	{
		inRlen = inLen;
	}
	else
	{
		assert(0);
	}

	for (i = 0;i<inLen;i++)
	{
		inputstoshort[i] = stoshort(input[i]);
	}

	//if(1)// 
	for (i = 0;i<inLen;i++)
	{
		if (abs((int)(inputstoshort[i]))>threshold)
		{
			cut = 0;
			break;
		}
	}
	if(cut == 1)
	{
		for (i = 0;i<inLen;i++)
		{
			output[i] = (float)(mAGC->memtherealnoise[i]);

		}

	} 
	else
	{
		LevelRun_API(&mAGC->LevelForAGC,inputstoshort,inLen,x_db);

		for(i = 0;i<inRlen;i++)
		{
			if(x_db[i]>maxx_db)
			{
				maxx_db = x_db[i];
			}
		}
		noise_db5(maxx_db,&mAGC->memnoiseVAD_db,&mAGC->memUpwatchVAD,mAGC->memnewnoiseVAD_db,NOISE_UP_THRESHOLDVAD*2/*10msframe*/,&localnoisechanged,&mAGC->memholdlowernoisewatch,&mAGC->memminxdb,&hold,input,inLen,mAGC->memtherealnoise_down,mAGC->memtherealnoise_up,mAGC->memtherealnoise/*output when localnoisechanged*/);
		if (input!=output)
		{
			for (i = 0;i<inLen;i++)
			{
				output[i] = input[i];
			}
		}
	}
	
	return cut;
	//return hold;
}






int NoiseRepairRun_API_10ms_tmod(AGC_ID *mAGC,float *input, int inLen, float *output,float threshold)
{
	int i;
	float maxx_db = -140.0;
	float x_db[SKR_MAX_FRAME_SAMPLE_MONO];
	int inRlen;
	int localnoisechanged;
	int hold;
	short inputstoshort[SKR_MAX_FRAME_SAMPLE_STEREO];//simply stoshort is enough for noiseproc
	int cut = 1;

	printf("NoiseRepairRun_API_10ms_tmod!\n");




	return 1;
	//return hold;
}

/* Initialize AGC module, call this once before using AGC module */
int AGCInit_API(AGC_ID *mAGC)
{
	return 0;
}

/* Uninitialize AGC module, call this once after using AGC module */
int AGCUninit_API(AGC_ID *mAGC)
{
	

	return 0;
}
void Options_for_TRAE_VAD(VAD_ID *mAGC)
{

	/*前处理部分;RadioLimitMod*/


	mAGC->LevelForAGC.LevelAttackms = 0.5;//暂时没用
	mAGC->LevelForAGC.LevelRealeasems = 300.0;

}




void VADReset_API(VAD_ID *mAGC)
{
	int i;


	mAGC->memnoiseVAD_db = 0.0;
	mAGC->memUpwatchVAD = 0;
	mAGC->memSpeech = 1;

	//mAGC->memSS = 0;
	mAGC->memminnoise_db = 0.0;
	//mAGC->memmax_db = -95.0;
	mAGC->mempeakavg_db = -95.0;
	mAGC->memmax_db = -95.0;

	/*AGCVAD:音尾保护*/
	mAGC->memSilenceCountDown = SPEECH_END_PROTECTION;

	for (i=0;i<NOISE_UP_THRESHOLDVAD;i++)
	{
		mAGC->memnewnoiseVAD_db[i] = 0.0;
	}
	for (i=0;i<MEMPREAVGDB;i++)
	{
		mAGC->mempreavgdb[i] = -140;
	}

	LevelReset_API(&mAGC->LevelForAGC);



}

void VADCalcu_API(VAD_ID *mAGC)
{
	float *numlp,*denlp,*numhp,*denhp;
	int orderlp,orderhp;



	mAGC->LevelForAGC.channel = mAGC->chanel;
	mAGC->LevelForAGC.samplerate = mAGC->samplerate;


	Options_for_TRAE_VAD(mAGC);

	LevelCalcu_API(&mAGC->LevelForAGC);



}

int VADRun_API(VAD_ID *mAGC, short *input, int inLen)
{
	int i;
	int vad;
	float avergex_db = 0;
    float maxx_db = -140.0;
	float capavergex_db = 0;
	float avgsubstax_db = 0;
	float avgsubendx_db = 0;

	float x_db[SKR_MAX_FRAME_SAMPLE_MONO];
	//float lastmicvol;
	int inRlen;
	

	short inputhped[SKR_MAX_FRAME_SAMPLE_STEREO];
	short pre_input[SKR_MAX_FRAME_SAMPLE_STEREO];

	short *pinput;
	int inabs;
	int maxinabs;
    



	if (ZEROINPUT_THRESHOLD>0)
	{
		for (i = 0,maxinabs = 0;i<inLen;i++)
		{
			inabs = abs(input[i]);
			if (inabs > maxinabs)
			{
				maxinabs = inabs;
			}
		}

		if (maxinabs < ZEROINPUT_THRESHOLD)
		{
			//thisframe = 0;

			return 0;//vad
		}
	}
	

	{
		pinput = input;
	}

	
	



	if (mAGC->chanel == 2)
	{
		inRlen = inLen/2;
	}
	else if(mAGC->chanel == 1)
	{
		inRlen = inLen;
	}
	else
	{
		assert(0);
	}


	{

		
		//LevelRun_API(&mAGC->LevelForAGC,inputvboosted,inLen,x_db);//先看下..高通后boost的噪声条件？等等等等..AGC高通需要慎重
		LevelRun_API(&mAGC->LevelForAGC,pinput,inLen,x_db);

	}



	{
		for (i = 0;i<inRlen/2;i++)
		{
			avgsubstax_db += x_db[i];
		}
		avgsubstax_db = 2*avgsubstax_db/inRlen;

		for (;i<inRlen;i++)
		{
			avgsubendx_db += x_db[i];
		}
		avgsubendx_db = 2*avgsubendx_db/inRlen;

		
		mAGC->mempeakavg_db = avergex_db = (avgsubendx_db + avgsubstax_db)/2;
	}
    {
        for(i = 0;i<inRlen;i++)
        {
            if(x_db[i]>maxx_db)
            {
                 maxx_db= x_db[i];
            }
        }
    }
    
    mAGC->memmax_db = maxx_db;
	
	

	
	{
		noise_db3(maxx_db,inRlen,&mAGC->memnoiseVAD_db,&mAGC->memUpwatchVAD,mAGC->memnewnoiseVAD_db,NOISE_UP_THRESHOLDVAD);
	}




	{

		return AGCVAD(&mAGC->memSpeech,&mAGC->memSilenceCountDown,mAGC->memnoiseVAD_db,avgsubstax_db,avgsubendx_db,avergex_db);

	}



}

int VAD2Run_API(VAD_ID *mAGC, short *input, int inLen,float vippesvdb,float period)
{
	int i;
	int vad;
	float avergex_db = 0;
    float maxx_db = -140.0;
	float capavergex_db = 0;
	float avgsubstax_db = 0;
	float avgsubendx_db = 0;
	float lasthighdb;

	float x_db[SKR_MAX_FRAME_SAMPLE_MONO];
	//float lastmicvol;
	int inRlen = 0;
	

	short inputhped[SKR_MAX_FRAME_SAMPLE_STEREO];
	short pre_input[SKR_MAX_FRAME_SAMPLE_STEREO];

	short *pinput;

	int inabs;
	int maxinabs;
    



	if (ZEROINPUT_THRESHOLD>0)
	{
		for (i = 0,maxinabs = 0;i<inLen;i++)
		{
			inabs = abs(input[i]);
			if (inabs > maxinabs)
			{
				maxinabs = inabs;
			}
		}

		if (maxinabs < ZEROINPUT_THRESHOLD)
		{
			//thisframe = 0;

			return 0;//vad
		}
	}
	

	{
		pinput = input;
	}

	
	



	if (mAGC->chanel == 2)
	{
		inRlen = inLen/2;
	}
	else if(mAGC->chanel == 1)
	{
		inRlen = inLen;
	}
	else
	{
		assert(0);
	}


	{

		//LevelRun_API(&mAGC->LevelForAGC,inputvboosted,inLen,x_db);//先看下..高通后boost的噪声条件？等等等等..AGC高通需要慎重
		LevelRun_API(&mAGC->LevelForAGC,pinput,inLen,x_db);

	}



	{
		for (i = 0;i<inRlen/2;i++)
		{
			avgsubstax_db += x_db[i];
		}
		avgsubstax_db = 2*avgsubstax_db/inRlen;

		for (;i<inRlen;i++)
		{
			avgsubendx_db += x_db[i];
		}
		avgsubendx_db = 2*avgsubendx_db/inRlen;

		
		mAGC->mempeakavg_db = avergex_db = (avgsubendx_db + avgsubstax_db)/2;
	}
    {
        for(i = 0;i<inRlen;i++)
        {
            if(x_db[i]>maxx_db)
            {
                 maxx_db= x_db[i];
            }
        }
    }
    
    mAGC->memmax_db = maxx_db;
	
	

	
	{
		noise_dbvip(maxx_db,inRlen,&mAGC->memnoiseVAD_db,&mAGC->memUpwatchVAD,mAGC->memnewnoiseVAD_db,NOISE_UP_THRESHOLDVAD);
	}

	for (i = 0;i<3;i++)
	{
		mAGC->mempreavgdb[i] = mAGC->mempreavgdb[i+1];
	}
	mAGC->mempreavgdb[i] = avergex_db;
	lasthighdb = -100;
	for (i = 0;i<3;i++)
	{
		if (mAGC->mempreavgdb[i]>lasthighdb)
		{
			lasthighdb = mAGC->mempreavgdb[i];
		}
	}

	{

		return vipVAD(&mAGC->memSpeech,&mAGC->memSilenceCountDown,mAGC->memnoiseVAD_db,avgsubstax_db,avgsubendx_db,avergex_db,vippesvdb,lasthighdb,period);

	}



}
int VAD2mod3Run_API(VAD_ID *mAGC, short *input, int inLen,float vippesvdb,float period)
{
	int i;
	int vad;
	float avergex_db = 0;
    float maxx_db = -140.0;
	float capavergex_db = 0;
	float avgsubstax_db = 0;
	float avgsubendx_db = 0;

	float x_db[SKR_MAX_FRAME_SAMPLE_MONO];
	//float lastmicvol;
	int inRlen;
	

	short inputhped[SKR_MAX_FRAME_SAMPLE_STEREO];
	short pre_input[SKR_MAX_FRAME_SAMPLE_STEREO];

	short *pinput;

	int inabs;
	int maxinabs;
    



	if (ZEROINPUT_THRESHOLD>0)
	{
		for (i = 0,maxinabs = 0;i<inLen;i++)
		{
			inabs = abs(input[i]);
			if (inabs > maxinabs)
			{
				maxinabs = inabs;
			}
		}

		if (maxinabs < ZEROINPUT_THRESHOLD)
		{
			//thisframe = 0;

			return 0;//vad
		}
	}
	

	{
		pinput = input;
	}

	
	



	if (mAGC->chanel == 2)
	{
		inRlen = inLen/2;
	}
	else if(mAGC->chanel == 1)
	{
		inRlen = inLen;
	}
	else
	{
		assert(0);
	}


	{

		//LevelRun_API(&mAGC->LevelForAGC,inputvboosted,inLen,x_db);//先看下..高通后boost的噪声条件？等等等等..AGC高通需要慎重
		LevelRun_API(&mAGC->LevelForAGC,pinput,inLen,x_db);

	}



	{
		for (i = 0;i<inRlen/2;i++)
		{
			avgsubstax_db += x_db[i];
		}
		avgsubstax_db = 2*avgsubstax_db/inRlen;

		for (;i<inRlen;i++)
		{
			avgsubendx_db += x_db[i];
		}
		avgsubendx_db = 2*avgsubendx_db/inRlen;

		
		mAGC->mempeakavg_db = avergex_db = (avgsubendx_db + avgsubstax_db)/2;
	}
    {
        for(i = 0;i<inRlen;i++)
        {
            if(x_db[i]>maxx_db)
            {
                 maxx_db= x_db[i];
            }
        }
    }
    
    mAGC->memmax_db = maxx_db;
	
	

	
	{
		noise_dbvip(maxx_db,inRlen,&mAGC->memnoiseVAD_db,&mAGC->memUpwatchVAD,mAGC->memnewnoiseVAD_db,NOISE_UP_THRESHOLDVAD);
	}




	{

		return vipVADmod3(&mAGC->memSpeech,&mAGC->memSilenceCountDown,mAGC->memnoiseVAD_db,avgsubstax_db,avgsubendx_db,avergex_db,vippesvdb,period);

	}



}
int VAD2mod4Run_API(VAD_ID *mAGC, short *input, int inLen,float vippesvdb,float period)
{
	int i;
	int vad;
	float avergex_db = 0;
	float maxx_db = -140.0;
	float capavergex_db = 0;
	float avgsubstax_db = 0;
	float avgsubendx_db = 0;
	float lasthighdb;

	float x_db[SKR_MAX_FRAME_SAMPLE_MONO];
	//float lastmicvol;
	int inRlen;


	short inputhped[SKR_MAX_FRAME_SAMPLE_STEREO];
	short pre_input[SKR_MAX_FRAME_SAMPLE_STEREO];

	short *pinput;

	int inabs;
	int maxinabs;




	if (ZEROINPUT_THRESHOLD>0)
	{
		for (i = 0,maxinabs = 0;i<inLen;i++)
		{
			inabs = abs(input[i]);
			if (inabs > maxinabs)
			{
				maxinabs = inabs;
			}
		}

		if (maxinabs < ZEROINPUT_THRESHOLD)
		{
			//thisframe = 0;

			return 0;//vad
		}
	}


	{
		pinput = input;
	}






	if (mAGC->chanel == 2)
	{
		inRlen = inLen/2;
	}
	else if(mAGC->chanel == 1)
	{
		inRlen = inLen;
	}
	else
	{
		assert(0);
	}


	{

		//LevelRun_API(&mAGC->LevelForAGC,inputvboosted,inLen,x_db);//先看下..高通后boost的噪声条件？等等等等..AGC高通需要慎重
		LevelRun_API(&mAGC->LevelForAGC,pinput,inLen,x_db);

	}



	{
		for (i = 0;i<inRlen/2;i++)
		{
			avgsubstax_db += x_db[i];
		}
		avgsubstax_db = 2*avgsubstax_db/inRlen;

		for (;i<inRlen;i++)
		{
			avgsubendx_db += x_db[i];
		}
		avgsubendx_db = 2*avgsubendx_db/inRlen;


		mAGC->mempeakavg_db = avergex_db = (avgsubendx_db + avgsubstax_db)/2;
	}
	{
		for(i = 0;i<inRlen;i++)
		{
			if(x_db[i]>maxx_db)
			{
				maxx_db= x_db[i];
			}
		}
	}

	mAGC->memmax_db = maxx_db;




	{
		noise_dbvip(maxx_db,inRlen,&mAGC->memnoiseVAD_db,&mAGC->memUpwatchVAD,mAGC->memnewnoiseVAD_db,NOISE_UP_THRESHOLDVAD);
	}

	for (i = 0;i<3;i++)
	{
		mAGC->mempreavgdb[i] = mAGC->mempreavgdb[i+1];
	}
	mAGC->mempreavgdb[i] = avergex_db;
	lasthighdb = -100;
	for (i = 0;i<3;i++)
	{
		if (mAGC->mempreavgdb[i]>lasthighdb)
		{
			lasthighdb = mAGC->mempreavgdb[i];
		}
	}

	{

		return vipVADmod4(&mAGC->memSpeech,&mAGC->memSilenceCountDown,mAGC->memnoiseVAD_db,avgsubstax_db,avgsubendx_db,avergex_db,vippesvdb,lasthighdb,period);

	}



}
void PESVResetCalcu_API(PESV_ID *mPESV)//if samplerate has changed reset must be done
{
	
	//assert(mPESV->chanel == 1);
	/*now we had use peak only, pesv_rms can be removed...
	VWLevelReset_API_RMS(&mPESV->mvwl_rms);
	mPESV->mvwl_rms.WindowLen = 2;
*/
	mPESV->mvwl_peak.samplerate = mPESV->samplerate;
	mPESV->mvwl_peak.WindowLen = 3;
	VWLevelReset_API_Peak(&mPESV->mvwl_peak);

	LevelReset_API(&mPESV->mlevel);
	mPESV->mlevel.channel = mPESV->chanel;
	mPESV->mlevel.samplerate = mPESV->samplerate;
	mPESV->mlevel.LevelAttackms = 0.05;
	mPESV->mlevel.LevelRealeasems = 100;
	LevelCalcu_API(&mPESV->mlevel);

	mPESV->m_AGCVAD.chanel = mPESV->chanel;
	mPESV->m_AGCVAD.samplerate = mPESV->samplerate;//may change within set
	VADCalcu_API(&mPESV->m_AGCVAD);
	VADReset_API(&mPESV->m_AGCVAD);

	mPESV->mDCC.Chanel = mPESV->chanel;
	mPESV->mDCC.samplerate = mPESV->samplerate;
	DCCReset_API(&mPESV->mDCC);
	DCCCalcu_API(&mPESV->mDCC);


}
void PESVRun_API(PESV_ID *mPESV,short *x,int xlen)
{
	float x_db[SKR_MAX_FRAME_SAMPLE_MONO];
	int inRlen;

	inRlen = xlen/mPESV->chanel;

	if (mPESV->needDC == 0)
	{

	}
	else
	{
		DCCRun_API(&mPESV->mDCC,x, xlen, x);
	}


	VADRun_API(&mPESV->m_AGCVAD, x, xlen);  

	//if (boost[1] == 1)
	if(mPESV->m_AGCVAD.memSpeech == 1)//this is more aggressive
	{
		//VWLevelRun_API_RMS(&mPESV->mvwl_rms,x,xlen);now we had use peak only, pesv_rms can be removed...

		LevelRun_API(&mPESV->mlevel,x,xlen,x_db);

		VWLevelRun_API_Peak(&mPESV->mvwl_peak,x_db,inRlen);
	}

}
void PESV_API(PESV_ID *mPESV,float *peakdb,float *peaklq,float *rmsdb,float *rmslq)
{
	*peakdb = Statistics_API(&mPESV->mvwl_peak);
	*peaklq = 0;// levelquality(*peakdb);
	*rmsdb = -150;//Statistics_API(&mPESV->mvwl_rms);
	*rmslq = 0;//levelquality(*rmsdb);
}
