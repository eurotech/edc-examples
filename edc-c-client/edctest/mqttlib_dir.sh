# !/bin/bash

MACHINETYPE=`uname -m`

if [ "${MACHINETYPE}" == "x86_64" ]; then
    MQTTLIB_DIR=linux_ia64
elif [ "${MACHINETYPE}" == "s390x" ]; then
	MQTTLIB_DIR=linux_s390x
else
  	MQTTLIB_DIR=linux_ia32
fi
