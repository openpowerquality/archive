#include "powertransformtask.hpp"
#include "powerspectrum.hpp"

PowerTransformTask::PowerTransformTask(FrameQueuePointer iq, FrameQueuePointer oq)
{
    oq_ = oq;
    iq_ = iq;
}


void removeBaselineAndNormalize(vector<double> &signal)
{
     double average = std::accumulate(signal.begin(), signal.end(), 0);
     average /= signal.size();
     average /= 1<<16;
     for(size_t i = 0 ; i< signal.size(); i++)
     {
         signal[i] /= 1<<16;
         signal[i] -= average;
     }
}

void PowerTransformTask::run()
{
    try
    {
        while(true)
        {
            OpqFrame* next = iq_->pop();
            removeBaselineAndNormalize(next->data);
            next->fft = powerSpectrum(next->data);

            oq_->push(next);
            boost::this_thread::interruption_point();
        }
    }
    catch(boost::thread_interrupted &e)
    {
        return;
    }

}
