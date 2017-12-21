package com.eurotech.cloud.geckoboard.resources;

import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import sun.net.www.protocol.http.AuthCacheImpl;
import sun.net.www.protocol.http.AuthCacheValue;

import com.eurotech.cloud.apis.v2.model.EdcMessage;
import com.eurotech.cloud.apis.v2.model.EdcMetric;
import com.eurotech.cloud.apis.v2.model.MessagesResult;
import com.eurotech.cloud.geckoboard.model.BulletResponse;
import com.eurotech.cloud.geckoboard.model.BulletResponse.Amber;
import com.eurotech.cloud.geckoboard.model.BulletResponse.Comparative;
import com.eurotech.cloud.geckoboard.model.BulletResponse.Current;
import com.eurotech.cloud.geckoboard.model.BulletResponse.Green;
import com.eurotech.cloud.geckoboard.model.BulletResponse.Item;
import com.eurotech.cloud.geckoboard.model.BulletResponse.Measure;
import com.eurotech.cloud.geckoboard.model.BulletResponse.Range;
import com.eurotech.cloud.geckoboard.model.BulletResponse.Red;
import com.eurotech.cloud.geckoboard.model.LineResponse;
import com.eurotech.cloud.geckoboard.model.LineResponse.Settings;
import com.eurotech.cloud.geckoboard.model.MeterResponse;
import com.eurotech.cloud.geckoboard.model.MeterResponse.MaxValue;
import com.eurotech.cloud.geckoboard.model.MeterResponse.MinValue;
import com.eurotech.cloud.geckoboard.model.NumberResponse;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;


@Path("/gecko")
public class GeckoResource {
    public static final String APIS_TEST_URL = "http://api-stage.everyware-cloud.com/v2/";

    static {
        AuthCacheValue.setAuthCache(new AuthCacheImpl());
        Authenticator.setDefault( new EdcAuthenticator());
    }

    public static class EdcAuthenticator extends Authenticator {
        public PasswordAuthentication getPasswordAuthentication () {
            return new PasswordAuthentication ("edcguest", "Welcome1".toCharArray());
        }
    }


    @GET
    @Path("health")
    @Produces({MediaType.APPLICATION_XML})
    public String getHealth() {
        return "OK";
    }


    @GET
    @Path("meter")
    @Produces({MediaType.APPLICATION_XML})
    public MeterResponse getMeter(@QueryParam("topic")   String topic,
                                  @QueryParam("metric")  String metric,
                                  @QueryParam("minText") @DefaultValue("")  String minText,
                                  @QueryParam("maxText") @DefaultValue("")  String maxText,
                                  @QueryParam("hours")   @DefaultValue("1") int hours) {
        Date now = new Date();
        Client       client = Client.create();
        WebResource apisWeb = client.resource(APIS_TEST_URL);
        WebResource messagesWebXml = apisWeb.path("messages.xml");
        messagesWebXml = messagesWebXml.queryParam("topic",     topic);
        messagesWebXml = messagesWebXml.queryParam("startDate", String.valueOf(now.getTime()));
        messagesWebXml = messagesWebXml.queryParam("endDate",   String.valueOf(now.getTime() - (long) 60*60*1000));
        messagesWebXml = messagesWebXml.queryParam("fetch", "metrics");
        MessagesResult result = messagesWebXml.get(MessagesResult.class);

        String curValue = null;
        String minValue = null;
        String maxValue = null;

        List<EdcMessage> messages = result.getMessages();
        if (messages != null) {

            for (EdcMessage msg : messages) {
                List<EdcMetric> metrics = msg.getEdcPayload().getMetrics().getMetrics();
                for (EdcMetric m : metrics) {
                    if (m.getName().equals(metric)) {
                        if (curValue == null) {
                            curValue = m.getValue();
                        }
                        if (minValue == null || Double.valueOf(m.getValue()) < Double.valueOf(minValue)) {
                            minValue = m.getValue();
                        }
                        if (maxValue == null || Double.valueOf(m.getValue()) > Double.valueOf(maxValue)) {
                            maxValue = m.getValue();
                        }
                    }
                }
            }
        } else {

            curValue = "0";
            minValue = "0";
            maxValue = "0";
        }

        MeterResponse meterResp = new MeterResponse();
        meterResp.setItem(curValue);

        MinValue min = new MinValue();
        min.setValue(minValue);
        min.setText(minText);
        meterResp.setMin(min);

        MaxValue max = new MaxValue();
        max.setValue(maxValue);
        max.setText(maxText);
        meterResp.setMax(max);

        return meterResp;
    }



    @GET
    @Path("number")
    @Produces({MediaType.APPLICATION_XML})
    public NumberResponse getNumber(@QueryParam("topic")    String topic,
                                    @QueryParam("metric")   String metric,
                                    @QueryParam("absolute") @DefaultValue("") String absolute,
                                    @QueryParam("type")     @DefaultValue("") String type) {
        Client       client = Client.create();
        WebResource apisWeb = client.resource(APIS_TEST_URL);
        WebResource messagesWebXml = apisWeb.path("messages.xml");
        messagesWebXml = messagesWebXml.queryParam("topic", topic);
        messagesWebXml = messagesWebXml.queryParam("limit", "2");
        messagesWebXml = messagesWebXml.queryParam("fetch", "metrics");

        MessagesResult result = messagesWebXml.get(MessagesResult.class);
        List<EdcMessage> messages = result.getMessages();

        String newValue = null;
        String oldValue = null;
        List<EdcMetric> metrics = null;
        if (messages != null && messages.size() == 2) {

            EdcMessage msgNew = messages.get(0);
            metrics = msgNew.getEdcPayload().getMetrics().getMetrics();
            for (EdcMetric m : metrics) {
                if (m.getName().equals(metric)) {
                    newValue = m.getValue();
                    break;
                }
            }

            EdcMessage msgOld = messages.get(1);
            metrics = msgOld.getEdcPayload().getMetrics().getMetrics();
            for (EdcMetric m : metrics) {
                if (m.getName().equals(metric)) {
                    oldValue = m.getValue();
                    break;
                }
            }
        }

        NumberResponse.Item itemNew = new NumberResponse.Item();
        itemNew.setValue(newValue);

        NumberResponse.Item itemOld = new NumberResponse.Item();
        itemOld.setValue(oldValue);

        List<NumberResponse.Item> items = new ArrayList<NumberResponse.Item>();
        items.add(itemNew);
        items.add(itemOld);

        NumberResponse numberResp = new NumberResponse();
        numberResp.setType(type);
        numberResp.setAbsolute(absolute);
        numberResp.setItems(items);

        return numberResp;
    }


    @GET
    @Path("bullet")
    @Produces({MediaType.APPLICATION_XML})
    public BulletResponse getBullet(@QueryParam("topic")       String topic,
                                    @QueryParam("metric")      String metric,
                                    @QueryParam("orientation") String orientation,
                                    @QueryParam("label")       String label,
                                    @QueryParam("sublabel")    String sublabel,
                                    @QueryParam("point")       List<String> points,
                                    @QueryParam("comparative") String comparative,
                                    @QueryParam("red_start")   String red_start,
                                    @QueryParam("red_end")     String red_end,
                                    @QueryParam("amber_start") String amber_start,
                                    @QueryParam("amber_end")   String amber_end,
                                    @QueryParam("green_start")   String green_start,
                                    @QueryParam("green_end")     String green_end) {
        Client       client = Client.create();
        WebResource apisWeb = client.resource(APIS_TEST_URL);
        WebResource messagesWebXml = apisWeb.path("messages.xml");
        messagesWebXml = messagesWebXml.queryParam("topic", topic);
        messagesWebXml = messagesWebXml.queryParam("limit", "1");
        messagesWebXml = messagesWebXml.queryParam("fetch", "metrics");
        MessagesResult result = messagesWebXml.get(MessagesResult.class);

        String curValue = null;
        List<EdcMessage> messages = result.getMessages();
        if (messages != null && messages.size() > 0) {

            EdcMessage msg = messages.get(0);
            List<EdcMetric> metrics = msg.getEdcPayload().getMetrics().getMetrics();
            for (EdcMetric m : metrics) {
                if (m.getName().equals(metric)) {
                    curValue = m.getValue();
                }
            }
        }

        BulletResponse bulletResp = new BulletResponse();
        bulletResp.setOrientation(orientation);

        Item item = new Item();
        item.setLabel(label);
        item.setSublabel(sublabel);
        item.setPoints(points);

        Comparative comp = new Comparative();
        comp.setPoint(comparative);
        item.setComparative(comp);

        Measure measure = new Measure();
        Current current = new Current();
        current.setStart("0");
        current.setEnd(curValue);
        measure.setCurrent(current);

//        Projected projected = new Projected();
//        projected.setStart("0");
//        projected.setEnd("23");
//        measure.setProjected(projected);

        Range range = new Range();
        Red red = new Red();
        red.setStart(red_start);
        red.setEnd(red_end);
        range.setRed(red);
        Amber amber = new Amber();
        amber.setStart(amber_start);
        amber.setEnd(amber_end);
        range.setAmber(amber);
        Green green = new Green();
        green.setStart(green_start);
        green.setEnd(green_end);
        range.setGreen(green);
        item.setRange(range);

        item.setMeasure(measure);
        bulletResp.setItem(item);

        return bulletResp;
    }


    @GET
    @Path("line")
    @Produces({MediaType.APPLICATION_XML})
    public LineResponse getLine(@QueryParam("topic")  String topic,
                                @QueryParam("metric") String metric,
                                @QueryParam("hours")  @DefaultValue("1") int hours) {
        Date now = new Date();
        Date old = new Date(now.getTime() - (long) 60*60*1000);
        Client       client = Client.create();
        WebResource apisWeb = client.resource(APIS_TEST_URL);
        WebResource messagesWebXml = apisWeb.path("messages.xml");
        messagesWebXml = messagesWebXml.queryParam("topic",     topic);
        messagesWebXml = messagesWebXml.queryParam("startDate", String.valueOf(now.getTime()));
        messagesWebXml = messagesWebXml.queryParam("endDate",   String.valueOf(old.getTime()));
        messagesWebXml = messagesWebXml.queryParam("fetch", "metrics");
        MessagesResult result = messagesWebXml.get(MessagesResult.class);

        List<EdcMessage> messages = result.getMessages();

        String minValue = null;
        String maxValue = null;
        List<String> items = new ArrayList<String>();
        for (EdcMessage msg : messages) {
            List<EdcMetric> metrics = msg.getEdcPayload().getMetrics().getMetrics();
            for (EdcMetric m : metrics) {
                if (m.getName().equals(metric)) {

                    items.add(m.getValue());
                    if (minValue == null || Double.valueOf(m.getValue()) < Double.valueOf(minValue)) {
                        minValue = m.getValue();
                    }
                    if (maxValue == null || Double.valueOf(m.getValue()) > Double.valueOf(maxValue)) {
                        maxValue = m.getValue();
                    }
                }
            }
        }

        LineResponse lineResp = new LineResponse();
        lineResp.setItems(items);

        Settings settings = new Settings();
        settings.setColour("ff9900");

        List<String> axisx = new ArrayList<String>();
        long xInterval = (now.getTime() - old.getTime())/5;
        DateFormat df = DateFormat.getTimeInstance(DateFormat.SHORT);
        for (int i=0; i<=5; i++) {
            axisx.add(df.format( new Date(old.getTime() + (long) xInterval*i)));
        }
        settings.setAxisx(axisx);

        List<String> axisy = new ArrayList<String>();
        double yInterval = (Double.valueOf(maxValue) - Double.valueOf(minValue))/5;
        NumberFormat nf = NumberFormat.getInstance();
        nf.setMaximumFractionDigits(2);
        for (int i=0; i<=5; i++) {
            axisy.add(nf.format(Double.valueOf(minValue) + (double) yInterval*i));
        }
        settings.setAxisy(axisy);

        lineResp.setSettings(settings);
        return lineResp;
    }

    @GET
    @Path("highline")
    @Produces({MediaType.APPLICATION_JSON})
    public String getHighLine(@QueryParam("topic")  String topic,
                              @QueryParam("metric") List<String> metrics,
                              @QueryParam("hours")  @DefaultValue("1") int hours) {
        Date now = new Date();
        Date old = new Date(now.getTime() - (long) 60*60*1000);
        Client       client = Client.create();
        WebResource apisWeb = client.resource(APIS_TEST_URL);
        WebResource messagesWebXml = apisWeb.path("messages.xml");
        messagesWebXml = messagesWebXml.queryParam("topic",     topic);
        messagesWebXml = messagesWebXml.queryParam("startDate", String.valueOf(now.getTime()));
        messagesWebXml = messagesWebXml.queryParam("endDate",   String.valueOf(old.getTime()));
        messagesWebXml = messagesWebXml.queryParam("fetch", "metrics");
        MessagesResult result = messagesWebXml.get(MessagesResult.class);

        List<EdcMessage> messages = result.getMessages();

        StringBuilder sb = new StringBuilder();
        sb.append("{")
        .append("chart:{type:'spline',zoomType:'x',renderTo:'container',plotBackgroundColor:'rgba(35,37,38,0)',backgroundColor:'rgba(35,37,38,100)',borderColor:'rgba(35,37,38,100)',lineColor:'rgba(35,37,38,100)',plotBorderColor:'rgba(35,37,38,100)',plotBorderWidth:null,plotShadow:false,height:340},")
        .append("credits:{enabled:false},")
        .append("colors:['#058DC7','#50B432','#EF561A'],")
        .append("legend:{layout:'horizontal',borderColor:'rgba(35,37,38,100)',itemWidth:55,margin:5,width:200},")
        .append("title:{text:null},")
//          .append("tooltip:{formatter:function(){return'<b>'+this.point.name+'</b>: '+this.y+' users';}},")
        .append("yAxis:{title:{text:null}},")
        .append("xAxis:{type:'datetime',dateTimeLabelFormats:{minute:'%H:%M',hour:'%H:%M',day:'%e/%m %H:%M',month:'%e. %b',year:'%b'}},");

        sb.append("series: [");
        for (int i=0; i<metrics.size(); i++) {

            String metric = metrics.get(i);
            sb.append("{")
            .append("type: 'spline',")
            .append("name: '")
            .append(metric)
            .append("',")
            .append("data: [");

            for (int j=0; j<messages.size(); j++) {

                EdcMessage msg = messages.get(j);
                List<EdcMetric> msgMetrics = msg.getEdcPayload().getMetrics().getMetrics();
                for (EdcMetric m : msgMetrics) {
                    if (m.getName().equals(metric)) {
                        sb.append("[")
                        .append(msg.getTimestamp().getTime())
                        .append(",")
                        .append(m.getValue())
                        .append("]");
                    }
                }
                if (j != messages.size() - 1) {
                    sb.append(",");
                }
            }
            sb.append("]}");
            if (i != metrics.size() - 1) {
                sb.append(",");
            }
        }
        sb.append("]")
        .append("}");

        return sb.toString();
    }
}
