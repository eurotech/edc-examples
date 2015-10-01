google.load('visualization', '1', {'packages':['table']});
// google.load("visualization", "1", {packages:["corechart"]});

// Set a callback to run when the API is loaded.
google.setOnLoadCallback($.init);

$(function init(){
	// use the jquery sortable plugin to give the page a dashboard look and feel
	$( ".portlet" ).addClass( "ui-widget ui-widget-content ui-helper-clearfix ui-corner-all" )
		.find( ".portlet-header" )
		.addClass( "ui-widget-header ui-corner-all" )
		.prepend( "<span class='ui-icon ui-icon-minusthick'></span>")
		.end()
		.find( ".portlet-content" );

	$( ".portlet-header .ui-icon" ).click(function() {
		$( this ).toggleClass( "ui-icon-minusthick" ).toggleClass( "ui-icon-plusthick" );
		$( this ).parents( ".portlet:first" ).find( ".portlet-content" ).toggle();
	});

	$( ".column" ).disableSelection();

	// Tabs
	$('#tabs').tabs();

	// Dialog
	$('#dialog').dialog({
		modal: true,
		width: 300,
		buttons: {
			"Login": function() {
				$(this).dialog("close");
				var dsUrl    = "https://api-sandbox.everyware-cloud.com/gvds";
				var username = $("input#username").attr("value");
				var password = $("input#password").attr("value");
				var clientID   = $("input#clientID").attr("value");
				var getTopic   = $("input#getTopic").attr("value");
				var postTopic   = $("input#postTopic").attr("value");
				$.ajax({
					type: "GET",
					url:  dsUrl+"/login",

			      //Removed to fix IE issue    
			      xhrFields: {
			      	withCredentials: true
			      },          
			      beforeSend : function(req) {
			      	req.setRequestHeader('Authorization', "Basic " +Base64.encode(username+":"+password))
			      }
			  	}).done(function( msg ) {
			  		$("p").html("Welcome, "+username);
					// Table
					var wrapper = new google.visualization.ChartWrapper({
						chartType: 'Table',
						dataSourceUrl: dsUrl+'?topic='+encodeURIComponent(username+"/"+clientID+"/"+getTopic)+'&limit=1',
						query: "SELECT c3 LABEL c3 'C3'",
						refreshInterval: 1,
						containerId: 'chart_table_c3'
					});
					wrapper.draw();
					// Column Chart T4
					var wrapper = new google.visualization.ChartWrapper({
						chartType: 'ColumnChart',
						chartType: 'ScatterChart',
						dataSourceUrl: dsUrl+'?topic='+encodeURIComponent(username+"/"+clientID+"/"+getTopic)+'&limit=1',
						query: "SELECT receivedOn, it4 LABEL it4 'Toggle 4'",
						refreshInterval: 1,
						options: {
							backgroundColor : "none",
							colors:['#FFCC00'],
							vAxis: {viewWindow : {min: 0 ,max: 1}, textStyle : {color: "black"}, baselineColor: "gray", textPosition: "none"},
							hAxis: {textStyle : {color: "gray"}, baselineColor: "gray", textPosition: "none"},
							legend: {position: 'bottom'},
							bar : {groupWidth : '50%'},
							width: 180,
							height: 100
						},
						containerId: 'chart_gauge_t4'
					});
					wrapper.draw();
					// Column Chart T5
					var wrapper = new google.visualization.ChartWrapper({
						chartType: 'ColumnChart',
						dataSourceUrl: dsUrl+'?topic='+encodeURIComponent(username+"/"+clientID+"/"+getTopic)+'&limit=1',
						query: "SELECT receivedOn, it5 LABEL it5 'Toggle 5'",
						refreshInterval: 1,
						options: {
							backgroundColor : "none",
							colors:['#0033FF'],
							vAxis: {viewWindow : {min: 0 ,max: 1}, textStyle : {color: "black"}, baselineColor: "gray", textPosition: "none"},
							hAxis: {textStyle : {color: "gray"}, baselineColor: "gray", textPosition: "none"},
							legend: {position: 'bottom'},
							chartArea : {left: 40},
							bar : {groupWidth : '50%'},
							width: 180,
							height: 100
						},
						containerId: 'chart_gauge_t5'
					});
					wrapper.draw();
					// Column Chart T6
					var wrapper = new google.visualization.ChartWrapper({
						chartType: 'ColumnChart',
						dataSourceUrl: dsUrl+'?topic='+encodeURIComponent(username+"/"+clientID+"/"+getTopic)+'&limit=1',
						query: "SELECT receivedOn, it6 LABEL it6 'Toggle 6'",
						refreshInterval: 1,
						options: {
							backgroundColor : "none",
							colors:['#CC0000'],
							vAxis: {viewWindow : {min: 0 ,max: 1}, textStyle : {color: "black"}, baselineColor: "gray", textPosition: "none"},
							hAxis: {textStyle : {color: "gray"}, baselineColor: "gray", textPosition: "none"},
							legend: {position: 'bottom'},
							chartArea : {left: 50},
							bar : {groupWidth : '50%'},
							width: 180,
							height: 100
						},
						containerId: 'chart_gauge_t6'
					});
					wrapper.draw();
					// Gauge Qc
					var wrapper = new google.visualization.ChartWrapper({
						chartType: 'Gauge',
						dataSourceUrl: dsUrl+'?topic='+encodeURIComponent(username+"/"+clientID+"/"+getTopic)+'&limit=1',
						query: "SELECT qc LABEL qc 'QC'",
						refreshInterval: 1,
						options: {
							min: 0, max: 30,
							minorTicks: 2, 
							greenFrom: 10, greenTo: 20,
							yellowFrom: 20, yellowTo: 25,
							redFrom: 25, redTo: 30,
							height: 170
						},
						containerId: 'chart_gauge_qc'
					});
					wrapper.draw();
					// LineChart			        
					var wrapper = new google.visualization.ChartWrapper({
						chartType: 'LineChart',
						dataSourceUrl: dsUrl+'?topic='+encodeURIComponent(username+"/"+clientID+"/"+getTopic)+'&limit=20',
						query: "SELECT receivedOn, qc format receivedOn 'MMM dd, yyyy HH:mm:ss.SSS'",
						refreshInterval: 1,
						options: {
							curveType: 'function',
							legend: {position: 'top'},
							hAxis: {title: 'Time', format: 'HH:mm:ss', gridlines: {count: 10}},
							pointSize: 1,
							height: 200,
							width: 550,
						},
						containerId: 'chart_line'
					});
					wrapper.draw();
					doDemo(username,password,clientID,postTopic,getTopic);	
				}).fail(function( msg ) {    	
					alert("Failed to login: "+msg.status+" - "+msg.statusText);
				});
			}
		}			
	});
	$(function() {
		$( "#check" ).button();
		$( "#format" ).buttonset();
	});		
}); //end of init function

