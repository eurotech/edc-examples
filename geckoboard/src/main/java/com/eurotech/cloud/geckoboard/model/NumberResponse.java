package com.eurotech.cloud.geckoboard.model;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="root")
@XmlAccessorType(XmlAccessType.FIELD)
public class NumberResponse {
    @XmlElement(name="absolute")
    private String absolute;

    @XmlElement(name="type")
    private String type;

    @XmlElement(name="item")
    private List<Item> items;

    public NumberResponse() {
    }

    public String getAbsolute() {
        return absolute;
    }

    public void setAbsolute(String absolute) {
        this.absolute = absolute;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<Item> getItems() {
        return items;
    }

    public void setItems(List<Item> items) {
        this.items = items;
    }


    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Item {

        @XmlElement(name="value")
        private String value;

        @XmlElement(name="text")
        private String text;

        public Item() {
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
