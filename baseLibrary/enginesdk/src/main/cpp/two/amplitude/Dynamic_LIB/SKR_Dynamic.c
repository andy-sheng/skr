#include "Dynamic_control.h"
#include "SKR_Dynamic_functions.h"
#include <stdlib.h>
#include "SKR_Dynamic_typedefs.h"
#include <assert.h>


void DynamicReset_API(Dynamic_ID *mDynamic)
{
	mDynamic->memgain = 1.0;
	mDynamic->memgain_d = 1.0;
}


void DynamicCalcu_API(Dynamic_ID *mDynamic)
{
	int i;
		
	/*把输出输入电平曲线映射为目标增益曲线*/
	mDynamic->Plen = mDynamic->CurveOption.PLen;
	for (i = 0;i < mDynamic->CurveOption.PLen; i++)
	{
		mDynamic->P_x_db[i]=mDynamic->CurveOption.P_db[i].x_db;
	}
	mDynamic->k[0] = mDynamic->CurveOption.k - 1;
	mDynamic->b_db[0] = mDynamic->CurveOption.P_db[0].y_db - mDynamic->CurveOption.k*mDynamic->CurveOption.P_db[0].x_db;
	for (i = 1;i < mDynamic->CurveOption.PLen;i++)
	{
		mDynamic->k[i] = (mDynamic->CurveOption.P_db[i].y_db - mDynamic->CurveOption.P_db[i-1].y_db)/(mDynamic->CurveOption.P_db[i].x_db - mDynamic->CurveOption.P_db[i-1].x_db) - 1;
		mDynamic->b_db[i] = (mDynamic->CurveOption.P_db[i].x_db * mDynamic->CurveOption.P_db[i-1].y_db - mDynamic->CurveOption.P_db[i-1].x_db*mDynamic->CurveOption.P_db[i].y_db)/(mDynamic->CurveOption.P_db[i].x_db - mDynamic->CurveOption.P_db[i-1].x_db);
	}
	mDynamic->k[mDynamic->CurveOption.PLen] = (mDynamic->CurveOption.P_db[mDynamic->CurveOption.PLen - 1].y_db - mDynamic->CurveOption.b_db)/mDynamic->CurveOption.P_db[mDynamic->CurveOption.PLen - 1].x_db - 1;
	mDynamic->b_db[mDynamic->CurveOption.PLen] = mDynamic->CurveOption.b_db;


	if (mDynamic->neverclipping!=0 && mDynamic->neverclipping!=1)
	{
		mDynamic->neverclipping = 0;
		assert(0);
	}

	if (mDynamic->k[mDynamic->CurveOption.PLen]+1>0)//jia hui lai
	{
		mDynamic->x_clipdb = -mDynamic->b_db[mDynamic->CurveOption.PLen]/(mDynamic->k[mDynamic->CurveOption.PLen]+1);
	}
	else
	{
		mDynamic->x_clipdb = -1;//this says: mDynamic->x_clipdb>0 it exist
	}

	/*指数逼近系数计算*/
	if (mDynamic->samplerate != 0)
	{
		mDynamic->Abeta = pow((1-SIGMA),1/(mDynamic->samplerate*(mDynamic->DynamicAttackms/1000.0)));   //pow((1-SIGMA),samplerate/(DynamicAttackms/1000.0)); --wrong             
		mDynamic->Aalpha = 1 - mDynamic->Abeta;
		mDynamic->Rbeta = pow((1-SIGMA),1/(mDynamic->samplerate*(mDynamic->DynamicReleasems/1000.0)));// pow((1-SIGMA),samplerate/(DynamicReleasems/1000.0));
		mDynamic->Ralpha = 1 - mDynamic->Rbeta;	
	}
	else
	{
		mDynamic->Abeta = 0.5;
		mDynamic->Aalpha = 0.5;
		mDynamic->Rbeta = 0.5;
		mDynamic->Ralpha = 0.5;
	}


}

void DynamicRun_API
(Dynamic_ID *mDynamic,
 short *input,
 float *x_db,
 int inLen,
 short *output,
 float *sidechain
 )
{
	int i,j;
	int inRlen;
	float *gain_d_dB;
	float *gain_d;
	float *gain;

	float thegain_d_dB[SKR_MAX_FRAME_SAMPLE_MONO];//优化掉malloc
	float thegain_d[SKR_MAX_FRAME_SAMPLE_MONO];
	float thegain[SKR_MAX_FRAME_SAMPLE_MONO];

	if (mDynamic->Bypass == 1)//此时不工作，也无side输出
	{
		for (i=0;i<inLen;i++)
		{
			output[i] = input[i];
		}
		return;
	}
	else
	{
		if (mDynamic->Chanel == 1)
		{
			inRlen = inLen;
		}
		else if(mDynamic->Chanel == 2)
		{
			inRlen = inLen/2;
		}
		else
		{
			assert(0);
		}
		switch(mDynamic->SideChain)
		{
		case NOUSE:
			gain_d_dB = thegain_d_dB;
			gain_d = thegain_d;
			gain = thegain;
			break;
		case GAIN_D_DB:
			gain_d_dB = sidechain;
			gain_d = thegain_d;
			gain = thegain;
			break;
		case GAIN_D:
			gain_d_dB = thegain_d_dB;
			gain_d = sidechain;
			gain = thegain;
			break;
		case GAIN:
			gain_d_dB = thegain_d_dB;
			gain_d = thegain_d;
			gain = sidechain;
			break;
		}

		for (i=0;i<inRlen;i++)
		{
			gain_d_dB[i]=Gain_d_dB(x_db[i],mDynamic->k,mDynamic->b_db,mDynamic->Plen,mDynamic->P_x_db);//优化考虑去掉x的db以及 g的dB 在run里，gain_d 是x 的指数函数 gain 以指数逼近gain_d 事实上，这个微分方程是个iir
		}
		for (i=0;i<inRlen;i++)
		{
			gain_d[i] = idB(gain_d_dB[i]);
		}
		
		Gain_d_To_Gain(gain_d,gain,&mDynamic->memgain,&mDynamic->memgain_d,mDynamic->Aalpha,mDynamic->Abeta, mDynamic->Ralpha, mDynamic->Rbeta,inRlen);

		if (mDynamic->Chanel == 1)
		{
			for (i=0;i<inLen;i++)
			{
				//output[i] = (short)input[i]*gain[i];//一般不需要饱和乘output[i] = smult(input[i], idB(gain_dB[i]));
				output[i] = stoshort(input[i]*gain[i]);
			}
		} 
		else
		{
			for (i=0,j=0;j<inRlen;i+=2,j++)
			{
				//output[i] = (short)input[i]*gain[i];//一般不需要饱和乘output[i] = smult(input[i], idB(gain_dB[i]));
				output[i] = stoshort(input[i]*gain[j]);
				output[i+1] = stoshort(input[i+1]*gain[j]);
			}
		}
	}
	
}



//void DynamicRun_API_dinsout
//(Dynamic_ID *mDynamic,
// float *input,
// float *x_db,
// int inLen,
// short *output,
// float *sidechain
// )
//{
//	int i,j;
//	int inRlen;
//	float *gain_d_dB;//如果malloc很吃cpu就优化掉
//	float *gain_d;
//	float *gain;
//
//	if (mDynamic->Bypass == 1)//此时不工作，也无side输出
//	{
//		for (i=0;i<inLen;i++)
//		{
//			output[i] = stoshort(input[i]);
//		}
//		return;
//	}
//	else
//	{
//		if (mDynamic->Chanel == 1)
//		{
//			inRlen = inLen;
//		}
//		else if(mDynamic->Chanel == 2)
//		{
//			inRlen = inLen/2;
//		}
//		else
//		{
//			assert(0);
//		}
//		switch(mDynamic->SideChain)
//		{
//		case NOUSE:
//			gain_d_dB = (float *)malloc(inRlen*sizeof(float));
//			gain_d = (float *)malloc(inRlen*sizeof(float));
//			gain = (float *)malloc(inRlen*sizeof(float));
//			break;
//		case GAIN_D_DB:
//			gain_d_dB = sidechain;
//			gain_d = (float *)malloc(inRlen*sizeof(float));
//			gain = (float *)malloc(inRlen*sizeof(float));
//			break;
//		case GAIN_D:
//			gain_d_dB = (float *)malloc(inRlen*sizeof(float));
//			gain_d = sidechain;
//			gain = (float *)malloc(inRlen*sizeof(float));
//			break;
//		case GAIN:
//			gain_d_dB = (float *)malloc(inRlen*sizeof(float));
//			gain_d = (float *)malloc(inRlen*sizeof(float));
//			gain = sidechain;
//			break;
//		}
//
//		for (i=0;i<inRlen;i++)
//		{
//			gain_d_dB[i]=Gain_d_dB(x_db[i],mDynamic->k,mDynamic->b_db,mDynamic->Plen,mDynamic->P_x_db);//优化考虑去掉x的db以及 g的dB 在run里，gain_d 是x 的指数函数 gain 以指数逼近gain_d 事实上，这个微分方程是个iir
//		}
//		for (i=0;i<inRlen;i++)
//		{
//			gain_d[i] = idB(gain_d_dB[i]);
//		}
//
//		Gain_d_To_Gain(gain_d,gain,&mDynamic->memgain,&mDynamic->memgain_d,mDynamic->Aalpha,mDynamic->Abeta, mDynamic->Ralpha, mDynamic->Rbeta,inRlen);
//
//		if (mDynamic->Chanel == 1)
//		{
//			for (i=0;i<inLen;i++)
//			{
//				//output[i] = (short)input[i]*gain[i];//一般不需要饱和乘output[i] = smult(input[i], idB(gain_dB[i]));
//				output[i] = stoshort(input[i]*gain[i]);
//			}
//		} 
//		else
//		{
//			for (i=0,j=0;j<inRlen;i+=2,j++)
//			{
//				//output[i] = (short)input[i]*gain[i];//一般不需要饱和乘output[i] = smult(input[i], idB(gain_dB[i]));
//				output[i] = stoshort(input[i]*gain[j]);
//				output[i+1] = stoshort(input[i+1]*gain[j]);
//			}
//		}
//		switch(mDynamic->SideChain)
//		{
//		case NOUSE:
//			free(gain_d_dB); 
//			free(gain_d);
//			free(gain);
//			break;
//		case GAIN_D_DB:
//			//free(gain_d_dB); 
//			free(gain_d);
//			free(gain);
//			break;
//		case GAIN_D:
//			free(gain_d_dB); 
//			//free(gain_d);
//			free(gain);
//			break;
//		case GAIN:
//			free(gain_d_dB); 
//			free(gain_d);
//			//free(gain);
//			break;
//		}
//	}
//
//}

void DynamicRun_API_intinsout
(Dynamic_ID *mDynamic,
 int *input,
 float *x_db,
 int inLen,
 short *output,
 float *sidechain
 )
{
	int i,j;
	int inRlen = 0;
	float *gain_d_dB;
	float *gain_d;
	float *gain;

	float thegain_d_dB[SKR_MAX_FRAME_SAMPLE_MONO];//优化掉malloc
	float thegain_d[SKR_MAX_FRAME_SAMPLE_MONO];
	float thegain[SKR_MAX_FRAME_SAMPLE_MONO];

	if (mDynamic->Bypass == 1)//此时不工作，也无side输出
	{
		for (i=0;i<inLen;i++)
		{
			output[i] = stoshort((float)(input[i]));
		}
		return;
	}
	else
	{
		if (mDynamic->Chanel == 1)
		{
			inRlen = inLen;
		}
		else if(mDynamic->Chanel == 2)
		{
			inRlen = inLen/2;
		}
		else
		{
			assert(0);
		}
		switch(mDynamic->SideChain)
		{
		case NOUSE:
			gain_d_dB = thegain_d_dB;
			gain_d = thegain_d;
			gain = thegain;
			break;
		case GAIN_D_DB:
			gain_d_dB = sidechain;
			gain_d = thegain_d;
			gain = thegain;
			break;
		case GAIN_D:
			gain_d_dB = thegain_d_dB;
			gain_d = sidechain;
			gain = thegain;
			break;
		case GAIN:
			gain_d_dB = thegain_d_dB;
			gain_d = thegain_d;
			gain = sidechain;
			break;
		}

		if (mDynamic->neverclipping == 1 && mDynamic->x_clipdb>0)
		{
			for (i=0;i<inRlen;i++)
			{
				gain_d_dB[i]=Gain_d_dB(x_db[i],mDynamic->k,mDynamic->b_db,mDynamic->Plen,mDynamic->P_x_db);//优化考虑去掉x的db以及 g的dB 在run里，gain_d 是x 的指数函数 gain 以指数逼近gain_d 事实上，这个微分方程是个iir
				if (x_db[i]>mDynamic->x_clipdb)
				{
					gain_d_dB[i] = -x_db[i];
				}
			}
		} 
		else
		{
			for (i=0;i<inRlen;i++)
			{
				gain_d_dB[i]=Gain_d_dB(x_db[i],mDynamic->k,mDynamic->b_db,mDynamic->Plen,mDynamic->P_x_db);//优化考虑去掉x的db以及 g的dB 在run里，gain_d 是x 的指数函数 gain 以指数逼近gain_d 事实上，这个微分方程是个iir
			}
		}


		for (i=0;i<inRlen;i++)
		{
			gain_d[i] = idB(gain_d_dB[i]);
		}

		Gain_d_To_Gain(gain_d,gain,&mDynamic->memgain,&mDynamic->memgain_d,mDynamic->Aalpha,mDynamic->Abeta, mDynamic->Ralpha, mDynamic->Rbeta,inRlen);

		if (mDynamic->Chanel == 1)
		{
			for (i=0;i<inLen;i++)
			{
				//output[i] = (short)input[i]*gain[i];//一般不需要饱和乘output[i] = smult(input[i], idB(gain_dB[i]));
				output[i] = stoshort(input[i]*gain[i]);
			}
		} 
		else
		{
			for (i=0,j=0;j<inRlen;i+=2,j++)
			{
				//output[i] = (short)input[i]*gain[i];//一般不需要饱和乘output[i] = smult(input[i], idB(gain_dB[i]));
				output[i] = stoshort(input[i]*gain[j]);
				output[i+1] = stoshort(input[i+1]*gain[j]);
			}
		}
	}

}
