#ifndef SKR_Resample_TYPEDEFS_H
#define SKR_Resample_TYPEDEFS_H

#define MAXNS 40

typedef struct Resample_channel_mem{
	/*mems:*/
	//short memfilterin[RESAMPLE_MAXORDER];//注意，目前滤波采用级联，没有针对大幅补零情况进行优化，这是因为这种情况线性内插的速度更快。如果采用直接型滤波方案，可以针对稀疏序列进行优化
	//float memfilterout[RESAMPLE_MAXORDER];//千万注意。。。

	float mempx[MAXNS*(2+1)];//j
	float mempy[MAXNS*(2+1)];//j
	int memDindex;
	int memLindex;
	int memk;// 0到I-1之间
	short meminput[1];//因为取不到x(n+1)，无法算，下一帧时有了x(n+1)，但是没了x(n)，这里用它来存上一帧的末尾以备使用
	float meminternalinput[1];
}Resample_MEM;


#endif