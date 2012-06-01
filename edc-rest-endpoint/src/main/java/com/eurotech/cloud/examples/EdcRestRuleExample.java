package com.eurotech.cloud.examples;


import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.ws.rs.core.MediaType;

import com.eurotech.cloud.apis.v2.model.Account;
import com.eurotech.cloud.apis.v2.model.EdcMessage;
import com.eurotech.cloud.apis.v2.model.EdcMetric;
import com.eurotech.cloud.apis.v2.model.EdcMetricsMapType;
import com.eurotech.cloud.apis.v2.model.EdcPayload;
import com.eurotech.cloud.apis.v2.model.ErrorBean;
import com.eurotech.cloud.apis.v2.model.Rule;
import com.eurotech.cloud.apis.v2.model.RuleActionConfiguration;
import com.eurotech.cloud.apis.v2.model.RuleCreator;
import com.eurotech.cloud.apis.v2.model.xml.Parameter;
import com.eurotech.cloud.apis.v2.model.xml.ParametersMapType;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;


/**
 * The Edc REST Endpoint Example illustrates how to setup a REST Endpoint 
 * to receive callbacks from the rules in the Everyware Device Cloud.
 * 
 * EdcRestRuleExample is a standalone java program which, using the Everyware Cloud
 * REST APIs, connects to your sandbox account, creates a could of Rules - one
 * with a REST Rule Action using the GET method and one using the POST method -
 * and posts a message which will trigger both rules.
 */
public class EdcRestRuleExample
{
    // >>>>>> Set these variables according to your Cloud user account
    public static final String API_URL   = "https://api-sandbox.everyware-cloud.com/v2/"; // URL for API connection
    public static final String USERNAME  = "jpttest2";                                    // Username in account, to use for API
    public static final String PASSWORD  = "We@come2";                                    // Password associated with Username
    public static final String CLIENT_ID = "MyEclipseClient2";                            // Unique Client ID of this client device
    //
    // Hostname and port of where the Edc REST Endpoint is deployed.
    // Very Important: Keep in mind this address must be reachable from the Internet for the notification to work!
    //
    public static final String REST_TARGET_ADDRESS = "10.1.1.1:8080";                   
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

        // Create Rule using REST, to trigger on receipt of a publish
        createGetRule();
        createPostRule();

        // Publish data using REST - because of the rule, an event will be triggered
        restPublish();
        sleep(2);

        // Delete the rule using REST
        // deleteRule();

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

    
    
    private static void createGetRule() {
        
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
        ruleCreator.setName("APIs Test GET Rest Rule Name");
        ruleCreator.setEnabled(true);
        ruleCreator.setDescription("APIs Test Rest  Rule Description");
        ruleCreator.setQuery("select *, doubleMetric('temperature') as temperature from EdcMessageEvent where doubleMetric('temperature') > 0.5");

        // Create publish action for the rule
        RuleActionConfiguration restActionConfig = new RuleActionConfiguration();
        restActionConfig.setRuleActionInfoName("rest");

        List<Parameter> publishParams = new ArrayList<Parameter>();
        Parameter publishParam1 = new Parameter();
        publishParam1.setName("url");
        publishParam1.setValue("http://"+REST_TARGET_ADDRESS+"/edc-rest-endpoint/notify?topic=$semanticTopic&asset=$asset&temperature=$temperature");
        publishParams.add(publishParam1);

        Parameter publishParam2 = new Parameter();
        publishParam2.setName("method");
        publishParam2.setValue("GET");
        publishParams.add(publishParam2);

        ParametersMapType publishParamsMap = new ParametersMapType();
        publishParamsMap.setParameters(publishParams);

        restActionConfig.setParameterValues(publishParamsMap);

        // Add all actions to the rule
        List<RuleActionConfiguration> actionConfigs = new ArrayList<RuleActionConfiguration>();
        actionConfigs.add(restActionConfig);
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


    private static void createPostRule() {
        
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
        ruleCreator.setName("APIs Test POST Rest Rule Name");
        ruleCreator.setEnabled(true);
        ruleCreator.setDescription("APIs Test Rest  Rule Description");
        ruleCreator.setQuery("select *, doubleMetric('temperature') as temperature from EdcMessageEvent where doubleMetric('temperature') > 0.5");

        // Create publish action for the rule
        RuleActionConfiguration restActionConfig = new RuleActionConfiguration();
        restActionConfig.setRuleActionInfoName("rest");

        List<Parameter> publishParams = new ArrayList<Parameter>();
        Parameter publishParam1 = new Parameter();
        publishParam1.setName("url");
        publishParam1.setValue("http://"+REST_TARGET_ADDRESS+"/edc-rest-endpoint/notify");
        publishParams.add(publishParam1);

        Parameter publishParam2 = new Parameter();
        publishParam2.setName("method");
        publishParam2.setValue("POST");
        publishParams.add(publishParam2);

        Parameter publishParam3 = new Parameter();
        publishParam3.setName("metrics");
        publishParam3
                .setValue("{\"metrics\":[{\"name\":\"topic\", \"value\":\"$semanticTopic\", \"type\":\"String\"},{\"name\":\"asset\", \"value\":\"$asset\", \"type\":\"String\"},{\"name\":\"temperature\", \"value\":\"$temperature\", \"type\":\"Double\"}]}");
        publishParams.add(publishParam3);


        ParametersMapType publishParamsMap = new ParametersMapType();
        publishParamsMap.setParameters(publishParams);

        restActionConfig.setParameterValues(publishParamsMap);

        // Add all actions to the rule
        List<RuleActionConfiguration> actionConfigs = new ArrayList<RuleActionConfiguration>();
        actionConfigs.add(restActionConfig);
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

    
    @SuppressWarnings("unused")
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

        msg = new EdcMessage();
        msg.setTopic(USERNAME + "/" + CLIENT_ID + pubTopic);
        msg.setTimestamp(new Date());
        msg.setEdcPayload(payload);

        messagesWebStore = apisWeb.path("messages").path("publish");
        messagesWebStore.type(MediaType.APPLICATION_XML).accept(MediaType.APPLICATION_XML).post(msg);
        System.out.println("Published message using REST on topic: " + pubTopic);
    }



    private static EdcPayload createPayload(String prefix) {
        EdcMetric metric = null;
        List<EdcMetric> metrics = new ArrayList<EdcMetric>();

        // create new metrics for a message
        metric = new EdcMetric();
        metric.setName("temperature");
        metric.setType("double");
        metric.setValue("0.75");
        metrics.add(metric);

        // build the payload
        EdcMetricsMapType metricsMap = new EdcMetricsMapType();
        metricsMap.setMetrics(metrics);

        EdcPayload payload = new EdcPayload();
        payload.setMetrics(metricsMap);

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
