#include "analysistask.hpp"
#include "gausianpeak.hpp"
#include "rmsvoltage.hpp"
#include "coresettings.hpp"
AnalysisTask::AnalysisTask(FrameQueuePointer iq, FrameQueuePointer oq)
{
    iq_ = iq;
    oq_ = oq;
}

void AnalysisTask::run()
{
    try
    {
        OpqSettings* set = OpqSettings::Instance();
        double SAMPLING_RATE = boost::get<double>(set->getSetting("cal.sampling_rate"));
        double VOLTAGE_SCALING = boost::get<double>(set->getSetting("cal.voltage_scaling"));
        while(true)
        {
            OpqFrame* next = iq_->pop();
            OpqSetting frequency = OpqSetting((double)(SAMPLING_RATE*gausianPeak(next)/(next->fft.size())));

            next->parameters["f"] = frequency;
            int start = 0;
            int end = 0;
            bool even = false;
            for(int i = 0; i< next->data.size() -1; i++)
            {
                if(start == 0)
                {
                    if(next->data[i]*next->data[i-1] < 0)
                        start = i;
                }
                else if(next->data[i]*next->data[i-1] < 0)
                {
                    if(even)
                        end = i;
                    even = !even;
                }
            }
            std::vector<double> dataNoEdges;
            dataNoEdges.resize(end - start, 0);
            std::copy(next->data.begin() + start, next->data.begin() + end, dataNoEdges.begin());
            next->parameters["vrms"] = (double)(VOLTAGE_SCALING*rmsVoltage(dataNoEdges));

            next->parameters["thd"] = "TO DO";
            for(size_t i = 0; i < next->data.size(); i++)
            {
                next->data[i] *= VOLTAGE_SCALING;
            }
            oq_->push(next);
            boost::this_thread::interruption_point();
        }
    }
    catch(boost::thread_interrupted &e)
    {
        return;
    }
}
