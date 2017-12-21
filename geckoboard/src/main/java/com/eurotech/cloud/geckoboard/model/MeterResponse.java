package com.eurotech.cloud.geckoboard.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;


@XmlRootElement(name="root")
@XmlAccessorType(XmlAccessType.FIELD)
public class MeterResponse {
    @XmlElement(name="item")
    private String item;

    @XmlElement(name="type")
    private String type;

    @XmlElement(name="min")
    private MinValue min;

    @XmlElement(name="max")
    private MaxValue max;


    public MeterResponse() {
    }

    public String getItem() {
        return item;
    }

    public void setItem(String item) {
        this.item = item;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public MinValue getMin() {
        return min;
    }

    public void setMin(MinValue min) {
        this.min = min;
    }

    public MaxValue getMax() {
        return max;
    }

    public void setMax(MaxValue max) {
        this.max = max;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class MinValue {

        @XmlElement(name="value")
        private String value;

        @XmlElement(name="text")
        private String text;

        public MinValue() {
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class MaxValue {

        @XmlElement(name="value")
        private String value;

        @XmlElement(name="text")
        private String text;

        public MaxValue() {
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }
    }
}
