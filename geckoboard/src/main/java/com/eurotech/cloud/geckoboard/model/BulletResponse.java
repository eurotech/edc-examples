package com.eurotech.cloud.geckoboard.model;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="root")
@XmlAccessorType(XmlAccessType.FIELD)
public class BulletResponse {
    @XmlElement(name="orientation")
    private String orientation;

    @XmlElement(name="item")
    private Item item;


    public BulletResponse() {
    }

    public String getOrientation() {
        return orientation;
    }

    public void setOrientation(String orientation) {
        this.orientation = orientation;
    }

    public Item getItem() {
        return item;
    }

    public void setItem(Item item) {
        this.item = item;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Item {

        @XmlElement(name="label")
        private String label;

        @XmlElement(name="sublabel")
        private String sublabel;

        @XmlElementWrapper(name="axis")
        @XmlElement(name="point")
        private List<String> points;

        @XmlElement(name="measure")
        private Measure measure;

        @XmlElement(name="comparative")
        private Comparative comparative;

        @XmlElement(name="range")
        private Range range;

        public Item() {
        }

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }

        public String getSublabel() {
            return sublabel;
        }

        public void setSublabel(String sublabel) {
            this.sublabel = sublabel;
        }

        public List<String> getPoints() {
            return points;
        }

        public void setPoints(List<String> points) {
            this.points = points;
        }

        public Measure getMeasure() {
            return measure;
        }

        public void setMeasure(Measure measure) {
            this.measure = measure;
        }

        public Comparative getComparative() {
            return comparative;
        }

        public void setComparative(Comparative comparative) {
            this.comparative = comparative;
        }

        public Range getRange() {
            return range;
        }

        public void setRange(Range range) {
            this.range = range;
        }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Measure {

        @XmlElement(name="current")
        private Current current;

        @XmlElement(name="projected")
        private Projected projected;

        public Measure() {
        }

        public Current getCurrent() {
            return current;
        }

        public void setCurrent(Current current) {
            this.current = current;
        }

        public Projected getProjected() {
            return projected;
        }

        public void setProjected(Projected projected) {
            this.projected = projected;
        }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Current {

        @XmlElement(name="start")
        private String start;

        @XmlElement(name="end")
        private String end;

        public Current() {
        }

        public String getStart() {
            return start;
        }

        public void setStart(String start) {
            this.start = start;
        }

        public String getEnd() {
            return end;
        }

        public void setEnd(String end) {
            this.end = end;
        }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Projected {

        @XmlElement(name="start")
        private String start;

        @XmlElement(name="end")
        private String end;

        public Projected() {
        }

        public String getStart() {
            return start;
        }

        public void setStart(String start) {
            this.start = start;
        }

        public String getEnd() {
            return end;
        }

        public void setEnd(String end) {
            this.end = end;
        }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Comparative {

        @XmlElement(name="point")
        public String point;

        public Comparative() {
        }

        public String getPoint() {
            return point;
        }

        public void setPoint(String point) {
            this.point = point;
        }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Range {

        @XmlElement(name="red")
        private Red red;

        @XmlElement(name="amber")
        private Amber amber;

        @XmlElement(name="green")
        private Green green;

        public Range() {
        }

        public Red getRed() {
            return red;
        }

        public void setRed(Red red) {
            this.red = red;
        }

        public Amber getAmber() {
            return amber;
        }

        public void setAmber(Amber amber) {
            this.amber = amber;
        }

        public Green getGreen() {
            return green;
        }

        public void setGreen(Green green) {
            this.green = green;
        }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Red {

        @XmlElement(name="start")
        private String start;

        @XmlElement(name="end")
        private String end;

        public Red() {
        }

        public String getStart() {
            return start;
        }

        public void setStart(String start) {
            this.start = start;
        }

        public String getEnd() {
            return end;
        }

        public void setEnd(String end) {
            this.end = end;
        }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Green {

        @XmlElement(name="start")
        private String start;

        @XmlElement(name="end")
        private String end;

        public Green() {
        }

        public String getStart() {
            return start;
        }

        public void setStart(String start) {
            this.start = start;
        }

        public String getEnd() {
            return end;
        }

        public void setEnd(String end) {
            this.end = end;
        }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Amber {

        @XmlElement(name="start")
        private String start;

        @XmlElement(name="end")
        private String end;

        public Amber() {
        }

        public String getStart() {
            return start;
        }

        public void setStart(String start) {
            this.start = start;
        }

        public String getEnd() {
            return end;
        }

        public void setEnd(String end) {
            this.end = end;
        }
    }
}
