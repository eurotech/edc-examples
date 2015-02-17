var subSocket;
var socket = $.atmosphere;
function getStreams () {
    "use strict";

    var content = $('#content_stream');
    var submit  = $("input#getstreams").attr("value");
    if (submit == 'Stop') {
    	socket.unsubscribe();
    	$("input#getstreams").attr("value", "Get Streams");
    	content.html($('<p>', { text: 'Disconnected' }));
    	return;
    }
    
    var input = $('#input');
    var status = $('#status');
    var myName = false;
    var author = null;
    var logged = false;
    var url      = $("input#url").attr("value");
    var url    = 'https://api-sandbox.everyware-cloud.com/v2/streams/subscribe.json'
    var username = $("input#username").attr("value");
    var password = $("input#password").attr("value");
    var auth     = make_base_auth(username, password);  
    var gettopic = $("input#getTopic").attr("value");
    var clientID   = $("input#clientID").attr("value");
     
//    var topicQuery  = 'topic='+ username +'/%2B/%23&';
    var topicQuery  = 'topic='+ username +"/"+clientID+"/"+gettopic+'&';
      
      
    var request = { url: url+'?'+topicQuery,

    				enableXDR: true,
    				withCredentials: true,    				
    				dropAtmosphereHeaders: false,
    				attachHeadersAsQueryString: false,
    				executeCallbackBeforeReconnect: false,
    				headers: {'Authorization':auth},
    				logLevel : 'debug',
                    transport : 'long-polling' ,
                    fallbackTransport: 'streaming'};



    request.onOpen = function(response) {
        content.html($('<p>', { text: 'Atmosphere connected using ' + response.transport }));
    };

    
    request.onMessage = function (response) {        
    	var message = response.responseBody;
    	if (message.length != 0) {
	        try {        	
	            var jsonMessage = jQuery.parseJSON(message);
	        } catch (e) {
	            console.log('This doesn\'t look like a valid JSON: ', message.data);
	            return;
	        }
	        var msg = jsonMessage.receivedOn+"-"+jsonMessage.topic+"\n";
	        for (var metricIdx in jsonMessage.payload.metrics.metric) {
	        	var metric = jsonMessage.payload.metrics.metric[metricIdx];
	        	msg = msg+"  "+metric.name+" "+metric.type+" "+metric.value+"\n"; 
	        }	        
	        $('div#messages_stream').prepend("<pre>"+msg+"</pre>");
    	}
    };

    
    request.onError = function(response) {
        content.html($('<p>', { text: 'Sorry, but there\'s some problem with your '
            + 'socket or the server is down' }));
    };

    
    var subSocket = socket.subscribe(request);
    $("input#getstreams").attr("value", "Stop");
    

    function make_base_auth(user, password) {
    	  var tok = user + ':' + password;
    	  var hash = Base64.encode(tok);
    	  return "Basic " + hash;
    }
}


function onUnload () {
	socket.unsubscribe();
}


