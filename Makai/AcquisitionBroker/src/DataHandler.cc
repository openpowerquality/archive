#include <zmqpp/socket.hpp>
#include <zmqpp/message.hpp>
#include <redox.hpp>
#include "DataHandler.hpp"
#include "util.h"
#include "opq.pb.h"

using namespace std;

DataHandler::DataHandler(Config &c, zmqpp::context &ctx) : _ctx(ctx), _config(c) {
}

void DataHandler::handle_data_loop() {

    //Socket for talking to Mauka
    auto backend_pub = zmqpp::socket(_ctx, zmqpp::socket_type::pub);
    backend_pub.connect(_config.backend_interface_pub);

    //Redis connection
    redox::Redox rdx;
    rdx.connect(_config.redis_host, _config.redis_port);
    std::string redisPass = _config.redis_pass;
    if (redisPass != "") {
        auto &c = rdx.commandSync<string>({"AUTH", redisPass});
        if (!c.ok()) {
            throw runtime_error("Could not connect to Redis");
        }
        c.free();
    }

    //Socket that pulls data from boxes.
    auto box_pull = zmqpp::socket{_ctx, zmqpp::socket_type::pull};
    auto server_cert = load_certificate(_config.private_cert);
    box_pull.set(zmqpp::socket_option::curve_server, true);
    box_pull.set(zmqpp::socket_option::curve_secret_key, server_cert.second);
    box_pull.set(zmqpp::socket_option::zap_domain, "global");
    box_pull.bind(_config.box_interface_pull);
    _done = false;
    while (!_done) {
        //Receive a data message
        zmqpp::message zm;
        box_pull.receive(zm);

        //Deserialize
        auto serialized_resp = zm.get(0);
        opq::proto::RequestDataMessage header;
        header.ParseFromString(serialized_resp);

        //Get the boxID and the event number
        auto boxid = header.boxid();
        auto sequence_number = header.sequence_number();
        if(header.type() == header.RESP)
        {
        //Push every part of the message except for the header to redis.
            for (int i = 1; i < zm.parts(); i++) {
                rdx.command({"RPUSH",
                             (EVENT_BOX_DATA_PREFIX + "." + to_string(sequence_number) + "." + to_string(boxid)),
                             zm.get(i)
                            });
            }
        }
    }

}
