package com.eurotech.cloud.examples;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;


/**
 * This is an example of how to define a RESTful endpoint that can be used as a callback for the Everyware Cloud Rules Engine.
 * The callback can be programmed to respond to GET or POST requests. 
 * This example endpoint will store all the notifications it received in memory and report them 
 * when querying the REST call:
 * http://host:post/edc-rest-endpoint/report
 * 
 * For GET requests, query parameters can be used to send information about the event that triggered the rule.
 * The URL of the REST Rule Action can be parametrized using the names of the tokens selected by the Rule.
 * For example, the URL for a REST Rule Action based on GET HTTP method can be specified as the following:
 * http://host:post/edc-rest-endpoint/notify?topic=$semanticTopic&asset=$asset&int=$int
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
	
	//
	// Thread Pool that will be used to process the notifications as they are coming in.
	// The notifications should be processed asynchronously so that the HTTP Request is
	// released in the fastest possible way.
	private static final int   THREAD_POOL_SIZE = 5;
	private static final ExecutorService s_pool = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
	
	//
	// List of notifications received so far
	private static List<String> s_notifications = new ArrayList<String>();
    
	
    /**
     * Returns the list of notifications received so far.
     * 
     * @param account THe name of the Account for this the Rule was triggered.
     * @param asset The name of the Asset for this the Rule was triggered.
     * @param intValue The average Qc value the reached a certain threshold.
     */
    @GET
    @Path("report")
    @Produces(MediaType.TEXT_PLAIN)
    public String report() 
    {
        if (s_notifications.size() == 0) {
            return "No notifications received";
        }
        
        StringBuilder sb = new StringBuilder();
        for (String s : s_notifications) {
            sb.append(s).append("\n");
        }
        return sb.toString();
    }
    
	
	/**
	 * This is an example of a REST callback using the HTTP GET method.
	 * The account and asset names are supplied as query parameters into the callback.
	 * For a GET callback method with this signature, the URL for a REST Rule Action based should be:
	 * http://host:post/edc-rest-endpoint/notify?topic=$semanticTopic&asset=$asset&int=$int
	 * 
	 * @param account THe name of the Account for this the Rule was triggered.
	 * @param asset The name of the Asset for this the Rule was triggered.
	 * @param intValue The average Qc value the reached a certain threshold.
	 */
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public String notify(@QueryParam("topic")       String topic,
				         @QueryParam("asset")       String asset,
					     @QueryParam("temperature") double temperature) 
	{
		s_logger.info(">> GET - EdcRuleNotification.notify.");
		s_logger.info("topic: "+topic+" asset: "+asset+" temperature: "+temperature);

		//
		// Spawn off a thread to process the incoming notification and return.
		try {
			s_pool.execute( new NotificationHandler("GET", topic, asset, temperature));
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
	 * @param intValue The average Qc value the reached a certain threshold.
	 */
	@POST
    @Produces(MediaType.TEXT_PLAIN)
	public String notify(byte[] payload) 
	{
		s_logger.info(">> POST - EdcRuleNotification.notify.");
		try {

			com.eurotech.cloud.message.EdcPayload edcPayload = null;
			edcPayload = com.eurotech.cloud.message.EdcPayload.buildFromByteArray(payload);
			String       topic = (String)  edcPayload.getMetric("topic");
			String       asset = (String)  edcPayload.getMetric("asset");
			double temperature = (Double)  edcPayload.getMetric("temperature");
			
			s_logger.info("topic: "+topic+" asset: "+asset+" temperature: "+temperature);

			//
			// Spawn off a thread to process the incoming notification and return.
			s_pool.execute( new NotificationHandler("POST", topic, asset, temperature));
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
	    private String method;
		private String topic;
		private String asset;
		private double temperature;
		 
		public NotificationHandler(String method,
		                           String topic,
								   String asset,
								   double temperature) {
            this.method   = method;
			this.topic    = topic;
			this.asset    = asset;
			this.temperature = temperature;
		}
		
		@Override
		public void run() 
		{
			//
			// Example:
			//
			// More complex task will be normally performed here.
	        // You can correlate data received from the notification to other business data 
	        // in your systems, and/or interact with other system in your IT infrastructure. 
			//
	        // In this extremely simple case, we only collect the notification we received.
	        //
	        StringBuilder sb = new StringBuilder();
	        sb.append(new Date())
	          .append(":")
              .append(this.method)
              .append(":")
	          .append(this.topic)
	          .append(":")
	          .append(this.asset)
	          .append(":")
	          .append(temperature);
	        s_notifications.add(sb.toString());
	    }
	}
}
