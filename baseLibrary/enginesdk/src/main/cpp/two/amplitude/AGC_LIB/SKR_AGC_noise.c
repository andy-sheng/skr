#include "SKR_AGC_defines.h"
#include <stdlib.h>
#include "../../common/functions.h"

#if 0
int noise_db(float *x_db,int nLen,float *memnoise_db,int *memUpwatch,float *memnewnoise_db)
{
	int i;
	float minx_db = 0;
	int changed = 0;

	for (i=0;i < nLen;i++)
	{
		minx_db = THEMINOF(minx_db,x_db[i]);
	}
	if (minx_db < *memnoise_db)
	{
		*memnoise_db = minx_db;
		*memUpwatch = 0;
		changed = 1;
	} 
	else
	{
		*memUpwatch++;
		*memnewnoise_db = THEMINOF(minx_db,*memnewnoise_db);

		if (*memUpwatch > NOISE_UP_THRESHOLD)//连续这些帧的话就更新
		{
			*memnoise_db = *memnewnoise_db;
			*memUpwatch = 0;
			changed = 1;
		} 
	}

	return changed;
}

#endif
int noise_db2(float x_db,int nLen,float *memnoise_db,int *memUpwatch,float *memnewnoise_db,int updateThreshold)
{
	int i;
	float minx_db = 0;
	int changed = 0;

	if (x_db < *memnoise_db)
	{
		*memnoise_db = x_db;
		*memUpwatch = 0;
		//*memnewnoise_db = *memnoise_db;
		changed = 1;
	}
    else if(x_db > *memnoise_db + 18 && x_db > -50)//this time x_db may not noise
    {
        *memUpwatch = 0;
    }
	else
	{
		memnewnoise_db[*memUpwatch] = x_db;
		*memUpwatch = *memUpwatch + 1;//*memUpwatch++;
		
		if (*memUpwatch >= updateThreshold)//连续这些帧的话就更新
		{
			for (i=0;i<updateThreshold;i++)
			{
				minx_db = THEMINOF(memnewnoise_db[i],minx_db);
			}
			*memnoise_db = minx_db;
			*memUpwatch = 0;
			changed = 1;
		} 
	}

	return changed;
}

int noise_db3(float x_db,int nLen,float *memnoise_db,int *memUpwatch,float *memnewnoise_db,int updateThreshold)
{
	int i;
	float minx_db = 0;
	int changed = 0;

	if (x_db < *memnoise_db)
	{
		///////normally? if the noise is suddenly low by eccut or noisegate or xn is suddenly quiet?/////////
		if (*memnoise_db<-40.0 && x_db<*memnoise_db - 10.0)//be sure memnoise is noise first
		{
			//don't update noisedb
		}
		else
		{
			
			
			*memnoise_db = THEMAXOF(x_db,-80);//x_db;//

			*memUpwatch = 0;
			//*memnewnoise_db = *memnoise_db;
			changed = 1;

		}

		
	}
    else if(x_db > *memnoise_db + 21 && x_db > -40 && *memnoise_db>-56)//this time x_db may not noise//tmp
    {
        //*memUpwatch = 0;
    }
	else
	{
		memnewnoise_db[*memUpwatch] = x_db;
		*memUpwatch = *memUpwatch + 1;//*memUpwatch++;
		
		if (*memUpwatch >= updateThreshold)//连续这些帧的话就更新
		{
			for (i=0;i<updateThreshold;i++)
			{
				minx_db = THEMINOF(memnewnoise_db[i],minx_db);
			}
            if(minx_db - *memnoise_db > 7.0)//tmp
            {
                *memnoise_db += 7.0;//tmp
            }
            else
            {
                *memnoise_db = minx_db;
            }
			
			*memUpwatch = 0;
			changed = 1;
		}
	}


    
	return changed;
}
int noise_dbvip(float x_db,int nLen,float *memnoise_db,int *memUpwatch,float *memnewnoise_db,int updateThreshold)
{
	int i;
	float minx_db = 0;
	int changed = 0;

	if (x_db < *memnoise_db)
	{
		///////normally? if the noise is suddenly low by eccut or noisegate or xn is suddenly quiet?/////////
		if (*memnoise_db<-40.0 && x_db<*memnoise_db - 10.0)//be sure memnoise is noise first
		{
			//don't update noisedb
		}
		else
		{
			
			
			*memnoise_db = THEMAXOF(x_db,-50);//x_db;//

			*memUpwatch = 0;
			//*memnewnoise_db = *memnoise_db;
			changed = 1;

		}

		
	}
    else if(x_db > *memnoise_db + 21 && x_db > -40 && *memnoise_db>-56)//this time x_db may not noise//tmp
    {
        //*memUpwatch = 0;
    }
	else
	{
		memnewnoise_db[*memUpwatch] = x_db;
		*memUpwatch = *memUpwatch + 1;//*memUpwatch++;
		
		if (*memUpwatch >= updateThreshold)//连续这些帧的话就更新
		{
			for (i=0;i<updateThreshold;i++)
			{
				minx_db = THEMINOF(memnewnoise_db[i],minx_db);
			}
            if(minx_db - *memnoise_db > 7.0)//tmp
            {
                *memnoise_db += 7.0;//tmp
            }
            else
            {
                *memnoise_db = minx_db;
            }
			
			*memUpwatch = 0;
			changed = 1;
		}
	}


    
	return changed;
}
int noise_db4(float x_db,float *memnoise_db,int *memUpwatch,float *memnewnoise_db,int updateThreshold,int *localnoisechanged,int *memholdlowernoisewatch,float *memminxdb/*if don't hold,this xdb will become the noise*/,int *hold/*now it is for debug*/,short *input,int inlen,short *memtherealnoise_down,short *memtherealnoise_up,short *therealnoise/*output when localnoisechanged*/)
{
	int i;
	float minx_db = 0;
	int thisiscut = 0;
	int j;
	float slowupmodgain;
	
	*hold = 0;
	*localnoisechanged = 0;

  
	if (x_db < *memnoise_db)
	{
		*memUpwatch = 0;
		////if the memnoise is low than -45dB,we hold it 200ms,in this 200ms if it x_db lower than memnoise_db - 10.0 we think this is cut and we.if in this 200ms the lowest x_db is 
		if(x_db>=-65.0)//update directly
		{
			*memnoise_db = x_db;
			*localnoisechanged = -1;//means noise down
				
			for (j = 0;j<inlen;j++)
			{
				therealnoise[j] = input[j];
			}
		}
		else
		{
			if (*memnoise_db>-55.0)//we are not sure we have got the memnoise, so update directly.that is to say,we only solve "cut" after we got the memnoise
			{
				*memnoise_db = x_db;
				*localnoisechanged = -1;
				for (j = 0;j<inlen;j++)
				{
					therealnoise[j] = input[j];//before we get the memnoise the therealnoise may be wrong at first
				}
			}
			else
			{
				if (*memholdlowernoisewatch == -9999)//hold end
				{
					if (*memminxdb<*memnoise_db-15)//in the holding time(200ms) the noise is 10dB lower...
					{
						thisiscut = 1;
					}
					else
					{
						*memnoise_db = *memminxdb;//if xdb is lower than -55db we only update noise when we are sure this is not cut.
						*localnoisechanged = -1;
						for (j = 0;j<inlen;j++)
						{
							therealnoise[j] = memtherealnoise_down[j];
						}
					}
					*memminxdb = 0;
					*memholdlowernoisewatch = 0;
				}
				if (*memholdlowernoisewatch == 0)// 
				{
					//don't update noisedb
					//thisiscut = 1;
					*memholdlowernoisewatch = 200;//200ms
					*memminxdb = 0;//reset
				}
				if (*memholdlowernoisewatch>0)//now is holding time
				{
					thisiscut = 1;
				}
			}
		}
	}
	else if(x_db > *memnoise_db + 18 && x_db > -50)//this time x_db may not noise
	{
		//*memUpwatch = 0;
	}
	else
	{
		
		if (x_db<memnewnoise_db[*memUpwatch])
		{
			for (j = 0;j<inlen;j++)
			{
				memtherealnoise_up[j] = input[j];
			}
		}
		
		
		memnewnoise_db[*memUpwatch] = x_db;
		*memUpwatch = *memUpwatch + 1;//*memUpwatch++;
		
		if (*memUpwatch >= updateThreshold)//连续这些帧的话就更新
		{
			for (i=0;i<updateThreshold;i++)
			{
				minx_db = THEMINOF(memnewnoise_db[i],minx_db);
			}
			if(minx_db - *memnoise_db > 7.0)
			{
				*memnoise_db += 2.0;////////////////?
				*localnoisechanged = 1;

				slowupmodgain = idB(*memnoise_db - minx_db);

				for (j = 0;j<inlen;j++)
				{
					therealnoise[j] = (short)(memtherealnoise_up[j]*slowupmodgain);
				}
			}
			else
			{
				*memnoise_db = minx_db;
				*localnoisechanged = 1;
				for (j = 0;j<inlen;j++)
				{
					therealnoise[j] = memtherealnoise_up[j];
				}
			}
			
			*memUpwatch = 0;

			//reset
			for (i=0;i<updateThreshold;i++)
			{
				memnewnoise_db[i] = 0;
			}

			//changed = 1;
		}
	}

	if (*memholdlowernoisewatch>0)//holding.... 
	{
		if (*memminxdb > x_db)
		{
			*memminxdb = x_db;

			for (j = 0;j<inlen;j++)
			{
				memtherealnoise_down[j] = input[j];
			}

		}


		*hold = 1;

		*memholdlowernoisewatch -=20;

		if(*memholdlowernoisewatch<0)
		{
			*memholdlowernoisewatch = -9999;//finish hold
		}
	}

    
	return thisiscut;
}








int noise_db5(float x_db,float *memnoise_db,int *memUpwatch,float *memnewnoise_db,int updateThreshold,int *localnoisechanged,int *memholdlowernoisewatch,int *memminxdb/*if don't hold,this xdb will become the noise*/,int *hold/*now it is for debug*/,float *input,int inlen,short *memtherealnoise_down,short *memtherealnoise_up,short *therealnoise/*output when localnoisechanged*/)
{
	int i;
	float minx_db = 0;
	int thisiscut = 0;
	int j;
	float slowupmodgain;
	
	*hold = 0;
	*localnoisechanged = 0;


	//it don't think there is cut,because cut is extern info,and when cut happen this func will not be runned

  
	if (x_db < *memnoise_db)
	{
		*memUpwatch = 0;
		
		//if(1)//update directly
		{
			*memnoise_db = x_db;
			*localnoisechanged = -1;//means noise down
				
			for (j = 0;j<inlen;j++)
			{
				therealnoise[j] = (short)(input[j]);
			}
		}
		
	}
	else if(x_db > *memnoise_db + 18 && x_db > -50)//this time x_db may not noise
	{
		//*memUpwatch = 0;
	}
	else
	{
		
		if (x_db<memnewnoise_db[*memUpwatch])
		{
			for (j = 0;j<inlen;j++)
			{
				memtherealnoise_up[j] = (short)(input[j]);
			}
		}
		
		
		memnewnoise_db[*memUpwatch] = x_db;
		*memUpwatch = *memUpwatch + 1;//*memUpwatch++;
		
		if (*memUpwatch >= updateThreshold)//连续这些帧的话就更新
		{
			for (i=0;i<updateThreshold;i++)
			{
				minx_db = THEMINOF(memnewnoise_db[i],minx_db);
			}
			if(minx_db - *memnoise_db > 7.0)
			{
				*memnoise_db += 2.0;////////////////?
				*localnoisechanged = 1;

				slowupmodgain = idB(*memnoise_db - minx_db);

				for (j = 0;j<inlen;j++)
				{
					therealnoise[j] = (short)(memtherealnoise_up[j]*slowupmodgain);
				}
			}
			else
			{
				*memnoise_db = minx_db;
				*localnoisechanged = 1;
				for (j = 0;j<inlen;j++)
				{
					therealnoise[j] = memtherealnoise_up[j];
				}
			}
			
			*memUpwatch = 0;

			//reset
			for (i=0;i<updateThreshold;i++)
			{
				memnewnoise_db[i] = 0;
			}

			//changed = 1;
		}
	}



    
	return thisiscut;
}








int Max_dbCalcu(float *mem_db,int *memwatch,float *memnew_db,float x_db,int updateThreshold)
{
	int i;
	float maxx_db = -100.0;
	int changed = 0;

	if (x_db > *mem_db)
	{
		*mem_db = x_db;
		*memwatch = 0;
		changed = 1;
	} 
	else
	{
		memnew_db[*memwatch] = x_db;
		*memwatch = *memwatch + 1;//*memwatch++;

		if (*memwatch >= updateThreshold)//连续这些帧的话就更新
		{
			for (i=0;i<updateThreshold;i++)
			{
				maxx_db = THEMAXOF(memnew_db[i],maxx_db);
			}
			*mem_db = maxx_db;
			*memwatch = 0;
			changed = 1;
		} 
	}

	return changed;
}