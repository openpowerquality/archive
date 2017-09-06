#include <iostream>
#include <cmath>
#include <vector>
#include <stdlib.h>

#include "coresettings.hpp"
#include <string>
#include "acquisitiontask.hpp"
#include "powertransformtask.hpp"
#include "analysistask.hpp"
#include "filtertask.hpp"
#include "opqwebsocket.hpp"

#include <boost/thread/thread.hpp>
#include <boost/date_time.hpp>
#include <endian.h>

using namespace std;


int main(int argc, char** argv)
{
    OpqSettings *set = OpqSettings::Instance();
    char *settingsFile = getenv ("OPQD_SETTINGS_FILE");
    if(settingsFile == NULL)
    {
        set->loadFromFile(std::string("settings.set"));
    }
    else
    {
        set->loadFromFile(std::string(settingsFile));
        cout << "Reading settings from " << settingsFile << endl;
    }
    FrameQueuePointer acqQ(new FrameQueue);
    FrameQueuePointer fftQ(new FrameQueue);
    FrameQueuePointer anaQ(new FrameQueue);
    FrameQueuePointer fltrQ(new FrameQueue);

    AcquisitionTask *acq = new AcquisitionTask(acqQ);
    PowerTransformTask *fft = new PowerTransformTask(acqQ, fftQ);
    AnalysisTask * ana = new AnalysisTask(fftQ, anaQ);
    FilterTask * fltr = new FilterTask(anaQ, fltrQ);
    OpqWebsocket *ws = new OpqWebsocket(fltrQ);

    boost::this_thread::sleep(boost::posix_time::millisec(1000));
    boost::thread_group threads;
    threads.add_thread(new boost::thread(&AcquisitionTask::run, acq));
    threads.add_thread(new boost::thread(&PowerTransformTask::run, fft));
    threads.add_thread(new boost::thread(&AnalysisTask::run, ana));
    threads.add_thread(new boost::thread(&FilterTask::run, fltr));
    threads.add_thread(new boost::thread(&OpqWebsocket::run, ws));
    threads.join_all();
    delete acq;
    delete fft;
    delete ana;
    delete fltr;
    delete ws;
    return 0;
}
