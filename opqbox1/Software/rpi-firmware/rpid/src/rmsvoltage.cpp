#include "rmsvoltage.hpp"
#include <cmath>

double rmsVoltage(vector<double> data)
{
    double av = 0;
    for(unsigned int i = 0; i < data.size(); i++)
    {
        data[i] *=data[i];
        av += data[i];
    }
    return sqrt(av/data.size());
}
