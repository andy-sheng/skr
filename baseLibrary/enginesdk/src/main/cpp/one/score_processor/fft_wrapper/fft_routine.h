#ifndef FFT_ROUTINE_H
#define FFT_ROUTINE_H

#include "mayer_fft.h"
#include <string.h>
#include <stdlib.h>
#include <math.h>

#ifndef PI
#define PI (float)3.14159265358979323846
#endif

#define audio_log2(x) (log(x) * 1.44269504088896340736)

// Variables for FFT routine
class FftRoutine
{
protected:
	size_t m_nfft;
	float * m_fft_data;
    /** 为了计算倒谱图的参数 **/
    size_t m_halfSize;
    float *m_fftfreqre;
    float *m_fftfreqim;
    float *m_halfmag;
    float *m_halfmagpower;

public:
	// Constructor for FFT routine
	FftRoutine(size_t nfft)
	{
        m_nfft = nfft;
        m_fft_data = new float[nfft];
        
        /** 为了计算倒谱图的参数 **/
        m_halfSize = nfft / 2;
        m_fftfreqre = new float[m_halfSize + 1];
        m_fftfreqim = new float[m_halfSize + 1];
        memset(m_fftfreqre, 0, (m_halfSize + 1) * sizeof(float));
        memset(m_fftfreqim, 0, (m_halfSize + 1) * sizeof(float));
        m_halfmag = new float[m_halfSize + 1];
        m_halfmagpower = new float[m_halfSize + 1];
        memset(m_halfmag, 0, (m_halfSize + 1) * sizeof(float));
        memset(m_halfmagpower, 0, (m_halfSize + 1) * sizeof(float));
	}

	// Destructor for FFT routine
	virtual ~FftRoutine()
	{
		delete [] m_fft_data;
        if (m_fftfreqre) {
            delete[] m_fftfreqre;
            m_fftfreqre = NULL;
        }
        if (m_fftfreqim) {
            delete[] m_fftfreqim;
            m_fftfreqim = NULL;
        }
        if (m_halfmag) {
            delete[] m_halfmag;
            m_halfmag = NULL;
        }
        if (m_halfmagpower) {
            delete[] m_halfmagpower;
            m_halfmagpower = NULL;
        }
	}

	// Perform forward FFT of real data
	// Accepts:
	//   membvars - pointer to struct of FFT variables
	//   input - pointer to an array of (real) input values, size nfft
	//   output_re - pointer to an array of the real part of the output,
	//     size nfft/2 + 1
	//   output_im - pointer to an array of the imaginary part of the output,
	//     size nfft/2 + 1
	virtual void fft_forward(float* input, float* output_re, float* output_im)
	{
		int ti;
		int nfft;
		int hnfft;

		nfft = (int)m_nfft;
		hnfft = nfft/2;

		for (ti=0; ti<nfft; ti++) {
			m_fft_data[ti] = input[ti];
		}

		MayerFft::mayer_realfft(nfft, m_fft_data);

		output_im[0] = 0;
		for (ti=0; ti<hnfft; ti++) {
			output_re[ti] = m_fft_data[ti];
			output_im[ti+1] = m_fft_data[nfft-1-ti];
		}
		output_re[hnfft] = m_fft_data[hnfft];
		output_im[hnfft] = 0;
	}

	// Perform inverse FFT, returning real data
	// Accepts:
	//   membvars - pointer to struct of FFT variables
	//   input_re - pointer to an array of the real part of the output,
	//     size nfft/2 + 1
	//   input_im - pointer to an array of the imaginary part of the output,
	//     size nfft/2 + 1
	//   output - pointer to an array of (real) input values, size nfft
	virtual void fft_inverse(float* input_re, float* input_im, float* output)
	{
		size_t ti;
		size_t nfft;
		size_t hnfft;

		nfft = m_nfft;
		hnfft = nfft/2;

		for (ti=0; ti<hnfft; ti++) {
			m_fft_data[ti] = input_re[ti];
			m_fft_data[nfft-1-ti] = input_im[ti+1];
		}
		m_fft_data[hnfft] = input_re[hnfft];

		MayerFft::mayer_realifft((int)nfft, m_fft_data);

		for (ti=0; ti<nfft; ti++) {
			output[ti] = m_fft_data[ti];
		}
        float scale = 1.0 / m_nfft;
        for (int i = 0; i < m_nfft; i++) {
            output[i] *= scale;
        }
	}
    
    virtual void CalcCepstrum(const float inPut[], float outPut[]) {
        //1 进行fft变换
        this->fft_forward((float*)inPut, m_fftfreqre, m_fftfreqim);
        //2 去除直流分量，保留下第一项中的虚部
        m_fftfreqre[0] = 0;
        float zxk = m_fftfreqim[0];
        //3 求频域下幅值的平方，第一项为原来第一项虚部的平方
        for (size_t i = 0; i < m_halfSize; i++)
        {
            m_fftfreqre[i] = m_fftfreqre[i] * m_fftfreqre[i] + m_fftfreqim[i] * m_fftfreqim[i];
            m_fftfreqim[i] = 0;
        }
        m_fftfreqre[0] = zxk * zxk;
        //4 对幅值平方进行反向fft计算
        this->fft_inverse(m_fftfreqre, m_fftfreqim, outPut);
        //5 获取归一化系数
        float tf = (float)1 / outPut[0];
        //6 进行归一化
        for (size_t i = 0; i < m_nfft; i++)
        {
            outPut[i] = outPut[i] * tf;
        }
        outPut[0] = 1;
    }
    
    virtual void Rec2Polar(const float inputRe[], const float inputIm[], float amplitude[],float phase[]) {
        
        for (int i=0; i<=m_halfSize; i++) {
            amplitude[i] = sqrt( inputRe[i]*inputRe[i] + inputIm[i]*inputIm[i] );
            phase[i] = atan2(inputIm[i], inputRe[i]);
        }
    }
    
    
    
    virtual void Polar2Rec(const float amplitude[],const float phase[],float outputRe[], float outputIm[]) {

        for (int i=0; i<=m_halfSize; i++) {
            outputRe[i] = amplitude[i]*cos(phase[i]);
            outputIm[i] = amplitude[i]*sin(phase[i]);
        }
    }
};

#endif
