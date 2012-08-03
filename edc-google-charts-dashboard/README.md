# Everyware Device Cloud (EDC): Google Chart Tools Dashboard v2.0.0


## Description
[Google Chart Tools](https://developers.google.com/chart/) is a JavaScript framework to plot data into rich and interactive charts displayed on a web page.
The Everyware Device Cloud (EDC) platform comes with native support for the [Google Chart Tools Datasource Protocol](https://developers.google.com/chart/interactive/docs/dev/implementing_data_source).
As such, you can securely use your EDC account as a data source for Google Charts and build rich and powerful dashboards with only simple JavaScript and HTML.
This tutorial will walk you through a sample dashboard that was built to visualize the data published by the sample [edc-java-client](https://github.com/eurotech/edc-examples/tree/master/edc-java-client).


## Pre-requisites
Please complete the following steps before starting this tutorial:

1. Make sure to have an EDC account and to have completed the [edc-java-client](https://github.com/eurotech/edc-examples/tree/master/edc-java-client) example so some data is published under your account.
2. Familiarize yourself with the [Google Chart Tools](https://developers.google.com/chart/) by browsing the [chart gallery](https://developers.google.com/chart/interactive/docs/gallery) or experimenting with the [playground](http://code.google.com/apis/ajax/playground/?type=visualization).


## Quick Start
Open the `dashboard.html` file under `src/main/webapp` in your favorite programming editor - you can use eclipse if this is what you accustomed to.
Following the data model used in the `edc-java-client` example, `dashboard.html` assumes that your data is published under the `sample/data` topic and it contains metrics named `int, dbl, flt`.
Change the following lines of code to use your EDC username and password.

    // Issue the first login call into the platform
    var dsUrl    = "https://api-sandbox.everyware-cloud.com/gvds";
    var username = "my_username";
    var password = "my_password";

That should be it!
Now open `dashboard.html` with the Firefox or Safari browser and enjoy your dashboard.

Note: if you use the Chrome browser and you open the `dashboard.html` as a file, 
you would need to [disable the security policy on same origin](http://stackoverflow.com/questions/3102819/chrome-disable-same-origin-policy) 
to make the dashboard work.

![screenshot](https://github.com/eurotech/edc/raw/release-2.0.0/examples/edc-google-charts-dashboard/doc/screenshot.jpg)


## Plotting a Chart
In this section, we will walk through the JavaScript code required to plot one of the charts in the dashboard.

We will start from the [JQuery AJAX call](http://api.jquery.com/jQuery.ajax/) which logs into the EDC platform.
It performs HTTP Basic Authentication on the HTTPS-protected URL of the EDC Datasource for Google Chart Tools.
Upon successful authentication, a session is established and all the follow-up requests from the Google Chart Tools library will be trusted.
This is a mandatory step before you can proceed further and you always need to start your dashboard code with the login call.

    $.ajax({
        type: "GET",
        url:  dsUrl+"/login",
        xhrFields: {
            withCredentials: true
        },          
        beforeSend : function(req) {
            req.setRequestHeader('Authorization', "Basic " +Base64.encode(username+":"+password))
        }
    }).done(function( msg ) { ... }

A corresponding `dsUrl+"/logout"` AJAX call is available to close the server-side session and invalidate the associated cookie.
This example does not include a logout call but it is certainly advised to address this aspect before deploying this in production.

In the next step, we will look at the implementation of the `done()` callback function which is responsible for the plotting of the charts.
We will use the line chat at the top of the page as an example.

        // LineChart
        var wrapper = new google.visualization.ChartWrapper({
            chartType: 'LineChart',
            dataSourceUrl: dsUrl+'?topic='+encodeURIComponent(username+'/+/sample/#'),
            query: "SELECT receivedOn, dbl, flt format receivedOn 'MMM dd, yyyy HH:mm:ss.SSS'",
            refreshInterval: 60,
            options: {
                curveType: 'function',
                legend: {position: 'top'},
                hAxis: {title: 'Time', baseline: 'continuous', format: 'HH:mm:ss.SSS', gridlines: {count: 10}},
                pointSize: 3,
                height: 200
            },
            containerId: 'chart_line'
        });
        wrapper.draw()

In this example, we use the [google.visualization.ChartWrapper](https://developers.google.com/chart/interactive/docs/reference#chartwrapperobject)
class to plot the graph. In the first field `chartType`, we defined that we want for a [LineChart](https://developers.google.com/chart/interactive/docs/gallery/linechart).

The second field is the URL of the EDC Data Source that will be used by the Google Chart Library to fetch the data to be displayed.
The EDC Data Source based URL is: https://api-sandbox.everyware-cloud.com/gvds and it takes the following query parameters:

* `topic`: Mandatory parameter with the topic for which messages are requested. The value of the topic parameter should be URL encoded.
* `limit`: Maximum number of entries to be returned. 
* `offset`: Starting offset for the entries to be returned. 
* `startDate`: Start date of the date range requested. The parameter is expressed as a long counting the number of milliseconds since January 1, 1970, 00:00:00 GMT. The default value of 0 means no start date.
* `endDate`: End date of the date range requested. The parameter is expressed as a long counting the number of milliseconds since January 1, 1970, 00:00:00 GMT. The default value of 0 means no end date.

The data returned by the `dataSourceUrl` will contain the following fields for each `EdcMessage` published under the specified topic and in the specified date range.

* account: account used to publish the MQTT message
* asset: identifier of the asset publishing the MQTT message
* receivedOn: timestamp added by the server upon MQTT message reception
* sentOn: timestamp added by the client upon MQTT message transmission
* semanticTopic: the semantic part of the topic to which the messages was published to (e.g. “sample/data”)
* position: holds location-related properties and has the following sub-fields: latitude, longitude, altitude, precision, heading, speed, timestamp, satellites, status
* metrics: for each metric contained in the EdcMessage, a field will be available. (e.g. `int`, `dlb`, `flt` in the case of the `edc-java-client`)

The data returned by the EDC Data Source is then further processed using the query expressed in the `query` parameter.
The query is expressed using the Google Chart Tools [Query Language](https://developers.google.com/chart/interactive/docs/querylanguage) which is a SQL-like language allowing to do basic filtering and grouping of the data returned by the data source.
Each of aforementioned fields returned by the EDC Data Source can be referenced in the query.
It is important that the data returned by the query is formatted according the data format requirements of the chart type that you are about to draw.
For example, the [data format for a LineChart](https://developers.google.com/chart/interactive/docs/gallery/linechart#Data_Format) requires the first column to contain X-axis values (e.g. `receivedOn` in this case) and the following columns to contain the Y-axis values for each data series to be plotted (e.g. `dbl, flt` in this example).
The data format slightly varies for each chart type, so please refer to the chart documentation to craft the appropriate query on the data source.

The following `refreshInterval` determines how frequently in seconds to requery the datasource for updated data.

The `options` object parameter is specific to each chart type and controls the visualization of the chart.

Finally, the `containerId` parameter specifies the `id` of the `div` element that will contain the generated graph.


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

