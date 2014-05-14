# !/bin/bash

. ./mqttlib_dir.sh

g++ -Wall \
 -I../edcclient/ -I../protobuf/src/ -I../org.eclipse.paho.mqtt.c/src/ -I../config \
 -L../protobuf/src/.libs/ -L../org.eclipse.paho.mqtt.c/build/${MQTTLIB_DIR}/ \
 -o edctest \
 edctest.cpp ../edcclient/edcpayload.pb.cc ../edcclient/config.cpp \
 -lmqttv3c -lpthread -lprotobuf
