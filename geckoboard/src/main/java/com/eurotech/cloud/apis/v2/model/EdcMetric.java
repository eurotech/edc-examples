// Generated by Enunciate
package com.eurotech.cloud.apis.v2.model;

/**
 * (no documentation provided)
 */
@javax.xml.bind.annotation.XmlType (
    name = "edcMetric",
    namespace = ""
)
@javax.xml.bind.annotation.XmlRootElement (
    name = "metric",
    namespace = ""
)
public class EdcMetric implements java.io.Serializable {

    private java.lang.String _type;
    private java.lang.String _value;
    private java.lang.String _name;

    /**
     * (no documentation provided)
     */
    @javax.xml.bind.annotation.XmlElement (
        name = "type",
        namespace = ""
    )
    public java.lang.String getType() {
        return this._type;
    }

    /**
     * (no documentation provided)
     */
    public void setType(java.lang.String _type) {
        this._type = _type;
    }

    /**
     * (no documentation provided)
     */
    @javax.xml.bind.annotation.XmlElement (
        name = "value",
        namespace = ""
    )
    public java.lang.String getValue() {
        return this._value;
    }

    /**
     * (no documentation provided)
     */
    public void setValue(java.lang.String _value) {
        this._value = _value;
    }

    /**
     * (no documentation provided)
     */
    @javax.xml.bind.annotation.XmlElement (
        name = "name",
        namespace = ""
    )
    public java.lang.String getName() {
        return this._name;
    }

    /**
     * (no documentation provided)
     */
    public void setName(java.lang.String _name) {
        this._name = _name;
    }

}
