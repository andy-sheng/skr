#include "../../common/functions.h"

void CalcuLinearParameter(int D,int I,int *xIndexDelta,float *w1,float *w2)
{
	int k;
	float DchuyiI = (float)D/I;

	xIndexDelta[0] = (int)(DchuyiI * 1) - (int)(DchuyiI*0);//n1的坐标减去0
	w1[0] = DchuyiI*(0+1) - (int)(DchuyiI * (0+1));
	w2[0] = 1-w1[0];
	for (k = 1;k < I;k++)
	{
		xIndexDelta[k] = (int)(MchuyiNchangyiK(D,I,k+1)) - (int)(MchuyiNchangyiK(D,I,k));
		w1[k] = MchuyiNchangyiK(D,I,k+1) - (int)(MchuyiNchangyiK(D,I,k+1));//w1k中最后一个元素装的是最后一个坐标的左距离，为0
		w2[k] = 1 - w1[k];
	}
}
void CalcuLinearParameter2(int D,int I,int *xIndexDelta,float *w1,float *w2)//暂时放下这种，目前有逻辑错误，只是为了初始化不用I-1,而用0
{
	int k;

	float DchuyiI = (float)D/I;
	//int IntPart_DchuyiI = DchuyiI;
	//float FracPart_DchuyiI = DchuyiI - IntPart_DchuyiI;

	xIndexDelta[0] = 0;//(int)(DchuyiI*(I)) - (int)(DchuyiI*(I-1));
	w1[0] = 1;//(DchuyiI*I) - (int)(DchuyiI*I);//w1k中最后一个元素装的是最后一个坐标的左距离，为0
	w2[0] = 1 - w1[0];

	for (k = 1;k < I;k++)
	{
		xIndexDelta[k] = (int)(DchuyiI*(k)) - (int)(DchuyiI*(k-1));
		w1[k] = (DchuyiI*k) - (int)(DchuyiI*k);//w1k中最后一个元素装的是最后一个坐标的左距离，为0
		w2[k] = 1 - w1[k];
	}
}

