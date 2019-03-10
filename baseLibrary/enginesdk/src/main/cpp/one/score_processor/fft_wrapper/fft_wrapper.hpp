//
//  fft_wrapper.hpp
//
//    使用FFT获得频域数据
//    为了适应iOS和Android两个平台，由wrapper区分vDsp实现和软件计算实现
//
//

#ifndef FFT_WRAPPER_HPP
#define FFT_WRAPPER_HPP
#include "fft_routine.h"

class CFFTWrapper {
private:
    FftRoutine* fftRoutine;
public:
    /* Constructor for FFT wrapper
     * @param nFft window size
     */
    explicit CFFTWrapper(size_t nFft);

    /* Destructor for FFT wrapper
     */
    ~CFFTWrapper();
    
    /*fft变换
     input: 输入的时域数据，长度：nFFT
     outputRe：输出的实部数据，长度 nFFT/2 + 1
     outputIm: 输出虚部数据，长度 nFFT/2 + 1
     */
    void FftForward(const float input[], float outputRe[], float outputIm[]);
    
    /*fft反变换
     inputRe：输入的实部数据，长度 nFFT/2 + 1
     inputIm: 输入虚部数据，长度 nFFT/2 + 1
     output: 输出的时域数据，长度：nFFT
     */
    void FftInverse(const float inputRe[], const float inputIm[], float output[]);
    
    /*
     计算梅尔倒谱
     inPut：输入的时域数据，len nFFT
     outPut:输出的梅尔倒谱数据，也是时域上的数据，len nFFT
     */
    void CalcCepstrum(const float inPut[], float outPut[]);
    
    /*
     把实部虚部数据转换为幅度和相位, 长度均为nFFT/2 +1
     */
    void Rec2Polar(const float inputRe[], const float inputIm[], float amplitude[],float phase[]);
    
    /*
     把幅度和相位转换为实部虚部数据,长度均为nFFT/2 +1
     */
    void Polar2Rec(const float amplitude[],const float phase[],float outputRe[], float outputIm[]);
};

#endif

