//
// Created by 昝晓飞 on 16/6/27.
//

#ifndef KSYSTREAMERANDROID_CIPHERUTILTY_H
#define KSYSTREAMERANDROID_CIPHERUTILTY_H

#include <list>
#include <string.h>

#include "CipherConstants.h"

typedef struct _CIPHER_CONTENT
{
    int   type;
    const char*  key;
    const char*  body;
}CIPHER_CONTENT;

typedef CIPHER_CONTENT ST_CIPHERCONTENT;

typedef struct _CIPHER_LIST_INFO
{
    int mCipherNumber;
    ST_CIPHERCONTENT* mCipherInfos;
}ST_CIPHER_INFO;

class CipherUtility {

public:
	CipherUtility( void ) {};
    virtual ~CipherUtility( void ) {};

    static CipherUtility * GetInstance();

    ST_CIPHER_INFO* GetCipherContentList( void );
};



#endif //KSYSTREAMERANDROID_CIPHERUTILTY_H
