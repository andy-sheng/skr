/*type:0-short,1-int,2-float,3-float,
how C can use template...?
"chanel"--number of arrays
if inchanel/outchanel is 1, we solute in1/out1
len is the input array's len
*/
void ChanelConvert(int inchanel,int outchanel,int len,short *in1,short *in2,short *out1,short *out2)
{
	int i,j;
	if(inchanel < outchanel)
	{
		for(i=j=0;i<len;i+=2,j++)
		{
			//out==in is ok
			out1[j]=in1[i];
			out2[j]=in1[i+1];	
		}		
	}
	else 
	{
		for (i=j=0;i<len;j+=2,i++)
		{
			//out == in is wrong
			out1[j] = in1[i];
			out1[j+1] = in2[i];
		}
	}

}
void ChanelConvert_f(int inchanel,int outchanel,int len,float *in1,float *in2,float *out1,float *out2)
{
	int i,j;
	if(inchanel < outchanel)
	{
		for(i=j=0;i<len;i+=2,j++)
		{
			//out==in is ok
			out1[j]=in1[i];
			out2[j]=in1[i+1];	
		}		
	}
	else 
	{
		for (i=j=0;i<len;j+=2,i++)
		{
			//out == in is wrong
			out1[j] = in1[i];
			out1[j+1] = in2[i];
		}
	}

}

void ChannelSplit(short *in,int inlen,short *out[],int outchannel)
{
	int i, j;

	for (i=0;i<inlen/outchannel;i++)
	{
		for (j=0;j<outchannel;j++)
		{
			out[j][i] = in[i*outchannel+j];
		}		
	}
}

void ChannelMerge(short *in, int inlen, short *out[], int outchannel)
{
	int i, j;

	for (i = 0; i < inlen; i++)
	{
		for (j = 0; j < outchannel; j++)
		{
			in[i*outchannel + j] = out[j][i];   
		}
	}
}