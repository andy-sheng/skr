#ifndef SKR_FAGC_DEFINES_H
#define SKR_FAGC_DEFINES_H

#ifdef __cplusplus
extern "C"
{
#endif

#if defined(_WIN32) || defined(WIN32)
#define ISPC (1)//a temp define
#else
#define ISPC (0)//a temp define
#endif

//#define NOISE_UP_THRESHOLD (150)
#define NOISE_UP_THRESHOLD (120)
//#define NOISE_UP_THRESHOLDVAD (40)//for VAD
#define NOISE_UP_THRESHOLDVAD (100)//for VAD temp
#define MAXSOMEIN_UPDATE_WINDOW (210)
#define MEMPREAVGDB (100)
#define NOISE_DB_MAX (-17.0)
#define NOISE_DB_MIN (-69.0)

#if ISPC
#define MAXGAIN (7.0)
#define MINGAIN (0.0)
#else
#define MAXGAIN (7.0)
#define MINGAIN (0.0)
#endif

#define NOISE_TROUBLE_START (-49.5)//(-60.0)//(-55.0)//
#define NOISE_TROUBLE_COUT (-42.5)//(-42.5)//(-45.0)//

#define K3ZUO (0.37)//curv第三个拐点以左的线段的斜率
#define K1YOU (1.73)//curv第一个拐点以右的线段的斜率

#define CLIPPING_TTHRESHOLD (1.36e-3)
#define CLIPPING_SERIOUS_TTHRESHOLD (2.18e-3)
#define DERIVATIVE_TTHRESHOLD (800.0)
#define PEAK_THRESHOLD (9000)
#define FULL_LOAD (0.973)

#define BOOST_MINNOISE_DB (-40.0)
#if ISPC
#define VOL_MINNOISE_DB (-40.0) //meeting it should be more
#define VVOL_BUBBLE_THRESHOLD (-57)
#define VVOL_BUBBLE_MOD (15)
#define STHOLDDB (-95.0)
#define FSTAPDOWN (500)
#else
#define VOL_MINNOISE_DB (-41.0) //vol and vvol should also have this
#define VVOL_BUBBLE_THRESHOLD (-68)
#define VVOL_BUBBLE_MOD (24)
#define STHOLDDB (-50.0)
#define FSTAPDOWN (300)
#endif

#define SPEECH_END_PROTECTION (11)
#define SPEECH_END_PROTECTION_FOR_USE_P (100)
#define CS_END_PROTECTION (20)
#define SS_END_PROTECTION (0)
    
#define SPEECH_END_PROTECTION_GATE (4)

#define T1_DB 9.8
#define T2_DB 16.0
#define TVAD_DB 8.7

#define ZEROINPUT_THRESHOLD (0) //-48db 100:-50.3db sol cut problem;if don't solve set it to 0
#define DY_NS_IDB (0.55)


#define DIGITALGAIN_NOBOOST (1.0)
#define DIGITALGAIN_BOOST_XP_FIRST (1.0)
//#define DIGITALGAIN_BOOST_XP_OFF (1.0)
//#define DIGITALGAIN_BOOST_XP_ON (1.0)
//#define DIGITALGAIN_BOOST_W7_3 (1.0)
//#define DIGITALGAIN_BOOST_W7_2 (1.0)
//#define DIGITALGAIN_BOOST_W7_1 (1.0)



//#define N_Down 200
//#define N_UP 130
//#define K 0.6
//#define M 130
//#define F 20 
//#define X 30


//speed limit settings
#if ISPC
#define BEGINING_TIME (15000) //ms 
#define RVOL_UP_HOLD_STEADY_TIME (4000)
#define RVOL_DOWN_HOLD_STEADY_TIME (200)
#define VVOL_UP_HOLD_STEADY_TIME (3000)//4000 3500
#define VVOL_DOWN_HOLD_STEADY_TIME (800)//2000

#define RVOL_UP_HOLD_BEGINING_TIME (2500)
#define RVOL_DOWN_HOLD_BEGINING_TIME (200)
#define VVOL_UP_HOLD_BEGINING_TIME (600)//(2000)//4000//vvolgain is after noisegetproc so fast vvup is allowed 
#define VVOL_DOWN_HOLD_BEGINING_TIME (300)//1000 
#else
#define BEGINING_TIME (17000) //ms
#define RVOL_UP_HOLD_STEADY_TIME (4000)
#define RVOL_DOWN_HOLD_STEADY_TIME (200)
#define VVOL_UP_HOLD_STEADY_TIME (3000)//vvolgain is after noisegetproc so fast vvup is allowed////if we use VMICECNS or GAINECNS we must getnoise before the vvgain.
#define VVOL_DOWN_HOLD_STEADY_TIME (800)//vvolgain is after noisegetproc so fast vvup is allowed 

#define RVOL_UP_HOLD_BEGINING_TIME (2500)
#define RVOL_DOWN_HOLD_BEGINING_TIME (200)
#define VVOL_UP_HOLD_BEGINING_TIME (1300)//vvolgain is after noisegetproc so fast vvup is allowed 
#define VVOL_DOWN_HOLD_BEGINING_TIME (400) //vvolgain is after noisegetproc so fast vvup is allowed 
#endif

#define LIMVOL (0.01300f) //>0.0487632562752728
#define TARD_SMSCMOD_DB (3.0) //Speeker mic "short circuit"

#define SMALLDB (-26.0)
#define SMALL_SMSCMOD_DB (-30.0) 



#define TARD_VMICMOD_DB (0.0) //virmicmod
#define CURV_B (-1.2)

#define VOLFORFB_5 (1.0)


////////////////////////////////ecnlp.h//////////////////////////////////////////


#define EC_RESULT_DB (22.0) //has ns's 10.0
#define EC_RESILT_SSMOD_DB (-5.0)
#define EC_RESULT_2_DB (10.0)

#define ES_CMP (0.00001)
#define SS_FB_MOD_DB (3.0)
#define NORMAL_FB_MOD_DB (2.0)
#define NSS_SS_VOL (0.4)
#define SSVOLLOW (0.3)//now we don't use it



//////////////////////////////////mic.h////////////////////////////////////////

/*capdev capability(...vvol,vboost,vol,boost,Sublevel,allowup)*/
#define CAPABILITY_TWOMIC	   (64) //force to use vmic:fb decide xpboost use mic's or vmic's or all,if vmic is used the bit is 1(ps:if the caller finds boost failed it can cheat a xpboost condition by modifying boostinf to agc and call agccalu again or call setmic separately
#define CAPABILITY_VVOL        (32)
#define CAPABILITY_VBOOST	   (16)
#define CAPABILITY_RVOL         (8)
#define CAPABILITY_RBOOST       (4)
#define CAPABILITY_RSUBLEVEL    (2)
#define CAPABILITY_RALLOWUP     (1)//allow real mic boost up

/*conditions:*/
#if ISPC
#define FIRSTVIRTUALVOL_DB (6) //gainmod_dB
#define VVOLMIN_DB (0.0)
#define VVOLMAX_DB (36.0)//same with mobile!
#else
#define FIRSTVIRTUALVOL_DB (15.0) //gainmod_dB
#define VVOLMIN_DB (0.0)
#define VVOLMAX_DB (36.0)//36? infact if the device is normal,30 is ok
#endif
#define VBOOST_MIN_DB (0.0)
#define VBOOST_MAX_DB (30.0)
#define VBOOST_STEP_DB (10.0)

#define VVOLMOD (0.0)//
#define RVOLMOD (3.0)
#define SSGAIN_MINFIRST_DB (1.0)
////////////////////////////////fb.h//////////////////////////////////////////
#define RMIC_NORMAL (10)
#define RMIC_XPBOOST_USEVBOOST (-20)
#define NORMIC_NOTUSEVBOOST (-5)
#define NORMIC_ALLUSE (-1)
#define RMIC_XPBOOST_USEVBOOST_NOBOOSTUP (-30)//not finished
////////////////////////////////dy.h//////////////////////////////////////////
//#define DY_NLPENHANCE       (16)
#define DY_BUBBLE_DTXCNG (2048)
#define DY_VARIBLECUR_BUBBLE (1024)
#define DY_NSGATE (512)
#define DY_STATIC_RADIOLIMIT (256)
#define DY_NSGATE_ONLY (128)//this means if (dy & DY_NSGATE_ONLY == 1) dy only like a noise gate, this is for the ec now
#define DY_NS (64)
#define DY_VAD_EXP_CTRL (32)
#define DY_VAD_INTERNAL_CTRL (16) 
#define DY_NLP       (8)
#define DY_VARIBLECUR    (4)
#define DY_SPLIT     (2)//allow real mic boost up
#define DY_ENABLE (1)


//////////////////////////////////////////////////////////////////////////
#define BASEDECAY  (20.f)
#define BASEFAREFFECT  (2.5f)
#define BASENOISYTHRESHOLD (400.f)
#define CUTPROBADD (35)



#ifdef __cplusplus
}
#endif

#endif