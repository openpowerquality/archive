#!/bin/bash

protoc OpqProtocol.proto --java_out=sources/java
protoc OpqProtocol.proto --cpp_out=sources/cpp
