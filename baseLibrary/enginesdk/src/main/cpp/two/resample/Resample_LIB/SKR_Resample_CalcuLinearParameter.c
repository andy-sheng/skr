#include "../../common/functions.h"

void CalcuLinearParameter(int D,int I,int *xIndexDelta,float *w1,float *w2)
{
	int k;
	float DchuyiI = (float)D/I;

	xIndexDelta[0] = (int)(DchuyiI * 1) - (int)(DchuyiI*0);//n1�������ȥ0
	w1[0] = DchuyiI*(0+1) - (int)(DchuyiI * (0+1));
	w2[0] = 1-w1[0];
	for (k = 1;k < I;k++)
	{
		xIndexDelta[k] = (int)(MchuyiNchangyiK(D,I,k+1)) - (int)(MchuyiNchangyiK(D,I,k));
		w1[k] = MchuyiNchangyiK(D,I,k+1) - (int)(MchuyiNchangyiK(D,I,k+1));//w1k�����һ��Ԫ��װ�������һ�����������룬Ϊ0
		w2[k] = 1 - w1[k];
	}
}
void CalcuLinearParameter2(int D,int I,int *xIndexDelta,float *w1,float *w2)//��ʱ�������֣�Ŀǰ���߼�����ֻ��Ϊ�˳�ʼ������I-1,����0
{
	int k;

	float DchuyiI = (float)D/I;
	//int IntPart_DchuyiI = DchuyiI;
	//float FracPart_DchuyiI = DchuyiI - IntPart_DchuyiI;

	xIndexDelta[0] = 0;//(int)(DchuyiI*(I)) - (int)(DchuyiI*(I-1));
	w1[0] = 1;//(DchuyiI*I) - (int)(DchuyiI*I);//w1k�����һ��Ԫ��װ�������һ�����������룬Ϊ0
	w2[0] = 1 - w1[0];

	for (k = 1;k < I;k++)
	{
		xIndexDelta[k] = (int)(DchuyiI*(k)) - (int)(DchuyiI*(k-1));
		w1[k] = (DchuyiI*k) - (int)(DchuyiI*k);//w1k�����һ��Ԫ��װ�������һ�����������룬Ϊ0
		w2[k] = 1 - w1[k];
	}
}

