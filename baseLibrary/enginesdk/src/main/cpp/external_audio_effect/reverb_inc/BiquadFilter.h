#ifndef __BIQUADFILTER_H__
#define __BIQUADFILTER_H__

#include <vector>

namespace BiquadFilter
{
	class Filter{
	public:
        Filter();
		virtual ~Filter();
		void filtering(std::vector<float> *x);
		void reset();
	protected:
		void alloc();

	protected:

		std::vector<float> a;
		std::vector<float> b;
    private:
		float bin1; //= 0.0, bin2 = 0.0;
		float bout1;// = 0.0, bout2 = 0.0;
		float bin2; //= 0.0, bin2 = 0.0;
		float bout2;// = 0.0, bout2 = 0.0;
	};


	class LPFilter : public Filter{
	public:
		LPFilter(float cutoff, float Q);
	private:
		float cutoff;

		float Q;
	};


	class HPFilter : public Filter{
	public:
		HPFilter(float cutoff, float Q);
	private:
		float cutoff;

		float Q;
	};
   
    class BPFilter : public Filter{
	public:
		BPFilter(float low_edge, float high_edge);
	private:
		float low_edge;
		float high_edge;
	};

	class NTFilter : public Filter{
	public:
		NTFilter(float low_edge, float high_edge);
	private:
		float low_edge;
		float high_edge;
	};


	class LSFilter : public Filter{
	public:
		LSFilter(float cutoff, float Q, float gain);
	private:
		float cutoff;
		float Q;
		float gain;
	};


	class HSFilter : public Filter{
	public:
		HSFilter(float cutoff, float Q, float gain);
	private:
		float cutoff;
		float Q;
		float gain;
	};
 
    class PKFilter : public Filter{
	public:
		PKFilter(float low_edge, float high_edge, float gain);
	private:
		float low_edge;
		float high_edge;
		float gain;
	};

    class APFilter : public Filter{
	public:
		APFilter(float cutoff, float Q);
	private:
		float cutoff;
		float Q;
	};
}

#endif
