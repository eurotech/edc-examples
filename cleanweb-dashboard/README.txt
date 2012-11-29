Quick notes:

0) After login, wait at least 5 seconds that the input status are updated.


1) Toggles Red/Green buttons on the PCB image, PLC inputs tab and Quad Counter tab:

On the dashboard, Toggles Red/Green buttons on the PCB image, PLC inputs tab and Quad Counter tab are updated every 5 seconds. This is so because updates follow a query to EC platform, which is performed using REST APIs with jQuery.AJAX

2) Clicking the LEDs:

LEDs are updated as described at point 1). However, by clicking on the LEDs areas you can publish data to the EDC Dev Kit and turn the LEDs ON/OFF. 
Please, note that the change of color of the LEDs area after a "click" is an instantaneous graphical effect. Colors in LEDs area are also update after a query accordingly to the value of the corresponding metrics.
So if there is a mismatching between the color determined by the clicking and the color determined by the result arriving from a previously instanced query, the color of LEDs in the corresponding areas on the dashboards can vary. 
If this happens, just wait for the next query which will update the colors. 

This aspect should be resolved in the next version which will use data streams.

3) Colored LED (#4):

In order to turn ON the LED 4:
 a) select the color on the dropdown menu which is in the dashboard, just above the LED4 area;
 b) click on the LED4 area

 
In order to turn OFF the LED 4:

 a) on the dropdown menu which is in the dashboard, select the SAME COLOR which is displayed in the LED4 area;
 b) click on the LED4 area

Please, note that you always have to turn off the color that is on before turning on another color.



4) Data Streams:

a) Click on the "Data Streams" tab and click on "Get Streams" 

b) than, generate some data FROM the EDC Dev Kit.

c) Please, note that at the moment the "Data Streams" tab cannot show the data published from the Dashboard to the EDC Dev Kit.