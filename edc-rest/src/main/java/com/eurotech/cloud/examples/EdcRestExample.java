package com.eurotech.cloud.examples;


import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.ws.rs.core.MediaType;

//import API model types used in examples
import com.eurotech.cloud.apis.v2.model.Account;
import com.eurotech.cloud.apis.v2.model.CountResult;
import com.eurotech.cloud.apis.v2.model.Device;
import com.eurotech.cloud.apis.v2.model.DevicesResult;
import com.eurotech.cloud.apis.v2.model.EdcMessage;
import com.eurotech.cloud.apis.v2.model.EdcMetric;
import com.eurotech.cloud.apis.v2.model.EdcMetricsMapType;
import com.eurotech.cloud.apis.v2.model.EdcPayload;
import com.eurotech.cloud.apis.v2.model.EdcPosition;
import com.eurotech.cloud.apis.v2.model.EdcTopicInfo;
import com.eurotech.cloud.apis.v2.model.ErrorBean;
import com.eurotech.cloud.apis.v2.model.MessagesResult;
import com.eurotech.cloud.apis.v2.model.MetricsResult;
import com.eurotech.cloud.apis.v2.model.Rule;
import com.eurotech.cloud.apis.v2.model.RuleActionConfiguration;
import com.eurotech.cloud.apis.v2.model.RuleCreator;
import com.eurotech.cloud.apis.v2.model.TopicsResult;
import com.eurotech.cloud.apis.v2.model.xml.Parameter;
import com.eurotech.cloud.apis.v2.model.xml.ParametersMapType;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;


/**
 * This example class demonstrates the use of REST calls to query the Everyware Cloud
 * 
 * First, several REST APIs are called to read devices, messages, etc. (it assumes that the EdcJavaClient example has previously been run).
 * 
 * Next, we create a Rule in the Everyware Cloud, and publish a message that triggers that rule. The Rule, in turn, republishes another message, and sends an e-mail notification. A REST call stores
 * another message directly into the database.
 * 
 * Then the REST API is used to read back all three messages: 1) original publish, 2) republish by the rule, and 3) direct data store.
 * 
 * Finally, the Rule is deleted.
 */
public class EdcRestExample
{
    // >>>>>> Set these variables according to your Cloud user account
    public static final String API_URL   = "https://api-sandbox.everyware-cloud.com/v2/"; // URL for API connection
    public static final String USERNAME  = "jpttest2";                                   // Username in account, to use for API
    public static final String PASSWORD  = "We@come2";                                   // Password associated with Username
    public static final String CLIENT_ID = "MyEclipseClient2";                           // Unique Client ID of this client device
    // <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<

    static Client              client    = null;
    static Rule                rule      = null;


    public static void main(String[] args) 
        throws Exception 
    {
        //
        // SSL configuration
        SSLContext ctx = SSLContext.getInstance("SSL");
        ctx.init(null, null, null);

        //
        // Client Config
        ClientConfig config = new com.sun.jersey.api.client.config.DefaultClientConfig();
        config.getProperties().put(com.sun.jersey.client.urlconnection.HTTPSProperties.PROPERTY_HTTPS_PROPERTIES, new com.sun.jersey.client.urlconnection.HTTPSProperties(getHostnameVerifier(), ctx));
        client = Client.create(config);
        client.addFilter(new com.sun.jersey.api.client.filter.HTTPBasicAuthFilter(USERNAME, PASSWORD));

        //
        // Verify connection to Cloud
        String apiPath = "accounts.xml";
        try {
            WebResource apisWeb = client.resource(API_URL).path(apiPath);
            apisWeb.get(new GenericType<List<Account>>() {
            });
        }
        catch (Exception e) {
            throw new Exception("\n\nUnable to connect to the cloud -- check your URL, " + "username, and password in a browser to make sure you can log in.\n" + "URL: " + API_URL + apiPath + "\n");
        }

        // Test of simple REST calls to read data from Cloud
        listAccounts();
        listDevices();
        listTopics();
        listMetrics(USERNAME + "/+/sample/data");
        getMessageCount();

        System.out.println("##############\n Beginning test of listMessages()");
        listMessages("", 10, 0);

        // Create Rule using REST, to trigger on receipt of a publish
        createRule();

        // Publish data using REST - because of the rule, a publish and a store event will be triggered
        restPublish();

        // Store data to Cloud directly using REST
        restStore();

        // Read the above data back from data store
        restRead();

        // Delete the rule using REST
        deleteRule();

        System.out.println("\n Done.");
    }


    private static HostnameVerifier getHostnameVerifier() {
        return new HostnameVerifier() {
            @Override
            public boolean verify(String hostname, javax.net.ssl.SSLSession sslSession) {
                return true;
            }
        };
    }


    private static void listAccounts() {
        System.out.println("\n##############\n Beginning test of listAccounts()");
        String apiPath = "accounts.xml";
        WebResource apisWeb = client.resource(API_URL).path(apiPath);
        List<Account> accounts = (List<Account>) apisWeb.get(new GenericType<List<Account>>() {
        });
        System.out.println("Accounts.size(): " + accounts.size());
        System.out.println("Account name: " + accounts.get(0).getName());
        sleep(1);
    }


    private static void listDevices() {
        System.out.println("\n##############\n Beginning test of listDevices()");
        String apiPath = "devices.xml";
        WebResource apisWeb = client.resource(API_URL).path(apiPath);
        DevicesResult result = apisWeb.get(DevicesResult.class);
        List<Device> devices = (List<Device>) result.getDevices();
        if (devices == null) {
            System.err.println("listDevices() - No devices presently listed in the account.");
        }
        else {
            System.out.println("Device.size(): " + devices.size());
            for (Device d : devices) {
                System.out.println("device- displayName:" + d.getDisplayName() + " lastEventOn:" + d.getLastEventOn());
            }
        }
        sleep(1);
    }


    private static void listTopics() {
        System.out.println("\n##############\n Beginning test of listTopics()");
        String apiPath = "topics.xml";
        WebResource apisWeb = client.resource(API_URL).path(apiPath);
        TopicsResult result = apisWeb.get(TopicsResult.class);
        List<EdcTopicInfo> topics = (List<EdcTopicInfo>) result.getTopics();
        if (topics == null) {
            System.err.println("listTopics() - No topics have been published to the account.");
        }
        else {
            System.out.println("\nTopicsResult.size(): " + topics.size());
            for (EdcTopicInfo t : topics) {
                System.out.println("topic:" + t.getFullTopic() + " lastMessageOn:" + t.getLastMessageTimestamp());
            }
        }
        sleep(1);
    }


    private static void listMetrics(String topic) {
        System.out.println("\n##############\n Beginning test of listMetrics(), search by topic: " + topic);
        String apiPath = "metrics/searchByTopic.xml";
        WebResource apisWeb = client.resource(API_URL).path(apiPath);
        apisWeb = apisWeb.queryParam("topic", topic);
        MetricsResult result = apisWeb.get(MetricsResult.class);
        List<EdcMetric> metrics = (List<EdcMetric>) result.getMetrics();
        if (metrics == null) {
            System.err.println("listMetrics() - No metrics have been published to the account on topic " + topic);
        }
        else {
            System.out.println("MetricsResult.size(): " + metrics.size());
            for (EdcMetric m : metrics) {
                System.out.println("metric: " + m.getName() + " " + m.getType());
            }
        }
        sleep(1);
    }


    private static void getMessageCount() {
        System.out.println("\n##############\n Beginning test of getMessageCount()");
        String apiPath = "messages/count.xml";
        WebResource apisWeb = client.resource(API_URL).path(apiPath);
        CountResult result = apisWeb.get(CountResult.class);
        System.out.println("Messages Count: " + result.getCount());
        System.out.println();
        sleep(1);
    }


    /**
     * REST query for messages
     * 
     * @param topic
     *            Enter a <code>String</code> to searchByTopic, or an empty String
     * @param limit
     *            Enter non-zero int to limit number of messages to read
     * @param recentSeconds
     *            Enter non-zero int to use startDate of number of seconds prior to current time
     */
    private static void listMessages(String topic, int limit, int recentSeconds) {
        String apiPath = "";
        WebResource apisWeb = null;
        long startDate;

        if (topic.isEmpty()) {
            apiPath = "messages.xml";
            apisWeb = client.resource(API_URL).path(apiPath);
        }
        else {
            apiPath = "messages/searchByTopic.xml";
            apisWeb = client.resource(API_URL).path(apiPath);
            apisWeb = apisWeb.queryParam("topic", topic);
        }

        if (limit > 0) {
            apisWeb = apisWeb.queryParam("limit", "" + limit);
        }

        if (recentSeconds > 0) {
            // get current date, minus 2 minutes, to use as startDate for message query
            startDate = (new Date()).getTime() - (recentSeconds * 1000);
            apisWeb = apisWeb.queryParam("startDate", "" + startDate);
            System.out.println("\nRead messages using startDate of: " + new Date(startDate) + " (long=" + startDate + ")\n");
        }

        MessagesResult result = apisWeb.get(MessagesResult.class);
        List<EdcMessage> messages = result.getMessages();
        if (messages == null) {
            System.err.println("listMessages() - There are mo messages to list.");
        }
        else {
            for (int i = 0; i < messages.size(); i++) {
                EdcMessage message = messages.get(i);
                System.out.println("Message topic(): " + message.getTopic());
                System.out.println("Received on: " + message.getTimestamp());
                for (EdcMetric m : message.getEdcPayload().getMetrics().getMetrics()) {
                    printMessageMetric(m);
                }
                System.out.println();
            }
        }
        sleep(1);
    }


    private static void printMessageMetric(EdcMetric m) {
        String content = "";
        if (m.getType().equalsIgnoreCase("Zbase64Binary")) {
            // display byte arrays as both hex characters and String
            byte[] contentArray = m.getValue().getBytes();
            for (int i = 0; i < contentArray.length; i++) {
                content = content + Integer.toHexString(0xFF & contentArray[i]) + " ";

            }
            content = content + " (as String: '" + new String(contentArray) + "')";
            System.out.println("metric: " + m.getName() + " " + m.getType() + " " + content);
        }
        else {
            System.out.println("metric: " + m.getName() + " " + m.getType() + " " + m.getValue());
        }
    }


    private static void createRule() {
        System.out.println("\n##############\n Beginning test of createRule()");

        // First get Account ID
        String apiPath = "accounts.xml";
        WebResource apisWeb = client.resource(API_URL).path(apiPath);
        List<Account> accounts = (List<Account>) apisWeb.get(new GenericType<List<Account>>() {
        });
        long accountID = accounts.get(0).getId();

        // Create the rule
        RuleCreator ruleCreator = new RuleCreator();
        ruleCreator.setAccountId(accountID);
        ruleCreator.setName("APIs Test Rule Name");
        ruleCreator.setEnabled(true);
        ruleCreator.setDescription("APis Test Rule Description");
        ruleCreator.setQuery("select *, doubleMetric('pub_double_metric') as dbl from EdcMessageEvent where semanticTopic = \"pub/test\"");

        // Create e-mail action for the rule
        RuleActionConfiguration emailActionConfig = new RuleActionConfiguration();
        emailActionConfig.setRuleActionInfoName("email");

        List<Parameter> emailParams = new ArrayList<Parameter>();
        Parameter emailParam1 = new Parameter();
        emailParam1.setName("to");
        emailParam1.setValue("jon.tandy@eurotech.com");
        emailParams.add(emailParam1);

        Parameter emailParam2 = new Parameter();
        emailParam2.setName("subject");
        emailParam2.setValue("E-mail from REST rule");
        emailParams.add(emailParam2);

        Parameter emailParam3 = new Parameter();
        emailParam3.setName("body");
        emailParam3.setValue("This e-mail was generated in response to receiving a publish message on topic $topic, " + "containing metric pub_double_metric= $dbl");
        emailParams.add(emailParam3);

        ParametersMapType emailParamsMap = new ParametersMapType();
        emailParamsMap.setParameters(emailParams);

        emailActionConfig.setParameterValues(emailParamsMap);

        // Create publish action for the rule
        RuleActionConfiguration publishActionConfig = new RuleActionConfiguration();
        publishActionConfig.setRuleActionInfoName("mqtt");

        List<Parameter> publishParams = new ArrayList<Parameter>();
        Parameter publishParam1 = new Parameter();
        publishParam1.setName("topic");
        publishParam1.setValue("$account/RulesAssistant/rule/test");
        publishParams.add(publishParam1);

        Parameter publishParam2 = new Parameter();
        publishParam2.setName("metrics");
        publishParam2
                .setValue("{\"metrics\":[{\"name\":\"new_string\", \"value\":\"$rule_string_metric\", \"type\":\"String\"},{\"name\":\"rule_double_metric\", \"value\":\"$dbl\", \"type\":\"Double\"}]}");
        publishParams.add(publishParam2);

        ParametersMapType publishParamsMap = new ParametersMapType();
        publishParamsMap.setParameters(publishParams);

        publishActionConfig.setParameterValues(publishParamsMap);

        // Add all actions to the rule
        List<RuleActionConfiguration> actionConfigs = new ArrayList<RuleActionConfiguration>();
        actionConfigs.add(emailActionConfig);
        actionConfigs.add(publishActionConfig);
        ruleCreator.setRuleActionConfigurations(actionConfigs);

        WebResource rulesWeb = client.resource(API_URL).path("rules.xml");
        try {

            rule = rulesWeb.accept(MediaType.APPLICATION_XML).type(MediaType.APPLICATION_XML).post(Rule.class, ruleCreator);
            System.out.println("Created rule ID " + rule.getId());
        }
        catch (UniformInterfaceException uie) {
            boolean handled = false;
            ErrorBean errorBean = uie.getResponse().getEntity(ErrorBean.class);
            if (uie.getResponse().getClientResponseStatus().getStatusCode() == 400) {
                if (errorBean.getMessage().contains("EdcDuplicateNameException")) {
                    System.err.println("createRule(): Unable to create rule -- EdcDuplicateNameException");
                    handled = true;
                    return;
                }
            }
            if (!handled) {
                System.out.println(errorBean.getMessage());
                throw uie;
            }
        }

        // Read back rule to verify that it was created
        rulesWeb = client.resource(API_URL).path("rules/" + rule.getId() + ".xml");
        Rule ruleCheck;
        try {

            ruleCheck = rulesWeb.accept(MediaType.APPLICATION_XML).type(MediaType.APPLICATION_XML).get(Rule.class);
            System.out.println("Rule verified with ID, " + ruleCheck.getId() + " and Query: " + ruleCheck.getQuery());
        }
        catch (UniformInterfaceException uie) {
            ErrorBean errorBean = uie.getResponse().getEntity(ErrorBean.class);
            System.out.println(errorBean.getMessage());
            throw uie;
        }
    }


    private static void deleteRule() {
        System.out.println("\n##############\n Beginning test of deleteRule()");

        long ruleID = 0;
        try {
            ruleID = rule.getId();
        }
        catch (NullPointerException e) {
            System.err.println("deleteRule(): Couldn't get rule ID from previous createRule().");
            // ruleID = 41; //use this to force a delete of specific rule
        }

        WebResource rulesWeb = client.resource(API_URL).path("rules/" + ruleID + ".xml");
        try {

            rule = rulesWeb.accept(MediaType.APPLICATION_XML).type(MediaType.APPLICATION_XML).delete(Rule.class);
        }
        catch (UniformInterfaceException uie) {
            if (uie.getResponse().getClientResponseStatus().getStatusCode() == 204) {
                System.out.println("Rule ID " + ruleID + " deleted.");
                return;
            }

            ErrorBean errorBean = uie.getResponse().getEntity(ErrorBean.class);
            if (errorBean.getMessage().contains("Rule not found")) {
                System.err.println("deleteRule(): Rule ID " + ruleID + " was not found and could not be deleted.");
            }
            else {
                System.out.println(errorBean.getMessage());
                throw uie;
            }
        }
    }


    private static void restPublish() {
        // PUBLISH a message to the broker
        System.out.println("\n##############\n Beginning test of restPublish()");

        String pubTopic = "/pub/test";
        EdcMessage msg = null;
        EdcPayload payload = null;
        WebResource apisWeb = client.resource(API_URL);
        WebResource messagesWebStore = null;
        payload = createPayload("pub");
        payload.setBody("PUBLISH - store data".getBytes());

        msg = new EdcMessage();
        msg.setTopic(USERNAME + "/" + CLIENT_ID + pubTopic);
        msg.setTimestamp(new Date());
        msg.setEdcPayload(payload);

        messagesWebStore = apisWeb.path("messages").path("publish");
        messagesWebStore.type(MediaType.APPLICATION_XML).accept(MediaType.APPLICATION_XML).post(msg);
        System.out.println("Published message using REST on topic: " + pubTopic);
    }


    private static void restStore() {
        // POST a message to the data store
        System.out.println("\n##############\n Beginning test of restStore()");

        String storeTopic = "/apis/test";
        EdcMessage msg = null;
        EdcPayload payload = null;
        WebResource apisWeb = client.resource(API_URL);
        WebResource messagesWebStore = null;
        payload = createPayload("api");
        payload.setBody("POST - store data".getBytes());

        msg = new EdcMessage();
        msg.setTopic(USERNAME + "/" + CLIENT_ID + storeTopic);
        msg.setTimestamp(new Date());
        msg.setEdcPayload(payload);

        messagesWebStore = apisWeb.path("messages").path("store");
        messagesWebStore.type(MediaType.APPLICATION_XML).accept(MediaType.APPLICATION_XML).post(msg);
        System.out.println("Published message using REST on topic: " + storeTopic);
    }


    private static void restRead() {
        // GET all recent messages back from data store using REST
        System.out.println("\n##############\n Beginning test of restRead()");
        System.out.println("Waiting several seconds, to make sure all published messages have reached the account.");
        sleep(10);
        listMessages("", 10, 30);
    }


    private static EdcPayload createPayload(String prefix) {
        EdcMetric metric = null;
        List<EdcMetric> metrics = new ArrayList<EdcMetric>();

        // create new metrics for a message
        metric = new EdcMetric();
        metric.setName(prefix + "_string_metric");
        metric.setType("string");
        metric.setValue("This is a String");
        metrics.add(metric);

        metric = new EdcMetric();
        metric.setName(prefix + "_int_metric");
        metric.setType("int");
        metric.setValue("123456789");
        metrics.add(metric);

        double rand = Math.random();
        metric = new EdcMetric();
        metric.setName(prefix + "_double_metric");
        metric.setType("double");
        metric.setValue("" + rand);
        metrics.add(metric);
        System.out.println("Created payload with metric '" + prefix + "_double_metric': " + rand);

        metric = new EdcMetric();
        metric.setName(prefix + "_boolean_metric");
        metric.setType("boolean");
        metric.setValue("true");
        metrics.add(metric);

        // build the payload
        EdcMetricsMapType metricsMap = new EdcMetricsMapType();
        metricsMap.setMetrics(metrics);

        EdcPayload payload = new EdcPayload();
        payload.setMetrics(metricsMap);

        EdcPosition position = new EdcPosition();
        position.setLatitude(0.5);
        position.setLongitude(0.5);
        payload.setPosition(position);

        return payload;
    }


    private static void sleep(int seconds) {
        try {
            Thread.sleep(seconds * 1000);
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
