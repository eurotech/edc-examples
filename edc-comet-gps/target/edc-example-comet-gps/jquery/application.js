var subSocket;
var socket = $.atmosphere;
function onSubmit () {
    "use strict";

    var content = $('#content');
    var submit  = $("input#subscribe").attr("value");
    if (submit == 'Unsubscribe') {
    	socket.unsubscribe();
    	$("input#subscribe").attr("value", "Subscribe");
    	content.html($('<p>', { text: 'Disconnected' }));
    	return;
    }
    
    var input = $('#input');
    var status = $('#status');
    var myName = false;
    var author = null;
    var logged = false;
    var url      = $("input#url").attr("value");
    var username = $("input#username").attr("value");
    var password = $("input#password").attr("value");
    var auth     = make_base_auth(username, password);
    var topics   = $("input#topic").attr("value");
    var topicQuery  = '';
    var topicsArray = topics.split(',');
    for (var t in topicsArray) {
    	topicQuery = topicQuery + 'topic='+topicsArray[t]+'&';
    }
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
	        //$('div#messages').prepend("<pre>"+msg+"</pre>");
		alert(msg);
    	}
    };

    
    request.onError = function(response) {
        content.html($('<p>', { text: 'Sorry, but there\'s some problem with your '
            + 'socket or the server is down' + response}));
    };

    
    var subSocket = socket.subscribe(request);
    $("input#subscribe").attr("value", "Unsubscribe");
    
    

    function make_base_auth(user, password) {
    	  var tok = user + ':' + password;
    	  var hash = Base64.encode(tok);
    	  return "Basic " + hash;
    }
}


function onUnload () {
	socket.unsubscribe();
}


