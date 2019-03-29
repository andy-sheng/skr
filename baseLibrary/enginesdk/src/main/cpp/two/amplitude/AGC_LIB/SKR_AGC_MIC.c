#include "SKR_AGC_defines.h"
#include "../../common/functions.h"
#include "AGC_control.h"
#include "SKR_AGC_typedefs.h"
#include <math.h>
#include <assert.h>

void SetMicProperty(THE_MIC *amic,int fbkind,float *boostinfo,/*,int vol*/float vvolmaxdB,float vvolmindB)
{
	//amic->rvol = vol/65535.0;
	amic->rvolmin = LIMVOL;
	amic->rvolmax = 1.0;
	//amic->rboost_dB = boostinfo[0];
	amic->rboostmin_dB = boostinfo[1];
	amic->rboostmax_dB = boostinfo[2];
	amic->rstep_dB = boostinfo[3];

	amic->capability = 0;

	switch(fbkind)
	{
	case RMIC_NORMAL://1:AAGC(normally,real boost, no vboost);
		amic->capability &= ~CAPABILITY_TWOMIC;
		amic->capability &= ~CAPABILITY_VVOL;
		amic->capability &= ~CAPABILITY_VBOOST;
		amic->capability |= CAPABILITY_RVOL;

		if (amic->rstep_dB > 0.0)//w7boost
		{
			amic->capability |= CAPABILITY_RBOOST;
			amic->capability |= CAPABILITY_RSUBLEVEL;
			amic->capability |= CAPABILITY_RALLOWUP;
		}
		else if(boostinfo[0] > -0.5)//xpboost
		{
			amic->capability |= CAPABILITY_RBOOST;
			amic->capability &= ~CAPABILITY_RSUBLEVEL;
			amic->capability |= CAPABILITY_RALLOWUP;
		}
		else//noboost
		{
			amic->capability &= ~CAPABILITY_RBOOST;
			amic->capability &= ~CAPABILITY_RSUBLEVEL;
			amic->capability &= ~CAPABILITY_RALLOWUP;
		}
		break;
	case RMIC_XPBOOST_USEVBOOST://VAGC(The only difference with kind1 is xp's boost use vmic's boost,it doesn't change xp's boost position)
		amic->capability &= ~CAPABILITY_VVOL;
		amic->capability |= CAPABILITY_RVOL;

		if (amic->rstep_dB > 0.0)//w7boost
		{
			amic->capability &= ~CAPABILITY_VBOOST;
			amic->capability |= CAPABILITY_RBOOST;
			amic->capability |= CAPABILITY_RSUBLEVEL;
			amic->capability &= ~CAPABILITY_TWOMIC;
			amic->capability |= CAPABILITY_RALLOWUP;
		}
		else if(boostinfo[0] > -0.5)//xpboost
		{
			amic->capability |= CAPABILITY_VBOOST;
			amic->capability &= ~CAPABILITY_RBOOST;
			amic->capability &= ~CAPABILITY_RSUBLEVEL;
			amic->capability |= CAPABILITY_TWOMIC;
			amic->capability |= CAPABILITY_RALLOWUP;
		}
		else//noboost
		{
			amic->capability &= ~CAPABILITY_VBOOST;
			amic->capability &= ~CAPABILITY_RBOOST;
			amic->capability &= ~CAPABILITY_RSUBLEVEL;
			amic->capability &= ~CAPABILITY_TWOMIC;
			amic->capability &= ~CAPABILITY_RALLOWUP;
		}
		break;
	case NORMIC_NOTUSEVBOOST://mobile not use vboost
		amic->capability &= ~CAPABILITY_TWOMIC;
		amic->capability |= CAPABILITY_VVOL;
		amic->capability &= ~CAPABILITY_VBOOST;
		amic->capability &= ~CAPABILITY_RVOL;
		amic->capability &= ~CAPABILITY_RALLOWUP;
		amic->capability &= ~CAPABILITY_RBOOST;
		amic->capability &= ~CAPABILITY_RSUBLEVEL;
		break;

	case NORMIC_ALLUSE://mobile use vboost and vvol
		amic->capability &= ~CAPABILITY_TWOMIC;
		amic->capability |= CAPABILITY_VVOL;
		amic->capability |= CAPABILITY_VBOOST;
		amic->capability &= ~CAPABILITY_RVOL;
		amic->capability &= ~CAPABILITY_RALLOWUP;
		amic->capability &= ~CAPABILITY_RBOOST;
		amic->capability &= ~CAPABILITY_RSUBLEVEL;
        break;

	//not finished
	case RMIC_XPBOOST_USEVBOOST_NOBOOSTUP://(The only difference with kind2 is that kind3 allows boostdown,doesn't allow boostup)//this type need two boost output
		amic->capability &= ~CAPABILITY_VVOL;
		amic->capability |= CAPABILITY_RVOL;

		if (amic->rstep_dB > 0.0)//w7boost
		{
			amic->capability &= ~CAPABILITY_VBOOST;
			amic->capability |= CAPABILITY_RBOOST;
			amic->capability |= CAPABILITY_RSUBLEVEL;
			amic->capability &= ~CAPABILITY_TWOMIC;
			amic->capability |= CAPABILITY_RALLOWUP;
		}
		else if(boostinfo[0] > -0.5)//xpboost
		{
			amic->capability |= CAPABILITY_VBOOST;
			amic->capability &= ~CAPABILITY_RBOOST;
			amic->capability &= ~CAPABILITY_RSUBLEVEL;
			amic->capability |= CAPABILITY_TWOMIC;
			amic->capability &= ~CAPABILITY_RALLOWUP;
		}
		else//noboost
		{
			amic->capability &= ~CAPABILITY_VBOOST;
			amic->capability &= ~CAPABILITY_RBOOST;
			amic->capability &= ~CAPABILITY_RSUBLEVEL;
			amic->capability |= CAPABILITY_TWOMIC;
			amic->capability &= ~CAPABILITY_RALLOWUP;
		}
		break;
	default:
		amic->capability = 0;
	}

	if (amic->capability & CAPABILITY_VBOOST)
	{
		//amic->vvol = 1.0;
		amic->vvolmin = LIMVOL;
		amic->vvolmax = 1.0;
	}
	else
	{
		assert(vvolmindB<=vvolmaxdB);
		assert(vvolmindB>=VVOLMIN_DB&&vvolmindB<=VVOLMAX_DB);
		assert(vvolmaxdB>=VVOLMIN_DB&&vvolmaxdB<=VVOLMAX_DB);

		if (vvolmindB<VVOLMIN_DB)
		{
			vvolmindB = VVOLMIN_DB;
		}
		if (vvolmaxdB>VVOLMAX_DB)
		{
			vvolmaxdB = VVOLMAX_DB;
		}
		if (vvolmindB>vvolmaxdB)
		{
			vvolmindB = vvolmaxdB;
		}

		amic->vvolmin = idB(vvolmindB);
		amic->vvolmax = idB(vvolmaxdB);
		
	}
	//amic->vboost_dB = 0.0;
	amic->vboostmin_dB = VBOOST_MIN_DB;
	amic->vboostmax_dB = VBOOST_MAX_DB;
	amic->vstep_dB = VBOOST_STEP_DB;
}

void MicChange(THE_MIC *amic,float *boostinfo/*,int vol*/)
{

}

void BoostChangeFeedbackParameter(AGC_ID *mAGC,float boostpostion)
{
	if (boostpostion>=0.6)
	{
		mAGC->N20ms_ForDown = 420;//#define N_Down 200
		//mAGC->N20ms_ForUP = 240;//#define N_UP 130
		mAGC->SmallVol_Threshold = 0.63;//#define K 0.6
		mAGC->N20msSmallVol_Threshold = 50;//#define M 130
		mAGC->N20msNegMod_Threshold = 3;//#define F 20 
		mAGC->N20msSatu_Threshold = 5;//#define X 30
		mAGC->UpSpeed_dB = 0.73;
		mAGC->DownSpeed_dB = 1.08;
	} 
	else if(boostpostion<=0.6&&boostpostion>=0.3)
	{
		mAGC->N20ms_ForDown = 400;//#define N_Down 200
		//mAGC->N20ms_ForUP = 240;//#define N_UP 130
		mAGC->SmallVol_Threshold = 0.58;//#define K 0.6
		mAGC->N20msSmallVol_Threshold = 60;//#define M 130
		mAGC->N20msNegMod_Threshold = 3;//#define F 20 
		mAGC->N20msSatu_Threshold = 5;//#define X 30
		mAGC->UpSpeed_dB = 0.77;
		mAGC->DownSpeed_dB = 0.93;

	}else
	{
		mAGC->N20ms_ForDown = 400;//#define N_Down 200
		//mAGC->N20ms_ForUP = 240;//#define N_UP 130
		mAGC->SmallVol_Threshold = 0.4;//#define K 0.6
		mAGC->N20msSmallVol_Threshold = 65;//#define M 130
		mAGC->N20msNegMod_Threshold = 4;//#define F 20 
		mAGC->N20msSatu_Threshold = 4;//#define X 30


	}

}


//void VolChangeFeedbackParameter(AGC_ID *mAGC,float vol)//不能改她，否则boost会有影响，boost
//{
//	if (vol < 0.4)
//	{
//		mAGC->UPIntevalTime_ms = 200;
//	} 
//	else if(vol < 0.65)
//	{
//		mAGC->UPIntevalTime_ms = 300;
//	}else
//	{
//		mAGC->UPIntevalTime_ms = 660;
//	}
//
//}


float GainMicMod_fast(THE_MIC *amic,float boost_dB,float vol,float Gain)
{
	float gainmod = 1.0;

	if (Gain == 1.0)
	{
		return gainmod;
	}

	if (amic->capability & CAPABILITY_RSUBLEVEL)//w7boost
	{
		if (boost_dB/(amic->rboostmax_dB-amic->rboostmin_dB)<0.2)
		{
			if (Gain>1.001)
			{
				gainmod*=1.25;
			}
			if (Gain>1.001 && vol<0.50)
			{
				gainmod*=1.3;
			}
		} 
		else if(boost_dB/(amic->rboostmax_dB-amic->rboostmin_dB)<0.5)
		{
			if (Gain>1.001)
			{
				gainmod*=1.1;
			}
			if (Gain>1.001 && vol<0.35)//if (Gain>1.001&&boost[2]<9000)
			{
				gainmod*=1.2;
			}
		}
		else
		{
			if (Gain>1.001 && vol<0.2)//if (Gain>1.001&&boost[2]<9000)
			{
				gainmod*=1.4;
			}

		}

	}
	else if (amic->capability & CAPABILITY_VBOOST)//vboost vol
	{
		if (boost_dB/(amic->vboostmax_dB-amic->vboostmin_dB)<0.2)
		{
			if (Gain>1.001)
			{
				gainmod*=1.25;
			}
			if (Gain>1.001 && vol<0.50)
			{
				gainmod*=1.3;
			}
		} 
		else if(boost_dB/(amic->vboostmax_dB-amic->vboostmin_dB)<0.5)
		{
			if (Gain>1.001)
			{
				gainmod*=1.1;
			}
			if (Gain>1.001 && vol<0.35)//if (Gain>1.001&&boost[2]<9000)
			{
				gainmod*=1.2;
			}
		}
		else
		{
			if (Gain>1.001 && vol<0.2)//if (Gain>1.001&&boost[2]<9000)
			{
				gainmod*=1.4;
			}

		}
	}
	else if ((amic->capability & CAPABILITY_RBOOST) && (amic->capability & CAPABILITY_RSUBLEVEL) == 0 )//xpboost
	{
		if (boost_dB>0.5)
		{
			if (Gain>1.001 && vol<0.45)//if (Gain>1.001&&boost[2]<9000)
			{
				gainmod*=1.05;
			}
			if (Gain>1.001 && vol<0.25)//if (Gain>1.001&&boost[2]<9000)
			{
				gainmod*=1.2;
			}
		} 
		else
		{
			if (Gain>1.001 && vol<0.45)//if (Gain>1.001&&boost[2]<9000)
			{
				gainmod*=1.2;
			}
			if (Gain>1.001 && vol<0.25)//if (Gain>1.001&&boost[2]<9000)
			{
				gainmod*=1.4;
			}
		}
	}
	else if ((amic->capability & CAPABILITY_VBOOST) == 0 && (amic->capability & CAPABILITY_RBOOST) == 0)//noboost
	{
		if (Gain>1.001 && vol<0.4)//if (Gain>1.001&&boost[2]<9000)
		{
			gainmod*=1.3;
		}
		if (Gain>1.001 && vol<0.25)//if (Gain>1.001&&boost[2]<9000)
		{
			gainmod*=1.4;
		}
	}
	return gainmod;
}
float GainMicMod_mid(THE_MIC *amic,float boost_dB,float vol,float Gain)
{
	float gainmod = 1.0;

	if (Gain == 1.0)
	{
		return gainmod;
	}

	if (amic->capability & CAPABILITY_RSUBLEVEL)//w7boost
	{
		if (boost_dB/(amic->rboostmax_dB-amic->rboostmin_dB)<0.2)
		{
			//if (Gain>1.001)
			//{
			//	//gainmod*=1.25;
			//	gainmod*=1.1;
			//}
			if (Gain>1.001 && vol<0.45)
			{
				//gainmod*=1.3;
				gainmod*=1.08;
			}
		} 
		else if(boost_dB/(amic->rboostmax_dB-amic->rboostmin_dB)<0.5)
		{
			//if (Gain>1.001)
			//{
			//	//gainmod*=1.1;
			//	gainmod*=1.05;
			//}
			if (Gain>1.001 && vol<0.35)//if (Gain>1.001&&boost[2]<9000)
			{
				//gainmod*=1.2;
				gainmod*=1.05;
			}
		}
		else
		{
			if (Gain>1.001 && vol<0.2)//if (Gain>1.001&&boost[2]<9000)
			{
				//gainmod*=1.4;
				gainmod*=1.13;
			}

		}

	}
	else if (amic->capability & CAPABILITY_VBOOST)//vboost vol
	{
		if (boost_dB/(amic->vboostmax_dB-amic->vboostmin_dB)<0.2)
		{
			//if (Gain>1.001)
			//{
			//	//gainmod*=1.25;
			//	gainmod*=1.1;
			//}
			if (Gain>1.001 && vol<0.50)
			{
				//gainmod*=1.3;
				gainmod*=1.05;
			}
		} 
		else if(boost_dB/(amic->vboostmax_dB-amic->vboostmin_dB)<0.5)
		{
			//if (Gain>1.001)
			//{
			//	//gainmod*=1.1;
			//	gainmod*=1.05;
			//}
			if (Gain>1.001 && vol<0.35)//if (Gain>1.001&&boost[2]<9000)
			{
				//gainmod*=1.2;
				gainmod*=1.05;
			}
		}
		else
		{
			if (Gain>1.001 && vol<0.2)//if (Gain>1.001&&boost[2]<9000)
			{
				//gainmod*=1.4;
				gainmod*=1.2;
			}

		}
	}
	else if ((amic->capability & CAPABILITY_RBOOST) && (amic->capability & CAPABILITY_RSUBLEVEL) == 0 )//xpboost
	{
		if (boost_dB>0.5)
		{
			if (Gain>1.001 && vol<0.45)//if (Gain>1.001&&boost[2]<9000)
			{
				gainmod*=1.05;
			}
			if (Gain>1.001 && vol<0.25)//if (Gain>1.001&&boost[2]<9000)
			{
				//gainmod*=1.2;
				gainmod*=1.1;
			}
		} 
		else
		{
			if (Gain>1.001 && vol<0.45)//if (Gain>1.001&&boost[2]<9000)
			{
				//gainmod*=1.2;
				gainmod*=1.1;
			}
			if (Gain>1.001 && vol<0.25)//if (Gain>1.001&&boost[2]<9000)
			{
				//gainmod*=1.4;
				gainmod*=1.05;
			}
		}
	}
	else if ((amic->capability & CAPABILITY_VBOOST) == 0 && (amic->capability & CAPABILITY_RBOOST) == 0 && amic->capability & CAPABILITY_RVOL)//noboost and rvol:we don't mod vvol
	{
		if (Gain>1.001 && vol<0.4)//if (Gain>1.001&&boost[2]<9000)
		{
			//gainmod*=1.3;
			gainmod*=1.15;
		}
		if (Gain>1.001 && vol<0.25)//if (Gain>1.001&&boost[2]<9000)
		{
			//gainmod*=1.4;
			gainmod*=1.1;
		}
		if (Gain>1.001 && vol<0.10)//if (Gain>1.001&&boost[2]<9000)
		{
			//gainmod*=1.4;
			gainmod*=1.1;
		}
	}
	return gainmod;
}
float GainMicMod_slow(THE_MIC *amic,float boost_dB,float vol,float Gain)
{
	float gainmod = 1.0;

	if (Gain == 1.0)
	{
		return gainmod;
	}

	if (amic->capability & CAPABILITY_RSUBLEVEL)//w7boost
	{
		if (boost_dB/(amic->rboostmax_dB-amic->rboostmin_dB)<0.2)
		{
			//if (Gain>1.001 && vol<0.70)
			//{
				//gainmod*=1.25;
			//	gainmod*=1.1;
			//}
			if (Gain>1.001 && vol<0.40)
			{
				//gainmod*=1.3;
				gainmod*=1.05;
			}
		} 
		else if(boost_dB/(amic->rboostmax_dB-amic->rboostmin_dB)<0.5)
		{
			//if (Gain>1.001 && vol<0.70)
			//{
				//gainmod*=1.1;
				//gainmod*=1.05;
			//}
			if (Gain>1.001 && vol<0.35)//if (Gain>1.001&&boost[2]<9000)
			{
				//gainmod*=1.2;
				gainmod*=1.05;
			}
		}
		else
		{
			if (Gain>1.001 && vol<0.2)//if (Gain>1.001&&boost[2]<9000)
			{
				//gainmod*=1.4;
				gainmod*=1.05;
			}

		}

	}
	else if (amic->capability & CAPABILITY_VBOOST)//vboost vol
	{
		if (boost_dB/(amic->vboostmax_dB-amic->vboostmin_dB)<0.2)
		{
			//if (Gain>1.001 && vol<0.70)
			//{
			//	//gainmod*=1.25;
			//	gainmod*=1.1;
			//}
			if (Gain>1.001 && vol<0.50)
			{
				//gainmod*=1.3;
				gainmod*=1.05;
			}
		} 
		else if(boost_dB/(amic->vboostmax_dB-amic->vboostmin_dB)<0.5)
		{
			//if (Gain>1.001 && vol<0.70)
			//{
			//	//gainmod*=1.1;
			//	gainmod*=1.05;
			//}
			if (Gain>1.001 && vol<0.35)//if (Gain>1.001&&boost[2]<9000)
			{
				//gainmod*=1.2;
				gainmod*=1.005;
			}
		}
		else
		{
			if (Gain>1.001 && vol<0.2)//if (Gain>1.001&&boost[2]<9000)
			{
				//gainmod*=1.4;
				gainmod*=1.05;
			}

		}
	}
	else if ((amic->capability & CAPABILITY_RBOOST) && (amic->capability & CAPABILITY_RSUBLEVEL) == 0 )//xpboost
	{
		if (boost_dB>0.5)
		{
			if (Gain>1.001 && vol<0.45)//if (Gain>1.001&&boost[2]<9000)
			{
				gainmod*=1.05;
			}
			if (Gain>1.001 && vol<0.25)//if (Gain>1.001&&boost[2]<9000)
			{
				//gainmod*=1.2;
				gainmod*=1.1;
			}
		} 
		else
		{
			if (Gain>1.001 && vol<0.45)//if (Gain>1.001&&boost[2]<9000)
			{
				//gainmod*=1.2;
				gainmod*=1.1;
			}
			if (Gain>1.001 && vol<0.25)//if (Gain>1.001&&boost[2]<9000)
			{
				//gainmod*=1.4;
				gainmod*=1.05;
			}
		}
	}
	else if ((amic->capability & CAPABILITY_VBOOST) == 0 && (amic->capability & CAPABILITY_RBOOST) == 0 && amic->capability & CAPABILITY_RVOL)//noboost
	{
		//if (Gain>1.001 && vol<0.4)//if (Gain>1.001&&boost[2]<9000)
		//{
			//gainmod*=1.3;
			//gainmod*=1.15;
		//}
		if (Gain>1.001 && vol<0.25)//if (Gain>1.001&&boost[2]<9000)
		{
			//gainmod*=1.4;
			gainmod*=1.05;
		}
	}
	return gainmod;
}








float boostdB(THE_MIC *amic,int boostmove,float boostpostion_dB,int *boost,int *vboost,float boost_dBlimit)
{
	if (boostmove == 0)
	{
		boost[0] = vboost[0] = 0;
	} 
	else
	{
		///////////////////////////////boost///////////////////////////////////////////
		
		boost[0] = boostmove;
		if (amic->capability & CAPABILITY_RSUBLEVEL)//w7  
		{
			if (boostmove > 0)
			{
				if(boostpostion_dB + 1>THEMINOF(amic->rboostmax_dB,boost_dBlimit) || (amic->capability & CAPABILITY_RALLOWUP) == 0 )
				{
					boost[0] = 0;//may be we need digital boostup...
				}
			} 
			else
			{
				if(boostpostion_dB - 1<amic->rboostmin_dB)
				{
					boost[0] = 0;//may be we need digital boostdown...
				}
			}
		}
		else if(amic->capability & CAPABILITY_RBOOST)//xp 
		{
			if (boostmove > 0)
			{
				if(boostpostion_dB > THEMINOF(0.5,boost_dBlimit)|| (amic->capability & CAPABILITY_RALLOWUP) == 0)
				{
					boost[0] = 0;//may be we need digital boostup...
				}
			} 
			else
			{
				if(boostpostion_dB < 0.5)
				{
					boost[0] = 0;//may be we need digital boostdown...
				}
			}

		}
		else// no boost,but if WinIn.cpp is old AGC boost will be no use
		{
			boost[0] = 0;
		}
		////////////////////////////////vboost//////////////////////////////////////////
		vboost[0] = boostmove;
		if (amic->capability & CAPABILITY_VBOOST)//vboost  
		{
			if (boostmove > 0)
			{
				if(amic->vboost_dB + 1>THEMINOF(amic->vboostmax_dB,boost_dBlimit))
				{
					vboost[0] = 0;//may be we need digital boostup...
				}
			} 
			else
			{
				if(amic->vboost_dB - 1<amic->vboostmin_dB)
				{
					vboost[0] = 0;//may be we need digital boostdown...
				}
			}
		}
		else
		{
			vboost[0] = 0;
		}
	}
	amic->vboost_dB += vboost[0]*amic->vstep_dB;

	return boost[0]*fabs(amic->rstep_dB)+vboost[0]*amic->vstep_dB;
}



float volSmoothgain(float boostdB)
{
	float boostdBmod;

	if (boostdB>0)
	{
		boostdBmod = boostdB + 4;
	}
	else
	{
		boostdBmod = boostdB;
	}

	if (fabs(boostdB) <1.1)
	{
		return idB(-boostdB * 25);
	}
	else
	{
		return idB(-boostdBmod);//
	}
}


void MicAdjust(THE_MIC *amic,int *fbinfo,int boost,int vboost,float *memvol)
{
	int fbinfotmp = 65535;

	if (amic->capability & CAPABILITY_RVOL)
	{
		if (memvol[0] > amic->rvolmax )//boost[2] = 0xFFFF;
		{
			memvol[0] = amic->rvolmax;//1.0如果被当成1.00000000043。。。//注意，当内部计算同步时，不能因为“内部”认为的vol到顶儿了，就不去通知外面set，因为很可能这个顶儿是错的，如果外面停在23%就不会上调了。这将是严重的错误。//如果是实时获取，判断过到顶儿可以通过gain_dB置0，不通知外部..但是boost还要看这个量！
		}
		if (memvol[0] < amic->rvolmin)//if (boost[2] < 650*3)
		{
			memvol[0] = amic->rvolmin;//so..
		}
		fbinfo[2] = (int)(memvol[0]*65535);
		amic->rvol = fbinfo[2];
	} 
	if (amic->capability & CAPABILITY_RBOOST)
	{
		fbinfo[0] = boost;
	}
	
	if (amic->capability & CAPABILITY_VVOL)
	{
		if (memvol[0] > amic->vvolmax )//boost[2] = 0xFFFF;
		{
			memvol[0] = amic->vvolmax;//1.0如果被当成1.00000000043。。。//注意，当内部计算同步时，不能因为“内部”认为的vol到顶儿了，就不去通知外面set，因为很可能这个顶儿是错的，如果外面停在23%就不会上调了。这将是严重的错误。//如果是实时获取，判断过到顶儿可以通过gain_dB置0，不通知外部..但是boost还要看这个量！
		}
		if (memvol[0] < amic->vvolmin)//if (boost[2] < 650*3)
		{
			memvol[0] = amic->vvolmin;//so..
		}
		amic->vvol = fbinfotmp = (int)(fbinfotmp * memvol[0]);
	}

	if (amic->capability & CAPABILITY_VBOOST)
	{
		//fbinfotmp = (int)(fbinfotmp * idB(amic->vstep_dB) * vboost);
		fbinfotmp = (int)(fbinfotmp * idB(amic->vboost_dB));
	}

	if ((amic->capability & CAPABILITY_VVOL) || (amic->capability & CAPABILITY_VBOOST))
	{
		fbinfo[0] = fbinfotmp;
	}
}

//void CheckBoostVeryWeak(THE_MIC *amic,int boost,int VeryWeak)
//{
//
//}
//
//if (*boost>0)
//{
//	mAGC->memboostupdevicenormal = 0;//after boostup we must recheck if device can be work normally
//}
//
//if (avergex_db<-80.5)//极弱输入的处理
//{
//	//if (mAGC->memmicvol<0.6)// if memmicvol is >0.6 and avergex_db is still also so low that must be inputvboosted is quiet mute,not low vol caused
//	//{			
//	//	Gain = idB(mAGC->UpSpeed_dB);
//	//	*gainmod_dB = 2*mAGC->UpSpeed_dB;
//	//}
//	if(mAGC->memboostupdevicenormal<30)//memboostupdevicenormal once >0.6s we believe this device can be work normally at this boost position,and we don't check allzero problem before next boostup
//	{
//		if (gainmod_dB[4]>0)//w7  
//		{
//			if(mAGC->memboost_dB/(gainmod_dB[3]-gainmod_dB[2])>0.8)//0.3->0.8
//			{
//				mAGC->memLongAllZero ++;
//			}
//		}
//		else if(gainmod_dB[1]>-0.5)//xp 
//		{
//			if(mAGC->memboost_dB > 0.5)
//			{
//				mAGC->memLongAllZero ++;
//			}
//		}
//
//		if (mAGC->memLongAllZero>20)
//		{
//			mAGC->memLongAllZero = 0;
//			boostmove = -1;//boost may cause "allzero" inputvboosted
//			if (gainmod_dB[4]>0)//w7  
//			{
//				mAGC->memboost_dBlimit = mAGC->memboost_dB - gainmod_dB[4];
//			}
//			else if(gainmod_dB[1]>-0.5)//xp 
//			{
//				mAGC->memboost_dBlimit = 0.0;
//			}
//			//if this boost position caused  "allzero" inputvboosted,we never boost up to this position again
//		}
//	}
//
//}
//else
//{
//	mAGC->memLongAllZero = 0;
//	if (mAGC->memboostupdevicenormal<35)
//	{
//		mAGC->memboostupdevicenormal++;
//	}
//}
