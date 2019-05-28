
#include "../../common/functions.h"

#include "AGC_control.h"
#include <assert.h>

///*calcu SS and CS with rc info to help ec*/
//if(mAGC->FeedbackKind == 11 || mAGC->FeedbackKind == 21 || mAGC->FeedbackKind == 31 || mAGC->DyKind == 41 || mAGC->FeedbackKind == -51 || mAGC->FeedbackKind == -21)
//{
//	if (1)//get rc info from ec and calcu signalspeechlikely and capspeechlikely
//	{
//		mAGC->memSS = boost[4];
//	} 
//	else//estimate rc info and calcu signalspeechlikely and capspeechlikely
//	{
//		//
//		if (mAGC->memSS == 1)
//		{
//			SSmodECresultdB = EC_RESILT_SSMOD_DB;
//		} 
//		else
//		{
//			SSmodECresultdB = 0.0;
//		}
//
//		if (capavergex_db - (avergex_db)>EC_RESULT_DB + SSmodECresultdB)//infact we should use input's avergex_db instead of inputvboosted's
//		{
//			//signalspeechlikely = 0;
//			mAGC->memSS = 1;
//		}
//		else
//		{
//			//signalspeechlikely = 0;
//			mAGC->memSS = 0;
//		}
//		//
//		if (capavergex_db - (avergex_db)<EC_RESULT_2_DB)//infact we should use input's avergex_db instead of inputvboosted's
//		{
//			//signalspeechlikely = 0;
//			mAGC->memCS = 1;
//		}
//		else
//		{
//			//signalspeechlikely = 0;
//			mAGC->memCS = 0;
//		}
//	}
//
//	/*calcu CS and SS*/
//	if (mAGC->memSS == 1)
//	{
//		SS = 1;
//		mAGC->memSSSilenceCountDown = SS_END_PROTECTION;
//	} 
//	else
//	{
//		if (mAGC->memSSSilenceCountDown>0)
//		{
//			mAGC->memSSSilenceCountDown--;
//		}
//		if (mAGC->memSSSilenceCountDown>0)
//		{
//			SS = 1;
//		}
//		else
//		{
//			SS = 0;
//		}
//	}
//	if (mAGC->memCS == 1)
//	{
//		CS = 1;
//		mAGC->memCSSilenceCountDown = CS_END_PROTECTION;
//	} 
//	else
//	{
//		if (mAGC->memCSSilenceCountDown>0)
//		{
//			mAGC->memCSSilenceCountDown--;
//		}
//		if (mAGC->memCSSilenceCountDown>0)
//		{
//			CS = 1;
//		}
//		else
//		{
//			CS = 0;
//		}
//	}
//
//	//////////////////////////////////////////////////////////////////////////
//	if (SS == 1 /*&& CS != 1*/)
//	{
//		ES = 1;
//	} 
//	else
//	{
//		ES = 0;
//	}
//
//}
//void Estimate2(ECNLP *NLP_SIMPLEX,float capavergex_db,float avergex_db)
//{
//	float SSmodECresultdB;
//
//	if (NLP_SIMPLEX->memSS == 1)
//	{
//		SSmodECresultdB = EC_RESILT_SSMOD_DB;
//	} 
//	else
//	{
//		SSmodECresultdB = 0.0;
//	}
//
//	if (capavergex_db - (avergex_db)>EC_RESULT_DB + SSmodECresultdB)//infact we should use input's avergex_db instead of inputvboosted's
//	{
//		//signalspeechlikely = 0;
//		NLP_SIMPLEX->memSS = 1;
//	}
//	else
//	{
//		//signalspeechlikely = 0;
//		NLP_SIMPLEX->memSS = 0;
//	}
//	//
//	if (capavergex_db - (avergex_db)<EC_RESULT_2_DB)//infact we should use input's avergex_db instead of inputvboosted's
//	{
//		//signalspeechlikely = 0;
//		NLP_SIMPLEX->memCS = 1;
//	}
//	else
//	{
//		//signalspeechlikely = 0;
//		NLP_SIMPLEX->memCS = 0;
//	}
//}
//
//
//void SimplexCalcu2(ECNLP *NLP_SIMPLEX,int ecinf,int Nearendvad,float capavergex_db,float avergex_db)
//{
//	float SSmodECresultdB;
//
//	if (1)//get rc info from ec and calcu signalspeechlikely and capspeechlikely
//	{
//		NLP_SIMPLEX->memSS = ecinf;
//	} 
//	else//estimate rc info and calcu signalspeechlikely and capspeechlikely
//	{
//		Estimate(NLP_SIMPLEX,capavergex_db,avergex_db);
//	}
//
//	/*calcu CS and SS*/
//	if (NLP_SIMPLEX->memSS == 1)
//	{
//		NLP_SIMPLEX->SS = 1;
//		NLP_SIMPLEX->memSSSilenceCountDown = SS_END_PROTECTION;
//	} 
//	else
//	{
//		if (NLP_SIMPLEX->memSSSilenceCountDown>0)
//		{
//			NLP_SIMPLEX->memSSSilenceCountDown--;
//		}
//		if (NLP_SIMPLEX->memSSSilenceCountDown>0)
//		{
//			NLP_SIMPLEX->SS = 1;
//		}
//		else
//		{
//			NLP_SIMPLEX->SS = 0;
//		}
//	}
//	if (NLP_SIMPLEX->memCS == 1)
//	{
//		NLP_SIMPLEX->CS = 1;
//		NLP_SIMPLEX->memCSSilenceCountDown = CS_END_PROTECTION;
//	} 
//	else
//	{
//		if (NLP_SIMPLEX->memCSSilenceCountDown>0)
//		{
//			NLP_SIMPLEX->memCSSilenceCountDown--;
//		}
//		if (NLP_SIMPLEX->memCSSilenceCountDown>0)
//		{
//			NLP_SIMPLEX->CS = 1;
//		}
//		else
//		{
//			NLP_SIMPLEX->CS = 0;
//		}
//	}
//
//	//////////////////////////////////////////////////////////////////////////
//	if (NLP_SIMPLEX->SS == 1 /*&& NLP_SIMPLEX->CS != 1*/)
//	{
//		NLP_SIMPLEX->ES = 1;
//	} 
//	else
//	{
//		NLP_SIMPLEX->ES = 0;
//	}
//
//}

float Capavgx_db_ESMOD(int es,int fbsimplex)
{
	float capavgx_db_nlpmod;

	switch (fbsimplex)
	{
	case 0:
		capavgx_db_nlpmod = 0.0;
		break;
	case 1:
		if (es != 0)
		{
			capavgx_db_nlpmod = SS_FB_MOD_DB;
		} 
		else
		{
			capavgx_db_nlpmod = 0.0;
		}
		break;
	case 2:
		if (es != 0)
		{
			capavgx_db_nlpmod = SS_FB_MOD_DB;
		} 
		else
		{
			capavgx_db_nlpmod = NORMAL_FB_MOD_DB;
		}
		break;
	}
	return capavgx_db_nlpmod;
}







void ESDetect_Simplex(ECNLP *NLP_SIMPLEX,int ecinf,int Nearendvad)
{
	if (ecinf == 0)
	{
		NLP_SIMPLEX->memCS = Nearendvad;
	}

	if (NLP_SIMPLEX->memCS == 1)
	{
		NLP_SIMPLEX->CS = 1;
		NLP_SIMPLEX->memCSSilenceCountDown = CS_END_PROTECTION;
	} 
	else
	{
		if (NLP_SIMPLEX->memCSSilenceCountDown>0)
		{
			NLP_SIMPLEX->memCSSilenceCountDown--;
		}
		if (NLP_SIMPLEX->memCSSilenceCountDown>0)
		{
			NLP_SIMPLEX->CS = 1;
		}
		else
		{
			NLP_SIMPLEX->CS = 0;
		}
	}

	NLP_SIMPLEX->SS = ecinf;//not protect ss
		
	if (NLP_SIMPLEX->SS == 2)//
	{
		NLP_SIMPLEX->ES = NLP_SIMPLEX->SS;
	}
	else if (NLP_SIMPLEX->SS != 0 && NLP_SIMPLEX->CS != 1)
	{
		NLP_SIMPLEX->ES = NLP_SIMPLEX->SS;
	} 
	else
	{
		NLP_SIMPLEX->ES = 0;
	}

}


void SCDetect_Simplex(ECNLP *NLP_SIMPLEX,float vol,int agressive)
{
	if (NLP_SIMPLEX->memSSdown_gain < NLP_SIMPLEX->SSdown_gainmax)
	{
		NLP_SIMPLEX->ShortCircuit = 0;
	}
	else
	{
		if (agressive == 1)
		{
			if(vol - NLP_SIMPLEX->memmicvolSS > 0.15 /*|| NLP_SIMPLEX->memmicvolSS < SSVOLLOW*/)
			{
				NLP_SIMPLEX->ShortCircuit = 1;
			}
			else
			{
				NLP_SIMPLEX->ShortCircuit = 0;
			}
		}
		else
		{
			if (vol - NLP_SIMPLEX->memmicvolSS > NSS_SS_VOL/* || NLP_SIMPLEX->memmicvolSS < SSVOLLOW*/)
			{
				NLP_SIMPLEX->ShortCircuit = 1;
			}
			else
			{
				NLP_SIMPLEX->ShortCircuit = 0;
			}
		}
	}
	
}


//ES,SC:ShortCircuit
void FB_Simplex(ECNLP *NLP_SIMPLEX,int FBSimplex)
{
	NLP_SIMPLEX->capavgx_db_nlpmod = Capavgx_db_ESMOD(NLP_SIMPLEX->SS,FBSimplex);
	
	if (FBSimplex>0 && NLP_SIMPLEX->ShortCircuit == 1 )
	{
		NLP_SIMPLEX->TargetD_db_nlpmod = -TARD_SMSCMOD_DB;
		NLP_SIMPLEX->Smalldb_nlpmod = -SMALLDB + SMALL_SMSCMOD_DB;
		NLP_SIMPLEX->DownIntevalTime_ms_nlpmod = -10;
		NLP_SIMPLEX->UPIntevalTime_ms_nlpmod = 200;
	} 
	else
	{
		NLP_SIMPLEX->TargetD_db_nlpmod = 0.0;
		NLP_SIMPLEX->Smalldb_nlpmod = 0.0;
		NLP_SIMPLEX->DownIntevalTime_ms_nlpmod = 0.0;
		NLP_SIMPLEX->UPIntevalTime_ms_nlpmod = 0.0;
	}
	if (FBSimplex>0)
	{
		assert(FBSimplex == 1);
		NLP_SIMPLEX->CSA = NLP_SIMPLEX->CS;
		NLP_SIMPLEX->SSA = NLP_SIMPLEX->SS;
		NLP_SIMPLEX->ESA = NLP_SIMPLEX->ES;
	} 
	else
	{
		assert(FBSimplex == 0);
		NLP_SIMPLEX->CSA = 0;
		NLP_SIMPLEX->SSA = 0;
		NLP_SIMPLEX->ESA = 0;
	}
}







//void UPIntevalTime_msSCMOD
//if (SSdowngain == 1)
//{
//	mAGC->memmicvolSS = mAGC->memmicvol;
//}
//
//if (mAGC->memmicvolSS < SSVOLLOW)
//{
//	if (mAGC->memmicvol < 0.4)
//	{
//		mAGC->UPIntevalTime_ms = 500;
//	} 
//	else if(mAGC->memmicvol < 0.65)
//	{
//		mAGC->UPIntevalTime_ms = 550;
//	}else
//	{
//		mAGC->UPIntevalTime_ms = 660;
//	}
//	mAGC->memboost_dBlimit = mAGC->memboost_dB;//this is to say the boost position maybe high to SS,we can't let it up again.
//}
//else
//{
//	if (mAGC->memmicvol < 0.4)
//	{
//		mAGC->UPIntevalTime_ms = 200;
//	} 
//	else if(mAGC->memmicvol < 0.65)
//	{
//		mAGC->UPIntevalTime_ms = 300;
//	}else
//	{
//		mAGC->UPIntevalTime_ms = 660;
//	}
//}
//
//
//
//
//
//
//
//
//
//
//
//
//
//////////////////////////////////////////////////////////////////////////



int ESdy(Dynamic_ID *theDy,int es,float noise_db,float k)//return if curchange
{
	float limnoise_db;
	int curchange = 0;

	//	limnoise_db = THEMINOF(noise_db,NOISE_DB_MAX);//not appropriate for this condition.it must be other 
	//	limnoise_db = THEMAXOF(limnoise_db,NOISE_DB_MIN);

		
	if (es != 0)
	{
		if (es == 2)
		{
			k = 0.03;
			limnoise_db = noise_db;
			if (limnoise_db<-40)
			{
				limnoise_db = -40;
			}

			theDy->CurveOption.PLen = 1;
			theDy->CurveOption.P_db[0].x_db = limnoise_db+4;
			theDy->CurveOption.P_db[0].y_db = limnoise_db+4;
			theDy->CurveOption.b_db = theDy->CurveOption.P_db[0].y_db - theDy->CurveOption.P_db[0].x_db * k;
			curchange = 1;
		} 
		else
		{
			limnoise_db = noise_db;

			theDy->CurveOption.PLen = 1;
			theDy->CurveOption.P_db[0].x_db = limnoise_db;
			theDy->CurveOption.P_db[0].y_db = limnoise_db;
			theDy->CurveOption.b_db = theDy->CurveOption.P_db[0].y_db - theDy->CurveOption.P_db[0].x_db * k;
			curchange = 1;
		}
		
		

		//theDy->Bypass = 1;
	}
	else
	{
		//theDy->Bypass = 0;
	}

	return curchange;

}