//BiquadFilter.cpp

#include "reverb_inc/BiquadFilter.h"

//using
using std::vector;


namespace BiquadFilter{

    Filter::Filter()
    {
		reset();
    }

	Filter::~Filter(){}

	void Filter::filtering(vector<float> *x){
		vector<float> &in = *x;
		vector<float> out(in.size());

        float b00 = b[0] / a[0];
        float b10 = b[1] / a[0];
        float b20 = b[2] / a[0];
        float a10 = a[1] / a[0];
        float a20 = a[2] / a[0];

        for (unsigned int i = 0; i < out.size(); i++){
			out[i] = b00 * in[i] + b10 * bin1 + b20 * bin2 - a10 * bout1 - a20 * bout2;
//			out[i] = (b[0] / a[0]) * in[i] + (b[1] / a[0]) * bin1 + (b[2] / a[0]) * bin2 - (a[1] / a[0]) * bout1 - (a[2] / a[0]) * bout2;
			//update input buf
			bin2 = bin1;
			bin1 = in[i];
			//update output buf
			bout2 = bout1;
			bout1 = out[i];
		}

		//copy
		for (unsigned int i = 0; i < in.size(); i++){
			in[i] = out[i];
		}

	}

	void Filter::alloc(){
		a.resize(3);
		b.resize(3);
	}

	void Filter::reset()
	{
		bin1 = 0; //= 0.0, bin2 = 0.0;
		bout1 = 0;// = 0.0, bout2 = 0.0;
		bin2 = 0; //= 0.0, bin2 = 0.0;
		bout2 = 0;// = 0.0, bout2 = 0.0;
	}

}