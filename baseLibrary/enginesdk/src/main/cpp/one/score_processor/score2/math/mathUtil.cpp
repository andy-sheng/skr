#ifdef __APPLE__
#include <Accelerate/Accelerate.h>
#endif
#ifdef __ANDROID__
#include "NE10.h"
#endif
#include "mathUtil.hpp"
#include "audio_engine_common.h"
#include <assert.h>
#include <string.h>
#define LOG_TAG "MathUtil"

#include <algorithm>
using namespace std;

CMathUtil g_mathUtilInstance;
static CMathUtil::AccelerateType g_accelerateType;

CMathUtil::CMathUtil() {
    g_accelerateType = NONACCELERATE;
#ifdef __NON_ACCELERATE__
    g_accelerateType = NONACCELERATE;
#elif defined(__APPLE__)
    g_accelerateType = APPLE_IOS_ACC;
#elif defined(__ANDROID__)
    if (ne10_init() != NE10_OK) {
        LOGE("Failed to initialise Ne10.");
        g_accelerateType = NONACCELERATE;
    } else {
        g_accelerateType = ANDROID_NE10;
    }
    LOGI("RUN in CMathUtil::CMathUtil()");
#endif
}

CMathUtil::AccelerateType CMathUtil::getAccelerateStatus(){
    return g_accelerateType;
}

void CMathUtil::SetAccelerateType(AccelerateType acctype){
    #ifdef DEBUG
    g_accelerateType = acctype;
    #endif
}
CMathUtil::~CMathUtil() {
}

void CMathUtil::FFTMag(const float inputRe[], const float inputIm[], float magoutput[], int len) {
    switch (g_accelerateType) {
        case NONACCELERATE: {
            for (int i = 0; i < len; i++) {
                magoutput[i] = sqrtf(  (inputRe[i] * inputRe[i]) + (inputIm[i] * inputIm[i]));
            }
            break;
        }
#ifdef __APPLE__
        case APPLE_IOS_ACC: {
            DSPSplitComplex tempSplitComplex;
            tempSplitComplex.realp = (float*)inputRe;
            tempSplitComplex.imagp = (float*)inputIm;
            vDSP_zvabs(&tempSplitComplex, 1, magoutput, 1, len);
            break;
        }
#elif defined(__ANDROID__)
        case ANDROID_NE10: {
            ne10_float32_t* temp = (ne10_float32_t*) NE10_MALLOC (len * sizeof (ne10_float32_t));
            ne10_mul_float_neon((ne10_float32_t*)temp, (ne10_float32_t*)inputRe, (ne10_float32_t*)inputRe, len);
            ne10_mul_float_neon((ne10_float32_t*)magoutput, (ne10_float32_t*)inputIm, (ne10_float32_t*)inputIm, len);
            //ne10_mul_float_neon((ne10_float32_t*)magoutput, temp, (ne10_float32_t*)magoutput, len);
            ne10_add_float_neon((ne10_float32_t*)magoutput, (ne10_float32_t*)temp, (ne10_float32_t*)magoutput, len);
            NE10_FREE(temp);
            for (int i = 0; i < len; i++) {
                magoutput[i] = sqrtf(magoutput[i]);
            }
        }
#endif
        default:
            assert(0);
            break;
    }
}

void CMathUtil::SquaredAddArray(const float inputRe[], const float inputIm[], float output[], int len){
    switch (g_accelerateType) {
        case NONACCELERATE: {
            for (int i = 0; i < len; i++) {
                output[i] = sqrtf(  (inputRe[i] * inputRe[i]) + (inputIm[i] * inputIm[i]));
            }
            break;
        }
#ifdef __APPLE__
        case APPLE_IOS_ACC: {
            DSPSplitComplex tempSplitComplex;
            tempSplitComplex.realp = (float*)inputRe;
            tempSplitComplex.imagp = (float*)inputIm;
            vDSP_zvmags(&tempSplitComplex, 1, output, 1, len);
            break;
        }
#elif defined(__ANDROID__)
        case ANDROID_NE10: {
            ne10_float32_t* temp = (ne10_float32_t*) NE10_MALLOC (len * sizeof (ne10_float32_t));
            ne10_mul_float_neon((ne10_float32_t*)temp, (ne10_float32_t*)inputRe, (ne10_float32_t*)inputRe, len);
            ne10_mul_float_neon((ne10_float32_t*)output, (ne10_float32_t*)inputIm, (ne10_float32_t*)inputIm, len);
            ne10_add_float_neon((ne10_float32_t*)output, (ne10_float32_t*)temp, (ne10_float32_t*)output, len);
            NE10_FREE(temp);
        }
#endif
        default:
            assert(0);
            break;
    }
}

void CMathUtil::FFTPhase(const float inputRe[], const float inputIm[], float phaseput[], int len) {
    switch (g_accelerateType) {
#ifdef __APPLE__
        case APPLE_IOS_ACC: {
            DSPSplitComplex tempSplitComplex;
            tempSplitComplex.realp = (float*)inputRe;
            tempSplitComplex.imagp = (float*)inputIm;
            vDSP_zvphas(&tempSplitComplex, 1, phaseput, 1, len);
            break;
        }
#endif
        case NONACCELERATE:
        case ANDROID_NE10: {
            for (int i = 0; i < len; i++) {
                phaseput[i] = atan2f(inputRe[i], inputIm[i]);
            }
            break;
        }
        default:
            assert(0);
            break;
    }
}

void CMathUtil::MultiArrayByScale(const float input[], float output[], float scale, int len) {
    switch (g_accelerateType) {
        case NONACCELERATE: {
            for (int i = 0; i < len; i++) {
                output[i] = input[i] * scale;
            }
            break;
        }
#ifdef __APPLE__
        case APPLE_IOS_ACC: {
            vDSP_vsmul(input, 1, &scale, output, 1, len);
            break;
        }
#elif defined(__ANDROID__)
        case ANDROID_NE10: {
            ne10_mulc_float((ne10_float32_t*)output, (ne10_float32_t*)input, scale, len);
            break;
        }
#endif
        default:
            assert(0);
            break;
    }
}

void CMathUtil::MultiArrayByScale(const double input[], double output[], double scale, int len){
    switch (g_accelerateType) {
#ifdef __APPLE__
        case APPLE_IOS_ACC: {
            vDSP_vsmulD(input, 1, &scale, output, 1, len);
            break;
        }
#endif
        case ANDROID_NE10:
        case NONACCELERATE: {
            for (int i = 0; i < len; i++) {
                output[i] = input[i] * scale;
            }
            break;
        }

        default:
            assert(0);
            break;
    }
}

void CMathUtil::DivArrayByScale(const float input[], float output[], float scale, int len) {
    switch (g_accelerateType) {
        case NONACCELERATE: {
            for (int i = 0; i < len; i++) {
                output[i] = input[i] / scale;
            }
            break;
        }
#ifdef __APPLE__
        case APPLE_IOS_ACC: {
            vDSP_vsdiv(input, 1, &scale, output, 1, len);
            break;
        }
#elif defined(__ANDROID__)
        case ANDROID_NE10: {
            ne10_divc_float((ne10_float32_t*)output, (ne10_float32_t*)input, scale, len);
            break;
        }
#endif
        default:
            assert(0);
            break;
    }
}

void CMathUtil::DivArrayByScale(const double input[], double output[], double scale, int len){
    switch (g_accelerateType) {
#ifdef __APPLE__
        case APPLE_IOS_ACC: {
            vDSP_vsdivD(input, 1, &scale, output, 1, len);
            break;
        }
#endif
        case ANDROID_NE10:
        case NONACCELERATE: {
            for (int i = 0; i < len; i++) {
                output[i] = input[i] / scale;
            }
            break;
        }
        default:
            assert(0);
            break;
    }
}
void CMathUtil::MultiArrayByWeights(float output[], const float input[], const float weight[], int len) {
    switch (g_accelerateType) {
        case NONACCELERATE: {
            for (int i = 0; i < len; i++) {
                output[i] = input[i] * weight[i];
            }
            break;
        }
#ifdef __APPLE__
        case APPLE_IOS_ACC: {
            vDSP_vmul(input, 1, weight, 1, output, 1, len);
            break;
        }
#elif defined(__ANDROID__)
        case ANDROID_NE10: {
            ne10_mul_float((ne10_float32_t*)output, (ne10_float32_t*)input, (ne10_float32_t*)weight, len);
            break;
        }
#endif
        default:
            assert(0);
            break;
    }
}
void CMathUtil::MultiArrayByWeights(double output[], const double input[], const double weight[], int len){
    switch (g_accelerateType) {
#ifdef __APPLE__
        case APPLE_IOS_ACC: {
            vDSP_vmulD(input, 1, weight, 1, output, 1, len);
            break;
        }
#endif
        case ANDROID_NE10: 
        case NONACCELERATE: {
            for (int i = 0; i < len; i++) {
                output[i] = input[i] * weight[i];
            }
            break;
        }

        default:
            assert(0);
            break;
    }
    
}
void CMathUtil::DivArrayByweights(const float input[], const float weight[], float output[], int len) {
    switch (g_accelerateType) {
        case NONACCELERATE: {
            for (int i = 0; i < len; i ++) {
                output[i] = input[i] / weight[i];
            }
            break;
        }
#ifdef __APPLE__
        case APPLE_IOS_ACC: {
            vDSP_vdiv(weight, 1, input, 1, output, 1, len);
            break;
        }
#elif defined(__ANDROID__)
        case ANDROID_NE10: {
            ne10_div_float((ne10_float32_t*)output, (ne10_float32_t*)input, (ne10_float32_t*)weight, len);
            break;
        }
#endif
        default:
            assert(0);
            break;
    }
}

void CMathUtil::PowerArray(const float input[], float output[], int len) {
    switch (g_accelerateType) {
        case NONACCELERATE: {
            for (int i = 0; i < len; i++) {
                output[i] = input[i] * input[i];
            }
            break;
        }
#ifdef __APPLE__
        case APPLE_IOS_ACC: {
            vDSP_vsq(input, 1, output, 1, len);
            break;
        }
#elif defined(__ANDROID__)
        case ANDROID_NE10: {
            ne10_mul_float((ne10_float32_t*)output, (ne10_float32_t*)input, (ne10_float32_t*)input, len);
            break;
        }
#endif
        default:
            assert(0);
            break;
    }
}

void CMathUtil::SumArray(const float inArray[], float &out, int len){
    switch (g_accelerateType) {
#ifdef __APPLE__
        case APPLE_IOS_ACC: {
            vDSP_sve(inArray, 1, &out, len);
            break;
        }
#endif
        case NONACCELERATE:
        case ANDROID_NE10: {
            out = 0;
            for (int i = 0; i < len; i++) {
                out = out + inArray[i];
            }
            break;
        }
        default:
            assert(0);
            break;
    }
}

void CMathUtil::SumArray(const double inArray[], double &out, int len){
    switch (g_accelerateType) {
#ifdef __APPLE__
        case APPLE_IOS_ACC: {
            vDSP_sveD(inArray, 1, &out, len);
            break;
        }
#endif
        case NONACCELERATE:
        case ANDROID_NE10: {
            out = 0;
            for (int i = 0; i < len; i++) {
                out = out + inArray[i];
            }
            break;
        }
        default:
            assert(0);
            break;
    }
}

void CMathUtil::SumSquareArray(const float inArray[], float &out, int len){
    switch (g_accelerateType) {
#ifdef __APPLE__
        case APPLE_IOS_ACC: {
            vDSP_svesq(inArray, 1, &out, len);
            break;
        }
#endif
        case NONACCELERATE:
        case ANDROID_NE10: {
            out = 0;
            for (int i = 0; i < len; i++) {
                out = out + (inArray[i]*inArray[i]);
            }
            break;
        }
        default:
            assert(0);
            break;
    }
}

float CMathUtil::GetFadeOutScale( float curFrameIdx, int fadeOutFrameLen) {
    if (curFrameIdx >= fadeOutFrameLen) {
        return 0;
    }
    float scale = cos(1.0 * curFrameIdx * PI / fadeOutFrameLen) * 0.5 + 0.5;
    return scale;
}

float CMathUtil::GetFadeInScale( float curFrameIdx, int fadeInFrameLen) {
    
    if (curFrameIdx >= fadeInFrameLen) {
        return 1;
    }
    float scale = cos(PI + (1.0 * curFrameIdx * PI / fadeInFrameLen)) * 0.5 + 0.5;
    return scale;
}

static Point2D PointOnCubicBezier( const Point2D cp[4], float t){
    float   ax, bx, cx;
    float   ay, by, cy;
    float   tSquared, tCubed;
    Point2D result;
    
    cx = 3.0 * (cp[1].x - cp[0].x);
    bx = 3.0 * (cp[2].x - cp[1].x) - cx;
    ax = cp[3].x - cp[0].x - cx - bx;
    
    cy = 3.0 * (cp[1].y - cp[0].y);
    by = 3.0 * (cp[2].y - cp[1].y) - cy;
    ay = cp[3].y - cp[0].y - cy - by;
    
    tSquared = t * t;
    tCubed = tSquared * t;
    result.x = (ax * tCubed) + (bx * tSquared) + (cx * t) + cp[0].x;
    result.y = (ay * tCubed) + (by * tSquared) + (cy * t) + cp[0].y;
    return result;
}
void CMathUtil::ComputeBezier( const Point2D cp[4],  Point2D* outCurve,int outNumberOfPoints ){
    float   dt;
    int i;
    dt = 1.0 / ( outNumberOfPoints - 1 );
    for( i = 0; i < outNumberOfPoints; i++)
        outCurve[i] = PointOnCubicBezier( cp, i*dt );
}

void CMathUtil::ComputeBezier( const Point2D cp[4],  float* outXValue,float* outYValue,int outNumberOfPoints ){
    float   ax, bx, cx;
    float   ay, by, cy;
    float   tSquared, tCubed;
    Point2D result;
    
    cx = 3.0 * (cp[1].x - cp[0].x);
    bx = 3.0 * (cp[2].x - cp[1].x) - cx;
    ax = cp[3].x - cp[0].x - cx - bx;
    
    cy = 3.0 * (cp[1].y - cp[0].y);
    by = 3.0 * (cp[2].y - cp[1].y) - cy;
    ay = cp[3].y - cp[0].y - cy - by;
    
    float dt= 1.0 / ( outNumberOfPoints - 1 );
    float dxdata[outNumberOfPoints];
    for(int i=0;i<outNumberOfPoints;i++){
        dxdata[i]=i;
    }
    MultiArrayByScale(dxdata, dxdata, dt, outNumberOfPoints);
    
    float dxSquareData[outNumberOfPoints];
    FillArray(dxSquareData, 0, outNumberOfPoints);
    PowerArray(dxdata, dxSquareData, outNumberOfPoints);
    
    float dxCubeData[outNumberOfPoints];
    FillArray(dxCubeData, 0, outNumberOfPoints);
    MultiArrayByWeights(dxCubeData, dxdata, dxSquareData, outNumberOfPoints);
    
    if(outXValue!=NULL){
        float tmp1[outNumberOfPoints],tmp2[outNumberOfPoints],tmp3[outNumberOfPoints];
        FillArray(tmp1, 0, outNumberOfPoints);
        FillArray(tmp2, 0, outNumberOfPoints);
        FillArray(tmp3, 0, outNumberOfPoints);
        MultiArrayByScale(dxCubeData, tmp1, ax, outNumberOfPoints);
        MultiArrayByScale(dxSquareData, tmp2, bx, outNumberOfPoints);
        MultiArrayByScale(dxdata, tmp3, cx, outNumberOfPoints);
        AddArray(tmp1, tmp2, outXValue, outNumberOfPoints);
        AddArray(outXValue, tmp3, outXValue, outNumberOfPoints);
        AddArrayByScale(outXValue, outXValue, cp[0].x, outNumberOfPoints);
    }
    if(outYValue!=NULL){
        float tmp1[outNumberOfPoints],tmp2[outNumberOfPoints],tmp3[outNumberOfPoints];
        FillArray(tmp1, 0, outNumberOfPoints);
        FillArray(tmp2, 0, outNumberOfPoints);
        FillArray(tmp3, 0, outNumberOfPoints);
        MultiArrayByScale(dxCubeData, tmp1, ay, outNumberOfPoints);
        MultiArrayByScale(dxSquareData, tmp2, by, outNumberOfPoints);
        MultiArrayByScale(dxdata, tmp3, cy, outNumberOfPoints);
        AddArray(tmp1, tmp2, outYValue, outNumberOfPoints);
        AddArray(outYValue, tmp3, outYValue, outNumberOfPoints);
        AddArrayByScale(outYValue, outYValue, cp[0].y, outNumberOfPoints);
    }
}

void CMathUtil::FillArray(float inArray[], float value, int len) {
    switch (g_accelerateType) {
        case NONACCELERATE: {
            for (int i = 0; i < len; i++) {
                inArray[i] = value;
            }
            break;
        }
#ifdef __APPLE__
        case APPLE_IOS_ACC: {
            vDSP_vfill(&value, inArray, 1, len);
            break;
        }
#elif defined(__ANDROID__)
        case ANDROID_NE10: {
            ne10_setc_float_neon((ne10_float32_t *)inArray, value, len);
            break;
        }
#endif
        default:
            assert(0);
            break;
    }
}

void CMathUtil::LogArray(float inArray[],float outArray[],int len){
    switch (g_accelerateType) {
#ifdef __APPLE__
        case APPLE_IOS_ACC: {
            vvlogf(outArray, inArray, &len);
            break;
        }
#endif
        case NONACCELERATE:
        case ANDROID_NE10: {
            for (int i = 0; i < len; i++) {
                outArray[i]=logf(inArray[i]);
            }
            break;
        }
        default:
            assert(0);
            break;
    }
}

void CMathUtil::ExpArray(float inArray[],float outArray[],int len){
    switch (g_accelerateType) {
#ifdef __APPLE__
        case APPLE_IOS_ACC: {
            vvexpf(outArray, inArray, &len);
            break;
        }
#endif
        case NONACCELERATE:
        case ANDROID_NE10: {
            for (int i = 0; i < len; i++) {
                outArray[i]=expf(inArray[i]);
            }
            break;
        }
        default:
            assert(0);
            break;
    }
}

void CMathUtil::AddArray(float inArrayA[],float inArrayB[], float outArrayC[],int len){
    switch (g_accelerateType) {
        case NONACCELERATE: {
            for (int i = 0; i < len; i++) {
                outArrayC[i] = inArrayA[i]+inArrayB[i];
            }
            break;
        }
#ifdef __APPLE__
        case APPLE_IOS_ACC: {
            vDSP_vadd(inArrayA, 1, inArrayB, 1, outArrayC, 1, len);
            break;
        }
#elif defined(__ANDROID__)
        case ANDROID_NE10: {
            ne10_add_float_neon((ne10_float32_t*)outArrayC, (ne10_float32_t*)inArrayA, (ne10_float32_t*)inArrayB, len);
            break;
        }
#endif
        default:
            assert(0);
            break;
    }
}

void CMathUtil::SubArray(const float inArrayA[], const float inArrayB[], float outArrayC[],int len){
    switch (g_accelerateType) {
        case NONACCELERATE: {
            for (int i = 0; i < len; i++) {
                outArrayC[i] = inArrayA[i]-inArrayB[i];
            }
            break;
        }
#ifdef __APPLE__
        case APPLE_IOS_ACC: {
            vDSP_vsub(inArrayB, 1, inArrayA, 1, outArrayC, 1, len);
            break;
        }
#elif defined(__ANDROID__)
        case ANDROID_NE10: {
            ne10_sub_float_neon((ne10_float32_t*)outArrayC, (ne10_float32_t*)inArrayA, (ne10_float32_t*)inArrayB, len);
            break;
        }
#endif
        default:
            assert(0);
            break;
    }
}
void CMathUtil::AddArrayByScale(const float inArrayA[],float outArray[],float scale,int len){
    switch (g_accelerateType) {
        case NONACCELERATE: {
            for (int i = 0; i < len; i++) {
                outArray[i] = inArrayA[i]+scale;
            }
            break;
        }
#ifdef __APPLE__
        case APPLE_IOS_ACC: {
            vDSP_vsadd(inArrayA, 1, &scale, outArray, 1, len);
            break;
        }
#elif defined(__ANDROID__)
        case ANDROID_NE10: {
            ne10_addc_float_neon((ne10_float32_t*)outArray, (ne10_float32_t*)inArrayA, scale, len);
            break;
        }
#endif
        default:
            assert(0);
            break;
    }
}

void CMathUtil::ConvArray(float inPaddingArrayA[],float weightArrayB[],float outArrayC[],int outarrayClen,int weightArrayBlen){
    switch (g_accelerateType) {
        case NONACCELERATE: {
            //int halfBLen =weightArrayBlen/2;
            for (int i=0; i<outarrayClen; i++) {
                float sum=0;
                for (int j=0; j<weightArrayBlen; j++) {
                    sum+= ( inPaddingArrayA[i+j]*weightArrayB[j] );
                }
                outArrayC[i]=sum;
            }
            break;
        }
#ifdef __APPLE__
        case APPLE_IOS_ACC: {
            vDSP_conv(inPaddingArrayA, 1, weightArrayB+weightArrayBlen-1, -1, outArrayC, 1, outarrayClen, weightArrayBlen);
            break;
        }
#elif defined(__ANDROID__)
        case ANDROID_NE10: {
            float output[weightArrayBlen];
            int halfBLen =weightArrayBlen/2;
            for (int i=0; i<outarrayClen; i++) {
                float sum=0;
                ne10_mul_float((ne10_float32_t*)output, (ne10_float32_t*)(inPaddingArrayA+i), (ne10_float32_t*)weightArrayB, weightArrayBlen);
                for (int j=0; j<weightArrayBlen; j++) {
                    sum+= output[j];
                }
                outArrayC[i]=sum;
            }
            break;
        }
#endif
        default:
            assert(0);
            break;
    }
}

void CMathUtil::RootSquaredAddArray(const float inArrayA[], const float inArrayB[], float outArray[], int len){
    switch (g_accelerateType) {
        case NONACCELERATE: {
            for (int i = 0; i < len; i++) {
                outArray[i] = sqrtf(inArrayA[i] * inArrayA[i] + inArrayB[i] * inArrayB[i]);
            }
            break;
        }
#ifdef __APPLE__
        case APPLE_IOS_ACC: {
            vDSP_vdist(inArrayA, 1, inArrayB, 1, outArray, 1, len);
            break;
        }
#elif defined(__ANDROID__)
        case ANDROID_NE10: {
            ne10_vec2f_t inArray[len];
            for (int i = 0; i < len; i++) {
                inArray[i].x = inArrayA[i];
                inArray[i].y = inArrayB[i];
            }
            ne10_len_vec2f_neon((ne10_float32_t *)outArray, inArray, len);
            break;
        }
#endif
        default:
            assert(0);
            break;
    }
}

float CMathUtil::CalcHanningWindowScale(float windowsize,float hopsize, bool overlapInAndOut){
    int stepNum = windowsize/hopsize;
    float scale=0;
    float x = windowsize/2;
    for (int i=0; i<stepNum; i++) {
        scale+= ((0.5 - 0.5*cos( 2 * PI * (x-i*hopsize) / windowsize ))*(0.5 - 0.5*cos( 2 * PI * (x-i*hopsize) / windowsize )));
    }
    
    //    for (int i=0; i<windowsize; i++) {//测试所有位置上的累加和，应该均相同
    //        float scale2=0;
    //        float x2 = i;
    //        for (int i=0; i<olen; i++) {
    ////            scale2 += ( 0.5-0.5*cos( (0.5 - 0.5*cos( 2 * PI * (x2-i*hopsize) / windowsize )) ) );
    //            scale2 += ((0.5 - 0.5*cos( 2 * PI * (x2-i*hopsize) / windowsize ))*(0.5 - 0.5*cos( 2 * PI * (x2-i*hopsize) / windowsize )));
    //        }
    //        cout<<__FUNCTION__<<" accumulate weight on "<<i<<"  = "<< scale2<<endl;
    //    }
    return 1/scale;
}

bool UPgreater( float elem1, float elem2 )
{
    return elem1 > elem2;
}
bool Downgreater( float elem1, float elem2 )
{
    return elem1 < elem2;
}

void CMathUtil::SortArray(float inArray[], int len,int sortFlag){
    switch (g_accelerateType) {
#ifdef __APPLE__
        case APPLE_IOS_ACC: {
            vDSP_vsort(inArray, len, sortFlag);
            break;
        }
#endif
        case NONACCELERATE:
        case ANDROID_NE10: {
            if (sortFlag==0) {
                std::sort(&inArray[0], &inArray[len],UPgreater);
            }else{
                std::sort(&inArray[0], &inArray[len],Downgreater);
            }
            break;
        }
        default:
            assert(0);
            break;
    }
}

void CMathUtil::FindMaxInArray(const float *input,int inSize,float& maxValue,unsigned long &maxidx){
    switch (g_accelerateType) {
#ifdef __APPLE__
        case APPLE_IOS_ACC: {
            vDSP_maxvi(input,1,&maxValue,&maxidx,inSize);
            break;
        }
#endif
        case NONACCELERATE:
        case ANDROID_NE10: {
            maxValue=-INFINITY;
            for (int i=0; i<inSize; i++) {
                if (maxValue<input[i]) {
                    maxValue=input[i];
                    maxidx=i;
                }
            }
            break;
        }
        default:
            assert(0);
            break;
    }
}

void CMathUtil::FindMinInArray(const float *input,int inSize,float& minValue,unsigned long &minidx){
    switch (g_accelerateType) {
#ifdef __APPLE__
        case APPLE_IOS_ACC: {
            vDSP_minvi(input, 1, &minValue, &minidx, inSize);
            break;
        }
#endif
        case NONACCELERATE:
        case ANDROID_NE10: {
            minValue=INFINITY;
            for (int i=0; i<inSize; i++) {
                if (minValue>input[i]) {
                    minValue=input[i];
                    minidx=i;
                }
            }
            break;
        }
        default:
            assert(0);
            break;
    }
}

void CMathUtil::CalCosWindow(float *windowWeight, int winLen){
    switch (g_accelerateType) {
#ifdef __APPLE__
        case APPLE_IOS_ACC: {
            vDSP_hann_window(windowWeight, winLen, vDSP_HANN_DENORM);
            break;
        }
#endif
        case NONACCELERATE:
        case ANDROID_NE10: {
            for (int i=0; i<winLen; i++) {
                windowWeight[i] = 0.5-0.5*cos(2 * PI * i/winLen);
            }
            break;
        }
        default:
            assert(0);
            break;
    }
}

void CMathUtil::ABSArray(const float inArray[],float outArray[],int len){

    switch (g_accelerateType) {
#ifdef __APPLE__
        case APPLE_IOS_ACC: {
            vDSP_vabs(inArray, 1, outArray, 1, len);
            break;
        }
#endif
        case NONACCELERATE:
        case ANDROID_NE10: {
            for(int i=0;i<len;i++){
                outArray[i] = fabs(inArray[i]);
            }
            break;
        }
        default:
            assert(0);
            break;
    }
}

void CMathUtil::ClearArray(float* inArray,int len){
    switch (g_accelerateType) {
    #ifdef __APPLE__
        case APPLE_IOS_ACC: {
            vDSP_vclr(inArray, 1, len);
            break;
        }
    #endif
        case NONACCELERATE:{
            memset(inArray,0,len*sizeof(float));
            break;
        }
    #ifdef __ANDROID__
        case ANDROID_NE10: {
            ne10_setc_float_neon((ne10_float32_t *)inArray, 0, len);
            break;
        }
    #endif
        default:
            assert(0);
            break;
    }
}

void CMathUtil::LowerThreshArray(const float *input,float lowerThresh,float* output,int len){
    switch (g_accelerateType) {
    #ifdef __APPLE__
        case APPLE_IOS_ACC: {
            vDSP_vthr(input, 1, &lowerThresh, output, 1, len);
            break;
        }
    #endif
        case NONACCELERATE:
        case ANDROID_NE10: {
            for (int i=0; i<len; i++) {
                if (input[i]<lowerThresh) {
                    output[i]=lowerThresh;
                }else{
                    output[i]=input[i];
                }
            }
            break;
        }
        default:
            assert(0);
            break;
    }
}

void CMathUtil::ReverseArray(float *data,int len){
    switch (g_accelerateType) {
    #ifdef __APPLE__
        case APPLE_IOS_ACC: {
            vDSP_vrvrs(data, 1, len);
            break;
        }
    #endif
        case NONACCELERATE:
        case ANDROID_NE10: {
            float tmp=0;
            for (int i=0; i<len/2; i++) {
                tmp = data[i];
                data[i] = data[len-i-1];
                data[len-i-1]=tmp;
            }
            break;
        }
        default:
            assert(0);
            break;
    }
}

void CMathUtil::Float2Double(float inArray[],double outArray[],int len){
    switch (g_accelerateType) {
#ifdef __APPLE__
        case APPLE_IOS_ACC: {
            vDSP_vspdp(inArray, 1, outArray, 1, len);
            break;
        }
#endif
        case NONACCELERATE:
        case ANDROID_NE10: {
            int i=0;
            while (i<len) {
                outArray[i] = inArray[i];
                ++i;
            }
            break;
        }
        default:
            assert(0);
            break;
    }
}

void CMathUtil::Double2Float(double inArray[],float outArray[],int len){
    switch (g_accelerateType) {
#ifdef __APPLE__
        case APPLE_IOS_ACC: {
            vDSP_vdpsp(inArray, 1, outArray, 1, len);
            break;
        }
#endif
        case NONACCELERATE:
        case ANDROID_NE10: {
            int i=0;
            while (i<len) {
                outArray[i] = inArray[i];
                ++i;
            }
            break;
        }
        default:
            assert(0);
            break;
    }    
}

void CMathUtil::ShortSample2Float(short inSamples[],float outSamples[]){
    assert(0);
}

void CMathUtil::FloatSample2Short(float inSamples[],short outSamples[]){
    assert(0);
}

void CMathUtil::DivScaleByArray(const float *input,float* output,float scale,int len){
    switch (g_accelerateType) {
    #ifdef __APPLE__
        case APPLE_IOS_ACC: {
            vDSP_svdiv(&scale, input, 1, output, 1, len);
            break;
        }
    #endif
        case NONACCELERATE:
        case ANDROID_NE10: {
            for (int i=0; i<len; i++) {
                output[i]=scale/input[i];
            }
            break;
        }
        default:
            assert(0);
            break;
    }
}
void CMathUtil::DivScaleByArray(const double input[],double output[],double scale,int len){
    switch (g_accelerateType) {
#ifdef __APPLE__
        case APPLE_IOS_ACC: {
            vDSP_svdivD(&scale, input, 1, output, 1, len);
            break;
        }
#endif
        case NONACCELERATE:
        case ANDROID_NE10: {
            for (int i=0; i<len; i++) {
                output[i]=scale/input[i];
            }
            break;
        }
        default:
            assert(0);
            break;
    }
}
//默认的流程
//switch (g_accelerateType) {
//#ifdef __APPLE__
//    case APPLE_IOS_ACC: {
//        
//        break;
//    }
//#endif
//    case NONACCELERATE:
//    case ANDROID_NE10: {
//        
//        break;
//    }
//    default:
//        break;
//}


