package com.eurotech.cloud.examples;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;

import com.eurotech.cloud.apis.v2.model.EdcMessage;
import com.eurotech.cloud.apis.v2.model.EdcMetric;
import com.eurotech.cloud.apis.v2.model.EdcMetricsMapType;
import com.eurotech.cloud.apis.v2.model.EdcPayload;
import com.eurotech.cloud.apis.v2.model.MessagesResult;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;


/**
 * This is an example of how to define a RESTful endpoint that can be used as a callback for the Everyware Device Cloud Rules Engine.
 * The callback can be programmed to respond to GET or POST requests. 
 * 
 * For GET requests, query parameters can be used to send information about the event that triggered the rule.
 * The URL of the REST Rule Action can be parametrized using the names of the tokens selected by the Rule.
 * For example, the URL for a REST Rule Action based on GET HTTP method can be specified as the following:
 * http://host:post/edcapp/notify?account=$account&asset=$asset&avgQc=$avgQc
 * 
 * When a Rule is evaluated positively, the tokens prefixed by the dollar sign are substituted
 * with the corresponding value as returned by the evaluated Rule.
 *   
 * For POST requests, a full EdcMessage can be used to package information about the event that triggered the rule.
 * Follow the user interface in the Everyware Cloud Console to configure which parameters to include as metrics
 * in the EdcMessage sent as a body of the notification.  
 * 
 */
@Path("notify")
public class EdcRestEndpoint 
{
	private static final Logger s_logger = Logger.getLogger(EdcRestEndpoint.class.getName());
	
	private static final int   THREAD_POOL_SIZE = 5;
	private static final ExecutorService s_pool = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
	
    // >>>>>> Set these variables according to your Cloud user account
    public static final String API_URL   = "https://api-sandbox.everyware-cloud.com/v2/"; // URL for API connection
    public static final String USERNAME  = "jpttest2";                                   // Username in account, to use for API
    public static final String PASSWORD  = "We@come2";                                   // Password associated with Username
    public static final String CLIENT_ID = "MyEclipseClient2";                           // Unique Client ID of this client device
    // <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<

    
	
	/**
	 * This is an example of a REST callback using the HTTP GET method.
	 * The account and asset names are supplied as query parameters into the callback.
	 * For a GET callback method with this signature, the URL for a REST Rule Action based should be:
	 * http://host:post/edcapp/notify?account=$account&asset=$asset&avgQc=$avgQc
	 * 
	 * @param account THe name of the Account for this the Rule was triggered.
	 * @param asset The name of the Asset for this the Rule was triggered.
	 * @param avgQc The average Qc value the reached a certain threshold.
	 */
	@GET
	@Produces("text/plain")
	public String notify(@QueryParam("account") String account,
				         @QueryParam("asset") String asset,
					     @QueryParam("avgQc") String avgQc) {

		s_logger.info(">> GET - EdcRuleNotification.notify.");
		s_logger.info("account: "+account+" asset: "+asset+" avgQc: "+avgQc);

		//
		// Spawn off a thread to process the incoming notification and return.
		try {
			s_pool.execute( new NotificationHandler(account, asset, avgQc));
		}
		catch (Throwable t) {
			t.printStackTrace();
			throw new WebApplicationException(t);
		}
		return "OK";
	}

	
	/**
	 * This is an example of a REST callback using the HTTP GET method.
	 * The account and asset names are supplied as query parameters into the callback.
	 * For a GET callback method with this signature, the URL for a REST Rule Action based should be:
	 * http://host:post/edcapp/notify?account=$account&asset=$asset&avgQc=$avgQc
	 * 
	 * @param account THe name of the Account for this the Rule was triggered.
	 * @param asset The name of the Asset for this the Rule was triggered.
	 * @param avgQc The average Qc value the reached a certain threshold.
	 */
	@POST
	@Produces("text/plain")
	public String notify(byte[] payload) {

		s_logger.info(">> POST - EdcRuleNotification.notify.");
		try {

			com.eurotech.cloud.message.EdcPayload edcPayload = null;
			edcPayload = com.eurotech.cloud.message.EdcPayload.buildFromByteArray(payload);
			String account = (String) edcPayload.getMetric("account");
			String asset   = (String) edcPayload.getMetric("asset");
			String avgQc   = (String) edcPayload.getMetric("avgQc");
			
			s_logger.info("account: "+account+" asset: "+asset+" avgQc: "+avgQc);

			//
			// Spawn off a thread to process the incoming notification and return.
			s_pool.execute( new NotificationHandler(account, asset, avgQc));
		}
		catch (Throwable t) {
			t.printStackTrace();
			throw new WebApplicationException(t);
		}
		return "OK";
	}

	
	/**
	 * Handler of each notification.
	 */
	private static class NotificationHandler implements Runnable
	{
		private String account;
		private String asset;
		private String avgQc;
		
		public NotificationHandler(String account,
								   String asset,
								   String avgQc) {
			this.account = account;
			this.asset   = asset;
			this.avgQc   = avgQc;
		}
		
		@Override
		public void run() 
		{
		    try {
    			//
    			// Example:
    			//
    			// Verify whether the t4 button was pressed in the last minute.
    			// If so, ignore this event, otherwise send a notification to the device.
    			//
    
    		    //
    	        // SSL configuration
    	        SSLContext ctx = SSLContext.getInstance("SSL");
    	        ctx.init(null, null, null);
    
    		    //
    	        // Client Config
    	        ClientConfig config = new com.sun.jersey.api.client.config.DefaultClientConfig();
    	        config.getProperties().put(com.sun.jersey.client.urlconnection.HTTPSProperties.PROPERTY_HTTPS_PROPERTIES, new com.sun.jersey.client.urlconnection.HTTPSProperties(getHostnameVerifier(), ctx));
    	        Client apiClient = Client.create(config);
    	        apiClient.addFilter(new com.sun.jersey.api.client.filter.HTTPBasicAuthFilter(USERNAME, PASSWORD));
    			WebResource edcApis = apiClient.resource(API_URL);
    	        
    			// find if t4 was pressed in the last minute
    			WebResource t4Msgs  = edcApis.path("messages").path("searchByMetric.xml");
    	        t4Msgs = t4Msgs.queryParam("topic", "devkit/+/us/kansas/stilwell").queryParam("metric", "c3").queryParam("type", "int").queryParam("min", "130").queryParam("max", "180");
    	        MessagesResult result = t4Msgs.get(MessagesResult.class);
    
    	        s_logger.info(">> EdcRuleNotification.notify. result.size: "+result.getMessages().size());
    
    	        
    	        // if t4 was pressed, use the REST APIs to send a message to the device
    	        if (result.getMessages().size() > 0) {
    
    	        	// build a payload to 
    	            List<EdcMetric> metrics = new ArrayList<EdcMetric>();	            
    	            EdcMetric metric = new EdcMetric();
    	            metric.setName("light");
    	            metric.setType("string");
    	            metric.setValue("true");
    	            metrics.add(metric);
    	            
    	            EdcMetricsMapType metricsMap = new EdcMetricsMapType();
    	            metricsMap.setMetrics(metrics);
    	            
    	            EdcPayload payload = new EdcPayload();
    	            payload.setMetrics(metricsMap);
    	            
    	            EdcMessage msg = new EdcMessage();
    	            msg.setTopic(account+"/"+asset+"/us/kansas/led/3");
    	            msg.setTimestamp( new Date());
    	            msg.setEdcPayload(payload);
    	            
    	            WebResource publishWeb = edcApis.path("messages").path("publish.xml");
    	            publishWeb.type(MediaType.APPLICATION_XML).accept(MediaType.APPLICATION_XML).post(msg);
    	        }
		    }
		    catch (Exception e) {
		        e.printStackTrace();
		    }
		}
		
	    private HostnameVerifier getHostnameVerifier() {
	        return new HostnameVerifier() {
	            @Override
	            public boolean verify(String hostname, javax.net.ssl.SSLSession sslSession) {
	                return true;
	            }
	        };
	    }
	}
}
