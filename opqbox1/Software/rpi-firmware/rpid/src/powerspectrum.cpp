#include "powerspectrum.hpp"
//#include <gsl/gsl_errno.h>
#include <gsl/gsl_fft_real.h>
#include <gsl/gsl_fft_complex.h>
//#include <gsl/gsl_fft_halfcomplex.h>

#include <vector>

static gsl_fft_complex_wavetable* complex;
static gsl_fft_complex_workspace * work;
static int size = -1;

vector<double> powerSpectrum(vector<double> input)
{
    size_t length = input.size();
    if(length != size)
    {
        if(size >= 0)
        {
            gsl_fft_complex_workspace_free (work);
            gsl_fft_complex_wavetable_free (complex);
        }
        complex = gsl_fft_complex_wavetable_alloc(length);
        work = gsl_fft_complex_workspace_alloc(length);
        size  = length;

    }
    double reals[length*2];
    for(size_t i = 0; i <length; i++)
    {
        reals[i*2] = input[i];
        reals[i*2+1] = 0;
    }


    gsl_fft_complex_transform (reals, 1, length, complex, work, gsl_fft_forward);

    for(int i = 0; i< length; i++)
    {
        input[i] = sqrt(reals[i*2]*reals[i*2] + reals[i*2+1]*reals[i*2+1])/length;
    }
    return input;
}
