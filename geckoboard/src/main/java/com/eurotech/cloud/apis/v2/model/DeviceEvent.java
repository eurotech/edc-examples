// Generated by Enunciate
package com.eurotech.cloud.apis.v2.model;

/**
 * (no documentation provided)
 */
@javax.xml.bind.annotation.XmlType (
    name = "deviceEvent",
    namespace = "http://eurotech.com/edc/2.0"
)
@javax.xml.bind.annotation.XmlRootElement (
    name = "deviceEvent",
    namespace = "http://eurotech.com/edc/2.0"
)
public class DeviceEvent implements java.io.Serializable {

    private java.lang.String _eventType;
    private java.lang.String _clientId;
    private java.lang.String _eventMessage;
    private java.util.Date _sentOn;
    private com.eurotech.cloud.apis.v2.model.EdcPosition _position;
    private java.lang.String _accountName;
    private java.util.Date _receivedOn;

    /**
     * (no documentation provided)
     */
    @javax.xml.bind.annotation.XmlElement (
        name = "eventType",
        namespace = "http://eurotech.com/edc/2.0"
    )
    public java.lang.String getEventType() {
        return this._eventType;
    }

    /**
     * (no documentation provided)
     */
    public void setEventType(java.lang.String _eventType) {
        this._eventType = _eventType;
    }

    /**
     * (no documentation provided)
     */
    @javax.xml.bind.annotation.XmlElement (
        name = "clientId",
        namespace = "http://eurotech.com/edc/2.0"
    )
    public java.lang.String getClientId() {
        return this._clientId;
    }

    /**
     * (no documentation provided)
     */
    public void setClientId(java.lang.String _clientId) {
        this._clientId = _clientId;
    }

    /**
     * (no documentation provided)
     */
    @javax.xml.bind.annotation.XmlElement (
        name = "eventMessage",
        namespace = "http://eurotech.com/edc/2.0"
    )
    public java.lang.String getEventMessage() {
        return this._eventMessage;
    }

    /**
     * (no documentation provided)
     */
    public void setEventMessage(java.lang.String _eventMessage) {
        this._eventMessage = _eventMessage;
    }

    /**
     * (no documentation provided)
     */
    @javax.xml.bind.annotation.XmlElement (
        name = "sentOn",
        namespace = "http://eurotech.com/edc/2.0"
    )
    public java.util.Date getSentOn() {
        return this._sentOn;
    }

    /**
     * (no documentation provided)
     */
    public void setSentOn(java.util.Date _sentOn) {
        this._sentOn = _sentOn;
    }

    /**
     * (no documentation provided)
     */
    @javax.xml.bind.annotation.XmlElement (
        name = "position",
        namespace = "http://eurotech.com/edc/2.0"
    )
    public com.eurotech.cloud.apis.v2.model.EdcPosition getPosition() {
        return this._position;
    }

    /**
     * (no documentation provided)
     */
    public void setPosition(com.eurotech.cloud.apis.v2.model.EdcPosition _position) {
        this._position = _position;
    }

    /**
     * (no documentation provided)
     */
    @javax.xml.bind.annotation.XmlElement (
        name = "accountName",
        namespace = "http://eurotech.com/edc/2.0"
    )
    public java.lang.String getAccountName() {
        return this._accountName;
    }

    /**
     * (no documentation provided)
     */
    public void setAccountName(java.lang.String _accountName) {
        this._accountName = _accountName;
    }

    /**
     * (no documentation provided)
     */
    @javax.xml.bind.annotation.XmlElement (
        name = "receivedOn",
        namespace = "http://eurotech.com/edc/2.0"
    )
    public java.util.Date getReceivedOn() {
        return this._receivedOn;
    }

    /**
     * (no documentation provided)
     */
    public void setReceivedOn(java.util.Date _receivedOn) {
        this._receivedOn = _receivedOn;
    }

}
