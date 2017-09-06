#include "../lib/filtertask.hpp"
#include "coresettings.hpp"
#include "pinctl.hpp"

enum FilterState {FIDLE, FACCUMULATING};
enum PacketType {FGOOD, FMEASUREMENT, FBAD};
FilterTask::FilterTask(FrameQueuePointer iq, FrameQueuePointer oq)
{
    oq_ = oq;
    iq_ = iq;
    exportPin(LED2);
    setPinDirection(LED2, OUT);
    setPinValue(LED2, HIGH);
}

FilterTask::~FilterTask()
{
    setPinValue(LED2, HIGH);
    unExportPin(LED2);
}

void FilterTask::run()
{
    try
    {
        OpqSettings* set = OpqSettings::Instance();

        double Fexp = boost::get<double>(set->getSetting("filter.expected.f"));
        double Vexp = boost::get<double>(set->getSetting("filter.expected.vrms"));
        int updateF = boost::get<int>(set->getSetting("filter.measurement.f"));

        int frameCounter = 0;
        int badCounter = 0;
        OpqFrame* last = NULL;
        FilterState state= FIDLE;

        while(true)
        {
            OpqFrame* next = iq_->pop();
            PacketType type = FBAD;
            double Fthresh = boost::get<double>(set->getSetting("filter.thresh.f"));
            double Vthresh = boost::get<double>(set->getSetting("filter.thresh.vrms"));
            double Fmeas = boost::get<double>(next->parameters["f"]);
            double Vmeas = boost::get<double>(next->parameters["vrms"]);
            std::cout << Vmeas << std::endl;
            if(fabs(Fexp - Fmeas) >= Fthresh)
            {
                frameCounter = 0;
                next->parameters["event.type"] = EVENT_FREQUENCY;
                setPinValue(LED2, LOW);
                type = FBAD;
            }
            else if(fabs(Vexp - Vmeas) >= Vthresh)
            {
                frameCounter = 0;
                next->parameters["event.type"] = EVENT_VOLTAGE;
                setPinValue(LED2, LOW);
                type = FBAD;
            }
            else
            {
                frameCounter++;
                if(updateF > 0 && frameCounter > updateF)
                {
                    next->parameters["event.type"] = MEASUREMENT;
                    setPinValue(LED2, LOW);
                    frameCounter = 0;
                    type = FMEASUREMENT;
                }
                else
                {
                    setPinValue(LED2, HIGH);
                    type = FGOOD;
                }
            }
            switch(state)
            {
            case FIDLE:
                switch(type)
                {
                case FBAD:
                    last = next;
                    state = FACCUMULATING;;
                    break;
                case FMEASUREMENT:
                    oq_->push(next);
                    break;
                case FGOOD:
                    delete next;
                    break;
                }
                break;
            case FACCUMULATING:
                switch(type)
                {
                case FBAD:
                    if(badCounter < 15)
                    {
                        last->duration++;
                        delete next;
                        badCounter++;
                        break;
                    }

                case FMEASUREMENT:
                case FGOOD:
                    delete next;
                    badCounter = 0;
                    oq_->push(last);
                    state = FIDLE;
                    break;
                }
                break;
            }
        }
    }
    catch(boost::thread_interrupted &e)
    {
        setPinValue(LED2, HIGH);
        return;
    }
}
