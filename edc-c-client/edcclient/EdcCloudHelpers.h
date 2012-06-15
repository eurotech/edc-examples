#ifndef _EDCCLOUDHELPERS_H
#define _EDCCLOUDHELPERS_H

#include "edcpayload.pb.h"
#include <list>

using namespace std;
using namespace edcdatatypes;

#define ACCOUNT_NAME             "cloud.client.account"
#define ASSET_ID                 "cloud.asset.id"
#define BROKER_URL               "cloud.client.broker.url"
#define CLEAN_START              "cloud.clean.start"
#define CLIENT_ID                "cloud.client.broker.client.id"
#define USERNAME                 "cloud.client.broker.username"
#define PASSWORD                 "cloud.client.broker.password"
#define KEEP_ALIVE               "cloud.client.broker.keep.alive"
#define RECONNECT_INTERVAL       "cloud.client.broker.reconnect.interval"
#define WILL_TOPIC_SUFFIX        "cloud.client.will.topic.suffix"
#define WILL_TOPIC               "cloud.client.will.topic"
#define WILL_MESSAGE             "cloud.client.will.message"
#define WILL_QOS                 "cloud.client.will.qos"
#define WILL_RETAIN              "cloud.client.will.retain"
#define DISCONNECT_TOPIC_SUFFIX  "cloud.client.disconnect.topic.suffix"
#define DISCONNECT_TOPIC         "cloud.client.disconnect.topic"
#define DISCONNECT_MESSAGE       "cloud.client.disconnect.message"
#define DISCONNECT_QOS           "cloud.client.disconnect.qos"
#define DISCONNECT_RETAIN        "cloud.client.disconnect.retain"
#define BIRTH_TOPIC_SUFFIX       "cloud.client.birth.topic.suffix"
#define BIRTH_TOPIC              "cloud.client.birth.topic"
#define BIRTH_MESSAGE            "cloud.client.will.message"
#define BIRTH_QOS                "cloud.client.birth.qos"
#define BIRTH_RETAIN             "cloud.client.birth.retain"
#define CONTROL_SUB_TOPICS       "cloud.client.control.sub.topic"
#define CONTROL_SUB_QOS          "cloud.client.control.sub.qos"


class EdcConfiguration{
	


private:
	string				m_accountName;
	string				m_assetId;
	string				m_brokerUrl;
	bool				m_cleanStart;
	string				m_clientId;
	string				m_username;
	string				m_password;
	short				m_keepAlive;
	short				m_reconnectInterval;
	string				m_willTopicSuffix;
	string				m_willTopic;
	string				m_willMessage;
	int					m_willQos;
	bool				m_willRetain;
	string				m_disconnectTopicSuffix;
	string				m_disconnectTopic;
	string				m_disconnectEdcMessage;
	int					m_disconnectQos;
	bool				m_disconnectRetain;
	string				m_birthTopicSuffix;
	string				m_birthTopic;
	EdcPayload			m_birthEdcPayload;
	int					m_birthQos;
	bool				m_birthRetain;
	list<string>		m_controlSubTopics;
	int					m_controlSubQos;    
	string 				m_controlRootTopic;

    /**
     * Creates and EdcConfiguration using the supplied parameter values.
     * 
     * @param accountName				The name of the account this device will connect under
     * @param assetId					The asset ID that will be used as the second level in the full topic space
     * @param brokerUrl					The URL of the broker
     * @param clientId					The MQtt client ID
     * @param username					The MQtt username
     * @param password					The MQtt password
     * @param keepAlive					The MQtt keepalive time in seconds
     * @param reconnectInterval			The interval that reconnect attempts are made
     * @param willMessage				The LWT message payload
     * @param willQos					The quality of service that the last LWT message is published on
     * @param willRetain				If the LWT should be retained
     */
public:

	EdcConfiguration(){}

    EdcConfiguration(string accountName,
                            string assetId,
                            string brokerUrl,
                            string clientId,
                            string username,
                            string password,
                            short keepAlive,
                            short reconnectInterval,
                            string willMessage,
                            int willQos,
                            bool willRetain) {
        
        m_accountName = accountName;
        m_assetId = assetId;
        m_brokerUrl = brokerUrl;
        m_cleanStart = true;
        m_clientId = clientId;
        m_username = username;
        m_password = password;
        m_keepAlive = keepAlive;
        m_reconnectInterval = reconnectInterval;
        m_willMessage = willMessage;
        m_willQos = willQos;
        m_willRetain = willRetain;
    }
    
    /**
     * Creates and EdcConfiguration using the supplied parameter values.
     * 
     * @param accountName				The name of the account this device will connect under
     * @param assetId					The asset ID that will be used as the second level in the full topic space
     * @param brokerUrl					The URL of the broker
     * @param clientId					The MQtt client ID
     * @param username					The MQtt username
     * @param password					The MQtt password
     * @param keepAlive					The MQtt keepalive time in seconds
     * @param reconnectInterval			The interval that reconnect attempts are made
     * @param willTopicSuffix
     * @param willTopic
     * @param willMessage
     * @param willQos
     * @param willRetain
     * @param disconnectTopicSuffix
     * @param disconnectTopic
     * @param disconnectEdcMessage
     * @param disconnectQos
     * @param disconnectRetain
     * @param birthTopicSuffix
     * @param birthTopic
     * @param birthEdcPayload
     * @param birthQos
     * @param birthRetain
     * @param controlSubTopics
     * @param controlSubQos
     */
     EdcConfiguration(string accountName,
                            string assetId,
                            string brokerUrl,
                            string clientId,
                            string username,
                            string password,
                            short keepAlive,
                            short reconnectInterval,
                            string willTopicSuffix,
                            string willTopic,
                            string willMessage,
                            int willQos,
                            bool willRetain,
                            string disconnectTopicSuffix,
                            string disconnectTopic,
                            string disconnectEdcMessage,
                            int disconnectQos,
                            bool disconnectRetain,
                            string birthTopicSuffix,
                            string birthTopic,
                            EdcPayload birthEdcPayload,
                            int birthQos,
                            bool birthRetain,
                            list<string> controlSubTopics,
                            int controlSubQos) {
        
        m_accountName = accountName;
        m_assetId = assetId;
        m_brokerUrl = brokerUrl;
        m_cleanStart = true;
        m_clientId = clientId;
        m_username = username;
        m_password = password;
        m_keepAlive = keepAlive;
        m_reconnectInterval = reconnectInterval;
        m_willTopicSuffix = willTopicSuffix;
        m_willTopic = willTopic;
        m_willMessage = willMessage;
        m_willQos = willQos;
        m_willRetain = willRetain;
        m_disconnectTopicSuffix = disconnectTopicSuffix;
        m_disconnectTopic = disconnectTopic;
        m_disconnectEdcMessage = disconnectEdcMessage;
        m_disconnectQos = disconnectQos;
        m_disconnectRetain = disconnectRetain;
        m_birthTopicSuffix = birthTopicSuffix;
        m_birthTopic = birthTopic;
        m_birthEdcPayload = birthEdcPayload;
        m_birthQos = birthQos;
        m_birthRetain = birthRetain;
        m_controlSubTopics = controlSubTopics;
        m_controlSubQos = controlSubQos;
    }


     void setAccountName(string accountName) {
        m_accountName = accountName;
    }


     void setReconnectInterval(short reconnectInterval) {
        m_reconnectInterval = reconnectInterval;
    }


     void setWillTopicSuffix(string willTopicSuffix) {
        m_willTopicSuffix = willTopicSuffix;
    }


     void setWillTopic(string willTopic) {
        m_willTopic = willTopic;
    }


     void setWillMessage(string willMessage) {
        m_willMessage = willMessage;
    }


     void setWillQos(int willQos) {
        m_willQos = willQos;
    }


     void setWillRetain(bool willRetain) {
        m_willRetain = willRetain;
    }


     void setDisconnectTopicSuffix(string disconnectTopicSuffix) {
        m_disconnectTopicSuffix = disconnectTopicSuffix;
    }


     void setDisconnectTopic(string disconnectTopic) {
        m_disconnectTopic = disconnectTopic;
    }


     void setDisconnectEdcMessage(string disconnectEdcMessage) {
        m_disconnectEdcMessage = disconnectEdcMessage;
    }


     void setDisconnectQos(int disconnectQos) {
        m_disconnectQos = disconnectQos;
    }


     void setDisconnectRetain(bool disconnectRetain) {
        m_disconnectRetain = disconnectRetain;
    }


     void setBirthTopicSuffix(string birthTopicSuffix) {
        m_birthTopicSuffix = birthTopicSuffix;
    }


     void setBirthTopic(string birthTopic) {
        m_birthTopic = birthTopic;
    }


     void setBirthEdcPayload(EdcPayload birthEdcPayload) {
        m_birthEdcPayload = birthEdcPayload;
    }


     void setBirthQos(int birthQos) {
        m_birthQos = birthQos;
    }


     void setBirthRetain(bool birthRetain) {
        m_birthRetain = birthRetain;
    }


     void setControlSubTopics(list<string> controlSubTopics) {
        m_controlSubTopics = controlSubTopics;
    }


     void setControlSubQos(int controlSubQos) {
        m_controlSubQos = controlSubQos;
    }


    // getters
     string getAccountName() {
        return m_accountName;
    }


     string getDefaultAssetId() {
        return m_assetId;
    }


     string getBrokerUrl() {
        return m_brokerUrl;
    }


     bool isCleanStart() {
        return m_cleanStart;
    }


     string getClientId() {
		 /*
        if (m_clientId == NULL) {
            m_clientId = NetUtils.getMacAddress();
        }
		*/
        return m_clientId;
    }


     string getUsername() {
        return m_username;
    }


     string getPassword() {
        return m_password;
    }


     short getKeepAlive() {
        return m_keepAlive;
    }


     short getReconnectInterval() {
        return m_reconnectInterval;
    }


     string getControlRootTopic() {
		 if (m_controlRootTopic.empty()) {
           string topic = "$EDC";

		   return topic;
        }
        else {
            return m_controlRootTopic;
        }
    }


     string getWillTopicSuffix() {
        return m_willTopicSuffix;
    }


     string getWillTopic() {
		 if (m_willTopic.empty()) {
        
		    string topic = "$EDC";
		    topic += "/";
            topic += m_accountName;
            topic += "/";
            topic += m_clientId;
            topic += "/";
            topic += m_willTopicSuffix;
		      
		    return topic;        
        }
        else {
            return m_willTopic;
        }
    }


     string getWillMessage() {
        return m_willMessage;
    }


     int getWillQos() {
        return m_willQos;
    }


     bool isWillRetain() {
        return m_willRetain;
    }


     string getDisconnectTopicSuffix() {
        return m_disconnectTopicSuffix;
    }


     string getDisconnectTopic() {
        if (m_disconnectTopic.empty()) {
		    string topic = "$EDC";
			topic += "/";
			topic += m_accountName;
            topic += "/";
            topic += m_clientId;
            topic += "/";
            topic += m_disconnectTopicSuffix;
		      
		    return topic;        
        }
        else {
            return m_disconnectTopic;
        }
    }


     string getDisconnectEdcMessage() {
        return m_disconnectEdcMessage;
    }


     int getDisconnectQos() {
        return m_disconnectQos;
    }


     bool isDisconnectRetain() {
        return m_disconnectRetain;
    }


     string getBirthTopicSuffix() {
        return m_birthTopicSuffix;
    }


     string getBirthTopic() {
        if (m_birthTopic.empty()) {
			string topic = "$EDC";
		    topic += "/";
			topic += m_accountName;
            topic += "/";
            topic += m_clientId;
            topic += "/";
            topic += m_birthTopicSuffix;

            return topic;
        }
        else {
            return m_birthTopic;
        }
    }


     EdcPayload getBirthEdcPayload() {
        return m_birthEdcPayload;
    }


     int getBirthQos() {
        return m_birthQos;
    }


     bool isBirthRetain() {
        return m_birthRetain;
    }


     list<string> getControlSubTopics() {
        return m_controlSubTopics;
    }


     int getControlSubQos() {
        return m_controlSubQos;
    }


    // setters
     void setKeepAlive(short seconds) {
        m_keepAlive = seconds;
    }


     void setUsername(string username) {
        m_username = username;
    }


     void setPassword(string password) {
        m_password = password;
    }


     void setClientId(string clientId) {
        m_clientId = clientId;
    }


     void setDefaultAssetId(string assetId) {
        m_assetId = assetId;
    }


     void setBrokerUrl(string brokerUrl) {
        m_brokerUrl = brokerUrl;
    }


     void setCleanStart(bool cleanStart) {
        m_cleanStart = cleanStart;
    }
};

#define UPTIME 					 "uptime"
#define DISPLAY_NAME 			 "display_name"
#define MODEL_NAME 				 "model_name"
#define MODEL_ID 				 "model_id"
#define PART_NUMBER 			 "part_number"
#define SERIAL_NUMBER 			 "serial_number"
#define FIRMWARE_VERSION 		 "firmware_version"
#define BIOS_VERSION 			 "bios_version"
#define OS 						 "os"
#define OS_VERSION 				 "os_version"
#define JVM_NAME 				 "jvm_name"
#define JVM_VERSION 			 "jvm_version"
#define JVM_PROFILE 			 "jvm_profile"
#define CONNECTION_INTERFACE 	 "connection_interface"
#define CONNECTION_IP 			 "connection_ip"



class EdcDeviceProfile {
	
private:

	string uptime;
	string displayName;
	string modelName;
	string modelId;
	string partNumber;
	string serialNumber;
	string firmwareVersion;
	string biosVersion;
	string os;
	string osVersion;
	string jvmName;
	string jvmVersion;
	string jvmProfile;
	string connectionInterface;
	string connectionIp;
	double latitude;
	double longitude;
	double altitude;

public:
	EdcDeviceProfile(){}
    /**
     * Constructs an EdcDeviceProfile from a Properties object that contains all the parameters 
     * that make up the profile.  
     * 
     * @param props		A Properties object containing all the device parameters.
     */
     /*
    EdcDeviceProfile(Properties props) {
		this(props.getProperty(UPTIME),
				props.getProperty(DISPLAY_NAME),
				props.getProperty(MODEL_NAME),
				props.getProperty(MODEL_ID),
				props.getProperty(PART_NUMBER),
				props.getProperty(SERIAL_NUMBER),
				props.getProperty(FIRMWARE_VERSION),
				props.getProperty(BIOS_VERSION),
				props.getProperty(OS),
				props.getProperty(OS_VERSION),
				props.getProperty(JVM_NAME),
				props.getProperty(JVM_VERSION),
				props.getProperty(JVM_PROFILE),
				props.getProperty(CONNECTION_INTERFACE),
				props.getProperty(CONNECTION_IP));
	}
	*/

    /**
     * Creates an EdcDeviceProfile using the values of the supplied parameters.
     * 
     * @param uptime				The length of time the unit has been powered on.
     * @param displayName			A readable display name for the device.
     * @param modelName				The device model name.
     * @param modelId				The device model ID.
     * @param partNumber			The part number of the device.
     * @param serialNumber			The serial number of the device.
     * @param firmwareVersion		The version of firmware running on the device.
     * @param biosVersion			The version of the BIOS on the device.
     * @param os					The name of the operating system
     * @param osVersion				The version of the operating system
     * @param jvmName				The name of the JVM
     * @param jvmVersion			The version of the JVM
     * @param jvmProfile			The profile of the JVM
     * @param connectionInterface	The name of the interface used to connect to the cloud
     * @param connectionIp			The IP address of the interface used to connect to the cloud
     */


   EdcDeviceProfile(string uptime, string displayName,
			string modelName, string modelId, string partNumber,
			string serialNumber, string firmwareVersion, string biosVersion,
			string os, string osVersion, string jvmName, string jvmVersion,
			string jvmProfile, string connectionInterface, string connectionIp) {

		this->uptime = uptime;
		this->displayName = displayName;
		this->modelName = modelName;
		this->modelId = modelId;
		this->partNumber = partNumber;
		this->serialNumber = serialNumber;
		this->firmwareVersion = firmwareVersion;
		this->biosVersion = biosVersion;
		this->os = os;
		this->osVersion = osVersion;
		this->jvmName = jvmName;
		this->jvmVersion = jvmVersion;
		this->jvmProfile = jvmProfile;
		this->connectionInterface = connectionInterface;
		this->connectionIp = connectionIp;
	}

    /**
     * Creates an EdcDeviceProfile using the values of the supplied parameters.  This constructor
     * also contains values for the GPS location of the device.
     * 
     * @param uptime				The length of time the unit has been powered on.
     * @param displayName			A readable display name for the device.
     * @param modelName				The device model name.
     * @param modelId				The device model ID.
     * @param partNumber			The part number of the device.
     * @param serialNumber			The serial number of the device.
     * @param firmwareVersion		The version of firmware running on the device.
     * @param biosVersion			The version of the BIOS on the device.
     * @param os					The name of the operating system
     * @param osVersion				The version of the operating system
     * @param jvmName				The name of the JVM
     * @param jvmVersion			The version of the JVM
     * @param jvmProfile			The profile of the JVM
     * @param connectionInterface	The name of the interface used to connect to the cloud
     * @param connectionIp			The IP address of the interface used to connect to the cloud
     * @param latitude				The latitude of the device's location
     * @param longitude				The longitude of the device's location
     * @param altitude				The altitude of the device's location
     */
      EdcDeviceProfile(string uptime, string displayName,
                            string modelName, string modelId, string partNumber,
                            string serialNumber, string firmwareVersion, string biosVersion,
                            string os, string osVersion, string jvmName, string jvmVersion,
                            string jvmProfile, string connectionInterface, string connectionIp,
                            double latitude, double longitude, double altitude) {
        EdcDeviceProfile(uptime, displayName,
             modelName, modelId, partNumber,
             serialNumber, firmwareVersion, biosVersion,
             os, osVersion, jvmName, jvmVersion,
             jvmProfile, connectionInterface, connectionIp);
        this->latitude = latitude;
        this->longitude = longitude;
        this->altitude = altitude;
    }
    

    
    /**
     * Returns The length of time the unit has been powered on.
     * 
     * @return	A string representing the length of time the device has been powered on.
     */
	 string getUptime() {
		return uptime;
	}
	
	/**
	 * Returns the readable display name for the device.
	 * 
     * @return	A string representing the readable display name for the device.
	 */
	 string getDisplayName() {
		return displayName;
	}
	
	/**
	 * Returns the device model name
	 * 
     * @return	A string representing the device model name
	 */
	 string getModelName() {
		return modelName;
	}
	
	/**
	 * Returns the device model ID.
	 * 
     * @return	A string representing the device model ID.
	 */
	 string getModelId() {
		return modelId;
	}
	
	/**
	 * Returns the part number of the device.
	 * 
     * @return	A string representing the part number of the device.
	 */
	 string getPartNumber() {
		return partNumber;
	}
	
	/**
	 * Returns the serial number of the device.
	 * 
     * @return	A string representing the serial number of the device.
	 */
	 string getSerialNumber() {
		return serialNumber;
	}
	
	/**
	 * Returns the version of firmware running on the device.
	 * 
     * @return	A string representing the version of firmware running on the device.
	 */
	 string getFirmwareVersion() {
		return firmwareVersion;
	}
	
	/**
	 * Returns the version of the BIOS on the device.
	 * 
     * @return	A string representing the version of the BIOS on the device.
	 */
	 string getBiosVersion() {
		return biosVersion;
	}
	
	/**
	 * Returns the name of the operating system.
	 * 
     * @return	A string representing the name of the operating system.
	 */
	 string getOs() {
		return os;
	}
	
	/**
	 * Returns the version of the operating system.
	 * 
     * @return	A string representing the version of the operating system.
	 */
	 string getOsVersion() {
		return osVersion;
	}
	
	/**
	 * Returns the name of the JVM.
	 * 
     * @return	A string representing the name of the JVM.
	 */
	 string getJvmName() {
		return jvmName;
	}
	
	/**
	 * Returns the version of the JVM.
	 * 
     * @return	A string representing the version of the JVM.
	 */
	 string getJvmVersion() {
		return jvmVersion;
	}
	
	/**
	 * Returns the profile of the JVM.
	 * 
     * @return	A string representing the profile of the JVM.
	 */
	 string getJvmProfile() {
		return jvmProfile;
	}
	
	/**
	 * Returns the name of the interface used to connect to the cloud.
	 * 
     * @return	A string representing the name of the interface used to connect to the cloud.
	 */
	 string getConnectionInterface() {
		return connectionInterface;
	}
	
	/**
	 * Returns the IP address of the interface used to connect to the cloud.
	 * 
     * @return	A string representing the IP address of the interface used to connect to the cloud.
	 */
	 string getConnectionIp() {
		return connectionIp;
	}
	
	/**
	 * Returns the latitude of the device's location.
	 * 
     * @return	A string representing the latitude of the device's location.
	 */
	 double getLatitude() {
        return latitude;
    }
	
	/**
	 * Returns the longitude of the device's location.
	 * 
     * @return	A string representing the longitude of the device's location.
	 */
     double getLongitude() {
        return longitude;
    }
	
	/**
	 * Returns the altitude of the device's location.
	 * 
     * @return	A string representing thealtitude of the device's location.
	 */
     double getAltitude() {
        return altitude;
    }

	/**
	 * Sets the length of time the unit has been powered on.
	 * 
     * @param uptime	A string representing the length of time the unit has been powered on.
	 */ 
     void setUptime(string uptime) {
        this->uptime = uptime;
    }

	/**
	 * Sets the readable display name for the device
	 * 
     * @param displayName	A string representing the readable display name for the device
	 */
     void setDisplayName(string displayName) {
        this->displayName = displayName;
    }

	/**
	 * Sets the device model name.
	 * 
     * @param modelName	A string representing the device model name.
	 */
     void setModelName(string modelName) {
        this->modelName = modelName;
    }

	/**
	 * Sets the device model ID.
	 * 
     * @param modelId	A string representing the device model ID.
	 */
     void setModelId(string modelId) {
        this->modelId = modelId;
    }

	/**
	 * Sets the part number of the device.
	 * 
     * @param partNumber	A string representing the part number of the device.
	 */
     void setPartNumber(string partNumber) {
        this->partNumber = partNumber;
    }

	/**
	 * Sets the serial number of the device.
	 * 
     * @param serialNumber	A string representing the serial number of the device.
	 */
     void setSerialNumber(string serialNumber) {
        this->serialNumber = serialNumber;
    }

	/**
	 * Sets the version of firmware running on the device.
	 * 
     * @param firmwareVersion	A string representing the version of firmware running on the device.
	 */
     void setFirmwareVersion(string firmwareVersion) {
        this->firmwareVersion = firmwareVersion;
    }

	/**
	 * Sets the version of the BIOS on the device.
	 * 
     * @param biosVersion	A string representing the version of the BIOS on the device.
	 */
     void setBiosVersion(string biosVersion) {
        this->biosVersion = biosVersion;
    }

	/**
	 * Sets the name of the operating system.
	 * 
     * @param os	A string representing the name of the operating system.
	 */
     void setOs(string os) {
        this->os = os;
    }

	/**
	 * Sets the version of the operating system.
	 * 
     * @param osVersion	A string representing the version of the operating system.
	 */
     void setOsVersion(string osVersion) {
        this->osVersion = osVersion;
    }

	/**
	 * Sets the name of the JVM.
	 * 
     * @param jvmName	A string representing the name of the JVM.
	 */
     void setJvmName(string jvmName) {
        this->jvmName = jvmName;
    }

	/**
	 * Sets the version of the JVM.
	 * 
     * @param jvmVersion	A string representing the version of the JVM.
	 */
     void setJvmVersion(string jvmVersion) {
        this->jvmVersion = jvmVersion;
    }

	/**
	 * Sets the profile of the JVM.
	 * 
     * @param jvmProfile	A string representing the profile of the JVM.
	 */
     void setJvmProfile(string jvmProfile) {
        this->jvmProfile = jvmProfile;
    }

	/**
	 * Sets the name of the interface used to connect to the cloud.
	 * 
     * @param connectionInterface	A string representing the name of the interface used to connect to the cloud.
	 */
     void setConnectionInterface(string connectionInterface) {
        this->connectionInterface = connectionInterface;
    }

	/**
	 * Sets the IP address of the interface used to connect to the cloud.
	 * 
     * @param connectionIp	A string representing the IP address of the interface used to connect to the cloud.
	 */
     void setConnectionIp(string connectionIp) {
        this->connectionIp = connectionIp;
    }

	/**
	 * Sets the latitude of the device's location.
	 * 
     * @param latitude	A string representing the latitude of the device's location.
	 */
     void setLatitude(double latitude) {
        this->latitude = latitude;
    }

	/**
	 * Sets the longitude of the device's location.
	 * 
     * @param longitude	A string representing the  longitude of the device's location.
	 */
     void setLongitude(double longitude) {
        this->longitude = longitude;
    }

	/**
	 * Sets the altitude of the device's location.
	 * 
     * @param altitude	A string representing the  altitude of the device's location.
	 */
     void setAltitude(double altitude) {
        this->altitude = altitude;
    }
};

class EdcSubscription {
	/**
	 * The topic to subscribe on.
	 */
private: 
	string m_semanticTopic;
	
	/**
	 * The Quality of Service to subscribe with.
	 */
	 int qos;
	
	/**
	 * The default constructor.
	 */
	EdcSubscription() {
		m_semanticTopic.erase();
		qos = 0;
	}
	
	/**
	 * A constructor method specifying the semantic topic as a String and QoS for the subscription. 
	 * 
	 * @param topic		A String object representing the semantic topic.
	 * @param qos		An integer indicating the Quality of Service.
	 */
public:
	
	EdcSubscription(string semanticTopic, int qos) {
		m_semanticTopic = semanticTopic;
		this->qos = qos;
	}

	/**
	 * Returns the subscription topic.
	 * 
	 * @return	A String object containing the semantic topic.
	 */
	string getSemanticTopic() {
		return m_semanticTopic;
	}

	/**
	 * Sets the subscription semantic topic.
	 * 
	 * @param topic	A String object containing the subscription.
	 */
	void setSemanticTopic(string semanticTopic) {
		m_semanticTopic = semanticTopic;
	}

	/**
	 * Returns the Quality of Service on the semantic topic.
	 * 
	 * @return	An integer indicating the Quality of Service.
	 */
	int getQos() {
		return qos;
	}

	/**
	 * Sets the Quality of Service of the subscription.
	 * 
	 * @param qos	An integer indicating the Quality of Service.
	 */
	void setQos(int qos) {
		this->qos = qos;
	}
	
	bool exactlyEquals(EdcSubscription sub) {
		if(m_semanticTopic.compare(sub.getSemanticTopic()) == 0 && this->qos==sub.getQos()) {
			return true;
		}
		return false;
	}

	bool equals(EdcSubscription sub) {
		return ((m_semanticTopic.compare(sub.getSemanticTopic())) == 0);
	}
};

#endif //_EDCCLOUDHELPERS_H