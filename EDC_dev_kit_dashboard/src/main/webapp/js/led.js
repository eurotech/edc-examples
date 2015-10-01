
function displayLed4(){

	var ledchip="#led4";
	var gotLedValue="";
	var rgbcolor="white";
	var newLedStatus="";
	led4redStatus="";
	led4greenStatus="";
	led4blueStatus="";

	if(redIsOn && (!greenIsOn && !blueIsOn)){
		led4redStatus=!led4redStatus;
		gotLedValue=!gotLedValue;
		rgbcolor="red";
		$(function() {
			$("#led4red").animate({
				backgroundColor: "red",
				color: "red",
				opacity: "0.6",
			}, 10 );
			$("#led4green").animate({
				backgroundColor: "green",
				color: "green",
				opacity: "0.4",
			}, 10 );
			$("#led4blue").animate({
				backgroundColor: "blue",
				color: "blue",
				opacity: "0.4",
			}, 10 );
		});
	} else if(greenIsOn && (!redIsOn && !blueIsOn)){
		led4greenStatus=!led4greenStatus;
		gotLedValue=!gotLedValue;
		rgbcolor="green";
		$(function() {
			$("#led4red").animate({
				backgroundColor: "red",
				color: "red",
				opacity: "0.4",
			}, 10 );
			$("#led4green").animate({
				backgroundColor: "green",
				color: "green",
				opacity: "0.6",
			}, 10 );
			$("#led4blue").animate({
				backgroundColor: "blue",
				color: "blue",
				opacity: "0.4",
			}, 10 );
		});
	} else if(blueIsOn && (!greenIsOn && !redIsOn)){
		led4blueStatus=!led4blueStatus;
		gotLedValue=!gotLedValue;
		rgbcolor="blue";
		$(function() {
			$("#led4red").animate({
				backgroundColor: "red",
				color: "red",
				opacity: "0.4",
			}, 10 );
			$("#led4green").animate({
				backgroundColor: "green",
				color: "green",
				opacity: "0.4",
			}, 10 );
			$("#led4blue").animate({
				backgroundColor: "blue",
				color: "blue",
				opacity: "0.6",
			}, 10 );
		});
	} else if(redIsOn && greenIsOn && !blueIsOn){
		led4redStatus=!led4redStatus;
		led4greenStatus=!led4greenStatus;	  
		gotLedValue=!gotLedValue;
		rgbcolor="yellow";
		$(function() {
			$("#led4red").animate({
				backgroundColor: "red",
				color: "red",
				opacity: "0.6",
			}, 10 );
			$("#led4green").animate({
				backgroundColor: "green",
				color: "green",
				opacity: "0.6",
			}, 10 );
			$("#led4blue").animate({
				backgroundColor: "blue",
				color: "blue",
				opacity: "0.4",
			}, 10 );
		});
	} else if(redIsOn && !greenIsOn && blueIsOn){
		led4redStatus=!led4redStatus;
		led4blueStatus=!led4blueStatus;	  
		gotLedValue=!gotLedValue;
		rgbcolor="purple";
		$(function() {
			$("#led4red").animate({
				backgroundColor: "red",
				color: "red",
				opacity: "0.6",
			}, 10 );
			$("#led4green").animate({
				backgroundColor: "green",
				color: "green",
				opacity: "0.4",
			}, 10 );
			$("#led4blue").animate({
				backgroundColor: "blue",
				color: "blue",
				opacity: "0.6",
			}, 10 );
		});
	} else if(!redIsOn && greenIsOn && blueIsOn){
		led4greenStatus=!led4greenStatus;
		led4blueStatus=!led4blueStatus;	  
		gotLedValue=!gotLedValue;
		rgbcolor="#00CCCC";
		$(function() {
			$("#led4red").animate({
				backgroundColor: "red",
				color: "red",
				opacity: "0.4",
			}, 10 );
			$("#led4green").animate({
				backgroundColor: "green",
				color: "green",
				opacity: "0.6",
			}, 10 );
			$("#led4blue").animate({
				backgroundColor: "blue",
				color: "blue",
				opacity: "0.6",
			}, 10 );
		});
	} else if(redIsOn && greenIsOn && blueIsOn){
		led4redStatus=!led4redStatus;
		led4greenStatus=!led4greenStatus;
		led4blueStatus=!led4blueStatus;	  
		gotLedValue=!gotLedValue;
		rgbcolor="white";
		$(function() {
			$("#led4red").animate({
				backgroundColor: "red",
				color: "red",
				opacity: "0.6",
			}, 10 );
			$("#led4green").animate({
				backgroundColor: "green",
				color: "green",
				opacity: "0.6",
			}, 10 );
			$("#led4blue").animate({
				backgroundColor: "blue",
				color: "blue",
				opacity: "0.6",
			}, 10 );
		});
	} else {
		led4redStatus="";
		led4greenStatus="";
		led4blueStatus="";
		$(function() {
			$("#led4red").animate({
				backgroundColor: "red",
				color: "red",
				opacity: "0.4",
			}, 10 );
			$("#led4green").animate({
				backgroundColor: "green",
				color: "green",
				opacity: "0.4",
			}, 10 );
			$("#led4blue").animate({
				backgroundColor: "blue",
				color: "blue",
				opacity: "0.4",
			}, 10 );
		});
	}


	if(gotLedValue){
		newLedStatus=!newLedStatus;	
		$(function() {
			$(ledchip).animate({
				backgroundColor: rgbcolor,
				color: "red",
				opacity: "1",
			}, 10 );
		});
	} else {
		newLedStatus="";
		$(function() {
			$(ledchip).animate({
				backgroundColor: "red",
				color: rgbcolor,
				opacity: "0.1",
			}, 10 );
		});
	}
	led4status=newLedStatus;
} //end of displayLed4

function displayToggleStatus(toggleID,gotToggleValue){
	if(toggleID == '4'){
		var togglechip="#toggle4"
	}	else if (toggleID == '5') {
		var togglechip="#toggle5"
	} else if (toggleID == '6') {
		var togglechip="#toggle6"
	}

	if(gotToggleValue){
		$(function() {
			$(togglechip).animate({
				backgroundColor: "green",
				color: "red",
				opacity: "0.4",
			}, 10 );
		});
	} else {
		$(function() {
			$(togglechip).animate({
				backgroundColor: "red",
				color: "green",
				opacity: "0.4",
			}, 10 );
		});
	}
} //end of displayLedStatus

function displayLedStatus(ledID,gotLedValue,ledNcolor){
	if(ledID=='1'){
		var ledchip="#led1";
	}	else if (ledID=='2') {
		var ledchip="#led2";
	} else if (ledID=='3') {
		var ledchip="#led3";
	} else if (ledID=='4') {
		var ledchip="#led4";
	}


	if(gotLedValue){
		$(function() {
			newLedStatus="";
			newLedStatus=!newLedStatus;
		
			$(ledchip).animate({
				backgroundColor: ledNcolor,
				color: "red",
				opacity: "1",
			}, 10 );
		});
	} else {			
		newLedStatus="";
		$(function() {
			$(ledchip).animate({
				backgroundColor: "red",
				color: ledNcolor,
				opacity: "0.1",
			}, 10 );
		});
	}

	if(ledID=='1'){
		led1status=newLedStatus;
	}	else if (ledID=='2') {
		led2status=newLedStatus;
	}  else if (ledID=='3') {
		led3status=newLedStatus;
	}  else if (ledID=='4') {
		led4status=newLedStatus;
	}
} //end of displayLedStatus
