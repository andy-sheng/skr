//
// Created by 昝晓飞 on 16/6/27.
//

#include "CipherUtility.h"
#include "jni/util/jni_cache.h"

//all cipher shader 14
ST_CIPHERCONTENT stCipherContents[] = {
    {BEAUTY_DENOISE_FILTER, FRAGMENT_SHADER_BEAUTY_DENOISE_KEY, FRAGMENT_SHADER_BEAUTY_DENOISE},
    {BEAUTY_SOFT_FILTER, FRAGMENT_SHADER_BEAUTY_SOFT_KEY, FRAGMENT_SHADER_BEAUTY_SOFT},
    {BEAUTY_ILLUSION_FILTER, FRAGMENT_SHADER_BEAUTY_ILLUSION_KEY, FRAGMENT_SHADER_BEAUTY_ILLUSION},
    {BEAUTY_SKIN_WHITEN_FILTER, FRAGMENT_SHADER_BEAUTY_SKINWHITEN_KEY, FRAGMENT_SHADER_BEAUTY_SKINWHITEN},
    {BEAUTY_SMOOTH_FILTER, FRAGMENT_SHADER_BEAUTY_SMOOTH_KEY, FRAGMENT_SHADER_BEAUTY_SMOOTH},
    {BEAUTY_SHARPEN_FILTER, FRAGMENT_SHADER_BEAUTY_SHARPEN_KEY, FRAGMENT_SHADER_BEAUTY_SHARPEN},
    {BEAUTY_SOFT_EXT_FILTER, FRAGMENT_SHADER_BEAUTY_SOFT_EXT_KEY, FRAGMENT_SHADER_BEAUTY_SOFT_EXT},
    {BEAUTY_SKIN_DETECT_FILTER, FRAGMENT_SHADER_BEAUTY_SKIN_DETECT_KEY, FRAGMENT_SHADER_BEAUTY_SKIN_DETECT},
    {BEAUTY_GRIND_FACE_FILTER, FRAGMENT_SHADER_BEAUTY_GRIND_FACE_KEY, FRAGMENT_SHADER_BEAUTY_GRIND_FACE},
    {BEAUTY_LOOK_UP_FILTER, FRAGMENT_SHADER_BEAUTY_LOOK_UP_KEY, FRAGMENT_SHADER_BEAUTY_LOOK_UP},
    {BEAUTY_GRIND_SIMPLE_FILTER, FRAGMENT_SHADER_BEAUTY_GRIND_SIMPLE_KEY, FRAGMENT_SHADER_BEAUTY_GRIND_SIMPLE},
    {BEAUTY_ADJ_SKIN_COLOR_FILTER, FRAGMENT_SHADER_BEAUTY_ADJ_SKIN_COLOR_KEY, FRAGMENT_SHADER_BEAUTY_ADJ_SKIN_COLOR},
    {BEAUTY_GRIND_ADVANCE_FILTER, FRAGMENT_SHADER_BEAUTY_GRIND_ADVANCE_KEY,
            FRAGMENT_SHADER_BEAUTY_GRIND_ADVANCE},
    {BEAUTY_1977_FILTER,FRAGMENT_SHADER_BEAUTY_1977_KEY,FRAGMENT_SHADER_BEAUTY_1977},
    {BEAUTY_AMARO_FILTER,FRAGMENT_SHADER_BEAUTY_AMARO_KEY,FRAGMENT_SHADER_BEAUTY_AMARO},
    {BEAUTY_BRANNAN_FILTER,FRAGMENT_SHADER_BEAUTY_BRANNAN_KEY,FRAGMENT_SHADER_BEAUTY_BRANNAN},
    {BEAUTY_EARLY_BIRD_FILTER,FRAGMENT_SHADER_BEAUTY_EARLY_BIRD_KEY,FRAGMENT_SHADER_BEAUTY_EARLY_BIRD},
    {BEAUTY_HEFE_FILTER,FRAGMENT_SHADER_BEAUTY_HEFE_KEY,FRAGMENT_SHADER_BEAUTY_HEFE},
    {BEAUTY_HUDSON_FILTER,FRAGMENT_SHADER_BEAUTY_HUDSON_KEY,FRAGMENT_SHADER_BEAUTY_HUDSON},
    {BEAUTY_INK_FILTER,FRAGMENT_SHADER_BEAUTY_INK_KEY,FRAGMENT_SHADER_BEAUTY_INK},
    {BEAUTY_LOMO_FILTER,FRAGMENT_SHADER_BEAUTY_LOMO_KEY,FRAGMENT_SHADER_BEAUTY_LOMO},
    {BEAUTY_LORD_KELVIN_FILTER,FRAGMENT_SHADER_BEAUTY_LORD_KELVIN_KEY,FRAGMENT_SHADER_BEAUTY_LORD_KELVIN},
    {BEAUTY_NASHVILLE_FILTER,FRAGMENT_SHADER_BEAUTY_NASHVILLE_KEY,FRAGMENT_SHADER_BEAUTY_NASHVILLE},
    {BEAUTY_RISE_FILTER,FRAGMENT_SHADER_BEAUTY_RISE_KEY,FRAGMENT_SHADER_BEAUTY_RISE},
    {BEAUTY_SIERRA_FILTER,FRAGMENT_SHADER_BEAUTY_SIERRA_KEY,FRAGMENT_SHADER_BEAUTY_SIERRA},
    {BEAUTY_SUTRO_FILTER,FRAGMENT_SHADER_BEAUTY_SUTRO_KEY,FRAGMENT_SHADER_BEAUTY_SUTRO},
    {BEAUTY_TOASTER_FILTER,FRAGMENT_SHADER_BEAUTY_TOASTER_KEY,FRAGMENT_SHADER_BEAUTY_TOASTER},
    {BEAUTY_VALENCIA_FILTER,FRAGMENT_SHADER_BEAUTY_VALENCIA_KEY,FRAGMENT_SHADER_BEAUTY_VALENCIA},
    {BEAUTY_WALDEN_FILTER,FRAGMENT_SHADER_BEAUTY_WALDEN_KEY,FRAGMENT_SHADER_BEAUTY_WALDEN},
    {BEAUTY_XPROLL_FILTER,FRAGMENT_SHADER_BEAUTY_XPROLL_KEY,FRAGMENT_SHADER_BEAUTY_XPROLL},
 };

static CipherUtility _ginCipherUtility;
CipherUtility * CipherUtility::GetInstance( void )
{
    return &_ginCipherUtility;
}

/**
* return all cipher
*/
ST_CIPHER_INFO* CipherUtility::GetCipherContentList( void ) {
    ST_CIPHER_INFO* cipherInfos = new ST_CIPHER_INFO();
    cipherInfos->mCipherNumber = INFO_NUMBER(stCipherContents);
    cipherInfos->mCipherInfos = stCipherContents;
    return cipherInfos;
}

CipherUtility *GetCipherUtilityInstance()
{
	return CipherUtility::GetInstance();
}