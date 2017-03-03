package com.eurotech.cloud.geckoboard.model;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;


@XmlRootElement(name="root")
@XmlAccessorType(XmlAccessType.FIELD)
public class LineResponse {
    @XmlElement(name="item")
    private List<String> items;

    @XmlElement(name="settings")
    private Settings settings;


    public LineResponse() {
    }


    public List<String> getItems() {
        return items;
    }

    public void setItems(List<String> items) {
        this.items = items;
    }

    public Settings getSettings() {
        return settings;
    }

    public void setSettings(Settings settings) {
        this.settings = settings;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Settings {

        @XmlElement(name="colour")
        private String colour;

        @XmlElement(name="axisx")
        private List<String> axisx;

        @XmlElement(name="axisy")
        private List<String> axisy;

        public Settings() {
        }

        public String getColour() {
            return colour;
        }

        public void setColour(String colour) {
            this.colour = colour;
        }

        public List<String> getAxisx() {
            return axisx;
        }

        public void setAxisx(List<String> axisx) {
            this.axisx = axisx;
        }

        public List<String> getAxisy() {
            return axisy;
        }

        public void setAxisy(List<String> axisy) {
            this.axisy = axisy;
        }
    }
}
