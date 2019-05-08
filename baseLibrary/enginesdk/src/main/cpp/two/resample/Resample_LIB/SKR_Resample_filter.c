#include <stdlib.h>
#include "SKR_Resample_functions.h"
#include <math.h>
#include <assert.h>

void filterc(float *b,float *a,int n,int ns,float *x,int len,float *px,float *py)
{
	int i,j,k,n1;
	n1=n+1;
	for(j=0;j<ns;j++)
	{
		for(k=0;k<len;k++)
		{
			px[j*n1+0]=x[k];
			x[k]=b[j*n1+0]*px[j*n1+0];
			for(i=1;i<=n;i++)
			{
				x[k]+=b[j*n1+i]*px[j*n1+i]-a[j*n1+i]*py[j*n1+i];
			}
			if(fabs(x[k])>1.0e10)assert(0);
			if(fabs(x[k])<0.000001)
			{
				x[k] = 0;
			}
			for(i=n;i>=2;i--)
			{
				px[j*n1+i]=px[j*n1+i-1];
				py[j*n1+i]=py[j*n1+i-1];
			}
			px[j*n1+1]=px[j*n1+0];
			py[j*n1+1]=x[k];
		}
	}
}

void filtercfix(float *b,float *a,int n,int ns,short *x,short *y,int len,short *px,float *py)//wrong:暂时不用这个
{
	int i,j,k,n1;
	float xk;
	
	n1=n+1;
	for(j=0;j<ns;j++)
	{
		for(k=0;k<len;k++)
		{
			px[j*n1+0]=x[k];
			
			//x[k]=b[j*n1+0]*px[j*n1+0];
			xk=b[j*n1+0]*px[j*n1+0];
			for(i=1;i<=n;i++)
			{
				//x[k]+=b[j*n1+i]*px[j*n1+i]-a[j*n1+i]*py[j*n1+i];
				xk+=b[j*n1+i]*px[j*n1+i]-a[j*n1+i]*py[j*n1+i];
			}
			//if(fabs(x[k])>1.0e10)exit(0);
			for(i=n;i>=2;i--)
			{
				px[j*n1+i]=px[j*n1+i-1];
				py[j*n1+i]=py[j*n1+i-1];
			}
			px[j*n1+1]=px[j*n1+0];
			//py[j*n1+1]=x[k];
			py[j*n1+1]=xk;
			y[k] = stoshort(xk);
		}
	}
}









