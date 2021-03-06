cmake_minimum_required(VERSION 3.0)
project(AcquisitionBroker)

set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -std=c++14")
include_directories(${AcquisitionBroker_SOURCE_DIR}/proto)
include_directories(${AcquisitionBroker_SOURCE_DIR}/lib)

if(EXISTS ${AcquisitionBroker_SOURCE_DIR}/proto)
    execute_process(
            WORKING_DIRECTORY ${AcquisitionBroker_SOURCE_DIR}/proto
            COMMAND git pull
    )
else()
    execute_process(
        WORKING_DIRECTORY ${AcquisitionBroker_SOURCE_DIR}
        COMMAND git clone https://github.com/openpowerquality/opq-proto.git proto
)
endif()
execute_process(
        COMMAND protoc -I=proto/ --cpp_out=proto/ proto/opq.proto
        WORKING_DIRECTORY ${AcquisitionBroker_SOURCE_DIR}
)
set(SOURCE_FILES
        src/broker.cc
        src/config.cc
        lib/config.hpp
        lib/json.hpp
        src/DataHandler.cc
        lib/DataHandler.hpp
        proto/opq.pb.cc
        proto/opq.pb.h
        lib/util.h
        src/RequestHandler.cc
        lib/RequestHandler.h)

add_executable(AcquisitionBroker ${SOURCE_FILES})

target_link_libraries(AcquisitionBroker zmq zmqpp stdc++fs protobuf pthread redox hiredis ev)
