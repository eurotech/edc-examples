# !/bin/bash

. ./mqttlib_dir.sh

export LD_LIBRARY_PATH=../org.eclipse.paho.mqtt.c/build/${MQTTLIB_DIR}/:../protobuf/src/.libs/:${LD_LIBRARY_PATH}

./edctest
