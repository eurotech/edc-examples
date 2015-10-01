function makeXML(username,clientID,publishTopic,name,lightValue,ledID){
	var XMLData0;
	XMLData0 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
		"<message xmlns=\"http://eurotech.com/edc/2.0\">" +
		"<topic>"+ publishTopic + "/" + ledID + "</topic>" +
		"<payload>" +
		"<metrics>" +
		"<metric>" +
		"<name>" + name +"</name>" +
		"<type>String</type>" +
		"<value>" + lightValue + "</value>" +
		"</metric>" +
		"</metrics>" +
		"</payload>" +
		"</message>";
	console.log('makeXML: ' + XMLData0);

	if(ledID=='1'){
		XMLData1=XMLData0;
	}	else if (ledID=='2') {
		XMLData2=XMLData0;
	}  else if (ledID=='3') {
		XMLData3=XMLData0;
	} else if (ledID=='4') {
		XMLData4=XMLData0;
	}
}

function makeResetXML(username,clientID,resetTopic,resetMetric,resetValue,counterID){	
	var XMLData0;
	XMLData0 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
		"<message xmlns=\"http://eurotech.com/edc/2.0\">" +
		"<receivedOn>2012-07-19T14:22:00.774Z</receivedOn>" +
		"<topic>"+ "$EDC/" + username + "/" + clientID + "/" + resetTopic + "/" + counterID + "</topic>" +
		"<payload>" +
		"<metrics>" +
		"<metric>" +
		"<name>" + resetMetric +"</name>" +
		"<type>String</type>" +
		"<value>" + resetValue + "</value>" +
		"</metric>" +
		"</metrics>" +
		"<sentOn>2012-07-19T14:22:10.774Z</sentOn>" +
		"</payload>" +
		"</message>";
	XMLReset=XMLData0;
}
