#ifndef COVREVERB_CONTROL_H
#define COVREVERB_CONTROL_H

#include "../../filter/Filter_LIB/Filter_SDK_API.h"
#include "../../Delay/Delay_LIB/Delay_SDK_API.h"

#ifdef __cplusplus
extern "C"
{
#endif

#define SMALLLHAMON     (1)
#define JUCAM           (2)
#define SMOKEYBAR       (3)
#define SHOWER          (4) 
#define LOWSHOWER       (5)

#define TWLRFRV018      (6)
#define TWLRFRV020      (7)
#define TWLRFRV025      (8)

#define THETRACKINGROOM (9)
#define DBYTAIL (10)
#define CD 11
#define TANDC 12

	typedef struct CovReverb_channel_memory{
		Filterlongfir_s mfir;
		Filterlongfir_s mfir2;
		Filterf_s iirf;

		int have;
		float dry;//bugfix  when kind5,wet must be 1.0

		int samplerate;
		int channelin;
		int channelout;
		int xsame;
		int reverbkind;
		float wet;//0-1
	}CovReverb_s;

	typedef struct CovReverbV2_channel_memory{//framelen can't change even recalcu
		FastCovVerylongh_s mfir;
		FastCovVerylongh_s mfir2;
		Buf16_s mDelay;
		Filterf_s iirf;

		int have;
		float dry;
		int fstdelay_n;//
		float h0;
		float h0R;

		int samplerate;
		int channelin;
		int channelout;
		int xsame;
		int reverbkind;
		float wet;//0-1
		int xframelen;//framelen/channel... !!
	}CovReverbV2_s;

	typedef struct covreverbV2framefree{//surport framelen change
		CovReverbV2_s mcrv2;
		Buf16_s bufin;
		Buf16_s bufout;
	}CovReverbV2framefree_s;

#ifdef __cplusplus
}
#endif

#endif