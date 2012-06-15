#!/bin/bash

set -e

pushd ./org.eclipse.paho.mqtt.c/build/
make MQTTCLIENT_DIR=../src/
popd

pushd ./protobuf/
chmod u+x ./configure
./configure
make clean
make
popd

pushd ./edctest/
chmod u+x build.sh
./build.sh
popd
