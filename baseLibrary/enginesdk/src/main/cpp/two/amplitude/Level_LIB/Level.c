#include <stdlib.h>
#include "Level_control.h"
#include "Level_defines.h"
#include "../../common/functions.h"
#include "../../common/defines.h"
#include <assert.h>

static float db_short[32769];
static int db_shortcalcued = 0;

static __inline float db_intin(int x)//x>=0
{
	assert(x>=0);

	if (x<32769)
	{
		return db_short[x];
	} 
	else
	{
		return 20*log10(x/U0);
	}
}



void LevelReset_API(Level_s *mLevel)
{
	mLevel->meminputLpeak_db[0] = -150;
}

void LevelCalcu_API(Level_s *mLevel)
{
	int i;

	mLevel->Tao_dBpT = (0 - MIN_RC_DB)*1000/(mLevel->LevelRealeasems * mLevel->samplerate);
	if (db_shortcalcued == 0)
	{
		for (i=0;i<32769;i++)
		{
			db_short[i] = db(i,U0);
		}
		db_shortcalcued = 1;
	}

}

void LevelRun_API(Level_s *mLevel,const short *x,int xlen,float *peakx_db)//充电时间常数为0的包络检波
{
	int i,j;
	int inRlen;
	
	if (mLevel->channel == 1)
	{
		peakx_db[0] = THEMAXOF(db_short[abs(x[0])], mLevel->meminputLpeak_db[0] - mLevel->Tao_dBpT);
		for (i = 1; i < xlen;i++)
		{
			peakx_db[i]=THEMAXOF(db_short[abs(x[i])],peakx_db[i-1] - mLevel->Tao_dBpT);
		}
		mLevel->meminputLpeak_db[0]=peakx_db[xlen-1];
	} 
	else if (mLevel->channel == 2)
	{
		inRlen = xlen/2;
		peakx_db[0] = THEMAXOF(db_short[THEMAXOF(abs(x[0]),abs(x[1]))] ,  mLevel->meminputLpeak_db[0] - mLevel->Tao_dBpT);
		for (j = 1,i = 2; j < inRlen; i+=2,j++)
		{
			peakx_db[j]=THEMAXOF(db_short[THEMAXOF(abs(x[i]),abs(x[i+1]))] ,  peakx_db[j-1] - mLevel->Tao_dBpT);
		}
		mLevel->meminputLpeak_db[0]=peakx_db[inRlen-1];
	}
	else
	{
		assert(0);
	}
	//mLevel->meminputLpeak_db[0]= THEMAXOF(mLevel->meminputLpeak_db[0],-150);//prevent -∞ in fact no need

}

float LevelRun_API_avgxdb(Level_s *mLevel,const short *x,int xlen)
{
	int i,j;
	int inRlen = 0;
	float peakx_db[SKR_MAX_FRAME_SAMPLE_MONO] = {0};
	float avgxdb;

	if (mLevel->channel == 1)
	{
		inRlen = xlen;
		peakx_db[0] = THEMAXOF(db_short[abs(x[0])], mLevel->meminputLpeak_db[0] - mLevel->Tao_dBpT);
		for (i = 1; i < xlen;i++)
		{
			peakx_db[i]=THEMAXOF(db_short[abs(x[i])],peakx_db[i-1] - mLevel->Tao_dBpT);
		}
		mLevel->meminputLpeak_db[0]=peakx_db[xlen-1];
	} 
	else if (mLevel->channel == 2)
	{
		inRlen = xlen/2;
		peakx_db[0] = THEMAXOF(db_short[THEMAXOF(abs(x[0]),abs(x[1]))] ,  mLevel->meminputLpeak_db[0] - mLevel->Tao_dBpT);
		for (j = 1,i = 2; j < inRlen; i+=2,j++)
		{
			peakx_db[j]=THEMAXOF(db_short[THEMAXOF(abs(x[i]),abs(x[i+1]))] ,  peakx_db[j-1] - mLevel->Tao_dBpT);
		}
		mLevel->meminputLpeak_db[0]=peakx_db[inRlen-1];
	}
	else
	{
		assert(0);
	}

	for (avgxdb=0,i=0;i<inRlen;i++)
	{
		avgxdb += peakx_db[i];
	}
	return avgxdb /= inRlen;
	//mLevel->meminputLpeak_db[0]= THEMAXOF(mLevel->meminputLpeak_db[0],-150);//prevent -∞ in fact no need

}
//void LevelRun_API_floatin(Level_s *mLevel,float *x,int xlen,float *peakx_db)//充电时间常数为0的包络检波
//{
//	int i,j;
//	int inRlen;
//
//	if (mLevel->Chanel == 1)
//	{
//		peakx_db[0] = THEMAXOF(db(abs(x[0]),U0),mLevel->meminputLpeak_db[0] - mLevel->Tao_dBpT);
//		for (i = 1; i < xlen;i++)
//		{
//			peakx_db[i]=THEMAXOF(db(abs(x[i]),U0),peakx_db[i-1] - mLevel->Tao_dBpT);
//		}
//		mLevel->meminputLpeak_db[0]=peakx_db[xlen-1];
//	} 
//	else if (mLevel->Chanel == 2)
//	{
//		inRlen = xlen/2;
//		peakx_db[0] = THEMAXOF(db(THEMAXOF(abs(x[0]),abs(x[1])),U0),mLevel->meminputLpeak_db[0] - mLevel->Tao_dBpT);
//		for (j = 1,i = 2; j < inRlen; i+=2,j++)
//		{
//			peakx_db[j]=THEMAXOF(db(THEMAXOF(abs(x[i]),abs(x[i+1])),U0),peakx_db[j-1] - mLevel->Tao_dBpT);
//		}
//		mLevel->meminputLpeak_db[0]=peakx_db[inRlen-1];
//	}
//	else
//	{
//		assert(0);
//	}
//
//}

void LevelRun_API_intin(Level_s *mLevel,int *x,int xlen,float *peakx_db)//充电时间常数为0的包络检波
{
	int i,j;
	int inRlen;

	if (mLevel->channel == 1)
	{
		peakx_db[0] = THEMAXOF(db_intin(abs(x[0])),mLevel->meminputLpeak_db[0] - mLevel->Tao_dBpT);
		for (i = 1; i < xlen;i++)
		{
			peakx_db[i]=THEMAXOF(db_intin(abs(x[i])),peakx_db[i-1] - mLevel->Tao_dBpT);
		}
		mLevel->meminputLpeak_db[0]=peakx_db[xlen-1];
	} 
	else if (mLevel->channel == 2)
	{
		inRlen = xlen/2;
		peakx_db[0] = THEMAXOF(db_intin(THEMAXOF(abs(x[0]),abs(x[1]))),mLevel->meminputLpeak_db[0] - mLevel->Tao_dBpT);
		for (j = 1,i = 2; j < inRlen; i+=2,j++)
		{
			peakx_db[j]=THEMAXOF(db_intin(THEMAXOF(abs(x[i]),abs(x[i+1]))),peakx_db[j-1] - mLevel->Tao_dBpT);
		}
		mLevel->meminputLpeak_db[0]=peakx_db[inRlen-1];
	}
	else
	{
		assert(0);
	}

}

/* now we had use peak only, pesv_rms can be removed...
void VWLevelReset_API_RMS(VWLevel_s *mVWLevel)
{
	int i;

	mVWLevel->memTotalframes = 0;
	mVWLevel->memTMPLEVEL = 0.0;
	mVWLevel->memframeTMP_i = 0;
	for (i = 0;i<VWWINDOWLEN;i++)
	{
		mVWLevel->memframeTMPLEVEL[i] = 0;
	}
	for(i = 0;i<STATIS_DB_N;i++)
	{
		mVWLevel->memPavglevel[i] = 0;
		mVWLevel->memPlevel[i] = 0;
	}

}
*/
void VWLevelReset_API_Peak(VWLevel_s *mVWLevel)
{
	int i;

	mVWLevel->memTotalframes = 0;
	mVWLevel->memTMPLEVEL = -150.0*mVWLevel->samplerate*0.02*mVWLevel->WindowLen;
	assert(mVWLevel->WindowLen>0&&mVWLevel->WindowLen<VWWINDOWLEN);
	mVWLevel->memframeTMP_i = 0;
	for (i = 0;i<VWWINDOWLEN;i++)
	{
		mVWLevel->memframeTMPLEVEL[i] = -150.0*mVWLevel->samplerate*0.02;
		assert(mVWLevel->samplerate > 0 && mVWLevel->samplerate < 192000);
	}
	for(i = 0;i<STATIS_DB_N;i++)
	{
		mVWLevel->memPavglevel[i] = 0;
		mVWLevel->memPlevel[i] = 0;
	}

}
/* now we had use peak only, pesv_rms can be removed...
void VWLevelRun_API_RMS(VWLevel_s *mVWLevel,short *x,int xlen)
{
	int i;
	float xx;
	float RMS_db;
	short RMS_db_n;

	mVWLevel->memTotalframes++;//if >max unint reset
	
	for (i=0,xx = 0;i<xlen;i++)
	{
		xx += x[i]*x[i];
	}

	//mVWLevel->memTMPLEVEL += xx - mVWLevel->memframeTMPLEVEL[mVWLevel->memframeTMP_i];
	mVWLevel->memTMPLEVEL -= mVWLevel->memframeTMPLEVEL[mVWLevel->memframeTMP_i];
	mVWLevel->memTMPLEVEL += xx;
	
	mVWLevel->memframeTMPLEVEL[mVWLevel->memframeTMP_i] = xx;

	mVWLevel->memframeTMP_i ++;
	mVWLevel->memframeTMP_i %= mVWLevel->WindowLen;

	RMS_db = db(sqrt(mVWLevel->memTMPLEVEL/(mVWLevel->WindowLen*xlen)),U0) + 3;//sine wave is 3dB larger than square wave
	if (RMS_db<-150)
	{
		RMS_db = -150;//avoid -∞ and avoid -32769
	}
	RMS_db_n = -(short)RMS_db;

	if (RMS_db_n<0)
	{
		//assert(0);
		RMS_db_n = 0;
	}
	if (RMS_db_n >= STATIS_DB_N)
	{
		RMS_db_n = STATIS_DB_N-1;
	}

	mVWLevel->memPlevel[RMS_db_n] ++;
	mVWLevel->memPavglevel[RMS_db_n] = (mVWLevel->memPavglevel[RMS_db_n]*(mVWLevel->memPlevel[RMS_db_n]-1)+RMS_db)/mVWLevel->memPlevel[RMS_db_n];

}
*/
void VWLevelRun_API_Peak(VWLevel_s *mVWLevel,float *x_db,int xlen)
{
	int i;
	float avgpeak_db;
	short peak_db_n;

	mVWLevel->memTotalframes++;//if >max unint reset

	for (i=0,avgpeak_db = 0;i<xlen;i++)
	{
		avgpeak_db += x_db[i];
	}

	mVWLevel->memTMPLEVEL += avgpeak_db - mVWLevel->memframeTMPLEVEL[mVWLevel->memframeTMP_i];
	mVWLevel->memframeTMPLEVEL[mVWLevel->memframeTMP_i] = avgpeak_db;

	mVWLevel->memframeTMP_i ++;
	mVWLevel->memframeTMP_i %= mVWLevel->WindowLen;

	avgpeak_db = mVWLevel->memTMPLEVEL/(mVWLevel->WindowLen*xlen);//sine wave is 3dB larger than square wave

	if (avgpeak_db<-150)
	{
		avgpeak_db = -150;//avoid -∞ and avoid -32769
	}
	peak_db_n = -(short)(avgpeak_db);

	if (peak_db_n<0)
	{
		//assert(0);
		peak_db_n = 0;
	}
	if (peak_db_n >= STATIS_DB_N)
	{
		peak_db_n = STATIS_DB_N-1;
	}

	mVWLevel->memPlevel[peak_db_n] ++;
	mVWLevel->memPavglevel[peak_db_n] = (mVWLevel->memPavglevel[peak_db_n]*(mVWLevel->memPlevel[peak_db_n]-1)+avgpeak_db)/mVWLevel->memPlevel[peak_db_n];





}
//void VWLevelRun_API_Peakmem(VWLevel_s *mVWLevel,float peak_db)
//{
//	int i;
//	float xx;
//	short peak_db_n;
//
//	mVWLevel->memTotalframes++;//if >max unint reset
//
//	peak_db_n = -(short)peak_db;
//	if (peak_db_n<0)
//	{
//		assert(0);
//		peak_db_n = 0;
//	}
//	if (peak_db_n >= STATIS_DB_N)
//	{
//		peak_db_n = STATIS_DB_N-1;
//	}
//
//	mVWLevel->memPlevel[peak_db_n] ++;
//	mVWLevel->memPavglevel[peak_db_n] = (mVWLevel->memPavglevel[peak_db_n]*(mVWLevel->memPlevel[peak_db_n]-1)+peak_db)/mVWLevel->memPlevel[peak_db_n];
//
//
//
//}
float Statistics_API(VWLevel_s *mVWLevel)
{
	//float p[5];
	int i,j;
	float voicewholeleveldb = 12345;
	int getvol=0;
	float sum;
	float sumdb;

	if (mVWLevel->memTotalframes == 0)//warning!
	{
		return 1234;
	}
	//if (mVWLevel->memTotalframes*0.02 < 15)//15s  we can use this in app
	//{
	//	return 123;
	//}
	for (i=0;i<STATIS_DB_N-1;i++)
	{
		if (
			mVWLevel->memPlevel[i]/(float)mVWLevel->memTotalframes>0.0078
			&& (mVWLevel->memPlevel[i+1]/(float)mVWLevel->memTotalframes - mVWLevel->memPlevel[i]/(float)mVWLevel->memTotalframes > -0.004 || mVWLevel->memPlevel[i+1]/(float)mVWLevel->memTotalframes > 0.01)
			||mVWLevel->memPlevel[i]/(float)mVWLevel->memTotalframes>0.015
			)//1%
		{
			voicewholeleveldb = mVWLevel->memPavglevel[i];
			getvol = 1;
			break;
		}
	}
	if (getvol == 1&&i>0)
	{
		sum = 0;
		sumdb = 0;
		for (j=0;j<i+1;j++)
		{
			sum += mVWLevel->memPlevel[j];
			sumdb += mVWLevel->memPlevel[j]*mVWLevel->memPavglevel[j];
		}
		voicewholeleveldb = sumdb/sum;
	}

	if (voicewholeleveldb == 12345 && mVWLevel->memPlevel[i]>0.5)
	{
		voicewholeleveldb = mVWLevel->memPavglevel[i];
	}
	return voicewholeleveldb;


}
