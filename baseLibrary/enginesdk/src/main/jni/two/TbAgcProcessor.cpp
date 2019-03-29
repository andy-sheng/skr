#include <jni.h>
#include <string>
#include "../../cpp/two/amplitude/libvoln/libvoln.h"

FILE *inputFile = NULL;

FILE *outputFile = NULL;

int flag = -1;

#define LOG_TAG "ITBEffectEngine"

#define FILEOPEN 1

extern "C"
JNIEXPORT jint JNICALL
Java_com_engine_effect_ITbAgcProcessor_init(JNIEnv *env, jobject ins) {
    flag = 0;
    return flag;
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_engine_effect_ITbAgcProcessor_process(JNIEnv *env, jobject ins, jbyteArray samplesJni,
                                                   jint len,
                                                   jint channels, jint sampleRate) {
    void *pvoln;
    FILE *frx,*fwy;
    int frame;
    int framelen;

    int samplerate;
    int channel;
    float maxgaindB;
    float fstgaindB;
    int needDC;
    int dykind;

    short y[6000];
    short x[6000];

    frx = fopen("input.pcm", "rb");
    fwy = fopen("output.pcm","wb");


    samplerate = 16000;
    channel = 1;

    maxgaindB = 20.f;
    fstgaindB = 15.f;
    needDC = 1;
    dykind = 0;

    framelen = (int)(samplerate*0.02);

    SKR_agc_create(&pvoln);
    SKR_agc_reset(pvoln);
    SKR_agc_config(pvoln,samplerate,channel,dykind,maxgaindB,fstgaindB,needDC);

    for(frame=0;;frame++)
    {
        if(fread(x, sizeof(short), framelen, frx) != framelen) break;

        SKR_agc_proc(pvoln, x, framelen,y);

        fwrite(y, sizeof(short), framelen, fwy);

        printf("Doing volume normalization...frame%d\r",frame);

    }

    printf("\nFininsh!\n\n\n");

    fclose(frx);
    fclose(fwy);

    SKR_agc_free(pvoln);

    return flag;
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_engine_effect_ITbAgcProcessor_destroyAgcProcessor(JNIEnv *env, jobject instance) {
    return flag;
}
