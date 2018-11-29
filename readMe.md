# Messenger Thing
Networking class project to build a chat messenger, UDP style. (no TCP or anything else fancy). This chat messenger mimics Address Resolution Protocol ([ARP](https://en.wikipedia.org/wiki/Address_Resolution_Protocol "Wikipedia ARP")) by broadcasting a 'Name Request' to the network in attempt to receive unicast reply with a 'Name Response' Once the 'Name Response' is received, our chat messenger can unicast a message to the responding user. The application assumes all users attempting communicate are on port 64000.


The 'Name Request' format: *????? OtherPerson ##### Me*

The 'Name Response' format: *##### OtherPerson ##### 123.123.123.123*

---

## Query Window
![Image of QueryWindow](https://github.com/xCito/InstantMessenger/blob/broadcastFeature/Screenshots/ssQueryWindow2.png)

This is the first window that will appear. This window will query the user for a nickname/username be known as on a network.

---

## Menu/Conversation Starter
![Image of Convo Starter](https://github.com/xCito/InstantMessenger/blob/broadcastFeature/Screenshots/ssBroadcasterWindow.png)

This is the second window that will appear. This window displays the user's chosen nickname/username, current IP address on their network, and the port this Messenger Thing application is running on.
The user enter's the username of another user (or their own) to start chatting with them.

---

## The Chat Window
![Image of Chat Window](https://github.com/xCito/InstantMessenger/blob/broadcastFeature/Screenshots/ssChatWindow2.png)

This window will appear when the this application receives a 'Name Request' or 'Name Response' from another user. In the case of when an incoming message is not from a user who has sent a 'Name Request' or 'Name Response', a new chat window will open as well, but will be labeled unknown. 
 