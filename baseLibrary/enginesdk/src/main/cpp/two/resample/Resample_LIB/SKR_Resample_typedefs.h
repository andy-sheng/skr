#ifndef SKR_Resample_TYPEDEFS_H
#define SKR_Resample_TYPEDEFS_H

#define MAXNS 40

typedef struct Resample_channel_mem{
	/*mems:*/
	//short memfilterin[RESAMPLE_MAXORDER];//ע�⣬Ŀǰ�˲����ü�����û����Դ��������������Ż���������Ϊ������������ڲ���ٶȸ��졣�������ֱ�����˲��������������ϡ�����н����Ż�
	//float memfilterout[RESAMPLE_MAXORDER];//ǧ��ע�⡣����

	float mempx[MAXNS*(2+1)];//j
	float mempy[MAXNS*(2+1)];//j
	int memDindex;
	int memLindex;
	int memk;// 0��I-1֮��
	short meminput[1];//��Ϊȡ����x(n+1)���޷��㣬��һ֡ʱ����x(n+1)������û��x(n)����������������һ֡��ĩβ�Ա�ʹ��
	float meminternalinput[1];
}Resample_MEM;


#endif