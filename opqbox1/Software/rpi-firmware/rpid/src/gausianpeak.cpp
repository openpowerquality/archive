#include "gausianpeak.hpp"
#include <algorithm>
#include <gsl/gsl_multifit.h>
#include <gsl/gsl_matrix.h>
#include <gsl/gsl_vector.h>
using namespace std;

static gsl_multifit_linear_workspace * work = NULL;
static int size = -1;

static void parabolicFit(std::vector<double> data, std::vector<double> error, double &C0, double &C1, double &C2)
{
    double chisq;
    gsl_matrix *X, *cov;
    gsl_vector *y, *w, *c;
    if(size != data.size())
    {
        size = data.size();

        if(work != NULL)
            gsl_multifit_linear_free(work);
         work = gsl_multifit_linear_alloc (size, 3);
    }
    X = gsl_matrix_alloc (size, 3);
    y = gsl_vector_alloc (size);
    w = gsl_vector_alloc (size);
    c = gsl_vector_alloc (3);
    cov = gsl_matrix_alloc (3, 3);
    for (int i = 0; i < size; i++)
    {
        gsl_matrix_set (X, i, 0, 1.0);
        gsl_matrix_set (X, i, 1, i);
        gsl_matrix_set (X, i, 2, i*i);

        gsl_vector_set (y, i, data[i]);
        gsl_vector_set (w, i, 1.0/(error[i]*error[i]));
    }
    gsl_multifit_wlinear (X, w, y, c, cov, &chisq, work);
    gsl_matrix_free(X);
    gsl_matrix_free(cov);
    gsl_vector_free(y);
    gsl_vector_free(w);
    gsl_vector_free(c);
    C0 = gsl_vector_get(c, 0);
    C1 = gsl_vector_get(c, 1);
    C2 = gsl_vector_get(c, 2);
}

float gausianPeak(OpqFrame* frame)
{
    std::vector<double> fft = frame->fft;

    int index =  distance(fft.begin(), max_element(fft.begin() + 4, fft.begin() + fft.size()/2));

    std::vector <double> peak;
    std::vector <double> error;
    for(int i = 0; i < 9; i++)
    {
        peak.push_back(frame->fft[index - 4 + i]);
        error.push_back(1);
    }
    for(int i = 0; i < peak.size(); i++)
    {
        peak[i] = log(peak[i]);
    }
    double C0;
    double C1;
    double C2;
    parabolicFit(peak,error,C0, C1, C2);
    return index - 4 - C1/(C2*2);
}
