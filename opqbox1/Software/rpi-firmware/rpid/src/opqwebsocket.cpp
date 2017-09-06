#include "coresettings.hpp"
#include "datastructures.hpp"
#include "opqwebsocket.hpp"
#include "opqjson.h"

#include <boost/date_time/posix_time/posix_time.hpp>
#include <boost/algorithm/string.hpp>
#include <boost/thread/thread.hpp>
#include <cstdint>
#include <ctime>
#include <stdio.h>
#include <sys/time.h>
#include <stdexcept>
#include <vector>

OpqWebsocket::OpqWebsocket(FrameQueuePointer iq)
{
    opqSettings_ = OpqSettings::Instance();
    wsUrl_ = boost::get<std::string>(opqSettings_->getSetting("ws.url"));
    deviceId_ = boost::get<uint64_t>(opqSettings_->getSetting("device.id"));
    deviceKey_ = boost::get<std::string>(opqSettings_->getSetting("device.key"));
    pingInterval_ = boost::get<int32_t>(opqSettings_->getSetting("device.pinginterval"));
    time(&lastPing_);
    lastEvent_ = 0;
    iq_ = iq;
    ws_ = NULL;
}

void OpqWebsocket::init()
{
    if(ws_ != NULL)
    {
        delete ws_;
        ws_ = NULL;
    }
    while(ws_ == NULL)
    {
        boost::this_thread::sleep(boost::posix_time::milliseconds(1000));
        ws_ = easywsclient::WebSocket::from_url(wsUrl_);
        boost::this_thread::interruption_point();
    }
}

void OpqWebsocket::callback(std::string message)
{
    if(ws_->getReadyState() != easywsclient::WebSocket::OPEN)
    {
        return;
    }
    std::cout << "ws recv:" << message << "\n";
}

void OpqWebsocket::run()
{
    time_t ts;
    double timeDiff;

    try
    {
        init();
        sendPingPacket();
        while(true)
        {
            if(ws_ == NULL || ws_->getReadyState() != easywsclient::WebSocket::OPEN)
            {
                    init();
            }
            // Check device queue
            bool ok;
            OpqFrame* next = iq_->pop_timeout(10, ok);
            if(ok) {
                handleFrame(next);
                delete next;
            }

            // Check for messages from cloud
                boost::function<void (std::string)> f;
                f = boost::bind(&OpqWebsocket::callback, this, _1);
                ws_->poll(10);
                ws_->dispatch(f);

            // Check if we need to send a ping

            time(&ts);
            timeDiff = difftime(ts, lastPing_);
            if(timeDiff > pingInterval_)
            {
                sendPingPacket();
                time(&lastPing_);
            }

            // Check if we've been interrupted
            boost::this_thread::interruption_point();
        }
    }
    catch(boost::thread_interrupted &e)
    {
        return;
    }
}

void OpqWebsocket::send(std::string message)
{
    if(ws_ != NULL && ws_->getReadyState() == easywsclient::WebSocket::OPEN)
    {
        ws_->send(message);
    }
}

void OpqWebsocket::sendPingPacket()
{
    struct timeval tv;
    gettimeofday(&tv, NULL);
    uint64_t timestamp = (uint64_t) tv.tv_sec * 1000 + (uint64_t) tv.tv_usec / 1000;
    std::vector<double> payload;

    char * json;

    jsonify(&json, (uint32_t) 0x00C0FFEE, (uint32_t) 0, deviceId_, deviceKey_, timestamp,
           0, (uint32_t) 0, 0.0, 0.0, (uint32_t) 0, payload);

    send(std::string(json));
    free(json);
}

void OpqWebsocket::handleFrame(OpqFrame *frame)
{
    OpqParameters parameters = frame->parameters;
    time_t ts;
    double timeDiff;
    uint32_t packetType = boost::get<int>(parameters["event.type"]);

    // Throttle event and frequency events
    constructAndSend(frame);
}

void OpqWebsocket::constructAndSend(OpqFrame *frame) {
    OpqParameters parameters = frame->parameters;
    uint32_t packetType = boost::get<int>(parameters["event.type"]);
    double frequency = (parameters.find("f") == parameters.end()) ? 0.0 : boost::get<double>(parameters["f"]);
    double voltage = (parameters.find("vrms") == parameters.end()) ? 0.0 : boost::get<double>(parameters["vrms"]);
    uint32_t duration = frame->duration;
    uint64_t timestamp = (uint64_t) frame->timeSec * 1000 + (uint64_t) frame->timeUsec / 1000;

    std::vector<double> payload;
    for(int i = 0; i < frame->data.size(); i++) {
      payload.push_back(frame->data[i]);
    }

    uint32_t payloadSize = (uint32_t) payload.size();

    char * json;
    jsonify(&json, (uint32_t) 0x00C0FFEE, packetType, deviceId_, deviceKey_, timestamp,
           duration, (uint32_t) 0, frequency, voltage, (uint32_t) payload.size(), payload);

    send(std::string(json));
    free(json);
}
