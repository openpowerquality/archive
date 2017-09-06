#include "acquisitiontask.hpp"
#include "coresettings.hpp"
#include <boost/thread/exceptions.hpp>
#include "pinctl.hpp"

AcquisitionTask::AcquisitionTask(FrameQueuePointer oq) throw(std::runtime_error&)
{
    oq_ = oq;
    OpqSettings* set = OpqSettings::Instance();
    uart_.path = boost::get<std::string> (set->getSetting("uart.port"));
    if(uartInit(uart_) < 0)
        throw std::runtime_error("could not initialize UART.");
    exportPin(FLOW_CTL_PIN);
    setPinDirection(FLOW_CTL_PIN, OUT);
    setPinValue(FLOW_CTL_PIN, HIGH);
    exportPin(LED1);
    setPinDirection(LED1, OUT);
    setPinValue(LED1, HIGH);

}

AcquisitionTask::~AcquisitionTask()
{
    setPinValue(LED1, HIGH);
    unExportPin(FLOW_CTL_PIN);
    unExportPin(LED1);
}

void AcquisitionTask::run()
{
    OpqSettings* set = OpqSettings::Instance();
    try
    {
        bool ledState = false;
        while(true)
        {
            int blockSize = boost::get<int>(set->getSetting("uart.block_size"));
            //uartClear(uart_);
            setPinValue(FLOW_CTL_PIN, LOW);
            OpqFrame* next = uartRead(uart_, blockSize);
            setPinValue(FLOW_CTL_PIN, HIGH);
            oq_->push(next);
            boost::this_thread::interruption_point();
            if(ledState)
                setPinValue(LED1, LOW);
            else
                setPinValue(LED1, HIGH);
            ledState = !ledState;
        }
    }
    catch(boost::thread_interrupted &e)
    {
        setPinValue(LED1, HIGH);
        return;
    }
}
