/**
 * Copyright (c) 2011, 2014 Eurotech and/or its affiliates
 *
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Eurotech
 *
 * ********************************************************************************************
 *   This application is an example demonstrating the use of Kura Modbus service combined with
 *   Kura MQTT client service.
 *   It has been built to manage the Everyware Cloud development kit EDCK4000.
 *
 *   This dev kit provides some leds, buttons and a quad-counter drived with a PLC. This PLC
 *   communicate through a serial connection (RS232) with the associated gateway that is running
 *   this application.
 *
 *   The application open a MQTT connection with Everyware Cloud and poll periodically the PLC
 *   status, publishing the metrics (leds status, counters value) at a periodic rate and when
 *   modification occurs.
 *
 *   The application also subscribes to a control topic allowing an external process to send some requests :
 *   - drive leds state
 *   - the counters
 *   - start an alarm
 * ************************************************************************************************
 */
package com.eurotech.framework.edcdevkit;

import java.lang.Thread.State;
import java.util.Date;
import java.util.Map;
import java.util.Properties;

import org.eclipse.kura.KuraException;
import org.eclipse.kura.cloud.CloudClient;
import org.eclipse.kura.cloud.CloudClientListener;
import org.eclipse.kura.cloud.CloudService;
import org.eclipse.kura.configuration.ConfigurableComponent;
import org.eclipse.kura.message.KuraPayload;
import org.eclipse.kura.protocol.modbus.ModbusProtocolDeviceService;
import org.eclipse.kura.protocol.modbus.ModbusProtocolErrorCode;
import org.eclipse.kura.protocol.modbus.ModbusProtocolException;
import org.eclipse.kura.system.SystemService;
import org.eclipse.kura.watchdog.CriticalComponent;
import org.eclipse.kura.watchdog.WatchdogService;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ModbusManager implements ConfigurableComponent, CriticalComponent, CloudClientListener {

    private static final Logger s_logger = LoggerFactory.getLogger(ModbusManager.class);

    private Thread m_thread;
    private boolean m_threadShouldStop;
    private Map<String, Object> m_properties;
    private CloudService m_cloudService;
    private SystemService m_systemService;
    private ModbusProtocolDeviceService m_protocolDevice;
    private CloudClient m_cloudAppClient = null;
    private WatchdogService m_watchdogService;

    private int slaveAddr;
    private int pollInterval;	// milliseconds
    private int publishInterval;	// seconds
    private boolean initLeds;
    private boolean clientIsConnected = false;
    private boolean configured;
    private boolean metricsChanged;
    private static boolean[] lastDigitalInputs = new boolean[8];
    private static boolean[] lastDigitalOutputs = new boolean[6];
    private static int[] lastAnalogInputs = new int[8];
    private static boolean iJustConnected = false;
    private long publishTime = 0L;

    private static boolean doConnection = true;

    private static Properties modbusProperties;
    private static boolean wdConfigured = false;
    private static boolean connectionFailed = false;

    public void setModbusProtocolDeviceService(ModbusProtocolDeviceService modbusService) {
        this.m_protocolDevice = modbusService;
    }

    public void unsetModbusProtocolDeviceService(ModbusProtocolDeviceService modbusService) {
        this.m_protocolDevice = null;
    }

    public void setCloudService(CloudService cloudService) {
        this.m_cloudService = cloudService;
    }		// install event listener for serial ports

    public void unsetCloudService(CloudService cloudService) {
        this.m_cloudService = null;
    }

    public void setSystemService(SystemService sms) {
        this.m_systemService = sms;
    }

    public void unsetSystemService(SystemService sms) {
        this.m_systemService = null;
    }

    public void setWatchdogService(WatchdogService watchdogService) {
        this.m_watchdogService = watchdogService;
    }

    public void unsetWatchdogService(WatchdogService watchdogService) {
        this.m_watchdogService = null;
    }

    // ----------------------------------------------------------------
    //
    // Activation APIs
    //
    // ----------------------------------------------------------------

    protected void activate(ComponentContext componentContext, Map<String, Object> properties) {
        this.m_properties = properties;
        this.configured = false;

        modbusProperties = getModbusProperties();
        this.pollInterval = Integer.valueOf(modbusProperties.getProperty("pollInterval")).intValue();
        this.publishInterval = Integer.valueOf(modbusProperties.getProperty("publishInterval")).intValue();
        this.slaveAddr = Integer.valueOf(modbusProperties.getProperty("slaveAddr")).intValue();

        this.m_threadShouldStop = false;
        this.m_thread = new Thread(new Runnable() {

            @Override
            public void run() {
                Thread.currentThread().setName(getClass().getSimpleName());
                doModbusPollWork();
            }
        });
        this.m_thread.start();

        s_logger.info("ModbusManager activated");
    }

    protected void deactivate(ComponentContext componentContext) {
        s_logger.info("Modbus deactivate");
        if (this.m_watchdogService != null) {
            this.m_watchdogService.unregisterCriticalComponent(this);
        }
        this.m_threadShouldStop = true;
        while (this.m_thread.getState() != State.TERMINATED) {
            try {
                Thread.sleep(100);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        s_logger.info("Modbus polling thread killed");

        if (this.m_protocolDevice != null) {
            try {
                this.m_protocolDevice.disconnect();
            } catch (ModbusProtocolException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        doConnection = true;
        this.configured = false;

        // Releasing the CloudApplicationClient
        s_logger.info("Releasing CloudClient...");
        this.m_cloudAppClient.release();
    }

    public void updated(Map<String, Object> properties) {
        s_logger.info("updated...");
        this.m_properties = properties;
        modbusProperties = getModbusProperties();
        this.pollInterval = Integer.valueOf(modbusProperties.getProperty("pollInterval")).intValue();
        this.publishInterval = Integer.valueOf(modbusProperties.getProperty("publishInterval")).intValue();
        this.slaveAddr = Integer.valueOf(modbusProperties.getProperty("slaveAddr")).intValue();
        this.configured = false;
    }

    // ----------------------------------------------------------------
    //
    // Private Methods
    //
    // ----------------------------------------------------------------

    // Main loop querying periodically the modbus device and publishing the data
    private void doModbusPollWork() {
        while (!this.m_threadShouldStop) {
            if (modbusProperties != null) {
                // Try to establish the MQTT connection until a connection is available
                if (doConnection) {
                    doConnection = doConnectionWork();
                }

                if (!this.configured) {
                    this.initLeds = false;
                    try {
                        if (!connectionFailed) {
                            s_logger.debug("configureDevice");
                        }
                        configureDevice();
                        connectionFailed = false;
                    } catch (ModbusProtocolException e) {
                        if (!connectionFailed) {
                            s_logger.warn("The modbus port is not yet available");
                        }
                        connectionFailed = true;
                    }
                }

                if (this.configured) {
                    try {
                        if (!this.initLeds) {
                            initializeLeds();
                        }
                        if (this.initLeds) {
                            // Registering this application in the watchdog service
                            if (!wdConfigured) {
                                if (this.m_watchdogService != null) {
                                    this.m_watchdogService.registerCriticalComponent(this);
                                    wdConfigured = true;
                                }
                            }

                            performPoll();
                        }
                    } catch (ModbusProtocolException e) {
                        if (e.getCode() == ModbusProtocolErrorCode.NOT_CONNECTED) {
                            s_logger.error("Error on modbus polling, closing connection ");
                            this.configured = false;
                        } else {
                            s_logger.error("Error on modbus polling : " + e.getCode());
                        }
                    }
                }
            }
            try {
                Thread.sleep(this.pollInterval);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // Establishing a MQTT client connection to Everyware Cloud
    private boolean doConnectionWork() {
        String topic = null;
        try {
            // wait for a valid topic configured
            if (modbusProperties == null) {
                return true;
            }

            topic = modbusProperties.getProperty("controlTopic");
            if (topic == null) {
                return true;
            }

            if (this.m_cloudAppClient == null) {
                // Attempt to get Master Client reference
                s_logger.debug("Getting Cloud Client");
                try {
                    this.m_cloudAppClient = this.m_cloudService.newCloudClient("ModbusManager");
                    this.m_cloudAppClient.addCloudClientListener(this);
                } catch (KuraException e) {
                    s_logger.debug("Cannot get a Cloud Client");
                    e.printStackTrace();
                }

            }

            s_logger.debug("Checking for an active MQtt connection...");
            this.clientIsConnected = this.m_cloudAppClient.isConnected();

            if (!this.clientIsConnected) {
                s_logger.debug("Waiting for Cloud Client to connect");
                return true;
            }

        } catch (Exception e) {
            s_logger.debug("Cloud client is not yet available..");
            return true;
        }

        // Successfully connected
        s_logger.info("Successfully connected the Cloud Client");
        try {
            iJustConnected = true;
            this.m_cloudAppClient.controlSubscribe(topic + "/#", 0);

            String assetIdEth0 = this.m_systemService.getPrimaryMacAddress();
            this.m_cloudAppClient.subscribe("RulesAssistant/" + topic + "/led/#", 0);
            this.m_cloudAppClient.subscribe("RulesAssistant/" + topic + "/resetcnt/#", 0);
            this.m_cloudAppClient.subscribe("RulesAssistant/" + topic + "/alarm", 0);
            s_logger.info("Successfully subscribed with assetIdEth0=" + assetIdEth0 + " on topic:" + topic);
        } catch (KuraException e) {
            s_logger.debug("Error issuing MQTT subscription");
            e.printStackTrace();
            return true;
        }

        return false;
    }

    // Configuring the serial connection and connecting to the modbus device
    private void configureDevice() throws ModbusProtocolException {
        if (this.m_protocolDevice != null) {
            this.m_protocolDevice.disconnect();

            this.m_protocolDevice.configureConnection(modbusProperties);

            this.configured = true;
        }
    }

    // Initialize the properties received from the ConfigurableComponent service
    // The properties are set in the OSGI-INF/metatype xml file
    private Properties getModbusProperties() {
        Properties prop = new Properties();

        if (this.m_properties != null) {
            String portName = null;
            String serialMode = null;
            String baudRate = null;
            String bitsPerWord = null;
            String stopBits = null;
            String parity = null;
            String ptopic = null;
            String ctopic = null;
            String pollInt = null;
            String pubInt = null;
            String Slave = null;
            String Mode = null;
            String timeout = null;
            if (this.m_properties.get("slaveAddr") != null) {
                Slave = (String) this.m_properties.get("slaveAddr");
            }
            if (this.m_properties.get("transmissionMode") != null) {
                Mode = (String) this.m_properties.get("transmissionMode");
            }
            if (this.m_properties.get("respTimeout") != null) {
                timeout = (String) this.m_properties.get("respTimeout");
            }
            if (this.m_properties.get("port") != null) {
                portName = (String) this.m_properties.get("port");
            }
            if (this.m_properties.get("serialMode") != null) {
                serialMode = (String) this.m_properties.get("serialMode");
            }
            if (this.m_properties.get("baudRate") != null) {
                baudRate = (String) this.m_properties.get("baudRate");
            }
            if (this.m_properties.get("bitsPerWord") != null) {
                bitsPerWord = (String) this.m_properties.get("bitsPerWord");
            }
            if (this.m_properties.get("stopBits") != null) {
                stopBits = (String) this.m_properties.get("stopBits");
            }
            if (this.m_properties.get("parity") != null) {
                parity = (String) this.m_properties.get("parity");
            }
            if (this.m_properties.get("publishTopic") != null) {
                ptopic = (String) this.m_properties.get("publishTopic");
            }
            if (this.m_properties.get("controlTopic") != null) {
                ctopic = (String) this.m_properties.get("controlTopic");
            }
            if (this.m_properties.get("pollInterval") != null) {
                pollInt = (String) this.m_properties.get("pollInterval");
            }
            if (this.m_properties.get("publishInterval") != null) {
                pubInt = (String) this.m_properties.get("publishInterval");
            }

            if (portName == null) {
                return null;
            }
            if (baudRate == null) {
                baudRate = "9600";
            }
            if (stopBits == null) {
                stopBits = "1";
            }
            if (parity == null) {
                parity = "0";
            }
            if (bitsPerWord == null) {
                bitsPerWord = "8";
            }
            if (Slave == null) {
                Slave = "1";
            }
            if (Mode == null) {
                Mode = "RTU";
            }
            if (timeout == null) {
                timeout = "1000";
            }
            if (ptopic == null) {
                ptopic = "eurotech/demo";
            }
            if (ctopic == null) {
                ctopic = "eurotech/demo";
            }
            if (pollInt == null) {
                pollInt = "500";
            }
            if (pubInt == null) {
                pubInt = "180";
            }

            prop.setProperty("connectionType", serialMode);
            prop.setProperty("port", portName);
            prop.setProperty("exclusive", "false");
            prop.setProperty("mode", "0");
            prop.setProperty("baudRate", baudRate);
            prop.setProperty("stopBits", stopBits);
            prop.setProperty("parity", parity);
            prop.setProperty("bitsPerWord", bitsPerWord);
            prop.setProperty("slaveAddr", Slave);
            prop.setProperty("transmissionMode", Mode);
            prop.setProperty("respTimeout", timeout);
            prop.setProperty("publishTopic", ptopic);
            prop.setProperty("controlTopic", ctopic);
            prop.setProperty("pollInterval", pollInt);
            prop.setProperty("publishInterval", pubInt);

            return prop;
        } else {
            return null;
        }
    }

    public int bcd2Dec(int bcdVal) {
        byte bcd = (byte) bcdVal;
        int decimal = (bcd & 0x000F) + ((bcd & 0x000F0) >> 4) * 10 + ((bcd & 0x00F00) >> 8) * 100
                + ((bcd & 0x0F000) >> 12) * 1000 + ((bcd & 0xF0000) >> 16) * 10000;

        return decimal;
    }

    // Main loop poll of the modbus device
    private void performPoll() throws ModbusProtocolException {
        KuraPayload payload = new KuraPayload();

        this.metricsChanged = false;

        int it4 = 0;
        int it5 = 0;
        int it6 = 0;
        boolean[] digitalInputs;

        digitalInputs = this.m_protocolDevice.readDiscreteInputs(this.slaveAddr, 2048, 8);

        payload.addMetric("t3", new Boolean(digitalInputs[2]));
        if (digitalInputs[2] != lastDigitalInputs[2] || iJustConnected) {
            this.metricsChanged = true;
            s_logger.info("t3=" + digitalInputs[2]);
        }
        lastDigitalInputs[2] = digitalInputs[2];

        payload.addMetric("t4", new Boolean(digitalInputs[3]));
        if (digitalInputs[3]) {
            it4 = 1;
        }
        payload.addMetric("it4", new Integer(it4));
        if (digitalInputs[3] != lastDigitalInputs[3] || iJustConnected) {
            s_logger.info("t4=" + digitalInputs[3]);
            this.metricsChanged = true;
        }
        lastDigitalInputs[3] = digitalInputs[3];

        payload.addMetric("t5", new Boolean(digitalInputs[4]));
        if (digitalInputs[4]) {
            it5 = 1;
        }
        payload.addMetric("it5", new Integer(it5));
        if (digitalInputs[4] != lastDigitalInputs[4] || iJustConnected) {
            s_logger.info("t5=" + digitalInputs[4]);
            this.metricsChanged = true;
        }
        lastDigitalInputs[4] = digitalInputs[4];

        payload.addMetric("t6", new Boolean(digitalInputs[5]));
        if (digitalInputs[5]) {
            it6 = 1;
        }
        payload.addMetric("it6", new Integer(it6));
        if (digitalInputs[5] != lastDigitalInputs[5] || iJustConnected) {
            s_logger.info("t6=" + digitalInputs[5]);
            this.metricsChanged = true;
        }
        lastDigitalInputs[5] = digitalInputs[5];

        int[] analogInputs = this.m_protocolDevice.readInputRegisters(this.slaveAddr, 512, 8);

        int c3 = bcd2Dec(analogInputs[2]);
        payload.addMetric("c3", new Integer(c3));
        if (c3 != lastAnalogInputs[2] || iJustConnected) {
            s_logger.info("c3=" + c3);
            this.metricsChanged = true;
        }
        lastAnalogInputs[2] = c3;

        int qc = bcd2Dec(analogInputs[7]);
        payload.addMetric("qc", new Integer(qc));
        if (qc != lastAnalogInputs[7] || iJustConnected) {
            s_logger.info("qc=" + qc);
            this.metricsChanged = true;
        }
        lastAnalogInputs[7] = qc;

        // LEDs
        boolean[] digitalOutputs = this.m_protocolDevice.readCoils(this.slaveAddr, 2048, 6);

        payload.addMetric("LED1", new Boolean(digitalOutputs[0]));
        if (digitalOutputs[0] != lastDigitalOutputs[0] || iJustConnected) {
            s_logger.info("LED1=" + digitalOutputs[0]);
            this.metricsChanged = true;
        }
        lastDigitalOutputs[0] = digitalOutputs[0];

        payload.addMetric("LED2", new Boolean(digitalOutputs[1]));
        if (digitalOutputs[1] != lastDigitalOutputs[1] || iJustConnected) {
            s_logger.info("LED2=" + digitalOutputs[1]);
            this.metricsChanged = true;
        }
        lastDigitalOutputs[1] = digitalOutputs[1];

        payload.addMetric("LED3", new Boolean(digitalOutputs[2]));
        if (digitalOutputs[2] != lastDigitalOutputs[2] || iJustConnected) {
            s_logger.info("LED3=" + digitalOutputs[2]);
            this.metricsChanged = true;
        }
        lastDigitalOutputs[2] = digitalOutputs[2];

        payload.addMetric("LED4red", new Boolean(digitalOutputs[3]));
        if (digitalOutputs[3] != lastDigitalOutputs[3] || iJustConnected) {
            s_logger.info("LED4red=" + digitalOutputs[3]);
            this.metricsChanged = true;
        }
        lastDigitalOutputs[3] = digitalOutputs[3];

        payload.addMetric("LED4green", new Boolean(digitalOutputs[4]));
        if (digitalOutputs[4] != lastDigitalOutputs[4] || iJustConnected) {
            s_logger.info("LED4green=" + digitalOutputs[4]);
            this.metricsChanged = true;
        }
        lastDigitalOutputs[4] = digitalOutputs[4];

        payload.addMetric("LED4blue", new Boolean(digitalOutputs[5]));
        if (digitalOutputs[5] != lastDigitalOutputs[5] || iJustConnected) {
            s_logger.info("LED4blue=" + digitalOutputs[5]);
            this.metricsChanged = true;
        }
        lastDigitalOutputs[5] = digitalOutputs[5];

        // refresh Watchdog
        if (wdConfigured) {
            if (this.m_watchdogService != null) {
                this.m_watchdogService.checkin(this);
            }
        }

        if (this.clientIsConnected) {
            iJustConnected = false;
            // publish data
            long now = System.currentTimeMillis();
            if (this.metricsChanged || now - this.publishTime > this.publishInterval * 1000L) {
                try {
                    String topic = modbusProperties.getProperty("publishTopic");
                    if (this.metricsChanged) {
                        s_logger.info("One of the metrics changed");
                    }
                    Date pubDate = new Date();
                    payload.setTimestamp(pubDate);

                    s_logger.info("Publishing on topic: " + topic);
                    if (this.m_cloudAppClient != null) {
                        this.m_cloudAppClient.publish(topic, payload, 0, false);
                    }
                    this.publishTime = System.currentTimeMillis();

                } catch (Exception e) {
                    e.printStackTrace();
                }
                this.metricsChanged = false;
            }
        }
    }

    private void initializeLeds() throws ModbusProtocolException {
        s_logger.debug("Initializing LEDs");	// once on startup, turn on each light
        for (int led = 1; led <= 6; led++) {
            TurnOnLED(led, true);
            try {
                Thread.sleep(200);
            } catch (Exception e) {
                e.printStackTrace();
            }
            TurnOnLED(led, false);
        }
        this.initLeds = true;
    }

    // let state management
    private void TurnOnLED(int LED, boolean On) throws ModbusProtocolException {
        boolean TurnON = true;
        boolean TurnOFF = false;
        try {
            switch (LED) {
            case 1:
                s_logger.info("Setting LED" + LED + " to " + On);
                break;
            case 2:
                s_logger.info("Setting LED" + LED + " to " + On);
                break;
            case 3:
                s_logger.info("Setting LED" + LED + " to " + On);
                break;
            case 4:
                s_logger.info("Setting LED4 red to " + On);
                break;
            case 5:
                s_logger.info("Setting LED4 green to " + On);
                break;
            case 6:
                s_logger.info("Setting LED4 blue to " + On);
                break;
            default:
                s_logger.warn("Error in TurnOnLED - LED " + LED + " is not valid.");
                break;
            }
            this.m_protocolDevice.writeSingleCoil(this.slaveAddr, 2047 + LED, On ? TurnON : TurnOFF);
        } catch (ModbusProtocolException e) {
            throw e;
        }
    }

    private void ProcessKuraPayload(String assetId, String topic, KuraPayload msg, int qos, boolean retained) {
        Object[] Names = msg.metricNames().toArray();
        Properties props = new Properties();

        for (Object name : Names) {
            try {
                String key = (String) name;
                String value = (String) msg.getMetric(key);
                props.put(key, value);
            } catch (Exception ex) {
                s_logger.error("Error processing metric {}", name);
            }
        }
        ProcessPayload(props, topic);
    }

    // main routine to process various control topics from EDC or byte payload format
    // - led topic to drive led state
    // - resetcnt topic to reset the counters
    // - alarm topic to start an alarm of your choice (to implement)
    private void ProcessPayload(Properties props, String topic) {
        String controlTopic = modbusProperties.getProperty("controlTopic");

        if (topic.contains(controlTopic + "/1")) {
            s_logger.debug("topic contains '" + controlTopic + "/1' ");
            ProcessLEDMessage("1", props);

        } else if (topic.contains(controlTopic + "/2")) {
            s_logger.debug("topic contains '" + controlTopic + "/2' ");
            ProcessLEDMessage("2", props);

        } else if (topic.contains(controlTopic + "/3")) {
            s_logger.debug("topic contains '" + controlTopic + "/3' ");
            ProcessLEDMessage("3", props);

        } else if (topic.contains(controlTopic + "/4")) {
            s_logger.debug("topic contains '" + controlTopic + "/4' ");
            ProcessLEDMessage("4", props);

        } else if (topic.contains(controlTopic + "/resetcnt")) {
            s_logger.debug("topic contains '" + controlTopic + "/resetcnt' ");
            ProcessResetMessage(topic.substring(topic.indexOf("resetcnt/") + 9), props);

        } else if (topic.contains(controlTopic + "/alarm")) {
            s_logger.warn("topic contains '" + controlTopic + "/alarm' ");
            ProcessAlarmMessage(props);
        }
    }

    // handle LED controls
    private void ProcessLEDMessage(String LED, Properties props) {
        try {
            if (LED.equals("1")) {
                TurnOnLED(1, Boolean.parseBoolean((String) props.get("light")));
            } else if (LED.equals("2")) {
                TurnOnLED(2, Boolean.parseBoolean((String) props.get("light")));
            } else if (LED.equals("3")) {
                TurnOnLED(3, Boolean.parseBoolean((String) props.get("light")));

            } else if (LED.equals("4")) {
                Object value = null;
                if ((value = props.get("red")) != null) {
                    TurnOnLED(4, Boolean.parseBoolean((String) value));
                }
                if ((value = props.get("green")) != null) {
                    TurnOnLED(5, Boolean.parseBoolean((String) value));
                }
                if ((value = props.get("blue")) != null) {
                    TurnOnLED(6, Boolean.parseBoolean((String) value));
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // reset counter values
    private void ProcessResetMessage(String counter, Properties props) {
        s_logger.warn("Process Reset " + counter + " with value " + Boolean.parseBoolean((String) props.get("value")));
        try {
            if (counter.equals("c3")) {
                clearCounter(3, Boolean.parseBoolean((String) props.get("value")));
            }
            if (counter.equals("c4")) {
                clearCounter(4, Boolean.parseBoolean((String) props.get("value")));
            }
            if (counter.equals("c5")) {
                clearCounter(5, Boolean.parseBoolean((String) props.get("value")));
            }
            if (counter.equals("c6")) {
                clearCounter(6, Boolean.parseBoolean((String) props.get("value")));
            }
            if (counter.equals("qc")) {
                clearCounter(12, Boolean.parseBoolean((String) props.get("value")));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void clearCounter(int counter, boolean On) {
        boolean TurnON = true;
        boolean TurnOFF = false;
        try {
            switch (counter) {
            case 3:
                s_logger.debug("Counter c3 reset " + On);
                break;
            case 4:
                s_logger.debug("Counter c4 reset " + On);
                break;
            case 5:
                s_logger.debug("Counter c5 reset " + On);
                break;
            case 6:
                s_logger.debug("Counter c6 reset " + On);
                break;
            case 12:
                s_logger.debug("Counter qc reset " + On);
                break;
            default:
                s_logger.warn("Error in clearCounter - Counter " + counter + " is not valid.");
                break;
            }
            this.m_protocolDevice.writeSingleCoil(this.slaveAddr, 3072 + counter - 1, On ? TurnON : TurnOFF);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void ProcessAlarmMessage(Properties props) {
        // String sirenStr = (String)props.get("siren");
        // if(sirenStr.equals("true")) {
        // siren.startSiren();
        // } else {
        // }
    }

    // ----------------------------------------------------------------
    //
    // Inherited methods from CloudClientListener
    //
    // ----------------------------------------------------------------

    @Override
    public void onControlMessageArrived(String deviceId, String appTopic, KuraPayload msg, int qos, boolean retain) {
        s_logger.info("EDC control message received on topic: " + appTopic);
        ProcessKuraPayload(deviceId, appTopic, msg, qos, retain);
    }

    @Override
    public void onMessageArrived(String deviceId, String appTopic, KuraPayload msg, int qos, boolean retain) {
        s_logger.debug("EDC message received on topic: " + appTopic);
        ProcessKuraPayload(deviceId, appTopic, msg, qos, retain);
    }

    @Override
    public void onConnectionLost() {
        s_logger.debug("Connection Lost");
    }

    @Override
    public void onConnectionEstablished() {
        // TODO Auto-generated method stub

    }

    @Override
    public void onMessagePublished(int messageId, String appTopic) {
        // TODO Auto-generated method stub
    }

    @Override
    public void onMessageConfirmed(int messageId, String appTopic) {
        // TODO Auto-generated method stub
    }

    // ----------------------------------------------------------------
    //
    // Inherited methods from CriticalComponent for Watchdog service
    //
    // ----------------------------------------------------------------

    @Override
    public String getCriticalComponentName() {
        return "ModbusManager";
    }

    @Override
    public int getCriticalComponentTimeout() {
        return this.pollInterval * 2; // return double of the pollInterval
    }

}
