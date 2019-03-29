#ifndef NATUREMIX_CONTROL_H
#define NATUREMIX_CONTROL_H

#include "../../amplitude/Dynamic_LIB/Dynamic_SDK_API.h"
#include "../../amplitude/Level_LIB/Level_SDK_API.h"

#define MAXMIXNUM 40

#ifdef __cplusplus
extern "C"
{
#endif

	typedef struct NatureMix_memory{
		Dynamic_ID DyForNatureMix;
		Level_s LevelForNatureMix;

		float memlastframeweight[MAXMIXNUM];
		int memfstframe;

		int chanel;
		int samplerate;
		int DADD;//�Ժ���Щ�����Ƶ���߰ɡ��Ż�ʱ��˵������
		int mixnum;//����·��
		float weight[MAXMIXNUM];
		int sidechain; //-1 out level;0 not use(out dy's sidechain)
		int comlim;//1:comlim,0:no comlim

	}NatureMix_ID;



#ifdef __cplusplus
}
#endif

#endif