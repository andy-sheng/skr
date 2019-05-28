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


//inlenΪpcminput��Ԫ�ظ�����x_db��ÿ�������ķ�ֵ��ƽ�����channel=2��ÿ���������������㣬������������һ����ֵ��ƽ���õ�512��Ԫ�ش���x_db
//����ķֱ��Ǻ���ߵ�ƽ��Ƚϵõ��ģ�����short���㣬32768�����ֵ����Ӧ�ֱ���0����������ֵx����Ϊ20*log10(x/32768.0)��
//levelrun�ȶԲ��ν��С�����첨����Ȼ�������Էֱ���λ���
//LevelRun_API(&mlevel, pcminput, 1024, x_db);  

//���run��������һ��ֻ����һ����������Ҫʹ��
//framedb = LevelRun_API_avgxdb(&mlevel, pcminput, 1024); ��������ǵõ���һ֡���ݵķֱ��ľ�ֵframedb





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