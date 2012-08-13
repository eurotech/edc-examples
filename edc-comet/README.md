# Everyware Device Cloud (EDC): Comet Example v2.0.2


## Description
The Everyware Device Cloud (EDC) platform provides support for a Comet streaming API to get published data in real time.
This tutorial will walk you through a simple application to be able to see streaming data that is acquired via the EDC Comet API.


## Pre-requisites
Please complete the following steps before starting this tutorial:

1. Make sure to have an EDC account and to have completed the [edc-java-client](https://github.com/eurotech/edc-examples/tree/master/edc-java-client) example so you are able to publish data to your account in real time


## Quick Start

Open the `index.html` file under `src/main/webapp` in your favorite programming editor - you can use Eclipse if this is what you are accustomed to.
Following the data model used in the `edc-java-client` example, `dashboard.html` assumes that your data is published under the `sample/data` topic and it contains metrics named `int, dbl, flt`.
Change the following lines of code to use your EDC username, password, and account name.  These are listed as 'my_username', 'my_password', and 'my_accountname' respectively below.

    <td><input type="text" id="url" size="60" value="https://api-sandbox.everyware-cloud.com/v2/streams/subscribe.json"/></td>
    <td><input type="text" id="username" size="60" value="my_username"/></td>
    <td><input type="password" id="password" size="60" value="my_password"/></td>
    <td><input type="text" id="topic" size="60" value="my_accountname/%2B/%23"/></td>

That should be it!
Now open `index.html` with the Firefox or Safari browser.  At this point you can click the subscribe button in the browser to see streaming data.  Note after clicking the subscribe button you
should be able to start the edc-java-client to start publishing data.  That data should now show up in your browser window.


## Next Steps
This is a very simple example simply to show the Comet/streaming capabilities of the EDC platform.  You are encouraged to package this example into a proper WEB application before deploying
it into production. 
