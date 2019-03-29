#include <stdlib.h>
#include "Delay_control.h"
#include <assert.h>
#include "../../common/functions.h"
#include <math.h>

void BufresetAPI(Buf16_s *buf16)
{
	int i;
	for(i =0;i<BUFFLEN;i++)
	{
		buf16->membuf[i] = 0;
	}
}

void putinAPI(Buf16_s *buf16,short *input,int inlen)
{
	int i;
	for (i = 0;i<inlen;i++)
	{
		if (buf16->id+i>=BUFFLEN)
		{
			break;
			assert(0);
		}
		buf16->membuf[buf16->id+i] = input[i];// stoshort(sampleBuffer[i]*32768.0f);
	}
	buf16->id += i; 

}
void putinAPI_loop(Buf16_s *buf16,short *input,int inlen)
{
	int i;
	for (i = 0;i<inlen;i++)
	{
		if((buf16->rear+1)%BUFFLEN == buf16->front)//可以优化去掉这个if，判断一次不会满即可
		{
			//break;
			assert(0);
			return;
		}
		buf16->rear = (buf16->rear + 1)%BUFFLEN;
		buf16->membuf[buf16->rear] = input[i];
	}
}
void putinAPI_loop_onlychannelLin(Buf16_s *buf16,short *input,int inlen)
{
	int i;
	for (i = 0;i<inlen;i+=2)
	{
		if((buf16->rear+1)%BUFFLEN == buf16->front)//可以优化去掉这个if，判断一次不会满即可
		{
			//break;
			assert(0);
			return;
		}
		buf16->rear = (buf16->rear + 1)%BUFFLEN;
		buf16->membuf[buf16->rear] = input[i];
	}
}
void putin_iNormalizeAPI(Buf16_s *buf16,float *input,int inlen)
{
	int i;
	for (i = 0;i<inlen;i++)
	{
		if (buf16->id+i>=BUFFLEN)
		{
			break;
			assert(0);
		}
		buf16->membuf[buf16->id+i] = stoshort(input[i]*32768.0f);
	}
	buf16->id += i; 

}
void putin_iNormalizeAPI_loop(Buf16_s *buf16,float *input,int inlen)
{
	int i;
	for (i = 0;i<inlen;i++)
	{
		if((buf16->rear+1)%BUFFLEN == buf16->front)
		{
			//break;
			assert(0);
			return;
		}
		buf16->rear = (buf16->rear + 1)%BUFFLEN;
		buf16->membuf[buf16->rear] = stoshort(input[i]*32768.0f);
	}

}
int putoutAPI(Buf16_s *buf16,short *output,int outlen)//if no data output 0
{
	int i;
	int zero = 0;
	for (i = 0;i<outlen;i++)
	{
		if (i<buf16->id)
		{
			output[i] = buf16->membuf[i];
		}
		else
		{
			output[i] = 0;
			zero++;
		}
	}
	//for (i = 0;i<buf16->id;i++)//
	for (i = 0;i<buf16->id - outlen;i++)
	{
		buf16->membuf[i] = buf16->membuf[i+outlen];
	}
	buf16->id -= outlen;
	if (buf16->id < 0)
	{
		buf16->id = 0;
	}
	return zero;

}
int putoutAPI_loop(Buf16_s *buf16,short *output,int outlen)//if no data output 0
{
	int i;
	int zero = 0;
	for (i = 0;i<outlen;i++)
	{
		if (buf16->front != buf16->rear)//可以优化去掉这个if，如果一开始就判断不会被取空的话
		{
			buf16->front = (buf16->front + 1)%BUFFLEN;
			output[i] = buf16->membuf[buf16->front];
		}
		else
		{
			output[i] = 0;
			zero++;
		}
	}
	
	return zero;

}
int queuehave_API(Buf16_s *buf16)
{
	return (buf16->rear - buf16->front + BUFFLEN)%BUFFLEN;
}
int fastmoveAPI_loop(Buf16_s *buf16,int move)
{
	int i;
	int zero = 0;
	int havenum;
	int movedistance;

	havenum = queuehave_API(buf16);
	movedistance = abs(move);

	if (movedistance+havenum<BUFFLEN-1)
	{

		if (move>0)
		{
			for (i=0;i<havenum;i++)
			{
				buf16->membuf[(buf16->rear+move-i+ BUFFLEN)%BUFFLEN] = buf16->membuf[(buf16->rear-i+ BUFFLEN)%BUFFLEN];
			}
		} 
		else if(move<0)
		{
			for (i=0;i<havenum;i++)
			{
				buf16->membuf[(buf16->front+move+i+ BUFFLEN)%BUFFLEN] = buf16->membuf[(buf16->front+i)%BUFFLEN];
			}
		}
		buf16->rear = (buf16->rear+move)%BUFFLEN;
		buf16->front = (buf16->front+move)%BUFFLEN;
		return 0;//ok
	}
	else
	{
		return -1;//can't use this func to movequeue
	}

}

int insert0frontAPI_loop(Buf16_s *buf16,int n)
{
	int i;
	int zero = 0;
	int havenum;
	int movedistance;

	havenum = queuehave_API(buf16);

	if (n+havenum<BUFFLEN-1)
	{
		for (i=0;i<n;i++)
		{
			buf16->membuf[(buf16->front-i+ BUFFLEN)%BUFFLEN] = -440;
		}
		buf16->front = (buf16->front-n+ BUFFLEN)%BUFFLEN;
		return 0;//ok
	}
	else
	{
		return -1;//can't use this func to movequeue
	}

}
int insert0behindAPI_loop(Buf16_s *buf16,int n)
{
	int i;
	int zero = 0;
	int havenum;
	int movedistance;

	havenum = queuehave_API(buf16);

	if (n+havenum<BUFFLEN-1)
	{
		for (i=0;i<n;i++)
		{
			buf16->membuf[(buf16->rear+1+i)%BUFFLEN] = -40;
		}
		buf16->rear = (buf16->rear + n)%BUFFLEN;
		return 0;//ok
	}
	else
	{
		return -1;//can't use this func to movequeue
	}

}
int copyAPI_loop(Buf16_s *buf16,Buf16_s *buf16_1)
{
	int i;
	int n;

	buf16->front = buf16_1->front;
	buf16->rear = buf16_1->rear;
	for (i=0;i<queuehave_API(buf16_1);i++)
	{
		n = (buf16->front+1+i)%BUFFLEN;
		buf16->membuf[n] = buf16_1->membuf[n];
	}

	return 0;
}



void putoutAPI_onlymove(Buf16_s *buf16,int outlen)
{
	int i;
	
	//for (i = 0;i<buf16->id;i++)//
	for (i = 0;i<buf16->id - outlen;i++)
	{
		buf16->membuf[i] = buf16->membuf[i+outlen];
	}
	buf16->id -= outlen;
	if (buf16->id < 0)
	{
		buf16->id = 0;
	}


}
void putoutAPI_onlymove_loop(Buf16_s *buf16,int outlen)//if no data output 0
{
	int i;
	for (i = 0;i<outlen;i++)
	{
		if (buf16->front != buf16->rear)
		{
			buf16->front = (buf16->front + 1)%BUFFLEN;
		}
	}
}
int BufDelayRun_API(Buf16_s *buf16,short *input,int inlen,	short *output)
{
	putinAPI(buf16,input,inlen);
	return putoutAPI(buf16,output,inlen);
}
int BufDelayRun_API_loop(Buf16_s *buf16,short *input,int inlen,	short *output)
{
	putinAPI_loop(buf16,input,inlen);
	return putoutAPI_loop(buf16,output,inlen);
}
int putoutAPI_ForReframe(Buf16_s *buf16,short *output,int outlen)//if no data return 0
{
	int i;

	if (buf16->id>=outlen)
	{
		for (i = 0;i<outlen;i++)
		{
			output[i] = buf16->membuf[i];
		}
		for (i = 0;i<buf16->id - outlen;i++)
		{
			buf16->membuf[i] = buf16->membuf[i+outlen];
		}
		buf16->id -= outlen;
		return 1;
	} 
	else
	{
		return 0;
	}
}
int putoutAPI_ForReframe_loop(Buf16_s *buf16,short *output,int outlen)//if no data return 0
{
	int i;

	if (queuehave_API(buf16) >= outlen)
	{
		for (i = 0;i<outlen;i++)
		{
			buf16->front = (buf16->front + 1)%BUFFLEN;
			output[i] = buf16->membuf[buf16->front];
		}
		return 1;
	} 
	else
	{
		return 0;
	}
}
int putoutAPI_ForReframe_0(Buf16_s *buf16,short *output,int outlen)//if no data return 0; but use 0 as output
{
	int i;

	if (buf16->id>=outlen)
	{
		for (i = 0;i<outlen;i++)
		{
			output[i] = buf16->membuf[i];
		}
		for (i = 0;i<buf16->id - outlen;i++)
		{
			buf16->membuf[i] = buf16->membuf[i+outlen];
		}
		buf16->id -= outlen;
		return 1;
	} 
	else
	{
		for (i = 0;i<outlen;i++)
		{
			output[i] = 0;
		}
		return 0;
	}
}
int putoutAPI_ForReframe_0_loop(Buf16_s *buf16,short *output,int outlen)//if no data return 0
{
	int i;

	if (queuehave_API(buf16)>= outlen)
	{
		for (i = 0;i<outlen;i++)
		{
			buf16->front = (buf16->front + 1)%BUFFLEN;
			output[i] = buf16->membuf[buf16->front];
		}
		return 1;
	} 
	else
	{
		for (i = 0;i<outlen;i++)
		{
			output[i] = 0;
		}
		return 0;
	}
}