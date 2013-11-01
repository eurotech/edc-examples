package com.eurotech.cloud.examples;

// This example class demonstrates the use of REST calls to query the Everyware Cloud
// First, several REST APIs are called to read devices, messages, etc. (assumes that the MQTT publish example has previously been run).
// Next, we create a Rule in the Everyware Cloud, and publish a message that triggers that rule.
// The Rule, in turn, republishes another message, and sends an e-mail notification.
// A REST call stores another message directly into the database.
// Then the REST API is used to read back all three messages: 1) original publish, 2) republish, and 3) direct data store.
// Finally, the Rule is deleted.

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.ws.rs.core.MediaType;

//import API model types used in examples
import com.eurotech.cloud.apis.v2.model.Account;
import com.eurotech.cloud.apis.v2.model.AccountsResult;
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
import com.eurotech.cloud.apis.v2.model.RulesResult;
import com.eurotech.cloud.apis.v2.model.TopicsResult;
import com.eurotech.cloud.apis.v2.model.xml.Parameter;
import com.eurotech.cloud.apis.v2.model.xml.ParametersMapType;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.filter.GZIPContentEncodingFilter;


/**
 * This example class demonstrates the use of REST calls to query the Everyware Cloud
 * <p>
 * First, several REST APIs are called to read devices, messages, etc. (it assumes that the EdcJavaClient example has previously been run).
 * <p>
 * Next, we create a Rule in the Everyware Cloud, and publish a message that triggers that rule. The Rule, in turn, republishes another message, and sends an e-mail notification. A REST call stores
 * another message directly into the database.
 * <p>
 * Then the REST API is used to read back all three messages: 1) original publish, 2) republish by the rule, and 3) direct data store.<br>
 * 
 * Finally, the Rule is deleted.
 * <p>
 * Notice that we use the Jersey REST Client Library with the GZIPContentEncoding Filter. The request will be modified to set the Accept-Encoding header to "gzip"
 * so the entity will be compressed using gzip.If the response has a Content-Encoding header of "gzip" then the response entity will be uncompressed using gzip.<br>
 * This will give us a performance boost for large amount of data (large messages or large quantity of messages). 
 * 
 */
public class EdcRestExample
{
    // >>>>>> Set these variables according to your Cloud user account
	public static final String API_URL = "https://api-sandbox.everyware-cloud.com/v2/";	// URL for API connection 
	public static final String ACCOUNT = "myEdcAccount";				// Cloud account name
	public static final String USERNAME = "myEdcUserName";				// Username in account, requires Administrative permissions
	public static final String PASSWORD = "myEdcPassword3#";			// Password associated with Username
	public static final String ASSET_ID = "EdcTest-Device";			// Unique Client ID of this client device
	public static final String TEST_EMAIL = "my.name@domain.com";		// E-mail address to use for this sample application
	public static final String TEST_EMAIL2 = "rule.changed@domain.com";		// E-mail address to test rule update
    // <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<

    static Client              client    = null;
    static String              ruleName  = "APIs Test Rule Name";


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
        // Add GZIPContentEncodingFilter to compress the content returned by the server
        client.addFilter(new GZIPContentEncodingFilter(true));

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
        listMetrics(ACCOUNT + "/+/sample/data");
        getMessageCount();

        System.out.println("##############\n Beginning test of listMessages()");
        listMessages("", 10, 0);
        paginateMessages(ACCOUNT+"/"+ASSET_ID+"/#",10,5);

        // Create Rule using REST, to trigger on receipt of a publish
        createRule();

        // Publish data using REST - because of the rule, a publish and a store event will be triggered
        restPublish();

        // Store data to Cloud directly using REST
        restStore();

        // Update Rule using REST
        updateRule();
        
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


    //
    // List Account names
    //
    private static void listAccounts() {
        System.out.println("\n##############\n Beginning test of listAccounts()");
        String apiPath = "accounts.xml";
        WebResource apisWeb = client.resource(API_URL).path(apiPath);
        AccountsResult  result = apisWeb.get(AccountsResult.class);
        List<Account> accounts = result.getAccounts();
        System.out.println("Accounts.size(): " + accounts.size());
        System.out.println("Account name: " + accounts.get(0).getName());
        sleep(1);
    }


    //
    // List devices in account
    //
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


    //
    // List topics that have been published
    //
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


    //
    // List metrics that have been published, searchByTopic()
    //
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


    //
    // Get count of messages in account
    //
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
            //get current date, minus recentSeconds, to use as startDate for message query
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

                try {
                    for (EdcMetric m : message.getEdcPayload().getMetrics().getMetrics()) {
                        printMessageMetric(m);
                    }
                } catch (NullPointerException npe) {
                    System.out.println("(message does not contain any metrics)");
                }
                System.out.println();
            }
        }
        sleep(1);
    }


    //
    // Print content of a message metric
    //
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


    /**
     * REST query for messages using pagination to demonstrate use of MessagesResult.limitExceeded flag.
     * 
     * @param topic
     *            Enter a <code>String</code> to searchByTopic, or an empty String
     * @param limit
     *            Enter non-zero int to limit total number of messages to read
     * @param pageSize
     *            size of the page of messages to read
     */
    private static void paginateMessages(String topic, int limit, int pageSize) {
        String apiPath = "";
        WebResource apisWeb = null;

        if (topic.isEmpty()) {
            apiPath = "messages.xml";
            apisWeb = client.resource(API_URL).path(apiPath);
        }
        else {
            apiPath = "messages/searchByTopic.xml";
            apisWeb = client.resource(API_URL).path(apiPath);
            apisWeb = apisWeb.queryParam("topic", topic);
        }

        MessagesResult result;
        int offset = 0;
        boolean endOfData=false;
        do {
            apisWeb = client.resource(API_URL).path(apiPath);
            if (!topic.isEmpty()) apisWeb = apisWeb.queryParam("topic", topic);
            apisWeb = apisWeb.queryParam("limit", "" + pageSize);
            if(offset>0)
            	apisWeb = apisWeb.queryParam("offset", "" + offset);
	        result = apisWeb.get(MessagesResult.class);
	        List<EdcMessage> messages = result.getMessages();
	        if (messages == null) {
	            System.err.println("listMessages() - There are no messages to list.");
	            break;
	        }
	        else {
	            for (int i = 0; i < messages.size(); i++) {
	                EdcMessage message = messages.get(i);
	                System.out.println("Message topic(): " + message.getTopic()+"  Received on: " + message.getTimestamp());
	            }
	            if(result.getLimitExceeded()) System.out.println("limitExceeded set, more messages to read");
	            else {
	            	System.out.println("End of data");
	            	endOfData=true;
	            }
	            offset+=pageSize;
	        }
        } while((!endOfData)&&(offset<limit));
        sleep(1);
    }


    //
    // Create a new Rule
    //
    private static void createRule() {
        System.out.println("\n##############\n Beginning test of createRule()");

        // First get Account ID
        String apiPath = "accounts.xml";
        WebResource apisWeb = client.resource(API_URL).path(apiPath);
        AccountsResult  result = apisWeb.get(AccountsResult.class);
        List<Account> accounts = result.getAccounts();
        long accountID = accounts.get(0).getId();

        // Create the rule
        RuleCreator ruleCreator = new RuleCreator();
        ruleCreator.setAccountId(accountID);
        ruleCreator.setName(ruleName);
        ruleCreator.setEnabled(true);
        ruleCreator.setDescription("APis Test Rule Description");
        ruleCreator.setQuery("select *, doubleMetric('pub_double_metric') as dbl from EdcMessageEvent where semanticTopic = \"pub/test\"");

        // Create e-mail action for the rule
        RuleActionConfiguration emailActionConfig = new RuleActionConfiguration();
        emailActionConfig.setRuleActionInfoName("email");

        List<Parameter> emailParams = new ArrayList<Parameter>();
        Parameter emailParam1 = new Parameter();
        emailParam1.setName("to");
        emailParam1.setValue(TEST_EMAIL);
        emailParams.add(emailParam1);

        Parameter emailParam2 = new Parameter();
        emailParam2.setName("subject");
        emailParam2.setValue("E-mail from REST rule");
        emailParams.add(emailParam2);

        Parameter emailParam3 = new Parameter();
        emailParam3.setName("body");
        emailParam3.setValue("This e-mail was generated in response to receiving a publish message on topic $semanticTopic, " +
                     "containing metric pub_double_metric= $dbl");
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
        // value of 'metrics' parameter is a REST invocation in JSON format 
        publishParam2.setValue("{\"metrics\":[{\"name\":\"rule_string_metric\", \"value\":\"new_string\", \"type\":\"String\"},{\"name\":\"rule_double_metric\", \"value\":\"$dbl\", \"type\":\"Double\"}]}");
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
        Rule rule = null;
        try {

            rule = rulesWeb.accept(MediaType.APPLICATION_XML).type(MediaType.APPLICATION_XML).post(Rule.class, ruleCreator);
            System.out.println("Created rule ID " + rule.getId());
        }
        catch (UniformInterfaceException uie) {
            boolean handled = false;
            ErrorBean errorBean = uie.getResponse().getEntity(ErrorBean.class);
            if (uie.getResponse().getClientResponseStatus().getStatusCode() == 400) {
                if (errorBean.getMessage().contains("ruleName already exists")) {
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


    //
    // Update rule previously created
    //
    private static void updateRule() {
        System.out.println("\n##############\n Beginning test of updateRule()");
    	
        WebResource apisWeb = client.resource(API_URL);
        WebResource rulesWeb = apisWeb.path("rules.xml");
        RulesResult result = rulesWeb.get(RulesResult.class);

        //find the rule that was created earlier
        Rule localRule = null;
        for (Rule rule : result.getRules()) {
            if (ruleName.equals(rule.getName())) {
                localRule = rule;
            }
        }

        // change email address
        RuleActionConfiguration config = localRule.getRuleActionConfigurations().get(0);
        for (Parameter param : config.getParameterValues().getParameters()) {
            if ("to".equals(param.getName())) {
                System.out.println("Change "+param.getValue()+" to "+TEST_EMAIL2);
                param.setValue(TEST_EMAIL2);
            }
        }
        Rule updatedRule = null;
        WebResource rulesUpdateWeb = apisWeb.path("rules/"+localRule.getId()+".xml");
        try {
            
            updatedRule = rulesUpdateWeb.accept(MediaType.APPLICATION_XML)        
                                        .type(MediaType.APPLICATION_XML)
                                        .put(Rule.class, localRule);
            System.out.println("Rule updated");
        }
        catch (UniformInterfaceException uie) {
            boolean handled = false;
            if (uie.getResponse().getClientResponseStatus().getStatusCode() == 500) {                
                ErrorBean errorBean = uie.getResponse().getEntity(ErrorBean.class);
                if (errorBean.getMessage().contains("MqttBrokerUnavailableException")) {
                    System.err.println("RULES_TEST_WARNING: Rules not confirmed.");
                    handled = true;
                }
            }
            if (!handled) {
                throw uie;
            }
        }
        
        // Read back rule to verify that it was updated
        rulesWeb = client.resource(API_URL).path("rules/" + localRule.getId() + ".xml");
        Rule ruleCheck;
        try {
        	ruleCheck = rulesWeb.accept(MediaType.APPLICATION_XML).type(MediaType.APPLICATION_XML).get(Rule.class);
        	config = ruleCheck.getRuleActionConfigurations().get(0);
        	for (Parameter param : config.getParameterValues().getParameters()) {
        		if ("to".equals(param.getName())) {
        			System.out.println("email = "+param.getValue());
        		}
        	}
        }
        catch (UniformInterfaceException uie) {
            ErrorBean errorBean = uie.getResponse().getEntity(ErrorBean.class);
            System.out.println(errorBean.getMessage());
            throw uie;
        }
    }
    
   //
    // Delete rule previously created
    //
    private static void deleteRule() {
        System.out.println("\n##############\n Beginning test of deleteRule()");

        WebResource apisWeb = client.resource(API_URL);
        WebResource rulesWeb = apisWeb.path("rules.xml");
        RulesResult result = rulesWeb.get(RulesResult.class);
        
        //find the rule that was created earlier
        Rule localRule = null;
        for (Rule rule : result.getRules()) {
            if (ruleName.equals(rule.getName())) {
                localRule = rule;
            }
        }

        long ruleID = 0;
        try {
            ruleID = localRule.getId();
        }
        catch (NullPointerException e) {
            System.err.println("deleteRule(): Couldn't get rule ID for previously created rule (" + ruleName + ").");
            return;
        }

        rulesWeb = client.resource(API_URL).path("rules/" + ruleID + ".xml");
        try {

            Rule deleteRule = rulesWeb.accept(MediaType.APPLICATION_XML).type(MediaType.APPLICATION_XML).delete(Rule.class);
        }
        catch (UniformInterfaceException uie) {
            if (uie.getResponse().getClientResponseStatus().getStatusCode() == 204) {
                System.out.println("Rule ID " + ruleID + " deleted.");
                return;
            }

            ErrorBean errorBean = uie.getResponse().getEntity(ErrorBean.class);
            System.out.println(errorBean.getMessage());
            throw uie;
        }
    }


    //
    // Publish a message using REST API
    //
    private static void restPublish() {
        // PUBLISH a message to the broker
        System.out.println("\n##############\n Beginning test of restPublish()");

        String pubTopic = "/pub/test";
        WebResource apisWeb = client.resource(API_URL);
        EdcPayload payload = createPayload("pub");
        payload.setBody("PUBLISH - store data".getBytes());

        EdcMessage msg = new EdcMessage();
        msg.setTopic(ACCOUNT + "/" + ASSET_ID + pubTopic);
        msg.setTimestamp(new Date());
        msg.setEdcPayload(payload);

        WebResource messagesWebStore = apisWeb.path("messages").path("publish");
        try {
            messagesWebStore.type(MediaType.APPLICATION_XML).accept(MediaType.APPLICATION_XML).post(msg);
        }
        catch (UniformInterfaceException uie) {
            ErrorBean errorBean = uie.getResponse().getEntity(ErrorBean.class);
            System.out.println(errorBean.getMessage());
            throw uie;
        }
        System.out.println("Published message using REST on topic: " + pubTopic);
    }


    //
    // Store message using REST API
    //
    private static void restStore() {
        // POST a message to the data store
        System.out.println("\n##############\n Beginning test of restStore()");

        String storeTopic = "/apis/test";
        WebResource apisWeb = client.resource(API_URL);
        EdcPayload payload = createPayload("api");
        payload.setBody("POST - store data".getBytes());

        EdcMessage msg = new EdcMessage();
        msg.setTopic(ACCOUNT + "/" + ASSET_ID + storeTopic);
        msg.setTimestamp(new Date());
        msg.setEdcPayload(payload);

        WebResource messagesWebStore = apisWeb.path("messages").path("store");
        messagesWebStore.type(MediaType.APPLICATION_XML).accept(MediaType.APPLICATION_XML).post(msg);
        System.out.println("Stored message using REST on topic: " + storeTopic);
    }


    //
    // Read back all messages just stored and published
    //
    private static void restRead() {
        System.out.println("\n##############\n Beginning test of restRead()");
        System.out.println("Waiting several seconds, to make sure all published messages have reached the account.");
        sleep(10);
        listMessages("", 0, 30);
    }


    //
    // Create an EdcPayload
    //
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


    // Sleep for a number of seconds
    private static void sleep(int seconds) {
        try {
            Thread.sleep(seconds * 1000);
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
