package com.eurotech.edcandroid.gateway;

public class EdcConnectionIntents {

	public static final String EDC_ID = "com.eurotech.edcservice";

	public static final String SUBSCRIBE_INTENT                      = EDC_ID + ".SUBSCRIBE_INTENT";
	public static final String PUBLISH_INTENT                        = EDC_ID + ".PUBLISH_INTENT";
	public static final String CONTROL_PUBLISH_INTENT                = EDC_ID + ".CONTROL_PUBLISH_INTENT";
	public static final String TOPIC_LIST_INTENT                     = EDC_ID + ".TOPIC_LIST_INTENT";
	public static final String TOPIC_LIST_REPLY                      = EDC_ID + ".TOPIC_LIST_REPLY";
	
	public static final String TOPIC_UNSUBSCRIBE_INTENT              = EDC_ID + ".TOPICS_UNSUBSCRIBE_INTENT";

	public static final String RECEIVED_TOPIC                        = EDC_ID + ".RECEIVED_TOPIC";
	public static final String PUBLISHED_ASSET                       = EDC_ID + ".PUBLISHED_ASSET";
	public static final String PUBLISHED_TOPIC                       = EDC_ID + ".PUBLISHED_TOPIC";
	public static final String SUBSCRIBED_TOPICS                     = EDC_ID + ".SUBSCRIBED_TOPICS";
	public static final String SUBSCRIBED_ASSETS                     = EDC_ID + ".SUBSCRIBED_ASSETS";
	public static final String SUBSCRIPTION_LIST                     = EDC_ID + ".SUBSCRIBED_LIST";
	public static final String UNSUBSCRIBED_TOPICS                   = EDC_ID + ".UNSUBSCRIBED_TOPICS";
	public static final String RECEIVED_CONTROL_TOPIC                = EDC_ID + ".RECEIVED_CONTROL_TOPIC";
	
	public static final String STOP_SERVICE_INTENT                   = EDC_ID + ".STOP_SERVICE_INTENT";
	
	public static final String NOTIFICATION_INTENT                   = EDC_ID + ".NOTIFICATION_INTENT";
	public static final String NOTIFICATION_EXTRAS                   = EDC_ID + ".NOTIFICATION_EXTRAS";
	
	public static final String LOGO                                  = EDC_ID + ".LOGO";
	public static final String ALERT                                 = EDC_ID + ".ALERT";
	public static final String TITLE                                 = EDC_ID + ".TITLE";
	public static final String BODY                                  = EDC_ID + ".BODY";
	public static final String FLAGS                                 = EDC_ID + ".FLAGS";
	public static final String CLASS                                 = EDC_ID + ".CLASS";
	
	public static final String LOC_ID = "com.eurotech.application";
	public static final String UNREGISTER_MESSAGE_RECEIVER_INTENT    = LOC_ID + ".UNREGISTER_MESSAGE_RECEIVER_INTENT";
	public static final String REGISTER_MESSAGE_RECEIVER_INTENT      = LOC_ID + ".REGISTER_MESSAGE_RECEIVER_INTENT";
	public static final String TOPIC_INTENT_LIST_TERMINATION         = LOC_ID + ".TOPIC_INTENT_LIST_TERMINATION";
	public static final String EDC_SENSOR_ACTION                     = LOC_ID + ".EDC_SENSOR_ACTION";
	
}