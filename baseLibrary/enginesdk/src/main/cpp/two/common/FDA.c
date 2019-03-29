#include <math.h>
#include <stdlib.h>
#include <assert.h>
#include "functions.h"
#include "defines.h"

static float warp(float f)
{	
	return(tan((4.0*atan(1.0))*f));
}

static void ChebyII(int ln,int k,int n,float ws,float att,float *d,float *c)
{
	int i;
	float gam,alpha,beta,sigma,omega,scln,scld,pi;
	pi = 4.0*atan(1.0);
	gam = pow((att+sqrt(att*att-1.0)),1.0/ln);
	alpha = 0.5*(1.0/gam-gam)*sin((2*(k+1)-1)*pi/(2*ln));
	beta = 0.5*(1.0/gam+gam)*cos((2*(k+1)-1)*pi/(2*ln));
	sigma = ws*alpha/(alpha*alpha+beta*beta);
	omega = -1.0*ws*beta/(alpha*alpha+beta*beta);
	for(i=0;i<=n;i++)
	{
		d[i] = 0.0;
		c[i] = 0.0;
	}
	if(ln%2==1&&k+1==(ln+1)/2)
	{
		d[0]=-1.0*sigma;
		c[0]=d[0];
		c[1]=1.0;
	}
	else
	{
		scln = sigma*sigma+omega*omega;
		scld = pow(ws/cos((2*(k+1)-1)*pi/(2*ln)),2);//??2 ci fang
		d[0]=scln*scld;
		d[2]=scln;
		c[0]=d[0];
		c[1]=-2.0*sigma*scld;
		c[2]=scld;
	}
}

 void bilinear(float d[],float c[],float b[],float a[],int n)
{
	int i,j,n1;
	float sum,atmp,scale,*temp;
	
	n1=n+1;
	temp=(float *)malloc(n1*n1*sizeof(float));
	for (j=0;j<=n;j++)
	{
		temp[j*n1+0]=1.0;
	}
	sum=1.0;
	for(i=1;i<=n;i++)
	{
		sum=sum*(float)(n-i+1)/(float)i;
		temp[0*n1+i]=sum;
	}
	for(i=1;i<=n;i++)
	for(j=1;j<=n;j++)
	{
		temp[j*n1+i]=temp[(j-1)*n1+i]-temp[j*n1+i-1]-temp[(j-1)*n1+i-1];
	}
	for(i=n;i>=0;i--)
	{
		b[i]=0.0;
		atmp=0.0;
		for(j=0;j<=n;j++)
		{
			b[i]=b[i]+temp[j*n1+i]*d[j];
			atmp=atmp+temp[j*n1+i]*c[j];
		}
		scale=atmp;
		if(i!=0) a[i]=atmp;
	}
	for(i=0;i<=n;i++)
	{
		b[i]=b[i]/scale;
		a[i]=a[i]/scale;
	}
	a[0]=1.0;
	free(temp);
}

 void bilinear_d(double d[],double c[],double b[],double a[],int n)
 {
	 int i,j,n1;
	 double sum,atmp,scale,*temp;

	 n1=n+1;
	 temp=(double *)malloc(n1*n1*sizeof(double));
	 for (j=0;j<=n;j++)
	 {
		 temp[j*n1+0]=1.0;
	 }
	 sum=1.0;
	 for(i=1;i<=n;i++)
	 {
		 sum=sum*(double)(n-i+1)/(double)i;
		 temp[0*n1+i]=sum;
	 }
	 for(i=1;i<=n;i++)
		 for(j=1;j<=n;j++)
		 {
			 temp[j*n1+i]=temp[(j-1)*n1+i]-temp[j*n1+i-1]-temp[(j-1)*n1+i-1];
		 }
		 for(i=n;i>=0;i--)
		 {
			 b[i]=0.0;
			 atmp=0.0;
			 for(j=0;j<=n;j++)
			 {
				 b[i]=b[i]+temp[j*n1+i]*d[j];
				 atmp=atmp+temp[j*n1+i]*c[j];
			 }
			 scale=atmp;
			 if(i!=0) a[i]=atmp;
		 }
		 for(i=0;i<=n;i++)
		 {
			 b[i]=b[i]/scale;
			 a[i]=a[i]/scale;
		 }
		 a[0]=1.0;
		 free(temp);
 }
static void fbltforlow(float d[],float c[],int n,float fln, float b[],float a[])
{
	int i,m,n1,n2;
	float w1;
	w1=tan((4.0*atan(1.0))*fln);
	for(i=n;i>=0;i--)
	{if((c[i]!=0.0)||(d[i]!=0.0))
	break;
	}
	m=i;
	n2=m;
	n1=n2+1;
	
	for (i=0;i<=m;i++)
	{
		d[i]=d[i]/pow(w1,i);
		c[i]=c[i]/pow(w1,i);
	}
	bilinear(d, c, b, a, n);
}

void ChebyII_Lowpassc(int Order,float f1,float f2,float dB,float *b,float *a)
{
	int k;
	float omega,lamda;
	float d[5],c[5];

	omega = warp(f2)/warp(f1);
	lamda = pow(10.0,(dB/20.0));

	for(k=0;k<Order/2;k++)
	{
		ChebyII(Order,k,4,omega,lamda,d,c);
		fbltforlow(d,c,2,f1,&b [k*(2+1)+0 ], &a [k*(2+1)+0 ]);
	}
}

////////////////////use matlab table///////////////////////
void SecOrSecMatlab(float *b,float *a,float *num,float *den,int ns)
{
	int i,j;
	for (i = 0; i<ns;i++)
	{
		for (j = 0;j<3;j++)
		{
			b[3*i+j] = num[3*(2*i)+0]*num[3*(2*i+1)+j];
			a[3*i+j] = den[3*(2*i)+0]*den[3*(2*i+1)+j];
		}
	}
	for (j = 0;j<3;j++)
	{
		b[3*(i-1)+j] *= num[3*(2*i)+0];
		a[3*(i-1)+j] *= den[3*(2*i)+0];
	}


}
void Hejw(float b[],float a[],int m,int n,float x[],float y[],float freq/*0~0.5*/,int sign)//ÆµÏì
{

	int i;
	int k;
	float ar,ai,br,bi,zr,zi,im,re,den,numr,numi,temp;

	if (freq>0.5)
	{
		freq = 0.5;
		//assert(0);
	}


	//for(k=0;k<len;k++)
	k = 0;
	{
		//freq=k*0.5/(len-1);
		zr=cos(-8.0*atan(1.0)*freq);
		zi=sin(-8.0*atan(1.0)*freq);

		br=0.0;
		bi=0.0;
		for(i=m;i>0;i--)
		{re=br;
		im=bi;
		br=(re+b[i])*zr-im*zi;
		bi=(re+b[i])*zi+im*zr;
		}
		ar=0.0;
		ai=0.0;
		for(i=n;i>0;i--)
		{
			re=ar;
			im=ai;
			ar=(re+a[i])*zr-im*zi;
			ai=(re+a[i])*zi+im*zr;
		}
		br=br+b[0];
		ar=ar+1.0;
		numr=ar*br+ai*bi;
		numi=ar*bi-ai*br;
		den=ar*ar+ai*ai;

		x[k]=numr/den;
		y[k]=numi/den;



		switch(sign)
		{
		case 1:
			{
				temp=sqrt(x[k]*x[k]+y[k]*y[k]);

				y[k]=atan2(y[k],x[k]);//////////////

				x[k]=temp;
				break;
			}

		case 2:
			{
				temp=x[k]*x[k]+y[k]*y[k];

				y[k]=atan2(y[k],x[k]);

				x[k]=10.0*log10(temp);

			}


		}


	}



}

float Aejw_dB(float b[],float a[],int m,int n,float freq/*0~0.5*/)//dB(|Hejw|)
{
	float x,y;
	Hejw(b,a,m,n,&x,&y,freq,2);
	return x;
}

void Hejwd(double b[],double a[],int m,int n,double x[],double y[],double freq/*0~0.5*/,int sign)//ÆµÏì
{

	int i;
	int k;
	double ar,ai,br,bi,zr,zi,im,re,den,numr,numi,temp;

	if (freq>0.5)
	{
		freq = 0.5;
		//assert(0);
	}


	//for(k=0;k<len;k++)
	k = 0;
	{
		//freq=k*0.5/(len-1);
		zr=cos(-8.0*atan(1.0)*freq);
		zi=sin(-8.0*atan(1.0)*freq);

		br=0.0;
		bi=0.0;
		for(i=m;i>0;i--)
		{re=br;
		im=bi;
		br=(re+b[i])*zr-im*zi;
		bi=(re+b[i])*zi+im*zr;
		}
		ar=0.0;
		ai=0.0;
		for(i=n;i>0;i--)
		{
			re=ar;
			im=ai;
			ar=(re+a[i])*zr-im*zi;
			ai=(re+a[i])*zi+im*zr;
		}
		br=br+b[0];
		ar=ar+1.0;
		numr=ar*br+ai*bi;
		numi=ar*bi-ai*br;
		den=ar*ar+ai*ai;

		x[k]=numr/den;
		y[k]=numi/den;



		switch(sign)
		{
		case 1:
			{
				temp=sqrt(x[k]*x[k]+y[k]*y[k]);

				y[k]=atan2(y[k],x[k]);//////////////

				x[k]=temp;
				break;
			}

		case 2:
			{
				temp=x[k]*x[k]+y[k]*y[k];

				y[k]=atan2(y[k],x[k]);

				x[k]=10.0*log10(temp);

			}


		}


	}



}

double Aejw_dBd(double b[],double a[],int m,int n,double freq/*0~0.5*/)//dB(|Hejw|)
{
	double x,y;
	Hejwd(b,a,m,n,&x,&y,freq,2);
	return x;
}

void FDAapfilter2(float *b,float *a,float r,float w)
{
	assert(r<1);
	assert(r>0);

	b[0] = r*r;
	b[1] = -2*r*cos(w);
	b[2] = 1;
	a[0] = 1;
	a[1] = -2*r*cos(w);
	a[2] = r*r;
}
void FDAapfilter1(float *b,float *a,float reala)//
{
	assert(fabs(reala)<1);
	assert(fabs(reala)>0);

	b[0] = -reala;
	b[1] = 1;

	a[0] = 1;
	a[1] = -reala;

}


void FDAbandEQfilter_A2D(float *b,float *a,float w0,float G,float deltaw,float GB,float G0)
{
	float beta;

	beta = sqrt((GB*GB-G0*G0)/(G*G-GB*GB))*tan(deltaw/2);

	b[0] = (G0 + G*beta)/(1 + beta);
	b[1] = -2*(G0 * cos(w0))/(1 + beta);
	b[2] = (G0 - G*beta)/(1 + beta);

	a[0] = 1;
	a[1] = -2*cos(w0)/(1 + beta);
	a[2] = (1 - beta)/(1 + beta);
}
void FDAbandEQfilter_A2D_d(double *b,double *a,double w0,double G,double deltaw,double GB,double G0)
{
	double beta;

	beta = sqrt((GB*GB-G0*G0)/(G*G-GB*GB))*tan(deltaw/2);

	b[0] = (G0 + G*beta)/(1 + beta);
	b[1] = -2*(G0 * cos(w0))/(1 + beta);
	b[2] = (G0 - G*beta)/(1 + beta);

	a[0] = 1;
	a[1] = -2*cos(w0)/(1 + beta);
	a[2] = (1 - beta)/(1 + beta);
}

float omiga(float f,float fs)
{
	return tan(SKR_PAI*f/fs);
}
double omiga_d(double f,double fs)
{
	return tan(SKR_PAI*f/fs);
}













