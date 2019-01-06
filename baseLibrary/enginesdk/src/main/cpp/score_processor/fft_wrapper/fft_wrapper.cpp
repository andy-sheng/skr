#include "fft_wrapper.hpp"

#define LOG_TAG "CFFTWrapper"

CFFTWrapper::CFFTWrapper(size_t nfft) {
    fftRoutine = new FftRoutine(nfft);
}

CFFTWrapper::~CFFTWrapper() {
    if (fftRoutine) {
        delete fftRoutine;
        fftRoutine = NULL;
    }
}

void CFFTWrapper::FftForward(const float input[], float outputRe[], float outputIm[]) {
    fftRoutine->fft_forward((float*)input, outputRe, outputIm);
}

void CFFTWrapper::FftInverse(const float inputRe[], const float inputIm[], float output[]) {
    fftRoutine->fft_inverse((float*)inputRe, (float*)inputIm, output);
}

void CFFTWrapper::CalcCepstrum(const float inPut[], float outPut[]) {
    fftRoutine->CalcCepstrum(inPut, outPut);
}


void CFFTWrapper::Rec2Polar(const float inputRe[], const float inputIm[], float amplitude[],float phase[]) {
    fftRoutine->Rec2Polar(inputRe, inputIm, amplitude, phase);
}

void CFFTWrapper::Polar2Rec(const float amplitude[],const float phase[],float outputRe[], float outputIm[]) {
    fftRoutine->Polar2Rec(amplitude, phase, outputRe, outputIm);
}
