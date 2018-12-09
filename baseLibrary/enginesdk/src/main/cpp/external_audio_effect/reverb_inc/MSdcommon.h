/************************************************************************/
/* mix sound lib                                                        */
/* written by ethanzhao, 5.13, 2014										*/
/************************************************************************/

#ifndef KALA_MIX_SOUND_FILE_COMMON_H
#define KALA_MIX_SOUND_FILE_COMMON_H

/************************************************************************************************/
/*									return value meanings									    */
#define err_kala_audio_base_h_null					-1        // point is NULL
#define err_kala_audio_base_h_param_invalid			-2	      // invalid parameter
#define err_kala_audio_base_h_malloc_null			-3        // malloc return NULL
#define err_kala_audio_base_h_in_open_file			-4        // error when open text or audio file
#define err_kala_audio_base_h_in_no_enough_data		-5        // no enough input data
#define err_kala_audio_base_h_in_data_overflow		-6        // data overflow
#define err_kala_audio_base_h_in_audio_tpye			-7        // audio type error,not 16 bit maybe
#define err_kala_audio_base_h_lib_init				-8        // lib init or do error.
#define err_kala_audio_base_h_sentence_file_error	-9		  // error when init sentence file
#define err_kala_audio_base_h_note_file_error		-10		  // error when init note file.
#define err_kala_audio_base_h_section_info_error	-11		  // section info error
#define err_kala_audio_base_h_qrc_only_one_sentence -12		  // qrc only has one sentence;
#define err_kala_audio_base_h_clipped_happened      -13       // clipped happened when process audio data
#define err_kala_audio_base_h_unknown				-100      // unknown error

enum
{
	err_kala_audio_base_h_success									= 0,				/* 0	Success.  **/
	err_kala_audio_base_h_sl_error_to_get_result					= 1,				/* 1	Success and ,use in function putbuffer.  **/
	err_kala_audio_base_h_sl_error_to_get_last_result				= 2,				/* 2	Success.  **/
	err_kala_audio_base_h_ts_vad_ok									= 1,				/* 1	Success.  **/
	err_kala_audio_base_h_ts_stoped									= 2,				/* 1	Success.  **/
	err_kala_audio_base_h_sl_error_param							= -29999,			
	err_kala_audio_base_h_sl_error_init,			
	err_kala_audio_base_h_sl_error_mfcc,			
	err_kala_audio_base_h_sl_error_dict_path,		
	err_kala_audio_base_h_sl_error_input_word,	
	err_kala_audio_base_h_sl_error_dec_init,	
	err_kala_audio_base_h_sl_error_dec_sp,	
	err_kala_audio_base_h_sl_error_word_num,
	err_kala_audio_base_h_sl_error_qrc_init,
	err_kala_audio_base_h_sl_error_mle_init,
	err_kala_audio_base_h_sl_error_pitch_init,
	err_kala_audio_base_h_sl_error_qrc_file,
	err_kala_audio_base_h_sl_error_note_file,
	err_kala_audio_base_h_sl_error_qrc_combine,
	err_kala_audio_base_h_sl_error_buffer_length,
	err_kala_audio_base_h_sl_error_sent_id,
	err_kala_audio_base_h_sl_error_start_session,
	err_kala_audio_base_h_sl_error_mde_word,	
	err_kala_audio_base_h_sl_error_get_pitch,	
	err_kala_audio_base_h_sl_error_pitch_time,	
	err_kala_audio_base_h_sl_error_buffer_time,	
	err_kala_audio_base_h_sl_error_sent_time,	
	err_kala_audio_base_h_sl_error_not_last,	
	err_kala_audio_base_h_sl_error_is_last,	
	err_kala_audio_base_h_sl_error_miss_buffer,	
	err_kala_audio_base_h_sl_error_no_create,	
	err_kala_audio_base_h_sl_error_no_word,	
	err_kala_audio_base_h_ts_error_init								= -39999,
	err_kala_audio_base_h_ts_error_no_support,
	err_kala_audio_base_h_ts_error_param,
	err_kala_audio_base_h_ts_error_dec_init,
	err_kala_audio_base_h_ts_error_sts_init,
	err_kala_audio_base_h_ts_error_pitch,
	err_kala_audio_base_h_ts_error_seg_id,
	err_kala_audio_base_h_ts_error_no_buffer,
	err_kala_audio_base_h_ts_error_out_buffer,
	err_kala_audio_base_h_ts_error_noise,
	err_kala_audio_base_h_ts_error_word								= -38999,
	err_kala_audio_base_h_ts_error_vad								= -37999,
	err_kala_audio_base_h_ts_error_sts_proc							= -36999,
};

/* err_kala_audio_base_h_low_level_prefix:
 desc: Low Level lib (such as webrtc) error occurs.
       To get the corresponding low level error, plus err_kala_audio_base_h_low_level_base

 example:
       int err = XXX();

       if (err < err_kala_audio_base_h_low_level_prefix)
       {
           // Low Level error occurs.
           int errLL = err + err_kala_audio_base_h_low_level_base;

       }
*/

#define err_kala_audio_base_h_low_level_base		-10000      
#define ok_mix_sound_h								0         // successful
/************************************************************************************************/

#include "stdlib.h"
#include "string.h"

//#define TEST_FOR_DEBUG_LOG 1

/*
* common file of this project,some type defines,and the buffer use the original common file
*/

#ifndef AudioSample
#define AudioSample short
#endif

typedef AudioSample Asample;
//
//#ifndef WORD32
//#define WORD32 int
//#endif
//
//#ifndef UWORD32
//#define UWORD32 unsigned WORD32
//#endif
//
//#ifndef WORD16
//#define WORD16 short
//#endif
//
//#ifndef UWORD16
//#define UWORD16 unsigned WORD16
//#endif
//
//#ifndef HRESULT
//#define HRESULT WORD32
//#endif
//
//#ifndef HANDLE
//#define HANDLE void*
//#endif

//#ifndef max
//#define max(a,b)            (((a) > (b)) ? (a) : (b))
//#endif
//
//#ifndef min
//#define min(a,b)            (((a) < (b)) ? (a) : (b))
//#endif

#ifndef SAFE_FREE
#define SAFE_FREE(p)       { if(p) { free(p);     (p)=NULL; } }
#endif    

#ifndef SAFE_RELEASE
#define SAFE_RELEASE(p)      { if(p) { (p)->Release(); (p)=NULL; } }
#endif

#ifndef SHORTMAX
#define SHORTMAX 32767
#endif
#ifndef SHORTMIN
#define SHORTMIN -32768
#endif

#ifndef INT32_MAX
#define INT32_MAX (WORD32)0x7fffffffL
#endif
#ifndef INT32_MIN
#define INT32_MIN (WORD32)0x80000000L
#endif

#ifndef Clip_short	
#define Clip_short(x) (short)((x)>SHORTMAX?SHORTMAX:((x)<SHORTMIN?SHORTMIN:(x)))
#endif

#ifndef TWO_PI
#define TWO_PI	(2*3.1415f)
#endif


typedef struct _tagSentenceTime
{
	int bgn;
	int ed;
}SentTime;


#define CHECK_INPUT(p)  { if((p) == NULL) return err_kala_audio_base_h_null;}
#define CHECK_MALLOC(p) { if((p) == NULL) return err_kala_audio_base_h_malloc_null;} 
#define CHECK_RETURN(x) { if((x)<0) return x;}


#ifndef safe_free
#define safe_free(p)      { if(p) { free(p); (p)=NULL; } }
#endif

#define KALA_DEFAULT_SAMPLE_RATE		44100
#define KALA_DEFAULT_CHANNEL			1

#define KALA_NOTE_SCORE_RATE			60
#define KALA_MELODY_SCORE_RATE			40

#define KALA_TIME_BLOCK_COUNT			3

#define KALA_SCORE_SENTENCE_SCORE_MAX	100
#define KALA_SCORE_SENTENCE_SCORE_MIN	0

#define KALA_NOTE_UI_SILENCE			0//38

#define KALA_NOTE_UI_MIN				0		// min note value for ui.
#define KALA_NOTE_UI_MAX				100		// max note value for ui.

#endif

