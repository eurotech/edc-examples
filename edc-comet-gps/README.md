# Everyware Device Cloud (EDC): GPS Comet Example utilizing Google Chart Tools v2.0.2


## Description
[Google Chart Tools](https://developers.google.com/chart/) is a JavaScript framework to plot data into rich and interactive charts displayed on a web page.
The Everyware Device Cloud (EDC) platform comes with native support for the [Google Chart Tools Datasource Protocol](https://developers.google.com/chart/interactive/docs/dev/implementing_data_source).
As such, you can securely use your EDC account as a data source for Google Charts and build rich and powerful dashboards with only simple JavaScript and HTML.
This tutorial will walk you through a sample dashboard that was built to visualize the data published by the sample [edc-java-client-gps](https://github.com/eurotech/edc-examples/tree/master/edc-java-client-gps).


## Pre-requisites
Please complete the following steps before starting this tutorial:

1. Make sure to have an EDC account and to have completed the [edc-java-client-gps](https://github.com/eurotech/edc-examples/tree/master/edc-java-client-gps) example so some data is published under your account.  You will also probably want to have this publishing when you view the dashboard so you can see the data updated in real time.
2. Familiarize yourself with the [Google Chart Tools](https://developers.google.com/chart/) by browsing the [chart gallery](https://developers.google.com/chart/interactive/docs/gallery) or experimenting with the [playground](http://code.google.com/apis/ajax/playground/?type=visualization).


## Quick Start
Open `index.html` with the Firefox or Safari browser and enjoy your dashboard.  You will need to enter your account name, user name, password, and the specific 'asset' you want to track.

![screenshot](https://github.com/eurotech/edc-examples/blob/master/edc-comet-gps/doc/screenshot.jpg)


## Next Steps
There are certainly more elaborated processing of the data that can be performed and that are not illustrated in this example.
We encourage you to explore the Google Chart Tools documentation for [Data Views](https://developers.google.com/chart/interactive/docs/reference#DataView) 
and the [Data Manipulation Methods](https://developers.google.com/chart/interactive/docs/reference#google_visualization_data)

Moreover, Google Charts can be published as Gadgets into the iGoogle portal pages.
You can find a tutorial for that process here.
https://developers.google.com/chart/interactive/docs/dev/gadgets

Finally, you are encouraged to package this example into a proper WEB application before deploying it into production.
Better handing of the login and logout aspects is appropriate. A suggestion can be to convert this example into a JSP-page
and package it as J2EE war file that can be deployed into an application container like tomcat.
This example is kept simple on purpose as it focuses on the plotting of a charts using EDC as a Datasource.

