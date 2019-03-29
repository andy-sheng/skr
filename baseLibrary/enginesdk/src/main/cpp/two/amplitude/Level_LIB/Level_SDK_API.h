//how to use?
//#include "Level_SDK_API.h"

//short pcminput[1024];
//float x_db[512];

//Level_s mlevel;
//mlevel.samplerate = samplerate;
//mlevel.channel = 2;
//mlevel.LevelRealeasems = 300;
//LevelReset_API(&mlevel);
//LevelCalcu_API(&mlevel);


//inlen为pcminput的元素个数，x_db是每个样本的峰值电平，如果channel=2，每个样本是两个样点，这两个样点有一个峰值电平，得到512个元素存于x_db
//这里的分贝是和最高电平相比较得到的，对于short样点，32768是最大值，对应分贝是0，对于样点值x，则为20*log10(x/32768.0)；
//levelrun先对波形进行“包络检波”，然后对输出以分贝单位输出
//LevelRun_API(&mlevel, pcminput, 1024, x_db);  

//这个run函数和上一个只能用一个，根据需要使用
//framedb = LevelRun_API_avgxdb(&mlevel, pcminput, 1024); 这个函数是得到这一帧数据的分贝的均值framedb





#ifndef LEVEL_SDK_API_H
#define LEVEL_SDK_API_H

#include "Level_control.h"

#ifdef __cplusplus
extern "C"
{
#endif

	void LevelReset_API(Level_s *mLevel);
	void LevelCalcu_API(Level_s *mLevel);
	void LevelRun_API(Level_s *mLevel,	const short *input,int inLen,	float *x_db);


	float LevelRun_API_avgxdb(Level_s *mLevel,const short *x,int xlen);

	void VWLevelReset_API_Peak(VWLevel_s *mVWLevel);
	void VWLevelRun_API_Peak(VWLevel_s *mVWLevel,float *x_db,int xlen);
	float Statistics_API(VWLevel_s *mVWLevel);


#ifdef __cplusplus
}
#endif

#endif