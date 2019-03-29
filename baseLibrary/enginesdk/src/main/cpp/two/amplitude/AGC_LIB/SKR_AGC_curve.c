/*以后不能叫curve了，参数方面都在此文件...*/
#include "SKR_AGC_defines.h"
#include "AGC_control.h"
#include <stdlib.h>
#include <assert.h>
#include "../../common/functions.h"
#include "SKR_AGC_functions.h"

void UpdateCurv(Y_X_db_Curve *CurveOption,float noise_db)
{
	float limnoise_db;

	limnoise_db = THEMINOF(noise_db,NOISE_DB_MAX);
	limnoise_db = THEMAXOF(limnoise_db,NOISE_DB_MIN);

	CurveOption->P_db[0].x_db = limnoise_db-5;
	CurveOption->P_db[0].y_db = limnoise_db-5;

	CurveOption->P_db[1].x_db = limnoise_db + 0.28;
	CurveOption->P_db[1].y_db = limnoise_db + 2.69;

	CurveOption->P_db[2].x_db = (CurveOption->P_db[3].y_db - CurveOption->P_db[1].y_db + K1YOU*CurveOption->P_db[1].x_db - K3ZUO*CurveOption->P_db[3].x_db)/(K1YOU - K3ZUO);
	CurveOption->P_db[2].y_db = K1YOU*(CurveOption->P_db[2].x_db - CurveOption->P_db[1].x_db) + CurveOption->P_db[1].y_db;

}

void GainCurv(Y_X_db_Curve *CurveOption,float gain_dB)
{

	CurveOption->P_db[0].x_db = -10;
	CurveOption->P_db[0].y_db = CurveOption->P_db[0].x_db + gain_dB;
	if (CurveOption->P_db[0].y_db>CurveOption->b_db-0.5)
	{
		CurveOption->P_db[0].y_db = CurveOption->b_db-0.5;
	}
	
}

void UpdateCurv2(Y_X_db_Curve *CurveOption,float noise_db)
{
	float limnoise_db;
	float maxgain;


	
	
	limnoise_db = THEMINOF(noise_db,NOISE_DB_MAX);
	limnoise_db = THEMAXOF(limnoise_db,NOISE_DB_MIN);

	CurveOption->PLen = 4;
	CurveOption->P_db[0].x_db = limnoise_db-3;
	CurveOption->P_db[0].y_db = limnoise_db-3;

	CurveOption->P_db[1].x_db = limnoise_db + 0.43;
	CurveOption->P_db[1].y_db = limnoise_db + 1.02;

	CurveOption->P_db[3].y_db = -3.0;//>P_db[1].x_db(NOISE_DB_MAX (-22.5)+ 0.43)
	CurveOption->P_db[3].x_db = -6.0;

	CurveOption->P_db[2].x_db = (CurveOption->P_db[3].y_db - CurveOption->P_db[1].y_db + K1YOU*CurveOption->P_db[1].x_db - K3ZUO*CurveOption->P_db[3].x_db)/(K1YOU - K3ZUO);
	CurveOption->P_db[2].y_db = K1YOU*(CurveOption->P_db[2].x_db - CurveOption->P_db[1].x_db) + CurveOption->P_db[1].y_db;

	maxgain = MAXGAIN;
	if (noise_db>NOISE_TROUBLE_START)
	{
		maxgain = (MAXGAIN-MINGAIN)/(NOISE_TROUBLE_START - NOISE_TROUBLE_COUT)*noise_db + (NOISE_TROUBLE_COUT*MAXGAIN-NOISE_TROUBLE_START*MINGAIN)/(NOISE_TROUBLE_COUT-NOISE_TROUBLE_START);//连结(NOISE_TROUBLE_START,MAXGAIN),(NOISE_TROUBLE_COUT,0)
	}
	if (noise_db>NOISE_TROUBLE_COUT)
	{
		maxgain = MINGAIN;
	}
		
	if (CurveOption->P_db[2].y_db >CurveOption->P_db[2].x_db + maxgain)
	{
		CurveOption->P_db[2].y_db = CurveOption->P_db[2].x_db + maxgain;
	}

	//if (noise_db>-40.0)
	//{
	//	CurveOption->PLen = 1;
	//	CurveOption->P_db[0].x_db = -10.0;//v1:-28.0;//v2beta:-26.5
	//	CurveOption->P_db[0].y_db = -10.0;//v1:-19.0;//v2beta:-15.5
	//	//CurveOption->k = 1.02;//v1:1.15;//v2beta:1.17
	//	//mAGC->DyForAGC.CurveOption.b_db = -0.8;//v2beta:-1.2	
	//	//CurveOption->b_db = -2.4;//v2beta:-1.2

	//}
	CurveOption->b_db = CURV_B;//v2beta:-1.2  //because UpdateCurv_ES will change this so we must reset it

}


void UpdateCurv3(Y_X_db_Curve *CurveOption,float noise_db,float avg_db,int VAD,int BubbleGate)
{
	float limnoise_db;
	float maxgain;
	float noiseloss;

	if (BubbleGate == DY_VARIBLECUR_BUBBLE)
	{
		noiseloss = 20;
	}
	else if(BubbleGate == 0)
	{
		noiseloss = 0;
	}
	else
	{
		noiseloss = 0;
		assert(0);
	}


	limnoise_db = THEMINOF(noise_db,NOISE_DB_MAX-noiseloss);
	limnoise_db = THEMAXOF(limnoise_db,NOISE_DB_MIN-noiseloss);

	CurveOption->PLen = 4;

	if (VAD == 1)
	{
		//CurveOption->P_db[0].x_db = limnoise_db-1;
		//CurveOption->P_db[0].y_db = limnoise_db-1;
		//CurveOption->P_db[1].x_db = limnoise_db + 2.43;
		//CurveOption->P_db[1].y_db = limnoise_db + 4.02;

		CurveOption->P_db[0].x_db = limnoise_db-2/*+noiseloss*/;
		CurveOption->P_db[0].y_db = limnoise_db-2/*+noiseloss*/;

		CurveOption->P_db[1].x_db = limnoise_db/*+noiseloss*/ + 1.43;
		CurveOption->P_db[1].y_db = limnoise_db/*+noiseloss*/ + 2.72;

		CurveOption->P_db[3].y_db = -3.0;//>P_db[1].x_db(NOISE_DB_MAX (-22.5)+ 0.43)
		CurveOption->P_db[3].x_db = -6.0;

		maxgain = MAXGAIN;

		CurveOption->k = 1.0;
	} 
	else
	{
		CurveOption->P_db[0].x_db = limnoise_db + 3;
		CurveOption->P_db[0].y_db = limnoise_db + 3;

		CurveOption->P_db[1].x_db = limnoise_db + 9.43;
		CurveOption->P_db[1].y_db = limnoise_db + 11.02;

		CurveOption->P_db[3].y_db = -10.0;//>P_db[1].x_db(NOISE_DB_MAX (-22.5)+ 0.43)
		CurveOption->P_db[3].x_db = -10.0;

		maxgain = MAXGAIN/4.0;
		CurveOption->k = 1.5;
	}



	CurveOption->P_db[2].x_db = (CurveOption->P_db[3].y_db - CurveOption->P_db[1].y_db + K1YOU*CurveOption->P_db[1].x_db - K3ZUO*CurveOption->P_db[3].x_db)/(K1YOU - K3ZUO);
	CurveOption->P_db[2].y_db = K1YOU*(CurveOption->P_db[2].x_db - CurveOption->P_db[1].x_db) + CurveOption->P_db[1].y_db;

	if (BubbleGate == DY_VARIBLECUR_BUBBLE)
	{
		CurveOption->P_db[1].x_db = THEMINOF(limnoise_db+noiseloss + 1.43,CurveOption->P_db[2].x_db-3);
		CurveOption->P_db[1].y_db = THEMINOF(limnoise_db+noiseloss + 2.72,CurveOption->P_db[2].x_db-2);

		CurveOption->P_db[0].x_db = THEMINOF(limnoise_db-2+noiseloss,CurveOption->P_db[1].x_db-0.001);
		CurveOption->P_db[0].y_db = THEMINOF(limnoise_db-2+noiseloss,CurveOption->P_db[1].x_db-0.001);
	}
	
	
	if (noise_db>NOISE_TROUBLE_START)
	{
		maxgain = (MAXGAIN-MINGAIN)/(NOISE_TROUBLE_START - NOISE_TROUBLE_COUT)*noise_db + (NOISE_TROUBLE_COUT*MAXGAIN-NOISE_TROUBLE_START*MINGAIN)/(NOISE_TROUBLE_COUT-NOISE_TROUBLE_START);//连结(NOISE_TROUBLE_START,MAXGAIN),(NOISE_TROUBLE_COUT,0)
	}
	if (noise_db>NOISE_TROUBLE_COUT)
	{
		maxgain = MINGAIN;
	}

	if (CurveOption->P_db[2].y_db >CurveOption->P_db[2].x_db + maxgain)
	{
		CurveOption->P_db[2].y_db = CurveOption->P_db[2].x_db + maxgain;
	}

	//if (noise_db>-40.0)
	//{
	//	CurveOption->PLen = 1;
	//	CurveOption->P_db[0].x_db = -10.0;//v1:-28.0;//v2beta:-26.5
	//	CurveOption->P_db[0].y_db = -10.0;//v1:-19.0;//v2beta:-15.5
	//	//CurveOption->k = 1.02;//v1:1.15;//v2beta:1.17
	//	//mAGC->DyForAGC.CurveOption.b_db = -0.8;//v2beta:-1.2	
	//	//CurveOption->b_db = -2.4;//v2beta:-1.2

	//}
	CurveOption->b_db = CURV_B;//v2beta:-1.2  //because UpdateCurv_ES will change this so we must reset it

}



void GainSpeechCurv(Y_X_db_Curve *CurveOption,float noise_db,float avg_db,int somein)
{
	if (avg_db-noise_db>9/*&&noise_db<-40.0*/)
	{
		CurveOption->P_db[0].x_db = avg_db;
		CurveOption->P_db[0].y_db = -20;
		if (CurveOption->P_db[0].y_db >CurveOption->P_db[0].x_db + 10.5)
		{
			CurveOption->P_db[0].y_db = CurveOption->P_db[0].x_db + 10.5;
		}
	}	 
	else
	{
		CurveOption->P_db[0].x_db = -12;
		CurveOption->P_db[0].y_db = -12;

	}
	//CurveOption->k = 1.0;
	//CurveOption->b_db = -2.5;
	//CurveOption->PLen = 1;

}

void StaticCurvReset(Y_X_db_Curve *CurveOption,int kind)
{
	if (kind & DY_STATIC_RADIOLIMIT)
	{
		CurveOption->PLen = 1;
		CurveOption->P_db[0].x_db = -15.0;//v1:-28.0;//v2beta:-26.5
		CurveOption->P_db[0].y_db = -3.1;//v1:-19.0;//v2beta:-15.5
		CurveOption->b_db = CURV_B;//v2beta:-1.2
		CurveOption->k = 1;
	}//add else if..
	else
	{
		CurveOption->PLen = 1;
		CurveOption->P_db[0].x_db = -1.8;//v1:-28.0;//v2beta:-26.5
		CurveOption->P_db[0].y_db = -1.8;//v1:-19.0;//v2beta:-15.5
		CurveOption->b_db = 0;//v2beta:-1.2
		CurveOption->k = 1;
	}

}

int UpdateDy(Dynamic_ID *theDy,int dykind,int es,float noise_db,float avg_db,int vadresult,int babble/*,int samplerate*/)//dy:split?variablecur?nlp?return if need split
{
	int curchange = 0;

	if (dykind & DY_VARIBLECUR)
	{
		//UpdateCurv2(&theDy->CurveOption,noise_db);//point,b
		UpdateCurv3(&theDy->CurveOption,noise_db,avg_db,vadresult,babble);
		curchange = 1;
	} 
	else if(dykind & DY_NLP)//如果不是变曲线，但是被nlp刷了的话要重置
	{
		StaticCurvReset(&theDy->CurveOption,dykind);//point,b
		curchange = 1;
	}

	if (dykind & DY_NLP)
	{

		curchange += ESdy(theDy,es,noise_db,1.0/*ES_CMP*/);//point,b

	}
	
	if (curchange != 0)
	{
		DynamicCalcu_API(theDy);
	}

	return curchange;//(dykind & DY_SPLIT) && (samplerate>12000);
}