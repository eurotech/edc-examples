+----------------------------------------------------------------+
|                                                                |
| Everyware Device Cloud (EDC): Android Application v.1.0.0      |
|                                                                |
+----------------------------------------------------------------+

+-------------+
| Description |
+-------------+

The Edc Android application demonstrates the use of the Edc platform from a Android smartphone.
The user, after registering to e broker, can publish, control-publish, auto-publish, and receive
messages directly on the smartphone.
The application apk can be found inside ./releases/<ver.>


The Applicaation allows the following actions, corresponding to the indicated icons shown on its
main page:
 
0. Manage configuration [Overflow (3-dots) menu (top right) and menu button (depending on devices)]
1. Publish messages     ["Publish" icon]
2. Receive messages     ["Receive" icon]
3. Send commands        ["Command" icon]
4. Manage topics        ["Topics" icon]
5. Notification         [Status Bar]


+-------------------------+
| 0. Manage configuration |
+-------------------------+
From the Overflow menu (obtained clicking the 3-dots menu at the top right of the main window, or the
menu button of some devices), the user can select:

        +--------+-------------+
        | 0.1    | Settings    |
        +--------+-------------+
                 +----------+------------------+
                 | 0.1.1    | Cloud Connection |
                 +----------+------------------+
                              Here, the connection parameters namely Username, Password, broker address and port,
                              Account Name e Device Name (i.e. assed ID), can be specified, and the connection
                              enabled or disabled.

                 +----------+------------------+
                 | 0.1.2    | Auto-Publish     |
                 +----------+------------------+
                              Here, the auto-publish frequency time, and the device sensor values to be published,
                              are selected, and the whole feature enabled or disabled

                 +----------+------------------+
                 | 0.1.3    | Command          |
                 +----------+------------------+
                              Here, the commands for remote interaction (through control-messages) that are enabled,
                              as well the whole feature enabling, can be specified.

        +--------+-------------+
        | 0.2    | Dock        |
        +--------+-------------+
                   This selection stops the application, still keeping the Edc Service alive (if already started).

        +--------+-------------+
        | 0.3    | Stop & Exit |
        +--------+-------------+
                   This selection stops the application and the Edc Service altogether.

        +--------+-------------+
        | 0.4    | About       |
        +--------+-------------+
                   This selection gives some info about the application, namely release and build date.


+-------------------------+
| 1. Publish              |
+-------------------------+
Clicking the "Publish" icon, a pop-up menu shows, where the user can specify the lists of assets and of topics, namely the device IDs
and topics under which the message will be published, and the message body. The specified message can be sent or canceled clicking
the corresponding button.

+-------------------------+
| 2. Receive              |
+-------------------------+
Clicking the "Receive" icon, a window shows up, where the messages corresponding to the topics that the user has subscribed to
(see following Section "4. Topics") are displayed as they arrive.

+-------------------------+
| 3. Command              |
+-------------------------+
Clicking the "Command" icon, a pop-up menu shows, where the user can specify the lists of assets and of topics, namely the device IDs
and topics under which the message will be published, and the message body. The specified message can be sent or canceled clicking
the corresponding button.

+-------------------------+
| 4. Topics               |
+-------------------------+
Clicking the "Topics" icon, a windows appears. Here, the user can subscribe to specific topics clicking the "Subscribe" button at the
bottom of the screen and filling up the subsequent pop-up dialog, where a list of topics can be introduced. In addition, if some
topics have been subscribed, their list appears inside the window, allowing the user to select and unsubscribe them individually,
or globally if preferred.

+-------------------------+
| 5. Notifications        |
+-------------------------+
The application Status Bar hosts a variety of messages, whose text is displayed and kept for consultation.
The notifications are also tagged with a small Eurotech Cloud icon, whose color (green, yellow and red) synthesizes the severity
level of the notification itself.
