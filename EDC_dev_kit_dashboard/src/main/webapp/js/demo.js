	var led1status="";
	var led2status="";
	var led3status="";
	var led4status="";
	var led4redStatus="";
	var led4greenStatus="";
	var led4blueStatus="";
	var clickLed1="";
	var clickLed2="";
	var clickLed3="";
	var clickLed4="";
	var clickLed4red="";
	var clickLed4green="";
	var clickLed4blue="";
	window.led3String = ""
	window.c3String = ""
	window.t5String="";
	window.t6String="";
	window.qcString = ""
	var XMLData1="";
	var XMLData2="";
	var XMLData3="";
	var XMLData4="";
	var redIsOn="";
	var greenIsOn="";
	var blueIsOn="";
	var readRed="";
	var readGreen="";
	var readBlue="";
	var XMLrule1="";
	var XMLrule2="";
	var XMLReset="";
	var apiURL="https://api-sandbox.everyware-cloud.com/v2/messages/searchByTopic.xml?topic=";
	var publishURL="https://api-sandbox.everyware-cloud.com/v2/messages/publish.xml";
	var logoutURL= "https://api-sandbox.everyware-cloud.com/gvds/logout";


function doDemo (username,password,clientID,postTopic,getTopic) {

	$(document).ready(function(){
		setInterval(function() {
			readRed="";
			readGreen="";
			readBlue="";
			var url = apiURL+username+"/"+clientID+"/"+getTopic;
			function make_base_auth(user, password) {
				var tok = user + ':' + password;
				var hash = btoa(tok);
				return "Basic " + hash;
			}

			$.ajax({
				type: "GET",
				url: url,
				dataType: 'xml',
				async : true,
				data: '{}',
				beforeSend: function (xhr){
					xhr.setRequestHeader('Authorization', make_base_auth(username, password));
				},
				success: function(xml){
					$(xml).find('receivedOn').first().each(function(){
						var sTime = $(this).text();
					});
					$(xml).find('metrics').first().each(function(){
						$(this).find('metric').each(function(){
							var sName = $(this).find('name').text();
							var sType = $(this).find('type').text();
							var sValue = $(this).find('value').text();

							if(sName=='c3'){
								window.c3String=$(this).find('value').text();

								$('div.c3-text').text(window.c3String).css({'background-color' : 'none', 'font-weight' : 'bolder', 'position': 'absolute',
									'left': '205px', 'top': '330px', 'color': 'white', 'font-family': 'tahoma', 'font-size': '11pt', 'padding-top': '19px', 'padding-bottom': '19px',
									'padding-right': '13px', 'padding-left': '13px'});
							}

							if(sName=='qc'){
								window.qcString=$(this).find('value').text();
								$('div.qc-text').text(window.qcString).css({'background-color' : 'none', 'font-weight' : 'bolder', 'position': 'absolute',
									'left': '555px', 'top': '331px', 'color': 'white', 'font-family': 'tahoma', 'font-size': '11pt', 'padding-top': '19px', 'padding-bottom': '19px',
									'padding-right': '20px', 'padding-left': '20px'});
							}

							if(sName=='LED1'){
								window.led1String=$(this).find('value').text();
								var ledID="1";
								var led1color="gold";
								var gotLed1Value="";
								var ledstatus=led1status;

								if(sValue=='true'){
									gotLed1Value=!gotLed1Value;
								}
								if(!clickLed1){
									displayLedStatus(ledID,gotLed1Value,led1color);
								}
								clickLed1="";
							}

							if(sName=='LED2'){
								window.led2String=$(this).find('value').text();
								var ledID="2";
								var led2color="gold";
								var gotLed2Value="";
								var ledstatus=led2status;

								if(sValue=='true'){
									gotLed2Value=!gotLed2Value;
								}
								if(!clickLed2){
									displayLedStatus(ledID,gotLed2Value,led2color);
								}
								clickLed2="";
							}

							if(sName=='LED3'){
								window.led3String=$(this).find('value').text();
								var ledID="3";
								var led3color="gold";
								var gotLed3Value="";
								var ledstatus=led3status;

								if(sValue=='true'){
									gotLed3Value=!gotLed3Value;
								}
								if(!clickLed3){
									displayLedStatus(ledID,gotLed3Value,led3color);
								}
								clickLed3="";
							}

							if(sName=='LED4red' || sName=='LED4green' || sName=='LED4blue') {

								if(sName=='LED4red'){
									readRed=!readRed;
									window.led4rString=$(this).find('value').text();
									redIsOn="";
									var ledID="4"
									var gotLed4Value="";
									var led4color="red";
									if(sValue=='true'){
										gotLed4Value=!gotLed4Value;
										redIsOn=!redIsOn;
									}
								}

								if(sName=='LED4green'){
									readGreen=!readGreen;
									window.led4gString=$(this).find('value').text();
									greenIsOn="";
									var ledID="4"
									var gotLed4Value="";
									var led4color="green";
									if(sValue=='true'){
										gotLed4Value=!gotLed4Value;
										greenIsOn=!greenIsOn;
									}
								}

								if(sName=='LED4blue'){
									readBlue=!readBlue;
									window.led4gString=$(this).find('value').text();
									blueIsOn="";
									var ledID="4"
									var gotLed4Value="";
									var led4color="blue";
									if(sValue=='true'){
										gotLed4Value=!gotLed4Value;
										blueIsOn=!blueIsOn;
									}
								}
								if(readRed && readGreen && readBlue) {
									if(!clickLed4){
										displayLed4();
									}
									clickLed4="";
								}
							}

							if(sName=='t4'){
								window.t4String=$(this).find('value').text();
								var toggleID="4"
								var gotToggle4Value="";

								if(sValue=='true'){
									gotToggle4Value=!gotToggle4Value;
								}
								displayToggleStatus(toggleID,gotToggle4Value);
							}

							if(sName=='t5'){
								window.t5String=$(this).find('value').text();
								var toggleID="5"
								var gotToggle5Value="";


								if(sValue=='true'){
									gotToggle5Value=!gotToggle5Value;
								}
								displayToggleStatus(toggleID,gotToggle5Value);
							}

							if(sName=='t6'){
								window.t6String=$(this).find('value').text();
								var toggleID="6"
								var gotToggle6Value="";

								if(sValue=='true'){
									gotToggle6Value=!gotToggle6Value;
								}
								displayToggleStatus(toggleID,gotToggle6Value);
							}


						});
					});
				},error: function() {
					alert("An error occurred while processing XML file.");
				}
			});
		}, 1000);
	});

	//LED 1
	$(document).ready(function() {
		$("#led1").click(function() {
			clickLed1=!clickLed1;
			var light="false";
			var opacity="0.1";
			background_color="gold";
			if (!led1status){
				opacity="1";
				light = "true";
			}

			led1status=!led1status;
			$(this).css({'background-color':background_color,'opacity':opacity});
			var url = publishURL;
			function make_base_auth(user, password) {
				var tok = user + ':' + password;
				var hash = btoa(tok);
				return "Basic " + hash;
			}

			var ledID="1";
			var ledName="light";
			var publishTopic="$EDC/" + username + "/" + clientID + "/" + postTopic;
			makeXML(username,clientID,publishTopic,ledName,light,ledID);

			$.ajax({
				type: "POST",
				url: url,
				contentType: 'application/xml',
				async : true,
				beforeSend: function (xhr){
					xhr.setRequestHeader('Authorization', make_base_auth(username, password));
				},
				processData: true,
				data: XMLData1,

				error: function(XMLHttpRequest, textStatus, errorThrown){
					alert(errorThrown);
				},
				success: function(data, textStatus, XMLHttpRequest){
					//alert("Succeeded");
				}
			});

			var ledID="1";
			var ledName="light";
			var publishTopic=username + "/" + clientID + "/" +postTopic;
			makeXML(username,clientID,publishTopic,ledName,light,ledID);

			$.ajax({
				type: "POST",
				url: url,
				contentType: 'application/xml',
				async : true,
				beforeSend: function (xhr){
					xhr.setRequestHeader('Authorization', make_base_auth(username, password));
				},
				processData: true,
				data: XMLData1,
				error: function(XMLHttpRequest, textStatus, errorThrown){
					alert(errorThrown);
				}, success: function(data, textStatus, XMLHttpRequest){
					//alert("Succeeded");
				}
			});
	   	}); // end of click function
	});

	//LED 2
	$(document).ready(function() {
		$("#led2").click(function() {
			clickLed2=!clickLed2;
			var light="false";
			var opacity="0.1";
			if (!led2status){
				opacity="1";
				light = "true";
			}
			led2status=!led2status;
			color=$(this).css("background-color");
			background_color=$(this).css("color");
			$(this).css({'background-color':background_color,'color':color,'opacity':opacity});

			var url = publishURL;
			function make_base_auth(user, password) {
				var tok = user + ':' + password;
				var hash = btoa(tok);
				return "Basic " + hash;
			}

			var ledID="2";
			var ledName="light";
			var publishTopic="$EDC/" + username + "/" + clientID + "/" + postTopic;
			makeXML(username,clientID,publishTopic,ledName,light,ledID);
			$.ajax({
				type: "POST",
				url: url,
				contentType: 'application/xml',
				async : true,
				beforeSend: function (xhr){
					xhr.setRequestHeader('Authorization', make_base_auth(username, password));
				},
				processData: true,
				data: XMLData2,
				error: function(XMLHttpRequest, textStatus, errorThrown){
					alert(errorThrown);
				}, success: function(data, textStatus, XMLHttpRequest){
					//alert("Succeeded");
				}
			});

			var ledID="2";
			var ledName="light";
			var publishTopic=username + "/" + clientID + "/" + postTopic;
			makeXML(username,clientID,publishTopic,ledName,light,ledID);
			$.ajax({
				type: "POST",
				url: url,
				contentType: 'application/xml',
				async : true,
				beforeSend: function (xhr){
					xhr.setRequestHeader('Authorization', make_base_auth(username, password));
				},
				processData: true,
				data: XMLData2,
				error: function(XMLHttpRequest, textStatus, errorThrown){
					alert(errorThrown);
				}, success: function(data, textStatus, XMLHttpRequest){
					//alert("Succeeded");
				}
			});
	   	}); // end of click function
	});

	//LED 3
	$(document).ready(function() {
		$("#led3").click(function() {
			clickLed3=!clickLed3;
			var light="false";
			var opacity="0.1";

			if (!led3status){
				opacity="1";
				light = "true";
			}
			led3status=!led3status;
			color=$(this).css("background-color");
			background_color=$(this).css("color");
			$(this).css({'background-color':background_color,'color':color,'opacity':opacity});

			var url = publishURL;
			function make_base_auth(user, password) {
				var tok = user + ':' + password;
				var hash = btoa(tok);
				return "Basic " + hash;
			}

			var ledID="3";
			var ledName="light";
			var publishTopic="$EDC/" + username + "/" + clientID + "/" + postTopic;
			makeXML(username,clientID,publishTopic,ledName,light,ledID);
			$.ajax({
				type: "POST",
				url: url,
				contentType: 'application/xml',
				async : true,
				beforeSend: function (xhr){
					xhr.setRequestHeader('Authorization', make_base_auth(username, password));
				},
				processData: true,
				data: XMLData3,
				error: function(XMLHttpRequest, textStatus, errorThrown){
					alert(errorThrown);
				}, success: function(data, textStatus, XMLHttpRequest){
				//alert("Succeeded");
				}
			});

			var ledID="3";
			var ledName="light";
			var publishTopic=username + "/" + clientID + "/" + postTopic;
			makeXML(username,clientID,publishTopic,ledName,light,ledID);
			$.ajax({
				type: "POST",
				url: url,
				contentType: 'application/xml',
				async : true,
				beforeSend: function (xhr){
					xhr.setRequestHeader('Authorization', make_base_auth(username, password));
				},
				processData: true,
				data: XMLData3,
				error: function(XMLHttpRequest, textStatus, errorThrown){
					alert(errorThrown);
				}, success: function(data, textStatus, XMLHttpRequest){
					//alert("Succeeded");
				}
			});
		}); // end of click function
	});

	//LED 4 RED
	$(document).ready(function() {
		$("#led4red").click(function() {
			var ledColor   = "red"
			clickLed4red=!clickLed4red;
			var light="false";
			var opacity="0.4";
			color=$(this).css("background-color");
			background_color=$(this).css("color");

			if (!led4redStatus){
				opacity="1";
				light = "true";
			}

			led4redStatus=!led4redStatus;

			var url = publishURL;
			function make_base_auth(user, password) {
				var tok = user + ':' + password;
				var hash = btoa(tok);
				return "Basic " + hash;
			}

			var ledID="4";
			var ledName=ledColor;
			var publishTopic="$EDC/" + username + "/" + clientID + "/" + postTopic;
			makeXML(username,clientID,publishTopic,ledName,light,ledID);

			$.ajax({
				type: "POST",
				url: url,
				contentType: 'application/xml',
				async : true,
				beforeSend: function (xhr){
					xhr.setRequestHeader('Authorization', make_base_auth(username, password));
				},
				processData: true,
				data: XMLData4,
				error: function(XMLHttpRequest, textStatus, errorThrown){
					alert(errorThrown);
				}, success: function(data, textStatus, XMLHttpRequest){
					//alert("Succeeded");
				}
			});

			var ledID="4";
			var ledName=ledColor;
			var publishTopic=username + "/" + clientID + "/" + postTopic;
			makeXML(username,clientID,publishTopic,ledName,light,ledID);

			$.ajax({
				type: "POST",
				url: url,
				contentType: 'application/xml',
				async : true,
				beforeSend: function (xhr){
					xhr.setRequestHeader('Authorization', make_base_auth(username, password));
				},
				processData: true,
				data: XMLData4,
				error: function(XMLHttpRequest, textStatus, errorThrown){
					alert(errorThrown);
				}, success: function(data, textStatus, XMLHttpRequest){
					//alert("Succeeded");
				}
			});
	   	}); // end of click function
	});


	//LED 4 GREEN
	$(document).ready(function() {
		$("#led4green").click(function() {
			var ledColor   = "green"
			clickLed4green=!clickLed4green;
			var light="false";
			var opacity="0.4";

			color=$(this).css("background-color");
			background_color=$(this).css("color");

			if (!led4greenStatus){
				opacity="1";
				light = "true";
			}
			led4greenStatus=!led4greenStatus;

			var url = publishURL;
			function make_base_auth(user, password) {
				var tok = user + ':' + password;
				var hash = btoa(tok);
				return "Basic " + hash;
			}

			var ledID="4";
			var ledName=ledColor;
			var publishTopic="$EDC/" + username + "/" + clientID + "/" + postTopic;
			makeXML(username,clientID,publishTopic,ledName,light,ledID);

			$.ajax({
				type: "POST",
				url: url,
				contentType: 'application/xml',
				async : true,
				beforeSend: function (xhr){
					xhr.setRequestHeader('Authorization', make_base_auth(username, password));
				},
				processData: true,
				data: XMLData4,
				error: function(XMLHttpRequest, textStatus, errorThrown){
					alert(errorThrown);
				},
				success: function(data, textStatus, XMLHttpRequest){
					//alert("Succeeded");
				}
			});

			var ledID="4";
			var ledName=ledColor;
			var publishTopic=username + "/" + clientID + "/" + postTopic;
			makeXML(username,clientID,publishTopic,ledName,light,ledID);
			$.ajax({
				type: "POST",
				url: url,
				contentType: 'application/xml',
				async : true,
				beforeSend: function (xhr){
					xhr.setRequestHeader('Authorization', make_base_auth(username, password));
				},
				processData: true,
				data: XMLData4,
				error: function(XMLHttpRequest, textStatus, errorThrown){
					alert(errorThrown);
				},
				success: function(data, textStatus, XMLHttpRequest){
					//alert("Succeeded");
				}
			});
		}); // end of click function
	});

	//LED 4 BLUE
	$(document).ready(function() {
		$("#led4blue").click(function() {
			var ledColor   = "blue"
			clickLed4blue=!clickLed4blue;
			var light="false";
			var opacity="0.4";

			color=$(this).css("background-color");
			background_color=$(this).css("color");

			if (!led4blueStatus){
				opacity="1";
				light = "true";
			}

			led4blueStatus=!led4blueStatus;
			var url = publishURL;
			function make_base_auth(user, password) {
				var tok = user + ':' + password;
				var hash = btoa(tok);
				return "Basic " + hash;
			}

			var ledID="4";
			var ledName=ledColor;
			var publishTopic="$EDC/" + username + "/" + clientID + "/" + postTopic;
			makeXML(username,clientID,publishTopic,ledName,light,ledID);

			$.ajax({
				type: "POST",
				url: url,
				contentType: 'application/xml',
				async : true,
				beforeSend: function (xhr){
					xhr.setRequestHeader('Authorization', make_base_auth(username, password));
				},
				processData: true,
				data: XMLData4,
				error: function(XMLHttpRequest, textStatus, errorThrown){
					alert(errorThrown);
				},
				success: function(data, textStatus, XMLHttpRequest){
					//alert("Succeeded");
				}
			});

			var ledID="4";
			var ledName=ledColor;
			var publishTopic=username + "/" + clientID + "/" + postTopic;
			makeXML(username,clientID,publishTopic,ledName,light,ledID);

			$.ajax({
				type: "POST",
				url: url,
				contentType: 'application/xml',
				async : true,
				beforeSend: function (xhr){
					xhr.setRequestHeader('Authorization', make_base_auth(username, password));
				},
				processData: true,
				data: XMLData4,
				error: function(XMLHttpRequest, textStatus, errorThrown){
					alert(errorThrown);
				},
				success: function(data, textStatus, XMLHttpRequest){
					//alert("Succeeded");
				}
			});
	   	}); // end of click function
	});


	//RESET COUNTER C3
	$(document).ready(function() {
		$("#reset_c3").click(function() {
			var url = publishURL;
			function make_base_auth(user, password) {
				var tok = user + ':' + password;
				var hash = btoa(tok);
				return "Basic " + hash;
			}

			var counterID="c3";
			var resetMetric="value";
			var resetValue = "true";
			var resetTopic=postTopic + "/resetcnt";
			makeResetXML(username,clientID,resetTopic,resetMetric,resetValue,counterID);

			$.ajax({
				type: "POST",
				url: url,
				contentType: 'application/xml',
				async : true,
				beforeSend: function (xhr){
					xhr.setRequestHeader('Authorization', make_base_auth(username, password));
				},
				processData: true,
				data: XMLReset,
				error: function(XMLHttpRequest, textStatus, errorThrown){
					alert(errorThrown);
				},
				success: function(data, textStatus, XMLHttpRequest){
					//alert("Succeeded");
				}
			});
		}); // end of click function
	});

	$('#logout').click(function(e){
		e.preventDefault();

		$('<div id="confirmLogout" title="Logout">Are you sure you want to log out?</div>').dialog({
			modal: true,
			height: 120,
			width:350,
			show:"blind",
			hide: "blind",
			buttons:{
				"Yes":function() {
					$.ajax({
						url: logoutURL,
						complete:function() {
							$('#confirmLogout').html("Logged out!");
							$('.ui-dialog-buttonpane').css('display','none');
							var dlg = $("#confirmLogout").parents(".ui-dialog:first");
							dlg.animate({ width: 250},120);
							setTimeout(function(){$("#confirmLogout").dialog("close")},1000);
							setTimeout(function(){location.reload();},1000);
						}
					});
				},
				"No":function() {
					$("#confirmLogout").dialog("close");
				}
			},
			close:function(){
				$("#confirmLogout").remove();
			}
		});
		return false;
	});
}
