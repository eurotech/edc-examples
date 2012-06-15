# !/bin/bash

. ./mqttlib_dir.sh

gcc -Wall \
 -I../edcclient/ -I../protobuf/src/ -I../org.eclipse.paho.mqtt.c/src/ \
 -L../protobuf/src/.libs/ -L../org.eclipse.paho.mqtt.c/build/${MQTTLIB_DIR}/ \
 -o edctest \
 edctest.cpp ../edcclient/edcpayload.pb.cc \
 -lmqttv3c -lprotobuf
