#include "SKR_AGC_defines.h"
#include "AGC_control.h"
#include <stdlib.h>
#include "../../common/functions.h"

//void LeftDerta(short *y,int inLen,int *derta_y,short *memy)
//{
//	int i;
//
//	derta_y[0] = y[0] - memy[0];
//	for (i = 1;i<inLen;i++)
//	{
//		derta_y[i] = y[i] - y[i-1];
//	}
//	memy[0] = y[inLen - 1];
//}

#define KMETHORD (0)
#define PEAKMETHORD (1)


int ClippingDetec(short *input, int inLen, /*short *meminput,δ��*/ int chanel, int samplerate)
{
	int peakpointL_positive;//��Ϊֱ��Ӱ��Ӧ�������������������þ���ֵ��׼ȷ������©����ֵ��������������У������������ʱ��һ��2��3���뼴����Ϊ��������ô�������20ms��peak����������Ҫ�ֳ�10ms��֡������
	int peakpointL_negative;
	int clippingwatchL_positive;
	int clippingwatchL_negative;

	int peakpointR_positive;
	int peakpointR_negative;
	int clippingwatchR_positive;
	int clippingwatchR_negative;

	int tmpT;
	int tmpT2;
	int i;
	int clipping = 0;

#if KMETHORD
	int DetecContinue = 0;
	float tmpK_Rpositive_T;
#endif

	tmpT = (int)(CLIPPING_TTHRESHOLD*samplerate);
	tmpT2 = (int)(CLIPPING_SERIOUS_TTHRESHOLD*samplerate);

	if (chanel == 1)
	{
		for(peakpointR_positive=peakpointR_negative=i=0;i<inLen;i++)
		{
			peakpointR_positive = THEMAXOF(peakpointR_positive,input[i]);
			peakpointR_negative = THEMINOF(peakpointR_negative,input[i]);
		}
		
#if PEAKMETHORD
		if (THEMAXOF(peakpointR_positive,-peakpointR_negative)<PEAK_THRESHOLD)
		{
			return 0;
		}
#endif

#if KMETHORD		
		tmpK_Rpositive_T = DERIVATIVE_TTHRESHOLD/samplerate*THEMAXOF(peakpointR_positive,-peakpointR_negative);
#endif
		peakpointR_positive = (int)(peakpointR_positive * FULL_LOAD);//��Ϊ��������
		peakpointR_negative = (int)(peakpointR_negative * FULL_LOAD);

		for(clippingwatchR_positive=clippingwatchR_negative=i=0;i<inLen;i++)
		{
			if (input[i]>peakpointR_positive)
			{
				clippingwatchR_positive++;
				if (clippingwatchR_positive>tmpT)
				{
					clipping = 1;
#if KMETHORD					
					//...�����ǲ���ƽ������������졣��ʵ���������ֱ��ж��ƺ���ʵ��
					for (i = 1;i<inLen;i++)
					{
						if (abs(input[i] - input[i-1])>tmpK_Rpositive_T)//if (abs(input[i] - input[i-1])/THEMAXOF(peakpointR_positive,-peakpointR_negative)>DERIVATIVE_TTHRESHOLD/samplerate)
						{
							DetecContinue = 1;
						}
					}
					if (DetecContinue == 0)
					{
						return 0;//�ðɣ����ü����
					}
					//...
#endif
					if (clippingwatchR_positive>tmpT2)
					{
						clipping = 2;
						break;//DSP�Ļ�����ȥ����Щ�������ٸ�����...
					}
				}
			} 
			else
			{
				clippingwatchR_positive = 0;
			}
			if (clipping < 2)
			{
				if (input[i]<peakpointR_negative)
				{
					clippingwatchR_negative++;
					if (clippingwatchR_negative>tmpT)
					{
						clipping = 1;
						if (clippingwatchR_negative>tmpT2)
						{
							clipping = 2;
							break;//DSP�Ļ�����ȥ����Щ�������ٸ�����...
						}
					}
				} 
				else
				{
					clippingwatchR_negative = 0;
				}
			}
			
		}
	} 
	else
	{
		for(peakpointL_positive=peakpointL_negative=peakpointR_negative=peakpointR_positive=i=0;i+1<inLen;i+=2)//i+1<inLen�����inLen��ż���Ͷ������һ������ĵ�
		{
			peakpointR_positive = THEMAXOF(peakpointR_positive,input[i]);
			peakpointR_negative = THEMINOF(peakpointR_negative,input[i]);
			peakpointL_positive = THEMAXOF(peakpointL_positive,input[i+1]);
			peakpointL_negative = THEMINOF(peakpointL_negative,input[i+1]);
		}
#if PEAKMETHORD
		if (THEMAXOF(peakpointR_positive,-peakpointR_negative)<PEAK_THRESHOLD||THEMAXOF(peakpointL_positive,-peakpointL_negative)<PEAK_THRESHOLD)
		{
			return 0;
		}
#endif

#if KMETHORD
		tmpK_Rpositive_T = DERIVATIVE_TTHRESHOLD/samplerate*THEMAXOF(peakpointR_positive,-peakpointR_negative);
#endif
		peakpointR_positive = (int)(peakpointR_positive * FULL_LOAD);
		peakpointR_negative = (int)(peakpointR_negative * FULL_LOAD);
		peakpointL_positive = (int)(peakpointL_positive * FULL_LOAD);
		peakpointL_negative = (int)(peakpointL_negative * FULL_LOAD);

		

		for(clippingwatchR_negative=clippingwatchR_positive=i=0;i+1<inLen;i+=2)//i+1<inLen�����inLen��ż���Ͷ������һ������ĵ�
		{
			if (input[i]>peakpointR_positive)
			{
				clippingwatchR_positive++;
				if (clippingwatchR_positive>tmpT)
				{
					clipping = 1;
#if KMETHORD
					//...�����ǲ���ƽ������������졣��ʵ���������ֱ��ж��ƺ���ʵ��
					for (i = 2;i+1<inLen;i+=2)//i+1<inLen�����inLen��ż���Ͷ������һ������ĵ�
					{
						if (abs(input[i] - input[i-2])>tmpK_Rpositive_T)//if (abs(input[i] - input[i-1])/THEMAXOF(peakpointR_positive,-peakpointR_negative)>DERIVATIVE_TTHRESHOLD/samplerate)
						{
							DetecContinue = 1;
						}
					}
					if (DetecContinue == 0)
					{
						return 0;//�ðɣ����ü���ˣ�����������ֻ��һ������
					}
					//...
#endif
					if (clippingwatchR_positive>tmpT2)
					{
						clipping = 2;
						break;
					}
				}
			} 
			else
			{
				clippingwatchR_positive = 0;
			}
			if (clipping < 2)
			{
				if (input[i]<peakpointR_negative)
				{
					clippingwatchR_negative++;
					if (clippingwatchR_negative>tmpT)
					{
						clipping = 1;
						if (clippingwatchR_negative>tmpT2)
						{
							clipping = 2;
							break;
						}
					}
				} 
				else
				{
					clippingwatchR_negative = 0;
				}
			}
			
		}
		if (clipping < 2)
		{
			for(clippingwatchL_negative=clippingwatchL_positive=i=0;i+1<inLen;i+=2)//i+1<inLen�����inLen��ż���Ͷ������һ������ĵ�
			{
				if (input[i+1]>peakpointL_positive)
				{
					clippingwatchL_positive++;
					if (clippingwatchL_positive>tmpT)
					{
						clipping = 1;
						if (clippingwatchL_positive>tmpT2)
						{
							clipping = 2;
							break;
						}
					}
				} 
				else
				{
					clippingwatchL_positive = 0;
				}
				if (clipping < 2)
				{
					if (input[i+1]<peakpointL_negative)
					{
						clippingwatchL_negative++;
						if (clippingwatchL_negative>tmpT)
						{
							clipping = 1;
							if (clippingwatchL_negative>tmpT2)
							{
								clipping = 2;
								break;
							}
						}
					} 
					else
					{
						clippingwatchL_negative = 0;
					}
				}
			}
		}


	}
	return clipping;

}