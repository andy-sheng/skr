#include <stdlib.h>

int Gcd(int m,int n)//计算m，n的最大公约数http://zhidao.baidu.com/question/335690846.html
{
	int b,c;
	if(m>n)
		;
	else
		b=m,m=n,n=b;
	do
	{
		c=m%n;
		if(c==0)	break;	
		m=n;
		n=c;//a=n,n=c,m=a;
	}
	while(c>=1);
	return n;	
}


