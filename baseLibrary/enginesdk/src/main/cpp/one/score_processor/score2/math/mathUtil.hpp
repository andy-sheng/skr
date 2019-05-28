#ifndef mathUtil_hpp
#define mathUtil_hpp

#include <stdio.h>

#include <math.h>

#ifndef PI
#define PI (float)3.14159265358979323846
#endif

#ifndef TWOPI
#define TWOPI (float)6.283185307179586
#endif

#define arlog2(x) (log(x) * 1.44269504088896340736)

#ifndef  MAXFLOAT
#define MAXFLOAT 0x1.fffffep+127f
#endif

typedef struct tagPoint2D{
    float x;
    float y;
}Point2D;

class CMathUtil
{
public:
    typedef enum AccelerateType {
        ANDROID_NE10, 
        APPLE_IOS_ACC, 
        NONACCELERATE
    }AccelerateType;    
public:
    CMathUtil();
    ~CMathUtil();
public:
    //获取当前系统下的加速库使用情况
    static AccelerateType getAccelerateStatus();
    
    //指定代码的加速类型，注意：仅用于测试加速前后的时间差，或验证加速前后的结果；
    static void SetAccelerateType(AccelerateType acctype);
    
    //根据fft变换后的实部和虚部计算幅度
    static void FFTMag(const float inputRe[], const float inputIm[], float magoutput[], int len);
    
    //根据fft变换后的实部和虚部计算相位
    static void FFTPhase(const float inputRe[], const float inputIm[], float phaseput[], int len);
    
    //求数组绝对值,输入输出地址可以相同
    static void ABSArray(const float inArray[],float outArray[],int len);
    
    //把inArray数组中的每一项赋值为同一个数值
    static void FillArray(float inArray[], float value, int len);
    
    //把inArray数组中的值清0
    static void ClearArray(float inArray[],int len);
    
    //求数组中每个元素的平方,支持input 和output地址相同
    static void PowerArray(const float input[], float output[], int len);
    
    //计算两个数组的平方和，inputRe[i]*inputRe[i] + inputIm[i]+inputIm[i];也可以看做是fft幅度的平方
    static void SquaredAddArray(const float inputRe[], const float inputIm[], float output[], int len);
    
    //计算两个数组的均方根和 sqrt(inputRe[i]*inputRe[i] + inputIm[i]+inputIm[i])
    static void RootSquaredAddArray(const float inArrayA[], const float inArrayB[], float outArray[], int len);
    
    //如果数组中元素小于value则赋为value,否则不变
    static void LowerThreshArray(const float input[],float lowerThresh,float output[],int len);
    
    //把数组反序 data[i]=data[len-i-1]
    static void ReverseArray(float data[],int len);

    //数组类型转换 double->float 转换
    static void Float2Double(float inArray[],double outArray[],int len);
    
    //数组类型转换 flot->double 转换
    static void Double2Float(double inArray[],float outArray[],int len);
    
    //sample转为0-1的float值 short->float 转换
    static void ShortSample2Float(short inSamples[],float outSamples[]);
    
    //把0-1的float值转为-32768~32767的sample值 float->float 转换
    static void FloatSample2Short(float inSamples[],short outSamples[]);
    
    //对数组排序，sortFlag=1 升序排序，sortFlag=-1 降序排序
    static void SortArray(float inArray[], int len,int sortFlag=1);
    
    //计算两个数组A，B的和，结果放入C数组中，C可以为A或B的地址
    static void AddArray(float inArrayA[],float inArrayB[], float outArrayC[],int len);
    
    //计算数组A加上一个系数，支持inarray和outarray地址相同
    static void AddArrayByScale(const float inArrayA[],float outArray[],float scale,int len);
    
    //计算两个数组A，B的差(C=A-B)，结果放入C数组中，C可以为A或B的地址
    static void SubArray(const float inArrayA[], const float inArrayB[], float outArrayC[],int len);
    
    //将数组的值乘以一个系数，支持input 和output地址相同
    static void MultiArrayByScale(const float input[], float output[], float scale, int len);
    static void MultiArrayByScale(const double input[], double output[], double scale, int len);
    
    //求数组和数组每个元素的乘积，input[i]*weight[i]
    static void MultiArrayByWeights(float output[], const float input[], const float weight[], int len);
    static void MultiArrayByWeights(double output[], const double input[], const double weight[], int len);
    
    //计算数组A除以一个系数，支持inarray和outarray地址相同
    static void DivArrayByScale(const float input[], float output[], float scale, int len);
    static void DivArrayByScale(const double input[], double output[], double scale, int len);
    
    //用系数scale除以数组A中每个元素，out[i]=scale/in[i];
    static void DivScaleByArray(const float input[],float output[],float scale,int len);
    static void DivScaleByArray(const double input[],double output[],double scale,int len);
    
    //求数组和数组每个元素相除,input[i]/weight[i]
    static void DivArrayByweights(const float input[], const float weight[], float output[], int len);
    
    //寻找数组中的最大值以及对应的索引位置
    static void FindMaxInArray(const float input[],int inSize,float& maxValue,unsigned long &maxidx);
    
    //寻找数组中的最小值以及对应的索引位置
    static void FindMinInArray(const float input[],int inSize,float& minValue,unsigned long &minidx);
    
    //求数组中每个元素的和 sum(a[0-n])
    static void SumArray(const float inArray[], float &out, int len);
    static void SumArray(const double inArray[], double &out, int len);
    
    //求数组中每个元素的平方和 sum(a[0-n]*a[0-n])
    static void SumSquareArray(const float inArray[], float &out, int len);
    
    //获取当前frameidx下的淡出系数，frameidx的范围是[0 ~ (fadeOutTimeLen/1000)*sampleRate]
    static float GetFadeOutScale( float curFrameIdx, int fadeOutFrameLen);
    
    //获取当前frameidx下的淡入系数
    static float GetFadeInScale( float curFrameIdx, int fadeInFrameLen);
    
    /*三阶贝塞尔曲线，给出4个控制点，和希望输出的点个数，得到最终输出的曲线上的点,outNumberOfPoints包含起点和终点
    曲线起点是PO,终点是P3,输出的中间点走向是起始于P0走向P1，并从P2的方向来到P3。*/
    static void ComputeBezier( const Point2D cp[4],  Point2D* outCurve,int outNumberOfPoints );
    //把贝塞尔曲线插值后x轴和y轴上的值分开返回，可以给空指针表示不需要某个轴上的值,outNumberOfPoints包含起点和终点
    static void ComputeBezier( const Point2D cp[4],  float* outXValue,float* outYValue,int outNumberOfPoints );
    
    //计算数组中每个元素的log值(自然对数),inArray 和outArray可以是同一个地址
    static void LogArray(float inArray[],float outArray[],int len);
    
    //计算数组中每个元素的exp值(自然对数)，inArray 和outArray可以是同一个地址
    static void ExpArray(float inArray[],float outArray[],int len);
    
    //对数组A按照weight数组B进行卷积操作，结果放到C中，A和C的长度一致，但是卷积前需要对A的头尾进行padding，Padding后PaddingA的长度 = C.len + B.len -1
    static void ConvArray(float inPaddingArrayA[],float weightArrayB[],float outArrayC[],int outarrayClen,int weightArrayBlen);
    
    //计算进行fft变换后Hanning窗口overlap时的幅度累加常数系数，如果不为1，需要对反变换后的数据进行幅度拉伸 ：A*scale
    //windowssize:fft窗口长度，hopsize：overlap的步长，overlapInAndOut:是否在fft变换时对输入和输出均加窗。
    static float CalcHanningWindowScale(float windowsize,float hopsize, bool overlapInAndOut=true);
    
    //计算一个归一化的余弦窗，winlen是余弦窗的长度
    static void CalCosWindow(float *windowWeight, int winLen);
    
    template <class _T>
    static void PrintArray(_T array[],int len,const char * title) {
        #ifdef DEBUG
        if(title)printf("================%s==============\n", title);
        for (int i=0; i<len; i++) {
            printf(" %f ",array[i]);
            if (i%20==0) {
                printf("\n");
            }
        }
        printf("\n");
        #endif
    }
};
#endif /* mathUtil_hpp */
